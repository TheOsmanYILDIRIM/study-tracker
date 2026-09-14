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
- [WIP / Yapılıyor: Rol Seçici, Çocuk İnteraktif Demo Tutorial, Çocuk Görev Masası, Ebeveyn Dashboard, Kanıt İnceleme, AI Plan Stüdyosu ve Geliştirici Konsolu]
- [ ] Rol Seçici & PIN Gate (`RoleSelectionScreen`)
- [ ] Çocuk İnteraktif Rehber & Demo (`ChildTutorialScreen`)
- [ ] Çocuk Görev Masası (`ChildHomeScreen`)
- [ ] Ebeveyn Dashboard & Onay Kuyruğu (`ParentDashboardScreen`)
- [ ] Kanıt İnceleme & Screenshot Timeline (`SessionReviewScreen`)
- [ ] AI Plan Stüdyosu (`AIPlanStudioScreen`)
- [ ] Geliştirici & Test Modu Konsolu (`DeveloperConsoleScreen`)

