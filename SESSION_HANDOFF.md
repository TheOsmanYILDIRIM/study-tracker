# StudyTracker — Session Handoff

Updated: 2026-10-08 (GitHub main)

## Current verified context
- V2 uses Cloudflare KV as canonical storage in `worker/v2/storage.js`; measurement attempts have individual keys and a separate derived list index.
- 2026-10-05 CI optimization is recorded in `AGENTS.md`. Existing `backlog.md` and `log.md` contain historical work; do not mistake their checkboxes for passing current tests.
- The handoff was absent before this session.

## Changes in this session
- `d0cba3a`: reject malformed non-array KV list values with an explicit `TypeError` instead of silently treating them as empty. Missing keys (`null`) still read as empty lists.
- `a173949`: regression in `worker/test-v2.js` checks explicit rejection and preservation of the corrupt stored value.
- No app data or KV production records were modified.

## Verification
- Source and regression test were read/written via GitHub connector.
- CI and full Node suite have **not been executed or confirmed** in this session; do not mark green without a run.
- Run `node worker/test-v2.js`, `node worker/test-v2-staging.js`, `node cli/test-v2.js`, `node cli/test-revision.js` from repo root. Check Actions and Android build status.

## Open risks
1. KV attempt writes and list index updates are not atomic. Concurrent clients may lose index entries; a failed pointer/index write can leave an attempt record undiscoverable.
2. Verify Android-to-Worker sync on different mobile networks (historical 'connection reset' on cellular), including retry and duplicate submissions.
3. Validate upgrade persistence: old student progress, lessons, completed quizzes and attempts must survive catalog updates, APK replacement and offline recovery.

## Next step
Design and test an append-only attempt retrieval/reconciliation strategy under concurrent writes and partial KV failures before changing production write semantics. Prefer narrowly scoped, test-backed improvements. Keep `AGENTS.md` for stable rules and this file for transient state.

## 2026-10-08 — V2 navigation and completion UI
- `d047c13`: V2 lessons now derive each lesson's items from the already collected family item list instead of starting a separate Room collector for every list row; resume uses immediate `scrollToItem` rather than slow animated scrolling.
- `1eeaa9a`: completed V2 learning item cards use muted gray card, type icon, labels and an explicit checkmark. This is presentation-only; attempts are unchanged.
- Verification: GitHub commits confirmed; Android build and real-device latency measurement **not yet run**. Never promise literal 0 ms: asynchronous Room and first-load rendering still take finite time.
- Next: measure tap-to-first-content on device; reuse cached course/lesson/attempt state across navigation, eliminate additional per-card Room subscriptions, and implement short content-aware enter transition if perceptual latency remains.

## 2026-10-08 — Global navigation motion
- `3576e4c`: AppNavGraph NavHost now applies consistent 190–220 ms fade + subtle horizontal slide on push, reversed on back; all routes inherit it, including startup's first destination transition.
- This masks navigation composition changes but does not eliminate actual initial data-fetch latency. No spinner or artificial delay was introduced.
- Android build/device verification pending. Next: check actual launch and blank-state behavior on hardware; use local cached content or lightweight skeleton for cold-load without hiding genuine errors.
