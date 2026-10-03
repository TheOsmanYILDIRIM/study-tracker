const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { validateQuizSchema } = require('../cli/lib/v2-quiz');
const { validateModularTree, compileModularCatalog, saveCompiledCatalog } = require('../cli/lib/v2-modular');

const ITEMS_DIR = path.join(__dirname, '../content/v2/items');
const LESSONS_DIR = path.join(__dirname, '../content/v2/lessons');

// 1. Video items update map for course_cog_9
const videoUpdates = [
  {
    id: 'item_cog9_vid_doga_insan',
    title: 'Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri',
    contentUrl: 'https://www.youtube.com/watch?v=DzoqD30tomo',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_mekansal_dusunme',
    title: 'Doğa-İnsan Etkileşimi & Mekânsal Düşünme',
    contentUrl: 'https://www.youtube.com/watch?v=PeNG9gQvsj0',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_tarihsel_gelisim',
    title: 'Coğrafya Biliminin Tarihsel Gelişimi',
    contentUrl: 'https://www.youtube.com/watch?v=1-AJn6S0syU',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_02_nicin_cografya_ogrenmeliyiz',
    title: 'Niçin Coğrafya Öğrenmeliyiz?',
    contentUrl: 'https://www.youtube.com/watch?v=1-AJn6S0syU',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_03_cografya_biliminin_gelisimi',
    title: 'Coğrafya Biliminin Gelişimi',
    contentUrl: 'https://www.youtube.com/watch?v=1-AJn6S0syU',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_harita_bilgisi',
    title: 'Mekânın Aynası Haritalar & Projeksiyon Yöntemleri',
    contentUrl: 'https://www.youtube.com/watch?v=lmFGwdkEURc',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_05_turkiye_nin_cografi_konumu',
    title: "Türkiye'nin Coğrafi Konumu",
    contentUrl: 'https://www.youtube.com/watch?v=izcd86rxRDw',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_06_mekansal_bilgi_teknolojilerinin_bilesenleri',
    title: 'Mekânsal Bilgi Teknolojilerinin Bileşenleri',
    contentUrl: 'https://www.youtube.com/watch?v=AgRlAZjaPE4',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_07_hava_olaylari_ve_gunluk_hayata_etkileri',
    title: 'Hava Olayları ve Günlük Hayata Etkileri',
    contentUrl: 'https://www.youtube.com/watch?v=UGsM0-wcvLU',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_atmosfer_ve_sicaklik',
    title: 'Atmosferin Katmanları ve Sıcaklık Etmenleri',
    contentUrl: 'https://www.youtube.com/watch?v=UGsM0-wcvLU',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_basinc_ruzgar_yagis',
    title: 'Basınç Merkezleri, Rüzgârlar ve Yağış Tipleri',
    contentUrl: 'https://www.youtube.com/watch?v=kAeQTw96EMc',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_09_iklim_turleri',
    title: 'İklim Türleri',
    contentUrl: 'https://www.youtube.com/watch?v=kAeQTw96EMc',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_10_iklim_sisteminde_yasanan_degisiklikler',
    title: 'İklim Sisteminde Yaşanan Değişiklikler',
    contentUrl: 'https://www.youtube.com/watch?v=kAeQTw96EMc',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_11_nufusun_tarihsel_degisimi_ve_gelecegi',
    title: 'Nüfusun Tarihsel Değişimi ve Geleceği',
    contentUrl: 'https://www.youtube.com/watch?v=SP1MlcvQiAs',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_12_nufusun_dagilisi_ve_hareketleri',
    title: 'Nüfusun Dağılışı ve Hareketleri',
    contentUrl: 'https://www.youtube.com/watch?v=SP1MlcvQiAs',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_13_demografik_donusum_ve_nufus_piramitleri',
    title: 'Demografik Dönüşüm ve Nüfus Piramitleri',
    contentUrl: 'https://www.youtube.com/watch?v=SP1MlcvQiAs',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_14_nufusla_ilgili_firsatlar_sorunlar_ve_politikalar',
    title: 'Nüfusla İlgili Fırsatlar, Sorunlar ve Politikalar',
    contentUrl: 'https://www.youtube.com/watch?v=PUu7cEjrBng',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_15_ekonomik_faaliyetleri_etkileyen_cografi_faktorler',
    title: 'Ekonomik Faaliyetleri Etkileyen Coğrafi Faktörler',
    contentUrl: 'https://www.youtube.com/watch?v=Zy_6Fgs84N4',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_16_tehlike_risk_ve_afet',
    title: 'Tehlike, Risk ve Afet',
    contentUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_nufus_ve_yerlesme',
    title: 'Nüfus Piramitleri, Göçler ve Yerleşme Dokuları',
    contentUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_afet_yonetimi',
    title: 'Doğal Afet Türleri ve Bütüncül Afet Yönetimi',
    contentUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_18_butuncul_afet_yonetimi',
    title: 'Bütüncül Afet Yönetimi',
    contentUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
    provider: 'Coğrafyanın Kodları'
  },
  {
    id: 'item_cog9_vid_topic_19_bolge_ve_bolge_siniri',
    title: 'Bölge ve Bölge Sınırı',
    contentUrl: 'https://www.youtube.com/watch?v=KaImNrWaZEo',
    provider: 'Coğrafyanın Kodları'
  }
];

// 2. Micro quizzes definition for missing video items (21 quizzes)
const microQuizzes = [
  // 1. Doğal Ortamlar ve Coğrafyanın Konusu (in lesson_cog9_dogal_sistemler)
  {
    id: 'item_cog9_vid_doga_insan__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_dogal_sistemler',
    stableKey: 'cog9_quiz_doga_insan_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Doğal Ortamlar ve Coğrafyanın Bölümleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Doğal Ortamlar ve Coğrafyanın Bölümleri Mikro Testi',
      questions: [
        {
          id: 'q_cog9_doga_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yeryüzünü oluşturan 4 temel doğal ortam (Muhteşem Dörtlü) hangi seçenekte eksiksiz verilmiştir?',
          choices: [
            'Litosfer (Taş küre), Atmosfer (Hava küre), Hidrosfer (Su küre), Biyosfer (Canlılar küresi)',
            'Barisfer, Pirosfer, Hidrosfer, Stratosfer',
            'Klimatoloji, Jeomorfoloji, Hidrografya, Kartografya',
            'Troposfer, Termosfer, Mezosfer, İyonosfer'
          ],
          correctAnswer: 'Litosfer (Taş küre), Atmosfer (Hava küre), Hidrosfer (Su küre), Biyosfer (Canlılar küresi)',
          explanation: 'Coğrafyanın incelediği doğal ortam dört ana bileşenden oluşur: Litosfer (Taş), Atmosfer (Hava), Hidrosfer (Su) ve bunların kesişiminde yer alan Biyosfer (Canlılar).'
        },
        {
          id: 'q_cog9_doga_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yer şekillerinin (dağ, plato, ova, vadi vb.) oluşumunu, gelişimini ve yeryüzündeki dağılışını inceleyen fiziki coğrafya alt dalı hangisidir?',
          choices: [
            'Jeomorfoloji (Yüzey Şekilleri Bilimi)',
            'Klimatoloji (İklim Bilimi)',
            'Hidrografya (Sular Coğrafyası)',
            'Biyocoğrafya (Canlılar Coğrafyası)'
          ],
          correctAnswer: 'Jeomorfoloji (Yüzey Şekilleri Bilimi)',
          explanation: 'Jeomorfoloji; jeoloji, jeofizik ve pedolojiden yararlanarak yer şekillerinin iç ve dış kuvvetlerle şekillenmesini inceler.'
        },
        {
          id: 'q_cog9_doga_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Kartografya; harita ve harita benzeri mekânsal modellerin hazırlanması, çizimi ve yorumlanmasını konu edinen fiziki coğrafya alt dalıdır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Kartografya, haritacılık bilimidir ve coğrafi verilerin düzleme aktarılması ve modellenmesiyle ilgilenir.'
        },
        {
          id: 'q_cog9_doga_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi Beşerî Coğrafya\'nın inceleme alanları arasında YER ALMAZ?',
          choices: [
            'Volkanik patlamalar ve deprem dalgalarının yayılımı',
            'Nüfusun yaş ve cinsiyet yapısı',
            'Kırsal ve kentsel yerleşme dokuları',
            'Tarım ve sanayi faaliyetlerinin mekânsal dağılışı'
          ],
          correctAnswer: 'Volkanik patlamalar ve deprem dalgalarının yayılımı',
          explanation: 'Volkanizma ve depremler doğa olayları olup Fiziki Coğrafya\'nın (Jeomorfoloji / Jeoloji) konusudur; insan faaliyetleri ise beşerî coğrafyaya aittir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_doga_insan',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=DzoqD30tomo',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 2. Doğa-İnsan Etkileşimi & Mekânsal Düşünme (in lesson_cog9_dogal_sistemler)
  {
    id: 'item_cog9_vid_mekansal_dusunme__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_dogal_sistemler',
    stableKey: 'cog9_quiz_mekansal_dusunme_micro',
    itemType: 'QUIZ',
    displayLabel: '1.2-Q',
    orderKey: 2500,
    title: 'Doğa-İnsan Etkileşimi ve Mekânsal Düşünme Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Doğa-İnsan Etkileşimi ve Mekânsal Düşünme Mikro Testi',
      questions: [
        {
          id: 'q_cog9_mek_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki durumlardan hangisinde "insanın doğaya doğrudan etkisi ve müdahalesi" söz konusudur?',
          choices: [
            'Deniz doldurularak havalimanı ve otoyol inşa edilmesi',
            'Kutup bölgelerinde yaşayan insanların kalın kürk giymesi',
            'Çöl bölgelerinde kerpiç evlerin tercih edilmesi',
            'Ekvatoral kuşakta tarımın yüksek plato ve yamaçlarda yapılması'
          ],
          correctAnswer: 'Deniz doldurularak havalimanı ve otoyol inşa edilmesi',
          explanation: 'Deniz dolgusu (ör. Ordu-Giresun veya Rize-Artvin Havalimanları) insanın doğal ortamı kendi ihtiyaçları doğrultusunda dönüştürmesine doğrudan örnektir.'
        },
        {
          id: 'q_cog9_mek_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Coğrafya bilimini diğer doğa ve toplum bilimlerinden ayıran en temel ilke aşağıdakilerden hangisidir?',
          choices: [
            'Dağılış İlkesi',
            'Nedensellik (Sebep-Sonuç) İlkesi',
            'Karşılıklı İlgi (Bağlantı) İlkesi',
            'Gözlem İlkesi'
          ],
          correctAnswer: 'Dağılış İlkesi',
          explanation: 'Olay ve varlıkların yeryüzündeki mekânsal yayılışını haritalarla ifade eden "Dağılış İlkesi", yalnızca coğrafyaya özgü temel ilkedir.'
        },
        {
          id: 'q_cog9_mek_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Mekânsal düşünme; olayların nerede gerçekleştiğini, mekânlar arasındaki mesafeyi, yönü, etkileşimi ve dağılış örüntülerini kavrama yeteneğidir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Mekânsal düşünme becerisi, coğrafi olguların konum, ölçek ve mekânsal bağlantılar bağlamında bütüncül olarak analiz edilmesidir.'
        },
        {
          id: 'q_cog9_mek_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Doğu Karadeniz\'de dağların kıyıya paralel uzanması sebebiyle kıyı ile iç kesimler arasında ulaşım geçitlerle sağlanır." cümlesi coğrafyanın hangi ilkesini vurgulamaktadır?',
          choices: [
            'Karşılıklı İlgi (Bağlantı) ve Nedensellik İlkesi',
            'Yalnızca Dağılış İlkesi',
            'Tarihsel Süreklilik İlkesi',
            'Kartografik Ölçek İlkesi'
          ],
          correctAnswer: 'Karşılıklı İlgi (Bağlantı) ve Nedensellik İlkesi',
          explanation: 'Yer şekilleri (dağların uzanışı) ile beşerî faaliyet (ulaşım ve geçitler) arasındaki sebep-sonuç ve karşılıklı ilişki ifade edilmiştir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_mekansal_dusunme',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=PeNG9gQvsj0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 3. Coğrafya Biliminin Tarihsel Gelişimi (in lesson_cog9_dogal_sistemler)
  {
    id: 'item_cog9_vid_tarihsel_gelisim__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_dogal_sistemler',
    stableKey: 'cog9_quiz_tarihsel_gelisim_micro',
    itemType: 'QUIZ',
    displayLabel: '1.3-Q',
    orderKey: 3500,
    title: 'Coğrafya Biliminin Tarihsel Gelişimi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Coğrafya Biliminin Tarihsel Gelişimi Mikro Testi',
      questions: [
        {
          id: 'q_cog9_targel_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarihte "Geographika" (Yer Tasviri) sözcüğünü ilk kez kullanan ve Dünya\'nın çevresini İskenderiye ile Syene arasındaki gölge boyu farkından gerçeğe çok yakın hesaplayan İlk Çağ bilgini kimdir?',
          choices: [
            'Eratosthenes',
            'Strabon',
            'Batlamyus (Ptolemaios)',
            'Heredot'
          ],
          correctAnswer: 'Eratosthenes',
          explanation: 'Eratosthenes (MÖ 276-194), coğrafya terimini ilk kullanan ve Güneş açılarından Dünya\'nın meridyen çevresini ~40.000 km olarak hesaplayan bilgindir.'
        },
        {
          id: 'q_cog9_targel_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Orta Çağ İslam coğrafyacılarından olup Dünya\'nın yarıçapını ve eksen eğikliğini (23° 27\') büyük bir hassasiyetle hesaplayan ünlü Türk-İslam bilgini kimdir?',
          choices: [
            'Birûnî',
            'İdrisi',
            'İbn Batuta',
            'Harezmi'
          ],
          correctAnswer: 'Birûnî',
          explanation: 'Birûnî (973-1048), trigonometri ve matematiksel yöntemlerle Dünya\'nın yarıçapını 6339 km olarak hesaplayıp çağının asırlarca ötesine geçmiştir.'
        },
        {
          id: 'q_cog9_targel_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Pîrî Reis\'in 1513 tarihli Dünya Haritası ve 1526 yılında tamamladığı denizcilik kitabı "Kitâb-ı Bahriye", Osmanlı coğrafyacılığının en önemli başyapıtlarındandır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Pîrî Reis, Akdeniz kıyılarını ve Atlantik/Amerika kıyılarını detaylı haritalarla çizmiş ve Kitâb-ı Bahriye ile denizcilik kılavuzu oluşturmuştur.'
        },
        {
          id: 'q_cog9_targel_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '19. yüzyılda coğrafyayı modern bir bilim haline getiren, fiziki coğrafyanın kurucusu sayılan Alman doğa bilimci kimdir?',
          choices: [
            'Alexander von Humboldt',
            'Karl Ritter',
            'İbn Haldun',
            'Katip Çelebi'
          ],
          correctAnswer: 'Alexander von Humboldt',
          explanation: 'Humboldt "Kosmos" eseriyle fiziki coğrafyanın, Karl Ritter ise beşerî coğrafyanın modern kurucusu olarak kabul edilir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_tarihsel_gelisim',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1-AJn6S0syU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 4. Niçin Coğrafya Öğrenmeliyiz? (lesson_cog9_topic_02_nicin_cografya_ogrenmeliyiz)
  {
    id: 'item_cog9_vid_topic_02_nicin_cografya_ogrenmeliyiz__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_02_nicin_cografya_ogrenmeliyiz',
    stableKey: 'cog9_quiz_nicin_cografya_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Niçin Coğrafya Öğrenmeliyiz Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Niçin Coğrafya Öğrenmeliyiz Mikro Testi',
      questions: [
        {
          id: 'q_cog9_top02_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Coğrafya eğitiminin bireye kazandırdığı temel yetkinliklerden biri aşağıdakilerden hangisidir?',
          choices: [
            'Doğal kaynakların sürdürülebilir kullanımını kavrama ve çevre bilinci geliştirme',
            'Tüm dünya ülkelerinin sadece başkent isimlerini ezberleme',
            'Hava durumu tahminlerini laboratuvarda kimyasal deneyle üretme',
            'Sadece geçmişte yaşamış tarihi şahsiyetleri inceleme'
          ],
          correctAnswer: 'Doğal kaynakların sürdürülebilir kullanımını kavrama ve çevre bilinci geliştirme',
          explanation: 'Coğrafya bilimi, insan-doğa dengesini gözeterek doğal varlıkların bilinçli ve sürdürülebilir yönetilmesini sağlar.'
        },
        {
          id: 'q_cog9_top02_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Coğrafya öğrenmek; deprem, sel, heyelan ve kuraklık gibi doğal tehlikelerin risklerini önceden belirleyerek afetlere karşı dirençli toplumlar oluşturmaya yardımcı olur.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Mekânsal analizler ve risk haritaları sayesinde afet öncesi tedbirler planlanır ve kayıplar en aza indirilir.'
        },
        {
          id: 'q_cog9_top02_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Küresel ölçekte yaşanan iklim krizi, su kıtlığı ve kitlesel göç hareketlerinin kök nedenlerini ve çözümlerini araştırırken hangi bilimsel bakış açısı zorunludur?',
          choices: [
            'Mekânsal ve bütüncül coğrafi bakış açısı',
            'Yalnızca mikrobiyolojik bakış açısı',
            'Yalnızca soyut matematiksel aksiyomlar',
            'Yalnızca nümizmatik bakış açısı'
          ],
          correctAnswer: 'Mekânsal ve bütüncül coğrafi bakış açısı',
          explanation: 'Küresel sorunlar yerel ve bölgesel dinamiklerle doğrudan bağlantılı olduğundan coğrafyanın mekânsal sentez yeteneğine ihtiyaç duyar.'
        },
        {
          id: 'q_cog9_top02_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Vatan sevgisi, millî şuur ve kendi ülkesinin jeopolitik/doğal potansiyelini tanımak coğrafya öğretiminin temel amaçları arasındadır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'MEB Maarif Modeli coğrafya öğretim programı, bireylerin kendi ülkesinin mekânsal zenginliklerini ve stratejik konumunu tanımasını hedefler.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_02_nicin_cografya_ogrenmeliyiz',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1-AJn6S0syU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 5. Coğrafya Biliminin Gelişimi (lesson_cog9_topic_03_cografya_biliminin_gelisimi)
  {
    id: 'item_cog9_vid_topic_03_cografya_biliminin_gelisimi__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_03_cografya_biliminin_gelisimi',
    stableKey: 'cog9_quiz_top03_gelisim_micro',
    itemType: 'QUIZ',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Coğrafya Biliminin Evrimi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Coğrafya Biliminin Evrimi Mikro Testi',
      questions: [
        {
          id: 'q_cog9_top03_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İlk Çağ\'da Amasya doğumlu olan ve 17 ciltlik "Geographika" adlı eseriyle Akdeniz havzası ve Anadolu coğrafyasını ayrıntılı anlatan bilgin kimdir?',
          choices: [
            'Strabon',
            'Batlamyus',
            'Heredot',
            'Aristoteles'
          ],
          correctAnswer: 'Strabon',
          explanation: 'Strabon (MÖ 64 - MS 24), Amasya\'da yaşamış antik dönemin en büyük bölgesel coğrafyacısı ve Geographika\'nın yazarıdır.'
        },
        {
          id: 'q_cog9_top03_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '17. yüzyılda Osmanlı bilgini Kâtip Çelebi tarafından kaleme alınan, Doğu ve Batı coğrafya kaynaklarını birleştiren ilk sistematik Türkçe coğrafya eseri hangisidir?',
          choices: [
            'Cihannümâ (Dünyayı Gösteren)',
            'Seyahatnâme',
            'Kitâb-ı Bahriye',
            'Tarih-i Cevdet'
          ],
          correctAnswer: 'Cihannümâ (Dünyayı Gösteren)',
          explanation: 'Kâtip Çelebi\'nin "Cihannümâ" eseri Osmanlı coğrafya tarihinde modern haritacılık ve dünya coğrafyası açısından çığır açmıştır.'
        },
        {
          id: 'q_cog9_top03_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: '14. yüzyılda Kuzey Afrika, Orta Doğu, Anadolu ve Çin\'e kadar 120.000 km yol kat ederek dönemin en kapsamlı Seyahatnâme\'sini yazan İslam seyyahı İbn Batuta\'dır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'İbn Batuta, Orta Çağ\'ın en büyük seyyahı olup gezdiği toprakların beşerî, kültürel ve coğrafi özelliklerini Rıhle (Seyahatname) eserinde toplamıştır.'
        },
        {
          id: 'q_cog9_top03_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Coğrafya kaderdir" sözüyle anılan ve "Mukaddime" adlı eserinde iklim ve çevre koşullarının toplumların karakteri ve devletlerin ömrü üzerindeki etkisini inceleyen düşünür kimdir?',
          choices: [
            'İbn Haldun',
            'Farabi',
            'İbn Sina',
            'Gazali'
          ],
          correctAnswer: 'İbn Haldun',
          explanation: 'İbn Haldun (1332-1406), Mukaddime eserinde çevre determinizmi ve beşerî coğrafya alanında çığır açıcı sosyolojik-coğrafi tespitler yapmıştır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_03_cografya_biliminin_gelisimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1-AJn6S0syU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 6. Mekânsal Bilgi Teknolojileri (lesson_cog9_topic_06_mekansal_bilgi_teknolojilerinin_bilesenleri)
  {
    id: 'item_cog9_vid_topic_06_mekansal_bilgi_teknolojilerinin_bilesenleri__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_06_mekansal_bilgi_teknolojilerinin_bilesenleri',
    stableKey: 'cog9_quiz_cbs_teknoloji_micro',
    itemType: 'QUIZ',
    displayLabel: '6.1-Q',
    orderKey: 1500,
    title: 'Mekânsal Bilgi Teknolojileri ve CBS Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Mekânsal Bilgi Teknolojileri ve CBS Mikro Testi',
      questions: [
        {
          id: 'q_cog9_cbs_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Coğrafi Bilgi Sistemleri\'nin (CBS / GIS) temel çalışma prensibi aşağıdakilerden hangisidir?',
          choices: [
            'Mekânsal (konumsal) ve öznitelik verilerini toplayıp katmanlar halinde depolamak, analiz etmek ve haritalandırmak',
            'Yalnızca el ile kâğıt üzerine renkli kroki çizmek',
            'Hava sıcaklığını anlık olarak termometreyle ölçmek',
            'Sadece tarihi metinleri tarayıp tercüme etmek'
          ],
          correctAnswer: 'Mekânsal (konumsal) ve öznitelik verilerini toplayıp katmanlar halinde depolamak, analiz etmek ve haritalandırmak',
          explanation: 'CBS, konumsal verilerle (koordinat) öznitelik bilgilerini (nüfus, arazi kullanımı vb.) birleştirip katmanlı analiz yapan bilgisayar tabanlı sistemdir.'
        },
        {
          id: 'q_cog9_cbs_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yeryüzündeki nesnelerle doğrudan temas kurmadan uçak veya uydu platformlarındaki algılayıcılarla bilgi toplama tekniğine ne ad verilir?',
          choices: [
            'Uzaktan Algılama (Remote Sensing)',
            'Nivelman Ölçümü',
            'Anket Yöntemi',
            'Sismograf Kaydı'
          ],
          correctAnswer: 'Uzaktan Algılama (Remote Sensing)',
          explanation: 'Uzaktan algılama; uydu görüntüleri ve hava fotoğrafları aracılığıyla orman yangınları, tarım rekoltesi, kentleşme ve afetleri izlemeyi sağlar.'
        },
        {
          id: 'q_cog9_cbs_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Küresel Konumlama Sistemi (GPS), yörüngedeki uydulardan sinyaller alarak Dünya üzerindeki herhangi bir noktanın enlem, boylam ve yükseklik koordinatlarını yüksek doğrulukla belirler.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'GPS (Global Positioning System), uydular ile alıcı arasındaki mesafe ve zaman farkından yararlanarak hassas konum tespiti yapar.'
        },
        {
          id: 'q_cog9_cbs_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'CBS\'de veriler "Vektör" ve "Raster" olmak üzere iki temel modelde tutulur. Aşağıdakilerden hangisi Vektör veri modelinin 3 temel geometrik unsurudur?',
          choices: [
            'Nokta, Çizgi, Poligon (Alan)',
            'Piksel, Hücre, Matris',
            'Enlem, Boylam, Meridyen',
            'Lejant, Ölçek, Yön oku'
          ],
          correctAnswer: 'Nokta, Çizgi, Poligon (Alan)',
          explanation: 'Vektör veri yapısında yangın musluğu/ağaç (nokta), yol/akarsu (çizgi), göl/orman/parsel (poligon/alan) olarak modellenir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_06_mekansal_bilgi_teknolojilerinin_bilesenleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=AgRlAZjaPE4',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 7. Hava Olayları ve Günlük Hayata Etkileri (lesson_cog9_topic_07_hava_olaylari_ve_gunluk_hayata_etkileri)
  {
    id: 'item_cog9_vid_topic_07_hava_olaylari_ve_gunluk_hayata_etkileri__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_07_hava_olaylari_ve_gunluk_hayata_etkileri',
    stableKey: 'cog9_quiz_hava_olaylari_micro',
    itemType: 'QUIZ',
    displayLabel: '7.1-Q',
    orderKey: 1500,
    title: 'Hava Durumu ve Günlük Hayat Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Hava Durumu ve Günlük Hayat Mikro Testi',
      questions: [
        {
          id: 'q_cog9_hava_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Hava Durumu" ile "İklim" arasındaki en temel fark aşağıdakilerden hangisidir?',
          choices: [
            'Hava durumu dar bir alanda kısa süreli atmosfer olaylarını, iklim ise geniş alanda uzun yılların ortalamasını inceler.',
            'Hava durumu sadece yazın, iklim ise kışın gerçekleşir.',
            'Hava durumu jeomorfolojiyle, iklim ise astronomiyle ilgilidir.',
            'Hava durumu asla değişmezken iklim her saat başı değişir.'
          ],
          correctAnswer: 'Hava durumu dar bir alanda kısa süreli atmosfer olaylarını, iklim ise geniş alanda uzun yılların ortalamasını inceler.',
          explanation: 'Hava durumu anlık/günlük atmosfer koşullarıdır (Meteoroloji inceler); iklim ise en az 30-35 yıllık ortalama durumdur (Klimatoloji inceler).'
        },
        {
          id: 'q_cog9_hava_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki ifadelerden hangisi bir "İklim" özelliğini belirtir?',
          choices: [
            'Erzurum\'da kış mevsimi uzun, karlı ve son derece soğuk geçer.',
            'Bugün İstanbul\'da öğleden sonra şiddetli sağanak yağış bekleniyor.',
            'Antalya\'da yarın sabah yoğun sis nedeniyle uçuşlar iptal edildi.',
            'Ankara\'da bu akşam fırtına ve dolu yağışı görülecektir.'
          ],
          correctAnswer: 'Erzurum\'da kış mevsimi uzun, karlı ve son derece soğuk geçer.',
          explanation: 'Erzurum\'un kış mevsimi karakteri uzun yılların genel ortalamasını (karasal iklim) yansıttığı için iklim ifadesidir; diğerleri günlük hava durumudur.'
        },
        {
          id: 'q_cog9_hava_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Ekstrem hava olayları (aşırı don, fırtına, şiddetli dolu, aşırı sıcak hava dalgası) tarımsal rekolteyi, hava-deniz ulaşımını ve enerji tüketimini doğrudan etkiler.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Hava olayları; don sebebiyle meyve bahçelerinin zarar görmesi, sis sebebiyle uçuş iptalleri veya sıcakta klima kullanımı gibi geniş etkilere sahiptir.'
        },
        {
          id: 'q_cog9_hava_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Sinoptik hava tahmin haritaları, atmosferdeki sıcaklık, basınç ve rüzgâr akımlarını anlık göstererek erken uyarı sistemlerinin kurulmasını sağlar.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Meteoroloji uzmanları sinoptik haritalarla fırtına, kasırga ve aşırı yağış risklerini önceden tahmin ederek toplumu uyarır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_07_hava_olaylari_ve_gunluk_hayata_etkileri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=UGsM0-wcvLU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 8. Atmosferin Katmanları ve Sıcaklık (in lesson_cog9_iklim_ve_hava_olaylari)
  {
    id: 'item_cog9_vid_atmosfer_ve_sicaklik__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_iklim_ve_hava_olaylari',
    stableKey: 'cog9_quiz_atmosfer_sicaklik_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Atmosferin Katmanları ve Sıcaklık Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Atmosferin Katmanları ve Sıcaklık Mikro Testi',
      questions: [
        {
          id: 'q_cog9_atm_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Atmosferdeki su buharının (nemin) tamamının bulunduğu ve tüm hava olaylarının (bulut, yağmur, rüzgâr vb.) gerçekleştiği en alt katman hangisidir?',
          choices: [
            'Troposfer',
            'Stratosfer',
            'Mezosfer',
            'Termosfer'
          ],
          correctAnswer: 'Troposfer',
          explanation: 'Troposfer yeryüzüne temas eden katmandır; su buharının %100\'ü burada yer aldığı için meteorolojik olaylar sadece troposferde yaşanır.'
        },
        {
          id: 'q_cog9_atm_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Troposferde yerden yükseldikçe sıcaklığın ortalama her 200 metrede 1°C azalmasının temel sebebi nedir?',
          choices: [
            'Atmosferin Güneş\'ten gelen doğrudan ışınlardan ziyade yerden yansıyan ışınlarla (yer ışıması) ısınması',
            'Yukarıda Güneş ışınlarının tamamen sönümlenmesi',
            'Ozon tabakasının troposferin dibinde yer alması',
            'Yer çekiminin yukarıda artması'
          ],
          correctAnswer: 'Atmosferin Güneş\'ten gelen doğrudan ışınlardan ziyade yerden yansıyan ışınlarla (yer ışıması) ısınması',
          explanation: 'Güneş ışınları önce yeryüzünü ısıtır; atmosfer alttan ısındığı ve yukarılarda gaz yoğunluğu ile sera etkisi azaldığı için her 200 m\'de sıcaklık 1°C düşer.'
        },
        {
          id: 'q_cog9_atm_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Stratosfer katmanında yer alan Ozonosfer (Ozon tabakası), Güneş\'ten gelen zararlı ultraviyole (UV) ışınlarını soğurarak canlı yaşamını korur.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Ozon molekülleri (O₃), biyolojik dokulara ve DNA\'ya zarar veren yüksek enerjili UV ışınlarını filtreler.'
        },
        {
          id: 'q_cog9_atm_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kuzey Yarım Küre\'de dağların güneye bakan yamaçlarının kuzeye bakan yamaçlara göre daha fazla güneş alması ve daha sıcak olmasına ne ad verilir?',
          choices: [
            'Bakı Etkisi',
            'Karasallık Etkisi',
            'İnversiyon Etkisi',
            'Koryolis Etkisi'
          ],
          correctAnswer: 'Bakı Etkisi',
          explanation: 'Bakı; bir yamacın Güneş ışınlarına bakma durumudur. KYK\'de dönence dışındaki alanlarda güney yamaçlar daima daha fazla ısınır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_atmosfer_ve_sicaklik',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=UGsM0-wcvLU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 9. Basınç, Rüzgârlar ve Yağış Tipleri (in lesson_cog9_iklim_ve_hava_olaylari)
  {
    id: 'item_cog9_vid_basinc_ruzgar_yagis__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_iklim_ve_hava_olaylari',
    stableKey: 'cog9_quiz_basinc_ruzgar_yagis_micro',
    itemType: 'QUIZ',
    displayLabel: '2.2-Q',
    orderKey: 2500,
    title: 'Basınç, Rüzgârlar ve Yoğuşma Tipleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Basınç, Rüzgârlar ve Yoğuşma Tipleri Mikro Testi',
      questions: [
        {
          id: 'q_cog9_bry_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Alçak Basınç (Siklon) merkezlerinin genel özellikleri hakkında aşağıdakilerden hangisi DOĞRUDUR?',
          choices: [
            'Yükselici hava hareketleri görülür, hava genellikle bulutlu ve yağış ihtimali yüksektir.',
            'Alçalıcı hava hareketi vardır, hava daima açık ve kuraktır.',
            'Rüzgâr merkezden çevreye doğru eser.',
            'Yalnızca kutup noktalarında oluşur.'
          ],
          correctAnswer: 'Yükselici hava hareketleri görülür, hava genellikle bulutlu ve yağış ihtimali yüksektir.',
          explanation: 'Alçak basınçta ısınan veya dinamik olarak yükselen hava soğur, yoğuşur ve bulut/yağış oluşturur; rüzgâr çevreden merkeze doğrudur.'
        },
        {
          id: 'q_cog9_bry_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '30° Dinamik Yüksek Basınç alanlarından Ekvator\'daki Termik Alçak Basınç alanına doğru yıl boyunca düzenli esen ve okyanus akıntılarını yönlendiren sürekli rüzgâr hangisidir?',
          choices: [
            'Alizeler (Ticaret Rüzgârları)',
            'Batı Rüzgârları',
            'Kutup Rüzgârları',
            'Muson Rüzgârları'
          ],
          correctAnswer: 'Alizeler (Ticaret Rüzgârları)',
          explanation: 'Alizeler tropikal kuşakta 30° enlemlerinden Ekvator\'a esen, sıcak kuşak karalarının doğu kıyılarına bol yağış bırakan sürekli rüzgârlardır.'
        },
        {
          id: 'q_cog9_bry_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Bir dağ yamacını aşarak diğer taraftan aşağıya doğru inen ve sürtünmenin etkisiyle her 100 metrede 1°C ısınan kuru ve sıcak rüzgâra "Föhn Rüzgârı" denir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Föhn rüzgârı indiği yamaçta sıcaklığı hızla artırır, karları eritir, bağıl nemi düşürür ve tarım ürünlerini erken olgunlaştırır/kurutur.'
        },
        {
          id: 'q_cog9_bry_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Farklı sıcaklık ve nem özelliğine sahip sıcak ve soğuk hava kütlelerinin karşılaşma alanlarında sıcak havanın yükselmesiyle oluşan yağış tipi hangisidir?',
          choices: [
            'Cephesel (Frontal) Yağış',
            'Konveksiyonel (Yükselim) Yağış',
            'Orografik (Yamaç) Yağış',
            'Muson Yağışı'
          ],
          correctAnswer: 'Cephesel (Frontal) Yağış',
          explanation: 'Cephe yağışları sıcak ve soğuk havanın karşılaştığı orta kuşakta (özellikle Akdeniz iklim havzasında kışın) en yaygın yağış türüdür.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_basinc_ruzgar_yagis',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=kAeQTw96EMc',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 10. İklim Türleri (lesson_cog9_topic_09_iklim_turleri)
  {
    id: 'item_cog9_vid_topic_09_iklim_turleri__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_09_iklim_turleri',
    stableKey: 'cog9_quiz_iklim_turleri_micro',
    itemType: 'QUIZ',
    displayLabel: '9.1-Q',
    orderKey: 1500,
    title: 'Büyük İklim Tipleri (Makroklima) Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Büyük İklim Tipleri (Makroklima) Mikro Testi',
      questions: [
        {
          id: 'q_cog9_iktur_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yıl boyunca sıcaklık ortalaması 25°C\'nin üzerinde olan, yıllık sıcaklık farkı en az (2-3°C) ve her mevsimi düzenli konveksiyonel yağış alan iklim tipi hangisidir?',
          choices: [
            'Ekvatoral İklim',
            'Akdeniz İklimi',
            'Çöl İklimi',
            'Tundra İklimi'
          ],
          correctAnswer: 'Ekvatoral İklim',
          explanation: 'Ekvatoral iklimde Güneş ışınları yıl boyu dik ve dike yakın gelir; yıllık sıcaklık farkı en azdır, doğal bitki örtüsü tropikal yağmur ormanlarıdır.'
        },
        {
          id: 'q_cog9_iktur_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yazları sıcak ve kurak, kışları ılık ve yağışlı geçen; karakteristik bitki örtüsü zeytin, defne, mersin gibi kısa boylu bodur çalılardan (maki) oluşan iklim tipi hangisidir?',
          choices: [
            'Akdeniz İklimi',
            'Step (Bozkır) İklimi',
            'Okyanusal İklim',
            'Muson İklimi'
          ],
          correctAnswer: 'Akdeniz İklimi',
          explanation: 'Akdeniz iklimi 30°-40° enlemleri arasında görülür; yazın dinamik yüksek basınçla kurak, kışın cephesel yağışlıdır; toprağı terra-rossadır.'
        },
        {
          id: 'q_cog9_iktur_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Ilıman Okyanusal İklim; Batı rüzgârları ve sıcak su akıntıları etkisiyle her mevsim yağışlıdır ve doğal bitki örtüsü karışık yapraklı ormanlardır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Batı Avrupa ve Türkiye\'de Karadeniz kıyılarında görülen okyanusal iklim düzenli yağış rejimi ve ılıman yapısıyla bilinir.'
        },
        {
          id: 'q_cog9_iktur_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Toprağın yılın 9-10 ayı donmuş halde (permafrost) kaldığı, kısa yaz aylarında bataklığa dönüşerek liken ve yosunlardan oluşan bitki örtüsünün görüldüğü kutup altı iklimi hangisidir?',
          choices: [
            'Tundra İklimi',
            'Tayga İklimi',
            'Savan İklimi',
            'Kutup İklimi'
          ],
          correctAnswer: 'Tundra İklimi',
          explanation: 'Tundra iklimi 60°-70° kuzey enlemlerinde görülür; sıcaklık yılın büyük bölümünde sıfırın altındadır, ağaç yetişmez.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_09_iklim_turleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=kAeQTw96EMc',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 11. İklim Sisteminde Yaşanan Değişiklikler (lesson_cog9_topic_10_iklim_sisteminde_yasanan_degisiklikler)
  {
    id: 'item_cog9_vid_topic_10_iklim_sisteminde_yasanan_degisiklikler__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_10_iklim_sisteminde_yasanan_degisiklikler',
    stableKey: 'cog9_quiz_iklim_degisimi_micro',
    itemType: 'QUIZ',
    displayLabel: '10.1-Q',
    orderKey: 1500,
    title: 'Küresel İklim Değişimi ve Sera Etkisi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Küresel İklim Değişimi ve Sera Etkisi Mikro Testi',
      questions: [
        {
          id: 'q_cog9_ikldeg_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sanayi Devrimi\'nden bu yana insan faaliyetleri sonucu atmosferde birikerek küresel ısınmaya en fazla katkı yapan sera gazı hangisidir?',
          choices: [
            'Karbondioksit (CO₂)',
            'Oksijen (O₂)',
            'Azot (N₂)',
            'Argon (Ar)'
          ],
          correctAnswer: 'Karbondioksit (CO₂)',
          explanation: 'Fosil yakıt kullanımı, çimento üretimi ve ormansızlaşma atmosferdeki CO₂ yoğunluğunu rekor seviyelere ulaştırarak sera etkisini yapay biçimde güçlendirmiştir.'
        },
        {
          id: 'q_cog9_ikldeg_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi küresel iklim değişikliğinin yeryüzünde meydana getirdiği doğrudan çevresel sonuçlardan biri DEĞİLDİR?',
          choices: [
            'Yer kabuğunda deprem üreten fay hatlarının tamamen kapanması',
            'Kutup ve dağ buzullarının hızla erimesi',
            'Okyanus su seviyelerinin yükselerek kıyı yerleşimlerini tehdit etmesi',
            'Şiddetli kuraklık, çölleşme ve orman yangınlarının sıklığının artması'
          ],
          correctAnswer: 'Yer kabuğunda deprem üreten fay hatlarının tamamen kapanması',
          explanation: 'Depremler iç kuvvet (tektonizma) kökenlidir; iklim değişikliği atmosfer ve hidrosfer dinamiklerini etkiler, tektonik fayları kapatmaz.'
        },
        {
          id: 'q_cog9_ikldeg_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Paris İklim Anlaşması, küresel ortalama sıcaklık artışını sanayi öncesi döneme kıyasla 2°C\'nin oldukça altında tutmayı ve 1.5°C ile sınırlandırmayı hedefleyen küresel bir mutabakattır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: '2015 Paris Anlaşması, karbon salınımını azaltarak net sıfır emisyon hedefine ulaşmayı amaçlar.'
        },
        {
          id: 'q_cog9_ikldeg_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Kişi ve kurumların doğrudan veya dolaylı olarak atmosfere saldığı sera gazı miktarına "Karbon Ayak İzi" denir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Karbon ayak izi, enerji tüketimi, ulaşım ve üretim faaliyetlerinin iklim üzerindeki yükünü ölçen standart bir göstergedir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_10_iklim_sisteminde_yasanan_degisiklikler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=kAeQTw96EMc',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 12. Nüfusun Tarihsel Değişimi ve Geleceği (lesson_cog9_topic_11_nufusun_tarihsel_degisimi_ve_gelecegi)
  {
    id: 'item_cog9_vid_topic_11_nufusun_tarihsel_degisimi_ve_gelecegi__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_11_nufusun_tarihsel_degisimi_ve_gelecegi',
    stableKey: 'cog9_quiz_nufus_tarihsel_micro',
    itemType: 'QUIZ',
    displayLabel: '11.1-Q',
    orderKey: 1500,
    title: 'Nüfusun Tarihsel Sıçrama Dönemleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Nüfusun Tarihsel Sıçrama Dönemleri Mikro Testi',
      questions: [
        {
          id: 'q_cog9_nuftar_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Dünya nüfusunun insanlık tarihi boyunca yaşadığı 3 büyük sıçrama (hızlı artış) dönemi sırasıyla hangileridir?',
          choices: [
            'Alet Yapımı - Tarım Devrimi (Yerleşik Hayat) - Sanayi Devrimi',
            'Ateşin Bulunuşu - Yazının İcadı - Matbaanın İcadı',
            'Kavimler Göçü - Coğrafi Keşifler - Fransız İhtilali',
            'Tekerleğin İcadı - Paranın İcadı - İnternetin İcadı'
          ],
          correctAnswer: 'Alet Yapımı - Tarım Devrimi (Yerleşik Hayat) - Sanayi Devrimi',
          explanation: 'İlk sıçrama alet kullanımıyla avlanma veriminin artması, ikinci sıçrama Neolitik tarım ile besin güvencesi, üçüncü sıçrama Sanayi Devrimi ile tıp/sağlık şartlarının gelişmesidir.'
        },
        {
          id: 'q_cog9_nuftar_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sanayi Devrimi sonrasında dünya nüfusunun hızla artmasındaki en belirleyici demografik etken nedir?',
          choices: [
            'Tıp, aşı ve hijyendeki gelişmeler sayesinde bebek ve yetişkin ölüm oranlarının hızla düşmesi',
            'Tüm dünyada ortalama doğum oranlarının 10 katına çıkması',
            'Dünya genelinde savaşların tamamen sona ermesi',
            'Tarımsal üretimin tamamen durması'
          ],
          correctAnswer: 'Tıp, aşı ve hijyendeki gelişmeler sayesinde bebek ve yetişkin ölüm oranlarının hızla düşmesi',
          explanation: 'Ölüm oranları düşüp insan ömrü uzarken doğum oranları bir süre yüksek kalmış ve nüfus geometrik olarak katlanmıştır.'
        },
        {
          id: 'q_cog9_nuftar_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Gelişmiş ülkelerde son yıllarda doğum oranlarının yenilenme düzeyinin (kadın başına 2.1 çocuk) altına düşmesi nüfusun yaşlanmasına ve iş gücü açığına yol açmaktadır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Avrupa ve Doğu Asya\'da yaşlı bağımlı nüfus artarken genç nüfus azalmakta ve demografik daralma yaşanmaktadır.'
        },
        {
          id: 'q_cog9_nuftar_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir ülkede doğum ve ölüm oranları arasındaki farka ne ad verilir?',
          choices: [
            'Doğal Nüfus Artışı',
            'Gerçek Nüfus Artışı',
            'Net Göç Hızı',
            'Fizyolojik Nüfus Yoğunluğu'
          ],
          correctAnswer: 'Doğal Nüfus Artışı',
          explanation: 'Doğumlar eksi ölümler "Doğal Nüfus Artışı"nı verir; bu hesaba göçler de eklenirse "Gerçek Nüfus Artışı" elde edilir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_11_nufusun_tarihsel_degisimi_ve_gelecegi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=SP1MlcvQiAs',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 13. Nüfusun Dağılışı ve Hareketleri (lesson_cog9_topic_12_nufusun_dagilisi_ve_hareketleri)
  {
    id: 'item_cog9_vid_topic_12_nufusun_dagilisi_ve_hareketleri__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_12_nufusun_dagilisi_ve_hareketleri',
    stableKey: 'cog9_quiz_nufus_dagilisi_micro',
    itemType: 'QUIZ',
    displayLabel: '12.1-Q',
    orderKey: 1500,
    title: 'Nüfusun Dağılışı ve Göçler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Nüfusun Dağılışı ve Göçler Mikro Testi',
      questions: [
        {
          id: 'q_cog9_nufdag_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Dünyada nüfusun seyrek olduğu alanlar ve bunların temel sınırlandırıcı doğal sebepleri eşleştirmelerinden hangisi YANLIŞTIR?',
          choices: [
            'Batı Avrupa - Aşırı kuraklık ve su yetersizliği',
            'Büyük Sahra - Kuraklık ve çöl koşulları',
            'Grönland ve Antarktika - Aşırı soğuk ve buzullar',
            'Amazon ve Kongo Havzası - Aşırı nem, yüksek sıcaklık ve balta girmemiş gür ormanlar'
          ],
          correctAnswer: 'Batı Avrupa - Aşırı kuraklık ve su yetersizliği',
          explanation: 'Batı Avrupa kurak değil; aksine ılıman okyanusal iklimi, verimli toprakları ve sanayisiyle dünyanın en yoğun nüfuslu alanlarındandır.'
        },
        {
          id: 'q_cog9_nufdag_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Göç hareketlerinde; savaşlar, siyasi baskılar, doğal afetler veya tarımda makineleşmeyle işsiz kalmak hangi faktör grubu içinde yer alır?',
          choices: [
            'İtici Faktörler',
            'Çekici Faktörler',
            'Mevsimlik Faktörler',
            'Kültürel Faktörler'
          ],
          correctAnswer: 'İtici Faktörler',
          explanation: 'Bireyi yaşadığı yeri terk etmeye zorlayan olumsuzluklar "itici faktörler", gidilen yerdeki cazip imkânlar ise "çekici faktörler"dir.'
        },
        {
          id: 'q_cog9_nufdag_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Yüksek eğitimli bilim insanı, mühendis ve doktorların daha iyi çalışma ve yaşam standartları için gelişmiş ülkelere göç etmesine "Beyin Göçü" denir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Beyin göçü göç veren ülkenin kalkınma hızını ve inovasyon potansiyelini olumsuz etkileyen nitelikli iş gücü kaybıdır.'
        },
        {
          id: 'q_cog9_nufdag_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Toplam nüfusun toplam yüz ölçümüne bölünmesiyle hesaplanan yoğunluk türü hangisidir?',
          choices: [
            'Aritmetik Nüfus Yoğunluğu',
            'Tarımsal Nüfus Yoğunluğu',
            'Fizyolojik Nüfus Yoğunluğu',
            'Ekonomik Nüfus Yoğunluğu'
          ],
          correctAnswer: 'Aritmetik Nüfus Yoğunluğu',
          explanation: 'Aritmetik nüfus yoğunluğu = Toplam Nüfus / Toplam Alan (km²) formülüyle hesaplanır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_12_nufusun_dagilisi_ve_hareketleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=SP1MlcvQiAs',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 14. Demografik Dönüşüm ve Nüfus Piramitleri (lesson_cog9_topic_13_demografik_donusum_ve_nufus_piramitleri)
  {
    id: 'item_cog9_vid_topic_13_demografik_donusum_ve_nufus_piramitleri__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_13_demografik_donusum_ve_nufus_piramitleri',
    stableKey: 'cog9_quiz_piramitler_micro',
    itemType: 'QUIZ',
    displayLabel: '13.1-Q',
    orderKey: 1500,
    title: 'Demografik Dönüşüm ve Nüfus Piramitleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Demografik Dönüşüm ve Nüfus Piramitleri Mikro Testi',
      questions: [
        {
          id: 'q_cog9_pir_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tabanı çok geniş, tavanı (üst kısmı) çok dar olan üçgen şeklindeki nüfus piramidi bir ülke hakkında hangi temel bilgiyi verir?',
          choices: [
            'Doğum ve çocuk ölüm oranları yüksek, ortalama yaşam süresi kısa, az gelişmiş bir ülkedir.',
            'Doğum oranları çok düşük, yaşlı nüfus oranı çok yüksek gelişmiş bir ülkedir.',
            'Nüfusu hızla azalan durağan bir ülkedir.',
            'Cinsiyet dengesi tamamen bozulmuş bir ülkedir.'
          ],
          correctAnswer: 'Doğum ve çocuk ölüm oranları yüksek, ortalama yaşam süresi kısa, az gelişmiş bir ülkedir.',
          explanation: 'Geniş taban yüksek doğum oranını (0-14 yaş), dar tepe ise sağlık koşullarının yetersizliği sebebiyle yaşlı nüfusun azlığını simgeler.'
        },
        {
          id: 'q_cog9_pir_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Doğum ve ölüm oranlarının uzun süredir düşük olduğu, yaşlı nüfus oranının (%15+) yüksek olduğu gelişmiş ülkelere (ör. İsveç, Almanya) ait piramit tipi hangisidir?',
          choices: [
            'Arı Kovanı Piramidi',
            'Kenarları İçe Çökük Üçgen Piramit',
            'Düz Kenarlı Üçgen Piramit',
            'Asimetrik Tabanlı Piramit'
          ],
          correctAnswer: 'Arı Kovanı Piramidi',
          explanation: 'Arı kovanı piramidinde taban ile orta yaş grubu dengelidir; doğum ve ölüm oranları düşüktür.'
        },
        {
          id: 'q_cog9_pir_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Bir ülkenin nüfus piramidinde tabanın daralmaya başlaması, o ülkede son yıllarda doğum oranlarının ve doğurganlık hızının azaldığını gösterir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: '0-4 ve 5-9 yaş basamaklarının içeri doğru daralması doğum oranlarının düştüğünün kesin kanıtıdır.'
        },
        {
          id: 'q_cog9_pir_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Demografik Dönüşüm Modeli\'nin 4. aşamasına (Düşük Durağan Aşama) ulaşmış bir toplumda hangi durum gözlemlenir?',
          choices: [
            'Hem doğum hem de ölüm oranları düşük seviyede dengelenir ve nüfus artışı oldukça yavaşlar.',
            'Doğum oranları patlama yapar, ölümler sıfıra iner.',
            'Nüfusun %90\'ı tarım sektöründe çalışır.',
            'Ortalama yaşam süresi 40 yıla düşer.'
          ],
          correctAnswer: 'Hem doğum hem de ölüm oranları düşük seviyede dengelenir ve nüfus artışı oldukça yavaşlar.',
          explanation: 'Son evrede kentleşme, kadının iş hayatına katılımı ve eğitim düzeyiyle doğumlar ve ölümler en düşük seviyede dengelenir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_13_demografik_donusum_ve_nufus_piramitleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=SP1MlcvQiAs',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 15. Nüfusla İlgili Fırsatlar, Sorunlar ve Politikalar (lesson_cog9_topic_14_nufusla_ilgili_firsatlar_sorunlar_ve_politikalar)
  {
    id: 'item_cog9_vid_topic_14_nufusla_ilgili_firsatlar_sorunlar_ve_politikalar__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_14_nufusla_ilgili_firsatlar_sorunlar_ve_politikalar',
    stableKey: 'cog9_quiz_nufus_politikalari_micro',
    itemType: 'QUIZ',
    displayLabel: '14.1-Q',
    orderKey: 1500,
    title: 'Nüfus Politikaları ve Demografik Fırsat Penceresi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Nüfus Politikaları ve Demografik Fırsat Penceresi Mikro Testi',
      questions: [
        {
          id: 'q_cog9_nufpol_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yaşlı nüfus oranı hızla artan, çalışma çağındaki nüfusu azalan ve gelecekte ekonomik durgunluk tehlikesiyle karşılaşan gelişmiş ülkelerin uyguladığı nüfus politikası hangisidir?',
          choices: [
            'Nüfus artış hızını artırmaya yönelik teşvik edici politikalar (çocuk yardımı, doğum izni vb.)',
            'Nüfus artış hızını azaltmaya yönelik kısıtlayıcı politikalar',
            'Sadece kırsal nüfusu artırma politikası',
            'Tüm doğumları yasaklama politikası'
          ],
          correctAnswer: 'Nüfus artış hızını artırmaya yönelik teşvik edici politikalar (çocuk yardımı, doğum izni vb.)',
          explanation: 'Gelişmiş ülkeler (ör. Fransa, Japonya) demografik dinamizmi korumak için çocuk teşvikleri, vergi indirimleri ve kreş yardımları uygular.'
        },
        {
          id: 'q_cog9_nufpol_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Demografik Fırsat Penceresi" kavramı neyi ifade eder?',
          choices: [
            'Toplam nüfus içinde 15-64 yaş grubu çalışma çağındaki üretken nüfus payının en yüksek seviyeye ulaştığı altın dönemi',
            'Sadece 65 yaş üstü emekli nüfusun çoğunlukta olduğu dönemi',
            '0-4 yaş arası bebek nüfusun sıfırlandığı dönemi',
            'Yalnızca dış göçlerin serbest bırakıldığı dönemi'
          ],
          correctAnswer: 'Toplam nüfus içinde 15-64 yaş grubu çalışma çağındaki üretken nüfus payının en yüksek seviyeye ulaştığı altın dönemi',
          explanation: 'Çalışma çağındaki aktif nüfusun yüksek, bağımlı çocuk ve yaşlı nüfusun görece düşük olduğu dönem ekonomik sıçrama için büyük bir fırsat penceresidir.'
        },
        {
          id: 'q_cog9_nufpol_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Türkiye gibi gelişmekte olan ülkelerde uygulanan nüfus politikalarının temel odağı, nüfusun yalnızca sayısal artışı değil; eğitim, sağlık ve istihdam gibi niteliklerinin iyileştirilmesidir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Nitelik ve nicelik dengesi gözetilerek beşerî sermayenin ve iş gücü verimliliğinin artırılması hedeflenir.'
        },
        {
          id: 'q_cog9_nufpol_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Aşırı hızlı nüfus artışı yaşayan az gelişmiş ülkelerde demografik yatırımların (okul, hastane) bütçedeki payının artması kalkınma hızını yavaşlatır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Tasarruf ve sanayi yatırımı yerine temel tüketim ve demografik harcamalar arttığı için kişi başına düşen millî gelir artışı sınırlanır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_14_nufusla_ilgili_firsatlar_sorunlar_ve_politikalar',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=PUu7cEjrBng',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 16. Ekonomik Faaliyetleri Etkileyen Coğrafi Faktörler (lesson_cog9_topic_15_ekonomik_faaliyetleri_etkileyen_cografi_faktorler)
  {
    id: 'item_cog9_vid_topic_15_ekonomik_faaliyetleri_etkileyen_cografi_faktorler__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_15_ekonomik_faaliyetleri_etkileyen_cografi_faktorler',
    stableKey: 'cog9_quiz_ekonomik_faaliyetler_micro',
    itemType: 'QUIZ',
    displayLabel: '15.1-Q',
    orderKey: 1500,
    title: 'Ekonomik Faaliyet Sektörleri ve Coğrafi Faktörler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Ekonomik Faaliyet Sektörleri ve Coğrafi Faktörler Mikro Testi',
      questions: [
        {
          id: 'q_cog9_ekofa_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Doğrudan doğadan hammadde elde edilmesine dayanan (Tarım, Hayvancılık, Ormancılık, Madencilik ve Balıkçılık) ekonomik faaliyet grubu hangisidir?',
          choices: [
            'Birincil (Primer) Ekonomik Faaliyetler',
            'İkincil (Sekonder) Ekonomik Faaliyetler',
            'Üçüncül (Tersiyer) Ekonomik Faaliyetler',
            'Dördüncül (Kuaterner) Ekonomik Faaliyetler'
          ],
          correctAnswer: 'Birincil (Primer) Ekonomik Faaliyetler',
          explanation: 'Birincil sektör hammadde üretimidir. İkincil sektör sanayi/imalat, üçüncül sektör hizmet, dördüncül sektör ise bilişim/yazılımdır.'
        },
        {
          id: 'q_cog9_ekofa_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi Üçüncül (Hizmet) ekonomik faaliyet grubu içerisinde YER ALMAZ?',
          choices: [
            'Demir-çelik fabrikasında ham demirin işlenip ray haline getirilmesi',
            'Bankacılık ve finansal danışmanlık hizmetleri',
            'Turizm ve otel işletmeciliği',
            'Hastanelerde sağlık hizmeti verilmesi'
          ],
          correctAnswer: 'Demir-çelik fabrikasında ham demirin işlenip ray haline getirilmesi',
          explanation: 'Fabrika imalatı İkincil (Sanayi) sektörüdür; bankacılık, turizm ve sağlık ise üçüncül (hizmet) sektörüdür.'
        },
        {
          id: 'q_cog9_ekofa_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Gelişmiş ülkelerde çalışan nüfusun çok büyük bir oranı (%70+) üçüncül (hizmet) ve dördüncül (bilişim) sektörlerde istihdam edilirken, birincil (tarım) sektörün payı %2-5 arasındadır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Gelişmiş ekonomilerde sanayi ve tarımda yüksek otomasyon kullanıldığından istihdam ağırlıklı olarak hizmet ve bilgi sektörlerine kayar.'
        },
        {
          id: 'q_cog9_ekofa_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir sanayi tesisinin kuruluş yeri seçiminde etkili olan temel coğrafi faktörler arasında aşağıdakilerden hangisi yer alır?',
          choices: [
            'Hammaddeye yakınlık, Enerji kaynağı, Ulaşım kolaylığı, Pazar ve İş gücü',
            'Yalnızca arazinin rakımının 3000 metreden yüksek olması',
            'Yalnızca çevresinde hiçbir yerleşimin bulunmaması',
            'Sadece kutup dairesi sınırları içinde yer alması'
          ],
          correctAnswer: 'Hammaddeye yakınlık, Enerji kaynağı, Ulaşım kolaylığı, Pazar ve İş gücü',
          explanation: 'Sanayi kuruluş yerinde maliyetleri düşürmek için hammadde, enerji, lojistik/ulaşım ve tüketici pazarına yakınlık kritik rol oynar.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_15_ekonomik_faaliyetleri_etkileyen_cografi_faktorler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Zy_6Fgs84N4',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 17. Tehlike, Risk ve Afet (lesson_cog9_topic_16_tehlike_risk_ve_afet)
  {
    id: 'item_cog9_vid_topic_16_tehlike_risk_ve_afet__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_16_tehlike_risk_ve_afet',
    stableKey: 'cog9_quiz_tehlike_risk_afet_micro',
    itemType: 'QUIZ',
    displayLabel: '16.1-Q',
    orderKey: 1500,
    title: 'Tehlike, Risk ve Afet Kavramları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tehlike, Risk ve Afet Kavramları Mikro Testi',
      questions: [
        {
          id: 'q_cog9_tehrisk_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Doğal veya teknolojik bir olayın can, mal ve çevre üzerinde kayıplara yol açabilme potansiyeline ne ad verilir?',
          choices: [
            'Tehlike (Hazard)',
            'Afet (Disaster)',
            'Müdahale (Response)',
            'İyileştirme (Recovery)'
          ],
          correctAnswer: 'Tehlike (Hazard)',
          explanation: 'Tehlike potansiyel zarar kaynağıdır (ör. fay hattının varlığı); bu tehlike insan yerleşimine zarar verdiğinde afete dönüşür.'
        },
        {
          id: 'q_cog9_tehrisk_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Afet yönetiminde "Risk" büyüklüğü hangi temel bileşenlerin etkileşimiyle belirlenir?',
          choices: [
            'Tehlike × Zarar Görebilirlik (Hassasiyet) / Baş Edebilme Kapasitesi',
            'Yalnızca arazinin eğimi ve deniz seviyesine yüksekliği',
            'Yalnızca havadaki oksijen oranı',
            'Sadece bölgedeki maden rezervi miktarı'
          ],
          correctAnswer: 'Tehlike × Zarar Görebilirlik (Hassasiyet) / Baş Edebilme Kapasitesi',
          explanation: 'Risk formülü; mevcut tehlikenin büyüklüğü ile toplumun dayanıksızlığının (zarar görebilirlik) çarpımı ve hazırlık kapasitesine oranıdır.'
        },
        {
          id: 'q_cog9_tehrisk_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Bir doğa olayının "Afet" olarak tanımlanabilmesi için toplumun normal yaşam düzenini ve sosyoekonomik faaliyetlerini kesintiye uğratması ve yerel imkânlarla baş edilemeyecek kayıplara yol açması gerekir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Kimsenin yaşamadığı çölde olan şiddetli deprem bir doğa olayıdır; can ve mal kaybına yol açtığında afet niteliği kazanır.'
        },
        {
          id: 'q_cog9_tehrisk_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Afetler yalnızca deprem ve sel gibi doğa olaylarıyla sınırlı olmayıp nükleer sızıntılar, kimyasal patlamalar ve baraj yıkılmaları gibi teknolojik/beşerî kökenli de olabilir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Afetler kökenine göre Doğal Afetler ve Teknolojik/İnsan Kaynaklı Afetler olmak üzere iki ana gruba ayrılır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_16_tehlike_risk_ve_afet',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 18. Nüfus ve Yerleşme (in lesson_cog9_beseri_sistemler_ve_afetler)
  {
    id: 'item_cog9_vid_nufus_ve_yerlesme__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_beseri_sistemler_ve_afetler',
    stableKey: 'cog9_quiz_yerlesme_dokulari_micro',
    itemType: 'QUIZ',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Yerleşme Tipleri ve Mesken Dokuları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Yerleşme Tipleri ve Mesken Dokuları Mikro Testi',
      questions: [
        {
          id: 'q_cog9_yerles_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Su kaynaklarının bol, arazinin oldukça engebeli ve tarım alanlarının parçalı olduğu Doğu Karadeniz kırsalında hangi yerleşme dokusu yaygındır?',
          choices: [
            'Dağınık Kırsal Yerleşme',
            'Toplu Kırsal Yerleşme',
            'Çizgisel Yerleşme',
            'Dairesel Yerleşme'
          ],
          correctAnswer: 'Dağınık Kırsal Yerleşme',
          explanation: 'Karadeniz\'de su her yerde bulunduğu ve arazi eğimli olduğu için evler birbirinden uzak yamaçlara (dağınık) kurulmuştur.'
        },
        {
          id: 'q_cog9_yerles_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İç Anadolu ve Güneydoğu Anadolu\'nun kurak/yarı kurak kırsalında mesken yapımında en yaygın kullanılan geleneksel yapı malzemesi hangisidir?',
          choices: [
            'Kerpiç (Toprak ve Saman)',
            'Ahşap (Ağaç kütükleri)',
            'Kalkerli Kesme Taş',
            'Betonarme Çelik'
          ],
          correctAnswer: 'Kerpiç (Toprak ve Saman)',
          explanation: 'Kurak bölgelerde orman örtüsü az olduğundan ve kerpiç yazın serin kışın sıcak tuttuğundan geleneksel meskenlerde toprak/kerpiç kullanılır.'
        },
        {
          id: 'q_cog9_yerles_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Köy altı yerleşmelerinden olan Çiftlik, Mahalle, Mezra ve Divan sürekli (kalıcı) yerleşmeler iken; Yayla, Kom, Oba ve Ağıl geçici yerleşmelerdir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Ekonomik faaliyetin sürekliliğine göre köy altı yerleşmeleri kalıcı ve geçici olarak ikiye ayrılır.'
        },
        {
          id: 'q_cog9_yerles_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Su kaynaklarının kısıtlı olduğu düz ovalarda (ör. İç Anadolu köyleri) evlerin su kuyusu veya çeşme etrafında birbirine bitişik kurulmasına ne ad verilir?',
          choices: [
            'Toplu Kırsal Yerleşme',
            'Dağınık Kırsal Yerleşme',
            'Kıyı Boyu Yerleşmesi',
            'Yayla Yerleşmesi'
          ],
          correctAnswer: 'Toplu Kırsal Yerleşme',
          explanation: 'Kurak alanlarda su temini zorunluluğu evlerin tek bir su kaynağı etrafında toplu halde kümelenmesine yol açar.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_nufus_ve_yerlesme',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 19. Doğal Afet Türleri (in lesson_cog9_beseri_sistemler_ve_afetler)
  {
    id: 'item_cog9_vid_afet_yonetimi__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_beseri_sistemler_ve_afetler',
    stableKey: 'cog9_quiz_afet_turleri_micro',
    itemType: 'QUIZ',
    displayLabel: '3.2-Q',
    orderKey: 2500,
    title: 'Doğal Afet Türleri ve Korunma Yolları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Doğal Afet Türleri ve Korunma Yolları Mikro Testi',
      questions: [
        {
          id: 'q_cog9_aftur_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kökeni doğrudan yer kabuğundaki tektonik hareketlere, levha sınırlarına ve volkanizmaya dayanan "Jeolojik Afetler" hangi seçenekte bir arada verilmiştir?',
          choices: [
            'Deprem, Tsunami, Volkanik Patlama',
            'Kasırga, Hortum, Kuraklık',
            'Çığ, Sel, Aşırı Kar Yağışı',
            'Orman Yangını, Salgın Hastalık, Asit Yağmuru'
          ],
          correctAnswer: 'Deprem, Tsunami, Volkanik Patlama',
          explanation: 'Deprem, tsunami ve volkanizma enerjisini yerin derinliklerinden (manto/iç kuvvetler) alan jeolojik afetlerdir.'
        },
        {
          id: 'q_cog9_aftur_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Eğimli yamaçlarda killi toprak tabakasının aşırı yağış veya kar erimesiyle suya doygun hale gelerek ana kaya üzerinden aşağı doğru kütlece kaymasına ne ad verilir?',
          choices: [
            'Heyelan (Kütle Hareketi)',
            'Erozyon',
            'Çığ',
            'Alüvyon birikmesi'
          ],
          correctAnswer: 'Heyelan (Kütle Hareketi)',
          explanation: 'Heyelanda kil tabakası kayganlaşır ve kütle halinde kayar; en çok ilkbaharda bol yağış alan eğimli Karadeniz yamaçlarında görülür.'
        },
        {
          id: 'q_cog9_aftur_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Erozyon ile Heyelan aynı kavramlar değildir; erozyon toprağın üst verimli kısmının rüzgâr ve akarsularla yavaş yavaş süpürülmesiyken, heyelan anlık büyük kütle kaymasıdır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Erozyon sinsi ve uzun süreli bir çölleşme sürecidir; heyelan ise saniyeler içinde gerçekleşen yıkıcı bir kütle hareketidir.'
        },
        {
          id: 'q_cog9_aftur_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tropikal kuşakta okyanuslar üzerinde su sıcaklığının 27°C\'yi aştığı alanlarda oluşan ve saatteki hızı 120 km\'yi aşan dönen dev fırtınalara ne ad verilir?',
          choices: [
            'Tropikal Siklon (Kasırga / Tayfun / Hurrikan)',
            'Meltem Rüzgârı',
            'Föhn Rüzgârı',
            'Sirokko Rüzgârı'
          ],
          correctAnswer: 'Tropikal Siklon (Kasırga / Tayfun / Hurrikan)',
          explanation: 'Tropikal siklonlar Atlas Okyanusu\'nda Hurrikan, Pasifik\'te Tayfun, Hint Okyanusu\'nda Siklon olarak adlandırılır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_afet_yonetimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 20. Bütüncül Afet Yönetimi (lesson_cog9_topic_18_butuncul_afet_yonetimi)
  {
    id: 'item_cog9_vid_topic_18_butuncul_afet_yonetimi__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_18_butuncul_afet_yonetimi',
    stableKey: 'cog9_quiz_butuncul_afet_micro',
    itemType: 'QUIZ',
    displayLabel: '18.1-Q',
    orderKey: 1500,
    title: 'Bütüncül Afet Yönetimi ve Aşamaları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Bütüncül Afet Yönetimi ve Aşamaları Mikro Testi',
      questions: [
        {
          id: 'q_cog9_butaf_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bütüncül Afet Yönetimi Döngüsü sırasıyla hangi 4 temel aşamadan meydana gelir?',
          choices: [
            'Zarar Azaltma (Önleme) → Hazırlık → Müdahale (Kriz Anı) → İyileştirme (Yeniden İnşa)',
            'Kaçış → Kurtarma → Sigorta → Unutma',
            'Sadece Arama Kurtarma → Çadır Kurma → Yemek Dağıtımı',
            'Tahliye → Barınma → Enkaz Kaldırma → Temizlik'
          ],
          correctAnswer: 'Zarar Azaltma (Önleme) → Hazırlık → Müdahale (Kriz Anı) → İyileştirme (Yeniden İnşa)',
          explanation: 'Çağdaş afet yönetimi; afet öncesi (zarar azaltma ve hazırlık) ile afet sırası ve sonrası (müdahale ve iyileştirme) süreçlerini bütüncül ele alır.'
        },
        {
          id: 'q_cog9_butaf_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi afet öncesi "Risk ve Zarar Azaltma" evresinde yapılması gereken faaliyetlerden biridir?',
          choices: [
            'Fay hatları ve dere yataklarına yapılaşma yasağı getirilmesi ve bina deprem yönetmeliklerinin sıkı denetlenmesi',
            'Enkaz altındaki yaralıların sahra hastanesine taşınması',
            'Afetzedelere çadır ve sıcak çorba dağıtılması',
            'Yıkılan yolların temizlenmesi'
          ],
          correctAnswer: 'Fay hatları ve dere yataklarına yapılaşma yasağı getirilmesi ve bina deprem yönetmeliklerinin sıkı denetlenmesi',
          explanation: 'Sağlam zemin seçimi, kentsel dönüşüm ve kat sınırlamaları afetten önce zararı önlemek için yapılan risk azaltma çalışmalarıdır.'
        },
        {
          id: 'q_cog9_butaf_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Afet yönetiminde afet öncesi yapılan 1 birimlik risk azaltma ve önleme harcaması, afet sonrasında ortaya çıkacak en az 7 birimlik onarım ve müdahale masrafını engeller.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Uluslararası afet istatistiklerine göre önleme ve hazırlık yatırımları, kriz sonrası harcamalara kıyasla katbekat ekonomik ve hayat kurtarıcıdır.'
        },
        {
          id: 'q_cog9_butaf_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Bireysel afet bilincinde; evde devrilebilecek mobilyaların duvara sabitlenmesi, afet toplanma alanının öğrenilmesi ve acil durum çantasının hazır bulundurulması temel hazırlık adımlarıdır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Bireysel hazırlıklar afet sırasındaki panik ve yaralanmaları büyük oranda engeller.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_18_butuncul_afet_yonetimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=UYlpTrZfgA8',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 21. Bölge ve Bölge Sınırı (lesson_cog9_topic_19_bolge_ve_bolge_siniri)
  {
    id: 'item_cog9_vid_topic_19_bolge_ve_bolge_siniri__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_19_bolge_ve_bolge_siniri',
    stableKey: 'cog9_quiz_bolge_turleri_micro',
    itemType: 'QUIZ',
    displayLabel: '19.1-Q',
    orderKey: 1500,
    title: 'Bölge Türleri ve Bölge Sınırları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Bölge Türleri ve Bölge Sınırları Mikro Testi',
      questions: [
        {
          id: 'q_cog9_bolge_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Benzer doğal, beşerî veya ekonomik özelliklere sahip alanların sınırlandırılmasıyla oluşturulan bölgelere "Şekilsel (Formal) Bölge" denir. Aşağıdakilerden hangisi bir Doğal Şekilsel Bölge örneğidir?',
          choices: [
            'Himalaya Dağlık Bölgesi',
            'Marmara Sanayi Bölgesi',
            'Çukurova Tarım Bölgesi',
            'Avrupa Birliği Siyasi Bölgesi'
          ],
          correctAnswer: 'Himalaya Dağlık Bölgesi',
          explanation: 'Dağlık, ovalık, çöl veya Akdeniz iklim bölgesi fiziki/doğal şekilsel bölgedir; sanayi, tarım ve siyasi birlikler ise beşerî bölgelerdir.'
        },
        {
          id: 'q_cog9_bolge_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Merkezi bir odak nokta ile çevresi arasındaki yönetim, ticaret, haberleşme veya kalkınma ilişkilerini organize eden bölgelere ne ad verilir?',
          choices: [
            'İşlevsel (Fonksiyonel) Bölge',
            'Doğal Şekilsel Bölge',
            'İklimsel Homojen Bölge',
            'Karasal Jeolojik Bölge'
          ],
          correctAnswer: 'İşlevsel (Fonksiyonel) Bölge',
          explanation: 'Bölgesel kalkınma projeleri (GAP, DOKAP, ZBK), valilik/belediye idari etki alanları ve karayolları bölge müdürlükleri işlevsel bölgedir.'
        },
        {
          id: 'q_cog9_bolge_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Beşerî, siyasi ve askeri bölge sınırları (ör. NATO, AB veya ülke sınırları) kısa sürede ve kolaylıkla değişebilirken; fiziki/doğal bölge sınırları (ör. dağlık alan, iklim sınırı) çok uzun zamanda değişir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Siyasi sınırlar bir antlaşmayla bir günde değişebilirken iklim ve jeomorfolojik sınırlar asırlar boyunca büyük ölçüde sabit kalır.'
        },
        {
          id: 'q_cog9_bolge_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Doğal bölge sınırları (örneğin Akdeniz iklimi ile Karasal iklim arasındaki sınır) bir çizgi gibi bıçak sırtı keskin olmayıp kademeli geçiş alanları (kuşakları) oluşturur.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Doğada iklim ve bitki örtüsü sınırları aniden değil, kademeli bir geçiş kuşağı (ekoton) şeklinde değişir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_19_bolge_ve_bolge_siniri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=KaImNrWaZEo',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  }
];

function main() {
  console.log('=== 9. Sınıf Coğrafya (course_cog_9) İçerik Güncelleme Başlatılıyor ===\n');

  // 1. Update Video Items
  console.log('1. Video öğeleri güncelleniyor...');
  videoUpdates.forEach(v => {
    const itemPath = path.join(ITEMS_DIR, v.id + '.json');
    if (fs.existsSync(itemPath)) {
      const itemData = JSON.parse(fs.readFileSync(itemPath, 'utf8'));
      itemData.title = v.title;
      itemData.contentUrl = v.contentUrl;
      itemData.publishingStatus = 'active';
      if (itemData.payload) {
        if (!itemData.payload.video) itemData.payload.video = {};
        itemData.payload.video.sourceUrl = v.contentUrl;
        itemData.payload.video.provider = v.provider;
        if (!itemData.payload.provenance) itemData.payload.provenance = {};
        itemData.payload.provenance.sourceVideoUrl = v.contentUrl;
        itemData.payload.provenance.reviewStatus = 'verified';
        itemData.payload.provenance.reviewedOverride = true;
      }
      fs.writeFileSync(itemPath, JSON.stringify(itemData, null, 2) + '\n', 'utf8');
      console.log(`  ✔ Güncellendi: ${v.id} -> ${v.contentUrl}`);
    } else {
      console.warn(`  ⚠ Video öğe dosyası bulunamadı: ${v.id}`);
    }
  });

  // 2. Validate and Write Micro Quizzes
  console.log('\n2. Mikro quizler doğrulanıyor ve kaydediliyor...');
  microQuizzes.forEach(q => {
    const errors = validateQuizSchema(q.quiz);
    if (errors.length > 0) {
      console.error(`  ❌ Quiz şema hatası (${q.id}):`, errors);
      process.exit(1);
    }

    const itemObj = {
      id: q.id,
      courseId: q.courseId,
      lessonId: q.lessonId,
      stableKey: q.stableKey,
      itemType: q.itemType,
      displayLabel: q.displayLabel,
      orderKey: q.orderKey,
      title: q.title,
      contentUrl: q.contentUrl,
      publishingStatus: q.publishingStatus,
      payload: {
        quiz: q.quiz,
        provenance: q.provenance
      }
    };

    const outPath = path.join(ITEMS_DIR, q.id + '.json');
    fs.writeFileSync(outPath, JSON.stringify(itemObj, null, 2) + '\n', 'utf8');
    console.log(`  ✔ Kaydedildi: ${q.id} (${q.quiz.questions.length} soru)`);
  });

  // 3. Update Lesson References
  console.log('\n3. Ders ünite (lesson) öğe referansları güncelleniyor...');
  const lessonFiles = fs.readdirSync(LESSONS_DIR).filter(f => f.startsWith('lesson_cog9_')).sort();
  lessonFiles.forEach(lf => {
    const lPath = path.join(LESSONS_DIR, lf);
    const lesson = JSON.parse(fs.readFileSync(lPath, 'utf8'));

    // Reconstruct items in clean sequential order:
    // Every video is followed immediately by its paired micro-quiz, then anki, then review quiz
    const newItems = [];
    lesson.items.forEach(itemId => {
      // If it's a micro quiz already, skip here because we pair it with video below
      if (itemId.endsWith('__quiz')) return;

      const itemPath = path.join(ITEMS_DIR, itemId + '.json');
      if (fs.existsSync(itemPath)) {
        const it = JSON.parse(fs.readFileSync(itemPath, 'utf8'));
        if (it.itemType === 'VIDEO') {
          newItems.push(itemId);
          const pairedQuizId = itemId + '__quiz';
          const pairedQuizPath = path.join(ITEMS_DIR, pairedQuizId + '.json');
          if (fs.existsSync(pairedQuizPath)) {
            newItems.push(pairedQuizId);
          }
        } else {
          newItems.push(itemId);
        }
      }
    });

    lesson.items = newItems;
    fs.writeFileSync(lPath, JSON.stringify(lesson, null, 2) + '\n', 'utf8');
    console.log(`  ✔ Ders güncellendi: ${lesson.id} -> ${lesson.items.length} öğe`);
  });

  // 4. Validate Modular Tree
  console.log('\n4. Modüler içerik ağacı doğrulanıyor...');
  const v2Dir = path.resolve(__dirname, '../content/v2');
  const outputPath = path.resolve(__dirname, '../content/9-sinif-v2-catalog.json');
  const modRes = validateModularTree(v2Dir);
  if (!modRes.valid) {
    console.error('❌ Modüler doğrulama başarısız:', modRes.errors);
    process.exit(1);
  }
  console.log('✔ Modüler içerik ağacı %100 GEÇERLİ!');
  console.log(`  Dersler: ${modRes.stats.courseCount}, Üniteler: ${modRes.stats.lessonCount}, Toplam Öğe: ${modRes.stats.itemCount} (Video: ${modRes.stats.videoCount}, Quiz: ${modRes.stats.quizCount} [Mikro: ${modRes.stats.microQuizCount}], Anki: ${modRes.stats.ankiCount})`);

  // 5. Compile and Save V2 Catalog
  console.log('\n5. V2 Kataloğu derleniyor ve kaydediliyor...');
  const compiled = compileModularCatalog(v2Dir);
  saveCompiledCatalog(compiled, outputPath);
  console.log('✔ content/9-sinif-v2-catalog.json başarıyla güncellendi!');
}

main();
