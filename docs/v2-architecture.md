# StudyTracker V2 Architecture & Design Specification (Phases 1 & 2)

> **CANONICAL V2 OVERRIDE — 2026-10-02**
> StudyTracker V2 is a measurement/learning-optimization product, not a parental-control/evidence-capture product. Cloudflare KV is the active backend; earlier D1 rollout text in this historical document is superseded. VIDEO completion is self-reported; learning evidence comes from QUIZ, delayed recall and ANKI metrics. V1 screenshot/overlay/accessibility/parent-approval semantics must not gate V2 progress.


## 1. Overview & Coexistence Strategy

StudyTracker V2 introduces a high-resolution measurement and curriculum domain designed for deterministic tracking, immutable versioning, modular ordering, and AI analysis while maintaining **100% backward compatibility** with the existing V1 sync engine, Room database entities, and weekly plan workflows.

```
┌────────────────────────────────────────────────────────┐
│                   StudyTracker V1 + V2                 │
├────────────────────────────┬───────────────────────────┤
│    V1 Plan & Sync Model    │   V2 Curriculum & Measure │
├────────────────────────────┼───────────────────────────┤
│ • Weekly Plan DSL          │ • Courses -> Lessons ->   │
│ • Occurrences & Tasks      │   Learning Items          │
│ • Parent Review Gate       │ • Video / Quiz / Anki     │
│ • Task Status & Sharding   │ • Immutable IDs           │
│ • KV Storage               │ • Content Versioning      │
│                            │ • Modular Puzzle Ordering │
│                            │ • Append-Only Attempts    │
│                            │ • D1 + KV Fallback        │
│                            │ • Trusted Self-Completion │
└────────────────────────────┴───────────────────────────┘
```

---

## 2. Core Architectural Principles

### 2.1 Immutable Primary IDs & Stable Keys
- **Primary IDs (`id`)**: Universally unique, immutable identifiers assigned upon creation (e.g. `course_mat_9`, `lesson_gercek_sayilar`, `item_mat9_vid17`). Once created, IDs are never modified or recycled.
- **Stable Keys (`stable_key`)**: Immutable domain/business references (e.g. `mat9_sayilar_vid17`) that survive across imports, exports, and schema migrations.
- **Display Labels (`display_label`)**: Purely presentational labels (e.g. `17`, `17.2`, `18`, `Video 17`). Display labels are **never** used as primary keys, foreign keys, or database query identifiers.

### 2.2 Content Versioning & Append-Only Attempts
- **Learning Items are Containers**: When content changes (e.g. YouTube video URL updated, quiz questions refined, title revised), a **new version entity** (`learning_item_versions`) is appended with incremented `version_number` (e.g. `v1 -> v2`).
- **Pointers Update Safely**: The parent item’s `current_version_id` advances to point to the newest version. The `item.id` and `item.stable_key` remain strictly unchanged.
- **Referential Integrity for Attempts**: Student attempts (`attempts`) record `item_id` and the explicit `version_id` that was active when the student completed the attempt. Historical metrics remain 100% valid even if the content is revised later.
- **Trusted Student Self-Completion**: In V2, student completion is trusted. VIDEO completions are self-reported. No surveillance, accessibility services, floating overlays, screenshots, or parent review gating are required for student progress.

### 2.3 Modular "Puzzle" Ordering & Order Keys
- **Spacing-Based Rank Strategy**: Items and lessons use `order_key` (floating-point / real index) with initial steps of `1000.0` (`1000.0, 2000.0, 3000.0...`).
- **Inserting Intermediate Items**:
  - `insert-before`: Assigns midpoint between previous item and target item: `(prev.order_key + target.order_key) / 2.0`.
  - `insert-after`: Assigns midpoint between target item and next item: `(target.order_key + next.order_key) / 2.0`.
- **Rebalancing**: If the numeric gap between adjacent keys shrinks below `1e-4`, a deterministic rebalance re-spaces the items in that lesson (`1000.0, 2000.0, 3000.0...`). Rebalance **only** alters `order_key`; primary IDs and attempts are never touched.

### 2.4 Explicit Prerequisites & Dependency Graph
- Prerequisites are declared explicitly in `item_prerequisites` (e.g. Item B requires Item A with optional `min_score: 70.0`).
- Prerequisite status is evaluated deterministically at query time without implied sequential ordering based on names or display numbers.
- Circular dependencies (`A -> B -> A`) are detected via depth-first graph traversal and rejected at insertion time.

### 2.5 Strict AI Boundary (CLI-Only / No Runtime SDK)
- There is **no AI SDK, API client, background daemon, or scheduler** inside the Android application or Cloudflare Worker runtime.
- The CLI exposes deterministic, machine-readable data endpoints and commands (`studytracker-cli v2 ... --json` and `v2 export-context`).
- An external AI system (or LLM coding assistant) interacts with StudyTracker solely as an external operator via standard CLI invocations.

---

## 3. Real 9th-Grade Curriculum Reference Model

The canonical fixture for StudyTracker V2 is MEB 9th Grade Mathematics:

```
📚 Course: 9. Sınıf Matematik [course_mat_9]
 └── 📖 Lesson: Gerçek Sayılar [lesson_gercek_sayilar] (Order: 1000.0)
      ├── ▶ Video 17: Gerçek Sayılar ve Aralık Kavramı [item_mat9_vid17] (Order: 1000.0)
      │     └── Version 1 -> Version 2 (HD Revizyon)
      ├── 📝 Quiz 17: Gerçek Sayılar Tarama Testi 1 [item_mat9_quiz17] (Order: 2000.0)
      │     └── Version 1
      ├── 📝 Quiz 17.2: Gerçek Sayılar Pekiştirme Testi [item_mat9_quiz17_2] (Order: 2500.0)
      │     ├── Position: inserted between Quiz 17 and Video 18
      │     └── Prerequisite: requires Quiz 17 (minScore: 70.0)
      └── ▶ Video 18: Mutlak Değer ve Özellikleri [item_mat9_vid18] (Order: 3000.0)
            └── Version 1
```

---

## 4. Storage Architecture (D1 SQLite + KV Fallback)

### 4.1 Dual-Engine Abstraction
The V2 engine in `worker/v2/storage.js` provides an abstraction layer:
- **D1 SQLite Mode**: Activated when Cloudflare D1 binding `env.DB` is present. Runs optimized SQL queries and transactional statements based on `worker/schema-v2.sql`.
- **KV / In-Memory Fallback**: Operates when `env.DB` is not yet provisioned. Stores normalized JSON document collections under key namespaces (`v2:<familyCode>:courses`, `v2:<familyCode>:lessons`, etc.). Allows full testing and deployment without breaking changes.

### 4.2 D1 SQLite Schema (`worker/schema-v2.sql`)
- `courses`
- `lessons`
- `learning_items`
- `learning_item_versions`
- `item_prerequisites`
- `attempts` (with unique idempotency index on `(family_code, student_id, client_attempt_id)`)
- `quiz_answers`

---

## 5. HTTP API Namespace (`/api/v3/*`)

All V2 endpoints reside under `/api/v3/*` to avoid collisions with V1 `/api/v2/*` endpoints:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v3/catalog` or `/api/v3/curriculum` | Full hierarchical tree of courses, lessons, items, and current versions |
| `GET` | `/api/v3/courses` | List courses |
| `POST` | `/api/v3/courses` | Create course |
| `POST` | `/api/v3/courses/:id/archive` | Archive course |
| `GET` | `/api/v3/lessons` | List lessons |
| `POST` | `/api/v3/lessons` | Create lesson |
| `POST` | `/api/v3/lessons/:id/archive` | Archive lesson |
| `GET` | `/api/v3/items` | List items (filterable by lessonId) |
| `GET` | `/api/v3/items/:id` | Get item details and version history |
| `POST` | `/api/v3/items` | Create item (supports `insert-before` / `insert-after`) |
| `PATCH` | `/api/v3/items/:id/content` | Create **new content version** (item ID immutable) |
| `POST` | `/api/v3/items/:id/reorder` | Reorder item (modifies `order_key` only) |
| `POST` | `/api/v3/items/:id/archive` | Archive item (preserves historical attempts) |
| `GET` | `/api/v3/prerequisites` | List prerequisites |
| `POST` | `/api/v3/prerequisites` | Add prerequisite (with cycle prevention) |
| `DELETE` | `/api/v3/prerequisites` | Remove prerequisite |
| `GET` | `/api/v3/attempts` | Query student attempts (supports studentId, itemId, limit) |
| `POST` | `/api/v3/attempts` | Record student attempt (idempotent duplicate 200, append-only 201) |
| `GET` | `/api/v3/analytics/progress` | Deterministic progress & mastery summary |
| `GET` | `/api/v3/export-context` | Emits bounded JSON analysis package for external AI |

---

## 6. Phase 2 Content JSON Schema Definitions

In `learning_item_versions.payload_json` (or `payload`), each item type has a standard JSON schema:

### 6.1 VIDEO Schema
```json
{
  "url": "https://www.youtube.com/watch?v=mat9_vid17",
  "description": "Gerçek sayılar kümesi ve aralık kavramı konu anlatımı",
  "provider": "YOUTUBE"
}
```

### 6.2 QUIZ Schema
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

### 6.3 ANKI Schema
```json
{
  "deckName": "9. Sınıf Matematik - Gerçek Sayılar",
  "packageUri": "com.ichi2.anki",
  "webUrl": "https://ankiweb.net/decks",
  "instructions": "Desteyi açıp günlük 10 kart tekrarını tamamlayın."
}
```

---

## 7. Phase 2 Student Flow & Navigation Architecture

In Phase 2, a complete student flow is integrated seamlessly into the Android client:

```
ChildHomeScreen (V1 + V2 Banner)
  │
  └───> V2CoursesScreen ("Öğrenme Akışı / Dersler")
          │
          └───> V2LessonsScreen ("Üniteler & Konular")
                  │
                  └───> V2LearningFlowScreen ("Öğrenme Akışı / Modül Listesi")
                          ├── Video Action (Browser/Player Handoff -> Self-Report Complete)
                          ├── Quiz Action (Interactive Solver -> Automatic Score & Metrics)
                          └── Anki Action (External App Handoff -> Card Count & Metric Capture)
```

### 7.1 Learning Item States & Presentation
- **`COMPLETED`**: Displayed with green badge, best score (if quiz), and checkmark.
- **`AVAILABLE`**: Active border, next unfinished step highlighted with "Sıradaki Adım" pill.
- **`LOCKED_BY_PREREQUISITE`**: Locked icon; tapping shows exact missing prerequisite requirements (e.g. "Quiz 17'den en az %70 puan gerekli").
- **`ARCHIVED`**: Hidden from active view or grayed out without erasing attempt history.

### 7.2 Deterministic Local-First Sync Policy
1. **Catalog Pull**:
   - `V2CurriculumRepository.syncCatalog()` fetches `/api/v3/catalog`.
   - Upserts courses, lessons, items, versions, and prerequisites into local Room database.
   - **Never** deletes or overwrites student attempts.
2. **Attempt Capture**:
   - Every student action records an append-only `Attempt` locally first with `clientAttemptId` (UUID) and `syncStatus: "PENDING"`.
   - Asynchronous idempotent push is made to `/api/v3/attempts`.
   - On success, `syncStatus` transitions to `"SYNCED"`.
   - If offline, attempt remains in SQLite and will sync on subsequent connection.
3. **Prerequisite Calculation**:
   - Evaluated dynamically in `V2ProgressEngine` using local attempt records.
   - Zero dependency on parent reviews or network connectivity.

---

## 8. Deterministic Reminders Foundation

StudyTracker V2 includes a lightweight local reminder foundation:
- **`ItemReminderConfig`**: Stores `itemId`, `itemTitle`, `lessonTitle`, `enabled`, `dueEpochMillis`, `reminderNote`.
- **`V2ReminderHelper`**: Schedules exact/inexact RTC alarms via Android `AlarmManager` targeting `StudyReminderReceiver` with action `ACTION_V2_ITEM_REMINDER`.
- **Notification Delivery**: Posts normal notifications on channel `StudyNotificationManager.CHANNEL_REMINDERS`. If notification permissions are disabled or missing, the application remains fully functional with zero surveillance.

---

## 9. Android Room Schema Migration Reference (Room Version 8)

Room Database version 8 (`MIGRATION_7_8`):
- `courses`
- `lessons`
- `learning_items`
- `learning_item_versions`
- `item_prerequisites`
- `attempts`
- `quiz_answers`

All legacy V1 tables (`task_templates`, `occurrences`, `active_plan`, `sessions`, `screenshots`, `reviews`, `quizzes`) remain 100% operational and unaffected.

---

## 10. Vault-Backed Seed Catalog & Content Ingestion Pipeline (Phase 3)

StudyTracker V2 Phase 3 introduces a deterministic content ingestion pipeline rooted in authoritative Obsidian Vault records:

```
┌─────────────────────────────────────────────────────────────┐
│                 Authoritative Vault Sources                 │
│  (10-Projects/ 9th Grade Video Guides & Study Plans)        │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│               Deterministic Catalog Manifest                │
│             content/9-sinif-v2-catalog.json                 │
│       content/9-sinif-v2-catalog.sources.md (Audit)         │
├─────────────────────────────────────────────────────────────┤
│ • 9 Real Courses (Mat, Fiz, Kim, Biyo, Tar, Cog, Ing, Alm) │
│ • 12 Concept-Based Lessons (no rigid date IDs)              │
│ • 38 Learning Items (VIDEO, ANKI, QUIZ) with Provenance     │
│ • History Canonical Source: Mehmet Celal ÖZYILDIZ           │
│ • Exact Video IDs Preferred Over Generic Repeated URLs      │
│ • No Fabricated Questions (Draft/Unpublished Only)          │
│ • SHA-256 Content Fingerprints                              │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   CLI Seed Tooling Engine                   │
│  • validate: structural & audit rules check                 │
│  • diff    : detects missing, updated, and extra items      │
│  • apply   : idempotent apply; creates new version for      │
│              modified URLs; preserves attempts & extra items│
└─────────────────────────────────────────────────────────────┘
```

---

## 11. Safe V1 -> V2 Migration Architecture (Phase 3)

The V1->V2 migration engine enables seamless transitions from legacy weekly occurrences/sessions to V2 append-only attempts:

1. **Mapping Priorities & Confidence Tiers**:
   - `exact`: Exact YouTube video ID match or explicit legacy mapping.
   - `high`: Normalized subject and title token overlap (>75%).
   - `medium`: Ambiguous or partial match (50-75%).
   - `unmatched`: No matching V2 learning item found.
2. **Safety Gating**:
   - Only `exact` and `high` confidence completed records are converted into attempts.
   - `medium` and `unmatched` records are strictly excluded from automated execution and saved to `skippedRecords` with clear evidence.
3. **Idempotency**:
   - Each attempt uses a deterministic `clientAttemptId` (`mig_v1_${legacyId}`). Duplicate runs return `duplicate: true` without creating redundant attempts.
4. **Non-Destructive Guarantee**:
   - Zero V1 records are deleted or reset. Parent review approvals are preserved as legacy metadata and do not gate student completion.

---

## 12. Phase 4 Content Review, Publishing Visibility & Staging Readiness

### 12.1 Publishing Status & Student Catalog Isolation
- **States**: `draft`, `active`, `archived`.
- **Student Isolation**: `GET /api/v3/catalog` filters strictly on `publishing_status = 'active'`. Incomplete, unreviewed, or rejected items remain hidden from student workflows.
- **Admin & Parent Visibility**: Endpoints support `?status=all` to inspect the full curriculum graph.

### 12.2 Review Workflows & Override Preservation
- `studytracker-cli v2 review approve|reject|replace-content` operates with zero mutation on immutable `item.id` and `item.stable_key`.
- Rejection unpublishes the item (`archived`/`draft`) while strictly preserving student attempts.
- Replaced content creates a new version (`version_number: N+1`).
- Seed synchronization (`diffSeed` / `applySeed`) detects `reviewedOverridesPreserved` and never overwrites manual curator approvals with raw seed defaults.

### 12.3 Staging D1 Migration & Storage Health
- Checked-in D1 migration: `worker/migrations/0001_v2_schema.sql`.
- `/api/v3/health` endpoint exposes `storageBackend` (`d1` vs `kv_fallback`), `environment` (`staging` vs `production`), `staging` flag, and `schemaVersion` without disclosing secrets.
- `studytracker-cli v2 doctor` executes comprehensive dry-run diagnostics across API health, backend storage, attempts accessibility, and catalog drift.

---

## 13. Phase 5 Real Staging Rollout & Cloudflare D1 Architecture

### 13.1 Dedicated Staging Infrastructure
- **Cloudflare D1 Database**: `studytracker-v2-staging` bound exclusively as `env.DB`.
- **Cloudflare Worker**: `studytracker-v2-staging` configured via `worker/wrangler.staging.toml`.
- **Environment Isolation**:
  - `ENVIRONMENT = "staging"`
  - `STAGING = "true"`
  - `DB_NAME = "studytracker-v2-staging"`
- **Zero Production Touch Rule**: Production Cloudflare KV and Worker (`studytracker-sync`) remain completely untouched.

### 13.2 High-Fidelity D1 Storage Engine
The D1 storage engine (`worker/v2/storage.js`) translates curriculum operations into SQLite SQL statements:
- All tables adhere strictly to `worker/migrations/0001_v2_schema.sql`.
- Parent foreign keys (`learning_items`) are inserted before child rows (`learning_item_versions`).
- Idempotent attempt deduplication is enforced at the database level via unique composite index `idx_attempts_idempotency(family_code, student_id, client_attempt_id)`.
- Per-question analytics are persisted in normalized `quiz_answers` table with foreign key cascading to `attempts`.

### 13.3 End-to-End Staging Verification Matrix
1. **Health & Diagnostics**: `/api/v3/health` confirms `storageBackend: 'd1'`, `environment: 'staging'`. `v2 doctor` reports `overallHealthy: true`.
2. **Seed Lifecycle**: Seed validation, diff calculation, apply execution, and post-apply diff (0 drift, 100% idempotent).
3. **Publishing Semantics**: Student catalog hides draft & archived items; admin queries with `status=all` return all items.
4. **Student Attempts**: VIDEO self-complete, Quiz 17.2 puzzle scoring, and ANKI card metric completions.
5. **Content Versioning**: Modifying item content increments version number while preserving immutable item ID and historical attempt linkages.
6. **Offline Sync**: Pending offline attempts upload once with deduplication on retry.
7. **Safe V1 Migration**: Read-only extraction from V1, planned with high-confidence gating, and applied idempotently to Staging D1 only.
8. **Android Staging Configuration**: Dual BuildConfig URLs (`V2_BASE_URL` and `V2_STAGING_URL`) with runtime switching in `V2CloudClient.kt`.



