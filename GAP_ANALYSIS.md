# NexusSMS — TRACKER.md vs Source Code Gap Analysis

> **Generated**: 2026-07-08  
> **Scope**: All ~3782 lines of TRACKER.md compared against actual source code in the project  
> **Source spec**: `/home/auz/Downloads/git/NexusSMS/TRACKER.md`

---

## 1. Features / APIs Not Yet Implemented

### 1.1 REST / Backend API (Section 4 of TRACKER.md)

**Status: NOT IMPLEMENTED — no backend server code exists anywhere in the project.**

The following endpoints specified in TRACKER.md have zero implementation:

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/v1/send-message` | POST | Send RCS message via API |
| `/api/v1/conversations` | GET | List conversations |
| `/api/v1/themes` | POST | Create theme |
| `/api/v1/themes/:themeId` | PUT | Update theme |
| `/api/v1/scheduled-messages` | POST | Create scheduled message |
| `/api/v1/scheduled-messages/:conversationId` | GET | Get scheduled messages for conversation |
| `/api/v1/scheduled-messages/:messageId` | PUT | Update scheduled message |
| `/api/v1/backup` | POST | Create backup |
| `/api/v1/restore` | POST | Restore from backup |
| `/api/v1/backup/schedule` | POST | Schedule backup |
| `/api/v1/auth/google` | POST | Google OAuth |
| `/api/v1/auth/social` | POST | Social media OAuth |

### 1.2 Google Drive Backup — Real Implementation

**Status: STUB / MOCK.** File: `GoogleDriveClient.kt`

| Method | Current Behavior | Required |
|---|---|---|
| `authenticate()` | Always returns `true` | Real Google Sign-In + OAuth flow |
| `uploadFile()` | Returns `"mock_file_id"` | Drive API v3 file creation |
| `downloadFile()` | Returns `null` | Drive API v3 file download |
| `listBackupFiles()` | Returns `emptyList()` | Drive API v3 file listing |
| `deleteFile()` | Body is empty | Drive API v3 file deletion |

`GoogleDriveBackupService.kt` depends on this client and is non-functional without it.  
`BackupWorker.kt` wraps it with WorkManager but cannot perform real backups.  
`BackupViewModel.createManualBackup()` inserts a MANUAL record into DB but never collects/upload data.

### 1.3 RCS — Real Backend Integration

**Status: STUB.** File: `RcsService.kt`

All RCS methods store data locally as fake messages instead of communicating with a real RCS backend:

| Method | Current Behavior |
|---|---|
| `sendRcsMessage()` | Inserts fake message via repo |
| `sendTypingIndicator()` | Inserts fake message `"typing_indicator"` |
| `sendReadReceipt()` | Inserts fake message `"read_receipt"` |
| `sendReaction()` | Inserts fake message `"reaction:{emoji}"` |
| `syncRcsMessages()` | No-op (empty method) |
| `checkRcsCapability()` | Returns `RcsCapability(true, true, true)` |
| `sendSticker()` | Stub — only logs |
| `sendGiphy()` | Stub — only logs |
| `startRcsSession()` | Sets `isSessionActive`, stores session |
| `endRcsSession()` | Clears session |
| `setDisplayName()` | Stores in map |
| `setProfilePhoto()` | Stores in map |

No Android RCS APIs (`RcsManager`, `RcsCapabilities`) are used.  
No actual network calls to an RCS relay/backend.

### 1.4 Social Media Integration — Real Platform APIs

**Status: STUB.** File: `SocialMediaIntegrationService.kt`

| Method | Current Behavior |
|---|---|
| `connectAccount()` | Updates `isConnected=true` in DB |
| `disconnectAccount()` | Updates `isConnected=false` in DB |
| `sendSocialMediaMessage()` | Stores as outgoing message in DB |
| `receiveSocialMediaMessages()` | Returns `emptyList()` |
| `syncMessagesFromPlatform()` | Returns `emptyList()` |

No actual API calls to Discord, Telegram, Facebook Messenger, Matrix, or Viber.

### 1.5 Media Attachment Handling

**Status: MISSING.** File: `ChatDetailScreen.kt`

- `AttachFile` button present but **no file picker** integration (`ActivityResultContracts` not used)
- `Image` icon button present but **no gallery/camera picker** integration
- No media compression logic per TRACKER.md Section 5.11 (GIPHY, stickers, media compression)

**MediaPicker.kt** exists at `/ui/components/MediaPicker.kt` but was not analyzed in detail initially.

### 1.6 Location Sharing

**Status: MISSING.**

- `Message` entity has a `location` field (String?)
- ChatDetailScreen.kt has a location icon button with `TODO("Location sharing")`
- No FusedLocationProviderClient, no map preview, no location message sending

### 1.7 Message Reactions UI

**Status: MISSING.**

- `Reaction` entity and `ReactionDao` / `ReactionRepository` exist
- `ReactionDao` has `getReactionsForMessage()` and `getReactionsByConversation()`
- `RcsService.sendReaction()` creates fake "reaction:{emoji}" messages
- **No UI** for adding or displaying reactions on messages in ChatDetailScreen.kt

### 1.8 iOS RCS Interop (Section 5.7)

**Status: NOT IMPLEMENTED on Android side.**

- `ios/` directory exists with `Package.swift` and `Sources/` (not analyzed)
- No cross-platform RCS sync code on Android
- No platform-agnostic message encoding/decoding

### 1.9 Contact Integration

**Status: MISSING.**

- `READ_CONTACTS` / `WRITE_CONTACTS` permissions declared in AndroidManifest.xml
- No contact picker (`ContactsContract`) for selecting conversation recipients
- No contact photo/sync integration

---

## 2. Database Schema

All 10 Room entities are **fully implemented** with all fields matching TRACKER.md (with minor naming differences noted below).

### 2.1 `Message` entity
- **File**: `data/models/Message.kt` (61 lines)
- **Table**: `messages`
- Fields:
  - `id: Long` (PK, auto-generate) — spec uses `Int`
  - `conversationId: String` (FK to Conversation)
  - `senderId: String?` — spec uses `Int?`
  - `content: String`
  - `timestamp: Date` (Long epoch stored via DateConverter)
  - `isRead: Boolean = false`
  - `isSent: Boolean = false`
  - `isEncrypted: Boolean = false`
  - `encryptionIV: String?`
  - `encryptedKey: String?`
  - `mimeType: String?`
  - `attachmentUri: String?`
  - `attachmentSize: Long?`
  - `location: String?`
  - `platform: String?` (for social media cross-posting)
- **Indices**: `[conversationId]`
- **Diff from spec**: `senderId` is `String?` (spec says `Int?`); `id` is `Long` (spec says `Int`)

### 2.2 `Conversation` entity
- **File**: `data/models/Conversation.kt` (50 lines)
- **Table**: `conversations`
- Fields:
  - `id: String` (PK) — spec uses `Int`
  - `participants: List<String>` (JSON string via JsonConverter)
  - `lastMessage: String?`
  - `lastMessageTimestamp: Long?`
  - `isPinned: Boolean = false`
  - `isArchived: Boolean = false`
  - `theme: String?`
  - `encryptionEnabled: Boolean = false`
  - `platform: String = "sms"`
  - `unreadCount: Int = 0`
- **Indices**: `[lastMessageTimestamp]`
- **Diff from spec**: `participants` is `List<String>` (spec: `List<Int>`) — actual uses phone numbers as identifiers, spec uses `Int` IDs; `id` is `String` (spec: `Int`)

### 2.3 `Shortcut` entity
- **File**: `data/models/Shortcut.kt` (35 lines)
- **Table**: `shortcuts`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `trigger: String` (unique)
  - `content: String`
  - `category: String?`
  - `usageCount: Int = 0`
  - `lastUsed: Date?`
- **Diff from spec**: **None significant** — spec name is not specified but structure matches

### 2.4 `Signature` entity
- **File**: `data/models/Signature.kt` (26 lines)
- **Table**: `signatures`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `name: String`
  - `content: String`
  - `isDefault: Boolean = false`
  - `format: String = "TEXT"` (TEXT/HTML/RICH_TEXT)
- **Diff from spec**: No significant differences

### 2.5 `ScheduledMessage` entity
- **File**: `data/models/ScheduledMessage.kt` (47 lines)
- **Table**: `scheduled_messages`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `conversationId: String`
  - `content: String`
  - `scheduledTime: Long`
  - `repeatType: String = "ONCE"` (ONCE/DAILY/WEEKLY/MONTHLY)
  - `isActive: Boolean = true`
  - `lastSent: Long?`
  - `createdAt: Long`
- **Indices**: `[conversationId]`
- **Diff from spec**: No significant differences

### 2.6 `Theme` entity
- **File**: `data/models/Theme.kt` (51 lines)
- **Table**: `themes`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `name: String`
  - `primaryColor: String`
  - `secondaryColor: String` ← **Spec calls this `accentColor`**
  - `backgroundColor: String`
  - `surfaceColor: String`
  - `isCustom: Boolean` ← **Spec calls this `isBuiltIn`** (logically inverted)
- **Diff from spec**: `secondaryColor` instead of `accentColor`; `isCustom` instead of `isBuiltIn` (inverted boolean logic)

### 2.7 `SocialAccount` entity
- **File**: `data/models/SocialAccount.kt` (36 lines)
- **Table**: `social_accounts`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `platform: String`
  - `accountName: String`
  - `accountId: String`
  - `accessToken: String?`
  - `isConnected: Boolean = false`
  - `lastSynced: Date?`
- **Diff from spec**: No significant differences

### 2.8 `Reaction` entity
- **File**: `data/models/Reaction.kt` (25 lines)
- **Table**: `reactions`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `messageId: Long` (FK to Message)
  - `senderId: String` — spec uses `Int`
  - `emoji: String`
  - `timestamp: Date`
- **Indices**: `[messageId, senderId]`
- **Diff from spec**: `senderId` is `String` (spec: `Int`)

### 2.9 `BackupMetadata` entity
- **File**: `data/models/BackupMetadata.kt` (33 lines)
- **Table**: `backup_metadata`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `backupTime: Long`
  - `backupSize: Long?`
  - `fileUri: String?`
  - `type: String` (MANUAL/AUTO)
  - `status: String` (SUCCESS/FAILED/IN_PROGRESS)
- **Diff from spec**: No significant differences

### 2.10 `AppSecuritySettings` entity
- **File**: `data/models/AppSecuritySettings.kt` (41 lines)
- **Table**: `app_security_settings`
- Fields:
  - `id: Long` (PK, auto-generate)
  - `appLockEnabled: Boolean = false`
  - `lockType: String = "PIN"` (PIN/PATTERN/PASSWORD)
  - `lockTimeout: Long = 300000` (5 min)
  - `biometricEnabled: Boolean = false`
  - `hideContentInRecents: Boolean = false`
  - `disableScreenshots: Boolean = false`
  - `sensitiveActions: String?` (JSON list of sensitive actions)
- **Diff from spec**: No significant differences

### 2.11 Database Configuration
- **File**: `data/database/NexusSMSDatabase.kt`
- **Version**: 1
- **Migrations**: Empty list in `NexusSMSDatabaseMigrations.kt`
- **TypeConverters**: `DateConverter.kt` (Date ↔ Long), `JsonConverter.kt` (List<String>, List<Int>, Map<String,List<String>>, Map<String,String>, List<Map<String,Any>>)

### 2.12 All DAOs
- **File**: `data/database/Daos.kt`
- All 10 DAOs with Insert/Update/Delete/Query methods
- Extra convenience methods beyond spec: `getPinnedConversations()`, `clearDefaultSignature()`, `getAllThemes()`, `getAccountsByPlatform()`
- All return types use Kotlin Flow for reactive observation

---

## 3. UI Screens

**All 12 screens implemented.** Summary table:

| # | Screen | File | Notes |
|---|---|---|---|
| 1 | Main (NavHost) | `MainScreen.kt` | Bottom nav: Messages / Settings (2 tabs only — no Backup or Security tabs) |
| 2 | Conversation List | `ConversationListScreen.kt` | LazyColumn, pinned section, search, swipe-to-delete |
| 3 | Chat Detail | `ChatDetailScreen.kt` | Message bubbles, input bar, RCS status, typing indicator — **reactions, media, location missing** |
| 4 | Settings | `SettingsScreen.kt` | Dark mode toggle, navigation to all sub-settings |
| 5 | Security Settings | `SecuritySettingsScreen.kt` | App lock, PIN type, biometric, timeout, sensitive actions |
| 6 | Shortcuts | `ShortcutsScreen.kt` | Searchable, add/edit/delete, category selector |
| 7 | Signatures | `SignaturesScreen.kt` | Add/edit/delete, default selector, format selector |
| 8 | Themes | `ThemesScreen.kt` | Built-in + custom, preview cards, color dialog |
| 9 | Scheduled Messages | `ScheduledMessagesScreen.kt` | List + add/edit with date/time picker |
| 10 | Social Accounts | `SocialAccountsScreen.kt` | Platform list, add/edit/delete, connect/disconnect |
| 11 | Backup | `BackupScreen.kt` | Create backup button, progress, restore, auto-schedule — **all simulated** |
| 12 | App Lock | `AppLockScreen.kt` | PIN entry, biometric fallback |

### Missing UI Features
- **Message reactions**: No UI to add/display emoji reactions on messages
- **Media attachments**: No file picker, gallery picker, or camera capture
- **Location sharing**: Location button with TODO stub
- **Contact picker**: No contact selection for new conversations
- **Bottom nav tabs**: Only 2 tabs (Messages, Settings) — no dedicated Backup or Security tabs as implied by Settings menu

---

## 4. Security Model

### 4.1 App Lock
- **AppLockManager.kt**: PIN/PATTERN/PASSWORD lock
  - SHA-256 hashing for PIN/password storage
  - `setLock()`, `verifyLock()`, `disableLock()`, `changeLock()` — all implemented
  - `EncryptedSharedPreferences` for storage
- **BiometricAuthManager.kt**: BiometricPrompt integration
  - `isBiometricAvailable()` checks BiometricManager
  - `authenticate()` with `BiometricPrompt` + crypto object
  - `authenticateWithResult()` callback-based auth
- **SessionManager.kt**: Session timeout management
  - Default timeout: 300s (configurable per TRACKER.md)
  - `startSession()`, `lockSession()`, `unlockSession()`, `isSessionTimedOut()`
- **AppLockScreen.kt**: PIN entry + biometric fallback UI

### 4.2 Encryption — SECURITY GAP vs Spec
- **EncryptionManager.kt** uses `AES/CBC/PKCS5Padding`
- **TRACKER.md specifies `AES/GCM/NoPadding`** (authenticated encryption)
- Impact: AES-CBC provides **no integrity checking** (no authentication tag). Vulnerable to padding oracle attacks and tampering.
- IV size: 16 bytes (CBC block size) vs spec's 12 bytes (GCM nonce)
- `encryptAES256()` / `decryptAES256()` — core encrypt/decrypt methods
- `encryptWithKey()` / `decryptWithKey()` — per-message key encryption
- `shouldEncryptForContact()` / `isEncryptedMessage()` — helpers

### 4.3 Additional Security UI
- `SecuritySettingsScreen.kt`: All settings toggles working
- `AppSecuritySettings` entity: All fields persisted
- `hideContentInRecents`, `disableScreenshots`, `sensitiveActions` — implemented in screen but no code to enforce at Activity/Window level was found in `MainActivity.kt`
- `LockScreenOverlay.kt`: Full lock screen overlay composable with biometric fallback

### 4.4 Permissions (AndroidManifest.xml)
- READ_SMS, SEND_SMS, RECEIVE_SMS — declared
- READ_CONTACTS, WRITE_CONTACTS — declared but not used
- READ_PHONE_STATE — declared
- INTERNET, ACCESS_NETWORK_STATE — declared
- CAMERA — declared but not used
- READ_MEDIA_IMAGES — declared but not used
- POST_NOTIFICATIONS — declared
- SCHEDULE_EXACT_ALARM — declared

---

## 5. State Management

### Sealed Classes (UiState)

| File | States |
|---|---|
| `ChatUiState.kt` | `Loading`, `Idle(conversation, messages, isRcsEnabled, typingUsers)`, `Error(message)` |
| `ShortcutUiState.kt` | `Loading`, `Success(shortcuts)`, `Error(message)` |

### ViewModel State Flows

| ViewModel | Key State | Updates Via |
|---|---|---|
| ChatViewModel | `chatUiState: StateFlow<ChatUiState>` | Messages from MessageDao, Conversation from ConversationDao |
| ConversationListViewModel | `conversations: StateFlow<List<Conversation>>`, `searchQuery` | ConversationDao.getAllConversations() |
| SettingsViewModel | `isDarkMode`, `currentTheme` | ThemeManager, Settings |
| SecuritySettingsViewModel | Security settings fields | AppSecuritySettingsDao |
| AppLockViewModel | Lock state, error | AppLockManager |
| BackupViewModel | `backupState`, `backupProgress` | Simulated (no real pipeline) |
| ShortcutsViewModel | `uiState: StateFlow<ShortcutUiState>` | ShortcutDao |
| SignaturesViewModel | `signatures: StateFlow<List<Signature>>` | SignatureDao |
| ThemesViewModel | `themes`, `builtInThemes`, custom theme color | ThemeDao + ThemeManager |
| ScheduledMessagesViewModel | `scheduledMessages` | ScheduledMessageDao |
| SocialAccountsViewModel | `accounts` | SocialAccountDao |

---

## 6. Complete File Inventory (app/src/main/java)

### Models (`data/models/`) — 10 files
1. `Message.kt`
2. `Conversation.kt`
3. `Shortcut.kt`
4. `Signature.kt`
5. `ScheduledMessage.kt`
6. `Theme.kt`
7. `SocialAccount.kt`
8. `Reaction.kt`
9. `BackupMetadata.kt`
10. `AppSecuritySettings.kt`

### Database (`data/database/`) — 3 files
1. `NexusSMSDatabase.kt`
2. `NexusSMSDatabaseMigrations.kt` (empty)
3. `Daos.kt`

### Converters (`data/converters/`) — 2 files
1. `DateConverter.kt`
2. `JsonConverter.kt`

### Repositories (`data/repository/`) — 8 files
1. `MessageRepository.kt`
2. `ConversationRepository.kt`
3. `ShortcutRepository.kt`
4. `SignatureRepository.kt`
5. `ThemeRepository.kt`
6. `ScheduledMessageRepository.kt`
7. `SocialAccountRepository.kt`
8. `ReactionRepository.kt`

### DI (`di/`) — 1 file
1. `AppModule.kt`

### UI Screens (`ui/screens/`) — 12 files
1. `MainScreen.kt`
2. `ConversationListScreen.kt`
3. `ChatDetailScreen.kt`
4. `SettingsScreen.kt`
5. `SecuritySettingsScreen.kt`
6. `ShortcutsScreen.kt`
7. `SignaturesScreen.kt`
8. `ThemesScreen.kt`
9. `ScheduledMessagesScreen.kt`
10. `SocialAccountsScreen.kt`
11. `BackupScreen.kt`
12. `AppLockScreen.kt`

### UI Components (`ui/components/`) — 10 files
1. `ChatBubble.kt` (as `MessageBubble.kt`)
2. `ConversationItem.kt`
3. `CommonComponents.kt`
4. `EmojiPicker.kt`
5. `PINEntryDialog.kt`
6. `LockScreenOverlay.kt`
7. `BackupProgressIndicator.kt`
8. `ThemeColorPicker.kt`
9. `ShortcutPreview.kt`
10. `MediaPicker.kt`

### UI Theme (`ui/theme/`) — 2 files
1. `Theme.kt`
2. `Type.kt`

### UI State (`ui/state/`) — 2 files
1. `ChatUiState.kt`
2. `ShortcutUiState.kt`

### ViewModels (`ui/viewmodels/`) — 6 dedicated files + 6 inline
1. `ChatViewModel.kt`
2. `ConversationListViewModel.kt`
3. `SettingsViewModel.kt`
4. `SecuritySettingsViewModel.kt`
5. `AppLockViewModel.kt`
6. `BackupViewModel.kt`
7. *Inline*: ShortcutsViewModel, SignaturesViewModel, ThemesViewModel, ScheduledMessagesViewModel, SocialAccountsViewModel (inside respective Screen files)

### Features

#### Security (`features/security/`) — 3 files
1. `AppLockManager.kt`
2. `BiometricAuthManager.kt`
3. `SessionManager.kt`

#### Backup (`features/backup/`) — 4 files
1. `GoogleDriveBackupService.kt`
2. `BackupWorker.kt`
3. `GoogleDriveClient.kt` (MOCK)
4. `models/BackupData.kt`

#### RCS (`features/rcs/`) — 1 file
1. `RcsService.kt` (STUB)

#### Social (`features/social/`) — 1 file
1. `SocialMediaIntegrationService.kt` (STUB)

#### Theme (`features/theme/`) — 1 file
1. `ThemeManager.kt`

#### Shortcodes (`features/shortcodes/`) — 1 file
1. `ShortcodeExpansionService.kt`

### Services (`services/`) — 2 files
1. `MessageService.kt`
2. `ScheduledMessageWorker.kt`

### Receivers (`receivers/`) — 1 file
1. `SmsReceiver.kt`

### Utils — 1 file
1. `Validators.kt`

### App-level — 2 files
1. `NexusSMSApplication.kt`
2. `MainActivity.kt`

### Not Found / Missing
- **`Security/EncryptionManager.kt`** is at `security/EncryptionManager.kt` (not in `features/security/`)
- **No `Color.kt`** file (the `ui/theme/` package only has `Theme.kt` and `Type.kt`)
- **No dedicated REST API server code** anywhere in project
- **No `ui/navigation/`** package — navigation is in `MainScreen.kt`

---

## 7. Summary of Gaps by Severity

### Critical (blocking real-world use)
1. **Encryption algorithm mismatch**: AES-CBC instead of AES-GCM — no integrity/authentication
2. **Google Drive backup**: All methods mocked — no real backup/restore
3. **RCS Service**: All methods are stubs — no real RCS communication
4. **No backend API server**: REST endpoints specified in TRACKER.md have no implementation

### High (major feature gaps)
5. **Social media integration**: All platform APIs are stubs
6. **Media attachments**: No file picker, gallery, camera integration
7. **Location sharing**: Field exists in model but no UI or implementation
8. **Message reactions**: Model+DAO exist but no UI
9. **Contact integration**: Permission declared but no contact picker

### Medium (polish and completeness)
10. **Theme field naming**: `secondaryColor` vs `accentColor`, `isCustom` vs `isBuiltIn`
11. **ID types**: `String`/`Long` in code vs `Int` in spec
12. **Bottom navigation**: Missing dedicated Backup/Security tabs
13. **No screenshots enforcement**: Fields declared but no Activity-level enforcement
14. **Database migrations**: Empty placeholder — schema changes will break existing installs

### Low (minor improvements)
15. **`compileSdk = 37`** (prerelease) with **`minSdk = 24`** vs spec's **`minSdk = 33`**
16. **iOS RCS interop**: No cross-platform sync on Android side
17. **GIPHY / Sticker packs**: Specified in Media System but not implemented

---

## 8. File Reference Index

| File Path | Lines | Role |
|---|---|---|
| `/home/auz/Downloads/git/NexusSMS/TRACKER.md` | ~3782 | Specification document |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/security/EncryptionManager.kt` | ~100 | AES-CBC encryption (needs GCM) |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/backup/GoogleDriveClient.kt` | ~21 | Mock Drive client |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/backup/GoogleDriveBackupService.kt` | ~100 | Backup service (depends on mock) |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/backup/BackupWorker.kt` | ~50 | WorkManager backup worker |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/ui/viewmodels/BackupViewModel.kt` | ~80 | Backup UI state (simulated) |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/rcs/RcsService.kt` | ~150 | Stub RCS implementation |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/social/SocialMediaIntegrationService.kt` | ~140 | Stub social media integration |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/data/database/Daos.kt` | ~300 | All 10 DAOs |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/data/database/NexusSMSDatabase.kt` | ~50 | Room DB (version 1) |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/data/database/NexusSMSDatabaseMigrations.kt` | ~10 | Empty migrations |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/di/AppModule.kt` | ~200 | Hilt DI module |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/ui/screens/MainScreen.kt` | ~150 | NavHost + bottom nav |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/ui/screens/ChatDetailScreen.kt` | ~400 | Chat UI (reactions/media/location missing) |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/ui/screens/BackupScreen.kt` | ~200 | Backup UI (simulated progress) |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/ui/components/EmojiPicker.kt` | ~200 | Emoji picker component |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/ui/components/LockScreenOverlay.kt` | ~80 | Lock screen overlay |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/security/AppLockManager.kt` | ~100 | PIN/PATTERN/PASSWORD lock |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/security/BiometricAuthManager.kt` | ~100 | Biometric auth |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/security/SessionManager.kt` | ~60 | Session timeout |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/ui/screens/SecuritySettingsScreen.kt` | ~300 | Security settings UI |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/shortcodes/ShortcodeExpansionService.kt` | ~100 | Shortcut expansion |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/features/theme/ThemeManager.kt` | ~120 | Theme management |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/receivers/SmsReceiver.kt` | ~120 | SMS reception |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/services/MessageService.kt` | ~180 | SMS/MMS sending |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/java/com/nexussms/services/ScheduledMessageWorker.kt` | ~80 | Scheduled message worker |
| `/home/auz/Downloads/git/NexusSMS/app/src/main/AndroidManifest.xml` | ~40 | Permissions + components |
| `/home/auz/Downloads/git/NexusSMS/app/build.gradle.kts` | ~60 | Build config |
