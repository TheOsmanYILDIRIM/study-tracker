package com.studytracker

import com.studytracker.core.data.plan_engine.SimplePlanParser
import com.studytracker.core.data.plan_engine.ValidationResult
import com.studytracker.core.domain.model.TaskKind
import org.junit.Assert.*
import org.junit.Test

class SimplePlanParserTest {

    @Test
    fun `parse valid simple plan text successfully`() {
        val sampleText = """
            HAFTA = 2026-W38
            BASLANGIC = 2026-09-14
            OGRENCI = child_1

            [DERSLER]
            mat = Matematik | 40 dk
            turkce = Türkçe | 30 dk
            fen = Fen Bilimleri | 35 dk
            kitap = Kitap Okuma | 20 dk

            [GUNLER]
            Pazartesi = mat, turkce, kitap
            Sali = fen, kitap
            Carsamba = mat, fen, kitap

            [HAFTALIK]
            deneme = Hafta Sonu Deneme | 90 dk
        """.trimIndent()

        val (plan, result) = SimplePlanParser.parse(sampleText)

        assertNotNull(plan)
        assertTrue(result is ValidationResult.Valid)
        assertEquals("2026-W38", plan?.weekId)
        assertEquals("2026-09-14", plan?.weekStartDate)
        assertEquals(5, plan?.tasks?.size) // mat, turkce, fen, kitap, deneme
        assertEquals(8, plan?.dailyOccurrences?.size) // 3 on Mon, 2 on Tue, 3 on Wed
        assertEquals(1, plan?.weeklyOccurrences?.size)

        // Verify occurrences
        val mondayTasks = plan?.dailyOccurrences?.filter { it.date == "2026-09-14" }
        assertEquals(3, mondayTasks?.size)
        assertEquals("Matematik", mondayTasks?.find { it.taskId == "mat" }?.title)
        assertEquals(40, mondayTasks?.find { it.taskId == "mat" }?.plannedMinutes)

        val tuesdayTasks = plan?.dailyOccurrences?.filter { it.date == "2026-09-15" }
        assertEquals(2, tuesdayTasks?.size)
    }

    @Test
    fun `export and re-import preserves plan semantics`() {
        val originalText = """
            HAFTA = 2026-W38
            BASLANGIC = 2026-09-14
            OGRENCI = child_1

            [DERSLER]
            mat = Matematik | 40 dk
            turkce = Türkçe | 30 dk

            [GUNLER]
            Pazartesi = mat, turkce

            [HAFTALIK]
            deneme = Deneme | 90 dk
        """.trimIndent()

        val (plan1, _) = SimplePlanParser.parse(originalText)
        assertNotNull(plan1)

        val exported = SimplePlanParser.exportToSimpleText(plan1!!)
        assertTrue(exported.contains("HAFTA = 2026-W38"))
        assertTrue(exported.contains("mat = Matematik | 40 dk"))

        val (plan2, result2) = SimplePlanParser.parse(exported)
        assertTrue(result2 is ValidationResult.Valid)
        assertEquals(plan1.weekId, plan2?.weekId)
        assertEquals(plan1.dailyOccurrences.size, plan2?.dailyOccurrences?.size)
    }

    @Test
    fun `parse complex text with preamble and prompt template successfully without duplicate errors`() {
        val userPastedText = """
            Sen uzman bir ders çalışma planı hazırlayıcısısın.
            Amacın: Android StudyTracker uygulaması için haftalık ders çalışma planı üretmek.

            ÇIKTI FORMATI KURALLARI (AŞIRI BASİT DEĞİŞKEN FORMATI):
            Aşağıdaki basit, temiz değişken formatında çıktı üret. JSON veya Markdown kod blokları kullanma!

            Örnek Çıktı Şablonu:
            HAFTA = 2026-W38
            BASLANGIC = 2026-09-14
            OGRENCI = child_1

            [DERSLER]
            mat = Matematik | 40 dk | Soru Çözümü
            turkce = Türkçe | 30 dk | Paragraf ve Dil Bilgisi
            fen = Fen Bilimleri | 35 dk | Konu Tekrarı
            kitap = Kitap Okuma | 20 dk | Günlük 20 Sayfa

            [GUNLER]
            Pazartesi = mat, turkce, kitap
            Sali = fen, turkce, kitap
            Carsamba = mat, fen, kitap
            Persembe = turkce, fen, kitap
            Cuma = mat, turkce, kitap
            Cumartesi = fen, mat, kitap
            Pazar = kitap

            [HAFTALIK]
            deneme = Hafta Sonu Deneme Sınavı | 90 dk | Genel Tekrar Denemesi

            ---
            Hedef Hafta Bilgisi:
            Hafta: 2026-W38
            Başlangıç Tarihi: 2026-09-14
            Öğrenci: child_1

            Mevcut Plan Metni:
            null

            Kullanıcı Özel İsteği:
            HAFTA = 2026-W38
            BASLANGIC = 2026-09-14
            OGRENCI = child_1
            [DERSLER]
            mat = Matematik | 40 dk | Konu Tekrarı ve Soru Çözümü
            turkce = Türkçe | 35 dk | Paragraf ve Dil Bilgisi
            fen = Fen Bilimleri | 35 dk | Konu Tekrarı ve Test
            sosyal = Sosyal Bilgiler | 30 dk | Kavram Tekrarı ve Soru Çözümü
            ingilizce = İngilizce | 25 dk | Kelime Ezberi ve Alıştırma
            kitap = Kitap Okuma | 20 dk | Serbest Okuma
            [GUNLER]
            Pazartesi = mat, turkce, kitap
            Sali = fen, ingilizce, kitap
            Carsamba = mat, sosyal, kitap
            Persembe = turkce, fen, kitap
            Cuma = mat, ingilizce, kitap
            Cumartesi = fen, sosyal, kitap
            Pazar = kitap
            [HAFTALIK]
            deneme = Hafta Sonu Deneme Sınavı | 90 dk | Tüm Dersler Genel Değerlendirme Denemesi


            Şimdi hiçbir sohbet metni eklemeden doğrudan yukarıdaki [DERSLER], [GUNLER] ve [HAFTALIK] formatında planı yaz:
        """.trimIndent()

        val (plan, result) = com.studytracker.core.data.plan_engine.PlanValidator.parseAndValidate(userPastedText)

        assertNotNull(plan)
        assertTrue("Validation should pass but was: $result", result is ValidationResult.Valid)
        assertEquals("2026-W38", plan?.weekId)
        assertEquals("2026-09-14", plan?.weekStartDate)
        assertTrue((plan?.dailyOccurrences?.size ?: 0) > 0)
    }

    @Test
    fun `parse clean user plan snippet with Turkish day names and pipe descriptions`() {
        val cleanSnippet = """
            HAFTA = 2026-W38
            BASLANGIC = 2026-09-14
            OGRENCI = child_1
            [DERSLER]
            mat = Matematik | 40 dk | Konu Tekrarı ve Soru Çözümü
            turkce = Türkçe | 35 dk | Paragraf ve Dil Bilgisi
            fen = Fen Bilimleri | 35 dk | Konu Tekrarı ve Test
            sosyal = Sosyal Bilgiler | 30 dk | Kavram Tekrarı ve Soru Çözümü
            ingilizce = İngilizce | 25 dk | Kelime Ezberi ve Alıştırma
            kitap = Kitap Okuma | 20 dk | Serbest Okuma
            [GUNLER]
            Pazartesi = mat, turkce, kitap
            Sali = fen, ingilizce, kitap
            Carsamba = mat, sosyal, kitap
            Persembe = turkce, fen, kitap
            Cuma = mat, ingilizce, kitap
            Cumartesi = fen, sosyal, kitap
            Pazar = kitap
            [HAFTALIK]
            deneme = Hafta Sonu Deneme Sınavı | 90 dk | Tüm Dersler Genel Değerlendirme Denemesi
        """.trimIndent()

        val (plan, result) = com.studytracker.core.data.plan_engine.PlanValidator.parseAndValidate(cleanSnippet)

        assertNotNull(plan)
        assertTrue("Validation should pass: $result", result is ValidationResult.Valid)
        assertEquals("2026-W38", plan?.weekId)
        assertEquals("2026-09-14", plan?.weekStartDate)
        assertEquals(7, plan?.tasks?.size)
        assertEquals(19, plan?.dailyOccurrences?.size)
        assertEquals(1, plan?.weeklyOccurrences?.size)
    }
}
