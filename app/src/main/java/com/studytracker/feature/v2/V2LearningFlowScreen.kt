package com.studytracker.feature.v2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.LocalV2AttemptRepositoryImpl
import com.studytracker.core.data.local.repository.LocalV2CurriculumRepositoryImpl
import com.studytracker.core.domain.engine.*
import com.studytracker.core.domain.model.*
import com.studytracker.core.ui.components.ZenParallaxBackground
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun V2LearningFlowScreen(
    lessonId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { AppPreferences.getInstance(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val familyCode by prefs.familyPairCode.collectAsState()

    val curriculumRepo = remember(db) { LocalV2CurriculumRepositoryImpl(db) }
    val attemptRepo = remember(db) { LocalV2AttemptRepositoryImpl(db) }

    val items by curriculumRepo.getLearningItemsForLesson(lessonId).collectAsState(initial = emptyList())
    val allPrereqs by curriculumRepo.getPrerequisites().collectAsState(initial = emptyList())
    val attempts by attemptRepo.getAttempts(familyCode, "student_default").collectAsState(initial = emptyList())

    // Active item action dialog state
    var selectedVideoItem by remember { mutableStateOf<LearningItem?>(null) }
    var selectedQuizItem by remember { mutableStateOf<LearningItem?>(null) }
    var selectedAnkiItem by remember { mutableStateOf<LearningItem?>(null) }
    var lockedPrereqNotice by remember { mutableStateOf<V2ItemProgress?>(null) }

    val lessonProgress = remember(items, allPrereqs, attempts) {
        val dummyLesson = Lesson(id = lessonId, courseId = "", familyCode = familyCode, title = "Öğrenme Akışı")
        V2ProgressEngine.evaluateLessonProgress(
            lesson = dummyLesson,
            items = items,
            prerequisites = allPrereqs,
            attempts = attempts
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ZenParallaxBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Öğrenme Akışı",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = ZomoTextPrimary
                            )
                            Text(
                                text = "${lessonProgress.completedItems}/${lessonProgress.totalItems} Tamamlandı (%${lessonProgress.completionPercentage})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (lessonProgress.completionPercentage == 100) ZenForestGreen else ZenMoonGold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri",
                                tint = ZomoTextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            if (lessonProgress.itemsProgress.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu ünitede henüz öğrenme adımı bulunmuyor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZomoTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lessonProgress.itemsProgress, key = { it.item.id }) { itemProgress ->
                        val item = itemProgress.item
                        val isNextItem = item.id == lessonProgress.nextUnfinishedItem?.id

                        LearningItemPuzzleCard(
                            itemProgress = itemProgress,
                            isNextItem = isNextItem,
                            onClick = {
                                when (itemProgress.state) {
                                    V2ItemState.LOCKED_BY_PREREQUISITE -> {
                                        lockedPrereqNotice = itemProgress
                                    }
                                    V2ItemState.ARCHIVED -> {
                                        Toast.makeText(context, "Bu adım arşivlendi.", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        when (item.itemType) {
                                            ItemType.VIDEO -> selectedVideoItem = item
                                            ItemType.QUIZ -> selectedQuizItem = item
                                            ItemType.ANKI -> selectedAnkiItem = item
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- Dialogs ---

        // 1. Locked Prerequisite Notice Dialog
        lockedPrereqNotice?.let { prog ->
            AlertDialog(
                onDismissRequest = { lockedPrereqNotice = null },
                shape = RoundedCornerShape(18.dp),
                containerColor = ZenNightSurface,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = ZenRoseCoral)
                        Text(
                            text = "Ön Koşul Gerekli",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZomoTextPrimary
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${prog.item.displayLabel} adımını açmak için aşağıdaki gereksinimleri tamamlamalısın:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZomoTextSecondary
                        )
                        prog.prerequisites.forEach { req ->
                            val reqItem = items.find { it.id == req.requiredItemId }
                            val label = reqItem?.displayLabel ?: req.requiredItemId
                            val minScoreText = if (req.minScore != null) " (en az %${req.minScore.toInt()} puan)" else ""
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (req.isSatisfied) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (req.isSatisfied) ZenForestGreen else ZenRoseCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$label$minScoreText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ZomoTextPrimary
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { lockedPrereqNotice = null }) {
                        Text("Anladım", color = ZenMoonGold, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 2. Video Action Dialog
        selectedVideoItem?.let { item ->
            val curVer = item.currentVersion
            val payload = remember(curVer) { V2ProgressEngine.parseVideoPayload(curVer?.payloadJson) }
            val videoUrl = curVer?.contentUrl ?: payload.url

            VideoItemActionDialog(
                item = item,
                videoUrl = videoUrl,
                onDismiss = { selectedVideoItem = null },
                onComplete = { durationSeconds ->
                    scope.launch {
                        val attempt = Attempt(
                            id = "att_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                            clientAttemptId = "cli_vid_${UUID.randomUUID()}",
                            familyCode = familyCode,
                            studentId = "student_default",
                            itemId = item.id,
                            versionId = item.currentVersionId,
                            status = AttemptStatus.COMPLETED,
                            durationSeconds = durationSeconds,
                            startedAt = System.currentTimeMillis() - (durationSeconds * 1000L),
                            completedAt = System.currentTimeMillis(),
                            metadataJson = buildJsonObject {
                                put("syncStatus", "PENDING")
                                put("selfCompleted", true)
                                put("itemType", "VIDEO")
                                put("measurementKind", "VIDEO")
                            }.toString()
                        )
                        attemptRepo.recordAttempt(attempt)
                        selectedVideoItem = null
                        Toast.makeText(context, "🌟 ${item.displayLabel} tamamlandı!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 3. Quiz Action Dialog
        selectedQuizItem?.let { item ->
            val curVer = item.currentVersion
            val quizPayload = remember(curVer) { V2ProgressEngine.parseQuizPayload(curVer?.payloadJson) }

            QuizItemActionDialog(
                item = item,
                payload = quizPayload,
                onDismiss = { selectedQuizItem = null },
                onSubmit = { score, durationSeconds, answerMetrics ->
                    scope.launch {
                        val attemptId = "att_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                        val attempt = Attempt(
                            id = attemptId,
                            clientAttemptId = "cli_quiz_${UUID.randomUUID()}",
                            familyCode = familyCode,
                            studentId = "student_default",
                            itemId = item.id,
                            versionId = item.currentVersionId,
                            status = AttemptStatus.COMPLETED,
                            score = score,
                            durationSeconds = durationSeconds,
                            startedAt = System.currentTimeMillis() - (durationSeconds * 1000L),
                            completedAt = System.currentTimeMillis(),
                            metadataJson = buildJsonObject {
                                put("syncStatus", "PENDING")
                                put("itemType", "QUIZ")
                                put("measurementKind", "QUIZ")
                                put("score", score)
                            }.toString()
                        )
                        attemptRepo.recordAttempt(attempt, answerMetrics)
                        selectedQuizItem = null
                        Toast.makeText(context, "📝 Quiz tamamlandı! Puan: %${score.toInt()}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // 4. Anki Action Dialog
        selectedAnkiItem?.let { item ->
            val curVer = item.currentVersion
            val ankiPayload = remember(curVer) { V2ProgressEngine.parseAnkiPayload(curVer?.payloadJson) }

            AnkiItemActionDialog(
                item = item,
                payload = ankiPayload,
                onDismiss = { selectedAnkiItem = null },
                onComplete = { reviewedCards, durationSeconds ->
                    scope.launch {
                        val attempt = Attempt(
                            id = "att_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                            clientAttemptId = "cli_anki_${UUID.randomUUID()}",
                            familyCode = familyCode,
                            studentId = "student_default",
                            itemId = item.id,
                            versionId = item.currentVersionId,
                            status = AttemptStatus.COMPLETED,
                            durationSeconds = durationSeconds,
                            startedAt = System.currentTimeMillis() - (durationSeconds * 1000L),
                            completedAt = System.currentTimeMillis(),
                            metadataJson = buildJsonObject {
                                put("syncStatus", "PENDING")
                                put("itemType", "ANKI")
                                put("measurementKind", "ANKI")
                                put("reviewedCount", reviewedCards)
                                put("reviewedCardCount", reviewedCards)
                            }.toString()
                        )
                        attemptRepo.recordAttempt(attempt)
                        selectedAnkiItem = null
                        Toast.makeText(context, "🗂️ ${item.displayLabel} tamamlandı! ($reviewedCards kart)", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun LearningItemPuzzleCard(
    itemProgress: V2ItemProgress,
    isNextItem: Boolean,
    onClick: () -> Unit
) {
    val item = itemProgress.item
    val (typeIcon, typeColor, typeName) = when (item.itemType) {
        ItemType.VIDEO -> Triple(Icons.Default.PlayCircle, ZenSkyCyan, "Video")
        ItemType.QUIZ -> Triple(Icons.Default.Quiz, ZenMoonGold, "Quiz")
        ItemType.ANKI -> Triple(Icons.Default.Layers, ZenLavender, "Anki Kartları")
    }

    val (cardBorderColor, cardAlpha) = when (itemProgress.state) {
        V2ItemState.COMPLETED -> Pair(ZenForestGreen.copy(alpha = 0.5f), 0.95f)
        V2ItemState.AVAILABLE -> if (isNextItem) Pair(ZenMoonGold, 0.95f) else Pair(ZenNightBorder, 0.85f)
        V2ItemState.LOCKED_BY_PREREQUISITE -> Pair(ZenNightBorder.copy(alpha = 0.3f), 0.6f)
        V2ItemState.ARCHIVED -> Pair(ZenNightBorder.copy(alpha = 0.2f), 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenNightSurface.copy(alpha = cardAlpha)),
        border = BorderStroke(if (isNextItem) 1.5.dp else 1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type Icon / Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(typeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (itemProgress.state) {
                        V2ItemState.COMPLETED -> Icons.Default.CheckCircle
                        V2ItemState.LOCKED_BY_PREREQUISITE -> Icons.Default.Lock
                        else -> typeIcon
                    },
                    contentDescription = null,
                    tint = when (itemProgress.state) {
                        V2ItemState.COMPLETED -> ZenForestGreen
                        V2ItemState.LOCKED_BY_PREREQUISITE -> Color.Gray
                        else -> typeColor
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title & Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.displayLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZomoTextPrimary
                    )

                    // Type tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = typeColor
                        )
                    }

                    if (isNextItem && itemProgress.state == V2ItemState.AVAILABLE) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ZenMoonGold.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Sıradaki Adım",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = ZenMoonGold
                            )
                        }
                    }
                }

                Text(
                    text = item.currentVersion?.title ?: item.displayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = ZomoTextSecondary,
                    maxLines = 1
                )

                if (itemProgress.state == V2ItemState.COMPLETED) {
                    val scoreText = if (itemProgress.bestScore != null) " • Puan: %${itemProgress.bestScore.toInt()}" else ""
                    Text(
                        text = "Tamamlandı$scoreText",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ZenForestGreen
                    )
                } else if (itemProgress.state == V2ItemState.LOCKED_BY_PREREQUISITE) {
                    Text(
                        text = "🔒 Ön koşul kilitli (Detay için dokun)",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZenRoseCoral
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ZomoTextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

// --- VIDEO ACTION DIALOG ---
@Composable
private fun VideoItemActionDialog(
    item: LearningItem,
    videoUrl: String,
    onDismiss: () -> Unit,
    onComplete: (durationSeconds: Int) -> Unit
) {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ZenNightSurface),
            border = BorderStroke(1.dp, ZenNightBorder)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ZenSkyCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ZenSkyCyan)
                    }
                    Column {
                        Text(
                            text = item.displayLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZomoTextPrimary
                        )
                        Text(
                            text = item.currentVersion?.title ?: "Ders Videosu",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZomoTextSecondary
                        )
                    }
                }

                if (videoUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Video bağlantısı açılamadı: $videoUrl", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Videoyu Aç (YouTube / Tarayıcı)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Videoyu izledikten sonra 'Tamamladım' butonuna dokunarak ilerlemeni kaydedebilirsin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZomoTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kapat", color = ZomoTextSecondary)
                    }

                    Button(
                        onClick = {
                            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000L).toInt().coerceAtLeast(30)
                            onComplete(elapsedSec)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tamamladım", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- QUIZ ACTION DIALOG ---
@Composable
private fun QuizItemActionDialog(
    item: LearningItem,
    payload: QuizPayload,
    onDismiss: () -> Unit,
    onSubmit: (score: Double, durationSeconds: Int, answerMetrics: List<QuizAnswerMetric>) -> Unit
) {
    val questions = remember(payload) {
        if (payload.questions.isNotEmpty()) payload.questions
        else listOf(
            QuizQuestionPayload(
                id = "q1",
                prompt = "${item.displayLabel} kapsamındaki temel kavramları anladınız mı?",
                type = "TRUE_FALSE",
                choices = listOf("Evet, anladım", "Hayır, tekrar etmeliyim"),
                correctAnswer = "Evet, anladım"
            )
        )
    }

    var selectedAnswers by remember { mutableStateOf(mapOf<Int, String>()) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ZenNightSurface),
            border = BorderStroke(1.dp, ZenNightBorder),
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ZenMoonGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = ZenMoonGold)
                    }
                    Column {
                        Text(
                            text = item.displayLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZomoTextPrimary
                        )
                        Text(
                            text = "${questions.size} Soru",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZomoTextSecondary
                        )
                    }
                }

                HorizontalDivider(color = ZenNightBorder.copy(alpha = 0.5f))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(questions.indices.toList()) { index ->
                        val q = questions[index]
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${index + 1}. ${q.prompt}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ZomoTextPrimary
                            )

                            val choices = if (q.choices.isNotEmpty()) q.choices else listOf("A) Doğru", "B) Yanlış")
                            choices.forEach { choice ->
                                val isSelected = selectedAnswers[index] == choice
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ZenMoonGold.copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable {
                                            selectedAnswers = selectedAnswers + (index to choice)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedAnswers = selectedAnswers + (index to choice) },
                                        colors = RadioButtonDefaults.colors(selectedColor = ZenMoonGold)
                                    )
                                    Text(
                                        text = choice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) ZomoTextPrimary else ZomoTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal", color = ZomoTextSecondary)
                    }

                    Button(
                        onClick = {
                            val durationSec = ((System.currentTimeMillis() - startTime) / 1000L).toInt().coerceAtLeast(10)
                            var correctCount = 0
                            val metrics = questions.mapIndexed { idx, q ->
                                val sel = selectedAnswers[idx]
                                val isCorr = sel != null && (sel == q.correctAnswer || (q.correctAnswer.isBlank() && sel.isNotBlank()))
                                if (isCorr) correctCount++
                                QuizAnswerMetric(
                                    id = "ans_${UUID.randomUUID()}",
                                    attemptId = "",
                                    questionId = q.id.ifBlank { "q_$idx" },
                                    questionIndex = idx,
                                    selectedOption = sel,
                                    isCorrect = isCorr,
                                    durationSeconds = durationSec / questions.size
                                )
                            }
                            val score = if (questions.isNotEmpty()) (correctCount.toDouble() / questions.size.toDouble()) * 100.0 else 100.0
                            onSubmit(score, durationSec, metrics)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenMoonGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Sınavı Gönder", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- ANKI ACTION DIALOG ---
@Composable
private fun AnkiItemActionDialog(
    item: LearningItem,
    payload: AnkiPayload,
    onDismiss: () -> Unit,
    onComplete: (reviewedCards: Int, durationSeconds: Int) -> Unit
) {
    val context = LocalContext.current
    var reviewedCountText by remember { mutableStateOf("10") }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ZenNightSurface),
            border = BorderStroke(1.dp, ZenNightBorder)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ZenLavender.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = ZenLavender)
                    }
                    Column {
                        Text(
                            text = item.displayLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZomoTextPrimary
                        )
                        Text(
                            text = payload.deckName.ifBlank { item.currentVersion?.title ?: "Anki Deste Tekrarı" },
                            style = MaterialTheme.typography.bodySmall,
                            color = ZomoTextSecondary
                        )
                    }
                }

                if (payload.instructions != null && payload.instructions.isNotBlank()) {
                    Text(
                        text = payload.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZomoTextSecondary
                    )
                }

                Button(
                    onClick = {
                        val launched = try {
                            val pkg = payload.packageUri ?: "com.ichi2.anki"
                            val pm = context.packageManager
                            val intent = pm.getLaunchIntentForPackage(pkg)
                            if (intent != null) {
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                                true
                            } else false
                        } catch (_: Exception) {
                            false
                        }

                        if (!launched && !payload.webUrl.isNullOrBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payload.webUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Anki veya web bağlantısı açılamadı.", Toast.LENGTH_SHORT).show()
                            }
                        } else if (!launched) {
                            Toast.makeText(context, "AnkiDroid cihazınızda bulunamadı.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenLavender),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anki'yi Aç", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = reviewedCountText,
                    onValueChange = { reviewedCountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tekrar Edilen Kart Sayısı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kapat", color = ZomoTextSecondary)
                    }

                    Button(
                        onClick = {
                            val count = reviewedCountText.toIntOrNull() ?: 10
                            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000L).toInt().coerceAtLeast(30)
                            onComplete(count, elapsedSec)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tamamladım", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
