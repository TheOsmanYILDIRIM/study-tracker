package com.studytracker.feature.parent

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.LocalOccurrenceRepositoryImpl
import com.studytracker.core.data.local.repository.LocalPlanRepositoryImpl
import com.studytracker.core.data.plan_engine.AIPromptBuilder
import com.studytracker.core.data.plan_engine.SimplePlanParser
import com.studytracker.core.domain.repository.PlanImportResult
import com.studytracker.core.ui.components.WeekCalendarPicker
import com.studytracker.core.ui.theme.EmeraldContainer
import com.studytracker.core.ui.theme.EmeraldSuccess
import com.studytracker.core.ui.theme.SapphirePrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPlanStudioScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getInstance(context) }
    val planRepo = remember { LocalPlanRepositoryImpl(db) }
    val occurrenceRepo = remember { LocalOccurrenceRepositoryImpl(db) }

    val activePlan by remember(planRepo) { planRepo.getActivePlan() }.collectAsState(initial = null)
    val occurrences by remember(occurrenceRepo) { occurrenceRepo.getAllOccurrences() }.collectAsState(initial = emptyList())

    // Derive initial week info
    val initialWeekInfo = remember {
        val cal = Calendar.getInstance(Locale.US)
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.minimalDaysInFirstWeek = 4
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        val weekId = String.format(Locale.US, "%04d-W%02d", year, week)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        Pair(weekId, startDate)
    }

    var targetWeekId by remember { mutableStateOf(initialWeekInfo.first) }
    var targetWeekStartDate by remember { mutableStateOf(initialWeekInfo.second) }
    var childId by remember { mutableStateOf("child_1") }
    var userCustomRequest by remember { mutableStateOf("") }

    var planTextInput by remember { mutableStateOf("") }
    var importResult by remember { mutableStateOf<PlanImportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    LaunchedEffect(activePlan) {
        activePlan?.let {
            targetWeekId = it.weekId
            targetWeekStartDate = it.weekStartDate
            childId = it.childId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🤖 AI Plan Stüdyosu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Graphical Calendar Week Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SapphirePrimary)
                            Text("1. Takvimden Hedef Haftayı Seçin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Haftayı elle yazmak yerine aşağıdaki takvimden dokunarak kolayca belirleyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        WeekCalendarPicker(
                            selectedWeekId = targetWeekId,
                            selectedStartDate = targetWeekStartDate,
                            onWeekSelected = { wId, sDate ->
                                targetWeekId = wId
                                targetWeekStartDate = sDate
                            }
                        )

                        OutlinedTextField(
                            value = userCustomRequest,
                            onValueChange = { userCustomRequest = it },
                            label = { Text("Özel Plan İsteğiniz (Opsiyonel)") },
                            placeholder = { Text("Örn: Çarşamba ve Cuma günleri İngilizce ağırlıklı olsun...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Button(
                            onClick = {
                                val prompt = AIPromptBuilder.buildPrompt(
                                    currentPlan = activePlan,
                                    allOccurrences = occurrences,
                                    targetWeekId = targetWeekId,
                                    targetWeekStartDate = targetWeekStartDate,
                                    childId = childId,
                                    userCustomRequest = userCustomRequest
                                )
                                clipboardManager.setText(AnnotatedString(prompt))
                                Toast.makeText(context, "📋 AI Master Prompt panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📋 AI Master Prompt'unu Kopyala", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 2: Simple Plan / JSON Import Area
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldSuccess)
                            Text("2. Planı İçe Aktar (Basit Format / JSON)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "AI çıktısını veya aşağıdaki basit değişken formatındaki ders planını buraya yapıştırın.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Action Quick Buttons: Load Template & Export Current Plan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    planTextInput = """
HAFTA = $targetWeekId
BASLANGIC = $targetWeekStartDate
OGRENCI = $childId

[DERSLER]
mat = Matematik | 40 dk | Soru Çözümü & Konu Tekrarı
turkce = Türkçe | 30 dk | Paragraf ve Dil Bilgisi
fen = Fen Bilimleri | 35 dk | Deney ve Ünite Değerlendirme
sosyal = Sosyal Bilgiler | 25 dk | Harita & Tarih Özeti
ingilizce = İngilizce | 25 dk | Kelime ve Dinleme
kitap = Kitap Okuma | 20 dk | 25 Sayfa Kitap

[GUNLER]
Pazartesi = mat, turkce, kitap
Sali = fen, sosyal, kitap
Carsamba = mat, ingilizce, kitap
Persembe = turkce, fen, kitap
Cuma = mat, sosyal, ingilizce
Cumartesi = fen, mat, kitap
Pazar = kitap

[HAFTALIK]
deneme = Hafta Sonu Deneme Sınavı | 90 dk | LGS / Genel Değerlendirme Denemesi
                                    """.trimIndent()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Şablon Doldur", fontSize = 11.sp)
                            }

                            if (activePlan != null) {
                                OutlinedButton(
                                    onClick = {
                                        planTextInput = SimplePlanParser.exportToSimpleText(activePlan!!)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Output, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aktif Planı Çek", fontSize = 11.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = planTextInput,
                            onValueChange = { planTextInput = it },
                            placeholder = { Text("HAFTA = $targetWeekId\n[DERSLER]\nmat = Matematik | 40 dk\n[GUNLER]\nPazartesi = mat...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 6,
                            maxLines = 12,
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        Button(
                            onClick = {
                                if (planTextInput.isBlank()) {
                                    errorMessage = "Lütfen önce geçerli bir plan metni veya JSON yapıştırın."
                                    return@Button
                                }
                                isImporting = true
                                errorMessage = null
                                importResult = null
                                scope.launch {
                                    val res = planRepo.importPlanJson(planTextInput)
                                    isImporting = false
                                    res.onSuccess {
                                        importResult = it
                                        Toast.makeText(context, "✅ Plan başarıyla birleştirildi ve yüklendi!", Toast.LENGTH_LONG).show()
                                    }.onFailure {
                                        errorMessage = it.localizedMessage ?: "İçe aktarma hatası"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📥 Planı Doğrula ve İçe Aktar (Merge)", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Success Result Box
                        importResult?.let { res ->
                            Surface(
                                color = EmeraldContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎉 Plan Başarıyla İçe Aktarıldı!", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                    Text("• Mod: ${if (res.isSameWeekRevision) "Aynı Hafta Revizyonu" else "Yeni Hafta Planı"}", fontSize = 12.sp, color = Color(0xFF065F46))
                                    Text("• Korunan Tamamlanmış Görev: ${res.preservedCount} adet", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF065F46))
                                    Text("• Eklenen Yeni Görev: ${res.occurrencesCreated} adet", fontSize = 12.sp, color = Color(0xFF065F46))
                                    Text("• Güncellenen Görev: ${res.occurrencesUpdated} adet", fontSize = 12.sp, color = Color(0xFF065F46))
                                }
                            }
                        }

                        // Error Box
                        errorMessage?.let { err ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Text(text = err, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
