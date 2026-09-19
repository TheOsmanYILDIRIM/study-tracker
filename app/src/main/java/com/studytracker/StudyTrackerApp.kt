package com.studytracker

import android.app.Application
import com.studytracker.core.notification.StudyNotificationManager
import com.studytracker.core.notification.StudyReminderScheduler

class StudyTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Bildirim kanallarını oluştur (Android 8.0+)
        StudyNotificationManager.createNotificationChannels(this)

        // Arkaplan 5 dakikalık bildirim ve hatırlatıcı kontrolcüsünü başlat
        StudyReminderScheduler.start5MinuteChecker(this)
    }
}
