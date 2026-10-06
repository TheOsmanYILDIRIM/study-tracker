package com.studytracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.studytracker.R
import com.studytracker.core.domain.model.ItemType
import com.studytracker.core.ui.theme.ZenLavender
import com.studytracker.core.ui.theme.ZenMoonGold
import com.studytracker.core.ui.theme.ZenSkyCyan

private val youtubeIdPatterns = listOf(
    Regex("""(?:youtu\.be/)([A-Za-z0-9_-]{6,})""", RegexOption.IGNORE_CASE),
    Regex("""(?:v=)([A-Za-z0-9_-]{6,})""", RegexOption.IGNORE_CASE),
    Regex("""(?:youtube\.com/(?:shorts|embed)/)([A-Za-z0-9_-]{6,})""", RegexOption.IGNORE_CASE)
)

fun youtubeThumbnailUrl(videoUrl: String?): String? {
    if (videoUrl.isNullOrBlank()) return null
    val id = youtubeIdPatterns.firstNotNullOfOrNull { pattern ->
        pattern.find(videoUrl)?.groupValues?.getOrNull(1)
    } ?: return null
    return "https://i.ytimg.com/vi/$id/hqdefault.jpg"
}

@Composable
fun LearningItemVisual(
    itemType: ItemType,
    videoUrl: String?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val thumb = youtubeThumbnailUrl(videoUrl)

    when {
        itemType == ItemType.VIDEO && thumb != null -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumb)
                    .memoryCacheKey("study-video-thumb:$thumb")
                    .diskCacheKey("study-video-thumb:$thumb")
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = "Video kapağı",
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(shape)
            )
        }
        itemType == ItemType.ANKI -> {
            Box(
                modifier = modifier.clip(shape).background(ZenLavender.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_anki_logo),
                    contentDescription = "Anki",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        itemType == ItemType.QUIZ -> {
            Box(
                modifier = modifier.clip(shape).background(ZenMoonGold.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_quiz_badge),
                    contentDescription = "Test",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        else -> {
            Box(
                modifier = modifier.clip(shape).background(ZenSkyCyan.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = ZenSkyCyan,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
