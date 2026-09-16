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
### [2026-09-15] Tamamlandı: Tam Ekran Arka Plan, Hafif Kutu Butonlar ve R8 Optimized Release APK Derlemesi
- **Tam Ekran Atmosferik Arka Plan:** Tüm ekranların arkasına tam ekran `Image(bg_zen_night / bg_zen_day)` yerleştirilerek yüksek kontrastlı yarı saydam okuma katmanı (`0xD9080D1A` / `0xB3EEF2F6`) entegre edildi.
- **Hafif Kutu Butonlar:** `StudyTaskCard` içindeki ağır Material3 `Button` (dahili `Surface`, `InteractionSource`, `animateElevation` ve ripple layer) yerine saf `Box.background(shape).clickable` butonları uygulandı.
- **R8 ProGuard & Release APK Optimizasyonu:** Compose derleyicisinin debug trace overhead'ini sıfırlayan, metot inlining ve dead-code elimination sağlayan `assembleRelease` CI/CD hattı kuruldu. 16MB'lık debug APK yerine 2.1MB'lık ultra-optimize imzalı Release APK üretildi (CI Run ID: `35018741072` ✓).

### [2026-09-15] Tamamlandı: Görev İptal, İncelemedeki Görevi Yeniden Başlatma, Haftalık Hedefler Üste (LazyRow) & Canlı Paralaks Gece Arka Planı
- **Görevi İptal Etme (`cancelSession`):** Çalışma sırasında veya mola verildiğinde görevi iptal edip durumu `PENDING`'e geri döndüren mekanizma `SessionStateManager`'a eklendi; Floating HUD ve `LiveActiveSessionBanner` bileşenlerine kırmızı "İptal Et" butonu yerleştirildi.
- **İncelemedeki Görevi Yeniden Başlatma:** Ebeveyn onayı bekleyen (`WAITING_REVIEW`) görevlerin üzerine yazılabilmesi için `StudyTaskCard` ve `StudyWeeklyTaskCard` kartlarına "Yeniden Başlat / Tekrar" butonu eklendi.
- **Gündüz Modunu Kaldırma & Tek Zen Gece Teması:** Tüm ekranlardan (ChildHome, ParentDashboard, RoleSelection, DeveloperConsole) gündüz modu toggle butonları ve dinamik kontroller kaldırılarak atmosferik Zen Masal Gece teması sabitlendi.
- **Haftalık Hedefler Üste & Yatay Kaydırma (LazyRow):** Çocuk ana ekranında haftalık hedefler en üste taşındı ve yatay kaydırmalı `LazyRow` içinde `StudyWeeklyTaskCard` ile gösterildi; günlük görevler altta dikey sıralandı.
- **Mükerrer Hero Kartının Kaldırılması & Canlı Paralaks Arka Plan:** Yinelenen hero progress kartı kaldırıldı; arka plandaki masal görseli %20 daha açık parlak bir filtreyle belirginleştirildi ve GPU `graphicsLayer` üzerinde çalışan yavaş, akıcı bir nefes alma/paralaks süzülme animasyonu (`rememberInfiniteTransition`) uygulandı.

### [2026-09-16] Tamamlandı: Tek Parça Zen Masal Arkaplanı, Dinamik Parlaklık Fullenmesi, Uçan Yıldız ve Test Modu Canlı Önizleme Sliderı
- **Tek Parça Yüksek Kaliteli Gece İllüstrasyonu:** Çoklu katman ayrıştırma karmaşası yerine tek parça yüksek çözünürlüklü masal gecesi görseline (`bg_zen_night.webp`) dönüştürüldü; GPU süzülme ve nefes alma paralaksı korundu.
- **Canlı Parlaklık & Renk Doygunluğu Fullenmesi:**
  - 0% ilerlemede arkaplan gizemli, loş ve sakin bir gece modundadır (`alpha = 0.55f`, koyu okuma koruması).
  - Görevler tamamlandıkça (100% fullendiğinde) renk doygunluğu (`ColorMatrix saturation: 0.85 -> 1.30`) artar, okuma koruması açılarak masal görseli ve altın takımyıldızları tüm ışıltısıyla parlar.
- **Uçan Kuyruklu Yıldız & Patlama Halkası (`FlyingComet` & `StarBurstRing`):** Görev bitirildiğinde görev kartından gökyüzüne süzülen altın-cyan kuyruklu yıldız parçacığı uçar (`Animatable(1200ms)`) ve hedef gökyüzü noktasında süpernova patlama dalgası oluşturur.
- **🧪 Test Modu Canlı Önizleme Sliderı:**
  - Hem `ChildHomeScreen`'de (Test modu açıkken) hem de `DeveloperConsoleScreen` içinde 0% - 100% arasında sürüklenebilir interaktif Slider yerleştirildi.

### [2026-09-16] Tamamlandı: Tek Cihazda Çift APK (Flavors) & Supabase Gerçek Zamanlı Bulut Senkronizasyonu
- **Gradle Product Flavors (Çift APK):** `child` ve `parent` flavor'ları tanımlandı. Tek cihazda aynı anda yüklenebilen iki bağımsız uygulama paketi (`com.studytracker.child` - "StudyTracker Öğrenci" ve `com.studytracker.parent` - "StudyTracker Veli") oluşturuldu.
- **Role Özel Otomatik Başlangıç:** `BuildConfig.APP_ROLE` ile Öğrenci APK'sı doğrudan Çalışma Masasına/Rehbere, Veli APK'sı ise PIN korumalı Ebeveyn Masasına yönlenir.
- **Supabase REST, Storage & Realtime İstemcisi:** OkHttp ve Kotlinx Serialization ile güçlendirilmiş hafif `SupabaseHttpClient` ve veri transfer modelleri (`RemoteOccurrenceSyncDto`, `RemoteSessionSyncDto`, `RemoteScreenshotSyncDto`, `RemoteReviewSyncDto`) kodlandı.
- **6 Haneli Aile Eşleşme Kodu (`ST-XXXX`):** `CloudSyncManager` ile cihazlar arası eşleşme, veritabanı kopyalama ve iki yönlü senkronizasyon sağlandı.
- **Modern Arayüz ve Otomatik Eşitleme:** Tüm ekranlara (RoleSelection, ParentDashboard, ChildHome, DevConsole) entegre `CloudSyncDialog` bileşeni eklendi; ders bitirme ve inceleme kararlarında arka planda otomatik bulut eşitlemesi bağlandı.

### [2026-09-16] Tamamlandı: Çift Yönlü Sağlam Senkronizasyon (Bidirectional Reconciliation) & Otomatik Eşitleme
- **Kök Hata Tespiti & Çözümü:**
  - Veli ve Öğrenci cihazlarının / APK'larının senkronize olamamasının ana sebebi, `CloudSyncManager.syncAll()` içinde "eğer yerel veritabanı boşsa köprüden oku, değilse köprünün üzerine yerel veriyi yaz" mantığının bulunmasıydı. Öğrenci veya Veli tarafında herhangi bir eski görev varken senkronizasyon tetiklendiğinde diğer tarafın güncel planı veya onay durumu okunmayıp üzerine yazılıyordu.
  - Ayrıca iki bağımsız APK ilk kurulduğunda rastgele farklı aile kodları ürettiği için birbirlerinin dosya köprüsünü göremiyorlardı.
- **İki Yönlü Akıllı Durum Mutabakatı (Status Reconciliation Engine):**
  - **Durum Hiyerarşisi:** `APPROVED > WAITING_REVIEW > ACTIVE > PENDING`. Taraflardan biri görevi onayladığında onay durumu korunur; öğrenci görevi bitirdiğinde onay bekleyen durum veli tarafına anında yansıtılır.
  - **Haftalık Görev & Geri Bildirim:** Veli reddettiğinde girilen açıklama notu öğrenci ekranında kırmızı uyarı kartı olarak görünür; haftalık soru/hedef sayaçları `maxOf(local, remote)` ile birleştirilir.
  - **Plan & Şablon Eşitlemesi:** Veli AI Plan Stüdyosu'ndan yeni veya revize bir plan yüklediğinde, öğrenci uygulaması açılır açılmaz yeni haftayı ve dersleri otomatik olarak alır.
- **Çok Konumlu Paylaşılan Dosya Köprüsü (Multi-Path Local Bridge):**
  - `Environment.DIRECTORY_DOWNLOADS`, `Environment.DIRECTORY_DOCUMENTS`, `/sdcard/Download`, `/sdcard/Documents`, `/storage/emulated/0/...` ve uygulama dizinlerinin tamamı taranarak en güncel `updatedAt` zaman damgalı köprü yükü seçilir ve tüm erişilebilir konumlara yazılır.
- **Varsayılan Paylaşılan Aile Kodu (`ST-2026`):**
  - Her iki APK ilk kez yüklendiğinde varsayılan olarak `ST-2026` ortak aile koduna bağlanır; kullanıcının elle kod kopyalama/yazma zorunluluğu olmadan tek cihazda veya aynı ağda doğrudan çalışır.
- [x] Otomatik Tetikleme:
  - Veli paneli açıldığında (`LaunchedEffect`), Öğrenci masası açıldığında (`LaunchedEffect`), AI Stüdyosu'nda plan içe aktarıldığında ve veli onay/red kararı verdiğinde anında `syncAll()` çağrılır.
- **Birim Testleri:**
  - `SyncReconciliationTest` ile durum çözünürlüğü, haftalık hedef tamamlama ve ebeveyn geri bildirim notlarının aktarımı doğrulandı.

### [2026-09-16] Tamamlandı: Android ContentProvider Binder IPC & Kalıcı İmzalı APK Derlemesi
- **Sıfır Gecikmeli Doğrudan IPC Köprüsü (`StudySyncProvider`):** Android Scoped Storage kısıtlamalarını aşmak ve tek cihazda yan yana çalışan Veli ve Öğrenci APK'ları arasında 0ms hızında veri aktarmak için ContentProvider Binder IPC altyapısı kodlandı (`content://com.studytracker.child.syncprovider` & `content://com.studytracker.parent.syncprovider`).
- **Gerçek Zamanlı İnternet / Bulut Senkronizasyonu (Supabase Backend):** Farklı fiziksel cihazlar üzerinden test yapılabilmesi için Supabase REST & Realtime mimarisi ve SQL tablo şemaları hazırlandı.
- **CI/CD Derleme & Kurulum:** GitHub Actions workflow (`Build & Release StudyTracker APK`, Run ID: `35087642980` ✓) başarıyla tamamlandı; imzalı `StudyTracker-Veli.apk` (2.5MB) ve `StudyTracker-Ogrenci.apk` (2.5MB) `/sdcard/Download/` dizinine aktarıldı.

### [2026-09-16] Tamamlandı: Sıfır Kurulum & Girişsiz İnternet Bulut Rölesi (Zero-Config Cloud Relay)
- **Girişsiz ve Ücretsiz Bulut Rölesi Entegrasyonu:** Kullanıcının hesap açması, veritabanı kurması veya API anahtarı girmesi gerekmeksizin, yalnızca ortak 6 haneli Aile Kodu (`ST-XXXX`) ile çalışan küresel bulut rölesi (`ntfy.sh` tabanlı `studytracker_relay_${familyCode}`) doğrudan `CloudSyncManager` içine entegre edildi.
- **Üç Katmanlı Eşitleme Mimarisi:** Senkronizasyon çağrıldığında sistem sırasıyla; (1) Tek cihazdaki diğer APK'nın Binder IPC ContentProvider'ını, (2) İnternet üzerindeki bulut rölesini, (3) Yerel dosya köprüsünü sorgular; en güncel zaman damgasına sahip veriyi alıp yerel Room veritabanıyla akıllıca birleştirir ve her üç kanala birden geri dağıtır.
- **Sıfır Çaba ile Uzaktan Eşitleme:** Veli dünyanın herhangi bir yerinde 4G/5G ile plan eklediğinde veya ders onayladığında, öğrencinin evdeki Wi-Fi'ya bağlı telefonu anında güncellenir.

### [2026-09-16] Düzeltildi: Bulut Yük Boyutu (Attachment URL) & Android 11+ Package Visibility IPC
- **Kök Hata 1 (Bulut Yükü 4KB Sınırı):** ntfy.sh bulut rölesinde 19 derslik plan JSON'u (>4KB) `attachment.url` (`https://ntfy.sh/file/...`) olarak saklandığı için `eventObj["message"]` içindeki metin parse edilemiyordu. `readFromCloudRelay` fonksiyonuna `attachment.url` üzerinden tam JSON dosyasını indirme ve parse etme desteği eklendi.
- **Kök Hata 2 (Android 11+ IPC Görünürlüğü):** Android 11+ (API 30+) işletim sistemi `AndroidManifest.xml` içinde `<queries>` bloğu tanımlanmadığında diğer APK'nın ContentProvider'ını engelliyordu. `com.studytracker.parent` ve `com.studytracker.child` paketleri ve `syncprovider` otoriteleri `<queries>` etiketine eklenerek Binder IPC erişimi açıldı.


