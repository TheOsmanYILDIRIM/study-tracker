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
import androidx.compose.ui.draw.shadow
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

    val dailyOccurrences by remember(allOccurrences) {
        derivedStateOf { allOccurrences.filter { it.type == TaskKind.DAILY } }
    }
    val weeklyOccurrences by remember(allOccurrences) {
        derivedStateOf { allOccurrences.filter { it.type == TaskKind.WEEKLY } }
    }

    Scaffold(
        containerColor = ZomoBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZomoBackground,
                    titleContentColor = Color(0xFF1E1B4B)
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ZomoSquirclePurple,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text("Ebeveyn Masası", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E1B4B))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPlanStudio) {
                        Surface(
                            shape = CircleShape,
                            color = ZomoMintAccent.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Plan Stüdyosu", tint = Color(0xFF0F766E), modifier = Modifier.size(18.dp))
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
            // Zomo Pill Tabs
            Surface(
                color = ZomoBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(ZomoSoftLavender)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 1
                    Surface(
                        onClick = { selectedTabIndex = 0 },
                        shape = RoundedCornerShape(50),
                        color = if (selectedTabIndex == 0) Color.White else Color.Transparent,
                        shadowElevation = if (selectedTabIndex == 0) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🚨 Onay Masası",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == 0) ZomoPurplePrimary else Color(0xFF6B7280)
                            )
                            if (waitingSessions.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        "${waitingSessions.size}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tab 2
                    Surface(
                        onClick = { selectedTabIndex = 1 },
                        shape = RoundedCornerShape(50),
                        color = if (selectedTabIndex == 1) Color.White else Color.Transparent,
                        shadowElevation = if (selectedTabIndex == 1) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📅 Haftalık Plan",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == 1) ZomoPurplePrimary else Color(0xFF6B7280)
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(10.dp, shape = RoundedCornerShape(28.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ZomoHeroGradient)
                                    .padding(20.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = ZomoMintAccent
                                        ) {
                                            Text(
                                                text = "$approvedTasks/$totalTasks Tamamlandı",
                                                color = Color(0xFF064E3B),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Öğrenci: ${activePlan?.childId ?: "child_1"} • Zaman Dilimi: ${activePlan?.timezone ?: "Europe/Istanbul"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Studio Action Button (Neon Mint Pill)
                    item {
                        Button(
                            onClick = onNavigateToPlanStudio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(4.dp, shape = RoundedCornerShape(50), spotColor = ZomoMintAccent.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZomoMintAccent,
                                contentColor = Color(0xFF064E3B)
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🤖 AI Plan Stüdyosu & İçe/Dışa Aktar", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
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
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1B4B)
                            )
                            if (waitingSessions.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ZomoSquirclePink
                                ) {
                                    Text(
                                        "${waitingSessions.size} bekliyor",
                                        color = Color(0xFFBE123C),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (waitingSessions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, shape = RoundedCornerShape(24.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.06f)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = ZomoCardBackground)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = ZomoSquircleEmerald,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                                            }
                                        }
                                        Text(
                                            "İncelenmeyi bekleyen oturum yok 🎉",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4B5563),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(waitingSessions, key = { it.sessionId }) { session ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, shape = RoundedCornerShape(24.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
                                border = BorderStroke(1.dp, ZomoSquircleAmber.copy(alpha = 0.5f))
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
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = ZomoSquircleAmber,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = session.occurrenceKey,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Color(0xFF1E1B4B)
                                                )
                                                Text(
                                                    text = "Başlama: ${timeFormat.format(Date(session.startTime))}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF6B7280)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = ZomoSoftLavender
                                        ) {
                                            Text(
                                                text = "📸 ${session.screenshotCount} Kanıt",
                                                color = ZomoPurplePrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.3f))
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
                                                containerColor = ZomoMintAccent,
                                                contentColor = Color(0xFF064E3B)
                                            ),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Onayla", fontWeight = FontWeight.ExtraBold)
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
                            Text("🗓️ Gün Filtresi", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1B4B))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for ((key, label) in DAY_FILTERS) {
                                    val isSelected = selectedDayFilter == key
                                    Surface(
                                        onClick = { selectedDayFilter = key },
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) ZomoPurplePrimary else ZomoCardBackground,
                                        border = BorderStroke(1.dp, if (isSelected) ZomoPurplePrimary else ZomoSquirclePurple.copy(alpha = 0.4f)),
                                        shadowElevation = if (isSelected) 2.dp else 0.dp
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF4B5563),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
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
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1B4B)
                            )
                        }
                    }

                    if (filteredTasks.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, shape = RoundedCornerShape(24.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.06f)),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = ZomoCardBackground)
                            ) {
                                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Bu güne ait tanımlı ders bulunamadı.", color = Color(0xFF6B7280), fontSize = 13.sp)
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
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1B4B)
                            )
                        }

                        if (weeklyOccurrences.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(2.dp, shape = RoundedCornerShape(24.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.06f)),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = ZomoCardBackground)
                                ) {
                                    Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Haftalık genel hedef tanımlanmamış.", color = Color(0xFF6B7280), fontSize = 13.sp)
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
        OccurrenceStatus.APPROVED -> Triple(ZomoSquircleEmerald, Color(0xFF059669), "✅ Onaylandı")
        OccurrenceStatus.WAITING_REVIEW -> Triple(ZomoSquircleAmber, Color(0xFFD97706), "⏳ İnceleniyor")
        OccurrenceStatus.ACTIVE -> Triple(ZomoSquirclePurple, ZomoPurplePrimary, "⚡ Devam Ediyor")
        OccurrenceStatus.REJECTED -> Triple(ZomoSquirclePink, Color(0xFFE11D48), "❌ Tekrar İsteniyor")
        OccurrenceStatus.PENDING -> Triple(ZomoSoftLavender, Color(0xFF6B7280), "🕒 Bekliyor")
        OccurrenceStatus.ARCHIVED -> Triple(ZomoSoftLavender, Color(0xFF9CA3AF), "📦 Arşivlendi")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(20.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
        border = BorderStroke(1.dp, ZomoSquirclePurple.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1E1B4B)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        color = statusFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (task.date != null) {
                    Text(
                        text = "📅 ${task.date}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                Text(
                    text = "⏱️ Hedef: ${task.plannedMinutes} dk",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }

            if (!task.youtubeUrl.isNullOrBlank()) {
                Surface(
                    color = ZomoSquircleBlue,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔗 ${task.youtubeUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (task.warning && !task.warningText.isNullOrBlank()) {
                Surface(
                    color = ZomoSquirclePink,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ebeveyn Notu: ${task.warningText}",
                        color = Color(0xFFBE123C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
