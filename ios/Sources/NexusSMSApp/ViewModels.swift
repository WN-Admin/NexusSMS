import Foundation
import SwiftUI
import Combine

// MARK: - AppState

final class AppState: ObservableObject {
    @Published var messageService = MessageService()
    @Published var shortcutService = ShortcutService()
    @Published var themeManager = ThemeService()
    @Published var socialService = SocialIntegrationService()
    @Published var signatureService = SignatureService()
    @Published var scheduledMessageService = ScheduledMessageService()
    @Published var backupService = BackupService()
    @Published var appLockManager = AppLockManager()
    @Published var rcsService = RcsService()
    @Published var isLocked = false

    init() {
        checkLockStatus()
    }

    func checkLockStatus() {
        isLocked = appLockManager.settings.appLockEnabled && !appLockManager.isAuthenticated
    }
}

// MARK: - ConversationListViewModel

final class ConversationListViewModel: ObservableObject {
    @Published private(set) var conversations: [Conversation] = []
    @Published var searchQuery = ""
    @Published var filteredConversations: [Conversation] = []
    private let service: MessageService
    private var cancellables = Set<AnyCancellable>()

    init(service: MessageService) {
        self.service = service
        load()
        setupSearch()
    }

    func load() {
        conversations = service.conversations
        filteredConversations = conversations
    }

    private func setupSearch() {
        $searchQuery
            .debounce(for: .milliseconds(300), scheduler: RunLoop.main)
            .sink { [weak self] query in
                guard let self = self else { return }
                if query.isEmpty {
                    self.filteredConversations = self.conversations
                } else {
                    self.filteredConversations = self.conversations.filter {
                        $0.participantName.localizedCaseInsensitiveContains(query) ||
                        $0.lastMessage.localizedCaseInsensitiveContains(query)
                    }
                }
            }
            .store(in: &cancellables)
    }

    func pinConversation(_ conversation: Conversation) {
        guard let index = conversations.firstIndex(where: { $0.id == conversation.id }) else { return }
        let updated = Conversation(
            id: conversation.id,
            participantName: conversation.participantName,
            participantPhone: conversation.participantPhone,
            lastMessage: conversation.lastMessage,
            lastMessageTime: conversation.lastMessageTime,
            unreadCount: conversation.unreadCount,
            isPinned: true,
            isMuted: conversation.isMuted,
            themeId: conversation.themeId,
            messageType: conversation.messageType,
            socialMediaPlatform: conversation.socialMediaPlatform
        )
        conversations[index] = updated
        filteredConversations = conversations
    }

    func muteConversation(_ conversation: Conversation) {
        guard let index = conversations.firstIndex(where: { $0.id == conversation.id }) else { return }
        let updated = Conversation(
            id: conversation.id,
            participantName: conversation.participantName,
            participantPhone: conversation.participantPhone,
            lastMessage: conversation.lastMessage,
            lastMessageTime: conversation.lastMessageTime,
            unreadCount: conversation.unreadCount,
            isPinned: conversation.isPinned,
            isMuted: true,
            themeId: conversation.themeId,
            messageType: conversation.messageType,
            socialMediaPlatform: conversation.socialMediaPlatform
        )
        conversations[index] = updated
        filteredConversations = conversations
    }

    func deleteConversation(_ id: UUID) {
        conversations.removeAll { $0.id == id }
        filteredConversations = conversations
    }
}

// MARK: - ChatViewModel

final class ChatViewModel: ObservableObject {
    @Published var messageText = ""
    @Published var selectedMessageType: MessageType = .sms
    @Published private(set) var messages: [Message] = []
    @Published var showEmojiPicker = false
    @Published var showMediaPicker = false
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
        service.sendMessage(
            conversationId: conversation.id,
            content: messageText,
            messageType: selectedMessageType,
            recipientPhone: conversation.participantPhone
        )
        messageText = ""
        loadMessages()
    }

    func addReaction(_ emoji: String, to messageId: UUID) {
        guard let index = messages.firstIndex(where: { $0.id == messageId }) else { return }
        let msg = messages[index]
        let updated = Message(
            id: msg.id,
            conversationId: msg.conversationId,
            senderId: msg.senderId,
            recipientId: msg.recipientId,
            content: msg.content,
            timestamp: msg.timestamp,
            isIncoming: msg.isIncoming,
            isSent: msg.isSent,
            isDelivered: msg.isDelivered,
            isRead: msg.isRead,
            attachmentUrls: msg.attachmentUrls,
            messageType: msg.messageType,
            socialMediaPlatform: msg.socialMediaPlatform,
            encryptionType: msg.encryptionType,
            signature: msg.signature,
            reactions: msg.reactions + [emoji]
        )
        messages[index] = updated
    }
}

// MARK: - SettingsViewModel

final class SettingsViewModel: ObservableObject {
    @Published var themes: [ThemeModel] = []
    @Published var currentTheme: ThemeModel?
    @Published var socialAccounts: [SocialAccount] = []
    @Published var signatures: [Signature] = []
    @Published var scheduledMessages: [ScheduledMessage] = []
    @Published var backups: [BackupMetadata] = []
    private let themeService: ThemeService
    private let socialService: SocialIntegrationService
    private let signatureService: SignatureService
    private let scheduledService: ScheduledMessageService
    private let backupService: BackupService

    init(themeService: ThemeService, socialService: SocialIntegrationService, signatureService: SignatureService, scheduledService: ScheduledMessageService, backupService: BackupService) {
        self.themeService = themeService
        self.socialService = socialService
        self.signatureService = signatureService
        self.scheduledService = scheduledService
        self.backupService = backupService
        load()
    }

    func load() {
        themes = themeService.themes
        currentTheme = themeService.currentTheme
        socialAccounts = socialService.accounts
        signatures = signatureService.signatures
        scheduledMessages = scheduledService.scheduledMessages
        backups = backupService.backups
    }

    func selectTheme(_ theme: ThemeModel) {
        themeService.selectTheme(theme)
        currentTheme = theme
    }

    func createCustomTheme(name: String, primary: String, secondary: String, bubbleSent: String, bubbleReceived: String, text: String, background: String, isDark: Bool) {
        let theme = ThemeModel(
            id: UUID(),
            name: name,
            primaryColor: ColorData(hex: primary),
            secondaryColor: ColorData(hex: secondary),
            bubbleSentColor: ColorData(hex: bubbleSent),
            bubbleReceivedColor: ColorData(hex: bubbleReceived),
            textColor: ColorData(hex: text),
            backgroundColor: ColorData(hex: background),
            isDarkMode: isDark,
            isCustom: true
        )
        themeService.addTheme(theme)
        load()
    }

    func deleteTheme(_ theme: ThemeModel) {
        themes.removeAll { $0.id == theme.id }
    }

    func connectSocialAccount(platform: String, username: String, displayName: String) {
        socialService.connect(platform: platform, accountId: "\(platform)_\(UUID())", username: username, displayName: displayName)
        load()
    }

    func disconnectSocialAccount(_ account: SocialAccount) {
        socialAccounts.removeAll { $0.id == account.id }
    }

    func addSignature(name: String, content: String, isDefault: Bool) {
        signatureService.add(name: name, content: content, isDefault: isDefault)
        load()
    }

    func updateSignature(_ signature: Signature) {
        signatureService.update(signature)
        load()
    }

    func deleteSignature(_ signature: Signature) {
        signatureService.delete(signature)
        load()
    }

    func cancelScheduled(_ id: UUID) {
        scheduledService.cancel(id)
        load()
    }

    func rescheduleScheduled(_ id: UUID, to date: Date) {
        scheduledService.reschedule(id, to: date)
        load()
    }

    func createBackup() async -> Bool {
        let result = await backupService.createBackup(dataTypes: ["shortcuts", "signatures", "themes", "settings"])
        await MainActor.run { load() }
        return result
    }
}

// MARK: - ShortcutsViewModel

final class ShortcutsViewModel: ObservableObject {
    @Published private(set) var shortcuts: [Shortcut] = []
    @Published var searchQuery = ""
    @Published var filteredShortcuts: [Shortcut] = []
    @Published var showAddDialog = false
    private let service: ShortcutService
    private var cancellables = Set<AnyCancellable>()

    init(service: ShortcutService) {
        self.service = service
        load()
        $searchQuery
            .debounce(for: .milliseconds(200), scheduler: RunLoop.main)
            .sink { [weak self] query in
                guard let self = self else { return }
                if query.isEmpty { self.filteredShortcuts = self.shortcuts }
                else { self.filteredShortcuts = self.shortcuts.filter { $0.trigger.localizedCaseInsensitiveContains(query) || $0.expansion.localizedCaseInsensitiveContains(query) } }
            }
            .store(in: &cancellables)
    }

    func load() {
        shortcuts = service.shortcuts
        filteredShortcuts = shortcuts
    }

    func add(trigger: String, expansion: String, category: String) {
        service.addShortcut(trigger: trigger, expansion: expansion, category: category)
        load()
    }

    func delete(_ shortcut: Shortcut) {
        shortcuts.removeAll { $0.id == shortcut.id }
        filteredShortcuts = shortcuts
    }
}

// MARK: - ThemesViewModel

final class ThemesViewModel: ObservableObject {
    @Published private(set) var builtInThemes: [ThemeModel] = []
    @Published private(set) var customThemes: [ThemeModel] = []
    @Published var selectedTheme: ThemeModel?

    init(service: ThemeService) {
        builtInThemes = service.themes.filter { !$0.isCustom }
        customThemes = service.themes.filter { $0.isCustom }
        selectedTheme = service.currentTheme
    }

    func apply(_ theme: ThemeModel) {
        selectedTheme = theme
    }
}

// MARK: - SocialAccountsViewModel

final class SocialAccountsViewModel: ObservableObject {
    @Published private(set) var accounts: [SocialAccount] = []
    private let service: SocialIntegrationService

    init(service: SocialIntegrationService) {
        self.service = service
        load()
    }

    func load() {
        accounts = service.accounts
    }

    func connect(platform: String, username: String, displayName: String) {
        service.connect(platform: platform, accountId: "\(platform)_\(UUID())", username: username, displayName: displayName)
        load()
    }

    func disconnect(_ id: UUID) {
        accounts.removeAll { $0.id == id }
    }
}

// MARK: - BackupViewModel

final class BackupViewModel: ObservableObject {
    @Published private(set) var backups: [BackupMetadata] = []
    @Published var isBackingUp = false
    @Published var errorMessage: String?
    private let service: BackupService

    init(service: BackupService) {
        self.service = service
        load()
    }

    func load() {
        backups = service.backups
    }

    func createBackup() async {
        await MainActor.run { isBackingUp = true }
        let success = await service.createBackup(dataTypes: ["shortcuts", "signatures", "themes", "settings"])
        await MainActor.run {
            isBackingUp = false
            if !success { errorMessage = service.lastError ?? "Backup failed" }
            load()
        }
    }

    func restore(_ id: UUID) async -> Bool {
        let success = await service.restoreBackup(id)
        if !success { await MainActor.run { errorMessage = service.lastError ?? "Restore failed" } }
        return success
    }
}

// MARK: - AppLockViewModel

final class AppLockViewModel: ObservableObject {
    @Published var pinInput = ""
    @Published var error: String?
    @Published var isAuthenticated = false
    @Published var settings: AppSecuritySettings
    private let lockManager: AppLockManager

    init(lockManager: AppLockManager) {
        self.lockManager = lockManager
        self.settings = lockManager.settings
    }

    func verifyPin() {
        if lockManager.verifyPin(pinInput) {
            lockManager.authenticate()
            isAuthenticated = true
            error = nil
        } else {
            error = "Invalid PIN"
        }
    }

    func lock() {
        lockManager.lock()
        isAuthenticated = false
    }
}

// MARK: - SecuritySettingsViewModel

final class SecuritySettingsViewModel: ObservableObject {
    @Published var settings: AppSecuritySettings
    private let lockManager: AppLockManager

    init(lockManager: AppLockManager) {
        self.lockManager = lockManager
        self.settings = lockManager.settings
    }

    func toggleAppLock() {
        settings.appLockEnabled.toggle()
        lockManager.updateSettings(settings)
    }

    func setLockType(_ type: String) {
        settings.appLockType = type
        lockManager.updateSettings(settings)
    }

    func toggleBiometricForRead() {
        settings.requireBiometricForRead.toggle()
        lockManager.updateSettings(settings)
    }

    func toggleBiometricForSend() {
        settings.requireBiometricForSend.toggle()
        lockManager.updateSettings(settings)
    }

    func toggleBiometricForDelete() {
        settings.requireBiometricForDelete.toggle()
        lockManager.updateSettings(settings)
    }

    func toggleBiometricForForward() {
        settings.requireBiometricForForward.toggle()
        lockManager.updateSettings(settings)
    }

    func toggleHideMessages() {
        settings.hideMessages.toggle()
        lockManager.updateSettings(settings)
    }

    func toggleHideNotifications() {
        settings.hideNotificationContent.toggle()
        lockManager.updateSettings(settings)
    }
}
