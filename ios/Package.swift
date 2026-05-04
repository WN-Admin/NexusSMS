// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NexusSMSApp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .executable(name: "NexusSMSApp", targets: ["NexusSMSApp"])
    ],
    targets: [
        .executableTarget(
            name: "NexusSMSApp",
            dependencies: [],
            path: "Sources/NexusSMSApp"
        )
    ]
)
