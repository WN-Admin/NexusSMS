import SwiftUI

struct MessageBubbleView: View {
    let message: Message
    let onReaction: (String) -> Void

    init(message: Message, onReaction: @escaping (String) -> Void = { _ in }) {
        self.message = message
        self.onReaction = onReaction
    }

    private let reactionOptions = ["❤️", "😂", "👍", "😮", "😢", "🙏"]

    private var bubbleColor: Color {
        message.isIncoming ? Color(.systemGray5) : .blue
    }

    private var textColor: Color {
        message.isIncoming ? .primary : .white
    }

    private var alignment: HorizontalAlignment {
        message.isIncoming ? .leading : .trailing
    }

    private var formattedTime: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "h:mm a"
        return formatter.string(from: message.timestamp)
    }

    private var displayContent: String {
        SecurityService.shared.decrypt(message.content)
    }

    var body: some View {
        VStack(alignment: alignment == .leading ? .leading : .trailing, spacing: 2) {
            HStack {
                if !message.isIncoming {
                    Spacer(minLength: 60)
                }

                VStack(alignment: .trailing, spacing: 2) {
                    Text(displayContent)
                        .font(.body)
                        .foregroundStyle(textColor)
                        .fixedSize(horizontal: false, vertical: true)

                    HStack(spacing: 4) {
                        if message.messageType == .rcs {
                            Text("RCS")
                                .font(.system(size: 8, weight: .bold))
                                .foregroundStyle(textColor.opacity(0.7))
                        }

                        Text(formattedTime)
                            .font(.caption2)
                            .foregroundStyle(textColor.opacity(0.7))

                        if !message.isIncoming {
                            if message.isRead {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 10))
                                    .foregroundStyle(.white.opacity(0.8))
                            } else if message.isDelivered {
                                Image(systemName: "checkmark.circle")
                                    .font(.system(size: 10))
                                    .foregroundStyle(.white.opacity(0.6))
                            } else if message.isSent {
                                Image(systemName: "checkmark")
                                    .font(.system(size: 10))
                                    .foregroundStyle(.white.opacity(0.5))
                            }
                        }
                    }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .fill(bubbleColor)
                }
                .contextMenu {
                    Button {
                        UIPasteboard.general.string = displayContent
                    } label: {
                        Label("Copy", systemImage: "doc.on.doc")
                    }

                    Divider()

                    ForEach(reactionOptions, id: \.self) { emoji in
                        Button {
                            onReaction(emoji)
                        } label: {
                            Text(emoji)
                        }
                    }

                    Divider()

                    Button(role: .destructive) {

                    } label: {
                        Label("Delete", systemImage: "trash")
                    }
                }

                if message.isIncoming {
                    Spacer(minLength: 60)
                }
            }

            if !message.reactions.isEmpty {
                HStack {
                    if message.isIncoming {
                        reactionRow.padding(.leading, 12)
                    } else {
                        Spacer()
                        reactionRow.padding(.trailing, 12)
                    }
                }
            }
        }
    }

    private var reactionRow: some View {
        HStack(spacing: 2) {
            ForEach(Array(Set(message.reactions)), id: \.self) { reaction in
                Text(reaction)
                    .font(.caption)
                    .padding(.horizontal, 4)
                    .padding(.vertical, 2)
                    .background(.bar)
                    .clipShape(Capsule())
            }
        }
    }
}
