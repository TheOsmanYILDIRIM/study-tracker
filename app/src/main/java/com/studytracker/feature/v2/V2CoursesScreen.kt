package com.studytracker.feature.v2

import android.widget.Toast
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.LocalV2AttemptRepositoryImpl
import com.studytracker.core.data.local.repository.LocalV2CurriculumRepositoryImpl
import com.studytracker.core.domain.engine.V2ProgressEngine
import com.studytracker.core.domain.model.ItemType
import com.studytracker.core.domain.model.LearningItem
import com.studytracker.core.ui.components.LearningItemVisual
import com.studytracker.core.ui.components.youtubeThumbnailUrl
import com.studytracker.core.ui.components.CloudSyncDialog
import com.studytracker.core.ui.components.ZenParallaxBackground
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

private data class StudentResumeTarget(
    val courseTitle: String,
    val lessonId: String,
    val lessonTitle: String,
    val item: LearningItem
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun V2CoursesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCourse: (String) -> Unit,
    onResumeLesson: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { AppPreferences.getInstance(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val familyCode by prefs.familyPairCode.collectAsState()
    val isParent = com.studytracker.BuildConfig.APP_ROLE == "PARENT"

    val curriculumRepo = remember(db) { LocalV2CurriculumRepositoryImpl(db) }
    val attemptRepo = remember(db) { LocalV2AttemptRepositoryImpl(db) }

    val courses by curriculumRepo.getCourses(familyCode).collectAsState(initial = emptyList())
    val attempts by attemptRepo.getAttempts(familyCode, "student_default").collectAsState(initial = emptyList())
    val allItems by curriculumRepo.getLearningItemsForFamily(familyCode).collectAsState(initial = emptyList())
    val allPrereqs by curriculumRepo.getPrerequisites().collectAsState(initial = emptyList())

    val resumeTarget by produceState<StudentResumeTarget?>(
        initialValue = null,
        key1 = courses,
        key2 = allItems,
        key3 = Pair(allPrereqs, attempts)
    ) {
        value = null
        for (course in courses) {
            val courseLessons = curriculumRepo.getLessonsForCourse(course.id).first()
            val progress = V2ProgressEngine.evaluateCourseProgress(
                course = course,
                lessons = courseLessons,
                itemsByLessonId = allItems.groupBy { it.lessonId },
                prerequisites = allPrereqs,
                attempts = attempts
            )
            val lesson = progress.resumeLesson
            val item = progress.resumeItem
            if (lesson != null && item != null) {
                value = StudentResumeTarget(
                    courseTitle = course.title,
                    lessonId = lesson.id,
                    lessonTitle = lesson.title,
                    item = item
                )
                break
            }
        }
    }

    var isSyncing by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    fun refreshCatalog(showToast: Boolean = true) {
        scope.launch {
            isSyncing = true
            val adminToken = prefs.familyAdminToken.value.ifBlank { null }
            val res = curriculumRepo.syncCatalog(familyCode, adminToken)
            isSyncing = false
            if (showToast) {
                if (res.isSuccess) {
                    Toast.makeText(context, "Dersler güncellendi.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Dersler yenilenemedi. İnternet bağlantını kontrol et.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(familyCode) {
        if (familyCode.isNotBlank()) {
            refreshCatalog(showToast = false)
        }
    }

    LaunchedEffect(allItems) {
        allItems
            .asSequence()
            .filter { it.itemType == ItemType.VIDEO }
            .mapNotNull { item ->
                val version = item.currentVersion
                val payload = V2ProgressEngine.parseVideoPayload(version?.payloadJson)
                youtubeThumbnailUrl(version?.contentUrl ?: payload.url)
            }
            .distinct()
            .forEach { thumbnailUrl ->
                Coil.imageLoader(context).enqueue(
                    ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .memoryCacheKey("study-video-thumb:$thumbnailUrl")
                        .diskCacheKey("study-video-thumb:$thumbnailUrl")
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                )
            }
    }

    if (showSettingsDialog) {
        CloudSyncDialog(
            isParent = com.studytracker.BuildConfig.APP_ROLE == "PARENT",
            onDismissRequest = { showSettingsDialog = false }
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
                                text = "Derslerim",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = ZomoTextPrimary
                            )
                            Text(
                                text = "Kaldığın yerden devam et",
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
                    actions = {
                        IconButton(
                            onClick = { showSettingsDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ayarlar ve eşleştirme",
                                tint = ZomoTextPrimary
                            )
                        }

                        if (isParent) {
                            IconButton(
                                onClick = { refreshCatalog(showToast = true) },
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = ZenMoonGold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Yenile",
                                        tint = ZenMoonGold
                                    )
                                }
                            }

                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ZenNightSurface.copy(alpha = 0.9f)),
                        border = BorderStroke(1.dp, ZenNightBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = ZenMoonGold,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Henüz ders yok",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZomoTextPrimary
                            )
                            Text(
                                text = "Derslerin hazırlanıyor. Biraz sonra tekrar deneyebilirsin.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZomoTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Button(
                                onClick = { refreshCatalog(showToast = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = ZenMoonGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dersleri Yenile", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    resumeTarget?.let { target ->
                        item(key = "global_resume") {
                            val version = target.item.currentVersion
                            val videoUrl = if (target.item.itemType == ItemType.VIDEO) {
                                version?.contentUrl ?: V2ProgressEngine.parseVideoPayload(version?.payloadJson).url
                            } else null

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onResumeLesson(target.lessonId) },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = ZenSkyCyan.copy(alpha = 0.12f)
                                ),
                                border = BorderStroke(1.5.dp, ZenSkyCyan.copy(alpha = 0.75f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Devam Et",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = ZomoTextPrimary
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        LearningItemVisual(
                                            itemType = target.item.itemType,
                                            videoUrl = videoUrl,
                                            modifier = Modifier.size(width = 112.dp, height = 68.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = target.courseTitle,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = ZenSkyCyan
                                            )
                                            Text(
                                                text = target.lessonTitle,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = ZomoTextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = version?.title ?: target.item.displayLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ZomoTextSecondary,
                                                maxLines = 2
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onResumeLesson(target.lessonId) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ZenSkyCyan)
                                    ) {
                                        Icon(
                                            imageVector = when (target.item.itemType) {
                                                ItemType.VIDEO -> Icons.Default.PlayArrow
                                                ItemType.QUIZ -> Icons.Default.Quiz
                                                ItemType.ANKI -> Icons.Default.Layers
                                            },
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (target.item.itemType) {
                                                ItemType.VIDEO -> "Videoya Devam Et"
                                                ItemType.QUIZ -> "Testi Çöz"
                                                ItemType.ANKI -> "Kartlara Başla"
                                            },
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "courses_header") {
                        Text(
                            text = "Dersler",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ZomoTextPrimary
                        )
                    }

                    items(courses, key = { it.id }) { course ->
                        val lessons by curriculumRepo.getLessonsForCourse(course.id).collectAsState(initial = emptyList())
                        val courseProgress = remember(course, lessons, allItems, allPrereqs, attempts) {
                            V2ProgressEngine.evaluateCourseProgress(
                                course = course,
                                lessons = lessons,
                                itemsByLessonId = allItems.groupBy { it.lessonId },
                                prerequisites = allPrereqs,
                                attempts = attempts
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onNavigateToCourse(course.id) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ZenNightSurface.copy(alpha = 0.92f)
                            ),
                            border = BorderStroke(1.dp, ZenNightBorder)
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(ZenMoonGold.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = ZenMoonGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = course.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ZomoTextPrimary
                                            )
                                            Text(
                                                text = "${course.gradeLevel}. Sınıf • ${course.subject}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ZomoTextSecondary
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = ZomoTextSecondary
                                    )
                                }

                                if (course.description.isNotBlank()) {
                                    Text(
                                        text = course.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ZomoTextSecondary,
                                        maxLines = 2
                                    )
                                }

                                HorizontalDivider(color = ZenNightBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                                if (isParent && courseProgress.resumeLesson != null && courseProgress.resumeItem != null) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ZenSkyCyan.copy(alpha = 0.10f),
                                        border = BorderStroke(1.dp, ZenSkyCyan.copy(alpha = 0.35f))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Kaldığın yer: ${courseProgress.resumeLesson.title}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = ZomoTextPrimary
                                            )
                                            Text(
                                                text = courseProgress.resumeItem.currentVersion?.title ?: courseProgress.resumeItem.displayLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ZomoTextSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${lessons.size} konu",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = ZenMoonGold
                                    )
                                    Text(
                                        text = if (courseProgress.resumeItem == null && courseProgress.totalItems > 0) "Bitti ✓" else "Konuları aç →",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ZomoTextPrimary
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
