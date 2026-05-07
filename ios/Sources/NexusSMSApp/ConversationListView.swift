import SwiftUI

struct ConversationListView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        ConversationListContent(service: appState.messageService)
    }
}

private struct ConversationListContent: View {
    @StateObject private var viewModel: ConversationListViewModel

    init(service: MessageService) {
        _viewModel = StateObject(wrappedValue: ConversationListViewModel(service: service))
    }

    var body: some View {
        List {
            if viewModel.filteredConversations.isEmpty {
                ContentUnavailableView(
                    "No Messages",
                    systemImage: "message.slash",
                    description: Text(
                        viewModel.searchQuery.isEmpty
                            ? "Start a conversation to see your messages here."
                            : "No results for \"\(viewModel.searchQuery)\"."
                    )
                )
            } else {
                ForEach(viewModel.filteredConversations) { conversation in
                    NavigationLink(destination: ChatDetailView(conversation: conversation)) {
                        ConversationListItemView(conversation: conversation)
                    }
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            withAnimation {
                                viewModel.deleteConversation(conversation.id)
                            }
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }

                        if !conversation.isMuted {
                            Button {
                                viewModel.muteConversation(conversation)
                            } label: {
                                Label("Mute", systemImage: "bell.slash.fill")
                            }
                            .tint(.orange)
                        }
                    }
                    .swipeActions(edge: .leading, allowsFullSwipe: true) {
                        if !conversation.isPinned {
                            Button {
                                viewModel.pinConversation(conversation)
                            } label: {
                                Label("Pin", systemImage: "pin.fill")
                            }
                            .tint(.yellow)
                        }
                    }
                }
            }
        }
        .searchable(text: $viewModel.searchQuery, placement: .navigationBarDrawer, prompt: "Search messages or contacts")
        .refreshable {
            viewModel.load()
        }
        .onAppear {
            viewModel.load()
        }
        .navigationTitle("Messages")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    viewModel.load()
                } label: {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
    }
}
