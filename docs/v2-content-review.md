# StudyTracker V2 Content Review, Quiz Authoring & Staging Readiness Guide

This document outlines the operational review workflows, quiz authoring standards, publishing visibility semantics, and staging/D1 readiness diagnostics introduced in StudyTracker V2 Phase 4.

---

## 1. Content Publishing Semantics & Visibility

In StudyTracker V2, every learning item has an explicit `publishing_status` that controls its lifecycle and catalog visibility:

| Status | Meaning | Student Catalog Visibility | Parent/Admin Inspection |
|---|---|---|---|
| `active` | Production-ready, verified educational content | **Visible** | Visible |
| `draft` | Content under review, incomplete quiz, or unverified channel link | **Hidden** | Visible (`--status all` / `status=all`) |
| `archived` | Deprecated or replaced content; historical record preserved | **Hidden** | Visible (`--status all` / `status=all`) |

### Core Guarantees:
- **Student Catalog Safety**: The standard student endpoint (`GET /api/v3/catalog`) strictly returns only `active` items. Drafts and archived items are completely excluded by default.
- **Needs Review vs Active**: Items flagged with `reviewStatus: "needs_review"` (e.g. channel URLs, unverified links) must remain `draft` unless explicitly marked safe and confirmed.
- **Canonical History Teacher Enforcement**: Active History video items must belong to `Mehmet Celal ÖZYILDIZ`. Any active History item with an unverified or conflicting teacher fails seed validation.

---

## 2. Content Review Workflow CLI

The V2 review workflow enables curators and parents to inspect, approve, reject, or replace content with full provenance tracking:

### 2.1 List Items for Review
List all items filtered by review status or course:
```bash
# List all items needing review:
studytracker-cli v2 review list --status needs_review

# List draft items in a specific course as JSON:
studytracker-cli v2 review list --status draft --course course_mat_9 --json
```

### 2.2 Show Item Review Details
Inspect the current version, payload, review state, and full version history for an item:
```bash
studytracker-cli v2 review show item_mat9_vid17 --json
```

### 2.3 Approve an Item
Approving transitions the item to `publishingStatus: "active"` and `reviewStatus: "verified"`. Immutable IDs and stable keys are strictly preserved:
```bash
studytracker-cli v2 review approve item_mat9_vid17 --note "Verified MEB 2026 alignment"
```

### 2.4 Reject an Item
Rejecting unpublishes the item by transitioning it to `publishingStatus: "archived"` (or `draft`) and `reviewStatus: "rejected"`. **Student attempts and completion history are never deleted**:
```bash
studytracker-cli v2 review reject item_mat9_vid17 --reason "Channel URL was deprecated by publisher"
```

### 2.5 Replace Content
Replaces the active content of an item by appending a **new content version** (`version_number: N+1`). The immutable item ID, stable key, and existing attempts remain intact:
```bash
studytracker-cli v2 review replace-content item_mat9_vid17 \
  --file /path/to/new-content.json \
  --note "Upgraded video source to 2026 HD remaster"
```

### 2.6 Reviewed Overrides Preservation
When an item has been manually approved or updated through the review workflow (`provenance.reviewedOverride = true` or verified version > 1), subsequent `seed diff` and `seed apply` operations **will not silently overwrite** the manual review. The diff engine marks the item as `reviewedOverridesPreserved`.

---

## 3. Quiz Authoring & Import Workflow

StudyTracker V2 enforces deterministic, source-backed quiz authoring without hallucinated questions.

### 3.1 Supported Question Types
Only two question types are permitted:
1. `MULTIPLE_CHOICE`: Minimum 2 choices, `correctAnswer` must exactly match one of the choices.
2. `TRUE_FALSE`: Strict boolean or canonical `"Doğru"` / `"Yanlış"` / `"True"` / `"False"` representation.

### 3.2 Quiz JSON Schema
```json
{
  "questions": [
    {
      "id": "q1",
      "prompt": "$\\sqrt{2}$ sayısı hangi sayı kümesine aittir?",
      "type": "MULTIPLE_CHOICE",
      "choices": [
        "A) Doğal Sayılar (N)",
        "B) Tam Sayılar (Z)",
        "C) Rasyonel Sayılar (Q)",
        "D) İrrasyonel Sayılar (I)"
      ],
      "correctAnswer": "D) İrrasyonel Sayılar (I)",
      "explanation": "Karekök dışına tam çıkamayan sayılar irrasyoneldir."
    },
    {
      "id": "q2",
      "prompt": "Her rasyonel sayı aynı zamanda bir gerçek sayıdır.",
      "type": "TRUE_FALSE",
      "choices": ["Doğru", "Yanlış"],
      "correctAnswer": "Doğru",
      "explanation": "Gerçek sayılar kümesi (R), rasyonel ve irrasyonel sayıların birleşimidir."
    }
  ]
}
```

### 3.3 Validate a Quiz File
Validates question IDs, non-empty prompts, choice matching, and computes a deterministic SHA-256 fingerprint:
```bash
studytracker-cli v2 quiz validate --file content/quiz-mat-kumeler.json --json
```

### 3.4 Attach Quiz to an Existing Item
Updates the content version of an existing QUIZ item without altering its ID:
```bash
studytracker-cli v2 quiz attach --item item_mat9_quiz17 --file content/quiz-mat-kumeler.json
```

### 3.5 Create a New Quiz Item (Puzzle Insertion)
Creates a new quiz item and positions it using modular order keys:
```bash
# Insert Quiz 17.2 between Quiz 17 and Video 18:
studytracker-cli v2 quiz create \
  --lesson lesson_gercek_sayilar \
  --label "Quiz 17.2" \
  --stable-key mat9_quiz_17_2 \
  --title "Gerçek Sayılar Pekiştirme Testi" \
  --file content/quiz-mat-kumeler.json \
  --after item_mat9_quiz17
```

---

## 4. Staging & D1 Storage Readiness

### 4.1 Schema Migration (`worker/migrations/0001_v2_schema.sql`)
The checked-in SQL migration defines all relational tables, foreign key constraints, and indexing for Cloudflare D1:
- `courses`
- `lessons`
- `learning_items` (includes `publishing_status` and index `idx_learning_items_status`)
- `learning_item_versions`
- `item_prerequisites`
- `attempts`
- `quiz_answers`

### 4.2 Staging D1 Setup Commands (Documentation Only)
To apply the schema to a staging D1 database via Wrangler:
```bash
# Execute migration against Cloudflare D1 staging database:
wrangler d1 execute studytracker-staging-db --file=worker/migrations/0001_v2_schema.sql

# Verify table creation:
wrangler d1 execute studytracker-staging-db --command="SELECT name FROM sqlite_master WHERE type='table';"
```

### 4.3 Health Endpoint (`GET /api/v3/health`)
The authenticated health endpoint reports version, schema version, and the active storage backend (`d1` vs `kv_fallback`):
```bash
curl -H "x-family-code: YOUR_FAMILY_CODE" https://api.studytracker.internal/api/v3/health
```
Response:
```json
{
  "status": "ok",
  "version": "v2",
  "schemaVersion": "v2",
  "storageBackend": "d1",
  "timestamp": "2026-09-30T19:00:00.000Z"
}
```

---

## 5. Diagnostic Tooling: V2 Doctor

The `studytracker-cli v2 doctor` command runs read-only diagnostic checks across the entire V2 system:

```bash
studytracker-cli v2 doctor [--json]
```

### Diagnostic Checks Performed:
1. **API v3 Availability**: Verifies that `/api/v3/catalog` and `/api/v3/health` respond successfully.
2. **Storage Backend**: Reports active backend (`d1` or `kv_fallback`) and schema version.
3. **Catalog Summary**: Counts active, draft, and archived courses, lessons, and items.
4. **Attempts Endpoint**: Checks query availability of student attempt records.
5. **Seed Drift Analysis**: Performs a read-only dry-run diff between the local seed catalog and server state, identifying pending changes or preserved reviewed overrides.
