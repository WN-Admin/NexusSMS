import SwiftUI

struct SignaturesView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingAddSheet = false
    @State private var newName = ""
    @State private var newContent = ""
    @State private var newIsDefault = false

    var body: some View {
        ZStack {
            if appState.signatureService.signatures.isEmpty {
                emptyState
            } else {
                listContent
            }
        }
        .navigationTitle("Signatures")
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
            Image(systemName: "signature")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text("No Signatures")
                .font(.title2)
                .fontWeight(.semibold)
            Text("Add signatures to automatically append to your messages.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
        }
    }

    private var listContent: some View {
        List {
            ForEach(appState.signatureService.signatures) { signature in
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text(signature.name)
                                .font(.headline)
                            if signature.isDefault {
                                Text("Default")
                                    .font(.caption)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Capsule().fill(Color.accentColor))
                            }
                        }
                        Text(signature.content)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                    }
                    Spacer()
                    if !signature.isDefault {
                        Button("Set Default") {
                            appState.signatureService.setDefault(signature.id)
                        }
                        .font(.caption)
                        .buttonStyle(.bordered)
                    }
                }
                .padding(.vertical, 4)
            }
            .onDelete { indexSet in
                for index in indexSet {
                    let sig = appState.signatureService.signatures[index]
                    appState.signatureService.delete(sig)
                }
            }
        }
    }

    private var addSheet: some View {
        NavigationStack {
            Form {
                Section("Signature Details") {
                    TextField("Name", text: $newName)
                    TextEditor(text: $newContent)
                        .frame(minHeight: 100)
                }
                Section {
                    Toggle("Set as Default", isOn: $newIsDefault)
                }
            }
            .navigationTitle("Add Signature")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { showingAddSheet = false }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        let name = newName.trimmingCharacters(in: .whitespaces)
                        let content = newContent.trimmingCharacters(in: .whitespaces)
                        guard !name.isEmpty, !content.isEmpty else { return }
                        appState.signatureService.add(name: name, content: content, isDefault: newIsDefault)
                        newName = ""
                        newContent = ""
                        newIsDefault = false
                        showingAddSheet = false
                    }
                    .fontWeight(.semibold)
                    .disabled(newName.trimmingCharacters(in: .whitespaces).isEmpty || newContent.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}
