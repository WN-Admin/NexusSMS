import Foundation
import CryptoKit
import Combine
import SwiftUI

final class LocalStorage {
    static let shared = LocalStorage()
    private let defaults = UserDefaults.standard

    func store<T: Codable>(_ value: T, forKey key: String) {
        guard let data = try? JSONEncoder().encode(value) else { return }
        defaults.set(data, forKey: key)
    }

    func retrieve<T: Codable>(_ type: T.Type, forKey key: String) -> T? {
        guard let data = defaults.data(forKey: key) else { return nil }
        return try? JSONDecoder().decode(type, from: data)
    }
}

final class ShortcutService: ObservableObject {
    @Published private(set) var shortcuts: [Shortcut] = []
    private let storageKey = "nexussms_shortcuts"

    init() {
        shortcuts = LocalStorage.shared.retrieve([Shortcut].self, forKey: storageKey) ?? []
        if shortcuts.isEmpty {
            addDefaultShortcuts()
        }
    }

    private func save() {
        LocalStorage.shared.store(shortcuts, forKey: storageKey)
    }

    func addDefaultShortcuts() {
        shortcuts = [
            Shortcut(id: UUID(), trigger: "!ato", expansion: "At The Office", category: "Status", usageCount: 0),
            Shortcut(id: UUID(), trigger: "@home", expansion: "I'm on my way home.", category: "Travel", usageCount: 0),
            Shortcut(id: UUID(), trigger: "!brb", expansion: "Be right back.", category: "Quick Reply", usageCount: 0)
        ]
        save()
    }

    func addShortcut(trigger: String, expansion: String, category: String = "") {
        let shortcut = Shortcut(id: UUID(), trigger: trigger, expansion: expansion, category: category, usageCount: 0)
        shortcuts.append(shortcut)
        save()
    }

    func expand(message: String) -> String {
        var expanded = message
        let tokens = message.split(separator: " ")
        for token in tokens {
            if token.hasPrefix("!") || token.hasPrefix("@") {
                if let shortcut = shortcuts.first(where: { $0.trigger == token }) {
                    expanded = expanded.replacingOccurrences(of: String(token), with: shortcut.expansion)
                    incrementUsage(for: shortcut.id)
                }
            }
        }
        return expanded
    }

    func incrementUsage(for id: UUID) {
        if let index = shortcuts.firstIndex(where: { $0.id == id }) {
            shortcuts[index].usageCount += 1
            save()
        }
    }

    func deleteShortcut(_ id: UUID) {
        shortcuts.removeAll { $0.id == id }
        save()
    }
}

final class ThemeService: ObservableObject {
    @Published private(set) var themes: [ThemeModel] = []
    @Published var currentTheme: ThemeModel?
    private let storageKey = "nexussms_themes"

    init() {
        themes = LocalStorage.shared.retrieve([ThemeModel].self, forKey: storageKey) ?? []
        if themes.isEmpty {
            themes = ThemeService.defaultThemes
            save()
        }
        currentTheme = themes.first
    }

    private func save() {
        LocalStorage.shared.store(themes, forKey: storageKey)
    }

    func selectTheme(_ theme: ThemeModel) {
        currentTheme = theme
    }

    func addTheme(_ theme: ThemeModel) {
        themes.append(theme)
        save()
    }

    func deleteTheme(_ id: UUID) {
        themes.removeAll { $0.id == id }
        save()
    }

    static var defaultThemes: [ThemeModel] {
        [
            ThemeModel(
                id: UUID(),
                name: "Light",
                primaryColor: ColorData(hex: "#2196F3"),
                secondaryColor: ColorData(hex: "#03DAC6"),
                bubbleSentColor: ColorData(hex: "#2196F3"),
                bubbleReceivedColor: ColorData(hex: "#E8E8E8"),
                textColor: ColorData(hex: "#000000"),
                backgroundColor: ColorData(hex: "#FFFFFF"),
                isDarkMode: false,
                isCustom: false
            ),
            ThemeModel(
                id: UUID(),
                name: "Dark",
                primaryColor: ColorData(hex: "#BB86FC"),
                secondaryColor: ColorData(hex: "#03DAC6"),
                bubbleSentColor: ColorData(hex: "#BB86FC"),
                bubbleReceivedColor: ColorData(hex: "#3F3F3F"),
                textColor: ColorData(hex: "#FFFFFF"),
                backgroundColor: ColorData(hex: "#121212"),
                isDarkMode: true,
                isCustom: false
            )
        ]
    }
}

final class SecurityService {
    static let shared = SecurityService()
    private let symmetricKey = SymmetricKey(size: .bits256)

    func encrypt(_ text: String) -> String {
        let data = Data(text.utf8)
        if let sealed = try? AES.GCM.seal(data, using: symmetricKey) {
            return sealed.combined?.base64EncodedString() ?? text
        }
        return text
    }

    func decrypt(_ base64: String) -> String {
        guard let data = Data(base64Encoded: base64),
              let sealed = try? AES.GCM.SealedBox(combined: data),
              let decrypted = try? AES.GCM.open(sealed, using: symmetricKey) else {
            return base64
        }
        return String(decoding: decrypted, as: UTF8.self)
    }
}

final class SocialIntegrationService: ObservableObject {
    @Published private(set) var accounts: [SocialAccount] = []
    private let storageKey = "nexussms_social_accounts"

    init() {
        accounts = LocalStorage.shared.retrieve([SocialAccount].self, forKey: storageKey) ?? []
    }

    func connect(platform: String, accountId: String, username: String, displayName: String) {
        let account = SocialAccount(
            id: UUID(),
            platform: platform,
            accountId: accountId,
            username: username,
            accessToken: "token_\(UUID().uuidString)",
            refreshToken: "",
            isActive: true,
            displayName: displayName
        )
        accounts.append(account)
        save()
    }

    private func save() {
        LocalStorage.shared.store(accounts, forKey: storageKey)
    }

    func disconnectAccount(_ id: UUID) {
        accounts.removeAll { $0.id == id }
        save()
    }
}

final class MessageService: ObservableObject {
    @Published private(set) var conversations: [Conversation] = []
    @Published private(set) var messages: [Message] = []
    @Published private(set) var scheduledMessages: [ScheduledMessage] = []
    private let conversationKey = "nexussms_conversations"
    private let messageKey = "nexussms_messages"
    private let scheduleKey = "nexussms_scheduled_messages"

    init() {
        conversations = LocalStorage.shared.retrieve([Conversation].self, forKey: conversationKey) ?? []
        messages = LocalStorage.shared.retrieve([Message].self, forKey: messageKey) ?? []
        scheduledMessages = LocalStorage.shared.retrieve([ScheduledMessage].self, forKey: scheduleKey) ?? []
        if conversations.isEmpty {
            seedSampleConversation()
        }
    }

    private func save() {
        LocalStorage.shared.store(conversations, forKey: conversationKey)
        LocalStorage.shared.store(messages, forKey: messageKey)
        LocalStorage.shared.store(scheduledMessages, forKey: scheduleKey)
    }

    private func seedSampleConversation() {
        let sample = Conversation(
            id: UUID(),
            participantName: "Avery",
            participantPhone: "+1234567890",
            lastMessage: "Welcome to NexusSMS!",
            lastMessageTime: Date(),
            unreadCount: 1,
            isPinned: false,
            isMuted: false,
            themeId: nil,
            messageType: .sms,
            socialMediaPlatform: ""
        )
        conversations.append(sample)
        messages.append(Message(
            id: UUID(),
            conversationId: sample.id,
            senderId: sample.participantPhone,
            recipientId: "self",
            content: "Welcome to NexusSMS!",
            timestamp: Date(),
            isIncoming: true,
            isSent: true,
            isDelivered: true,
            isRead: false,
            attachmentUrls: [],
            messageType: .sms,
            socialMediaPlatform: "",
            encryptionType: .aes256,
            signature: "",
            reactions: []
        ))
        save()
    }

    func sendMessage(conversationId: UUID, content: String, messageType: MessageType, recipientPhone: String) {
        let text = ShortcutService().expand(message: content)
        let signedText = text + "\n\n— NexusSMS"
        let encrypted = SecurityService.shared.encrypt(signedText)
        let message = Message(
            id: UUID(),
            conversationId: conversationId,
            senderId: "self",
            recipientId: recipientPhone,
            content: encrypted,
            timestamp: Date(),
            isIncoming: false,
            isSent: true,
            isDelivered: true,
            isRead: true,
            attachmentUrls: [],
            messageType: messageType,
            socialMediaPlatform: messageType == .social ? "Discord" : "",
            encryptionType: .aes256,
            signature: "— NexusSMS",
            reactions: []
        )
        messages.append(message)
        if let index = conversations.firstIndex(where: { $0.id == conversationId }) {
            conversations[index].lastMessage = text
            conversations[index].lastMessageTime = Date()
        }
        save()
    }

    func scheduledMessage(_ scheduled: ScheduledMessage) {
        scheduledMessages.append(scheduled)
        save()
    }

    func messages(for conversationId: UUID) -> [Message] {
        messages.filter { $0.conversationId == conversationId }.sorted { $0.timestamp < $1.timestamp }
    }
}

// MARK: - SignatureService

final class SignatureService: ObservableObject {
    @Published private(set) var signatures: [Signature] = []
    private let storageKey = "nexussms_signatures"

    init() {
        signatures = LocalStorage.shared.retrieve([Signature].self, forKey: storageKey) ?? []
    }

    private func save() { LocalStorage.shared.store(signatures, forKey: storageKey) }

    func add(name: String, content: String, isDefault: Bool = false) {
        let sig = Signature(id: UUID(), name: name, content: content, isDefault: isDefault, format: "TEXT", fontFamily: nil, fontSize: 12, createdAt: Date(), updatedAt: Date())
        if isDefault { clearDefaults() }
        signatures.append(sig)
        save()
    }

    func update(_ signature: Signature) {
        if let i = signatures.firstIndex(where: { $0.id == signature.id }) {
            var updated = signature
            updated.updatedAt = Date()
            if updated.isDefault { clearDefaults() }
            signatures[i] = updated
            save()
        }
    }

    func delete(_ signature: Signature) {
        signatures.removeAll { $0.id == signature.id }
        save()
    }

    func setDefault(_ id: UUID) {
        clearDefaults()
        if let i = signatures.firstIndex(where: { $0.id == id }) {
            signatures[i].isDefault = true
            save()
        }
    }

    private func clearDefaults() {
        for i in signatures.indices { signatures[i].isDefault = false }
    }
}

// MARK: - ScheduledMessageService

final class ScheduledMessageService: ObservableObject {
    @Published private(set) var scheduledMessages: [ScheduledMessage] = []
    private let storageKey = "nexussms_scheduled_messages"

    init() {
        scheduledMessages = LocalStorage.shared.retrieve([ScheduledMessage].self, forKey: storageKey) ?? []
    }

    private func save() { LocalStorage.shared.store(scheduledMessages, forKey: storageKey) }

    func schedule(conversationId: UUID, recipientPhone: String, content: String, at date: Date) {
        let msg = ScheduledMessage(id: UUID(), conversationId: conversationId, recipientPhone: recipientPhone, content: content, scheduledTime: date, createdTime: Date(), isRcs: false, attachmentUrls: [], status: "PENDING")
        scheduledMessages.append(msg)
        save()
    }

    func cancel(_ id: UUID) {
        if let i = scheduledMessages.firstIndex(where: { $0.id == id }) {
            scheduledMessages[i].status = "CANCELLED"
            save()
        }
    }

    func reschedule(_ id: UUID, to newDate: Date) {
        if let i = scheduledMessages.firstIndex(where: { $0.id == id }) {
            scheduledMessages[i].scheduledTime = newDate
            scheduledMessages[i].status = "PENDING"
            save()
        }
    }

    func delete(_ msg: ScheduledMessage) {
        scheduledMessages.removeAll { $0.id == msg.id }
        save()
    }
}

// MARK: - BackupService

final class BackupService: ObservableObject {
    @Published private(set) var backups: [BackupMetadata] = []
    @Published var isBackingUp = false
    @Published var lastError: String?
    private let storageKey = "nexussms_backups"

    init() {
        backups = LocalStorage.shared.retrieve([BackupMetadata].self, forKey: storageKey) ?? []
    }

    private func save() { LocalStorage.shared.store(backups, forKey: storageKey) }

    func createBackup(dataTypes: [String]) async -> Bool {
        await MainActor.run { isBackingUp = true; lastError = nil }
        defer { Task { @MainActor in isBackingUp = false } }

        do {
            try await Task.sleep(nanoseconds: 2_000_000_000)
            let backup = BackupMetadata(id: UUID(), backupType: "MANUAL", timestamp: Date(), size: 1024, dataIncluded: dataTypes, status: "COMPLETED", isAutomatic: false, backupFrequency: "MANUAL", encryptedBackup: true)
            await MainActor.run {
                backups.insert(backup, at: 0)
                save()
            }
            return true
        } catch {
            await MainActor.run { lastError = error.localizedDescription }
            return false
        }
    }

    func restoreBackup(_ id: UUID) async -> Bool {
        do {
            try await Task.sleep(nanoseconds: 1_000_000_000)
            return true
        } catch {
            await MainActor.run { lastError = error.localizedDescription }
            return false
        }
    }
}

// MARK: - RcsService

final class RcsService {
    func isRcsAvailable(for phoneNumber: String) -> Bool { false }
    func sendRcsMessage(recipient: String, content: String, conversationId: UUID) -> Bool { false }
}

// MARK: - AppLockManager

final class AppLockManager: ObservableObject {
    @Published var settings: AppSecuritySettings
    @Published var isAuthenticated = false
    private let storageKey = "nexussms_security"

    init() {
        settings = LocalStorage.shared.retrieve(AppSecuritySettings.self, forKey: storageKey) ??
            AppSecuritySettings(biometricEnabled: false, biometricType: "FINGERPRINT", appLockEnabled: false, appLockType: "PIN", appLockTimeout: 300, requireBiometricForRead: false, requireBiometricForSend: false, requireBiometricForDelete: false, requireBiometricForForward: false, lastAuthTime: Date(), isSessionLocked: false, hideMessages: false, hideNotificationContent: false, disableScreenshots: false)
    }

    private func save() { LocalStorage.shared.store(settings, forKey: storageKey) }
    func updateSettings(_ newSettings: AppSecuritySettings) { settings = newSettings; save() }

    func verifyPin(_ pin: String) -> Bool {
        guard let stored = settings.appLockValue else { return false }
        return pin == stored
    }

    func setPin(_ pin: String) {
        settings.appLockValue = pin
        settings.appLockEnabled = true
        save()
    }

    func isSessionExpired() -> Bool {
        Date().timeIntervalSince(settings.lastAuthTime) > settings.appLockTimeout
    }

    func authenticate() { isAuthenticated = true; settings.lastAuthTime = Date(); save() }
    func lock() { isAuthenticated = false; settings.isSessionLocked = true; save() }
}

// MARK: - BiometricAuthManager

final class BiometricAuthManager {
    enum BiometricType { case faceID, touchID, none }

    var biometryType: BiometricType {
        #if targetEnvironment(simulator)
        return .none
        #else
        return .touchID
        #endif
    }

    var isAvailable: Bool { biometryType != .none }
}
