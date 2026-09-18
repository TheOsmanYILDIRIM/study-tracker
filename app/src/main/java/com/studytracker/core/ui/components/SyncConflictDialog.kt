package com.studytracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class PlanVersionSummary(
    val source: String,
    val weekId: String,
    val taskCount: Int,
    val sampleTasks: List<String>,
    val updatedAtFormatted: String
)

data class SyncConflictData(
    val cloud: PlanVersionSummary,
    val local: PlanVersionSummary
)

@Composable
fun SyncConflictDialog(
    conflict: SyncConflictData,
    onDismissRequest: () -> Unit,
    onDownloadCloud: () -> Unit,
    onSmartMerge: () -> Unit,
    onUploadLocal: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFFB703).copy(alpha = 0.6f),
                            Color(0xFF8B5CF6).copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF130924))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon & Title
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFB703).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFFB703).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Çakışma",
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Senkronizasyon Çakışması",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Buluttaki plan ile bu cihazdaki plan farklı. Hangi kaynağın geçerli olmasını istersiniz?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cloud Version Card
                VersionCard(
                    title = "☁️ Buluttaki Sürüm",
                    accentColor = Color(0xFF00F5D4),
                    summary = conflict.cloud
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Local Version Card
                VersionCard(
                    title = "📱 Bu Cihazdaki Sürüm",
                    accentColor = Color(0xFFA78BFA),
                    summary = conflict.local
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                // 1. Download Cloud (Recommended)
                Button(
                    onClick = onDownloadCloud,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = Color(0xFF090414),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "☁️ Buluttan İndir (Önerilen)",
                        color = Color(0xFF090414),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Smart Merge
                OutlinedButton(
                    onClick = onSmartMerge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF00F5D4)))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF1E1038))
                ) {
                    Icon(
                        imageVector = Icons.Default.MergeType,
                        contentDescription = null,
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔀 Akıllı Birleştir (Onayları Koru)",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Upload Local
                OutlinedButton(
                    onClick = onUploadLocal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(Color(0xFFFFB703).copy(alpha = 0.6f), Color(0xFFEF4444).copy(alpha = 0.6f)))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF24141E))
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📱 Cihazdakini Buluta Zorla Yükle",
                        color = Color(0xFFFFB703),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Vazgeç / İptal",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionCard(
    title: String,
    accentColor: Color,
    summary: PlanVersionSummary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1038).copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
                // Source Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Kaynak: ${summary.source}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📅 Hafta: ${summary.weekId}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                )
                Text(
                    text = "📚 ${summary.taskCount} Görev",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (summary.updatedAtFormatted.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🕒 Son Değişiklik: ${summary.updatedAtFormatted}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f))
                )
            }

            if (summary.sampleTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Örnek Dersler:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                )
                summary.sampleTasks.take(3).forEach { sample ->
                    Text(
                        text = "  • $sample",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.85f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
