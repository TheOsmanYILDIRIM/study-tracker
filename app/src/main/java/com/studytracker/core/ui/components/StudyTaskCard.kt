package com.studytracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.ui.theme.*

@Composable
fun StudyTaskCard(
    occurrence: Occurrence,
    onStartClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = when {
        occurrence.warning -> AmberContainer.copy(alpha = 0.5f)
        occurrence.status == OccurrenceStatus.APPROVED -> EmeraldContainer.copy(alpha = 0.4f)
        occurrence.status == OccurrenceStatus.ACTIVE -> PurpleContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        occurrence.warning -> AmberWarning
        occurrence.status == OccurrenceStatus.APPROVED -> EmeraldSuccess
        occurrence.status == OccurrenceStatus.ACTIVE -> PurpleActive
        else -> Color.Transparent
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when {
                        occurrence.youtubeUrl != null -> Icons.Default.PlayCircle
                        occurrence.type == TaskKind.WEEKLY -> Icons.Default.EmojiEvents
                        else -> Icons.Default.MenuBook
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = occurrence.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status Badge
                StatusBadge(status = occurrence.status, warning = occurrence.warning)
            }

            // Subtitle info (Duration / Weekly Progress)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️ Planlanan: ${occurrence.plannedMinutes} dakika",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (occurrence.type == TaskKind.WEEKLY && occurrence.targetCount != null) {
                    Text(
                        text = "🎯 Hedef: ${occurrence.approvedCount} / ${occurrence.targetCount} Yapıldı",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Warning Banner if rejected
            if (occurrence.warning && !occurrence.warningText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberWarning.copy(alpha = 0.2f))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Uyarı",
                            tint = AmberDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Ebeveyn Notu: ${occurrence.warningText}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // Action Button Area
            when (occurrence.status) {
                OccurrenceStatus.PENDING -> {
                    if (occurrence.warning) {
                        Button(
                            onClick = onRetryClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dersi Tekrar Başlat", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onStartClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Çalışmayı Başlat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
                OccurrenceStatus.ACTIVE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PurpleActive.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = PurpleActive)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Şu anda çalışılıyor... (Yüzen düğme aktif)",
                                color = PurpleActive,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                OccurrenceStatus.WAITING_REVIEW -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⏳ Ebeveyn Onayı Bekliyor",
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                OccurrenceStatus.APPROVED -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✅ Görev Başarıyla Tamamlandı",
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
fun StatusBadge(status: OccurrenceStatus, warning: Boolean) {
    val (label, bg, fg) = when {
        warning -> Triple("⚠️ Tekrar", AmberContainer, Color(0xFF92400E))
        status == OccurrenceStatus.APPROVED -> Triple("✅ Onaylandı", EmeraldContainer, Color(0xFF065F46))
        status == OccurrenceStatus.WAITING_REVIEW -> Triple("⏳ Onay Bekliyor", Color(0xFFFEF3C7), Color(0xFF92400E))
        status == OccurrenceStatus.ACTIVE -> Triple("⚡ Aktif", PurpleContainer, Color(0xFF5B21B6))
        else -> Triple("Yapılacak", LightCard, Color.Gray)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
