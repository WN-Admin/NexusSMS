import SwiftUI

struct BackupView: View {
    @ObservedObject var viewModel: BackupViewModel
    @State private var showRestoreConfirmation = false
    @State private var selectedBackup: BackupMetadata?
    @State private var restoreSucceeded = false
    @State private var showError = false

    private let sizeFormatter: ByteCountFormatter = {
        let f = ByteCountFormatter()
        f.countStyle = .file
        return f
    }()

    private let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .short
        return f
    }()

    var body: some View {
        List {
            manualBackupSection
            backupHistorySection
            restoreSection
        }
        .navigationTitle("Backup & Restore")
        .alert("Restore Backup", isPresented: $showRestoreConfirmation, presenting: selectedBackup) { backup in
            Button("Restore", role: .destructive) {
                restoreBackup(backup.id)
            }
            Button("Cancel", role: .cancel) {}
        } message: { backup in
            Text("Restore backup from \(dateFormatter.string(from: backup.timestamp))? This will replace all current data.")
        }
        .alert("Restore Complete", isPresented: $restoreSucceeded) {
            Button("OK") { restoreSucceeded = false }
        } message: {
            Text("Your data has been restored successfully.")
        }
        .alert("Restore Failed", isPresented: $showError) {
            Button("OK") { showError = false }
        } message: {
            Text(viewModel.errorMessage ?? "An unknown error occurred.")
        }
        .onAppear {
            viewModel.load()
        }
    }

    private var manualBackupSection: some View {
        Section("Manual Backup") {
            if viewModel.isBackingUp {
                HStack {
                    ProgressView()
                        .padding(.trailing, 8)
                    Text("Creating backup...")
                        .foregroundColor(.secondary)
                }
            } else {
                Button(action: {
                    Task { await viewModel.createBackup() }
                }) {
                    Label("Create Backup", systemImage: "icloud.and.arrow.up")
                }
            }
        }
    }

    private var backupHistorySection: some View {
        Section("Backup History") {
            if viewModel.backups.isEmpty {
                Text("No backups yet")
                    .foregroundColor(.secondary)
                    .font(.subheadline)
            } else {
                ForEach(viewModel.backups) { backup in
                    HStack {
                        Image(systemName: statusIcon(backup.status))
                            .foregroundColor(statusColor(backup.status))
                            .font(.title3)

                        VStack(alignment: .leading, spacing: 2) {
                            Text(dateFormatter.string(from: backup.timestamp))
                                .font(.subheadline)
                            Text(sizeFormatter.string(fromByteCount: backup.size))
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }

                        Spacer()

                        if backup.isAutomatic {
                            Text("Auto")
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Capsule().fill(Color.secondary.opacity(0.15)))
                        }
                    }
                    .padding(.vertical, 2)
                }
            }
        }
    }

    private var restoreSection: some View {
        Section("Restore") {
            if viewModel.backups.isEmpty {
                Text("No backups available to restore")
                    .foregroundColor(.secondary)
                    .font(.subheadline)
            } else {
                ForEach(viewModel.backups) { backup in
                    Button(action: {
                        selectedBackup = backup
                        showRestoreConfirmation = true
                    }) {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(dateFormatter.string(from: backup.timestamp))
                                    .font(.subheadline)
                                    .foregroundColor(.primary)
                                Text(sizeFormatter.string(fromByteCount: backup.size))
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                            Image(systemName: "arrow.down.doc.fill")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
        }
    }

    private func statusIcon(_ status: String) -> String {
        switch status {
        case "COMPLETED": return "checkmark.circle.fill"
        case "FAILED": return "xmark.circle.fill"
        case "IN_PROGRESS": return "arrow.triangle.2.circlepath"
        default: return "circle"
        }
    }

    private func statusColor(_ status: String) -> Color {
        switch status {
        case "COMPLETED": return .green
        case "FAILED": return .red
        case "IN_PROGRESS": return .blue
        default: return .gray
        }
    }

    private func restoreBackup(_ id: UUID) {
        Task {
            let success = await viewModel.restore(id)
            await MainActor.run {
                if success {
                    restoreSucceeded = true
                } else {
                    showError = true
                }
            }
        }
    }
}
