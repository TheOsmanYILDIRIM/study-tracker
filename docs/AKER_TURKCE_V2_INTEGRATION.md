# Aker Kartal TYT Türkçe 2027 → StudyTracker V2

## Authoritative architecture
- Editable modular sources: `content/v2/catalog.json` → `content/v2/courses/<courseId>.json` → `content/v2/lessons/<lessonId>.json` → `content/v2/items/<itemId>.json`.
- `content/9-sinif-v2-catalog.json` and `dist/studytracker-v2-catalog.json` are generated outputs. Never hand-edit them.
- Commands: `npm run content:validate`, `npm run content:compile`, `npm run content:bundle`.
- Current course: `course_tde_9`, 16 existing lesson refs. Preserve historical attempts, IDs and stableKeys; never overwrite live KV as part of draft work.

## Curriculum and pacing contract
- StudyTracker V2 is topic/subtopic and student-paced; **NOT 18-week or semester based**. Viewing videos alone does not establish completion: verified quizzes/learning checks are needed.
- 71-caption Aker Kartal TYT Türkçe playlist is a proposed teaching resource, not a complete replacement for 9th-grade TDE learning outcomes. The previous 11 topic buckets are provisional. Match actual timestamped transcript excerpts to precise TDE outcome components as direct, partial or unmatched; report gaps and avoid invented video URLs or quizzes.
- Preserve reading, writing, speaking, listening and literary-text objectives with separate activities where the TYT playlist does not cover them.
- Source of curriculum and handoff: `TheOsmanYILDIRIM/lise1-ogrenme-programi` `AGENTS.md`, `SESSION_HANDOFF.md`, `curriculum/yillik_planlar/TDE_Yillik_Plan_AL9.md`, and Aker branch `feature/aker-tyt-turkce-2027`.
- Binary ZIP upload is postponed by user; do not block offline alignment on GitHub upload. Parallel Math 9 agent owns mathematics files.

## Next implementation steps
1. Inspect the 16 current TDE lesson JSONs and all item references, stableKeys and attempt/progress contracts.
2. Review each of 71 source captions at timestamp level, summarize actual subtopics and identify gaps against TDE outcomes.
3. Build isolated, non-destructive modular replacement draft and migration/compatibility checks.
4. Validate, compile and bundle; only merge/publish after evidence and migration tests pass.
