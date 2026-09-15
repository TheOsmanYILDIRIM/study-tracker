package com.studytracker.feature.child

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.domain.manager.ActiveSessionState
import com.studytracker.core.domain.manager.SessionStateManager
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.ui.components.StudyTaskCard
import com.studytracker.core.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.*

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

    val activeState by stateManager.activeState.collectAsState()
    val isSessionActive = activeState != null

    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val dailyTasks by remember(occurrences, todayDate) {
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🚀 Görev Masam", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Bugün: $todayDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToRole) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTutorial) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Rehber")
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
            // Header Progress Card
            item(key = "progress_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Haftalık İlerlemen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("$approvedTasks / $totalTasks Tamamlandı", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            // Live Active / Paused Session Banner (Isolated Composable)
            if (isSessionActive) {
                item(key = "live_active_banner") {
                    LiveActiveSessionBanner(stateManager = stateManager)
                }
            }

            // Warning Banner for Rejected tasks
            if (rejectedTasks.isNotEmpty()) {
                item(key = "rejected_header") {
                    Text(
                        text = "⚠️ Tekrar Edilmesi Gereken Görevler (${rejectedTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                items(rejectedTasks, key = { "rej_" + it.occurrenceKey }) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = { stateManager.startSession(task.occurrenceKey, task.title) },
                        onRetryClick = { stateManager.startSession(task.occurrenceKey, task.title) }
                    )
                }
            }

            // Daily Tasks Section
            item(key = "daily_header") {
                Text(
                    text = "📅 Bugünkü Görevler (${dailyTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (dailyTasks.isEmpty()) {
                item(key = "daily_empty") {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Bugün için tanımlı görev bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(dailyTasks, key = { "daily_" + it.occurrenceKey }) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = { stateManager.startSession(task.occurrenceKey, task.title) },
                        onRetryClick = { stateManager.startSession(task.occurrenceKey, task.title) }
                    )
                }
            }

            // Weekly Tasks Section
            item(key = "weekly_header") {
                Text(
                    text = "🎯 Bu Haftanın Genel Hedefleri (${weeklyTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (weeklyTasks.isEmpty()) {
                item(key = "weekly_empty") {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Bu hafta için haftalık hedef bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(weeklyTasks, key = { "weekly_" + it.occurrenceKey }) { task ->
                    StudyTaskCard(
                        occurrence = task,
                        onStartClick = { stateManager.startSession(task.occurrenceKey, task.title) },
                        onRetryClick = { stateManager.startSession(task.occurrenceKey, task.title) }
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

    val mins = active.elapsedSeconds / 60
    val secs = active.elapsedSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mins, secs)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active.isPaused) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (active.isPaused) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = if (active.isPaused) MaterialTheme.colorScheme.error else EmeraldSuccess
                    )
                    Text(
                        text = if (active.isPaused) "⏸️ Ders Duraklatıldı" else "⚡ Ders Devam Ediyor",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = timeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Ders: ${active.occurrenceTitle} • 📸 ${active.screenshotCount} ekran görüntüsü",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { stateManager.togglePause() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active.isPaused) EmeraldSuccess else MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (active.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (active.isPaused) "Devam Et" else "Duraklat")
                }

                Button(
                    onClick = { stateManager.finishSession() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tamamla")
                }
            }
        }
    }
}

