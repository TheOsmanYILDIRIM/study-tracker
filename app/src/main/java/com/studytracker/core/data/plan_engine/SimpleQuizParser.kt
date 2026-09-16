package com.studytracker.core.data.plan_engine

import com.studytracker.core.domain.model.Quiz
import com.studytracker.core.domain.model.QuizOption
import com.studytracker.core.domain.model.QuizQuestion
import java.util.UUID

object SimpleQuizParser {

    /**
     * Basit ve toleranslı metin ayrıştırıcı (DSL).
     * AI (ChatGPT/Claude/Gemini) çıktılarını, markdown bloklarını veya kopyalanan test metinlerini
     * hatasız bir şekilde Quiz nesnelerine dönüştürür.
     */
    fun parse(rawText: String): List<Quiz> {
        val cleanText = rawText
            .replace(Regex("""```[a-zA-Z]*"""), "")
            .replace("```", "")

        val lines = cleanText.lines().map { it.trim() }
        val quizzes = mutableListOf<Quiz>()

        var currentTitle = "Test / Soru Çözümü"
        var currentDate: String? = null
        var currentDurationMinutes = 15
        var currentDescription: String? = null
        var currentQuestions = mutableListOf<QuizQuestion>()

        var inQuiz = false
        var currentQuestionNumber = 0
        var currentQuestionTextLines = mutableListOf<String>()
        var currentOptions = mutableListOf<QuizOption>()
        var currentCorrectOption = ""
        var currentExplanation = ""
        var inQuestion = false

        fun flushQuestion() {
            if (inQuestion && (currentQuestionTextLines.isNotEmpty() || currentOptions.isNotEmpty())) {
                val qText = currentQuestionTextLines.joinToString("\n").trim()
                if (qText.isNotBlank() || currentOptions.isNotEmpty()) {
                    currentQuestionNumber++
                    val qId = "q_${currentQuestionNumber}_${UUID.randomUUID().toString().take(6)}"
                    currentQuestions.add(
                        QuizQuestion(
                            questionId = qId,
                            questionNumber = currentQuestionNumber,
                            text = qText,
                            options = currentOptions.toList(),
                            correctOption = currentCorrectOption.trim().uppercase(),
                            solutionExplanation = currentExplanation.trim().ifBlank { null }
                        )
                    )
                }
            }
            currentQuestionTextLines.clear()
            currentOptions.clear()
            currentCorrectOption = ""
            currentExplanation = ""
            inQuestion = false
        }

        fun flushQuiz() {
            flushQuestion()
            if (currentQuestions.isNotEmpty()) {
                val quizId = "quiz_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
                quizzes.add(
                    Quiz(
                        quizId = quizId,
                        title = currentTitle.trim(),
                        description = currentDescription?.trim(),
                        date = currentDate,
                        durationMinutes = currentDurationMinutes,
                        questions = currentQuestions.toList()
                    )
                )
            }
            currentTitle = "Test / Soru Çözümü"
            currentDate = null
            currentDurationMinutes = 15
            currentDescription = null
            currentQuestions = mutableListOf()
            currentQuestionNumber = 0
            inQuiz = false
        }

        for (line in lines) {
            val upper = line.uppercase()

            if (upper.startsWith("=== TEST:") || upper.startsWith("===TEST:") || upper.startsWith("### TEST:")) {
                if (inQuiz || currentQuestions.isNotEmpty()) {
                    flushQuiz()
                }
                inQuiz = true
                val titlePart = line.substringAfter(":").replace("===", "").replace("###", "").trim()
                if (titlePart.isNotBlank()) {
                    currentTitle = titlePart
                }
                continue
            }

            if (upper == "=== TEST_SONU ===" || upper == "===TEST_SONU===" || upper == "=== TEST SONU ===") {
                flushQuiz()
                continue
            }

            if (upper.startsWith("TARIH:") || upper.startsWith("TARİH:") || upper.startsWith("DATE:")) {
                currentDate = line.substringAfter(":").trim().filter { it.isDigit() || it == '-' }
                continue
            }

            if (upper.startsWith("SURE:") || upper.startsWith("SÜRE:") || upper.startsWith("DURATION:")) {
                val digits = line.substringAfter(":").trim().filter { it.isDigit() }
                currentDurationMinutes = digits.toIntOrNull() ?: 15
                continue
            }

            if (upper.startsWith("ACIKLAMA:") || upper.startsWith("AÇIKLAMA:") || upper.startsWith("DESC:")) {
                currentDescription = line.substringAfter(":").trim()
                continue
            }

            // Soru Başlangıcı: [SORU 1], [SORU 2], SORU 1:, 1. Soru, vb.
            val isQuestionHeader = (line.startsWith("[") && upper.contains("SORU")) ||
                    upper.startsWith("SORU ") ||
                    Regex("""^(\d+)[\.\)]\s*Soru""", RegexOption.IGNORE_CASE).containsMatchIn(line)

            if (isQuestionHeader) {
                flushQuestion()
                inQuestion = true
                inQuiz = true
                continue
            }

            if (upper.startsWith("DOGRU:") || upper.startsWith("DOĞRU:") || upper.startsWith("CORRECT:") || upper.startsWith("CEVAP:")) {
                currentCorrectOption = line.substringAfter(":").trim().take(1).uppercase()
                continue
            }

            if (upper.startsWith("COZUM:") || upper.startsWith("ÇÖZÜM:") || upper.startsWith("EXPLANATION:")) {
                currentExplanation = line.substringAfter(":").trim()
                continue
            }

            // Şıklar: A) ..., B) ..., C) ..., D) ..., E) ... veya A- ..., B. ...
            val optionMatch = Regex("""^([A-Ea-e])[\)\.\-]\s*(.*)$""").find(line)
            if (optionMatch != null && inQuestion) {
                val key = optionMatch.groupValues[1].uppercase()
                val optText = optionMatch.groupValues[2].trim()
                currentOptions.add(QuizOption(key = key, text = optText))
                continue
            }

            // Eğer sorunun içindeysek ve henüz şıklar başlamadıysa soru metnine ekle
            if (inQuestion) {
                if (currentOptions.isEmpty()) {
                    if (line.isNotBlank()) {
                        currentQuestionTextLines.add(line)
                    }
                } else if (currentExplanation.isNotBlank()) {
                    currentExplanation += "\n$line"
                }
            } else if (line.isNotBlank() && !line.startsWith("#") && !line.startsWith("//")) {
                // Soru başlığı olmadan doğrudan soru başladıysa
                inQuestion = true
                inQuiz = true
                currentQuestionTextLines.add(line)
            }
        }

        flushQuiz()

        return quizzes
    }

    /**
     * Bir Quiz nesnesini okunabilir ve kopyalanabilir basit metin formatına (DSL) dönüştürür.
     */
    fun exportToSimpleText(quiz: Quiz): String {
        return buildString {
            appendLine("=== TEST: ${quiz.title} ===")
            if (!quiz.date.isNullOrBlank()) {
                appendLine("TARIH: ${quiz.date}")
            }
            appendLine("SURE: ${quiz.durationMinutes}")
            if (!quiz.description.isNullOrBlank()) {
                appendLine("ACIKLAMA: ${quiz.description}")
            }
            appendLine()

            for ((idx, q) in quiz.questions.withIndex()) {
                appendLine("[SORU ${idx + 1}]")
                appendLine(q.text)
                for (opt in q.options) {
                    appendLine("${opt.key}) ${opt.text}")
                }
                if (q.correctOption.isNotBlank()) {
                    appendLine("DOGRU: ${q.correctOption}")
                }
                if (!q.solutionExplanation.isNullOrBlank()) {
                    appendLine("COZUM: ${q.solutionExplanation}")
                }
                appendLine()
            }
            appendLine("=== TEST_SONU ===")
        }
    }
}
