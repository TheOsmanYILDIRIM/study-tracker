# 9. Sınıf StudyTracker V2 Müfredat & İçerik Kaynakları Denetim Raporu

**Oluşturulma Tarihi:** 2026-09-30T19:00:00Z  
**Müfredat:** MEB Türkiye Yüzyılı Maarif Modeli (9. Sınıf / Lise 1)  
**Tohum Manifestosu:** [`content/9-sinif-v2-catalog.json`](file:///data/data/com.termux/files/home/projects/study-tracker/content/9-sinif-v2-catalog.json)

---

## 📊 Genel Özet & Metrikler

| Metrik | Adet |
|---|---|
| **Toplam Ders (Course)** | **9** |
| **Toplam Ünite / Konu (Lesson)** | **12** |
| **Toplam Öğrenme Öğesi (Learning Item)** | **38** |
| 🎬 **Video Sayısı (VIDEO)** | **34** |
| 📇 **Anki Destesi (ANKI)** | **4** |
| 📝 **Sınav / Test (QUIZ)** | **0** |
| 🟢 **Doğrulanmış (Verified)** | **22** |
| 🟡 **İnceleme Bekleyen (Needs Review)** | **16** |

---

## ⚖️ Temel İlke ve Kanonik Kurallar

1. **Tarih Dersi Kanonik Öğretmen Kuralı (Mehmet Celal ÖZYILDIZ):**
   - Tarih dersi için `10-Projects/1_ay_tarih_video_rehberi.md` dosyasındaki **Mehmet Celal ÖZYILDIZ** (*Benim Hocam*) videoları (`5QxOpTALmEE`, `lqEZ19uwyas`) kanonik kabul edilmiştir.
   - Eski/çakışan şablonlardaki (örn. Ramis Hoca veya 2025 şablonları) kaynaklar aktif tohumdan çıkarılmıştır.
2. **Değişebilir Güncel İçerikler:**
   - Matematik (Khan Academy), Fizik, Kimya, Biyoloji, Coğrafya, İngilizce ve Almanca kaynakları güncel içeriğe göre yapılandırılmış olup, gelecekte `update-content` ile yeni versiyonlar üretilebilir.
3. **Uydurma Veri Yasağı (No Fabricated Content):**
   - Kaynaklarda soru metni bulunmayan, sadece soru sayısı belirtilen başlıklar için sahte quiz sorusu **uydurulmamıştır**.
   - URL'si belirsiz veya kanal ana sayfası olan video eşleşmeleri `needs_review` olarak etiketlenmiştir.
4. **Anki Entegrasyonu:**
   - Yalnızca Vault içinde fiziksel karşılığı olan (`9_sinif_cografya_1_ay.apkg`, `9_sinif_ingilizce_1_ay.apkg`, `9_sinif_almanca_1_ay.apkg`, `9_sinif_tarih_anki.txt`) desteler kataloğa dahil edilmiştir.

---

## 🔍 Ders ve Kaynak Bazlı Denetim Detayları


### 📚 9. Sınıf Matematik (`course_mat_9`)
- **Branş:** Matematik | **Sıra:** 1000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Matematik Müfredatı (Sayılar Teması)
- **Üniteler:**

  - **Üslü Sayılar ve Temel Özellikleri (MAT.9.1.1)** (`lesson_mat9_uslu_sayilar`)
    - [1.1] (VIDEO) **Üslü Sayılara Giriş (1. Seviye)**
      - Stable Key: `mat9_vid_uslu_giris`
      - URL: https://www.youtube.com/watch?v=kYqP9K0Y0pU
      - Kaynak: `10-Projects/1_ay_matematik_khan_academy_videolari.md#L12` (1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1))
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `53b0d2ea576fbd09`
    - [1.2] (VIDEO) **Üslü İfadelerin Sadeleştirilmesi ve Çarpma**
      - Stable Key: `mat9_vid_uslu_kurallar`
      - URL: https://www.youtube.com/watch?v=s5Rz-1i0n18
      - Kaynak: `10-Projects/1_ay_matematik_khan_academy_videolari.md#L13` (1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1))
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `6ad350b6d1460777`
    - [1.3] (VIDEO) **Üslü Sayılarda Bölme İşlemi ve Özellikleri**
      - Stable Key: `mat9_vid_uslu_bolme`
      - URL: https://www.youtube.com/watch?v=Z8mH2nLqE9I
      - Kaynak: `10-Projects/1_ay_matematik_khan_academy_videolari.md#L14` (1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1))
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `a5610c5b67ab8f46`
    - [1.4] (VIDEO) **Üssü Sıfır, Negatif Sayı veya Kesir Olan Sayılar**
      - Stable Key: `mat9_vid_uslu_negatif`
      - URL: https://www.youtube.com/watch?v=9_d8mQJzL4w
      - Kaynak: `10-Projects/1_ay_matematik_khan_academy_videolari.md#L15` (1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1))
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `651bd6f1c898ab22`

  - **Köklü Sayılar ve Rasyonel Üsler (MAT.9.1.1)** (`lesson_mat9_koklu_sayilar`)
    - [2.1] (VIDEO) **Köklü Sayılar ve Üslü Sayılar Arasındaki İlişki**
      - Stable Key: `mat9_vid_koklu_mantik`
      - URL: https://www.youtube.com/watch?v=vVj4x9p0m1s
      - Kaynak: `10-Projects/1_ay_matematik_khan_academy_videolari.md#L20` (2. Hafta: Bilimsel Gösterim ve Köklü Sayılara Giriş (MAT.9.1.1))
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `22a5bbf935a63509`
    - [2.2] (VIDEO) **Kesirli Üslü ve Köklü İfadeler**
      - Stable Key: `mat9_vid_rasyonel_usler`
      - URL: https://www.youtube.com/watch?v=1xNq8o3l4wM
      - Kaynak: `10-Projects/1_ay_matematik_khan_academy_videolari.md#L21` (2. Hafta: Bilimsel Gösterim ve Köklü Sayılara Giriş (MAT.9.1.1))
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `9f9a7cecea7983b5`

  - **Gerçek Sayı Aralıkları ve Küme Sembolleri (MAT.9.1.2)** (`lesson_mat9_gercek_sayi_araliklari`)
    - [3.1] (VIDEO) **Gerçek Sayı Aralıkları & Gösterim**
      - Stable Key: `mat9_vid_araliklar_gosterim`
      - URL: https://www.youtube.com/watch?v=TNw7eEas9Oo
      - Kaynak: `10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L143` (3. Hafta: Aralıklar, Haritalar ve Atomun Dünyası)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `5dd225171327a852`
    - [3.2] (VIDEO) **Aralık Farkı ve Eşitsizlik Problemleri**
      - Stable Key: `mat9_vid_aralik_farki`
      - URL: https://www.youtube.com/watch?v=drPKmUSKYCI
      - Kaynak: `10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L182` (4. Hafta: Bilim İnsanları, Periyodik Sistem ve 1. Ay Kapanışı)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `fe8a09af222722ae`

### 📚 9. Sınıf Fizik (`course_fiz_9`)
- **Branş:** Fizik | **Sıra:** 2000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Fizik Müfredatı (Fizik Bilimi ve Kuvvet)
- **Üniteler:**

  - **Fizik Bilimi, Alt Dalları ve Vektörler (FİZ.9.1.1 - FİZ.9.2.4)** (`lesson_fiz9_fizik_bilimine_giris`)
    - [1.1] (VIDEO) **Fizik Bilimine Giriş (Temel Prensipler)**
      - Stable Key: `fiz9_vid_fizik_giris`
      - URL: https://www.youtube.com/watch?v=sO7N-V4T_YI
      - Kaynak: `10-Projects/1_ay_fizik_video_rehberi.md#L18` (1. Hafta: Fizik Bilimi ve Alt Dalları)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `f4c45d626dab0871`
    - [1.2] (VIDEO) **Vektörel ve Skaler Büyüklükler (Görsel Koordinat Anlatımı)**
      - Stable Key: `fiz9_vid_vektorler`
      - URL: https://www.youtube.com/watch?v=sO7N-V4T_YI
      - Kaynak: `10-Projects/1_ay_fizik_video_rehberi.md#L40` (3. Hafta: Vektörler ve Dik Kartezyen Koordinat Sistemi)
      - Durum: **verified** *(Uyarı: Reused video URL in source for fundamental vectors overview.)*
      - Parmak İzi (Fingerprint): `aca906bb96eeefe1`

### 📚 9. Sınıf Kimya (`course_kim_9`)
- **Branş:** Kimya | **Sıra:** 3000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Kimya Müfredatı (Kimya Bilimi ve Atom)
- **Üniteler:**

  - **Kimya Bilimi, Güvenlik ve Atom Teorileri (KİM.9.1.1 - KİM.9.1.3)** (`lesson_kim9_kimya_bilimi`)
    - [1.1] (VIDEO) **Kimya Bilimi ve Alt Disiplinleri**
      - Stable Key: `kim9_vid_kimya_bilimi`
      - URL: https://www.youtube.com/@BenimHocamLise
      - Kaynak: `10-Projects/1_ay_kimya_video_rehberi.md#L24` (1. Hafta (Kitap: Kimya Hayattır))
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `7eba82583279a29e`
    - [1.2] (VIDEO) **Laboratuvar Güvenlik Kuralları ve Uyarı Sembolleri**
      - Stable Key: `kim9_vid_guvenlik_sembolleri`
      - URL: https://www.youtube.com/@BenimHocamLise
      - Kaynak: `10-Projects/1_ay_kimya_video_rehberi.md#L25` (2. Hafta (Kitap: Güvenlik))
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `02feaaa0978807bb`
    - [1.3] (VIDEO) **Atom Modelleri (Dalton, Thomson, Rutherford, Bohr)**
      - Stable Key: `kim9_vid_atom_modelleri`
      - URL: https://www.youtube.com/@BenimHocamLise
      - Kaynak: `10-Projects/1_ay_kimya_video_rehberi.md#L26` (3. Hafta (Kitap: Atom Teorileri))
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `ce68350b50ecc36d`

### 📚 9. Sınıf Biyoloji (`course_biyo_9`)
- **Branş:** Biyoloji | **Sıra:** 4000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Biyoloji Müfredatı (Yaşam Teması)
- **Üniteler:**

  - **Yaşam Teması ve Bilimin Doğası (BİY.9.1.1 - BİY.9.1.2)** (`lesson_biyo9_yasam`)
    - [1.1] (VIDEO) **Biyolojiye Giriş: Yaşam Nedir?**
      - Stable Key: `biyo9_vid_yasam_nedir`
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/introduction-to-biology
      - Kaynak: `10-Projects/1_ay_biyoloji_video_rehberi.md#L14` (1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `38a93d2c455a802c`
    - [1.2] (VIDEO) **Bilim Nedir ve Nasıl Çalışır? (Bilimsel Yöntem)**
      - Stable Key: `biyo9_vid_bilim_nedir`
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/the-scientific-method
      - Kaynak: `10-Projects/1_ay_biyoloji_video_rehberi.md#L15` (1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `21cfa493eeec63d4`
    - [1.3] (VIDEO) **Kontrollü Deney Örnekleri**
      - Stable Key: `biyo9_vid_kontrollu_deney`
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/controlled-experiments
      - Kaynak: `10-Projects/1_ay_biyoloji_video_rehberi.md#L16` (1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `5866ca76a6a0fef9`
    - [1.4] (VIDEO) **Hipotez, Teori ve Kanun Arasındaki Fark**
      - Stable Key: `biyo9_vid_teori_yasa`
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/hypotheses-theories-and-laws
      - Kaynak: `10-Projects/1_ay_biyoloji_video_rehberi.md#L21` (3-4. Hafta: Biyolojide Dönüm Noktaları & Bilimsel Bilginin Gelişimi)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `faf97c1fd397354d`

### 📚 9. Sınıf Tarih (`course_tar_9`)
- **Branş:** Tarih | **Sıra:** 5000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Tarih Müfredatı (Mehmet Celal ÖZYILDIZ Kanonik)
- **Üniteler:**

  - **Geçmişin İnşa Sürecinde Tarih (TAR.9.1.1)** (`lesson_tar9_gecmisin_insasi`)
    - [1.1] (VIDEO) **Tarih Öğrenmenin Bireye ve Topluma Faydaları**
      - Stable Key: `tar9_vid_birey_toplum`
      - URL: https://www.youtube.com/watch?v=5QxOpTALmEE
      - Kaynak: `10-Projects/1_ay_tarih_video_rehberi.md#L14` (1. Hafta: Tarih Öğrenmenin Bireye ve Topluma Faydaları)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `e84d7f796f3af4d8`
    - [1.2] (VIDEO) **Tarihin Doğası & Olay-Olgu Ayrımı**
      - Stable Key: `tar9_vid_olay_olgu`
      - URL: https://www.youtube.com/watch?v=5QxOpTALmEE
      - Kaynak: `10-Projects/1_ay_tarih_video_rehberi.md#L25` (2. Hafta: Tarihin Doğası & Olay-Olgu Ayrımı)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `4d61793e4e631362`

  - **Kaynak Türleri, Tarih Yazıcılığı ve Takvimler (TAR.9.1.2)** (`lesson_tar9_kaynaklar_takvimler`)
    - [2.1] (VIDEO) **Tarihin Doğası, Kaynak Türleri ve Tarih Yazıcılığı**
      - Stable Key: `tar9_vid_kaynak_turleri`
      - URL: https://www.youtube.com/watch?v=lqEZ19uwyas
      - Kaynak: `10-Projects/1_ay_tarih_video_rehberi.md#L36` (3. Hafta: Tarihin Doğası, Kaynak Türleri ve Tarih Yazıcılığı)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `a748cc1157c43518`
    - [2.2] (VIDEO) **Tarihe Yardımcı Bilim Dalları ve Zamanın Taksimi (5 Takvim)**
      - Stable Key: `tar9_vid_yardimci_bilimler`
      - URL: https://www.youtube.com/watch?v=lqEZ19uwyas
      - Kaynak: `10-Projects/1_ay_tarih_video_rehberi.md#L47` (4. Hafta: Tarihe Yardımcı Bilim Dalları ve Zamanın Taksimi)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `6d257a30e34bb15f`
    - [A1] (ANKI) **9. Sınıf Tarih Temel Kavramlar Anki Kartları**
      - Stable Key: `tar9_anki_temel`
      - URL: *Dahili Deste*
      - Kaynak: `10-Projects/1_ay_tarih_video_rehberi.md#L50` (4. Hafta)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `ab2911db732e1d2e`

### 📚 9. Sınıf Coğrafya (`course_cog_9`)
- **Branş:** Coğrafya | **Sıra:** 6000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Coğrafya Müfredatı (Doğal Sistemler)
- **Üniteler:**

  - **Doğal Sistemler ve Coğrafyanın İlkeleri (COĞ.9.1.1)** (`lesson_cog9_dogal_sistemler`)
    - [1.1] (VIDEO) **Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri**
      - Stable Key: `cog9_vid_doga_insan`
      - URL: https://www.youtube.com/@cografyaninkodlari
      - Kaynak: `10-Projects/1_ay_cografya_video_rehberi.md#L14` (1. Hafta: Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri)
      - Durum: **needs_review** *(Uyarı: Channel homepage URL used in source instead of direct video ID; transcript timestamps verified.)*
      - Parmak İzi (Fingerprint): `253d882295aff469`
    - [1.2] (VIDEO) **Doğa-İnsan Etkileşimi & Mekânsal Düşünme**
      - Stable Key: `cog9_vid_mekansal_dusunme`
      - URL: https://www.youtube.com/@cografyaninkodlari
      - Kaynak: `10-Projects/1_ay_cografya_video_rehberi.md#L26` (2. Hafta: Doğa-İnsan Etkileşimi & Mekânsal Düşünme)
      - Durum: **needs_review** *(Uyarı: Channel homepage URL used in source instead of direct video ID.)*
      - Parmak İzi (Fingerprint): `7dba004c74189234`
    - [1.3] (VIDEO) **Coğrafya Biliminin Tarihsel Gelişimi**
      - Stable Key: `cog9_vid_tarihsel_gelisim`
      - URL: https://www.youtube.com/@cografyaninkodlari
      - Kaynak: `10-Projects/1_ay_cografya_video_rehberi.md#L37` (3. Hafta: Coğrafya Biliminin Tarihsel Gelişimi)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `09e699b7bb5f1abf`
    - [1.4] (VIDEO) **Mekânın Aynası Haritalar & Projeksiyon Yöntemleri**
      - Stable Key: `cog9_vid_harita_bilgisi`
      - URL: https://www.youtube.com/@cografyaninkodlari
      - Kaynak: `10-Projects/1_ay_cografya_video_rehberi.md#L48` (4. Hafta: Mekânın Aynası Haritalar & Projeksiyon Yöntemleri)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `c36333158d958f9b`
    - [A1] (ANKI) **9. Sınıf Coğrafya 1. Ay Anki Destesi**
      - Stable Key: `cog9_anki_1ay`
      - URL: *Dahili Deste*
      - Kaynak: `10-Projects/9_sinif_cografya_1_ay_plani.md#L76` (3. Anki Tekrar ve Pekiştirme Stratejisi)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `674113926dae634c`

### 📚 9. Sınıf İngilizce (`course_ing_9`)
- **Branş:** İngilizce | **Sıra:** 7000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf İngilizce Müfredatı (Theme 1)
- **Üniteler:**

  - **Orientation, Grammar & Vocabulary (Theme 1)** (`lesson_ing9_orientation_revision`)
    - [1.1] (VIDEO) **İngilizce Cümle Kurma Mantığı & Günlük Rutin İfadeleri**
      - Stable Key: `ing9_vid_cumle_kurma`
      - URL: https://www.youtube.com/@OzerKiraz
      - Kaynak: `10-Projects/1_ay_ingilizce_video_rehberi.md#L18` (1. Hafta: Orientation & Okul Yaşamına Giriş)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `3d98a1b65e57640f`
    - [1.2] (VIDEO) **Simple Present vs Present Continuous (Geniş Zaman - Şimdiki Zaman Farkı)**
      - Stable Key: `ing9_vid_simple_present`
      - URL: https://www.youtube.com/@OzerKiraz
      - Kaynak: `10-Projects/1_ay_ingilizce_video_rehberi.md#L29` (2. Hafta: Geniş Zaman vs Şimdiki Zaman)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `a014d60361471a63`
    - [1.3] (VIDEO) **Modals: Can, Must, Have to Farkı ve Kullanımı**
      - Stable Key: `ing9_vid_modals`
      - URL: https://www.youtube.com/@OzerKiraz
      - Kaynak: `10-Projects/1_ay_ingilizce_video_rehberi.md#L40` (3. Hafta: Yetenek ve Zorunluluk Kipleri)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `e5d50576dca9d3e6`
    - [1.4] (VIDEO) **Used to ve Could Kullanımı (Geçmiş Alışkanlıklar & Kibar İstekler)**
      - Stable Key: `ing9_vid_used_to`
      - URL: https://www.youtube.com/@OzerKiraz
      - Kaynak: `10-Projects/1_ay_ingilizce_video_rehberi.md#L51` (4. Hafta: Geçmiş Alışkanlıklar & Kibar İstekler)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `b82ba5fb9bb6bac3`
    - [A1] (ANKI) **9. Sınıf İngilizce 1. Ay Kelime Destesi**
      - Stable Key: `ing9_anki_1ay`
      - URL: *Dahili Deste*
      - Kaynak: `10-Projects/1_ay_ingilizce_kelime_listesi.md#L1` (9. Sınıf İngilizce 1. Ay Kapsamlı Kelime Listesi)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `57b1af571319b9ec`

### 📚 9. Sınıf Almanca (`course_alm_9`)
- **Branş:** Almanca | **Sıra:** 8000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Almanca Müfredatı (A1.1 Hallo!)
- **Üniteler:**

  - **Modul 1: HALLO! & Informationen zur Person (A1.1)** (`lesson_alm9_modul1_hallo`)
    - [1.1] (VIDEO) **Almanca Selamlaşma, Vedalaşma ve Alfabe (Das ABC)**
      - Stable Key: `alm9_vid_selamlasma_alfabe`
      - URL: https://www.youtube.com/@AlmancaKolay
      - Kaynak: `10-Projects/1_ay_almanca_video_rehberi.md#L20` (1. Hafta: Selamlaşma / Vedalaşma ve Alfabe)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `10d0f77cb54c3edd`
    - [1.2] (VIDEO) **Kendini Tanıtma, Adını Söyleme ve Hal-Hatır Sorma**
      - Stable Key: `alm9_vid_kendini_tanitma`
      - URL: https://www.youtube.com/@AlmancaKolay
      - Kaynak: `10-Projects/1_ay_almanca_video_rehberi.md#L30` (2. Hafta: Kendini Tanıtma ve Hal-Hatır)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `7c6de36c4dc9e14b`
    - [1.3] (VIDEO) **0-20 Arası Sayılar (Die Zahlen) ve Telefon Numarası**
      - Stable Key: `alm9_vid_sayilar`
      - URL: https://www.youtube.com/@AlmancaKolay
      - Kaynak: `10-Projects/1_ay_almanca_video_rehberi.md#L40` (3. Hafta: Sayılar ve Telefon Numarası)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `940836d5d4d9caf3`
    - [1.4] (VIDEO) **Ülkeler, Diller ve Nereli Olduğunu Söyleme (Woher kommst du?)**
      - Stable Key: `alm9_vid_ulkeler_diller`
      - URL: https://www.youtube.com/@AlmancaKolay
      - Kaynak: `10-Projects/1_ay_almanca_video_rehberi.md#L50` (4. Hafta: Ülkeler, Diller ve İkamet)
      - Durum: **needs_review** 
      - Parmak İzi (Fingerprint): `8491cf610c6bd2b8`
    - [A1] (ANKI) **9. Sınıf Almanca 1. Ay Kelime ve Cümle Destesi**
      - Stable Key: `alm9_anki_1ay`
      - URL: *Dahili Deste*
      - Kaynak: `10-Projects/1_ay_almanca_kelime_listesi.md#L1` (9. Sınıf Almanca (A1.1) 1. Ay Kelime Rehberi)
      - Durum: **verified** 
      - Parmak İzi (Fingerprint): `9e0ff7f38dd78cd2`

### 📚 9. Sınıf Türk Dili ve Edebiyatı (`course_tde_9`)
- **Branş:** Türk Dili ve Edebiyatı | **Sıra:** 9000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf TDE Müfredatı
- **Üniteler:**

  - **Metin İnceleme, Anlam ve Yazım Kuralları** (`lesson_tde9_metin_ve_anlam`)
    - [1.1] (VIDEO) **TDE: Metinde Anlam & Söz Sanatları**
      - Stable Key: `tde9_vid_soz_sanatlari`
      - URL: https://www.youtube.com/watch?v=NBhw_SkvV8E
      - Kaynak: `10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L87` (2. Hafta: Alt Dallar, Güvenlik ve Anlamlandırma)
      - Durum: **needs_review** *(Uyarı: Reused generic video URL across weeks in legacy study plan.)*
      - Parmak İzi (Fingerprint): `af1de06e2e3cd0c1`

---

## ⚠️ Denetim Uyarıları & Tespit Edilen Anomaliler (Audit Warnings)

1. **Yinelenen Video URL'leri (Repeated Video URLs across Topics):**
   - Eski `9_sinif_4_haftalik_studytracker_calisma_plani.md` şablonunda tüm Biyoloji haftaları için `nn2uXdrHDMo`, Coğrafya için `rBGiYJ4Z89Y`, Fizik için `5pmUa7MZLuo` ve Kimya için `CdjnEn8X9ZM` kullanılmıştır.
   - V2 Tohumunda bu tekrarlar elenmiş, branş rehberi dosyalarındaki gerçek Khan Academy veya transkriptli videolar önceliklendirilmiştir.
2. **Kanal Ana Sayfası Bağlantıları (Channel Homepage URLs):**
   - Coğrafya (`@cografyaninkodlari`), İngilizce (`@OzerKiraz`), Almanca (`@AlmancaKolay`) ve Kimya (`@BenimHocamLise`) rehberlerinde spesifik video ID yerine kanal ana sayfası linkleri mevcuttur.
   - Bu öğeler `reviewStatus: needs_review` olarak işaretlenmiştir.
3. **Müfredat Bağımsızlığı & Puzzle Sıralama:**
   - Tüm öğeler `order_key` ile modüler yapıya kavuşturulmuştur. İleride araya `Quiz 17.2` gibi testler eklenirken var olan öğelerin `id` ve `stable_key` değerleri değişmez.
