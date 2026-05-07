import SwiftUI

struct ScheduledMessagesView: View {
    @EnvironmentObject var appState: AppState
    @State private var showingRescheduleSheet = false
    @State private var rescheduleTarget: ScheduledMessage?
    @State private var rescheduleDate = Date()

    private let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .short
        return f
    }()

    var body: some View {
        ZStack {
            if appState.scheduledMessageService.scheduledMessages.isEmpty {
                emptyState
            } else {
                listContent
            }
        }
        .navigationTitle("Scheduled Messages")
        .sheet(isPresented: $showingRescheduleSheet) {
            rescheduleSheet
        }
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "calendar.badge.clock")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text("No Scheduled Messages")
                .font(.title2)
                .fontWeight(.semibold)
            Text("Schedule messages to be sent automatically at a later time.")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
        }
    }

    private var listContent: some View {
        List {
            ForEach(appState.scheduledMessageService.scheduledMessages) { msg in
                VStack(alignment: .leading, spacing: 6) {
                    Text(msg.content)
                        .font(.body)
                        .lineLimit(2)

                    HStack {
                        Image(systemName: "clock")
                            .font(.caption)
                        Text(dateFormatter.string(from: msg.scheduledTime))
                            .font(.caption)
                        Spacer()
                        statusBadge(msg.status)
                    }
                    .foregroundColor(.secondary)
                }
                .padding(.vertical, 4)
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button(role: .destructive) {
                        appState.scheduledMessageService.delete(msg)
                    } label: {
                        Label("Delete", systemImage: "trash")
                    }

                    Button {
                        appState.scheduledMessageService.cancel(msg.id)
                    } label: {
                        Label("Cancel", systemImage: "xmark.circle")
                    }
                    .tint(.orange)
                }
                .contextMenu {
                    Button {
                        rescheduleTarget = msg
                        rescheduleDate = msg.scheduledTime
                        showingRescheduleSheet = true
                    } label: {
                        Label("Reschedule", systemImage: "calendar")
                    }

                    Button(role: .destructive) {
                        appState.scheduledMessageService.delete(msg)
                    } label: {
                        Label("Delete", systemImage: "trash")
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func statusBadge(_ status: String) -> some View {
        HStack(spacing: 4) {
            Circle()
                .fill(statusColor(status))
                .frame(width: 8, height: 8)
            Text(status)
                .font(.caption)
                .fontWeight(.semibold)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 3)
        .background(statusColor(status).opacity(0.12))
        .cornerRadius(6)
    }

    private func statusColor(_ status: String) -> Color {
        switch status {
        case "PENDING": return .blue
        case "SENT": return .green
        case "CANCELLED": return .orange
        case "FAILED": return .red
        default: return .gray
        }
    }

    private var rescheduleSheet: some View {
        NavigationStack {
            Form {
                Section("New Date & Time") {
                    DatePicker("Scheduled Time", selection: $rescheduleDate, in: Date()...)
                        .datePickerStyle(.graphical)
                }

                if let target = rescheduleTarget {
                    Section("Message") {
                        Text(target.content)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Reschedule")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        showingRescheduleSheet = false
                        rescheduleTarget = nil
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        if let target = rescheduleTarget {
                            appState.scheduledMessageService.reschedule(target.id, to: rescheduleDate)
                        }
                        showingRescheduleSheet = false
                        rescheduleTarget = nil
                    }
                    .fontWeight(.semibold)
                }
            }
        }
    }
}
