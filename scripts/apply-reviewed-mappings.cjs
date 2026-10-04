#!/usr/bin/env node
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..');
const ITEMS_DIR = path.join(ROOT, 'content', 'v2', 'items');
const TRANSCRIPTS_DIR = path.join(ROOT, 'content', 'v2', 'transcripts');
const MANIFEST_PATH = path.join(ROOT, 'content', 'v2', 'transcript-corpus', 'manifest.json');

function sha16(str) {
  return crypto.createHash('sha256').update(str).digest('hex').slice(0, 16);
}

const PRIMARY_METADATA = {
  course_fiz_9: { provider: 'Özcan Aykın FİZİK', teacher: 'Özcan Aykın FİZİK' },
  course_biyo_9: { provider: 'Dr. Biyoloji', teacher: 'Dr. Biyoloji' },
  course_kim_9: { provider: 'Meschemy Kimya', teacher: 'Meschemy Kimya' },
  course_cog_9: { provider: 'Coğrafyanın Kodları', teacher: 'Yunus Hoca' },
  course_tde_9: { provider: 'Deniz Hoca', teacher: 'Deniz Hoca' },
  course_ing_9: { provider: 'Ms. Jasmin ELT', teacher: 'Ms. Jasmin ELT' },
  course_alm_9: { provider: 'tonguç 9. SINIF', teacher: 'Tonguç Akademi' },
  course_mat_9: { provider: 'Rehber Matematik', teacher: 'Deniz Mehmet Hoca' }
};

const MAP = {
  // FİZİK (19)
  "item_fiz9_vid_alt_dallar": "1Hg2eF907YA",
  "item_fiz9_vid_fizik_giris": "1Hg2eF907YA",
  "item_fiz9_vid_topic_01_fizik_bilimi": "1Hg2eF907YA",
  "item_fiz9_vid_topic_05_temel_ve_turetilmis_nicelikler": "7aVrdQ7uSQ4",
  "item_fiz9_vid_dogadaki_kuvvetler": "VwxSW7Rmr2U",
  "item_fiz9_vid_topic_09_hareket_ve_hareket_turleri": "Qp211SqOGJ4",
  "item_fiz9_vid_topic_10_katilarda_basinc": "DnQNBoR_Dsw",
  "item_fiz9_vid_topic_11_sivilarda_basinc": "dkwbOGhBedc",
  "item_fiz9_vid_basinc_prensipleri": "jOKADXphfC8",
  "item_fiz9_vid_kaldirma_kuvveti": "XQHH2VV1Bbk",
  "item_fiz9_vid_topic_13_kaldirma_kuvveti": "XQHH2VV1Bbk",
  "item_fiz9_vid_topic_14_bernoulli_ilkesi": "1Q3CtzfV1lQ",
  "item_fiz9_vid_isi_sicaklik_kavram": "kGrdvkxHxjA",
  "item_fiz9_vid_topic_16_isi_oz_isi_ve_isi_sigasi": "VhqHkBN6xMQ",
  "item_fiz9_vid_hal_degisimi": "3pRu3T0Q03U",
  "item_fiz9_vid_topic_17_hal_degisimi": "3pRu3T0Q03U",
  "item_fiz9_vid_topic_18_isil_denge": "2-aHeYUXvCI",
  "item_fiz9_vid_topic_19_isi_aktarim_yollari": "CRg4b_g1IXc",
  "item_fiz9_vid_topic_20_isi_iletim_hizi": "CRg4b_g1IXc",

  // BİYOLOJİ (16)
  "item_biyo9_vid_bilim_nedir": "V0SzzIeniHc",
  "item_biyo9_vid_kontrollu_deney": "V0SzzIeniHc",
  "item_biyo9_vid_topic_02_bilimsel_arastirma_surecleri_ve_bilim_etigi": "V0SzzIeniHc",
  "item_biyo9_vid_yasam_nedir": "JqVImq7hFjk",
  "item_biyo9_vid_topic_03_canlilarin_ortak_ozellikleri": "JqVImq7hFjk",
  "item_biyo9_vid_topic_04_inorganik_molekuller": "aV3n4v0t2HI",
  "item_biyo9_vid_topic_05_karbonhidratlar": "MgsF9_Yi6es",
  "item_biyo9_vid_topic_06_lipitler": "fG5tAZFYTTM",
  "item_biyo9_vid_topic_07_proteinler": "H_I4wF3hBOg",
  "item_biyo9_vid_topic_08_enzimler": "56fiFbIYsS0",
  "item_biyo9_vid_topic_09_nukleik_asitler": "gqDpv-_j-m4",
  "item_biyo9_vid_topic_10_vitaminler": "9ONeiMJL6VY",
  "item_biyo9_vid_madde_gecisleri": "NYz4uNbiphU",
  "item_biyo9_vid_hucre_organeller": "4wHBI6iwHno",
  "item_biyo9_vid_siniflandirma": "rit0LlD2Fnw",
  "item_biyo9_vid_topic_14_uc_ust_alem_sistemi_ve_canli_gruplari": "rit0LlD2Fnw",

  // KİMYA (16)
  "item_kim9_vid_kimya_bilimi": "VfexRwrzEDA",
  "item_kim9_vid_guvenlik_sembolleri": "3uhN8JlukLg",
  "item_kim9_vid_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik": "3uhN8JlukLg",
  "item_kim9_vid_atom_modelleri": "w4hn63SL3y4",
  "item_kim9_vid_topic_04_atom_orbitalleri_ve_elektron_dizilimi": "hDUv4CkJDCM",
  "item_kim9_vid_topic_05_periyodik_tabloda_yer_bulma": "aVTZGka6GvM",
  "item_kim9_vid_periyodik_ozellikler": "_2Lb0ybajvE",
  "item_kim9_vid_topic_06_periyodik_ozellikler": "_2Lb0ybajvE",
  "item_kim9_vid_topic_07_metalik_bag": "fX3ojMBUxOg",
  "item_kim9_vid_topic_08_iyonik_bag": "F4eYD8UdDXI",
  "item_kim9_vid_topic_09_kovalent_bag": "xJ6i_sC7Mcc",
  "item_kim9_vid_topic_10_lewis_nokta_yapisi": "xJ6i_sC7Mcc",
  "item_kim9_vid_topic_11_molekul_polarligi_ve_apolarligi": "xJ6i_sC7Mcc",
  "item_kim9_vid_zayif_etkilesimler": "fX3ojMBUxOg",
  "item_kim9_vid_topic_14_katilar_ve_ozellikleri": "glDg6lFPsag",
  "item_kim9_vid_topic_15_sivilar_ve_ozellikleri": "wbL70rDWleU",

  // COĞRAFYA (15)
  "item_cog9_vid_doga_insan": "NlHi5ndIjpA",
  "item_cog9_vid_topic_02_nicin_cografya_ogrenmeliyiz": "NlHi5ndIjpA",
  "item_cog9_vid_topic_05_turkiye_nin_cografi_konumu": "ymLvyLld3cs",
  "item_cog9_vid_harita_bilgisi": "pPeiiLXr-aU",
  "item_cog9_vid_atmosfer_ve_sicaklik": "lZDwJNl8OuA",
  "item_cog9_vid_basinc_ruzgar_yagis": "aweDcCk0fJ0",
  "item_cog9_vid_topic_09_iklim_turleri": "9WB1e2c80gA",
  "item_cog9_vid_topic_11_nufusun_tarihsel_degisimi_ve_gelecegi": "4cbRTAG8M4U",
  "item_cog9_vid_topic_12_nufusun_dagilisi_ve_hareketleri": "XwwB4_c182w",
  "item_cog9_vid_topic_13_demografik_donusum_ve_nufus_piramitleri": "iZ2F-uPaDr8",
  "item_cog9_vid_topic_15_ekonomik_faaliyetleri_etkileyen_cografi_faktorler": "dDXeN9Oll4I",
  "item_cog9_vid_afet_yonetimi": "paXdOyeI8zo",
  "item_cog9_vid_topic_16_tehlike_risk_ve_afet": "paXdOyeI8zo",
  "item_cog9_vid_topic_18_butuncul_afet_yonetimi": "paXdOyeI8zo",
  "item_cog9_vid_topic_19_bolge_ve_bolge_siniri": "BM78gvglAmY",

  // TDE (8)
  "item_tde9_vid_edebiyat_guzel_sanatlar": "NBhw_SkvV8E",
  "item_tde9_vid_hikaye_turleri": "xGTH9SQLhrE",
  "item_tde9_vid_soz_sanatlari": "3bW8GrvE-fs",
  "item_tde9_vid_topic_03_sozun_inceligi_mulakat_dinleme_izleme": "BMaVTFcOC2c",
  "item_tde9_vid_topic_02_sozun_inceligi_siiri_betimleme_paragrafina_donusturm": "ij26b-lboNY",
  "item_tde9_vid_topic_06_anlam_arayisi_hikaye_karakteri_sunumu": "3sxZJrvAYIM",
  "item_tde9_vid_topic_07_anlam_arayisi_siir_dinleme_izleme": "jBQ6PMMdPDk",
  "item_tde9_vid_topic_08_anlam_arayisi_siir_yazma": "jBQ6PMMdPDk",

  // İNGİLİZCE (6)
  "item_ing9_vid_appearance_personality": "GjocuWHrXyM",
  "item_ing9_vid_cumle_kurma": "7hlpdxnFQ5o",
  "item_ing9_vid_topic_01_theme_1_school_life": "AhpYrUKeHNE",
  "item_ing9_vid_topic_05_theme_5_life_in_the_house_neighbourhood": "GUM8bs1kRwc",
  "item_ing9_vid_topic_07_theme_7_life_in_the_world_nature": "qXhKi2v5EGM",
  "item_ing9_vid_topic_08_theme_8_life_in_the_universe_future": "yksFI9sPggY",

  // ALMANCA (11)
  "item_alm9_vid_kendini_tanitma": "hRYyziD_8XU",
  "item_alm9_vid_sayilar": "MTYC1fG5Z7Q",
  "item_alm9_vid_schulsachen_artikel": "8ktg5DqzffM",
  "item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer": "MTYC1fG5Z7Q",
  "item_alm9_vid_topic_04_alter_alphabet_und_buchstabieren": "MTYC1fG5Z7Q",
  "item_alm9_vid_topic_05_schulsachen": "8ktg5DqzffM",
  "item_alm9_vid_topic_09_familie_und_familienmitglieder": "4mfOLK3RVT4",
  "item_alm9_vid_topic_10_berufe": "Vgt876VAW78",
  "item_alm9_vid_topic_12_uhrzeiten": "mvhqOxWEmBc",
  "item_alm9_vid_topic_13_tageszeiten_und_tagesablauf": "JPV8sHLi9tY",
  "item_alm9_vid_ulkeler_diller": "R6O9NcPPB2s",

  // MATEMATİK (6)
  "item_mat9_vid_uslu_giris": "ZAqla3j4kLQ",
  "item_mat9_vid_uslu_kurallar": "-OYMlnoW9gw",
  "item_mat9_vid_uslu_bolme": "uwl25uvO1sQ",
  "item_mat9_vid_koklu_mantik": "k9Gx8yy6xSo",
  "item_mat9_vid_sayi_kumeleri": "OLV5wBFLVsU",
  "item_mat9_vid_ucgende_aci": "sZrQZ7YU5gs"
};

const SHARED_REASONS = {
  "1Hg2eF907YA": "Kapsamlı TYT Fizik dersi; fizik bilimine giriş, fiziğin alt dalları ve temel kavramları bir bütün olarak kapsar.",
  "7aVrdQ7uSQ4": "Temel ve türetilmiş büyüklükler (KISA MUZ) ile skaler ve vektörel nicelikler ortak video kapsamı",
  "XQHH2VV1Bbk": "Sıvıların ve gazların kaldırma kuvveti konu anlatımı ve prensipleri tek bir bütünleşik derste işlenir.",
  "3pRu3T0Q03U": "Maddelerde hâl değişimi, erime-donma ve buharlaşma-yoğuşma grafikleri aynı derste eksiksiz anlatılmaktadır.",
  "CRg4b_g1IXc": "Isı aktarım yolları (iletim, konveksiyon, ışıma) ve ısı iletim hızını etkileyen faktörler tek ders videosunda birlikte işlenir.",
  "V0SzzIeniHc": "Bilimsel araştırma basamakları, kontrollü deney düzenekleri ve bilim etiği aynı ders videosunda bütünsel aktarılmaktadır.",
  "JqVImq7hFjk": "Yaşam bilimi biyolojiye giriş ve canlıların 12 ortak özelliği tek derste eksiksiz anlatılmaktadır.",
  "rit0LlD2Fnw": "Canlıların sınıflandırılması ilkeleri, hiyerarşik kategoriler ve üç üst âlem sistemi aynı derste öğretilmektedir.",
  "3uhN8JlukLg": "Kimya laboratuvarında güvenlik kuralları ve tehlike uyarı piktogramları tek ders videosunda birlikte işlenir.",
  "_2Lb0ybajvE": "Atom yarıçapı, iyonlaşma enerjisi, elektron ilgisi ve elektronegatiflik gibi periyodik özelliklerin değişimi aynı videoda anlatılır.",
  "fX3ojMBUxOg": "Metalik bağ modeli ve moleküller arası zayıf etkileşimler (Van der Waals, hidrojen bağı) aynı derste incelenir.",
  "xJ6i_sC7Mcc": "Kovalent bağ oluşumu, Lewis elektron nokta gösterimleri ve moleküllerin polar/apolar niteliği tek derste kapsamlı biçimde işlenir.",
  "NlHi5ndIjpA": "Doğa-insan etkileşimi ve coğrafya öğrenmenin önemi Yunus Hoca tarafından tek ders videosunda ele alınmaktadır.",
  "paXdOyeI8zo": "Doğal afet türleri, tehlike-risk kavramları ve afet yönetim süreçleri tek derste bütüncül olarak öğretilir.",
  "jBQ6PMMdPDk": "Kapsamlı Deniz Hoca şiir dersi; şiirin yapı unsurları, ahenk ögeleri, dinleme/izleme ve yazma süreçlerini birlikte açıklar.",
  "MTYC1fG5Z7Q": "0-20 arası Almanca sayılar, alfabe kodlama ve telefon numarası/yaş sorma diyalogları aynı videoda anlatılmaktadır.",
  "8ktg5DqzffM": "Okul eşyaları (Schulsachen) ve artikeller (der/die/das/ein/eine/kein) aynı Tonguç dersinde birlikte işlenir.",
  "uwl25uvO1sQ": "Üslü ifadelerde bölme işlemi, taban-üs kuralları ve negatif üs mantığı ortak video kapsamı",
  "1-AJn6S0syU": "Coğrafya biliminin konusu, bölümleri, tarihsel gelişimi ve niçin coğrafya öğrenmeliyiz ünitesi ortak video kapsamı",
  "t3MFxbdUS34": "Gerçek sayıların işlem özellikleri, cebirsel ispat ve modelleme ortak video kapsamı",
  "k0yL5iVZiAQ": "Geometrik dönüşümler: öteleme, yansıma, dönme ve simetri eksenleri ortak video kapsamı",
  "50sc1KH0blc": "MEB 9. sınıf Nevruz belgeseli dinleme/izleme ve belgeseli infografiğe dönüştürme atölye ünitesi ortak video kapsamı",
  "JPGuz-gG_3A": "Biyografi ve otobiyografi tür özellikleri, tezkire geleneği ve otobiyografi yazma ilkeleri ortak video kapsamı"
};

// Multi-concept unmapped items
const MULTI_CONCEPT_UNMAPPED = new Set([
  'item_alm9_vid_selamlasma_alfabe',
  'item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde',
  'item_biyo9_vid_enzimler_nukleik',
  'item_biyo9_vid_organik_inorganik',
  'item_cog9_vid_mekansal_dusunme',
  'item_cog9_vid_nufus_ve_yerlesme',
  'item_cog9_vid_topic_07_hava_olaylari_ve_gunluk_hayata_etkileri',
  'item_ing9_vid_simple_present',
  'item_kim9_vid_bag_turleri',
  'item_kim9_vid_katilar_ve_sivilar',
  'item_kim9_vid_topic_12_bilesiklerin_adlandirilmasi',
  'item_tde9_vid_sozcukte_anlam_isim_sifat',
  'item_tde9_vid_topic_13_dilin_zenginligi_roman_tiyatro_ve_elestiri'
]);

function main() {
  const manifest = JSON.parse(fs.readFileSync(MANIFEST_PATH, 'utf8'));
  const vids = new Map(manifest.transcripts.map(t => [t.videoId, t]));

  const itemFiles = fs.readdirSync(ITEMS_DIR).filter(f => f.endsWith('.json'));
  const oldItemMap = new Map();
  for (const f of itemFiles) {
    const item = JSON.parse(fs.readFileSync(path.join(ITEMS_DIR, f), 'utf8'));
    oldItemMap.set(item.id, JSON.parse(JSON.stringify(item)));
  }

  const mappingSummary = {
    generatedAt: new Date().toISOString(),
    totalCourses: 9,
    totalVideoItems: 174,
    mappedVideoItemsCount: 97,
    unresolvedNonHistoryItemsCount: 60,
    historyItemsCount: 17,
    perCourse: {},
    mappedItems: [],
    unresolvedItems: []
  };

  // Step 1: Apply reviewed video mappings
  for (const [itemId, videoId] of Object.entries(MAP)) {
    const itemPath = path.join(ITEMS_DIR, `${itemId}.json`);
    const item = JSON.parse(fs.readFileSync(itemPath, 'utf8'));
    const oldUrl = item.contentUrl;
    const manifestEntry = vids.get(videoId);
    if (!manifestEntry) throw new Error(`Video ID ${videoId} not found in manifest`);

    const primaryMeta = PRIMARY_METADATA[item.courseId] || {};
    const newUrl = `https://www.youtube.com/watch?v=${videoId}`;
    const cleanFp = manifestEntry.cleanSha256.slice(0, 16);

    // Copy clean and raw transcript files
    const txtDest = path.join(TRANSCRIPTS_DIR, `${itemId}.tr.txt`);
    const vttDest = path.join(TRANSCRIPTS_DIR, `${itemId}.tr.tr.vtt`);
    fs.copyFileSync(path.join(ROOT, manifestEntry.cleanPath), txtDest);
    fs.copyFileSync(path.join(ROOT, manifestEntry.rawPath), vttDest);

    item.contentUrl = newUrl;
    item.payload = item.payload || {};
    item.payload.provider = primaryMeta.provider;
    item.payload.teacher = primaryMeta.teacher;
    item.payload.verifiedLectureTitle = manifestEntry.title;

    item.payload.provenance = item.payload.provenance || {};
    item.payload.provenance.sourceVideoUrl = newUrl;
    item.payload.provenance.provider = primaryMeta.provider;
    item.payload.provenance.teacher = primaryMeta.teacher;
    item.payload.provenance.reviewedOverride = true;
    item.payload.provenance.reviewStatus = 'verified';
    delete item.payload.provenance.reviewNote;
    item.payload.provenance.transcriptLanguage = 'tr';
    item.payload.provenance.transcriptKind = 'subtitle';
    item.payload.provenance.transcriptPath = `content/v2/transcripts/${itemId}.tr.txt`;
    item.payload.provenance.transcriptRawPath = `content/v2/transcripts/${itemId}.tr.tr.vtt`;
    item.payload.provenance.transcriptFingerprint = cleanFp;
    item.payload.provenance.fingerprint = sha16([item.stableKey, item.title, item.contentUrl].join('|'));

    fs.writeFileSync(itemPath, JSON.stringify(item, null, 2) + '\n', 'utf8');
  }

  // Step 2: Recalculate sharedSource across all video items
  const allVideoItems = [];
  for (const f of fs.readdirSync(ITEMS_DIR).filter(f => f.endsWith('.json') && !f.endsWith('__quiz.json'))) {
    const item = JSON.parse(fs.readFileSync(path.join(ITEMS_DIR, f), 'utf8'));
    if (item.itemType === 'VIDEO') allVideoItems.push(item);
  }

  const urlMap = new Map();
  for (const v of allVideoItems) {
    if (!v.contentUrl) continue;
    const list = urlMap.get(v.contentUrl) || [];
    list.push(v);
    urlMap.set(v.contentUrl, list);
  }

  for (const [url, items] of urlMap.entries()) {
    const videoId = url.match(/(?:v=|\/)([a-zA-Z0-9_-]{11})/)?.[1];
    const isShared = items.length > 1;

    for (const item of items) {
      if (item.courseId === 'course_tar_9') continue; // Do not touch history items

      item.payload = item.payload || {};
      item.payload.provenance = item.payload.provenance || {};

      if (isShared) {
        item.payload.provenance.sharedSource = true;
        const reason = (videoId && SHARED_REASONS[videoId]) || item.payload.provenance.sharedSourceReason || 'Kapsamlı ders videosu birden fazla müfredat konusunu bütünleşik olarak kapsar.';
        item.payload.provenance.sharedSourceReason = reason;
      } else {
        item.payload.provenance.sharedSource = false;
        delete item.payload.provenance.sharedSourceReason;
      }
      fs.writeFileSync(path.join(ITEMS_DIR, `${item.id}.json`), JSON.stringify(item, null, 2) + '\n', 'utf8');
    }
  }

  // Step 3: Update linked micro-quizzes
  const quizFiles = fs.readdirSync(ITEMS_DIR).filter(f => f.endsWith('__quiz.json'));
  for (const qf of quizFiles) {
    const qPath = path.join(ITEMS_DIR, qf);
    const quizItem = JSON.parse(fs.readFileSync(qPath, 'utf8'));
    const parentId = quizItem.payload?.provenance?.derivedFromItemId;
    if (!parentId) continue;

    const parentPath = path.join(ITEMS_DIR, `${parentId}.json`);
    if (!fs.existsSync(parentPath)) continue;
    const parent = JSON.parse(fs.readFileSync(parentPath, 'utf8'));

    const qProv = quizItem.payload.provenance;
    qProv.sourceVideoUrl = parent.contentUrl;
    qProv.transcriptLanguage = 'tr';
    qProv.transcriptKind = parent.payload?.provenance?.transcriptKind || 'subtitle';
    qProv.reviewStatus = parent.payload?.provenance?.reviewStatus || 'verified';
    if (parent.payload?.provenance?.reviewNote) {
      qProv.reviewNote = parent.payload.provenance.reviewNote;
    } else {
      delete qProv.reviewNote;
    }
    qProv.reviewedOverride = true;
    qProv.groundingType = 'transcript_grounded';
    qProv.transcriptPath = parent.payload?.provenance?.transcriptPath || null;
    qProv.transcriptFingerprint = parent.payload?.provenance?.transcriptFingerprint || null;

    if (parent.payload?.provenance?.sharedSource) {
      qProv.sharedSource = true;
      qProv.sharedSourceReason = parent.payload.provenance.sharedSourceReason;
    } else {
      qProv.sharedSource = false;
      delete qProv.sharedSourceReason;
    }

    fs.writeFileSync(qPath, JSON.stringify(quizItem, null, 2) + '\n', 'utf8');
  }

  // Step 4: Generate content/v2/transcript-first-manual-review.json
  const manualReviewItems = [];
  for (const item of allVideoItems) {
    if (item.courseId === 'course_tar_9') continue; // Non-history only
    if (MAP[item.id]) continue; // Mapped

    let reason;
    if (item.courseId === 'course_mat_9') {
      reason = 'downloaded Rehber Matematik corpus is incomplete beyond powers/roots/sets/straight-line angles';
    } else if (MULTI_CONCEPT_UNMAPPED.has(item.id)) {
      reason = 'card title spans multiple source videos';
    } else {
      reason = 'downloaded primary corpus has no single transcript that fully covers this item';
    }

    manualReviewItems.push({
      itemId: item.id,
      courseId: item.courseId,
      lessonId: item.lessonId,
      stableKey: item.stableKey,
      title: item.title,
      url: item.contentUrl,
      teacher: item.payload?.teacher || item.payload?.provenance?.teacher || null,
      provider: item.payload?.provider || item.payload?.provenance?.provider || null,
      reason
    });
  }

  manualReviewItems.sort((a, b) => a.courseId.localeCompare(b.courseId) || a.itemId.localeCompare(b.itemId));

  const manualReviewPath = path.join(ROOT, 'content', 'v2', 'transcript-first-manual-review.json');
  fs.writeFileSync(manualReviewPath, JSON.stringify({
    version: '1.0.0',
    description: 'Unresolved non-History video items requiring manual review or multi-video split in future phases',
    generatedAt: new Date().toISOString(),
    totalUnresolved: manualReviewItems.length,
    items: manualReviewItems
  }, null, 2) + '\n', 'utf8');

  // Step 5: Build mapping summary
  const courseGroups = {};
  for (const item of allVideoItems) {
    const cId = item.courseId;
    courseGroups[cId] = courseGroups[cId] || { mapped: 0, unresolved: 0, total: 0 };
    courseGroups[cId].total++;
    if (MAP[item.id]) {
      courseGroups[cId].mapped++;
    } else if (cId !== 'course_tar_9') {
      courseGroups[cId].unresolved++;
    }
  }
  mappingSummary.perCourse = courseGroups;

  for (const [itemId, videoId] of Object.entries(MAP)) {
    const itemPath = path.join(ITEMS_DIR, `${itemId}.json`);
    const item = JSON.parse(fs.readFileSync(itemPath, 'utf8'));
    const oldItem = oldItemMap.get(itemId);
    const manifestEntry = vids.get(videoId);

    mappingSummary.mappedItems.push({
      itemId,
      courseId: item.courseId,
      title: item.title,
      oldUrl: oldItem?.contentUrl || null,
      newUrl: item.contentUrl,
      videoTitle: manifestEntry.title,
      teacher: item.payload.teacher,
      provider: item.payload.provider,
      evidenceCleanPath: manifestEntry.cleanPath,
      evidenceRawPath: manifestEntry.rawPath,
      sharedSource: Boolean(item.payload?.provenance?.sharedSource),
      sharedSourceReason: item.payload?.provenance?.sharedSourceReason || null
    });
  }

  mappingSummary.unresolvedItems = manualReviewItems;

  const summaryPath = path.join(ROOT, 'content', 'v2', 'transcript-first-mapping-summary.json');
  fs.writeFileSync(summaryPath, JSON.stringify(mappingSummary, null, 2) + '\n', 'utf8');

  console.log('✅ All transcript-first mappings applied successfully!');
  console.log(`   - Mapped video items: ${mappingSummary.mappedItems.length}`);
  console.log(`   - Unresolved non-History items: ${manualReviewItems.length}`);
  console.log(`   - Per course:`, courseGroups);
}

main();
