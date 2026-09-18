package com.studytracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.ui.theme.*

private val DAYS_LIST = listOf(
    "MON" to "Pzt",
    "TUE" to "Sal",
    "WED" to "Çar",
    "THU" to "Per",
    "FRI" to "Cum",
    "SAT" to "Cmt",
    "SUN" to "Paz"
)

private val DURATION_PRESETS = listOf(15, 20, 25, 30, 40, 45, 60, 90)

@Composable
fun EditTaskDialog(
    task: Occurrence,
    onDismissRequest: () -> Unit,
    onSaveTask: (updatedTask: Occurrence) -> Unit,
    onDeleteTask: (occurrenceKey: String) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var plannedMinutesStr by remember { mutableStateOf(task.plannedMinutes.toString()) }
    var youtubeUrl by remember { mutableStateOf(task.youtubeUrl ?: extractVideoUrl(task) ?: "") }
    var targetCountStr by remember { mutableStateOf((task.targetCount ?: 0).let { if (it > 0) it.toString() else "" }) }
    var parentNote by remember { mutableStateOf(task.warningText ?: "") }
    var selectedDay by remember {
        val d = task.date ?: ""
        mutableStateOf(if (d.length >= 3) d.take(3).uppercase() else "MON")
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ZenRoseCoral)
                    Text("Dersi Programdan Sil?", fontWeight = FontWeight.Bold, color = ZomoTextPrimary)
                }
            },
            text = {
                Text(
                    "'${task.title}' dersi haftalık programdan ve buluttan tamamen silinecektir.\n\nEmin misiniz?",
                    color = ZomoTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteTask(task.occurrenceKey)
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenRoseCoral)
                ) {
                    Text("Evet, Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Vazgeç", color = ZomoTextSecondary)
                }
            },
            containerColor = Color(0xFF10192E)
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ZenSkyCyanContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "Dersi Düzenle",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Dersi Sil", tint = ZenRoseCoral)
                    }
                }

                Divider(color = ZenPaperBorder.copy(alpha = 0.3f), thickness = 0.8.dp)

                // Title Input
                Text("Ders / Görev Adı", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZomoTextSecondary)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Örn: Matematik - Fonksiyonlar") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenSkyCyan,
                        unfocusedBorderColor = ZenPaperBorder,
                        focusedContainerColor = Color(0xFF141F36),
                        unfocusedContainerColor = Color(0xFF141F36)
                    )
                )

                // Day Selection
                Text("Ders Günü", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZomoTextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DAYS_LIST.forEach { (code, label) ->
                        val isSelected = selectedDay == code
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ZenSkyCyan else Color(0xFF182642),
                            border = BorderStroke(1.dp, if (isSelected) ZenSkyCyan else ZenPaperBorder),
                            modifier = Modifier.clickable { selectedDay = code }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF080D1A) else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Planned Minutes
                Text("Hedef Süre (Dakika)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZomoTextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = plannedMinutesStr,
                        onValueChange = { plannedMinutesStr = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("40") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenSkyCyan,
                            unfocusedBorderColor = ZenPaperBorder,
                            focusedContainerColor = Color(0xFF141F36),
                            unfocusedContainerColor = Color(0xFF141F36)
                        )
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DURATION_PRESETS.take(4).forEach { min ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF182642),
                                border = BorderStroke(1.dp, ZenPaperBorder),
                                modifier = Modifier.clickable { plannedMinutesStr = min.toString() }
                            ) {
                                Text(
                                    text = "$min dk",
                                    fontSize = 11.sp,
                                    color = ZenSkyCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // YouTube URL
                Text("🎬 Video / Konu Anlatım Linki (YouTube)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZomoTextSecondary)
                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it.trim() },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://youtube.com/watch?v=...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenSkyCyan,
                        unfocusedBorderColor = ZenPaperBorder,
                        focusedContainerColor = Color(0xFF141F36),
                        unfocusedContainerColor = Color(0xFF141F36)
                    )
                )

                // Target Question Count (Optional)
                Text("Hedef Soru Sayısı (İsteğe Bağlı)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZomoTextSecondary)
                OutlinedTextField(
                    value = targetCountStr,
                    onValueChange = { targetCountStr = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Örn: 20") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenSkyCyan,
                        unfocusedBorderColor = ZenPaperBorder,
                        focusedContainerColor = Color(0xFF141F36),
                        unfocusedContainerColor = Color(0xFF141F36)
                    )
                )

                // Veli Notu / İpucu
                Text("Veli Notu & Yönergesi (Öğrenci Görür)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ZomoTextSecondary)
                OutlinedTextField(
                    value = parentNote,
                    onValueChange = { parentNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    placeholder = { Text("Örn: Örnek soruları çözmeyi unutma") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenSkyCyan,
                        unfocusedBorderColor = ZenPaperBorder,
                        focusedContainerColor = Color(0xFF141F36),
                        unfocusedContainerColor = Color(0xFF141F36)
                    )
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ZenPaperBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZomoTextSecondary)
                    ) {
                        Text("Vazgeç", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            val duration = plannedMinutesStr.toIntOrNull() ?: task.plannedMinutes
                            val tCount = targetCountStr.toIntOrNull()
                            val updated = task.copy(
                                title = title.trim(),
                                plannedMinutes = duration,
                                youtubeUrl = youtubeUrl.ifBlank { null },
                                targetCount = if (tCount != null && tCount > 0) tCount else null,
                                warningText = parentNote.ifBlank { null },
                                warning = parentNote.isNotBlank() && task.warning,
                                date = selectedDay
                            )
                            onSaveTask(updated)
                            onDismissRequest()
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenSkyCyan,
                            contentColor = Color(0xFF080D1A)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kaydet & Eşitle", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
