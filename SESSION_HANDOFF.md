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

## 2026-10-08 — User-supplied 60/60 Mathematics 9 Turkish VTTs
- User supplied `9.SINIF VİDEO DERS KİTABI KONU ANLATIM.zip`; 60 Turkish VTT files for 60 locked playlist positions, verified offline. Extracted 64,720 timecoded cues / 3,566,940 normalized characters. No missing caption files. Automatic captions may misrecognize math notation.
- LiseDers repo now has `data/video_playlists/PLSYiXUktJiZeqUJyNFUgFHwOUNydbC-II/transcript_evidence.json` with verified coverage and theme signal counts; `scripts/import_ytdlnis_subtitles.py` now supports opt-in complete-index archive matching, guarded by 60/60 unique position validation. Full normalized transcripts are not public Git source; retained in the conversation output ZIP.
- New canonical implementation plan: `docs/MATH9_TRANSCRIPT_REBUILD_PLAN.md`. 16 course subgroups / 8 broad themes / 60 fixed IDs. New lesson/item identities must not overwrite historical progress. No live V2 course/KV edits or quiz generation performed in this phase.
- Next: timestamp-level learning-outcome mapping, quiz evidence validation, versioned draft V2 catalog, offline/KV progress migration checks, only then publish.

## 2026-10-08 — Math9 draft implementation started
- User explicitly asked to start implementation. Added `scripts/build-math9-transcript-draft.py`: deterministic, isolated 60-video/16-lesson draft generator consuming locked LiseDers playlist, `content/math9/playlist_course_plan.json`, and user-supplied transcript ZIP with exact SHA-256. Produces draft VIDEO items, per-video transcript cue count/fingerprint and timestamp review manifest; **no fabricated quiz**, no edits to `content/v2` or live KV.
- Added `scripts/test-math9-transcript-draft.py` and `.github/workflows/math9-transcript-draft.yml` for synthetic ZIP end-to-end and V2 compiler regression checks. First Action run `37839516573` was in progress at handoff; verify result and fix if necessary.
- Added `content/math9/review_nicelikler_transcript_signals.json`: preliminary term-frequency evidence for videos 12–25; needs timestamp-level MEB validation, not publishable quiz evidence.
- **Blocker:** complete ZIP is present in conversation at `math9_normalized_transcripts.zip`, not yet remotely uploaded to LiseDers GitHub. Remote validation workflow exists. Do not claim real draft generated until the ZIP is available in CI and build has passed.
- Next: verify CI, upload and verify ZIP, run real 60-video draft build, review topic/timestamp outcomes, then prepare versioned course swap with historical progress protection.

## 2026-10-08 — 60-video manual transcript sample review
- User requested per-video human summary and curriculum alignment. Created `content/math9/manual_transcript_review_60.json` with **60 distinct video summaries**, candidate curriculum topic labels and video URLs. Review method: personally inspected 5 distributed timecoded transcript excerpts per video plus topical term signals; did **not** watch the actual video visuals or exhaustively read all 64,720 cues. Every item is `transcript_sampled_needs_full_validation`, not a certified learning-outcome mapping.
- Concrete findings: #12–20 mostly linear functions; #21 introduces absolute value graph; #22–23 linear transformation practice; #24 function inequalities/intervals; #25 absolute-value inequalities. #39–45 algorithms including graphs/flowcharts; #46–54 statistics; #55–59 probability; #60 mixed revision.
- Curriculum gaps: 60-video playlist does not clearly cover all preexisting Math 9 modules (e.g. real-number interval/set operations, general operation properties) as standalone lessons. Do not remove these outcomes without an explicit gap-fill source.
- Next: cross-check exact MEB kazanım text and codes, read targeted timestamp segments for ambiguous lessons, mark fully reviewed rows, then draft validated micro quizzes. Existing live V2/KV remains unchanged.

## 2026-10-08 — timestamped outcome evidence and quiz draft pass
- Produced `math9_timestamped_curriculum_review.json` as a conversation artifact from the actual 60-video ZIP: 60/60 video IDs, 229 timestamped evidence excerpts; each retains a provisional outcome and `candidate_needs_full_outcome_validation`. It has **not** been committed to GitHub yet. Official subcodes outside 9.1.1/9.2.1–9.2.3 are intentionally marked unverified rather than invented.
- Added `content/math9/quiz_drafts_8.json`: eight original, four-choice, arithmetically checked **draft** concept questions tied to video ID and timestamp; not claimed to be transcribed verbatim or visually verified. No live V2 item/quiz changes.
- Next: verify official MEB exact outcome text for 9.3–9.6, visually validate math symbols against videos, import timestamp review artifact to repo, expand evidence-based quizzes, then validate V2 staging catalog. Existing progress must remain unchanged.

## 2026-10-08 — Actual Mathematics 9 video link replacement
- User explicitly requested replacing video URLs in the **existing StudyTracker Math 9 course**, not rebuilding cards. Completed **24 of 29** current Math 9 VIDEO item source URL changes in `content/v2/items/`; retained IDs, stableKeys, lesson links, order and quiz files. Updated teacher/channel and playlist provenance; old URLs preserved as `previousSourceVideoUrl`, old transcript path/fingerprint removed from changed VIDEO provenance.
- Five items retained old sources due to no reliable direct coverage in playlist: `aralik_farki`, `araliklar_gosterim`, `cebirsel_ispat`, `sayi_kumeleri`, `topic_04_gercek_sayilarin_islem_ozelliklerini_cebirsel_ifade_`.
- Two pairs legitimately share video sources: geometric transformations (already explicit sharedSource) and Pisagor source used for two theorem cards (new explicit sharedSource). **Tales/Öklid coverage is incomplete** and must be filled separately.
- Linked existing quizzes remain unchanged with their historical source URL/fingerprint, while parent VIDEO provenance is flagged `quizRegroundingStatus: pending`. CLI regression explicitly checks that pending quizzes do not falsely claim grounding in replacement videos. These quizzes still require educational revalidation.
- New `.github/workflows/validate-math9-video-swap.yml` passed [run #37841377989](https://github.com/TheOsmanYILDIRIM/study-tracker/actions/runs/37841377989): 24 replacements/5 retained, modular V2 compilation, CLI checks, and regenerated `content/9-sinif-v2-catalog.json` committed as `a3cb6480eb`.
- **Not yet published to Cloudflare KV / live student devices.** Next: review quiz compatibility and uncovered outcomes, then perform safe catalog diff/apply with device verification. Do not reset student attempts or progress.
