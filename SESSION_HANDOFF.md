# StudyTracker — Current Session Handoff
Updated: 2026-10-08 | Canonical development target: **main**

## Product state
- StudyTracker V2 (Cloudflare KV backend, offline Room) with append-only student attempts and immutable course/lesson/item identifiers; do not reset progress or silently overwrite historical measurements.
- Simplified student flow: course cards with content-defined cover/theme, flat unit headings followed by VIDEO/QUIZ/ANKI cards, cached YouTube thumbnails, one-tap video open and return-to-finish prompt, direct quiz/Anki. `V2CoursesScreen`, `V2LessonsScreen`, `V2LearningFlowScreen` are current entrypoints.
- Completed item/course cards are gray but stay tappable. **Topic header/indicator fades only if `lessonProgress.itemsProgress.isNotEmpty()` and every state is `V2ItemState.COMPLETED`**, not from percentage rounding. Any unfinished or locked item keeps header vivid.
- App launcher and cold-start use hand-coded SVG book/comet, matching Android VectorDrawable + short Compose intro. Navigation fades/slides ~140–220 ms. Solar Odyssey discarded.

## Integration/verification
- **Merged into main:** PR #7, merge commit `f13180d59a57885d0dc62084752ca4904c07b28b`. Original diverged PR #6 was closed without merge. The new integration branch started from main `c74276b` and ported UI/domain/catalog updates selectively, preserving main-only content work.
- Main's KV corruption guards, worker tests, reviewed Math 9 playlist import tools, existing video ID locks, CI workflow, and docs remain unchanged. No production KV/deployment mutation.
- Large `content/9-sinif-v2-catalog.json` (>1 MB) cannot be read as text via the normal GitHub contents connector. **Always use `fetch_blob` by verified blob SHA, never treat an empty response as an empty file.** Integration V2 catalog restored exactly to original feature blob `33a6eb1f2652416b214dd79d98e1d437f2480695`.
- Source read-back tests confirm V2 one-tap routes, completed card gray style, icon parity, and topic strict completion predicate. Full Android CI, actual install and device-performance testing remain unverified until real workflow results are retrieved.
- GitHub Actions build is triggered on feature/main pushes and runs `node scripts/test-branding.cjs`, Android unit tests, worker/CLI tests, V2 content bundle and `./gradlew assembleRelease`.

## Independent Math 9 content work (preserved from main)
- Existing VIDEO cards only; replace source URLs with review-gated `scripts/replace-liseders-math-videos.py` (never auto-create lesson/item or rewrite quiz sources). Review approvals initially empty. Preserve IDs and student history.
- Previous verified Action #37826512790: 11 tests passed in replacement-only rehearsal; production V2 KV was NOT updated. Playlist matching is candidate data, not transcript/provenance proof.

## Next
1. Merge and main source verification completed. Old PR #6 closed. All future StudyTracker work should branch from or update main, not the old V2 feature branch.
2. Verify current GitHub Actions APK release build and regression tests; diagnose issues before claiming validated success.
3. Device-check cold-load flicker, 320dp screen and completed/not-completed topic states; check mobile-network sync and KV attempt index concurrency.
