package com.studytracker.feature.child

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.studytracker.core.ui.theme.*

private val FuturisticCardShape = RoundedCornerShape(22.dp)
private val FuturisticSquircleShape = RoundedCornerShape(20.dp)
private val FuturisticPillShape = CircleShape

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

    val totalSteps = 5

    fun completeAndExit() {
        appPreferences.setHasCompletedTutorial(true)
        onFinishTutorial()
    }

    Scaffold(
        containerColor = ZomoDarkCanvas,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZomoDarkCanvas,
                    titleContentColor = ZomoTextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ZomoVioletContainer,
                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text("Nasıl Çalışır? (Öğrenci Kılavuzu)", fontWeight = FontWeight.Black, fontSize = 17.sp, color = ZomoTextPrimary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { completeAndExit() },
                        shape = FuturisticPillShape
                    ) {
                        Text("Rehberi Geç", fontWeight = FontWeight.Bold, color = ZomoTextSecondary, fontSize = 13.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 8.dp),
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ADIM $step / $totalSteps",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = ZomoNeonMint,
                        fontSize = 12.sp
                    )
                    Text(
                        text = when (step) {
                            1 -> "1. Ders Masan"
                            2 -> "2. Yüzen Baloncuk (Dene)"
                            3 -> "3. Kolay İzinler"
                            4 -> "4. Testler & Sorular"
                            else -> "5. Veli Onayı & Yıldızlar"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ZomoTextSecondary,
                        fontSize = 12.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { step.toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(FuturisticPillShape),
                    color = ZomoNeonMint,
                    trackColor = ZomoDarkSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Content (Scrollable for smaller screens)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (step) {
                    1 -> {
                        // STEP 1: Task Hub & Daily Schedule
                        Surface(
                            shape = FuturisticSquircleShape,
                            color = ZomoVioletContainer,
                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(40.dp))
                            }
                        }

                        Text(
                            text = "📚 1. Ders Masan & Görevlerin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = ZomoTextPrimary
                        )

                        Text(
                            text = "Velin senin için her güne dersler ve hedefler belirler. Uygulamayı açtığında o gün yapman gereken tüm dersler sırayla karşına çıkar.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = ZomoTextSecondary,
                            lineHeight = 20.sp
                        )

                        Card(
                            shape = FuturisticCardShape,
                            colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                            border = BorderStroke(1.dp, ZomoDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(20.dp))
                                    Text("🎬 YouTube Dersi:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenMoonGold)
                                }
                                Text("Derste video varsa kartın üzerindeki YouTube butonuna dokun, video anında açılsın.", fontSize = 12.sp, color = ZomoTextSecondary)

                                Divider(color = ZomoDarkBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ZomoNeonMint, modifier = Modifier.size(20.dp))
                                    Text("▶️ Çalışmayı Başlat:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZomoNeonMint)
                                }
                                Text("Masaya oturup derse başlayacağın an yeşil 'Çalışmayı Başlat' düğmesine bas, ders sayacın aksın!", fontSize = 12.sp, color = ZomoTextSecondary)

                                Divider(color = ZomoDarkBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = ZenMintSoft, modifier = Modifier.size(20.dp))
                                    Text("⬇️ Ekranı Aşağı Kaydır:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenMintSoft)
                                }
                                Text("Velin yeni bir ders veya ödev eklediğinde ana ekranı aşağı doğru çekerek hemen yenileyebilirsin.", fontSize = 12.sp, color = ZomoTextSecondary)
                            }
                        }
                    }

                    2 -> {
                        // STEP 2: Floating Bubble (Interactive Sandbox)
                        Text(
                            text = "🟣 2. Sihirli Yüzen Baloncuk",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = ZomoTextPrimary
                        )

                        Text(
                            text = "Dersi başlattığında ekranına küçük bir baloncuk gelir. Sen YouTube'da video izlerken veya soru çözerken hep ekranda yüzmeye devam eder. Aşağıdaki kutuda hemen canlı dene:",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = ZomoTextSecondary,
                            lineHeight = 18.sp
                        )

                        // Interactive Sandbox Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(FuturisticCardShape)
                                .background(Color(0xFF0C061C))
                                .border(1.5.dp, ZomoPurplePrimary.copy(alpha = 0.5f), FuturisticCardShape),
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
                                        text = "🎉 Süper! Dersi Bitirmeyi Öğrendin!",
                                        color = ZomoNeonMint,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            isMockFinished = false
                                            mockScreenshotCount = 0
                                            isMockPaused = false
                                        },
                                        shape = FuturisticPillShape,
                                        border = BorderStroke(1.dp, ZomoNeonMint)
                                    ) {
                                        Text("Tekrar Canlı Dene", fontSize = 11.sp, color = ZomoNeonMint, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Card(
                            shape = FuturisticCardShape,
                            colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                            border = BorderStroke(1.dp, ZomoDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("👆 1 Kez Dokun: Anlık çalışma kanıtı (ekran fotoğrafı) çeker.", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = ZomoTextPrimary)
                                Text("⏸️ Mola Ver: Su içmeye giderken bas, sayaç dursun.", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = ZomoAmber)
                                Text("🛑 2 Saniye Basılı Tut: Dersi tamamen bitirir ve veline iletir.", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = ZenRoseCoral)
                            }
                        }

                        if (mockScreenshotCount > 0 && !isMockFinished) {
                            Surface(color = ZomoEmeraldContainer, shape = FuturisticPillShape, border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.4f))) {
                                Text("📸 Tebrikler! $mockScreenshotCount adet kanıt çektin!", color = ZomoEmerald, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }

                    3 -> {
                        // STEP 3: Easy Permissions
                        Surface(
                            shape = FuturisticSquircleShape,
                            color = ZomoAmberContainer,
                            border = BorderStroke(1.dp, ZomoAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = ZomoAmber, modifier = Modifier.size(40.dp))
                            }
                        }

                        Text(
                            text = "⚙️ 3. Gerekli 2 Kolay İzin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = ZomoTextPrimary
                        )

                        Text(
                            text = "Baloncuğun ekranda sorunsuz yüzmesi ve ders süresini takip edebilmemiz için telefonunda 2 küçük ayarı açman gerekir:",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = ZomoTextSecondary,
                            lineHeight = 20.sp
                        )

                        Card(
                            shape = FuturisticCardShape,
                            colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                            border = BorderStroke(1.dp, ZomoAmber.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Layers, contentDescription = null, tint = ZomoAmber, modifier = Modifier.size(20.dp))
                                    Text("1. Diğer Uygulamaların Üzerinde Göster:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZomoAmber)
                                }
                                Text("Baloncuğun YouTube ve ders uygulamalarının üzerinde yüzebilmesini sağlar.", fontSize = 12.sp, color = ZomoTextSecondary)

                                Divider(color = ZomoDarkBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = ZomoNeonMint, modifier = Modifier.size(20.dp))
                                    Text("2. Erişilebilirlik Hizmeti:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZomoNeonMint)
                                }
                                Text("Sen soru çözerken çalışmanı takip edip otomatik kanıt toplar.", fontSize = 12.sp, color = ZomoTextSecondary)
                            }
                        }

                        Surface(
                            shape = FuturisticSquircleShape,
                            color = Color(0xFF131D33),
                            border = BorderStroke(1.dp, ZomoDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = ZomoNeonMint, modifier = Modifier.size(18.dp))
                                Text("Ana ekrandaki uyarı kutularına dokunarak bu izinleri tek tıkla açabilirsin.", fontSize = 11.sp, color = ZomoTextSecondary)
                            }
                        }
                    }

                    4 -> {
                        // STEP 4: Quizzes & Question Counters
                        Surface(
                            shape = FuturisticSquircleShape,
                            color = ZomoEmeraldContainer,
                            border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Quiz, contentDescription = null, tint = ZomoEmerald, modifier = Modifier.size(40.dp))
                            }
                        }

                        Text(
                            text = "📝 4. Testler & Soru Sayacı",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = ZomoTextPrimary
                        )

                        Text(
                            text = "Bazı derslerin yanında çözmen gereken hedef soru sayısı veya eğlenceli mini testler bulunur.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = ZomoTextSecondary,
                            lineHeight = 20.sp
                        )

                        Card(
                            shape = FuturisticCardShape,
                            colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                            border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = ZomoEmerald, modifier = Modifier.size(20.dp))
                                    Text("🎯 Soru Hedefi (Örn: 3 Soru):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZomoEmerald)
                                }
                                Text("Dersi bitirirken kaç soru çözdüğünü girersin. Hedefe ulaştığında dersin tamamlanır!", fontSize = 12.sp, color = ZomoTextSecondary)

                                Divider(color = ZomoDarkBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(20.dp))
                                    Text("🧩 Sayısal / Genel Mini Quizler:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenMoonGold)
                                }
                                Text("Test butonuna basıp soruları çözebilirsin. Çözdüğün testlerin puanı anında veline gider.", fontSize = 12.sp, color = ZomoTextSecondary)
                            }
                        }
                    }

                    5 -> {
                        // STEP 5: Parent Approval & Stars
                        Surface(
                            shape = FuturisticSquircleShape,
                            color = ZomoEmeraldContainer,
                            border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(40.dp))
                            }
                        }

                        Text(
                            text = "🏆 5. Veli Onayı ve Yıldızlar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = ZomoTextPrimary
                        )

                        Text(
                            text = "Dersi bitirdiğinde aldığın kanıt fotoğrafları veline iletilir. İşte dersin tamamlanma adımları:",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = ZomoTextSecondary,
                            lineHeight = 20.sp
                        )

                        Card(
                            shape = FuturisticCardShape,
                            colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                            border = BorderStroke(1.dp, ZomoDarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = ZomoEmerald, modifier = Modifier.size(20.dp))
                                    Text("🟢 Onaylandı Rozeti:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZomoEmerald)
                                }
                                Text("Velin kanıtları inceleyip onayladığında dersin üzerinde parlayan yeşil yıldızlar ve onay rozeti çıkar.", fontSize = 12.sp, color = ZomoTextSecondary)

                                Divider(color = ZomoDarkBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ZenRoseCoral, modifier = Modifier.size(20.dp))
                                    Text("🔴 Kırmızı Uyarı Notu:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZenRoseCoral)
                                }
                                Text("Velin bir şeyi eksik bulursa dersin üzerinde kırmızı bir not belirir. O derse tekrar girip tamamlayabilirsin.", fontSize = 12.sp, color = ZomoTextSecondary)

                                Divider(color = ZomoDarkBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ZomoNeonMint, modifier = Modifier.size(20.dp))
                                    Text("❓ Takılırsan:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ZomoNeonMint)
                                }
                                Text("Ana sayfanın sağ üstündeki (?) simgesine basarak bu rehberi istediğin zaman tekrar açabilirsin.", fontSize = 12.sp, color = ZomoTextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = FuturisticPillShape,
                        border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.5f))
                    ) {
                        Text("Geri", color = ZomoPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                    shape = FuturisticPillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZomoNeonMint,
                        contentColor = ZomoNeonMintText
                    )
                ) {
                    Text(
                        text = if (step < totalSteps) "İleri ->" else "🚀 Görev Masama Başla!",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
