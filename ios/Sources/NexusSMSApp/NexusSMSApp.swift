import SwiftUI

@main
struct NexusSMSApp: App {
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .preferredColorScheme(appState.themeManager.currentTheme?.isDarkMode ?? false ? .dark : .light)
        }
    }
}
