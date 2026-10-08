#!/usr/bin/env python3
"""Offline end-to-end contract test for the isolated Math 9 draft generator."""
import hashlib
import importlib.util
import json
import tempfile
import unittest
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("math9_draft", HERE / "build-math9-transcript-draft.py")
builder = importlib.util.module_from_spec(spec)
spec.loader.exec_module(builder)


class Math9DraftTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        ids = [f"VideoID{i:04d}" for i in range(60)]
        self.catalog = {
            "playlist_id": "PLSYiXUktJiZeqUJyNFUgFHwOUNydbC-II",
            "videos": [{
                "id": vid, "position": i + 1,
                "title": f"9. Sınıf Matematik Ders {i+1}",
                "url": f"https://www.youtube.com/watch?v={vid}",
                "channel": "İlyas Güneş"
            } for i, vid in enumerate(ids)]
        }
        self.plan = {
            "course_id": "course_mat_9",
            "source_playlist_id": self.catalog["playlist_id"],
            "content_version": "math9_test_v1",
            "expected_video_ids": ids,
            "groups": [
                {"id": "ilk", "title": "Sayılar", "theme": "Sayılar",
                 "coverage": "title_topic", "first_position": 1, "last_position": 30},
                {"id": "son", "title": "Diğer", "theme": "Diğer",
                 "coverage": "theme_only", "first_position": 31, "last_position": 60}
            ]
        }
        self.zip_path = self.root / "captions.zip"
        with zipfile.ZipFile(self.zip_path, "w", compression=zipfile.ZIP_DEFLATED) as z:
            for vid in ids:
                z.writestr(f"transcripts/{vid}.tr.cues.json",
                           json.dumps({"id": vid, "cues": [
                               {"start": "00:00:01.000", "text": "Birinci konu"},
                               {"start": "00:00:04.000", "text": "İkinci örnek"}
                           ]}))
        self.original_sha = builder.EXPECTED_ZIP_SHA
        builder.EXPECTED_ZIP_SHA = hashlib.sha256(self.zip_path.read_bytes()).hexdigest()
        self.addCleanup(lambda: setattr(builder, "EXPECTED_ZIP_SHA", self.original_sha))

    def test_draft_is_complete_and_progress_safe(self):
        out = self.root / "draft"
        result = builder.build(self.plan, self.catalog, self.zip_path, out)
        self.assertEqual(result["video_count"], 60)
        self.assertEqual(result["lesson_count"], 2)
        self.assertEqual(result["quiz_count"], 0)
        self.assertEqual(result["needs_review_count"], 60)
        self.assertFalse(result["existing_student_progress_modified"])
        self.assertEqual(len(list((out / "items").glob("*.json"))), 60)
        self.assertEqual(len(list((out / "lessons").glob("*.json"))), 2)
        item = json.loads((out / "items/item_mat9_2026_v2_video_01.json").read_text())
        self.assertEqual(item["publishingStatus"], "draft")
        self.assertEqual(item["payload"]["provenance"]["transcriptCueCount"], 2)
        self.assertEqual(item["contentUrl"], self.catalog["videos"][0]["url"])
        self.assertEqual(item["payload"]["provenance"]["curriculumAlignment"], "pending_timestamp_level_review")
        self.assertEqual(len(list((out / "review").glob("*.json"))), 1)

    def test_source_order_change_rejected(self):
        self.catalog["videos"][0]["id"] = "OtherID0001"
        with self.assertRaisesRegex(ValueError, "Source playlist IDs changed"):
            builder.build(self.plan, self.catalog, self.zip_path, self.root / "out")

    def test_duplicate_group_coverage_rejected(self):
        self.plan["groups"][1]["first_position"] = 30
        with self.assertRaisesRegex(ValueError, "partition"):
            builder.build(self.plan, self.catalog, self.zip_path, self.root / "out")

    def test_refuses_to_overwrite_existing_draft(self):
        out = self.root / "draft"
        out.mkdir()
        (out / "sentinel").write_text("do not overwrite")
        with self.assertRaisesRegex(ValueError, "existing content is protected"):
            builder.build(self.plan, self.catalog, self.zip_path, out)
        self.assertEqual((out / "sentinel").read_text(), "do not overwrite")

    def test_invalid_transcript_fails_closed(self):
        self.zip_path.write_bytes(b"invalid")
        with self.assertRaisesRegex(ValueError, "Unreviewed transcript ZIP"):
            builder.build(self.plan, self.catalog, self.zip_path, self.root / "out")


if __name__ == "__main__":
    unittest.main(verbosity=2)
