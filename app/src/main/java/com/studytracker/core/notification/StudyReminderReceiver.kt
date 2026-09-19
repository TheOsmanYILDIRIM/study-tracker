package com.studytracker.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager
import com.studytracker.core.domain.model.OccurrenceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StudyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "StudyReminderReceiver tetiklendi, action=$action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = AppPreferences.getInstance(context)
                if (!prefs.isNotificationsEnabled.value) {
                    Log.d(TAG, "Bildirimler kullanıcı tarafından kapatılmış, atlanıyor.")
                    return@launch
                }

                // 1. Cloudflare KV üzerinden veliden gelen yeni mesajları kontrol et ve bildir
                try {
                    CloudflareSyncManager.checkAndDeliverPendingNotifications(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Bulut bildirim kontrolü hatası: ${e.message}", e)
                }

                // 2. Günlük ders hatırlatıcısı kontrolü (Öğrenci rolünde ve akşam 16:00 - 21:30 arası)
                checkDailyStudyReminder(context, prefs)

            } catch (e: Exception) {
                Log.e(TAG, "Arkaplan hatırlatıcı döngüsü hatası: ${e.message}", e)
            } finally {
                // 3. Her durumda sonraki 5 dakikalık kontrolü planla
                StudyReminderScheduler.scheduleNextCheck(context)
                pendingResult.finish()
            }
        }
    }

    private suspend fun checkDailyStudyReminder(context: Context, prefs: AppPreferences) {
        try {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            // Sadece akşam çalışma saatlerinde (16:00 - 21:00) nazik bir ders hatırlatıcısı ver
            if (hour in 16..21) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (prefs.lastStudyReminderDate != todayStr) {
                    val db = AppDatabase.getInstance(context)
                    val todayOccurrences = db.occurrenceDao().getOccurrencesByDate(todayStr)
                    val pendingCount = todayOccurrences.count {
                        it.status == OccurrenceStatus.PENDING || it.status == OccurrenceStatus.REJECTED
                    }

                    if (pendingCount > 0) {
                        StudyNotificationManager.showStudyReminderNotification(
                            context = context,
                            title = "Bugünkü Ders Vakti Geldi!",
                            message = "Bugün tamamlanmayı bekleyen $pendingCount dersin var. Kısa bir seansla başlamak ister misin? 🎯",
                            taskId = todayOccurrences.firstOrNull()?.taskId
                        )
                        prefs.lastStudyReminderDate = todayStr
                        Log.d(TAG, "Günlük ders hatırlatıcısı gönderildi ($pendingCount ders)")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkDailyStudyReminder hatası: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "StudyReminderReceiver"
    }
}
