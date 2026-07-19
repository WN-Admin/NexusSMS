# NexusSMS — Fix Plan, Phase 2

Follow-up sweep covering everything marked "not yet audited" in `fix-plan.md`: the four platform sync
services in full (not just token storage), the floating-bubble overlay service, a scan of the ~50
UI screens not touched by earlier passes, and a data-safety-form accuracy note. Same repo state,
commit `d81155f`.

Use alongside `fix-plan.md` — numbering continues from where that file's priority tiers left off
conceptually, but items here are self-contained.

---

## P1 — Fix before wide public rollout

### [ ] 11. Matrix sync loses messages beyond the homeserver's default timeline limit — no pagination/backfill
**File:** `features/matrix/MatrixSyncService.kt`

`sync()` takes `response.rooms?.join[roomId].timeline?.events` as if it's the complete set of new
messages for a room. Matrix servers intentionally truncate the timeline in a `/sync` response
(a `limited: true` flag plus a `prev_batch` token signal there's more history to page through via
`/rooms/{roomId}/messages`). This code never checks `limited` or calls the messages-pagination endpoint,
so:
- If more messages arrived in a room than the server's default timeline limit while the app was closed
  (device off, process killed for a while, etc.), everything past that limit is silently never imported —
  not delayed, **permanently skipped**, since the next sync's `since` token starts from where the last
  one left off, not from the gap.

**Suggested fix:**
```kotlin
val timeline = roomSync.timeline
if (timeline?.limited == true && timeline.prevBatch != null) {
    var prevBatch = timeline.prevBatch
    var pagesFetched = 0
    while (prevBatch != null && pagesFetched < MAX_BACKFILL_PAGES) {
        val page = api.getRoomMessages(token, roomId, from = prevBatch, dir = "b", limit = 100)
        // process page.chunk the same way as `events` below, then:
        prevBatch = page.end
        pagesFetched++
        if (page.chunk.isEmpty()) break
    }
}
```
Also worth persisting a per-room "last known event ID" so a gap can be detected and backfilled even if
`limited` isn't set for some transport reason.

### [ ] 12. Matrix `lastSyncToken` and Telegram `lastUpdateId` are in-memory only — same class of bug already fixed for WebDAV
**Files:** `features/matrix/MatrixSyncService.kt:24`, `features/telegram/TelegramService.kt:41`

```kotlin
private var lastSyncToken: String? = null   // MatrixSyncService — never persisted
private var lastUpdateId: Long? = null      // TelegramService — never persisted
```
Both are plain fields on a `@Singleton`. When the app process dies (which WorkManager-driven background
sync guarantees will happen between runs), these reset to `null`:
- Matrix: `incrementalSync()` sees `since == null` and silently falls back to `initialSync()` every
  time the process restarts — combined with #11 above, this compounds the missed-message risk.
- Telegram: `getUpdates(offset = null)` re-fetches from Telegram's default backlog, which can
  re-import already-seen messages (partially mitigated by de-dup elsewhere, but worth confirming) or
  behave inconsistently depending on how long the bot's update queue has been sitting.

**Suggested fix:** persist both in a small `DataStore`/`SharedPreferences` entry, written after every
successful sync and read at service construction:
```kotlin
class MatrixSyncService @Inject constructor(
    ...,
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("matrix_sync_state", Context.MODE_PRIVATE)
    private var lastSyncToken: String?
        get() = prefs.getString("last_sync_token", null)
        set(value) { prefs.edit().putString("last_sync_token", value).apply() }
    ...
```
(Same pattern for `lastUpdateId` in `TelegramService`, using `putLong`/`getLong` with a sentinel for null.)

### [ ] 13. Telegram sync silently drops every non-text update (photos, stickers, documents, edits)
**File:** `features/telegram/TelegramService.kt:150-157`

```kotlin
for (update in updates) {
    val msg = update.message ?: update.channelPost ?: continue
    if (msg.text.isNullOrBlank()) continue        // <-- skips BEFORE advancing lastUpdateId
    lastUpdateId = (update.updateId + 1)
    ...
```
Two problems in these five lines:
- Any update without plain `text` (a photo with only a caption in a different field, a sticker, a
  document, an edited-message update, etc.) is dropped entirely — not imported as any message type.
  For a "unified messaging" app that's a real feature gap on the Telegram side specifically (Matrix and
  Discord both handle non-text content types elsewhere in their sync code).
- Because the `continue` happens *before* `lastUpdateId` is advanced, Telegram's `getUpdates` will keep
  re-returning that same non-text update on every subsequent sync (Telegram's offset semantics require
  acknowledging every update you've seen, not just the ones you acted on) — wasted bandwidth every sync
  cycle, forever, until a text message happens to arrive after it in the same batch.

**Suggested fix:** advance the offset for every update regardless of whether it's imported, and handle
at least photo/document msgtypes the way Matrix does (map to `IMAGE`/`FILE` content types with a media
URL) instead of dropping them:
```kotlin
for (update in updates) {
    val msg = update.message ?: update.channelPost
    if (msg == null) { lastUpdateId = update.updateId + 1; continue }
    lastUpdateId = update.updateId + 1   // always advance, regardless of content type
    val text = msg.text ?: msg.caption
    if (text.isNullOrBlank() && msg.photo == null && msg.document == null) continue
    // ... build Message with contentType TEXT/IMAGE/FILE based on which field is present
}
```

### [ ] 14. Discord sync has no rate-limit handling and will fail silently for bots in more than a few guilds
**File:** `features/discord/DiscordService.kt` (`sync()`)

The sync loop is `for (guild in guilds) { for (channel in channels) { api.getMessages(...) } }` — one
HTTP call per channel, with no delay, no 429 detection, and no backoff. Discord's per-route rate limits
(roughly 5 requests/5s on many endpoints) will be hit quickly for any bot in a handful of guilds with
several channels each; those requests will fail, and there's no visible handling for a 429 response
specifically (only the generic per-channel `try/catch` that logs and `continue`s past a failure).
Net effect: a moderately active bot account will silently fail to import messages from most of its
channels on every sync, with no indication to the user why messages are missing.

**Suggested fix:** respect Discord's rate-limit headers (`X-RateLimit-Remaining`, `X-RateLimit-Reset-After`)
and add a small delay/backoff between requests in the channel loop; on a 429, parse `retry_after` from
the response body and delay before retrying that single request rather than moving on and losing it.

### [ ] 15. Floating bubble overlay never checks `SYSTEM_ALERT_WINDOW` permission before drawing
**File:** `features/notifications/FloatificationService.kt`

`showFloatification()` calls `windowManager.addView(overlayView, params)` with no prior check of
`Settings.canDrawOverlays(context)` anywhere in the file (confirmed — zero references to
`canDrawOverlays` in the whole codebase). `SYSTEM_ALERT_WINDOW` is a special permission the user must
grant manually in system settings; it isn't covered by the standard runtime-permission dialog. If it's
not granted, `addView` throws, which is caught by the surrounding `try/catch` — so it won't crash, but
the feature just does nothing on every trigger, forever, with only a `Timber.e` log the user never sees.

**Suggested fix:**
```kotlin
fun showFloatification(senderName: String, messagePreview: String, conversationId: String) {
    if (!enabled) return
    if (!Settings.canDrawOverlays(context)) {
        Timber.w("Floatification enabled but overlay permission not granted")
        return  // or: post a regular notification as a fallback, or surface a settings prompt
    }
    ...
```
Also worth disabling the "floating bubble" toggle in Settings UI (or showing a permission-needed badge)
when `canDrawOverlays()` is false, so the user isn't left wondering why nothing shows up.

---

## P3 — Minor / defensive coding

### [ ] 16. A few `.first()` calls on collections whose emptiness isn't locally obvious
**Files:** `ui/screens/ConversationListScreen.kt:757`, others found by grep

```kotlin
text = conversation.sourcePlatform.first().toString(),
```
`sourcePlatform` defaults to `"SMS"` in the `Conversation` model and every sync path sets it to a
non-empty platform string, so this isn't a confirmed live crash — but it's one accidental
`Conversation(sourcePlatform = "")` away from a `NoSuchElementException` crashing the conversation list.
Cheap to harden: `conversation.sourcePlatform.firstOrNull()?.toString() ?: "?"`.
(Checked the other `.first()`/`.last()`/`[0]` call sites flagged by the same grep across `ui/screens` —
`SpamDetectionScreen.kt`, `SocialAccountsScreen.kt`, `UnifiedContactsListScreen.kt` — all are correctly
guarded with an `isNotEmpty()` check beforehand. No action needed on those.)

---

## Swept this pass, no further issues found

- **UI screens (~50 files not touched by earlier passes)** — grepped for `TODO`/`FIXME` (zero hits,
  confirming the two found in `ConversationListScreen.kt` back in the first pass are still the only
  ones in the whole `ui/` tree) and for unsafe collection access (`.first()`/`.last()`/`[0]` without a
  preceding guard) — results covered in #16 above. No crash-causing or logic-inverting bugs found in a
  broader read-through of list/detail/settings screens; this class of screen is largely thin Compose
  wrappers over the ViewModels already audited in Phase 1.
- **Messenger (`features/messenger/MessengerService.kt`) sync** — uses cursor-based pagination
  correctly (`convResponse.paging?.cursors?.after`, capped by `maxPages`), which is the one platform
  integration that already does pagination right. No issues found.
- **`SmsSender`, `NotificationActionReceiver`, `SmsStatusReceiver`** — already covered in Phase 1,
  re-confirmed no changes.

---

## Play Store data-safety-form accuracy note

Not a code fix, but worth doing alongside the above: once P0 items from `fix-plan.md` are addressed
(plaintext bot tokens, fake E2E), the Play Console "Data safety" declaration should explicitly list, at
minimum:
- **SMS content** — read, sent, stored locally (and to WebDAV if configured).
- **Contacts** — read/write, used for unified contact matching.
- **Approximate/precise location** — collected only when the user actively shares location in a chat
  (true today, but only *after* item #8 from `fix-plan.md` removes the upfront request at launch —
  until then, the permission is requested before any location-sharing intent is expressed, which reads
  as broader collection than the feature needs).
- **Third-party platform tokens** (Telegram/Discord/Messenger bot tokens, Matrix access token) — stored
  on-device; flag as sensitive credential storage, and update this note once P0 #4 (token encryption)
  lands.
- Whether **safety-number/E2E verification** is described as full end-to-end encryption anywhere in
  store listing copy — it shouldn't be, until P0 #2 in `fix-plan.md` is actually implemented.
