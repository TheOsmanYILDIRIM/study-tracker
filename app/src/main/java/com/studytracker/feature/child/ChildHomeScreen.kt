package com.studytracker.feature.child

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.R
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.domain.manager.SessionStateManager
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.ui.components.StudyTaskCard
import com.studytracker.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val ZenHeroShape = RoundedCornerShape(22.dp)
private val ZenPillShape = CircleShape
private val ZenCardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildHomeScreen(
    onNavigateBackToRole: () -> Unit,
    onOpenTutorial: () -> Unit
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val isNightMode by appPreferences.isNightMode.collectAsState()

    val stateManager = remember { SessionStateManager.getInstance(context) }
    val occurrences by remember(stateManager) {
        stateManager.occurrenceRepository.getAllOccurrences()
    }.collectAsState(initial = emptyList())

    val isSessionActive by stateManager.isSessionActive.collectAsState()

    // Stable method reference
    val onTaskStart: (Occurrence) -> Unit = remember(stateManager) {
        { task -> stateManager.startSession(task.occurrenceKey, task.title) }
    }

    val todayDate = remember {
        SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR")).format(Date())
    }

    val dailyTasks = remember(occurrences) {
        occurrences.filter { it.type == TaskKind.DAILY }
    }

    val weeklyTasks = remember(occurrences) {
        occurrences.filter { it.type == TaskKind.WEEKLY }
    }

    val rejectedTasks = remember(occurrences) {
        occurrences.filter { it.warning }
    }

    val totalTasks = occurrences.size
    val approvedTasks = remember(occurrences) {
        occurrences.count { it.status == OccurrenceStatus.APPROVED }
    }
    val progress = remember(totalTasks, approvedTasks) {
        if (totalTasks > 0) approvedTasks.toFloat() / totalTasks else 0f
    }

    // High performance solid canvas background (zero alpha compositing fill-rate jank)
    Scaffold(
        containerColor = ZenNightCanvas,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZenNightCanvas,
                    titleContentColor = ZomoTextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ZenSkyCyanContainer)
                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Görev Masam", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ZomoTextPrimary)
                            Text(todayDate, style = MaterialTheme.typography.bodySmall, color = ZomoTextSecondary, fontSize = 11.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToRole) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ZomoTextPrimary)
                    }
                },
                actions = {
                    // Quick Day / Night Theme Toggle
                    IconButton(onClick = { appPreferences.toggleNightMode() }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(ZenPillShape)
                                .background(ZenPaperCard)
                                .border(1.dp, ZenPaperBorder, ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                contentDescription = "Tema Değiştir",
                                tint = if (isNightMode) ZenMoonGold else ZenSkyCyan,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    IconButton(onClick = onOpenTutorial) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(ZenPillShape)
                                .background(ZenPaperCard)
                                .border(1.dp, ZenPaperBorder, ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Rehber", tint = ZenSkyCyan, modifier = Modifier.size(17.dp))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Storybook Hero Artwork Card (Embedded as a crisp header card)
            item(key = "progress_hero_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(ZenHeroShape)
                        .border(1.dp, ZenPaperBorder, ZenHeroShape)
                ) {
                    Image(
                        painter = painterResource(id = if (isNightMode) R.drawable.bg_zen_night else R.drawable.bg_zen_day),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay for contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x33080D1A),
                                        Color(0xCC080D1A)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isNightMode) "Huzurlu Akşamlar ✨" else "Güzel Bir Gün ☀️",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bugünkü Hedeflerin",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(ZenPillShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), ZenPillShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$approvedTasks / $totalTasks Ders",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Compact Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(ZenPillShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = progress.coerceIn(if (totalTasks > 0) 0.03f else 0f, 1f))
                                        .clip(ZenPillShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(ZenSkyCyan, ZenMintSoft)
                                            )
                                        )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "%${(progress * 100).toInt()} Tamamlandı",
                                    color = ZenSkyCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (totalTasks - approvedTasks > 0) "${totalTasks - approvedTasks} ders kaldı" else "Tüm dersler bitti! 🎉",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Live Active Session Banner
            if (isSessionActive) {
                item(key = "live_active_banner") {
                    LiveActiveSessionBanner(stateManager = stateManager)
                }
            }

            // Warning Banner for Rejected tasks
            if (rejectedTasks.isNotEmpty()) {
                item(key = "rejected_header") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(ZenPillShape)
                                .background(ZenRoseCoral)
                        )
                        Text(
                            text = "Tekrar Edilmesi Gerekenler (${rejectedTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZenRoseCoral,
                            fontSize = 13.5.sp
                        )
                    }
                }

                items(
                    items = rejectedTasks,
                    key = { "rej_" + it.occurrenceKey },
                    contentType = { "study_task" }
                ) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = onTaskStart,
                        onRetryClick = onTaskStart,
                        modifier = Modifier.graphicsLayer {
                            shape = ZenCardShape
                            clip = true
                        }
                    )
                }
            }

            // Daily Tasks Section
            item(key = "daily_header") {
                Text(
                    text = "📅 Bugünkü Dersler (${dailyTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZomoTextPrimary,
                    fontSize = 14.sp
                )
            }

            if (dailyTasks.isEmpty()) {
                item(key = "daily_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                shape = ZenCardShape
                                clip = true
                            }
                            .background(ZenPaperCard)
                            .border(1.dp, ZenPaperBorder, ZenCardShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bugün için tanımlı ders bulunamadı. 🎉", color = ZomoTextSecondary, fontSize = 12.5.sp)
                    }
                }
            } else {
                items(
                    items = dailyTasks,
                    key = { "daily_" + it.occurrenceKey },
                    contentType = { "study_task" }
                ) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = onTaskStart,
                        onRetryClick = onTaskStart,
                        modifier = Modifier.graphicsLayer {
                            shape = ZenCardShape
                            clip = true
                        }
                    )
                }
            }

            // Weekly Tasks Section
            item(key = "weekly_header") {
                Text(
                    text = "🎯 Bu Haftanın Hedefleri (${weeklyTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZomoTextPrimary,
                    fontSize = 14.sp
                )
            }

            if (weeklyTasks.isEmpty()) {
                item(key = "weekly_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                shape = ZenCardShape
                                clip = true
                            }
                            .background(ZenPaperCard)
                            .border(1.dp, ZenPaperBorder, ZenCardShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bu hafta için haftalık hedef bulunamadı.", color = ZomoTextSecondary, fontSize = 12.5.sp)
                    }
                }
            } else {
                items(
                    items = weeklyTasks,
                    key = { "weekly_" + it.occurrenceKey },
                    contentType = { "study_task" }
                ) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = onTaskStart,
                        onRetryClick = onTaskStart,
                        modifier = Modifier.graphicsLayer {
                            shape = ZenCardShape
                            clip = true
                        }
                    )
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun LiveActiveSessionBanner(stateManager: SessionStateManager) {
    val activeState by stateManager.activeState.collectAsState()
    val active = activeState ?: return

    val isPaused = active.isPaused
    val title = active.occurrenceTitle
    val ssCount = active.screenshotCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ZenCardShape)
            .background(if (isPaused) Color(0xFF231808) else Color(0xFF0F2338))
            .border(1.5.dp, if (isPaused) ZenMoonGold else ZenSkyCyan, ZenCardShape)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = if (isPaused) ZenMoonGold else ZenSkyCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isPaused) "Ders Duraklatıldı" else "Ders Devam Ediyor ⚡",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 14.sp,
                        color = if (isPaused) ZenMoonGold else ZomoTextPrimary
                    )
                }

                LiveTimerText(stateManager = stateManager)
            }

            Text(
                text = "$title • 📸 $ssCount kanıt görüntüsü",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPaused) ZenMoonGold.copy(alpha = 0.85f) else ZomoTextSecondary,
                fontSize = 11.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { stateManager.togglePause() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) ZenSkyCyan else ZenPaperElevated,
                        contentColor = if (isPaused) ZenMintText else Color.White
                    ),
                    shape = ZenPillShape,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPaused) "Devam Et" else "Mola Ver", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                Button(
                    onClick = { stateManager.finishSession() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenRoseCoral,
                        contentColor = Color.White
                    ),
                    shape = ZenPillShape,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tamamla", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}

@Composable
fun LiveTimerText(stateManager: SessionStateManager) {
    val elapsed by stateManager.elapsedSeconds.collectAsState()
    val mins = elapsed / 60
    val secs = elapsed % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mins, secs)

    Box(
        modifier = Modifier
            .clip(ZenPillShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, ZenSkyCyan.copy(alpha = 0.4f), ZenPillShape)
            .padding(horizontal = 9.dp, vertical = 2.dp)
    ) {
        Text(
            text = timeText,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            fontFamily = FontFamily.Monospace,
            color = ZenSkyCyan
        )
    }
}
