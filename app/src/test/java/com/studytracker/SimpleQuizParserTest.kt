package com.studytracker

import com.studytracker.core.data.plan_engine.SimpleQuizParser
import org.junit.Assert.*
import org.junit.Test

class SimpleQuizParserTest {

    @Test
    fun `parse valid quiz DSL with LaTeX formulas successfully`() {
        val quizText = """
=== TEST: Matematik - Trigonometri 1 ===
TARIH: 2026-09-17
SURE: 20
ACIKLAMA: 11. Sınıf Trigonometrik Özdeşlikler

[SORU 1]
f(x) = \frac{\sin(2x)}{\cos(x)} ifadesinin en sade hali nedir?
A) 2\sin(x)
B) 2\cos(x)
C) \tan(x)
D) \cot(x)
DOGRU: A
COZUM: \sin(2x) = 2\sin(x)\cos(x) olduğundan paydadaki cos(x) sadeleşir.

[SORU 2]
\int_0^2 (3x^2 - 2x + 1) dx integralinin sonucu kaçtır?
A) 4
B) 5
C) 6
D) 8
DOGRU: C
COZUM: [x^3 - x^2 + x]_0^2 = 8 - 4 + 2 = 6.
=== TEST_SONU ===
        """.trimIndent()

        val quizzes = SimpleQuizParser.parse(quizText)
        assertEquals(1, quizzes.size)

        val quiz = quizzes.first()
        assertEquals("Matematik - Trigonometri 1", quiz.title)
        assertEquals("2026-09-17", quiz.date)
        assertEquals(20, quiz.durationMinutes)
        assertEquals(2, quiz.questions.size)

        // Soru 1
        val q1 = quiz.questions[0]
        assertEquals(1, q1.questionNumber)
        assertTrue(q1.text.contains("\\frac{\\sin(2x)}{\\cos(x)}"))
        assertEquals(4, q1.options.size)
        assertEquals("A", q1.correctOption)
        assertEquals("2\\sin(x)", q1.options[0].text)
        assertNotNull(q1.solutionExplanation)

        // Soru 2
        val q2 = quiz.questions[1]
        assertEquals(2, q2.questionNumber)
        assertTrue(q2.text.contains("\\int_0^2"))
        assertEquals("C", q2.correctOption)
    }

    @Test
    fun `export and re-parse quiz maintains fidelity`() {
        val originalText = """
=== TEST: Fizik - Optik ===
SURE: 15

[SORU 1]
Odak uzaklığı f = 10\text{ cm} olan çukur aynada cisim 20\text{ cm} uzaktadır.
A) 10\text{ cm}
B) 20\text{ cm}
C) 30\text{ cm}
D) 40\text{ cm}
DOGRU: B
COZUM: Cisim merkezde (2f) ise görüntü merkezde ve ters oluşur.
=== TEST_SONU ===
        """.trimIndent()

        val parsed = SimpleQuizParser.parse(originalText)
        assertEquals(1, parsed.size)

        val exported = SimpleQuizParser.exportToSimpleText(parsed.first())
        val reParsed = SimpleQuizParser.parse(exported)

        assertEquals("B", reParsed.first().questions.first().correctOption)
    }

    @Test
    fun `formatLatexToNativeMath properly transforms LaTeX equations and options`() {
        val rawQuestion = "\\arctan(1) + \\arcsin\\left(-\\frac{1}{2}\\right)\nifadesinin değeri kaçtır?"
        val formatted = com.studytracker.core.ui.components.formatLatexToNativeMath(rawQuestion)

        assertTrue(formatted.contains("arctan(1) + arcsin(-1/2)"))
        assertTrue(formatted.contains("ifadesinin değeri kaçtır?"))

        val optionText = "\\frac{\\pi}{12}"
        val formattedOption = com.studytracker.core.ui.components.formatLatexToNativeMath(optionText)
        assertEquals("π/12", formattedOption)

        val powerText = "x^2 + 2x - 3 = 0"
        val formattedPower = com.studytracker.core.ui.components.formatLatexToNativeMath(powerText)
        assertEquals("x² + 2x - 3 = 0", formattedPower)
    }
}


