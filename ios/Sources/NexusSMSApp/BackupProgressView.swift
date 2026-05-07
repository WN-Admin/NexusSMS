import SwiftUI

enum BackupStage: String {
    case shortcuts   = "Backing up shortcuts..."
    case encrypting  = "Encrypting data..."
    case uploading   = "Uploading..."
    case verifying   = "Verifying backup..."
    case cleaning    = "Cleaning up..."
}

enum BackupStatus {
    case inProgress
    case completed
    case failed(String)
}

struct BackupProgressView: View {
    let progress: Double?
    let stage: BackupStage
    let status: BackupStatus

    var body: some View {
        VStack(spacing: 20) {
            statusIcon
                .font(.system(size: 48))

            Text(stage.rawValue)
                .font(.headline)
                .multilineTextAlignment(.center)

            if let progress = progress {
                ProgressView(value: max(0, min(1, progress)))
                    .progressViewStyle(.linear)
                    .frame(maxWidth: 250)

                Text("\(Int(progress * 100))%")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .monospacedDigit()
            } else {
                ProgressView()
                    .progressViewStyle(.circular)
            }

            if case .failed(let message) = status {
                Text(message)
                    .font(.caption)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
            }
        }
        .padding(32)
        .frame(maxWidth: 320)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(.regularMaterial)
        )
        .shadow(radius: 10)
    }

    @ViewBuilder
    private var statusIcon: some View {
        switch status {
        case .inProgress:
            Image(systemName: "arrow.triangle.2.circlepath.icloud")
                .foregroundColor(.accentColor)
                .symbolEffect(.rotate, options: .repeat(.continuous))
        case .completed:
            Image(systemName: "checkmark.circle.fill")
                .foregroundColor(.green)
        case .failed:
            Image(systemName: "xmark.circle.fill")
                .foregroundColor(.red)
        }
    }
}
