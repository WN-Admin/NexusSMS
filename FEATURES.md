# NexusSMS Features Documentation

## Table of Contents
1. [Shortcode Messaging](#shortcode-messaging)
2. [RCS Support](#rcs-support)
3. [Message Scheduling](#message-scheduling)
4. [Signatures](#signatures)
5. [Enhanced Security](#enhanced-security)
6. [Themes](#themes)
7. [Social Media Integration](#social-media-integration)
8. [Rich Media Support](#rich-media-support)

---

## Shortcode Messaging

### Overview
Create unlimited custom shortcuts for frequently used messages. Use `!` or `@` as initiators followed by your shortcode.

### How to Create Shortcuts

1. Open Settings → Shortcuts
2. Tap "Add New Shortcut"
3. Enter trigger (e.g., `!ato`, `@home`)
4. Enter expansion message (e.g., "At The Office")
5. Save

### Usage Examples

| Trigger | Expansion | Use Case |
|---------|-----------|----------|
| `!ato` | "At The Office" | Work status |
| `@home` | "I'm on my way home" | Travel updates |
| `!busy` | "I'm currently busy. I'll call you back." | Quick response |
| `!thanks` | "Thanks for reaching out!" | Quick appreciation |

### Keyboard Expansion
- Type shortcode in message field
- Auto-complete suggestion appears
- Select to expand
- Edit before sending if needed

### Advanced Features
- Per-contact shortcuts
- Shortcut categories
- Usage analytics
- Most-used tracking
- Quick access menu

---

## RCS Support

### What is RCS?
Rich Communication Services - Modern messaging protocol supporting:
- Rich text formatting
- File sharing
- Typing indicators
- Read receipts
- Emoji reactions
- Group messaging

### RCS Features in NexusSMS

#### 1. Rich Messaging
- **Text Formatting**: Bold, italic, underline
- **Text Size**: Multiple font sizes
- **Colors**: Full color palette
- **Hyperlinks**: Clickable links

#### 2. Typing Indicators
- See when recipients are typing
- Real-time communication feedback

#### 3. Read Receipts
- Know when messages are read
- Detailed delivery tracking

#### 4. Reactions
- Reply with emojis
- Quick emotional response
- React to any message

#### 5. File Sharing
- Send documents up to 100MB
- Share photos and videos
- Auto-thumbnail generation
- Preview in conversation

#### 6. Group Messaging
- Create group conversations
- Add/remove members
- Group settings and themes
- Manage group admins

### Enabling RCS
1. Settings → Integrations → RCS Support
2. Verify phone number
3. Accept terms
4. RCS automatically enables with compatible contacts

### Fallback to SMS
- If recipient doesn't support RCS, falls back to SMS
- Automatic detection
- No manual intervention needed

---

## Message Scheduling

### Schedule Messages

#### How to Schedule
1. Compose message
2. Tap "Schedule" (clock icon)
3. Select date and time
4. Confirm

#### Scheduled Message Screen
- View all scheduled messages
- Edit or cancel schedules
- View send history
- Reschedule failed messages

### Features
- **Timezone Support**: Automatic device timezone
- **Recurring Messages**: Repeat daily/weekly/monthly
- **Batch Scheduling**: Schedule multiple messages
- **Smart Timing**: Suggested optimal send times
- **Reminders**: Get notified before sending

### Use Cases
- Send birthday messages at perfect time
- Reminder messages to yourself
- Follow-up messages for business
- Holiday greetings
- Regular check-ins

---

## Signatures

### Creating Signatures

1. Settings → Signatures
2. Tap "Add Signature"
3. Enter signature name and content
4. Set as default (optional)
5. Save

### Multiple Signatures

Create different signatures for:
- Work communications
- Personal messages
- Customer support
- Newsletter-style messages

### Example Signatures

**Professional:**
```
Best regards,
[Your Name]
[Your Title]
[Company]
```

**Casual:**
```
Cheers,
[Your Name]
```

**Auto-Response:**
```
Thanks for your message!
I'll get back to you ASAP.
```

### Signature Settings
- Auto-attach to all messages
- Choose signature per recipient
- Edit signature during compose
- Rich text formatting support

---

## Enhanced Security

### Encryption

#### AES-256 Encryption
- Military-grade encryption
- Encrypt specific conversations
- End-to-end encryption
- Key management via Android Keystore

#### How to Enable
1. Select conversation
2. Settings → Encryption
3. Toggle "End-to-End Encryption"
4. Share encryption key with recipient

### Cross-Device Security

#### Multi-Device Support
- Secure sync across devices
- Phone, tablet, computer support
- Encrypted cloud backup
- Device verification

#### Device Management
1. Settings → Security → Devices
2. View connected devices
3. Approve/remove devices
4. Remote wipe option

### Credential Storage

#### Secure Storage
- EncryptedSharedPreferences
- Master Key encryption
- Biometric authentication
- No plaintext passwords

#### Two-Factor Authentication
1. Enable in Settings → Security
2. Choose 2FA method (SMS/App)
3. Verify phone or authenticator app
4. Save recovery codes

### Privacy Features
- Disappearing messages
- Read-only mode
- Screenshot blocking
- Incognito typing

---

## Themes

### Built-in Themes

#### 1. Light (Default)
- Clean, professional appearance
- Blue primary color
- Ideal for daytime use

#### 2. Dark
- AMOLED-optimized
- Purple accent color
- Easy on eyes at night

#### 3. Ocean
- Blue gradient theme
- Calming colors
- Water-inspired palette

#### 4. Forest
- Green nature-inspired
- Earthy tones
- Energizing feel

#### 5. Sunset
- Orange and warm tones
- Vibrant appearance
- Sunset colors

#### 6. Purple Night
- Deep purple theme
- Elegant appearance
- Dark mode compatible

#### 7. Midnight
- Minimalist dark theme
- Navy blue primary
- Professional look

#### 8. Rose Gold
- Luxury rose gold tones
- Modern aesthetic
- Premium feel

### Creating Custom Themes

1. Settings → Appearance → Create Theme
2. Set colors:
   - **Primary Color**: Main app color
   - **Secondary Color**: Accent color
   - **Sent Bubble Color**: Your message bubbles
   - **Received Bubble Color**: Others' bubbles
   - **Text Color**: Message text
   - **Background Color**: Screen background
3. Save theme
4. Preview before applying

### Theme Customization

#### Per-Conversation Themes
- Right-click conversation
- Select "Set Theme"
- Choose unique theme for each contact
- Personal customization

#### Dark Mode
- Auto-switch with system settings
- Manual override option
- Dark mode only themes
- Scheduled mode switching

---

## Social Media Integration

### Supported Platforms

#### Facebook Messenger
- Connect Facebook account
- See Messenger conversations in NexusSMS
- Send messages through native Messenger
- Notifications for new messages
- Group chat support

#### Discord
- Connect Discord server
- Receive direct messages
- Chat with multiple servers
- Voice channel integration
- Bot support

#### Telegram
- Import Telegram contacts
- Telegram message history
- Telegram group chats
- Bot interactions
- Secret chat support

#### Viber
- Viber account connection
- Message synchronization
- Viber group chats
- Sticker sharing
- Call integration

#### Matrix
- Open-source messaging
- Self-hosted support
- Multi-account setup
- E2E encryption
- Bridge support

### Setting Up Social Integration

1. Settings → Integrations
2. Tap platform you want to connect
3. Authorize with your account
4. Grant necessary permissions
5. Contacts synced automatically

### Unified Inbox Features

- **Message Switching**: Quick switch between platforms
- **Notification Unified**: Single notification area
- **Contact Search**: Search across all platforms
- **Status Updates**: Show status on all platforms
- **Profile Sync**: Synchronize profiles

### Privacy & Permissions

- Revoke access anytime
- Individual platform privacy settings
- What data is synced (configurable)
- Encryption per platform
- Selective contact sync

---

## Rich Media Support

### Stickers

#### Sticker Packs
- Pre-installed sticker collections
- Download additional packs
- Create custom sticker packs
- Organize by category

#### Using Stickers
1. Tap emoji icon
2. Select Stickers tab
3. Choose sticker
4. Send immediately

### GIPHY Integration

#### Search & Send GIFs
1. Tap GIF icon in compose
2. Search for GIF
3. Preview options
4. Select and send

#### Trending GIFs
- View trending GIFs
- Category browsing
- Saved GIFs library
- GIF history

### Emoji Support

#### Full Emoji Library
- 3000+ emojis
- Search functionality
- Emoji variants (skin tones)
- Recently used section
- Favorite emojis

#### Emoji Reactions
- React to messages with emojis
- Multiple reactions per message
- React count display
- Remove reactions

### File Sharing

#### Supported Formats
- Documents (PDF, DOC, XLS)
- Images (JPG, PNG, GIF, WEBP)
- Videos (MP4, MOV, MKV)
- Audio (MP3, WAV, AAC)
- Archives (ZIP, RAR)

#### Upload Limits
- SMS: Up to 10MB
- RCS: Up to 100MB
- Social platforms: Per-platform limits

#### File Management
- Preview before sending
- Rename files
- Batch upload
- Cloud storage integration

---

## Additional Features

### Message Search
- Full-text search across conversations
- Search by sender, date range
- Hashtag search
- Saved searches

### Conversation Management
- Archive conversations
- Pin important contacts
- Mute notifications
- Mark as read/unread

### Notifications
- Customizable notification sounds
- LED indicators
- Vibration patterns
- Do Not Disturb mode

### Backup & Restore
- Encrypted cloud backup
- Manual backup export
- Selective restoration
- Automatic scheduled backups

### Performance
- Optimized message loading
- Database compression
- Cache management
- Battery optimization

---

## Accessibility Features

- Voice message support
- Text-to-speech
- Screen reader support
- High contrast mode
- Large text support

---

## Tips & Tricks

1. **Quick Compose**: Long-press compose button
2. **Search Shortcuts**: Tap search, type @ or !
3. **Backup Before Reinstall**: Export conversations
4. **Theme by Time**: Different themes for day/night
5. **Signature Automation**: Auto-append to all messages

---

For more information or feature requests, visit the GitHub repository or contact support.
