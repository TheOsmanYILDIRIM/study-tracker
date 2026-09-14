package com.studytracker.core.data.local.db.converter

import androidx.room.TypeConverter
import com.studytracker.core.domain.model.*

class AppTypeConverters {
    @TypeConverter
    fun fromTaskKind(value: TaskKind?): String? = value?.name

    @TypeConverter
    fun toTaskKind(value: String?): TaskKind? = value?.let { TaskKind.valueOf(it) }

    @TypeConverter
    fun fromContentType(value: ContentType?): String? = value?.name

    @TypeConverter
    fun toContentType(value: String?): ContentType? = value?.let { ContentType.valueOf(it) }

    @TypeConverter
    fun fromTargetMode(value: TargetMode?): String? = value?.name

    @TypeConverter
    fun toTargetMode(value: String?): TargetMode? = value?.let { TargetMode.valueOf(it) }

    @TypeConverter
    fun fromOccurrenceStatus(value: OccurrenceStatus?): String? = value?.name

    @TypeConverter
    fun toOccurrenceStatus(value: String?): OccurrenceStatus? = value?.let { OccurrenceStatus.valueOf(it) }

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus?): String? = value?.name

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus? = value?.let { SessionStatus.valueOf(it) }

    @TypeConverter
    fun fromUploadStatus(value: UploadStatus?): String? = value?.name

    @TypeConverter
    fun toUploadStatus(value: String?): UploadStatus? = value?.let { UploadStatus.valueOf(it) }

    @TypeConverter
    fun fromReviewStatus(value: ReviewStatus?): String? = value?.name

    @TypeConverter
    fun toReviewStatus(value: String?): ReviewStatus? = value?.let { ReviewStatus.valueOf(it) }
}
