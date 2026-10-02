# 9. Sınıf StudyTracker V2 Müfredat & İçerik Kaynakları Denetim Raporu

**Oluşturulma Tarihi:** 2026-10-02T09:00:00Z  
**Müfredat:** MEB Türkiye Yüzyılı Maarif Modeli (9. Sınıf / Lise 1)  
**Tohum Manifestosu:** [`content/9-sinif-v2-catalog.json`](file:///data/data/com.termux/files/home/projects/study-tracker/content/9-sinif-v2-catalog.json)

---

## 📊 Genel Özet & Metrikler (Full-Season V2)

| Metrik | Adet |
|---|---|
| **Toplam Ders (Course)** | **9** |
| **Toplam Ünite / Konu (Lesson)** | **35** |
| **Toplam Öğrenme Öğesi (Learning Item)** | **114** |
| 🎬 **Video Sayısı (VIDEO Toplam)** | **74** |
| &nbsp;&nbsp;&nbsp;&nbsp; 🟢 Aktif Doğrulanmış Video | **19** |
| &nbsp;&nbsp;&nbsp;&nbsp; 🟡 Taslak Video (URL Bekleyen) | **55** |
| 📝 **Sınav / Test (QUIZ - 3-7 Soru)** | **35** |
| 📇 **Anki Destesi (ANKI)** | **5** |
| 🟢 **Doğrulanmış (Verified)** | **59** |
| 🟡 **İnceleme Bekleyen (Needs Review)** | **55** |

---

## 📁 Kullanılan Proje-İçi Yerel Kaynak Dosyaları (Source of Truth)

Tüm ders planları, kazanım haritaları ve soru içerikleri doğrudan yerel proje dosyalarından derlenmiştir (Hiçbir web araştırması yapılmamıştır):

1. **Yıllık Ders Planları (DefterDoldur AL-9 Maarif Modeli):**
   - `~/vault/10-Projects/Matematik_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/Fizik_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/Kimya_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/Biyoloji_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/Tarih_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/Cografya_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/İngilizce_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/Almanca_Yillik_Plan_AL9.md`
   - `~/vault/10-Projects/TDE_Yillik_Plan_AL9.md`
   - `projects/lise1-ogrenme-programi/data/defterdoldur_tum_dersler_9al.json`
2. **Video Kürasyonu & Öğretmen Eşleştirmeleri:**
   - `~/vault/10-Projects/1_ay_tarih_video_rehberi.md` (Kanonik Öğretmen: **Mehmet Celal ÖZYILDIZ**)
   - `~/vault/10-Projects/1_ay_matematik_khan_academy_videolari.md` (Khan Academy Türkçe)
   - `~/vault/10-Projects/1_ay_biyoloji_video_rehberi.md` (Khan Academy)
   - `~/vault/10-Projects/1_ay_fizik_video_rehberi.md` (Khan Academy Türkçe)
   - `~/vault/10-Projects/1_ay_kimya_video_rehberi.md`
   - `~/vault/10-Projects/1_ay_cografya_video_rehberi.md`
   - `~/vault/10-Projects/1_ay_ingilizce_video_rehberi.md`
   - `~/vault/10-Projects/1_ay_almanca_video_rehberi.md`
3. **Anki Desteleri & Kelime Listeleri:**
   - `projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_cografya_1_ay.apkg`
   - `projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_ingilizce_1_ay.apkg`
   - `projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_almanca_1_ay.apkg`
   - `projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_tarih_anki.txt`
   - `projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_biyoloji_anki.txt`
4. **MEB Ders Kitapları Metinleri:**
   - `projects/lise1-ogrenme-programi/data/meb_kitaplari/matematik9.md`
   - `projects/lise1-ogrenme-programi/data/meb_kitaplari/fizik9.md`
   - `projects/lise1-ogrenme-programi/data/meb_kitaplari/kimya9.md`
   - `projects/lise1-ogrenme-programi/data/meb_kitaplari/biyoloji9.md`
   - `projects/lise1-ogrenme-programi/data/meb_kitaplari/tarih9.md`
   - `projects/lise1-ogrenme-programi/data/meb_kitaplari/cografya9.md`
   - `projects/lise1-ogrenme-programi/data/meb_kitaplari/tde9.md`

---

## ⚖️ Ürün Modeli ve Kurallar

1. **Takvim Bağımsızlığı & Öğrenci Odaklı Hız (Self-Paced):**
   - Hafta, gün ve ay kısıtlamaları kaldırılmıştır.
   - Öğrencinin ilerlemesi ve kaldığı yer doğrudan çözülen deneme ve quiz geçmişinden (`attempts` tablosu) türetilir.
2. **Sıfır Sahte URL Kuralı (No Fabricated URLs):**
   - Yerel dosyalarda doğrudan video bağlantısı bulunmayan tüm konu başlıkları `contentUrl: null` ve `publishingStatus: "draft"` olarak işaretlenmiştir.
3. **Kanonik Öğretmen İlkesi:**
   - Tarih dersinde **Mehmet Celal ÖZYILDIZ** (*Benim Hocam*) kanonik öğretmen seçimi korunmuştur.
4. **Gerçek QUIZ Soruları:**
   - Tüm QUIZ öğeleri Maarif Modeli kazanımlarıyla %100 uyumlu 3-7 adet deterministik, açıklamalı soru içermektedir.

---

## 🔍 Ders Bazlı Detay Listesi

### 📚 9. Sınıf Matematik (`course_mat_9`)
- **Branş:** Matematik | **Sıra:** 1000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Matematik Müfredatı (Sayılar, Geometri, Algoritma, İstatistik ve Olasılık)
- **Üniteler:**

  - **Sayılar: Üslü ve Köklü Gösterimler (MAT.9.1.1)** (`lesson_mat9_sayilar_uslu_koklu`)
    - [1.1] (VIDEO) **Üslü Sayılara Giriş (1. Seviye)**
      - Stable Key: `mat9_vid_uslu_giris`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=kYqP9K0Y0pU
      - Parmak İzi: `893ed24a871bd143`
    - [1.2] (VIDEO) **Üslü İfadelerin Sadeleştirilmesi ve Çarpma**
      - Stable Key: `mat9_vid_uslu_kurallar`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=s5Rz-1i0n18
      - Parmak İzi: `09e4ff23f901f518`
    - [1.3] (VIDEO) **Üslü Sayılarda Bölme İşlemi ve Özellikleri**
      - Stable Key: `mat9_vid_uslu_bolme`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=Z8mH2nLqE9I
      - Parmak İzi: `ed3787346eb0861d`
    - [1.4] (VIDEO) **Üssü Sıfır, Negatif Sayı veya Kesir Olan Sayılar**
      - Stable Key: `mat9_vid_uslu_negatif`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=9_d8mQJzL4w
      - Parmak İzi: `6cb73c7f5932687a`
    - [1.5] (VIDEO) **Köklü Sayılar ve Üslü Sayılar Arasındaki İlişki**
      - Stable Key: `mat9_vid_koklu_mantik`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=vVj4x9p0m1s
      - Parmak İzi: `156d7b115046cade`
    - [1.6] (VIDEO) **Kesirli Üslü ve Köklü İfadeler**
      - Stable Key: `mat9_vid_rasyonel_usler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=1xNq8o3l4wM
      - Parmak İzi: `f2b5f73128476d91`
    - [Q1] (QUIZ) **Üslü ve Köklü Sayılar Muhakeme Testi**
      - Stable Key: `mat9_quiz_uslu_koklu`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 4
      - Parmak İzi: `9676190cdd7bf378`

  - **Gerçek Sayı Aralıkları ve Küme Sembolleri (MAT.9.1.2)** (`lesson_mat9_sayilar_araliklar_kumeler`)
    - [2.1] (VIDEO) **Gerçek Sayı Aralıkları & Gösterim**
      - Stable Key: `mat9_vid_araliklar_gosterim`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=TNw7eEas9Oo
      - Parmak İzi: `61349661ecacb396`
    - [2.2] (VIDEO) **Aralık Farkı ve Eşitsizlik Problemleri**
      - Stable Key: `mat9_vid_aralik_farki`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=drPKmUSKYCI
      - Parmak İzi: `38986b05f958b3b9`
    - [Q1] (QUIZ) **Gerçek Sayı Aralıkları ve Kümeler Tarama Testi**
      - Stable Key: `mat9_quiz_araliklar_kumeler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 4
      - Parmak İzi: `2fefb39da31b382b`

  - **Sayı Kümeleri ve İşlem Özellikleri (MAT.9.1.3)** (`lesson_mat9_sayilar_islem_ozellikleri`)
    - [3.1] (VIDEO) **Doğal, Tam, Rasyonel ve Gerçek Sayı Kümeleri**
      - Stable Key: `mat9_vid_sayi_kumeleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `cf54a6722d62f41c`
    - [3.2] (VIDEO) **İşlem Özelliklerini Cebirsel İfade Etme ve Doğrulama**
      - Stable Key: `mat9_vid_cebirsel_ispat`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `5a4a81bbfad7b8d8`
    - [Q1] (QUIZ) **Sayı Kümeleri ve İspat Yöntemleri Testi**
      - Stable Key: `mat9_quiz_sayi_kumeleri`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `18de800d4d591dc3`

  - **Geometrik Şekiller: Üçgende Açı ve Kenar Özellikleri (MAT.9.2.1)** (`lesson_mat9_ucgende_acilar_kenarlar`)
    - [4.1] (VIDEO) **Üçgende İç ve Dış Açı Bağıntıları**
      - Stable Key: `mat9_vid_ucgende_aci`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `d08333380962e2da`
    - [4.2] (VIDEO) **Üçgen Eşitsizliği ve Açı-Kenar İlişkileri**
      - Stable Key: `mat9_vid_ucgen_esitsizligi`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `8d096350f5994c96`
    - [Q1] (QUIZ) **Üçgende Açı ve Kenar Bağıntıları Testi**
      - Stable Key: `mat9_quiz_ucgende_aci_kenar`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `e50cef5317d0d91c`

  - **Eşlik ve Benzerlik: Teoremler ve Dönüşümler (MAT.9.2.2 - MAT.9.2.3)** (`lesson_mat9_eslik_ve_benzerlik`)
    - [5.1] (VIDEO) **Geometrik Dönüşümler: Öteleme, Yansıma ve Dönme**
      - Stable Key: `mat9_vid_geometrik_donusumler`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `e880c5ae62554989`
    - [5.2] (VIDEO) **Eşlik ve Benzerlik Koşulları (A.A., K.A.K., K.K.K.)**
      - Stable Key: `mat9_vid_benzerlik_kosullari`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `37b5d36c1eebc4ba`
    - [5.3] (VIDEO) **Tales, Öklid ve Pisagor Teoremleri**
      - Stable Key: `mat9_vid_teoremler`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `8d4dc29f678d6d44`
    - [Q1] (QUIZ) **Eşlik, Benzerlik ve Teoremler Testi**
      - Stable Key: `mat9_quiz_benzerlik_teoremler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `d363f3ef65cd65fc`

  - **Algoritma ve Mantık Bağlaçları (MAT.9.3.1 - MAT.9.3.2)** (`lesson_mat9_algoritma_ve_mantik`)
    - [6.1] (VIDEO) **Algoritma Temelli Problem Çözme ve Adım Mantığı**
      - Stable Key: `mat9_vid_algoritma_temelleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `354887cf36962799`
    - [6.2] (VIDEO) **Mantık Bağlaçları (∧, ∨, ⇒, ⇔) ve Niceleyiciler (∀, ∃)**
      - Stable Key: `mat9_vid_mantik_baglaclari`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `3c037fb942c0c06a`
    - [Q1] (QUIZ) **Algoritma ve Sembolik Mantık Testi**
      - Stable Key: `mat9_quiz_algoritma_mantik`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `3a4b463df07e4788`

  - **İstatistiksel Araştırma Süreci ve Veri Dağılımları (MAT.9.4.1)** (`lesson_mat9_istatistik_veri_dagilimi`)
    - [7.1] (VIDEO) **Tek Nicel Değişkenli Veri Dağılımları ve Grafik Yorumlama**
      - Stable Key: `mat9_vid_veri_dagilimlari`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `6e6b584afe394da8`
    - [Q1] (QUIZ) **İstatistik ve Merkezi Eğilim Ölçüleri Testi**
      - Stable Key: `mat9_quiz_istatistik`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `908cfc851079225e`

  - **Veriden Olasılığa: Deneysel ve Teorik Olasılık (MAT.9.5.1)** (`lesson_mat9_veriden_olasiliga`)
    - [8.1] (VIDEO) **Deneysel ve Teorik Olasılık Karşılaştırması**
      - Stable Key: `mat9_vid_olasilik_turleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `d5d5033bb88ca311`
    - [Q1] (QUIZ) **Olasılık Hesaplamaları Testi**
      - Stable Key: `mat9_quiz_olasilik`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `fb82e0d04bd66b4e`


### 📚 9. Sınıf Fizik (`course_fiz_9`)
- **Branş:** Fizik | **Sıra:** 2000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Fizik Müfredatı (Fizik Bilimi, Kuvvet-Hareket, Akışkanlar ve Enerji)
- **Üniteler:**

  - **Fizik Bilimi ve Kariyer Keşfi (FİZ.9.1.1)** (`lesson_fiz9_fizik_bilimine_giris`)
    - [1.1] (VIDEO) **Fizik Bilimine Giriş (Temel Prensipler)**
      - Stable Key: `fiz9_vid_fizik_giris`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=sO7N-V4T_YI
      - Parmak İzi: `209e14527f3f8255`
    - [1.2] (VIDEO) **Fiziğin Alt Dalları ve Bilim Araştırma Merkezleri**
      - Stable Key: `fiz9_vid_alt_dallar`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `f643ca89c63459fd`
    - [Q1] (QUIZ) **Fizik Bilimi ve Alt Dalları Testi**
      - Stable Key: `fiz9_quiz_fizik_giris`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `46692a3e70359c73`

  - **Kuvvet ve Hareket: Büyüklükler ve Vektörler (FİZ.9.2.1 - FİZ.9.2.4)** (`lesson_fiz9_kuvvet_ve_hareket`)
    - [2.1] (VIDEO) **Vektörel ve Skaler Büyüklükler (Görsel Koordinat Anlatımı)**
      - Stable Key: `fiz9_vid_vektorler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=sO7N-V4T_YI
      - Parmak İzi: `6486243db87369b1`
    - [2.2] (VIDEO) **Doğadaki 4 Temel Kuvvet ve Hareket Türleri**
      - Stable Key: `fiz9_vid_dogadaki_kuvvetler`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `09cb02d72bd03694`
    - [Q1] (QUIZ) **Büyüklükler, Vektörler ve Hareket Testi**
      - Stable Key: `fiz9_quiz_vektor_hareket`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `4e5e91453b736f1b`

  - **Akışkanlar: Basınç ve Kaldırma Kuvveti (FİZ.9.3.1 - FİZ.9.3.3)** (`lesson_fiz9_akiskanlar_ve_basinc`)
    - [3.1] (VIDEO) **Katı, Sıvı ve Gaz Basıncı (Pascal ve Torricelli)**
      - Stable Key: `fiz9_vid_basinc_prensipleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `8dc3509e9793b3d3`
    - [3.2] (VIDEO) **Sıvıların Kaldırma Kuvveti ve Bernoulli İlkesi**
      - Stable Key: `fiz9_vid_kaldirma_kuvveti`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `f597e4070d0f7987`
    - [Q1] (QUIZ) **Basınç ve Kaldırma Kuvveti Testi**
      - Stable Key: `fiz9_quiz_basinc_kaldirma`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `b5e4615ad22b2695`

  - **Enerji, Isı ve Sıcaklık (FİZ.9.4.1 - FİZ.9.4.4)** (`lesson_fiz9_enerji_isi_sicaklik`)
    - [4.1] (VIDEO) **İç Enerji, Isı ve Sıcaklık Kavramları**
      - Stable Key: `fiz9_vid_isi_sicaklik_kavram`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `e546c866395ea3a9`
    - [4.2] (VIDEO) **Hâl Değişimi, Isıl Denge ve Isı Aktarım Yolları**
      - Stable Key: `fiz9_vid_hal_degisimi`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `026003d46f482694`
    - [Q1] (QUIZ) **Isı, Sıcaklık ve Hâl Değişimi Testi**
      - Stable Key: `fiz9_quiz_isi_sicaklik`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `29239d124da9f020`


### 📚 9. Sınıf Kimya (`course_kim_9`)
- **Branş:** Kimya | **Sıra:** 3000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Kimya Müfredatı (Kimya Bilimi, Atom, Etkileşimler ve Maddenin Halleri)
- **Üniteler:**

  - **Kimya Bilimi, Alt Disiplinler ve Laboratuvar Güvenliği (KİM.9.1.1 - KİM.9.1.2)** (`lesson_kim9_kimya_bilimi_guvenlik`)
    - [1.1] (VIDEO) **Kimya Bilimi, Günlük Hayat ve Alt Disiplinleri**
      - Stable Key: `kim9_vid_kimya_bilimi`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `fdea372d4ca8b20e`
    - [1.2] (VIDEO) **Laboratuvar Güvenlik Kuralları ve Uyarı Piktogramları**
      - Stable Key: `kim9_vid_guvenlik_sembolleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `9083b0011b7c547a`
    - [Q1] (QUIZ) **Kimya Bilimi ve Laboratuvar Güvenliği Testi**
      - Stable Key: `kim9_quiz_kimya_guvenlik`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `907fa86dc6b6d8c8`

  - **Atom Teorileri ve Periyodik Tablo (KİM.9.1.3 - KİM.9.1.4)** (`lesson_kim9_atom_ve_periyodik_sistem`)
    - [2.1] (VIDEO) **Atom Modelleri: Dalton, Thomson, Rutherford ve Bohr**
      - Stable Key: `kim9_vid_atom_modelleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `9d10f16edbd4f757`
    - [2.2] (VIDEO) **Modern Atom Teorisi ve Periyodik Özelliklerin Değişimi**
      - Stable Key: `kim9_vid_periyodik_ozellikler`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `6930cb3832e890be`
    - [Q1] (QUIZ) **Atomun Yapısı ve Periyodik Sistem Testi**
      - Stable Key: `kim9_quiz_atom_periyodik`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `f4ee54056b15ce28`

  - **Kimyasal Türler Arası Etkileşimler (KİM.9.2.1 - KİM.9.2.3)** (`lesson_kim9_kimyasal_turler_etkilesim`)
    - [3.1] (VIDEO) **Güçlü Etkileşimler: İyonik, Kovalent ve Metalik Bağ**
      - Stable Key: `kim9_vid_bag_turleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `b3ac44607a0489ab`
    - [3.2] (VIDEO) **Moleküller Arası Zayıf Etkileşimler ve Hidrojen Bağı**
      - Stable Key: `kim9_vid_zayif_etkilesimler`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `09566b06fd2755f6`
    - [Q1] (QUIZ) **Kimyasal Bağlar ve Etkileşimler Testi**
      - Stable Key: `kim9_quiz_baglar_etkilesim`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `5f11263ed19e23de`

  - **Maddenin Halleri ve Yeşil Kimya (KİM.9.2.4 - KİM.9.3.1)** (`lesson_kim9_maddenin_halleri_surdurulebilirlik`)
    - [4.1] (VIDEO) **Katı Türleri (Amorf, Kristal) ve Sıvılarda Viskozite**
      - Stable Key: `kim9_vid_katilar_ve_sivilar`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `d149100b0d230265`
    - [4.2] (VIDEO) **Nanoparçacıklar, Ekolojik Sürdürülebilirlik ve Yeşil Kimya**
      - Stable Key: `kim9_vid_yesil_kimya`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `ee46e61d9bd98733`
    - [Q1] (QUIZ) **Maddenin Halleri ve Sürdürülebilirlik Testi**
      - Stable Key: `kim9_quiz_maddenin_halleri`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `94f081c22bc3f7de`


### 📚 9. Sınıf Biyoloji (`course_biyo_9`)
- **Branş:** Biyoloji | **Sıra:** 4000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Biyoloji Müfredatı (Yaşam, Temel Bileşenler, Hücre ve Biyoçeşitlilik)
- **Üniteler:**

  - **Yaşam Teması ve Bilimin Doğası (BİY.9.1.1 - BİY.9.1.2)** (`lesson_biyo9_yasam`)
    - [1.1] (VIDEO) **Biyolojiye Giriş: Yaşam Nedir?**
      - Stable Key: `biyo9_vid_yasam_nedir`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/introduction-to-biology
      - Parmak İzi: `d2c241ae0e658169`
    - [1.2] (VIDEO) **Bilim Nedir ve Nasıl Çalışır? (Bilimsel Yöntem)**
      - Stable Key: `biyo9_vid_bilim_nedir`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/the-scientific-method
      - Parmak İzi: `a7fe85134f094b5e`
    - [1.3] (VIDEO) **Kontrollü Deney Örnekleri**
      - Stable Key: `biyo9_vid_kontrollu_deney`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/controlled-experiments
      - Parmak İzi: `f2d122a451808fa3`
    - [1.4] (VIDEO) **Hipotez, Teori ve Kanun Arasındaki Fark**
      - Stable Key: `biyo9_vid_teori_yasa`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/hypotheses-theories-and-laws
      - Parmak İzi: `ed7b85c3da2da5fb`
    - [Q1] (QUIZ) **Bilimsel Yöntem ve Yaşamın Doğası Testi**
      - Stable Key: `biyo9_quiz_yasam_bilim`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `461aabc187053e1a`

  - **Canlıların Temel Bileşenleri (BİY.9.1.3 - BİY.9.1.4)** (`lesson_biyo9_canlilarin_temel_bilesenleri`)
    - [2.1] (VIDEO) **İnorganik ve Organik Bileşikler: Karbonhidratlar, Yağlar ve Proteinler**
      - Stable Key: `biyo9_vid_organik_inorganik`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `98f11f3895cb8b34`
    - [2.2] (VIDEO) **Enzimlerin Yapısı, Çalışması ve Nükleik Asitler (DNA / RNA)**
      - Stable Key: `biyo9_vid_enzimler_nukleik`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `90fcf5e5109d9a94`
    - [A1] (ANKI) **Biyoloji: Organik Moleküller ve Enzimler Anki Destesi**
      - Stable Key: `biyo9_anki_temel_bilesenler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Anki Kart Sayısı: 14
      - Parmak İzi: `4c0113e8f306f05a`
    - [Q1] (QUIZ) **Canlıların Temel Bileşenleri Testi**
      - Stable Key: `biyo9_quiz_bilesenler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `779d7faa5f53aee7`

  - **Hücre Yapısı, Organeller ve Madde Geçişleri (BİY.9.2.1 - BİY.9.2.2)** (`lesson_biyo9_hucre_ve_madde_gecisleri`)
    - [3.1] (VIDEO) **Prokaryot ve Ökaryot Hücre, Sitoplazma ve Organeller**
      - Stable Key: `biyo9_vid_hucre_organeller`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `3e1ccb51c5c6cadc`
    - [3.2] (VIDEO) **Hücre Zarından Madde Taşınımı: Pasif, Aktif Taşıma ve Endositoz/Ekzositoz**
      - Stable Key: `biyo9_vid_madde_gecisleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `6c69ae5cb5b229d7`
    - [Q1] (QUIZ) **Hücre ve Madde Geçişleri Testi**
      - Stable Key: `biyo9_quiz_hucre_madde`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `bb4d7218aca7013b`

  - **Canlılar Dünyası ve Biyoçeşitlilik (BİY.9.2.3 - BİY.9.2.5)** (`lesson_biyo9_siniflandirma_ve_biyocesitlilik`)
    - [4.1] (VIDEO) **Sınıflandırma İlkeleri, İkili Adlandırma ve Canlılar Âlemleri**
      - Stable Key: `biyo9_vid_siniflandirma`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `6442c0e6c046456f`
    - [Q1] (QUIZ) **Sınıflandırma ve Biyoçeşitlilik Testi**
      - Stable Key: `biyo9_quiz_siniflandirma`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `4b5466a755d2af70`


### 📚 9. Sınıf Tarih (`course_tar_9`)
- **Branş:** Tarih | **Sıra:** 5000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Tarih Müfredatı (Mehmet Celal ÖZYILDIZ Kanonik)
- **Üniteler:**

  - **Geçmişin İnşa Sürecinde Tarih (TAR.9.1.1)** (`lesson_tar9_gecmisin_insasi`)
    - [1.1] (VIDEO) **Tarih Öğrenmenin Bireye ve Topluma Faydaları**
      - Stable Key: `tar9_vid_birey_toplum`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=5QxOpTALmEE
      - Parmak İzi: `125e2f913b1573d6`
    - [1.2] (VIDEO) **Tarihin Doğası & Olay-Olgu Ayrımı**
      - Stable Key: `tar9_vid_olay_olgu`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=5QxOpTALmEE
      - Parmak İzi: `8175e021fb9dcb69`
    - [Q1] (QUIZ) **Tarih Bilimi ve Metodolojisi Testi**
      - Stable Key: `tar9_quiz_gecmisin_insasi`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `61008abf0a494c90`

  - **Kaynak Türleri, Tarih Yazıcılığı ve Takvimler (TAR.9.1.2)** (`lesson_tar9_kaynaklar_takvimler`)
    - [2.1] (VIDEO) **Tarihin Doğası, Kaynak Türleri ve Tarih Yazıcılığı**
      - Stable Key: `tar9_vid_kaynak_turleri`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=lqEZ19uwyas
      - Parmak İzi: `a3311f9cb2414371`
    - [2.2] (VIDEO) **Tarihe Yardımcı Bilim Dalları ve Zamanın Taksimi (5 Takvim)**
      - Stable Key: `tar9_vid_yardimci_bilimler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=lqEZ19uwyas
      - Parmak İzi: `36da09cd4e03a26f`
    - [A1] (ANKI) **9. Sınıf Tarih Temel Kavramlar Anki Kartları**
      - Stable Key: `tar9_anki_temel`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Anki Kart Sayısı: 10
      - Parmak İzi: `cf86ee58f58af1d4`
    - [Q1] (QUIZ) **Tarihi Kaynaklar ve Takvim Sistemleri Testi**
      - Stable Key: `tar9_quiz_kaynaklar_takvimler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `c723303814c83b8e`

  - **Eski Çağ Medeniyetleri ve Konargöçer Yaşam (TAR.9.2.1 - TAR.9.2.4)** (`lesson_tar9_eski_cag_medeniyetleri`)
    - [3.1] (VIDEO) **Tarım Devrimi, İlk Şehir Devletleri ve Mezopotamya Medeniyetleri**
      - Stable Key: `tar9_vid_tarim_devrimi_mezopotamya`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `aeab39970dddc1bb`
    - [3.2] (VIDEO) **Anadolu Medeniyetleri, Eski Hukuk ve Türklerde Konargöçer Yaşam**
      - Stable Key: `tar9_vid_anadolu_ve_turkler`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `8f063a2d3ac90f28`
    - [Q1] (QUIZ) **Eski Çağ Medeniyetleri ve Hukuk Testi**
      - Stable Key: `tar9_quiz_eski_cag`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `4ca293dc42b2c524`

  - **Orta Çağ Medeniyetleri ve Ticaret Yolları (TAR.9.3.1 - TAR.9.3.4)** (`lesson_tar9_orta_cag_medeniyetleri`)
    - [4.1] (VIDEO) **Kavimler Göçü, Feodalizm ve Orta Çağ'ın Başlıca Devletleri**
      - Stable Key: `tar9_vid_orta_cag_gocler_devletler`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `65523afb0a6483eb`
    - [4.2] (VIDEO) **Orta Çağ Ticaret Yolları: İpek, Baharat ve Kürk Yolları**
      - Stable Key: `tar9_vid_orta_cag_ticaret_yollari`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `28f7f3fd5bad8f77`
    - [Q1] (QUIZ) **Orta Çağ Dünyası ve Ticaret Ağları Testi**
      - Stable Key: `tar9_quiz_orta_cag`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `b1e6b1df1b2c6b14`


### 📚 9. Sınıf Coğrafya (`course_cog_9`)
- **Branş:** Coğrafya | **Sıra:** 6000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Coğrafya Müfredatı (Doğal Sistemler, Harita, İklim, Nüfus ve Afetler)
- **Üniteler:**

  - **Doğal Sistemler ve Coğrafyanın İlkeleri (COĞ.9.1.1)** (`lesson_cog9_dogal_sistemler`)
    - [1.1] (VIDEO) **Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri**
      - Stable Key: `cog9_vid_doga_insan`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `475b76ac042a0e59`
    - [1.2] (VIDEO) **Doğa-İnsan Etkileşimi & Mekânsal Düşünme**
      - Stable Key: `cog9_vid_mekansal_dusunme`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `8b460fc3d1c4e78e`
    - [1.3] (VIDEO) **Coğrafya Biliminin Tarihsel Gelişimi**
      - Stable Key: `cog9_vid_tarihsel_gelisim`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `1f3480b9f4892e43`
    - [1.4] (VIDEO) **Mekânın Aynası Haritalar & Projeksiyon Yöntemleri**
      - Stable Key: `cog9_vid_harita_bilgisi`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `a3023d913ad0037c`
    - [A1] (ANKI) **9. Sınıf Coğrafya 1. Ay Anki Destesi**
      - Stable Key: `cog9_anki_1ay`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Anki Kart Sayısı: 24
      - Parmak İzi: `0cad6592eadf6031`
    - [Q1] (QUIZ) **Coğrafyanın Doğası ve İlkeleri Testi**
      - Stable Key: `cog9_quiz_dogal_sistemler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `f69ac1cf3729e33e`

  - **Doğal Sistemler ve Süreçler: İklim ve Hava Olayları (COĞ.9.2.1 - COĞ.9.2.3)** (`lesson_cog9_iklim_ve_hava_olaylari`)
    - [2.1] (VIDEO) **Atmosferin Katmanları ve Sıcaklık Etmenleri**
      - Stable Key: `item_cog9_vid_atmosfer_ve_sicaklik`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `db6d6a33b24d6013`
    - [2.2] (VIDEO) **Basınç Merkezleri, Rüzgârlar ve Yağış Tipleri**
      - Stable Key: `item_cog9_vid_basinc_ruzgar_yagis`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `c40e0c518894cbcd`
    - [Q1] (QUIZ) **Atmosfer ve İklim Elemanları Testi**
      - Stable Key: `cog9_quiz_iklim_atmosfer`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `08ae59ae76adea4f`

  - **Beşerî Sistemler, Nüfus ve Doğal Afetler (COĞ.9.3.1 - COĞ.9.4.3)** (`lesson_cog9_beseri_sistemler_ve_afetler`)
    - [3.1] (VIDEO) **Nüfus Piramitleri, Göçler ve Yerleşme Dokuları**
      - Stable Key: `item_cog9_vid_nufus_ve_yerlesme`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `b4823e5693786d7e`
    - [3.2] (VIDEO) **Doğal Afet Türleri ve Bütüncül Afet Yönetimi**
      - Stable Key: `item_cog9_vid_afet_yonetimi`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `53a3486c1bec7ce8`
    - [Q1] (QUIZ) **Nüfus ve Afet Yönetimi Testi**
      - Stable Key: `cog9_quiz_nufus_afetler`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `5cb5c76f9267a5f7`


### 📚 9. Sınıf İngilizce (`course_ing_9`)
- **Branş:** İngilizce | **Sıra:** 7000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf İngilizce Müfredatı (Themes 1 - 8)
- **Üniteler:**

  - **Theme 1: School Life & Orientation** (`lesson_ing9_orientation_revision`)
    - [1.1] (VIDEO) **İngilizce Cümle Kurma Mantığı & Günlük Rutin İfadeleri**
      - Stable Key: `ing9_vid_cumle_kurma`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `595d65faa853e4c2`
    - [1.2] (VIDEO) **Simple Present vs Present Continuous (Geniş Zaman - Şimdiki Zaman Farkı)**
      - Stable Key: `ing9_vid_simple_present`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `b1ca43fc855c2981`
    - [A1] (ANKI) **9. Sınıf İngilizce 1. Ay Kelime Destesi**
      - Stable Key: `ing9_anki_1ay`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Anki Kart Sayısı: 30
      - Parmak İzi: `3d872d09fd586a4f`
    - [Q1] (QUIZ) **School Life & Present Simple Test**
      - Stable Key: `ing9_quiz_school_life`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `ddea3d89003cd433`

  - **Theme 2 & 3: Personal Life, Appearance & Personality** (`lesson_ing9_personal_life_appearance`)
    - [2.1] (VIDEO) **Describing Physical Appearance and Personality Traits**
      - Stable Key: `item_ing9_vid_appearance_personality`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `5d544133f66ebdeb`
    - [Q1] (QUIZ) **Appearance & Personality Traits Test**
      - Stable Key: `ing9_quiz_appearance_personality`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `3693cba83e5f07b0`

  - **Theme 4 - 8: Family, Jobs, Nature & Future Predictions** (`lesson_ing9_family_and_world`)
    - [3.1] (VIDEO) **Modals: Can, Must, Have to Farkı ve Kullanımı**
      - Stable Key: `ing9_vid_modals`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `08dae6fa8fdeda08`
    - [3.2] (VIDEO) **Used to ve Could Kullanımı (Geçmiş Alışkanlıklar & Yetenek)**
      - Stable Key: `ing9_vid_used_to`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `346b706e304348ab`
    - [Q1] (QUIZ) **Modals and Future Predictions Test**
      - Stable Key: `ing9_quiz_modals_future`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `09ddc308f4888158`


### 📚 9. Sınıf Almanca (`course_alm_9`)
- **Branş:** Almanca | **Sıra:** 8000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Almanca Müfredatı (A1.1 Seviyesi)
- **Üniteler:**

  - **Modul 1: HALLO! & Informationen zur Person (A1.1)** (`lesson_alm9_modul1_hallo`)
    - [1.1] (VIDEO) **Almanca Selamlaşma, Vedalaşma ve Alfabe (Das ABC)**
      - Stable Key: `alm9_vid_selamlasma_alfabe`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `2bafa93bc0d93e82`
    - [1.2] (VIDEO) **Kendini Tanıtma, Adını Söyleme ve Hal-Hatır Sorma**
      - Stable Key: `alm9_vid_kendini_tanitma`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `ff41a131eaf524d1`
    - [1.3] (VIDEO) **0-20 Arası Sayılar (Die Zahlen) ve Telefon Numarası**
      - Stable Key: `alm9_vid_sayilar`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `15fb458cf92cbc54`
    - [1.4] (VIDEO) **Ülkeler, Diller ve Nereli Olduğunu Söyleme (Woher kommst du?)**
      - Stable Key: `alm9_vid_ulkeler_diller`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `c3f21f3eed9df052`
    - [A1] (ANKI) **9. Sınıf Almanca 1. Ay Kelime ve Cümle Destesi**
      - Stable Key: `alm9_anki_1ay`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Anki Kart Sayısı: 20
      - Parmak İzi: `5d6286621c26cf2d`
    - [Q1] (QUIZ) **Hallo & Persönliche Angaben Test**
      - Stable Key: `alm9_quiz_hallo_person`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `71b0641c032b5b4a`

  - **Modul 2 & 3: Meine Schule, Schulsachen & Der Alltag (A1.1)** (`lesson_alm9_schule_und_alltag`)
    - [2.1] (VIDEO) **Die Schulsachen und Artikel (Der, Die, Das / Ein, Eine, Kein)**
      - Stable Key: `item_alm9_vid_schulsachen_artikel`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `4cca94e386ebe6bd`
    - [Q1] (QUIZ) **Schulsachen & Artikel Test**
      - Stable Key: `alm9_quiz_schule_artikel`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `044fe75d3cedcf98`


### 📚 9. Sınıf Türk Dili ve Edebiyatı (`course_tde_9`)
- **Branş:** Türk Dili ve Edebiyatı | **Sıra:** 9000
- **Açıklama:** MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf TDE Müfredatı (Sözün İnceliği, Anlam Arayışı, Yapı Taşları, Roman)
- **Üniteler:**

  - **1. Tema: Sözün İnceliği (Şiir, Deneme ve Söz Sanatları)** (`lesson_tde9_metin_ve_anlam`)
    - [1.1] (VIDEO) **TDE: Metinde Anlam & Söz Sanatları**
      - Stable Key: `tde9_vid_soz_sanatlari`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - URL: https://www.youtube.com/watch?v=NBhw_SkvV8E
      - Parmak İzi: `9482d8d5961e7046`
    - [1.2] (VIDEO) **Edebiyatın Güzel Sanatlarla İlişkisi ve Metin Türleri**
      - Stable Key: `item_tde9_vid_edebiyat_guzel_sanatlar`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `b52a5298377cff88`
    - [Q1] (QUIZ) **Şiir Bilgisi ve Söz Sanatları Testi**
      - Stable Key: `tde9_quiz_sozun_inceligi`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `295cb2593369ac6e`

  - **2. Tema: Anlam Arayışı (Hikâye, Anı ve İletişim)** (`lesson_tde9_anlam_arayisi_hikaye`)
    - [2.1] (VIDEO) **Hikâye Unsurları: Olay ve Durum Hikâyesi Karşılaştırması**
      - Stable Key: `item_tde9_vid_hikaye_turleri`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `97ad2f7839dbed29`
    - [Q1] (QUIZ) **Hikâye Türü ve Anlatım Teknikleri Testi**
      - Stable Key: `tde9_quiz_hikaye_ve_anlam`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `4599998cc177eb73`

  - **3. ve 4. Tema: Anlamın Yapı Taşları, Roman ve Dil Bilgisi** (`lesson_tde9_dilin_yapisi_ve_roman`)
    - [3.1] (VIDEO) **Sözcük Türleri: İsimler, Sıfatlar, Zamirler ve Zarflar**
      - Stable Key: `item_tde9_vid_sozcukte_anlam_isim_sifat`
      - Durum: 🟡 **needs_review** (Yayın: `draft`)
      - Parmak İzi: `b313104e47766801`
    - [Q1] (QUIZ) **Roman ve Sözcük Türleri Testi**
      - Stable Key: `tde9_quiz_dil_bilgisi_roman`
      - Durum: 🟢 **verified** (Yayın: `active`)
      - Soru Sayısı: 3
      - Parmak İzi: `dde2950b5693c3c5`


