package com.studytracker.feature.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var selectedDayFilter by remember { mutableStateOf("ALL") } // ALL, MON, TUE, WED, THU, FRI, SAT, SUN

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayNameFormat = remember { SimpleDateFormat("EEEE", Locale("tr", "TR")) }

    val totalTasks = allOccurrences.size
    val approvedTasks = remember(allOccurrences) {
        allOccurrences.count { it.status == OccurrenceStatus.APPROVED }
    }
    val pendingTasks = remember(allOccurrences) {
        allOccurrences.count { it.status == OccurrenceStatus.PENDING }
    }

    // Grouping for Weekly / Daily view
    val dailyOccurrences by remember(allOccurrences) {
        derivedStateOf { allOccurrences.filter { it.type == TaskKind.DAILY } }
    }
    val weeklyOccurrences by remember(allOccurrences) {
        derivedStateOf { allOccurrences.filter { it.type == TaskKind.WEEKLY } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👨‍👧 Ebeveyn Masası", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPlanStudio) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Plan Stüdyosu", tint = MaterialTheme.colorScheme.primary)
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
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🚨 Onay Masası")
                            if (waitingSessions.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text("${waitingSessions.size}")
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("📅 Haftalık & Günlük Plan") }
                )
            }

            if (selectedTabIndex == 0) {
                // TAB 1: Review Queue & Summary
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Plan Overview Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
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
                                        text = "Aktif Hafta: ${activePlan?.weekId ?: "Plan Yüklenmedi"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldContainer
                                    ) {
                                        Text(
                                            text = "$approvedTasks/$totalTasks Tamamlandı",
                                            color = Color(0xFF065F46),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Öğrenci: ${activePlan?.childId ?: "child_1"} • Zaman Dilimi: ${activePlan?.timezone ?: "Europe/Istanbul"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
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
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🤖 AI Plan Stüdyosu & İçe/Dışa Aktar", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Waiting Review Queue Header
                    item {
                        Text(
                            text = "🚨 Onay Bekleyen Çalışma Oturumları",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (waitingSessions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Şu anda incelenmeyi bekleyen çalışma oturumu yok. 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(waitingSessions, key = { it.sessionId }) { session ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.6f))
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
                                        Text(
                                            text = session.occurrenceKey,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Başlama: ${timeFormat.format(Date(session.startTime))}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }

                                    Text(
                                        text = "📸 Alınan Kanıt Ekran Görüntüsü: ${session.screenshotCount} adet",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    // Action buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onNavigateToSessionReview(session.sessionId) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("İncele")
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
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Onayla")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                // TAB 2: Full Weekly & Daily Plan Inspection View (Öğrenci Modu Tarzı Plan İnceleme)
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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Day Selector Horizontal Filter
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🗓️ Gün Filtresi", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val dayFilters = remember {
                                    listOf(
                                        "ALL" to "Tüm Hafta",
                                        "MON" to "Pzt",
                                        "TUE" to "Sal",
                                        "WED" to "Çar",
                                        "THU" to "Per",
                                        "FRI" to "Cum",
                                        "SAT" to "Cmt",
                                        "SUN" to "Paz"
                                    )
                                }
                                for ((key, label) in dayFilters) {
                                    FilterChip(
                                        selected = selectedDayFilter == key,
                                        onClick = { selectedDayFilter = key },
                                        label = { Text(label, fontSize = 12.sp) }
                                    )
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
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (filteredTasks.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Bu güne ait tanımlı ders bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(filteredTasks, key = { "inspect_" + it.occurrenceKey }) { task ->
                            ParentTaskInspectionCard(task = task)
                        }
                    }

                    // Weekly Goals Section (Haftalık Genel Hedefler)
                    if (selectedDayFilter == "ALL" || selectedDayFilter == "SUN" || selectedDayFilter == "SAT") {
                        item {
                            Text(
                                text = "🎯 Haftalık Genel Hedefler (${weeklyOccurrences.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (weeklyOccurrences.isEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Haftalık genel hedef tanımlanmamış.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val statusColor = when (task.status) {
        OccurrenceStatus.APPROVED -> EmeraldSuccess
        OccurrenceStatus.WAITING_REVIEW -> AmberWarning
        OccurrenceStatus.ACTIVE -> PurpleActive
        OccurrenceStatus.REJECTED -> RoseReject
        OccurrenceStatus.PENDING -> MaterialTheme.colorScheme.outline
        OccurrenceStatus.ARCHIVED -> MaterialTheme.colorScheme.outlineVariant
    }

    val statusText = when (task.status) {
        OccurrenceStatus.APPROVED -> "✅ Onaylandı"
        OccurrenceStatus.WAITING_REVIEW -> "⏳ İnceleniyor"
        OccurrenceStatus.ACTIVE -> "⚡ Devam Ediyor"
        OccurrenceStatus.REJECTED -> "❌ Tekrar İsteniyor"
        OccurrenceStatus.PENDING -> "🕒 Bekliyor"
        OccurrenceStatus.ARCHIVED -> "📦 Arşivlendi"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "⏱️ Hedef: ${task.plannedMinutes} dk",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!task.youtubeUrl.isNullOrBlank()) {
                Text(
                    text = "🔗 ${task.youtubeUrl}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (task.warning && !task.warningText.isNullOrBlank()) {
                Surface(
                    color = RoseReject.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ebeveyn Notu: ${task.warningText}",
                        color = RoseReject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
