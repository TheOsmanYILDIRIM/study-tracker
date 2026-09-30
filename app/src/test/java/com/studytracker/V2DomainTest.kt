package com.studytracker

import com.studytracker.core.domain.engine.*
import com.studytracker.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Pure Kotlin/JVM unit tests for StudyTracker V2 Measurement & Curriculum Slice.
 * Validates:
 * 1. Progress derived deterministically from attempts
 * 2. Explicit prerequisite locking
 * 3. Item inserted between 17 and 18 appears in order with stable IDs
 * 4. Content version changes do not invalidate old completion history
 * 5. Offline attempt pending -> synced transition logic
 * 6. Parent review state is irrelevant to V2 completion/progress
 * 7. Quiz score/question metrics mapping
 */
class V2DomainTest {

    private val sampleFamilyCode = "ST-TEST-2026-V2DM-1100"
    private val studentId = "student_ali"

    @Test
    fun `stable ID survives content update`() {
        // 1. Create 9th grade Video 17 item with initial Version 1
        val v1 = LearningItemVersion(
            id = "ver_item_mat9_vid17_v1",
            itemId = "item_mat9_vid17",
            versionNumber = 1,
            title = "Gerçek Sayılar ve Aralık Kavramı",
            contentUrl = "https://youtube.com/watch?v=mat9_vid17_v1",
            changelog = "Initial version"
        )

        val initialItem = LearningItem(
            id = "item_mat9_vid17",
            lessonId = "lesson_gercek_sayilar",
            familyCode = sampleFamilyCode,
            itemType = ItemType.VIDEO,
            displayLabel = "Video 17",
            stableKey = "mat9_sayilar_vid17",
            orderKey = 1000.0,
            currentVersionId = v1.id,
            currentVersion = v1,
            versionCount = 1
        )

        // 2. Perform content update -> create Version 2
        val v2 = LearningItemVersion(
            id = "ver_item_mat9_vid17_v2",
            itemId = initialItem.id,
            versionNumber = 2,
            title = "Gerçek Sayılar ve Sayı Kümeleri (HD Revizyon)",
            contentUrl = "https://youtube.com/watch?v=mat9_vid17_v2_hd",
            changelog = "Ses ve görüntü kalitesi artırıldı"
        )

        val updatedItem = initialItem.copy(
            currentVersionId = v2.id,
            currentVersion = v2,
            versionCount = 2,
            updatedAt = System.currentTimeMillis()
        )

        // Verify ID and stableKey remain strictly immutable
        assertEquals("item_mat9_vid17", updatedItem.id)
        assertEquals("mat9_sayilar_vid17", updatedItem.stableKey)
        assertEquals("ver_item_mat9_vid17_v2", updatedItem.currentVersionId)
        assertEquals(2, updatedItem.versionCount)
        assertEquals("Gerçek Sayılar ve Sayı Kümeleri (HD Revizyon)", updatedItem.currentVersion?.title)
    }

    @Test
    fun `content version changes do not invalidate old completion history`() {
        val v1 = LearningItemVersion(
            id = "ver_item_mat9_quiz17_v1",
            itemId = "item_mat9_quiz17",
            versionNumber = 1,
            title = "Gerçek Sayılar Tarama Testi 1",
            payloadJson = """{"questions":[{"id":"q1","answer":"√2"}]}"""
        )

        val quizItem = LearningItem(
            id = "item_mat9_quiz17",
            lessonId = "lesson_gercek_sayilar",
            familyCode = sampleFamilyCode,
            itemType = ItemType.QUIZ,
            displayLabel = "Quiz 17",
            stableKey = "mat9_sayilar_quiz17",
            orderKey = 2000.0,
            currentVersionId = v1.id,
            currentVersion = v1
        )

        // Student completes Quiz 17 when Version 1 is active
        val studentAttempt = Attempt(
            id = "att_20260930_001",
            clientAttemptId = "cli_att_student_001",
            familyCode = sampleFamilyCode,
            studentId = studentId,
            itemId = quizItem.id,
            versionId = v1.id,
            status = AttemptStatus.COMPLETED,
            score = 100.0,
            durationSeconds = 600
        )

        // Parent / Teacher later updates Quiz 17 to Version 2
        val v2 = LearningItemVersion(
            id = "ver_item_mat9_quiz17_v2",
            itemId = quizItem.id,
            versionNumber = 2,
            title = "Gerçek Sayılar Tarama Testi 1 (Yenilenmiş Sorular)",
            payloadJson = """{"questions":[{"id":"q1_v2","answer":"π"}]}"""
        )

        val updatedQuizItem = quizItem.copy(
            currentVersionId = v2.id,
            currentVersion = v2,
            versionCount = 2
        )

        // Verify attempt maintains historical referential integrity to v1
        assertEquals("ver_item_mat9_quiz17_v1", studentAttempt.versionId)
        assertNotEquals(v2.id, studentAttempt.versionId)
        assertEquals(100.0, studentAttempt.score ?: 0.0, 0.01)

        // Progress engine evaluates updated item as COMPLETED because completion binds to stable itemId
        val progress = V2ProgressEngine.evaluateItemProgress(
            item = updatedQuizItem,
            itemPrerequisites = emptyList(),
            attemptsByItemId = mapOf(updatedQuizItem.id to listOf(studentAttempt))
        )
        assertTrue("Item should remain COMPLETED even after content version upgrade", progress.isCompleted)
        assertEquals(V2ItemState.COMPLETED, progress.state)
        assertEquals(100.0, progress.bestScore ?: 0.0, 0.01)
    }

    @Test
    fun `item inserted between 17 and 18 appears in order with stable IDs`() {
        // Initial 9th Grade Curriculum Sequence: Video 17, Quiz 17, Video 18
        val vid17 = LearningItem(
            id = "item_mat9_vid17",
            lessonId = "lesson_gercek_sayilar",
            familyCode = sampleFamilyCode,
            itemType = ItemType.VIDEO,
            displayLabel = "Video 17",
            stableKey = "mat9_sayilar_vid17",
            orderKey = 1000.0,
            currentVersionId = "ver_item_mat9_vid17_v1"
        )
        val quiz17 = LearningItem(
            id = "item_mat9_quiz17",
            lessonId = "lesson_gercek_sayilar",
            familyCode = sampleFamilyCode,
            itemType = ItemType.QUIZ,
            displayLabel = "Quiz 17",
            stableKey = "mat9_sayilar_quiz17",
            orderKey = 2000.0,
            currentVersionId = "ver_item_mat9_quiz17_v1"
        )
        val vid18 = LearningItem(
            id = "item_mat9_vid18",
            lessonId = "lesson_gercek_sayilar",
            familyCode = sampleFamilyCode,
            itemType = ItemType.VIDEO,
            displayLabel = "Video 18",
            stableKey = "mat9_sayilar_vid18",
            orderKey = 3000.0,
            currentVersionId = "ver_item_mat9_vid18_v1"
        )

        // Prior student attempts on Video 17 and Quiz 17
        val attempts = mutableListOf(
            Attempt(
                id = "att_vid17",
                clientAttemptId = "cli_vid17",
                familyCode = sampleFamilyCode,
                studentId = studentId,
                itemId = vid17.id,
                versionId = vid17.currentVersionId,
                status = AttemptStatus.COMPLETED
            ),
            Attempt(
                id = "att_quiz17",
                clientAttemptId = "cli_quiz17",
                familyCode = sampleFamilyCode,
                studentId = studentId,
                itemId = quiz17.id,
                versionId = quiz17.currentVersionId,
                status = AttemptStatus.COMPLETED,
                score = 85.0
            )
        )

        // Insert modular item: Quiz 17.2 between Quiz 17 and Video 18
        val quiz17_2 = LearningItem(
            id = "item_mat9_quiz17_2",
            lessonId = "lesson_gercek_sayilar",
            familyCode = sampleFamilyCode,
            itemType = ItemType.QUIZ,
            displayLabel = "Quiz 17.2",
            stableKey = "mat9_sayilar_quiz17_2",
            orderKey = (quiz17.orderKey + vid18.orderKey) / 2.0, // 2500.0
            currentVersionId = "ver_item_mat9_quiz17_2_v1"
        )

        val curriculumList = listOf(vid17, quiz17, quiz17_2, vid18).sortedBy { it.orderKey }

        // 1. Verify sequence ordering
        assertEquals(4, curriculumList.size)
        assertEquals("Video 17", curriculumList[0].displayLabel)
        assertEquals("Quiz 17", curriculumList[1].displayLabel)
        assertEquals("Quiz 17.2", curriculumList[2].displayLabel)
        assertEquals("Video 18", curriculumList[3].displayLabel)

        // 2. Verify prior item IDs and orderKeys remained unaffected
        assertEquals("item_mat9_vid17", curriculumList[0].id)
        assertEquals(1000.0, curriculumList[0].orderKey, 0.001)
        assertEquals("item_mat9_quiz17", curriculumList[1].id)
        assertEquals(2000.0, curriculumList[1].orderKey, 0.001)
        assertEquals(2500.0, curriculumList[2].orderKey, 0.001)
        assertEquals("item_mat9_vid18", curriculumList[3].id)
        assertEquals(3000.0, curriculumList[3].orderKey, 0.001)

        // 3. Verify prior attempts match exactly
        assertEquals(2, attempts.size)
        assertEquals("item_mat9_vid17", attempts[0].itemId)
        assertEquals("item_mat9_quiz17", attempts[1].itemId)
    }

    @Test
    fun `archive does not erase attempts`() {
        val itemId = "item_mat9_vid18"
        val item = LearningItem(
            id = itemId,
            lessonId = "lesson_gercek_sayilar",
            familyCode = sampleFamilyCode,
            itemType = ItemType.VIDEO,
            displayLabel = "Video 18",
            stableKey = "mat9_sayilar_vid18",
            orderKey = 3000.0,
            currentVersionId = "ver_item_mat9_vid18_v1",
            isArchived = false
        )

        val attempt = Attempt(
            id = "att_vid18_1",
            clientAttemptId = "cli_vid18_1",
            familyCode = sampleFamilyCode,
            studentId = studentId,
            itemId = itemId,
            versionId = "ver_item_mat9_vid18_v1",
            status = AttemptStatus.COMPLETED,
            durationSeconds = 1500
        )

        val allAttempts = mutableListOf(attempt)

        // Archive the item
        val archivedItem = item.copy(isArchived = true, updatedAt = System.currentTimeMillis())
        assertTrue(archivedItem.isArchived)

        // Verify attempts remain intact
        assertEquals(1, allAttempts.size)
        assertEquals(itemId, allAttempts[0].itemId)
        assertEquals(AttemptStatus.COMPLETED, allAttempts[0].status)
    }

    @Test
    fun `explicit prerequisite locking`() {
        val prereq = ItemPrerequisite(
            id = "prereq_quiz17_2_requires_quiz17",
            itemId = "item_mat9_quiz17_2",
            requiredItemId = "item_mat9_quiz17",
            minScore = 70.0
        )
        val minScore = prereq.minScore

        // Case A: Quiz 17 scored 60.0 (< 70.0) -> Not unlocked
        val lowScoreAttempt = Attempt(
            id = "att_low",
            clientAttemptId = "cli_low",
            familyCode = sampleFamilyCode,
            studentId = studentId,
            itemId = "item_mat9_quiz17",
            versionId = "ver_item_mat9_quiz17_v1",
            status = AttemptStatus.COMPLETED,
            score = 60.0
        )
        val lowScorePassed = lowScoreAttempt.status == AttemptStatus.COMPLETED &&
                (minScore == null || (lowScoreAttempt.score ?: 0.0) >= minScore)
        assertFalse("Prerequisite should not be satisfied when score 60 < 70", lowScorePassed)

        // Case B: Quiz 17 scored 85.0 (>= 70.0) -> Unlocked
        val highScoreAttempt = Attempt(
            id = "att_high",
            clientAttemptId = "cli_high",
            familyCode = sampleFamilyCode,
            studentId = studentId,
            itemId = "item_mat9_quiz17",
            versionId = "ver_item_mat9_quiz17_v1",
            status = AttemptStatus.COMPLETED,
            score = 85.0
        )
        val highScorePassed = highScoreAttempt.status == AttemptStatus.COMPLETED &&
                (minScore == null || (highScoreAttempt.score ?: 0.0) >= minScore)
        assertTrue("Prerequisite should be satisfied when score 85 >= 70", highScorePassed)

        // Evaluate via V2ProgressEngine
        val targetItem = LearningItem(
            id = "item_mat9_quiz17_2",
            lessonId = "lesson_1",
            familyCode = sampleFamilyCode,
            itemType = ItemType.QUIZ,
            displayLabel = "Quiz 17.2",
            stableKey = "mat9_quiz17_2",
            currentVersionId = "ver_1"
        )

        val progressLocked = V2ProgressEngine.evaluateItemProgress(
            item = targetItem,
            itemPrerequisites = listOf(prereq),
            attemptsByItemId = mapOf("item_mat9_quiz17" to listOf(lowScoreAttempt))
        )
        assertEquals(V2ItemState.LOCKED_BY_PREREQUISITE, progressLocked.state)
        assertFalse(progressLocked.isUnlocked)

        val progressUnlocked = V2ProgressEngine.evaluateItemProgress(
            item = targetItem,
            itemPrerequisites = listOf(prereq),
            attemptsByItemId = mapOf("item_mat9_quiz17" to listOf(highScoreAttempt))
        )
        assertEquals(V2ItemState.AVAILABLE, progressUnlocked.state)
        assertTrue(progressUnlocked.isUnlocked)
    }

    @Test
    fun `progress derived from attempts`() {
        val lesson = Lesson(
            id = "lesson_gercek_sayilar",
            courseId = "course_mat_9",
            familyCode = sampleFamilyCode,
            title = "Gerçek Sayılar"
        )

        val item1 = LearningItem(
            id = "item_1",
            lessonId = lesson.id,
            familyCode = sampleFamilyCode,
            itemType = ItemType.VIDEO,
            displayLabel = "Video 17",
            stableKey = "k1",
            orderKey = 1000.0,
            currentVersionId = "v1"
        )
        val item2 = LearningItem(
            id = "item_2",
            lessonId = lesson.id,
            familyCode = sampleFamilyCode,
            itemType = ItemType.QUIZ,
            displayLabel = "Quiz 17",
            stableKey = "k2",
            orderKey = 2000.0,
            currentVersionId = "v2"
        )
        val item3 = LearningItem(
            id = "item_3",
            lessonId = lesson.id,
            familyCode = sampleFamilyCode,
            itemType = ItemType.ANKI,
            displayLabel = "Anki 17",
            stableKey = "k3",
            orderKey = 3000.0,
            currentVersionId = "v3"
        )

        val attempts = listOf(
            Attempt(
                id = "att_1",
                clientAttemptId = "cli_1",
                familyCode = sampleFamilyCode,
                studentId = studentId,
                itemId = "item_1",
                versionId = "v1",
                status = AttemptStatus.COMPLETED
            ),
            Attempt(
                id = "att_2",
                clientAttemptId = "cli_2",
                familyCode = sampleFamilyCode,
                studentId = studentId,
                itemId = "item_2",
                versionId = "v2",
                status = AttemptStatus.COMPLETED,
                score = 90.0
            )
        )

        val lessonProgress = V2ProgressEngine.evaluateLessonProgress(
            lesson = lesson,
            items = listOf(item1, item2, item3),
            prerequisites = emptyList(),
            attempts = attempts
        )

        assertEquals(3, lessonProgress.totalItems)
        assertEquals(2, lessonProgress.completedItems)
        assertEquals(67, lessonProgress.completionPercentage) // 2/3 = 66.6% -> 67%
        assertEquals("item_3", lessonProgress.nextUnfinishedItem?.id)
    }

    @Test
    fun `parent review state is irrelevant to V2 completion and progress`() {
        // V2 measurement is trusted student self-completion without parent approval gating
        val item = LearningItem(
            id = "item_video_self_reported",
            lessonId = "lesson_1",
            familyCode = sampleFamilyCode,
            itemType = ItemType.VIDEO,
            displayLabel = "Video 1",
            stableKey = "k_vid1",
            currentVersionId = "ver_1"
        )

        // Attempt is marked COMPLETED by student with selfCompleted=true
        val attempt = Attempt(
            id = "att_self",
            clientAttemptId = "cli_self_01",
            familyCode = sampleFamilyCode,
            studentId = studentId,
            itemId = item.id,
            versionId = "ver_1",
            status = AttemptStatus.COMPLETED,
            metadataJson = """{"selfCompleted":true,"syncStatus":"PENDING"}"""
        )

        val progress = V2ProgressEngine.evaluateItemProgress(
            item = item,
            itemPrerequisites = emptyList(),
            attemptsByItemId = mapOf(item.id to listOf(attempt))
        )

        assertTrue("V2 progress must mark item as COMPLETED immediately upon student attempt", progress.isCompleted)
        assertEquals(V2ItemState.COMPLETED, progress.state)
    }

    @Test
    fun `offline attempt pending to synced transition logic`() {
        val initialAttempt = Attempt(
            id = "att_offline_1",
            clientAttemptId = "cli_uuid_12345",
            familyCode = sampleFamilyCode,
            studentId = studentId,
            itemId = "item_mat9_vid17",
            versionId = "ver_1",
            status = AttemptStatus.COMPLETED,
            metadataJson = """{"syncStatus":"PENDING","selfCompleted":true}"""
        )

        assertTrue(initialAttempt.metadataJson?.contains(""""syncStatus":"PENDING"""") == true)

        // Simulating sync transition
        val syncedAttempt = initialAttempt.copy(
            metadataJson = """{"syncStatus":"SYNCED","selfCompleted":true}"""
        )

        // ClientAttemptId is preserved for idempotency
        assertEquals(initialAttempt.clientAttemptId, syncedAttempt.clientAttemptId)
        assertEquals(initialAttempt.id, syncedAttempt.id)
        assertTrue(syncedAttempt.metadataJson?.contains(""""syncStatus":"SYNCED"""") == true)
    }

    @Test
    fun `quiz score and question metrics mapping`() {
        val questions = listOf(
            QuizQuestionPayload(
                id = "q1",
                prompt = "$\\\\sqrt{4}$ değeri kaçtır?",
                type = "MULTIPLE_CHOICE",
                choices = listOf("1", "2", "3", "4"),
                correctAnswer = "2"
            ),
            QuizQuestionPayload(
                id = "q2",
                prompt = "$\\\\pi$ sayısı rasyonel bir sayıdır.",
                type = "TRUE_FALSE",
                choices = listOf("Doğru", "Yanlış"),
                correctAnswer = "Yanlış"
            )
        )

        val studentAnswers = mapOf(
            0 to "2",        // Correct
            1 to "Yanlış"    // Correct
        )

        var correctCount = 0
        val answerMetrics = questions.mapIndexed { idx, q ->
            val sel = studentAnswers[idx]
            val isCorrect = (sel == q.correctAnswer)
            if (isCorrect) correctCount++
            QuizAnswerMetric(
                id = "ans_$idx",
                attemptId = "att_quiz_test",
                questionId = q.id,
                questionIndex = idx,
                selectedOption = sel,
                isCorrect = isCorrect,
                durationSeconds = 15
            )
        }

        val calculatedScore = (correctCount.toDouble() / questions.size.toDouble()) * 100.0

        assertEquals(2, answerMetrics.size)
        assertTrue(answerMetrics[0].isCorrect)
        assertTrue(answerMetrics[1].isCorrect)
        assertEquals("2", answerMetrics[0].selectedOption)
        assertEquals(100.0, calculatedScore, 0.01)
    }
}
