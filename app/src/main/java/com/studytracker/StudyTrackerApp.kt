package com.studytracker

import android.app.Application
import com.studytracker.core.notification.StudyNotificationManager
import com.studytracker.core.notification.StudyReminderScheduler

class StudyTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Bildirim kanallarını oluştur (Android 8.0+)
        StudyNotificationManager.createNotificationChannels(this)

        // Veli mesajı ve günlük ders hatırlatıcıları yalnız Öğrenci APK'da gerekir.
        if (BuildConfig.APP_ROLE == "CHILD") {
            StudyReminderScheduler.startReminderChecker(this)
        }
    }
}
