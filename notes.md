# StudyTracker - Proje Notları & Mimari Özet

- **Proje Adı:** StudyTracker
- **Amaç:** Tek Android uygulaması üzerinden ebeveyn ve çocuk modunda ders çalışma takibi, kanıt odaklı ekran görüntüsü denetimi, sistem üstü floating button oturum yönetimi ve AI haftalık plan JSON üretimi/içe aktarımı.
- **Kritik Kural:** AI runtime kararı vermez; plan güncellemesinde tamamlanmış görev durumları (`approved`) `occurrenceKey` bazlı korunur.
- **Teknoloji:** Kotlin, Jetpack Compose, Material 3, Room, WorkManager, MediaProjection API, System Alert Window Overlay.
- **Bağlantılı Belgeler:**
  - Mimari & Şartname: [SYSTEM_ARCHITECTURE.md](file:///data/data/com.termux/files/home/projects/study-tracker/SYSTEM_ARCHITECTURE.md)
  - UI & UX Planı: [UI_PLAN.md](file:///data/data/com.termux/files/home/projects/study-tracker/UI_PLAN.md)
