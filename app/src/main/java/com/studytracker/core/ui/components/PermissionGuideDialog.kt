package com.studytracker.core.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.studytracker.core.service.StudyAccessibilityService
import com.studytracker.core.ui.theme.*

@Composable
fun PermissionGuideDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var hasAccessibilityPermission by remember {
        mutableStateOf(StudyAccessibilityService.isAccessibilityServiceEnabled(context) || StudyAccessibilityService.isServiceRunning())
    }

    // Re-check permissions when user comes back from Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = checkOverlayPermission(context)
                hasAccessibilityPermission = StudyAccessibilityService.isAccessibilityServiceEnabled(context) || StudyAccessibilityService.isServiceRunning()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Scrim & Modal Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC080D1A))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF10192D),
                border = BorderStroke(1.dp, ZenNightBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
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
                                .size(40.dp)
                                .background(ZenMoonGoldContainer, CircleShape)
                                .border(1.dp, ZenMoonGold.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ZenMoonGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "İzin & Kanıt Hizmeti",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Ders takibi ve ebeveyn onayı için gerekli",
                                fontSize = 11.5.sp,
                                color = ZomoTextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Divider(color = ZenPaperBorder.copy(alpha = 0.3f), thickness = 0.8.dp)

                    Text(
                        text = "StudyTracker'ın sorunsuz çalışabilmesi için 2 temel izne ihtiyacı vardır. İlgili ayarları açmak için aşağıdaki butonları kullanabilirsiniz:",
                        fontSize = 12.sp,
                        color = ZomoTextSecondary,
                        lineHeight = 16.sp
                    )

                    // 1. Overlay Permission Card
                    PermissionItemCard(
                        title = "1. Yüzen Kronometre (Overlay)",
                        description = "Ders çalışırken YouTube veya test uygulamalarının üzerinde yüzen canlı süreyi ve sayaç butonunu gösterir.",
                        isGranted = hasOverlayPermission,
                        buttonText = if (hasOverlayPermission) "İzin Aktif ✅" else "İzni Aç / Ayarlar",
                        onAction = {
                            openOverlaySettings(context)
                        }
                    )

                    // 2. Accessibility Permission Card
                    PermissionItemCard(
                        title = "2. Kanıt Alma Hizmeti (Erişilebilirlik)",
                        description = "Ders bitiminde öğrencinin çalıştığını veliye kanıtlayacak ekran görüntülerini sessizce kaydedip veli onayına iletir.",
                        isGranted = hasAccessibilityPermission,
                        buttonText = if (hasAccessibilityPermission) "Hizmet Aktif ✅" else "Hizmeti Aç / Ayarlar",
                        onAction = {
                            StudyAccessibilityService.openAccessibilitySettings(context)
                        }
                    )

                    // Close Button
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasOverlayPermission && hasAccessibilityPermission) ZenForestGreen else ZenSkyCyan,
                            contentColor = Color(0xFF080D1A)
                        )
                    ) {
                        Text(
                            text = if (hasOverlayPermission && hasAccessibilityPermission) "Harika! Tüm İzinler Tamam" else "Anladım, Devam Et",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141F36),
        border = BorderStroke(1.dp, if (isGranted) ZenForestGreen.copy(alpha = 0.5f) else ZenPaperBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isGranted) ZenForestGreen.copy(alpha = 0.2f) else ZenMoonGold.copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, if (isGranted) ZenForestGreen else ZenMoonGold),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (isGranted) "AKTİF" else "GEREKLİ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) Color(0xFF86EFAC) else ZenMoonGold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = 11.5.sp,
                color = ZomoTextSecondary,
                lineHeight = 15.sp
            )

            OutlinedButton(
                onClick = onAction,
                enabled = !isGranted,
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isGranted) ZenForestGreen.copy(alpha = 0.6f) else ZenSkyCyan),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isGranted) Color(0xFF86EFAC) else ZenSkyCyan,
                    disabledContentColor = Color(0xFF86EFAC)
                )
            ) {
                Text(buttonText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun checkOverlayPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }
}

private fun openOverlaySettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
