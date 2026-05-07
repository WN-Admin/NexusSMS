import SwiftUI

// MARK: - Date formatting

extension Date {
    func formattedRelative() -> String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: self, relativeTo: Date())
    }

    func formattedTime() -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: self)
    }

    func formattedDate() -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: self)
    }
}

// MARK: - View padding

extension View {
    func standardPadding() -> some View {
        self.padding(.horizontal, 16).padding(.vertical, 8)
    }
}

// MARK: - Phone number formatting

extension String {
    func formattedPhoneNumber() -> String {
        let cleaned = self.filter { $0.isNumber }
        if cleaned.count == 10 {
            return "(\(cleaned.prefix(3))) \(cleaned.dropFirst(3).prefix(3))-\(cleaned.suffix(4))"
        }
        return self
    }
}

// MARK: - Colors for theme

extension Color {
    static let bubbleBlue = Color(red: 0.22, green: 0.60, blue: 0.95)
    static let bubbleGray = Color(red: 0.90, green: 0.90, blue: 0.92)
    static let unreadRed = Color.red
    static let pinYellow = Color.yellow
    static let mutedGray = Color.gray.opacity(0.5)
}
