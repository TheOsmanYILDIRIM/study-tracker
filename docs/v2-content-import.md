# StudyTracker V2 Content Ingestion, Seed Management & V1 Migration Guide

This document outlines the content ingestion pipeline, authoritative source rules, puzzle ordering workflows, versioning mechanisms, and safe V1->V2 migration procedures in StudyTracker V2.

---

## 1. Authoritative Content Sources & Precedence

All 9th-grade educational curricula in StudyTracker V2 are sourced directly from the user's Obsidian Vault (`10-Projects/` directory):

| File | Subject / Role | Precedence Rule |
|---|---|---|
| `1_ay_tarih_video_rehberi.md` | 9. Sınıf Tarih | **Kanonik Kaynak**: Mehmet Celal ÖZYILDIZ (*Benim Hocam*). Çakışan eski şablonlar elenir. |
| `1_ay_matematik_khan_academy_videolari.md` | 9. Sınıf Matematik | **Kanonik Kaynak**: Khan Academy Türkçe videoları ve MEB kazanımları. |
| `1_ay_fizik_video_rehberi.md` | 9. Sınıf Fizik | VIP Fizik & Özcan Aykın transkript dökümleri. |
| `1_ay_kimya_video_rehberi.md` | 9. Sınıf Kimya | Görkem Şahin / Benim Hocam Lise video kılavuzu. |
| `1_ay_biyoloji_video_rehberi.md` | 9. Sınıf Biyoloji | Khan Academy & Dr. Biyoloji kılavuzu. |
| `1_ay_cografya_video_rehberi.md` | 9. Sınıf Coğrafya | Coğrafyanın Kodları TYT kamp transkriptleri. |
| `9_sinif_cografya_1_ay_plani.md` | 9. Sınıf Coğrafya | 24 kartlık Anki deste entegrasyonu (`9_sinif_cografya_1_ay.apkg`). |
| `1_ay_ingilizce_kelime_listesi.md` | 9. Sınıf İngilizce | 150 kelimelik Anki deste entegrasyonu (`9_sinif_ingilizce_1_ay.apkg`). |
| `1_ay_almanca_kelime_listesi.md` | 9. Sınıf Almanca | 120 kelimelik Anki deste entegrasyonu (`9_sinif_almanca_1_ay.apkg`). |
| `9_sinif_4_haftalik_studytracker_calisma_plani.md` | Genel Çalışma Planı | İkincil kaynak (Branş rehberleriyle çakışma durumunda branş rehberi önceliklidir). |

### 1.1 Temel Denetim ve Doğruluk Kuralları
- **Tarih Kanonik Öğretmen Kuralı:** Tarih dersi için `Mehmet Celal ÖZYILDIZ` videoları (`5QxOpTALmEE`, `lqEZ19uwyas`) kanonik kabul edilir. Eski/farklı öğretmen linkleri aktif tohumdan çıkarılır.
- **Yinelenen Video Tespiti (Anti-Repeat):** Eski planlarda haftalar boyunca tekrarlanan sahte video URL'leri ayıklanmış, gerçek içerik eşleşmeleri kullanılmıştır.
- **Sıfır Uydurma İçerik (No Hallucinated Content):** Soru metni bulunmayan testler için öğrenciye sahte soru uydurulmaz; yalnızca kaynak destekli öğeler aktiftir.
- **URL Doğruluğu:** Kanal ana sayfası (`@...`) veya arama sonuçları (`results?search_query=...`) içeren bağlantılar `reviewStatus: "needs_review"` olarak etiketlenir.

---

## 2. İçerik Güncelleme: Öğrenci İlerlemesini Kaybetmeden Video Değiştirme

StudyTracker V2'de **Learning Item ID'leri ve Stable Key'leri değişmezdir (immutable)**. Bir videonun linki güncellendiğinde veya HD sürümü çıktığında:

1. `item.id` ve `item.stableKey` aynı kalır.
2. Yeni bir `learning_item_versions` kaydı oluşturulur (`version_number: 2`).
3. `item.currentVersionId` yeni sürüme işaret eder.
4. **Öğrencinin önceki girişimleri (Attempts)**, tamamladıkları eski versiyon ID'sine (`version_id`) bağlı kalmaya devam eder. Hiçbir ilerleme veya skor silinmez.

```bash
# CLI ile İçerik Sürümünü Güncelleme:
studytracker-cli v2 item update-content item_mat9_vid_uslu_giris \
  --title "Üslü Sayılara Giriş (2026 HD)" \
  --url "https://www.youtube.com/watch?v=kYqP9K0Y0pU" \
  --changelog "2026 Maarif Modeli HD Yenileme"
```

---

## 3. Puzzle Sıralama: Araya "Quiz 17.2" Ekleme

V2'de öğelerin sıralaması ID veya isimle değil, kayan noktalı `order_key` ile belirlenir (`1000.0, 2000.0, 3000.0...`).

Mevcut `Quiz 17` (`orderKey: 2000.0`) ile `Video 18` (`orderKey: 3000.0`) arasına yeni bir test eklemek için:

```bash
studytracker-cli v2 item insert-after item_mat9_quiz17 \
  --lesson lesson_gercek_sayilar \
  --type QUIZ \
  --label "Quiz 17.2" \
  --title "Gerçek Sayılar Pekiştirme Testi"
```

Bu işlem yeni öğeye otomatik olarak `orderKey: 2500.0` atar. Var olan hiçbir öğenin ID'si veya sırası bozulmaz.

---

## 4. Tohum Kataloğu Yönetimi (Seed Validate, Diff & Apply)

### 4.1 Tohum Doğrulama (`seed validate`)
Tohum manifestosunun şema geçerliliğini, ID tekilliğini ve denetim kurallarını kontrol eder:
```bash
studytracker-cli v2 seed validate --file content/9-sinif-v2-catalog.json --json
```

### 4.2 Fark Raporu (`seed diff`)
Yerel tohum dosyası ile sunucudaki aktif katalog arasındaki farkları hesaplar:
```bash
studytracker-cli v2 seed diff --json
```

### 4.3 Tohumu Uygulama (`seed apply`)
Tohumu sunucuya güvenli ve idempotent biçimde uygular:
```bash
# Simülasyon modu (Değişiklik yapmaz):
studytracker-cli v2 seed apply --dry-run --json

# Canlı uygulama:
studytracker-cli v2 seed apply --json
```

### 4.4 Manuel İnceleme Koruması (Reviewed Overrides Preservation)
`studytracker-cli v2 review approve` veya `replace-content` ile manuel olarak incelenmiş ve onaylanmış öğeler (`provenance.reviewedOverride === true` veya sürüm > 1), sonraki tohum uygulamalarında (`seed apply`) **sessizce ezilmez**. `seed diff` bu öğeleri `reviewedOverridesPreserved` olarak listeler ve yerel varsayılanlarla üzerine yazılmasını engeller.

---

## 5. İçerik İnceleme & Yayın Durumu Semantiği (Review & Publishing)

Detaylı inceleme iş akışları ve komutlar için [v2-content-review.md](file:///data/data/com.termux/files/home/projects/study-tracker/docs/v2-content-review.md) belgesine bakınız.

- **`active`**: Öğrenci kataloğunda görünür, MEB kazanımlarına uygun doğrulanmış içerik.
- **`draft`**: İnceleme bekleyen, tamamlanmamış veya belirsiz bağlantılar; öğrenci arayüzünde gizlenir.
- **`archived`**: Yayından kaldırılmış içerik; öğrencinin önceki tamamlama geçmişi (Attempts) korunur.

---

## 6. V1 -> V2 Güvenli Veri Geçişi (Migration Engine)

Legacy V1 haftalık planlarındaki tamamlanmış çalışmalar, V2 sistemine append-only `Attempt` olarak aktarılır.

### 6.1 Güven Seviyeleri & Eşleme Önceliği
1. **`exact`**: Birebir YouTube Video ID veya Stable Key eşleşmesi.
2. **`high`**: Branş ve ders başlığı token örtüşmesi (> %75).
3. **`medium`**: Kısmi başlık örtüşmesi (%50 - %75) -> **Otomatik uygulanmaz**.
4. **`unmatched`**: Eşleşmeyen serbest veya kaldırılmış dersler -> **Otomatik uygulanmaz**.

### 6.2 Geçiş İş Akışı

```bash
# 1. Analiz Çıkar:
studytracker-cli v2 migrate-v1 analyze --json

# 2. Güvenli Geçiş Planı Üret:
studytracker-cli v2 migrate-v1 plan --output /tmp/migration-plan.json --json

# 3. Planı Simüle Et:
studytracker-cli v2 migrate-v1 apply --plan /tmp/migration-plan.json --dry-run --json

# 4. Planı Canlı Uygula:
studytracker-cli v2 migrate-v1 apply --plan /tmp/migration-plan.json --json
```

### 6.3 Güvenlik Garantileri
- **Deterministik `clientAttemptId` (`mig_v1_${legacyId}`):** İkinci kez çalıştırıldığında sunucu yinelenen kaydı tanır ve mükerrer kayıt oluşturmaz.
- **Kayıpsız Geçiş:** V1 veritabanından hiçbir kayıt silinmez.
- **Veli İncelemesi Bağımsızlığı:** Veli onayları öğrencinin bitirme durumunu engellemez; metadata olarak korunur.
