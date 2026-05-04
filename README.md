# NexusSMS - Advanced Android Messaging App

An ultra-modern, feature-rich Android SMS and messaging application with support for RCS, social media integration, advanced security, unlimited themes, and much more.

## Features

### 1. **Unlimited Shortcode Messaging Shortcuts**
- Create custom shortcuts using `!` or `@` prefixes
- Example: `!ato` → "At The Office"
- Automatic expansion during message composition
- Usage tracking and analytics
- Per-contact shortcut management

### 2. **RCS (Rich Communication Services) Support**
- Similar to Google Messages
- Proprietary protocol for enhanced messaging
- Typing indicators
- Read receipts
- Reaction support
- File sharing capabilities

### 3. **Message Scheduling & Signatures**
- Schedule messages for later delivery
- Create multiple message signatures
- Default signature selection
- Rich text support

### 4. **Enhanced Security Features**
- AES-256 encryption for all messages
- Cross-device communication (phones, tablets, computers)
- Encrypted credential storage
- End-to-end encryption similar to Signal and Pulse SMS
- Secure backup and recovery

### 5. **Completely Ad-Free**
- No advertisements
- No tracking
- No premium features locked behind paywalls

### 6. **Unlimited Theme Support**
- 6+ built-in professional themes
- Custom theme creation
- Per-conversation theme customization
- Color picker with hex code support
- Dark and light mode themes

### 7. **Native Light and Dark Mode**
- Material Design 3 support
- Dynamic color support (Android 12+)
- Automatic theme switching based on system settings
- Manual theme override

### 8. **Rich Media & Sticker Support**
- Integrated emoji picker
- Giphy GIF support
- Sticker library
- Image and file sharing
- Similar to Microsoft Teams experience

### 9. **Social Media Platform Integration**
Supported platforms:
- **Facebook Messenger**
- **Discord**
- **Telegram**
- **Viber**
- **Matrix**

Unified inbox for all platforms with seamless switching.

## Project Structure

```
NexusSMS/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/nexussms/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── NexusSMSApplication.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── models/
│   │   │   │   │   ├── database/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── converters/
│   │   │   │   ├── services/
│   │   │   │   │   ├── MessageService.kt
│   │   │   │   │   └── ScheduledMessageWorker.kt
│   │   │   │   ├── receivers/
│   │   │   │   │   └── SmsReceiver.kt
│   │   │   │   ├── features/
│   │   │   │   │   ├── shortcodes/
│   │   │   │   │   ├── rcs/
│   │   │   │   │   ├── social/
│   │   │   │   │   └── theme/
│   │   │   │   ├── security/
│   │   │   │   │   └── EncryptionManager.kt
│   │   │   │   ├── di/
│   │   │   │   │   └── AppModule.kt
│   │   │   │   └── ui/
│   │   │   │       ├── screens/
│   │   │   │       ├── viewmodels/
│   │   │   │       └── theme/
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Technology Stack

### Architecture
- **MVVM** (Model-View-ViewModel) with Clean Architecture principles
- **Repository Pattern** for data access
- **Dependency Injection** using Hilt

### UI Framework
- **Jetpack Compose** for modern declarative UI
- **Material Design 3** components

### Database
- **Room** for local database management
- Type-safe database queries

### Networking
- **Retrofit** for API calls
- **OkHttp** for HTTP client
- **Gson** for JSON serialization

### Security
- **AndroidX Security** crypto
- **AES-256** encryption
- **Bouncy Castle** for cryptography

### Background Tasks
- **WorkManager** for scheduled messages
- Reliable task scheduling

### Other Libraries
- **Kotlin Coroutines** for async operations
- **Jetpack Navigation** (for future navigation impl.)
- **Coil** for image loading

## Installation & Setup

### Prerequisites
- Android Studio (latest version)
- Android SDK 34
- Kotlin 1.9.10 or later
- Gradle 8.1.2 or later

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/NexusSMS.git
   cd NexusSMS
   ```

2. **Open in Android Studio**
   - File → Open → Select NexusSMS folder

3. **Sync Gradle**
   - Android Studio will automatically sync dependencies

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on emulator or device**
   ```bash
   ./gradlew installDebug
   ```

## Key Components

### Data Models
- `Message` - SMS/RCS message entity
- `Conversation` - Conversation thread
- `Shortcut` - Shortcode expansion
- `ScheduledMessage` - Scheduled message
- `UserSignature` - Message signature
- `Theme` - Custom theme
- `SocialAccount` - Connected social media account

### Repositories
- `MessageRepository` - Message data access
- `ConversationRepository` - Conversation management
- `ShortcutRepository` - Shortcode management
- `ScheduledMessageRepository` - Scheduled message management
- `ThemeRepository` - Theme management
- `SocialAccountRepository` - Social account management

### Services
- `MessageService` - Send and manage messages
- `RcsService` - RCS communication
- `ShortcodeExpansionService` - Shortcode expansion
- `SocialMediaIntegrationService` - Social platform integration
- `EncryptionManager` - Encryption and security
- `ThemeManager` - Theme management

### ViewModels
- `ConversationListViewModel` - Manage conversation list
- `ChatViewModel` - Manage individual chat
- `SettingsViewModel` - Manage settings and preferences

## API Integration Points

### SMS/RCS APIs
- Android Telephony Manager
- Android SMS Manager
- Custom RCS Protocol Handler

### Social Media APIs
- Facebook Graph API
- Discord Bot API
- Telegram Bot API
- Viber Bot API
- Matrix Client-Server API

## Security Features

1. **End-to-End Encryption**
   - AES-256 encryption for messages
   - Secure key management

2. **Credential Storage**
   - EncryptedSharedPreferences for sensitive data
   - Master Key encryption

3. **Multi-Device Support**
   - Secure sync across devices
   - Cross-platform communication

## Building Release APK

```bash
./gradlew assembleRelease
```

The signed APK will be available in `app/build/outputs/apk/release/`

## Contributing

Contributions are welcome! Please follow these guidelines:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License - See LICENSE file for details

## Support

For issues, questions, or feature requests, please open an issue on GitHub.

## Future Enhancements

- [ ] Call integration
- [ ] Video call support
- [ ] Screen share capability
- [ ] Document scanning
- [ ] Advanced filtering and search
- [ ] Message backup and restore
- [ ] Cloud sync
- [ ] Accessibility improvements
- [ ] Translation support

## Changelog

### Version 1.0.0
- Initial release with core features
- SMS and RCS support
- Theme customization
- Social media integration
- Message scheduling
- End-to-end encryption
