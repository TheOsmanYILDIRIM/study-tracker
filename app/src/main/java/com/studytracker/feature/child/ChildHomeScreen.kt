package com.studytracker.feature.child

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.studytracker.core.ui.components.extractVideoUrl
import com.studytracker.core.ui.components.openVideoUrl
import com.studytracker.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.LocalQuizRepositoryImpl
import com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager
import com.studytracker.core.domain.model.Quiz
import com.studytracker.core.service.StudyAccessibilityService
import com.studytracker.core.ui.components.CloudSyncDialog
import com.studytracker.core.ui.components.PermissionGuideDialog
import android.provider.Settings
import android.os.Build
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll

private val ZenPillShape = CircleShape
private val ZenCardShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildHomeScreen(
    onNavigateBackToRole: () -> Unit,
    onOpenTutorial: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val quizRepo = remember { LocalQuizRepositoryImpl(db) }
    val quizzes by quizRepo.getAllQuizzes().collectAsState(initial = emptyList())

    val stateManager = remember { SessionStateManager.getInstance(context) }
    val occurrences by remember(stateManager) {
        stateManager.occurrenceRepository.getAllOccurrences()
    }.collectAsState(initial = emptyList())

    val isSessionActive by stateManager.isSessionActive.collectAsState()

    // Stable method reference with robust video launcher
    val onTaskStart: (Occurrence) -> Unit = remember(stateManager, context) {
        { task ->
            stateManager.startSession(task.occurrenceKey, task.title)
            val vUrl = extractVideoUrl(task)
            if (!vUrl.isNullOrBlank()) {
                openVideoUrl(context, vUrl)
            }
        }
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

    val completedTasksCount = remember(occurrences, quizzes) {
        occurrences.count {
            it.status == OccurrenceStatus.APPROVED ||
            it.status == OccurrenceStatus.WAITING_REVIEW
        } + quizzes.count { it.completed }
    }
    val totalTasksCount = remember(occurrences, quizzes) {
        (occurrences.size + quizzes.size).coerceAtLeast(1)
    }

    var localFlyingStarTrigger by remember { mutableStateOf(0L) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showPermissionGuideDialog by remember { mutableStateOf(false) }
    var showFinishNoteDialog by remember { mutableStateOf(false) }
    
    val hasOverlayPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
    }
    val hasAccessibilityPermission = remember {
        StudyAccessibilityService.isAccessibilityServiceEnabled(context)
    }

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

    // Detect newly approved tasks by parent and trigger the shooting star ascent!
    var previousApprovedKeys by remember { 
        mutableStateOf(occurrences.filter { it.status == OccurrenceStatus.APPROVED }.map { it.occurrenceKey }.toSet()) 
    }
    
    LaunchedEffect(occurrences) {
        val currentApproved = occurrences.filter { it.status == OccurrenceStatus.APPROVED }
        val newlyApproved = currentApproved.filter { it.occurrenceKey !in previousApprovedKeys }
        if (newlyApproved.isNotEmpty()) {
            localFlyingStarTrigger = System.currentTimeMillis()
            val taskName = newlyApproved.first().title
            Toast.makeText(context, "🌟 Tebrikler! '$taskName' velin tarafından onaylandı!", Toast.LENGTH_LONG).show()
        }
        previousApprovedKeys = currentApproved.map { it.occurrenceKey }.toSet()
    }

    // Öğrenci Öz Değerlendirme Durumları
    var evalUnderstanding by remember { mutableStateOf("Harika") }
    var evalFocus by remember { mutableStateOf("%100 Odak") }
    var evalQuestionsCount by remember { mutableStateOf("") }
    var studentNoteInput by remember { mutableStateOf("") }

    if (showPermissionGuideDialog) {
        PermissionGuideDialog(
            onDismissRequest = { showPermissionGuideDialog = false }
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
                    Text("Sıfırlama Seçenekleri", fontWeight = FontWeight.Bold, color = ZomoTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Lütfen yapmak istediğiniz sıfırlama işlemini seçin:",
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
                                    if (isSessionActive) {
                                        stateManager.cancelSession()
                                    }
                                    val res = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.resetAllProgress(context)
                                    res.onSuccess { msg ->
                                        Toast.makeText(context, "🔄 $msg", Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Hata: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🔄 Sadece Çalışma Sürelerini & Puanları Sıfırla", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenMoonGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ders programı korunur. Sadece tamamlanan dersler sıfırlanır.", fontSize = 11.sp, color = ZomoTextSecondary)
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
                                    if (isSessionActive) {
                                        stateManager.cancelSession()
                                    }
                                    val res = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.clearAllData(context)
                                    res.onSuccess { msg ->
                                        Toast.makeText(context, "🗑️ $msg", Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Hata: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🗑️ Tüm Planı & Dersleri Sıfırla (Temiz Masa)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenRoseCoral)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tüm dersler, testler ve kayıtlar tamamen temizlenir.", fontSize = 11.sp, color = ZomoTextSecondary)
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

    if (showFinishNoteDialog) {
        AlertDialog(
            onDismissRequest = { showFinishNoteDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Celebration, contentDescription = null, tint = ZenSkyCyan)
                    Text("Ders Değerlendirmesi 🎉", fontWeight = FontWeight.Bold, color = ZomoTextPrimary, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Dersi tamamladın! Çalışmanı değerlendir, veline rapor olarak iletilecektir:",
                        color = ZomoTextSecondary,
                        fontSize = 12.sp
                    )

                    // 1. Konuyu Anlama Düzeyi
                    Text("1. Konuyu ne kadar iyi anladın?", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.5.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Harika" to "🌟 Harika",
                            "İyi" to "👍 İyi",
                            "Zorlandım" to "🤔 Zorlandım",
                            "Anlamadım" to "❌ Zayıf"
                        ).forEach { (key, label) ->
                            val isSelected = evalUnderstanding == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ZenForestGreen.copy(alpha = 0.3f) else Color(0xFF182238),
                                border = BorderStroke(1.dp, if (isSelected) ZenForestGreen else ZenPaperBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { evalUnderstanding = key }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else ZomoTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 2. Odaklanma Seviyesi
                    Text("2. Odaklanma ve verimin nasıldı?", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.5.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "%100 Odak" to "⚡ %100 Odak",
                            "İyi" to "🎯 İyi",
                            "Dağıldı" to "📱 Dağıldı"
                        ).forEach { (key, label) ->
                            val isSelected = evalFocus == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ZenSkyCyan.copy(alpha = 0.3f) else Color(0xFF182238),
                                border = BorderStroke(1.dp, if (isSelected) ZenSkyCyan else ZenPaperBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { evalFocus = key }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else ZomoTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 3. Soru Sayısı / Hedef
                    Text("3. Çözülen Soru Sayısı (Varsa):", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.5.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("10", "20", "30", "50").forEach { count ->
                            val isSelected = evalQuestionsCount == count
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.3f) else Color(0xFF182238),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF8B5CF6) else ZenPaperBorder),
                                modifier = Modifier.clickable { evalQuestionsCount = count }
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                    Text(
                                        text = "$count Soru",
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else ZomoTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // 4. Öğrenci Notu / Detay
                    Text("4. Veline Notun / Zorlandığın Konular:", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.5.sp)
                    OutlinedTextField(
                        value = studentNoteInput,
                        onValueChange = { studentNoteInput = it },
                        placeholder = { Text("Örn: Formülleri iyi anladım, soru çözümü yaptım...", fontSize = 11.5.sp, color = ZomoTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenSkyCyan,
                            unfocusedBorderColor = ZenPaperBorder,
                            focusedTextColor = ZomoTextPrimary,
                            unfocusedTextColor = ZomoTextPrimary,
                            cursorColor = ZenSkyCyan
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parts = mutableListOf<String>()
                        if (evalUnderstanding.isNotBlank()) parts.add("⭐ Anlama: $evalUnderstanding")
                        if (evalFocus.isNotBlank()) parts.add("⚡ Odak: $evalFocus")
                        if (evalQuestionsCount.isNotBlank()) parts.add("🎯 $evalQuestionsCount Soru")
                        if (studentNoteInput.isNotBlank()) parts.add("📝 ${studentNoteInput.trim()}")

                        val compiledNote = if (parts.isNotEmpty()) parts.joinToString(" | ") else null

                        showFinishNoteDialog = false
                        localFlyingStarTrigger = System.currentTimeMillis()
                        stateManager.finishSession(studentNote = compiledNote)
                        
                        // Formu sıfırla
                        studentNoteInput = ""
                        evalQuestionsCount = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen),
                    shape = ZenPillShape
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Değerlendirmeyi Kaydet & Bitir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishNoteDialog = false }) {
                    Text("Geri Dön", color = ZomoTextSecondary)
                }
            },
            containerColor = Color(0xFF10192E)
        )
    }

    if (showCloudSyncDialog) {
        com.studytracker.core.ui.components.CloudSyncDialog(
            isParent = false,
            onDismissRequest = { showCloudSyncDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        ZenParallaxBackground(
            completedTasksCount = completedTasksCount,
            totalTasksCount = totalTasksCount,
            flyingStarTrigger = localFlyingStarTrigger
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
                        IconButton(onClick = { showPermissionGuideDialog = true }) {
                            val allGranted = hasOverlayPermission && hasAccessibilityPermission
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(if (allGranted) ZenForestGreen.copy(alpha = 0.2f) else ZenMoonGold.copy(alpha = 0.25f), ZenPillShape)
                                    .border(1.dp, if (allGranted) ZenForestGreen.copy(alpha = 0.5f) else ZenMoonGold, ZenPillShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "İzin Rehberi",
                                    tint = if (allGranted) ZenForestGreen else ZenMoonGold,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                        IconButton(onClick = { showResetConfirmDialog = true }) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ZenRoseCoral.copy(alpha = 0.15f), ZenPillShape)
                                    .border(1.dp, ZenRoseCoral.copy(alpha = 0.5f), ZenPillShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "İlerlemeyi Sıfırla", tint = ZenRoseCoral, modifier = Modifier.size(17.dp))
                            }
                        }
                        IconButton(onClick = { showCloudSyncDialog = true }) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ZenSkyCyanContainer, ZenPillShape)
                                    .border(1.dp, ZenSkyCyan.copy(alpha = 0.5f), ZenPillShape),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 0. Belirgin Kanıt Alma & Sayaç Hizmeti Açma Kartı
                    if (!hasOverlayPermission || !hasAccessibilityPermission) {
                        item(key = "permission_service_alert") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF2A1B0E),
                                border = BorderStroke(1.5.dp, ZenMoonGold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPermissionGuideDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(ZenMoonGold.copy(alpha = 0.2f))
                                            .border(1.dp, ZenMoonGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = ZenMoonGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🛡️ Kanıt Alma & Sayaç Hizmeti",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ZenMoonGold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Sürenin sayılması ve ekran kanıtı alınabilmesi için hizmeti açın.",
                                            fontSize = 11.5.sp,
                                            color = ZomoTextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }

                                    Button(
                                        onClick = { showPermissionGuideDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ZenMoonGold,
                                            contentColor = Color(0xFF080D1A)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Hizmeti Aç",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
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
                                    studentNoteInput = ""
                                    showFinishNoteDialog = true
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

                // 3. Daily Tasks & Quizzes Section (Dersler ve Testler Birlikte)
                val totalDailyCount = dailyTasks.size + quizzes.size
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
                            text = "📅 Bugünkü Dersler & Görevler ($totalDailyCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZomoTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }

                if (dailyTasks.isEmpty() && quizzes.isEmpty()) {
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
                            Text("Bugün için tanımlی ders veya test bulunamadı. 🎉", color = ZomoTextSecondary, fontSize = 12.5.sp)
                        }
                    }
                } else {
                    // Günlük Ders Görevleri
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

                    // Günlük Testler (Dersler ile aynı formatta, aralarında gösterilir)
                    items(
                        items = quizzes,
                        key = { "quiz_" + it.quizId },
                        contentType = { "quiz_task" }
                    ) { quiz ->
                        StudyQuizCard(
                            quiz = quiz,
                            onStartClick = { onNavigateToQuiz(quiz.quizId) },
                            modifier = Modifier.graphicsLayer {
                                shape = ZenCardShape
                                clip = true
                            }
                        )
                    }
                }

                item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
            }

            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color(0xFF141F36),
                contentColor = ZenSkyCyan
            )
        }
    }
}
}

@Composable
fun LiveActiveSessionBanner(
    stateManager: SessionStateManager,
    onFinishClick: () -> Unit = { stateManager.finishSession() }
) {
    val context = LocalContext.current
    val activeState by stateManager.activeState.collectAsState()
    val active = activeState ?: return

    val isPaused = active.isPaused
    val title = active.occurrenceTitle
    val ssCount = active.screenshotCount

    val activeOcc by remember(active.session.occurrenceKey) {
        stateManager.occurrenceRepository.getOccurrenceByKey(active.session.occurrenceKey)
    }.collectAsState(initial = null)
    val videoUrl = activeOcc?.let { extractVideoUrl(it) } ?: run {
        val m = Regex("""(https?://[^\s|"'<>)]+|(?:\bwww\.|(?:\bm\.)?youtube\.com/|youtu\.be/)[^\s|"'<>)]+)""", RegexOption.IGNORE_CASE).find(title)
        m?.value?.let { com.studytracker.core.ui.components.sanitizeUrl(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ZenCardShape)
            .background(if (isPaused) Color(0xFF261908) else ZenSkyCyanContainer)
            .border(1.2.dp, if (isPaused) ZenMoonGold else ZenSkyCyan, ZenCardShape)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(ZenPillShape)
                            .background(if (isPaused) ZenMoonGold else ZenForestGreen)
                    )
                    Text(
                        text = if (isPaused) "Ders Duraklatıldı" else "Ders Devam Ediyor",
                        color = if (isPaused) ZenMoonGold else ZenSkyCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "📸 $ssCount Kanıt",
                    color = ZomoTextSecondary,
                    fontSize = 11.5.sp
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ZomoTextPrimary,
                fontSize = 15.sp,
                maxLines = 1
            )

            // 🎬 Büyük Belirgin Tıklanabilir Video Kartı
            if (!videoUrl.isNullOrBlank()) {
                Surface(
                    onClick = { openVideoUrl(context, videoUrl) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33E11D48),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, ZenRoseCoral),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ZenRoseCoral, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎬 Videoyu / Dersi İzle (YouTube)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = videoUrl,
                                color = ZenRoseCoral.copy(alpha = 0.9f),
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = ZenRoseCoral,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Button
                Button(
                    onClick = {
                        if (isPaused) stateManager.resumeSession() else stateManager.pauseSession()
                    },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = ZenPillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) ZenMoonGold else Color(0x3000E5FF),
                        contentColor = if (isPaused) Color(0xFF451A03) else ZenSkyCyan
                    )
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPaused) "Devam Et" else "Mola Ver", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                // Finish Button
                Button(
                    onClick = onFinishClick,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = ZenPillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bitir", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
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

/**
 * Görev kartlarıyla birebir aynı boyut ve tasarımda, dersler arasında doğal olarak duran Test Kartı.
 */
@Composable
fun StudyQuizCard(
    quiz: Quiz,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = quiz.completed
    val totalQuestions = quiz.questions.size

    val borderColor = if (isCompleted) ZenForestGreen.copy(alpha = 0.5f) else Color(0xFF00E5FF).copy(alpha = 0.4f)
    val cardBg = if (isCompleted) Color(0xFF09171C) else ZenPaperCard

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ZenCardShape)
            .background(cardBg)
            .border(1.dp, borderColor, ZenCardShape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Squircle Icon Box (Ders kartlarıyla aynı boyutta)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCompleted) ZenForestContainer else Color(0xFF10283A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Quiz,
                    contentDescription = null,
                    tint = if (isCompleted) ZenForestGreen else Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Middle: Title & Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZomoTextPrimary,
                    fontSize = 14.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "📝 $totalQuestions Soru • ⏱ ${quiz.durationMinutes} dk",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZomoTextSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }

            // Right Side Action Pill
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .background(ZenForestContainer, ZenPillShape)
                        .border(1.dp, ZenForestGreen.copy(alpha = 0.5f), ZenPillShape)
                        .clickable { onStartClick() }
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
                            tint = ZenForestGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Tekrar Çöz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ZenForestGreen
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .background(Color(0xFF00E5FF), ZenPillShape)
                        .clickable { onStartClick() }
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
                            tint = Color(0xFF070B14),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Testi Çöz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF070B14)
                        )
                    }
                }
            }
        }
    }
}
