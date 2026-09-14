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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.ui.overlay.FloatingHUDView
import com.studytracker.core.ui.theme.EmeraldContainer
import com.studytracker.core.ui.theme.EmeraldSuccess
import com.studytracker.core.ui.theme.PurpleActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildTutorialScreen(
    onFinishTutorial: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var mockScreenshotCount by remember { mutableIntStateOf(0) }
    var isMockFinished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚀 StudyTracker Rehberi", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onFinishTutorial) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Indicator
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = EmeraldSuccess
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                            }
                        }

                        Text(
                            text = "Görev Masana Hoş Geldin!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Her gün ve her hafta için ebeveynin sana özel ders hedefleri hazırlar. Tek yapman gereken 'Çalışmayı Başlat' butonuna dokunmak!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                2 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🌟 Süper Gücün: Yüzen Düğme!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Ders başladığında ekranda bu mor düğme görünür. Hadi aşağıdaki düğmeyle şimdi dene:",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Interactive Sandbox Floating HUD Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F172A))
                                .border(2.dp, PurpleActive, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isMockFinished) {
                                FloatingHUDView(
                                    elapsedSeconds = 125,
                                    screenshotCount = mockScreenshotCount,
                                    isFinishing = false,
                                    onSingleTapCapture = {
                                        mockScreenshotCount++
                                    },
                                    onLongPressFinish = {
                                        isMockFinished = true
                                    }
                                )
                            } else {
                                Text(
                                    text = "🎉 Harika! Dersi Başarıyla Bitirdin!",
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👉 1 Kez Hafifçe Dokun: Anlık kanıt fotoğrafı çeker.", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("👉 2 Saniye Basılı Tut: Dersi bitirir ve ebeveynine iletir.", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        if (mockScreenshotCount > 0 && !isMockFinished) {
                            Surface(color = EmeraldContainer, shape = RoundedCornerShape(8.dp)) {
                                Text("📸 Tebrikler! $mockScreenshotCount adet fotoğraf çektin!", color = Color(0xFF065F46), modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                3 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldContainer,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(48.dp))
                            }
                        }

                        Text(
                            text = "Onay & Başarı Rozetleri",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Dersin bittiğinde fotoğraflar ebeveynine iletilir. Ebeveynin kontrol edip onayladığında görevin yeşil olur ve hedefini tamamlarsın!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                        if (step < 3) {
                            step++
                        } else {
                            onFinishTutorial()
                        }
                    },
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text(
                        text = if (step < 3) "İleri ->" else "🚀 Görev Masama Başla!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
