# Post views — Phase 1 (server batch) + Firebase dev

## Phase 1 shipped (current tree)

| Build | `IMPRESSIONS_SERVER_BATCH_ENABLED` | Impressions | Article onFlip GCM |
|-------|-------------------------------------|-------------|-------------------|
| **debug** | `true` | Local queue → `POST /app/v3/views/batch` | **Off** (batch path) |
| **release** | `true` | Same batch endpoint | **Off** |
| **Ad seats** | — | — | GCM upstream **on** (ad telemetry only) |

- Flip settle: `HomePage.onFlippedToPage1` → `ViewsBatchTracker.onFlipSettled` → enqueue + timer / count / lifecycle flush.
- Article **onFlip** FCM upstream (`BundleDataToGcmPush`) is **deprecated** for posts when batch is enabled; do not re-enable for articles without product sign-off.
- Flush: Home `onPause` (leave Home), process background (`ON_STOP`), trim / low memory; timer default **60s** (`AnalyticsBatchConfig.DEFAULT_VIEWS_INTERVAL_SEC`); early flush at **12** events (admin `views_batch_flush_*`).
- Pending queue: `SharedPrefs` key `app_views_batch_pending_json` — restored on next cold start.

## Firebase-only builds (rollback / local experiments)

Set `IMPRESSIONS_SERVER_BATCH_ENABLED` to `false` in `android/app/build.gradle` to restore Firebase `post_view` on flip via `PostViewAnalytics` (no `views/batch`).

## Dev testing (batch on)

1. Build debug or release APK with flag `true`.
2. Flip articles on Home; logcat filter `OkHttp` — expect periodic or leave-flush `views/batch` (not per-flip).
3. Confirm **no** GCM `onFlip` for articles (ads may still send).

## Not implemented (by design)

- No server cron / BigQuery pipeline for Firebase impressions (cost).
- Admin “Users / Impressions” does not ingest Firebase `post_view` when batch is on.

## Related files

- `PostViewAnalytics.java` — Firebase `post_view` (batch flag off)
- `ViewsBatchTracker.java` — batch gate + `onFlipSettled` + prefs backup
- `PostDwellTracker.java` — dwell segments (batch builds only)
- `AnalyticsBatchConfig.java` — flush interval / count defaults