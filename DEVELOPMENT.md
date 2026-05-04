# NexusSMS Development Guide

## Quick Start

### Prerequisites
- Android Studio Giraffe or later
- Android SDK 34
- JDK 17
- Gradle 8.1.2

### Initial Setup

1. **Clone and Open Project**
   ```bash
   git clone https://github.com/yourusername/NexusSMS.git
   cd NexusSMS
   ```

2. **Open in Android Studio**
   - File → Open → Select NexusSMS directory

3. **Sync Gradle Dependencies**
   - Android Studio will prompt to sync (click "Sync Now")
   - Or run: `./gradlew sync`

4. **Create Emulator or Connect Device**
   - Tools → Device Manager → Create Virtual Device
   - Or connect a physical Android device via USB

5. **Run the App**
   - Click "Run" (▶️) in Android Studio or
   - Terminal: `./gradlew installDebug`

## Project Architecture

### Package Structure

```
com.nexussms/
├── data/
│   ├── database/          # Room Database DAOs and Database class
│   ├── models/            # Data entities
│   ├── repository/        # Repository pattern implementation
│   └── converters/        # Type converters for Room
├── di/                    # Dependency Injection (Hilt) modules
├── features/
│   ├── rcs/              # RCS messaging feature
│   ├── shortcodes/       # Shortcode expansion feature
│   ├── social/           # Social media integration
│   └── theme/            # Theme management
├── receivers/            # Broadcast receivers (SMS receiver)
├── security/             # Encryption and security
├── services/             # Background services
├── ui/
│   ├── screens/          # Composable screen components
│   ├── theme/            # Compose theme setup
│   └── viewmodels/       # MVVM ViewModels
├── MainActivity.kt       # App entry point
└── NexusSMSApplication.kt # Application class
```

## Key Features Implementation

### 1. Shortcode Messaging

Location: `features/shortcodes/ShortcodeExpansionService.kt`

**Usage:**
```kotlin
// Create shortcut
shortcodeExpansionService.createShortcut("!ato", "At The Office")

// Expand message
val expanded = shortcodeExpansionService.expandMessage("I'm !ato")
// Result: "I'm At The Office"
```

### 2. RCS Support

Location: `features/rcs/RcsService.kt`

**Capabilities:**
- Send RCS messages with rich content
- Typing indicators
- Read receipts
- Reactions
- Sticker sharing

### 3. Message Encryption

Location: `security/EncryptionManager.kt`

**Features:**
- AES-256 encryption
- Secure credential storage
- Key management via Android Keystore

### 4. Social Media Integration

Location: `features/social/SocialMediaIntegrationService.kt`

**Supported Platforms:**
- Facebook Messenger
- Discord
- Telegram
- Viber
- Matrix

### 5. Theme Management

Location: `features/theme/ThemeManager.kt`

**Built-in Themes:**
- Light (default)
- Dark
- Ocean
- Forest
- Sunset
- Purple Night
- Midnight
- Rose Gold

**Custom Themes:**
Users can create custom themes with full color customization.

## Database Schema

### Room Entities

#### Message
- `id` - Primary key
- `conversationId` - Foreign key to Conversation
- `senderId` - Sender phone/ID
- `recipientId` - Recipient phone/ID
- `content` - Message text
- `timestamp` - Message time
- `messageType` - SMS, RCS, SOCIAL
- `encryptionType` - NONE, AES256, SIGNAL
- `attachmentUrls` - JSON array of URLs

#### Conversation
- `id` - Primary key
- `participantPhone` - Contact phone number
- `participantName` - Contact display name
- `lastMessage` - Preview of last message
- `isPinned` - Pin status
- `isMuted` - Mute status
- `theme` - Applied theme
- `messageType` - Default message type

#### Shortcut
- `id` - Primary key
- `trigger` - Shortcode trigger (e.g., "!ato")
- `expansion` - Expanded text
- `usageCount` - Number of times used

#### Theme
- `id` - Primary key
- `name` - Theme name
- `primaryColor` - Primary color (hex)
- `secondaryColor` - Secondary color (hex)
- `bubbleColorSent` - Sent message bubble color
- `bubbleColorReceived` - Received message bubble color
- `isDarkMode` - Dark mode flag
- `isCustom` - Custom theme flag

#### SocialAccount
- `id` - Primary key
- `platform` - Platform name
- `accountId` - Platform account ID
- `username` - Platform username
- `accessToken` - OAuth access token
- `refreshToken` - OAuth refresh token
- `isActive` - Account active status

## ViewModels

### ConversationListViewModel
Manages the list of conversations and user interactions.

**Key Methods:**
- `loadConversations()` - Load all conversations
- `selectConversation()` - Select a conversation
- `deleteConversation()` - Delete a conversation
- `pinConversation()` - Pin a conversation
- `markAsRead()` - Mark as read

### ChatViewModel
Manages individual chat conversations.

**Key Methods:**
- `loadConversation()` - Load conversation details
- `sendMessage()` - Send a message
- `setMessageType()` - Set SMS/RCS mode
- `addReaction()` - Add emoji reaction

### SettingsViewModel
Manages app settings and preferences.

**Key Methods:**
- `setCurrentTheme()` - Change theme
- `createCustomTheme()` - Create custom theme
- `createSignature()` - Create message signature

## Permissions

Required permissions in AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<!-- ... and more -->
```

## Building & Deployment

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Testing
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Debugging Tips

### Enable Network Logging
Add to OkHttp interceptor chain:
```kotlin
.addInterceptor(HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
})
```

### Database Inspection
Use Room Inspector in Android Studio:
- Build → Analyze APK → Room Inspector

### Logcat Filtering
```bash
adb logcat | grep -i nexussms
```

## Performance Optimization

1. **Database Queries**
   - Use pagination for large message lists
   - Index frequently queried fields

2. **Encryption**
   - Cache encrypted data when possible
   - Use background threads for crypto operations

3. **UI**
   - Use LazyColumn for message lists
   - Implement proper lifecycle management

## Common Issues & Solutions

### Gradle Sync Issues
```bash
./gradlew clean
./gradlew sync
```

### Build Cache Problems
```bash
./gradlew build --no-build-cache
```

### Emulator Performance
- Use Android Studio's built-in emulator
- Enable Hardware Acceleration
- Allocate sufficient RAM (4GB minimum)

## Contributing Guidelines

1. Create feature branch: `git checkout -b feature/my-feature`
2. Follow Kotlin coding standards
3. Write unit tests for new features
4. Update documentation
5. Create pull request with detailed description

## Code Style

- Use Kotlin idioms
- Follow Google's Kotlin style guide
- Use meaningful variable names
- Add KDoc comments for public APIs

## Resources

- [Android Documentation](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## License

MIT License - See LICENSE file in project root
