# StudyTracker V2 Architecture & Design Specification (Phase 1)

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
| `GET` | `/api/v3/catalog` or `/api/v3/curriculum` | Full hierarchical tree of courses, lessons, and items |
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
| `GET` | `/api/v3/attempts` | Query student attempts |
| `POST` | `/api/v3/attempts` | Record student attempt (idempotent, append-only) |
| `GET` | `/api/v3/analytics/progress` | Deterministic progress & mastery summary |
| `GET` | `/api/v3/export-context` | Emits bounded JSON analysis package for external AI |

---

## 6. CLI Command Reference

The StudyTracker CLI includes first-class V2 administration commands:

```bash
# Müfredat ve Ders İşlemleri
studytracker-cli v2 catalog [--course <id>] [--json]
studytracker-cli v2 course list [--all] [--json]
studytracker-cli v2 course create --title "9. Sınıf Matematik" --subject "Matematik" --grade 9
studytracker-cli v2 lesson create --course course_mat_9 --title "Gerçek Sayılar"

# Öğrenme Öğeleri & Sürümleme
studytracker-cli v2 item create --lesson lesson_gercek_sayilar --type VIDEO --label "Video 17" --title "Gerçek Sayılar Giriş"
studytracker-cli v2 item update-content item_mat9_vid17 --title "Gerçek Sayılar HD" --changelog "Revizyon"

# Puzzle / Modüler Araya Ekleme
studytracker-cli v2 item insert-after item_mat9_quiz17 --lesson lesson_gercek_sayilar --type QUIZ --label "Quiz 17.2" --title "Pekiştirme"

# Ön Koşul Yönetimi
studytracker-cli v2 prereq add --item item_mat9_quiz17_2 --requires item_mat9_quiz17 --min-score 70
studytracker-cli v2 prereq list

# Ölçme & İlerleme
studytracker-cli v2 attempts record --student student_ali --item item_mat9_quiz17 --score 85 --duration 450
studytracker-cli v2 progress --student student_ali --json

# Dış AI Entegrasyonu (Bounded Context Package)
studytracker-cli v2 export-context --student student_ali
```

---

## 7. Android Room Migration (Version 7 -> 8)

Room Database is updated to version 8 with migration `MIGRATION_7_8`:
- Added entities: `CourseEntity`, `LessonEntity`, `LearningItemEntity`, `LearningItemVersionEntity`, `ItemPrerequisiteEntity`, `AttemptEntity`, `QuizAnswerMetricEntity`.
- Added DAOs: `CourseDao`, `LessonDao`, `LearningItemDao`, `LearningItemVersionDao`, `ItemPrerequisiteDao`, `AttemptDao`, `QuizAnswerMetricDao`.
- All legacy V1 tables (`task_templates`, `occurrences`, `active_plan`, `sessions`, `screenshots`, `reviews`, `quizzes`) remain untouched and operational.
