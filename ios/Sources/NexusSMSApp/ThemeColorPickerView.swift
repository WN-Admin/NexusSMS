import SwiftUI

struct ThemeColorPickerView: View {
    @Environment(\.dismiss) private var dismiss
    let onColorSelected: (Color) -> Void

    @State private var selectedColor: Color = .blue
    @State private var hexInput: String = ""

    private let presetColors: [(name: String, color: Color)] = [
        ("Red", .red),
        ("Pink", .pink),
        ("Purple", .purple),
        ("Indigo", .indigo),
        ("Blue", .blue),
        ("Cyan", .cyan),
        ("Teal", .teal),
        ("Green", .green),
        ("Yellow", .yellow),
        ("Orange", .orange),
        ("Brown", .brown),
        ("Gray", .gray),
        ("Black", .black),
        ("White", .white),
    ]

    private let columns = Array(
        repeating: GridItem(.flexible(), spacing: 12),
        count: 5
    )

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    presetGrid
                    hexInputSection
                    previewSection
                }
                .padding(16)
            }
            .navigationTitle("Theme Color")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        onColorSelected(selectedColor)
                        dismiss()
                    }
                }
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }

    private var presetGrid: some View {
        LazyVGrid(columns: columns, spacing: 12) {
            ForEach(presetColors, id: \.name) { item in
                Button {
                    selectedColor = item.color
                    hexInput = ""
                } label: {
                    ZStack {
                        Circle()
                            .fill(item.color)
                            .frame(width: 48, height: 48)
                            .overlay(
                                Circle()
                                    .stroke(Color.primary, lineWidth: selectedColor == item.color ? 3 : 0)
                            )
                            .overlay(
                                Circle()
                                    .stroke(Color(.systemBackground), lineWidth: selectedColor == item.color ? 6 : 0)
                                    .opacity(0.5)
                            )

                        if selectedColor == item.color {
                            Image(systemName: "checkmark")
                                .font(.caption.bold())
                                .foregroundColor(item.color == .white || item.color == .yellow ? .black : .white)
                        }
                    }
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var hexInputSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Custom Hex Color")
                .font(.subheadline)
                .fontWeight(.semibold)

            HStack(spacing: 8) {
                Text("#")
                    .font(.title3)
                    .foregroundColor(.secondary)

                TextField("e.g. FF5733", text: $hexInput)
                    .font(.system(.body, design: .monospaced))
                    .textFieldStyle(.roundedBorder)
                    .autocorrectionDisabled()
                    .onChange(of: hexInput) { _, newValue in
                        let filtered = newValue.filter { $0.isHexDigit }
                        if filtered != newValue {
                            hexInput = String(filtered.prefix(6))
                        }
                        hexInput = String(hexInput.prefix(6))
                        if hexInput.count == 6, let color = Color(hex: hexInput) {
                            selectedColor = color
                        }
                    }
            }

            if hexInput.count == 6 {
                let isValid = Color(hex: hexInput) != nil
                Text(isValid ? "Valid hex color" : "Invalid hex color")
                    .font(.caption)
                    .foregroundColor(isValid ? .green : .red)
            }
        }
    }

    private var previewSection: some View {
        VStack(spacing: 8) {
            Text("Preview")
                .font(.subheadline)
                .fontWeight(.semibold)
                .frame(maxWidth: .infinity, alignment: .leading)

            HStack(spacing: 12) {
                RoundedRectangle(cornerRadius: 12)
                    .fill(selectedColor)
                    .frame(height: 60)
                    .overlay(
                        Text("Sample Text")
                            .foregroundColor(selectedColor.brightness() > 0.6 ? .black : .white)
                            .font(.headline)
                    )

                Circle()
                    .fill(selectedColor)
                    .frame(width: 44, height: 44)
            }
        }
    }
}

extension Color {
    init?(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        guard hex.count == 6, let intValue = UInt64(hex, radix: 16) else { return nil }
        self.init(
            red: Double((intValue >> 16) & 0xFF) / 255,
            green: Double((intValue >> 8) & 0xFF) / 255,
            blue: Double(intValue & 0xFF) / 255
        )
    }

    func brightness() -> Double {
        guard let components = cgColor?.components, components.count >= 3 else { return 0 }
        return (0.299 * components[0] + 0.587 * components[1] + 0.114 * components[2])
    }
}
