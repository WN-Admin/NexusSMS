# NexusSMS — Complete Code Audit & Fix List

Repo audited: `WN-Admin/NexusSMS` (cloned fresh), `main` branch, commit at time of audit.
Scope: full `app/src/main` Kotlin source (162 files, ~30.8k LOC), Gradle/build config, CI workflows, manifest, resource XML.

Every item below was verified by reading the actual source and, where relevant, tracing call sites across files (not inferred from naming or docs). File paths are relative to repo root. Severity reflects real-world user/data impact, not code style.

---

## CRITICAL — data loss, broken security guarantees, auth bypass

### C1. Database migrations never actually run — every schema bump wipes the entire database
**Files:** `app/src/main/java/com/nexusmedia/nexussms/data/database/NexusSMSDatabase.kt` (line 76), `app/src/main/java/com/nexusmedia/nexussms/di/AppModule.kt` (`provideDatabase`, ~line 55), `app/src/main/java/com/nexusmedia/nexussms/data/database/NexusSMSDatabaseMigrations.kt`

`NexusSMSDatabaseMigrations.kt` correctly defines `MIGRATION_1_2` through `MIGRATION_7_8` and wraps them in a no-arg extension function:

```kotlin
fun RoomDatabase.Builder<NexusSMSDatabase>.addMigrations(): RoomDatabase.Builder<NexusSMSDatabase> {
    val migrations = NexusSMSDatabaseMigrations.migrations
    return if (migrations.isNotEmpty()) this.addMigrations(*migrations.toTypedArray()) else this
}
```

But both places that actually build the database call it as:

```kotlin
Room.databaseBuilder(context, NexusSMSDatabase::class.java, "nexussms_database")
    .addMigrations()
    .fallbackToDestructiveMigration()
    .build()
```

`RoomDatabase.Builder` already has a **member** function `addMigrations(vararg migrations: Migration)`, which accepts zero arguments. In Kotlin, a member function always wins over an extension function of the same name — the extension defined above is shadowed and is dead code. `.addMigrations()` here resolves to Room's real builder method called with an **empty** migration array. Combined with `.fallbackToDestructiveMigration()`, this means every time `NexusSMSDatabase`'s `version` is bumped (currently 8), Room finds no matching migration path and **drops and recreates every table** — all messages, conversations, contacts, security/vault settings, spam data, templates, everything.

This isn't a hypothetical: the 7 migration classes exist, are well-written, and are simply never wired to the builder that's actually used at runtime.

**Fix:** call the real migrations explicitly, e.g.:
```kotlin
.addMigrations(*NexusSMSDatabaseMigrations.migrations.toTypedArray())
```
and delete the shadowed extension function (or rename it to something that can't collide with the builder's own member, e.g. `applyKnownMigrations()`, and call that instead). Do this in both `NexusSMSDatabase.kt`'s companion `getDatabase()` (itself dead/unused — see C-note below) and `AppModule.provideDatabase()` (the one actually used by Hilt).

*Side note:* `NexusSMSDatabase.getDatabase(context)` (the manual singleton in the companion object) is never called anywhere in the app — `AppModule.provideDatabase()` is the only live path. The dead manual singleton should be deleted to avoid a future accidental second `RoomDatabase` instance pointed at the same file.

---

### C2. "AES-256 encrypted messaging" doesn't work between two devices, and messages are frequently mislabeled as encrypted when they are plaintext
**Files:** `security/EncryptionManager.kt`, `receivers/SmsReceiver.kt` (line 59), `services/MessageService.kt` (line 92), `ui/viewmodels/ChatViewModel.kt` (lines 189, 251-257, 646-665)

`EncryptionManager.encryptAES256()` / `decryptAES256()` use a single AES key generated per-device inside `AndroidKeyStore` (`getOrCreateAESKey()`, alias `nexussms_aes_key`). Android Keystore keys are **non-exportable and unique per device** — no other phone can ever produce ciphertext this device can decrypt, and this device can never decrypt anything encrypted by another phone.

`MessageService.sendSMS()` (dead code, see H6, but shows the intended design) builds an outgoing encrypted payload with exactly this method: `"ENC:${encryptionManager.encryptAES256(...)}"`. `SmsReceiver.onReceive()` (line 57-59) detects incoming `"ENC:"` messages and tries to decrypt them with the **same per-device** `decryptAES256()`. Since the sender and receiver are different devices with different, non-exportable Keystore keys, **decryption of a genuinely cross-device "encrypted" SMS will always fail** (GCM auth-tag mismatch). The codebase already contains the right primitive for this — `KeyExchangeManager.deriveSharedSecret()` (ECDH) plus `EncryptionManager.encryptForContact()/decryptForContact()` — but nothing in the send/receive path uses it. The per-contact E2E system and the actual SMS encrypt/decrypt code are two disconnected halves of a feature.

Separately, and worse: in the code path that's actually reachable from the chat UI (`ChatViewModel.sendMessage()`, "SMS" branch, lines 243-259), the outgoing message is built like this:

```kotlin
messageContent = encryptionManager.generateMessageSignature(messageContent) // just appends a text signature, no crypto
...
val message = Message(
    ...
    content = messageContent,      // <-- plaintext, never passed through encryptAES256/encryptForContact
    isEncrypted = true,             // <-- hardcoded true regardless
    encryptionAlgorithm = "AES256", // <-- hardcoded regardless
    ...
)
```
The exact same pattern repeats in `sendSmsDirectly()` (lines 646-665). `generateMessageSignature()` only concatenates the user's configured text signature onto the message — it performs no encryption at all. So every SMS sent from the main chat screen is transmitted as **plain SMS text**, while the database record and (presumably) any UI badge/lock icon driven by `isEncrypted`/`encryptionAlgorithm` claims it was AES-256 encrypted. This is a false security claim stored as fact, not a cosmetic bug — a user relying on the "Encrypted" indicator for a sensitive conversation is not protected.

**Fix, in order:**
1. Stop setting `isEncrypted = true` / `encryptionAlgorithm = "AES256"` unless the content was actually run through a real encryption call.
2. Route the SMS encryption feature through the existing ECDH machinery (`KeyExchangeManager.deriveSharedSecret(contactId)` → `EncryptionManager.encryptForContact(plaintext, sharedKey)`), not the device-bound `encryptAES256`.
3. On the receive side, mirror this: derive the same per-contact shared secret and call `decryptForContact`, not `decryptAES256`.
4. Until (2)/(3) land, remove the `"ENC:"`-over-SMS feature from the UI entirely rather than ship a broken security promise.

---

### C3. Cloud backup encryption uses the same non-exportable per-device key — an encrypted backup cannot be restored on a new device
**Files:** `features/backup/WebDavBackupService.kt` (line 136), `features/backup/GoogleDriveBackupService.kt` (line 76), `security/EncryptionManager.kt`

Same root cause as C2. Both backup services encrypt the backup payload with `encryptionManager.encryptAES256(payload)` — the AndroidKeyStore per-device key. The entire point of a cloud backup feature is disaster recovery / device migration. If a user enables "encrypt my backup" and then loses or replaces their phone (the primary real-world reason to use a backup), the new device generates a **new, different** Keystore key on first run, and the old backup is permanently undecryptable — there is no key export, no passphrase-derived key, nothing recoverable. This silently defeats the feature's only real purpose while still marketing it as "encrypted backup."

**Fix:** derive the backup encryption key from something portable — a user-supplied passphrase run through PBKDF2/Argon2 (same pattern already used correctly in `VaultManager.hashPinWithSalt`), not an AndroidKeyStore key. Store only the salt (not the key) alongside the backup, and require the user to enter the passphrase on restore.

---

### C4. "Hide in Vault" does not hide the conversation from the main conversation list
**Files:** `features/security/VaultManager.kt`, `ui/screens/MainScreen.kt` (line 113-120), `ui/viewmodels/ConversationListViewModel.kt`, `data/repository/ConversationRepository.kt`

`MainScreen`'s `onHideInVault` callback calls `vaultManager.hideConversation(...)`, which stores a `HiddenConversation` record in `VaultManager`'s own `EncryptedSharedPreferences`. That's it. Confirmed by tracing every reference to `VaultManager` in the app: **`ConversationListViewModel` and `ConversationRepository` never call `isConversationHidden()`, never read `vaultManager.hiddenConversations`, and never filter the main conversation query by anything vault-related.** `ConversationListViewModel.init` subscribes directly to `conversationRepository.getAllConversations()` with no vault filter applied anywhere in the pipeline.

The practical result: a user taps "Hide in Vault" on a conversation, believing it will disappear from the main list (that's the entire premise of the feature, backed by a PIN/decoy-PIN/lockout system that otherwise looks carefully built), and the conversation **stays visible in the normal list**, unchanged. The only thing that happens is a duplicate metadata record gets written to an encrypted prefs file that nothing else reads.

**Fix:** `ConversationRepository`/`ConversationDao` needs a query variant that excludes conversation IDs present in `VaultManager.hiddenConversations`, and `ConversationListViewModel` needs to combine that Flow with the vault-hidden-ID set before exposing `conversationList`. This is not a styling gap — the feature is currently a no-op.

---

### C5. App-lock/biometric screen can be bypassed for a frame on cold start, and silently disables itself if biometric hardware becomes unavailable
**File:** `app/src/main/java/com/nexusmedia/nexussms/MainActivity.kt` (lines 52-136)

Two separate bugs in the same gate:

**(a) Race on first composition.** `settings by appSecuritySettingsDao.getSecuritySettings().collectAsState(initial = null)` starts as `null` until the Flow's first emission arrives. The lock-gating condition is:
```kotlin
val needsLock = settings != null && (settings!!.requireBiometricOnStartup || settings!!.appLockEnabled)
if (needsLock && !isAuthenticated) { AppLockScreen(...) } else { MainScreen() }
```
On the very first frame, `settings == null`, so `needsLock` evaluates to `false` **even if app lock is enabled** — `MainScreen()` (the full conversation list) renders immediately, before the DB has told the app whether locking is required. The lock screen only appears once the Flow emits and recomposition happens. This is a fail-*open* default on a security gate; it should fail closed (assume locked until settings are known).

**(b) Biometric-only lock silently disables itself if biometrics become unavailable.** In the `LaunchedEffect(settings, sessionChecked)` block:
```kotlin
if (settings!!.requireBiometricOnStartup && biometricAuthManager.isBiometricAvailable()) {
    biometricAuthManager.showBiometricPrompt(...)
} else if (settings!!.appLockEnabled && settings!!.appLockValue != null) {
    // PIN fallback, handled by AppLockScreen
} else {
    isAuthenticated = true   // <-- unlocks with no authentication at all
}
```
If a user configured *only* biometric lock (`requireBiometricOnStartup = true`, `appLockEnabled = false`, no PIN set) and then removes their fingerprint/face enrollment in system settings (or biometric hardware becomes unavailable for any reason), `isBiometricAvailable()` returns `false`, the first branch is skipped, the second branch is also skipped (no app lock/PIN configured), and the code falls through to `isAuthenticated = true` — unlocking the app with zero authentication. Disabling fingerprints in Android's own settings (which requires no access to NexusSMS at all) is enough to fully defeat this app's lock screen.

**Fix:** (a) default the lock-required state to "locked" until settings have loaded (e.g., track a separate `settingsLoaded` boolean and never render `MainScreen()` before it's true), rather than deriving "no lock needed" from a null default. (b) when biometric is required but unavailable and no PIN fallback exists, block access and prompt the user to set a PIN, rather than falling through to auto-unlock.

Also in the same file: three separate `CoroutineScope(Dispatchers.IO).launch { ... }` calls (lines 68, 96, 129) create ad-hoc, unscoped coroutines instead of using `lifecycleScope`. They're never cancelled on Activity destruction — minor leak, but also means the `ON_START` session check can still be mutating `isAuthenticated` after the Activity is gone in edge cases. Use `lifecycleScope.launch` for all three.

---

### C6. A single malformed/malicious "ENC:" SMS drops every other message in the same incoming batch
**File:** `receivers/SmsReceiver.kt` (lines 51-190)

```kotlin
scope.launch {
    try {
        for (smsMessage in smsMessages) {
            ...
            val messageBody = if (isEncryptedPayload) {
                encryptionManager.decryptAES256(rawBody.removePrefix("ENC:"))   // can throw
            } else rawBody
            ...
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pendingResult.finish()
    }
}
```
`decryptAES256` throws (`AEADBadTagException`, malformed-Base64 `IllegalArgumentException`, or an `ArrayIndexOutOfBoundsException` from the fixed 12-byte IV slice on a too-short payload) whenever the body starts with `"ENC:"` but wasn't produced by this exact device's `encryptAES256` — which, per C2, is **every legitimate cross-device "encrypted" message**, plus any ordinary text/spam message that happens to start with the literal characters `ENC:`. That single exception is caught only by the **outer** try/catch wrapping the entire `for` loop, so it aborts processing of every remaining `SmsMessage` in that broadcast — messages are never written to the database and the user is never notified. `pendingResult.finish()` still fires, so there's no retry; the message is gone. This is trivially triggerable by anyone who texts the recipient a message starting with `ENC:`.

**Fix:** wrap the per-message body (decryption, spam check, DB insert, notification) in its own try/catch inside the loop so one bad message can't take down the rest of the batch, and treat undecryptable `"ENC:"` payloads as a decode failure (store/display as "message could not be decrypted") rather than letting the exception propagate.

---

## HIGH — broken features, real correctness bugs, security design gaps

### H1. Multi-part (long) SMS messages are not reassembled — each PDU segment becomes its own message bubble
**File:** `receivers/SmsReceiver.kt` (lines 46, 53-184)

`Telephony.Sms.Intents.getMessagesFromIntent(intent)` returns one `SmsMessage` per PDU segment for messages that arrive together as a single logical SMS (the standard case for a long text split across multiple 160-character segments). The correct, well-known pattern is to concatenate `messageBody` across the whole array into a single string before creating one `Message` row. This code instead does `for (smsMessage in smsMessages)` and creates a **separate `Message`, separate conversation update, separate spam check, separate automation-rule evaluation, and separate notification** for every segment. A long incoming text becomes several message bubbles instead of one, unread counts get incremented once per segment instead of once per logical message, and automation rules fire multiple times for what the sender considers a single message.

**Fix:** join `smsMessages.joinToString("") { it.messageBody ?: "" }` (using the first segment's `originatingAddress`/`timestampMillis`) into one body before running the rest of the per-message pipeline once.

---

### H2. Live-received messages and re-imported messages can duplicate
**Files:** `receivers/SmsReceiver.kt` (message construction, line 107-118), `data/repository/SmsImporter.kt` (line 69-80), `data/database/NexusSMSDatabaseMigrations.kt` (unique index, line 19)

The unique index `(conversationId, sourceSmsId)` is the app's only duplicate-prevention mechanism for imported SMS (`insertImportedMessage` uses `OnConflictStrategy.IGNORE`). `SmsImporter.importAllSms()`/`resyncFromDevice()` correctly populate `sourceSmsId` from the system Telephony provider's `_ID`. But `SmsReceiver.onReceive()` — which fires live, in real time, for every incoming text — constructs its `Message` **without setting `sourceSmsId` at all** (it defaults to `null`). Since `NULL` values are never considered equal to each other or to any real ID for the purposes of this unique constraint, a message received live (row with `sourceSmsId = null`) and later re-imported via "Import SMS" or "Re-sync from device" (row with the real `sourceSmsId`) are **not recognized as duplicates** and both rows persist — every message received while the app was running shows up twice after the next import/resync.

**Fix:** have `SmsReceiver` look up (or the system assign) the real Telephony `_ID` for the message it just received and store it as `sourceSmsId`, or maintain a separate content-hash/timestamp-based dedupe key that both paths populate consistently.

---

### H3. Manual safety-number verification is mathematically broken — the two participants compute different numbers for the same key pair
**File:** `security/SafetyNumberManager.kt` (`generateSafetyNumberFromFingerprints`, lines 87-101, 163-172)

```kotlin
fun generateSafetyNumber(contactId, myPublicKey, theirPublicKey): SafetyNumber {
    val myFingerprint = generateFingerprint(myPublicKey)
    val theirFingerprint = generateFingerprint(theirPublicKey)
    val combinedFingerprints = myFingerprint + theirFingerprint   // order-dependent concatenation
    val safetyNumber = generateSafetyNumberFromFingerprints(combinedFingerprints)
    ...
}
```
On device A, `combinedFingerprints = A_fp + B_fp`. On device B (the other party in the same conversation), `"my"` and `"their"` are swapped, so `combinedFingerprints = B_fp + A_fp`. Because string concatenation isn't commutative, `SHA-256(A_fp + B_fp) != SHA-256(B_fp + A_fp)` in general — **the two devices will display different safety numbers for the same, legitimate, unchanged key pair.** `verifySafetyNumber()` does a plain equality check between the locally stored number and a manually-typed/scanned number from the other device, which will fail even when nothing is wrong, defeating the entire point of the feature (and training users to distrust or ignore a security warning that is actually a false positive).

Note the QR-code path (`verifyQrCode`, lines 108-120) *does* correctly cross-compare (`scannedMyFingerprint == stored.theirFingerprint && scannedTheirFingerprint == stored.myFingerprint`) — only the plain numeric "compare these digits" path is broken.

**Fix:** make the fingerprint combination order-independent — e.g. sort the two fingerprints lexicographically before concatenating (this is the standard approach, matching how Signal/other implementations construct commutative safety numbers), so both devices compute an identical string regardless of which one is "me."

---

### H4. Detected-key-change (MITM) warning system is built but never called
**File:** `security/EncryptionKeyVerifier.kt`, `security/KeyExchangeManager.kt` (`processKeyExchange`, lines 113-141)

`EncryptionKeyVerifier.verifyContact()` correctly distinguishes `NewKey` vs. `KeyChanged` vs. `Verified`/`Unverified` by comparing an incoming public key against previously-received keys for that contact — exactly the check needed to warn a user "this contact's encryption key changed, this could mean their device changed or someone is intercepting your messages." Searching the entire codebase, **nothing calls `verifyContact()` or references `VerificationResult` outside of `EncryptionKeyVerifier.kt` itself.** Instead, `KeyExchangeManager.processKeyExchange()` accepts any incoming `KeyExchangeMessage` for a contact, appends it to the stored key list, and unconditionally regenerates+overwrites the safety number (which resets `isVerified` back to `false`, but with no distinction shown to the user between "first time talking to this contact" and "this contact's key just changed"). A real MITM/key-substitution attempt would be accepted silently.

**Fix:** call `EncryptionKeyVerifier.verifyContact()` from `processKeyExchange()` (or the UI layer that consumes it) and surface a distinct, explicit warning when the result is `KeyChanged`, rather than silently downgrading to "unverified."

---

### H5. E2E key exchange has no forward secrecy, and the private key isn't hardware-backed
**File:** `security/KeyExchangeManager.kt` (lines 90-111, 143-151, 225-237)

`generateKeyBundle()` creates a static EC (P-256) key pair via a plain `KeyPairGenerator.getInstance("EC")` — not `KeyPairGenerator.getInstance("EC", "AndroidKeyStore")` — and stores the **raw PKCS8-encoded private key bytes** (base64) inside `EncryptedSharedPreferences`. `deriveECDHSecret()` then does one ECDH computation per contact from this single static identity key, hashed once with plain `SHA-256` (no HKDF, no per-message ratchet, no ephemeral keys). Practically: (1) the private key exists as extractable bytes rather than being confined to secure hardware, so a compromise of the app's storage layer directly yields the key material; (2) because the same static key is reused for every contact and every message, a single private-key compromise retroactively decrypts **all** past traffic with **all** contacts — there is no forward secrecy at all, unlike the Signal-style trust model this feature's UI (safety numbers, QR verification, decoy vault) otherwise implies.

Additionally, `generateKeyBundle()` has no guard against being called more than once: it unconditionally generates a new key pair and overwrites the stored identity bundle every time it's invoked, silently invalidating every existing contact's derived shared secret and safety number with no user-facing "your identity key changed" flow.

**Fix:** generate the EC key pair inside `AndroidKeyStore` so the private key never exists as exportable bytes; derive per-message or per-session keys via HKDF with proper context binding instead of raw `SHA-256(sharedSecret)`; guard `generateKeyBundle()` so it's only ever called once per identity (or treated as an explicit, user-confirmed "rotate my key" action).

---

### H6. Four independent, duplicated SMS-send implementations — one is dead code with real bugs, two others race and cross-contaminate delivery status
**Files:** `services/SmsSender.kt` + `receivers/SmsStatusReceiver.kt` (the correct path), `services/MessageService.kt` (dead), `ui/viewmodels/ChatViewModel.kt` (`sendMessage` SMS branch, lines 244-333, and `sendSmsDirectly`, lines 646-739)

There are four separate places in this codebase that build `PendingIntent`s, call `SmsManager.sendMultipartTextMessage`, and track sent/delivered status:

1. **`SmsSender.sendTextMessage()`** — clean, injectable, takes a real `conversationId`, tags its `PendingIntent`s with `EXTRA_MESSAGE_ID`, and is correctly paired with the manifest-registered `SmsStatusReceiver`, which reads that extra and calls back into `SmsSender.applySentResult/applyDeliveredResult`. This is the one correctly-built implementation.
2. **`MessageService.sendSMS()/sendRCSMessage()/sendEncryptedMessage()`** — a bound `Service`, declared in the manifest, but **never bound to or called from anywhere in the app** (confirmed: no `bindService`/`MessageServiceBinder` reference anywhere outside the file itself). Dead code — but it's live enough to be a landmine if anything ever calls it: it hardcodes `conversationId = "0"` for every message it sends (with a comment acknowledging "caller should replace this," which nothing does), and `sendRCSMessage()` unconditionally inserts a `Message` row with `status = "SENT"` **without ever transmitting anything** — it fabricates a false "sent successfully" record for a feature (`RcsService`) that is explicitly a stub (see L-notes: `RcsService.kt` has three `TODO`s stating RCS send isn't implemented).
3. **`ChatViewModel.sendMessage()`**, "SMS" branch — reimplements the entire PendingIntent/BroadcastReceiver dance inline, a third time, and (per C2) hardcodes `isEncrypted = true`.
4. **`ChatViewModel.sendSmsDirectly()`** — a near-identical fourth copy of the same inline pattern, used for the non-SMS-platform routing fallback.

Implementations 3 and 4 register their own anonymous, dynamically-scoped `sentReceiver`/`deliveredReceiver` via `context.registerReceiver(...)` **for the generic action string `"com.nexusmedia.nexussms.SMS_SENT"`, without attaching a message-ID extra to the `PendingIntent`s that trigger them.** If a user sends two messages in quick succession, two separate receiver instances end up registered for the same broadcast action. Android delivers a matching broadcast to *all* currently-registered matching receivers, and since neither receiver can tell from the intent which specific message the broadcast was actually about (each just closes over whichever `message`/`msg` local it captured), the *first* sent-broadcast that arrives can mark a completely unrelated, still-in-flight message as `"SENT"` (or `"FAILED"`) in the database. This is a genuine, reproducible status-corruption bug caused directly by the duplication, and it doesn't happen in the correctly-built `SmsSender`/`SmsStatusReceiver` path because that one does tag the extra.

**Fix:** delete `MessageService.kt` entirely (or finish and wire it up if it's meant to replace `SmsSender`) and delete the two inline implementations in `ChatViewModel`, routing all SMS sends — including the platform-routing fallback and the non-SIM-selector path — through `SmsSender.sendTextMessage()`, which already handles SIM selection status tracking correctly and is the only version that tags its intents.

---

### H7. CI: release build signing is broken in both workflow files (env var name mismatch + missing keystore)
**Files:** `.github/workflows/android-ci.yml`, `.github/workflows/android.yml`, `app/build.gradle.kts` (signingConfigs, lines ~19-32)

`app/build.gradle.kts` reads the release keystore password from:
```kotlin
storePassword = localProps["STORE_PASSWORD"] ?: System.getenv("STORE_PASSWORD") ?: ""
```
Both CI workflows set the environment variable as `KEYSTORE_PASSWORD`, not `STORE_PASSWORD`:
```yaml
env:
  KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
  KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
  KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
```
`System.getenv("STORE_PASSWORD")` will always be `null` in CI, so `storePassword` falls back to `""`. Separately, `app/release.keystore` is (correctly) gitignored and **neither workflow has a step that materializes it** on the runner (e.g. decoding a base64 secret to `app/release.keystore` before the build step) — the file simply won't exist in a fresh checkout. The `release` job in `android-ci.yml` (`./gradlew bundleRelease`) and the `build-release` job in `android.yml` (`./gradlew assembleRelease`) will fail outright the first time either is triggered by a version tag, for two independent reasons.

**Fix:** rename the env var to `STORE_PASSWORD` (or change the Gradle script to read `KEYSTORE_PASSWORD`) in both workflow files, and add a step before the build that writes the base64-encoded keystore secret out to `app/release.keystore`.

---

## MEDIUM — real bugs and dead/disconnected functionality

### M1. Spam-detection schema is duplicated across two separate Room databases; the copy inside the main database is completely unused
**Files:** `data/database/NexusSMSDatabase.kt` (entities list + `spamDao()`), `data/database/NexusSMSDatabaseMigrations.kt` (`MIGRATION_7_8`), `features/security/SpamDatabaseProvider.kt`, `di/AppModule.kt` (`provideSpamDao`)

`SpamDetectionEntity`/`SpamRuleEntity`/`SpamDao` are declared as entities of **both** the main `NexusSMSDatabase` (`nexussms_database` file, migration `MIGRATION_7_8` creates `spam_detections`/`spam_rules` tables there too) **and** a completely separate standalone `SpamDatabase` (`spam_database` file, version 1). `AppModule.provideSpamDao()` wires the standalone database's DAO as the one actually injected everywhere; `NexusSMSDatabase.spamDao()` is declared but — confirmed by searching every call site of `.spamDao()` in the codebase — **never invoked**. The main database carries a fully dead, unused copy of this schema (wasted migration work, wasted disk space, and a trap for a future contributor who reasonably assumes `NexusSMSDatabase.spamDao()` is the live one).

**Fix:** remove `SpamDetectionEntity`/`SpamRuleEntity`/`spamDao()` from `NexusSMSDatabase` and drop `MIGRATION_7_8`'s spam-table creation (bump the standalone `SpamDatabase` version if a real migration is ever needed there instead).

### M2. Message reactions bypass the dedicated reactions table/schema entirely
**Files:** `ui/viewmodels/ChatViewModel.kt` (`addReaction`, lines 409-419), `data/database/Daos.kt` (`ReactionDao`), `data/repository/` (no `ReactionRepository` used by `ChatViewModel`), `data/models/Message.kt` (`reactions: String`, documented as `// JSON: {emoji: [phoneNumbers]}`)

There's a proper `Reaction` entity, `ReactionDao` (with `getReactionsByMessage`/grouped `getReactionSummary`), and `ReactionRepository` designed to support multiple people reacting with multiple different emoji per message. `ChatViewModel` never imports or injects `ReactionRepository`. Instead, `addReaction()` does:
```kotlin
val updatedMessage = message.copy(reactions = reaction)   // overwrites the whole field with one raw string
```
This both bypasses the real reactions table (which stays empty forever) and violates `Message.reactions`'s own documented format (a JSON map of emoji → list of phone numbers) — it just stores the latest reaction string, discarding whatever was there before. A message can display at most one reaction, and it isn't attributable to who reacted.

**Fix:** route `addReaction`/reaction display through `ReactionRepository`/`ReactionDao` as designed, or stop maintaining the parallel `Message.reactions` field.

### M3. Automation execution history is defined in the database but never written
**Files:** `features/automation/AutomationDatabase.kt` (`ExecutionLogEntity`, `insertLog`), `features/automation/RuleEngine.kt` (`_executionLogs`, in-memory only)

`AutomationDatabase` declares `automation_execution_logs` with `insertLog()`, `getRecentLogs()`, `getLogsForRule()` — clearly meant to back a persistent "automation history" view. `RuleEngine.evaluateMessage()` keeps its own execution log purely in an in-memory `MutableStateFlow` (capped at 1000, reset on every process death) and never calls `AutomationDao.insertLog()` anywhere in the codebase. Any UI that reads `getRecentLogs()`/`getLogsForRule()` will always see an empty table.

**Fix:** call `automationDao.insertLog(...)` from `RuleEngine.executeRule()` (or wherever the caller has access to the DAO) so history survives process restarts.

### M4. JSON `TypeConverter`s are defined but unused (self-documented)
**File:** `data/converters/JsonConverter.kt`

The file's own doc comment says it plainly: *"current entities store many JSON-backed fields as `String`, but the tracker spec expects a JSON TypeConverter to support richer model shapes later."* `fromJsonListString`/`listStringToJson`/map variants are registered via `@TypeConverters(DateConverter::class, JsonConverter::class)` on the database but no entity field in the codebase is currently typed as `List<String>`/`Map<String,String>` etc. — fields like `Message.mediaUrls`, `Message.metadata`, `Message.reactions` are all plain `String` with a comment describing the JSON shape, manually joined/parsed ad hoc elsewhere (e.g. `attachments.joinToString(",")` in `ChatViewModel`). This is the same "infrastructure built, never connected" pattern as M2/M3, just for the type-conversion layer.

### M5. `Timber.e(TAG, "message")` misuse discards the actual error message and the stack trace in 7 files (32 call sites)
**Files:** `features/security/BiometricAuthManager.kt`, `features/security/SessionManager.kt`, `features/backup/WebDavBackupService.kt`, `features/backup/GoogleDriveClient.kt`, `features/backup/WebDavBackupWorker.kt`, `features/backup/GoogleDriveBackupService.kt`, `features/backup/BackupWorker.kt`

Timber's `e(String message, Object... args)` overload treats its first argument as a **format string**, not a log tag (Timber derives tags automatically; it has no `Log.e(tag, msg)`-style overload). Code throughout these files does:
```kotlin
Timber.e(TAG, "Error checking biometric availability: ${e.message}")
```
This resolves to `e(message = TAG, args = [the interpolated string])`. Since `TAG` (e.g. `"BiometricAuthManager"`) contains no `%` format specifiers, `String.format` silently drops the second argument — **the actual descriptive error text is discarded**, only the literal class-name string gets logged, and because the real `Throwable` was never passed in (only `e.message` was manually interpolated), **no stack trace is ever recorded either**. Every catch block in these 7 files that uses this pattern is effectively logging nothing useful. Contrast with the correct pattern used elsewhere in the same codebase (`EncryptionManager.kt`, `KeyExchangeManager.kt`): `Timber.e(e, "message")` (Throwable first).

**Fix:** replace every `Timber.e(TAG, "...")` in the listed files with `Timber.e(e, "...")`, and remove the now-redundant `TAG` constants where they exist solely for this purpose.

### M6. Phone-number normalization is applied inconsistently, causing conversation-matching and blocklist misses
**Files:** `data/repository/SmsImporter.kt` (`normalizePhone`, line 172), `ui/viewmodels/ChatViewModel.kt` (inline `Regex("[^+\\d]")`, line 123), `receivers/SmsReceiver.kt` (`findConversationWithParticipant(senderPhoneNumber)`, line 73 — **no normalization**), `features/security/SpamBlocklistManager.kt` (`isBlocked`, line 70-72 — **exact-match only, no normalization**), `data/database/Daos.kt` (`findConversationWithParticipant`, substring `LIKE` match)

There's no shared phone-normalization utility (`utils/Validators.kt` doesn't have one). `SmsImporter` normalizes with `Regex("[^+\\d]")` before matching conversations by participant; `ChatViewModel.loadConversation()` duplicates the same regex inline for avatar lookups. `SmsReceiver.onReceive()` — the live, real-time incoming-message path — passes the **raw, unnormalized** `originatingAddress` straight into `findConversationWithParticipant()`, which itself does a `LIKE '%' || :phoneNumber || '%'` substring match rather than an exact match on a canonical form. Two consequences: (1) the same real contact can end up split across multiple conversation threads if the carrier ever varies the address format (with/without `+`, country code, punctuation); (2) `SpamBlocklistManager.isBlocked()` does an exact `Set<String>.contains(number)` check with no normalization at all, so a number blocked in one format will not be recognized as blocked if a later message arrives with the address formatted differently — a blocked spammer's messages can get back through.

**Fix:** add one canonical normalization function (e.g. to `Validators`) and use it at every write and read site that stores or looks up a phone number — conversation matching, blocklist checks, and the DAO query itself (ideally matching against a normalized column rather than a `LIKE` substring scan on the raw field).

### M7. Send-delay setting is displayed and configurable but never applied to actual sending
**Files:** `ui/viewmodels/ChatViewModel.kt` (`_sendDelaySeconds`/`setSendDelay`, lines 483-488), `ui/screens/ChatDetailScreen.kt` (line 166, 715-732), `ui/screens/MessagingSettingsScreen.kt` (lines 60-133), `features/messaging/MessagingPreferences.kt`

`MessagingSettingsScreen` lets the user configure `MessagingPreferences.sendDelaySeconds` (a persisted "undo send" style delay), and `ChatDetailScreen` displays a countdown UI driven by `ChatViewModel.sendDelaySeconds` when it's `> 0`. But `ChatViewModel.sendMessage()` never reads `_sendDelaySeconds.value` (or `MessagingPreferences.sendDelaySeconds`) to actually defer transmission — the SMS/RCS send call fires immediately regardless of the configured delay. The two values are also disconnected from each other: `ChatViewModel._sendDelaySeconds` always starts at `0` and nothing initializes it from the persisted `MessagingPreferences.sendDelaySeconds` when a chat is opened, so even the UI display doesn't reliably reflect what's configured in Settings.

**Fix:** initialize `ChatViewModel`'s delay state from `MessagingPreferences` on load, and actually gate the send call behind a cancellable delay (e.g. `delay(sendDelaySeconds * 1000L)` inside the launched coroutine, with a way for the user to cancel within the window) before it reaches `SmsSender`.

### M8. Scheduled messages rely on a 15-minute WorkManager poll despite the app requesting (and never using) `SCHEDULE_EXACT_ALARM`
**Files:** `AndroidManifest.xml` (line 36), `services/ScheduledMessageScheduler.kt`, `services/ScheduledMessageWorker.kt`

`AndroidManifest.xml` declares `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />`, but no `AlarmManager`/`setExact*`/`setAndAllowWhileIdle` call exists anywhere in the codebase (confirmed by search). The actual scheduling mechanism is `PeriodicWorkRequestBuilder<ScheduledMessageWorker>(15, TimeUnit.MINUTES)` — WorkManager's minimum periodic interval. A message the user schedules for an exact time can be sent anywhere up to ~15+ minutes late (more under Doze/battery restrictions), which doesn't match the precise date-and-time picker UI in `ScheduledMessagesScreen`. The unused, sensitive `SCHEDULE_EXACT_ALARM` permission also draws unnecessary Play Store policy scrutiny for a permission the app doesn't actually exercise.

**Fix:** either use `AlarmManager.setExactAndAllowWhileIdle` for near-term precise sends (falling back to the WorkManager poll as a safety net for missed alarms after reboot) and keep the permission, or drop the permission and make the UI honest about the delivery-time tolerance.

### M9. Live chat message list has no pagination — the whole conversation history loads and re-emits on every change
**Files:** `data/repository/MessageRepository.kt` (`getConversationMessages`, line 37-38 → `messageDao.getAllMessagesByConversation`), `data/database/Daos.kt` (both `getMessagesByConversation(limit, offset)` — paginated, defined but unused by the live chat screen — and `getAllMessagesByConversation` — unbounded), `ui/viewmodels/ChatViewModel.kt` (`loadConversation`, line 144)

The DAO has a properly paginated query (`getMessagesByConversation(conversationId, limit, offset)`), but `ChatViewModel.loadConversation()` subscribes to `messageRepository.getConversationMessages(conversationId)`, which calls the **unbounded** `getAllMessagesByConversation` — no `LIMIT`. For any conversation with a long history (which is exactly the common case for an SMS app used over months/years with one contact), opening that chat loads every message it has ever contained into a `StateFlow`, and Room re-runs and re-emits the full list on every single row change in that conversation (new message, status update, lock toggle, etc.). The paginated DAO method exists and is simply not the one wired up.

**Fix:** switch the live chat screen to the paginated query (with incremental loading as the user scrolls up) instead of `getAllMessagesByConversation`.

### M10. WebDAV file-listing parser uses brittle regex against a hardcoded XML namespace prefix
**File:** `features/backup/WebDavClient.kt` (`parseMultiStatusResponse`, lines 196-236)

`listFiles()` parses the WebDAV `PROPFIND` multistatus XML response with hand-rolled regexes hardcoded to the `D:` namespace prefix (`Regex("<D:href>([^<]+)</D:href>")`, `<D:getlastmodified>`, etc.) instead of a real XML parser. Real-world WebDAV servers (Nextcloud, ownCloud, generic Apache `mod_dav`, etc. — exactly the kind of self-hosted target this "connect your own WebDAV server" feature is built for) commonly use different namespace prefixes (`d:`, `lp1:`, or none) depending on server/version. Against any server that doesn't happen to use the literal `D:` prefix, `listFiles()` will silently return an empty or partial list with no error — backups appear to have vanished from the server even though they're present.

**Fix:** parse with `XmlPullParser`/`javax.xml.parsers.DocumentBuilder` matching on local element name regardless of namespace prefix, not string regex.

### M11. A single bad automation rule (invalid regex) silently disables every other rule for that message; user-supplied regex has no ReDoS protection and is recompiled on every message
**File:** `features/automation/RuleEngine.kt` (`matchesRule`, lines 63-104)

`matchesRule()` builds a fresh `Regex(pattern, RegexOption.IGNORE_CASE)` from user-configured `senderPattern`/`contentPattern` strings on every single call — i.e., once per rule per incoming message, with no caching of compiled patterns. `Regex(...)` throws `PatternSyntaxException` for an invalid pattern, and nothing in `matchesRule`/`evaluateMessage` catches it locally; the only catch is the caller in `SmsReceiver` wrapping the *entire* automation block, meaning **one rule with a malformed regex aborts evaluation of every other (valid) rule** for that message, silently, for every future message until the user finds and fixes/removes the bad rule. There is also no timeout/complexity guard on user-supplied patterns, so a pathological pattern (catastrophic backtracking) entered in the Automation screen can hang message processing.

**Fix:** validate regex patterns at rule-save time (reject/warn on invalid syntax before persisting), cache compiled `Regex` objects per rule instead of recompiling per message, and catch pattern errors per-rule inside `matchesRule` so one broken rule doesn't take down the rest.

### M12. Two Gson deserialization calls are unguarded and can crash the app on corrupted preference data
**Files:** `features/security/VaultManager.kt` (`loadHiddenConversations`, lines 319-325, called from `init {}`), `security/KeyExchangeManager.kt` (`getMyKeyBundle`, `getReceivedKeys`, lines 153-167)

Both `VaultManager.loadHiddenConversations()` and `KeyExchangeManager`'s key-bundle getters call `gson.fromJson(json, type)` directly with no try/catch, unlike the equivalent `SafetyNumberManager.loadVerifiedKeys()` in the same codebase, which does wrap the same kind of call. If the stored JSON in `EncryptedSharedPreferences` is ever corrupted or from an incompatible future schema, `VaultManager`'s version throws inside its Hilt-singleton `init {}` block (crashing app startup, since `VaultManager` is injected early), and `KeyExchangeManager`'s versions throw whenever a key bundle is read (breaking the entire E2E feature until the corrupted pref is manually cleared).

**Fix:** wrap both in try/catch matching the pattern already used in `SafetyNumberManager`, returning a safe default (empty list / null) and logging on failure instead of propagating.

### M13. Two duplicate GitHub Actions workflows run the same lint/test/build jobs on every push
**Files:** `.github/workflows/android-ci.yml`, `.github/workflows/android.yml`

Both files trigger on `push` to `main`/`develop` and `pull_request` to `main`, and both independently run a lint job, a unit-test job, and a debug-APK build job (plus the broken release job from H7) — every commit produces two of each check, doubling CI minutes and producing two separate, confusingly-named status checks on every PR for what's functionally the same job.

**Fix:** consolidate into one workflow file (or clearly split responsibilities and remove the overlap), keeping whichever has the more complete caching/config (`android.yml` has Gradle caching steps that `android-ci.yml` lacks).

---

## LOW — code quality, hardening, minor correctness

- **`utils/Validators.kt` — `isValidPhoneNumber()` is defined but never called anywhere.** New-conversation phone number entry has no validation via this function; malformed numbers can be used to create conversations/attempt sends with no client-side check.
- **`local.properties` and the entire `.idea/` directory are tracked in git despite being listed in `.gitignore`** (confirmed via `git ls-files`) — they were evidently committed before the ignore rules were added and never removed. `local.properties` currently only exposes a local SDK path/username, not a secret, but tracking a supposedly-ignored, machine-specific file is a repo-hygiene problem that will keep generating spurious diffs and confusion for every contributor with a different local setup.
- **Non-constant-time credential comparisons.** `VaultManager.unlockVault()`/`changePin()`/`disableVault()` and `AppLockManager.verifyLock()` compare PBKDF2 hash hex strings with plain `==`. Low practical risk locally, but a straightforward hardening fix is `MessageDigest.isEqual(a.toByteArray(), b.toByteArray())` (or a manual constant-time compare) instead of `==`.
- **`Daos.kt` search queries (`searchMessages`, `searchConversations`, `findConversationWithParticipant`) build `LIKE '%' || :query || '%'` clauses with no escaping of `%`/`_`.** A literal `%` or `_` typed by the user acts as a SQL wildcard instead of a literal character, producing surprising search results (e.g. searching "50% off" behaves like a fuzzy match instead of literal text). These full-scan leading-wildcard `LIKE` queries also can't use an index and will get slow as message history grows; Room's FTS4/FTS5 virtual tables are the standard fix for in-app message search at scale.
- **`RuleEngine.createOtpForwardRule()` auto-copies detected OTP codes to the system clipboard.** The Android clipboard is a well-known cross-app leak vector (any other app with clipboard read access, or the OS clipboard history on some OEM skins, can see it). Worth a second look given this app also markets itself on E2E-encryption/vault privacy features elsewhere — auto-copying sensitive one-time codes cuts against that.
- **`Math.random()` used instead of `UUID.randomUUID()`/`SecureRandom` for identifiers** in `security/KeyExchangeManager.kt` (`getOrCreateDeviceId`, line 177) and `features/matrix/MatrixMessageService.kt` (transaction IDs, lines 35/78/129). Not a cryptographic secret in either case, but `Math.random() * 10000` combined with a millisecond timestamp has a real (if small) collision window for the Matrix transaction IDs, which are meant to be unique idempotency keys.
- **Debug build variant is signed with the same release signing config and `isDebuggable = true`** (`app/build.gradle.kts`, `buildTypes { debug { signingConfig = signingConfigs.getByName("app") ... } }`). Not unsafe by itself, but it means a debuggable build can be produced bearing the same signature as production releases — worth confirming that's intentional rather than a copy-paste from the release block.
- **`MmsHelper.compressImageForMms()` can still return an image larger than the carrier limit after its compression loop exits** (loop stops once `quality <= 30` regardless of whether the size target was met) — only a `Timber.w` warning is logged; the oversized bytes are returned to the caller as if compression succeeded, deferring the failure to whatever tries to actually send the MMS.
- **`ConversationRepository.markConversationAsRead()` and `clearUnreadCount()` are byte-for-byte identical implementations** (both: fetch conversation, `copy(unreadCount = 0)`, update) — dead duplication; one of the two names should be removed and all call sites pointed at the other.
- **`printStackTrace()` used instead of the app's own logging (Timber) in 7 places**: `ChatViewModel.kt` (lines 331, 368, 378, 560), `SmsReceiver.kt` (line 186), `MessageService.kt` (lines 133, 158). `printStackTrace()` writes to stderr, isn't tagged, and isn't captured the same way as the rest of the app's Timber-based logging — these should be `Timber.e(e, "...")` like the rest of the codebase.
- **`MessageService.kt` is entirely unreachable dead code** (see H6) but is still declared in the manifest as a `<service>`. If it's not going to be finished and wired up, delete the class and its manifest entry; leaving it in place means the `conversationId = "0"` and fake-success-RCS-send bugs described in H6 are one accidental `bindService()` call away from firing in production.

---

## Systemic pattern worth calling out

A recurring shape across this codebase: a feature's persistence/verification layer is built carefully and correctly, and then the code that's actually reachable from the UI/runtime never calls it. Confirmed instances, independently traced:

1. Vault "hide conversation" metadata is recorded, but nothing filters the conversation list by it (C4).
2. The E2E key-change/MITM verifier (`EncryptionKeyVerifier`) exists but is never invoked (H4).
3. The `Reaction`/`ReactionDao` table exists but `ChatViewModel` writes reactions into a different, ad hoc field instead (M2).
4. The `automation_execution_logs` table/DAO exists but `RuleEngine` never inserts into it (M3).
5. The `JsonConverter` type converters exist (and self-document that they're not yet connected) while entities keep hand-rolled `String` fields (M4).
6. The database migrations are fully written but the builder that's actually used never receives them (C1).
7. The paginated message query exists in the DAO but the live chat screen uses the unbounded one instead (M9).

Given how consistent this pattern is, it's worth treating as a process issue, not just a pile of unrelated bugs: when a new DB table, DAO method, or manager class is added, add an explicit "who calls this" check (or a simple lint/grep-based CI check for unused-but-public DAO methods) before considering the feature done.

---

## What looked solid (for calibration)

To be clear about what's *not* broken: `VaultManager`'s PIN/decoy-PIN hashing (PBKDF2-HMAC-SHA256, random salt, lockout after 5 attempts) is properly implemented; `AppLockManager`'s PIN storage follows the same good pattern; `backup_rules.xml`/`data_extraction_rules.xml` correctly exclude databases and key material from Android's auto-backup/cloud-backup/device-transfer where it matters; `SmsSender.kt`/`SmsStatusReceiver.kt` (the one non-duplicated send path) is a clean, correctly-tagged implementation; `PendingIntent`s throughout the codebase consistently use `FLAG_IMMUTABLE`; manifest-exported components are minimal and appropriately permission-gated (`SmsReceiver` requires `BROADCAST_SMS`, everything else defaults to non-exported).
