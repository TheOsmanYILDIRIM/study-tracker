package com.studytracker.feature.parent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private val ZenHeroShape = RoundedCornerShape(22.dp)
private val ZenCardShape = RoundedCornerShape(16.dp)
private val ZenSquircleShape = RoundedCornerShape(12.dp)
private val ZenPillShape = CircleShape

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
    var showSyncDialog by remember { mutableStateOf(false) }

    if (showSyncDialog) {
        com.studytracker.core.ui.components.CloudSyncDialog(
            onDismissRequest = { showSyncDialog = false }
        )
    }

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
                                .background(ZenForestGreenContainer)
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
                    IconButton(onClick = { showSyncDialog = true }) {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "🚨 Onay Masası",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
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
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "📅 Haftalık Plan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = if (selectedTabIndex == 1) ZenMintText else ZomoTextSecondary
                        )
                    }
                }
            }

            if (selectedTabIndex == 0) {
                // TAB 1: Review Queue & Summary
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Plan Overview Summary Card without duplicate picture
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
                                            text = "Aktif Çalışma Planı",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ZenMoonGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = activePlan?.weekId ?: "Plan Yüklenmedi",
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
                                        text = "${dailyOccurrences.size} Günlük • ${weeklyOccurrences.size} Haftalık Görev",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZomoTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                    Text(
                                        text = if (totalTasks > 0) "%${(approvedTasks * 100 / totalTasks)} İlerleme" else "%0",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ZenSkyCyan,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Quick Studio Action Button
                    item {
                        Button(
                            onClick = onNavigateToPlanStudio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = ZenPillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ZenSkyCyan,
                                contentColor = ZenMintText
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = ZenMintText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🤖 AI Plan Stüdyosu & İçe/Dışa Aktar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                        "İncelenmeyi bekleyen oturum yok 🎉",
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
                                                    text = session.occurrenceKey,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ZomoTextPrimary,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Başlangıç: ${timeFormat.format(Date(session.startTime))}" +
                                                            (session.endTime?.let { " • Bitiş: ${timeFormat.format(Date(it))}" } ?: ""),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ZomoTextSecondary,
                                                    fontSize = 11.5.sp
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

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onNavigateToSessionReview(session.sessionId) },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            shape = ZenPillShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ZenSkyCyan,
                                                contentColor = ZenMintText
                                            )
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = ZenMintText)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Kanıtları İncele", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    sessionRepo.submitReview(
                                                        Review(
                                                            sessionId = session.sessionId,
                                                            occurrenceKey = session.occurrenceKey,
                                                            reviewStatus = ReviewStatus.APPROVED,
                                                            reviewNote = "Ebeveyn tarafından hızlı onaylandı",
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
                                            )
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Hızlı Onayla", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }
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
                                OccurrenceAdminCard(occurrence = occ)
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
                                OccurrenceAdminCard(occurrence = occ)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}
}

@Composable
fun OccurrenceAdminCard(
    occurrence: Occurrence,
    modifier: Modifier = Modifier
) {
    val badgeText: String
    val badgeBg: Color
    val badgeFg: Color

    when (occurrence.status) {
        OccurrenceStatus.APPROVED -> {
            badgeText = "Onaylandı"
            badgeBg = ZenForestContainer
            badgeFg = ZenForestGreen
        }
        OccurrenceStatus.WAITING_REVIEW -> {
            badgeText = "İnceleniyor"
            badgeBg = ZenMoonGoldContainer
            badgeFg = ZenMoonGold
        }
        OccurrenceStatus.ACTIVE -> {
            badgeText = "Aktif"
            badgeBg = ZenSkyCyanContainer
            badgeFg = ZenSkyCyan
        }
        else -> {
            badgeText = "Bekliyor"
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
            .border(1.dp, ZenPaperBorder, ZenCardShape)
            .padding(12.dp)
    ) {
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
                    fontSize = 13.5.sp
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

            Box(
                modifier = Modifier
                    .clip(ZenPillShape)
                    .background(badgeBg)
                    .border(1.dp, badgeFg.copy(alpha = 0.3f), ZenPillShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeFg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
