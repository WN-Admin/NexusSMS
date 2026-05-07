import SwiftUI

struct SecuritySettingsView: View {
    @ObservedObject var viewModel: SecuritySettingsViewModel

    var body: some View {
        Form {
            appLockSection
            biometricSection
            privacySection
        }
        .navigationTitle("Privacy & Security")
    }

    private var appLockSection: some View {
        Section("App Lock") {
            Toggle("Enable App Lock", isOn: Binding(
                get: { viewModel.settings.appLockEnabled },
                set: { _ in viewModel.toggleAppLock() }
            ))

            if viewModel.settings.appLockEnabled {
                Picker("Lock Type", selection: Binding(
                    get: { viewModel.settings.appLockType },
                    set: { viewModel.setLockType($0) }
                )) {
                    Text("PIN").tag("PIN")
                    Text("Pattern").tag("Pattern")
                    Text("Password").tag("Password")
                }
            }
        }
    }

    private var biometricSection: some View {
        Section("Biometric Authentication") {
            Toggle("Require for Read Messages", isOn: Binding(
                get: { viewModel.settings.requireBiometricForRead },
                set: { _ in viewModel.toggleBiometricForRead() }
            ))

            Toggle("Require for Send Messages", isOn: Binding(
                get: { viewModel.settings.requireBiometricForSend },
                set: { _ in viewModel.toggleBiometricForSend() }
            ))

            Toggle("Require for Delete Messages", isOn: Binding(
                get: { viewModel.settings.requireBiometricForDelete },
                set: { _ in viewModel.toggleBiometricForDelete() }
            ))

            Toggle("Require for Forward Messages", isOn: Binding(
                get: { viewModel.settings.requireBiometricForForward },
                set: { _ in viewModel.toggleBiometricForForward() }
            ))
        }
    }

    private var privacySection: some View {
        Section("Privacy") {
            Toggle("Hide Messages in Lock Screen", isOn: Binding(
                get: { viewModel.settings.hideMessages },
                set: { _ in viewModel.toggleHideMessages() }
            ))

            Toggle("Hide Notification Content", isOn: Binding(
                get: { viewModel.settings.hideNotificationContent },
                set: { _ in viewModel.toggleHideNotifications() }
            ))

            Toggle("Disable Screenshots", isOn: Binding(
                get: { viewModel.settings.disableScreenshots },
                set: { _ in }
            ))
        }
    }
}
