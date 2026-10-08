package com.studytracker.core.ui.components

import android.graphics.Color as AndroidColor
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.studytracker.core.domain.model.CourseVisual

fun courseVisualColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return runCatching { Color(AndroidColor.parseColor(hex)) }.getOrDefault(fallback)
}

@Composable
fun CourseCoverVisual(
    visual: CourseVisual,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val coverUrl = visual.coverUrl
    val primary = courseVisualColor(visual.primaryColor, Color(0xFF4B7D83))
    val shape = RoundedCornerShape(12.dp)

    if (!coverUrl.isNullOrBlank()) {
        val imageData: Any = if (coverUrl.startsWith("data:image/") && coverUrl.contains(";base64,")) {
            val encoded = coverUrl.substringAfter(";base64,")
            runCatching { Base64.decode(encoded, Base64.DEFAULT) }.getOrElse { coverUrl }
        } else {
            coverUrl
        }
        val cacheKey = "course-cover:" + coverUrl.hashCode()

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageData)
                .memoryCacheKey(cacheKey)
                .diskCacheKey(cacheKey)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape)
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(primary.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = contentDescription,
                tint = primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
