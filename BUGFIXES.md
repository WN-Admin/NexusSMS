# Bug Fixes Applied

This is a summary of every change made to get the project to open and build in Android Studio.

## Project structure (would not open at all)

- **Added Gradle Wrapper** — `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.4).
- **Removed `local.properties`** — was hardcoded to `/home/auz/Android/Sdk`. Android Studio will regenerate this on first open with your machine's SDK path.
- **Added launcher icons** — `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round` were referenced in the manifest but didn't exist. Added adaptive icons (`mipmap-anydpi-v26/`) plus PNG fallbacks at every density (`mipmap-mdpi` through `mipmap-xxxhdpi`).
- **Removed `ios/` folder** — Swift sources, irrelevant to the Android build.
- **Removed `.idea/` and `.gradle/`** — IDE/build caches that shouldn't be in a distributed project.

## Dependency / build script issues

- **Root `build.gradle.kts`**: removed `com.google.protobuf` plugin (no `.proto` files, no config block); bumped AGP to 8.1.4.
- **App `build.gradle.kts`**:
  - Removed non-existent dependencies: `androidx.telephony:telephony`, `androidx.telephony:telephony-data`, `androidx.filepicker:filepicker`, `com.google.protobuf:protobuf-kotlin-lite`, `org.bouncycastle:bcprov-jdk15on` (unused), `androidx.work:work-multiprocess` (unused).
  - Switched Compose dependencies to use the **Compose BOM** so versions stay in sync.
  - Added `androidx.hilt:hilt-work` + `androidx.hilt:hilt-compiler` (needed for `@HiltWorker`).
  - Added `androidx.lifecycle:lifecycle-viewmodel-compose`.
  - Set `release.isMinifyEnabled = false` (the original ProGuard rules were minimal and minify on a debug-signed release just makes testing harder).
- **`gradle.properties`**: removed `-XX:MaxPermSize=512m` (gone since Java 8).

## AndroidManifest

- Removed duplicate `SCHEDULE_EXACT_ALARM` permission.
- Removed `WRITE_SMS` (not a real runtime permission since KitKat).
- Removed `WRITE_EXTERNAL_STORAGE` (no-op on API 30+; kept `READ_EXTERNAL_STORAGE` with `maxSdkVersion="32"` and added `READ_MEDIA_IMAGES`).
- Removed `CHANGE_NETWORK_STATE` (unused).
- Removed `BIND_JOB_SERVICE` permission from `MessageService` — it extends `Service`, not `JobService`, so the permission was wrong and would have failed to bind.
- Removed the `<service>` declaration for `ScheduledMessageWorker` — it's a `CoroutineWorker`, not a `Service`. WorkManager handles its own lifecycle.
- Added `<provider>` block to disable WorkManager's default startup initializer so the Hilt-aware `Configuration.Provider` on `NexusSMSApplication` is used instead.
- Bumped `tools:targetApi` to 33.

## XML resources

- **`data_extraction_rules.xml`**: rewrote with the correct schema. The old version used `<domain-config>` (which is for `network_security_config.xml`, not data extraction).
- **`styles.xml`**: trimmed to just the activity theme. Compose drives all the actual styling via `MaterialTheme`.

## Code: critical compile errors

- **`ChatDetailScreen.kt`**: deleted the homemade `Modifier.widthIn(max: Dp)` and `Modifier.clickableElement(...)` extension functions. Both had nonsense bodies (`object : Modifier { override fun foldIn... = initial }`) and the `widthIn` one shadowed the real Compose modifier from `androidx.compose.foundation.layout`. Call sites now use the stdlib `Modifier.widthIn(max = 280.dp)` and `Modifier.clickable { onClick() }`.
- **`ConversationListScreen.kt`**: removed `import androidx.compose.material3.FAB` — `FAB` doesn't exist in Material3, only `FloatingActionButton`.
- **`SmsReceiver.kt`**: `conversationRepository.getConversationByPhone(...)` and `getConversation(...)` return `Flow<Conversation?>`, but the old code treated them as `Conversation?` / `Conversation`. Added `.first()` calls and proper null-checks.
- **`ScheduledMessageWorker.kt`**: rewrote as a `@HiltWorker` with `@AssistedInject` constructor. The old version used `@Inject lateinit var` fields, which Hilt does not auto-inject for `Worker` subclasses — they would have been `null` at runtime.

## Code: runtime hangs / leaks

- **`ConversationListViewModel.kt`**: `pinConversation`, `unpinConversation`, `muteConversation`, `unmuteConversation` previously did `repo.getConversation(id).collect { ... }`. The Room flow never completes, so the coroutine sat there forever and the update only fired the first time. Now uses `.first()`. Also switched the init-block flow observers to `launchIn(viewModelScope)` so they react to all updates instead of just the first.
- **`ChatViewModel.kt`**: `loadConversation` had the same bug — the `collect{}` blocked `loadMessages` from ever being called. Now uses two separate `launchIn` jobs that can be cancelled and replaced when the conversation changes. Also fixed `addReaction` to use `.first()` instead of `.collect{}`.
- **`SettingsViewModel.kt`**: `loadThemes()` and `loadSignatures()` were called in init **and** after every write operation, each launching a fresh `.collect{}`. That leaks observers indefinitely. Now uses `launchIn(viewModelScope)` once at construction and lets Room re-emit naturally on writes.
- **`ThemeManager.initializeDefaultThemes`**: was checking `themeRepository.getAllThemes().isEmpty()` — but `getAllThemes()` returns a `Flow<List<Theme>>`, not a `List`. The check was always false. Fixed to materialize via `.first().isEmpty()`.

## Code: smaller fixes

- **`MainScreen.kt`**: was ignoring the `paddingValues` from `Scaffold`, so content drew under the bottom navigation bar. Now wraps content in a `Box(Modifier.padding(paddingValues))`.
- **`ChatDetailScreen.kt`**: `viewModel.loadConversation(conversationId)` was being called directly in the composable body (re-runs on every recomposition). Now wrapped in `LaunchedEffect(conversationId)`.
- **`MainActivity.kt`**: stripped unused imports (`Build`, `MaterialTheme`, color schemes, etc.).
- **`NexusSMSApplication.kt`**: now implements `Configuration.Provider` with an injected `HiltWorkerFactory` so `ScheduledMessageWorker` can be constructed.
- **All screens**: switched from the deprecated Material 3 `Divider()` to `HorizontalDivider()`.
- Various unused imports removed across multiple files.

## What still doesn't work (intentional — out of scope for "make it build")

- **`SocialMediaIntegrationService.disconnectAccount`** is a stub.
- **`MessageService.sendSMS`** uses the deprecated `SmsManager.getDefault()` — works on all supported APIs, but on API 31+ you'd want `context.getSystemService(SmsManager::class.java)`.
- **Default-SMS-app handling**: this app is not registered as the system default SMS app (would need additional intent filters and the `WRITE_SMS` permission gate, plus the user explicitly setting it as default in system settings). It will *receive* SMS via the broadcast receiver but the system also delivers them to the system default app simultaneously.
- **`addReaction` / `sendReadReceipt` / `sendTypingIndicator` / `shareSticker`** in `RcsService` are mostly empty bodies. RCS over an unofficial protocol would need real backend work.
- **No actual unit tests** beyond the JUnit dependencies being present.

## Opening it

1. Unzip.
2. `File → Open` in Android Studio, point at the unzipped folder.
3. Android Studio will create `local.properties` automatically pointing at your SDK.
4. Let it sync Gradle (~3–5 min first time, downloads ~500 MB of dependencies and the AGP/Kotlin/Compose toolchain).
5. Run on an emulator running API 24+ or a connected device.
