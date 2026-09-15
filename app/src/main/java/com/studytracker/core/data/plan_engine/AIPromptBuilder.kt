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
        val simplePlanText = if (currentPlan != null) SimplePlanParser.exportToSimpleText(currentPlan) else "null"

        return buildString {
            if (isRevision) {
                appendLine("Sen mevcut bir haftalık ders çalışma planını güncelleyen bir asistansın.")
                appendLine("ÖNEMLİ KURAL: Bu aynı haftanın revize planıdır. Tamamlanmış (approved) ve aktif görevleri KORU.")
            } else {
                appendLine("Sen uzman bir ders çalışma planı hazırlayıcısısın.")
                appendLine("Amacın: Android StudyTracker uygulaması için haftalık ders çalışma planı üretmek.")
            }
            appendLine()
            appendLine("ÇIKTI FORMATI KURALLARI (AŞIRI BASİT DEĞİŞKEN FORMATI):")
            appendLine("Aşağıdaki basit, temiz değişken formatında çıktı üret. JSON veya Markdown kod blokları kullanma!")
            appendLine()
            appendLine("Örnek Çıktı Şablonu:")
            appendLine("HAFTA = $targetWeekId")
            appendLine("BASLANGIC = $targetWeekStartDate")
            appendLine("OGRENCI = $childId")
            appendLine()
            appendLine("[DERSLER]")
            appendLine("mat = Matematik | 40 dk | Soru Çözümü")
            appendLine("turkce = Türkçe | 30 dk | Paragraf ve Dil Bilgisi")
            appendLine("fen = Fen Bilimleri | 35 dk | Konu Tekrarı")
            appendLine("kitap = Kitap Okuma | 20 dk | Günlük 20 Sayfa")
            appendLine()
            appendLine("[GUNLER]")
            appendLine("Pazartesi = mat, turkce, kitap")
            appendLine("Sali = fen, turkce, kitap")
            appendLine("Carsamba = mat, fen, kitap")
            appendLine("Persembe = turkce, fen, kitap")
            appendLine("Cuma = mat, turkce, kitap")
            appendLine("Cumartesi = fen, mat, kitap")
            appendLine("Pazar = kitap")
            appendLine()
            appendLine("[HAFTALIK]")
            appendLine("deneme = Hafta Sonu Deneme Sınavı | 90 dk | Genel Tekrar Denemesi")
            appendLine()
            appendLine("---")
            appendLine("Hedef Hafta Bilgisi:")
            appendLine("Hafta: $targetWeekId")
            appendLine("Başlangıç Tarihi: $targetWeekStartDate")
            appendLine("Öğrenci: $childId")
            appendLine()
            appendLine("Mevcut Plan Metni:")
            appendLine(simplePlanText)
            appendLine()
            appendLine("Kullanıcı Özel İsteği:")
            appendLine(if (userCustomRequest.isNotBlank()) userCustomRequest else "Standart dengeli haftalık ders programı oluştur.")
            appendLine()
            appendLine("Şimdi hiçbir sohbet metni eklemeden doğrudan yukarıdaki [DERSLER], [GUNLER] ve [HAFTALIK] formatında planı yaz:")
        }
    }
}
