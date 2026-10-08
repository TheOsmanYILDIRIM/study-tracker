# StudyTracker — Simplified V2 handoff
Updated: 2026-10-08
Active target: `feature/student-simple-flow-thumbnails` (NOT `main`)

## Current product truth
- V2 is measurement/curriculum based. Preserve course/lesson/item IDs, append-only attempts, Room progress and KV sync.
- User-facing simplified child flow: direct course → flat topic/item list, one-tap VIDEO opens external player, return-to-finish confirmation, direct QUIZ and Anki launching; cached course covers and video thumbnails.
- `AGENTS.md` on this branch holds the stable UI and content rules.

## This session: wrong-branch correction
- Prior splash, global navigation transition, gray-completed-card and icon work was accidentally applied to `main`; that was *not* this simplified V2 branch. Main was not changed in this correction session.
- Copied the approved **original** SVG book-and-comet icon and vector splash to THIS feature branch: `assets/brand/studytracker-icon.svg`, `ic_launcher_foreground.xml` / background, `StudyLaunchOverlay.kt`, `MainActivity.kt`, native dark launch windows/themes and `launch_colors.xml`. NO Solar Odyssey.
- `NavGraph.kt`: subtle 140–220 ms fade/slide transitions, preserving simplified V2 `autoStart`/`targetItemId` routes and one-tap actions.
- `V2LearningFlowScreen.kt`: completed VIDEO/QUIZ/ANKI cards are gray with muted thumbnails, gray check and explicit completion. Their cached image components, click handling and attempts remain unchanged.
- `V2CoursesScreen.kt`: completed course cards gray with muted subject cover; dynamic course visual colors and cache preserved for ongoing courses.
- `V2LessonsScreen.kt`: completed unit header/check and progress bar gray; flat lesson/item UI preserved.
- `scripts/test-branding.cjs`: guards SVG/Android 23-shape parity, original animation active, V2 one-tap navigation, course/lesson/item graying, no Solar Odyssey.
- `.github/workflows/build-apk.yml`: executes branding/V2 regression script on feature push.

## Verification
- GitHub read-back and 8 static source assertions passed: 23 matched shapes, classic splash, activity connection, V2 one-tap route, cached covers + gray courses, flat lessons + gray header, one-tap video + gray item thumbnails, CI test wiring.
- Latest build, APK installation, Android unit test and visual performance verification NOT yet confirmed. No Termux Gradle build. CI is triggered by pushes to `feature/**`, but do not infer success without actual run logs.
- No data migrations, catalog rewrites, KV production operations or merges were performed.

## Next
1. Inspect feature-branch GitHub Actions build logs/artifact for actual compilation. Address failures without touching `main`.
2. Run real Android cold-start and navigation timing checks. Validate original splash 1.1s, dynamic covers and one-tap video confirmation; ensure async data does not flash a false empty state.
3. Validate completed states and small-screen overflow for course/item cards.
4. Preserve pending independent quiz-audit AGY job; do not create a duplicate while running.
