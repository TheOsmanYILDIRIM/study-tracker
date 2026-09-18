package com.studytracker.core.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.package_exchange.StudyPackageExchangeManager
import com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CloudSyncDialog(
    isParent: Boolean,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val prefs = remember { AppPreferences.getInstance(context) }
    val currentCode by prefs.familyPairCode.collectAsState()

    var codeInput by remember(currentCode) { mutableStateOf(currentCode) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResultText by remember { mutableStateOf<String?>(null) }
    var syncResultSuccess by remember { mutableStateOf<Boolean?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val res = StudyPackageExchangeManager.importPackageFromUri(context, uri)
                res.onSuccess { msg ->
                    syncResultSuccess = true
                    syncResultText = "✅ $msg"
                    Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    syncResultSuccess = false
                    syncResultText = "❌ İçe aktarma hatası: ${err.message}"
                    Toast.makeText(context, "Hata: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Dialog(onDismissRequest = { if (!isSyncing) onDismissRequest() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF10192D),
            border = BorderStroke(1.dp, ZenNightBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(ZenSkyCyanContainer, CircleShape)
                            .border(1.dp, ZenSkyCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = ZenSkyCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isParent) "☁️ Aile Bulut & Paylaşım" else "☁️ Ebeveyn Bulut & Paylaşım",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Sunucusuz bulut eşitleme ve WhatsApp köprüsü",
                            fontSize = 11.5.sp,
                            color = ZomoTextSecondary
                        )
                    }
                }

                Divider(color = ZenPaperBorder.copy(alpha = 0.3f), thickness = 0.8.dp)

                // 1. Cloudflare Sync Section
                Text(
                    text = if (isParent) "1. Aile Eşleşme Kodunuz (Cloudflare)" else "1. Ebeveyn Aile Kodu (Cloudflare)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.uppercase().trim() },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Örn: ST-4921", color = ZomoTextSecondary.copy(alpha = 0.6f)) },
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ZenSkyCyan
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenSkyCyan,
                            unfocusedBorderColor = ZenPaperBorder,
                            focusedContainerColor = Color(0xFF141F36),
                            unfocusedContainerColor = Color(0xFF141F36)
                        )
                    )

                    IconButton(
                        onClick = {
                            if (codeInput.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(codeInput))
                                Toast.makeText(context, "📋 Aile Kodu kopyalandı: $codeInput", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF182642), RoundedCornerShape(10.dp))
                                .border(1.dp, ZenPaperBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (isParent) {
                        IconButton(
                            onClick = {
                                val newCode = prefs.generateNewFamilyCode()
                                codeInput = newCode
                                Toast.makeText(context, "🎲 Yeni Aile Kodu üretildi: $newCode", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF182642), RoundedCornerShape(10.dp))
                                    .border(1.dp, ZenPaperBorder, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Yeni Kod", tint = ZenMoonGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Cloud Sync Trigger Button
                Button(
                    onClick = {
                        if (codeInput.isBlank()) {
                            Toast.makeText(context, "Lütfen bir Aile Kodu girin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        prefs.setFamilyPairCode(codeInput)
                        isSyncing = true
                        syncResultText = null
                        syncResultSuccess = null

                        scope.launch {
                            val res = CloudflareSyncManager.syncWithCloud(context)
                            isSyncing = false
                            if (res.isSuccess) {
                                syncResultSuccess = true
                                syncResultText = res.getOrNull() ?: "Senkronizasyon Başarılı"
                                Toast.makeText(context, "✅ Senkronizasyon Başarılı!", Toast.LENGTH_SHORT).show()
                            } else {
                                syncResultSuccess = false
                                syncResultText = "Hata: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenSkyCyan,
                        contentColor = Color(0xFF080D1A)
                    )
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = Color(0xFF080D1A), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bulutla Eşitleniyor...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("☁️ Şimdi Bulutla Eşitle", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = ZenPaperBorder.copy(alpha = 0.3f), thickness = 0.8.dp)

                // 2. WhatsApp & File Share Section
                Text(
                    text = "2. Alternatif: WhatsApp & Dosya Köprüsü (.studyplan)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val pkgFile = if (isParent) {
                                        StudyPackageExchangeManager.exportPlanPackage(context)
                                    } else {
                                        StudyPackageExchangeManager.exportDailyReportPackage(context)
                                    }
                                    val title = if (isParent) "Haftalık Çalışma Planı" else "Günlük Çalışma Raporu"
                                    StudyPackageExchangeManager.sharePackageFile(context, pkgFile, title)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Paylaşım hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF25D366)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF25D366)
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isParent) "Planı Paylaş" else "Raporu Paylaş", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ZenMoonGold),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ZenMoonGold
                        )
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dosya Yükle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Result Box
                if (syncResultText != null) {
                    val isSuccess = syncResultSuccess == true
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSuccess) ZenForestGreen.copy(alpha = 0.15f) else ZenRoseCoral.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isSuccess) ZenForestGreen else ZenRoseCoral)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isSuccess) ZenForestGreen else ZenRoseCoral,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = syncResultText!!,
                                fontSize = 12.sp,
                                color = if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Close Button
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ZenPaperBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZomoTextSecondary)
                ) {
                    Text("Kapat", fontSize = 13.sp)
                }
            }
        }
    }
}
