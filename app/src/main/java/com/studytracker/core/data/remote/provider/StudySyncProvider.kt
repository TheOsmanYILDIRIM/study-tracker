package com.studytracker.core.data.remote.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.*
import com.studytracker.core.data.remote.supabase.*
import com.studytracker.core.domain.model.ContentType
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.ReviewStatus
import com.studytracker.core.domain.model.SessionStatus
import com.studytracker.core.domain.model.TargetMode
import com.studytracker.core.domain.model.TaskKind
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StudySyncProvider : ContentProvider() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val ctx = context ?: return null
        val db = AppDatabase.getInstance(ctx)
        val familyCode = arg ?: "ST-2026"

        return when (method) {
            "getSyncPayload" -> {
                try {
                    val resultBundle = Bundle()
                    val payload = runBlocking {
                        val plan = db.planDao().getActivePlanOnce()?.let {
                            LocalPlanSyncDto(
                                planId = it.planId,
                                weekId = it.weekId,
                                weekStartDate = it.weekStartDate,
                                childId = it.childId,
                                timezone = it.timezone,
                                updatedAt = it.updatedAt,
                                rawJson = it.rawJson
                            )
                        }
                        val tasks = db.taskTemplateDao().getAllTasksOnce().map {
                            LocalTaskTemplateSyncDto(
                                taskId = it.taskId,
                                title = it.title,
                                kind = it.kind.name,
                                contentType = it.contentType.name,
                                youtubeUrl = it.youtubeUrl,
                                plannedMinutes = it.plannedMinutes,
                                targetMode = it.targetMode?.name,
                                targetCount = it.targetCount,
                                targetMinutes = it.targetMinutes,
                                reviewRequired = it.reviewRequired,
                                active = it.active
                            )
                        }
                        val occurrences = db.occurrenceDao().getAllOccurrencesOnce().map {
                            RemoteOccurrenceSyncDto(
                                id = it.occurrenceKey,
                                familyCode = familyCode,
                                date = it.date ?: "",
                                planId = it.taskId,
                                subject = it.title,
                                topic = it.type.name,
                                targetDurationMin = it.plannedMinutes,
                                targetQuestionCount = it.targetCount ?: 0,
                                completedDurationMin = it.targetMinutes ?: 0,
                                completedQuestionCount = it.approvedCount,
                                status = it.status.name,
                                parentNote = it.warningText ?: "",
                                weekId = it.weekId ?: "",
                                orderIndex = 0
                            )
                        }
                        val sessions = db.sessionDao().getAllSessionsOnce().map {
                            RemoteSessionSyncDto(
                                id = it.sessionId,
                                familyCode = familyCode,
                                occurrenceId = it.occurrenceKey,
                                startTime = it.startTime,
                                endTime = it.endTime,
                                durationMin = if (it.endTime != null) ((it.endTime - it.startTime) / 60000).toInt() else 0,
                                isCompleted = it.status != SessionStatus.ACTIVE
                            )
                        }
                        val reviews = db.reviewDao().getAllReviewsOnce().map {
                            RemoteReviewSyncDto(
                                id = "rev_${it.sessionId}",
                                familyCode = familyCode,
                                sessionId = it.sessionId,
                                isApproved = it.reviewStatus == ReviewStatus.APPROVED,
                                rejectionReason = if (it.reviewStatus != ReviewStatus.APPROVED) it.reviewNote else null,
                                parentRating = if (it.reviewStatus == ReviewStatus.APPROVED) 5 else 1,
                                feedbackNote = it.reviewNote,
                                reviewedAt = it.reviewedAt
                            )
                        }
                        SharedFamilySyncPayload(
                            familyCode = familyCode,
                            plan = plan,
                            tasks = tasks,
                            occurrences = occurrences,
                            sessions = sessions,
                            reviews = reviews,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    resultBundle.putString("payload", json.encodeToString(payload))
                    resultBundle
                } catch (e: Exception) {
                    Log.e("StudySyncProvider", "Error in getSyncPayload: ${e.message}")
                    null
                }
            }
            "putSyncPayload" -> {
                try {
                    val payloadJson = extras?.getString("payload")
                    if (!payloadJson.isNullOrBlank()) {
                        val payload = json.decodeFromString<SharedFamilySyncPayload>(payloadJson)
                        runBlocking {
                            payload.plan?.let { p ->
                                db.planDao().setActivePlan(
                                    PlanEntity(
                                        planId = p.planId,
                                        weekId = p.weekId,
                                        weekStartDate = p.weekStartDate,
                                        childId = p.childId,
                                        timezone = p.timezone,
                                        updatedAt = p.updatedAt,
                                        rawJson = p.rawJson
                                    )
                                )
                            }
                            if (payload.tasks.isNotEmpty()) {
                                db.taskTemplateDao().upsertTasks(payload.tasks.map {
                                    TaskTemplateEntity(
                                        taskId = it.taskId,
                                        title = it.title,
                                        kind = try { TaskKind.valueOf(it.kind) } catch (_: Exception) { TaskKind.DAILY },
                                        contentType = try { ContentType.valueOf(it.contentType) } catch (_: Exception) { ContentType.READING },
                                        youtubeUrl = it.youtubeUrl,
                                        plannedMinutes = it.plannedMinutes,
                                        targetMode = it.targetMode?.let { tm -> try { TargetMode.valueOf(tm) } catch (_: Exception) { null } },
                                        targetCount = it.targetCount,
                                        targetMinutes = it.targetMinutes,
                                        reviewRequired = it.reviewRequired,
                                        active = it.active
                                    )
                                })
                            }
                            if (payload.occurrences.isNotEmpty()) {
                                val currentOccs = db.occurrenceDao().getAllOccurrencesOnce().associateBy { it.occurrenceKey }
                                val mergedOccs = payload.occurrences.map { remote ->
                                    val local = currentOccs[remote.id]
                                    val remoteStatus = try { OccurrenceStatus.valueOf(remote.status) } catch (_: Exception) { OccurrenceStatus.PENDING }
                                    val resolvedStatus = when {
                                        local?.status == OccurrenceStatus.APPROVED || remoteStatus == OccurrenceStatus.APPROVED -> OccurrenceStatus.APPROVED
                                        local?.status == OccurrenceStatus.WAITING_REVIEW || remoteStatus == OccurrenceStatus.WAITING_REVIEW -> OccurrenceStatus.WAITING_REVIEW
                                        local?.status == OccurrenceStatus.ACTIVE || remoteStatus == OccurrenceStatus.ACTIVE -> OccurrenceStatus.ACTIVE
                                        remoteStatus == OccurrenceStatus.REJECTED || local?.status == OccurrenceStatus.REJECTED -> OccurrenceStatus.PENDING
                                        else -> local?.status ?: remoteStatus
                                    }

                                    OccurrenceEntity(
                                        occurrenceKey = remote.id,
                                        taskId = remote.planId,
                                        type = try { TaskKind.valueOf(remote.topic) } catch (_: Exception) { TaskKind.DAILY },
                                        date = remote.date.ifEmpty { null },
                                        weekId = remote.weekId.ifEmpty { null },
                                        title = remote.subject,
                                        plannedMinutes = remote.targetDurationMin,
                                        youtubeUrl = null,
                                        reviewRequired = true,
                                        status = resolvedStatus,
                                        warning = remote.parentNote.isNotBlank() && resolvedStatus != OccurrenceStatus.APPROVED,
                                        warningText = remote.parentNote.ifEmpty { null },
                                        rejectCount = maxOf(local?.rejectCount ?: 0, if (remote.parentNote.isNotBlank()) 1 else 0),
                                        approvedCount = maxOf(local?.approvedCount ?: 0, remote.completedQuestionCount),
                                        targetCount = if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null,
                                        targetMinutes = if (remote.completedDurationMin > 0) remote.completedDurationMin else null
                                    )
                                }
                                db.occurrenceDao().upsertOccurrences(mergedOccs)
                            }
                            for (sess in payload.sessions) {
                                val current = db.sessionDao().getSessionById(sess.id)
                                val resolvedStatus = when {
                                    current?.status == SessionStatus.APPROVED -> SessionStatus.APPROVED
                                    current?.status == SessionStatus.REJECTED -> SessionStatus.REJECTED
                                    sess.isCompleted -> SessionStatus.WAITING_REVIEW
                                    else -> current?.status ?: SessionStatus.ACTIVE
                                }
                                db.sessionDao().upsertSession(
                                    SessionEntity(
                                        sessionId = sess.id,
                                        occurrenceKey = sess.occurrenceId,
                                        childId = "child_1",
                                        startTime = sess.startTime,
                                        endTime = sess.endTime ?: current?.endTime,
                                        status = resolvedStatus,
                                        screenshotCount = current?.screenshotCount ?: 1,
                                        finalScreenshotUrl = current?.finalScreenshotUrl
                                    )
                                )
                            }
                            for (rev in payload.reviews) {
                                val reviewEntity = ReviewEntity(
                                    sessionId = rev.sessionId,
                                    occurrenceKey = rev.sessionId,
                                    reviewStatus = if (rev.isApproved) ReviewStatus.APPROVED else ReviewStatus.REJECTED,
                                    reviewNote = rev.feedbackNote ?: rev.rejectionReason,
                                    reviewedAt = rev.reviewedAt
                                )
                                db.reviewDao().insertReview(reviewEntity)
                            }
                        }
                    }
                    val res = Bundle()
                    res.putBoolean("success", true)
                    res
                } catch (e: Exception) {
                    Log.e("StudySyncProvider", "Error in putSyncPayload: ${e.message}")
                    null
                }
            }
            else -> null
        }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.studytracker.sync"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
