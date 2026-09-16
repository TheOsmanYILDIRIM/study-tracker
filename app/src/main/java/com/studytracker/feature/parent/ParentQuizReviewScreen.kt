package com.studytracker.feature.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.LocalQuizRepositoryImpl
import com.studytracker.core.domain.model.Quiz
import com.studytracker.core.domain.model.QuizQuestion
import com.studytracker.core.ui.components.LatexMathView
import com.studytracker.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val ZenCardShape = RoundedCornerShape(18.dp)
private val ZenPillShape = CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentQuizReviewScreen(
    quizId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val quizRepo = remember { LocalQuizRepositoryImpl(db) }

    val quizState by quizRepo.observeQuizById(quizId).collectAsState(initial = null)
    val quiz = quizState

    if (quiz == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B14)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ZenSkyCyan)
        }
        return
    }

    val questions = quiz.questions
    val total = questions.size.coerceAtLeast(1)
    val correct = quiz.correctCount
    val wrong = quiz.wrongCount
    val empty = quiz.emptyCount
    val successRate = (correct * 100) / total

    val timeFormat = remember { SimpleDateFormat("d MMMM HH:mm", Locale("tr", "TR")) }
    val submitTimeStr = quiz.submittedAt?.let { timeFormat.format(Date(it)) } ?: "Henüz Tamamlanmadı"

    Scaffold(
        containerColor = Color(0xFF070B14),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1424)),
                title = {
                    Column {
                        Text(
                            text = quiz.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ZomoTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Sınav & Test İnceleme Raporu",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZenSkyCyan,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = ZomoTextPrimary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Özet Başarı & Skor Kartı
            item(key = "quiz_score_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2E)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, ZenSkyCyan.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📊 Öğrenci Sonuç Özeti",
                                    fontWeight = FontWeight.Bold,
                                    color = ZenSkyCyan,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$total Soruda $correct Doğru",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(ZenPillShape)
                                    .background(if (successRate >= 70) ZenForestContainer else ZenRoseContainer)
                                    .border(1.dp, if (successRate >= 70) ZenForestGreen.copy(alpha = 0.6f) else ZenRoseCoral.copy(alpha = 0.6f), ZenPillShape)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "%$successRate Başarı",
                                    color = if (successRate >= 70) ZenForestGreen else ZenRoseCoral,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Metrik Rozetleri: Doğru, Yanlış, Boş, Süre
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Doğru
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0C241B))
                                    .border(1.dp, ZenForestGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✅ $correct", fontWeight = FontWeight.Bold, color = ZenForestGreen, fontSize = 14.sp)
                                    Text("Doğru", color = ZomoTextSecondary, fontSize = 10.sp)
                                }
                            }

                            // Yanlış
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2E1218))
                                    .border(1.dp, ZenRoseCoral.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("❌ $wrong", fontWeight = FontWeight.Bold, color = ZenRoseCoral, fontSize = 14.sp)
                                    Text("Yanlış", color = ZomoTextSecondary, fontSize = 10.sp)
                                }
                            }

                            // Boş
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF151D2C))
                                    .border(1.dp, Color(0xFF2A3952), RoundedCornerShape(10.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚪ $empty", fontWeight = FontWeight.Bold, color = ZomoTextSecondary, fontSize = 14.sp)
                                    Text("Boş", color = ZomoTextMuted, fontSize = 10.sp)
                                }
                            }

                            // Süre
                            val mins = quiz.studentDurationSeconds / 60
                            val secs = quiz.studentDurationSeconds % 60
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF102336))
                                    .border(1.dp, ZenSkyCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        String.format(Locale.US, "%02d:%02d", mins, secs),
                                        fontWeight = FontWeight.Bold,
                                        color = ZenSkyCyan,
                                        fontSize = 13.5.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text("Süre", color = ZomoTextSecondary, fontSize = 10.sp)
                                }
                            }
                        }

                        if (!quiz.studentNote.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x6014203D),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Psychology, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Öğrenci Öz Değerlendirmesi",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = ZenSkyCyan
                                        )
                                    }
                                    Text(
                                        text = quiz.studentNote,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text(
                            text = "📅 Tamamlanma: $submitTimeStr",
                            color = ZomoTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }


            item {
                Text(
                    text = "📝 Soru Bazlı Analiz ve Çözümler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ZomoTextPrimary,
                    fontSize = 14.sp
                )
            }

            // Soru Soru Detaylı Kartlar
            itemsIndexed(questions, key = { _, q -> q.questionId }) { idx, q ->
                val studentAns = quiz.studentAnswers[q.questionId]?.trim()
                val correctAns = q.correctOption.trim()
                val isCorrect = !studentAns.isNullOrBlank() && studentAns.equals(correctAns, ignoreCase = true)
                val isWrong = !studentAns.isNullOrBlank() && !studentAns.equals(correctAns, ignoreCase = true)
                val isEmpty = studentAns.isNullOrBlank()

                val cardBorderColor = when {
                    isCorrect -> ZenForestGreen.copy(alpha = 0.6f)
                    isWrong -> ZenRoseCoral.copy(alpha = 0.6f)
                    else -> Color(0xFF223552)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A2B)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Soru Başlığı ve Sonuç Rozeti
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Soru ${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                color = ZenSkyCyan,
                                fontSize = 13.5.sp
                            )

                            // Rozet
                            when {
                                isCorrect -> {
                                    Surface(
                                        shape = ZenPillShape,
                                        color = ZenForestContainer,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "✅ Doğru Yapıldı (Seçilen: $studentAns)",
                                            color = ZenForestGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                isWrong -> {
                                    Surface(
                                        shape = ZenPillShape,
                                        color = ZenRoseContainer,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenRoseCoral.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "❌ Yanlış (Seçilen: $studentAns • Doğru: $correctAns)",
                                            color = ZenRoseCoral,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = ZenPillShape,
                                        color = Color(0xFF1B263B),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334666))
                                    ) {
                                        Text(
                                            text = "⚪ Boş Bırakıldı (Doğru: $correctAns)",
                                            color = ZomoTextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Soru Metni (LaTeX Destekli)
                        LatexMathView(
                            text = q.text,
                            fontSize = 14.5.sp,
                            textColor = ZomoTextPrimary,
                            accentColor = Color(0xFF00E5FF)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Şıklar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (opt in q.options) {
                                val isOptionCorrect = opt.key.equals(correctAns, ignoreCase = true)
                                val isOptionSelectedByStudent = opt.key.equals(studentAns, ignoreCase = true)

                                val optBgColor = when {
                                    isOptionCorrect -> Color(0xFF0D2B20)
                                    isOptionSelectedByStudent && !isOptionCorrect -> Color(0xFF33151B)
                                    else -> Color(0xFF0B1320)
                                }
                                val optBorderColor = when {
                                    isOptionCorrect -> ZenForestGreen.copy(alpha = 0.8f)
                                    isOptionSelectedByStudent && !isOptionCorrect -> ZenRoseCoral.copy(alpha = 0.8f)
                                    else -> Color(0xFF1B283D)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(optBgColor)
                                        .border(1.dp, optBorderColor, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isOptionCorrect -> ZenForestGreen
                                                        isOptionSelectedByStudent -> ZenRoseCoral
                                                        else -> Color(0xFF1A263B)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = opt.key,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOptionCorrect || isOptionSelectedByStudent) Color.White else ZomoTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Box(modifier = Modifier.weight(1f)) {
                                            LatexMathView(
                                                text = opt.text,
                                                fontSize = 13.sp,
                                                textColor = if (isOptionCorrect) Color.White else ZomoTextPrimary,
                                                accentColor = if (isOptionCorrect) ZenForestGreen else Color(0xFF00E5FF)
                                            )
                                        }

                                        if (isOptionCorrect) {
                                            Text("✓ Doğru", color = ZenForestGreen, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                        } else if (isOptionSelectedByStudent) {
                                            Text("✗ Öğrenci", color = ZenRoseCoral, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Çözüm Açıklaması
                        if (!q.solutionExplanation.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0A2234))
                                    .border(1.dp, ZenSkyCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(15.dp))
                                        Text("Soru Çözümü & Açıklama", fontWeight = FontWeight.Bold, color = ZenSkyCyan, fontSize = 11.5.sp)
                                    }
                                    LatexMathView(
                                        text = q.solutionExplanation,
                                        fontSize = 12.5.sp,
                                        textColor = ZomoTextPrimary,
                                        accentColor = ZenSkyCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
