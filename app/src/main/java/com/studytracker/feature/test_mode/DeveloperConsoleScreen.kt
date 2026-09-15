package com.studytracker.feature.test_mode

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch

private val ZenCardShape = RoundedCornerShape(20.dp)
private val ZenPillShape = CircleShape

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
    },
    {
      "occurrenceKey": "math_video:2026-06-16",
      "taskId": "math_video",
      "date": "2026-06-16",
      "title": "Matematik videosu izle",
      "plannedMinutes": 30,
      "youtubeUrl": "https://youtube.com/watch?v=math1",
      "reviewRequired": true
    },
    {
      "occurrenceKey": "reading:2026-06-16",
      "taskId": "reading",
      "date": "2026-06-16",
      "title": "Paragraf soru çözümü",
      "plannedMinutes": 20,
      "youtubeUrl": null,
      "reviewRequired": true
    },
    {
      "occurrenceKey": "anki:2026-06-16",
      "taskId": "anki",
      "date": "2026-06-16",
      "title": "Anki kelime tekrarı",
      "plannedMinutes": 25,
      "youtubeUrl": null,
      "reviewRequired": true
    },
    {
      "occurrenceKey": "math_video:2026-06-17",
      "taskId": "math_video",
      "date": "2026-06-17",
      "title": "Matematik videosu izle",
      "plannedMinutes": 30,
      "youtubeUrl": "https://youtube.com/watch?v=math1",
      "reviewRequired": true
    },
    {
      "occurrenceKey": "reading:2026-06-17",
      "taskId": "reading",
      "date": "2026-06-17",
      "title": "Paragraf soru çözümü",
      "plannedMinutes": 20,
      "youtubeUrl": null,
      "reviewRequired": true
    },
    {
      "occurrenceKey": "anki:2026-06-17",
      "taskId": "anki",
      "date": "2026-06-17",
      "title": "Anki kelime tekrarı",
      "plannedMinutes": 25,
      "youtubeUrl": null,
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
    val stateManager = remember { SessionStateManager.getInstance(context) }

    val planRepo = remember { LocalPlanRepositoryImpl(db) }
    val occurrenceRepo = remember { LocalOccurrenceRepositoryImpl(db) }
    val sessionRepo = remember { LocalSessionRepositoryImpl(db) }

    val isNightMode by appPreferences.isNightMode.collectAsState()
    val isTestModeEnabled by appPreferences.isTestModeEnabled.collectAsState()
    val isFakeCaptureEnabled by appPreferences.isFakeCaptureEnabled.collectAsState()
    val hasCompletedTutorial by appPreferences.hasCompletedTutorial.collectAsState()

    val occurrences by remember(occurrenceRepo) { occurrenceRepo.getAllOccurrences() }.collectAsState(initial = emptyList())
    val waitingSessions by remember(sessionRepo) { sessionRepo.getWaitingReviewSessions() }.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = ZenNightCanvas,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZenNightCanvas,
                    titleContentColor = ZomoTextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ZenSkyCyanContainer,
                            border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text("Ayarlar & Test Konsolu", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ZomoTextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ZomoTextPrimary)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Theme Mode Card (Day / Night Selection)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZenPaperCard),
                    border = BorderStroke(1.dp, ZenPaperBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                                    shape = RoundedCornerShape(10.dp),
                                    color = ZenMoonGoldContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                            contentDescription = null,
                                            tint = ZenMoonGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text("🎨 Görsel Tema Ayarı", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ZomoTextPrimary)
                                    Text(
                                        if (isNightMode) "Gece Modu: Derin yıldızlı masal gökyüzü" else "Gündüz Modu: Aydınlık doğa ve vadi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZomoTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isNightMode,
                                onCheckedChange = { night ->
                                    appPreferences.setNightMode(night)
                                    val msg = if (night) "🌙 Gece Modu Devrede" else "☀️ Gündüz Modu Devrede"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ZenMoonGold,
                                    checkedTrackColor = ZenSkyCyan
                                )
                            )
                        }
                    }
                }
            }

            // Master Test Mode Switch Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZenPaperCard),
                    border = BorderStroke(1.dp, ZenPaperBorder)
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
                                Text("🧪 Test Modu Anahtarı", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ZomoTextPrimary)
                                Text(
                                    if (isTestModeEnabled) "Test modu AÇIK. Rol seçim ekranında konsol butonu ve simülasyonlar görünür."
                                    else "Test modu KAPALI. Uygulama normal kullanıcı modunda çalışır.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZomoTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            }

                            Switch(
                                checked = isTestModeEnabled,
                                onCheckedChange = { newStatus ->
                                    appPreferences.setTestModeEnabled(newStatus)
                                    val msg = if (newStatus) "Test Modu AÇILDI" else "Test Modu KAPATILDI"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ZenSkyCyan
                                )
                            )
                        }
                    }
                }
            }

            // Environment & Drivers Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZenPaperCard),
                    border = BorderStroke(1.dp, ZenGlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⚙️ Sürücü ve Ortam Ayarları", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ZomoTextPrimary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ekran Yakalama Simülasyonu", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ZomoTextPrimary)
                                Text(
                                    if (isFakeCaptureEnabled) "Açık: Sanal çalışma şablonu fotoğrafları üretir."
                                    else "Kapalı: Erişilebilirlik servisiyle sessiz gerçek ekran yakalama devrede.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZomoTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                            Switch(
                                checked = isFakeCaptureEnabled,
                                onCheckedChange = { enabled ->
                                    appPreferences.setFakeCaptureEnabled(enabled)
                                    Toast.makeText(context, if (enabled) "Sanal sürücü devrede" else "Sessiz gerçek ekran yakalama devrede", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ZenSkyCyan
                                )
                            )
                        }

                        if (!isFakeCaptureEnabled) {
                            val isAccRunning = com.studytracker.core.service.StudyAccessibilityService.isServiceRunning()

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAccRunning) ZenForestContainer else ZenMoonGoldContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAccRunning) "⚡ Erişilebilirlik: AKTİF" else "⚙️ Erişilebilirlik İzni Gerekli",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = if (isAccRunning) ZenForestGreen else ZenMoonGold
                                        )
                                        Text(
                                            text = if (isAccRunning) "Sıfır sistem uyarısıyla arka planda sessiz ekran yakalanır."
                                            else "Sessiz ekran yakalamak için ayarlardan StudyTracker'ı 1 kez açın.",
                                            fontSize = 11.sp,
                                            color = ZomoTextSecondary
                                        )
                                    }

                                    if (!isAccRunning) {
                                        Button(
                                            onClick = {
                                                com.studytracker.core.service.StudyAccessibilityService.openAccessibilitySettings(context)
                                            },
                                            shape = ZenPillShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = ZenMoonGold, contentColor = Color(0xFF451A03))
                                        ) {
                                            Text("Ayarları Aç", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = ZenPaperBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Öğrenci Rehberi Durumu", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ZomoTextPrimary)
                                Text(
                                    if (hasCompletedTutorial) "Rehber tamamlandı (doğrudan masa açılır)" else "Rehber henüz görülmedi (ilk girişte açılacak)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZomoTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    appPreferences.setHasCompletedTutorial(false)
                                    Toast.makeText(context, "🎓 Rehber sıfırlandı! Öğrenci moduna girince rehber açılacak.", Toast.LENGTH_SHORT).show()
                                },
                                shape = ZenPillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan, contentColor = ZenMintText)
                            ) {
                                Text("Sıfırla", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Simulators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZenPaperCard),
                    border = BorderStroke(1.dp, ZenPaperBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🧪 Hızlı Simülasyon Tetikleyicileri", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ZomoTextPrimary)

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
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = ZenPillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan, contentColor = ZenMintText)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = ZenMintText)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📦 Örnek Haftalık Planı Yükle", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        // Create Mock Waiting Session
                        Button(
                            onClick = {
                                scope.launch {
                                    val key = "math_video:2026-06-15"
                                    val session = sessionRepo.startSession(key, "child_1")
                                    val driver = stateManager.getEffectiveCaptureDriver()
                                    driver.start(session.sessionId, key)
                                    driver.captureNow()
                                    driver.captureNow()
                                    val finalSs = driver.stop()
                                    sessionRepo.finishSession(session.sessionId, finalSs?.url)
                                    Toast.makeText(context, "⏳ Onay bekleyen oturum oluşturuldu!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = ZenPillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⏳ Sahte Onay Bekleyen Oturum Yarat", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
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
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = ZenPillShape,
                            border = BorderStroke(1.dp, ZenRoseCoral)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = ZenRoseCoral)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🧹 Test Veritabanını Tamamen Sıfırla", color = ZenRoseCoral, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }

            // Real-time Database Snapshot Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZenPaperCard),
                    border = BorderStroke(1.dp, ZenPaperBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📊 Canlı Veritabanı Sayaçları", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ZomoTextPrimary)
                        Text("• Toplam Görev Sayısı: ${occurrences.size}", color = ZomoTextSecondary, fontSize = 12.sp)
                        Text("• Tamamlanan (Approved): ${occurrences.count { it.status == OccurrenceStatus.APPROVED }}", color = ZenForestGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("• İnceleme Bekleyen Oturumlar: ${waitingSessions.size}", color = ZenMoonGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("• Aktif Oturum: ${if (stateManager.activeState.value != null) "ÇALIŞIYOR ⚡" else "Yok"}", color = ZenSkyCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
