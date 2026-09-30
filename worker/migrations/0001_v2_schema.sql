-- Migration: 0001_v2_schema.sql
-- Description: Initial StudyTracker V2 SQLite / D1 Schema with publishing_status and normalized tables

-- 1. Courses
CREATE TABLE IF NOT EXISTS courses (
    id TEXT NOT NULL PRIMARY KEY,
    family_code TEXT NOT NULL,
    title TEXT NOT NULL,
    subject TEXT NOT NULL,
    grade_level INTEGER NOT NULL DEFAULT 9,
    description TEXT,
    order_key REAL NOT NULL DEFAULT 1000.0,
    is_archived INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_courses_family ON courses(family_code, is_archived, order_key);

-- 2. Lessons / Topics
CREATE TABLE IF NOT EXISTS lessons (
    id TEXT NOT NULL PRIMARY KEY,
    course_id TEXT NOT NULL,
    family_code TEXT NOT NULL,
    title TEXT NOT NULL,
    order_key REAL NOT NULL DEFAULT 1000.0,
    is_archived INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_lessons_course ON lessons(course_id, is_archived, order_key);
CREATE INDEX IF NOT EXISTS idx_lessons_family ON lessons(family_code);

-- 3. Learning Items
CREATE TABLE IF NOT EXISTS learning_items (
    id TEXT NOT NULL PRIMARY KEY,
    lesson_id TEXT NOT NULL,
    family_code TEXT NOT NULL,
    item_type TEXT NOT NULL CHECK(item_type IN ('VIDEO', 'QUIZ', 'ANKI')),
    display_label TEXT NOT NULL,
    stable_key TEXT NOT NULL,
    order_key REAL NOT NULL DEFAULT 1000.0,
    current_version_id TEXT NOT NULL,
    publishing_status TEXT NOT NULL DEFAULT 'active' CHECK(publishing_status IN ('draft', 'active', 'archived')),
    is_archived INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(lesson_id) REFERENCES lessons(id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_learning_items_lesson ON learning_items(lesson_id, is_archived, order_key);
CREATE INDEX IF NOT EXISTS idx_learning_items_family_stable ON learning_items(family_code, stable_key);
CREATE INDEX IF NOT EXISTS idx_learning_items_status ON learning_items(family_code, publishing_status);

-- 4. Learning Item Versions
CREATE TABLE IF NOT EXISTS learning_item_versions (
    id TEXT NOT NULL PRIMARY KEY,
    item_id TEXT NOT NULL,
    version_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    content_url TEXT,
    payload_json TEXT,
    changelog TEXT,
    created_at INTEGER NOT NULL,
    FOREIGN KEY(item_id) REFERENCES learning_items(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_item_versions_num ON learning_item_versions(item_id, version_number);

-- 5. Item Prerequisites
CREATE TABLE IF NOT EXISTS item_prerequisites (
    id TEXT NOT NULL PRIMARY KEY,
    item_id TEXT NOT NULL,
    required_item_id TEXT NOT NULL,
    min_score REAL,
    created_at INTEGER NOT NULL,
    FOREIGN KEY(item_id) REFERENCES learning_items(id) ON DELETE CASCADE,
    FOREIGN KEY(required_item_id) REFERENCES learning_items(id) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_prereq_unique ON item_prerequisites(item_id, required_item_id);
CREATE INDEX IF NOT EXISTS idx_prereq_item ON item_prerequisites(item_id);

-- 6. Attempts
CREATE TABLE IF NOT EXISTS attempts (
    id TEXT NOT NULL PRIMARY KEY,
    client_attempt_id TEXT NOT NULL,
    family_code TEXT NOT NULL,
    student_id TEXT NOT NULL,
    item_id TEXT NOT NULL,
    version_id TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('STARTED', 'COMPLETED', 'ABANDONED')),
    score REAL,
    duration_seconds INTEGER NOT NULL DEFAULT 0,
    started_at INTEGER NOT NULL,
    completed_at INTEGER,
    metadata_json TEXT,
    created_at INTEGER NOT NULL,
    FOREIGN KEY(item_id) REFERENCES learning_items(id) ON DELETE RESTRICT,
    FOREIGN KEY(version_id) REFERENCES learning_item_versions(id) ON DELETE RESTRICT
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_attempts_idempotency ON attempts(family_code, student_id, client_attempt_id);
CREATE INDEX IF NOT EXISTS idx_attempts_student_item ON attempts(family_code, student_id, item_id, created_at);

-- 7. Quiz Answer Metrics
CREATE TABLE IF NOT EXISTS quiz_answers (
    id TEXT NOT NULL PRIMARY KEY,
    attempt_id TEXT NOT NULL,
    question_id TEXT NOT NULL,
    question_index INTEGER NOT NULL,
    selected_option TEXT,
    is_correct INTEGER NOT NULL CHECK(is_correct IN (0, 1)),
    duration_seconds INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    FOREIGN KEY(attempt_id) REFERENCES attempts(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_quiz_answers_attempt ON quiz_answers(attempt_id);
