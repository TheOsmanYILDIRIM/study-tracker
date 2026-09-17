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
            appendLine("mat_video = Matematik Video İzleme | 30 dk | https://youtu.be/example")
            appendLine("anki_cog = Coğrafya Anki Tekrarı | 15 dk")
            appendLine("fizik_video = Fizik Video İzleme | 30 dk | https://youtu.be/example")
            appendLine("kitap = Kitap Okuma | 25 dk")
            appendLine()
            appendLine("[GUNLER]")
            appendLine("Pazartesi = mat_video, anki_cog, kitap")
            appendLine("Sali = fizik_video, anki_cog, kitap")
            appendLine("Carsamba = mat_video, anki_cog, kitap")
            appendLine("Persembe = fizik_video, anki_cog, kitap")
            appendLine("Cuma = mat_video, anki_cog, kitap")
            appendLine("Cumartesi = fizik_video, anki_cog, kitap")
            appendLine("Pazar = anki_cog, kitap")
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

    fun buildQuizPrompt(
        topic: String,
        questionCount: Int = 5,
        targetGrade: String = "Ortaokul / Lise / YKS / KPSS",
        date: String? = null,
        durationMinutes: Int = 15,
        userCustomNotes: String = ""
    ): String {
        return buildString {
            appendLine("Sen uzman bir öğretmen ve sınav sorusu hazırlama uzmanısın.")
            appendLine("Amacın: Android StudyTracker uygulaması için '$topic' konusunda şıklı test soruları üretmek.")
            appendLine("Matematik ve fen formüllerini mutlaka standart LaTeX formatında ve '\$...\$' işaretleri arasına alarak yaz (örneğin: '\$\\frac{a}{b}\$', '\$x^2\$', '\$\\sqrt{x}\$', '\$\\sin(x)\$', '\$\\arctan(1) + \\arcsin\\left(-\\frac{1}{2}\\right)\$', '\$\\int_0^1 x dx\$'). Türkçe metinleri ise '\$...\$' dışında tut.")
            appendLine()
            appendLine("ÇIKTI FORMATI KURALLARI (BASİT TEST METİN FORMATI):")
            appendLine("Aşağıdaki tam formatta çıktı üret. JSON kullanma, sadece belirtilen formatı doldur!")
            appendLine()
            appendLine("=== TEST: $topic Testi ===")
            if (!date.isNullOrBlank()) {
                appendLine("TARIH: $date")
            }
            appendLine("SURE: $durationMinutes")
            appendLine("ACIKLAMA: $targetGrade seviyesinde $topic kazanım değerlendirme testi")
            appendLine()
            appendLine("[SORU 1]")
            appendLine("\$f(x) = \\frac{x^2 - 4}{x - 2}\$ fonksiyonunun \$x = 2\$ noktasındaki limiti kaçtır?")
            appendLine("A) \$2\$")
            appendLine("B) \$4\$")
            appendLine("C) \$0\$")
            appendLine("D) Tanımsız")
            appendLine("DOGRU: B")
            appendLine("COZUM: Pay çarpanlarına ayrılırsa \$\\frac{(x-2)(x+2)}{x-2} = x+2\$ olur. \$x=2\$ için limit \$4\$'tür.")
            appendLine()
            appendLine("---")
            appendLine("İstenen Test Özellikleri:")
            appendLine("- Konu: $topic")
            appendLine("- Soru Sayısı: $questionCount")
            appendLine("- Seviye: $targetGrade")
            if (userCustomNotes.isNotBlank()) {
                appendLine("- Ek Notlar / Özel İstek: $userCustomNotes")
            }
            appendLine()
            appendLine("Şimdi hiçbir selamlama veya sohbet metni eklemeden doğrudan yukarıdaki === TEST: ... === formatında $questionCount adet soru yaz:")
        }
    }
}


