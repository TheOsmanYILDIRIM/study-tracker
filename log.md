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
- **Ultra Hata Toleransı & Auto-Healing:** `SimplePlanParser` ve `PlanValidator` şablon/prompt karmaşık metinlerini ayıklayacak, çoklu şablon bloklarını filtreleyecek ve yinelenen anahtar otomatik iyileştirecek şekilde güncellendi (CI Run ID: `34957226496` ✓).
- **Room DB Kanonik JSON & Test:** `LocalPlanRepositoryImpl` içerisinde basit formatın kanonik JSON olarak veritabanına kaydedilmesi ve test süiti güncellendi (CI Run ID: `34957885567` ✓).
- **Sabit Keystore & Adaptive Icon:** Kalıcı imzalama ve özel uygulama ikonu bağlandı (CI Run ID: `34987156064` ✓).

### [2026-09-15] Tamamlandı: Jetpack Compose & Room Kapsamlı Performans ve FPS Optimizasyonu
- **Recomposition Yalıtımı:** `ChildHomeScreen` içinde saniyelik sayaç (`elapsedSeconds`) güncellemeleri `LiveActiveSessionBanner` alt bileşenine izole edildi. Ana ekran, görev listesi ve ilerleme kartının her saniye gereksiz yere baştan çizilmesi (recomposition thrashing) engellendi.
- **Flow & Collector Memoization:** Tüm ekranlarda (`ChildHomeScreen`, `ParentDashboardScreen`, `AIPlanStudioScreen`, `SessionReviewScreen`, `DeveloperConsoleScreen`) DAO çağrıları `remember(...) { ... }` içine alınarak her recomposition ve tuş basımında Room sorgularının iptal edilip baştan tetiklenmesi döngüsü ortadan kaldırıldı.
- **Async & LRU Korumalı Görsel Yükleme:** `EvidenceTimelineView` içinde ana iş parçacığında (UI thread) senkron `BitmapFactory.decodeFile` çağrısı kaldırıldı; arka planda çalışan (`Dispatchers.IO`) bellek önbellekli (LRU Cache) ve küçük resimler için 4x downsample eden (`inSampleSize = 4`) asenkron yükleme mekanizması uygulandı.
- **Room Veritabanı İndeksleri:** `OccurrenceEntity`, `SessionEntity` ve `ScreenshotEntity` tablolarında `date`, `weekId`, `status`, `sessionId` kolonlarına B-Tree indeksleri eklendi.
- **Pencere Sürükleme IPC Optimizasyonu:** `FloatingButtonService` içinde her mikroskopik piksel hareketinde yapılan `updateViewLayout` WindowManager IPC çağrıları optimize edildi.
- **CI/CD Derleme:** GitHub Actions workflow (Run ID: `34988316038`) başarıyla tamamlandı (1m 28s).

### [2026-09-15] Tamamlandı: Test Modu Kalıcı Yönetimi ve Güncel İnteraktif Rehber
- **Test Modu Açma/Kapama:** `AppPreferences` yöneticisi oluşturuldu. `DeveloperConsoleScreen` içine Test Modu Anahtarı (Master Switch) eklendi. Test modu kapatıldığında ana ekrandaki geliştirici butonu gizlenerek uygulama canlı üretim moduna geçer.
- **Tek Seferlik ve Kalıcı Rehber:** Öğrenci Moduna ilk girişte açılan, tamamlandığında veya geçildiğinde `hasCompletedTutorial = true` olarak kaydedilen mekanizma kuruldu. Sonraki girişlerde doğrudan Görev Masası açılır.
- **İsteğe Bağlı Tekrar Açma:** Görev Masası'nın sağ üstündeki (?) yardım butonu ile rehber unutulduğunda istenildiği zaman tekrar açılabilir. Geliştirici konsolunda da rehberi sıfırlama butonu eklendi.
- **Yeni Özelliklerle Donatılmış 4 Adımlı Rehber:** Saniyelik sayaç, tek dokunuşla kanıt alma, **Pause/Resume (Duraklat/Devam Et)** mola verme mantığı ve 2 saniye basılı tutarak bitirme hareketlerinin tamamı canlı sandbox deneme alanına entegre edildi.

### [2026-09-15] Tamamlandı: Sıfır Uyarı & Sessiz Ekran Yakalama (StudyAccessibilityService & AccessibilityCaptureDriver)
- **Sessiz Erişilebilirlik Mimarisi:** Android 11+ (API 30+) `AccessibilityService.takeScreenshot()` API'si kullanılarak her oturumda ekran kaydı onay penceresi ve rahatsız edici yeşil/kırmızı kayıt noktası çıkaran `MediaProjection` tamamen kaldırıldı.
- **Kullanıcı Dostu Tek Seferlik Kurulum:** Ebeveyn sistem ayarlarından StudyTracker erişilebilirlik servisini 1 kez açtığında, uygulama arka planda veya yüzen düğmeye basıldığında sıfır sistem uyarısıyla ve sıfır çökme riskiyle anında tam ekran Bitmap görüntüsü yakalar.
- **Sürücü & UI Entegrasyonu:** `AccessibilityCaptureDriver`, `SessionStateManager`, `DeveloperConsoleScreen` ve `accessibility_service_config.xml` entegrasyonu tamamlandı.

### [2026-09-15] Tamamlandı: Aşırı Yüksek Performans & 60-120 FPS Sıfır Gecikmeli Dokunma Optimizasyonu
- **Sıfır Gecikmeli Optimistik Dokunma Tepkisi (0ms Touch Response):** `SessionStateManager` içinde "Çalışmayı Başlat", "Duraklat/Devam Et", "Manuel Kanıt Al" ve "Tamamla" aksiyonları tıklandığı an UI durumunu (`_activeState.value`) anında Main thread'de güncelleyecek şekilde yeniden yapılandırıldı. Veritabanı yazımları, dosya sıkıştırma ve bitmap işlemleri arka plana (`Dispatchers.IO`) taşınarak dokunma gecikmesi tamamen sıfırlandı.
- **Recomposition Storm ve Ticker Yalıtımı:** `ChildHomeScreen` ana ekranının saniyelik sayaç akışından etkilenmemesi için `isSessionActive` boolean `StateFlow` ayrıştırıldı. Sayaç metni `LiveTimerText` mikro-bileşenine izole edildi; böylece her saniye ana ekranın ve LazyColumn'daki tüm kartların baştan çizilmesi engellendi.
- **Compose @Immutable Kararlılığı:** `Occurrence`, `TaskTemplate`, `Plan`, `Session`, `Screenshot`, `Review` domain modellerine `@Immutable` annotasyonları eklendi. Compose derleyicisinin gereksiz recomposition'ları akıllıca atlaması (Smart Recomposition Skipping) sağlandı.
- **Room Flow Distinct & IO Offloading:** `LocalRepositories.kt` üzerindeki tüm DAO Flow akışlarına `.distinctUntilChanged()` ve `.flowOn(Dispatchers.IO)` eklendi; mükerrer ve gereksiz UI tetiklemeleri önlendi.
- **Statik Shape & Modifier Tahsisatı:** `StudyTaskCard` ve `ParentDashboardScreen` bileşenlerinde her frame'de üretilen nesne tahsisatları (`Triple(...)`, `RoundedCornerShape(...)`, `DAY_FILTERS`) statik ve değişmez hale getirildi.

### [2026-09-15] Tamamlandı: Zen Gece/Gündüz Teması, Yarı Saydam Koruyucu Arkalıklar ve Sıfır-Kasma 60/120 FPS Performans Çözümü
- **GPU Overdraw & Sıfır-Kasma Kaydırma (Zero-Jank 60/120 FPS):** Ekran arkasındaki tam boy şeffaf resim ve şeffaf `Scaffold` yapısı yerine, GPU Z-buffer depth testing'den tam yararlanan katı `ZenNightCanvas` (`#080D1A`) / `ZenDayCanvas` (`#EEF2F6`) tuval mimarisi uygulandı. Masal kitabı / doğa illüstrasyonu (`bg_zen_night.webp` / `bg_zen_day.webp`) üstteki yuvarlatılmış Hero Progress Card içine yerleştirildi. Böylece liste kaydırılırken GPU'nun tüm ekranı tekrar tekrar harmanlama yükü sıfırlandı.
- **Kart Katman Düzleştirme (Flattened Composition Tree):** `StudyTaskCard` içindeki iç içe geçmiş `Card -> Surface -> Box` hiyerarşisi kaldırılarak doğrudan donanım hızlandırmalı tek `Column`/`Row` + `clip` + `border` düğümüne dönüştürüldü. Ağır dinamik `shadow(elevation)` kaldırıldı.
- **Ebeveyn Masası Masal Görseli:** Ebeveyn paneline (`ParentDashboardScreen`) ve rol seçim ekranına (`RoleSelectionScreen`) de Zen atmosferik Hero kartı ve tema uyumu entegre edildi.
- **🌙 Gece / ☀️ Gündüz Dinamik Tema Geçişi:** `AppPreferences` yöneticisine `isNightMode` StateFlow ve `toggleNightMode` desteği eklendi. TopAppBar'a tek dokunuşluk 🌙/☀️ butonu ve geliştirici konsoluna tema seçici switch yerleştirildi.
- **Yarı Saydam Koruyucu Arkalıklar (High Contrast Backplates):** Görseller ve açık zeminler üzerindeki metinlerin kaybolmasını önlemek için yüksek kontrastlı `ZenTextBackplate` ve `ZenTopBarBackplate` uygulandı.
- **CI/CD Derleme:** GitHub Actions workflow (`Build & Release StudyTracker APK`, Run ID: `35015675276` ✓, Commit: `d6012ef`) başarıyla tamamlandı ve `StudyTracker.apk` indirilenler klasörüne aktarıldı.

