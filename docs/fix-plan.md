# NexusSMS — Fix Plan (Full Audit)

Complete sweep of `WN-Admin/NexusSMS`, branch state as of commit `d81155f` (master/dev/main converged).
This supersedes the previous fix-plan — everything from that pass is folded in below, plus a full sweep
of the areas that were previously unaudited (all ViewModels, remaining services/receivers, platform
sync services, permissions, release config).

Check items off as you land fixes.

---

## P0 — Blockers before any public release

### [ ] 1. App lock PIN screen has an authentication bypass, and PIN handling is implemented four different, disagreeing ways
**Files:** `ui/viewmodels/AppLockViewModel.kt`, `features/security/AppLockManager.kt`, `features/security/BiometricAuthManager.kt`, `ui/viewmodels/SecuritySettingsViewModel.kt`

This is the screen that gates the entire app (not just the vault), and it's the most serious issue found
in the whole audit. `AppLockScreen.kt` uses `AppLockViewModel.verifyPin()`:

```kotlin
fun verifyPin(pin: String) {
    val savedValue = settings?.appLockValue
    val hashedPin = hashPin(pin)
    if (savedValue == null || hashedPin == savedValue) {   // <-- bypass
        _isAuthenticated.value = true
        _isLocked.value = false
        ...
```

If `savedValue` is `null` — which happens if `settings` hasn't loaded yet from its `Flow` (a real race on
cold app start straight into the lock screen), or if app lock is enabled without a PIN configured —
**any input is accepted as a correct PIN.** This is a straightforward auth bypass on the primary lock
screen.

On top of that, there are four separate places that read or write `appLockValue`, and they don't agree:
- `AppLockViewModel.hashPin()` — unsalted single-round SHA-256, and on hash failure falls back to
  `catch (_: Exception) { pin }` (returns the **raw PIN** as the "hash" — fail-open, not fail-closed).
- `AppLockManager.hashInput()` / `verifyLock()` — a separate, near-duplicate unsalted SHA-256
  implementation. This one correctly returns `false` on a null stored value, so it doesn't have the
  bypass — but it's still weakly hashed, and duplicating the logic is exactly how #1 happened.
- `BiometricAuthManager.setupAppLock()` — the path actually used when a user first sets their PIN — has
  its own `hashPin()` too (need to confirm it matches `AppLockViewModel`'s hash function byte-for-byte,
  or PINs set through this path may never verify correctly against `AppLockViewModel.verifyPin()`).
- `SecuritySettingsViewModel.setLockValue(value)` writes `appLockValue` **completely unhashed**:
  ```kotlin
  fun setLockValue(value: String) {
      update { copy(appLockValue = value) }
  }
  ```
  If anything calls this instead of `setupAppLock()`, the PIN is stored in plaintext in the database.

**Suggested fix:**
- Consolidate to **one** implementation. Delete `AppLockManager` and the hashing in
  `AppLockViewModel`/`BiometricAuthManager`/`SecuritySettingsViewModel.setLockValue`; route all
  read/write through a single `AppLockManager`-equivalent that:
  - Uses PBKDF2WithHmacSHA256 with a random salt (mirror the pattern already fixed in `VaultManager`),
    not raw SHA-256.
  - Never treats a null stored value as "any input passes" — a missing PIN means lock verification
    should fail closed and prompt setup, not silently succeed.
  - Never falls back to the raw input on a hashing exception — fail closed, surface an error instead.
  - Delete or guard `SecuritySettingsViewModel.setLockValue()` so it can't write an unhashed PIN.
- Add the same failed-attempt lockout that `VaultManager.unlockVault()` now has — currently
  `AppLockViewModel.verifyPin()` has zero rate limiting on the primary lock screen.

---

### [ ] 2. "E2E encryption" / safety numbers is not real key exchange
**Files:** `security/KeyExchangeManager.kt`, `security/SafetyNumberManager.kt`, `security/EncryptionManager.kt`

`generateKeyBundle()` produces the "public" and "private" key as two independent calls to
`SecureRandom().nextBytes(32)` — no mathematical relationship, so nothing derived from them can
produce a real shared secret between two devices:

```kotlin
val keyPair = safetyNumberManager.generateRandomKey()    // "public" — unrelated random bytes
val privateKey = safetyNumberManager.generateRandomKey() // "private"
```

Actual message encryption (`EncryptionManager.getOrCreateAESKey()`) uses a symmetric AES key sealed in
the local Android Keystore — non-exportable by design, entirely disconnected from `KeyExchangeManager`.
Verifying a "safety number" today confirms nothing about what's actually encrypting your messages.

Also `SafetyNumberManager.verifiedKeys` is a plain in-memory `mutableMapOf` — not persisted — so every
verified contact reverts to unverified on process restart.

**Suggested fix:**
- Use a real key-agreement primitive: `KeyPairGenerator` with EC (P-256 via `KeyAgreement.getInstance("ECDH")`,
  or Curve25519 via a library) to generate an actual asymmetric pair.
- Exchange public keys, derive a shared secret via `KeyAgreement`, run it through HKDF to produce the
  AES key that actually encrypts that contact's messages — so the safety number reflects real key material.
- Persist verification state (`SafetyNumber`, `isVerified`) in `EncryptedSharedPreferences`, not an
  in-memory map.

---

### [ ] 3. Automation actions report success while doing nothing
**File:** `features/automation/ActionExecutor.kt`

`ARCHIVE`, `MARK_AS_READ`, `DELETE`, `MUTE_CONVERSATION`, `LABEL`, `NOTIFICATION`, `WEBHOOK`, and
`BLOCK_SENDER` are empty stubs (`// Would update conversation in database`), and `AUTO_REPLY` discards
its configured reply text. `execute()` wraps everything in one `try { ... Result.success(Unit) }`, so
the rule engine logs these as successful.

**Suggested fix:**
```kotlin
@Singleton
class ActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val smsSender: SmsSender,
    private val spamBlocklistManager: SpamBlocklistManager
) {
    private suspend fun archiveConversation(message: IncomingMessage) {
        conversationRepository.getConversationById(message.conversationId)?.let {
            conversationRepository.updateConversation(it.copy(isArchived = true))
        }
    }
    private suspend fun markAsRead(message: IncomingMessage) {
        conversationRepository.getConversationById(message.conversationId)?.let {
            conversationRepository.updateConversation(it.copy(unreadCount = 0))
        }
    }
    private suspend fun deleteMessage(message: IncomingMessage) {
        messageRepository.getMessageById(message.id)?.let { messageRepository.deleteMessage(it) }
    }
    private suspend fun muteConversation(message: IncomingMessage) {
        conversationRepository.getConversationById(message.conversationId)?.let {
            conversationRepository.updateConversation(it.copy(isMuted = true))
        }
    }
    private suspend fun blockSender(message: IncomingMessage) {
        spamBlocklistManager.blockNumber(message.senderNumber)
    }
    private suspend fun autoReply(message: IncomingMessage, config: Map<String, String>) {
        val replyMessage = config["message"] ?: return
        smsSender.sendTextMessage(message.conversationId, message.senderNumber, replyMessage)
    }
    // sendWebhook: POST via OkHttp (already a dependency) to config["url"]
    // showNotification / addLabel: wire to SmsNotificationHelper / a labels table
}
```
Until implemented, hide unfinished action types from the rule-builder UI so users can't configure
something that silently no-ops.

---

### [ ] 4. Platform bot tokens stored in plaintext despite a comment claiming otherwise
**Files:** `data/models/SocialAccount.kt`, `features/telegram/TelegramService.kt`, `features/discord/DiscordService.kt`, `features/messenger/MessengerService.kt`

```kotlin
val accessToken: String, // Encrypted   <-- aspirational comment, not actual behavior
```
`SocialAccount` is a Room entity in the main (unencrypted) `NexusSMSDatabase`. Neither
`TelegramService.connectBot()` nor `DiscordService.connectBot()` calls `EncryptionManager` before
storing the token.

**Suggested fix:** encrypt at the repository boundary:
```kotlin
suspend fun saveAccount(account: SocialAccount) {
    val encrypted = account.copy(
        accessToken = encryptionManager.encryptAES256(account.accessToken),
        refreshToken = account.refreshToken?.let { encryptionManager.encryptAES256(it) }
    )
    socialAccountDao.insert(encrypted)
}
suspend fun getAccount(id: String): SocialAccount? = socialAccountDao.getById(id)?.let {
    it.copy(
        accessToken = encryptionManager.decryptAES256(it.accessToken),
        refreshToken = it.refreshToken?.let { rt -> encryptionManager.decryptAES256(rt) }
    )
}
```

---

## P1 — Fix before wide public rollout

### [ ] 5. Channel routing `maxRetries` is configured but never used
**File:** `features/messaging/ChannelRouter.kt`

The channel loop visits each platform exactly once; the retry check
(`channelAttempts.size >= channel.maxRetries + 1`) can never see more than one attempt, so it's dead
code — a failed channel is never retried.

**Suggested fix:**
```kotlin
for (channel in sortedChannels) {
    var attemptNum = 0
    var succeeded = false
    while (attemptNum <= channel.maxRetries && !succeeded) {
        if (attempts.isNotEmpty() && channel.fallbackDelayMs > 0) delay(channel.fallbackDelayMs)
        val result = try { sendViaPlatform(channel.platform, message) } catch (e: Exception) { Result.failure(e) }
        attempts.add(AttemptedChannel(channel.platform, result.isSuccess, result.exceptionOrNull()?.message))
        updateChannelStats(channel.platform, result.isSuccess)
        succeeded = result.isSuccess
        attemptNum++
    }
    if (succeeded) {
        updateRoutingHistory(contactId, attempts)
        updateLastUsedPlatform(contactId, channel.platform)
        return RoutingResult(true, channel.platform, attempts)
    }
    if (!effectiveConfig.fallbackEnabled) break
}
```

### [ ] 6. Silent exception swallowing in live message paths
**Files:** `ui/viewmodels/ChatViewModel.kt:159`, `receivers/SmsReceiver.kt:157`

```kotlin
try { matrixSyncService.syncForRoom(...) } catch (_: Exception) {}   // ChatViewModel.kt:159
} catch (_: Exception) { }   // SmsReceiver.kt:157 — automation rule errors
```
Both fail with zero logging. Fix: `catch (e: Exception) { Timber.w(e, "...") }` at minimum. (The
`unregisterReceiver` empty catches elsewhere are fine as-is — expected-throw cleanup pattern.)

### [ ] 7. `RcsService` has no actual transport
**File:** `features/rcs/RcsService.kt`

`sendRcsMessage`/`sendTypingIndicator`/etc. only write to the local DB and mark status `"SENT"` — no
network call anywhere in the class. Cross-device RCS messaging doesn't function despite the UI being
fully wired. Implement a real transport or relabel/hide the feature until there is one.

### [ ] 8. Location permission requested upfront at first launch instead of contextually
**File:** `MainActivity.kt:150-153`, `ui/screens/ChatDetailScreen.kt`

```kotlin
// MainActivity.kt — part of the general startup permission bundle
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
}
```
`ACCESS_FINE_LOCATION` is bundled with SMS/contacts permissions at first launch, for a feature
(share-location-in-chat) that's only used deep in `ChatDetailScreen`. Requesting broad/sensitive
permissions upfront rather than in-context is bad practice for user trust, and Play Store reviewers
increasingly flag this pattern. `ChatDetailScreen.kt` already has its own in-context permission
request (`locationPermissionLauncher.launch(...)`) for when the user taps "share location" — **remove
the upfront request from `MainActivity` and rely solely on the in-context one.**

Separately: `locationManager.getLastKnownLocation(...)` returns a cached (possibly stale or null)
fix rather than requesting a live location — acceptable for a quick share, but worth a "location may be
approximate" note in the UI, or upgrading to `FusedLocationProviderClient` with a one-shot current
location request if accuracy matters.

---

## P2 — Worth doing, lower urgency

### [ ] 9. Spam detector: romance-scam pattern is prone to false positives on ordinary affectionate texts
**File:** `features/security/SpamDetector.kt` (`romance_1` pattern)

`"love you"`, `"miss you"`, and `(?i)(?:hi|hello|hey)\s+(?:dear|sweetheart|love|darling)` will match a
real partner/family text, currently enough alone to trigger a `MEDIUM`-risk spam notification. Consider
requiring 2+ matched patterns before surfacing a user-facing warning for this category.

### [ ] 10. Regex recompilation in the rule engine
**File:** `features/automation/RuleEngine.kt` (`matchesRule`)

Compiles `Regex(pattern, ...)` fresh per rule per message. `SpamDetector` was already fixed to
precompile once (see verified-fixed list) — mirror that pattern here for accounts with many rules.

---

## Verified fixed (no action needed — kept for audit trail)

- **[FIXED]** Release signing password hardcoded in `build.gradle.kts` → moved to `local.properties`/env var.
- **[FIXED]** `allowBackup=true` + full-DB backup rules exposed the entire plaintext SMS database via
  Android cloud backup → database domain excluded in `backup_rules.xml`/`data_extraction_rules.xml`.
- **[FIXED]** `EncryptionManager.shouldEncryptForContact()` substring false-positive bug → now uses
  `split(",").toSet().contains()`.
- **[FIXED]** Room DB version bumped past the last defined migration with `fallbackToDestructiveMigration()`
  set → guaranteed data wipe on update → migrations `5_6` through `7_8` added and verified against
  entity schemas.
- **[FIXED]** `SessionManager.endSession()` fired on every `ON_STOP` → forced re-auth on any brief
  backgrounding regardless of configured timeout → removed from `ON_STOP`.
- **[FIXED]** WebDAV scheduled backup: credentials never persisted → every scheduled run failed after
  first process death → now persisted correctly (URL/username in `SharedPreferences`, password in
  `EncryptedSharedPreferences`), worker re-authenticates before each run.
- **[FIXED]** WebDAV backup `dataTypes` didn't include `conversations`/`messages` → now included.
- **[FIXED]** Tech Support Scam regex matched bare "apple"/"google"/"microsoft" due to missing grouping
  → fixed, and all spam regexes precompiled once instead of per-message.
- **[FIXED]** Vault PIN hashing was unsalted single-round SHA-256, no lockout → PBKDF2WithHmacSHA256
  (100k iterations, random salt) + 5-attempt/30s lockout that survives process death.
- **[FIXED]** `VaultManager.unhideConversation()` always returned `null` (searched after removing) →
  fixed to capture the match before removal.

---

## Swept this pass, no issues found

- `services/SmsSender.kt` — multipart handling, PendingIntent immutability, sent/delivered status
  updates all correct.
- `receivers/NotificationActionReceiver.kt` — handles quick-reply from notification actions; confirmed
  `exported=false` in the manifest, so it can't be triggered by other apps. Logic itself is sound.
- `receivers/SmsStatusReceiver.kt` — `exported=false`, custom actions only reachable via same-app
  explicit `PendingIntent`. No issue.
- Content provider (`app/src/main/AndroidManifest.xml`) — `exported=false`. No issue.
- `NexusSMSApplication.kt` — `Timber.plant(DebugTree())` correctly gated behind `BuildConfig.DEBUG`, so
  verbose logs won't ship in release builds.
- No hardcoded `http://` endpoints or custom `TrustManager`/`HostnameVerifier` overrides found anywhere
  in the codebase (would indicate TLS validation bypass) — all network clients use default OkHttp TLS.
- No token/password/secret values found logged in plaintext via Timber anywhere (only descriptive
  messages like "token verification failed", not the token itself).
- `app/build.gradle.kts` — `isMinifyEnabled = true` with ProGuard rules applied for release. Fine.

---

## Not yet audited — genuinely out of scope for this pass

Everything security- and data-integrity-critical has now been swept (crypto, vault, spam, backup, DB,
automation, routing, SMS pipeline, app lock, notification receivers, platform token storage,
permissions, release config). What's left is lower-stakes and mostly cosmetic/UX territory:

- Remaining ~50 UI screens not directly tied to a security or data-handling flow (list/detail rendering,
  settings toggles that just flip a boolean, theming) — spot-checked several, no correctness issues
  found, but not every screen was read line-by-line.
- Matrix/Telegram/Discord/Messenger sync **message-parsing bodies** specifically (pagination cursors,
  rate-limit backoff, malformed-payload handling) — token storage was audited (see P0 #4) but the
  sync loops themselves were not read in full.
- Notification listener / floating bubble overlay service internals (`FloatificationService.kt`) —
  only its silent-catch blocks were flagged (see P1 #6's sibling notes); overlay permission handling
  and lifecycle weren't independently verified.
- Play Store data-safety-form accuracy vs. what the app actually collects, now that the P0 items above
  are known (location, contacts, SMS content, bot tokens).
