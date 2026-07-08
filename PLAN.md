# NexusSMS — Deployment Readiness Plan

## Phase 1: Build System & Infrastructure

### 1.1 Fix compileSdk (37 → 35)
- `app/build.gradle.kts`: change `compileSdk = 37` to `compileSdk = 35`
- Verify Room schema export, Compose compiler target compatibility

### 1.2 Enable R8 ProGuard minification
- `app/build.gradle.kts`: set `isMinifyEnabled = true` for release
- Review existing `proguard-rules.pro` — keep rules for Room, Hilt, Gson, OkHttp, Compose, Navigation, Biometric, Drive, WorkManager, Timber are already defined
- Test release build after enabling

### 1.3 Configure release signing
- Generate debug keystore at project level (or document the requirement)
- Add `signingConfigs { release { ... } }` block in `app/build.gradle.kts`

### 1.4 Set up GitHub Actions CI
- `lint` + `testDebug` + `assembleDebug` on every push/PR
- `assembleRelease` on tag push

## Phase 2: Complete Features from Docs (no stubs)

### 2.1 Navigation wiring (2 blockers)
- **ConversationListScreen.kt:151**: `clickable { /* Open conversation */ }` → navigate to `chat/{conversationId}`
- **ConversationListScreen.kt:68**: FAB `onClick = { /* Start new conversation */ }` → navigate to new conversation screen or dialog

### 2.2 Shortcut features (3 gaps)
- **Auto-complete in compose**: Add suggestion popup in `ChatDetailScreen` when user types `!` or `@`
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

### 2.7 Social media integrations (5 stubs → real)
- **SocialMediaIntegrationService.disconnectAccount()**: Implement — update DB, revoke token
- **SocialMediaIntegrationService.syncMessagesFromPlatform()**: Implement — query platform API via Retrofit/OkHttp
- **SocialMediaIntegrationService.updateAccountToken()**: Implement — persist new token
- **SocialAccountsScreen**: Add full OAuth-like connect flow (platform selection → auth → permissions)
- Note: Real Facebook/Telegram/Discord/Viber/Matrix APIs require app registration + API keys; implement the client-side framework with web hook simulation

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

## Phase 3: Core SMS fixes

### 3.1 Default SMS app registration
- Add `<action android:name="android.intent.action.VIEW" />` with `sms:` scheme to manifest
- Add `android.provider.Telephony.SMS_DEFAULT_APPLICATION` action
- Add default SMS app check + prompt in onboarding/settings
- Request `WRITE_SMS` permission

### 3.2 Deprecated SmsManager
- **MessageService.kt:83**: Replace `SmsManager.getDefault()` with `context.getSystemService(SmsManager::class.java)` (API 31+)

### 3.3 Encryption hardening
- **EncryptionManager**: Review AES-CBC vs AES-GCM as flagged in gap analysis
- Fix silent plaintext fallback on encryption failure (should throw, not return unencrypted data)

## Phase 4: Testing

### 4.1 Fix existing unit tests
- Verify all 10 repository/ViewModel tests compile and pass
- Fix any mock/assertion issues

### 4.2 Add critical tests
- Room DAO integration tests (androidTest)
- SMS send/receive flow tests
- Navigation tests

## Phase 5: Polish

### 5.1 Launcher icons
- Generate PNG fallbacks for all mipmap densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)

### 5.2 Permission audit
- Verify all 13 declared permissions are actually used
- Add runtime permission requests where missing

### 5.3 Clean up
- Remove `ios/` directory (BUGFIXES says already removed)
- Remove stale `PROJECT_SUMMARY.md` (claims navigation not implemented, but it is)

## Excluded (requires third-party API keys / backend server)
- Real Google Jibe RCS backend (needs carrier/MNO agreement)
- Facebook Messenger API (needs Meta app review)
- Telegram Bot API (needs bot token from @BotFather)
- Discord API (needs Discord app registration)
- Viber API (needs Vider REST API token)
- Matrix homeserver (needs server)
- GIPHY API (needs GIPHY API key)
