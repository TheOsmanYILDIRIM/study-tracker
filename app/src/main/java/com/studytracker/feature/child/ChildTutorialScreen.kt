package com.studytracker.feature.child

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.ui.overlay.FloatingHUDView
import com.studytracker.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildTutorialScreen(
    onFinishTutorial: () -> Unit
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }

    var step by remember { mutableIntStateOf(1) }
    var mockScreenshotCount by remember { mutableIntStateOf(0) }
    var isMockPaused by remember { mutableStateOf(false) }
    var isMockFinished by remember { mutableStateOf(false) }

    val totalSteps = 4

    fun completeAndExit() {
        appPreferences.setHasCompletedTutorial(true)
        onFinishTutorial()
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
                                Icon(Icons.Default.School, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text("Öğrenci Rehberi", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { completeAndExit() },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Rehberi Geç", fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Adım $step / $totalSteps",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ZomoPurplePrimary
                    )
                    Text(
                        text = when (step) {
                            1 -> "Görev Masan"
                            2 -> "Yüzen Düğme & Canlı Deneme"
                            3 -> "Mola Verme (Pause / Resume)"
                            else -> "Ebeveyn Onayı & Başarı"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }

                LinearProgressIndicator(
                    progress = { step.toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = ZomoMintAccent,
                    trackColor = ZomoSoftLavender
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (step) {
                1 -> {
                    // STEP 1: Daily & Weekly Task Hub
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = ZomoSquirclePurple,
                            modifier = Modifier.size(88.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(46.dp))
                            }
                        }

                        Text(
                            text = "📚 Ders ve Görev Masan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E1B4B)
                        )

                        Text(
                            text = "Ebeveynin senin için günlük dersler ve haftalık genel hedefler belirler. Her görevin hedef süresi, konusu ve varsa YouTube ders bağlantısı kart üzerinde yer alır.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF4B5563),
                            lineHeight = 20.sp
                        )

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
                            border = BorderStroke(1.dp, ZomoSquirclePurple.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(18.dp))
                                    Text("İpucu:", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = ZomoPurplePrimary)
                                }
                                Text("Ders çalışmaya başlamak için tek yapman gereken 'Çalışmayı Başlat' düğmesine dokunmaktır. Ders başladığında sistem üstü yüzen düğmen otomatik olarak açılır.", fontSize = 12.sp, color = Color(0xFF4B5563))
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: Interactive Sandbox Floating HUD
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🟣 Yüzen Düğme ile Canlı Deneme",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E1B4B)
                        )

                        Text(
                            text = "Ders çalışırken ekranda bu yüzen düğme bulunur. Aşağıdaki kutuda canlı olarak dene:",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF6B7280)
                        )

                        // Interactive Sandbox Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF160B29))
                                .border(1.5.dp, ZomoPurplePrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isMockFinished) {
                                FloatingHUDView(
                                    elapsedSeconds = 145,
                                    screenshotCount = mockScreenshotCount,
                                    isFinishing = false,
                                    isPaused = isMockPaused,
                                    onSingleTapCapture = {
                                        mockScreenshotCount++
                                    },
                                    onLongPressFinish = {
                                        isMockFinished = true
                                    },
                                    onTogglePause = {
                                        isMockPaused = !isMockPaused
                                    }
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "🎉 Harika! Dersi Başarıyla Bitirdin!",
                                        color = ZomoMintAccent,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            isMockFinished = false
                                            mockScreenshotCount = 0
                                            isMockPaused = false
                                        },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, ZomoMintAccent)
                                    ) {
                                        Text("Tekrar Dene", fontSize = 12.sp, color = ZomoMintAccent, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📸 1 Kez Dokun: Anlık çalışma kanıtı çeker.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E1B4B))
                            Text("⏸️ / ▶️ Duraklat/Devam Et: Mola vermek için basabilirsin.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E1B4B))
                            Text("🛑 2 Saniye Basılı Tut: Dersi tamamlar ve ebeveynine iletir.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E1B4B))
                        }

                        if (mockScreenshotCount > 0 && !isMockFinished) {
                            Surface(color = ZomoSquircleEmerald, shape = RoundedCornerShape(50)) {
                                Text("📸 Tebrikler! $mockScreenshotCount adet kanıt çektin!", color = Color(0xFF059669), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: Pause / Resume Feature
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = ZomoSquircleAmber,
                            modifier = Modifier.size(88.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PauseCircle, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(46.dp))
                            }
                        }

                        Text(
                            text = "⏸️ Mola Verme ve Devam Etme",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E1B4B)
                        )

                        Text(
                            text = "Ders sırasında su içmek veya kısa bir ara vermek istersen yüzen düğmedeki veya ana ekrandaki 'Duraklat' butonuna dokunabilirsin.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF4B5563),
                            lineHeight = 20.sp
                        )

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
                            border = BorderStroke(1.dp, ZomoSquircleAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    Text("Sayaç Durur, Kanıt Alınmaz:", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFFD97706))
                                }
                                Text("Ders duraklatıldığında çalışma süresi sayacı durur ve otomatik ekran görüntüsü alınmaz. Masana döndüğünde 'Devam Et' diyerek kaldığın yerden çalışmaya devam edersin.", fontSize = 12.sp, color = Color(0xFF4B5563))
                            }
                        }
                    }
                }

                4 -> {
                    // STEP 4: Review & Badges
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = ZomoSquircleEmerald,
                            modifier = Modifier.size(88.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(46.dp))
                            }
                        }

                        Text(
                            text = "🏆 Ebeveyn Onayı ve Rozetler",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E1B4B)
                        )

                        Text(
                            text = "Dersi bitirdiğinde aldığın kanıt fotoğrafları ebeveyn onay masasına iletilir. Ebeveynin inceleyip onayladığında görev 'Onaylandı' yeşil rozetine kavuşur!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF4B5563),
                            lineHeight = 20.sp
                        )

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
                            border = BorderStroke(1.dp, ZomoSquircleEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                                    Text("Tebrikler, Rehberi Tamamladın!", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF059669))
                                }
                                Text("Bu rehber ilk girişinde gösterilir. İleride kuralları hatırlamak istersen Görev Masası'nın sağ üstündeki (?) simgesine dokunarak rehberi istediğin zaman tekrar açabilirsin.", fontSize = 12.sp, color = Color(0xFF4B5563))
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons (Pills)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.3f))
                    ) {
                        Text("Geri", color = ZomoPurplePrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            completeAndExit()
                        }
                    },
                    modifier = Modifier.weight(2f).height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZomoMintAccent,
                        contentColor = Color(0xFF064E3B)
                    )
                ) {
                    Text(
                        text = if (step < totalSteps) "İleri ->" else "🚀 Görev Masama Başla!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
