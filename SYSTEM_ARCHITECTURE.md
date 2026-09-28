# StudyTracker - Sistem Mimarisi ve Teknik Şartname (v3.0-Hardened)

Bu doküman, **StudyTracker** Android uygulamasının, Cloudflare Worker/KV senkronizasyon motorunun, CLI araçlarının ve veri modellerinin güncel sertleştirilmiş mimarisini tanımlar.

---

## 1. Temel Prensipler ve Güvenlik Modeli

1. **İki Ayrı Flavor & Rol Ayrımı:**
   - **Veli Uygulaması (`com.studytracker.parent`):** Plan oluşturma, ders silme/düzenleme, öğrenci oturumu onaylama/reddetme, test ekleme ve admin token yönetimi.
   - **Öğrenci Uygulaması (`com.studytracker.child`):** Günlük/haftalık dersleri görüntüleme, sayaç ve kanıt ile ders oturumu yürütme, test çözme ve veliye not/soru sayısı iletme.
2. **Yüksek Entropili Aile Kodu & Admin Token:**
   - Aile Kodu: `ST-XXXX-XXXX-XXXX-XXXX` formatında yüksek entropili benzersiz anahtar.
   - Admin Token: Veli ve CLI ayrıcalıklı işlemleri (plan yükleme, silme, iade, sıfırlama) `X-Admin-Token` başlığı gerektirir. Token öğrenci yanıtlarında sızdırılmaz.
3. **Sertleştirilmiş IPC & ContentProvider Güvenliği:**
   - `StudySyncProvider` signature permission ve katı caller package allowlist ile kısıtlıdır. Arbitrary file path erişimi engellenmiş, regex doğrulamalı kanıt okuma getirilmiştir.
   - `.studyplan` paketleri kullanıcı onayı, aile kodu eşleşmesi ve boyut sınırlarıyla korunur.
4. **Gerçek Kanıt & Sıfır Sentetik Fallback:**
   - Release derlemelerinde sahte/sentetik kanıt sürücüsü devre dışıdır; erişilebilirlik servisi ile gerçek ekran yakalama yapılır.

---

## 2. Sistem Mimarisi ve Bileşenleri

```text
┌─────────────────────────────────────────────────────────────┐
│                       StudyTracker Eko-Sistemi              │
└─────────────────────────────────────────────────────────────┘
                               │
       ┌───────────────────────┼────────────────────────┐
       ▼                       ▼                        ▼
┌───────────────┐      ┌───────────────┐       ┌─────────────────┐
│   Veli APK    │      │  Öğrenci APK  │       │ StudyTracker    │
│  (com.parent) │      │  (com.child)  │       │ CLI (Node.js)   │
└───────┬───────┘      └───────┬───────┘       └────────┬────────┘
        │                      │                        │
        │ X-Admin-Token        │ X-Family-Code          │ X-Admin-Token
        ▼                      ▼                        ▼
┌────────────────────────────────────────────────────────────────┐
│               Cloudflare Worker (studytracker-sync)            │
│               - Multi-tenant Sharded Sync Engine               │
│               - Eventual Consistency & Idempotent Commands     │
│               - Snapshot / Reset / 24h Restore Desteği         │
└──────────────────────────────┬─────────────────────────────────┘
                               │
                               ▼
┌────────────────────────────────────────────────────────────────┐
│                     Cloudflare KV Depolama                     │
│  - plan / tasks / occurrences / sessions / reviews / quizzes   │
└────────────────────────────────────────────────────────────────┘
```

---

## 3. Veri Modelleri (Room Schema v6)

### 3.1. Occurrence (Görev Örneği)
```kotlin
data class Occurrence(
    val occurrenceKey: String,        // "math_video:2026-09-28" veya "exam:2026-W38"
    val taskId: String,
    val type: TaskKind,               // DAILY, WEEKLY
    val date: String? = null,         // YYYY-MM-DD
    val weekId: String? = null,       // YYYY-Www
    val title: String,
    val plannedMinutes: Int,
    val youtubeUrl: String? = null,
    val reviewRequired: Boolean = true,
    val status: OccurrenceStatus = OccurrenceStatus.PENDING,
    val warning: Boolean = false,
    val warningText: String? = null,
    val rejectCount: Int = 0,
    val approvedCount: Int = 0,       // weekly onay sayacı
    val targetCount: Int? = null,
    val targetMinutes: Int? = null,
    val completedQuestionCount: Int = 0, // Yapısal tamamlanan soru sayısı (v6)
    val studentNote: String? = null
)
```

### 3.2. Session (Çalışma Oturumu)
```kotlin
data class Session(
    val sessionId: String,
    val occurrenceKey: String,
    val childId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val screenshotCount: Int = 0,
    val activeDurationSeconds: Long = 0L,   // Net çalışma süresi saniye (v5)
    val reportedQuestionCount: Int = 0,     // Öğrenci beyan soru sayısı (v6)
    val finalScreenshotUrl: String? = null,
    val studentNote: String? = null
)
```

### 3.3. Review & Quiz
- `Review`: Ebeveyn onay (`APPROVED`) veya iade (`REJECTED`) kararı, not ve puanı.
- `Quiz`: LaTeX formüllü MEB Maarif Modeli uyumlu testler, stable ID ve öğrenci cevap kayıtları.

---

## 4. Room Migration Zinciri (v1 -> v6)

- **v1 -> v2:** `studentNote` kolonları eklendi.
- **v2 -> v3:** `quizzes` tablosu oluşturuldu.
- **v3 -> v4:** `quizzes.studentNote` eklendi.
- **v4 -> v5:** `sessions.activeDurationSeconds` eklendi.
- **v5 -> v6:** `occurrences.completedQuestionCount` ve `sessions.reportedQuestionCount` eklendi.

---

## 5. Senkronizasyon & Paket Paylaşımı

1. **Cloudflare KV API:**
   - `GET /api/sync?code=...` & `GET /api/v2/sync`: Konsolide plan ve durum sorgulama.
   - `POST /api/sync`: Çift yönlü mutabakat, snapshot ve admin komutları (`PATCH_TASK`, `DELETE_TASK`, `RESET`, `WIPE`, `RESTORE`).
   - `POST /api/v2/reviews`: Veli inceleme kararlarının anlık otoriter işlenmesi.
2. **Çevrimdışı / Alternatif Paylaşım (`.studyplan`):**
   - WebP formatında sıkıştırılmış ekran görüntüleri ile ultra-küçük JSON paketi oluşturulur; WhatsApp veya dosya köprüsü ile taşınabilir.
