package com.studytracker.feature.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.ui.theme.*

private val FuturisticHeroShape = RoundedCornerShape(32.dp)
private val FuturisticCardShape = RoundedCornerShape(28.dp)
private val FuturisticSquircleShape = RoundedCornerShape(18.dp)
private val FuturisticPillShape = CircleShape

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
    val isTestModeEnabled by appPreferences.isTestModeEnabled.collectAsState()
    val hasCompletedTutorial by appPreferences.hasCompletedTutorial.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

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
                            shape = RoundedCornerShape(14.dp),
                            color = ZomoVioletContainer,
                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = ZomoPurplePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            "StudyTracker",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = ZomoTextPrimary
                        )
                        if (isTestModeEnabled) {
                            Surface(
                                shape = FuturisticPillShape,
                                color = ZomoPinkContainer,
                                border = BorderStroke(1.dp, ZomoPink.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "🧪 TEST",
                                    color = ZomoPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDevMode) {
                        Surface(
                            shape = FuturisticPillShape,
                            color = ZomoDarkSurface,
                            border = BorderStroke(1.dp, ZomoDarkBorder),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isTestModeEnabled) Icons.Default.Build else Icons.Default.Settings,
                                    contentDescription = "Ayarlar / Test Konsolu",
                                    tint = if (isTestModeEnabled) ZomoPurplePrimary else ZomoTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Futuristic Hero Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FuturisticHeroShape)
                    .background(ZomoHeroGradient)
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = FuturisticPillShape,
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ZomoNeonMint,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Akıllı Çalışma & Hedef Takibi",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Geleceğini Şekillendir,\nHer Gün Bir Adım İleri!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 30.sp
                    )

                    Text(
                        text = "Lütfen devam etmek için giriş yapacağınız modu seçin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role Selection Cards Column (Futuristic Dark Glass)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    shape = FuturisticCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                    border = BorderStroke(1.dp, ZomoDarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = FuturisticSquircleShape,
                            color = ZomoVioletContainer,
                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = ZomoPurplePrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Öğrenci Modu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = ZomoTextPrimary
                                )
                                Surface(
                                    shape = FuturisticPillShape,
                                    color = ZomoNeonMintContainer
                                ) {
                                    Text(
                                        "ÖĞRENCİ",
                                        color = ZomoNeonMint,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (hasCompletedTutorial) "Bugünkü derslerine ve görevlerine başla" else "İlk giriş: Hızlı rehber ve görev masam",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZomoTextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Surface(
                            shape = FuturisticPillShape,
                            color = ZomoNeonMint,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = ZomoNeonMintText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Parent Mode Card
                Card(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FuturisticCardShape,
                    colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
                    border = BorderStroke(1.dp, ZomoGlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = FuturisticSquircleShape,
                            color = ZomoEmeraldContainer,
                            border = BorderStroke(1.dp, ZomoEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SupervisorAccount,
                                    contentDescription = null,
                                    tint = ZomoEmerald,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Ebeveyn Modu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = ZomoTextPrimary
                                )
                                Surface(
                                    shape = FuturisticPillShape,
                                    color = ZomoVioletContainer
                                ) {
                                    Text(
                                        "YÖNETİM",
                                        color = ZomoPurplePrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Haftalık plan oluştur, onay kuyruğunu ve kanıtları incele",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZomoTextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Surface(
                            shape = FuturisticPillShape,
                            color = Color(0x33A855F7),
                            border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ZomoPurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Developer / Test Mode Entrance Button
            if (isTestModeEnabled) {
                OutlinedButton(
                    onClick = onNavigateToDevMode,
                    shape = FuturisticPillShape,
                    border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp), tint = ZomoPurplePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🛠️ Geliştirici & Test Konsolu", color = ZomoPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Parent PIN Dialog (Dark Futuristic)
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinText = ""
                pinError = false
            },
            shape = FuturisticCardShape,
            containerColor = ZomoDarkCard,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ZomoVioletContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text("Ebeveyn PIN Girişi", fontWeight = FontWeight.Black, color = ZomoTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Lütfen 4 haneli ebeveyn PIN kodunuzu girin (Varsayılan: 1234):", fontSize = 13.sp, color = ZomoTextSecondary)
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4) pinText = it },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZomoPurplePrimary,
                            unfocusedBorderColor = ZomoDarkBorder,
                            focusedTextColor = ZomoTextPrimary,
                            unfocusedTextColor = ZomoTextPrimary
                        )
                    )
                    if (pinError) {
                        Text("Hatalı PIN! Lütfen tekrar deneyin.", color = ZomoPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    colors = ButtonDefaults.buttonColors(containerColor = ZomoNeonMint, contentColor = ZomoNeonMintText),
                    shape = FuturisticPillShape
                ) {
                    Text("Giriş Yap", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPinDialog = false },
                    shape = FuturisticPillShape
                ) {
                    Text("İptal", color = ZomoTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
