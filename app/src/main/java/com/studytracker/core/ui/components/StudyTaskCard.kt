package com.studytracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .background(ZenMoonGold, ZenPillShape)
                                .clickable { onRetryClick(occurrence) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF451A03),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Tekrarla",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF451A03)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .background(ZenSkyCyan, ZenPillShape)
                                .clickable { onStartClick(occurrence) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = ZenMintText
                                )
                                Text(
                                    text = "Başla",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ZenMintText
                                )
                            }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(ZenPillShape)
                                .background(ZenMoonGoldContainer)
                                .border(1.dp, ZenMoonGold.copy(alpha = 0.4f), ZenPillShape)
                                .padding(horizontal = 8.dp),
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

                        // Child can restart review-waiting task
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(ZenPillShape)
                                .background(ZenSkyCyanContainer)
                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.5f), ZenPillShape)
                                .clickable { onStartClick(occurrence) }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "Yeniden",
                                    color = ZenSkyCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
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

/**
 * Compact horizontal weekly target card designed for LazyRow
 */
@Composable
fun StudyWeeklyTaskCard(
    occurrence: Occurrence,
    onStartClick: (Occurrence) -> Unit,
    onRetryClick: (Occurrence) -> Unit,
    modifier: Modifier = Modifier
) {
    val target = occurrence.targetCount ?: 1
    val approved = occurrence.approvedCount ?: 0
    val fraction = (approved.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)

    val tileBg = ZenMoonGoldContainer
    val tileFg = ZenMoonGold

    val borderColor = when {
        occurrence.warning -> ZenMoonGold.copy(alpha = 0.7f)
        occurrence.status == OccurrenceStatus.APPROVED -> ZenForestGreen.copy(alpha = 0.5f)
        occurrence.status == OccurrenceStatus.ACTIVE -> ZenSkyCyan.copy(alpha = 0.8f)
        else -> ZenPaperBorder
    }

    Column(
        modifier = modifier
            .width(230.dp)
            .clip(ZenCardShape)
            .background(ZenPaperCard)
            .border(1.dp, borderColor, ZenCardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top: Icon + Badge/Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(ZenSquircleShape)
                    .background(tileBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = tileFg,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Status or Start button
            when (occurrence.status) {
                OccurrenceStatus.PENDING -> {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .background(ZenSkyCyan, ZenPillShape)
                            .clickable { onStartClick(occurrence) }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = ZenMintText
                            )
                            Text(
                                text = "Başla",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = ZenMintText
                            )
                        }
                    }
                }
                OccurrenceStatus.ACTIVE -> {
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(ZenPillShape)
                            .background(ZenSkyCyanContainer)
                            .border(1.dp, ZenSkyCyan.copy(alpha = 0.5f), ZenPillShape)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aktif ⚡",
                            color = ZenSkyCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                OccurrenceStatus.WAITING_REVIEW -> {
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(ZenPillShape)
                            .background(ZenMoonGoldContainer)
                            .border(1.dp, ZenMoonGold.copy(alpha = 0.4f), ZenPillShape)
                            .clickable { onStartClick(occurrence) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(11.dp))
                            Text(
                                text = "Tekrar",
                                color = ZenMoonGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                OccurrenceStatus.APPROVED -> {
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(ZenPillShape)
                            .background(ZenForestContainer)
                            .border(1.dp, ZenForestGreen.copy(alpha = 0.4f), ZenPillShape)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ Bitti",
                            color = ZenForestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                else -> {
                    StatusBadge(status = occurrence.status, warning = occurrence.warning)
                }
            }
        }

        // Middle: Title
        Text(
            text = occurrence.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ZomoTextPrimary,
            fontSize = 13.5.sp,
            maxLines = 1
        )

        // Progress Bar & Target Text
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color(0x33000000), ZenPillShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = fraction.coerceIn(if (fraction > 0f) 0.05f else 0f, 1f))
                        .background(ZenMoonGold, ZenPillShape)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${occurrence.plannedMinutes} dk",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZomoTextSecondary,
                    fontSize = 10.5.sp
                )
                Text(
                    text = "🎯 $approved/$target",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = ZenMoonGold,
                    fontSize = 10.5.sp
                )
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
