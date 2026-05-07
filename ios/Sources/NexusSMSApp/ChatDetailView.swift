import SwiftUI

struct ChatDetailView: View {
    @EnvironmentObject var appState: AppState
    let conversation: Conversation

    var body: some View {
        ChatDetailContent(conversation: conversation, service: appState.messageService)
    }
}

private struct ChatDetailContent: View {
    @StateObject private var viewModel: ChatViewModel
    @FocusState private var isInputFocused: Bool

    init(conversation: Conversation, service: MessageService) {
        _viewModel = StateObject(wrappedValue: ChatViewModel(conversation: conversation, service: service))
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 4) {
                        ForEach(viewModel.messages) { message in
                            MessageBubbleView(
                                message: message,
                                onReaction: { emoji in
                                    viewModel.addReaction(emoji, to: message.id)
                                }
                            )
                            .id(message.id)
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                }
                .defaultScrollAnchor(.bottom)
                .onChange(of: viewModel.messages.count) { _, _ in
                    if let last = viewModel.messages.last {
                        withAnimation {
                            proxy.scrollTo(last.id, anchor: .bottom)
                        }
                    }
                }
            }

            Divider()

            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    Button {
                        viewModel.showEmojiPicker = true
                    } label: {
                        Image(systemName: "face.smiling")
                            .font(.title2)
                            .foregroundStyle(.secondary)
                    }

                    TextField("Message", text: $viewModel.messageText, axis: .vertical)
                        .focused($isInputFocused)
                        .textFieldStyle(.plain)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 20))
                        .lineLimit(1...5)
                        .onSubmit {
                            sendMessage()
                        }

                    Button {
                        sendMessage()
                    } label: {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.system(size: 32))
                            .foregroundStyle(viewModel.messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? Color.gray.opacity(0.4) : .blue)
                    }
                    .disabled(viewModel.messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)

                HStack(spacing: 16) {
                    Button {

                    } label: {
                        Image(systemName: "photo.on.rectangle.angled")
                            .font(.title3)
                            .foregroundStyle(.secondary)
                    }

                    Button {

                    } label: {
                        Image(systemName: "mic.fill")
                            .font(.title3)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    Menu {
                        Button { viewModel.selectedMessageType = .sms } label: {
                            Label("SMS", systemImage: viewModel.selectedMessageType == .sms ? "checkmark" : "")
                        }
                        Button { viewModel.selectedMessageType = .rcs } label: {
                            Label("RCS", systemImage: viewModel.selectedMessageType == .rcs ? "checkmark" : "")
                        }
                        if !conversation.socialMediaPlatform.isEmpty {
                            Button { viewModel.selectedMessageType = .social } label: {
                                Label(conversation.socialMediaPlatform, systemImage: viewModel.selectedMessageType == .social ? "checkmark" : "")
                            }
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: viewModel.selectedMessageType == .social ? "globe" : "bubble.left.and.bubble.right.fill")
                                .font(.caption)
                            Text(viewModel.selectedMessageType == .social ? conversation.socialMediaPlatform : viewModel.selectedMessageType.rawValue.uppercased())
                                .font(.caption2.bold())
                        }
                        .foregroundStyle(.secondary)
                    }
                }
                .padding(.horizontal, 12)
                .padding(.bottom, 4)
            }
            .background(.bar)
        }
        .navigationTitle(conversation.participantName)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {

                } label: {
                    Image(systemName: "phone")
                }
            }
        }
        .sheet(isPresented: $viewModel.showEmojiPicker) {
            EmojiPickerView { emoji in
                viewModel.messageText.append(emoji)
            }
        }
    }

    private func sendMessage() {
        guard !viewModel.messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        viewModel.sendMessage()
        isInputFocused = false
    }
}
