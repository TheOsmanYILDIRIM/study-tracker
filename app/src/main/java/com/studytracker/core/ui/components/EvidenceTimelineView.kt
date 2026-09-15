package com.studytracker.core.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.domain.model.Screenshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private object BitmapMemoryCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // 1/8th of available memory

    val lruCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    suspend fun loadBitmap(filePath: String, isThumbnail: Boolean): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = if (isThumbnail) "thumb_$filePath" else "full_$filePath"
        lruCache.get(cacheKey)?.let { return@withContext it }

        val file = File(filePath)
        if (!file.exists()) return@withContext null

        val options = BitmapFactory.Options().apply {
            if (isThumbnail) {
                inSampleSize = 4 // 4x downscale for thumbnails (fast & low RAM)
                inPreferredConfig = Bitmap.Config.RGB_565
            } else {
                inSampleSize = 1
                inPreferredConfig = Bitmap.Config.RGB_565
            }
        }

        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            if (bitmap != null) {
                lruCache.put(cacheKey, bitmap)
            }
            bitmap
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun EvidenceTimelineView(
    screenshots: List<Screenshot>,
    modifier: Modifier = Modifier
) {
    var selectedScreenshot by remember(screenshots) { mutableStateOf(screenshots.firstOrNull()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📸 Kanıt Zaman Tüneli (${screenshots.size} Ekran Görüntüsü)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Horizontal thumbnail selector with key and async cached bitmap loading
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(screenshots, key = { it.screenshotId }) { ss ->
                val isSelected = selectedScreenshot?.screenshotId == ss.screenshotId
                val borderModifier = if (isSelected) {
                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                }

                val thumbBitmap by produceState<Bitmap?>(initialValue = null, key1 = ss.url) {
                    value = BitmapMemoryCache.loadBitmap(ss.url, isThumbnail = true)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(75.dp)
                        .then(borderModifier)
                        .clickable { selectedScreenshot = ss }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thumbBitmap != null) {
                            Image(
                                bitmap = thumbBitmap!!.asImageBitmap(),
                                contentDescription = "Küçük Resim",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("⏳", fontSize = 12.sp)
                        }

                        // Time badge on thumbnail
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = timeFormat.format(Date(ss.capturedAt)),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Full Preview Card with async loading
        selectedScreenshot?.let { ss ->
            val fullBitmap by produceState<Bitmap?>(initialValue = null, key1 = ss.url) {
                value = BitmapMemoryCache.loadBitmap(ss.url, isThumbnail = false)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (fullBitmap != null) {
                        Image(
                            bitmap = fullBitmap!!.asImageBitmap(),
                            contentDescription = "Büyük Ekran Görüntüsü",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }
        }
    }
}
