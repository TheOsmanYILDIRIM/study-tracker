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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.ui.theme.*

private val ZomoCardShape = RoundedCornerShape(24.dp)
private val ZomoTileShape = RoundedCornerShape(16.dp)
private val ZomoPillShape = CircleShape
private val ZomoBannerShape = RoundedCornerShape(14.dp)

@Composable
fun StudyTaskCard(
    occurrence: Occurrence,
    onStartClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (tileBg, tileFg, iconVector) = when {
        occurrence.youtubeUrl != null -> Triple(ZomoPinkContainer, ZomoPink, Icons.Default.PlayCircle)
        occurrence.type == TaskKind.WEEKLY -> Triple(ZomoAmberContainer, ZomoAmber, Icons.Default.EmojiEvents)
        occurrence.title.contains("kitap", ignoreCase = true) || occurrence.title.contains("paragraf", ignoreCase = true) ->
            Triple(ZomoSkyContainer, ZomoSky, Icons.Default.MenuBook)
        else -> Triple(ZomoVioletContainer, ZomoPurplePrimary, Icons.Default.School)
    }

    val borderColor = when {
        occurrence.warning -> ZomoAmber.copy(alpha = 0.6f)
        occurrence.status == OccurrenceStatus.APPROVED -> ZomoEmerald.copy(alpha = 0.5f)
        occurrence.status == OccurrenceStatus.ACTIVE -> ZomoPurplePrimary
        else -> ZomoLavenderBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = ZomoCardShape, spotColor = ZomoPurplePrimary.copy(alpha = 0.08f))
            .border(width = 1.2.dp, color = borderColor, shape = ZomoCardShape),
        shape = ZomoCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Top Row: Pastel Squircle Icon + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 3D/Pastel Squircle Icon Tile
                Surface(
                    shape = ZomoTileShape,
                    color = tileBg,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = tileFg,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = occurrence.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "⏱️ ${occurrence.plannedMinutes} dakika" +
                                if (occurrence.type == TaskKind.WEEKLY && occurrence.targetCount != null)
                                    " • 🎯 ${occurrence.approvedCount}/${occurrence.targetCount}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge Pill
                StatusBadge(status = occurrence.status, warning = occurrence.warning)
            }

            // Parent Warning Note Banner
            if (occurrence.warning && !occurrence.warningText.isNullOrBlank()) {
                Surface(
                    shape = ZomoBannerShape,
                    color = ZomoAmberContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Uyarı",
                            tint = ZomoAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Not: ${occurrence.warningText}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // Action Pill Button Area
            when (occurrence.status) {
                OccurrenceStatus.PENDING -> {
                    if (occurrence.warning) {
                        Button(
                            onClick = onRetryClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZomoAmber,
                                contentColor = Color.White
                            ),
                            shape = ZomoPillShape
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dersi Tekrar Başlat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        // High-contrast Neon Mint Pill Button (Zomo style)
                        Button(
                            onClick = onStartClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZomoNeonMint,
                                contentColor = Color(0xFF042F2E)
                            ),
                            shape = ZomoPillShape
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF042F2E))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Çalışmayı Başlat", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }
                OccurrenceStatus.ACTIVE -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ZomoPillShape,
                        color = ZomoVioletContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Şu anda çalışılıyor... (Yüzen düğme aktif)",
                                color = ZomoPurplePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                OccurrenceStatus.WAITING_REVIEW -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ZomoPillShape,
                        color = ZomoAmberContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = ZomoAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⏳ Ebeveyn Onayı Bekliyor",
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                OccurrenceStatus.APPROVED -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ZomoPillShape,
                        color = ZomoEmeraldContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZomoEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✅ Görev Başarıyla Tamamlandı",
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
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
    val label: String
    val bg: Color
    val fg: Color

    if (warning) {
        label = "⚠️ Tekrar"
        bg = ZomoAmberContainer
        fg = Color(0xFF92400E)
    } else {
        when (status) {
            OccurrenceStatus.APPROVED -> {
                label = "✅ Onaylandı"
                bg = ZomoEmeraldContainer
                fg = Color(0xFF065F46)
            }
            OccurrenceStatus.WAITING_REVIEW -> {
                label = "⏳ İnceleniyor"
                bg = ZomoAmberContainer
                fg = Color(0xFF92400E)
            }
            OccurrenceStatus.ACTIVE -> {
                label = "⚡ Aktif"
                bg = ZomoVioletContainer
                fg = ZomoPurplePrimary
            }
            else -> {
                label = "Yapılacak"
                bg = ZomoLavenderCard
                fg = ZomoTextSecondary
            }
        }
    }

    Surface(
        color = bg,
        shape = ZomoPillShape
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}


