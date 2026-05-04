import Foundation
import SwiftUI

final class AppState: ObservableObject {
    @Published var messageService = MessageService()
    @Published var shortcutService = ShortcutService()
    @Published var themeManager = ThemeService()
    @Published var socialService = SocialIntegrationService()
}

final class ConversationListViewModel: ObservableObject {
    @Published private(set) var conversations: [Conversation] = []
    private let service: MessageService

    init(service: MessageService) {
        self.service = service
        conversations = service.conversations
    }

    func refresh() {
        conversations = service.conversations
    }

    func pinConversation(_ conversation: Conversation) {
        if let index = service.conversations.firstIndex(where: { $0.id == conversation.id }) {
            var updated = service.conversations[index]
            updated = Conversation(
                id: updated.id,
                participantName: updated.participantName,
                participantPhone: updated.participantPhone,
                lastMessage: updated.lastMessage,
                lastMessageTime: updated.lastMessageTime,
                unreadCount: updated.unreadCount,
                isPinned: true,
                isMuted: updated.isMuted,
                themeId: updated.themeId,
                messageType: updated.messageType,
                socialMediaPlatform: updated.socialMediaPlatform
            )
            service.conversations[index] = updated
            refresh()
        }
    }
}

final class ChatViewModel: ObservableObject {
    @Published var messageText = ""
    @Published var selectedMessageType: MessageType = .sms
    @Published private(set) var messages: [Message] = []
    let conversation: Conversation
    private let service: MessageService

    init(conversation: Conversation, service: MessageService) {
        self.conversation = conversation
        self.service = service
        loadMessages()
    }

    private func loadMessages() {
        messages = service.messages(for: conversation.id)
    }

    func sendMessage() {
        guard !messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        service.sendMessage(conversationId: conversation.id, content: messageText, messageType: selectedMessageType, recipientPhone: conversation.participantPhone)
        messageText = ""
        loadMessages()
    }
}

final class SettingsViewModel: ObservableObject {
    @Published var themes: [ThemeModel] = []
    @Published var currentTheme: ThemeModel?
    @Published var socialAccounts: [SocialAccount] = []
    private let themeService: ThemeService
    private let socialService: SocialIntegrationService

    init(themeService: ThemeService, socialService: SocialIntegrationService) {
        self.themeService = themeService
        self.socialService = socialService
        themes = themeService.themes
        currentTheme = themeService.currentTheme
        socialAccounts = socialService.accounts
    }

    func selectTheme(_ theme: ThemeModel) {
        themeService.selectTheme(theme)
        currentTheme = theme
    }

    func connectSampleAccount() {
        socialService.connect(platform: "Discord", accountId: "discord_123", username: "NexusUser", displayName: "Nexus User")
        socialAccounts = socialService.accounts
    }
}
