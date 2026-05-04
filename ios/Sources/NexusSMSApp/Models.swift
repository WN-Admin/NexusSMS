import Foundation
import SwiftUI

enum MessageType: String, Codable, CaseIterable {
    case sms
    case rcs
    case social
}

enum EncryptionType: String, Codable, CaseIterable {
    case none
    case aes256
    case signal
}

struct Shortcut: Identifiable, Codable {
    let id: UUID
    let trigger: String
    let expansion: String
    let category: String
    var usageCount: Int
}

struct ThemeModel: Identifiable, Codable {
    let id: UUID
    let name: String
    let primaryColor: ColorData
    let secondaryColor: ColorData
    let bubbleSentColor: ColorData
    let bubbleReceivedColor: ColorData
    let textColor: ColorData
    let backgroundColor: ColorData
    let isDarkMode: Bool
    let isCustom: Bool
}

struct ColorData: Codable {
    let hex: String

    var color: Color {
        Color(hex: hex)
    }
}

struct Message: Identifiable, Codable {
    let id: UUID
    let conversationId: UUID
    let senderId: String
    let recipientId: String
    let content: String
    let timestamp: Date
    let isIncoming: Bool
    let isSent: Bool
    let isDelivered: Bool
    let isRead: Bool
    let attachmentUrls: [String]
    let messageType: MessageType
    let socialMediaPlatform: String
    let encryptionType: EncryptionType
    let signature: String
    let reactions: [String]
}

struct Conversation: Identifiable, Codable {
    let id: UUID
    let participantName: String
    let participantPhone: String
    let lastMessage: String
    let lastMessageTime: Date
    let unreadCount: Int
    let isPinned: Bool
    let isMuted: Bool
    let themeId: UUID?
    let messageType: MessageType
    let socialMediaPlatform: String
}

struct ScheduledMessage: Identifiable, Codable {
    let id: UUID
    let conversationId: UUID
    let recipientPhone: String
    let content: String
    let scheduledTime: Date
    let createdTime: Date
    let isRcs: Bool
    let attachmentUrls: [String]
    let status: String
}

struct SocialAccount: Identifiable, Codable {
    let id: UUID
    let platform: String
    let accountId: String
    let username: String
    let accessToken: String
    let refreshToken: String
    let isActive: Bool
    let displayName: String
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int = UInt64()
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
