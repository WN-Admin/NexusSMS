import SwiftUI

enum MediaType: String, CaseIterable, Identifiable {
    case camera
    case gallery
    case video
    case audio
    case document
    case location
    case contact

    var id: String { rawValue }

    var icon: String {
        switch self {
        case .camera:    return "camera.fill"
        case .gallery:   return "photo.on.rectangle"
        case .video:     return "video.fill"
        case .audio:     return "mic.fill"
        case .document:  return "doc.fill"
        case .location:  return "location.fill"
        case .contact:   return "person.crop.square.fill"
        }
    }

    var label: String {
        switch self {
        case .camera:    return "Camera"
        case .gallery:   return "Gallery"
        case .video:     return "Video"
        case .audio:     return "Audio"
        case .document:  return "Document"
        case .location:  return "Location"
        case .contact:   return "Contact"
        }
    }
}

struct MediaPickerView: View {
    @Environment(\.dismiss) private var dismiss
    let onMediaSelected: (MediaType) -> Void

    private let columns = Array(
        repeating: GridItem(.flexible(), spacing: 16),
        count: 3
    )

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(MediaType.allCases) { type in
                        Button {
                            onMediaSelected(type)
                            dismiss()
                        } label: {
                            VStack(spacing: 10) {
                                Image(systemName: type.icon)
                                    .font(.system(size: 32))
                                    .foregroundColor(.accentColor)
                                    .frame(width: 60, height: 60)
                                    .background(Color.accentColor.opacity(0.1))
                                    .clipShape(RoundedRectangle(cornerRadius: 14))

                                Text(type.label)
                                    .font(.caption)
                                    .fontWeight(.medium)
                                    .foregroundColor(.primary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color(.systemGray6))
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(16)
            }
            .navigationTitle("Attach Media")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}
