# StudyTracker - Proje Günlüğü (Log)

### [2026-09-14] Başlangıç: Mimari ve UI Planlaması
- StudyTracker projesi başlatıldı.
- Sistem Mimarisi ve Teknik Şartname hazırlandı: [SYSTEM_ARCHITECTURE.md](file:///data/data/com.termux/files/home/projects/study-tracker/SYSTEM_ARCHITECTURE.md)
- Detaylı UI/UX Tasarım Planı oluşturuldu: [UI_PLAN.md](file:///data/data/com.termux/files/home/projects/study-tracker/UI_PLAN.md)

### [2026-09-14] Tamamlandı: Core Domain, Room, Floating HUD, Plan Merge Engine ve UI
- Android Gradle KTS, versiyon kataloğu ve GitHub Actions CI/CD iş akışı kuruldu.
- Core domain modelleri, Room veritabanı (6 entity, 6 DAO, TypeConverters) ve repository implementasyonları kodlandı.
- **Revize Plan vs Yeni Hafta Planı:** `PlanMergeEngine` üzerinde aynı hafta revizyonunda (`weekId` aynı) tamamlanmış (`approved`), bekleyen (`waiting_review`) ve aktif (`active`) görevleri koruyan; yeni haftada ise görevleri sıfırdan `pending` başlatan kayıpsız birleştirme motoru geliştirildi.
- `AIPromptBuilder` ile dinamik AI Master Prompt üreticisi kodlandı.
- `FakeCaptureDriver` ile yerel simülasyon ve test ortamı hazırlandı.
- `SessionStateManager`, `FloatingButtonService` (Overlay) ve `FloatingHUDView` (tek dokunuş screenshot, 2sn dairesel basılı tutma bitirme) kodlandı.
- **Çocuk İnteraktif Rehber & Demo Modu:** İlk açılışta açılan 3 adımlı sanal yüzen düğme deneme alanı ve rehber ekranı (`ChildTutorialScreen`) geliştirildi.
- Ebeveyn Dashboard, Kanıt İnceleme Zaman Tüneli, AI Plan Stüdyosu ve Geliştirici Test Konsolu Jetpack Compose ile tamamlandı.
- Tüm birim testler (`PlanMergeEngineTest`) yazıldı ve Git reposuna işlendi.

### [2026-09-14] Başarılı: GitHub Actions CI/CD ve APK Üretimi
- GitHub üzerinde `TheOsmanYILDIRIM/study-tracker` reposu senkronize edildi.
- GitHub Actions CI/CD workflow (`Build & Release StudyTracker APK`, Run ID: `34897481921`) tüm testleri ve derleme adımlarını başarıyla tamamladı (4m 17s).
- `StudyTracker-debug-apk` artifact'i (app-debug.apk) GitHub Actions üzerinden üretildi.

### [2026-09-15] Tamamlandı: Ebeveyn Plan İnceleme, Duraklatma (Pause/Resume), Hafta Takvimi & Basit Değişken Formatı
- **Ebeveyn Haftalık & Günlük Plan İnceleme:** `ParentDashboardScreen` sekmeli yapıya geçirildi ("Onay Masası" ve "Haftalık & Günlük Plan"). Gün gün (Pzt, Sal, Çar, Per, Cum, Cmt, Paz) ve tüm hafta görünümünde dersler, süreler, hedef durumları ve ebeveyn notları incelenebiliyor.
- **Öğrenci Dersi Duraklatma (Pause / Resume):** `SessionStateManager` ve `FloatingHUDView` içine duraklatma desteği eklendi. Sistem üstü yüzen pencerede ve öğrenci masasında animasyonlu duraklatıldı durumu, sayaç durdurma ve devam ettirme sağlandı.
- **Takvim Tabanlı Hafta Seçici:** `WeekCalendarPicker` bileşeni geliştirilerek haftaların elle yazılması yerine ay/hafta takviminden görsel olarak seçilmesi sağlandı.
- **Bozulmayan Basit Değişken/Metin Plan Formatı:** JSON kırılganlığını önlemek için `SimplePlanParser` ve `SimplePlanExporter` geliştirildi (`HAFTA = ...`, `[DERSLER]`, `[GUNLER]`, `[HAFTALIK]`). Hem basit metin hem JSON otomatik algılanıyor.
- **Birim Testleri:** `SimplePlanParserTest` ile parser ve exporter doğrulaması tamamlandı.
- **CI/CD Derleme:** GitHub Actions workflow (Run ID: `34955820944`) başarıyla tamamlandı ve yeni `StudyTracker-debug-apk` artifact'i üretildi.
- **Ultra Hata Toleransı & Auto-Healing:** `SimplePlanParser` ve `PlanValidator` şablon/prompt karmaşık metinlerini ayıklayacak, çoklu şablon bloklarını filtreleyecek ve yinelenen anahtarları otomatik iyileştirecek şekilde güncellendi (CI Run ID: `34957226496` ✓).
- **Room DB Kanonik JSON & Test:** `LocalPlanRepositoryImpl` içerisinde basit formatın kanonik JSON olarak veritabanına kaydedilmesi ve test süiti güncellendi (CI Run ID: `34957885567` ✓).
- **Sabit Keystore & Adaptive Icon:** Kalıcı imzalama ve özel uygulama ikonu bağlandı (CI Run ID: `34987156064` ✓).

### [2026-09-15] Tamamlandı: Jetpack Compose & Room Kapsamlı Performans ve FPS Optimizasyonu
- **Recomposition Yalıtımı:** `ChildHomeScreen` içinde saniyelik sayaç (`elapsedSeconds`) güncellemeleri `LiveActiveSessionBanner` alt bileşenine izole edildi. Ana ekran, görev listesi ve ilerleme kartının her saniye gereksiz yere baştan çizilmesi (recomposition thrashing) engellendi.
- **Flow & Collector Memoization:** Tüm ekranlarda (`ChildHomeScreen`, `ParentDashboardScreen`, `AIPlanStudioScreen`, `SessionReviewScreen`, `DeveloperConsoleScreen`) DAO çağrıları `remember(...) { ... }` içine alınarak her recomposition ve tuş basımında Room sorgularının iptal edilip baştan tetiklenmesi döngüsü ortadan kaldırıldı.
- **Async & LRU Korumalı Görsel Yükleme:** `EvidenceTimelineView` içinde ana iş parçacığında (UI thread) senkron `BitmapFactory.decodeFile` çağrısı kaldırıldı; arka planda çalışan (`Dispatchers.IO`) bellek önbellekli (LRU Cache) ve küçük resimler için 4x downsample eden (`inSampleSize = 4`) asenkron yükleme mekanizması uygulandı.
- **Room Veritabanı İndeksleri:** `OccurrenceEntity`, `SessionEntity` ve `ScreenshotEntity` tablolarında `date`, `weekId`, `status`, `sessionId` kolonlarına B-Tree indeksleri eklendi.
- **Pencere Sürükleme IPC Optimizasyonu:** `FloatingButtonService` içinde her mikroskopik piksel hareketinde yapılan `updateViewLayout` WindowManager IPC çağrıları optimize edildi.





