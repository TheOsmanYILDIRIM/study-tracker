package com.studytracker.feature.child

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.studytracker.core.ui.components.StudyWeeklyTaskCard
import com.studytracker.core.ui.components.ZenParallaxBackground
import com.studytracker.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val ZenPillShape = CircleShape
private val ZenCardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildHomeScreen(
    onNavigateBackToRole: () -> Unit,
    onOpenTutorial: () -> Unit
) {
    val context = LocalContext.current
    val stateManager = remember { SessionStateManager.getInstance(context) }
    val occurrences by remember(stateManager) {
        stateManager.occurrenceRepository.getAllOccurrences()
    }.collectAsState(initial = emptyList())

    val isSessionActive by stateManager.isSessionActive.collectAsState()

    // Stable method reference
    val onTaskStart: (Occurrence) -> Unit = remember(stateManager) {
        { task -> stateManager.startSession(task.occurrenceKey, task.title) }
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

    val appPreferences = remember { AppPreferences.getInstance(context) }
    val isTestModeEnabled by appPreferences.isTestModeEnabled.collectAsState()
    val testProgressOverride by appPreferences.testProgressOverride.collectAsState()
    val testFlyingStarTrigger by appPreferences.testFlyingStarTrigger.collectAsState()

    val completedTasksCount = remember(occurrences) {
        occurrences.count {
            it.status == OccurrenceStatus.APPROVED ||
            it.status == OccurrenceStatus.WAITING_REVIEW
        }
    }
    val totalTasksCount = remember(occurrences) {
        occurrences.size.coerceAtLeast(1)
    }

    var localFlyingStarTrigger by remember { mutableStateOf(0L) }
    var showSyncDialog by remember { mutableStateOf(false) }
    val effectiveFlyingStarTrigger = remember(localFlyingStarTrigger, testFlyingStarTrigger) {
        maxOf(localFlyingStarTrigger, testFlyingStarTrigger)
    }

    if (showSyncDialog) {
        com.studytracker.core.ui.components.CloudSyncDialog(
            onDismissRequest = { showSyncDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ZenParallaxBackground(
            completedTasksCount = completedTasksCount,
            totalTasksCount = totalTasksCount,
            progressOverride = testProgressOverride,
            flyingStarTrigger = effectiveFlyingStarTrigger
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xB3080D1A),
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
                                    .border(1.dp, ZenSkyCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    "Çalışma Masam",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ZomoTextPrimary
                                )
                                Text(
                                    SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR")).format(Date()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZomoTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBackToRole) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ZomoTextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSyncDialog = true }) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ZenSkyCyanContainer, ZenPillShape)
                                    .border(1.dp, ZenSkyCyan.copy(alpha = 0.4f), ZenPillShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = "Bulut Senkronizasyonu", tint = ZenSkyCyan, modifier = Modifier.size(17.dp))
                            }
                        }
                        IconButton(onClick = onOpenTutorial) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ZenPaperCard, ZenPillShape)
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🧪 TEST MODU CANLI ÖNİZLEME SLIDERI (Yalnızca test modu açıkken görünür)
                if (isTestModeEnabled) {
                    item(key = "test_mode_preview_card") {
                        val currentSliderVal = testProgressOverride ?: (completedTasksCount.toFloat() / totalTasksCount.coerceAtLeast(1))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ZenCardShape)
                                .background(Color(0xE60D1929))
                                .border(1.5.dp, ZenMoonGold.copy(alpha = 0.7f), ZenCardShape)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(18.dp))
                                        Text(
                                            "🧪 Canlı Parlaklık & Yıldız Simülatörü",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = ZenMoonGold
                                        )
                                    }
                                    Text(
                                        "${(currentSliderVal * 100).toInt()}% Full",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ZenSkyCyan
                                    )
                                }

                                Text(
                                    "Slider'ı kaydırarak arkaplanın canlanmasını, parlamasını ve takımyıldızlarını test edin:",
                                    fontSize = 11.sp,
                                    color = ZomoTextSecondary
                                )

                                Slider(
                                    value = currentSliderVal,
                                    onValueChange = { appPreferences.setTestProgressOverride(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ZenMoonGold,
                                        activeTrackColor = ZenSkyCyan,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(28.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            localFlyingStarTrigger = System.currentTimeMillis()
                                        },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        shape = ZenPillShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = ZenMoonGold, contentColor = Color(0xFF451A03)),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✨ Yıldız Uçur", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    if (testProgressOverride != null) {
                                        OutlinedButton(
                                            onClick = { appPreferences.setTestProgressOverride(null) },
                                            modifier = Modifier.height(34.dp),
                                            shape = ZenPillShape,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.5f)),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Gerçek Veri", fontSize = 10.5.sp, color = ZenSkyCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Live Active Session Banner (Shows when session is ongoing or paused)
                if (isSessionActive) {
                    item(key = "live_active_banner") {
                        LiveActiveSessionBanner(
                            stateManager = stateManager,
                            onFinishClick = {
                                localFlyingStarTrigger = System.currentTimeMillis()
                                stateManager.finishSession()
                            }
                        )
                    }
                }

                // 1. Warning Banner & Tasks for Rejected items
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

                // 2. Weekly Goals Section AT TOP in a Horizontal LazyRow
                if (weeklyTasks.isNotEmpty()) {
                    item(key = "weekly_header") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(ZenPillShape)
                                    .background(ZenMoonGold)
                            )
                            Text(
                                text = "🎯 Haftalık Hedefler (${weeklyTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ZenMoonGold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    item(key = "weekly_row") {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(
                                items = weeklyTasks,
                                key = { "w_" + it.occurrenceKey },
                                contentType = { "weekly_task" }
                            ) { task ->
                                StudyWeeklyTaskCard(
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
                    }
                }

                // 3. Daily Tasks Section Vertical
                item(key = "daily_header") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(ZenPillShape)
                                .background(ZenSkyCyan)
                        )
                        Text(
                            text = "📅 Bugünkü Dersler (${dailyTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZomoTextPrimary,
                            fontSize = 14.sp
                        )
                    }
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

                item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun LiveActiveSessionBanner(
    stateManager: SessionStateManager,
    onFinishClick: () -> Unit = { stateManager.finishSession() }
) {
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
                // 1. Pause / Resume Button
                Button(
                    onClick = { stateManager.togglePause() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) ZenSkyCyan else ZenPaperElevated,
                        contentColor = if (isPaused) ZenMintText else Color.White
                    ),
                    shape = ZenPillShape,
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(if (isPaused) "Devam Et" else "Mola Ver", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // 2. Complete Button
                Button(
                    onClick = onFinishClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenForestGreen,
                        contentColor = Color.White
                    ),
                    shape = ZenPillShape,
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Bitir", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // 3. Cancel / Abandon Button
                Button(
                    onClick = { stateManager.cancelSession() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x33FF4D4F),
                        contentColor = ZenRoseCoral
                    ),
                    shape = ZenPillShape,
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("İptal Et", fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
