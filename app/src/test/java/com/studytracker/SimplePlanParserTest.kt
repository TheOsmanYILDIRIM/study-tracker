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
}
