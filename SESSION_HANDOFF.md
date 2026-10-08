# StudyTracker — Session Handoff

Updated: 2026-10-08 — main branch

## Product and architecture
- V2 learning/measurement flow is the default Android navigation. Room is the local source of truth; Cloudflare KV is canonical remote storage. Attempts are append-only and catalog updates must preserve progress.
- Android has separate child/parent flavors, with the same launch branding.
- Stable engineering policies belong in `AGENTS.md`. Do not treat historical backlog checkmarks as current test evidence.

## Latest work
- `d0cba3a`, `a173949`: malformed KV list values are rejected instead of silently interpreted as empty; regression test added.
- `d047c13`: remove per-row Room flow subscriptions from V2 lessons screen and replace slow animated resume scroll with immediate positioning.
- `1eeaa9a`: completed learning cards are muted gray, with explicit checked status.
- `3576e4c`: global 190–220 ms fade/slide navigation transitions.
- **Branding / launch (this session):**
  - New hand-authored SVG: `assets/brand/studytracker-icon.svg` (night sky, open book, comet, golden star).
  - Android VectorDrawable `app/src/main/res/drawable/ic_launcher_foreground.xml` uses the *same 23 path geometries*; matching launcher background updated. Existing adaptive-icon mipmap files reference the foreground/background.
  - Native launch window on API 26–30 and Android 12+ system splash set to display the vector on a dark background; no rasterization.
  - `feature/launch/StudyLaunchOverlay.kt`: brief (~1.1 s) book-icon pop/tilt, orbiting comet, stars, and sparkle animation; fades out over 260 ms while real NavHost composes underneath. Brand SVG is not loaded from the network.
  - Notification permission request deferred until after launch to avoid interrupting the reveal; deep-link import consent skips introductory animation.
  - `scripts/test-branding.cjs` validates SVG/native path equality and splash wiring; `build-apk.yml` runs it before Android tests/build.

## Verification
- GitHub read-after-write confirmed vector resources, Compose overlay, MainActivity connection, and CI test hook.
- Connector-side verification passed: SVG and VectorDrawable each have **23 geometrically identical paths**, native/Compose splash references exist.
- Android Gradle build, physical-device animation, launch-time profiling, and the new Node test *have not been observed to complete*. GitHub combined status returned no check contexts and commit workflow lookup returned no PR runs; do not claim CI success without the actual run result.
- Expected CI: `node scripts/test-branding.cjs`, `./gradlew test`, `./gradlew assembleRelease` using GitHub Actions (never local Termux Gradle).

## Next actions / known risks
1. Confirm GitHub Actions build result and APK artifacts; fix compile/resource problems if any, then review actual launch on device (API 26 and API 31+).
2. Check cold launch and resume behavior: avoid genuine empty-state flicker if Room data arrives after the ~1.1 s intro; prefer local cache/skeleton over artificial waits.
3. Stress-test KV attempt concurrency/partial failures and mobile-network reconnect for progress safety. Do not overwrite student history.
4. Keep brand icon SVG and Android VectorDrawable path shapes synchronized whenever editing; the CI validator enforces this.
