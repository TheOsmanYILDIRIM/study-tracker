# StudyTracker V2 Staging Environment & D1 Rollout Guide

## 1. Overview & Isolation Guarantee

StudyTracker V2 Phase 5 establishes an isolated, production-grade staging environment on Cloudflare Workers and Cloudflare D1. The staging infrastructure is completely decoupled from the V1 production Cloudflare KV and legacy sync pipeline.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Cloudflare Edge Network                         │
├──────────────────────────────────┬─────────────────────────────────────┤
│      Production Environment      │         Staging Environment         │
├──────────────────────────────────┼─────────────────────────────────────┤
│ • Worker: studytracker-sync      │ • Worker: studytracker-v2-staging   │
│ • Storage: Cloudflare KV Shards  │ • Storage: Cloudflare D1 (SQLite)   │
│ • Base URL:                      │ • Base URL:                         │
│   studytracker-sync.workers.dev  │   studytracker-v2-staging.          │
│ • Production Data UNTOUCHED      │   osman13241429.workers.dev         │
└──────────────────────────────────┴─────────────────────────────────────┘
```

> [!IMPORTANT]
> **Production Safety Guarantee**:
> 1. The production Cloudflare Worker (`studytracker-sync`) and Cloudflare KV namespaces are never modified or reset during staging testing or rollout.
> 2. V1 to V2 migration tools read legacy occurrences and sessions in a strictly **read-only** manner and apply generated attempts **only** to the staging database.

---

## 2. Infrastructure Specification

### 2.1 Staging Cloudflare D1 Database
- **Database Name**: `studytracker-v2-staging`
- **Schema Migration**: `worker/migrations/0001_v2_schema.sql`
- **Binding Name**: `DB` (accessible via `env.DB` inside Worker execution context)
- **Engine**: SQLite / Cloudflare D1 Serverless SQL

### 2.2 Staging Cloudflare Worker
- **Script Name**: `studytracker-v2-staging`
- **Configuration File**: `worker/wrangler.staging.toml`
- **Environment Variables**:
  - `ENVIRONMENT = "staging"`
  - `STAGING = "true"`
  - `DB_NAME = "studytracker-v2-staging"`
- **Health Endpoint**: `GET /api/v3/health`
  - Expected Response: `{"success": true, "status": "ok", "version": "2.0.0", "schemaVersion": "v2", "storageBackend": "d1", "environment": "staging", "staging": true, "database": "studytracker-v2-staging"}`

---

## 3. Tooling & CLI Integration

### 3.1 CLI Staging Flag & Environment Variables
The StudyTracker CLI provides built-in staging support via `--staging` flag and environment variables:

| Environment Variable | CLI Option | Purpose | Default |
|---|---|---|---|
| `STUDYTRACKER_STAGING=true` | `--staging` | Routes all V2 CLI operations to staging Worker | `false` |
| `STUDYTRACKER_STAGING_WORKER_URL` | N/A | Custom staging worker URL | `https://studytracker-v2-staging.osman13241429.workers.dev` |
| `STUDYTRACKER_STAGING_FAMILY_CODE` | `--code <CODE>` | Staging family code override | Loaded from config / `ST-2026` |
| `STUDYTRACKER_STAGING_ADMIN_TOKEN` | N/A | Staging admin authentication token | Loaded from config |

### 3.2 Staging Diagnostic Doctor
Run non-destructive diagnostic checks on the staging instance:
```bash
node cli/bin/studytracker-cli.js v2 doctor --staging --json
```
Verifies:
1. `/api/v3/health` reachability and storage backend (`d1`).
2. Live catalog counts and publishing status split (active, draft, archived).
3. Attempt recording endpoint responsiveness.
4. Seed drift detection (without mutating database state).

---

## 4. Content Seeding & Publishing Semantics

### 4.1 Seed Pipeline
1. **Validation**: `studytracker-cli v2 seed validate --file content/9-sinif-v2-catalog.json` (verifies curriculum structure, 9 courses, 38 items, audit rules, and canonical history teacher requirements).
2. **Diff**: `studytracker-cli v2 seed diff --staging` (computes delta against staging D1).
3. **Apply**: `studytracker-cli v2 seed apply --staging` (creates courses, lessons, items, and initial versions).
4. **Idempotency Verification**: Running `seed diff` immediately after apply confirms `0 drift` (0 courses to create, 0 items to create, 0 items to update).

### 4.2 Publishing Semantics
- **Active Items (`publishingStatus: "active"`)**: Visible in the student mobile application catalog (`GET /api/v3/catalog`).
- **Draft Items (`publishingStatus: "draft"`)**: Hidden from the student catalog; accessible only via admin inspection (`GET /api/v3/items?status=all` or `studytracker-cli v2 review list`).
- **Archived Items (`publishingStatus: "archived"`)**: Hidden from student catalog. Historical attempts linked to archived items remain permanently intact with referential integrity.

---

## 5. End-to-End Verification Workflows

### 5.1 Video Self-Complete & Idempotency
- Student completes video lecture (e.g. `item_mat9_vid_uslu_giris`).
- App submits attempt with unique `clientAttemptId` (e.g. `stg-attempt-vid-17-001`).
- Server inserts row into `attempts` table.
- Resubmitting with identical `clientAttemptId` returns `200 OK` with `duplicate: true`, preventing duplicate metrics.

### 5.2 Quiz Authoring & Question Metrics
- Quiz created with puzzle ordering (e.g. Quiz 17.2 inserted between Video 17 and Video 18 with order key interpolation).
- Student completes quiz and submits per-question answer metrics.
- Server validates answers, calculates overall score (e.g. 100%), records attempt, and inserts individual question records into `quiz_answers` table.

### 5.3 ANKI Flashcard Completion
- Student completes Anki deck review.
- App sends `reviewed_cards`, `retention_rate`, and deck metadata in attempt payload.
- Server stores progress in `attempts` table with JSON metadata preservation.

### 5.4 Content Versioning & Immutable IDs
- Updating item content (URL, title, payload) creates a **new version entity** (`learning_item_versions`, e.g. `v1 -> v2`).
- Parent `item.id` and `item.stable_key` remain strictly unchanged.
- Historical attempts maintain their foreign key to `v1`, preserving historical measurement validity.

### 5.5 Offline Sync Simulation
- When device is offline, attempt is captured locally in Room DB with `clientAttemptId`.
- Upon network restoration, background sync worker pushes attempt to `/api/v3/attempts`.
- Server records attempt as `COMPLETED`. Any retry from sync queue is acknowledged idempotently without duplicate records.

---

## 6. Safe V1 -> V2 Migration Flow

1. **Analyze**: `studytracker-cli v2 migrate-v1 analyze --staging` analyzes V1 occurrences and sessions against V2 curriculum.
2. **Plan**: `studytracker-cli v2 migrate-v1 plan --staging --output migration-plan.json` filters out unmatched or ambiguous items, generating deterministic `clientAttemptId`s (`mig_v1_<legacyId>`).
3. **Apply**: `studytracker-cli v2 migrate-v1 apply --staging --plan migration-plan.json` applies attempts exclusively to staging D1.
4. **Idempotent Re-run**: Executing apply again detects all attempts as duplicates (`duplicateCount == totalAttempts`, `recordedCount == 0`).
5. **Read-Only Guarantee**: V1 production KV state is never mutated, reset, or deleted.

---

## 7. Android Staging Build Configuration

Android app supports environment-driven staging configuration without hardcoding secrets or breaking production defaults:

### 7.1 Gradle Build Types (`app/build.gradle.kts`)
- `release`: Points to production URL (`STUDYTRACKER_V2_BASE_URL` or `https://studytracker-sync.osman13241429.workers.dev`).
- `debug`: Defaults to staging URL (`STUDYTRACKER_V2_STAGING_URL` or `https://studytracker-v2-staging.osman13241429.workers.dev`).
- `defaultConfig`: Exposes `BuildConfig.V2_BASE_URL` and `BuildConfig.V2_STAGING_URL`.

### 7.2 Dynamic Runtime Switch (`V2CloudClient.kt`)
```kotlin
// Switch to staging at runtime for debugging
V2CloudClient.setCustomBaseUrl("https://studytracker-v2-staging.osman13241429.workers.dev")

// Reset to build-type default
V2CloudClient.resetBaseUrl()
```

> [!CAUTION]
> **No Local Gradle Execution Rule**: Local `./gradlew` execution is strictly prohibited inside the Termux Android Linux environment to prevent thermal throttling and memory exhaustion. All Android compilation and instrumentation tests run exclusively in GitHub Actions CI.
