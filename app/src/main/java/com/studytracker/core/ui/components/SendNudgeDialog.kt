package com.studytracker.core.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager
import kotlinx.coroutines.launch

private data class NudgePreset(
    val chipLabel: String,
    val title: String,
    val message: String,
    val type: String
)

private val NUDGE_PRESETS = listOf(
    NudgePreset(
        chipLabel = "⏰ Ders Vakti",
        title = "Ders Çalışma Zamanı!",
        message = "Bugünkü çalışma planını başlatma vakti geldi. Hazırsan başlayalım! 🚀",
        type = "REMINDER"
    ),
    NudgePreset(
        chipLabel = "🌟 Harika Gidiyorsun",
        title = "Tebrikler!",
        message = "Çalışmalarını ve gayretini takdir ediyorum, harika gidiyorsun! 🌟",
        type = "PRAISE"
    ),
    NudgePreset(
        chipLabel = "☕ Mola Ver",
        title = "Kısa Dinlenme Molası",
        message = "10 dakika gözlerini dinlendir, bir bardak su iç ve tazelen. ☕",
        type = "REMINDER"
    ),
    NudgePreset(
        chipLabel = "📇 Anki Kartları",
        title = "Anki Tekrarı",
        message = "Bugünkü Anki kelime ve kavram kartlarını tamamlamayı unutma! 📇",
        type = "REMINDER"
    ),
    NudgePreset(
        chipLabel = "✍️ Ödevini Bitir",
        title = "Ödev & Soru Pratiği",
        message = "Bugünkü dersin mikro soru pratiğini tamamlayıp onay masasına gönder. 🎯",
        type = "REMINDER"
    ),
    NudgePreset(
        chipLabel = "🚨 Önemli Uyarı",
        title = "Önemli Hatırlatma",
        message = "Lütfen bugünkü bekleyen ders görevlerini tamamla. 🚨",
        type = "URGENT"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNudgeDialog(
    onDismissRequest: () -> Unit,
    onMessageSent: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var titleInput by remember { mutableStateOf("Ders Çalışma Zamanı!") }
    var messageInput by remember { mutableStateOf("Bugünkü çalışma planını başlatma vakti geldi. Hazırsan başlayalım! 🚀") }
    var selectedType by remember { mutableStateOf("REMINDER") }
    var isSending by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isSending) onDismissRequest() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Başlık
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Öğrenciye Bildirim Gönder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Öğrencinin cihazına anlık bildirim ve mesaj iletir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hızlı Şablonlar (Chips)
                Text(
                    text = "Hızlı Şablonlar:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(NUDGE_PRESETS) { preset ->
                        AssistChip(
                            onClick = {
                                titleInput = preset.title
                                messageInput = preset.message
                                selectedType = preset.type
                            },
                            label = { Text(preset.chipLabel, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (titleInput == preset.title) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bildirim Türü Seçimi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "REMINDER" to "⏰ Hatırlatıcı",
                        "PRAISE" to "🌟 Motivasyon",
                        "URGENT" to "🚨 Acil"
                    ).forEach { (typeKey, label) ->
                        val isSelected = selectedType == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = typeKey },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bildirim Başlığı
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Bildirim Başlığı") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mesaj Metni
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    label = { Text("Mesaj / Hatırlatma Metni") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Butonlar
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        enabled = !isSending,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (messageInput.isBlank()) {
                                Toast.makeText(context, "Lütfen bir mesaj metni yazın", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSending = true
                            scope.launch {
                                val res = CloudflareSyncManager.sendMessageToStudent(
                                    context = context,
                                    title = titleInput,
                                    message = messageInput,
                                    type = selectedType
                                )
                                isSending = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "🚀 Bildirim başarıyla iletildi!", Toast.LENGTH_SHORT).show()
                                    onMessageSent()
                                    onDismissRequest()
                                } else {
                                    Toast.makeText(context, "⚠️ Hata: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isSending && messageInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("İletiliyor...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bildirim Gönder")
                        }
                    }
                }
            }
        }
    }
}
