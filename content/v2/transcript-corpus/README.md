# StudyTracker Transcript Source Corpus (v2)

This directory contains the recovered transcript-first source corpus for StudyTracker curriculum subjects.

## Source Extraction

Subtitles were downloaded using subtitle-only `yt-dlp` without downloading media files:

```bash
yt-dlp --skip-download --write-subs --write-auto-subs --sub-langs "tr" -P "/sdcard/Download/YTDLnis" -o "%(playlist_title)s/%(playlist_index)s - %(title)s.%(ext)s" --sleep-interval 1 "PLAYLIST_URL"
```

## Structure

```
content/v2/transcript-corpus/
├── manifest.json
├── README.md
├── almanca/
│   ├── raw/   # Verbatim .vtt subtitles
│   └── clean/ # Cleaned text transcripts
├── biyoloji/
├── cografya/
├── fizik/
├── ingilizce/
├── kimya/
├── matematik/
└── tde/
```

## Cleaning Filter

The raw `.vtt` subtitles are filtered into plain text `.txt` via a lightweight streaming filter:
- Strips `WEBVTT` headers, cue timestamps (`-->`), cue indices, metadata tags, and inline formatting tags (`<c>`, `<b>`, timestamp cues).
- Unescapes HTML entities (`&amp;`, `&quot;`, `&#39;`, `&lt;`, `&gt;`).
- Collapses whitespace and suppresses immediately consecutive duplicate cue lines.
- Preserves complete spoken sentences in chronological order.

## Semantic Mapping Notice

> **IMPORTANT:** Semantic mapping to syllabus topics / curriculum nodes is **intentionally excluded** from this corpus recovery step. This directory serves strictly as an immutable, verified raw and clean transcript data layer.

## Corpus Summary

- **Total Video Transcripts:** 319
- **Total Raw Bytes:** 58,835,226 bytes (56.11 MB)
- **Total Clean Bytes:** 8,954,201 bytes (8.54 MB)

| Subject | Files | Raw Size | Clean Size |
|---|---|---|---|
| `fizik` | 117 | 30,822,368 B | 3,836,167 B |
| `cografya` | 41 | 12,939,071 B | 1,614,520 B |
| `biyoloji` | 60 | 1,708,229 B | 823,211 B |
| `kimya` | 34 | 2,041,873 B | 1,300,439 B |
| `tde` | 33 | 4,956,675 B | 619,428 B |
| `almanca` | 12 | 1,293,759 B | 153,866 B |
| `ingilizce` | 9 | 871,122 B | 103,433 B |
| `matematik` | 13 | 4,202,129 B | 503,137 B |
