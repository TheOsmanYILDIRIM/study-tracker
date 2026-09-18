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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.ui.theme.*

import android.content.Context

private val ZenCardShape = RoundedCornerShape(16.dp)
private val ZenSquircleShape = RoundedCornerShape(12.dp)
private val ZenPillShape = CircleShape

private val URL_FIND_REGEX = Regex("""(https?://[^\s|"'<>)]+|(?:\bwww\.|(?:\bm\.)?youtube\.com/|youtu\.be/)[^\s|"'<>)]+)""", RegexOption.IGNORE_CASE)

fun sanitizeUrl(rawUrl: String): String {
    var u = rawUrl.trim()
    u = u.removePrefix("<").removeSuffix(">")
        .removePrefix("(").removeSuffix(")")
        .removePrefix("[").removeSuffix("]")
        .removePrefix("\"").removeSuffix("\"")
        .removePrefix("'").removeSuffix("'")
        .trim()

    while (u.isNotEmpty() && (u.endsWith(")") || u.endsWith("]") || u.endsWith(".") || u.endsWith(",") || u.endsWith(";") || u.endsWith(">"))) {
        u = u.dropLast(1).trim()
    }

    if (u.isBlank()) return ""

    return when {
        u.startsWith("http://", ignoreCase = true) || u.startsWith("https://", ignoreCase = true) -> u
        u.startsWith("youtu.be/", ignoreCase = true) ||
        u.startsWith("youtube.com/", ignoreCase = true) ||
        u.startsWith("www.youtube.com/", ignoreCase = true) ||
        u.startsWith("m.youtube.com/", ignoreCase = true) -> "https://$u"
        u.startsWith("www.", ignoreCase = true) -> "https://$u"
        else -> if (u.contains("youtu.be") || u.contains("youtube.com")) {
            if (!u.startsWith("http://") && !u.startsWith("https://")) "https://$u" else u
        } else if (!u.startsWith("http://") && !u.startsWith("https://") && (u.contains(".com") || u.contains(".org") || u.contains(".net") || u.contains(".edu"))) {
            "https://$u"
        } else u
    }
}

fun extractVideoUrl(occurrence: Occurrence): String? {
    if (!occurrence.youtubeUrl.isNullOrBlank()) {
        val sanitized = sanitizeUrl(occurrence.youtubeUrl)
        if (sanitized.isNotBlank()) return sanitized
    }

    val matchTitle = URL_FIND_REGEX.find(occurrence.title)
    if (matchTitle != null) {
        val sanitized = sanitizeUrl(matchTitle.value)
        if (sanitized.isNotBlank()) return sanitized
    }

    val matchNote = occurrence.studentNote?.let { URL_FIND_REGEX.find(it) }
    if (matchNote != null) {
        val sanitized = sanitizeUrl(matchNote.value)
        if (sanitized.isNotBlank()) return sanitized
    }

    val matchWarning = occurrence.warningText?.let { URL_FIND_REGEX.find(it) }
    if (matchWarning != null) {
        val sanitized = sanitizeUrl(matchWarning.value)
        if (sanitized.isNotBlank()) return sanitized
    }

    return null
}

fun extractYoutubeVideoId(url: String): String? {
    val clean = sanitizeUrl(url)
    if (clean.isBlank()) return null

    val vParamRegex = Regex("""[?&]v=([a-zA-Z0-9_-]{11})""")
    val matchV = vParamRegex.find(clean)
    if (matchV != null) return matchV.groupValues[1]

    val youtuBeRegex = Regex("""youtu\.be/([a-zA-Z0-9_-]{11})""")
    val matchYt = youtuBeRegex.find(clean)
    if (matchYt != null) return matchYt.groupValues[1]

    val embedRegex = Regex("""youtube\.com/(?:embed|shorts)/([a-zA-Z0-9_-]{11})""")
    val matchEmbed = embedRegex.find(clean)
    if (matchEmbed != null) return matchEmbed.groupValues[1]

    return null
}

fun openVideoUrl(context: Context, rawUrl: String) {
    val cleanUrl = sanitizeUrl(rawUrl)
    if (cleanUrl.isBlank()) {
        android.widget.Toast.makeText(context, "Video linki bulunamadı", android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    val videoId = extractYoutubeVideoId(cleanUrl)

    // 1. YouTube App Intent (vnd.youtube: scheme - opens directly in official app, ReVanced, NewPipe if supported)
    if (!videoId.isNullOrBlank()) {
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appIntent)
            return
        } catch (_: Exception) {
            // Fallthrough to universal browser / app-links intent
        }
    }

    // 2. Universal HTTPS Intent (Handles Android App Links & Default Browsers)
    try {
        val webUri = Uri.parse(cleanUrl)
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        return
    } catch (_: Exception) {
        // Direct launch failed, fallthrough to chooser
    }

    // 3. Fallback to App Chooser
    try {
        val webUri = Uri.parse(cleanUrl)
        val chooserIntent = Intent.createChooser(
            Intent(Intent.ACTION_VIEW, webUri).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            "Dersi / Videoyu Aç"
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Video açılamadı: ${e.localizedMessage ?: cleanUrl}", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
fun StudyTaskCard(
    occurrence: Occurrence,
    onStartClick: (Occurrence) -> Unit,
    onRetryClick: (Occurrence) -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveVideoUrl = extractVideoUrl(occurrence)
    val displayTitle = occurrence.title
        .replace(URL_FIND_REGEX, "")
        .replace(Regex("""\s*\|\s*"""), " • ")
        .trim()
        .trim('•', ' ', '-', '|')
        .ifBlank { occurrence.title }

    val tileBg: Color
    val tileFg: Color
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector

    when {
        effectiveVideoUrl != null || occurrence.title.contains("video", ignoreCase = true) || occurrence.title.contains("izle", ignoreCase = true) -> {
            tileBg = ZenRoseContainer
            tileFg = ZenRoseCoral
            iconVector = Icons.Default.SmartDisplay
        }
        occurrence.title.contains("anki", ignoreCase = true) || occurrence.title.contains("kart", ignoreCase = true) -> {
            tileBg = ZenMintContainer
            tileFg = ZenMintSoft
            iconVector = Icons.Default.FlipToFront
        }
        occurrence.type == TaskKind.WEEKLY -> {
            tileBg = ZenMoonGoldContainer
            tileFg = ZenMoonGold
            iconVector = Icons.Default.EmojiEvents
        }
        occurrence.title.contains("kitap", ignoreCase = true) || occurrence.title.contains("paragraf", ignoreCase = true) || occurrence.title.contains("edebiyat", ignoreCase = true) || occurrence.title.contains("tde", ignoreCase = true) -> {
            tileBg = ZenSkyCyanContainer
            tileFg = ZenSkyCyan
            iconVector = Icons.Default.MenuBook
        }
        occurrence.title.contains("mat", ignoreCase = true) -> {
            tileBg = ZenSkyCyanContainer
            tileFg = ZenSkyCyan
            iconVector = Icons.Default.Calculate
        }
        occurrence.title.contains("fizik", ignoreCase = true) || occurrence.title.contains("kimya", ignoreCase = true) || occurrence.title.contains("biyo", ignoreCase = true) || occurrence.title.contains("fen", ignoreCase = true) -> {
            tileBg = ZenForestContainer
            tileFg = ZenForestGreen
            iconVector = Icons.Default.Biotech
        }
        occurrence.title.contains("cografya", ignoreCase = true) || occurrence.title.contains("coğrafya", ignoreCase = true) -> {
            tileBg = ZenForestContainer
            tileFg = ZenForestGreen
            iconVector = Icons.Default.Public
        }
        occurrence.title.contains("tarih", ignoreCase = true) -> {
            tileBg = ZenMoonGoldContainer
            tileFg = ZenMoonGold
            iconVector = Icons.Default.HistoryEdu
        }
        occurrence.title.contains("ingilizce", ignoreCase = true) || occurrence.title.contains("almanca", ignoreCase = true) || occurrence.title.contains("dil", ignoreCase = true) -> {
            tileBg = ZenLavenderContainer
            tileFg = ZenLavender
            iconVector = Icons.Default.Translate
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZomoTextPrimary,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
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

        // Video Link Action Button
        if (effectiveVideoUrl != null) {
            val context = LocalContext.current
            val validUrl = effectiveVideoUrl
            Surface(
                onClick = { openVideoUrl(context, validUrl) },
                shape = RoundedCornerShape(10.dp),
                color = Color(0x33E11D48),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, ZenRoseCoral),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(ZenRoseCoral, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎬 Videoyu / Dersi Aç (YouTube)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = validUrl,
                            color = ZenRoseCoral.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = ZenRoseCoral,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Student Note Banner
        if (!occurrence.studentNote.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ZenSkyCyan.copy(alpha = 0.08f))
                    .border(1.dp, ZenSkyCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = ZenSkyCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Notum: ${occurrence.studentNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZenSkyCyan,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
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
            fontSize = 13.sp,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
