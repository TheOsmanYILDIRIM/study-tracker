package com.studytracker.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object StudyReminderScheduler {

    private const val TAG = "StudyReminderScheduler"
    private const val REQUEST_CODE = 4001
    const val INTERVAL_MILLIS = 5 * 60 * 1000L // 5 Dakikada bir arkaplan kontrolü

    const val ACTION_CHECK_REMINDERS = "com.studytracker.action.CHECK_REMINDERS"

    /**
     * 5 dakikada bir arkaplanda bildirim ve veli mesajlarını kontrol eden alarm döngüsünü başlatır.
     */
    fun start5MinuteChecker(context: Context) {
        scheduleNextCheck(context, delayMillis = 10 * 1000L) // İlk kontrol 10 sn sonra
    }

    fun scheduleNextCheck(context: Context, delayMillis: Long = INTERVAL_MILLIS) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, StudyReminderReceiver::class.java).apply {
                action = ACTION_CHECK_REMINDERS
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = System.currentTimeMillis() + delayMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Sonraki 5 dakikalık bildirim kontrolü kuruldu: ${delayMillis / 1000}s sonra")
        } catch (e: Exception) {
            Log.e(TAG, "scheduleNextCheck hatası: ${e.message}", e)
        }
    }

    fun cancel(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, StudyReminderReceiver::class.java).apply {
                action = ACTION_CHECK_REMINDERS
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "cancel hatası: ${e.message}", e)
        }
    }
}
