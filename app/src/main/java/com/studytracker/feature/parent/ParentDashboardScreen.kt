package com.studytracker.feature.parent

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.R
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.LocalOccurrenceRepositoryImpl
import com.studytracker.core.data.local.repository.LocalPlanRepositoryImpl
import com.studytracker.core.data.local.repository.LocalSessionRepositoryImpl
import com.studytracker.core.domain.model.*
import com.studytracker.core.ui.components.ZenParallaxBackground
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import com.studytracker.core.data.local.repository.LocalQuizRepositoryImpl
import com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager
import com.studytracker.core.domain.model.Quiz
import com.studytracker.core.ui.components.CloudSyncDialog
import com.studytracker.core.ui.components.EditTaskDialog
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll

private val DAY_FILTERS = listOf(
    "ALL" to "Tüm Hafta",
    "MON" to "Pzt",
    "TUE" to "Sal",
    "WED" to "Çar",
    "THU" to "Per",
    "FRI" to "Cum",
    "SAT" to "Cmt",
    "SUN" to "Paz"
)

private val ZenCardShape = RoundedCornerShape(18.dp)
private val ZenSquircleShape = RoundedCornerShape(12.dp)
private val ZenPillShape = CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlanStudio: () -> Unit,
    onNavigateToSessionReview: (sessionId: String) -> Unit,
    onNavigateToQuizReview: (quizId: String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getInstance(context) }
    val sessionRepo = remember { LocalSessionRepositoryImpl(db) }
    val occurrenceRepo = remember { LocalOccurrenceRepositoryImpl(db) }
    val planRepo = remember { LocalPlanRepositoryImpl(db) }
    val quizRepo = remember { LocalQuizRepositoryImpl(db) }

    val waitingSessions by remember(sessionRepo) { sessionRepo.getWaitingReviewSessions() }.collectAsState(initial = emptyList())
    val allOccurrences by remember(occurrenceRepo) { occurrenceRepo.getAllOccurrences() }.collectAsState(initial = emptyList())
    val activePlan by remember(planRepo) { planRepo.getActivePlan() }.collectAsState(initial = null)
    val quizzes by quizRepo.getAllQuizzes().collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedDayFilter by remember { mutableStateOf("ALL") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Occurrence?>(null) }
    var sessionToReject by remember { mutableStateOf<Session?>(null) }
    var rejectNoteInput by remember { mutableStateOf("") }
    var showAIQuizDialog by remember { mutableStateOf(false) }

    // Auto-sync on startup
    LaunchedEffect(Unit) {
        CloudflareSyncManager.syncWithCloud(context)
    }

    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            CloudflareSyncManager.syncWithCloud(context)
            pullRefreshState.endRefresh()
        }
    }

    if (taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            onDismissRequest = { taskToEdit = null },
            onSaveTask = { updated ->
                scope.launch {
                    occurrenceRepo.updateOccurrence(updated)
                    val taskEntity = db.taskTemplateDao().getTaskById(updated.taskId)
                    if (taskEntity != null) {
                        db.taskTemplateDao().upsertTasks(listOf(
                            taskEntity.copy(
                                title = updated.title,
                                plannedMinutes = updated.plannedMinutes,
                                youtubeUrl = updated.youtubeUrl,
                                targetCount = updated.targetCount
                            )
                        ))
                    }
                    CloudflareSyncManager.syncWithCloud(context)
                    Toast.makeText(context, "✅ '${updated.title}' güncellendi ve bulutla eşitlendi", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteTask = { key ->
                scope.launch {
                    val occ = occurrenceRepo.getOccurrenceByKeyOnce(key)
                    occurrenceRepo.deleteOccurrence(key)
                    if (occ != null) {
                        val remainingOccs = db.occurrenceDao().getAllOccurrencesOnce()
                        if (remainingOccs.none { it.taskId == occ.taskId }) {
                            db.taskTemplateDao().deleteTask(occ.taskId)
                        }
                    }
                    CloudflareSyncManager.syncWithCloud(context)
                    Toast.makeText(context, "🗑️ Ders programdan ve buluttan silindi", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = ZenRoseCoral)
                    Text("Sıfırlama & Temizleme", fontWeight = FontWeight.Bold, color = ZomoTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Lütfen yapmak istediğiniz işlemi seçin:",
                        color = ZomoTextSecondary,
                        fontSize = 13.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF182642),
                        border = BorderStroke(1.dp, ZenPaperBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showResetConfirmDialog = false
                                scope.launch {
                                    val res = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.resetAllProgress(context, activePlan?.weekId)
                                    CloudflareSyncManager.syncWithCloud(context)
                                    res.onSuccess { msg ->
                                        Toast.makeText(context, "🔄 $msg", Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Hata: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🔄 Sadece Öğrenci İlerlemesini Sıfırla", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenMoonGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ders programı ve testler korunur. Sadece tamamlanan dersler, süreler ve onaylar sıfırlanır.", fontSize = 11.sp, color = ZomoTextSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ZenRoseCoral.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ZenRoseCoral.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showResetConfirmDialog = false
                                scope.launch {
                                    val res = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.clearAllData(context)
                                    CloudflareSyncManager.syncWithCloud(context)
                                    res.onSuccess { msg ->
                                        Toast.makeText(context, "🗑️ $msg", Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Hata: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🗑️ Tüm Planı & Testleri Komple Sil (Temiz Sayfa)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenRoseCoral)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tüm ders planları, görevler, testler ve geçmiş kayıtlar tamamen silinir. Bulut sıfırlanır.", fontSize = 11.sp, color = ZomoTextSecondary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Vazgeç", color = ZomoTextSecondary)
                }
            },
            containerColor = Color(0xFF10192E)
        )
    }

    if (sessionToReject != null) {
        val currentSession = sessionToReject!!
        AlertDialog(
            onDismissRequest = { sessionToReject = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = ZenRoseCoral)
                    Text("Oturumu Reddet & Not Yaz", fontWeight = FontWeight.Bold, color = ZomoTextPrimary, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Ders: ${currentSession.occurrenceKey}\nÖğrencinin bu çalışmasını yetersiz bulduysanız veya eksik kanıt varsa açıklama notu yazarak reddedebilirsiniz:",
                        color = ZomoTextSecondary,
                        fontSize = 12.5.sp
                    )
                    OutlinedTextField(
                        value = rejectNoteInput,
                        onValueChange = { rejectNoteInput = it },
                        placeholder = { Text("Örn: Kanıt ekranında ders içeriği görünmüyor, lütfen tekrar çalış.", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenRoseCoral,
                            unfocusedBorderColor = ZenPaperBorder,
                            focusedTextColor = ZomoTextPrimary,
                            unfocusedTextColor = ZomoTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val note = rejectNoteInput.ifBlank { "Ebeveyn tarafından eksik görüldü" }
                        sessionToReject = null
                        rejectNoteInput = ""
                        scope.launch {
                            sessionRepo.submitReview(
                                Review(
                                    sessionId = currentSession.sessionId,
                                    occurrenceKey = currentSession.occurrenceKey,
                                    reviewStatus = ReviewStatus.REJECTED,
                                    reviewNote = note,
                                    reviewedAt = System.currentTimeMillis()
                                )
                            )
                            occurrenceRepo.setWarning(currentSession.occurrenceKey, true, note)
                            Toast.makeText(context, "Ders reddedildi ve not iletildi.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenRoseCoral)
                ) {
                    Text("Reddet & Not Gönder", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToReject = null; rejectNoteInput = "" }) {
                    Text("Vazgeç", color = ZomoTextSecondary)
                }
            },
            containerColor = Color(0xFF10192E)
        )
    }

    if (showAIQuizDialog) {
        AIQuizStudioDialog(
            onDismissRequest = { showAIQuizDialog = false },
            onQuizCreated = {}
        )
    }

    if (showCloudSyncDialog) {
        com.studytracker.core.ui.components.CloudSyncDialog(
            isParent = true,
            onDismissRequest = { showCloudSyncDialog = false }
        )
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    val totalTasks = allOccurrences.size + quizzes.size
    val approvedTasks = remember(allOccurrences, quizzes) {
        allOccurrences.count { it.status == OccurrenceStatus.APPROVED } + quizzes.count { it.completed }
    }
    val approvedOccurrences = remember(allOccurrences) {
        allOccurrences.filter { it.status == OccurrenceStatus.APPROVED }
    }

    val dailyOccurrences = remember(allOccurrences) {
        allOccurrences.filter { it.type == TaskKind.DAILY }
    }

    val weeklyOccurrences = remember(allOccurrences) {
        allOccurrences.filter { it.type == TaskKind.WEEKLY }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        Scaffold(
            containerColor = ZenNightCanvas,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZenTopBarBackplate,
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
                                .background(ZenForestContainer)
                                .border(1.dp, ZenForestGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(20.dp))
                        }
                        Text("Ebeveyn Masası", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ZomoTextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ZomoTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showResetConfirmDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenRoseCoral.copy(alpha = 0.15f))
                                .border(1.dp, ZenRoseCoral.copy(alpha = 0.5f), ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "İlerlemeyi Sıfırla", tint = ZenRoseCoral, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = { showCloudSyncDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenSkyCyanContainer)
                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.5f), ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Bulut Senkronizasyonu", tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val file = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.exportPlanPackage(context)
                            com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.sharePackageFile(
                                context,
                                file,
                                "Haftalık Çalışma Planını Öğrenciye Gönder"
                            )
                        }
                    }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenForestGreen.copy(alpha = 0.25f))
                                .border(1.dp, ZenForestGreen.copy(alpha = 0.6f), ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Planı Paylaş", tint = ZenForestGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onNavigateToPlanStudio) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenSkyCyanContainer)
                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.5f), ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Plan Stüdyosu", tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            )
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Navigation Tabs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(ZenPillShape)
                    .background(ZenPaperCard)
                    .border(1.dp, ZenPaperBorder, ZenPillShape)
                    .padding(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ZenPillShape)
                            .background(if (selectedTabIndex == 0) ZenSkyCyan else Color.Transparent)
                            .clickable { selectedTabIndex = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "🚨 Öğrenci İcraat Masası",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (selectedTabIndex == 0) ZenMintText else ZomoTextSecondary
                            )
                            if (waitingSessions.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(ZenPillShape)
                                        .background(ZenRoseCoral)
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        "${waitingSessions.size}",
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Tab 2
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ZenPillShape)
                            .background(if (selectedTabIndex == 1) ZenSkyCyan else Color.Transparent)
                            .clickable { selectedTabIndex = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "📅 Haftalık Plan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedTabIndex == 1) ZenMintText else ZomoTextSecondary
                        )
                    }
                }
            }

            if (selectedTabIndex == 0) {
                // TAB 1: Student Accomplishments & Review Desk
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Student Performance Summary Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ZenCardShape)
                                .background(ZenPaperCard)
                                .border(1.dp, ZenPaperBorder, ZenCardShape)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "🎓 Öğrenci Çalışma Karnesi",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ZenMoonGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = activePlan?.weekId ?: "Haftalık Plan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ZomoTextPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(ZenPillShape)
                                            .background(ZenForestContainer)
                                            .border(1.dp, ZenForestGreen.copy(alpha = 0.4f), ZenPillShape)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$approvedTasks/$totalTasks Tamamlandı",
                                            color = ZenForestGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${waitingSessions.size} Onay Bekleyen • ${totalTasks - approvedTasks} Kalan Ders",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZomoTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                    Text(
                                        text = if (totalTasks > 0) "%${(approvedTasks * 100 / totalTasks)} Başarı Oranı" else "%0",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ZenSkyCyan,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Quick Studio Action Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToPlanStudio,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = ZenPillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZenSkyCyan,
                                    contentColor = Color(0xFF070B14)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Haftalık Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showAIQuizDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = ZenPillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color(0xFF070B14)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Test & Soru", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Waiting Review Queue Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚨 Öğrencinin Onay Bekleyen Oturumları",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ZomoTextPrimary,
                                fontSize = 14.sp
                            )
                            if (waitingSessions.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(ZenPillShape)
                                        .background(ZenRoseContainer)
                                        .border(1.dp, ZenRoseCoral.copy(alpha = 0.4f), ZenPillShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${waitingSessions.size} bekliyor",
                                        color = ZenRoseCoral,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (waitingSessions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ZenCardShape)
                                    .background(ZenPaperCard)
                                    .border(1.dp, ZenPaperBorder, ZenCardShape)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(ZenSquircleShape)
                                            .background(ZenForestContainer)
                                            .border(1.dp, ZenForestGreen.copy(alpha = 0.4f), ZenSquircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(24.dp))
                                    }
                                    Text(
                                        "İncelenmeyi bekleyen öğrenci oturumu yok 🎉",
                                        fontWeight = FontWeight.Bold,
                                        color = ZomoTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(waitingSessions, key = { it.sessionId }) { session ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        shape = ZenCardShape
                                        clip = true
                                    }
                                    .background(ZenPaperCard)
                                    .border(1.dp, ZenMoonGold.copy(alpha = 0.5f), ZenCardShape)
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(ZenSquircleShape)
                                                    .background(ZenMoonGoldContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(18.dp))
                                            }
                                            Column {
                                                Text(
                                                    text = "🎓 Öğrenci Tamamladı: ${session.occurrenceKey}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = ZomoTextPrimary,
                                                    fontSize = 13.5.sp
                                                )
                                                Text(
                                                    text = "Başlangıç: ${timeFormat.format(Date(session.startTime))}" +
                                                            (session.endTime?.let { " • Bitiş: ${timeFormat.format(Date(it))}" } ?: ""),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ZomoTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(ZenPillShape)
                                                .background(ZenSkyCyanContainer)
                                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.3f), ZenPillShape)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "📸 ${session.screenshotCount} Kanıt",
                                                color = ZenSkyCyan,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    session.studentNote?.takeIf { it.isNotBlank() }?.let { note ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF0F2D3D))
                                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.EditNote, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = "Öğrenci Notu: $note",
                                                    color = Color.White,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = { onNavigateToSessionReview(session.sessionId) },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            shape = ZenPillShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ZenSkyCyan,
                                                contentColor = ZenMintText
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp), tint = ZenMintText)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Kanıtları İncele", fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                        }

                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "Öğrenci çalışması onaylandı! 🌟", Toast.LENGTH_SHORT).show()
                                                scope.launch {
                                                    sessionRepo.submitReview(
                                                        Review(
                                                            sessionId = session.sessionId,
                                                            occurrenceKey = session.occurrenceKey,
                                                            reviewStatus = ReviewStatus.APPROVED,
                                                            reviewNote = "Ebeveyn tarafından onaylandı",
                                                            reviewedAt = System.currentTimeMillis()
                                                        )
                                                    )
                                                }
                                            },
                                            modifier = Modifier.height(36.dp),
                                            shape = ZenPillShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ZenForestGreen,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Aferin & Onayla", fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                sessionToReject = session
                                                rejectNoteInput = ""
                                            },
                                            modifier = Modifier.height(36.dp),
                                            shape = ZenPillShape,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ZenRoseCoral.copy(alpha = 0.6f)),
                                            contentPadding = PaddingValues(horizontal = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = ZenRoseCoral, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Reddet", color = ZenRoseCoral, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 📝 Öğrencinin Çözdüğü Testler & Sınavlar
                    if (quizzes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📝 Öğrencinin Testleri & Soru Sonuçları (${quizzes.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 13.5.sp
                                )
                                TextButton(
                                    onClick = { showAIQuizDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Test Ekle", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        items(quizzes, key = { "p_quiz_" + it.quizId }) { q ->
                            val isCompleted = q.completed
                            val totalQ = q.questions.size.coerceAtLeast(1)
                            val correctQ = q.correctCount
                            val wrongQ = q.wrongCount
                            val emptyQ = q.emptyCount
                            val successRate = (correctQ * 100) / totalQ

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ZenCardShape)
                                    .background(ZenPaperCard)
                                    .border(1.dp, if (isCompleted) Color(0xFF00E5FF).copy(alpha = 0.5f) else ZenPaperBorder, ZenCardShape)
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(ZenSquircleShape)
                                                    .background(if (isCompleted) ZenForestContainer else ZenSkyCyanContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isCompleted) Icons.Default.Assessment else Icons.Default.Quiz,
                                                    contentDescription = null,
                                                    tint = if (isCompleted) ZenForestGreen else Color(0xFF00E5FF),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = q.title,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ZomoTextPrimary,
                                                    fontSize = 13.sp,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = if (isCompleted)
                                                        "✅ $correctQ D • ❌ $wrongQ Y • ⚪ $emptyQ B • %$successRate Başarı"
                                                    else
                                                        "$totalQ Soru • ${q.durationMinutes} dk • Henüz Çözülmedi",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isCompleted) ZenForestGreen else ZomoTextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(ZenPillShape)
                                                .background(if (isCompleted) ZenForestContainer else Color(0x15FFFFFF))
                                                .border(1.dp, if (isCompleted) ZenForestGreen.copy(alpha = 0.5f) else ZenPaperBorder, ZenPillShape)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (isCompleted) "%$successRate Başarı" else "⏳ Bekliyor",
                                                color = if (isCompleted) ZenForestGreen else ZomoTextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (isCompleted && !q.studentNote.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0x6014203D),
                                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                                                Text(
                                                    text = q.studentNote,
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { onNavigateToQuizReview(q.quizId) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = ZenPillShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCompleted) Color(0xFF00E5FF) else ZenSkyCyanContainer,
                                            contentColor = if (isCompleted) Color(0xFF070B14) else ZenSkyCyan
                                        )
                                    ) {

                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (isCompleted) "Sonuçları & Çözümleri İncele" else "Soruları Önizle",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Öğrencinin Onaylanmış Başarıları & Geçmişi
                    if (approvedOccurrences.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🏆 Öğrencinin Başarıları & Onaylanan Dersler (${approvedOccurrences.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ZenForestGreen,
                                fontSize = 13.5.sp
                            )
                        }

                        items(approvedOccurrences, key = { "appr_" + it.occurrenceKey }) { occ ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ZenCardShape)
                                    .background(ZenPaperCard)
                                    .border(1.dp, ZenForestGreen.copy(alpha = 0.3f), ZenCardShape)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(ZenSquircleShape)
                                            .background(ZenForestContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(18.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = occ.title,
                                            fontWeight = FontWeight.Bold,
                                            color = ZomoTextPrimary,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${occ.plannedMinutes} dk" + (occ.date?.let { " • $it" } ?: "") +
                                                    if (occ.type == TaskKind.WEEKLY && occ.targetCount != null) " • 🎯 ${occ.approvedCount}/${occ.targetCount}" else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ZomoTextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                        if (!occ.studentNote.isNullOrBlank()) {
                                            Text(
                                                text = "📝 Öğrenci: ${occ.studentNote}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ZenSkyCyan,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(ZenPillShape)
                                            .background(ZenForestContainer)
                                            .border(1.dp, ZenForestGreen.copy(alpha = 0.5f), ZenPillShape)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "✅ Başarıyla Tamamlandı",
                                            color = ZenForestGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            } else {
                // TAB 2: Weekly Plan Overview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Day Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for ((code, label) in DAY_FILTERS) {
                            val isSelected = selectedDayFilter == code
                            Box(
                                modifier = Modifier
                                    .clip(ZenPillShape)
                                    .background(if (isSelected) ZenSkyCyan else ZenPaperCard)
                                    .border(1.dp, if (isSelected) ZenSkyCyan else ZenPaperBorder, ZenPillShape)
                                    .clickable { selectedDayFilter = code }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) ZenMintText else ZomoTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    val filteredDailyOccurrences = remember(dailyOccurrences, selectedDayFilter) {
                        if (selectedDayFilter == "ALL") {
                            dailyOccurrences
                        } else {
                            val dayIndex = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").indexOf(selectedDayFilter)
                            val calendar = Calendar.getInstance(Locale.US)

                            dailyOccurrences.filter { occ ->
                                val dateStr = occ.date ?: ""
                                try {
                                    val occDate = dateFormat.parse(dateStr)
                                    if (occDate != null) {
                                        calendar.time = occDate
                                        val occDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                                        val normalizedDay = when (occDayOfWeek) {
                                            Calendar.MONDAY -> "MON"
                                            Calendar.TUESDAY -> "TUE"
                                            Calendar.WEDNESDAY -> "WED"
                                            Calendar.THURSDAY -> "THU"
                                            Calendar.FRIDAY -> "FRI"
                                            Calendar.SATURDAY -> "SAT"
                                            Calendar.SUNDAY -> "SUN"
                                            else -> ""
                                        }
                                        normalizedDay == selectedDayFilter
                                    } else false
                                } catch (_: Exception) {
                                    true
                                }
                            }
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (selectedDayFilter == "ALL" && weeklyOccurrences.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🎯 Haftalık Hedefler (${weeklyOccurrences.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ZomoTextPrimary
                                )
                            }

                            items(weeklyOccurrences, key = { "wk_" + it.occurrenceKey }) { occ ->
                                OccurrenceAdminCard(occurrence = occ, onEdit = { taskToEdit = occ })
                            }
                        }

                        item {
                            Text(
                                text = "📅 Günlük Ders Görevleri (${filteredDailyOccurrences.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ZomoTextPrimary
                            )
                        }

                        if (filteredDailyOccurrences.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(ZenCardShape)
                                        .background(ZenPaperCard)
                                        .border(1.dp, ZenPaperBorder, ZenCardShape)
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Seçilen gün için tanımlı görev bulunamadı.", color = ZomoTextSecondary, fontSize = 12.5.sp)
                                }
                            }
                        } else {
                            items(filteredDailyOccurrences, key = { "dl_" + it.occurrenceKey }) { occ ->
                                OccurrenceAdminCard(occurrence = occ, onEdit = { taskToEdit = occ })
                            }
                        }

                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        }

        PullToRefreshContainer(
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Color(0xFF141F36),
            contentColor = ZenForestGreen
        )
    }
}

@Composable
fun OccurrenceAdminCard(
    occurrence: Occurrence,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val badgeText: String
    val badgeBg: Color
    val badgeFg: Color

    when (occurrence.status) {
        OccurrenceStatus.APPROVED -> {
            badgeText = "✅ Onaylandı"
            badgeBg = ZenForestContainer
            badgeFg = ZenForestGreen
        }
        OccurrenceStatus.WAITING_REVIEW -> {
            badgeText = "⏳ Onay Bekliyor"
            badgeBg = ZenMoonGoldContainer
            badgeFg = ZenMoonGold
        }
        OccurrenceStatus.ACTIVE -> {
            badgeText = "⚡ Çalışıyor"
            badgeBg = ZenSkyCyanContainer
            badgeFg = ZenSkyCyan
        }
        else -> {
            badgeText = "⚪ Yapılmadı"
            badgeBg = Color(0x15FFFFFF)
            badgeFg = ZomoTextSecondary
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                shape = ZenCardShape
                clip = true
            }
            .background(ZenPaperCard)
            .border(1.dp, if (occurrence.warning) ZenRoseCoral.copy(alpha = 0.6f) else ZenPaperBorder, ZenCardShape)
            .clickable { onEdit() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(ZenSquircleShape)
                        .background(if (occurrence.status == OccurrenceStatus.APPROVED) ZenForestContainer else ZenSkyCyanContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (occurrence.status == OccurrenceStatus.APPROVED) Icons.Default.Check else Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (occurrence.status == OccurrenceStatus.APPROVED) ZenForestGreen else ZenSkyCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = occurrence.title,
                        fontWeight = FontWeight.Bold,
                        color = ZomoTextPrimary,
                        fontSize = 13.5.sp,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${occurrence.plannedMinutes} dk" +
                                (occurrence.date?.let { " • $it" } ?: "") +
                                if (occurrence.type == TaskKind.WEEKLY && occurrence.targetCount != null)
                                    " • 🎯 ${occurrence.approvedCount}/${occurrence.targetCount}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZomoTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(ZenPillShape)
                            .background(badgeBg)
                            .border(1.dp, badgeFg.copy(alpha = 0.4f), ZenPillShape)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeFg,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF182642))
                            .border(0.8.dp, ZenPaperBorder, CircleShape)
                            .clickable { onEdit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = ZenSkyCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            if (occurrence.warning && !occurrence.warningText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZenRoseCoral.copy(alpha = 0.12f))
                        .border(1.dp, ZenRoseCoral.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ZenRoseCoral, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Reddedildi: ${occurrence.warningText}",
                            color = ZenRoseCoral,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!occurrence.studentNote.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZenSkyCyan.copy(alpha = 0.10f))
                        .border(1.dp, ZenSkyCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Öğrenci Notu: ${occurrence.studentNote}",
                            color = ZenSkyCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
