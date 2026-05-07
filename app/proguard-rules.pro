# NexusSMS App
-keep class com.nexussms.** { *; }
-keep interface com.nexussms.** { *; }
-keep enum com.nexussms.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.* class * { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class hilt_aggregated_deps { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules_BindsModule { *; }
-keep class **_HiltModules_Module { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Google Drive / Play Services
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.android.gms.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Timber
-keep class com.jakewharton.timber.** { *; }
