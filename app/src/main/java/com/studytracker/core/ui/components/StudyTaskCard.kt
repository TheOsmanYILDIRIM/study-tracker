package com.studytracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

private val ZenCardShape = RoundedCornerShape(16.dp)
private val ZenSquircleShape = RoundedCornerShape(12.dp)
private val ZenPillShape = CircleShape

@Composable
fun StudyTaskCard(
    occurrence: Occurrence,
    onStartClick: (Occurrence) -> Unit,
    onRetryClick: (Occurrence) -> Unit,
    modifier: Modifier = Modifier
) {
    val tileBg: Color
    val tileFg: Color
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector

    when {
        occurrence.youtubeUrl != null -> {
            tileBg = ZenRoseContainer
            tileFg = ZenRoseCoral
            iconVector = Icons.Default.PlayCircle
        }
        occurrence.type == TaskKind.WEEKLY -> {
            tileBg = ZenMoonGoldContainer
            tileFg = ZenMoonGold
            iconVector = Icons.Default.EmojiEvents
        }
        occurrence.title.contains("kitap", ignoreCase = true) || occurrence.title.contains("paragraf", ignoreCase = true) -> {
            tileBg = ZenSkyCyanContainer
            tileFg = ZenSkyCyan
            iconVector = Icons.Default.MenuBook
        }
        occurrence.title.contains("mat", ignoreCase = true) -> {
            tileBg = ZenSkyCyanContainer
            tileFg = ZenSkyCyan
            iconVector = Icons.Default.Calculate
        }
        occurrence.title.contains("fen", ignoreCase = true) || occurrence.title.contains("fizik", ignoreCase = true) -> {
            tileBg = ZenForestContainer
            tileFg = ZenForestGreen
            iconVector = Icons.Default.Biotech
        }
        else -> {
            tileBg = ZenLavenderContainer
            tileFg = ZenLavender
            iconVector = Icons.Default.School
        }
    }

    val borderColor = when {
        occurrence.warning -> ZenMoonGold.copy(alpha = 0.65f)
        occurrence.status == OccurrenceStatus.APPROVED -> ZenForestGreen.copy(alpha = 0.4f)
        occurrence.status == OccurrenceStatus.ACTIVE -> ZenSkyCyan.copy(alpha = 0.75f)
        else -> ZenPaperBorder
    }

    val cardBg = if (occurrence.status == OccurrenceStatus.ACTIVE) ZenPaperElevated else ZenPaperCard

    // Hardware RenderNode Layer (GPU retains rendered texture during scrolling, 0 redraws)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ZenCardShape)
            .background(cardBg)
            .border(1.dp, borderColor, ZenCardShape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Main Single-Row Task: Icon + Title/Details + Right Action/Status Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Flat Squircle Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ZenSquircleShape)
                    .background(tileBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = tileFg,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Middle: Title & Duration / Target
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = occurrence.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZomoTextPrimary,
                    fontSize = 14.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${occurrence.plannedMinutes} dk" +
                            if (occurrence.type == TaskKind.WEEKLY && occurrence.targetCount != null)
                                " • 🎯 ${occurrence.approvedCount}/${occurrence.targetCount}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZomoTextSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }

            // Right Side: Action Pill or Status Chip
            when (occurrence.status) {
                OccurrenceStatus.PENDING -> {
                    if (occurrence.warning) {
                        Button(
                            onClick = { onRetryClick(occurrence) },
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZenMoonGold,
                                contentColor = Color(0xFF451A03)
                            ),
                            shape = ZenPillShape,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Tekrarla", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = { onStartClick(occurrence) },
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZenSkyCyan,
                                contentColor = ZenMintText
                            ),
                            shape = ZenPillShape,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = ZenMintText
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Başla", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
                OccurrenceStatus.ACTIVE -> {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(ZenPillShape)
                            .background(ZenSkyCyanContainer)
                            .border(1.dp, ZenSkyCyan.copy(alpha = 0.5f), ZenPillShape)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(12.dp))
                            Text(
                                text = "Çalışılıyor ⚡",
                                color = ZenSkyCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
                OccurrenceStatus.WAITING_REVIEW -> {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(ZenPillShape)
                            .background(ZenMoonGoldContainer)
                            .border(1.dp, ZenMoonGold.copy(alpha = 0.4f), ZenPillShape)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(12.dp))
                            Text(
                                text = "İnceleniyor",
                                color = ZenMoonGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
                OccurrenceStatus.APPROVED -> {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(ZenPillShape)
                            .background(ZenForestContainer)
                            .border(1.dp, ZenForestGreen.copy(alpha = 0.4f), ZenPillShape)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(12.dp))
                            Text(
                                text = "Bitti",
                                color = ZenForestGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
                else -> {
                    StatusBadge(status = occurrence.status, warning = occurrence.warning)
                }
            }
        }

        // Compact Parent Warning Note
        if (occurrence.warning && !occurrence.warningText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ZenMoonGoldContainer)
                    .border(1.dp, ZenMoonGold.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ZenMoonGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = occurrence.warningText ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = ZenMoonGold,
                        fontSize = 10.5.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: OccurrenceStatus, warning: Boolean) {
    val label: String
    val bg: Color
    val fg: Color

    if (warning) {
        label = "⚠️ Tekrar"
        bg = ZenMoonGoldContainer
        fg = ZenMoonGold
    } else {
        when (status) {
            OccurrenceStatus.APPROVED -> {
                label = "✓ Bitti"
                bg = ZenForestContainer
                fg = ZenForestGreen
            }
            OccurrenceStatus.WAITING_REVIEW -> {
                label = "⏳ İnceleniyor"
                bg = ZenMoonGoldContainer
                fg = ZenMoonGold
            }
            OccurrenceStatus.ACTIVE -> {
                label = "⚡ Aktif"
                bg = ZenSkyCyanContainer
                fg = ZenSkyCyan
            }
            else -> {
                label = "🕒 Bekliyor"
                bg = Color(0x15FFFFFF)
                fg = ZomoTextSecondary
            }
        }
    }

    Box(
        modifier = Modifier
            .clip(ZenPillShape)
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.3f), ZenPillShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
