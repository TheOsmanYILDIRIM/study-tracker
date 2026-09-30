package com.studytracker.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.studytracker.MainActivity
import com.studytracker.R
import kotlinx.serialization.Serializable

private const val TAG = "V2Reminder"

@Serializable
data class ItemReminderConfig(
    val itemId: String,
    val itemTitle: String,
    val lessonTitle: String = "",
    val enabled: Boolean = true,
    val dueEpochMillis: Long = 0L,
    val reminderNote: String? = null
)

object V2ReminderHelper {

    const val ACTION_V2_ITEM_REMINDER = "com.studytracker.action.V2_ITEM_REMINDER"
    const val EXTRA_ITEM_ID = "EXTRA_V2_ITEM_ID"
    const val EXTRA_ITEM_TITLE = "EXTRA_V2_ITEM_TITLE"
    const val EXTRA_LESSON_TITLE = "EXTRA_V2_LESSON_TITLE"
    const val EXTRA_NOTE = "EXTRA_V2_NOTE"

    fun scheduleItemReminder(context: Context, config: ItemReminderConfig) {
        if (!config.enabled || config.dueEpochMillis <= System.currentTimeMillis()) return
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, StudyReminderReceiver::class.java).apply {
                action = ACTION_V2_ITEM_REMINDER
                putExtra(EXTRA_ITEM_ID, config.itemId)
                putExtra(EXTRA_ITEM_TITLE, config.itemTitle)
                putExtra(EXTRA_LESSON_TITLE, config.lessonTitle)
                putExtra(EXTRA_NOTE, config.reminderNote)
            }

            val requestCode = (config.itemId.hashCode() and 0x7FFFFFFF)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    config.dueEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    config.dueEpochMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "V2 Hatırlatıcı kuruldu: ${config.itemTitle} (${config.dueEpochMillis})")
        } catch (e: Exception) {
            Log.e(TAG, "scheduleItemReminder error: ${e.message}", e)
        }
    }

    fun cancelItemReminder(context: Context, itemId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, StudyReminderReceiver::class.java).apply {
                action = ACTION_V2_ITEM_REMINDER
            }
            val requestCode = (itemId.hashCode() and 0x7FFFFFFF)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "V2 Hatırlatıcı iptal edildi: $itemId")
        } catch (e: Exception) {
            Log.e(TAG, "cancelItemReminder error: ${e.message}", e)
        }
    }

    fun showItemReminderNotification(context: Context, config: ItemReminderConfig): Boolean {
        StudyNotificationManager.createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_V2_ITEM_ID", config.itemId)
            putExtra("EXTRA_OPEN_ROLE", "CHILD")
        }

        val requestCode = (config.itemId.hashCode() and 0x7FFFFFFF)
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "📚 Çalışma Vakti: ${config.itemTitle}"
        val text = config.reminderNote?.ifBlank { null } ?: if (config.lessonTitle.isNotBlank()) {
            "${config.lessonTitle} modülüne devam etmek için tıkla."
        } else {
            "Sıradaki öğrenme adımına başlamaya hazır mısın?"
        }

        val notification = NotificationCompat.Builder(context, StudyNotificationManager.CHANNEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(requestCode, notification)
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission missing: ${e.message}")
            false
        }
    }
}
