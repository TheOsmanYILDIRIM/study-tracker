package com.studytracker.feature.child

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.domain.manager.SessionStateManager
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.ui.components.StudyTaskCard
import com.studytracker.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val ZomoHeroCardShape = RoundedCornerShape(28.dp)
private val ZomoPillShape = CircleShape

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

    // High performance: Only observe boolean status change at screen level (prevents 1s recompositions!)
    val isSessionActive by stateManager.isSessionActive.collectAsState()

    val handleStartSession: (Occurrence) -> Unit = remember(stateManager) {
        { task ->
            stateManager.startSession(task.occurrenceKey, task.title)
        }
    }

    val todayDate = remember {
        SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date())
    }

    val dailyTasks by remember(occurrences) {
        derivedStateOf { occurrences.filter { it.type == TaskKind.DAILY } }
    }

    val weeklyTasks by remember(occurrences) {
        derivedStateOf { occurrences.filter { it.type == TaskKind.WEEKLY } }
    }

    val rejectedTasks by remember(occurrences) {
        derivedStateOf { occurrences.filter { it.warning } }
    }

    val totalTasks = occurrences.size
    val approvedTasks = remember(occurrences) {
        occurrences.count { it.status == OccurrenceStatus.APPROVED }
    }
    val progress = remember(totalTasks, approvedTasks) {
        if (totalTasks > 0) approvedTasks.toFloat() / totalTasks else 0f
    }

    Scaffold(
        containerColor = ZomoLavenderBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZomoLavenderBg,
                    titleContentColor = ZomoTextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ZomoPurplePrimary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text("Görev Masam", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ZomoTextPrimary)
                            Text(todayDate, style = MaterialTheme.typography.bodySmall, color = ZomoTextSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToRole) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ZomoTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTutorial) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(36.dp).shadow(2.dp, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HelpOutline, contentDescription = "Rehber", tint = ZomoPurplePrimary, modifier = Modifier.size(20.dp))
                            }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Zomo Hero Gradient Card (Cloud Storage / Goal Progress Style)
            item(key = "progress_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, shape = ZomoHeroCardShape, spotColor = ZomoPurplePrimary.copy(alpha = 0.35f))
                        .clip(ZomoHeroCardShape)
                        .background(ZomoHeroGradient)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Haftalık Çalışma Durumu",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Başarı İlerlemen 🚀",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            }

                            // Glowing Translucent Pill Badge
                            Surface(
                                shape = ZomoPillShape,
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "$approvedTasks / $totalTasks Ders",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Custom Neon Mint Progress Bar with Rounded Thumb
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(ZomoPillShape)
                                    .background(Color.Black.copy(alpha = 0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = progress.coerceIn(0.04f, 1f))
                                        .clip(ZomoPillShape)
                                        .background(ZomoNeonMint)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "%${(progress * 100).toInt()} Tamamlandı",
                                    color = ZomoNeonMint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (totalTasks - approvedTasks > 0) "${totalTasks - approvedTasks} ders kaldı" else "Tüm dersler bitti! 🎉",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Live Active / Paused Session Banner (Fully Isolated Composable)
            if (isSessionActive) {
                item(key = "live_active_banner") {
                    LiveActiveSessionBanner(stateManager = stateManager)
                }
            }

            // Warning Banner for Rejected tasks
            if (rejectedTasks.isNotEmpty()) {
                item(key = "rejected_header") {
                    Text(
                        text = "⚠️ Tekrar Edilmesi Gerekenler (${rejectedTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ZomoPink
                    )
                }

                items(rejectedTasks, key = { "rej_" + it.occurrenceKey }) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = { handleStartSession(task) },
                        onRetryClick = { handleStartSession(task) }
                    )
                }
            }

            // Daily Tasks Section
            item(key = "daily_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Bugünkü Görevler (${dailyTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ZomoTextPrimary
                    )
                }
            }

            if (dailyTasks.isEmpty()) {
                item(key = "daily_empty") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Bugün için tanımlı görev bulunamadı. 🎉", color = ZomoTextSecondary)
                        }
                    }
                }
            } else {
                items(dailyTasks, key = { "daily_" + it.occurrenceKey }) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = { handleStartSession(task) },
                        onRetryClick = { handleStartSession(task) }
                    )
                }
            }

            // Weekly Tasks Section
            item(key = "weekly_header") {
                Text(
                    text = "🎯 Bu Haftanın Genel Hedefleri (${weeklyTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ZomoTextPrimary
                )
            }

            if (weeklyTasks.isEmpty()) {
                item(key = "weekly_empty") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Bu hafta için haftalık hedef bulunamadı.", color = ZomoTextSecondary)
                        }
                    }
                }
            } else {
                items(weeklyTasks, key = { "weekly_" + it.occurrenceKey }) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = { handleStartSession(task) },
                        onRetryClick = { handleStartSession(task) }
                    )
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(24.dp)) }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = RoundedCornerShape(24.dp), spotColor = if (isPaused) ZomoAmber.copy(alpha = 0.3f) else ZomoPurplePrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaused) ZomoAmberContainer else ZomoVioletContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        tint = if (isPaused) ZomoAmber else ZomoPurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isPaused) "Ders Duraklatıldı" else "Ders Devam Ediyor",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPaused) Color(0xFF78350F) else ZomoPurpleDark
                    )
                }

                // Isolated micro timer text
                LiveTimerText(stateManager = stateManager)
            }

            Text(
                text = "Ders: $title • 📸 $ssCount ekran görüntüsü",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPaused) Color(0xFF92400E) else ZomoPurpleDark
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { stateManager.togglePause() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) ZomoNeonMint else Color.White,
                        contentColor = if (isPaused) Color(0xFF042F2E) else ZomoPurplePrimary
                    ),
                    shape = ZomoPillShape
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPaused) "Devam Et" else "Mola Ver", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { stateManager.finishSession() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZomoPink,
                        contentColor = Color.White
                    ),
                    shape = ZomoPillShape
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tamamla", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LiveTimerText(stateManager: SessionStateManager) {
    val activeState by stateManager.activeState.collectAsState()
    val elapsed = activeState?.elapsedSeconds ?: 0L
    val mins = elapsed / 60
    val secs = elapsed % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mins, secs)

    Text(
        text = timeText,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        fontFamily = FontFamily.Monospace,
        color = ZomoPurplePrimary
    )
}



