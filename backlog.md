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

## 8. Sessiz Gerçek Ekran Yakalama (StudyAccessibilityService Driver)
- [x] `AccessibilityService.takeScreenshot()` Tabanlı Sessiz Arka Plan Ekran Yakalama Sürücüsü (`AccessibilityCaptureDriver`), Sıfır Sistem Uyarısı & Tek Seferlik Ayar Mimarisi
