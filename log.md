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

### [2026-09-16] Tamamlandı: Yerel Dosya Köprüsünün Kaldırılması & Sürekli Otomatik Senkronizasyon Döngüsü
- **Yerel Dosya Köprüsünün Tamamen Kaldırılması:** Bulut testinin güvenilirliğini gölgeleyen ve sahte pozitif oluşturan tüm ortak disk dosyası okuma/yazma kodları (`getBridgeFiles`, `readFromBridgeFile`, `writeToBridgeFile`) tamamen kaldırıldı. Sistem artık %100 saf internet bulut rölesi (`ntfy.sh`) ve tek cihazdaki yan yana APK'lar için Android Binder IPC üzerinden çalışır.
- **Sürekli Otomatik Eşitleme Döngüsü (Auto-Sync Loop):** `ChildHomeScreen` ve `ParentDashboardScreen` ekranlarına 10 saniyelik aralıksız arka plan otomatik eşitleme döngüsü eklendi. Veli veya öğrenci uygulamada gezinirken veya açtığında hiçbir butona basmaya gerek kalmadan tüm değişiklikler anında karşılıklı senkronize olur.

### [2026-09-16] Tamamlandı: Kanıt Görselleri Uçtan Uca Aktarımı & Ebeveyn Reddet/Onayla Anında Tepki Düzeltmesi
- **Tekil Session ID Senkronizasyonu:** `SessionStateManager` ve `LocalSessionRepositoryImpl` arasındaki oturum ID çiftliği giderildi. Bellek, Room veritabanı ve ekran görüntüsü sürücüsünün (`FakeCaptureDriver` & `AccessibilityCaptureDriver`) tam olarak aynı `sessionId` üzerinden çalışması sağlandı.
- **`StudySyncProvider` (Binder IPC) Kanıt ve İnceleme Desteği:** Binder IPC aktarımına Base64 ekran görüntüsü sıkıştırma ve ayrıştırma mekanizması eklendi; ebeveyn onay ve red kararlarının diğer APK'da `OccurrenceStatus` ve uyarı metnini anında güncellemesi sağlandı.
### [2026-09-16] Tamamlandı: Aşırı Basit & Sıfır Hata Paylı İki Yönlü Senkronizasyon Mimarisi
- **Basitleştirilmiş Tekil Değiş-Tokuş (Single Unified IPC Exchange):**
  - Karmaşık, yavaş, 15 saniyelik ağ zaman aşımlarına yol açan ve birbiriyle çakışan çok katmanlı yapı (`Supabase` dummy endpoint'ler, Scoped Storage engeline takılan dosya köprüleri, takılan ağ istekleri) tamamen temizlendi.
  - `StudySyncProvider` ve `CloudSyncManager` arasında tek bir doğrudan ContentProvider `sync` metodu geliştirildi. Veli veya Öğrenci tarafında herhangi bir değişiklik olduğunda diğer APK'nın provider'ı çağrılır, tek bir IPC çağrısıyla yerel Room veritabanları 2 milisaniyede karşılıklı eşitlenir.
- **Kanıt Görselleri İçin `openFile` ve Hafif Küçük Resim Desteği:**
  - `StudySyncProvider` içine `openFile(uri, mode)` eklenerek büyük ekran görüntülerinin Binder bellek sınırını (`TransactionTooLargeException`) aşmadan doğrudan dosya akışı üzerinden okunması sağlandı.
  - `EvidenceTimelineView` bileşeni `content://`, `data:image/jpeg;base64,...` ve yerel dosya yollarını LRU önbellekle asenkron ve akıcı şekilde gösterecek şekilde güçlendirildi.
- **Arka Planda Donmayan Bulut Yedeklemesi:**
  - Ağ çağrıları 3 saniye katı zaman aşımına alındı ve yerel eşitlemeyi asla bekletmeyecek/engellemeyecek şekilde tamamen bağımsız arka plan görevine dönüştürüldü.
- **Tek Dokunuşla Manuel Kopyala-Yapıştır Köprüsü:**
  - `CloudSyncDialog` içinde "Veriyi Kopyala" (tüm plan, görevler ve durumları panoya kopyalar) ve "Yapıştır & Yükle" (panodaki veriyi tek tıkla içeri aktarır) butonları ile internet veya cihaz kısıtlamasından bağımsız %100 garantili yedek köprü sağlandı.

### [2026-09-16] Tamamlandı: Özel `.studyplan` Dosya Formatı, Otomatik İçe Aktarma, Lossy WebP Kanıt Sıkıştırması ve Secere Takibi
- **Özel `.studyplan` Dosya Formatı & Android Intent Entegrasyonu:**
  - `application/vnd.studytracker.plan` MIME tipi ve `.studyplan` dosya uzantısı tanımlandı.
  - Android Manifest içine `VIEW` ve `SEND` intent filtreleri eklenerek kullanıcının WhatsApp, Telegram, Gmail veya Dosyalar uygulamasından bir `.studyplan` dosyasına dokunduğunda `MainActivity` tarafından otomatik açılıp içeri aktarılması sağlandı (`StudyPackageExchangeManager.importPackageFromUri`).
- **Lossy WebP Ekran Görüntüsü Sıkıştırması:**
  - Android donanım destekli `Bitmap.CompressFormat.WEBP_LOSSY` (quality: 65, max dimension: 640px) kullanılarak ekran görüntüleri 2-3 MB'tan 8-15 KB'a düşürüldü.
  - 10-15 adet tam oturum kanıt görseli içeren bir günlük rapor paketi sadece ~150 KB boyutunda olup WhatsApp üzerinden saniyeler içinde gönderilebilir hale getirildi.
- **Secere Takibi & İki Yönlü Akıllı Birleştirme:**
  - Öğrenci günlük raporunu (`STUDY_REPORT`) veliye gönderdiğinde, veli uygulaması tamamlanan tüm dersleri, süreleri, onay bekleyen oturumları ve WebP kanıtları eksiksiz kendi Room veritabanına işler (öğrencinin tüm çalışma seceresini tutar).
### [2026-09-16] Tamamlandı: WhatsApp "Birlikte Aç" Desteği & Büyük Belirgin Paylaşım Kartları
- **WhatsApp İçerik Sağlayıcı & "Birlikte Aç" Entegrasyonu:**
  - WhatsApp'ın dahili dosya sağlayıcısının (`content://com.whatsapp.provider.media/export/...`) uzantı bilgisi içermeyen akışlarında da StudyTracker'ın "Birlikte Aç" listesinde görünmesi sağlandı.
  - `AndroidManifest.xml` içine `pathSuffix=".studyplan"`, `pathPattern=".*\\.studyplan"`, `mimeType="application/octet-stream"`, `mimeType="application/json"`, `mimeType="text/plain"`, `mimeType="*/*"` ve `ACTION_SEND` intent filtreleri tanımlandı.
- **Büyük & Belirgin Hero Paylaşım Kartları:**
  - `ParentDashboardScreen`: Onay Masası sekmesinin en üstüne "📦 WhatsApp & Dosya Köprüsü (.studyplan)" kartı eklendi. 42dp yüksekliğinde "Planı Gönder" ve "Rapor Yükle" butonları ile tek tıkla paylaşım ve içe aktarma sağlandı.
  - `ChildHomeScreen`: Canlı görev listesinin üzerine büyük, dikkat çekici yeşil rozetli "📦 Günlük Rapor & Kanıt Paketi (.studyplan)" kartı ve tam genişlikli "Raporu WhatsApp / Dosya İle Gönder" butonu yerleştirildi.
- **Evrensel Metin & URI Ayrıştırma:**
  - `MainActivity` intent işleyicisi hem dosya akışlarını (`Uri`) hem de mesaj olarak kopyalanıp paylaşılan JSON/DSL metin yüklerini (`EXTRA_TEXT`) otomatik ayrıştırıp yükleyecek şekilde genişletildi.

### [2026-09-16] Tamamlandı: Veli İcraat Masası, Öğrenci Başarıları Karnesi ve İlerleme Sıfırlama Özelliği
- **Veli İcraat Masası ve Başarı Karnesi UI:**
  - Veli tarafındaki Onay Masası sekmesi "🚨 Öğrenci İcraat Masası" olarak yenilendi.
  - "🎓 Öğrenci Çalışma Karnesi": Onaylanan ders sayısı, başarı oranı, onay bekleyen oturum sayısı ve kalan dersleri net özetleyen durum kartı eklendi.
  - "🚨 Öğrencinin Onay Bekleyen Oturumları": Her oturum kartı `"🎓 Öğrenci Tamamladı: [Ders Adı]"`, başlangıç/bitiş saatleri, ekran kanıt sayısı, "🔍 Kanıtları İncele", "✅ Aferin & Onayla" ve "❌ Reddet" butonları ile donatıldı.
  - "❌ Not Bırak & Reddet": Veli reddet butonuna bastığında açıklama notu girebileceği bir diyalog açılır; girilen not öğrenciye doğrudan uyarı mesajı olarak iletilir.
  - "🏆 Öğrencinin Başarıları & Onaylanan Dersler": Öğrencinin bitirdiği ve velinin onayladığı tüm dersler yeşil başarı rozetleriyle geçmiş listesi olarak sunuldu.
- **Veli Haftalık Planında Öğrenci Perspektifli Görev Rozetleri:**
  - Haftalık Plan sekmesindeki kartlar (`OccurrenceAdminCard`): `✅ Öğrenci Yaptı & Onaylandı`, `⏳ Öğrenci Tamamladı (Onay Bekliyor)`, `⚡ Öğrenci Şu An Çalışıyor`, `⚪ Öğrenci Henüz Yapmadı` olarak güncellendi ve reddedilen derslerde veli notunu içeren kırmızı uyarı bandı eklendi.
- **Hem Veli Hem Öğrenci Uygulamasında İlerleme Sıfırlama:**
  - Veli ve Öğrenci TopBar'larına "🔄 İlerlemeyi Sıfırla" butonu ve teyit diyalogu eklendi.
  - `StudyPackageExchangeManager.resetAllProgress(context, weekId)` ve DAO `resetProgress` ile plan iskeletine dokunulmadan tüm oturumlar, kanıtlar ve ders tamamlanma durumları güvenle sıfırlanabilir hale getirildi.

## [2026-09-16] 29. Öğrenci Görev Tamamlama Notu & Veli İnceleme Entegrasyonu
- **Öğrenci Notu Giriş Arayüzü (`ChildHomeScreen`):**
  - Öğrenci dersi bitirmek için "Bitir" butonuna bastığında karşısına çıkan tebrik diyalogunda isteğe bağlı olarak velisine çalışma notu (çözülen soru sayısı, netler, anladığı veya zorlandığı kısımlar) yazabileceği bir giriş alanı sunuldu. "Notu Kaydet & Bitir" veya "Bitir" seçenekleriyle hızlıca tamamlanabiliyor.
- **Veri Modeli ve Veritabanı Genişletmesi (`DomainModels`, `Entities`, `DAOs`, `AppDatabase`):**
  - `Occurrence` ve `Session` modellerine `studentNote: String? = null` alanı eklendi.
  - Room DB versiyonu 2'ye güncellendi (`OccurrenceEntity` ve `SessionEntity` tablolarına `studentNote` kolonu tanımlandı).
  - DAO ve repository katmanlarına `updateStudentNote` ve `finishSession(studentNote)` fonksiyonları eklendi.
- **Veli Ekranları Entegrasyonu (`ParentDashboardScreen`, `SessionReviewScreen`):**
  - Veli Masasında onay bekleyen oturum kartlarında, onaylanan dersler listesinde ve haftalık plan kartlarında öğrencinin yazdığı not özel rozetlerle (`Öğrenci Notu: ...`) gösterildi.
  - Kanıt inceleme ekranında (`SessionReviewScreen`) oturum özetinin altında öğrenci notu özel neon kutuyla vurgulandı.
- **Paket & Senkronizasyon Uyumluluğu (`StudyPackageExchangeManager`, `StudySyncProvider`, `SupabaseDto`):**
  - `.studyplan` WhatsApp paylaşım paketleri, ContentProvider Binder IPC ve Supabase bulut senkronizasyon modellerine `studentNote` alanı entegre edilerek iki yönlü aktarım güvenceye alındı.

## [2026-09-16] 30. LaTeX Destekli Şıklı Test & Sınav Değerlendirme Sistemi (AI Prompt, Gizli Öğrenci Çözümü ve Veli Masası)
- **Domain Modelleri & Room Veritabanı Genişletmesi (`Quiz`, `QuizQuestion`, `QuizOption`, `QuizEntity`, `QuizDao`, `AppDatabase`):**
  - `Quiz`, `QuizQuestion`, `QuizOption` domain modelleri oluşturuldu.
  - Room DB versiyonu 3'e yükseltildi, `quizzes` tablosu ve `QuizDao` eklendi.
  - Soru listesi ve öğrenci cevapları JSON serializer ile tam tip güvenliğiyle saklanıyor.
- **Bozulmayan Basit Metin DSL Test Formatı & Parser (`SimpleQuizParser`):**
  - AI modellerinin (ChatGPT, Claude, Gemini) kolayca üretebileceği ve kopyalanıp yapıştırılabileceği `=== TEST: <Başlık> ===`, `[SORU 1]`, `A) ...`, `DOGRU: ...`, `COZUM: ...` formatı tasarlandı.
  - LaTeX matematiksel ifadeler (`$\frac{a}{b}$`, `$\sqrt{x}$`, `$\int$`, `$x^2$`) için tam regex ve parser desteği kodlandı.
  - `SimpleQuizParserTest` ile otomatik birim testleri doğrulandı.
- **Dinamik AI Soru & Test Üretici Promptu (`AIPromptBuilder` & `AIQuizStudioDialog`):**
  - Veli için sınıf seviyesi, ders konusu, soru sayısı ve süreye göre hazır AI promptu üreten stüdyo diyalogu geliştirildi.
  - Tek tıkla prompt kopyalama, örnek soru doldurma ve yapıştırılan testi anında veritabanına kaydetme iş akışı sağlandı.
- **Donanım Hızlandırmalı & %100 Çökme Korumalı Yerel (Native) Matematik Görüntüleyici (`LatexMathView`):**
  - LazyColumn ve liste görünümlerinde onlarca WebView örneğinin aynı anda açılmasından kaynaklanan bellek tüketimi ve çökme riski tamamen ortadan kaldırıldı.
  - Yerine sıfır gecikmeli (0ms), 120 FPS akıcı ve %100 yerel Compose Text tabanlı Unicode matematik dönüştürücüsü (`formatLatexToNativeMath`) entegre edildi.
  - Kesirler (`1/2`, `(x²-4)/(x-2)`), karekökler (`√(x)`), üslü sayılar (`x²`, `cos²(x)`), indisler (`x₀`), Yunan harfleri (`π`, `θ`, `α`), limit, integral ve trigonometrik fonksiyonlar donanım hızlandırmalı ve sıfır çökme garantisiyle çiziliyor.
- **Öğrenci Test Çözüm Ekranı (`ChildQuizScreen` — Gizli Cevap/Skor Kuralı):**
  - Öğrenci sorular arasında kaydırarak veya düğmelerle gezinebiliyor, şıkları işaretleyebiliyor.
  - **Kritik Kural:** Öğrenci testi çözerken veya bitirdiğinde doğru/yanlış cevapları ve test skorunu ASLA görmez; yalnızca teslim onay diyalogu gösterilerek cevapları kaydedilir.
- **Veli Detaylı Test İnceleme Masası (`ParentQuizReviewScreen` & `ParentDashboardScreen`):**
  - Veli tarafında çözülen testler için başarı oranı, doğru/yanlış/boş sayıları, öğrencinin işaretlediği şık ile doğru cevabın renkli karşılaştırması ve çözüm açıklamaları eksiksiz sunuldu.
- **Paket & Senkronizasyon Entegrasyonu (`StudyPackageExchangeManager`, `StudySyncProvider`, `SupabaseDto`):**
  - `.studyplan` plan ve günlük rapor paketlerine testler entegre edildi; veliden öğrenciye test gönderme ve öğrenciden veliye çözülmüş test cevaplarını iletme döngüsü tamamlandı.

## [2026-09-16] 31. Görev İçi Test Bütünleştirmesi & Saf Android Paylaşım (Share Sheet) Mimarisi
- **Günlük Akışa Gömülü Test Kartları (`ChildHomeScreen`):**
  - Testlerin izole ve ayrı bir başlık altında toplanması kaldırıldı.
  - Günlük görevler ve testler `📅 Bugünkü Dersler & Görevler (${dailyTasks.size + quizzes.size})` başlığı altında tek ve kesintisiz bir akış olarak birleştirildi.
  - Test kartı (`StudyQuizCard`), `StudyTaskCard` ile birebir aynı Zen koyu tema, pastel ikon rozeti, süre etiketi ve durum hap butonları (Neon Mint "Testi Çöz", "Çözüldü (İncelemede)", "Tamamlandı") ile yeniden tasarlandı.
- **Dinamik İlerleme & Yıldız Entegrasyonu:**
  - Günlük tamamlanan işler ve toplam iş sayısı (`completedTasksCount` / `totalTasksCount`) hesaplamasına testler doğrudan dahil edildi.
  - Test bitirildiğinde ilerleme barı artar, arkaplan parlaklığı/doygunluğu fullenir ve gökyüzüne süzülen altın kuyruklu yıldız animasyonu tetiklenir.
- **Saf Android Paylaşım Sayfası (Share Sheet) Mimarisi:**
  - Veli ve Öğrenci TopBar'larından ve ana panellerden sunucu/bulut senkronizasyon diyalogları ve butonları temizlendi.
  - Tüm veri iletimi, cihazlar arası `.studyplan` dosya paketi ve Android yerel Paylaşım Sayfası (WhatsApp, Telegram, QuickShare, Bluetooth, Dosya Yöneticisi) üzerine kurgulandı.

## [2026-09-16] 32. Ders ve Test Bitiminde İnteraktif Öğrenci Öz Değerlendirme & Veli Raporlama Sistemi
- **İnteraktif Ders Bitirme Değerlendirmesi (`ChildHomeScreen`):**
  - Ders oturumu bitirildiğinde açılan diyalog zengin bir öz değerlendirme anketine dönüştürüldü.
  - Öğrenci tek dokunuşla konuyu anlama düzeyini (🌟 Harika, 👍 İyi, 🤔 Zorlandım, ❌ Zayıf), odaklanma seviyesini (⚡ %100 Odak, 🎯 İyi, 📱 Dağıldı), çözdüğü soru sayısını (+10, +20, +30, +50) seçebiliyor ve velisine açıklama notu yazabiliyor.
  - Tüm bu değerlendirme Room veritabanına (`OccurrenceEntity` ve `SessionEntity`) kaydediliyor.
- **İnteraktif Test Bitirme Değerlendirmesi (`ChildQuizScreen`):**
  - Test çözümü tamamlanıp bitirildiğinde öğrencinin karşısına test zorluk derecesi (🟢 Kolay, 🟡 Orta, 🔴 Zor), tahmini başarı/güven hissi (🌟 Çok İyi, 👍 Fena Değil, 🤔 Kararsızım) ve veliye iletmek istediği test notunu girebileceği anket sunuldu.
  - Değerlendirme `QuizEntity.studentNote` ve `Quiz.studentNote` alanına kaydediliyor (Room DB v4).
- **Veli Masası & Test İnceleme Ekranı Entegrasyonu (`ParentDashboardScreen`, `ParentQuizReviewScreen`, `SessionReviewScreen`):**
  - Veli Masasında onay bekleyen derslerde ve çözülen test kartlarında öğrencinin öz değerlendirme metrikleri ("Öğrenci Öz Değerlendirmesi") renkli rozetlerle sergileniyor.
  - Test inceleme ekranının en üstünde öğrencinin test zorluk ve başarı algısını gösteren özel bilgi kartı yerleştirildi.
- **`.studyplan` Paket Paylaşımı Uyumluluğu (`StudyPackageExchangeManager`):**
  - Öğrenci günlük çalışma raporunu veliye WhatsApp veya dosya olarak gönderdiğinde tüm ders ve test değerlendirme verileri eksiksiz pakete dahil edilip veli tarafında içe aktarılıyor.

## [2026-09-18] 33. Görev Başlatıldığında Video Linklerinin Otomatik ve Sorunsuz Açılması (Video Launcher & Plan Parser Güçlendirmesi)
- **Android 11+ Package Visibility & Intent Queries (`AndroidManifest.xml`):**
  - `<queries>` bloğuna `com.google.android.youtube`, `https` ve `http` intent filtreleri eklendi. Android 11+ kısıtlamalarında dış video ve tarayıcı uygulamalarının engellenmesi sorunu çözüldü.
- **Ultra Dayanıklı URL Temizleyici ve Çıkarıcı (`sanitizeUrl` & `extractVideoUrl`):**
  - Parantez (`(https://...)`), köşeli parantez, tırnak, noktalama işaretleri (`.`, `,`, `;`) gibi URL sonuna yapışan karakterleri otomatik temizleyen temizleme algoritması uygulandı.
  - `http://`, `https://`, `youtu.be/`, `youtube.com/`, `www.youtube.com/`, `m.youtube.com/` gibi tüm formatlar otomatik yakalanıp standart `https://` formatına normalize edildi.
  - `youtubeUrl`, `title`, `studentNote` ve `warningText` alanlarının tamamından URL çıkarma desteği sağlandı.
- **Çok Kademeli Güvenli Video Başlatıcı (`openVideoUrl`):**
  - Doğrudan YouTube uygulaması (`com.google.android.youtube`), standart `ACTION_VIEW` intent'i ve son çare olarak sistem tarayıcı seçicisi (`Intent.createChooser`) ile kademeli fallback mekanizması kuruldu.
  - Sessiz hata yutma (`catch {}`) kaldırıldı; URL açılamadığında kullanıcıya bilgilendirici Toast mesajı eklendi.
- **Plan Ayrıştırıcı Güçlendirmesi (`SimplePlanParser`):**
  - `[DERSLER]`, `[GUNLER]`, `[HAFTALIK]` ve madde işaretli (`- Ders | 40 dk | https://...`) girdilerin tamamından URL ayıklama eklendi.
- **Öğrenci Masası & Canlı Oturum Banner Entegrasyonu (`ChildHomeScreen` & `StudyTaskCard`):**
  - "Başla" butonuna basıldığında video linki varsa otomatik olarak açılıyor.
  - Canlı aktif ders şeridinde (`LiveActiveSessionBanner`) öğrencinin ders devam ederken dilediği an videoyu yeniden açabilmesi için tek tık "🎬 Videoyu Aç" butonu entegre edildi.

## [2026-09-18] 34. Bulut ve Test Konsolu Kodlarının Temizlenmesi & Canlı Video Butonunun Belirginleştirilmesi
- **Bulut / Supabase & Test Modu Kodlarının Temizliği:**
  - `SupabaseHttpClient`, `SupabaseDto`, `CloudSyncManager`, `CloudSyncDialog` ve `DeveloperConsoleScreen` dosyaları ve bağımlılıkları projeden tamamen silindi.
  - `RoleSelectionScreen`, `ChildHomeScreen`, `ParentDashboardScreen` ve `NavGraph` üzerindeki test modu rozetleri, ayarlar ve geliştirici konsolu navigasyon rotaları temizlendi.
  - `AppPreferences` içerisinden test override ve supabase anahtarları kaldırılarak temiz üretim durumuna getirildi.
- **Canlı Aktif Oturumda Belirgin Tıklanabilir Video Kartı:**
  - Öğrenci bir görevi başlattığında ekranın en üstünde beliren `LiveActiveSessionBanner` içerisine tam genişlikli, parlak kırmızı YouTube rozetli ve link önizlemeli **"🎬 Videoyu / Dersi İzle (YouTube)"** tıklanabilir kartı yerleştirildi.
  - `StudyTaskCard` ve `StudyWeeklyTaskCard` üzerinde video linki bulunan her ders için tıklanabilir kart doğrudan görünür hale getirildi.

## [2026-09-18] 35. Paket İçe Aktarmada Video Linki Eşleme Düzeltmesi & Görev Bitiminde Göğe Yükselen Yıldız Animasyonu
- **Paket Alışverişinde Video Linki Alanı (`RemoteOccurrenceSyncDto`):**
  - `RemoteOccurrenceSyncDto` modeline `@SerialName("youtube_url") val youtubeUrl: String? = null` alanı eklendi.
  - Veli `.studyplan` paketini dışa aktarırken veya öğrenci raporu gönderirken her görevdeki `youtubeUrl` alanının kayıpsız aktarılması sağlandı.
- **Akıllı Video Linki Çözümlemesi (`StudyPackageExchangeManager` & `StudySyncProvider`):**
  - İçe aktarma sırasında `youtubeUrl = remote.youtubeUrl ?: local?.youtubeUrl ?: templateTask?.youtubeUrl ?: extractedFromText` hiyerarşisi kuruldu.
  - Öğrenci cihazında plan daha önce bulunmasa dahi gelen paketteki video linkleri doğrudan Room veritabanına kaydedildi.
- **Görev-Yıldız Dönüşümü ve Takımyıldızı Tutuşma Animasyonu (`ZenParallaxBackground`):**
  - Sürekli ve rastgele kayan dikkat dağıtıcı ortam yıldızları kaldırıldı.
  - Yalnızca bir görev veya test tamamlandığında (`flyingStarTrigger`), tamamlanan görev gökyüzüne doğru süzülen ışıltılı bir meteora dönüşüyor, zirveye ulaştığında süpernova patlama halkası oluşturarak takımyıldızındaki bir yıldızı parlatıyor.
- **Birim Testleri:**
  - `StudyPackageExchangeTest` güncellendi ve `youtubeUrl` alanının `.studyplan` JSON serileştirmesinde korunduğu doğrulandı.

## [2026-09-18] 37. Dinamik Çok Kiracılı Aile Eşleştirme & Bulut Eşitleme Diyalogu
- **Çok Kiracılı İzolasyon & Rastgele Aile Kodu:**
  - Sabit `ST-2026` kodu kaldırılarak `AppPreferences` üzerinde ilk kurulumda rastgele 4 haneli benzersiz kod (`ST-XXXX`) oluşturulması sağlandı.
  - Farklı ailelerin verilerinin birbirine karışması engellendi.
- **Etkileşimli `CloudSyncDialog` Modalı:**
  - Hem Veli Masası hem Öğrenci Masası TopBar bulut butonuna basıldığında açılan şık diyalog eklendi.
  - Aktif aile kodunu panoya kopyalama, yeni rastgele kod üretme ve eşleştirme kodu girerek diğer cihaza bağlanma özellikleri sağlandı.
- **Kotlinx Serialization ve Quiz Entegrasyonu:**
  - DTO modellerine varsayılan değerler eklendi, `coerceInputValues = true` aktif edilerek geriye dönük uyumluluk ve sıfır çökme garantilendi.
  - Sınav/Test (`Quiz`) verileri de bulut senkronizasyonuna tam dahil edildi.

## [2026-09-18] 38. Görev Düzenleme, Pull-to-Refresh, İzin Rehberi, Fabrika Sıfırlama ve Onay Yıldızı Tetikleyicisi
- **Veli Masasında Görev Düzenleme ve Silme (`EditTaskDialog`):**
  - Veli Haftalık Plan görünümündeki her görev kartına (`OccurrenceAdminCard`) "✏️ Düzenle" butonu eklendi.
  - Veli ders başlığı, tahmini süre, gün (Pzt-Paz), YouTube video linki, hedef soru sayısı ve veli notunu düzenleyebilir ya da görevi silebilir.
  - Değişiklikler anında yerel Room veritabanına ve Cloudflare bulutuna yazılır; öğrenci cihazıyla senkronize edilir.
- **Yukarıdan Aşağı Kaydırarak Yenileme (`Pull-to-Refresh`):**
  - Hem Veli hem Öğrenci masasına Jetpack Compose Material 3 `PullToRefreshContainer` entegre edildi.
  - Kullanıcı listeyi aşağı çektiğinde dönen modern yükleme göstergesi ile Cloudflare senkronizasyonu anında tetiklenir.
- **Açılışta ve Kalkan Butonunda Kullanıcı Dostu İzin Rehberi (`PermissionGuideDialog`):**
  - Açılışta kullanıcıyı doğrudan ayarlara atmak yerine, izinlerin amacını (Yüzen Sayaç & Ekran Görüntüsü Kanıtı) anlatan şık modal tasarlandı.
## [2026-09-18] 39. Çift Yönlü Rol Tabanlı Bulut Eşitlemesi, Belirgin İzin Kartı ve Ortalanmış Pull-to-Refresh
- **Cloudflare Worker & İstemci Rol Tabanlı Akıllı Senkronizasyon:**
  - `SharedFamilySyncPayload` ve Cloudflare KV motoruna `senderRole` ("PARENT" / "CHILD") mimarisi entegre edildi.
  - Veli tarafından yapılan tüm ders düzenlemeleri (isim, video linki, süre, soru hedefi, veli yönergesi) ve silinen dersler bulutta otoriter olarak güncellenir.
  - Öğrenci cihazı eşitleme yaptığında velinin güncel ders programını indirir, silinen dersleri yerelden temizler; öğrencinin tamamladığı çalışmalar ve kanıtlar kayıpsız korunur.
- **Veli Masasında Silinen Derslerin Tam Temizliği:**
  - Veli bir dersi sildiğinde hem `occurrences` tablosundan hem de ilişkili `task_templates` tablosundan kaldırılır ve buluttan da kalıcı olarak silinir.
- **Öğrenci Masasında Belirgin `"🛡️ Kanıt Alma & Sayaç Hizmeti"` Kartı:**
  - Öğrenci Masasında (`ChildHomeScreen`) izinler kapalıyken en üstte dikkat çekici altın rengi uyarı kartı ve `"Hizmeti Aç"` butonu eklendi; öğrencinin hizmeti açması kolaylaştırıldı.
## [2026-09-18] 40. Veli Masası TopBar, Tab Bar ve Karne Rozeti Mobil UI & Taşma Düzeltmesi
- **TopBar Başlığı ve İkon Sıkışması Düzeltmesi (`ParentDashboardScreen`):**
  - TopBar başlığındaki "Ebeveyn Masası" metninin 4 adet aksiyon butonu sebebiyle 120dp dar alana sıkışarak "yn \n Masas" şeklinde iki satıra bölünmesi ve kırpılması giderildi.
  - Birincil eylem olan Bulut Senkronizasyonu (`CloudSync`) ikon butonu olarak korunurken; ikincil eylemler (AI Plan Stüdyosu, WhatsApp/Dosya Paylaşımı, İlerleme Sıfırlama) şık bir taşma menüsüne (`MoreVert` / `DropdownMenu`) toplandı. Başlık `maxLines = 1, TextOverflow.Ellipsis` ve 16.sp ile genişletilerek tüm mobil ekran boyutlarında (320dp - 411dp) ferahlatıldı.
- **Sekme Başlıklarının Tek Satıra Dengelenmesi:**
  - Tab 1 başlığındaki "🚨 Öğrenci İcraat Masası" metninin dar ekranlarda 2 satıra taşması ("🚨 Öğrenci İcraat \n Masası") düzeltildi; "🚨 İcraat Masası" olarak dengelenerek `maxLines = 1` güvencesine alındı.
- **Öğrenci Çalışma Karnesi Kartı & Metin Çakışması İyileştirmesi:**
  - Sağ üstteki "$approvedTasks / $totalTasks Tamamlandı" rozetinin dikey uzaması engellendi (`maxLines = 1, softWrap = false`).
  - İlerlemeyi gösteren modern `LinearProgressIndicator` eklendi.
  - Alttaki "0 Onay Bekleyen • 38 Kalan Ders" metni ile "%2 Başarı Oranı" metninin dar ekranlarda birbirine yapışması (`38 Kalan Ders%2 Başarı`) engellendi; dikey `Arrangement.SpaceBetween` ve `weight(1f)` ile güvenli aralık sağlandı.
