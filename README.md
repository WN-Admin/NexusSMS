# NexusSMS

A feature-rich Android SMS/MMS messaging application with RCS support, end-to-end encryption, social media integration, and a fully customizable UI built with Jetpack Compose and Material Design 3.

**By Nexus Media** | admin@watchnexus.ca | Canada

### App Identity
| Field | Value |
|---|---|
| Application ID | `com.nexusmedia.nexussms` |
| Namespace | `com.nexusmedia.nexussms` |
| App Label | NexusSMS |
| Version | 1.0.3 (code: 103) |

## Features

### Messaging
- **SMS/MMS** - Send and receive text and multimedia messages
- **RCS (Rich Communication Services)** - Typing indicators, read receipts, reactions, and sticker sharing with carrier-capable contacts
- **Message Scheduling** - Schedule messages with date/time picker, supports ONCE/DAILY/WEEKLY/MONTHLY recurrence
- **Message Encryption** - AES-256-GCM encryption for sensitive messages with per-contact encryption toggles

### Shortcodes
- Create custom text expansion shortcuts with `!` or `@` triggers (e.g., `!brb` expands to "Be right back")
- Sort shortcuts by usage count to surface most-used expansions
- Auto-complete suggestions while typing in the chat input
- Per-contact shortcut overrides and category organization

### Signatures
- Multiple message signatures with default selection
- Template library: Professional, Casual, Auto-Response, Work Hours, Out of Office
- Rich text format support (TEXT/HTML)

### Themes
- 8 built-in professional themes plus unlimited custom themes
- Per-conversation theme customization
- Color picker with hex code input
- Dark and light mode with system-aware switching

### Security
- **App Lock** - PIN, pattern, or password protection with biometric fallback
- **Session Management** - Configurable timeout with auto-lock
- **Secure Storage** - EncryptedSharedPreferences with Android Keystore-backed master key
- **Screenshot Prevention** - Optional FLAG_SECURE enforcement
- **Content Privacy** - Hide message content in recent apps

### Social Media Integration
Connect to messaging platforms via direct API integration:
- **Matrix** — Full Matrix Client-Server API: OAuth login, room sync, message send/receive
- **Telegram** — Bot API via @BotFather: verify token, poll updates, send messages
- **Discord** — Bot API: list guilds/channels, fetch/send messages
- **Facebook Messenger** — Graph API v18.0: Page Access Token auth, conversation sync, send
- **Signal** — Via local app detection (read-only)
- **Slack** — Via local app detection (read-only)

Each API-connected platform shows as a filterable tab in the conversation list. Messages sync from the platform into the local Room database and can be replied to from within NexusSMS.

### Backup & Restore
- **Google Drive Backup** - Real Google Drive API v3 integration with OAuth2 authentication
- Encrypted backup upload with AES-256
- Automatic backup scheduling (hourly/daily/weekly/monthly)
- One-tap restore from backup history

### Chat Features
- Emoji picker with 8 categories and search
- Sticker picker with 6 categories
- Image and file attachments
- Location sharing via Google Maps links
- Message reactions (long-press to react)
- Shortcut auto-complete bar

## Architecture

```
com.nexusmedia.nexussms/
├── data/
│   ├── models/          # 10 Room entities
│   ├── database/        # NexusSMSDatabase, DAOs, migrations
│   ├── repository/      # 8 repository classes
│   └── converters/      # Date, JSON type converters
├── features/
│   ├── backup/          # Google Drive backup/restore
│   ├── rcs/             # RCS messaging service
│   ├── social/          # Social media integration
│   ├── shortcodes/      # Text expansion service
│   ├── security/        # App lock, biometrics, session
│   └── theme/           # Theme manager
├── security/            # AES-256-GCM encryption
├── services/            # MessageService, ScheduledMessageWorker
├── receivers/           # SmsReceiver
├── di/                  # Hilt dependency injection
├── ui/
│   ├── screens/         # 12 composable screens
│   ├── components/      # Reusable UI components
│   ├── viewmodels/      # 6 ViewModels
│   ├── state/           # UI state classes
│   └── theme/           # Material 3 theme
└── utils/               # Validators
```

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material Design 3 |
| Architecture | MVVM, Repository Pattern |
| Database | Room (10 entities, 10 DAOs) |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Networking | Retrofit, OkHttp, Google Drive API v3 |
| Security | AES-256-GCM, EncryptedSharedPreferences, BiometricPrompt |
| Background | WorkManager |
| Image Loading | Coil |
| Logging | Timber |

## Build

### Requirements
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35
- Min SDK: 24 (Android 7.0)

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
Set environment variables for signing:
```bash
export KEYSTORE_PASSWORD="your-password"
export KEY_ALIAS="your-alias"
export KEY_PASSWORD="your-key-password"
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest
```

## CI/CD

GitHub Actions workflow (`.github/workflows/android.yml`) runs on every push/PR:
1. **Lint** - Android lint checks
2. **Unit Tests** - JUnit + MockK test suite
3. **Debug Build** - Assemble debug APK
4. **Release Build** - Assemble signed release APK on tag push

## Permissions

| Permission | Purpose |
|---|---|
| `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS` | Core SMS functionality |
| `READ_CONTACTS` | Contact name resolution |
| `READ_PHONE_STATE` | Device identification |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Network communication |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Location sharing |
| `CAMERA`, `READ_MEDIA_IMAGES` | Media attachments |
| `POST_NOTIFICATIONS` | Message notifications |
| `SCHEDULE_EXACT_ALARM` | Scheduled messages |

## License

MIT License - see [LICENSE](LICENSE) for details.
