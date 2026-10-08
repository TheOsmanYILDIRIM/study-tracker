package com.studytracker.core.data.local.repository

import android.util.Log
import androidx.room.withTransaction
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.*
import com.studytracker.core.data.remote.v2.*
import com.studytracker.core.domain.model.*
import com.studytracker.core.domain.repository.V2AttemptRepository
import com.studytracker.core.domain.repository.V2CurriculumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

private const val TAG = "V2Repository"

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    coerceInputValues = true
}

// --- Entity <-> Domain Mappings ---

private fun parseCourseVisual(raw: String?): CourseVisual {
    if (raw.isNullOrBlank()) return CourseVisual()
    return runCatching {
        json.decodeFromString(CourseVisual.serializer(), raw)
    }.getOrDefault(CourseVisual())
}

fun CourseEntity.toDomain() = Course(
    id = id,
    familyCode = familyCode,
    title = title,
    subject = subject,
    gradeLevel = gradeLevel,
    description = description ?: "",
    visual = parseCourseVisual(visualJson),
    orderKey = orderKey,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Course.toEntity() = CourseEntity(
    id = id,
    familyCode = familyCode,
    title = title,
    subject = subject,
    gradeLevel = gradeLevel,
    description = description,
    visualJson = json.encodeToString(CourseVisual.serializer(), visual),
    orderKey = orderKey,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun LessonEntity.toDomain() = Lesson(
    id = id,
    courseId = courseId,
    familyCode = familyCode,
    title = title,
    orderKey = orderKey,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Lesson.toEntity() = LessonEntity(
    id = id,
    courseId = courseId,
    familyCode = familyCode,
    title = title,
    orderKey = orderKey,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun LearningItemEntity.toDomain(
    currentVersion: LearningItemVersion? = null,
    versionCount: Int = 1,
    prerequisites: List<ItemPrerequisite> = emptyList()
) = LearningItem(
    id = id,
    lessonId = lessonId,
    familyCode = familyCode,
    itemType = itemType,
    displayLabel = displayLabel,
    stableKey = stableKey,
    orderKey = orderKey,
    currentVersionId = currentVersionId,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    currentVersion = currentVersion,
    versionCount = versionCount,
    prerequisites = prerequisites
)

fun LearningItem.toEntity() = LearningItemEntity(
    id = id,
    lessonId = lessonId,
    familyCode = familyCode,
    itemType = itemType,
    displayLabel = displayLabel,
    stableKey = stableKey,
    orderKey = orderKey,
    currentVersionId = currentVersionId,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun LearningItemVersionEntity.toDomain() = LearningItemVersion(
    id = id,
    itemId = itemId,
    versionNumber = versionNumber,
    title = title,
    contentUrl = contentUrl,
    payloadJson = payloadJson,
    changelog = changelog ?: "",
    createdAt = createdAt
)

fun LearningItemVersion.toEntity() = LearningItemVersionEntity(
    id = id,
    itemId = itemId,
    versionNumber = versionNumber,
    title = title,
    contentUrl = contentUrl,
    payloadJson = payloadJson,
    changelog = changelog,
    createdAt = createdAt
)

fun ItemPrerequisiteEntity.toDomain() = ItemPrerequisite(
    id = id,
    itemId = itemId,
    requiredItemId = requiredItemId,
    minScore = minScore,
    createdAt = createdAt
)

fun ItemPrerequisite.toEntity() = ItemPrerequisiteEntity(
    id = id,
    itemId = itemId,
    requiredItemId = requiredItemId,
    minScore = minScore,
    createdAt = createdAt
)

fun AttemptEntity.toDomain() = Attempt(
    id = id,
    clientAttemptId = clientAttemptId,
    familyCode = familyCode,
    studentId = studentId,
    itemId = itemId,
    versionId = versionId,
    status = status,
    score = score,
    durationSeconds = durationSeconds,
    startedAt = startedAt,
    completedAt = completedAt,
    metadataJson = metadataJson,
    createdAt = createdAt
)

fun Attempt.toEntity() = AttemptEntity(
    id = id,
    clientAttemptId = clientAttemptId,
    familyCode = familyCode,
    studentId = studentId,
    itemId = itemId,
    versionId = versionId,
    status = status,
    score = score,
    durationSeconds = durationSeconds,
    startedAt = startedAt,
    completedAt = completedAt,
    metadataJson = metadataJson,
    createdAt = createdAt
)

fun QuizAnswerMetricEntity.toDomain() = QuizAnswerMetric(
    id = id,
    attemptId = attemptId,
    questionId = questionId,
    questionIndex = questionIndex,
    selectedOption = selectedOption,
    isCorrect = isCorrect,
    durationSeconds = durationSeconds,
    createdAt = createdAt
)

fun QuizAnswerMetric.toEntity() = QuizAnswerMetricEntity(
    id = id,
    attemptId = attemptId,
    questionId = questionId,
    questionIndex = questionIndex,
    selectedOption = selectedOption,
    isCorrect = isCorrect,
    durationSeconds = durationSeconds,
    createdAt = createdAt
)

// --- Curriculum Repository Implementation ---

class LocalV2CurriculumRepositoryImpl(
    private val db: AppDatabase
) : V2CurriculumRepository {

    override fun getCourses(familyCode: String): Flow<List<Course>> {
        return db.courseDao().getActiveCourses(familyCode)
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> {
        return db.lessonDao().getActiveLessonsForCourse(courseId)
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getLessonById(lessonId: String): Flow<Lesson?> {
        return db.lessonDao().getActiveLessonsForCourse("") // We can query by lessonId
            .map { db.lessonDao().getLessonById(lessonId)?.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getLearningItemsForLesson(lessonId: String): Flow<List<LearningItem>> {
        return db.learningItemDao().getActiveItemsForLesson(lessonId)
            .map { entities ->
                entities.map { itemEntity ->
                    val versionEntity = db.learningItemVersionDao().getVersionById(itemEntity.currentVersionId)
                    val versions = db.learningItemVersionDao().getVersionsForItemOnce(itemEntity.id)
                    val prereqs = db.itemPrerequisiteDao().getPrerequisitesForItemOnce(itemEntity.id)
                    itemEntity.toDomain(
                        currentVersion = versionEntity?.toDomain(),
                        versionCount = versions.size.coerceAtLeast(1),
                        prerequisites = prereqs.map { it.toDomain() }
                    )
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getLearningItemsForFamily(familyCode: String): Flow<List<LearningItem>> {
        return db.learningItemDao().getActiveItemsForFamily(familyCode)
            .map { entities ->
                entities.map { itemEntity ->
                    val versionEntity = db.learningItemVersionDao().getVersionById(itemEntity.currentVersionId)
                    val versions = db.learningItemVersionDao().getVersionsForItemOnce(itemEntity.id)
                    val prereqs = db.itemPrerequisiteDao().getPrerequisitesForItemOnce(itemEntity.id)
                    itemEntity.toDomain(
                        currentVersion = versionEntity?.toDomain(),
                        versionCount = versions.size.coerceAtLeast(1),
                        prerequisites = prereqs.map { it.toDomain() }
                    )
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }
    override fun getLearningItem(itemId: String): Flow<LearningItem?> {
        return db.learningItemDao().observeItemById(itemId)
            .map { itemEntity ->
                if (itemEntity == null) null
                else {
                    val versionEntity = db.learningItemVersionDao().getVersionById(itemEntity.currentVersionId)
                    val versions = db.learningItemVersionDao().getVersionsForItemOnce(itemEntity.id)
                    val prereqs = db.itemPrerequisiteDao().getPrerequisitesForItemOnce(itemEntity.id)
                    itemEntity.toDomain(
                        currentVersion = versionEntity?.toDomain(),
                        versionCount = versions.size.coerceAtLeast(1),
                        prerequisites = prereqs.map { it.toDomain() }
                    )
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getPrerequisites(): Flow<List<ItemPrerequisite>> {
        return db.learningItemDao().getActiveItemsForFamily("")
            .map {
                db.itemPrerequisiteDao().getAllPrerequisitesOnce().map { it.toDomain() }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun syncCatalog(familyCode: String, adminToken: String?): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val res = V2CloudClient.fetchCatalog(familyCode, adminToken)
            if (res.isFailure) {
                return@withContext Result.failure(res.exceptionOrNull() ?: Exception("Katalog çekilemedi"))
            }

            val coursesDto = res.getOrNull() ?: emptyList()
            var totalItemsCount = 0

            db.withTransaction {
                for (cDto in coursesDto) {
                    val courseEntity = CourseEntity(
                        id = cDto.id,
                        familyCode = familyCode,
                        title = cDto.title,
                        subject = cDto.subject,
                        gradeLevel = cDto.gradeLevel,
                        description = cDto.description,
                        visualJson = cDto.visual?.let {
                            json.encodeToString(V2CourseVisualDto.serializer(), it)
                        },
                        orderKey = cDto.orderKey,
                        isArchived = cDto.isArchived,
                        createdAt = if (cDto.createdAt > 0) cDto.createdAt else System.currentTimeMillis(),
                        updatedAt = if (cDto.updatedAt > 0) cDto.updatedAt else System.currentTimeMillis()
                    )
                    db.courseDao().upsertCourse(courseEntity)

                    for (lDto in cDto.lessons) {
                        val lessonEntity = LessonEntity(
                            id = lDto.id,
                            courseId = cDto.id,
                            familyCode = familyCode,
                            title = lDto.title,
                            orderKey = lDto.orderKey,
                            isArchived = lDto.isArchived,
                            createdAt = if (lDto.createdAt > 0) lDto.createdAt else System.currentTimeMillis(),
                            updatedAt = if (lDto.updatedAt > 0) lDto.updatedAt else System.currentTimeMillis()
                        )
                        db.lessonDao().upsertLesson(lessonEntity)

                        for (iDto in lDto.items) {
                            totalItemsCount++
                            val itemType = try {
                                ItemType.valueOf(iDto.itemType.uppercase())
                            } catch (_: Exception) {
                                ItemType.VIDEO
                            }

                            val itemEntity = LearningItemEntity(
                                id = iDto.id,
                                lessonId = lDto.id,
                                familyCode = familyCode,
                                itemType = itemType,
                                displayLabel = iDto.displayLabel,
                                stableKey = if (iDto.stableKey.isNotBlank()) iDto.stableKey else iDto.id,
                                orderKey = iDto.orderKey,
                                currentVersionId = iDto.currentVersionId.ifBlank { iDto.currentVersion?.id ?: "ver_${iDto.id}_v1" },
                                isArchived = iDto.isArchived,
                                createdAt = if (iDto.createdAt > 0) iDto.createdAt else System.currentTimeMillis(),
                                updatedAt = if (iDto.updatedAt > 0) iDto.updatedAt else System.currentTimeMillis()
                            )
                            db.learningItemDao().upsertItem(itemEntity)

                            // Save current version if present
                            iDto.currentVersion?.let { vDto ->
                                val payloadStr = when {
                                    vDto.payload != null -> vDto.payload.toString()
                                    else -> null
                                }
                                val versionEntity = LearningItemVersionEntity(
                                    id = vDto.id,
                                    itemId = iDto.id,
                                    versionNumber = vDto.versionNumber,
                                    title = vDto.title.ifBlank { iDto.displayLabel },
                                    contentUrl = vDto.contentUrl,
                                    payloadJson = payloadStr,
                                    changelog = vDto.changelog,
                                    createdAt = if (vDto.createdAt > 0) vDto.createdAt else System.currentTimeMillis()
                                )
                                db.learningItemVersionDao().insertVersion(versionEntity)
                            }

                            // Save prerequisites
                            for (pDto in iDto.prerequisites) {
                                val prereqEntity = ItemPrerequisiteEntity(
                                    id = if (pDto.id.isNotBlank()) pDto.id else "prereq_${iDto.id}_${pDto.requiredItemId}",
                                    itemId = iDto.id,
                                    requiredItemId = pDto.requiredItemId,
                                    minScore = pDto.minScore,
                                    createdAt = if (pDto.createdAt > 0) pDto.createdAt else System.currentTimeMillis()
                                )
                                db.itemPrerequisiteDao().insertPrerequisite(prereqEntity)
                            }
                        }
                    }
                }
            }

            Log.i(TAG, "V2 Catalog synced: ${coursesDto.size} courses, $totalItemsCount items")
            Result.success(totalItemsCount)
        } catch (e: Exception) {
            Log.e(TAG, "syncCatalog error: ${e.message}", e)
            Result.failure(e)
        }
    }
}

// --- Attempt Repository Implementation ---

class LocalV2AttemptRepositoryImpl(
    private val db: AppDatabase
) : V2AttemptRepository {

    override fun getAttempts(familyCode: String, studentId: String): Flow<List<Attempt>> {
        return db.attemptDao().getAttemptsForStudent(familyCode, studentId)
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getAttemptsForItem(familyCode: String, studentId: String, itemId: String): Flow<List<Attempt>> {
        return db.attemptDao().getAttemptsForItem(familyCode, studentId, itemId)
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    private fun parseMetadata(raw: String?): JsonObject {
        if (raw.isNullOrBlank()) return JsonObject(emptyMap())
        return runCatching { json.parseToJsonElement(raw) as? JsonObject }
            .getOrNull() ?: JsonObject(emptyMap())
    }

    private fun withSyncStatus(raw: String?, status: String): String {
        val base = parseMetadata(raw)
        return buildJsonObject {
            base.forEach { (key, value) -> put(key, value) }
            put("syncStatus", status)
        }.toString()
    }

    private fun toAttemptRequest(
        attempt: Attempt,
        quizAnswers: List<QuizAnswerMetric>
    ): V2AttemptRequestDto {
        return V2AttemptRequestDto(
            clientAttemptId = attempt.clientAttemptId,
            studentId = attempt.studentId,
            itemId = attempt.itemId,
            versionId = attempt.versionId,
            status = attempt.status.name,
            score = attempt.score,
            durationSeconds = attempt.durationSeconds,
            startedAt = attempt.startedAt,
            completedAt = attempt.completedAt,
            metadata = parseMetadata(attempt.metadataJson),
            quizAnswers = quizAnswers.map {
                V2QuizAnswerDto(
                    id = it.id,
                    questionId = it.questionId,
                    questionIndex = it.questionIndex,
                    selectedOption = it.selectedOption,
                    isCorrect = it.isCorrect,
                    durationSeconds = it.durationSeconds
                )
            }
        )
    }

    private suspend fun pushAttempt(
        attempt: Attempt,
        quizAnswers: List<QuizAnswerMetric>,
        adminToken: String? = null
    ): Result<Attempt> {
        val netRes = V2CloudClient.recordAttempt(
            familyCode = attempt.familyCode,
            attemptRequest = toAttemptRequest(attempt, quizAnswers),
            adminToken = adminToken
        )

        if (netRes.isFailure) {
            return Result.failure(netRes.exceptionOrNull() ?: IllegalStateException("Attempt sync failed"))
        }

        val syncedAttempt = attempt.copy(
            metadataJson = withSyncStatus(attempt.metadataJson, "SYNCED")
        )
        db.attemptDao().upsertAttempt(syncedAttempt.toEntity())
        return Result.success(syncedAttempt)
    }

    override suspend fun recordAttempt(
        attempt: Attempt,
        quizAnswers: List<QuizAnswerMetric>
    ): Result<Attempt> = withContext(Dispatchers.IO) {
        try {
            // Durable-first: measurement is committed locally before any network work.
            val localAttempt = attempt.copy(
                metadataJson = withSyncStatus(attempt.metadataJson, "PENDING")
            )

            db.withTransaction {
                val existing = db.attemptDao().getAttemptByClientId(
                    localAttempt.familyCode,
                    localAttempt.studentId,
                    localAttempt.clientAttemptId
                )

                if (existing == null) {
                    db.attemptDao().insertAttempt(localAttempt.toEntity())
                    if (quizAnswers.isNotEmpty()) {
                        db.quizAnswerMetricDao().insertAnswerMetrics(quizAnswers.map { it.toEntity() })
                    }
                }
            }

            val canonicalLocal = db.attemptDao().getAttemptByClientId(
                localAttempt.familyCode,
                localAttempt.studentId,
                localAttempt.clientAttemptId
            )?.toDomain() ?: localAttempt

            // Network is best-effort. Failure never rolls back the local measurement.
            val synced = pushAttempt(canonicalLocal, quizAnswers)
            if (synced.isSuccess) {
                Result.success(synced.getOrThrow())
            } else {
                Log.w(TAG, "Attempt saved locally and queued for retry: ${synced.exceptionOrNull()?.message}")
                Result.success(canonicalLocal)
            }
        } catch (e: Exception) {
            Log.e(TAG, "recordAttempt error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncPendingAttempts(
        familyCode: String,
        studentId: String,
        adminToken: String?
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val attempts = db.attemptDao()
                .getAttemptsForStudentOnce(familyCode, studentId)
                .map { it.toDomain() }
                .filter {
                    parseMetadata(it.metadataJson)["syncStatus"]
                        ?.toString()
                        ?.trim('"') != "SYNCED"
                }

            var syncedCount = 0
            for (attempt in attempts) {
                val answers = db.quizAnswerMetricDao()
                    .getAnswersForAttemptOnce(attempt.id)
                    .map { it.toDomain() }

                val result = pushAttempt(attempt, answers, adminToken)
                if (result.isSuccess) {
                    syncedCount++
                } else {
                    Log.w(
                        TAG,
                        "Pending attempt ${attempt.id} remains queued: ${result.exceptionOrNull()?.message}"
                    )
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "syncPendingAttempts error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
