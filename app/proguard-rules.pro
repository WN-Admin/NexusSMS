# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Room Database
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class **_Factory { *; }
-keep class **_Module { *; }
-keep class **_Impl { *; }

# Data Models
-keep class com.nexussms.data.models.** { *; }
-keep class com.nexussms.data.repository.** { *; }

# Security
-keep class androidx.security.crypto.** { *; }
-keep class javax.crypto.** { *; }

# Retrofit & OkHttp
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class com.google.gson.** { *; }

# Protocol Buffers
-keep class com.google.protobuf.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keep class **_Worker { *; }

# Keep Kotlin metadata
-keepclassmembers class ** {
    *** Companion;
}
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Signature
-keepattributes SourceFile
-keepattributes LineNumberTable
