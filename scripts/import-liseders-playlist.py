#!/usr/bin/env python3
"""Safely import reviewed LiseDers video-to-lesson matches into V2 modular content.

Dry-run by default. --apply requires an explicit reviewed approvals JSON. New
stable VIDEO item IDs never replace existing IDs, URLs, quiz items or progress.
New items stay draft unless an approver explicitly sets publish=true.
"""
import argparse
import collections
import hashlib
import json
import re
from pathlib import Path

ID_RE = re.compile(r"^[A-Za-z0-9_-]{11}$")

def read(path):
    return json.loads(path.read_text(encoding="utf-8"))

def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

def prepare(mapping, approvals, root):
    if mapping.get("schema_version") != 1 or mapping.get("course_id") != "course_mat_9":
        raise ValueError("Unsupported mapping schema or course")
    course = read(root / "content/v2/courses/course_mat_9.json")
    if course.get("id") != "course_mat_9":
        raise ValueError("Missing math course")
    allowed_lessons = set(course["lessons"])
    videos = mapping.get("videos") or []
    by_video = {}
    for video in videos:
        v_id = str(video.get("video_id", ""))
        if not ID_RE.fullmatch(v_id) or v_id in by_video:
            raise ValueError(f"Invalid/duplicate video id: {v_id}")
        expected = "https://www.youtube.com/watch?v=" + v_id
        if video.get("url") != expected:
            raise ValueError(f"Unsafe URL mismatch for {v_id}")
        by_video[v_id] = video
    existing = {}
    item_dir = root / "content/v2/items"
    for file in item_dir.glob("*.json"):
        item = read(file)
        url = item.get("contentUrl")
        if item.get("itemType") == "VIDEO" and url:
            match = re.search(r"(?:youtube\.com/watch\?v=|youtu\.be/)([\w-]{11})", url)
            if match:
                existing.setdefault(match.group(1), []).append(item["id"])
    lesson_data = {}
    actions = []
    seen = set()
    for approval in approvals.get("approvals", []):
        v_id = str(approval.get("video_id") or "")
        lesson_id = str(approval.get("lesson_id") or "")
        if v_id in seen:
            raise ValueError(f"Duplicate approval: {v_id}")
        seen.add(v_id)
        video = by_video.get(v_id)
        if not video:
            raise ValueError(f"Approval references missing playlist video: {v_id}")
        if lesson_id not in allowed_lessons:
            raise ValueError(f"Invalid mathematics lesson ID: {lesson_id}")
        if not approval.get("reviewed") or not str(approval.get("reason") or "").strip():
            raise ValueError(f"Approval requires reviewed=true and written reason: {v_id}")
        if video["review_status"] == "already_present" or v_id in existing:
            actions.append({"video_id": v_id, "status": "existing", "item_ids": existing.get(v_id, [])})
            continue
        lesson_file = root / "content/v2/lessons" / f"{lesson_id}.json"
        lesson = lesson_data.setdefault(lesson_id, read(lesson_file))
        if lesson.get("courseId") != "course_mat_9" or lesson.get("id") != lesson_id:
            raise ValueError("Lesson parent mismatch")
        best = video.get("best_match") or {}
        if best.get("lesson_id") != lesson_id and not approval.get("override"):
            raise ValueError(f"Non-primary match requires override=true: {v_id}")
        item_id = "item_mat9_ig_" + hashlib.sha256(v_id.encode()).hexdigest()[:12]
        item_file = item_dir / f"{item_id}.json"
        if item_file.exists():
            raise ValueError(f"Item ID collision: {item_id}")
        if any(item_id in read(root / "content/v2/lessons" / (p + ".json")).get("items", [])
               for p in allowed_lessons):
            raise ValueError(f"Item ref collision: {item_id}")
        max_order = max(
            (read(item_dir / f"{i}.json").get("orderKey", 0)
             for i in lesson["items"]), default=0)
        order_key = max_order + 100
        # Reserve slots when several new videos belong to one lesson.
        planned = [a for a in actions if a.get("lesson_id") == lesson_id and a.get("status") == "add"]
        if planned:
            order_key = max(order_key, max(a["item"]["orderKey"] for a in planned) + 100)
        item = {
            "id": item_id, "courseId": "course_mat_9", "lessonId": lesson_id,
            "stableKey": item_id.removeprefix("item_"),
            "itemType": "VIDEO", "displayLabel": f"İG {video['position']}",
            "orderKey": order_key, "title": video["title"],
            "contentUrl": video["url"],
            "publishingStatus": "active" if approval.get("publish") is True else "draft",
            "payload": {
                "provider": "İlyas Güneş", "teacher": "İlyas Güneş", "topic": lesson["title"],
                "provenance": {
                    "schemaVersion": "v2", "reviewStatus": "verified",
                    "sourceRef": "https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi",
                    "sourceVideoUrl": video["url"], "playlistId": mapping.get("playlist_id"),
                    "playlistPosition": video["position"], "transcriptStatus": video.get("transcript_status"),
                    "transcriptFingerprint": video.get("transcript_fingerprint"),
                    "reviewedOverride": bool(approval.get("override")),
                    "reviewedOverrideReason": approval["reason"] if approval.get("override") else None,
                    "reviewNote": approval["reason"],
                    "fingerprint": hashlib.sha256(
                        (item_id + "|" + video["url"] + "|" + lesson_id).encode()
                    ).hexdigest()[:16]
                }
            }
        }
        actions.append({"video_id": v_id, "status": "add", "lesson_id": lesson_id,
                        "item": item})
    return actions, lesson_data

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--mapping", required=True)
    p.add_argument("--approvals", required=True)
    p.add_argument("--root", default=".")
    p.add_argument("--report")
    p.add_argument("--apply", action="store_true")
    args = p.parse_args()
    root = Path(args.root).resolve()
    mapping = read(Path(args.mapping))
    approvals = read(Path(args.approvals))
    if not isinstance(approvals.get("approvals"), list):
        raise ValueError("Approvals must contain an approvals array")
    actions, lessons = prepare(mapping, approvals, root)
    if args.apply:
        for a in actions:
            if a["status"] != "add":
                continue
            lesson = lessons[a["lesson_id"]]
            lesson["items"].append(a["item"]["id"])
            write(root / "content/v2/items" / (a["item"]["id"] + ".json"), a["item"])
        for lesson_id, lesson in lessons.items():
            if any(a["status"] == "add" and a["lesson_id"] == lesson_id for a in actions):
                write(root / "content/v2/lessons" / (lesson_id + ".json"), lesson)
    report = {"applied": args.apply, "total_playlist_videos": len(mapping["videos"]),
              "approvals": len(approvals["approvals"]),
              "added": sum(a["status"] == "add" for a in actions),
              "existing": sum(a["status"] == "existing" for a in actions),
              "drafts": sum(a["status"] == "add" and a["item"]["publishingStatus"] == "draft"
                            for a in actions),
              "actions": [{"video_id": a["video_id"], "status": a["status"],
                           "lesson_id": a.get("lesson_id"),
                           "item_id": a.get("item", {}).get("id")} for a in actions]}
    if args.report:
        write(Path(args.report), report)
    print(json.dumps(report, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()
