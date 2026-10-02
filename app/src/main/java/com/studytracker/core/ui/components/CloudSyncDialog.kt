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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.package_exchange.StudyPackageExchangeManager
import com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager
import com.studytracker.core.data.remote.v2.V2CloudClient
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CloudSyncDialog(
    isParent: Boolean,
    onDismissRequest: () -> Unit,
    onConflictDetected: ((SyncConflictData, com.studytracker.core.data.remote.cloudflare.CloudSyncPayloadWrapper) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val prefs = remember { AppPreferences.getInstance(context) }
    val currentCode by prefs.familyPairCode.collectAsState()
    val adminToken by prefs.familyAdminToken.collectAsState()

    var codeInput by remember(currentCode) { mutableStateOf(currentCode) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResultText by remember { mutableStateOf<String?>(null) }
    var syncResultSuccess by remember { mutableStateOf<Boolean?>(null) }
    var adminTokenInput by remember(adminToken) { mutableStateOf(adminToken) }

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
                                scope.launch {
                                    isSyncing = true
                                    val created = CloudflareSyncManager.createFamily(context)
                                    isSyncing = false
                                    created.onSuccess { newCode ->
                                        codeInput = newCode
                                        Toast.makeText(context, "🔐 Yeni güvenli Aile Kodu üretildi: $newCode", Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Yeni kod oluşturulamadı: ${err.message}", Toast.LENGTH_LONG).show()
                                    }
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
                                Icon(Icons.Default.Refresh, contentDescription = "Yeni Kod", tint = ZenMoonGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (isParent) {
                    if (adminToken.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF101D32),
                            border = BorderStroke(1.dp, ZenPaperBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CLI Yönetici Anahtarı", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Text("••••••••" + adminToken.takeLast(6), color = ZomoTextSecondary, fontSize = 10.5.sp)
                                }
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(adminToken))
                                    Toast.makeText(context, "🔐 Yönetici anahtarı panoya kopyalandı", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Yönetici anahtarını kopyala", tint = ZenSkyCyan)
                                }
                            }
                        }
                        Text(
                            "Bu anahtarı yalnız kendi CLI cihazınızda kullanın; öğrenci cihazıyla paylaşmayın.",
                            fontSize = 10.5.sp,
                            color = ZomoTextMuted
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ZenMoonGold.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, ZenMoonGold.copy(alpha = 0.65f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Yönetici anahtarı bu cihazda kayıtlı değil",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Mevcut aileye ait yönetici anahtarını biliyorsan aşağıya girip kaydedebilirsin. Bu anahtar yalnız cihazdaki özel uygulama ayarlarında tutulur.",
                                    color = ZomoTextSecondary,
                                    fontSize = 10.5.sp
                                )
                                OutlinedTextField(
                                    value = adminTokenInput,
                                    onValueChange = { adminTokenInput = it.trim() },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    label = { Text("Yönetici anahtarı") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ZenSkyCyan,
                                        unfocusedBorderColor = ZenPaperBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Button(
                                    onClick = {
                                        val clean = adminTokenInput.trim()
                                        if (clean.isBlank()) {
                                            Toast.makeText(context, "Yönetici anahtarı boş olamaz", Toast.LENGTH_SHORT).show()
                                        } else {
                                            prefs.setFamilyAdminToken(clean)
                                            Toast.makeText(context, "🔐 Yönetici anahtarı kaydedildi", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan)
                                ) {
                                    Text("Anahtarı kaydet", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isSyncing = true
                                            val pair = CloudflareSyncManager.pairFamilyCode(context, codeInput)
                                            isSyncing = false
                                            pair.onSuccess { pairedCode ->
                                                codeInput = pairedCode
                                                if (prefs.familyAdminToken.value.isNotBlank()) {
                                                    Toast.makeText(context, "🔐 CLI yönetici anahtarı oluşturuldu ve kaydedildi", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Bu aile kodunun anahtarı geri alınamıyor. Yeni güvenli aile oluşturabilirsin.", Toast.LENGTH_LONG).show()
                                                }
                                            }.onFailure { err ->
                                                Toast.makeText(context, "Eşleştirme başarısız: ${err.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    enabled = !isSyncing,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182642))
                                ) {
                                    Text("Mevcut aile kodunu güvenli eşleştir")
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isSyncing = true
                                            val created = CloudflareSyncManager.createFamily(context)
                                            isSyncing = false
                                            created.onSuccess { newCode ->
                                                codeInput = newCode
                                                if (prefs.familyAdminToken.value.isNotBlank()) {
                                                    Toast.makeText(context, "🔐 Yeni aile kodu ve CLI yönetici anahtarı oluşturuldu", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Yeni aile oluşturuldu ancak yönetici anahtarı kaydedilemedi", Toast.LENGTH_LONG).show()
                                                }
                                            }.onFailure { err ->
                                                Toast.makeText(context, "Yeni güvenli aile oluşturulamadı: ${err.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    enabled = !isSyncing,
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, ZenMoonGold),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenMoonGold)
                                ) {
                                    Text("Yeni aile kodu + yönetici anahtarı oluştur")
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "CLI yönetici anahtarı yalnız StudyTracker Veli uygulamasında gösterilir.",
                        fontSize = 10.5.sp,
                        color = ZomoTextMuted
                    )
                }

                // Cloud Sync Trigger Button
                Button(
                    onClick = {
                        if (codeInput.isBlank()) {
                            Toast.makeText(context, "Lütfen bir Aile Kodu girin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSyncing = true
                        syncResultText = null
                        syncResultSuccess = null

                        scope.launch {
                            val pairResult = CloudflareSyncManager.pairFamilyCode(context, codeInput)
                            if (pairResult.isFailure) {
                                isSyncing = false
                                syncResultSuccess = false
                                syncResultText = "Eşleştirme hatası: ${pairResult.exceptionOrNull()?.message}"
                                return@launch
                            }
                            codeInput = pairResult.getOrThrow()

                            val v2CloudStatus = V2CloudClient.fetchCatalog(
                                familyCode = codeInput,
                                adminToken = if (isParent) prefs.familyAdminToken.value.ifBlank { null } else null,
                                role = if (isParent) "PARENT" else "CLIENT"
                            ).fold(
                                onSuccess = { courses ->
                                    val lessonCount = courses.sumOf { it.lessons.size }
                                    val itemCount = courses.sumOf { course -> course.lessons.sumOf { it.items.size } }
                                    "V2 bulut: ${courses.size} ders, $lessonCount konu, $itemCount öğe"
                                },
                                onFailure = { err -> "V2 bulut okunamadı: ${err.message}" }
                            )

                            if (isParent && onConflictDetected != null) {
                                when (val checkRes = CloudflareSyncManager.syncWithConflictCheck(context)) {
                                    is com.studytracker.core.data.remote.cloudflare.SyncCheckResult.Conflict -> {
                                        isSyncing = false
                                        onDismissRequest()
                                        onConflictDetected(checkRes.conflictData, checkRes.cloudData)
                                    }
                                    is com.studytracker.core.data.remote.cloudflare.SyncCheckResult.Success -> {
                                        isSyncing = false
                                        syncResultSuccess = true
                                        syncResultText = "${checkRes.message}\n$v2CloudStatus"
                                        Toast.makeText(context, "✅ $v2CloudStatus", Toast.LENGTH_LONG).show()
                                    }
                                    is com.studytracker.core.data.remote.cloudflare.SyncCheckResult.Error -> {
                                        isSyncing = false
                                        syncResultSuccess = false
                                        syncResultText = "V1: ${checkRes.message}\n$v2CloudStatus"
                                    }
                                }
                            } else {
                                val res = CloudflareSyncManager.syncWithCloud(context)
                                isSyncing = false
                                if (res.isSuccess) {
                                    syncResultSuccess = true
                                    val v1Status = res.getOrNull() ?: "V1 senkronizasyon başarılı"
                                    syncResultText = "$v1Status\n$v2CloudStatus"
                                    Toast.makeText(context, "✅ $v2CloudStatus", Toast.LENGTH_LONG).show()
                                } else {
                                    syncResultSuccess = false
                                    syncResultText = "V1 hata: ${res.exceptionOrNull()?.message}\n$v2CloudStatus"
                                }
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

                if (isParent) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isSyncing = true
                                syncResultText = null
                                syncResultSuccess = null
                                val result = StudyPackageExchangeManager.uploadLocalV2CatalogToCloud(context)
                                isSyncing = false
                                result.onSuccess { msg ->
                                    syncResultSuccess = true
                                    syncResultText = msg
                                    Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                                }.onFailure { err ->
                                    syncResultSuccess = false
                                    syncResultText = "Yerelden buluta aktarım hatası: ${err.message}"
                                    Toast.makeText(context, "❌ ${err.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ZenMoonGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenMoonGold)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yereldeki V2’yi Buluta Aktar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
