package com.studytracker.feature.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.studytracker.core.ui.theme.*
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
    val screenshots by remember(sessionId) {
        db.screenshotDao().getScreenshotsForSession(sessionId)
            .map { list -> list.map { it.toDomain() } }
    }.collectAsState(initial = emptyList())

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(sessionId) {
        session = sessionRepo.getSessionById(sessionId)
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
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text("Kanıt İnceleme", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E1B4B))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = ZomoCardBackground,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject Button (Pill)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E), contentColor = Color.White),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reddet (Uyarı)", fontWeight = FontWeight.ExtraBold)
                    }

                    // Approve Button (Neon Mint Pill)
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
                        colors = ButtonDefaults.buttonColors(containerColor = ZomoMintAccent, contentColor = Color(0xFF064E3B)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Onayla", fontWeight = FontWeight.ExtraBold)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(24.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
                    border = BorderStroke(1.dp, ZomoSquirclePurple.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = session?.occurrenceKey ?: "Yükleniyor...",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1E1B4B)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ZomoSoftLavender
                            ) {
                                Text(
                                    text = "📸 ${screenshots.size} Görsel",
                                    color = ZomoPurplePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        session?.let { s ->
                            val startStr = timeFormat.format(Date(s.startTime))
                            val endStr = s.endTime?.let { timeFormat.format(Date(it)) } ?: "Devam Ediyor"
                            val durationMins = s.endTime?.let { (it - s.startTime) / 60000 } ?: 0
                            Text(
                                text = "⏰ Başlama: $startStr • Bitiş: $endStr • Süre: $durationMins dk",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }

                        OutlinedTextField(
                            value = reviewNote,
                            onValueChange = { reviewNote = it },
                            placeholder = { Text("Ebeveyn notu / geri bildirimi ekleyin (opsiyonel)...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZomoPurplePrimary,
                                unfocusedBorderColor = ZomoSquirclePurple.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Timeline & Screenshots Header
            item {
                Text(
                    text = "📸 Alınan Çalışma Kanıtları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1B4B)
                )
            }

            // Timeline component
            item {
                EvidenceTimelineView(
                    screenshots = screenshots,
                    onScreenshotClick = { /* full preview */ }
                )
            }
        }
    }
}
