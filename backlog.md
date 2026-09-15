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

## 16. Ayrıştırılmış Işık Katmanları ile Dinamik Parlama & Yanıp Sönme Efektleri
- [x] Orijinal Kağıt Kesim Görselinden Işıkların Ayrıştırılması (`bg_zen_lights_sky.webp`, `bg_zen_lights_forest.webp`, `bg_zen_lights_lake.webp`)
- [x] Gökyüzü Yıldızları, Çam Ormanı Ateşböcekleri ve Su Yansıması İçin Bağımsız Titreşim ve Yanıp Sönme (`ZenParallaxBackground.kt`)

