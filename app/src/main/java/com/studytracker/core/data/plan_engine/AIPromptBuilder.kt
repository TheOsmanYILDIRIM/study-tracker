package com.studytracker.core.data.plan_engine

import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.Plan
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AIPromptBuilder {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    fun buildPrompt(
        currentPlan: Plan?,
        allOccurrences: List<Occurrence>,
        targetWeekId: String,
        targetWeekStartDate: String,
        childId: String,
        userCustomRequest: String
    ): String {
        val completedKeys = allOccurrences
            .filter { it.status == OccurrenceStatus.APPROVED }
            .map { it.occurrenceKey }

        val activeKeys = allOccurrences
            .filter { it.status == OccurrenceStatus.ACTIVE }
            .map { it.occurrenceKey }

        val waitingReviewKeys = allOccurrences
            .filter { it.status == OccurrenceStatus.WAITING_REVIEW }
            .map { it.occurrenceKey }

        val currentPlanJsonString = if (currentPlan != null) {
            json.encodeToString(currentPlan)
        } else {
            "null (Yeni plan oluşturulacak)"
        }

        val completedKeysJson = json.encodeToString(completedKeys)
        val activeKeysJson = json.encodeToString(activeKeys)
        val waitingReviewKeysJson = json.encodeToString(waitingReviewKeys)

        val isRevision = currentPlan != null && currentPlan.weekId == targetWeekId

        return buildString {
            if (isRevision) {
                appendLine("Sen mevcut bir haftalık ders çalışma planını güncelleyen bir asistansın.")
                appendLine()
                appendLine("ÖNEMLİ KURAL: Bu aynı haftanın revize planıdır. Tamamlanmış (approved), aktif veya inceleme bekleyen görevlerin taskId ve occurrenceKey değerlerini KESİNLİKLE DEĞİŞTİRME ve silme!")
            } else {
                appendLine("Sen bir ders çalışma planı üreticisisin.")
                appendLine("Amacın: Android StudyTracker uygulamasına içe aktarılacak haftalık plan üretmek.")
            }
            appendLine()
            appendLine("Çıktı Kuralları:")
            appendLine("1. Sadece geçerli JSON döndür.")
            appendLine("2. Markdown (```json gibi) KULLANMA.")
            appendLine("3. Açıklama veya sohbet metni YAZMA.")
            appendLine("4. Kod bloğu veya yorum satırı KULLANMA.")
            appendLine("5. Tüm alan adları İngilizce olmalıdır.")
            appendLine("6. Eksik bilgi varsa mantıklı varsayılan değer koy.")
            appendLine("7. Günlük görev kind: 'daily', haftalık görev kind: 'weekly' olmalıdır.")
            appendLine("8. Günlük occurrenceKey formatı: '{taskId}:{YYYY-MM-DD}'")
            appendLine("9. Haftalık occurrenceKey formatı: '{taskId}:{YYYY-Www}'")
            appendLine()
            appendLine("Mevcut Plan JSON:")
            appendLine(currentPlanJsonString)
            appendLine()
            appendLine("Tamamlanan (Approved) occurrenceKey Listesi:")
            appendLine(completedKeysJson)
            appendLine()
            appendLine("Aktif Session occurrenceKey Listesi:")
            appendLine(activeKeysJson)
            appendLine()
            appendLine("Onay Bekleyen (Waiting Review) occurrenceKey Listesi:")
            appendLine(waitingReviewKeysJson)
            appendLine()
            appendLine("Hedef Hafta Bilgisi:")
            appendLine("WeekId: $targetWeekId")
            appendLine("WeekStartDate: $targetWeekStartDate")
            appendLine("ChildId: $childId")
            appendLine()
            appendLine("Kullanıcı Özel İsteği:")
            appendLine(if (userCustomRequest.isNotBlank()) userCustomRequest else "Standart dengeli haftalık ders programı oluştur.")
            appendLine()
            appendLine("Şimdi sadece geçerli JSON döndür.")
        }
    }
}
