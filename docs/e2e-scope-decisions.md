# NexusSMS End-to-End Encryption — Scope Decisions (Phase 0)

**Date:** 2026-07-18
**Status:** DECIDED — all downstream phases build against these choices

---

## Decision 1: Single device per identity (v1)

**Decision:** One device per phone number. No multi-device, no linked devices, no desktop clients.

**Rationale:** Multi-device support requires device-list management, per-device sessions, sync fan-out, and device revocation — each a non-trivial subsystem. NexusSMS is a phone-number-based SMS app; one device per number matches the transport model and the user's existing mental model. Multi-device is explicitly punted to a future version if demand warrants it.

**Impact on implementation:**
- The pre-key server stores exactly one identity key bundle per phone number.
- Registering a new device for the same number replaces the old bundle (old signed prekey + one-time prekeys discarded).
- Session state is 1:1 (one ratchet session per contact, not per device pair).

---

## Decision 2: Trust model — self-hosted, trusted circle (v1)

**Decision:** The pre-key server is self-hosted by each user or shared within a small trusted group. Not a public registration system. Authentication is a per-device API key, not phone-number OTP.

**Rationale:** Matches the app's existing self-hosted patterns (WebDAV backup, Matrix sync). A small service with per-device API-key auth is sufficient for the threat model of "people who already trust each other and each stand up or share infrastructure." Hardening for strangers (rate limiting, abuse resistance, phone-number verification) is a separate effort.

**Impact on implementation:**
- `POST /v1/keys/register` returns an API key (random token). Client stores it in `EncryptedSharedPreferences`.
- No phone-number verification flow at registration time.
- Server deployment: small VPS or serverless, SQLite or Postgres, TLS mandatory (Let's Encrypt).
- The server is only needed for the initial X3DH handshake. Once a session is established, message exchange works over SMS with no server dependency.

---

## Decision 3: Reinstall / new phone — identity-key loss is accepted

**Decision:** If a user reinstalls or gets a new phone, their identity key changes. All contacts will see the safety-number change and must re-verify. No identity-key backup or portability in v1.

**Rationale:** Identity-key backup/portability is a genuinely hard problem (effectively: how do you back up a private key such that it survives device loss but can't be intercepted?). It's adjacent to the backup-passphrase work already tracked under C3, but materially harder because a leaked private key is worse than a leaked data blob. Solving both in the same pass doubles the risk of getting either wrong.

**Impact on implementation:**
- Identity key pair is generated once per install, stored in `EncryptedSharedPreferences` (or AndroidKeyStore for the private key).
- On reinstall, a new identity key pair is generated. The old one is gone.
- Contacts with existing sessions will detect the key change on the next X3DH handshake (the new identity key won't match the one they cached). The `KeyChangeWarningStore` flow already handles surfacing this to the user.
- `KeyVerificationScreen` is rebuilt on top of the new identity keys (Phase 5).

---

## Decision 4: Skipped-message keys — bounded cache with eviction

**Decision:** The Double Ratchet's skipped-message-keys cache is capped at **1000 entries** per session. When the cap is reached, the oldest entries are evicted. A message that arrives after its key has been evicted cannot be decrypted and is stored as `[Encrypted — key expired]`.

**Rationale:** The Double Ratchet spec allows out-of-order delivery by caching one message key per skipped DH output. A malicious or misbehaving sender can force unbounded storage growth by deliberately skipping messages. Signal's own implementation uses a similar cap (~1000). 1000 is generous enough for real-world SMS reordering (which is rarely more than a few dozen messages out of order) while preventing abuse.

**Impact on implementation:**
- `SkippedMessageKeys` data structure: a bounded map of `(ratchetPublicKey, messageNumber) -> messageKey`.
- Eviction policy: FIFO (oldest evicted first when cap reached).
- On decrypt failure due to missing key: store placeholder text, log a warning.
- The cap is a compile-time constant, not user-configurable.

---

## Decision 5: Message framing — accept segment cost (v1)

**Decision:** Encrypted messages simply cost more SMS segments. No compact binary-over-SMS encoding in v1.

**Rationale:** A Double Ratchet message is ~128 bytes of header (32-byte ratchet public key + 8-byte previous-chain-length + 8-byte message number) + ciphertext + 16-byte MAC, base64-encoded for text transport. A typical short message expands from 1 SMS segment to 2-3. Building a binary-over-SMS encoding would save ~30% on segment count but adds significant complexity (binary framing, fragmentation, reassembly) and is premature optimization for v1.

**Impact on implementation:**
- Encrypted message wire format: `E2E:{base64(header + ciphertext + mac)}`.
- Multi-part SMS (concatenated segments) handles the larger payload automatically — Android's `SmsManager` already supports this.
- The existing multi-part reassembly in `SmsReceiver.groupMultiPartMessages()` already handles concatenated PDUs.
- Segment count increase is documented in the UI (e.g., "Encrypted — 2 segments") as user-visible feedback.

---

## Crypto primitives and library choices

| Primitive | Library | License | Notes |
|-----------|---------|---------|-------|
| X25519 ECDH | Google Tink (`com.google.crypto.tink:tink-android`) | Apache 2.0 | `X25519HKDF` key agreement |
| HKDF-SHA256 | Google Tink | Apache 2.0 | Built into Tink's `HkdfPrfHmacSha256` |
| AES-256-GCM | Google Tink | Apache 2.0 | Symmetric authenticated encryption for message content |
| HMAC-SHA256 | Android standard (`javax.crypto.Mac`) | — | For message authentication (or use Tink's `Mac` API) |
| Key storage | `EncryptedSharedPreferences` + AndroidKeyStore | — | Identity private key in AndroidKeyStore; session state in encrypted Room DB |
| Serialization | Protocol buffers or JSON | — | For pre-key bundles, ratchet state persistence |

**Why not libsignal-android:** Signal's `libsignal-android` is AGPLv3. Bundling it statically in an MIT-licensed app creates copyleft compliance issues. The protocol logic (X3DH + Double Ratchet) is a well-defined state machine with published test vectors — tractable to implement correctly on top of audited primitives (Tink) with careful engineering.

**Why not the existing EncryptionManager:** The current `EncryptionManager` uses per-device AES keys with no ratcheting. The `KeyExchangeManager` has known bugs (wrong KeyStore key purpose, non-exportable private key export attempt). Both should be replaced, not patched, in Phase 4.

---

## Wire format

### Pre-key bundle (server → client)

```json
{
  "identityKey": "<base64 X25519 public key>",
  "signedPrekey": {
    "id": 1,
    "publicKey": "<base64 X25519 public key>",
    "signature": "<base64 Ed25519 signature over publicKey>"
  },
  "oneTimePrekey": {
    "id": 42,
    "publicKey": "<base64 X25519 public key>"
  }
}
```

### Encrypted SMS message

```
E2E:<base64 encoded payload>
```

Payload structure (binary, before base64):

```
[32 bytes] Ratchet public key (sender's current X25519 public key)
[8 bytes]  Previous chain length (varint)
[8 bytes]  Message number (varint)
[N bytes]  AES-256-GCM ciphertext (includes 16-byte auth tag)
```

The `E2E:` prefix replaces the existing `ENC:` prefix. `SmsReceiver` dispatches on this prefix to route through the new decryption path.

---

## Phase ordering (unchanged from plan)

| Phase | Deliverable | Dependencies |
|-------|-------------|-------------|
| 0 | This document | — |
| 1 | Pre-key server (new repo) | Phase 0 |
| 2 | Client X3DH handshake (no message encrypt yet) | Phase 1 |
| 3 | Double Ratchet state machine (transport-agnostic, parallel with Phase 2) | Phase 0 |
| 4 | Wire into SmsSender/SmsReceiver + session persistence | Phase 2 + 3 |
| 5 | UI rebuild (key verification, safety numbers, key-change warnings) | Phase 4 |
| 6 | Interop/adversarial testing | Phase 4 + 5 |
