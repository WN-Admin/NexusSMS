import SwiftUI

struct AppLockView: View {
    @ObservedObject var viewModel: AppLockViewModel

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "lock.shield.fill")
                .font(.system(size: 64))
                .foregroundColor(.accentColor)

            Text("App Locked")
                .font(.title)
                .fontWeight(.bold)

            Text("Enter your PIN to unlock")
                .font(.subheadline)
                .foregroundColor(.secondary)

            SecureField("PIN", text: $viewModel.pinInput)
                .textFieldStyle(.roundedBorder)
                .keyboardType(.numberPad)
                .multilineTextAlignment(.center)
                .frame(maxWidth: 200)
                .onSubmit {
                    viewModel.verifyPin()
                }

            if let error = viewModel.error {
                Text(error)
                    .font(.callout)
                    .foregroundColor(.red)
                    .transition(.opacity)
            }

            Button(action: {
                viewModel.verifyPin()
            }) {
                Text("Unlock")
                    .fontWeight(.semibold)
                    .frame(maxWidth: 200)
            }
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.pinInput.isEmpty)

            Spacer()
        }
        .padding()
        .background(Color(.systemBackground))
    }
}
