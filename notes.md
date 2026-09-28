# StudyTracker - Proje Notları & Mimari Özet (v3.0-Hardened)

- **Proje Adı:** StudyTracker
- **Mimari Yapı:** Veli (`com.studytracker.parent`) ve Öğrenci (`com.studytracker.child`) olmak üzere iki bağımsız APK flavor'ı, Cloudflare Worker/KV çok kiracılı bulut eşitleme motoru ve Node.js tabanlı CLI yönetim aracı.
- **Güvenlik & Kimlik:** `ST-XXXX-XXXX-XXXX-XXXX` yüksek entropili aile kodları, veli ve CLI işlemleri için `X-Admin-Token` başlığı, kısıtlı `StudySyncProvider` IPC ve güvenli dosya paylaşım sınırları.
- **Veri Tabanı & Şema:** Room Schema v6 (`completedQuestionCount`, `reportedQuestionCount`, `activeDurationSeconds`, `approvedCount`), kayıpsız revize plan birleştirme (`PlanMergeEngine`), 24 saatlik durum yedeği ve sıfırlama geri alma (`RESTORE`).
- **Kanıt & Oturum:** Gerçek erişilebilirlik ekran görüntüsü yakalama (`AccessibilityCaptureDriver`), yüzen buton kontrolü ve MEB uyumlu LaTeX matematik formüllü Quiz motoru.
- **Bağlantılı Belgeler:**
  - Mimari & Şartname: [SYSTEM_ARCHITECTURE.md](file:///data/data/com.termux/files/home/projects/study-tracker/SYSTEM_ARCHITECTURE.md)
  - UI & UX Planı: [UI_PLAN.md](file:///data/data/com.termux/files/home/projects/study-tracker/UI_PLAN.md)
  - CLI Dokümantasyonu: [cli/README.md](file:///data/data/com.termux/files/home/projects/study-tracker/cli/README.md)
