import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        SettingsContent(
            themeService: appState.themeManager,
            socialService: appState.socialService,
            signatureService: appState.signatureService,
            scheduledService: appState.scheduledMessageService,
            backupService: appState.backupService
        )
    }
}

private struct SettingsContent: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel: SettingsViewModel

    init(themeService: ThemeService, socialService: SocialIntegrationService, signatureService: SignatureService, scheduledService: ScheduledMessageService, backupService: BackupService) {
        _viewModel = StateObject(wrappedValue: SettingsViewModel(
            themeService: themeService,
            socialService: socialService,
            signatureService: signatureService,
            scheduledService: scheduledService,
            backupService: backupService
        ))
    }

    var body: some View {
        Form {
            appearanceSection
            messagesSection
            shortcutsSection
            integrationsSection
            privacySection
            backupSection
            aboutSection
        }
        .navigationTitle("Settings")
    }

    // MARK: - Appearance

    private var appearanceSection: some View {
        Section("Appearance") {
            NavigationLink(destination: ThemesView()) {
                HStack {
                    Image(systemName: "paintpalette.fill")
                        .foregroundStyle(.purple)
                    VStack(alignment: .leading) {
                        Text("Theme")
                        if let theme = viewModel.currentTheme {
                            Text(theme.name)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }

            Toggle(isOn: .init(
                get: { viewModel.currentTheme?.isDarkMode ?? false },
                set: { newValue in
                    if let current = viewModel.currentTheme {
                        let updated = ThemeModel(
                            id: current.id,
                            name: current.name,
                            primaryColor: current.primaryColor,
                            secondaryColor: current.secondaryColor,
                            bubbleSentColor: current.bubbleSentColor,
                            bubbleReceivedColor: current.bubbleReceivedColor,
                            textColor: current.textColor,
                            backgroundColor: current.backgroundColor,
                            isDarkMode: newValue,
                            isCustom: current.isCustom
                        )
                        viewModel.selectTheme(updated)
                    }
                }
            )) {
                HStack {
                    Image(systemName: "moon.fill")
                        .foregroundStyle(.indigo)
                    Text("Dark Mode")
                }
            }
        }
    }

    // MARK: - Messages

    private var messagesSection: some View {
        Section("Messages") {
            NavigationLink(destination: SignaturesView()) {
                HStack {
                    Image(systemName: "signature")
                        .foregroundStyle(.blue)
                    VStack(alignment: .leading) {
                        Text("Signatures")
                        Text("Manage message signatures")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            NavigationLink(destination: ScheduledMessagesView()) {
                HStack {
                    Image(systemName: "clock.badge.checkmark.fill")
                        .foregroundStyle(.orange)
                    VStack(alignment: .leading) {
                        Text("Scheduled Messages")
                        Text("View and manage scheduled messages")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    // MARK: - Shortcuts

    private var shortcutsSection: some View {
        Section("Shortcuts") {
            NavigationLink(destination: ShortcutsView()) {
                HStack {
                    Image(systemName: "bolt.fill")
                        .foregroundStyle(.yellow)
                    VStack(alignment: .leading) {
                        Text("Quick Shortcuts")
                        Text("Manage text expansion shortcuts")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    // MARK: - Integrations

    private var integrationsSection: some View {
        Section("Integrations") {
            NavigationLink(destination: SocialAccountsView()) {
                HStack {
                    Image(systemName: "globe")
                        .foregroundStyle(.green)
                    VStack(alignment: .leading) {
                        Text("Social Accounts")
                        Text("Connect Discord, Telegram, and more")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            HStack {
                Image(systemName: "antenna.radiowaves.left.and.right")
                    .foregroundStyle(.blue)
                VStack(alignment: .leading) {
                    Text("RCS")
                    Text("Rich Communication Services")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                Text("Available")
                    .font(.caption)
                    .foregroundStyle(.green)
            }
        }
    }

    // MARK: - Privacy & Security

    private var privacySection: some View {
        Section("Privacy & Security") {
            NavigationLink(
                destination: SecuritySettingsView(
                    viewModel: SecuritySettingsViewModel(lockManager: appState.appLockManager)
                )
            ) {
                HStack {
                    Image(systemName: "lock.shield.fill")
                        .foregroundStyle(.red)
                    VStack(alignment: .leading) {
                        Text("App Lock")
                        Text("PIN, biometric, and privacy controls")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    // MARK: - Backup

    private var backupSection: some View {
        Section("Backup") {
            NavigationLink(
                destination: BackupView(
                    viewModel: BackupViewModel(service: appState.backupService)
                )
            ) {
                HStack {
                    Image(systemName: "icloud.fill")
                        .foregroundStyle(.blue)
                    VStack(alignment: .leading) {
                        Text("Google Drive Backup")
                        Text("Backup and restore your data")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    // MARK: - About

    private var aboutSection: some View {
        Section("About") {
            HStack {
                Image(systemName: "info.circle.fill")
                    .foregroundStyle(.gray)
                Text("Version")
                Spacer()
                Text("1.0.0")
                    .foregroundStyle(.secondary)
            }

            HStack {
                Image(systemName: "lock.shield")
                    .foregroundStyle(.gray)
                Text("Encryption")
                Spacer()
                Text("AES-256")
                    .foregroundStyle(.secondary)
            }
        }
    }
}
