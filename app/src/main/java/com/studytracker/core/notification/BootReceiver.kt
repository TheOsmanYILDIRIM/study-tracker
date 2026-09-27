package com.studytracker.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || 
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Cihaz yeniden başlatıldı / paket güncellendi, hatırlatıcı başlatılıyor...")
            if (com.studytracker.BuildConfig.APP_ROLE == "CHILD") StudyReminderScheduler.startReminderChecker(context)
        }
    }
}
