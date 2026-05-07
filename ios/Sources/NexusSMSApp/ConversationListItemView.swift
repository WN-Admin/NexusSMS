import SwiftUI

struct ConversationListItemView: View {
    let conversation: Conversation

    private var avatarLetter: String {
        String(conversation.participantName.prefix(1)).uppercased()
    }

    private var avatarColor: Color {
        let colors: [Color] = [.blue, .green, .orange, .purple, .pink, .teal, .indigo, .mint]
        let index = abs(conversation.id.hashValue) % colors.count
        return colors[index]
    }

    private var formattedTime: String {
        let calendar = Calendar.current
        if calendar.isDateInToday(conversation.lastMessageTime) {
            let formatter = DateFormatter()
            formatter.dateFormat = "h:mm a"
            return formatter.string(from: conversation.lastMessageTime)
        } else if calendar.isDateInYesterday(conversation.lastMessageTime) {
            return "Yesterday"
        } else {
            let formatter = DateFormatter()
            formatter.dateFormat = "M/d/yy"
            return formatter.string(from: conversation.lastMessageTime)
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(avatarColor)
                    .frame(width: 48, height: 48)

                Text(avatarLetter)
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundStyle(.white)
            }
            .overlay(alignment: .topTrailing) {
                if conversation.isPinned {
                    Image(systemName: "pin.fill")
                        .font(.system(size: 10))
                        .foregroundStyle(.yellow)
                        .offset(x: 4, y: -4)
                }
            }

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(conversation.participantName)
                        .font(.headline)
                        .lineLimit(1)

                    if conversation.messageType == .rcs {
                        Text("RCS")
                            .font(.caption2.bold())
                            .foregroundStyle(.blue)
                            .padding(.horizontal, 4)
                            .padding(.vertical, 1)
                            .background(Color.blue.opacity(0.1))
                            .clipShape(Capsule())
                    } else if conversation.messageType == .social && !conversation.socialMediaPlatform.isEmpty {
                        Text(conversation.socialMediaPlatform)
                            .font(.caption2.bold())
                            .foregroundStyle(.purple)
                            .padding(.horizontal, 4)
                            .padding(.vertical, 1)
                            .background(Color.purple.opacity(0.1))
                            .clipShape(Capsule())
                    }

                    Spacer(minLength: 8)

                    Text(formattedTime)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                HStack {
                    Text(conversation.lastMessage)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)

                    Spacer(minLength: 8)

                    if conversation.unreadCount > 0 {
                        Text("\(conversation.unreadCount)")
                            .font(.caption2.bold())
                            .foregroundStyle(.white)
                            .frame(minWidth: 20, minHeight: 20)
                            .padding(.horizontal, 4)
                            .background(Color.blue)
                            .clipShape(Capsule())
                    }
                }
            }
        }
        .padding(.vertical, 2)
    }
}
