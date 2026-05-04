# Contributing to NexusSMS

Thank you for your interest in contributing to NexusSMS! We welcome contributions from the community. This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

- Be respectful and inclusive
- No harassment or discrimination
- Focus on constructive feedback
- Respect intellectual property

## Getting Started

### Prerequisites
- Android Studio Giraffe or later
- JDK 17
- Git
- Kotlin knowledge

### Setup Development Environment

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/your-username/NexusSMS.git
   cd NexusSMS
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/original-owner/NexusSMS.git
   ```
4. Create feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Workflow

### 1. Create an Issue First
- Describe the bug or feature
- Provide context and examples
- Wait for feedback before coding

### 2. Write Code
- Follow Kotlin style guide
- Write clean, readable code
- Add comments for complex logic
- Use meaningful variable names

### 3. Write Tests
- Unit tests for business logic
- Integration tests for database
- UI tests for critical flows
- Aim for 80%+ code coverage

### 4. Commit Changes
```bash
git add .
git commit -m "feat: Add shortcode expansion for messages"
```

### Commit Message Format
```
<type>: <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test additions
- `chore`: Build/dependency changes

Example:
```
feat: Add RCS typing indicators

Implement typing indicator support for RCS messages
- Send typing notification when user starts typing
- Display typing indicator for received messages
- Auto-dismiss after 3 seconds of inactivity

Fixes #123
```

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a PR on GitHub with:
- Clear title
- Detailed description
- Screenshots/GIFs for UI changes
- Reference to related issues

## Code Style Guide

### Kotlin Conventions

#### Naming
```kotlin
// Classes: PascalCase
class UserProfile

// Functions/Variables: camelCase
fun sendMessage()
val messagePriority

// Constants: UPPER_SNAKE_CASE
const val MAX_MESSAGE_LENGTH = 160
```

#### Function Organization
```kotlin
class ChatViewModel {
    // Properties
    private val messageRepository: MessageRepository
    
    // Lifecycle methods
    override fun onCleared() { }
    
    // Public methods
    fun sendMessage() { }
    
    // Private methods
    private fun validateMessage() { }
}
```

#### Documentation
```kotlin
/**
 * Sends an encrypted message to the specified recipient.
 *
 * @param phoneNumber The recipient's phone number
 * @param content The message content
 * @param encryptionType The encryption type to use
 * @return The message ID if successful, null otherwise
 *
 * @throws IllegalArgumentException if phoneNumber is invalid
 */
fun sendEncryptedMessage(
    phoneNumber: String,
    content: String,
    encryptionType: String = "AES256"
): Long?
```

### Architecture Principles

1. **Separation of Concerns**
   - Data layer: Repository pattern
   - Business logic: Use cases/ViewModels
   - UI: Composable components

2. **Dependency Injection**
   - Use Hilt for DI
   - Constructor injection preferred
   - Avoid service locators

3. **Reactive Programming**
   - Use Flow for data streams
   - Coroutines for async operations
   - Avoid blocking calls

### Testing

#### Unit Tests
```kotlin
@Test
fun `test message encryption`() {
    val plaintext = "Hello World"
    val encrypted = encryptionManager.encryptAES256(plaintext)
    val decrypted = encryptionManager.decryptAES256(encrypted)
    
    assertEquals(plaintext, decrypted)
}
```

#### Integration Tests
```kotlin
@Test
fun `test message insertion and retrieval`() = runTest {
    val message = Message(
        id = 1,
        conversationId = 1,
        content = "Test message"
    )
    
    messageDao.insertMessage(message)
    val retrieved = messageDao.getMessage(1).first()
    
    assertEquals(message, retrieved)
}
```

## Pull Request Process

1. **Update Documentation**
   - Update README if adding features
   - Add/update DEVELOPMENT.md
   - Update FEATURES.md if applicable

2. **Ensure Tests Pass**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

3. **Check Code Quality**
   ```bash
   ./gradlew detekt
   ./gradlew ktlint
   ```

4. **Verify Build**
   ```bash
   ./gradlew build
   ```

5. **Screenshot/Demo**
   - Provide screenshots for UI changes
   - Screen recording for complex features
   - Before/after comparisons

## Review Process

- Maintainers will review your PR
- Constructive feedback provided
- Changes may be requested
- Once approved, PR will be merged

## Areas for Contribution

### High Priority
- Bug fixes
- Performance improvements
- Security enhancements
- Documentation improvements

### Medium Priority
- New features
- UI/UX improvements
- Accessibility enhancements
- Localization

### Help Wanted
- Look for issues labeled "help wanted"
- Good first issue for new contributors
- Mentorship available

## Reporting Issues

### Bug Reports
- Clear title and description
- Steps to reproduce
- Expected vs actual behavior
- Device/Android version info
- Logcat output

### Feature Requests
- Motivation and use case
- Proposed implementation (optional)
- Mockups/wireframes (if relevant)
- Acceptance criteria

## Resources

- [Android Developer Guide](https://developer.android.com)
- [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)
- [Architecture Patterns](https://developer.android.com/jetpack/guide)
- [Testing Guide](https://developer.android.com/training/testing)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Questions?

- Open an issue with tag `[QUESTION]`
- Discussions board (if available)
- Email maintainers

---

Thank you for contributing to NexusSMS! 🎉
