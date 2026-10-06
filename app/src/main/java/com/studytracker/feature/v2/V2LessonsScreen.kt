package com.studytracker.feature.v2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.LocalV2AttemptRepositoryImpl
import com.studytracker.core.data.local.repository.LocalV2CurriculumRepositoryImpl
import com.studytracker.core.domain.engine.V2ProgressEngine
import com.studytracker.core.domain.model.Lesson
import com.studytracker.core.ui.components.ZenParallaxBackground
import com.studytracker.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Course-first self-paced flow: resume is derived from attempts.
fun V2LessonsScreen(
    courseId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLearningFlow: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences.getInstance(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val familyCode by prefs.familyPairCode.collectAsState()

    val curriculumRepo = remember(db) { LocalV2CurriculumRepositoryImpl(db) }
    val attemptRepo = remember(db) { LocalV2AttemptRepositoryImpl(db) }

    val courses by curriculumRepo.getCourses(familyCode).collectAsState(initial = emptyList())
    val currentCourse = courses.firstOrNull { it.id == courseId }
    val lessons by curriculumRepo.getLessonsForCourse(courseId).collectAsState(initial = emptyList())
    val allItems by curriculumRepo.getLearningItemsForFamily(familyCode).collectAsState(initial = emptyList())
    val allPrereqs by curriculumRepo.getPrerequisites().collectAsState(initial = emptyList())
    val attempts by attemptRepo.getAttempts(familyCode, "student_default").collectAsState(initial = emptyList())

    val courseProgress = remember(currentCourse, lessons, allItems, allPrereqs, attempts) {
        currentCourse?.let { course ->
            V2ProgressEngine.evaluateCourseProgress(
                course = course,
                lessons = lessons,
                itemsByLessonId = allItems.groupBy { it.lessonId },
                prerequisites = allPrereqs,
                attempts = attempts
            )
        }
    }
    val listState = rememberLazyListState()
    val resumeLessonId = courseProgress?.resumeLesson?.id

    LaunchedEffect(resumeLessonId, lessons) {
        val index = lessons.indexOfFirst { it.id == resumeLessonId }
        if (index >= 0) listState.animateScrollToItem(index)
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
                                text = "Konular",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = ZomoTextPrimary
                            )
                            Text(
                                text = "Devam etmek istediğin konuyu seç",
                                style = MaterialTheme.typography.bodySmall,
                                color = ZomoTextSecondary
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
            if (lessons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu derste henüz ünite bulunmuyor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZomoTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(lessons, key = { it.id }) { lesson ->
                        val isResumeLesson = lesson.id == resumeLessonId
                        val items by curriculumRepo.getLearningItemsForLesson(lesson.id).collectAsState(initial = emptyList())
                        val lessonProgress = remember(items, allPrereqs, attempts) {
                            V2ProgressEngine.evaluateLessonProgress(
                                lesson = lesson,
                                items = items,
                                prerequisites = allPrereqs,
                                attempts = attempts
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onNavigateToLearningFlow(lesson.id) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isResumeLesson) ZenSkyCyan.copy(alpha = 0.10f) else ZenNightSurface.copy(alpha = 0.92f)
                            ),
                            border = BorderStroke(1.dp, if (isResumeLesson) ZenSkyCyan.copy(alpha = 0.65f) else ZenNightBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = lesson.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ZomoTextPrimary
                                            )
                                            if (isResumeLesson) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = ZenSkyCyan.copy(alpha = 0.18f)
                                                ) {
                                                    Text(
                                                        text = "BURADASIN",
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = ZenSkyCyan
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${lessonProgress.completedItems}/${lessonProgress.totalItems} adım bitti",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (lessonProgress.completionPercentage == 100) ZenForestGreen else ZenMoonGold
                                        )
                                    }

                                    // Percentage pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (lessonProgress.completionPercentage == 100) ZenForestGreen.copy(alpha = 0.2f) else ZenMoonGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "%${lessonProgress.completionPercentage}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (lessonProgress.completionPercentage == 100) ZenForestGreen else ZenMoonGold
                                        )
                                    }
                                }

                                // Linear Progress Indicator
                                val progressFraction = if (lessonProgress.totalItems > 0) lessonProgress.completedItems.toFloat() / lessonProgress.totalItems.toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = if (lessonProgress.completionPercentage == 100) ZenForestGreen else ZenMoonGold,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )

                                // Next unfinished item badge
                                if (lessonProgress.nextUnfinishedItem != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleOutline,
                                            contentDescription = null,
                                            tint = ZenSkyCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Devam et: ${lessonProgress.nextUnfinishedItem.currentVersion?.title ?: lessonProgress.nextUnfinishedItem.displayLabel}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ZomoTextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                } else if (lessonProgress.totalItems > 0 && lessonProgress.completionPercentage == 100) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ZenForestGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Bu konuyu bitirdin!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ZenForestGreen
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
}
