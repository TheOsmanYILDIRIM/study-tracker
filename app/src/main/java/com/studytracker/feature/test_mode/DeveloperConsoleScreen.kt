package com.studytracker.feature.test_mode

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.LocalOccurrenceRepositoryImpl
import com.studytracker.core.data.local.repository.LocalPlanRepositoryImpl
import com.studytracker.core.data.local.repository.LocalSessionRepositoryImpl
import com.studytracker.core.domain.manager.SessionStateManager
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.ui.theme.AmberContainer
import com.studytracker.core.ui.theme.AmberWarning
import com.studytracker.core.ui.theme.EmeraldSuccess
import com.studytracker.core.ui.theme.RoseReject
import kotlinx.coroutines.launch

private const val SAMPLE_WEEKLY_PLAN_JSON = """{
  "schemaVersion": 1,
  "planId": "plan_2026-W25_child_1",
  "weekId": "2026-W25",
  "weekStartDate": "2026-06-15",
  "childId": "child_1",
  "timezone": "Europe/Istanbul",
  "updatedAt": "2026-06-17T12:00:00+03:00",
  "tasks": [
    {
      "taskId": "math_video",
      "title": "Matematik videosu izle",
      "kind": "DAILY",
      "contentType": "VIDEO",
      "youtubeUrl": "https://youtube.com/watch?v=math1",
      "plannedMinutes": 30,
      "reviewRequired": true,
      "active": true
    },
    {
      "taskId": "reading",
      "title": "Paragraf soru çözümü",
      "kind": "DAILY",
      "contentType": "READING",
      "youtubeUrl": null,
      "plannedMinutes": 20,
      "reviewRequired": true,
      "active": true
    },
    {
      "taskId": "anki",
      "title": "Anki kelime tekrarı",
      "kind": "DAILY",
      "contentType": "APP",
      "youtubeUrl": null,
      "plannedMinutes": 25,
      "reviewRequired": true,
      "active": true
    },
    {
      "taskId": "weekly_exam",
      "title": "Haftalık Deneme Sınavı",
      "kind": "WEEKLY",
      "contentType": "EXAM",
      "youtubeUrl": null,
      "plannedMinutes": 120,
      "targetMode": "COUNT",
      "targetCount": 2,
      "targetMinutes": null,
      "reviewRequired": true,
      "active": true
    }
  ],
  "dailyOccurrences": [
    {
      "occurrenceKey": "math_video:2026-06-15",
      "taskId": "math_video",
      "date": "2026-06-15",
      "title": "Matematik videosu izle",
      "plannedMinutes": 30,
      "youtubeUrl": "https://youtube.com/watch?v=math1",
      "reviewRequired": true
    },
    {
      "occurrenceKey": "reading:2026-06-15",
      "taskId": "reading",
      "date": "2026-06-15",
      "title": "Paragraf soru çözümü",
      "plannedMinutes": 20,
      "youtubeUrl": null,
      "reviewRequired": true
    },
    {
      "occurrenceKey": "anki:2026-06-15",
      "taskId": "anki",
      "date": "2026-06-15",
      "title": "Anki kelime tekrarı",
      "plannedMinutes": 25,
      "youtubeUrl": null,
      "reviewRequired": true
    }
  ],
  "weeklyOccurrences": [
    {
      "occurrenceKey": "weekly_exam:2026-W25",
      "taskId": "weekly_exam",
      "weekId": "2026-W25",
      "title": "Haftalık Deneme Sınavı",
      "targetMode": "COUNT",
      "targetCount": 2,
      "targetMinutes": null,
      "plannedMinutes": 120,
      "reviewRequired": true
    }
  ]
}"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperConsoleScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val planRepo = remember { LocalPlanRepositoryImpl(db) }
    val occurrenceRepo = remember { LocalOccurrenceRepositoryImpl(db) }
    val sessionRepo = remember { LocalSessionRepositoryImpl(db) }
    val stateManager = remember { SessionStateManager.getInstance(context) }

    val isTestModeEnabled by appPreferences.isTestModeEnabled.collectAsState()
    val isFakeCaptureEnabled by appPreferences.isFakeCaptureEnabled.collectAsState()
    val hasCompletedTutorial by appPreferences.hasCompletedTutorial.collectAsState()

    val occurrences by remember(occurrenceRepo) { occurrenceRepo.getAllOccurrences() }.collectAsState(initial = emptyList())
    val waitingSessions by remember(sessionRepo) { sessionRepo.getWaitingReviewSessions() }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛠️ Geliştirici & Test Konsolu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Test Mode Switch Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTestModeEnabled) MaterialTheme.colorScheme.primaryContainer else AmberContainer
                    )
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTestModeEnabled) "🛠️ Test Modu: AÇIK" else "🔒 Test Modu: KAPALI",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (isTestModeEnabled)
                                        "Test modu açıkken sahte veriler ve geliştirici konsolu kolay erişimde kalır."
                                    else
                                        "Test modu kapatıldı. Uygulama canlı kullanım modundadır.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = isTestModeEnabled,
                                onCheckedChange = { newStatus ->
                                    appPreferences.setTestModeEnabled(newStatus)
                                    val msg = if (newStatus) "Test Modu AÇILDI" else "Test Modu KAPATILDI"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        if (!isTestModeEnabled) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberWarning.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 Test modunu kapattınız. Rol Seçim ekranındaki test konsolu butonu artık gizlenir (Giriş için sağ üstteki kilit ikonunu kullanabilirsiniz).",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Environment & Drivers Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Sürücü ve Ortam Ayarları", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ekran Yakalama Simülasyonu (FakeCaptureDriver)")
                                Text(
                                    "Açıkken gerçek ekran yerine sanal çalışma şablonu fotoğrafları üretir.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isFakeCaptureEnabled,
                                onCheckedChange = {
                                    appPreferences.setFakeCaptureEnabled(it)
                                    Toast.makeText(context, "Sanal sürücü: $it", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Öğrenci Rehberi Durumu")
                                Text(
                                    if (hasCompletedTutorial) "Öğrenci rehberi tamamlandı (doğrudan masa açılır)" else "Rehber henüz görülmedi (ilk girişte açılacak)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    appPreferences.setHasCompletedTutorial(false)
                                    Toast.makeText(context, "🎓 Rehber sıfırlandı! Öğrenci moduna girince rehber açılacak.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Rehberi Sıfırla", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Quick Simulators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🧪 Hızlı Simülasyon Tetikleyicileri", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        // Load Sample Plan
                        Button(
                            onClick = {
                                scope.launch {
                                    val res = planRepo.importPlanJson(SAMPLE_WEEKLY_PLAN_JSON)
                                    res.onSuccess {
                                        Toast.makeText(context, "📦 Örnek haftalık plan yüklendi!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📦 Örnek Haftalık Planı Yükle")
                        }

                        // Create Mock Waiting Session
                        Button(
                            onClick = {
                                scope.launch {
                                    val key = "math_video:2026-06-15"
                                    val session = sessionRepo.startSession(key, "child_1")
                                    stateManager.captureDriver.start(session.sessionId, key)
                                    stateManager.captureDriver.captureNow()
                                    stateManager.captureDriver.captureNow()
                                    val finalSs = stateManager.captureDriver.stop()
                                    sessionRepo.finishSession(session.sessionId, finalSs?.url)
                                    Toast.makeText(context, "⏳ Onay bekleyen oturum oluşturuldu!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⏳ Sahte Onay Bekleyen Oturum Yarat")
                        }

                        // Reset Database
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    planRepo.clearAllPlanData()
                                    sessionRepo.clearAllSessions()
                                    Toast.makeText(context, "🧹 Tüm test veritabanı temizlendi.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseReject)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🧹 Test Veritabanını Tamamen Sıfırla")
                        }
                    }
                }
            }

            // Real-time Database Snapshot Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📊 Canlı Veritabanı Sayaçları", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("• Toplam Görev Sayısı: ${occurrences.size}")
                        Text("• Tamamlanan (Approved): ${occurrences.count { it.status == OccurrenceStatus.APPROVED }}")
                        Text("• İnceleme Bekleyen Oturumlar: ${waitingSessions.size}")
                        Text("• Aktif Oturum: ${if (stateManager.activeState.value != null) "ÇALIŞIYOR ⚡" else "Yok"}")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
