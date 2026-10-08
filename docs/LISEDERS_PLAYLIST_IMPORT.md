# LiseDers playlist import

The upstream [LiseDers Actions workflow](https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi/actions/workflows/youtube-playlist.yml) produces `studytracker-mapping.json` after playlist discovery, Turkish subtitle attempts, and curriculum matching. Mapping scores are only *proposals*.

Edit `content/v2/playlist-imports/ilyas-gunes-mat9.approvals.json` with manually reviewed `{"video_id":"...","lesson_id":"...","reviewed":true,"reason":"source evidence","publish":false}`. To select a nonprimary valid lesson, set `override: true` and explain the choice. Avoid duplicate source URLs.

From this repo:
```bash
python scripts/import-liseders-playlist.py --mapping /path/to/studytracker-mapping.json --approvals content/v2/playlist-imports/ilyas-gunes-mat9.approvals.json
python scripts/import-liseders-playlist.py --mapping /path/to/studytracker-mapping.json --approvals content/v2/playlist-imports/ilyas-gunes-mat9.approvals.json --apply
node scripts/compile-v2-catalog.cjs
node scripts/generate-video-audit.cjs
```

The importer adds only new deterministic VIDEO items; previous VIDEO/QUIZ/ANKI identities, URLs and student progress are unchanged. New cards are draft unless explicitly published. It does not fabricate missing transcripts, link to ephemeral transcript paths, or silently regenerate old quizzes. Review diffs and rerun the normal StudyTracker tests before deploying the catalog.
