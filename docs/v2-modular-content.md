# StudyTracker V2 Modüler İçerik Mimarisi & AI Güvenli Düzenleme Rehberi

Bu doküman, StudyTracker V2 müfredat ve içerik yönetiminde kullanılan **modüler kaynak mimarisini (Modular Content Source of Truth)**, dosya ağacı hiyerarşisini ve bir AI ajanının ya da geliştiricinin tek bir öğeyi güvenle nasıl düzenleyeceğini açıklar.

---

## 1. Mimari Genel Bakış

Önceki monolitik `content/9-sinif-v2-catalog.json` dosyası (200+ KB) doğrudan elle veya AI tarafından düzenlenmesi riskli, birleştirme çakışmalarına (merge conflict) ve bağlam penceresi şişmesine açık bir yapıdaydı.

Yeni mimaride:
1. **Birincil Düzenlenebilir Kaynak:** `content/v2/` dizini altındaki atomik, modüler JSON dosyalarıdır.
2. **Derlenmiş Çıktı (Generated Artifact):** `content/9-sinif-v2-catalog.json` dosyası, `content/v2/` kaynağından deterministik olarak üretilen bir derleme çıktısıdır.
3. **Sıfır Veri Kaybı & Değişmezlik:** Tüm ders ID'leri, ünite ID'leri, öğe ID'leri, `stableKey`'ler, `orderKey`'ler ve yayınlama durumları (`publishingStatus`) %100 korunur.

---

## 2. Dizin ve Dosya Yapısı

```
content/
├── 9-sinif-v2-catalog.json            # DERLENMİŞ ÇIKTI (Generated Artifact)
├── 9-sinif-v2-catalog.sources.md      # Kaynak ve müfredat izlenebilirlik haritası
└── v2/                                # BİRİNCİL DÜZENLENEBİLİR KAYNAK
    ├── catalog.json                   # Üst düzey meta veriler + ders referansları
    ├── courses/                       # Ders meta verileri + ünite referansları
    │   ├── course_mat_9.json
    │   ├── course_fiz_9.json
    │   ├── course_kim_9.json
    │   ├── course_biyo_9.json
    │   ├── course_tar_9.json
    │   ├── course_cog_9.json
    │   ├── course_ing_9.json
    │   ├── course_alm_9.json
    │   └── course_tde_9.json
    ├── lessons/                       # Ünite/Konu meta verileri + öğe referansları
    │   ├── lesson_mat9_sayilar_uslu_koklu.json
    │   ├── lesson_tar9_gecmisin_insasi.json
    │   └── ... (35 ünite dosyası)
    ├── items/                         # Tek dosya = Tek atomik VIDEO / QUIZ / ANKI öğesi
    │   ├── item_mat9_vid_uslu_giris.json
    │   ├── item_mat9_vid_araliklar_gosterim__quiz.json
    │   ├── item_tar9_anki_temel.json
    │   └── ... (121 öğe dosyası)
    └── transcripts/                   # Transkript önbelleği (Gitignored)
        └── .gitkeep
```

---

## 3. Veri Modeli ve Şemalar

### A. `content/v2/catalog.json`
```json
{
  "schemaVersion": "v2",
  "generatedAt": "2026-10-02T12:00:00.000Z",
  "gradeLevel": "9. Sınıf",
  "academicYear": "2026-2027",
  "targetCurriculum": "MEB Türkiye Yüzyılı Maarif Modeli (9. Sınıf 2026-2027)",
  "courses": [
    "course_mat_9",
    "course_fiz_9",
    "course_kim_9",
    "course_biyo_9",
    "course_tar_9",
    "course_cog_9",
    "course_ing_9",
    "course_alm_9",
    "course_tde_9"
  ]
}
```

### B. `content/v2/courses/<courseId>.json`
```json
{
  "id": "course_mat_9",
  "title": "9. Sınıf Matematik",
  "subject": "Matematik",
  "gradeLevel": "9. Sınıf",
  "orderKey": 1000,
  "description": "MEB 9. Sınıf Matematik Müfredatı...",
  "lessons": [
    "lesson_mat9_kumeler_temel",
    "lesson_mat9_kumeler_islem",
    "lesson_mat9_sayilar_uslu_koklu"
  ]
}
```

### C. `content/v2/lessons/<lessonId>.json`
```json
{
  "id": "lesson_mat9_sayilar_uslu_koklu",
  "courseId": "course_mat_9",
  "stableKey": "mat9_sayilar_uslu_koklu",
  "title": "Sayılar: Üslü ve Köklü Gösterimler (MAT.9.1.1)",
  "orderKey": 3000,
  "items": [
    "item_mat9_vid_uslu_giris",
    "item_mat9_vid_uslu_kurallar",
    "item_mat9_quiz_uslu_koklu"
  ]
}
```

### D. `content/v2/items/<itemId>.json`
```json
{
  "id": "item_mat9_vid_araliklar_gosterim__quiz",
  "courseId": "course_mat_9",
  "lessonId": "lesson_mat9_sayilar_araliklar_kumeler",
  "stableKey": "mat9_quiz_araliklar_gosterim_micro",
  "itemType": "QUIZ",
  "displayLabel": "2.1-Q",
  "orderKey": 1500,
  "title": "Gerçek Sayı Aralıkları & Gösterim Mikro Testi",
  "contentUrl": null,
  "publishingStatus": "active",
  "payload": {
    "quiz": {
      "quizTitle": "Gerçek Sayı Aralıkları & Gösterim Mikro Testi",
      "questions": [
        {
          "id": "q_mat9_araliklar_1",
          "questionIndex": 0,
          "type": "MULTIPLE_CHOICE",
          "prompt": "Sayı doğrusunda 3 ile 7 arasındaki gerçek sayılardan 3 dahil, 7 dahil değilse bu aralık nasıl gösterilir?",
          "choices": ["[3, 7)", "(3, 7]", "(3, 7)", "[3, 7]"],
          "correctAnswer": "[3, 7)",
          "explanation": "Dahil olan sınır köşeli parantez, dahil olmayan normal parantezle gösterilir."
        }
      ],
      "questionCount": 1,
      "schemaVersion": "v2-quiz"
    },
    "provenance": {
      "derivedFromItemId": "item_mat9_vid_araliklar_gosterim",
      "sourceVideoUrl": "https://www.youtube.com/watch?v=TNw7eEas9Oo",
      "transcriptLanguage": "tr",
      "transcriptKind": "auto",
      "transcriptFingerprint": "d43ff753c7d5a6e2",
      "generatedBy": "gemini",
      "reviewStatus": "verified",
      "reviewedOverride": true,
      "importedAt": "2026-10-02T12:00:00.000Z",
      "schemaVersion": "v2"
    }
  }
}
```

---

## 4. AI İçin Güvenli Tek Öğe Düzenleme Protokolü

Bir AI ajanının kataloğu bozmadan tam olarak tek bir öğeyi düzenlemesi veya yeni bir öğe eklemesi için şu adımlar izlenir:

### Durum 1: Mevcut Bir Öğeyi Düzenleme (Örn: `item_mat9_vid_uslu_giris`)
1. **Yalnızca ilgili dosyayı açın:** `content/v2/items/item_mat9_vid_uslu_giris.json`.
2. **Değişikliği yapın:** Başlık, video URL veya payload alanını güncelleyin (`id` ve `stableKey` asla değiştirilmez!).
3. **Doğrulayın:**
   ```bash
   node cli/bin/studytracker-cli.js v2 modular validate
   ```
4. **Derleyin:**
   ```bash
   node scripts/compile-v2-catalog.cjs
   ```
5. **Testleri çalıştırın:**
   ```bash
   node cli/test-v2.js
   ```

### Durum 2: Yeni Bir Öğe Ekleme (Örn: Video ardına Mikro Quiz ekleme)
1. **Yeni öğe dosyasını oluşturun:** `content/v2/items/<yeni_id>.json`.
   - `id`, `courseId`, `lessonId`, `stableKey`, `itemType`, `displayLabel`, `orderKey`, `title`, `publishingStatus`, `payload` alanlarını tanımlayın.
2. **Ünite dosyasını güncelleyin:** `content/v2/lessons/<lessonId>.json`.
   - `items` dizisine `<yeni_id>` referansını istenen sıraya ekleyin.
3. **Doğrulayın ve Derleyin:**
   ```bash
   node cli/bin/studytracker-cli.js v2 modular validate
   node scripts/compile-v2-catalog.cjs
   ```

---

## 5. Doğrulama Motoru ve Güvenlik Kuralları

`validateModularTree` şu kontrolleri zorunlu kılar:
- **Asılı Referans Koruması (Dangling Refs):** `catalog.json` içindeki her ders `courses/` altında, `courses/` içindeki her ünite `lessons/` altında, `lessons/` içindeki her öğe `items/` altında fiziksel bir JSON dosyasına karşılık gelmelidir.
- **Ebeveyn Uyumsuzluğu Koruması (Wrong Parent Refs):** Bir öğe dosyasında belirtilen `lessonId` ve `courseId`, o öğeyi içeren ünite ve dersle birebir eşleşmelidir.
- **Benzersizlik (Unique IDs & StableKeys):** Tüm kurs, ders, ünite ve öğe ID'leri ile `stableKey`'ler global olarak tekil olmalıdır.
- **Quiz Şeması Doğrulaması:** QUIZ tipindeki tüm öğelerin `questions` dizisi, soru tipleri (`MULTIPLE_CHOICE`, `TRUE_FALSE`), şıkları ve `correctAnswer` doğruluğu otomatik kontrol edilir.
- **Sıralama Belirliliği (Deterministic Ordering):** Ünite içindeki tüm öğeler artan `orderKey` değerlerine sahip olmalıdır.

---

## 6. Transkript Tabanlı Mikro Testler (Provenance & Grounding)

Aktif YouTube videolarından `yt-dlp` ile çekilen Türkçe altyazılardan türetilen mikro testler şu meta verileri içerir:
- `derivedFromItemId`: Hangi video kaynağından türetildiği.
- `sourceVideoUrl`: Orijinal video adresi.
- `transcriptLanguage`: Altyazı dili (`tr`).
- `transcriptKind`: `manual` veya `auto`.
- `transcriptFingerprint`: Normalize edilmiş transkript metninin SHA-256 hash'i (ilk 16 karakter).
- `generatedBy`: Testi üreten model/sistem (`gemini`).
- `reviewStatus`: `verified` (transkript başarıyla alınıp grounded soru üretildiyse) veya `needs_review`.
- Altyazısı bulunamayan veya video formatı desteklenmeyen videolar için uydurma soru üretilmez, inceleme notu bırakılır.
