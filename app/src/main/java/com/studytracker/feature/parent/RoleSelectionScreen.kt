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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.ui.theme.*

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
        containerColor = ZomoBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZomoBackground,
                    titleContentColor = ZomoPurplePrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ZomoPurplePrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            "StudyTracker",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = ZomoPurplePrimary
                        )
                        if (isTestModeEnabled) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "🧪 TEST",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDevMode) {
                        Icon(
                            imageVector = if (isTestModeEnabled) Icons.Default.Build else Icons.Default.Settings,
                            contentDescription = "Ayarlar / Test Konsolu",
                            tint = if (isTestModeEnabled) ZomoPurplePrimary else Color(0xFF6B7280)
                        )
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
            // Zomo Hero Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, shape = RoundedCornerShape(28.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZomoHeroGradient)
                        .padding(24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ZomoMintAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Çalışma & Hedef Takibi",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Geleceğini Şekillendir,\nHer Gün Bir Adım İleri!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 28.sp
                        )

                        Text(
                            text = "Lütfen devam etmek için giriş yapacağınız modu seçin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role Selection Cards Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Child Card (Student Mode)
                Card(
                    onClick = {
                        if (hasCompletedTutorial) {
                            onNavigateToChildHome()
                        } else {
                            onNavigateToChildTutorial()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, shape = RoundedCornerShape(24.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
                    border = BorderStroke(1.dp, ZomoSquirclePurple.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ZomoSquirclePurple,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = ZomoPurplePrimary,
                                    modifier = Modifier.size(28.dp)
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
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ZomoMintAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "ÖĞRENCİ",
                                        color = Color(0xFF0F766E),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasCompletedTutorial) "Bugünkü derslerine ve görevlerine başla" else "İlk giriş: Hızlı rehber ve görev masam",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                                lineHeight = 16.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = ZomoSoftLavender,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = ZomoPurplePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Parent Card (Parent Mode)
                Card(
                    onClick = { showPinDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, shape = RoundedCornerShape(24.dp), spotColor = ZomoPurplePrimary.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ZomoCardBackground),
                    border = BorderStroke(1.dp, ZomoSquircleEmerald.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ZomoSquircleEmerald,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SupervisorAccount,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(28.dp)
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
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1B4B)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ZomoSquirclePurple
                                ) {
                                    Text(
                                        "YÖNETİM",
                                        color = ZomoPurplePrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Haftalık plan oluştur, onay kuyruğunu ve kanıtları incele",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                                lineHeight = 16.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = ZomoSoftLavender,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ZomoPurplePrimary,
                                    modifier = Modifier.size(16.dp)
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
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, ZomoPurplePrimary.copy(alpha = 0.4f)),
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

    // Parent PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinText = ""
                pinError = false
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = ZomoCardBackground,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ZomoSquirclePurple,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ZomoPurplePrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text("Ebeveyn PIN Girişi", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1B4B))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Lütfen 4 haneli ebeveyn PIN kodunuzu girin (Varsayılan: 1234):", fontSize = 13.sp, color = Color(0xFF4B5563))
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4) pinText = it },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text("Hatalı PIN! Lütfen tekrar deneyin.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    colors = ButtonDefaults.buttonColors(containerColor = ZomoMintAccent, contentColor = Color(0xFF064E3B)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Giriş Yap", fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPinDialog = false },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("İptal", color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
