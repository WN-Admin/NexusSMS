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
