package com.studytracker.feature.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.LocalV2AttemptRepositoryImpl
import com.studytracker.core.data.local.repository.LocalV2CurriculumRepositoryImpl
import com.studytracker.core.domain.engine.V2ProgressEngine
import com.studytracker.core.ui.components.ZenParallaxBackground
import com.studytracker.core.ui.components.CourseCoverVisual
import com.studytracker.core.ui.components.courseVisualColor
import com.studytracker.core.ui.components.fetchCourseVisualCatalog
import com.studytracker.core.ui.components.parseCourseVisualCatalog
import com.studytracker.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun V2LessonsScreen(
    courseId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLearningFlow: (lessonId: String, itemId: String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences.getInstance(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val familyCode by prefs.familyPairCode.collectAsState()

    val curriculumRepo = remember(db) { LocalV2CurriculumRepositoryImpl(db) }
    val attemptRepo = remember(db) { LocalV2AttemptRepositoryImpl(db) }

    val courses by curriculumRepo.getCourses(familyCode).collectAsState(initial = emptyList())
    val currentCourse = courses.firstOrNull { it.id == courseId }

    var courseVisualCatalog by remember {
        mutableStateOf(parseCourseVisualCatalog(prefs.courseVisualCatalogCacheJson))
    }

    LaunchedEffect(Unit) {
        fetchCourseVisualCatalog().onSuccess { raw ->
            prefs.courseVisualCatalogCacheJson = raw
            courseVisualCatalog = parseCourseVisualCatalog(raw)
        }
    }

    val effectiveCourseVisual = currentCourse?.let { courseVisualCatalog[it.id] ?: it.visual }
    val coursePrimary = courseVisualColor(effectiveCourseVisual?.primaryColor, ZenSkyCyan)
    val courseAccent = courseVisualColor(effectiveCourseVisual?.accentColor, ZenMoonGold)
    val courseSurface = courseVisualColor(effectiveCourseVisual?.surfaceColor, ZenNightSurface)
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

    Box(modifier = Modifier.fillMaxSize()) {
        ZenParallaxBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentCourse?.let { course ->
                                CourseCoverVisual(
                                    visual = effectiveCourseVisual ?: course.visual,
                                    contentDescription = "${course.subject} kapağı",
                                    modifier = Modifier.size(width = 34.dp, height = 46.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = currentCourse?.title ?: "Ders",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ZomoTextPrimary
                                )
                                Text(
                                    text = "Tüm çalışmalar tek sayfada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = coursePrimary
                                )
                            }
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                        text = "Bu derste henüz çalışma bulunmuyor.",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    lessons.forEach { lesson ->
                        val lessonItems = allItems
                            .filter { it.lessonId == lesson.id }
                            .sortedBy { it.orderKey }

                        val lessonProgress = V2ProgressEngine.evaluateLessonProgress(
                            lesson = lesson,
                            items = lessonItems,
                            prerequisites = allPrereqs,
                            attempts = attempts
                        )

                        item(key = "header_${lesson.id}") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lesson.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (lesson.id == courseProgress?.resumeLesson?.id) coursePrimary else ZomoTextPrimary
                                    )

                                    if (lessonProgress.completionPercentage == 100) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Bitti",
                                            tint = ZenForestGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (lesson.id == courseProgress?.resumeLesson?.id) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = coursePrimary.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = "BURADASIN",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = coursePrimary
                                            )
                                        }
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = {
                                        if (lessonProgress.totalItems == 0) 0f
                                        else lessonProgress.completedItems.toFloat() / lessonProgress.totalItems.toFloat()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(99.dp)),
                                    color = if (lessonProgress.completionPercentage == 100) ZenForestGreen else coursePrimary,
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )
                            }
                        }

                        lessonProgress.itemsProgress.forEach { itemProgress ->
                            item(key = itemProgress.item.id) {
                                LearningItemPuzzleCard(
                                    itemProgress = itemProgress,
                                    isNextItem = itemProgress.item.id == lessonProgress.nextUnfinishedItem?.id,
                                    isParent = false,
                                    onEdit = {},
                                    onClick = {
                                        onNavigateToLearningFlow(
                                            lesson.id,
                                            itemProgress.item.id
                                        )
                                    }
                                )
                            }
                        }

                        item(key = "space_${lesson.id}") {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
