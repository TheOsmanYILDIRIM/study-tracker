#!/usr/bin/env python3
"""Review-gated VIDEO URL replacement for existing StudyTracker Mathematics 9 items.

NEVER adds/removes items, lessons, quizzes, or changes their stable IDs/order.
Only existing math VIDEO items are eligible. Dry-run by default.

Approvals file:
{
  "schema_version": 1, "mode": "replace_existing_only",
  "playlist_id": "PLSYiXUktJiZeqUJyNFUgFHwOUNydbC-II",
  "approvals": [{
    "target_item_id": "item_mat9_vid_uslu_giris",
    "expected_old_url": "https://www.youtube.com/watch?v=FtJE835vtoM",
    "video_id": "mHq3Dz1Kyw4",
    "reviewed": true,
    "reason": "Watched and confirmed this new lecture covers the original subtopic",
    "quiz_reviewed": true,
    "quiz_review_note": "Compared all linked quiz questions with the new lecture"
  }]
}

A linked old transcript-grounded quiz is NOT silently re-grounded to the new
source. It keeps its historical provenance, and a mismatch warning is emitted.
"""
import argparse
import copy
import csv
import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

COURSE = "course_mat_9"
PLAYLIST = "PLSYiXUktJiZeqUJyNFUgFHwOUNydbC-II"
ID_RE = re.compile(r"^[A-Za-z0-9_-]{11}$")
ITEM_RE = re.compile(r"^item_mat9_[A-Za-z0-9_-]+$")
SRC_REF = (
    "https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi/"
    "blob/main/data/video_playlists/" + PLAYLIST + "/playlist-catalog.json"
)


def read(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def write(path, data):
    target = Path(path)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def get_video_id(url):
    match = re.fullmatch(r"https://(?:www\.)?youtube\.com/watch\?v=([A-Za-z0-9_-]{11})", url or "")
    return match.group(1) if match else None


def load_catalog(root):
    base = root / "content/v2"
    course = read(base / "courses" / (COURSE + ".json"))
    if course.get("id") != COURSE or not isinstance(course.get("lessons"), list):
        raise ValueError("StudyTracker math course is unavailable or invalid")
    lessons = {}
    for lesson_id in course["lessons"]:
        lesson = read(base / "lessons" / f"{lesson_id}.json")
        if lesson.get("id") != lesson_id or lesson.get("courseId") != COURSE:
            raise ValueError(f"Broken lesson reference: {lesson_id}")
        lessons[lesson_id] = lesson
    items = {}
    for path in (base / "items").glob("*.json"):
        item = read(path)
        if item.get("id") != path.stem:
            raise ValueError(f"Item file identity mismatch: {path.name}")
        items[item["id"]] = item
    videos = {}
    for lesson_id, lesson in lessons.items():
        for item_id in lesson.get("items", []):
            if item_id not in items:
                raise ValueError(f"Missing item reference: {item_id}")
            item = items[item_id]
            if item.get("courseId") != COURSE or item.get("lessonId") != lesson_id:
                raise ValueError(f"Invalid item parent: {item_id}")
            if item.get("itemType") == "VIDEO":
                videos[item_id] = item
    quizzes = defaultdict(list)
    for item in items.values():
        if item.get("courseId") != COURSE or item.get("itemType") != "QUIZ":
            continue
        prov = (item.get("payload") or {}).get("provenance") or {}
        derived = prov.get("derivedFromItemId")
        if derived in videos:
            quizzes[derived].append(item)
        elif item["id"].endswith("__quiz") and item["id"][:-6] in videos:
            quizzes[item["id"][:-6]].append(item)
    return base, videos, quizzes, items, lessons


def validate_mapping(mapping):
    if (mapping.get("schema_version") != 1 or
            mapping.get("playlist_id") != PLAYLIST or
            mapping.get("course_id") != COURSE):
        raise ValueError("Unexpected playlist, course or mapping schema")
    video_map = {}
    for video in mapping.get("videos") or []:
        vid = str(video.get("video_id") or "")
        if not ID_RE.fullmatch(vid) or vid in video_map:
            raise ValueError(f"Invalid or duplicate playlist video ID: {vid}")
        if video.get("url") != f"https://www.youtube.com/watch?v={vid}":
            raise ValueError(f"Untrusted or inconsistent URL: {vid}")
        video_map[vid] = video
    if not video_map:
        raise ValueError("No source playlist videos")
    return video_map


def review_rows(videos, quizzes, source):
    rows = []
    for item in sorted(videos.values(), key=lambda x: (x["lessonId"], x.get("orderKey", 0), x["id"])):
        available = [
            v for v in source.values()
            if (item["lessonId"] in (v.get("theme_lesson_ids") or []) or
                (v.get("best_match") or {}).get("lesson_id") == item["lessonId"])
        ]
        # Review candidates are not automatically assigned to items.
        available.sort(key=lambda x: (0 if x.get("teacher") == "İlyas Güneş" else 1,
                                      x.get("position") or 999))
        seen = set()
        candidates = []
        for video in available:
            if video["video_id"] in seen:
                continue
            seen.add(video["video_id"])
            candidates.append({
                "video_id": video["video_id"], "title": video.get("title"),
                "url": video["url"], "teacher": video.get("teacher"),
                "position": video.get("position"),
                "transcript_status": video.get("transcript_status"),
                "match_scope": video.get("matching_scope"),
            })
        linked = quizzes.get(item["id"], [])
        rows.append({
            "target_item_id": item["id"],
            "lesson_id": item["lessonId"],
            "existing_card_title": item.get("title"),
            "existing_url": item.get("contentUrl"),
            "existing_teacher": (item.get("payload") or {}).get("teacher")
                                or (item.get("payload") or {}).get("provider"),
            "linked_quizzes": [q["id"] for q in linked],
            "linked_transcript_grounded_quizzes": [
                q["id"] for q in linked
                if ((q.get("payload") or {}).get("provenance") or {}).get("groundingType")
                == "transcript_grounded"],
            "candidate_videos": candidates
        })
    return rows


def write_csv(path, rows):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["target_item_id", "lesson_id", "existing_card_title", "existing_teacher",
                         "expected_old_url", "candidate_video_id", "candidate_title",
                         "candidate_teacher", "candidate_url", "candidate_position",
                         "linked_quiz_count", "needs_manual_review"])
        for row in rows:
            for candidate in row["candidate_videos"] or [None]:
                writer.writerow([
                    row["target_item_id"], row["lesson_id"],
                    row["existing_card_title"], row["existing_teacher"],
                    row["existing_url"],
                    candidate["video_id"] if candidate else "",
                    candidate.get("title", "") if candidate else "",
                    candidate.get("teacher", "") if candidate else "",
                    candidate.get("url", "") if candidate else "",
                    candidate.get("position", "") if candidate else "",
                    len(row["linked_quizzes"]), "YES"
                ])


def prepare(mapping, approvals, root):
    source = validate_mapping(mapping)
    if (approvals.get("schema_version") != 1 or
            approvals.get("mode") != "replace_existing_only" or
            approvals.get("playlist_id") != PLAYLIST or
            not isinstance(approvals.get("approvals"), list)):
        raise ValueError("Expected explicit replace_existing_only approvals")
    base, videos, quizzes, all_items, lessons = load_catalog(root)
    rows = review_rows(videos, quizzes, source)
    operations = []
    warnings = []
    seen_targets = set()
    seen_sources = set()
    known_video_urls = defaultdict(list)
    for item in all_items.values():
        if item.get("itemType") == "VIDEO":
            video_id = get_video_id(item.get("contentUrl"))
            if video_id:
                known_video_urls[video_id].append(item["id"])
    for review in approvals["approvals"]:
        target_id = str(review.get("target_item_id") or "")
        video_id = str(review.get("video_id") or "")
        if (not ITEM_RE.fullmatch(target_id) or target_id not in videos or
                target_id in seen_targets):
            raise ValueError(f"Invalid, duplicate or non-existing Math VIDEO target: {target_id}")
        seen_targets.add(target_id)
        if video_id in seen_sources:
            raise ValueError(f"Replacement video is assigned more than once: {video_id}")
        seen_sources.add(video_id)
        selected = source.get(video_id)
        if not selected:
            raise ValueError(f"Video is not present in the approved playlist: {video_id}")
        item = videos[target_id]
        old_url = item.get("contentUrl")
        new_url = selected["url"]
        expected_url = review.get("expected_old_url")
        if not isinstance(expected_url, str) or not expected_url:
            raise ValueError(f"Expected original URL is mandatory: {target_id}")
        if not review.get("reviewed") is True or not review.get("reviewed_contents") is True:
            raise ValueError(f"Manual viewing and topic review are mandatory: {target_id}")
        reason = str(review.get("reason") or "").strip()
        if len(reason) < 20:
            raise ValueError(f"Specific written lesson evidence is mandatory: {target_id}")
        teacher = selected.get("teacher") or selected.get("channel")
        if teacher != "İlyas Güneş" and review.get("allow_other_teacher") is not True:
            raise ValueError(
                f"Video teacher is {teacher or 'unknown'}, not İlyas Güneş; "
                f"explicit allow_other_teacher needed: {target_id}"
            )
        matched_lessons = set(selected.get("theme_lesson_ids") or [])
        if (selected.get("best_match") or {}).get("lesson_id"):
            matched_lessons.add(selected["best_match"]["lesson_id"])
        if item["lessonId"] not in matched_lessons:
            if review.get("cross_topic_override") is not True:
                raise ValueError(f"Playlist topic does not match target lesson: {target_id}")
            if len(str(review.get("cross_topic_evidence") or "").strip()) < 20:
                raise ValueError(f"Cross-topic override needs written evidence: {target_id}")
        linked_quizzes = quizzes.get(target_id, [])
        if linked_quizzes:
            if review.get("quiz_reviewed") is not True:
                raise ValueError(f"Linked quiz must be manually reviewed: {target_id}")
            if len(str(review.get("quiz_review_note") or "").strip()) < 20:
                raise ValueError(f"Linked quiz review needs written evidence: {target_id}")
        if any(existing_id != target_id for existing_id in known_video_urls.get(video_id, [])):
            raise ValueError(f"Replacement URL is already used by another video item: {video_id}")
        if old_url == new_url:
            operations.append({
                "status": "unchanged", "target_item_id": target_id,
                "video_id": video_id, "old_url": old_url, "new_url": new_url,
                "linked_quizzes": [q["id"] for q in linked_quizzes]
            })
            continue
        if old_url != expected_url:
            raise ValueError(f"Existing video URL changed since review: {target_id}")
        updated = copy.deepcopy(item)
        payload = updated.setdefault("payload", {})
        prov = payload.setdefault("provenance", {})
        old_teacher = payload.get("teacher") or prov.get("teacher") or payload.get("provider")
        history = prov.get("videoReplacementHistory") or []
        if not isinstance(history, list):
            raise ValueError(f"Broken replacement history for {target_id}")
        history = history[-9:] + [{
            "previousUrl": old_url,
            "previousTeacher": old_teacher,
            "previousTranscriptFingerprint": prov.get("transcriptFingerprint"),
            "replacedWithVideoId": video_id,
            "reviewEvidence": reason,
        }]
        for key in ("transcriptPath", "transcriptRawPath", "transcriptFingerprint",
                    "transcriptLanguage", "transcriptKind", "verifiedLectureTitle",
                    "sourceSection", "sourceWeeks", "sourceTheme", "sourceTopic",
                    "videoCandidateTitle", "videoSearchScore"):
            prov.pop(key, None)
        payload.pop("verifiedLectureTitle", None)
        payload["provider"] = selected.get("channel") or teacher or "Unknown"
        payload["teacher"] = teacher
        payload["reviewStatus"] = "verified"
        prov.update({
            "schemaVersion": "v2",
            "reviewStatus": "verified",
            "reviewNote": reason,
            "reviewMethod": "manual_video_topic_and_quiz_review",
            "reviewedOverride": True,
            "reviewedOverrideReason": reason,
            "sourceRef": SRC_REF,
            "sourceVideoUrl": new_url,
            "sourcePlaylistId": PLAYLIST,
            "playlistPosition": selected.get("position"),
            "transcriptStatus": selected.get("transcript_status", "not_attempted"),
            "transcriptReviewRequired": selected.get("transcript_status") != "fetched",
            "videoReplacementHistory": history,
            "fingerprint": hashlib.sha256(
                f'{target_id}|{new_url}|{item["lessonId"]}'.encode("utf-8")
            ).hexdigest()[:16],
        })
        if selected.get("transcript_status") == "fetched" and selected.get("transcript_fingerprint"):
            prov["transcriptFingerprint"] = selected["transcript_fingerprint"]
            prov["transcriptLanguage"] = "tr"
        updated["contentUrl"] = new_url
        # Keep original title, stableKey, courseId, lessonId, orderKey, displayLabel,
        # publishingStatus, and all items/lesson references identical.
        legacy = []
        for q in linked_quizzes:
            qp = (q.get("payload") or {}).get("provenance") or {}
            if qp.get("sourceVideoUrl") and qp["sourceVideoUrl"] != new_url:
                legacy.append({"quiz_id": q["id"], "historical_source_url": qp["sourceVideoUrl"]})
        if legacy:
            warnings.append({
                "target_item_id": target_id, "type": "historical_quiz_provenance",
                "details": legacy,
                "note": "Quiz unchanged; historical transcript provenance refers to its original video. "
                        "Reviewer confirmed topic/quiz compatibility; do not re-label transcript grounding."
            })
        operations.append({
            "status": "replace", "target_item_id": target_id,
            "video_id": video_id, "lesson_id": item["lessonId"],
            "old_url": old_url, "new_url": new_url,
            "old_teacher": old_teacher, "new_teacher": teacher,
            "linked_quizzes": [q["id"] for q in linked_quizzes],
            "new_item": updated
        })
    summary = dict(Counter(op["status"] for op in operations))
    return {
        "mode": "replace_existing_only",
        "playlist_id": PLAYLIST, "total_existing_math_video_cards": len(videos),
        "reviewed_approvals": len(approvals["approvals"]),
        "replace_count": summary.get("replace", 0),
        "unchanged_count": summary.get("unchanged", 0),
        "unreviewed_count": len(videos) - len(approvals["approvals"]),
        "warnings": warnings,
        "operations": operations,
        "review_rows": rows
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--mapping", required=True)
    parser.add_argument("--approvals", required=True)
    parser.add_argument("--report")
    parser.add_argument("--review-csv")
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    result = prepare(read(args.mapping), read(args.approvals), root)
    if args.review_csv:
        write_csv(args.review_csv, result["review_rows"])
    # On dry-run, show the full comparison without writing any sources.
    output = {
        k: v for k, v in result.items()
        if k not in ("review_rows",)
    }
    operations = []
    for o in output["operations"]:
        operations.append({k: v for k, v in o.items() if k != "new_item"})
    output["operations"] = operations
    output["applied"] = args.apply
    if args.apply:
        for op in result["operations"]:
            if op["status"] == "replace":
                path = root / "content/v2/items" / f'{op["target_item_id"]}.json'
                write(path, op["new_item"])
    if args.report:
        write(args.report, output)
    print(json.dumps({k: output[k] for k in (
        "mode", "total_existing_math_video_cards", "reviewed_approvals",
        "replace_count", "unchanged_count", "unreviewed_count", "applied"
    )}, ensure_ascii=False))


if __name__ == "__main__":
    main()
