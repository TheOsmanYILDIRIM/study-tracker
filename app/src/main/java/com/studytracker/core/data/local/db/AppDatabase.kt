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
        QuizEntity::class
    ],
    version = 7,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_tracker_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
