package com.studytracker.feature.parent

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.R
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.ui.theme.*

private val ZenHeroShape = RoundedCornerShape(22.dp)
private val ZenCardShape = RoundedCornerShape(18.dp)
private val ZenSquircleShape = RoundedCornerShape(14.dp)
private val ZenPillShape = CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    onNavigateToChildHome: () -> Unit,
    onNavigateToChildTutorial: () -> Unit,
    onNavigateToParent: () -> Unit,
    onNavigateToDevMode: () -> Unit
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences.getInstance(context) }
    val isNightMode by appPreferences.isNightMode.collectAsState()
    val isTestModeEnabled by appPreferences.isTestModeEnabled.collectAsState()
    val hasCompletedTutorial by appPreferences.hasCompletedTutorial.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ZenSkyCyanContainer)
                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = ZenSkyCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "StudyTracker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = ZomoTextPrimary
                        )
                        if (isTestModeEnabled) {
                            Box(
                                modifier = Modifier
                                    .clip(ZenPillShape)
                                    .background(ZenRoseContainer)
                                    .border(1.dp, ZenRoseCoral.copy(alpha = 0.4f), ZenPillShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🧪 TEST",
                                    color = ZenRoseCoral,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick Day / Night Theme Toggle
                    IconButton(onClick = { appPreferences.toggleNightMode() }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenPaperCard)
                                .border(1.dp, ZenPaperBorder, ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                contentDescription = "Tema Değiştir",
                                tint = if (isNightMode) ZenMoonGold else ZenSkyCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToDevMode) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenPaperCard)
                                .border(1.dp, ZenPaperBorder, ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTestModeEnabled) Icons.Default.Build else Icons.Default.Settings,
                                contentDescription = "Ayarlar / Test Konsolu",
                                tint = if (isTestModeEnabled) ZenSkyCyan else ZomoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Storybook Hero Artwork Card with dark gradient and text overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(ZenHeroShape)
                    .border(1.dp, ZenPaperBorder, ZenHeroShape)
            ) {
                Image(
                    painter = painterResource(id = if (isNightMode) R.drawable.bg_zen_night else R.drawable.bg_zen_day),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignette gradient for 100% text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x33080D1A),
                                    Color(0xDD080D1A)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(ZenPillShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), ZenPillShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isNightMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = ZenMoonGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isNightMode) "Huzurlu Gece Modu" else "Aydınlık Gündüz Modu",
                                color = ZenMoonGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Hoş Geldin! ✨",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.2.sp
                        )
                        Text(
                            text = "Sakin bir zihinle hedeflerine adım at. Giriş yapacağın modu seç.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Role Selection Cards Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Student Mode Card
                Card(
                    onClick = {
                        if (hasCompletedTutorial) {
                            onNavigateToChildHome()
                        } else {
                            onNavigateToChildTutorial()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZenPaperCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenPaperBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(ZenSquircleShape)
                                .background(ZenSkyCyanContainer)
                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.35f), ZenSquircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = ZenSkyCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Öğrenci Modu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ZomoTextPrimary,
                                    maxLines = 1
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(ZenPillShape)
                                        .background(ZenSkyCyanContainer)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ÖĞRENCİ",
                                        color = ZenSkyCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            Text(
                                text = if (hasCompletedTutorial) "Bugünkü derslerine ve görevlerine başla" else "İlk giriş: Hızlı rehber ve görev masam",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZomoTextSecondary,
                                lineHeight = 15.sp,
                                maxLines = 2
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenSkyCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = ZenMintText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Parent Mode Card
                Card(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZenPaperCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenGlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(ZenSquircleShape)
                                .background(ZenForestContainer)
                                .border(1.dp, ZenForestGreen.copy(alpha = 0.35f), ZenSquircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SupervisorAccount,
                                contentDescription = null,
                                tint = ZenForestGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Ebeveyn Modu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ZomoTextPrimary,
                                    maxLines = 1
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(ZenPillShape)
                                        .background(ZenForestContainer)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "YÖNETİM",
                                        color = ZenForestGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            Text(
                                text = "Haftalık plan oluştur, onay kuyruğunu ve kanıtları incele",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZomoTextSecondary,
                                lineHeight = 15.sp,
                                maxLines = 2
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenPaperElevated)
                                .border(1.dp, ZenPaperBorder, ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = ZenSkyCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Developer / Test Mode Entrance Button
            if (isTestModeEnabled) {
                OutlinedButton(
                    onClick = onNavigateToDevMode,
                    shape = ZenPillShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ZenPaperCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(15.dp), tint = ZenSkyCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🛠️ Geliştirici & Test Konsolu", color = ZenSkyCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Parent PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinText = ""
                pinError = false
            },
            shape = ZenCardShape,
            containerColor = ZenNightCard,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ZenSkyCyanContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                    }
                    Text("Ebeveyn PIN Girişi", fontWeight = FontWeight.Bold, color = ZomoTextPrimary, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Lütfen 4 haneli ebeveyn PIN kodunuzu girin (Varsayılan: 1234):", fontSize = 13.sp, color = ZomoTextSecondary)
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4) pinText = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenSkyCyan,
                            unfocusedBorderColor = ZenNightBorder,
                            focusedTextColor = ZomoTextPrimary,
                            unfocusedTextColor = ZomoTextPrimary
                        )
                    )
                    if (pinError) {
                        Text("Hatalı PIN! Lütfen tekrar deneyin.", color = ZenRoseCoral, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinText == "1234" || pinText == "0000") {
                            showPinDialog = false
                            pinText = ""
                            onNavigateToParent()
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan, contentColor = ZenMintText),
                    shape = ZenPillShape
                ) {
                    Text("Giriş Yap", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPinDialog = false },
                    shape = ZenPillShape
                ) {
                    Text("İptal", color = ZomoTextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}
