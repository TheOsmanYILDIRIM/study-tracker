package com.studytracker.feature.child

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.R
import com.studytracker.core.domain.manager.SessionStateManager
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.ui.components.StudyTaskCard
import com.studytracker.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val ZenHeroShape = RoundedCornerShape(24.dp)
private val ZenPillShape = CircleShape
private val ZenCardShape = RoundedCornerShape(18.dp)

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

    // Stable method reference to prevent recomposition storms in items
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

    // Full-screen Storybook Paper Cutout Scene Root (Hardware accelerated static background)
    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 0: Vertical 9:16 Paper Cutout Illustration
        Image(
            painter = painterResource(id = R.drawable.bg_zen_night),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 1: Soft Translucent Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x77080D1A),
                            Color(0xB3080D1A),
                            Color(0xEB080D1A)
                        )
                    )
                )
        )

        // Layer 2: Scaffold with zero opacity background for ultra-smooth scrolling
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = ZomoTextPrimary
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ZenSkyCyanContainer,
                                border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.35f)),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text("Görev Masam", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ZomoTextPrimary)
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
                        IconButton(onClick = onOpenTutorial) {
                            Surface(
                                shape = ZenPillShape,
                                color = ZenPaperCard,
                                border = BorderStroke(1.dp, ZenPaperBorder),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = "Rehber", tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Storybook Layered Paper Progress Hero Card
                item(key = "progress_card") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 4.dp, shape = ZenHeroShape, spotColor = Color.Black),
                        shape = ZenHeroShape,
                        color = ZenPaperCard,
                        border = BorderStroke(1.dp, ZenPaperBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "İyi Çalışmalar ✨",
                                        color = ZenMoonGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Bugünkü Hedeflerin",
                                        color = ZomoTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Surface(
                                    shape = ZenPillShape,
                                    color = ZenSkyCyanContainer,
                                    border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "$approvedTasks / $totalTasks Ders",
                                        color = ZenSkyCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Compact Paper Progress Bar
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(9.dp)
                                        .clip(ZenPillShape)
                                        .background(Color.Black.copy(alpha = 0.4f))
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
                                        text = if (totalTasks - approvedTasks > 0) "${totalTasks - approvedTasks} ders kaldı" else "Tüm dersler tamamlandı! 🎉",
                                        color = ZomoTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal
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
                                shape = ZenPillShape,
                                color = ZenRoseContainer,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Text(
                                text = "Tekrar Edilmesi Gerekenler (${rejectedTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ZenRoseCoral,
                                fontSize = 14.5.sp
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
                            onStartClick = { onTaskStart(task) },
                            onRetryClick = { onTaskStart(task) }
                        )
                    }
                }

                // Daily Tasks Section
                item(key = "daily_header") {
                    Text(
                        text = "Bugünkü Dersler (${dailyTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZomoTextPrimary,
                        fontSize = 15.sp
                    )
                }

                if (dailyTasks.isEmpty()) {
                    item(key = "daily_empty") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ZenCardShape,
                            border = BorderStroke(1.dp, ZenPaperBorder),
                            color = ZenPaperCard
                        ) {
                            Box(modifier = Modifier.padding(18.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Bugün için tanımlı ders bulunamadı. 🎉", color = ZomoTextSecondary, fontSize = 12.5.sp)
                            }
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
                            onStartClick = { onTaskStart(task) },
                            onRetryClick = { onTaskStart(task) }
                        )
                    }
                }

                // Weekly Tasks Section
                item(key = "weekly_header") {
                    Text(
                        text = "Bu Haftanın Hedefleri (${weeklyTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ZomoTextPrimary,
                        fontSize = 15.sp
                    )
                }

                if (weeklyTasks.isEmpty()) {
                    item(key = "weekly_empty") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = ZenCardShape,
                            border = BorderStroke(1.dp, ZenPaperBorder),
                            color = ZenPaperCard
                        ) {
                            Box(modifier = Modifier.padding(18.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Bu hafta için haftalık hedef bulunamadı.", color = ZomoTextSecondary, fontSize = 12.5.sp)
                            }
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
                            onStartClick = { onTaskStart(task) },
                            onRetryClick = { onTaskStart(task) }
                        )
                    }
                }

                item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(18.dp)) }
            }
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = ZenCardShape, spotColor = Color.Black),
        shape = ZenCardShape,
        border = BorderStroke(1.5.dp, if (isPaused) ZenMoonGold else ZenSkyCyan),
        color = if (isPaused) Color(0xF0231808) else Color(0xF00F2338)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        tint = if (isPaused) ZenMoonGold else ZenSkyCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isPaused) "Ders Duraklatıldı" else "Ders Devam Ediyor ⚡",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 14.5.sp,
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
                    modifier = Modifier.weight(1f).height(38.dp),
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
                    modifier = Modifier.weight(1f).height(38.dp),
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

    Surface(
        shape = ZenPillShape,
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.35f))
    ) {
        Text(
            text = timeText,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = ZenSkyCyan,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
        )
    }
}
