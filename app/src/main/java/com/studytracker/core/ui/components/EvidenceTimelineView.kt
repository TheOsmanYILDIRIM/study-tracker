package com.studytracker.core.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.domain.model.Screenshot
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

        // Horizontal thumbnail selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(screenshots) { ss ->
                val isSelected = selectedScreenshot?.screenshotId == ss.screenshotId
                val borderModifier = if (isSelected) {
                    Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
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
                        val file = File(ss.url)
                        if (file.exists()) {
                            val bitmap = remember(ss.url) { BitmapFactory.decodeFile(file.absolutePath) }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Küçük Resim",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Text("Önizleme Yok", fontSize = 10.sp)
                        }

                        // Time badge on thumbnail
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = timeFormat.format(Date(ss.capturedAt)),
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Full Preview Card
        selectedScreenshot?.let { ss ->
            val file = File(ss.url)
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
                    if (file.exists()) {
                        val bitmap = remember(ss.url) { BitmapFactory.decodeFile(file.absolutePath) }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Büyük Ekran Görüntüsü",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Text("Görsel yüklenemedi: ${ss.url}")
                    }
                }
            }
        }
    }
}
