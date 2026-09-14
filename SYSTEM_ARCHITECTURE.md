# StudyTracker - Sistem Mimarisi ve Teknik Şartname

Bu doküman, **StudyTracker** Android uygulamasının temel mimarisini, veri modellerini, durum makinelerini, AI entegrasyon kurallarını ve test modunu tanımlar.

---

## 1. Temel Prensipler ve Kısıtlar

1. **Sıfır İhlal ve Gizlilik Odaklı:** `UsageStatsManager` veya agresif uygulama kilitleme mekanizmaları KESİNLİKLE kullanılmaz. Kanıt yalnızca oturum esnasında ve bitişinde alınan ekran görüntüleridir.
2. **AI Runtime Bağımsızlığı:** AI runtime'da karar vermez; AI yalnızca geliştirme aşamasında (kod yazımı) ve haftalık plan JSON'u üretiminde/güncellemesinde kullanılır.
3. **Tek Cihazda Çift Rol & Test Modu:** Tek bir APK içerisinde hem ebeveyn hem çocuk modu barındırılır. Test modu sayesinde Firebase veya gerçek `MediaProjection` izni olmadan sahte screenshot ve yerel veri tabanı ile uçtan uca simülasyon yapılabilir.
4. **Kayıpsız Plan Merge (Persistence of Completed States):** Plan güncellendiğinde, çocuk zaten aynı görevi yaptıysa ve görev hala yeni planda varsa, tamamlanmışlık (`approved`, `waiting_review`, `active`) asla kaybolmaz.

---

## 2. Modüler Mimari ve Paket Yapısı

```text
com.studytracker/
├── app/
│   ├── StudyTrackerApp.kt
│   └── navigation/
│       ├── NavGraph.kt
│       └── ScreenRoutes.kt
├── core/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── TaskTemplate.kt
│   │   │   ├── Occurrence.kt
│   │   │   ├── Session.kt
│   │   │   ├── Screenshot.kt
│   │   │   └── Review.kt
│   │   └── repository/
│   │       ├── PlanRepository.kt
│   │       ├── OccurrenceRepository.kt
│   │       ├── SessionRepository.kt
│   │       └── CaptureDriver.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── db/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   └── entity/
│   │   │   └── repository/
│   │   │       ├── LocalPlanRepository.kt
│   │   │       ├── LocalSessionRepository.kt
│   │   │       └── FakeCaptureDriver.kt
│   │   ├── remote/
│   │   │   ├── firestore/
│   │   │   └── repository/
│   │   │       ├── FirebasePlanRepository.kt
│   │   │       ├── FirebaseSessionRepository.kt
│   │   │       └── RealCaptureDriver.kt
│   │   └── plan_engine/
│   │       ├── PlanValidator.kt
│   │       ├── PlanMergeEngine.kt
│   │       └── AIPromptBuilder.kt
│   └── ui/
│       ├── theme/
│       ├── components/
│       └── overlay/
│           ├── FloatingButtonService.kt
│           └── FloatingButtonHUD.kt
└── feature/
    ├── parent/
    │   ├── dashboard/
    │   ├── plan_studio/
    │   ├── review/
    │   └── history/
    ├── child/
    │   ├── home/
    │   ├── active_session/
    │   └── warnings/
    └── test_mode/
        └── DeveloperConsoleScreen.kt
```

---

## 3. Domain Veri Modelleri

### 3.1. TaskTemplate (Görev Şablonu)
```kotlin
data class TaskTemplate(
    val taskId: String,               // slug: "math_video", "reading"
    val title: String,
    val kind: TaskKind,               // DAILY, WEEKLY
    val contentType: ContentType,     // VIDEO, READING, APP, EXAM, OTHER
    val youtubeUrl: String? = null,
    val plannedMinutes: Int,
    val targetMode: TargetMode? = null, // COUNT, MINUTES (weekly için)
    val targetCount: Int? = null,
    val targetMinutes: Int? = null,
    val reviewRequired: Boolean = true,
    val active: Boolean = true
)
```

### 3.2. Occurrence (Günün veya Haftanın Görev Örneği)
```kotlin
data class Occurrence(
    val occurrenceKey: String,        // "math_video:2026-06-16" veya "exam:2026-W25"
    val taskId: String,
    val type: TaskKind,               // DAILY, WEEKLY
    val date: String? = null,         // YYYY-MM-DD
    val weekId: String? = null,       // YYYY-Www
    val title: String,
    val plannedMinutes: Int,
    val youtubeUrl: String? = null,
    val reviewRequired: Boolean = true,
    val status: OccurrenceStatus = OccurrenceStatus.PENDING,
    val warning: Boolean = false,
    val warningText: String? = null,
    val rejectCount: Int = 0,
    val approvedCount: Int = 0,       // weekly için sayaç
    val targetCount: Int? = null,
    val targetMinutes: Int? = null
)

enum class OccurrenceStatus {
    PENDING,
    ACTIVE,
    WAITING_REVIEW,
    APPROVED,
    REJECTED,
    ARCHIVED
}
```

### 3.3. Session (Çalışma Oturumu)
```kotlin
data class Session(
    val sessionId: String,
    val occurrenceKey: String,
    val childId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val screenshotCount: Int = 0,
    val finalScreenshotUrl: String? = null
)

enum class SessionStatus {
    ACTIVE,
    WAITING_REVIEW,
    APPROVED,
    REJECTED,
    INVALID
}
```

### 3.4. Screenshot & Review
```kotlin
data class Screenshot(
    val screenshotId: String,
    val sessionId: String,
    val occurrenceKey: String,
    val capturedAt: Long,
    val url: String,                  // Dosya URI veya URL
    val sizeKb: Int,
    val uploadStatus: UploadStatus = UploadStatus.PENDING
)

data class Review(
    val sessionId: String,
    val occurrenceKey: String,
    val reviewStatus: ReviewStatus,   // APPROVED, REJECTED
    val reviewNote: String? = null,
    val reviewedAt: Long
)
```

---

## 4. Plan Merge ve Durum Koruma Algoritması (Revize vs Yeni Hafta)

> [!IMPORTANT]
> **Revize Plan vs Yeni Hafta Planı Kuralı:**
> - **Aynı Hafta Revizyonu (`newPlan.weekId == currentPlan.weekId`):** Tamamlanmış (`approved`), inceleme bekleyen (`waiting_review`) veya aktif (`active`) görevler korunur. Metadata güncellenir ancak tamamlanma sıfırlanmaz.
> - **Yeni Hafta Planı (`newPlan.weekId != currentPlan.weekId`):** Yeni haftaya ait occurrence'lar taze `PENDING` olarak başlar. Eski haftanın tamamlanmış kayıtları silinmez, geçmiş/arşiv veritabanında korunur.

```kotlin
fun mergePlan(
    currentPlan: Plan?,
    newPlan: Plan,
    localOccurrences: Map<String, Occurrence>
): MergeResult {
    val isSameWeekRevision = currentPlan != null && currentPlan.weekId == newPlan.weekId

    // 1. Task Template'leri upsert et (slug bazlı)
    // 2. Daily ve Weekly Occurrences merge:
    //    - Eğer aynı hafta revizyonu ise (isSameWeekRevision == true):
    //        - Var olan occurrence için status APPROVED ise -> status APPROVED korunur.
    //        - status WAITING_REVIEW veya ACTIVE ise -> durum korunur.
    //        - metadata (title, plannedMinutes, youtubeUrl) güncellenir.
    //        - weekly görevde approvedCount korunur; yeni targetCount <= approvedCount ise APPROVED kalır.
    //    - Eğer yeni bir hafta ise (isSameWeekRevision == false):
    //        - Yeni haftanın tüm occurrence'ları PENDING olarak başlatılır.
    //        - Eski haftanın kayıtları geçmiş raporları için veritabanında ARCHIVED/COMPLETED olarak saklanır.
    // 3. Yeni planda olmayan eski kayıtlar:
    //    - APPROVED ise -> history'de kalsın diye ARCHIVED yapılır.
    //    - PENDING ise -> ARCHIVED veya gizlenir.
}
```

---

## 5. Çocuk Onboarding & İnteraktif Tutorial Modu (Walkthrough HUD)

Çocuk modu ilk kez açıldığında (veya Ayarlar > Rehberi Tekrarla denildiğinde):
1. **Hoş Geldin & Görev Kartı Tanıtımı:** Görev kartlarının üzerindeki süre ve butonların ne anlama geldiği gösterilir.
2. **Yüzen Buton (Floating Button) Simülasyonu:**
   - Ekranda simüle edilmiş bir mini floating button belirir.
   - Çocuğa: *"Şimdi düğmeye bir kez hafifçe dokunarak fotoğraf çekmeyi dene!"* denir.
   - Dokununca flaş efekti ve `📸 1` sayacı artar.
3. **Görevi Bitirme Deneyimi (2 Saniye Basılı Tutma):**
   - Çocuğa: *"Harika! Şimdi dersin bittiğinde düğmeye 2 saniye basılı tutmayı dene."* denir.
   - Çocuk basılı tutarken dairesel yeşil halka dolar, tamamlanınca tebrik konfetisi patlar.
4. **Onay & Güven Mesajı:** *"Ders bittiğinde fotoğrafların ebeveynine gider ve onaylandığında görev yeşil olur!"* bilgisi verilerek ana ekrana geçilir.

---

## 6. Floating Button Overlay & Capture Yaşam Döngüsü

```text
[Çocuk "Başlat"a Dokunur]
       │
       ▼
Session oluşturulur (status = ACTIVE)
Occurrence.status = ACTIVE
FloatingButtonService başlatılır
       │
       ▼
[Floating Button Ekranın Üstünde Görünür]
  ├── Kısa Basma (Tap) ──> Anlık Ekran Görüntüsü Al (Flash Animasyonu + Sayacı Artır)
  └── Uzun Basma (2sn) ──> Dairesel İlerleme Dolar
                               │
                               ▼
                    [Görevi Bitir]
                    1. Son Ekran Görüntüsü Alınır
                    2. Session.status = WAITING_REVIEW
                    3. Occurrence.status = WAITING_REVIEW
                    4. Floating Button Kapatılır ve Gizlenir
                    5. Ebeveyn İnceleme Kuyruğuna Düşer
```
