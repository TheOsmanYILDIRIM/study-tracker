package com.studytracker.feature.child

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.ui.overlay.FloatingHUDView
import com.studytracker.core.ui.theme.AmberContainer
import com.studytracker.core.ui.theme.AmberWarning
import com.studytracker.core.ui.theme.EmeraldContainer
import com.studytracker.core.ui.theme.EmeraldSuccess
import com.studytracker.core.ui.theme.PurpleActive

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
        topBar = {
            TopAppBar(
                title = { Text("🚀 Öğrenci Rehberi", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { completeAndExit() }) {
                        Text("Rehberi Geç", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Adım $step / $totalSteps",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (step) {
                            1 -> "Görev Masan"
                            2 -> "Yüzen Düğme & İnteraktif Deneme"
                            3 -> "Mola Verme (Pause / Resume)"
                            else -> "Ebeveyn Onayı & Başarı"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { step.toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldSuccess
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
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                            }
                        }

                        Text(
                            text = "📚 Ders ve Görev Masan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Ebeveynin senin için günlük dersler ve haftalık genel hedefler belirler. Her görevin hedef süresi, konusu ve varsa YouTube ders bağlantısı kart üzerinde yer alır.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("💡 İpucu:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Ders çalışmaya başlamak için tek yapman gereken 'Çalışmayı Başlat' düğmesine dokunmaktır. Ders başladığında sistem üstü yüzen düğmen otomatik olarak açılır.", fontSize = 12.sp)
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
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Ders çalışırken ekranda bu yüzen düğme bulunur. Aşağıdaki kutuda canlı olarak dene:",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Interactive Sandbox Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F172A))
                                .border(2.dp, PurpleActive, RoundedCornerShape(16.dp)),
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
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            isMockFinished = false
                                            mockScreenshotCount = 0
                                            isMockPaused = false
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Tekrar Dene", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📸 1 Kez Dokun: Anlık çalışma kanıtı çeker.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("⏸️ / ▶️ Duraklat/Devam Et: Mola vermek için basabilirsin.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("🛑 2 Saniye Basılı Tut: Dersi tamamlar ve ebeveynine iletir.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        if (mockScreenshotCount > 0 && !isMockFinished) {
                            Surface(color = EmeraldContainer, shape = RoundedCornerShape(8.dp)) {
                                Text("📸 Tebrikler! $mockScreenshotCount adet fotoğraf çektin!", color = Color(0xFF065F46), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                            shape = CircleShape,
                            color = AmberContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PauseCircle, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(44.dp))
                            }
                        }

                        Text(
                            text = "⏸️ Mola Verme ve Devam Etme",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Ders sırasında su içmek veya kısa bir ara vermek istersen yüzen düğmedeki veya ana ekrandaki 'Duraklat' butonuna dokunabilirsin.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("⏱️ Sayaç Durur, Kanıt Alınmaz:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                                Text("Ders duraklatıldığında çalışma süresi sayacı durur ve otomatik ekran görüntüsü alınmaz. Masana döndüğünde 'Devam Et' diyerek kaldığın yerden çalışmaya devam edersin.", fontSize = 12.sp, color = Color(0xFF78350F))
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
                            shape = CircleShape,
                            color = EmeraldContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(44.dp))
                            }
                        }

                        Text(
                            text = "🏆 Ebeveyn Onayı ve Rozetler",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Dersi bitirdiğinde aldığın kanıt fotoğrafları ebeveyn onay masasına iletilir. Ebeveynin inceleyip onayladığında görev 'Onaylandı' yeşil rozetine kavuşur!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🎓 Tebrikler, Rehberi Tamamladın!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Bu rehber ilk girişinde gösterilir. İleride kuralları hatırlamak istersen Görev Masası'nın sağ üstündeki (?) simgesine dokunarak rehberi istediğin zaman tekrar açabilirsin.", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Geri")
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
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text(
                        text = if (step < totalSteps) "İleri ->" else "🚀 Görev Masama Başla!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
