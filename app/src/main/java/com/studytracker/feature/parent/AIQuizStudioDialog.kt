package com.studytracker.feature.parent

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.LocalQuizRepositoryImpl
import com.studytracker.core.data.plan_engine.AIPromptBuilder
import com.studytracker.core.data.plan_engine.SimpleQuizParser
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch

private val ZenCardShape = RoundedCornerShape(18.dp)
private val ZenPillShape = CircleShape

@Composable
fun AIQuizStudioDialog(
    onDismissRequest: () -> Unit,
    onQuizCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val quizRepo = remember { LocalQuizRepositoryImpl(db) }

    var topicInput by remember { mutableStateOf("Matematik - Trigonometri") }
    var questionCount by remember { mutableStateOf(5) }
    var targetGrade by remember { mutableStateOf("11. Sınıf / YKS") }
    var durationMinutes by remember { mutableStateOf(15) }
    var customNotes by remember { mutableStateOf("") }
    var pastedQuizText by remember { mutableStateOf("") }

    var isGeneratingPrompt by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(ZenCardShape),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1424)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, ZenSkyCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ZenPillShape)
                                .background(ZenSkyCyanContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                "🤖 AI Test & Soru Stüdyosu",
                                fontWeight = FontWeight.Bold,
                                color = ZomoTextPrimary,
                                fontSize = 14.5.sp
                            )
                            Text(
                                "LaTeX & Matematik Formül Destekli Şıklı Test",
                                color = ZenSkyCyan,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = ZomoTextSecondary)
                    }
                }

                Divider(color = Color(0xFF1E2D47))

                // 1. Form Parametreleri
                Text("1. Test Parametreleri", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)

                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text("Ders ve Konu Başlığı") },
                    placeholder = { Text("Örn: Matematik - Trigonometri Formülleri") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenSkyCyan,
                        unfocusedBorderColor = Color(0xFF1E2D47),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetGrade,
                        onValueChange = { targetGrade = it },
                        label = { Text("Hedef Seviye / Sınav") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenSkyCyan,
                            unfocusedBorderColor = Color(0xFF1E2D47),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = questionCount.toString(),
                        onValueChange = { questionCount = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 5 },
                        label = { Text("Soru Sayısı") },
                        modifier = Modifier.weight(0.6f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenSkyCyan,
                            unfocusedBorderColor = Color(0xFF1E2D47),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // 2. AI Prompter Butonu
                Button(
                    onClick = {
                        val prompt = AIPromptBuilder.buildQuizPrompt(
                            topic = topicInput,
                            questionCount = questionCount,
                            targetGrade = targetGrade,
                            durationMinutes = durationMinutes,
                            userCustomNotes = customNotes
                        )
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Test Prompt", prompt))
                        Toast.makeText(context, "🤖 AI Test İstemi Kopyalandı! ChatGPT/Claude'a yapıştırabilirsiniz.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = ZenPillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan, contentColor = ZenMintText)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Test İstemini (Prompt) Kopyala", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Örnek Test Yükle Hızlı Butonu
                TextButton(
                    onClick = {
                        pastedQuizText = """
=== TEST: $topicInput Testi ===
SURE: $durationMinutes
ACIKLAMA: $targetGrade seviyesinde kazanım değerlendirme testi

[SORU 1]
f(x) = \frac{\sin(2x)}{\cos(x)} ifadesinin en sade hali aşağıdakilerden hangisidir?
A) 2\sin(x)
B) 2\cos(x)
C) \tan(x)
D) \cot(x)
DOGRU: A
COZUM: \sin(2x) = 2\sin(x)\cos(x) özdeşliğinden payda ile pay sadeleşir ve 2\sin(x) kalır.

[SORU 2]
\int_0^2 (3x^2 - 2x + 1) dx integralinin sonucu kaçtır?
A) 4
B) 5
C) 6
D) 8
DOGRU: C
COZUM: [x^3 - x^2 + x]_0^2 = (8 - 4 + 2) - 0 = 6 bulunur.

[SORU 3]
\lim_{x \to 3} \frac{x^2 - 9}{x - 3} limitinin değeri kaçtır?
A) 3
B) 6
C) 9
D) 0
DOGRU: B
COZUM: \frac{(x-3)(x+3)}{x-3} = x+3 olur. x=3 için sonuç 6'dır.
=== TEST_SONU ===
                        """.trimIndent()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = ZenMoonGold, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Örnek LaTeX Test Metni Doldur (Hızlı Deneme)", color = ZenMoonGold, fontSize = 11.5.sp)
                }

                Divider(color = Color(0xFF1E2D47))

                // 3. AI Çıktısını Yapıştır & Kaydet
                Text("2. AI Test Metnini Buraya Yapıştırın", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)

                OutlinedTextField(
                    value = pastedQuizText,
                    onValueChange = { pastedQuizText = it },
                    placeholder = { Text("ChatGPT / Claude'dan gelen === TEST: ... === metnini buraya yapıştırın...", fontSize = 12.sp, color = ZomoTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ZenSkyCyan,
                        unfocusedBorderColor = Color(0xFF1E2D47),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Kaydet Butonu
                Button(
                    onClick = {
                        if (pastedQuizText.isBlank()) {
                            Toast.makeText(context, "Lütfen önce test metnini yapıştırın!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val parsed = SimpleQuizParser.parse(pastedQuizText)
                        if (parsed.isEmpty() || parsed.first().questions.isEmpty()) {
                            Toast.makeText(context, "Metinde geçerli soru bulunamadı. Lütfen şablon formatını kontrol edin.", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        scope.launch {
                            quizRepo.upsertQuizzes(parsed)
                            Toast.makeText(context, "🎉 ${parsed.size} adet test (${parsed.sumOf { it.questions.size }} soru) başarıyla kaydedildi!", Toast.LENGTH_LONG).show()
                            onQuizCreated()
                            onDismissRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = ZenPillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Testi Kaydet & Masaya Ekle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
