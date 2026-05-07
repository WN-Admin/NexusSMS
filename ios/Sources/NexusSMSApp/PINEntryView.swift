import SwiftUI

struct PINEntryView: View {
    let title: String
    let digitCount: Int
    let onPINEntered: (String) -> Void

    @State private var pin: String = ""
    @State private var errorMessage: String?
    @FocusState private var isFocused: Bool

    private let maxDigits: Int

    init(
        title: String = "Enter PIN",
        digitCount: Int = 4,
        onPINEntered: @escaping (String) -> Void
    ) {
        self.title = title
        self.digitCount = max(4, min(6, digitCount))
        self.maxDigits = self.digitCount
        self.onPINEntered = onPINEntered
    }

    var body: some View {
        VStack(spacing: 24) {
            Text(title)
                .font(.title2)
                .fontWeight(.semibold)

            digitIndicator

            SecureField("PIN", text: $pin)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .focused($isFocused)
                .frame(width: 0, height: 0)
                .opacity(0.001)
                .onChange(of: pin) { _, newValue in
                    let filtered = newValue.filter { $0.isNumber }
                    if filtered != newValue || filtered.count > maxDigits {
                        pin = String(filtered.prefix(maxDigits))
                    }
                    errorMessage = nil
                    if pin.count == maxDigits {
                        isFocused = false
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                            onPINEntered(pin)
                        }
                    }
                }

            if let error = errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundColor(.red)
                    .transition(.opacity)
            }
        }
        .onAppear { isFocused = true }
    }

    private var digitIndicator: some View {
        HStack(spacing: 12) {
            ForEach(0..<maxDigits, id: \.self) { index in
                Circle()
                    .stroke(lineWidth: 2)
                    .foregroundColor(index < pin.count ? .accentColor : .secondary.opacity(0.4))
                    .frame(width: 16, height: 16)
                    .overlay(
                        Circle()
                            .fill(Color.accentColor)
                            .frame(width: 10, height: 10)
                            .opacity(index < pin.count ? 1 : 0)
                    )
            }
        }
    }

    func showError(_ message: String) {
        withAnimation {
            errorMessage = message
            pin = ""
        }
        isFocused = true
    }
}
