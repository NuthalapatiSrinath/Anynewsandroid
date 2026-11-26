# Manual verify — critical APK (feed + ad page)

**Build:** `assembleRelease` → install on emulator-5554 or device.  
**APK copy:** `android/app/build/outputs/apk/release/AnyNews_<versionName>_release.apk` (or copy from there to **Downloads** for sideload).  
**Edge API HUD:** Top-right rolling log when `DEV_EDGE_HUD` / `EDGE_DEBUG_HUD` are true (current dev-API release). CF HIT = white, CF MISS = amber, Redis/Mongo/List POST = red. Flip the feed and confirm lines update while scrolling.  
**Tab:** Home / main national feed (center flip stream, not Special/Search/Video).

## A — Feed must not stop ~10

1. Cold-start app → wait for first card (skeleton clears).
2. Flip up rapidly through **20+** cards (do not pause on ads more than 1s each).
3. **Pass:** No hard stop at card 10–12; flip keeps going; new articles appear after ~15–18.
4. **Fail:** Toast “no articles further” with list size ≤12, or flip rubber-bands at end with no new pages.

**Log tag (optional):** `feed append seat=` in logcat after prefetch.

## Gemini cross-check — pagination trigger (dev `HomePage.java`)

| Step | Method | Lines (approx.) |
|------|--------|-----------------|
| Flip settle → prefetch schedule | `onFlippedToPage1` → `refreshDetails()` | ~4939 |
| Debounced near-end gate | `refreshDetails` → `runNearEndFeedPrefetch` | ~4764–4808 |
| `FEED_PREFETCH_FROM_END = 5` | constant | ~300 |
| Fire `List_All` / edge page-0 | `maybePrefetchNextFeedPage` → `ShowNews_Data_Values(..., 3)` | ~4824–4842 |
| Buffer response (prod pattern) | `convertListOfArticles` `reqPos==3` early return | ~4279–4287 |
| Merge buffered page | `applyBufferedFeedPage` → `convertListOfArticles(dummy,-1)` | ~4809–4817 |
| Keep flip index on append | `syncFlipViewToFeedIndex(seatBefore)` | ~4399–4407 |
| Over-flip safety net | `onOverFlip` → `applyBufferedFeedPage` | ~2135–2141 |

Prod reference: `uploads/HomePage-L1-L4598-0.java` — `refreshDetails` ~3299, `convertListOfArticles` ~2960, `onOverFlip` ~1310.

## B — Ad seats (0-based FlipView index)

Client-fixed indices: **1, 5, 9, 14, 19** (`FeedAdConfig.FIXED_AD_POSITIONS`).

| Flip # (1st card = post 0) | Seat index | What you see |
|---|---|---|
| 2nd card | **1** | Ad page |
| 6th card | **5** | Ad page |
| 10th card | **9** | Ad page |

## C — Ad page layout (%)

On any ad seat:

1. **Top ~10%:** “Advertise on AnyNews” link (blue, top-right).
2. **~30%:** Native ad area (filled ad OR dark gray `#1A1A1A` placeholder — not full black void).
3. **~10%:** Empty gap between ad and Recommended (middle stays empty if no ad fill).
4. **~38%:** “Recommended” title + 0–3 rows (thumb + headline) pinned **bottom** of card body.
5. **~10%:** “FLIP UP TO SKIP” pill above footer (inside flip card only).

## D — Footer on ad seat

- Bottom tab bar (Home / Special / Share / Videos / Profile) **visible** on ad seats.
- **Fail:** Entire bottom nav missing (only black + skip pill).

## E — Recommended + overlay

1. On ad seat with recommended rows → tap one row.
2. Article slides **up** over feed.
3. **Dismiss:** swipe **up** OR swipe **right** (back) → same slide-up dismiss.
4. Hardware back also dismisses overlay.

## F — Page 2 load (flip 18+)

1. From card 0, flip to index **17+** without stopping.
2. Within ~1–2 flips of the tail, next server page should append (list grows past ~20 items).
3. **Pass:** Card 19+ is new content, not duplicate of card 8.
4. **Optional:** Over-flip on last card while loading — should not cancel in-flight page (no permanent stuck `isApiRequested`).