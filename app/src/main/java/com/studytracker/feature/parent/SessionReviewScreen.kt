package com.studytracker.feature.parent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.LocalSessionRepositoryImpl
import com.studytracker.core.data.local.repository.toDomain
import com.studytracker.core.domain.model.Review
import com.studytracker.core.domain.model.ReviewStatus
import com.studytracker.core.domain.model.Screenshot
import com.studytracker.core.domain.model.Session
import com.studytracker.core.ui.components.EvidenceTimelineView
import com.studytracker.core.ui.theme.EmeraldSuccess
import com.studytracker.core.ui.theme.RoseReject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionReviewScreen(
    sessionId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val sessionRepo = remember { LocalSessionRepositoryImpl(db) }

    var session by remember { mutableStateOf<Session?>(null) }
    var reviewNote by remember { mutableStateOf("") }
    val screenshots by db.screenshotDao().getScreenshotsForSession(sessionId)
        .map { list -> list.map { it.toDomain() } }
        .collectAsState(initial = emptyList())

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(sessionId) {
        session = sessionRepo.getSessionById(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📸 Kanıt İnceleme", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject Button
                    Button(
                        onClick = {
                            val currentSession = session ?: return@Button
                            scope.launch {
                                sessionRepo.submitReview(
                                    Review(
                                        sessionId = currentSession.sessionId,
                                        occurrenceKey = currentSession.occurrenceKey,
                                        reviewStatus = ReviewStatus.REJECTED,
                                        reviewNote = reviewNote.ifBlank { "Bu görev onaylanmadı. Lütfen eksikleri tamamlayıp tekrar yap." },
                                        reviewedAt = System.currentTimeMillis()
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseReject),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reddet (Uyarı)", fontWeight = FontWeight.Bold)
                    }

                    // Approve Button
                    Button(
                        onClick = {
                            val currentSession = session ?: return@Button
                            scope.launch {
                                sessionRepo.submitReview(
                                    Review(
                                        sessionId = currentSession.sessionId,
                                        occurrenceKey = currentSession.occurrenceKey,
                                        reviewStatus = ReviewStatus.APPROVED,
                                        reviewNote = reviewNote.ifBlank { null },
                                        reviewedAt = System.currentTimeMillis()
                                    )
                                )
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Onayla", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Session Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Görev: ${session?.occurrenceKey ?: "Yükleniyor..."}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        session?.let { s ->
                            val startStr = timeFormat.format(Date(s.startTime))
                            val endStr = s.endTime?.let { timeFormat.format(Date(it)) } ?: "Devam Ediyor"
                            val durationMins = s.endTime?.let { (it - s.startTime) / 60000 } ?: 0
                            Text("⏰ Başlama: $startStr • Bitiş: $endStr", style = MaterialTheme.typography.bodySmall)
                            Text("⏱️ Toplam Çalışma Süresi: $durationMins dakika", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Screenshot Timeline Carousel Component
            item {
                EvidenceTimelineView(screenshots = screenshots)
            }

            // Parent Feedback Note Input
            item {
                Text(
                    text = "💬 Değerlendirme & Geri Bildirim Notu (Opsiyonel):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reviewNote,
                    onValueChange = { reviewNote = it },
                    placeholder = { Text("Örn: Formülleri biraz daha detaylı yazabilirsin...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
