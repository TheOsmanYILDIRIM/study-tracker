package com.studytracker.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.studytracker.MainActivity
import com.studytracker.R
import com.studytracker.core.data.remote.sync.RemoteMessageSyncDto

object StudyNotificationManager {

    const val CHANNEL_REMINDERS = "studytracker_reminders"
    const val CHANNEL_PARENT_NUDGES = "studytracker_parent_nudges"

    private const val NOTIFICATION_ID_BASE_PARENT = 1000
    private const val NOTIFICATION_ID_STUDY_REMINDER = 2000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Ders ve Çalışma Hatırlatıcıları Kanalı
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Ders ve Çalışma Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Planlanan dersler, etüt saatleri ve çalışma periyotları için hatırlatmalar"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            // 2. Veli Bildirimleri ve Mesaj Kanalı
            val nudgeChannel = NotificationChannel(
                CHANNEL_PARENT_NUDGES,
                "Veli Mesajları ve Motivasyon",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Veliden gelen anlık hatırlatmalar, motivasyon notları ve mesajlar"
                enableLights(true)
                lightColor = Color.MAGENTA
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(nudgeChannel)
        }
    }

    /**
     * Veliden gelen anlık mesaj veya motivasyon bildirimini gösterir.
     */
    fun showParentNudgeNotification(context: Context, messageDto: RemoteMessageSyncDto) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_MESSAGE_ID", messageDto.id)
            putExtra("EXTRA_OPEN_ROLE", "CHILD")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            messageDto.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emojiPrefix = when (messageDto.type) {
            "PRAISE" -> "🌟 "
            "URGENT" -> "🚨 "
            "CUSTOM" -> "💌 "
            else -> "⏰ "
        }

        val title = if (messageDto.title.isNotBlank()) {
            "$emojiPrefix${messageDto.title}"
        } else {
            "$emojiPrefix Velinden Mesaj Var!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_PARENT_NUDGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageDto.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageDto.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400))
            .build()

        try {
            val notificationId = NOTIFICATION_ID_BASE_PARENT + (messageDto.id.hashCode() % 1000).coerceAtLeast(0)
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+
        }
    }

    /**
     * Günlük veya yaklaşan ders için hatırlatıcı bildirim gösterir.
     */
    fun showStudyReminderNotification(context: Context, title: String, message: String, taskId: String? = null) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TASK_ID", taskId)
            putExtra("EXTRA_OPEN_ROLE", "CHILD")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (taskId ?: title).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📚 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        try {
            val notificationId = NOTIFICATION_ID_STUDY_REMINDER + ((taskId ?: title).hashCode() % 500).coerceAtLeast(0)
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
