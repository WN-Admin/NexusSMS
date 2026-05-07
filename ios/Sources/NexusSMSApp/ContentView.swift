import SwiftUI

struct ContentView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        Group {
            if appState.isLocked {
                LockedView()
            } else {
                TabView {
                    NavigationStack {
                        ConversationListView()
                    }
                    .tabItem {
                        Label("Messages", systemImage: "message.fill")
                    }

                    NavigationStack {
                        SettingsView()
                    }
                    .tabItem {
                        Label("Settings", systemImage: "gearshape.fill")
                    }
                }
                .environmentObject(appState)
            }
        }
    }
}

private struct LockedView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var viewModel: AppLockViewModel

    init() {
        _viewModel = StateObject(wrappedValue: AppLockViewModel(lockManager: AppLockManager()))
    }

    var body: some View {
        AppLockView(viewModel: viewModel)
            .transition(.opacity)
            .onChange(of: viewModel.isAuthenticated) { _, authenticated in
                if authenticated {
                    appState.appLockManager.authenticate()
                    appState.checkLockStatus()
                }
            }
    }
}
