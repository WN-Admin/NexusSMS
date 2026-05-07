import SwiftUI

struct ShortcutsView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingAddSheet = false
    @State private var newTrigger = ""
    @State private var newExpansion = ""
    @State private var newCategory = "Quick Reply"

    private let categories = ["Quick Reply", "Status", "Travel", "Custom"]

    var body: some View {
        ZStack {
            if appState.shortcutService.shortcuts.isEmpty {
                emptyState
            } else {
                listContent
            }
        }
        .navigationTitle("Shortcuts")
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
            Image(systemName: "text.insert")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text("No Shortcuts")
                .font(.title2)
                .fontWeight(.semibold)
            Text("Add shortcuts to quickly expand common phrases.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
        }
    }

    private var listContent: some View {
        List {
            ForEach(appState.shortcutService.shortcuts) { shortcut in
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(shortcut.trigger)
                            .font(.headline)
                            .fontWeight(.bold)
                        Spacer()
                        Text(shortcut.category)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(
                                Capsule()
                                    .fill(Color.secondary.opacity(0.15))
                            )
                    }
                    Text(shortcut.expansion)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Text("Used \(shortcut.usageCount) times")
                        .font(.caption2)
                        .foregroundColor(.gray)
                }
                .padding(.vertical, 4)
            }
            .onDelete { indexSet in
                for index in indexSet {
                    let shortcut = appState.shortcutService.shortcuts[index]
                    appState.shortcutService.deleteShortcut(shortcut.id)
                }
            }
        }
    }

    private var addSheet: some View {
        NavigationStack {
            Form {
                Section("Shortcut Details") {
                    TextField("Trigger (e.g. !ato)", text: $newTrigger)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                    TextField("Expansion text", text: $newExpansion)
                    Picker("Category", selection: $newCategory) {
                        ForEach(categories, id: \.self) { category in
                            Text(category).tag(category)
                        }
                    }
                }
            }
            .navigationTitle("Add Shortcut")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { showingAddSheet = false }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        let trigger = newTrigger.trimmingCharacters(in: .whitespaces)
                        let expansion = newExpansion.trimmingCharacters(in: .whitespaces)
                        guard !trigger.isEmpty, !expansion.isEmpty else { return }
                        appState.shortcutService.addShortcut(trigger: trigger, expansion: expansion, category: newCategory)
                        newTrigger = ""
                        newExpansion = ""
                        newCategory = "Quick Reply"
                        showingAddSheet = false
                    }
                    .fontWeight(.semibold)
                    .disabled(newTrigger.trimmingCharacters(in: .whitespaces).isEmpty || newExpansion.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}
