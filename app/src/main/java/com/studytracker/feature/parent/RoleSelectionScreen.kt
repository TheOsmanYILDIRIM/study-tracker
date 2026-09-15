package com.studytracker.feature.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.ui.theme.EmeraldSuccess
import com.studytracker.core.ui.theme.SapphirePrimary

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
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "StudyTracker",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isTestModeEnabled) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "🧪 TEST MODU",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Small discreeet dev mode access icon when button is hidden
                    IconButton(onClick = onNavigateToDevMode) {
                        Icon(
                            imageVector = if (isTestModeEnabled) Icons.Default.Build else Icons.Default.Settings,
                            contentDescription = "Ayarlar / Test Konsolu",
                            tint = if (isTestModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hoş Geldiniz!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Lütfen devam etmek için rolünüzü seçin",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Child Card
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
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.5.dp, SapphirePrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SapphirePrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Column {
                        Text("🚀 Öğrenci Modu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (hasCompletedTutorial) "Bugünkü derslerimi ve görevlerimi başlat" else "İlk giriş: Hızlı rehber ve görev masam",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Parent Card
            Card(
                onClick = { showPinDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                border = BorderStroke(1.5.dp, EmeraldSuccess)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldSuccess,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Column {
                        Text("👨‍👧 Ebeveyn Modu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Plan oluştur, onay kuyruğunu ve fotoğrafları incele", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Developer / Test Mode Entrance Button (Only prominent when Test Mode is Active)
            if (isTestModeEnabled) {
                OutlinedButton(
                    onClick = onNavigateToDevMode,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🛠️ Geliştirici & Test Modu Konsolu")
                }
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
            title = { Text("Ebeveyn PIN Girişi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lütfen 4 haneli ebeveyn PIN kodunuzu girin (Varsayılan: 1234):")
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4) pinText = it },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text("Hatalı PIN! Lütfen tekrar deneyin.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pinText == "1234" || pinText == "0000") {
                        showPinDialog = false
                        pinText = ""
                        onNavigateToParent()
                    } else {
                        pinError = true
                    }
                }) {
                    Text("Giriş Yap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
