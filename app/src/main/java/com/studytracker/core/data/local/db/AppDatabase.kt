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
    version = 5,
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
                        durationMinutes INTEGER NOT NULL DEFAULT 15,
                        targetOccurrenceKey TEXT,
                        questionsJson TEXT NOT NULL DEFAULT '[]',
                        completed INTEGER NOT NULL DEFAULT 0,
                        submittedAt INTEGER,
                        studentAnswersJson TEXT NOT NULL DEFAULT '{}',
                        studentDurationSeconds INTEGER NOT NULL DEFAULT 0,
                        correctCount INTEGER NOT NULL DEFAULT 0,
                        wrongCount INTEGER NOT NULL DEFAULT 0,
                        emptyCount INTEGER NOT NULL DEFAULT 0
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_tracker_db"
).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
