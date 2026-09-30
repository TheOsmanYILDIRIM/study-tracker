const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

function computeFingerprint(item) {
  const content = [
    item.stableKey,
    item.itemType,
    item.title,
    item.contentUrl || '',
    JSON.stringify(item.payload?.metadata || {})
  ].join('|');
  return sha256(content);
}

const catalog = {
  schemaVersion: "v2",
  generatedAt: "2026-09-30T19:00:00.000Z",
  gradeLevel: 9,
  academicYear: "2026-2027",
  targetCurriculum: "Türkiye Yüzyılı Maarif Modeli (9. Sınıf / Lise 1)",
  courses: []
};

// 1. MATEMATİK
const matLessons = [
  {
    id: "lesson_mat9_uslu_sayilar",
    stableKey: "lesson_mat9_uslu_sayilar",
    title: "Üslü Sayılar ve Temel Özellikleri (MAT.9.1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_mat9_vid_uslu_giris",
        stableKey: "mat9_vid_uslu_giris",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Üslü Sayılara Giriş (1. Seviye)",
        contentUrl: "https://www.youtube.com/watch?v=kYqP9K0Y0pU",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Cebir Öncesi Temeller",
          plannedWeek: 1,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L12",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_uslu_kurallar",
        stableKey: "mat9_vid_uslu_kurallar",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Üslü İfadelerin Sadeleştirilmesi ve Çarpma",
        contentUrl: "https://www.youtube.com/watch?v=s5Rz-1i0n18",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Üs Kuralları ve Sadeleştirme",
          plannedWeek: 1,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L13",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_uslu_bolme",
        stableKey: "mat9_vid_uslu_bolme",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Üslü Sayılarda Bölme İşlemi ve Özellikleri",
        contentUrl: "https://www.youtube.com/watch?v=Z8mH2nLqE9I",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Bölme Kuralı",
          plannedWeek: 1,
          schoolDay: "SALI",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L14",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_uslu_negatif",
        stableKey: "mat9_vid_uslu_negatif",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Üssü Sıfır, Negatif Sayı veya Kesir Olan Sayılar",
        contentUrl: "https://www.youtube.com/watch?v=9_d8mQJzL4w",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Sıfır ve Negatif Üs",
          plannedWeek: 1,
          schoolDay: "CARSAMBA",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L15",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  },
  {
    id: "lesson_mat9_koklu_sayilar",
    stableKey: "lesson_mat9_koklu_sayilar",
    title: "Köklü Sayılar ve Rasyonel Üsler (MAT.9.1.1)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_mat9_vid_koklu_mantik",
        stableKey: "mat9_vid_koklu_mantik",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Köklü Sayılar ve Üslü Sayılar Arasındaki İlişki",
        contentUrl: "https://www.youtube.com/watch?v=vVj4x9p0m1s",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Köklü Sayı Mantığı",
          plannedWeek: 2,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L20",
            sourceSection: "2. Hafta: Bilimsel Gösterim ve Köklü Sayılara Giriş (MAT.9.1.1)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_rasyonel_usler",
        stableKey: "mat9_vid_rasyonel_usler",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Kesirli Üslü ve Köklü İfadeler",
        contentUrl: "https://www.youtube.com/watch?v=1xNq8o3l4wM",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Rasyonel Üsler",
          plannedWeek: 2,
          schoolDay: "CARSAMBA",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L21",
            sourceSection: "2. Hafta: Bilimsel Gösterim ve Köklü Sayılara Giriş (MAT.9.1.1)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  },
  {
    id: "lesson_mat9_gercek_sayi_araliklari",
    stableKey: "lesson_mat9_gercek_sayi_araliklari",
    title: "Gerçek Sayı Aralıkları ve Küme Sembolleri (MAT.9.1.2)",
    orderKey: 3000.0,
    items: [
      {
        id: "item_mat9_vid_araliklar_gosterim",
        stableKey: "mat9_vid_araliklar_gosterim",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Gerçek Sayı Aralıkları & Gösterim",
        contentUrl: "https://www.youtube.com/watch?v=TNw7eEas9Oo",
        payload: {
          plannedWeek: 3,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L143",
            sourceSection: "3. Hafta: Aralıklar, Haritalar ve Atomun Dünyası",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_aralik_farki",
        stableKey: "mat9_vid_aralik_farki",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "Aralık Farkı ve Eşitsizlik Problemleri",
        contentUrl: "https://www.youtube.com/watch?v=drPKmUSKYCI",
        payload: {
          plannedWeek: 4,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L182",
            sourceSection: "4. Hafta: Bilim İnsanları, Periyodik Sistem ve 1. Ay Kapanışı",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  }
];

// 2. TARİH (Canonical: Mehmet Celal ÖZYILDIZ)
const tarLessons = [
  {
    id: "lesson_tar9_gecmisin_insasi",
    stableKey: "lesson_tar9_gecmisin_insasi",
    title: "Geçmişin İnşa Sürecinde Tarih (TAR.9.1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_tar9_vid_birey_toplum",
        stableKey: "tar9_vid_birey_toplum",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Tarih Öğrenmenin Bireye ve Topluma Faydaları",
        contentUrl: "https://www.youtube.com/watch?v=5QxOpTALmEE",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "00:00",
          endTimestamp: "28:45",
          topicDetails: "Tarihin Tanımı, Konusu, Bireye ve Topluma Faydaları",
          plannedWeek: 1,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L14",
            sourceSection: "1. Hafta: Tarih Öğrenmenin Bireye ve Topluma Faydaları",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_vid_olay_olgu",
        stableKey: "tar9_vid_olay_olgu",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Tarihin Doğası & Olay-Olgu Ayrımı",
        contentUrl: "https://www.youtube.com/watch?v=5QxOpTALmEE",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "28:45",
          endTimestamp: "45:20",
          topicDetails: "Tarihî Olay ve Olgu Ayrımı, Deney/Gözlem Yapılamaması, Objektiflik",
          plannedWeek: 2,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L25",
            sourceSection: "2. Hafta: Tarihin Doğası & Olay-Olgu Ayrımı",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  },
  {
    id: "lesson_tar9_kaynaklar_takvimler",
    stableKey: "lesson_tar9_kaynaklar_takvimler",
    title: "Kaynak Türleri, Tarih Yazıcılığı ve Takvimler (TAR.9.1.2)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_tar9_vid_kaynak_turleri",
        stableKey: "tar9_vid_kaynak_turleri",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Tarihin Doğası, Kaynak Türleri ve Tarih Yazıcılığı",
        contentUrl: "https://www.youtube.com/watch?v=lqEZ19uwyas",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "00:00",
          endTimestamp: "24:30",
          topicDetails: "Birinci/İkinci Elden Kaynaklar, Tarih Yazıcılığı Türleri: Hikâyeci, Öğretici, Araştırmacı",
          plannedWeek: 3,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L36",
            sourceSection: "3. Hafta: Tarihin Doğası, Kaynak Türleri ve Tarih Yazıcılığı",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_vid_yardimci_bilimler",
        stableKey: "tar9_vid_yardimci_bilimler",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Tarihe Yardımcı Bilim Dalları ve Zamanın Taksimi (5 Takvim)",
        contentUrl: "https://www.youtube.com/watch?v=lqEZ19uwyas",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "24:30",
          endTimestamp: "46:15",
          topicDetails: "Kronoloji, Arkeoloji, Epigrafi, Nümizmatik, Diplomatik & 5 Takvim",
          plannedWeek: 4,
          schoolDay: "MON",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L47",
            sourceSection: "4. Hafta: Tarihe Yardımcı Bilim Dalları ve Zamanın Taksimi",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_anki_temel",
        stableKey: "tar9_anki_temel",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 3000.0,
        title: "9. Sınıf Tarih Temel Kavramlar Anki Kartları",
        contentUrl: null,
        payload: {
          ankiSource: "9_sinif_tarih_anki.txt",
          cardCount: 10,
          plannedWeek: 4,
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L50",
            sourceSection: "4. Hafta",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  }
];

// 3. COĞRAFYA
const cogLessons = [
  {
    id: "lesson_cog9_dogal_sistemler",
    stableKey: "lesson_cog9_dogal_sistemler",
    title: "Doğal Sistemler ve Coğrafyanın İlkeleri (COĞ.9.1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_cog9_vid_doga_insan",
        stableKey: "cog9_vid_doga_insan",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri",
        contentUrl: "https://www.youtube.com/@cografyaninkodlari",
        payload: {
          teacher: "Coğrafyanın Kodları",
          startTimestamp: "04:58",
          endTimestamp: "33:02",
          topicDetails: "4 Temel Doğal Ortam & Fiziki Coğrafyanın Alt Dalları",
          plannedWeek: 1,
          schoolDay: "THU",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L14",
            sourceSection: "1. Hafta: Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review",
            auditNote: "Channel homepage URL used in source instead of direct video ID; transcript timestamps verified."
          }
        }
      },
      {
        id: "item_cog9_vid_mekansal_dusunme",
        stableKey: "cog9_vid_mekansal_dusunme",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Doğa-İnsan Etkileşimi & Mekânsal Düşünme",
        contentUrl: "https://www.youtube.com/@cografyaninkodlari",
        payload: {
          teacher: "Coğrafyanın Kodları",
          startTimestamp: "09:08",
          endTimestamp: "20:00",
          topicDetails: "İnsan ve Doğa Etkileşimi & Coğrafyanın Temel İlkeleri (Dağılış, Nedensellik, İlgi)",
          plannedWeek: 2,
          schoolDay: "THU",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L26",
            sourceSection: "2. Hafta: Doğa-İnsan Etkileşimi & Mekânsal Düşünme",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review",
            auditNote: "Channel homepage URL used in source instead of direct video ID."
          }
        }
      },
      {
        id: "item_cog9_vid_tarihsel_gelisim",
        stableKey: "cog9_vid_tarihsel_gelisim",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Coğrafya Biliminin Tarihsel Gelişimi",
        contentUrl: "https://www.youtube.com/@cografyaninkodlari",
        payload: {
          teacher: "Coğrafyanın Kodları",
          startTimestamp: "01:15",
          endTimestamp: "18:07",
          topicDetails: "İlk Çağ, Orta Çağ İslam Coğrafyası ve Osmanlı Coğrafyacıları",
          plannedWeek: 3,
          schoolDay: "THU",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L37",
            sourceSection: "3. Hafta: Coğrafya Biliminin Tarihsel Gelişimi",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_vid_harita_bilgisi",
        stableKey: "cog9_vid_harita_bilgisi",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Mekânın Aynası Haritalar & Projeksiyon Yöntemleri",
        contentUrl: "https://www.youtube.com/@cografyaninkodlari",
        payload: {
          teacher: "Coğrafyanın Kodları",
          startTimestamp: "00:00",
          endTimestamp: "32:45",
          topicDetails: "Harita Elemanları, Projeksiyon Yöntemleri ve Bozulmalar",
          plannedWeek: 4,
          schoolDay: "THU",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L48",
            sourceSection: "4. Hafta: Mekânın Aynası Haritalar & Projeksiyon Yöntemleri",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_anki_1ay",
        stableKey: "cog9_anki_1ay",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 5000.0,
        title: "9. Sınıf Coğrafya 1. Ay Anki Destesi",
        contentUrl: null,
        payload: {
          ankiPackage: "9_sinif_cografya_1_ay.apkg",
          cardCount: 24,
          plannedWeek: 4,
          provenance: {
            sourceRef: "10-Projects/9_sinif_cografya_1_ay_plani.md#L76",
            sourceSection: "3. Anki Tekrar ve Pekiştirme Stratejisi",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  }
];

// 4. FİZİK
const fizLessons = [
  {
    id: "lesson_fiz9_fizik_bilimine_giris",
    stableKey: "lesson_fiz9_fizik_bilimine_giris",
    title: "Fizik Bilimi, Alt Dalları ve Vektörler (FİZ.9.1.1 - FİZ.9.2.4)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_fiz9_vid_fizik_giris",
        stableKey: "fiz9_vid_fizik_giris",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Fizik Bilimine Giriş (Temel Prensipler)",
        contentUrl: "https://www.youtube.com/watch?v=sO7N-V4T_YI",
        payload: {
          provider: "Khan Academy Türkçe",
          plannedWeek: 1,
          schoolDay: "FRI",
          provenance: {
            sourceRef: "10-Projects/1_ay_fizik_video_rehberi.md#L18",
            sourceSection: "1. Hafta: Fizik Bilimi ve Alt Dalları",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_fiz9_vid_vektorler",
        stableKey: "fiz9_vid_vektorler",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Vektörel ve Skaler Büyüklükler (Görsel Koordinat Anlatımı)",
        contentUrl: "https://www.youtube.com/watch?v=sO7N-V4T_YI",
        payload: {
          provider: "Khan Academy Türkçe",
          plannedWeek: 3,
          schoolDay: "FRI",
          provenance: {
            sourceRef: "10-Projects/1_ay_fizik_video_rehberi.md#L40",
            sourceSection: "3. Hafta: Vektörler ve Dik Kartezyen Koordinat Sistemi",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified",
            auditNote: "Reused video URL in source for fundamental vectors overview."
          }
        }
      }
    ]
  }
];

// 5. KİMYA
const kimLessons = [
  {
    id: "lesson_kim9_kimya_bilimi",
    stableKey: "lesson_kim9_kimya_bilimi",
    title: "Kimya Bilimi, Güvenlik ve Atom Teorileri (KİM.9.1.1 - KİM.9.1.3)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_kim9_vid_kimya_bilimi",
        stableKey: "kim9_vid_kimya_bilimi",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Kimya Bilimi ve Alt Disiplinleri",
        contentUrl: "https://www.youtube.com/@BenimHocamLise",
        payload: {
          teacher: "Görkem Şahin",
          channel: "Benim Hocam Lise",
          plannedWeek: 1,
          schoolDay: "FRI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_kimya_video_rehberi.md#L24",
            sourceSection: "1. Hafta (Kitap: Kimya Hayattır)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_vid_guvenlik_sembolleri",
        stableKey: "kim9_vid_guvenlik_sembolleri",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Laboratuvar Güvenlik Kuralları ve Uyarı Sembolleri",
        contentUrl: "https://www.youtube.com/@BenimHocamLise",
        payload: {
          teacher: "Görkem Şahin",
          channel: "Benim Hocam Lise",
          plannedWeek: 2,
          schoolDay: "FRI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_kimya_video_rehberi.md#L25",
            sourceSection: "2. Hafta (Kitap: Güvenlik)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_vid_atom_modelleri",
        stableKey: "kim9_vid_atom_modelleri",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Atom Modelleri (Dalton, Thomson, Rutherford, Bohr)",
        contentUrl: "https://www.youtube.com/@BenimHocamLise",
        payload: {
          teacher: "Görkem Şahin",
          channel: "Benim Hocam Lise",
          plannedWeek: 3,
          schoolDay: "FRI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_kimya_video_rehberi.md#L26",
            sourceSection: "3. Hafta (Kitap: Atom Teorileri)",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      }
    ]
  }
];

// 6. BİYOLOJİ
const biyoLessons = [
  {
    id: "lesson_biyo9_yasam",
    stableKey: "lesson_biyo9_yasam",
    title: "Yaşam Teması ve Bilimin Doğası (BİY.9.1.1 - BİY.9.1.2)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_biyo9_vid_yasam_nedir",
        stableKey: "biyo9_vid_yasam_nedir",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Biyolojiye Giriş: Yaşam Nedir?",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/introduction-to-biology",
        payload: {
          provider: "Khan Academy",
          plannedWeek: 1,
          schoolDay: "THU",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L14",
            sourceSection: "1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_vid_bilim_nedir",
        stableKey: "biyo9_vid_bilim_nedir",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Bilim Nedir ve Nasıl Çalışır? (Bilimsel Yöntem)",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/the-scientific-method",
        payload: {
          provider: "Khan Academy",
          plannedWeek: 2,
          schoolDay: "THU",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L15",
            sourceSection: "1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_vid_kontrollu_deney",
        stableKey: "biyo9_vid_kontrollu_deney",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Kontrollü Deney Örnekleri",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/controlled-experiments",
        payload: {
          provider: "Khan Academy",
          plannedWeek: 3,
          schoolDay: "THU",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L16",
            sourceSection: "1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_vid_teori_yasa",
        stableKey: "biyo9_vid_teori_yasa",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Hipotez, Teori ve Kanun Arasındaki Fark",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/hypotheses-theories-and-laws",
        payload: {
          provider: "Khan Academy",
          plannedWeek: 4,
          schoolDay: "THU",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L21",
            sourceSection: "3-4. Hafta: Biyolojide Dönüm Noktaları & Bilimsel Bilginin Gelişimi",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  }
];

// 7. İNGİLİZCE
const ingLessons = [
  {
    id: "lesson_ing9_orientation_revision",
    stableKey: "lesson_ing9_orientation_revision",
    title: "Orientation, Grammar & Vocabulary (Theme 1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_ing9_vid_cumle_kurma",
        stableKey: "ing9_vid_cumle_kurma",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "İngilizce Cümle Kurma Mantığı & Günlük Rutin İfadeleri",
        contentUrl: "https://www.youtube.com/@OzerKiraz",
        payload: {
          teacher: "Özer Kiraz",
          plannedWeek: 1,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L18",
            sourceSection: "1. Hafta: Orientation & Okul Yaşamına Giriş",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_vid_simple_present",
        stableKey: "ing9_vid_simple_present",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Simple Present vs Present Continuous (Geniş Zaman - Şimdiki Zaman Farkı)",
        contentUrl: "https://www.youtube.com/@OzerKiraz",
        payload: {
          teacher: "Özer Kiraz",
          plannedWeek: 2,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L29",
            sourceSection: "2. Hafta: Geniş Zaman vs Şimdiki Zaman",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_vid_modals",
        stableKey: "ing9_vid_modals",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Modals: Can, Must, Have to Farkı ve Kullanımı",
        contentUrl: "https://www.youtube.com/@OzerKiraz",
        payload: {
          teacher: "Özer Kiraz",
          plannedWeek: 3,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L40",
            sourceSection: "3. Hafta: Yetenek ve Zorunluluk Kipleri",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_vid_used_to",
        stableKey: "ing9_vid_used_to",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Used to ve Could Kullanımı (Geçmiş Alışkanlıklar & Kibar İstekler)",
        contentUrl: "https://www.youtube.com/@OzerKiraz",
        payload: {
          teacher: "Özer Kiraz",
          plannedWeek: 4,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L51",
            sourceSection: "4. Hafta: Geçmiş Alışkanlıklar & Kibar İstekler",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_anki_1ay",
        stableKey: "ing9_anki_1ay",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 5000.0,
        title: "9. Sınıf İngilizce 1. Ay Kelime Destesi",
        contentUrl: null,
        payload: {
          ankiPackage: "9_sinif_ingilizce_1_ay.apkg",
          wordCount: 150,
          plannedWeek: 4,
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_kelime_listesi.md#L1",
            sourceSection: "9. Sınıf İngilizce 1. Ay Kapsamlı Kelime Listesi",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  }
];

// 8. ALMANCA
const almLessons = [
  {
    id: "lesson_alm9_modul1_hallo",
    stableKey: "lesson_alm9_modul1_hallo",
    title: "Modul 1: HALLO! & Informationen zur Person (A1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_alm9_vid_selamlasma_alfabe",
        stableKey: "alm9_vid_selamlasma_alfabe",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Almanca Selamlaşma, Vedalaşma ve Alfabe (Das ABC)",
        contentUrl: "https://www.youtube.com/@AlmancaKolay",
        payload: {
          teacher: "Erhan Özdemir (Almanca Kolay)",
          plannedWeek: 1,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L20",
            sourceSection: "1. Hafta: Selamlaşma / Vedalaşma ve Alfabe",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_vid_kendini_tanitma",
        stableKey: "alm9_vid_kendini_tanitma",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Kendini Tanıtma, Adını Söyleme ve Hal-Hatır Sorma",
        contentUrl: "https://www.youtube.com/@AlmancaKolay",
        payload: {
          teacher: "Erhan Özdemir (Almanca Kolay)",
          plannedWeek: 2,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L30",
            sourceSection: "2. Hafta: Kendini Tanıtma ve Hal-Hatır",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_vid_sayilar",
        stableKey: "alm9_vid_sayilar",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "0-20 Arası Sayılar (Die Zahlen) ve Telefon Numarası",
        contentUrl: "https://www.youtube.com/@AlmancaKolay",
        payload: {
          teacher: "Erhan Özdemir (Almanca Kolay)",
          plannedWeek: 3,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L40",
            sourceSection: "3. Hafta: Sayılar ve Telefon Numarası",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_vid_ulkeler_diller",
        stableKey: "alm9_vid_ulkeler_diller",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Ülkeler, Diller ve Nereli Olduğunu Söyleme (Woher kommst du?)",
        contentUrl: "https://www.youtube.com/@AlmancaKolay",
        payload: {
          teacher: "Erhan Özdemir (Almanca Kolay)",
          plannedWeek: 4,
          schoolDay: "SALI",
          channelUrl: true,
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L50",
            sourceSection: "4. Hafta: Ülkeler, Diller ve İkamet",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_anki_1ay",
        stableKey: "alm9_anki_1ay",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 5000.0,
        title: "9. Sınıf Almanca 1. Ay Kelime ve Cümle Destesi",
        contentUrl: null,
        payload: {
          ankiPackage: "9_sinif_almanca_1_ay.apkg",
          wordCount: 120,
          plannedWeek: 4,
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_kelime_listesi.md#L1",
            sourceSection: "9. Sınıf Almanca (A1.1) 1. Ay Kelime Rehberi",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      }
    ]
  }
];

// 9. TÜRK DİLİ VE EDEBİYATI
const tdeLessons = [
  {
    id: "lesson_tde9_metin_ve_anlam",
    stableKey: "lesson_tde9_metin_ve_anlam",
    title: "Metin İnceleme, Anlam ve Yazım Kuralları",
    orderKey: 1000.0,
    items: [
      {
        id: "item_tde9_vid_soz_sanatlari",
        stableKey: "tde9_vid_soz_sanatlari",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "TDE: Metinde Anlam & Söz Sanatları",
        contentUrl: "https://www.youtube.com/watch?v=NBhw_SkvV8E",
        payload: {
          plannedWeek: 2,
          schoolDay: "SALI",
          provenance: {
            sourceRef: "10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L87",
            sourceSection: "2. Hafta: Alt Dallar, Güvenlik ve Anlamlandırma",
            importedAt: "2026-09-30T19:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review",
            auditNote: "Reused generic video URL across weeks in legacy study plan."
          }
        }
      }
    ]
  }
];

const courseDefs = [
  {
    id: "course_mat_9",
    stableKey: "course_mat_9",
    title: "9. Sınıf Matematik",
    subject: "Matematik",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Matematik Müfredatı (Sayılar Teması)",
    orderKey: 1000.0,
    lessons: matLessons
  },
  {
    id: "course_fiz_9",
    stableKey: "course_fiz_9",
    title: "9. Sınıf Fizik",
    subject: "Fizik",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Fizik Müfredatı (Fizik Bilimi ve Kuvvet)",
    orderKey: 2000.0,
    lessons: fizLessons
  },
  {
    id: "course_kim_9",
    stableKey: "course_kim_9",
    title: "9. Sınıf Kimya",
    subject: "Kimya",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Kimya Müfredatı (Kimya Bilimi ve Atom)",
    orderKey: 3000.0,
    lessons: kimLessons
  },
  {
    id: "course_biyo_9",
    stableKey: "course_biyo_9",
    title: "9. Sınıf Biyoloji",
    subject: "Biyoloji",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Biyoloji Müfredatı (Yaşam Teması)",
    orderKey: 4000.0,
    lessons: biyoLessons
  },
  {
    id: "course_tar_9",
    stableKey: "course_tar_9",
    title: "9. Sınıf Tarih",
    subject: "Tarih",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Tarih Müfredatı (Mehmet Celal ÖZYILDIZ Kanonik)",
    orderKey: 5000.0,
    lessons: tarLessons
  },
  {
    id: "course_cog_9",
    stableKey: "course_cog_9",
    title: "9. Sınıf Coğrafya",
    subject: "Coğrafya",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Coğrafya Müfredatı (Doğal Sistemler)",
    orderKey: 6000.0,
    lessons: cogLessons
  },
  {
    id: "course_ing_9",
    stableKey: "course_ing_9",
    title: "9. Sınıf İngilizce",
    subject: "İngilizce",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf İngilizce Müfredatı (Theme 1)",
    orderKey: 7000.0,
    lessons: ingLessons
  },
  {
    id: "course_alm_9",
    stableKey: "course_alm_9",
    title: "9. Sınıf Almanca",
    subject: "Almanca",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Almanca Müfredatı (A1.1 Hallo!)",
    orderKey: 8000.0,
    lessons: almLessons
  },
  {
    id: "course_tde_9",
    stableKey: "course_tde_9",
    title: "9. Sınıf Türk Dili ve Edebiyatı",
    subject: "Türk Dili ve Edebiyatı",
    gradeLevel: 9,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf TDE Müfredatı",
    orderKey: 9000.0,
    lessons: tdeLessons
  }
];

// Add fingerprints and attach courseId/lessonId references
courseDefs.forEach(course => {
  course.lessons.forEach(lesson => {
    lesson.courseId = course.id;
    lesson.items.forEach(item => {
      item.lessonId = lesson.id;
      item.payload = item.payload || {};
      item.payload.provenance = item.payload.provenance || {};
      item.payload.provenance.fingerprint = computeFingerprint(item);
    });
  });
});

catalog.courses = courseDefs;

// Write catalog JSON
const outJsonPath = path.join(__dirname, '../content/9-sinif-v2-catalog.json');
fs.writeFileSync(outJsonPath, JSON.stringify(catalog, null, 2) + '\n', 'utf8');
console.log(`✅ Saved catalog manifest to ${outJsonPath}`);

// Generate Human-Readable Sources Audit Markdown
const totalCourses = catalog.courses.length;
const totalLessons = catalog.courses.reduce((acc, c) => acc + c.lessons.length, 0);
const allItems = catalog.courses.flatMap(c => c.lessons.flatMap(l => l.items));
const totalItems = allItems.length;
const videoCount = allItems.filter(i => i.itemType === 'VIDEO').length;
const ankiCount = allItems.filter(i => i.itemType === 'ANKI').length;
const quizCount = allItems.filter(i => i.itemType === 'QUIZ').length;
const verifiedCount = allItems.filter(i => i.payload?.provenance?.reviewStatus === 'verified').length;
const reviewCount = allItems.filter(i => i.payload?.provenance?.reviewStatus === 'needs_review').length;

const sourcesMd = `# 9. Sınıf StudyTracker V2 Müfredat & İçerik Kaynakları Denetim Raporu

**Oluşturulma Tarihi:** 2026-09-30T19:00:00Z  
**Müfredat:** MEB Türkiye Yüzyılı Maarif Modeli (9. Sınıf / Lise 1)  
**Tohum Manifestosu:** [\`content/9-sinif-v2-catalog.json\`](file:///data/data/com.termux/files/home/projects/study-tracker/content/9-sinif-v2-catalog.json)

---

## 📊 Genel Özet & Metrikler

| Metrik | Adet |
|---|---|
| **Toplam Ders (Course)** | **${totalCourses}** |
| **Toplam Ünite / Konu (Lesson)** | **${totalLessons}** |
| **Toplam Öğrenme Öğesi (Learning Item)** | **${totalItems}** |
| 🎬 **Video Sayısı (VIDEO)** | **${videoCount}** |
| 📇 **Anki Destesi (ANKI)** | **${ankiCount}** |
| 📝 **Sınav / Test (QUIZ)** | **${quizCount}** |
| 🟢 **Doğrulanmış (Verified)** | **${verifiedCount}** |
| 🟡 **İnceleme Bekleyen (Needs Review)** | **${reviewCount}** |

---

## ⚖️ Temel İlke ve Kanonik Kurallar

1. **Tarih Dersi Kanonik Öğretmen Kuralı (Mehmet Celal ÖZYILDIZ):**
   - Tarih dersi için \`10-Projects/1_ay_tarih_video_rehberi.md\` dosyasındaki **Mehmet Celal ÖZYILDIZ** (*Benim Hocam*) videoları (\`5QxOpTALmEE\`, \`lqEZ19uwyas\`) kanonik kabul edilmiştir.
   - Eski/çakışan şablonlardaki (örn. Ramis Hoca veya 2025 şablonları) kaynaklar aktif tohumdan çıkarılmıştır.
2. **Değişebilir Güncel İçerikler:**
   - Matematik (Khan Academy), Fizik, Kimya, Biyoloji, Coğrafya, İngilizce ve Almanca kaynakları güncel içeriğe göre yapılandırılmış olup, gelecekte \`update-content\` ile yeni versiyonlar üretilebilir.
3. **Uydurma Veri Yasağı (No Fabricated Content):**
   - Kaynaklarda soru metni bulunmayan, sadece soru sayısı belirtilen başlıklar için sahte quiz sorusu **uydurulmamıştır**.
   - URL'si belirsiz veya kanal ana sayfası olan video eşleşmeleri \`needs_review\` olarak etiketlenmiştir.
4. **Anki Entegrasyonu:**
   - Yalnızca Vault içinde fiziksel karşılığı olan (\`9_sinif_cografya_1_ay.apkg\`, \`9_sinif_ingilizce_1_ay.apkg\`, \`9_sinif_almanca_1_ay.apkg\`, \`9_sinif_tarih_anki.txt\`) desteler kataloğa dahil edilmiştir.

---

## 🔍 Ders ve Kaynak Bazlı Denetim Detayları

${catalog.courses.map(course => `
### 📚 ${course.title} (\`${course.id}\`)
- **Branş:** ${course.subject} | **Sıra:** ${course.orderKey}
- **Açıklama:** ${course.description}
- **Üniteler:**
${course.lessons.map(lesson => `
  - **${lesson.title}** (\`${lesson.id}\`)
${lesson.items.map(item => `    - [${item.displayLabel}] (${item.itemType}) **${item.title}**
      - Stable Key: \`${item.stableKey}\`
      - URL: ${item.contentUrl ? item.contentUrl : '*Dahili Deste*'}
      - Kaynak: \`${item.payload?.provenance?.sourceRef || 'N/A'}\` (${item.payload?.provenance?.sourceSection || ''})
      - Durum: **${item.payload?.provenance?.reviewStatus}** ${item.payload?.provenance?.auditNote ? `*(Uyarı: ${item.payload.provenance.auditNote})*` : ''}
      - Parmak İzi (Fingerprint): \`${item.payload?.provenance?.fingerprint}\``).join('\n')}`).join('\n')}`).join('\n')}

---

## ⚠️ Denetim Uyarıları & Tespit Edilen Anomaliler (Audit Warnings)

1. **Yinelenen Video URL'leri (Repeated Video URLs across Topics):**
   - Eski \`9_sinif_4_haftalik_studytracker_calisma_plani.md\` şablonunda tüm Biyoloji haftaları için \`nn2uXdrHDMo\`, Coğrafya için \`rBGiYJ4Z89Y\`, Fizik için \`5pmUa7MZLuo\` ve Kimya için \`CdjnEn8X9ZM\` kullanılmıştır.
   - V2 Tohumunda bu tekrarlar elenmiş, branş rehberi dosyalarındaki gerçek Khan Academy veya transkriptli videolar önceliklendirilmiştir.
2. **Kanal Ana Sayfası Bağlantıları (Channel Homepage URLs):**
   - Coğrafya (\`@cografyaninkodlari\`), İngilizce (\`@OzerKiraz\`), Almanca (\`@AlmancaKolay\`) ve Kimya (\`@BenimHocamLise\`) rehberlerinde spesifik video ID yerine kanal ana sayfası linkleri mevcuttur.
   - Bu öğeler \`reviewStatus: needs_review\` olarak işaretlenmiştir.
3. **Müfredat Bağımsızlığı & Puzzle Sıralama:**
   - Tüm öğeler \`order_key\` ile modüler yapıya kavuşturulmuştur. İleride araya \`Quiz 17.2\` gibi testler eklenirken var olan öğelerin \`id\` ve \`stable_key\` değerleri değişmez.
`;

const outSourcesPath = path.join(__dirname, '../content/9-sinif-v2-catalog.sources.md');
fs.writeFileSync(outSourcesPath, sourcesMd, 'utf8');
console.log(`✅ Saved sources audit report to ${outSourcesPath}`);
