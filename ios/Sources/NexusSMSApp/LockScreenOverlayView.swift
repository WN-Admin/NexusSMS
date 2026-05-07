import SwiftUI
import LocalAuthentication

struct LockScreenOverlayView: View {
    let onUnlock: () -> Void

    @State private var pinEntryView: PINEntryView?
    @State private var biometricAvailable = false

    var body: some View {
        ZStack {
            Rectangle()
                .fill(.ultraThinMaterial)
                .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                Image(systemName: "lock.fill")
                    .font(.system(size: 48))
                    .foregroundColor(.secondary)

                Text("App Locked")
                    .font(.title)
                    .fontWeight(.bold)

                Text("Enter your PIN to unlock")
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                PINEntryView(
                    title: "",
                    digitCount: 4,
                    onPINEntered: { pin in
                        if pin == storedPIN {
                            onUnlock()
                        } else {
                            pinEntryView?.showError("Incorrect PIN. Try again.")
                        }
                    }
                )
                .onAppear { pinEntryView = PINEntryView(title: "", digitCount: 4, onPINEntered: { _ in }) }
                .padding(.horizontal, 40)

                if biometricAvailable {
                    Button {
                        authenticateBiometric()
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: biometricIcon)
                            Text(biometricLabel)
                        }
                        .font(.body)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 10)
                        .background(Color.accentColor.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                    .buttonStyle(.plain)
                }

                Spacer()
            }
        }
        .onAppear {
            checkBiometrics()
        }
    }

    private var storedPIN: String {
        UserDefaults.standard.string(forKey: "app_pin") ?? ""
    }

    private var biometricIcon: String {
        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return "lock"
        }
        switch context.biometryType {
        case .faceID:  return "faceid"
        case .touchID: return "touchid"
        case .opticID: return "opticid"
        default:       return "lock"
        }
    }

    private var biometricLabel: String {
        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return "Unlock"
        }
        switch context.biometryType {
        case .faceID:  return "Face ID"
        case .touchID: return "Touch ID"
        case .opticID: return "Optic ID"
        default:       return "Biometric Unlock"
        }
    }

    private func checkBiometrics() {
        let context = LAContext()
        var error: NSError?
        biometricAvailable = context.canEvaluatePolicy(
            .deviceOwnerAuthenticationWithBiometrics,
            error: &error
        )
    }

    private func authenticateBiometric() {
        let context = LAContext()
        context.evaluatePolicy(
            .deviceOwnerAuthenticationWithBiometrics,
            localizedReason: "Unlock NexusSMS"
        ) { success, _ in
            if success {
                DispatchQueue.main.async {
                    onUnlock()
                }
            }
        }
    }
}
