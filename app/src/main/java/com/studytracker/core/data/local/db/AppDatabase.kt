package com.studytracker.core.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studytracker.core.data.local.db.converter.AppTypeConverters
import com.studytracker.core.data.local.db.dao.*
import com.studytracker.core.data.local.db.entity.*

@Database(
    entities = [
        TaskTemplateEntity::class,
        OccurrenceEntity::class,
        PlanEntity::class,
        SessionEntity::class,
        ScreenshotEntity::class,
        ReviewEntity::class,
        QuizEntity::class,
        CourseEntity::class,
        LessonEntity::class,
        LearningItemEntity::class,
        LearningItemVersionEntity::class,
        ItemPrerequisiteEntity::class,
        AttemptEntity::class,
        QuizAnswerMetricEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskTemplateDao(): TaskTemplateDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun planDao(): PlanDao
    abstract fun sessionDao(): SessionDao
    abstract fun screenshotDao(): ScreenshotDao
    abstract fun reviewDao(): ReviewDao
    abstract fun quizDao(): QuizDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun learningItemDao(): LearningItemDao
    abstract fun learningItemVersionDao(): LearningItemVersionDao
    abstract fun itemPrerequisiteDao(): ItemPrerequisiteDao
    abstract fun attemptDao(): AttemptDao
    abstract fun quizAnswerMetricDao(): QuizAnswerMetricDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE occurrences ADD COLUMN studentNote TEXT")
                db.execSQL("ALTER TABLE sessions ADD COLUMN studentNote TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS quizzes (
                        quizId TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT,
                        date TEXT,
                        weekId TEXT,
                        durationMinutes INTEGER NOT NULL,
                        targetOccurrenceKey TEXT,
                        questionsJson TEXT NOT NULL,
                        completed INTEGER NOT NULL,
                        submittedAt INTEGER,
                        studentAnswersJson TEXT NOT NULL,
                        studentDurationSeconds INTEGER NOT NULL,
                        correctCount INTEGER NOT NULL,
                        wrongCount INTEGER NOT NULL,
                        emptyCount INTEGER NOT NULL
                    )""".trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quizzes ADD COLUMN studentNote TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN activeDurationSeconds INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE occurrences ADD COLUMN completedQuestionCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN reportedQuestionCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    UPDATE sessions
                    SET updatedAt = CASE
                        WHEN endTime IS NOT NULL AND endTime > 0 THEN endTime
                        ELSE startTime
                    END
                    WHERE updatedAt = 0
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS courses (
                        id TEXT NOT NULL PRIMARY KEY,
                        familyCode TEXT NOT NULL,
                        title TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        gradeLevel INTEGER NOT NULL DEFAULT 9,
                        description TEXT,
                        orderKey REAL NOT NULL DEFAULT 1000.0,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_courses_family ON courses(familyCode, isArchived, orderKey)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS lessons (
                        id TEXT NOT NULL PRIMARY KEY,
                        courseId TEXT NOT NULL,
                        familyCode TEXT NOT NULL,
                        title TEXT NOT NULL,
                        orderKey REAL NOT NULL DEFAULT 1000.0,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_lessons_course ON lessons(courseId, isArchived, orderKey)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_lessons_family ON lessons(familyCode)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS learning_items (
                        id TEXT NOT NULL PRIMARY KEY,
                        lessonId TEXT NOT NULL,
                        familyCode TEXT NOT NULL,
                        itemType TEXT NOT NULL,
                        displayLabel TEXT NOT NULL,
                        stableKey TEXT NOT NULL,
                        orderKey REAL NOT NULL DEFAULT 1000.0,
                        currentVersionId TEXT NOT NULL,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_items_lesson ON learning_items(lessonId, isArchived, orderKey)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_learning_items_family_stable ON learning_items(familyCode, stableKey)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS learning_item_versions (
                        id TEXT NOT NULL PRIMARY KEY,
                        itemId TEXT NOT NULL,
                        versionNumber INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        contentUrl TEXT,
                        payloadJson TEXT,
                        changelog TEXT,
                        createdAt INTEGER NOT NULL
                    )""".trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_item_versions_num ON learning_item_versions(itemId, versionNumber)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS item_prerequisites (
                        id TEXT NOT NULL PRIMARY KEY,
                        itemId TEXT NOT NULL,
                        requiredItemId TEXT NOT NULL,
                        minScore REAL,
                        createdAt INTEGER NOT NULL
                    )""".trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_prereq_unique ON item_prerequisites(itemId, requiredItemId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_prereq_item ON item_prerequisites(itemId)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS attempts (
                        id TEXT NOT NULL PRIMARY KEY,
                        clientAttemptId TEXT NOT NULL,
                        familyCode TEXT NOT NULL,
                        studentId TEXT NOT NULL,
                        itemId TEXT NOT NULL,
                        versionId TEXT NOT NULL,
                        status TEXT NOT NULL,
                        score REAL,
                        durationSeconds INTEGER NOT NULL DEFAULT 0,
                        startedAt INTEGER NOT NULL,
                        completedAt INTEGER,
                        metadataJson TEXT,
                        createdAt INTEGER NOT NULL
                    )""".trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_attempts_idempotency ON attempts(familyCode, studentId, clientAttemptId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_attempts_student_item ON attempts(familyCode, studentId, itemId, createdAt)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS quiz_answers (
                        id TEXT NOT NULL PRIMARY KEY,
                        attemptId TEXT NOT NULL,
                        questionId TEXT NOT NULL,
                        questionIndex INTEGER NOT NULL,
                        selectedOption TEXT,
                        isCorrect INTEGER NOT NULL DEFAULT 0,
                        durationSeconds INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_quiz_answers_attempt ON quiz_answers(attemptId)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN visualJson TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_tracker_db"
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
