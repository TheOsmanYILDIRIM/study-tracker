package com.studytracker.core.ui.components

import androidx.compose.foundation.BorderStroke
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

private val FuturisticCardShape = RoundedCornerShape(26.dp)
private val FuturisticSquircleShape = RoundedCornerShape(18.dp)
private val FuturisticPillShape = CircleShape
private val FuturisticBannerShape = RoundedCornerShape(16.dp)

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
        occurrence.warning -> ZomoAmber.copy(alpha = 0.7f)
        occurrence.status == OccurrenceStatus.APPROVED -> ZomoEmerald.copy(alpha = 0.5f)
        occurrence.status == OccurrenceStatus.ACTIVE -> ZomoPurplePrimary
        else -> ZomoDarkBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = FuturisticCardShape),
        shape = FuturisticCardShape,
        colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Top Row: Futuristic Neon Squircle + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Futuristic Glowing Squircle Icon Container
                Surface(
                    shape = FuturisticSquircleShape,
                    color = tileBg,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = tileFg,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = occurrence.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ZomoTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "⏱️ ${occurrence.plannedMinutes} dakika" +
                                if (occurrence.type == TaskKind.WEEKLY && occurrence.targetCount != null)
                                    " • 🎯 ${occurrence.approvedCount}/${occurrence.targetCount}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZomoTextSecondary
                    )
                }

                // Status Badge Pill
                StatusBadge(status = occurrence.status, warning = occurrence.warning)
            }

            // Parent Warning Note Banner
            if (occurrence.warning && !occurrence.warningText.isNullOrBlank()) {
                Surface(
                    shape = FuturisticBannerShape,
                    color = ZomoAmberContainer,
                    border = BorderStroke(1.dp, ZomoAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                            text = "Ebeveyn Notu: ${occurrence.warningText}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ZomoAmber
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
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZomoAmber,
                                contentColor = Color(0xFF451A03)
                            ),
                            shape = FuturisticPillShape
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dersi Tekrar Başlat", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    } else {
                        // High-contrast Glowing Neon Mint Pill Button (Futuristic Cyber CTA)
                        Button(
                            onClick = onStartClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZomoNeonMint,
                                contentColor = ZomoNeonMintText
                            ),
                            shape = FuturisticPillShape
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp), tint = ZomoNeonMintText)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Çalışmayı Başlat", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
                OccurrenceStatus.ACTIVE -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = FuturisticPillShape,
                        color = ZomoVioletContainer,
                        border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Şu anda çalışılıyor... (Yüzen düğme aktif ⚡)",
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
                        shape = FuturisticPillShape,
                        color = ZomoAmberContainer,
                        border = BorderStroke(1.dp, ZomoAmber.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = ZomoAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⏳ Ebeveyn Onayı Bekliyor",
                                color = ZomoAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                OccurrenceStatus.APPROVED -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = FuturisticPillShape,
                        color = ZomoEmeraldContainer,
                        border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZomoEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✅ Görev Başarıyla Tamamlandı",
                                color = ZomoEmerald,
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
        fg = ZomoAmber
    } else {
        when (status) {
            OccurrenceStatus.APPROVED -> {
                label = "✅ Onaylandı"
                bg = ZomoEmeraldContainer
                fg = ZomoEmerald
            }
            OccurrenceStatus.WAITING_REVIEW -> {
                label = "⏳ İnceleniyor"
                bg = ZomoAmberContainer
                fg = ZomoAmber
            }
            OccurrenceStatus.ACTIVE -> {
                label = "⚡ Aktif"
                bg = ZomoVioletContainer
                fg = ZomoPurplePrimary
            }
            else -> {
                label = "🕒 Bekliyor"
                bg = Color(0x1FFFFFFF)
                fg = ZomoTextSecondary
            }
        }
    }

    Surface(
        color = bg,
        shape = FuturisticPillShape,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
