#!/usr/bin/env python3
"""Build an isolated, reproducible Math 9 V2 draft from the 60-video playlist.

Does not modify content/v2 or live student data. Never fabricates quizzes.
A complete, verified subtitle ZIP is required; its provenance is retained
as hashes and timestamps, not raw transcript content in the public catalog.
"""
import argparse
import hashlib
import json
import re
import shutil
import zipfile
from collections import Counter
from pathlib import Path

VIDEO_ID = re.compile(r"^[A-Za-z0-9_-]{11}$")
COURSE_ID = "course_mat_9"
EXPECTED_ZIP_SHA = "1c45d1b1e8d4caec312f117dcba4a6bc53b69aa5047d062a42a9c746f830f1d9"


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def write(path, value):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def build(plan, catalog, archive_path, output):
    if plan.get("course_id") != COURSE_ID:
        raise ValueError("Wrong course")
    if catalog.get("playlist_id") != plan.get("source_playlist_id"):
        raise ValueError("Playlist ID mismatch")
    videos = catalog.get("videos", [])
    if len(videos) != 60 or len(plan.get("expected_video_ids", [])) != 60:
        raise ValueError("Expected exactly 60 videos")
    ids = []
    for index, v in enumerate(videos, 1):
        vid = v.get("id", "")
        if not VIDEO_ID.fullmatch(vid) or v.get("position") != index:
            raise ValueError(f"Invalid video at position {index}")
        if not v.get("title") or not v.get("url") == f"https://www.youtube.com/watch?v={vid}":
            raise ValueError(f"Invalid video metadata at position {index}")
        ids.append(vid)
    if ids != plan["expected_video_ids"] or len(set(ids)) != 60:
        raise ValueError("Source playlist IDs changed")
    groups = plan.get("groups", [])
    positions = [n for g in groups for n in range(g["first_position"], g["last_position"] + 1)]
    if sorted(positions) != list(range(1, 61)):
        raise ValueError("Plan groups must partition 1..60 exactly once")
    raw = Path(archive_path).read_bytes()
    sha = hashlib.sha256(raw).hexdigest()
    if sha != EXPECTED_ZIP_SHA:
        raise ValueError("Unreviewed transcript ZIP SHA-256")
    cues_by_id = {}
    with zipfile.ZipFile(archive_path) as z:
        if z.testzip():
            raise ValueError("ZIP CRC failed")
        for vid in ids:
            name = f"transcripts/{vid}.tr.cues.json"
            if name not in z.namelist():
                raise ValueError(f"Missing transcript {vid}")
            parsed = json.loads(z.read(name))
            cues = parsed.get("cues")
            if parsed.get("id") != vid or not isinstance(cues, list) or not cues:
                raise ValueError(f"Invalid transcript {vid}")
            if not all(isinstance(x.get("start"), str) and isinstance(x.get("text"), str) for x in cues):
                raise ValueError(f"Invalid cue schema {vid}")
            cues_by_id[vid] = cues
    output = Path(output)
    if output.exists() and any(output.iterdir()):
        raise ValueError("Output must be a new or empty directory; existing content is protected")
    lessons = []
    items = []
    reviews = []
    for n, group in enumerate(groups, 1):
        lesson_id = f"lesson_mat9_2026_v2_{group['id']}"
        if not re.fullmatch(r"lesson_mat9_2026_v2_[a-z0-9_]+", lesson_id):
            raise ValueError("Invalid group identifier")
        item_ids = []
        for position in range(group["first_position"], group["last_position"] + 1):
            video = videos[position - 1]
            vid = video["id"]
            item_id = f"item_mat9_2026_v2_video_{position:02d}"
            item_ids.append(item_id)
            cues = cues_by_id[vid]
            transcript = "\n".join(x["text"] for x in cues)
            fingerprint = hashlib.sha256(transcript.encode("utf-8")).hexdigest()[:16]
            item = {
                "id": item_id, "courseId": COURSE_ID, "lessonId": lesson_id,
                "stableKey": item_id.removeprefix("item_"),
                "itemType": "VIDEO", "displayLabel": f"{position:02d}",
                "orderKey": position * 1000, "title": video["title"],
                "contentUrl": video["url"], "publishingStatus": "draft",
                "payload": {
                    "provider": video.get("channel") or "YouTube",
                    "teacher": video.get("channel"),
                    "topic": group["title"],
                    "provenance": {
                        "schemaVersion": "v2", "reviewStatus": "needs_review",
                        "sourceVideoUrl": video["url"],
                        "sourcePlaylistId": catalog["playlist_id"],
                        "playlistPosition": position,
                        "transcriptLanguage": "tr",
                        "transcriptKind": "user_supplied_ytdlnis_caption",
                        "transcriptFingerprint": fingerprint,
                        "transcriptCueCount": len(cues),
                        "transcriptFirstTimestamp": cues[0]["start"],
                        "transcriptLastTimestamp": cues[-1]["start"],
                        "transcriptArchiveSha256": sha,
                        "curriculumAlignment": "pending_timestamp_level_review",
                        "fingerprint": hashlib.sha256((item_id + "|" + video["url"] + "|" + fingerprint).encode()).hexdigest()[:16]
                    }
                }
            }
            items.append(item)
            reviews.append({
                "video_id": vid, "position": position,
                "lesson_id": lesson_id, "item_id": item_id,
                "topic": group["title"], "title": video["title"],
                "cue_count": len(cues), "first_timestamp": cues[0]["start"],
                "last_timestamp": cues[-1]["start"],
                "transcript_fingerprint": fingerprint,
                "curriculum_outcome": None, "timestamp_ranges": [],
                "status": "needs_review",
                "quiz_status": "not_generated"
            })
        lessons.append({
            "id": lesson_id, "courseId": COURSE_ID,
            "stableKey": lesson_id, "title": group["title"],
            "orderKey": n * 1000, "items": item_ids,
            "sourceMeta": {"theme": group["theme"], "coverage": group["coverage"],
                           "playlistPositions": [group["first_position"], group["last_position"]],
                           "curriculumReviewStatus": "pending"}
        })
    course = {
        "id": COURSE_ID, "title": "9. Sınıf Matematik",
        "subject": "Matematik", "gradeLevel": 9,
        "orderKey": 1000,
        "description": "2026-2027 MEB Matematik — transkript kanıtlı yeni taslak (yayınlanmamış)",
        "lessons": [x["id"] for x in lessons],
        "contentVersion": plan["content_version"],
        "publishingStatus": "draft"
    }
    for lesson in lessons:
        write(output / "lessons" / (lesson["id"] + ".json"), lesson)
    for item in items:
        write(output / "items" / (item["id"] + ".json"), item)
    write(output / "courses" / "course_mat_9.json", course)
    write(output / "review" / "timestamp_curriculum_review.json", {
        "schema_version": 1, "course_id": COURSE_ID,
        "transcript_archive_sha256": sha, "videos": reviews
    })
    manifest = {
        "schema_version": 1, "course_id": COURSE_ID,
        "version": plan["content_version"], "status": "draft_not_published",
        "video_count": len(items), "lesson_count": len(lessons),
        "quiz_count": 0, "needs_review_count": len(reviews),
        "archive_sha256": sha,
        "existing_student_progress_modified": False
    }
    write(output / "build_manifest.json", manifest)
    return manifest


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--plan", required=True)
    p.add_argument("--catalog", required=True)
    p.add_argument("--transcripts-zip", required=True)
    p.add_argument("--output", required=True)
    args = p.parse_args()
    result = build(load(args.plan), load(args.catalog), args.transcripts_zip, args.output)
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
