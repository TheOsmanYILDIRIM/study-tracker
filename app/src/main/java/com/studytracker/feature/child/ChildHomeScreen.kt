package com.studytracker.feature.child

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
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

private val FuturisticHeroShape = RoundedCornerShape(32.dp)
private val FuturisticPillShape = CircleShape
private val FuturisticCardShape = RoundedCornerShape(26.dp)

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

    // High performance: Only observe boolean status change at screen level
    val isSessionActive by stateManager.isSessionActive.collectAsState()

    val handleStartSession: (Occurrence) -> Unit = remember(stateManager) {
        { task ->
            stateManager.startSession(task.occurrenceKey, task.title)
        }
    }

    val todayDate = remember {
        SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date())
    }

    // Direct memoized lists (zero state churn)
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

    Scaffold(
        containerColor = ZomoDarkCanvas,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZomoDarkCanvas,
                    titleContentColor = ZomoTextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ZomoVioletContainer,
                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column {
                            Text("Görev Masam", fontWeight = FontWeight.Black, fontSize = 20.sp, color = ZomoTextPrimary)
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
                            shape = FuturisticPillShape,
                            color = ZomoDarkSurface,
                            border = BorderStroke(1.dp, ZomoDarkBorder),
                            modifier = Modifier.size(38.dp)
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
            // Futuristic Hero Gradient Card (Cyber Violet & Neon Progress)
            item(key = "progress_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FuturisticHeroShape)
                        .background(ZomoHeroGradient)
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }

                            // Glowing Translucent Pill Badge
                            Surface(
                                shape = FuturisticPillShape,
                                color = Color.Black.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "$approvedTasks / $totalTasks Ders",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }

                        // Custom Neon Mint Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(FuturisticPillShape)
                                    .background(Color.Black.copy(alpha = 0.35f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = progress.coerceIn(0.04f, 1f))
                                        .clip(FuturisticPillShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(ZomoNeonMint, Color(0xFF38BDF8))
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
                                    color = ZomoNeonMint,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (totalTasks - approvedTasks > 0) "${totalTasks - approvedTasks} ders kaldı" else "Tüm dersler bitti! 🎉",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Live Active / Paused Session Banner
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = FuturisticPillShape,
                            color = ZomoPinkContainer,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Text(
                            text = "Tekrar Edilmesi Gerekenler (${rejectedTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ZomoPink
                        )
                    }
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
                        shape = FuturisticCardShape,
                        border = BorderStroke(1.dp, ZomoDarkBorder),
                        colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface)
                    ) {
                        Box(modifier = Modifier.padding(28.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Bugün için tanımlı görev bulunamadı. 🎉", color = ZomoTextSecondary, fontSize = 13.sp)
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
                        shape = FuturisticCardShape,
                        border = BorderStroke(1.dp, ZomoDarkBorder),
                        colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Bu hafta için haftalık hedef bulunamadı.", color = ZomoTextSecondary, fontSize = 13.sp)
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
        modifier = Modifier.fillMaxWidth(),
        shape = FuturisticCardShape,
        border = BorderStroke(1.5.dp, if (isPaused) ZomoAmber else ZomoPurplePrimary),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaused) Color(0xFF261808) else Color(0xFF1B0F38)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = if (isPaused) ZomoAmber else ZomoPurplePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = if (isPaused) "Ders Duraklatıldı" else "Ders Devam Ediyor ⚡",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPaused) ZomoAmber else ZomoTextPrimary
                    )
                }

                // Isolated micro timer text (observes elapsedSeconds independently!)
                LiveTimerText(stateManager = stateManager)
            }

            Text(
                text = "Ders: $title • 📸 $ssCount kanıt görüntüsü",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPaused) ZomoAmber.copy(alpha = 0.85f) else ZomoTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { stateManager.togglePause() },
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) ZomoNeonMint else Color(0x33A855F7),
                        contentColor = if (isPaused) ZomoNeonMintText else Color.White
                    ),
                    shape = FuturisticPillShape
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
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZomoPink,
                        contentColor = Color.White
                    ),
                    shape = FuturisticPillShape
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
    val elapsed by stateManager.elapsedSeconds.collectAsState()
    val mins = elapsed / 60
    val secs = elapsed % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mins, secs)

    Surface(
        shape = FuturisticPillShape,
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, ZomoNeonMint.copy(alpha = 0.4f))
    ) {
        Text(
            text = timeText,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            color = ZomoNeonMint,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
