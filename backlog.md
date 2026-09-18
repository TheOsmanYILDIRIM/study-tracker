## 1. Android Proje İskeleti & CI/CD Kurulumu
- [x] Android Studio Gradle KTS, Versiyon Kataloğu, Manifest ve GitHub Actions CI/CD Altyapısı

## 2. Core Katmanı (Domain & Data)
- [x] Domain modelleri (`TaskTemplate`, `Occurrence`, `Session`, `Screenshot`, `Review`, `Plan`)
- [x] Local Room veritabanı entity'leri, DAO'lar ve TypeConverters
- [x] Plan doğrulayıcı (`PlanValidator`) ve Revize/Yeni Hafta Kayıpsız Plan Merge Motoru (`PlanMergeEngine`)
- [x] Gelişmiş AI Prompt Üreticisi (`AIPromptBuilder`)
- [x] Yerel depolama ve sahte ekran görüntüsü sürücüsü (`FakeCaptureDriver`)

## 3. Overlay & Oturum Yönetimi
- [x] SessionStateManager, FloatingButtonService ve Jetpack Compose Floating HUD (Tek tık screenshot, 2sn dairesel bitirme)
- [x] FloatingButtonService (SYSTEM_ALERT_WINDOW ile arka planda çalışma)
- [x] Floating HUD Compose UI ve dokunma/manyetik yapışma etkileşimi

## 4. Kullanıcı Arayüzü (Jetpack Compose Ekranları)
- [x] Rol Seçici & PIN Gate (`RoleSelectionScreen`)
- [x] Çocuk İnteraktif Rehber & Demo (`ChildTutorialScreen`)
- [x] Çocuk Görev Masası (`ChildHomeScreen`)
- [x] Ebeveyn Dashboard & Onay Kuyruğu (`ParentDashboardScreen`)
- [x] Kanıt İnceleme & Screenshot Timeline (`SessionReviewScreen`)
- [x] AI Plan Stüdyosu (`AIPlanStudioScreen`)
- [x] Geliştirici & Test Modu Konsolu (`DeveloperConsoleScreen`)

## 5. Yeni İyileştirmeler & Kullanıcı İstekleri
- [x] Ebeveyn Modunda Haftalık ve Günlük Plan İnceleme Görünümü (`ParentDashboardScreen` Sekmeli Yapı & Gün Filtreleri)
- [x] Öğrenci Modunda Ders Çalışma Oturumunu Duraklatma / Devam Etme (Pause/Resume HUD & Canlı Kontrol Kartı)
- [x] Plan Stüdyosunda Takvim Tabanlı Hafta Seçici Bileşeni (`WeekCalendarPicker`)
- [x] JSON Yerine Kolay Bozulmayan Basit Değişken/DSL Plan Formatı (`SimplePlanParser` & `SimplePlanExporter`)
- [x] Ultra Hata Toleranslı Plan Ayrıştırma (Preamble/Prompt Temizliği, Çoklu Blok ve Yinelenen Anahtar Otomatik Tekilleştirme / Auto-Healing)
- [x] Sabit Kalıcı Keystore İmzalama (`antigravity.keystore` ile üst üste güncelleme) & Modern Adaptive Uygulama İkonu (`ic_launcher` / `ic_launcher_round`)

## 6. Performans & FPS Optimizasyonu
- [x] Jetpack Compose Recomposition Yalıtımı, Flow Hatırlama, Async Resim Yükleme ve Room İndeksleme

## 7. Test Modu & Güncel Rehber (Tutorial) Yönetimi
- [x] Test Modunu Açma/Kapama Ayarı, Tek Seferlik ve İsteğe Bağlı Yeniden Açılabilen Güncel Tutorial

## 9. Kapsamlı FPS ve Sıfır Gecikmeli Dokunma Performans Optimizasyonu
- [x] Kapsamlı FPS ve Sıfır Gecikmeli Dokunma Performans Optimizasyonu (Ticker Recomposition İzolasyonu, @Immutable Modeller, Async Optimistic Dokunma Yanıtı, Room Flow Distinct Desteği, Shape & Modifier Sabitleme)

## 10. Zomo File / Pinterest Modern Tasarım Sistemi Dönüşümü
- [x] Pinterest Zomo File Tasarım Sistemi (Soft Lavender `#F5F2FB` Arka Plan, Canlı Violet/Purple Gradient Hero Kartları, Neon Mint `#2DD4BF` Pill CTA Butonlar, Pastel Squircle `16dp` Kategori Rozetleri, `24-28dp` Derin Yuvarlatılmış Kartlar ve Zarif Tipografi)

## 11. Cyber-Violet Dark Futuristic Tasarım Sistemi & Ticker İzolasyon Performans Optimizasyonu
- [x] Derin Cyber-Violet Koyu Mor Arka Plan (`#090414`), Koyu Cam Kartlar (`#140C28`), 32dp Hero Gradyanı, Neon Mint (`#00F5D4`) Pill Butonlar, Ticker Saniye Akışının Bağımsız StateFlow'a Ayrıştırılması ve 120 FPS Donanım Hızlandırmalı Kenarlık Optimizasyonu

## 18. Tek Arkaplan Resmi ile Canlı Parlaklık Artışı, Gökyüzüne Uçan Yıldız ve Test Modu Önizleme Sliderı
- [x] Tek Parça Yüksek Kaliteli Zen Masal Gece Görseline Geçiş (`bg_zen_night.webp`)
- [x] Görev İlerlemesiyle Dinamik Renk Doygunluğu (ColorMatrix saturation 0.85 -> 1.30) ve Parlaklık Fullenmesi
- [x] Görev Bitince Görev Kartından Gökyüzüne Parabolik Uçan Altın Kuyruklu Yıldız (`FlyingComet`) ve Patlama Halkası (`StarBurstRing`)
- [x] Test Modunda ve Geliştirici Konsolunda Anlık Canlı Parlaklık/Fullenme Slider'ı (`0% - 100%`) ve Hızlı Test Uçuşu Tetikleyicisi

## 19. Tek Cihazda Çift APK (Flavors) & Supabase Gerçek Zamanlı Bulut Senkronizasyonu
- [x] Gradle Product Flavors ile İki Bağımsız APK (`com.studytracker.child` & `com.studytracker.parent`)
- [x] Tek Cihaza Yan Yana Kurulum, Ayrı Uygulama Başlıkları ve Role Özel Doğrudan Başlangıç Ekranları
- [x] Supabase REST, Storage & Realtime İstemcisi (`SupabaseHttpClient`, `SupabaseDto`)
- [x] 6 Haneli Aile Eşleşme Kodu (`ST-XXXX`) ile Cihaz Eşleştirme ve Çift Yönlü Senkronizasyon (`CloudSyncManager`, `CloudSyncDialog`)
- [x] Oturum Tamamlama ve Kanıt Onay/Reddetme Süreçlerine Otomatik Arka Plan Bulut Eşitlemesi

## 20. Çift Yönlü Sağlam Senkronizasyon & Otomatik Eşitleme Düzeltmesi
- [x] Çift Yönlü Sağlam Senkronizasyon Motoru & Otomatik Eşitleme Düzeltmesi (İki yönlü durum mutabakatı, çoklu konumlu paylaşılan dosya köprüsü, varsayılan ST-2026 aile kodu, ekran açılışı & plan aktarımında otomatik tetikleme)

## 21. Android ContentProvider IPC & Gerçek Zamanlı Bulut Senkronizasyon Altyapısı
- [x] Android `StudySyncProvider` (Binder IPC) ile tek cihazda sıfır gecikmeli APK'lar arası doğrudan senkronizasyon (`content://com.studytracker.child.syncprovider` & `content://com.studytracker.parent.syncprovider`)
- [x] Farklı fiziksel cihazlar ve internet üzerinden test için Supabase bulut veri şeması (`occurrences`, `sessions`, `reviews`, `plans`, `tasks`, `screenshots`) ve dinamik ayar desteği
- [x] CI/CD ile kalıcı imzalı Release APK'ların üretimi ve `/sdcard/Download/` dizinine aktarılması

## 22. Sıfır Kurulum & Girişsiz İnternet Bulut Rölesi (Zero-Config Cloud Relay)
- [x] Hesap açma, API key ve SQL kurulumu gerektirmeyen hazır küresel bulut rölesi (`ntfy.sh` / `studytracker_relay_${familyCode}`)
- [x] Çift yönlü otomatik senkronizasyon (Binder IPC + Cloud Relay + Yerel Disk Köprüsü üçlü koruma)

## 24. Kanıt Görselleri Senkronizasyonu & Ebeveyn Reddet/Onayla Anında Geri Bildirim ve Kalıcı Senkronizasyon
- [x] Ebeveyn Onayla/Reddet butonlarında anında Toast ve geri dönüş, tekil Session ID ile kanıtların eşleşmesi, Binder IPC + Paylaşılan Dosya Köprüsü + Bulut Rölesi çok katmanlı kanıt aktarımı

## 25. Aşırı Basit & Sıfır Hata Paylı İki Yönlü Senkronizasyon Mimarisi
- [x] Karmaşık, yavaş ve zaman aşımına uğrayan çok katmanlı yapı yerine tek ve kesin çalışan 0-gecikmeli ContentProvider Binder IPC eşitleme köprüsü
- [x] `StudySyncProvider` içinde `sync` ve `openFile` metotları ile canlı veritabanı ve kanıt görsellerinin kesintisiz, anında aktarımı
- [x] Ağ kesintilerinden ve zaman aşımlarından etkilenmeyen, 3 saniye zaman aşımlı bağımsız ve arka planda çalışan bulut yedekleme sistemi
- [x] Farklı cihazlar için tek dokunuşla Android sistem paylaşım menüsü (WhatsApp/SMS/QuickShare/Nearby), panoya kopyalama ve içe aktarma mekanizması

## 26. Özel `.studyplan` Dosya Formatı, Otomatik İçe Aktarma, Lossy WebP Kanıt Sıkıştırması ve Secere Takibi
- [x] Özel `.studyplan` dosya uzantısı, MIME tipi (`application/vnd.studytracker.plan`) ve Android Intent Filter (`VIEW` / `SEND`) ile WhatsApp/Telegram'dan tek tıkla otomatik içe aktarma
- [x] Android `FileProvider` ile güvenli dosya paylaşımı ve Android sistem Paylaşım Sayfası (Share Sheet) entegrasyonu
- [x] Ekran görüntülerinin 8-15 KB'a düşürülmesi için donanım destekli Lossy WebP (`Bitmap.CompressFormat.WEBP_LOSSY`) sıkıştırması
- [x] Öğrenciden veliye gelen günlük raporda tamamlanan derslerin, sürelerin, onay bekleyen oturumların ve kanıtların seceresinin eksiksiz tutulması
- [x] Veliden öğrenciye gelen revize planlarda `PlanMergeEngine` ile öğrencinin önceden yaptığı aynı görevleri, soru sayılarını ve onayları kaybetmeden akıllı güncelleme yapabilmesi

## 27. WhatsApp "Birlikte Aç" Desteği & Büyük Belirgin Paylaşım Kartları
- [x] WhatsApp'ın içerik sağlayıcı (ContentProvider) üzerinden aktardığı dosyalarda `pathSuffix=".studyplan"`, `pathPattern=".*\\.studyplan"`, `mimeType="*/*"`, `application/octet-stream`, `application/json`, `text/plain` ve `ACTION_SEND` intent filter genişletmesi
- [x] Ebeveyn Masası (`ParentDashboardScreen`) üzerinde belirgin "📦 WhatsApp & Dosya Köprüsü" kartı ve 42dp yüksekliğinde "Planı Gönder" / "Rapor Yükle" butonları
- [x] Öğrenci Masası (`ChildHomeScreen`) üzerinde belirgin "📦 Günlük Rapor & Kanıt Paketi" kartı ve tam genişlikli "Raporu WhatsApp / Dosya İle Gönder" butonu
- [x] Hem dosya URI (`content://`) hem de doğrudan JSON metin yüklemesini destekleyen `MainActivity` intent işleyicisi

## 28. Veli İcraat Masası, Öğrenci Başarıları Karnesi ve İlerleme Sıfırlama Özelliği
- [x] Veli uygulamasında öğrencinin yaptığı icraatları, başarıları ve kanıtları öne çıkaran "🎓 Öğrenci Çalışma Karnesi", "🚨 Öğrencinin Onay Bekleyen Oturumları" ve "🏆 Öğrencinin Başarıları & Onaylanan Dersler" görünümleri
- [x] Veli haftalık plan sekmesinde görev durumlarının öğrenci perspektifli net etiketlerle sunulması (`✅ Öğrenci Yaptı & Onaylandı`, `⏳ Öğrenci Tamamladı (Onay Bekliyor)`, `⚡ Öğrenci Şu An Çalışıyor`, `⚪ Öğrenci Henüz Yapmadı`) ve reddedilen derslerde açıklama notu uyarısı
- [x] Veli için hızlı tek tıkla "❌ Not Bırak & Reddet" diyalogu (öğrenciye doğrudan açıklama notu gönderme)
- [x] Hem Veli hem Öğrenci uygulaması TopBar'ında "🔄 İlerlemeyi Sıfırla" butonu ve onay diyalogu (haftalık plan şablonunu koruyarak tüm tamamlanma kayıtlarını, oturumları ve kanıtları temizleme)

## 29. Öğrenci Görev Tamamlama Notu (Reflection & Soru Sayısı / Net Bildirimi)
- [x] Öğrenci dersi bitirdiğinde ("Bitir" butonuna bastığında) isteğe bağlı çalışma notu, çözülen soru sayısı, net veya anladığı konuları yazabileceği şık diyalog (`showFinishNoteDialog`)
- [x] Room veritabanında `studentNote` alanının `OccurrenceEntity` ve `SessionEntity` tablolarına eklenmesi ve DB versiyonunun 2'ye yükseltilmesi
- [x] Öğrenci kartlarında (`StudyTaskCard`) öğrencinin yazdığı notun "📝 Notum: [not]" rozetiyle gösterilmesi
- [x] Veli Masasında (`ParentDashboardScreen`) onay bekleyen oturumlarda, onaylanan dersler listesinde ve `OccurrenceAdminCard` bileşenlerinde öğrenci notunun belirgin vurgulanması
- [x] Veli Kanıt İnceleme ekranında (`SessionReviewScreen`) oturum detayları kartında öğrenci notunun özel neon kutuyla gösterilmesi
- [x] `.studyplan` paket alışverişi (`StudyPackageExchangeManager`), ContentProvider Binder IPC (`StudySyncProvider`) ve Supabase Cloud Sync (`RemoteOccurrenceSyncDto`) veri köprülerine `studentNote` alanının eklenmesi

## 30. LaTeX & Matematik Formül Destekli Şıklı Test & Sınav Değerlendirme Sistemi
- [x] `Quiz`, `QuizQuestion`, `QuizOption` domain modelleri, `QuizEntity`, Room `QuizDao`, `LocalQuizRepositoryImpl` ve DB v3 yükseltmesi
- [x] AI modelleri (ChatGPT/Claude/Gemini) için kolay ve bozulmaz test DSL formatı (`=== TEST: ... ===`, `[SORU 1]`, `A) ...`, `DOGRU: ...`, `COZUM: ...`) ve `SimpleQuizParser`
- [x] Veli AI Test & Soru Stüdyosu (`AIQuizStudioDialog`): Ders/konu, soru sayısı ve seviyeye göre anında LaTeX formatlı AI istemi (prompt) üretme ve yapıştırma alanı
- [x] Zengin donanım destekli, koyu tema uyumlu LaTeX matematik formül render bileşeni (`LatexMathView` / KaTeX) ve formül bütünlüğünü koruyan akıllı grup/satır ayrıştırıcı (`formatLatexString`)
- [x] Öğrenci Test Çözme Ekranı (`ChildQuizScreen`): Şık seçimi, soru gezinti şeridi, sayaç ve testi veliye gönderme onay diyalogu (**öğrenci çözerken veya bitirince doğru cevapları ve puanı görmez**)
- [x] Veli Test İnceleme Ekranı (`ParentQuizReviewScreen`): Başarı yüzdesi (%X), Doğru/Yanlış/Boş metrikleri, soru bazında öğrencinin seçimi ile doğru cevabın karşılaştırılması ve LaTeX çözümler
- [x] `.studyplan` dosya paketi, Binder IPC (`StudySyncProvider`) ve Supabase bulut senkronizasyonuna testlerin ve öğrenci cevaplarının tam entegrasyonu

## 31. Görev İçi Test Bütünleştirmesi & Saf Android Paylaşım (Share Sheet) Mimarisi
- [x] Öğrenci ana ekranında (`ChildHomeScreen`) testlerin izole/ayrı bir kategori olarak gösterilmesi kaldırıldı; günlük dersler ve görevler (`📅 Bugünkü Dersler & Görevler`) akışına birebir aynı kart tasarımı ve rozetlerle gömüldü
- [x] Test tamamlandığında günlük ilerleme çubuğu, gece masalı arkaplan doygunluğu ve gökyüzüne uçan kuyruklu yıldız animasyonunun dinamik olarak tetiklenmesi
- [x] Sunucu / Supabase bağımlılıkları ve butonlarının arayüzden tamamen temizlenmesi; Veli ve Öğrenci arasındaki tüm veri aktarımının saf Android Paylaşım Sayfası (`.studyplan` Share Sheet - WhatsApp, Telegram, QuickShare, Dosya) üzerinden gerçekleştirilmesi

## 32. Ders ve Test Bitiminde İnteraktif Öğrenci Öz Değerlendirme & Veli Raporlama Sistemi
- [x] Ders bitirme akışında (`ChildHomeScreen`) interaktif öğrenci öz değerlendirme anketi: Anlama Seviyesi (🌟 Harika, 👍 İyi, 🤔 Zorlandım, ❌ Zayıf), Odaklanma Seviyesi (⚡ %100, 🎯 İyi, 📱 Dağıldı), Soru Sayısı (+10, +20, +30, +50) ve Serbest Öğrenci Notu
- [x] Test bitirme akışında (`ChildQuizScreen`) interaktif test değerlendirme anketi: Zorluk Seviyesi (🟢 Kolay, 🟡 Orta, 🔴 Zor), Güven/His Düzeyi (🌟 Çok İyi, 👍 Fena Değil, 🤔 Kararsız) ve Veliye Özel Test Notu
- [x] `Quiz` ve `QuizEntity` modellerine `studentNote` alanı entegrasyonu, Room DB v4 yükseltmesi
- [x] Veli Paneli (`ParentDashboardScreen`) ve Test İnceleme Ekranında (`ParentQuizReviewScreen`) öğrencinin ders ve test öz değerlendirme metriklerinin ("🎓 Öğrenci Öz Değerlendirmesi") renkli kartlarla sergilenmesi
- [x] `.studyplan` paket alışverişine (`StudyPackageExchangeManager`) ders ve test değerlendirme verilerinin tam dahil edilmesi

## 33. Görev Başlatıldığında Video Linklerinin Otomatik ve Sorunsuz Açılması (Video Launcher & Plan Parser Güçlendirmesi)
- [x] Görev Başlatıldığında Video Linklerinin Güvenilir Açılması, Android 11+ Package Visibility Desteği, Gelişmiş URL Temizleme ve Plan Ayrıştırma İyileştirmesi

## 34. Bulut ve Test Konsolu Kodlarının Temizlenmesi & Canlı Video Butonunun Belirginleştirilmesi
- [x] Bulut/Supabase ve Test Modu/Geliştirici Konsolunun Kod Tabanından Temizlenmesi, Ders Başlatıldığında Aktif Şeritte ve Görev Kartlarında Belirgin Tıklanabilir Video Linki Kartının Sunulması

## 35. Paket İçe Aktarmada Video Linki Eşleme Düzeltmesi & Görev Bitiminde Göğe Yükselen Yıldız Animasyonu
- [x] `.studyplan` paket alışverişinde `RemoteOccurrenceSyncDto` modeline `youtubeUrl` alanının eklenmesi, içe/dışa aktarmada eksiksiz Room DB eşlemesi ve akıllı video linki çözümü
- [x] Görev tamamlandığında göğe yükselerek takımyıldızını parlatan meteor & süpernova halkası animasyonunun tamamlanması
