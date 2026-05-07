import SwiftUI

struct ThemesView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingAddSheet = false
    @State private var newName = ""
    @State private var newPrimaryColor = Color.blue
    @State private var newAccentColor = Color.teal
    @State private var newBackgroundColor = Color.white
    @State private var newIsDark = false

    var body: some View {
        List {
            Section("Built-in Themes") {
                ForEach(builtInThemes) { theme in
                    themeRow(theme)
                }
            }

            Section("Custom Themes") {
                if customThemes.isEmpty {
                    Text("No custom themes")
                        .foregroundColor(.secondary)
                        .font(.subheadline)
                } else {
                    ForEach(customThemes) { theme in
                        themeRow(theme)
                    }
                    .onDelete { indexSet in
                        for index in indexSet {
                            let theme = customThemes[index]
                            appState.themeManager.deleteTheme(theme.id)
                        }
                    }
                }
            }
        }
        .navigationTitle("Themes")
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

    private var builtInThemes: [ThemeModel] {
        appState.themeManager.themes.filter { !$0.isCustom }
    }

    private var customThemes: [ThemeModel] {
        appState.themeManager.themes.filter { $0.isCustom }
    }

    @ViewBuilder
    private func themeRow(_ theme: ThemeModel) -> some View {
        HStack(spacing: 12) {
            HStack(spacing: 4) {
                Circle()
                    .fill(theme.primaryColor.color)
                    .frame(width: 20, height: 20)
                Circle()
                    .fill(theme.secondaryColor.color)
                    .frame(width: 20, height: 20)
                Circle()
                    .fill(theme.backgroundColor.color)
                    .frame(width: 20, height: 20)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(theme.name)
                    .font(.headline)
                HStack(spacing: 4) {
                    if theme.isDarkMode {
                        Image(systemName: "moon.fill")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text("Dark")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    } else {
                        Image(systemName: "sun.max.fill")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text("Light")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }

            Spacer()

            if theme.id == appState.themeManager.currentTheme?.id {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.accentColor)
                    .font(.title3)
            } else {
                Button("Apply") {
                    appState.themeManager.selectTheme(theme)
                }
                .font(.caption)
                .buttonStyle(.borderedProminent)
            }
        }
        .padding(.vertical, 4)
    }

    private var addSheet: some View {
        NavigationStack {
            Form {
                Section("Theme Name") {
                    TextField("Name", text: $newName)
                }

                Section("Colors") {
                    ColorPicker("Primary", selection: $newPrimaryColor)
                    ColorPicker("Accent", selection: $newAccentColor)
                    ColorPicker("Background", selection: $newBackgroundColor)
                }

                Section {
                    Toggle("Dark Mode", isOn: $newIsDark)
                }
            }
            .navigationTitle("Add Theme")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { showingAddSheet = false }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        let name = newName.trimmingCharacters(in: .whitespaces)
                        guard !name.isEmpty else { return }
                        let theme = ThemeModel(
                            id: UUID(),
                            name: name,
                            primaryColor: ColorData(hex: newPrimaryColor.toHex()),
                            secondaryColor: ColorData(hex: newAccentColor.toHex()),
                            bubbleSentColor: ColorData(hex: newPrimaryColor.toHex()),
                            bubbleReceivedColor: ColorData(hex: newIsDark ? "#3F3F3F" : "#E8E8E8"),
                            textColor: ColorData(hex: newIsDark ? "#FFFFFF" : "#000000"),
                            backgroundColor: ColorData(hex: newBackgroundColor.toHex()),
                            isDarkMode: newIsDark,
                            isCustom: true
                        )
                        appState.themeManager.addTheme(theme)
                        newName = ""
                        newPrimaryColor = .blue
                        newAccentColor = .teal
                        newBackgroundColor = .white
                        newIsDark = false
                        showingAddSheet = false
                    }
                    .fontWeight(.semibold)
                    .disabled(newName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}

extension Color {
    func toHex() -> String {
        let uic = UIColor(self)
        guard let components = uic.cgColor.components, components.count >= 3 else {
            return "#000000"
        }
        let r = Float(components[0])
        let g = Float(components[1])
        let b = Float(components[2])
        return String(format: "#%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255))
    }
}
