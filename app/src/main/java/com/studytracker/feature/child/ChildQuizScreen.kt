package com.studytracker.feature.child

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private val ZenCardShape = RoundedCornerShape(18.dp)
private val ZenPillShape = CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildQuizScreen(
    quizId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val quizRepo = remember { LocalQuizRepositoryImpl(db) }

    val quizState by quizRepo.observeQuizById(quizId).collectAsState(initial = null)
    val quiz = quizState

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val studentAnswers = remember { mutableStateMapOf<String, String>() }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }

    // Quiz yüklendiğinde var olan cevapları aktar
    LaunchedEffect(quiz) {
        if (quiz != null && studentAnswers.isEmpty()) {
            studentAnswers.putAll(quiz.studentAnswers)
            if (quiz.studentDurationSeconds > 0) {
                elapsedSeconds = quiz.studentDurationSeconds
            }
        }
    }

    // Sayaç (Eğer test henüz tamamlanmadıysa)
    LaunchedEffect(quiz?.completed) {
        if (quiz?.completed == false) {
            while (true) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

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
    if (questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070B14))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ZenRoseCoral, modifier = Modifier.size(48.dp))
                Text("Bu testte henüz soru bulunmuyor.", color = ZomoTextPrimary, fontWeight = FontWeight.Bold)
                Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = ZenPaperElevated)) {
                    Text("Geri Dön")
                }
            }
        }
        return
    }

    val currentQuestion = questions.getOrElse(currentQuestionIndex) { questions.first() }

    // Testi Bitirme Onay Diyalogu
    if (showSubmitConfirmDialog) {
        val answeredCount = studentAnswers.values.count { it.isNotBlank() }
        val emptyCount = questions.size - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Celebration, contentDescription = null, tint = ZenSkyCyan)
                    Text("Testi Tamamla & Gönder", fontWeight = FontWeight.Bold, color = ZomoTextPrimary, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Toplam ${questions.size} sorudan $answeredCount tanesini yanıtladın." +
                                (if (emptyCount > 0) " $emptyCount soru boş bırakıldı." else " Tüm sorular cevaplandı! 🌟"),
                        color = ZomoTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Cevapların veline iletilecek ve incelemesine sunulacaktır. Testi bitirmek istediğine emin misin?",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        scope.launch {
                            quizRepo.submitQuizAnswers(quizId, studentAnswers.toMap(), elapsedSeconds)
                            Toast.makeText(context, "Test cevapların veline iletildi! 🎉", Toast.LENGTH_LONG).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen),
                    shape = ZenPillShape
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Evet, Bitir & Gönder", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Sorulara Dön", color = ZomoTextSecondary)
                }
            },
            containerColor = Color(0xFF10192E)
        )
    }

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
                            text = "Soru ${currentQuestionIndex + 1} / ${questions.size}",
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
                },
                actions = {
                    // Zaman Sayacı Rozeti
                    val mins = elapsedSeconds / 60
                    val secs = elapsedSeconds % 60
                    val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

                    Box(
                        modifier = Modifier
                            .clip(ZenPillShape)
                            .background(Color(0xFF182238))
                            .border(1.dp, ZenSkyCyan.copy(alpha = 0.4f), ZenPillShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = ZenSkyCyan, modifier = Modifier.size(14.dp))
                            Text(
                                text = timeStr,
                                color = ZenSkyCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF0D1424),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2E4D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Önceki Soru Butonu
                    OutlinedButton(
                        onClick = {
                            if (currentQuestionIndex > 0) currentQuestionIndex--
                        },
                        enabled = currentQuestionIndex > 0,
                        shape = ZenPillShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Önceki", fontSize = 12.sp)
                    }

                    // Sonraki Soru Butonu
                    if (currentQuestionIndex < questions.size - 1) {
                        Button(
                            onClick = { currentQuestionIndex++ },
                            shape = ZenPillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan, contentColor = ZenMintText),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Sonraki", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Testi Bitir Butonu
                    Button(
                        onClick = { showSubmitConfirmDialog = true },
                        shape = ZenPillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen, contentColor = Color.White),
                        modifier = Modifier.weight(1.2f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (quiz.completed) "Yeniden Gönder" else "Testi Bitir", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Soru Numaraları Seçim Şeridi
            item(key = "question_pills") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(questions) { idx, q ->
                        val isSelected = idx == currentQuestionIndex
                        val isAnswered = studentAnswers[q.questionId]?.isNotBlank() == true

                        val bgColor = when {
                            isSelected -> ZenSkyCyan
                            isAnswered -> ZenForestGreen.copy(alpha = 0.35f)
                            else -> Color(0xFF152033)
                        }
                        val fgColor = when {
                            isSelected -> ZenMintText
                            isAnswered -> ZenForestGreen
                            else -> ZomoTextSecondary
                        }
                        val borderColor = when {
                            isSelected -> ZenSkyCyan
                            isAnswered -> ZenForestGreen.copy(alpha = 0.8f)
                            else -> Color(0xFF223454)
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable { currentQuestionIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                color = fgColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Soru Metni Kartı (LaTeX Destekli)
            item(key = "question_card_${currentQuestion.questionId}") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ZenCardShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2E)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, ZenSkyCyan.copy(alpha = 0.4f))
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
                            Surface(
                                shape = ZenPillShape,
                                color = ZenSkyCyanContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Soru ${currentQuestionIndex + 1}",
                                    color = ZenSkyCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }

                            val selected = studentAnswers[currentQuestion.questionId]
                            if (!selected.isNullOrBlank()) {
                                Surface(
                                    shape = ZenPillShape,
                                    color = ZenForestContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenForestGreen.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "Cevap: $selected",
                                        color = ZenForestGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // LaTeX Soru Metni
                        LatexMathView(
                            text = currentQuestion.text,
                            fontSize = 15.sp,
                            textColor = ZomoTextPrimary,
                            accentColor = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            // Şıklar Listesi
            itemsIndexed(currentQuestion.options) { _, opt ->
                val isSelected = studentAnswers[currentQuestion.questionId] == opt.key

                val bgAnimation by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF103347) else Color(0xFF0D1726),
                    label = "option_bg"
                )
                val borderAnimation by animateColorAsState(
                    targetValue = if (isSelected) ZenSkyCyan else Color(0xFF1F2F4A),
                    label = "option_border"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ZenCardShape)
                        .background(bgAnimation)
                        .border(1.2.dp, borderAnimation, ZenCardShape)
                        .clickable {
                            studentAnswers[currentQuestion.questionId] = opt.key
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Şık Harfi Yuvarlağı (A, B, C, D, E)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ZenSkyCyan else Color(0xFF18273D))
                                .border(1.dp, if (isSelected) ZenSkyCyan else Color(0xFF2E4366), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.key,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ZenMintText else Color.White,
                                fontSize = 13.5.sp
                            )
                        }

                        // Şık Metni (LaTeX Destekli)
                        Box(modifier = Modifier.weight(1f)) {
                            LatexMathView(
                                text = opt.text,
                                fontSize = 14.sp,
                                textColor = if (isSelected) Color.White else ZomoTextPrimary,
                                accentColor = if (isSelected) ZenSkyCyan else Color(0xFF00E5FF)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ZenSkyCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Seçimi Temizle Butonu
            if (studentAnswers[currentQuestion.questionId]?.isNotBlank() == true) {
                item {
                    TextButton(
                        onClick = { studentAnswers.remove(currentQuestion.questionId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = ZomoTextMuted, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bu Sorudaki Seçimi Temizle", color = ZomoTextMuted, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}
