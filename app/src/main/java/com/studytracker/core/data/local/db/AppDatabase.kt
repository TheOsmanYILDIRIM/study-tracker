package com.studytracker.core.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 4,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_tracker_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
