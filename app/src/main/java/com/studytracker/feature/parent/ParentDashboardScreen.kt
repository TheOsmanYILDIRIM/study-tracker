package com.studytracker.feature.parent

import androidx.compose.foundation.BorderStroke
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
private val ZenCardShape = RoundedCornerShape(18.dp)
private val ZenSquircleShape = RoundedCornerShape(14.dp)
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
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val isNightMode by appPreferences.isNightMode.collectAsState()

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

    // Full-screen Storybook Paper Cutout Scene Root
    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 0: Vertical 9:16 Paper Cutout Illustration (Day or Night)
        Image(
            painter = painterResource(id = if (isNightMode) R.drawable.bg_zen_night else R.drawable.bg_zen_day),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 1: Frosted Dark Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x88080D1A),
                            Color(0xCC080D1A),
                            Color(0xF0080D1A)
                        )
                    )
                )
        )

        // Layer 2: Interactive UI
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = ZenTopBarBackplate,
                    border = BorderStroke(0.dp, Color.Transparent)
                ) {
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
                                    color = ZenForestContainer,
                                    border = BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(20.dp))
                                    }
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
                            // Quick Day / Night Toggle
                            IconButton(onClick = { appPreferences.toggleNightMode() }) {
                                Surface(
                                    shape = ZenPillShape,
                                    color = ZenPaperCard,
                                    border = BorderStroke(1.dp, ZenPaperBorder),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                            contentDescription = "Tema Değiştir",
                                            tint = if (isNightMode) ZenMoonGold else ZenSkyCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = onNavigateToPlanStudio) {
                                Surface(
                                    shape = ZenPillShape,
                                    color = ZenSkyCyanContainer,
                                    border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.5f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Plan Stüdyosu", tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Frosted Navigation Tabs
                Surface(
                    color = ZenTextBackplate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = ZenPillShape,
                    border = BorderStroke(1.dp, ZenPaperBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Tab 1
                        Surface(
                            onClick = { selectedTabIndex = 0 },
                            shape = ZenPillShape,
                            color = if (selectedTabIndex == 0) ZenSkyCyan else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🚨 Onay Masası",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (selectedTabIndex == 0) ZenMintText else ZomoTextSecondary
                                )
                                if (waitingSessions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = ZenPillShape,
                                        color = ZenRoseCoral,
                                        modifier = Modifier.padding(start = 2.dp)
                                    ) {
                                        Text(
                                            "${waitingSessions.size}",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Tab 2
                        Surface(
                            onClick = { selectedTabIndex = 1 },
                            shape = ZenPillShape,
                            color = if (selectedTabIndex == 1) ZenSkyCyan else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
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
                }

                if (selectedTabIndex == 0) {
                    // TAB 1: Review Queue & Summary
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Plan Overview Hero Card
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = ZenHeroShape,
                                color = ZenPaperCard,
                                border = BorderStroke(1.dp, ZenPaperBorder)
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
                                        Column {
                                            Text(
                                                text = "Aktif Hafta",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = ZomoTextSecondary
                                            )
                                            Text(
                                                text = activePlan?.weekId ?: "Plan Yüklenmedi",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = ZomoTextPrimary
                                            )
                                        }

                                        Surface(
                                            shape = ZenPillShape,
                                            color = ZenForestContainer,
                                            border = BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "$approvedTasks/$totalTasks Tamamlandı",
                                                color = ZenForestGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Öğrenci: ${activePlan?.childId ?: "child_1"} • Zaman Dilimi: ${activePlan?.timezone ?: "Europe/Istanbul"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZomoTextSecondary,
                                        fontSize = 11.sp
                                    )
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
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ZenTextBackplate,
                                border = BorderStroke(1.dp, ZenPaperBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                                        Surface(
                                            shape = ZenPillShape,
                                            color = ZenRoseContainer,
                                            border = BorderStroke(1.dp, ZenRoseCoral.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                "${waitingSessions.size} bekliyor",
                                                color = ZenRoseCoral,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (waitingSessions.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ZenCardShape,
                                    border = BorderStroke(1.dp, ZenPaperBorder),
                                    color = ZenPaperCard
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = ZenSquircleShape,
                                                color = ZenForestContainer,
                                                border = BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.4f)),
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(24.dp))
                                                }
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
                            }
                        } else {
                            items(waitingSessions, key = { it.sessionId }) { session ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ZenCardShape,
                                    color = ZenPaperCard,
                                    border = BorderStroke(1.dp, ZenMoonGold.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                                    shape = ZenSquircleShape,
                                                    color = ZenMoonGoldContainer,
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(20.dp))
                                                    }
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

                                            Surface(
                                                shape = ZenPillShape,
                                                color = ZenSkyCyanContainer,
                                                border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    text = "📸 ${session.screenshotCount} Kanıt",
                                                    color = ZenSkyCyan,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { onNavigateToSessionReview(session.sessionId) },
                                                modifier = Modifier.weight(1f).height(38.dp),
                                                shape = ZenPillShape,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ZenSkyCyan,
                                                    contentColor = ZenMintText
                                                )
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp), tint = ZenMintText)
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
                                                modifier = Modifier.height(38.dp),
                                                shape = ZenPillShape,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ZenForestGreen,
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
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
                                Surface(
                                    onClick = { selectedDayFilter = code },
                                    shape = ZenPillShape,
                                    color = if (isSelected) ZenSkyCyan else ZenPaperCard,
                                    border = BorderStroke(1.dp, if (isSelected) ZenSkyCyan else ZenPaperBorder)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ZenMintText else ZomoTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                                val mondayOffset = if (dayIndex >= 0) dayIndex else 0

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
                            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = ZenCardShape,
                                        border = BorderStroke(1.dp, ZenPaperBorder),
                                        color = ZenPaperCard
                                    ) {
                                        Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            Text("Seçilen gün için tanımlı görev bulunamadı.", color = ZomoTextSecondary, fontSize = 12.5.sp)
                                        }
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
fun OccurrenceAdminCard(occurrence: Occurrence) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ZenCardShape,
        color = ZenPaperCard,
        border = BorderStroke(1.dp, ZenPaperBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = ZenSquircleShape,
                color = if (occurrence.status == OccurrenceStatus.APPROVED) ZenForestContainer else ZenSkyCyanContainer,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (occurrence.status == OccurrenceStatus.APPROVED) Icons.Default.Check else Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (occurrence.status == OccurrenceStatus.APPROVED) ZenForestGreen else ZenSkyCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
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

            val (badgeText, badgeBg, badgeFg) = when (occurrence.status) {
                OccurrenceStatus.APPROVED -> Triple("Onaylandı", ZenForestContainer, ZenForestGreen)
                OccurrenceStatus.WAITING_REVIEW -> Triple("İnceleniyor", ZenMoonGoldContainer, ZenMoonGold)
                OccurrenceStatus.ACTIVE -> Triple("Aktif", ZenSkyCyanContainer, ZenSkyCyan)
                else -> Triple("Bekliyor", Color(0x15FFFFFF), ZomoTextSecondary)
            }

            Surface(
                shape = ZenPillShape,
                color = badgeBg,
                border = BorderStroke(1.dp, badgeFg.copy(alpha = 0.3f))
            ) {
                Text(
                    text = badgeText,
                    color = badgeFg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
