package com.studytracker.core.ui.components

import androidx.compose.foundation.BorderStroke
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

private val ZenCardShape = RoundedCornerShape(20.dp)
private val ZenSquircleShape = RoundedCornerShape(14.dp)
private val ZenPillShape = CircleShape

@Composable
fun StudyTaskCard(
    occurrence: Occurrence,
    onStartClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (tileBg, tileFg, iconVector) = when {
        occurrence.youtubeUrl != null -> Triple(ZenRoseContainer, ZenRoseCoral, Icons.Default.PlayCircle)
        occurrence.type == TaskKind.WEEKLY -> Triple(ZenMoonGoldContainer, ZenMoonGold, Icons.Default.EmojiEvents)
        occurrence.title.contains("kitap", ignoreCase = true) || occurrence.title.contains("paragraf", ignoreCase = true) ->
            Triple(ZenSkyCyanContainer, ZenSkyCyan, Icons.Default.MenuBook)
        occurrence.title.contains("mat", ignoreCase = true) ->
            Triple(ZenSkyCyanContainer, ZenSkyCyan, Icons.Default.Calculate)
        occurrence.title.contains("fen", ignoreCase = true) || occurrence.title.contains("fizik", ignoreCase = true) ->
            Triple(ZenForestContainer, ZenForestGreen, Icons.Default.Biotech)
        else -> Triple(ZenLavenderContainer, ZenLavender, Icons.Default.School)
    }

    val borderColor = when {
        occurrence.warning -> ZenMoonGold.copy(alpha = 0.6f)
        occurrence.status == OccurrenceStatus.APPROVED -> ZenForestGreen.copy(alpha = 0.35f)
        occurrence.status == OccurrenceStatus.ACTIVE -> ZenSkyCyan.copy(alpha = 0.7f)
        else -> ZenNightBorder
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ZenCardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (occurrence.status == OccurrenceStatus.ACTIVE) ZenNightCardElevated else ZenNightSurface
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Main Compact Row: Icon + Title/Details + Right Action/Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Soft Squircle Icon
                Surface(
                    shape = ZenSquircleShape,
                    color = tileBg,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = tileFg,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${occurrence.plannedMinutes} dk" +
                                if (occurrence.type == TaskKind.WEEKLY && occurrence.targetCount != null)
                                    " • 🎯 ${occurrence.approvedCount}/${occurrence.targetCount}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZomoTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                // Right Side: Action Pill or Status Chip
                when (occurrence.status) {
                    OccurrenceStatus.PENDING -> {
                        if (occurrence.warning) {
                            Button(
                                onClick = onRetryClick,
                                modifier = Modifier.height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZenMoonGold,
                                    contentColor = Color(0xFF451A03)
                                ),
                                shape = ZenPillShape,
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tekrarla", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = onStartClick,
                                modifier = Modifier.height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZenSkyCyan,
                                    contentColor = ZenMintText
                                ),
                                shape = ZenPillShape,
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp),
                                    tint = ZenMintText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Başla", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    OccurrenceStatus.ACTIVE -> {
                        Surface(
                            shape = ZenPillShape,
                            color = ZenSkyCyanContainer,
                            border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Çalışılıyor ⚡",
                                    color = ZenSkyCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    OccurrenceStatus.WAITING_REVIEW -> {
                        Surface(
                            shape = ZenPillShape,
                            color = ZenMoonGoldContainer,
                            border = BorderStroke(1.dp, ZenMoonGold.copy(alpha = 0.4f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "İnceleniyor",
                                    color = ZenMoonGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    OccurrenceStatus.APPROVED -> {
                        Surface(
                            shape = ZenPillShape,
                            color = ZenForestContainer,
                            border = BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Bitti",
                                    color = ZenForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    else -> {
                        StatusBadge(status = occurrence.status, warning = occurrence.warning)
                    }
                }
            }

            // Compact Parent Warning Note if rejected
            if (occurrence.warning && !occurrence.warningText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ZenMoonGoldContainer,
                    border = BorderStroke(1.dp, ZenMoonGold.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ZenMoonGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = occurrence.warningText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = ZenMoonGold,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
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

    Surface(
        color = bg,
        shape = ZenPillShape,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
