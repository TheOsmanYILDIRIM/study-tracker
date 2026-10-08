#!/usr/bin/env python3
"""Tests for replacement-only math video import; uses synthetic V2 fixtures."""
import copy
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("replace_math", HERE / "replace-liseders-math-videos.py")
replacement = importlib.util.module_from_spec(spec)
spec.loader.exec_module(replacement)


class VideoReplacementTests(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        self.addCleanup(self.folder.cleanup)
        self.root = Path(self.folder.name)
        self.v2 = self.root / "content/v2"
        for name in ("courses", "lessons", "items"):
            (self.v2 / name).mkdir(parents=True)
        self.video_id = "item_mat9_vid_uslu_giris"
        self.old_url = "https://www.youtube.com/watch?v=FtJE835vtoM"
        self.new_id = "mHq3Dz1Kyw4"
        self.new_url = "https://www.youtube.com/watch?v=" + self.new_id
        self.lesson_id = "lesson_mat9_sayilar_uslu_koklu"
        self.quiz_id = self.video_id + "__quiz"
        self.save("courses/course_mat_9", {
            "id": "course_mat_9", "lessons": [self.lesson_id]
        })
        self.save("lessons/" + self.lesson_id, {
            "id": self.lesson_id, "courseId": "course_mat_9",
            "items": [self.video_id, self.quiz_id]
        })
        self.original_video = {
            "id": self.video_id, "courseId": "course_mat_9",
            "lessonId": self.lesson_id, "itemType": "VIDEO",
            "stableKey": "mat9_vid_uslu_giris", "orderKey": 1000,
            "title": "Üslü Sayılara Giriş", "displayLabel": "1.1",
            "publishingStatus": "active", "contentUrl": self.old_url,
            "payload": {
                "provider": "Rehber Matematik", "teacher": "Mehmet Hoca",
                "provenance": {
                    "schemaVersion": "v2", "fingerprint": "abcd1234",
                    "sourceVideoUrl": self.old_url, "transcriptPath": "old.tr.txt",
                    "transcriptFingerprint": "oldtranscript123",
                    "verifiedLectureTitle": "Old verified lecture"
                }
            }
        }
        self.save("items/" + self.video_id, self.original_video)
        self.original_quiz = {
            "id": self.quiz_id, "courseId": "course_mat_9",
            "lessonId": self.lesson_id, "itemType": "QUIZ",
            "payload": {"provenance": {
                "derivedFromItemId": self.video_id, "groundingType": "transcript_grounded",
                "sourceVideoUrl": self.old_url, "transcriptFingerprint": "oldtranscript123"
            }}
        }
        self.save("items/" + self.quiz_id, self.original_quiz)
        self.mapping = {
            "schema_version": 1, "playlist_id": replacement.PLAYLIST,
            "course_id": replacement.COURSE, "videos": [{
                "video_id": self.new_id, "position": 1,
                "title": "1.Ders Üslü Sayılar - İlyas Güneş",
                "url": self.new_url, "teacher": "İlyas Güneş",
                "channel": "İlyas Güneş",
                "theme_lesson_ids": [self.lesson_id],
                "best_match": {"lesson_id": self.lesson_id},
                "transcript_status": "blocked"
            }]
        }
        self.approval = {
            "target_item_id": self.video_id, "video_id": self.new_id,
            "expected_old_url": self.old_url, "reviewed": True,
            "reviewed_contents": True,
            "reason": "Watched this lecture and checked every covered topic against the lesson",
            "quiz_reviewed": True,
            "quiz_review_note": "Read all quiz questions and confirmed they cover the new lesson content"
        }
        self.approvals = {
            "schema_version": 1, "mode": "replace_existing_only",
            "playlist_id": replacement.PLAYLIST, "approvals": [self.approval]
        }

    def save(self, relative, data):
        path = self.v2 / (relative + ".json")
        path.write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")

    def test_review_matrix_lists_old_video_and_quiz(self):
        r = replacement.prepare(self.mapping, {
            **self.approvals, "approvals": []
        }, self.root)
        self.assertEqual(r["total_existing_math_video_cards"], 1)
        self.assertEqual(r["replace_count"], 0)
        self.assertEqual(r["review_rows"][0]["existing_url"], self.old_url)
        self.assertEqual(r["review_rows"][0]["linked_quizzes"], [self.quiz_id])
        self.assertEqual(r["review_rows"][0]["candidate_videos"][0]["video_id"], self.new_id)

    def test_replacement_preserves_every_identity_and_old_quiz(self):
        before_quiz = (self.v2 / "items" / (self.quiz_id + ".json")).read_bytes()
        before_lesson = (self.v2 / "lessons" / (self.lesson_id + ".json")).read_bytes()
        result = replacement.prepare(self.mapping, self.approvals, self.root)
        self.assertEqual(result["replace_count"], 1)
        self.assertEqual(result["operations"][0]["linked_quizzes"], [self.quiz_id])
        new_item = result["operations"][0]["new_item"]
        for key in ("id", "stableKey", "lessonId", "courseId", "orderKey",
                    "displayLabel", "publishingStatus", "title"):
            self.assertEqual(new_item[key], self.original_video[key])
        self.assertEqual(new_item["contentUrl"], self.new_url)
        self.assertEqual(new_item["payload"]["teacher"], "İlyas Güneş")
        self.assertNotIn("transcriptPath", new_item["payload"]["provenance"])
        self.assertNotIn("transcriptFingerprint", new_item["payload"]["provenance"])
        self.assertEqual(result["warnings"][0]["type"], "historical_quiz_provenance")
        # Prepare is dry-run: no source files are touched, not even quizzes.
        self.assertEqual(replacement.read(self.v2 / "items" / (self.video_id + ".json")),
                         self.original_video)
        self.assertEqual((self.v2 / "items" / (self.quiz_id + ".json")).read_bytes(), before_quiz)
        self.assertEqual((self.v2 / "lessons" / (self.lesson_id + ".json")).read_bytes(), before_lesson)

    def test_concurrent_old_url_change_is_rejected(self):
        a = copy.deepcopy(self.approvals)
        a["approvals"][0]["expected_old_url"] = "https://www.youtube.com/watch?v=AAAAAAAAAAA"
        with self.assertRaisesRegex(ValueError, "changed since review"):
            replacement.prepare(self.mapping, a, self.root)

    def test_nonexisting_target_cannot_be_added(self):
        a = copy.deepcopy(self.approvals)
        a["approvals"][0]["target_item_id"] = "item_mat9_vid_not_an_item"
        with self.assertRaisesRegex(ValueError, "non-existing"):
            replacement.prepare(self.mapping, a, self.root)

    def test_quiz_review_mandatory_when_linked(self):
        a = copy.deepcopy(self.approvals)
        a["approvals"][0]["quiz_reviewed"] = False
        with self.assertRaisesRegex(ValueError, "quiz must be manually reviewed"):
            replacement.prepare(self.mapping, a, self.root)

    def test_title_match_does_not_replace_human_review(self):
        a = copy.deepcopy(self.approvals)
        a["approvals"][0]["reviewed_contents"] = False
        with self.assertRaisesRegex(ValueError, "Manual viewing"):
            replacement.prepare(self.mapping, a, self.root)

    def test_unapproved_other_teacher_is_rejected(self):
        m = copy.deepcopy(self.mapping)
        m["videos"][0]["teacher"] = "Nurtaç Kozak"
        m["videos"][0]["channel"] = "SORU ÇÖZÜM KANALI"
        with self.assertRaisesRegex(ValueError, "not İlyas Güneş"):
            replacement.prepare(m, self.approvals, self.root)

    def test_cross_topic_without_explicit_override_fails(self):
        m = copy.deepcopy(self.mapping)
        m["videos"][0]["theme_lesson_ids"] = ["lesson_other_topic"]
        m["videos"][0]["best_match"]["lesson_id"] = "lesson_other_topic"
        with self.assertRaisesRegex(ValueError, "does not match target lesson"):
            replacement.prepare(m, self.approvals, self.root)

    def test_same_url_is_idempotent(self):
        item = copy.deepcopy(self.original_video)
        item["contentUrl"] = self.new_url
        self.save("items/" + self.video_id, item)
        r = replacement.prepare(self.mapping, self.approvals, self.root)
        self.assertEqual(r["unchanged_count"], 1)
        self.assertEqual(r["replace_count"], 0)

    def test_source_playlist_is_verified(self):
        bad = copy.deepcopy(self.mapping)
        bad["playlist_id"] = "PLWRONG"
        with self.assertRaisesRegex(ValueError, "Unexpected playlist"):
            replacement.prepare(bad, self.approvals, self.root)


if __name__ == "__main__":
    unittest.main(verbosity=2)
