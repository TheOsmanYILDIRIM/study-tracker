package com.studytracker.feature.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.LocalOccurrenceRepositoryImpl
import com.studytracker.core.data.local.repository.LocalPlanRepositoryImpl
import com.studytracker.core.data.local.repository.LocalSessionRepositoryImpl
import com.studytracker.core.domain.model.*
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

private val FuturisticHeroShape = RoundedCornerShape(32.dp)
private val FuturisticCardShape = RoundedCornerShape(26.dp)
private val FuturisticSquircleShape = RoundedCornerShape(18.dp)
private val FuturisticPillShape = CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlanStudio: () -> Unit,
    onNavigateToSessionReview: (sessionId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val sessionRepo = remember { LocalSessionRepositoryImpl(db) }
    val occurrenceRepo = remember { LocalOccurrenceRepositoryImpl(db) }
    val planRepo = remember { LocalPlanRepositoryImpl(db) }

    val waitingSessions by remember(sessionRepo) { sessionRepo.getWaitingReviewSessions() }.collectAsState(initial = emptyList())
    val allOccurrences by remember(occurrenceRepo) { occurrenceRepo.getAllOccurrences() }.collectAsState(initial = emptyList())
    val activePlan by remember(planRepo) { planRepo.getActivePlan() }.collectAsState(initial = null)

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedDayFilter by remember { mutableStateOf("ALL") }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    val totalTasks = allOccurrences.size
    val approvedTasks = remember(allOccurrences) {
        allOccurrences.count { it.status == OccurrenceStatus.APPROVED }
    }

    val dailyOccurrences = remember(allOccurrences) {
        allOccurrences.filter { it.type == TaskKind.DAILY }
    }
    val weeklyOccurrences = remember(allOccurrences) {
        allOccurrences.filter { it.type == TaskKind.WEEKLY }
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
                            shape = RoundedCornerShape(14.dp),
                            color = ZomoEmeraldContainer,
                            border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = ZomoEmerald, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text("Ebeveyn Masası", fontWeight = FontWeight.Black, fontSize = 20.sp, color = ZomoTextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ZomoTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPlanStudio) {
                        Surface(
                            shape = FuturisticPillShape,
                            color = ZomoNeonMintContainer,
                            border = BorderStroke(1.dp, ZomoNeonMint.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Plan Stüdyosu", tint = ZomoNeonMint, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Futuristic Dark Pill Tabs
            Surface(
                color = ZomoDarkCanvas,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FuturisticPillShape)
                        .background(ZomoDarkSurface)
                        .border(1.dp, ZomoDarkBorder, FuturisticPillShape)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 1
                    Surface(
                        onClick = { selectedTabIndex = 0 },
                        shape = FuturisticPillShape,
                        color = if (selectedTabIndex == 0) ZomoPurplePrimary else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🚨 Onay Masası",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Black else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == 0) Color.White else ZomoTextSecondary
                            )
                            if (waitingSessions.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = FuturisticPillShape,
                                    color = ZomoPink,
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        "${waitingSessions.size}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tab 2
                    Surface(
                        onClick = { selectedTabIndex = 1 },
                        shape = FuturisticPillShape,
                        color = if (selectedTabIndex == 1) ZomoPurplePrimary else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📅 Haftalık Plan",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Black else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == 1) Color.White else ZomoTextSecondary
                            )
                        }
                    }
                }
            }

            if (selectedTabIndex == 0) {
                // TAB 1: Review Queue & Summary
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Plan Overview Hero Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(FuturisticHeroShape)
                                .background(ZomoHeroGradient)
                                .padding(22.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Aktif Hafta",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = activePlan?.weekId ?: "Plan Yüklenmedi",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        shape = FuturisticPillShape,
                                        color = ZomoNeonMint
                                    ) {
                                        Text(
                                            text = "$approvedTasks/$totalTasks Tamamlandı",
                                            color = ZomoNeonMintText,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Öğrenci: ${activePlan?.childId ?: "child_1"} • Zaman Dilimi: ${activePlan?.timezone ?: "Europe/Istanbul"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Quick Studio Action Button (Neon Mint Pill)
                    item {
                        Button(
                            onClick = onNavigateToPlanStudio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = FuturisticPillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZomoNeonMint,
                                contentColor = ZomoNeonMintText
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp), tint = ZomoNeonMintText)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🤖 AI Plan Stüdyosu & İçe/Dışa Aktar", fontWeight = FontWeight.Black, fontSize = 14.sp)
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
                                text = "🚨 Onay Bekleyen Oturumlar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = ZomoTextPrimary
                            )
                            if (waitingSessions.isNotEmpty()) {
                                Surface(
                                    shape = FuturisticPillShape,
                                    color = ZomoPinkContainer,
                                    border = BorderStroke(1.dp, ZomoPink.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        "${waitingSessions.size} bekliyor",
                                        color = ZomoPink,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (waitingSessions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = FuturisticCardShape,
                                border = BorderStroke(1.dp, ZomoDarkBorder),
                                colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = FuturisticSquircleShape,
                                            color = ZomoEmeraldContainer,
                                            border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.4f)),
                                            modifier = Modifier.size(54.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZomoEmerald, modifier = Modifier.size(28.dp))
                                            }
                                        }
                                        Text(
                                            "İncelenmeyi bekleyen oturum yok 🎉",
                                            fontWeight = FontWeight.Bold,
                                            color = ZomoTextSecondary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(waitingSessions, key = { it.sessionId }) { session ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = FuturisticCardShape,
                                colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                                border = BorderStroke(1.dp, ZomoAmber.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Surface(
                                                shape = FuturisticSquircleShape,
                                                color = ZomoAmberContainer,
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = ZomoAmber, modifier = Modifier.size(22.dp))
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = session.occurrenceKey,
                                                    fontWeight = FontWeight.Black,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = ZomoTextPrimary
                                                )
                                                Text(
                                                    text = "Başlama: ${timeFormat.format(Date(session.startTime))}",
                                                    fontSize = 11.sp,
                                                    color = ZomoTextSecondary
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = FuturisticPillShape,
                                            color = ZomoVioletContainer,
                                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "📸 ${session.screenshotCount} Kanıt",
                                                color = ZomoPurplePrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Action buttons (Pills)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onNavigateToSessionReview(session.sessionId) },
                                            modifier = Modifier.weight(1f),
                                            shape = FuturisticPillShape,
                                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.5f))
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp), tint = ZomoPurplePrimary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("İncele", color = ZomoPurplePrimary, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    sessionRepo.submitReview(
                                                        Review(
                                                            sessionId = session.sessionId,
                                                            occurrenceKey = session.occurrenceKey,
                                                            reviewStatus = ReviewStatus.APPROVED,
                                                            reviewNote = null,
                                                            reviewedAt = System.currentTimeMillis()
                                                        )
                                                    )
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ZomoNeonMint,
                                                contentColor = ZomoNeonMintText
                                            ),
                                            shape = FuturisticPillShape
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = ZomoNeonMintText)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Onayla", fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                // TAB 2: Full Weekly & Daily Plan Inspection View
                val filteredTasks = remember(dailyOccurrences, selectedDayFilter, activePlan) {
                    if (selectedDayFilter == "ALL") {
                        dailyOccurrences
                    } else {
                        val startDate = activePlan?.weekStartDate
                        if (startDate != null && startDate.isNotBlank()) {
                            val dayOffset = when (selectedDayFilter) {
                                "MON" -> 0
                                "TUE" -> 1
                                "WED" -> 2
                                "THU" -> 3
                                "FRI" -> 4
                                "SAT" -> 5
                                "SUN" -> 6
                                else -> 0
                            }
                            val cal = Calendar.getInstance(Locale.US)
                            try {
                                cal.time = dateFormat.parse(startDate) ?: Date()
                                cal.add(Calendar.DAY_OF_YEAR, dayOffset)
                                val targetDate = dateFormat.format(cal.time)
                                dailyOccurrences.filter { it.date == targetDate }
                            } catch (_: Exception) {
                                dailyOccurrences
                            }
                        } else {
                            dailyOccurrences
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Day Selector Horizontal Filter Chips
                    item(key = "day_filter_row") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🗓️ Gün Filtresi", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = ZomoTextPrimary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for ((key, label) in DAY_FILTERS) {
                                    val isSelected = selectedDayFilter == key
                                    Surface(
                                        onClick = { selectedDayFilter = key },
                                        shape = FuturisticPillShape,
                                        color = if (isSelected) ZomoPurplePrimary else ZomoDarkSurface,
                                        border = BorderStroke(1.dp, if (isSelected) ZomoPurplePrimary else ZomoDarkBorder)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            color = if (isSelected) Color.White else ZomoTextSecondary,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section Title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📖 Günlük Dersler (${filteredTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = ZomoTextPrimary
                            )
                        }
                    }

                    if (filteredTasks.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = FuturisticCardShape,
                                border = BorderStroke(1.dp, ZomoDarkBorder),
                                colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface)
                            ) {
                                Box(modifier = Modifier.padding(28.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Bu güne ait tanımlı ders bulunamadı.", color = ZomoTextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(filteredTasks, key = { "inspect_" + it.occurrenceKey }) { task ->
                            ParentTaskInspectionCard(task = task)
                        }
                    }

                    // Weekly Goals Section
                    if (selectedDayFilter == "ALL" || selectedDayFilter == "SUN" || selectedDayFilter == "SAT") {
                        item {
                            Text(
                                text = "🎯 Haftalık Genel Hedefler (${weeklyOccurrences.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = ZomoTextPrimary
                            )
                        }

                        if (weeklyOccurrences.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = FuturisticCardShape,
                                    border = BorderStroke(1.dp, ZomoDarkBorder),
                                    colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface)
                                ) {
                                    Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Haftalık genel hedef tanımlanmamış.", color = ZomoTextSecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            items(weeklyOccurrences, key = { "inspect_w_" + it.occurrenceKey }) { task ->
                                ParentTaskInspectionCard(task = task)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun ParentTaskInspectionCard(task: Occurrence) {
    val (statusBg, statusFg, statusText) = when (task.status) {
        OccurrenceStatus.APPROVED -> Triple(ZomoEmeraldContainer, ZomoEmerald, "✅ Onaylandı")
        OccurrenceStatus.WAITING_REVIEW -> Triple(ZomoAmberContainer, ZomoAmber, "⏳ İnceleniyor")
        OccurrenceStatus.ACTIVE -> Triple(ZomoVioletContainer, ZomoPurplePrimary, "⚡ Devam Ediyor")
        OccurrenceStatus.REJECTED -> Triple(ZomoPinkContainer, ZomoPink, "❌ Tekrar İsteniyor")
        OccurrenceStatus.PENDING -> Triple(Color(0x1FFFFFFF), ZomoTextSecondary, "🕒 Bekliyor")
        OccurrenceStatus.ARCHIVED -> Triple(Color(0x1FFFFFFF), ZomoTextMuted, "📦 Arşivlendi")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FuturisticCardShape,
        colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
        border = BorderStroke(1.dp, ZomoDarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    color = ZomoTextPrimary
                )
                Surface(
                    shape = FuturisticPillShape,
                    color = statusBg,
                    border = BorderStroke(1.dp, statusFg.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusText,
                        color = statusFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (task.date != null) {
                    Text(
                        text = "📅 ${task.date}",
                        fontSize = 12.sp,
                        color = ZomoTextSecondary
                    )
                }
                Text(
                    text = "⏱️ Hedef: ${task.plannedMinutes} dk",
                    fontSize = 12.sp,
                    color = ZomoTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!task.youtubeUrl.isNullOrBlank()) {
                Surface(
                    color = ZomoSkyContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ZomoSky.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔗 ${task.youtubeUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZomoSky,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (task.warning && !task.warningText.isNullOrBlank()) {
                Surface(
                    color = ZomoPinkContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ZomoPink.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ebeveyn Notu: ${task.warningText}",
                        color = ZomoPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
