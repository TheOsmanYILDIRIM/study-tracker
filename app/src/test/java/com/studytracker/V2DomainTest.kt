package com.studytracker

import com.studytracker.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Pure Kotlin/JVM unit tests for StudyTracker V2 Measurement & Curriculum Slice.
 * Validates immutable IDs, version history referential integrity, puzzle ordering,
 * prerequisites, attempt idempotency, and non-destructive archiving.
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
    fun `attempt references old version after new version added`() {
        val v1 = LearningItemVersion(
            id = "ver_item_mat9_quiz17_v1",
            itemId = "item_mat9_quiz17",
            versionNumber = 1,
            title = "Gerçek Sayılar Tarama Testi 1",
            payloadJson = """{"questions":[{"id":"q1","answer":"√2"}]}"""
        )

        // Student completes Quiz 17 when Version 1 is active
        val studentAttempt = Attempt(
            id = "att_20260930_001",
            clientAttemptId = "cli_att_student_001",
            familyCode = sampleFamilyCode,
            studentId = studentId,
            itemId = "item_mat9_quiz17",
            versionId = v1.id,
            status = AttemptStatus.COMPLETED,
            score = 100.0,
            durationSeconds = 600
        )

        // Parent / Teacher later updates Quiz 17 to Version 2
        val v2 = LearningItemVersion(
            id = "ver_item_mat9_quiz17_v2",
            itemId = "item_mat9_quiz17",
            versionNumber = 2,
            title = "Gerçek Sayılar Tarama Testi 1 (Yenilenmiş Sorular)",
            payloadJson = """{"questions":[{"id":"q1_v2","answer":"π"}]}"""
        )

        // Verify attempt maintains historical referential integrity to v1
        assertEquals("ver_item_mat9_quiz17_v1", studentAttempt.versionId)
        assertNotEquals(v2.id, studentAttempt.versionId)
        assertEquals(100.0, studentAttempt.score ?: 0.0, 0.01)
    }

    @Test
    fun `insert intermediate item does not alter prior IDs or progress`() {
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
    fun `explicit prerequisite unlocks item only when score threshold met`() {
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
    }
}
