# NexusSMS — Deployment Readiness Plan

**Last Updated**: July 12, 2026  
**Current Version**: 1.0.3 (code 103)  
**Package**: `com.nexusmedia.nexussms`  
**DB Version**: 4  
**HEAD Commit**: `64464c4` (dev + main)

---

## Phase 1: Build System & Infrastructure

### 1.1 Fix compileSdk (37 → 35) ✅
- `app/build.gradle.kts`: compileSdk=35, minSdk=24, targetSdk=35

### 1.2 Enable R8 ProGuard minification
- `app/build.gradle.kts`: set `isMinifyEnabled = true` for release
- ProGuard rules already defined for Room, Hilt, Gson, OkHttp, Compose, Navigation, Biometric, Drive, WorkManager, Timber

### 1.3 Configure release signing ✅
- Keystore: `app/release.keystore`, password: `nexussms123`, alias: `nexussms`

### 1.4 Set up GitHub Actions CI ✅
- `.github/workflows/android.yml`: lint + testDebug + assembleDebug on push/PR

---

## Phase 2: Complete Features from Docs (no stubs)

### 2.1 Navigation wiring ✅
- ConversationListScreen: click navigates to `chat/{conversationId}`
- FAB: starts new conversation

### 2.2 Shortcut features (3 gaps)
- **Auto-complete in compose**: Add suggestion popup when user types `!` or `@`
- **Edit-before-send**: Show expanded shortcut text with edit option before inserting
- **Most-used sort**: Add sort/filter to `ShortcutsScreen` by `usageCount`

### 2.3 RCS features (5 stubs → real)
- **RcsService.addReaction()**: Wire to `ReactionRepository` — persist reaction, link to message
- **RcsService.sendTypingIndicator()**: Implement via local broadcasts or RCS protocol
- **RcsService.sendReadReceipt()**: Implement read receipt persistence
- **RcsService.checkRcsCapability()**: Remove hardcoded `true` — implement actual capability discovery or at minimum a local cache
- **ChatDetailScreen**: Add reaction display/selection UI on messages (long-press or tap to react)

### 2.4 Shortcode expansion in compose (2 gaps)
- **Auto-complete suggestion**: Detect `!`/`@` trigger typing and show dropdown
- **Edit-before-send**: Intercept send with shortcut expansion preview

### 2.5 Signature features (3 gaps)
- **Auto-attach from Signature model**: Fix `EncryptionManager.generateMessageSignature()` to use `SignatureRepository` instead of SharedPrefs
- **Per-recipient signature UI**: Add signature selector per conversation in `ChatDetailScreen`
- **Example templates**: Add "Professional", "Casual", "Auto-Response" templates to `SignaturesScreen`

### 2.6 Scheduled message entry point (1 gap)
- **Schedule button in ChatDetailScreen**: Add clock icon next to send button → date/time picker → confirm

### 2.7 Social media integrations ✅
- **Matrix** — Full CS API: login, room sync, message send/receive, upload, mark-as-read (`features/matrix/`)
- **Telegram** — Bot API: verify token, poll updates, sync, send (`features/telegram/`)
- **Discord** — Bot API: list guilds/channels, fetch/send messages (`features/discord/`)
- **Facebook Messenger** — Graph API v18.0: Page Access Token auth, conversation sync, send (`features/messenger/`)
- SocialAccountsViewModel: login dialogs, sync buttons, disconnect/delete for all 4 platforms
- PlatformInfo.supportsApi flag for API-integrated platforms
- Dead code removed: `SocialMediaIntegrationService.kt` (old stub)

### 2.8 Google Drive backup (fully mocked → real)
- **GoogleDriveClient.kt**: Replace ALL 5 mocked methods:
  - `authenticate()` → real OAuth via `CredentialManager` / `GoogleSignIn`
  - `uploadFile()` → real Drive API upload via `DriveClient` or REST
  - `downloadFile()` → real Drive API download
  - `listBackupFiles()` → real Drive API file listing
  - `isAuthenticated()` → real token check
- **GoogleDriveBackupService.buildBackupData()**: Actually serialize shortcuts, signatures, themes from repositories (currently returns empty lists)

### 2.9 Chat input features (3 stubs → real)
- **Image picker**: Wire `onClick = { /* Pick image */ }` to `ActivityResultContracts.GetContent()`
- **File attachment**: Wire `onClick = { /* Attach file */ }` to `ActivityResultContracts.GetContent()`
- **Emoji picker**: Wire `onClick = { /* Emoji picker */ }` to `EmojiPicker` dialog

### 2.10 Media & reactions UI (3 gaps)
- **Emoji reactions on messages**: Add reaction picker (long-press message bubble) → save to `Reaction` entity
- **Sticker sending**: Add sticker tab to input area, wire to `RcsService.shareSticker()`
- **GIF search**: Add GIPHY API integration (requires API key) or offline GIF picker

---

## Phase 3: Core SMS fixes

### 3.1 Default SMS app registration ✅
- Manifest: `<action android:name="android.intent.action.VIEW" />` with `sms:` scheme
- Runtime permission checks in ConversationListScreen

### 3.2 Deprecated SmsManager ✅
- Uses `context.getSystemService(SmsManager::class.java)` (API 31+)

### 3.3 Encryption hardening
- **EncryptionManager**: AES-256-GCM ✅
- Fix silent plaintext fallback on encryption failure (should throw, not return unencrypted data)

---

## Phase 4: Testing

### 4.1 Fix existing unit tests ✅
- 43 tests pass across ChatViewModel (13 deps), ConversationListViewModel (7 deps), and others

### 4.2 Add critical tests
- Room DAO integration tests (androidTest)
- SMS send/receive flow tests
- Navigation tests

---

## Phase 5: Polish

### 5.1 Launcher icons ✅
- Generated from `assetts/comm_bubble.png` for all mipmap densities

### 5.2 Permission audit ✅
- All declared permissions are used; runtime permission requests in place

### 5.3 Clean up
- Remove stale `PROJECT_SUMMARY.md` (claims navigation not implemented, but it is)

---

## Completed (not in original plan)

### Package Rename
- `com.nexussms` → `com.nexusmedia.nexussms` across 91 files

### App Identity
- Application ID: `com.nexusmedia.nexussms`, version 1.0.3, code 103
- Company: Nexus Media, Canada

### Theme System Overhaul
- Self-contained HCT + TonalPalette generator (replaced restricted `material-color-utilities`)
- Google Fonts: Poppins (display) + Inter (body)
- 11 built-in themes with stable hardcoded IDs
- Dark mode toggle persists via SharedPreferences
- Per-conversation theme override via CompositionLocalProvider

### Database Migrations (v1 → v4)
- v1→v2: sourcePlatform columns on Conversation + Message
- v2→v3: sourceSmsId (Long?) + unique composite index for idempotent import
- v3→v4: contact_avatars table + ContactAvatarDao

### SMS Import Improvements
- Idempotent import (sourceSmsId unique index + IGNORE)
- Removed 100-message cap
- Re-sync from device (resyncFromDevice removes stale messages)
- Contact photo import from ContactsContract

### Avatar System
- NexusAvatar composable: Coil image loading + HCT tonal gradient fallback
- ContactAvatar data model + DAO + repository
- Avatar wired into conversation list + chat detail

### Chat UI Premium
- Message bubbles: asymmetric corners, avatar circles, delivery checkmarks, widthIn(max=300.dp)
- Bubble elevation from BubbleTheme applied as shadow
- AnimatedVisibility on message entry (fadeIn + slideInVertically)
- Per-conversation wallpaper with gradient presets + image URL
- Message order fix: DAO ORDER BY DESC + reverseLayout = true = newest at bottom

### Biometrics
- AppLockViewModel.verifyPin() SHA-256 hashes input
- AppLockScreen biometric button calls viewModel.verifyBiometric(activity)
- ProcessLifecycleOwner enforcement on relaunch

### Other Fixes
- Package visibility fix: `<queries>` block in AndroidManifest.xml
- ContactAvatarRepository for avatar management
- WallpaperPickerDialog in ChatDetailScreen
- NexusAvatar in ConversationListScreen top bar + ChatDetailScreen

---

## Excluded (requires third-party API keys / backend server)
- Real Google Jibe RCS backend (needs carrier/MNO agreement)
- WhatsApp Business API (needs Meta Business account + API review)
- GIPHY API (needs GIPHY API key)
- Signal protocol integration (needs signal-cli or libsignal)
