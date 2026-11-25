# Post views — Firebase (dev debug + dev release)

## Current behavior

| Build | `IMPRESSIONS_SERVER_BATCH_ENABLED` | Impressions | Dwell |
|-------|-------------------------------------|-------------|-------|
| **debug** | `false` (`android/app/build.gradle`) | Firebase Analytics event `post_view` on flip settle | Not sent to server |
| **release** (dev test / Firebase path) | `false` | Same Firebase `post_view` on flip settle | Not sent to server |
| **Play Store legacy** | `true` until next Play release with flag flipped | Local queue → `POST /app/v3/views/batch` | Same batch endpoint |

Wiring: `HomePage.onFlippedToPage1` → `ViewsBatchTracker.onFlipSettled` → `PostViewAnalytics` or enqueue + timer flush.

## Dev testing

1. Build debug APK (e.g. `AnyNews_dev_views_firebase_debug.apk`); enable **Firebase DebugView** (`adb shell setprop debug.firebase.analytics.app com.anyNews.anynews`).
2. Flip articles on Home; confirm `post_view` in DebugView (params: `item_id` = post id, `draft_id`, `feed_position`).
3. Logcat: filter `OkHttp` — **`views/batch` must not appear** on debug or dev-release builds while flipping.

## Not implemented (by design)

- No server cron / BigQuery pipeline for Firebase impressions (cost).
- Admin “Users / Impressions” for debug Firebase traffic is **not** fed from the API.

## Optional future

- GA4 BigQuery export → server or worker bulk ingest (only if product wants server-side rollups).
- Flip Play **release** back to `true` only when shipping batch impressions to production API.

## Related files

- `PostViewAnalytics.java` — Firebase `post_view`
- `ViewsBatchTracker.java` — batch gate + `onFlipSettled`
- `PostDwellTracker.java` — dwell segments (batch builds only)