package com.studytracker.core.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.window.Dialog
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.remote.sync.CloudSyncManager
import com.studytracker.core.data.remote.sync.SyncState
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch

private val ZenCardShape = RoundedCornerShape(20.dp)
private val ZenPillShape = CircleShape

@Composable
fun CloudSyncDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncManager = remember { CloudSyncManager.getInstance(context) }
    val prefs = remember { AppPreferences.getInstance(context) }

    val syncState by syncManager.syncState.collectAsState()
    val familyCode by prefs.familyPairCode.collectAsState()
    var inputCode by remember { mutableStateOf("") }
    var showCustomConfig by remember { mutableStateOf(false) }

    var customUrl by remember { mutableStateOf(prefs.supabaseUrl.value) }
    var customKey by remember { mutableStateOf(prefs.supabaseAnonKey.value) }

    val currentCode = remember(familyCode) {
        if (familyCode.isEmpty()) syncManager.getOrCreateFamilyCode() else familyCode
    }

    LaunchedEffect(Unit) {
        syncManager.syncAll()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = ZenCardShape,
            color = Color(0xF00D1527),
            border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenSkyCyanContainer)
                                .border(1.dp, ZenSkyCyan.copy(alpha = 0.4f), ZenPillShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Senkronizasyon Masası", color = ZomoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Girişsiz Hazır Bulut + Binder IPC", color = ZenSkyCyan, fontSize = 11.sp)
                        }
                    }

                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = ZomoTextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Divider(color = ZenPaperBorder, thickness = 0.8.dp)

                // Sync Mode Info Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZenSkyCyanContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(14.dp))
                            Text("Çift APK Köprüsü: Aktif", fontSize = 10.5.sp, color = ZenSkyCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x601A243D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenPaperBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = ZomoTextSecondary, modifier = Modifier.size(14.dp))
                            Text("Bulut Eşitleme: Otomatik", fontSize = 10.5.sp, color = ZomoTextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Family Code Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x8014203D))
                        .border(1.dp, ZenSkyCyan.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔑 AİLE EŞLEŞME KODU", color = ZomoTextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentCode,
                                color = ZenSkyCyan,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Aile Kodu", currentCode))
                                    Toast.makeText(context, "Aile kodu kopyalandı!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = ZenSkyCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            "Diğer cihazda da bu kodu girerek canlı senkronizasyonu başlatabilirsiniz.",
                            color = ZomoTextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Join Other Family Code Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase() },
                        placeholder = { Text("ST-XXXX", color = ZomoTextMuted, fontSize = 13.sp) },
                        label = { Text("Farklı Koda Bağlan", color = ZomoTextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ZomoTextPrimary,
                            unfocusedTextColor = ZomoTextPrimary,
                            focusedBorderColor = ZenSkyCyan,
                            unfocusedBorderColor = ZenPaperBorder,
                            focusedLabelColor = ZenSkyCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (inputCode.isNotBlank()) {
                                syncManager.joinFamily(inputCode)
                                inputCode = ""
                                Toast.makeText(context, "Yeni aile kodu kaydedildi!", Toast.LENGTH_SHORT).show()
                                scope.launch { syncManager.syncAll() }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Bağlan", color = Color(0xFF070B14), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Status banner
                when (val state = syncState) {
                    is SyncState.Syncing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ZenSkyCyan, strokeWidth = 2.dp)
                            Text("Bulutla eşitleniyor...", color = ZenSkyCyan, fontSize = 12.sp)
                        }
                    }
                    is SyncState.Success -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(16.dp))
                            Text(state.message, color = ZenForestGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    is SyncState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ZenRoseCoral, modifier = Modifier.size(16.dp))
                            Text(state.errorMessage, color = ZenRoseCoral, fontSize = 11.sp, maxLines = 2)
                        }
                    }
                    else -> {}
                }

                // Sync Now Action Button
                Button(
                    onClick = {
                        scope.launch {
                            syncManager.syncAll()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = ZenPillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF070B14), modifier = Modifier.size(18.dp))
                        Text("Şimdi Senkronize Et", color = Color(0xFF070B14), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        scope.launch {
                            val res = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.importPackageFromUri(context, uri)
                            res.onSuccess { msg ->
                                Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                                syncManager.syncAll()
                            }.onFailure { err ->
                                Toast.makeText(context, "❌ Yükleme hatası: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // .studyplan Package Sharing (WhatsApp, Telegram, QuickShare)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x60132038),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📦 .studyplan ÖZEL DOSYA KÖPRÜSÜ",
                            color = ZenSkyCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tüm haftalık planı veya günlük WebP kanıtlı çalışma raporunu tek bir dosya olarak WhatsApp'tan atın, diğer telefonda dokununca otomatik yüklensin.",
                            color = ZomoTextMuted,
                            fontSize = 10.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val file = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.exportDailyReportPackage(context)
                                        com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.sharePackageFile(
                                            context,
                                            file,
                                            "Çalışma Raporu ve Kanıtları Paylaş"
                                        )
                                    }
                                },
                                shape = ZenPillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyanContainer),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Raporu Paylaş", color = ZenSkyCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                shape = ZenPillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen.copy(alpha = 0.2f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(".studyplan Seç", color = ZenForestGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Direct Copy/Paste & Native Share Bridge Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val file = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.exportPlanPackage(context)
                                com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.sharePackageFile(
                                    context,
                                    file,
                                    "Haftalık Planı Paylaş"
                                )
                            }
                        },
                        shape = ZenPillShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Planı Gönder", color = ZenSkyCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipItem = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clipItem.isNullOrBlank() && clipItem.contains("familyCode")) {
                                scope.launch {
                                    val res = com.studytracker.core.data.package_exchange.StudyPackageExchangeManager.importPackageString(context, clipItem)
                                    res.onSuccess { count ->
                                        Toast.makeText(context, "✅ $count", Toast.LENGTH_LONG).show()
                                        syncManager.syncAll()
                                    }.onFailure {
                                        Toast.makeText(context, "❌ Geçersiz veri formatı!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Panoda geçerli StudyTracker verisi bulunamadı!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = ZenPillShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = ZenForestGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Panodan Yükle", color = ZenForestGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
