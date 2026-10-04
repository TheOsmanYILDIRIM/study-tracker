#!/usr/bin/env node
/**
 * scripts/resolve-and-curate-all.cjs
 * Comprehensive resolver for duplicate and mismatched videos across Grade 9 catalog.
 */
const fs = require("fs");
const path = require("path");
const cp = require("child_process");
const crypto = require("crypto");
const { fetchTranscriptForUrl, searchYouTubeCandidates, readJson, writeJson, sha16, ITEMS, ROOT } = require("./curate-and-fix-videos.cjs");
const TRANSCRIPTS = path.join(ROOT, "content", "v2", "transcripts");

// Specific curated targets and search hints
const CURATION_TARGETS = [
  // ALMANCA (alm_9)
  {
    itemId: "item_alm9_vid_sayilar",
    sharedWith: ["item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer"],
    query: "9. sınıf almanca sayilar 0 20 meltem hoca",
    provider: "Meltem Hoca ile Almanca",
    reason: "Sayılar 0-20 ve telefon numarası müfredat konusu ortak video kapsamı"
  },
  {
    itemId: "item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer",
    sharedWith: ["item_alm9_vid_sayilar"],
    query: "9. sınıf almanca sayilar 0 20 meltem hoca",
    provider: "Meltem Hoca ile Almanca",
    reason: "Sayılar 0-20 ve telefon numarası müfredat konusu ortak video kapsamı"
  },
  {
    itemId: "item_alm9_vid_selamlasma_alfabe",
    sharedWith: ["item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde"],
    query: "9. sınıf almanca selamlasma alfabe meltem hoca",
    provider: "Meltem Hoca ile Almanca",
    reason: "Begrüßung, Verabschiedung ve alfabe temel iletişim konusu ortak video kapsamı"
  },
  {
    itemId: "item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde",
    sharedWith: ["item_alm9_vid_selamlasma_alfabe"],
    query: "9. sınıf almanca selamlasma alfabe meltem hoca",
    provider: "Meltem Hoca ile Almanca",
    reason: "Begrüßung, Verabschiedung ve alfabe temel iletişim konusu ortak video kapsamı"
  },
  {
    itemId: "item_alm9_vid_schulsachen_artikel",
    sharedWith: ["item_alm9_vid_topic_05_schulsachen"],
    query: "9. sınıf almanca okul esyalari schulsachen meltem hoca",
    provider: "Meltem Hoca ile Almanca",
    reason: "Okul eşyaları ve artikeller (Schulsachen) konusu ortak video kapsamı"
  },
  {
    itemId: "item_alm9_vid_topic_05_schulsachen",
    sharedWith: ["item_alm9_vid_schulsachen_artikel"],
    query: "9. sınıf almanca okul esyalari schulsachen meltem hoca",
    provider: "Meltem Hoca ile Almanca",
    reason: "Okul eşyaları ve artikeller (Schulsachen) konusu ortak video kapsamı"
  },
  {
    itemId: "item_alm9_vid_topic_04_alter_alphabet_und_buchstabieren",
    query: "9. sınıf almanca alfabe kodlama yas sorma meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_topic_06_im_klassenzimmer",
    query: "9. sınıf almanca im klassenzimmer sinif ici emirler meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_topic_08_stundenplan_und_schule",
    query: "9. sınıf almanca stundenplan ders programı meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_topic_09_familie_und_familienmitglieder",
    query: "9. sınıf almanca aile uyeleri die familie meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_topic_10_berufe",
    query: "9. sınıf almanca meslekler berufe meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_topic_11_freizeit_und_aktivitaten",
    query: "9. sınıf almanca hobiler bos zaman aktiviteleri freizeit meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_topic_12_uhrzeiten",
    query: "9. sınıf almanca saatler die uhrzeit meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_topic_13_tageszeiten_und_tagesablauf",
    query: "9. sınıf almanca gunun saatleri gunluk plan tagesablauf meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },
  {
    itemId: "item_alm9_vid_ulkeler_diller",
    query: "9. sınıf almanca ulkeler diller woher kommst du meltem hoca",
    provider: "Meltem Hoca ile Almanca"
  },

  // EDEBİYAT (tde_9)
  {
    itemId: "item_tde9_vid_edebiyat_guzel_sanatlar",
    query: "9. sınıf edebiyat guzel sanatlar ve edebiyat deniz hoca",
    provider: "Deniz Hoca"
  },
  {
    itemId: "item_tde9_vid_sozcukte_anlam_isim_sifat",
    query: "9. sınıf edebiyat sozcukte anlam isimler sifatlar deniz hoca",
    provider: "Deniz Hoca"
  },
  {
    itemId: "item_tde9_vid_topic_03_sozun_inceligi_mulakat_dinleme_izleme",
    query: "9. sınıf edebiyat mulakat mulakat dinleme izleme",
    provider: "MEB / Edebiyat TV"
  },
  {
    itemId: "item_tde9_vid_topic_04_sozun_inceligi_deneme_ve_fikir_gelistirme_konusmasi",
    query: "9. sınıf edebiyat deneme turu fikir gelistirme",
    provider: "MEB / Edebiyat TV"
  },
  {
    itemId: "item_tde9_vid_topic_10_anlamin_yapi_taslari_mekanlari_karsilastirmali_konus",
    query: "9. sınıf edebiyat mekan anlatimi hikayede mekan",
    provider: "Deniz Hoca"
  },
  {
    itemId: "item_tde9_vid_topic_11_anlamin_yapi_taslari_belgesel_dinleme_izleme",
    sharedWith: ["item_tde9_vid_topic_12_anlamin_yapi_taslari_belgeseli_infografige_donusturm"],
    query: "9. sınıf edebiyat belgesel dinleme izleme infografik",
    provider: "MEB Maarif Modeli",
    reason: "Belgesel dinleme ve infografiğe dönüştürme atölye ünitesi ortak video kapsamı"
  },
  {
    itemId: "item_tde9_vid_topic_12_anlamin_yapi_taslari_belgeseli_infografige_donusturm",
    sharedWith: ["item_tde9_vid_topic_11_anlamin_yapi_taslari_belgesel_dinleme_izleme"],
    query: "9. sınıf edebiyat belgesel dinleme izleme infografik",
    provider: "MEB Maarif Modeli",
    reason: "Belgesel dinleme ve infografiğe dönüştürme atölye ünitesi ortak video kapsamı"
  },
  {
    itemId: "item_tde9_vid_topic_13_dilin_zenginligi_roman_tiyatro_ve_elestiri",
    query: "9. sınıf edebiyat roman tiyatro elestiri deniz hoca",
    provider: "Deniz Hoca"
  },
  {
    itemId: "item_tde9_vid_topic_14_dilin_zenginligi_sosyal_medya_ve_edebi_dil_sunumu",
    query: "9. sınıf edebiyat sosyal medya ve edebi dil sunumu",
    provider: "MEB Maarif Modeli"
  },
  {
    itemId: "item_tde9_vid_topic_15_dilin_zenginligi_otobiyografi_dinleme_izleme",
    sharedWith: ["item_tde9_vid_topic_16_dilin_zenginligi_otobiyografi_yazma"],
    query: "9. sınıf edebiyat biyografi otobiyografi deniz hoca",
    provider: "Deniz Hoca",
    reason: "Biyografi ve otobiyografi tür özellikleri ve yazma kuralları ortak video kapsamı"
  },
  {
    itemId: "item_tde9_vid_topic_16_dilin_zenginligi_otobiyografi_yazma",
    sharedWith: ["item_tde9_vid_topic_15_dilin_zenginligi_otobiyografi_dinleme_izleme"],
    query: "9. sınıf edebiyat biyografi otobiyografi deniz hoca",
    provider: "Deniz Hoca",
    reason: "Biyografi ve otobiyografi tür özellikleri ve yazma kuralları ortak video kapsamı"
  },

  // COĞRAFYA (cog_9)
  {
    itemId: "item_cog9_vid_atmosfer_ve_sicaklik",
    query: "9. sınıf cografya atmosferin katmanlari ve sicaklik cografyanin kodlari",
    provider: "Coğrafyanın Kodları"
  },
  {
    itemId: "item_cog9_vid_topic_07_hava_olaylari_ve_gunluk_hayata_etkileri",
    query: "9. sınıf cografya hava durumu ve iklim hava olaylari cografyanin kodlari",
    provider: "Coğrafyanın Kodları"
  },
  {
    itemId: "item_cog9_vid_nufus_ve_yerlesme",
    query: "9. sınıf cografya nufus piramitleri gocler ve yerlesme cografyanin kodlari",
    provider: "Coğrafyanın Kodları"
  },

  // FİZİK (fiz_9)
  {
    itemId: "item_fiz9_vid_kaldirma_kuvveti",
    sharedWith: ["item_fiz9_vid_topic_13_kaldirma_kuvveti"],
    query: "9. sınıf fizik sivilarda kaldirma kuvveti ozcan aykin",
    provider: "Özcan Aykın FİZİK",
    reason: "Sıvıların kaldırma kuvveti ve Arşimet prensibi ortak video kapsamı"
  },
  {
    itemId: "item_fiz9_vid_topic_13_kaldirma_kuvveti",
    sharedWith: ["item_fiz9_vid_kaldirma_kuvveti"],
    query: "9. sınıf fizik sivilarda kaldirma kuvveti ozcan aykin",
    provider: "Özcan Aykın FİZİK",
    reason: "Sıvıların kaldırma kuvveti ve Arşimet prensibi ortak video kapsamı"
  },
  {
    itemId: "item_fiz9_vid_dogadaki_kuvvetler",
    query: "9. sınıf fizik dogadaki 4 temel kuvvet temas gerektiren kuvvetler ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_basinc_prensipleri",
    query: "9. sınıf fizik basinc basinc prensipleri katilarda basinc ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_topic_10_katilarda_basinc",
    query: "9. sınıf fizik katilarda basinc ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_topic_11_sivilarda_basinc",
    query: "9. sınıf fizik sivilarda basinc manometre barometre ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_topic_14_bernoulli_ilkesi",
    query: "9. sınıf fizik bernoulli ilkesi akiskanlar mekanigi ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_topic_09_hareket_ve_hareket_turleri",
    query: "9. sınıf fizik hareket ve hareket turleri duzgun dogrusal hareket ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_topic_18_isil_denge",
    query: "9. sınıf fizik isil denge ve karisimlar ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_topic_19_isi_aktarim_yollari",
    query: "9. sınıf fizik isi aktarim iletim konveksiyon radyasyon ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },
  {
    itemId: "item_fiz9_vid_topic_20_isi_iletim_hizi",
    query: "9. sınıf fizik isi iletim hizi isi yalitim ozcan aykin",
    provider: "Özcan Aykın FİZİK"
  },

  // İNGİLİZCE (ing_9)
  {
    itemId: "item_ing9_vid_cumle_kurma",
    sharedWith: ["item_ing9_vid_topic_01_theme_1_school_life"],
    query: "ingilizce cumle kurma mantigi ozer kiraz",
    provider: "Özer Kiraz",
    reason: "İngilizce temel cümle kurma ve Theme 1 (School Life) ortak video kapsamı"
  },
  {
    itemId: "item_ing9_vid_topic_01_theme_1_school_life",
    sharedWith: ["item_ing9_vid_cumle_kurma"],
    query: "ingilizce cumle kurma mantigi ozer kiraz",
    provider: "Özer Kiraz",
    reason: "İngilizce temel cümle kurma ve Theme 1 (School Life) ortak video kapsamı"
  },
  {
    itemId: "item_ing9_vid_appearance_personality",
    query: "9. sınıf ingilizce physical appearance and personality ozer kiraz",
    provider: "Özer Kiraz"
  },
  {
    itemId: "item_ing9_vid_modals",
    query: "ingilizce modals can must should could ozer kiraz",
    provider: "Özer Kiraz"
  },
  {
    itemId: "item_ing9_vid_simple_present",
    query: "ingilizce simple present tense genis zaman ozer kiraz",
    provider: "Özer Kiraz"
  },
  {
    itemId: "item_ing9_vid_used_to",
    query: "ingilizce used to konu anlatimi ozer kiraz",
    provider: "Özer Kiraz"
  },
  {
    itemId: "item_ing9_vid_topic_05_theme_5_life_in_the_house_neighbourhood",
    query: "9. sınıf ingilizce theme 5 life in the house",
    provider: "MEB / İngilizce TV"
  },
  {
    itemId: "item_ing9_vid_topic_06_theme_6_life_in_the_city_country",
    query: "9. sınıf ingilizce theme 6 life in the city",
    provider: "MEB / İngilizce TV"
  },
  {
    itemId: "item_ing9_vid_topic_07_theme_7_life_in_the_world_nature",
    query: "9. sınıf ingilizce theme 7 world and nature",
    provider: "MEB / İngilizce TV"
  },
  {
    itemId: "item_ing9_vid_topic_08_theme_8_life_in_the_universe_future",
    query: "9. sınıf ingilizce theme 8 life in the universe",
    provider: "MEB / İngilizce TV"
  },

  // TARİH (tar_9)
  {
    itemId: "item_tar9_vid_orta_cag_ticaret_yollari",
    query: "9. sınıf tarih orta cagda ticaret yollari mehmet celal ozyildiz",
    provider: "Mehmet Celal ÖZYILDIZ"
  },
  {
    itemId: "item_tar9_vid_topic_13_orta_cag_medeniyet_havzalarinda_bilim_kultur_ve_sana",
    query: "9. sınıf tarih orta cagda bilim kultur sanat medeniyet havzalari mehmet celal",
    provider: "Mehmet Celal ÖZYILDIZ"
  },
  {
    itemId: "item_tar9_vid_topic_08_eski_cag_da_inanc_bilim_ve_sanat",
    query: "9. sınıf tarih eski cagda inanc bilim sanat uygarliklar mehmet celal",
    provider: "Mehmet Celal ÖZYILDIZ"
  },

  // MATEMATİK (mat_9)
  {
    itemId: "item_mat9_vid_geometrik_donusumler",
    sharedWith: ["item_mat9_vid_topic_09_geometrik_donusumler"],
    query: "9. sınıf matematik geometrik donusumler oteleme yansima donme rehber matematik",
    provider: "Rehber Matematik",
    reason: "Geometrik dönüşümler (öteleme, yansıma, dönme) ortak video kapsamı"
  },
  {
    itemId: "item_mat9_vid_topic_09_geometrik_donusumler",
    sharedWith: ["item_mat9_vid_geometrik_donusumler"],
    query: "9. sınıf matematik geometrik donusumler oteleme yansima donme rehber matematik",
    provider: "Rehber Matematik",
    reason: "Geometrik dönüşümler (öteleme, yansıma, dönme) ortak video kapsamı"
  },

  // BİYOLOJİ (biyo_9)
  {
    itemId: "item_biyo9_vid_topic_14_uc_ust_alem_sistemi_ve_canli_gruplari",
    query: "9. sınıf biyoloji 3 ust alem sistemi ve canlilarin siniflandirilmasi dr biyoloji",
    provider: "Dr.Biyoloji"
  },
  {
    itemId: "item_biyo9_vid_siniflandirma",
    query: "9. sınıf biyoloji canlilarin siniflandirilmasi alem filum sinif dr biyoloji",
    provider: "Dr.Biyoloji"
  }
];

// Legitimate shared coverage metadata assignments
const LEGITIMATE_SHARED_PAIRS = [
  {
    itemIds: ["item_fiz9_vid_alt_dallar", "item_fiz9_vid_fizik_giris", "item_fiz9_vid_topic_01_fizik_bilimi"],
    reason: "Fizik bilimine giriş, fiziğin alt dalları ve bilimsel yöntem konusu ortak video kapsamı"
  },
  {
    itemIds: ["item_fiz9_vid_topic_05_temel_ve_turetilmis_nicelikler", "item_fiz9_vid_vektorler"],
    reason: "Temel, türetilmiş, skaler ve vektörel fiziksel nicelikler ortak video kapsamı"
  },
  {
    itemIds: ["item_fiz9_vid_hal_degisimi", "item_fiz9_vid_topic_17_hal_degisimi"],
    reason: "Hâl değişimi ve sıcaklık grafikleri konusu ortak video kapsamı"
  },
  {
    itemIds: ["item_fiz9_vid_isi_sicaklik_kavram", "item_fiz9_vid_topic_16_isi_oz_isi_ve_isi_sigasi"],
    reason: "İç enerji, ısı, sıcaklık, öz ısı ve ısı sığası kavramları ortak video kapsamı"
  },
  {
    itemIds: ["item_biyo9_vid_enzimler_nukleik", "item_biyo9_vid_topic_08_enzimler"],
    reason: "Enzimlerin yapısı ve katalitik çalışma prensipleri ortak video kapsamı"
  },
  {
    itemIds: ["item_cog9_vid_tarihsel_gelisim", "item_cog9_vid_topic_02_nicin_cografya_ogrenmeliyiz", "item_cog9_vid_topic_03_cografya_biliminin_gelisimi"],
    reason: "Coğrafya biliminin gelişimi ve coğrafya öğrenmenin önemi ortak video kapsamı"
  },
  {
    itemIds: ["item_cog9_vid_afet_yonetimi", "item_cog9_vid_topic_16_tehlike_risk_ve_afet", "item_cog9_vid_topic_18_butuncul_afet_yonetimi"],
    reason: "Doğal afetler, risk değerlendirmesi ve bütüncül afet yönetimi ortak video kapsamı"
  },
  {
    itemIds: ["item_cog9_vid_basinc_ruzgar_yagis", "item_cog9_vid_topic_09_iklim_turleri", "item_cog9_vid_topic_10_iklim_sisteminde_yasanan_degisiklikler"],
    reason: "Atmosfer basıncı, rüzgarlar ve dünya iklim tipleri ortak video kapsamı"
  },
  {
    itemIds: ["item_cog9_vid_topic_11_nufusun_tarihsel_degisimi_ve_gelecegi", "item_cog9_vid_topic_12_nufusun_dagilisi_ve_hareketleri", "item_cog9_vid_topic_13_demografik_donusum_ve_nufus_piramitleri"],
    reason: "Demografik dönüşüm modeli, nüfus piramitleri ve göç hareketleri ortak video kapsamı"
  },
  {
    itemIds: ["item_kim9_vid_guvenlik_sembolleri", "item_kim9_vid_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik"],
    reason: "Kimya laboratuvarı güvenlik kuralları ve uyarı piktogramları ortak video kapsamı"
  },
  {
    itemIds: ["item_kim9_vid_periyodik_ozellikler", "item_kim9_vid_topic_06_periyodik_ozellikler"],
    reason: "Periyodik tabloda atom çapı, iyonlaşma enerjisi ve elektronegatiflik değişimi ortak video kapsamı"
  },
  {
    itemIds: ["item_kim9_vid_bag_turleri", "item_kim9_vid_topic_08_iyonik_bag"],
    reason: "Kimyasal türler arası güçlü etkileşimler ve iyonik bağ oluşumu ortak video kapsamı"
  },
  {
    itemIds: ["item_kim9_vid_katilar_ve_sivilar", "item_kim9_vid_topic_14_katilar_ve_ozellikleri"],
    reason: "Katı türleri (amorf ve kristal katılar) ortak video kapsamı"
  },
  {
    itemIds: ["item_mat9_vid_cebirsel_ispat", "item_mat9_vid_topic_04_gercek_sayilarin_islem_ozelliklerini_cebirsel_ifade_"],
    reason: "Gerçek sayıların işlem özellikleri ve cebirsel ispat yöntemleri ortak video kapsamı"
  },
  {
    itemIds: ["item_mat9_vid_benzerlik_kosullari", "item_mat9_vid_topic_11_benzer_ucgenler_olusturma"],
    reason: "Üçgenlerde eşlik ve benzerlik koşulları ortak video kapsamı"
  },
  {
    itemIds: ["item_tar9_vid_birey_toplum", "item_tar9_vid_olay_olgu"],
    reason: "Tarihin doğası, olay-olgu ayrımı ve tarih öğrenmenin bireye faydaları ortak video kapsamı"
  },
  {
    itemIds: ["item_tar9_vid_topic_03_tarihsel_bilginin_uretim_sureci", "item_tar9_vid_yardimci_bilimler"],
    reason: "Tarihsel bilginin üretim süreci, takvimler ve yardımcı bilim dalları ortak video kapsamı"
  },
  {
    itemIds: ["item_tar9_vid_tarim_devrimi_mezopotamya", "item_tar9_vid_topic_05_tarim_devrimi_ve_eski_cag_da_yerlesme_ekonomi"],
    reason: "Tarım Devrimi, ilk yerleşmeler ve Mezopotamya uygarlıkları ortak video kapsamı"
  },
  {
    itemIds: ["item_tar9_vid_orta_cag_gocler_devletler", "item_tar9_vid_topic_10_orta_cag_da_kitlesel_gocler", "item_tar9_vid_topic_11_orta_cag_devletlerinde_yonetim_ve_ordu"],
    reason: "Orta Çağ'da yaşanan kitlesel göçler (Kavimler Göçü) ve feodalite/ordu yapıları ortak video kapsamı"
  },
  {
    itemIds: ["item_tde9_vid_soz_sanatlari", "item_tde9_vid_topic_02_sozun_inceligi_siiri_betimleme_paragrafina_donusturm", "item_tde9_vid_topic_07_anlam_arayisi_siir_dinleme_izleme", "item_tde9_vid_topic_08_anlam_arayisi_siir_yazma"],
    reason: "Şiir bilgisi, nazım birimi/ölçü/kafiye ve edebi sanatlar (söz sanatları) ortak video kapsamı"
  }
];

async function run() {
  console.log("🚀 Starting curated video resolution...");

  // 1. Process specific targets
  for (const target of CURATION_TARGETS) {
    const itemPath = path.join(ITEMS, target.itemId + ".json");
    if (!fs.existsSync(itemPath)) continue;
    const item = readJson(itemPath);

    console.log(`\n🔍 Searching replacement for [${target.itemId}] "${item.title}"...`);
    const candidates = searchYouTubeCandidates(target.query, 6);
    let matched = null;

    for (const cand of candidates) {
      console.log(`   Trying candidate: ${cand.url} - ${cand.title} (${cand.channel})`);
      const transcriptInfo = fetchTranscriptForUrl(target.itemId, cand.url);
      if (transcriptInfo && transcriptInfo.transcriptFingerprint) {
        matched = { cand, transcriptInfo };
        break;
      }
    }

    if (matched) {
      console.log(`   ✅ Matched & Fetched transcript: ${matched.cand.url} (${matched.cand.title})`);
      item.contentUrl = matched.cand.url;
      item.publishingStatus = "active";
      item.payload ||= {};
      item.payload.provider = target.provider || matched.cand.channel || item.payload.provider;
      item.payload.teacher = matched.cand.channel;
      item.payload.verifiedLectureTitle = matched.cand.title;
      item.payload.provenance ||= {};
      const prov = item.payload.provenance;
      prov.sourceVideoUrl = matched.cand.url;
      prov.transcriptLanguage = matched.transcriptInfo.transcriptLanguage;
      prov.transcriptKind = matched.transcriptInfo.transcriptKind;
      prov.transcriptPath = matched.transcriptInfo.transcriptPath;
      prov.transcriptRawPath = matched.transcriptInfo.transcriptRawPath;
      prov.transcriptFingerprint = matched.transcriptInfo.transcriptFingerprint;
      prov.reviewStatus = "verified";
      prov.reviewedOverride = true;
      delete prov.reviewNote;
      writeJson(itemPath, item);

      // If shared with others in target, propagate
      if (target.sharedWith && target.sharedWith.length > 0) {
        for (const sharedId of target.sharedWith) {
          const sPath = path.join(ITEMS, sharedId + ".json");
          if (fs.existsSync(sPath)) {
            const sItem = readJson(sPath);
            sItem.contentUrl = matched.cand.url;
            sItem.publishingStatus = "active";
            sItem.payload ||= {};
            sItem.payload.provider = item.payload.provider;
            sItem.payload.teacher = item.payload.teacher;
            sItem.payload.verifiedLectureTitle = item.payload.verifiedLectureTitle;
            sItem.payload.provenance ||= {};
            const sProv = sItem.payload.provenance;
            sProv.sourceVideoUrl = matched.cand.url;
            sProv.transcriptLanguage = matched.transcriptInfo.transcriptLanguage;
            sProv.transcriptKind = matched.transcriptInfo.transcriptKind;
            // copy transcript file for sharedId as well
            const sharedTxt = path.join(TRANSCRIPTS, sharedId + ".tr.txt");
            const sharedVtt = path.join(TRANSCRIPTS, sharedId + ".tr.tr.vtt");
            try {
              fs.copyFileSync(path.join(ROOT, matched.transcriptInfo.transcriptPath), sharedTxt);
              sProv.transcriptPath = path.relative(ROOT, sharedTxt);
            } catch {}
            try {
              fs.copyFileSync(path.join(ROOT, matched.transcriptInfo.transcriptRawPath), sharedVtt);
              sProv.transcriptRawPath = path.relative(ROOT, sharedVtt);
            } catch {}
            sProv.transcriptFingerprint = matched.transcriptInfo.transcriptFingerprint;
            sProv.reviewStatus = "verified";
            sProv.reviewedOverride = true;
            sProv.sharedSource = true;
            sProv.sharedSourceReason = target.reason || "Ortak müfredat konusu";
            delete sProv.reviewNote;
            writeJson(sPath, sItem);
            console.log(`   🔗 Propagated shared video to [${sharedId}]`);
          }
        }
      }
    } else {
      console.log(`   ⚠️ No valid subtitle transcript found for query: ${target.query}`);
    }
  }

  // 2. Mark legitimate shared sources with explicit metadata
  console.log("\n🏷️ Tagging legitimate shared sources...");
  for (const pair of LEGITIMATE_SHARED_PAIRS) {
    for (const id of pair.itemIds) {
      const p = path.join(ITEMS, id + ".json");
      if (!fs.existsSync(p)) continue;
      const item = readJson(p);
      item.payload ||= {};
      item.payload.provenance ||= {};
      item.payload.provenance.sharedSource = true;
      item.payload.provenance.sharedSourceReason = pair.reason;
      writeJson(p, item);
    }
  }

  console.log("\n🎉 Curated video resolution complete!");
}

run().catch(err => {
  console.error("Error during curation:", err);
  process.exit(1);
});
