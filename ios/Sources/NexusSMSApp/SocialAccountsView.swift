import SwiftUI

struct SocialAccountsView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingAddSheet = false
    @State private var newPlatform = "Discord"
    @State private var newUsername = ""
    @State private var newDisplayName = ""

    private let platforms = ["Discord", "Telegram", "WhatsApp", "Signal", "Twitter"]

    var body: some View {
        ZStack {
            if appState.socialService.accounts.isEmpty {
                emptyState
            } else {
                listContent
            }
        }
        .navigationTitle("Social Accounts")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showingAddSheet = true }) {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showingAddSheet) {
            addSheet
        }
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "person.2.fill")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text("No Social Accounts")
                .font(.title2)
                .fontWeight(.semibold)
            Text("Connect your social media accounts to send messages across platforms.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
        }
    }

    private var listContent: some View {
        List {
            ForEach(appState.socialService.accounts) { account in
                HStack(spacing: 12) {
                    platformIcon(account.platform)
                        .font(.title2)
                        .foregroundColor(platformColor(account.platform))
                        .frame(width: 36, height: 36)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(account.displayName)
                            .font(.headline)
                        Text("@\(account.username)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    Toggle("", isOn: Binding(
                        get: { account.isActive },
                        set: { newValue in
                            var updated = account
                            updated = SocialAccount(
                                id: account.id,
                                platform: account.platform,
                                accountId: account.accountId,
                                username: account.username,
                                accessToken: account.accessToken,
                                refreshToken: account.refreshToken,
                                isActive: newValue,
                                displayName: account.displayName
                            )
                            if let i = appState.socialService.accounts.firstIndex(where: { $0.id == account.id }) {
                                appState.socialService.accounts[i] = updated
                            }
                        }
                    ))
                    .labelsHidden()
                }
                .padding(.vertical, 4)
                .swipeActions(edge: .trailing) {
                    Button(role: .destructive) {
                        appState.socialService.disconnectAccount(account.id)
                    } label: {
                        Label("Disconnect", systemImage: "link.slash")
                    }
                }
            }
        }
    }

    private func platformIcon(_ platform: String) -> Image {
        switch platform.lowercased() {
        case "discord": return Image(systemName: "bubble.left.and.bubble.right.fill")
        case "telegram": return Image(systemName: "paperplane.fill")
        case "whatsapp": return Image(systemName: "message.fill")
        case "signal": return Image(systemName: "lock.shield.fill")
        case "twitter": return Image(systemName: "bird.fill")
        default: return Image(systemName: "person.circle.fill")
        }
    }

    private func platformColor(_ platform: String) -> Color {
        switch platform.lowercased() {
        case "discord": return Color(red: 0.46, green: 0.40, blue: 0.74)
        case "telegram": return Color(red: 0.0, green: 0.58, blue: 0.82)
        case "whatsapp": return Color(red: 0.15, green: 0.68, blue: 0.38)
        case "signal": return Color(red: 0.0, green: 0.48, blue: 0.85)
        case "twitter": return Color(red: 0.11, green: 0.63, blue: 0.95)
        default: return .gray
        }
    }

    private var addSheet: some View {
        NavigationStack {
            Form {
                Section("Account Details") {
                    Picker("Platform", selection: $newPlatform) {
                        ForEach(platforms, id: \.self) { platform in
                            Text(platform).tag(platform)
                        }
                    }
                    TextField("Username", text: $newUsername)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                    TextField("Display Name", text: $newDisplayName)
                }
            }
            .navigationTitle("Add Account")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { showingAddSheet = false }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        let username = newUsername.trimmingCharacters(in: .whitespaces)
                        let displayName = newDisplayName.trimmingCharacters(in: .whitespaces)
                        guard !username.isEmpty, !displayName.isEmpty else { return }
                        appState.socialService.connect(platform: newPlatform, accountId: UUID().uuidString, username: username, displayName: displayName)
                        newPlatform = "Discord"
                        newUsername = ""
                        newDisplayName = ""
                        showingAddSheet = false
                    }
                    .fontWeight(.semibold)
                    .disabled(newUsername.trimmingCharacters(in: .whitespaces).isEmpty || newDisplayName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}
