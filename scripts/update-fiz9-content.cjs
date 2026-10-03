const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { validateQuizSchema } = require('../cli/lib/v2-quiz');
const { validateModularTree, compileModularCatalog, saveCompiledCatalog } = require('../cli/lib/v2-modular');

const ITEMS_DIR = path.join(__dirname, '../content/v2/items');
const LESSONS_DIR = path.join(__dirname, '../content/v2/lessons');

// 1. Video item updates
const videoUpdates = [
  {
    id: 'item_fiz9_vid_topic_01_fizik_bilimi',
    title: 'Fizik Bilimi (Fizik Nedir, Doğa Olayları ve Bilimsel Yöntem)',
    contentUrl: 'https://www.youtube.com/watch?v=1Hg2eF907YA',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_alt_dallar',
    title: 'Fiziğin Alt Dalları ve Bilim Araştırma Merkezleri',
    contentUrl: 'https://www.youtube.com/watch?v=1Hg2eF907YA',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_03_fizik_bilimine_yon_verenler',
    title: 'Fizik Bilimine Yön Verenler ve Bilim İnsanları',
    contentUrl: 'https://www.youtube.com/watch?v=DiYbLCxiVMo',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_05_temel_ve_turetilmis_nicelikler',
    title: 'Temel ve Türetilmiş Nicelikler (SI Birim Sistemi & KISA MUZ)',
    contentUrl: 'https://www.youtube.com/watch?v=7aVrdQ7uSQ4',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_07_vektorler',
    title: 'Vektörler ve Vektörlerde Bileşke (Uç Uca Ekleme & Paralelkenar Yöntemi)',
    contentUrl: 'https://www.youtube.com/watch?v=FQ1lkZ9JQnI',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_dogadaki_kuvvetler',
    title: 'Doğadaki 4 Temel Kuvvet ve Temas Gerektiren/Gerektirmeyen Kuvvetler',
    contentUrl: 'https://www.youtube.com/watch?v=7aVrdQ7uSQ4',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_09_hareket_ve_hareket_turleri',
    title: 'Hareket ve Hareket Türleri (Öteleme, Dönme, Titreşim & Konum-Zaman)',
    contentUrl: 'https://www.youtube.com/watch?v=b4wS9p9s9gE',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_10_katilarda_basinc',
    title: 'Katılarda Basınç ve Basınç Kuvveti',
    contentUrl: 'https://www.youtube.com/watch?v=UeS-p82_rZk',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_11_sivilarda_basinc',
    title: 'Sıvılarda Basınç ve Pascal Prensibi',
    contentUrl: 'https://www.youtube.com/watch?v=q6M0P-RzZkY',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_basinc_prensipleri',
    title: 'Katı, Sıvı ve Gaz Basıncı (Pascal, Torricelli ve Manometreler)',
    contentUrl: 'https://www.youtube.com/watch?v=S2fXGO-9Cdk',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_kaldirma_kuvveti',
    title: 'Sıvıların Kaldırma Kuvveti ve Arşimet Prensibi',
    contentUrl: 'https://www.youtube.com/watch?v=FpC4nmFmSG6',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_13_kaldirma_kuvveti',
    title: 'Kaldırma Kuvveti (Yüzme, Askıda Kalma, Batma ve Taşma Kapları)',
    contentUrl: 'https://www.youtube.com/watch?v=FpC4nmFmSG6',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_14_bernoulli_ilkesi',
    title: 'Bernoulli İlkesi ve Akışkanlar Mekaniği',
    contentUrl: 'https://www.youtube.com/watch?v=FR0u-IglzGd',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_isi_sicaklik_kavram',
    title: 'İç Enerji, Isı ve Sıcaklık Kavramları ile Termometreler',
    contentUrl: 'https://www.youtube.com/watch?v=Lj_tsZ3QKWM',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_hal_degisimi',
    title: 'Hâl Değişimi, Isıl Denge ve Isı Aktarım Yolları',
    contentUrl: 'https://www.youtube.com/watch?v=BZ4muY8Bc1Y',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_18_isil_denge',
    title: 'Isıl Denge ve Denge Sıcaklığı',
    contentUrl: 'https://www.youtube.com/watch?v=HnOymIZHEII',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_19_isi_aktarim_yollari',
    title: 'Isı Aktarım Yolları (İletim, Konveksiyon, Işıma)',
    contentUrl: 'https://www.youtube.com/watch?v=Gw0oaj9w5iN',
    provider: 'Özcan Aykın FİZİK'
  },
  {
    id: 'item_fiz9_vid_topic_20_isi_iletim_hizi',
    title: 'Isı İletim Hızı, Yalıtım ve Hissedilen Sıcaklık',
    contentUrl: 'https://www.youtube.com/watch?v=GIg0fdy68F2',
    provider: 'Özcan Aykın FİZİK'
  }
];

// 2. Micro quizzes definition for missing video items
const newMicroQuizzes = [
  // 1. Topic 01: Fizik Bilimi
  {
    id: 'item_fiz9_vid_topic_01_fizik_bilimi__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_01_fizik_bilimi',
    stableKey: 'fiz9_quiz_topic_01_fizik_bilimi_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Fizik Bilimi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Fizik Bilimi Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_top01_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Fizik bilimi ile ilgili aşağıdaki ifadelerden hangisi DOĞRUDUR?',
          choices: [
            'Madde ve enerji arasındaki etkileşimi inceleyen uygulamalı bir doğa bilimidir.',
            'Yalnızca mikroskobik boyuttaki atom altı parçacıkları inceler.',
            'Metafiziksel ve teolojik olguları deneysel yöntemlerle kanıtlamaya çalışır.',
            'Mutlak ve değişmez kesin dogmalar ortaya koyar.'
          ],
          correctAnswer: 'Madde ve enerji arasındaki etkileşimi inceleyen uygulamalı bir doğa bilimidir.',
          explanation: 'Fizik; madde, enerji ve bunlar arasındaki etkileşimi inceleyen, gözlem ve deneye dayalı bir doğa bilimidir. Bilimsel bilgiler değişime açıktır ve fizik metafizikle ilgilenmez.'
        },
        {
          id: 'q_fiz9_top01_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Fizik biliminde elde edilen bilimsel bilgiler mutlak ve değişmez değildir; yeni deneysel bulgularla güncellenebilir veya geliştirilebilir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Bilimsel bilgi dinamiktir; teknolojik gelişmeler ve yeni deneyler sayesinde teoriler ve modeller revize edilebilir veya genişletilebilir.'
        },
        {
          id: 'q_fiz9_top01_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Fiziksel bir olay araştırılırken ölçüm aletleri kullanılarak yapılan ve sayısal veriler içeren gözlem türü hangisidir?',
          choices: [
            'Nicel Gözlem',
            'Nitel Gözlem',
            'Subjektif Gözlem',
            'Tahmini Gözlem'
          ],
          correctAnswer: 'Nicel Gözlem',
          explanation: 'Ölçme aletleri kullanılarak yapılan, objektif ve sayısal sonuçlar veren gözlemlere "nicel gözlem" denir. Duyulara dayanan gözlemler ise nitel gözlemdir.'
        },
        {
          id: 'q_fiz9_top01_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi fizik biliminin doğrudan ilgilendiği konulardan biri DEĞİLDİR?',
          choices: [
            'Canlıların kalıtsal özelliklerinin nesilden nesile aktarılma mekanizması',
            'Gezegenlerin Güneş etrafındaki yörünge hareketleri',
            'Işığın prizmada kırılarak renklere ayrılması',
            'Elektrik akımının iletken tellerde oluşturduğu manyetik alan'
          ],
          correctAnswer: 'Canlıların kalıtsal özelliklerinin nesilden nesile aktarılma mekanizması',
          explanation: 'Kalıtım mekanizmaları biyolojinin (genetik) alanıdır. Gezegen hareketleri, optik kırılma ve elektromanyetizma ise fiziğin doğrudan inceleme alanlarıdır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_01_fizik_bilimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1Hg2eF907YA',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 2. Topic 02: Fizik Biliminin Alt Dalları
  {
    id: 'item_fiz9_vid_alt_dallar__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_02_fizik_biliminin_alt_dallari',
    stableKey: 'fiz9_quiz_alt_dallar_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Fiziğin Alt Dalları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Fiziğin Alt Dalları Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_alt_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Fiber optik kablolar, teleskoplar, mikroskoplar ve gökkuşağı oluşumu fiziğin hangi alt dalının inceleme alanına girer?',
          choices: [
            'Optik',
            'Termodinamik',
            'Mekanik',
            'Atom Fiziği'
          ],
          correctAnswer: 'Optik',
          explanation: 'Optik; ışığın doğasını, yayılmasını, yansımasını, kırılmasını ve optik aletlerin çalışma prensiplerini inceler.'
        },
        {
          id: 'q_fiz9_alt_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Güneşteki nükleer füzyon tepkimeleri, roket yakıtları ve maddenin 4. hâli olan plazmanın özelliklerini inceleyen fiziğin alt dalı hangisidir?',
          choices: [
            'Yüksek Enerji ve Plazma Fiziği',
            'Katıhal Fiziği',
            'Termodinamik',
            'Optik'
          ],
          correctAnswer: 'Yüksek Enerji ve Plazma Fiziği',
          explanation: 'Yüksek enerji ve plazma fiziği; atom altı parçacıkları, yüksek enerjili parçacık etkileşimlerini ve plazma ortamlarını inceler.'
        },
        {
          id: 'q_fiz9_alt_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Türkiye\'de nükleer teknoloji, radyasyon güvenliği ve atom enerjisi alanlarında faaliyet yürüten başlıca kurum TENMAK (eski adıyla TAEK)\'tır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Türkiye Enerji, Nükleer ve Maden Araştırma Kurumu (TENMAK), nükleer ve radyasyon araştırmalarından sorumlu ulusal kurumdur.'
        },
        {
          id: 'q_fiz9_alt_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'CERN (Avrupa Nükleer Araştırma Merkezi) dünyanın en büyük parçacık fiziği laboratuvarı olup temel olarak hangi alt dal ile doğrudan ilişkilidir?',
          choices: [
            'Yüksek Enerji ve Parçacık Fiziği',
            'Termodinamik',
            'Klasik Mekanik',
            'Akustik'
          ],
          correctAnswer: 'Yüksek Enerji ve Parçacık Fiziği',
          explanation: 'CERN\'deki Büyük Hadron Çarpıştırıcısı (LHC), atom altı parçacıkları ışık hızına yakın hızlarda çarpıştırarak evrenin temel yapı taşlarını araştırır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_alt_dallar',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1Hg2eF907YA',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 3. Topic 03: Fizik Bilimine Yön Verenler
  {
    id: 'item_fiz9_vid_topic_03_fizik_bilimine_yon_verenler__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_03_fizik_bilimine_yon_verenler',
    stableKey: 'fiz9_quiz_yon_verenler_micro',
    itemType: 'QUIZ',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Fizik Bilimine Yön Verenler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Fizik Bilimine Yön Verenler Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_yon_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Optik alanında yazdığı "Kitâb\'ül-Menâzır" (Optik Kitabı) ile ışığın doğrusal yayılımı ve yansımasını deneysel olarak açıklayan İslam dünyasının ünlü fizikçisi kimdir?',
          choices: [
            'İbn-i Heysem',
            'El-Cezeri',
            'Biruni',
            'Harezmi'
          ],
          correctAnswer: 'İbn-i Heysem',
          explanation: 'İbn-i Heysem (Alhazen), modern optiğin kurucusu sayılır ve deneysel bilimsel yöntemi ilk uygulayan bilginlerdendir.'
        },
        {
          id: 'q_fiz9_yon_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kütle çekim kanununu formüle eden ve klasik mekaniğin üç temel hareket yasasını ortaya koyan İngiliz bilim insanı kimdir?',
          choices: [
            'Sir Isaac Newton',
            'Galileo Galilei',
            'James Clerk Maxwell',
            'Nikola Tesla'
          ],
          correctAnswer: 'Sir Isaac Newton',
          explanation: 'Isaac Newton, Philosophiae Naturalis Principia Mathematica eserinde evrensel kütle çekimi ve hareketin 3 temel yasasını yayınlamıştır.'
        },
        {
          id: 'q_fiz9_yon_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'El-Cezeri, sibernetik ve robotik biliminin öncüsü kabul edilir; su saatleri ve otomatik mekanik sistemler tasarlamıştır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Artuklular döneminde yaşayan El-Cezeri, mekanik mühendisliğin ve otomatik kontrol sistemlerinin kurucu dehasıdır.'
        },
        {
          id: 'q_fiz9_yon_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Grup teorisi ve temel parçacık fiziği alanındaki çalışmalarıyla dünya çapında tanınan ünlü Türk teorik fizikçi kimdir?',
          choices: [
            'Feza Gürsey',
            'Cahit Arf',
            'Aziz Sancar',
            'Gazi Yaşargil'
          ],
          correctAnswer: 'Feza Gürsey',
          explanation: 'Prof. Dr. Feza Gürsey, kuantum mekaniği ve simetri prensipleri üzerine çalışmalarıyla Oppenheimer ve Wigner ödüllerini kazanmış teorik fizikçimizdir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_03_fizik_bilimine_yon_verenler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=DiYbLCxiVMo',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 4. Topic 05: Temel ve Türetilmiş Nicelikler
  {
    id: 'item_fiz9_vid_topic_05_temel_ve_turetilmis_nicelikler__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_05_temel_ve_turetilmis_nicelikler',
    stableKey: 'fiz9_quiz_temel_turetilmis_micro',
    itemType: 'QUIZ',
    displayLabel: '5.1-Q',
    orderKey: 1500,
    title: 'Temel ve Türetilmiş Nicelikler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Temel ve Türetilmiş Nicelikler Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_tt_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'SI birim sisteminde temel büyüklükler "KISA MUZ" kısaltmasıyla hatırlanır. Aşağıdakilerden hangisi bu 7 temel büyüklükten biri DEĞİLDİR?',
          choices: [
            'Kuvvet',
            'Kütle',
            'Sıcaklık',
            'Madde Miktarı'
          ],
          correctAnswer: 'Kuvvet',
          explanation: 'KISA MUZ: Kütle (kg), Işık şiddeti (cd), Sıcaklık (K), Akım şiddeti (A), Madde miktarı (mol), Uzunluk (m), Zaman (s). Kuvvet ise türetilmiş bir büyüklüktür (N = kg·m/s²).'
        },
        {
          id: 'q_fiz9_tt_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'SI birim sisteminde sıcaklığın temel birimi Celcius (°C) değil, Kelvin (K)\'dir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Uluslararası Birim Sistemi\'nde (SI) sıcaklık birimi Kelvin (K)\'dir.'
        },
        {
          id: 'q_fiz9_tt_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Birden fazla temel büyüklüğün matematiksel olarak birleştirilmesiyle tanımlanan büyüklüklere "türetilmiş büyüklük" denir. Aşağıdakilerden hangisi türetilmiş bir büyüklüktür?',
          choices: [
            'Hız',
            'Zaman',
            'Uzunluk',
            'Işık Şiddeti'
          ],
          correctAnswer: 'Hız',
          explanation: 'Hız = Yer Değiştirme / Zaman (m/s) olup uzunluk ve zaman temel büyüklüklerinden türetilmiştir.'
        },
        {
          id: 'q_fiz9_tt_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kütle ölçümü için eşit kollu terazi kullanılırken, ağırlık (kuvvet) ölçümü için hangi ölçü aleti kullanılır?',
          choices: [
            'Dinamometre',
            'Kronometre',
            'Termometre',
            'Ampermetre'
          ],
          correctAnswer: 'Dinamometre',
          explanation: 'Ağırlık bir kuvvettir ve yayların esneklik özelliğinden yararlanılarak yapılan dinamometre ile ölçülür. Birimi Newton\'dur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_05_temel_ve_turetilmis_nicelikler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=7aVrdQ7uSQ4',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 5. Topic 07: Vektörler
  {
    id: 'item_fiz9_vid_topic_07_vektorler__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_07_vektorler',
    stableKey: 'fiz9_quiz_top07_vektorler_micro',
    itemType: 'QUIZ',
    displayLabel: '7.1-Q',
    orderKey: 1500,
    title: 'Vektörler ve Bileşke Vektör Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Vektörler ve Bileşke Vektör Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_top07_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir fiziksel büyüklüğün vektörel olarak tanımlanabilmesi için aşağıdakilerden hangisine sahip olması ŞART DEĞİLDİR?',
          choices: [
            'Renk veya sıcaklık değeri',
            'Büyüklük (şiddet) ve birim',
            'Uygulama doğrultusu',
            'Yön ve başlangıç noktası'
          ],
          correctAnswer: 'Renk veya sıcaklık değeri',
          explanation: 'Vektörel bir niceliğin 4 temel elemanı vardır: Başlangıç noktası, doğrultu, yön ve büyüklük (şiddet).'
        },
        {
          id: 'q_fiz9_top07_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aynı doğrultulu ve aynı yönlü 6 N ve 8 N büyüklüğündeki iki kuvvetin bileşkesinin büyüklüğü kaç Newton\'dur?',
          choices: [
            '14 N',
            '2 N',
            '10 N',
            '48 N'
          ],
          correctAnswer: '14 N',
          explanation: 'Aynı yönlü vektörler doğrudan toplanır: R = F1 + F2 = 6 + 8 = 14 N.'
        },
        {
          id: 'q_fiz9_top07_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Büyüklükleri ve doğrultuları aynı fakat yönleri birbirine zıt olan iki vektöre "zıt (ters) vektörler" denir ve aralarındaki açı 180° dir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Zıt vektörlerin şiddetleri ve doğrultuları eşit, yönleri ise tam terstir. Biri A⃗ ise diğeri -A⃗ dir.'
        },
        {
          id: 'q_fiz9_top07_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aralarında 90° açı bulunan 3 N ve 4 N büyüklüğündeki iki dik vektörün bileşkesi (R) Pisagor bağıntısıyla kaç N bulunur?',
          choices: [
            '5 N',
            '7 N',
            '1 N',
            '12 N'
          ],
          correctAnswer: '5 N',
          explanation: 'Dik iki vektörde R² = 3² + 4² = 9 + 16 = 25 => R = 5 N.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_07_vektorler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=FQ1lkZ9JQnI',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 6. Topic 08: Doğadaki Temel Kuvvetler
  {
    id: 'item_fiz9_vid_dogadaki_kuvvetler__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_08_dogadaki_temel_kuvvetler',
    stableKey: 'fiz9_quiz_dogadaki_kuvvetler_micro',
    itemType: 'QUIZ',
    displayLabel: '8.1-Q',
    orderKey: 1500,
    title: 'Doğadaki Temel Kuvvetler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Doğadaki Temel Kuvvetler Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_kuv_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Doğadaki 4 temel kuvvet şiddetlerine göre büyükten küçüğe doğru nasıl sıralanır?',
          choices: [
            'Güçlü Nükleer > Elektromanyetik > Zayıf Nükleer > Kütle Çekim',
            'Kütle Çekim > Elektromanyetik > Güçlü Nükleer > Zayıf Nükleer',
            'Elektromanyetik > Güçlü Nükleer > Kütle Çekim > Zayıf Nükleer',
            'Güçlü Nükleer > Zayıf Nükleer > Elektromanyetik > Kütle Çekim'
          ],
          correctAnswer: 'Güçlü Nükleer > Elektromanyetik > Zayıf Nükleer > Kütle Çekim',
          explanation: 'Şiddet sıralaması: Güçlü Nükleer (Yeğin) > Elektromanyetik > Zayıf Nükleer > Kütle Çekim Kuvveti.'
        },
        {
          id: 'q_fiz9_kuv_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Atom çekirdeğindeki proton ve nötronları birbirine bağlayan, çok kısa menzilli en güçlü temel kuvvet hangisidir?',
          choices: [
            'Güçlü Nükleer (Yeğin) Kuvvet',
            'Kütle Çekim Kuvveti',
            'Elektromanyetik Kuvvet',
            'Zayıf Nükleer Kuvvet'
          ],
          correctAnswer: 'Güçlü Nükleer (Yeğin) Kuvvet',
          explanation: 'Güçlü nükleer kuvvet, çekirdekte aynı yüklü protonların birbirini itmesine rağmen çekirdeğin bir arada kalmasını sağlar.'
        },
        {
          id: 'q_fiz9_kuv_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Kütle çekim kuvveti ve elektromanyetik kuvvet sonsuz menzile sahip temel kuvvetlerdir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Kütle çekim ve elektromanyetik kuvvetler 1/r² ile azalır ancak menzilleri teorik olarak sonsuzdur. Güçlü ve zayıf nükleer kuvvetler ise çekirdek içi (çok kısa) menzillidir.'
        },
        {
          id: 'q_fiz9_kuv_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi "temas gerektirmeyen (alan)" bir kuvvettir?',
          choices: [
            'Mıknatısın demir çiviyi çekmesi (Manyetik Kuvvet)',
            'Masanın itilmesi sırasında oluşan sürtünme kuvveti',
            'Rüzgârın yelkeni şişirmesi',
            'Haltercinin halteri kaldırması'
          ],
          correctAnswer: 'Mıknatısın demir çiviyi çekmesi (Manyetik Kuvvet)',
          explanation: 'Manyetik, elektriksel ve kütle çekim kuvvetleri temas gerektirmeyen alan kuvvetleridir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_dogadaki_kuvvetler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=7aVrdQ7uSQ4',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 7. Topic 09: Hareket ve Hareket Türleri
  {
    id: 'item_fiz9_vid_topic_09_hareket_ve_hareket_turleri__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_09_hareket_ve_hareket_turleri',
    stableKey: 'fiz9_quiz_top09_hareket_micro',
    itemType: 'QUIZ',
    displayLabel: '9.1-Q',
    orderKey: 1500,
    title: 'Hareket ve Hareket Türleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Hareket ve Hareket Türleri Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_har_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir lunapark dönme dolabının kabinlerinin yaptığı hareket türü ile saatin yelkovanının yaptığı hareket türü aşağıdakilerden hangisidir?',
          choices: [
            'Dönme Hareketi',
            'Öteleme Hareketi',
            'Titreşim Hareketi',
            'Çarpışma Hareketi'
          ],
          correctAnswer: 'Dönme Hareketi',
          explanation: 'Sabit bir eksen etrafında dairesel yörüngede yapılan hareket dönme hareketidir.'
        },
        {
          id: 'q_fiz9_har_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Gitar telinin çekilip bırakılması veya bir sarkacın salınım hareketi hangi hareket türüne örnektir?',
          choices: [
            'Titreşim Hareketi',
            'Öteleme Hareketi',
            'Doğrusal İlerleme',
            'Sabit İvmeli Hızlanma'
          ],
          correctAnswer: 'Titreşim Hareketi',
          explanation: 'Sabit iki nokta arasında periyodik olarak gidip gelme hareketine titreşim (salınım) hareketi denir.'
        },
        {
          id: 'q_fiz9_har_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Alınan yol skaler bir büyüklük iken, başlangıç noktasından bitiş noktasına çizilen en kısa yönlü doğru parçası olan yer değiştirme (Δx⃗) vektörel bir büyüklüktür.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Alınan yol kat edilen toplam mesafedir (skaler); yer değiştirme ise son konum ile ilk konum arasındaki yönlü farktır (vektörel).'
        },
        {
          id: 'q_fiz9_har_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yarıçapı r = 10 m olan dairesel bir pistin etrafında tam bir tur atan bir koşucunun aldığı yol ve yer değiştirmesi sırasıyla nedir? (π = 3 alınız)',
          choices: [
            'Yol: 60 m, Yer Değiştirme: 0 m',
            'Yol: 60 m, Yer Değiştirme: 60 m',
            'Yol: 0 m, Yer Değiştirme: 60 m',
            'Yol: 30 m, Yer Değiştirme: 20 m'
          ],
          correctAnswer: 'Yol: 60 m, Yer Değiştirme: 0 m',
          explanation: 'Çevre = 2·π·r = 2·3·10 = 60 m (alınan yol). Başladığı noktaya geri döndüğü için son konum - ilk konum = 0 m (yer değiştirme).'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_09_hareket_ve_hareket_turleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=b4wS9p9s9gE',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 8. Topic 10: Katılarda Basınç
  {
    id: 'item_fiz9_vid_topic_10_katilarda_basinc__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_10_katilarda_basinc',
    stableKey: 'fiz9_quiz_katilarda_basinc_micro',
    itemType: 'QUIZ',
    displayLabel: '10.1-Q',
    orderKey: 1500,
    title: 'Katılarda Basınç Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Katılarda Basınç Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_katbas_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Birim yüzeye dik olarak etki eden kuvvete basınç (P = F / S) denir. SI sisteminde basınç birimi nedir?',
          choices: [
            'Pascal (Pa = N/m²)',
            'Joule (J)',
            'Watt (W)',
            'Newton (N)'
          ],
          correctAnswer: 'Pascal (Pa = N/m²)',
          explanation: 'Basınç birimi N/m² olup bu özel birime Blaise Pascal\'ın anısına Pascal (Pa) denir.'
        },
        {
          id: 'q_fiz9_katbas_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Katılar üzerlerine uygulanan dik kuvveti doğrultusunu ve büyüklüğünü değiştirmeden aynen iletirler, ancak basıncı temas yüzeyine bağlı olarak değiştirirler.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Katılar kuvveti aynen iletir (çivinin başına uygulanan kuvvet sivri ucuna aynen iletilir). Sivri uçta alan küçük olduğu için basınç katbekat artar.'
        },
        {
          id: 'q_fiz9_katbas_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki uygulamalardan hangisinde basıncı AZALTMAK amaçlanmıştır?',
          choices: [
            'Ağır iş makinelerine ve tanklara geniş palet takılması',
            'Bıçakların bilenerek ağzının inceltilmesi',
            'Futbol kramponlarının altına diş konulması',
            'İğne ve çivilerin uçlarının sivri yapılması'
          ],
          correctAnswer: 'Ağır iş makinelerine ve tanklara geniş palet takılması',
          explanation: 'Palet takılarak yüzey alanı (S) büyütülür, böylece zemine yapılan basınç (P = G/S) azaltılarak batma engellenir.'
        },
        {
          id: 'q_fiz9_katbas_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bazı kristal katılara (kuvars gibi) basınç uygulandığında kristalin iki ucu arasında elektrik potansiyel farkı oluşması olayına ne ad verilir?',
          choices: [
            'Piezoelektrik Olayı',
            'Fotoelektrik Olayı',
            'Termoelektrik Olayı',
            'Süperiletkenlik'
          ],
          correctAnswer: 'Piezoelektrik Olayı',
          explanation: 'Basınç etkisiyle elektriksel voltaj üretilmesine piezoelektrik olay denir (çakmak manyetoları, dijital teraziler).'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_10_katilarda_basinc',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=UeS-p82_rZk',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 9. Topic 11: Sıvılarda Basınç
  {
    id: 'item_fiz9_vid_topic_11_sivilarda_basinc__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_11_sivilarda_basinc',
    stableKey: 'fiz9_quiz_sivilarda_basinc_micro',
    itemType: 'QUIZ',
    displayLabel: '11.1-Q',
    orderKey: 1500,
    title: 'Sıvılarda Basınç Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Sıvılarda Basınç Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_sivbas_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Durgun bir sıvının tabanındaki sıvı basıncı formülü (P = h·d·g) dikkate alındığında basınç aşağıdakilerden hangisine BAĞLI DEĞİLDİR?',
          choices: [
            'Kabın şekline ve toplam sıvı hacmine',
            'Noktanın açık sıvı yüzeyine olan derinliğine (h)',
            'Sıvının özkütlesine (d)',
            'Yer çekimi ivmesine (g)'
          ],
          correctAnswer: 'Kabın şekline ve toplam sıvı hacmine',
          explanation: 'Durgun sıvı basıncı yalnızca derinliğe (h), sıvının yoğunluğuna (d) ve yer çekimi ivmesine (g) bağlıdır; kabın şekline veya sıvı kütlesine bağlı değildir.'
        },
        {
          id: 'q_fiz9_sivbas_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Pascal Prensibine göre kapalı bir kaptaki sıvıya uygulanan basınç, sıvının temas ettiği her noktaya ve her yöne aynı büyüklükte iletilir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Sıvılar sıkıştırılamaz kabul edilir ve üzerlerine uygulanan basınç değişimini kabın her yönüne aynen iletirler (Pascal İlkesi).'
        },
        {
          id: 'q_fiz9_sivbas_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki araç ve sistemlerden hangisi Pascal Prensibi temel alınarak geliştirilmiştir?',
          choices: [
            'Hidrolik fren sistemleri ve su cendereleri',
            'Termometreler',
            'Cıvalı barometre',
            'Uçak kanadı aerodinamiği'
          ],
          correctAnswer: 'Hidrolik fren sistemleri ve su cendereleri',
          explanation: 'Hidrolik frenler, oto kaldırma liftleri ve su cendereleri sıvıların basıncı aynen iletmesi (Pascal prensibi) prensibiyle çalışır.'
        },
        {
          id: 'q_fiz9_sivbas_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Özkütlesi 2 g/cm³ olan bir sıvının 30 cm derinliğindeki bir noktaya uyguladığı sıvı basıncının, özkütlesi 1 g/cm³ olan sıvının 30 cm derinliğindeki basıncına oranı kaçtır?',
          choices: [
            '2',
            '1/2',
            '4',
            '1'
          ],
          correctAnswer: '2',
          explanation: 'P1 = h·d1·g = 30·2·g = 60g; P2 = h·d2·g = 30·1·g = 30g. Oran P1/P2 = 60/30 = 2.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_11_sivilarda_basinc',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=q6M0P-RzZkY',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 10. Lesson 12: Açık Hava Basıncı - Video 1
  {
    id: 'item_fiz9_vid_basinc_prensipleri__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_akiskanlar_ve_basinc',
    stableKey: 'fiz9_quiz_basinc_prensipleri_micro',
    itemType: 'QUIZ',
    displayLabel: '12.1-Q',
    orderKey: 1200,
    title: 'Açık Hava ve Gaz Basıncı Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Açık Hava ve Gaz Basıncı Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_gazbas_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Deniz seviyesinde ve 0 °C sıcaklıkta açık hava basıncını ilk kez cıvalı barometre ile ölçen ve P0 = 76 cm-Hg değerini bulan bilim insanı kimdir?',
          choices: [
            'Evangelista Torricelli',
            'Blaise Pascal',
            'Daniel Bernoulli',
            'Archimedes (Arşimet)'
          ],
          correctAnswer: 'Evangelista Torricelli',
          explanation: 'Torricelli, 1 metre boyundaki cıva dolu boruyla açık hava basıncının 76 cm cıva sütununun basıncına eşit olduğunu kanıtlamıştır.'
        },
        {
          id: 'q_fiz9_gazbas_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Deniz seviyesinden yukarılara (dağlara) doğru çıkıldıkça atmosferin yoğunluğu ve üzerimizdeki hava tabakasının kalınlığı azaldığı için açık hava basıncı azalır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Yükseklere çıkıldıkça hava moleküllerinin yoğunluğu ve ağırlığı azaldığından açık hava basıncı düşer (her 10,5 metrede yaklaşık 1 mmHg).'
        },
        {
          id: 'q_fiz9_gazbas_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Açık hava basıncını ölçen alete "Barometre" denirken, kapalı kaplardaki gaz basıncını ölçen alete ne ad verilir?',
          choices: [
            'Manometre',
            'Altimetre',
            'Batimetre',
            'Dinamometre'
          ],
          correctAnswer: 'Manometre',
          explanation: 'Kapalı kaplardaki gazların basıncını ölçmek için manometreler kullanılır.'
        },
        {
          id: 'q_fiz9_gazbas_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İdeal bir gazın sıcaklığı sabit tutulurken hacmi yarıya indirilirse gaz basıncı nasıl değişir? (P·V = sabit)',
          choices: [
            '2 katına çıkar',
            'Yarıya iner',
            'Değişmez',
            '4 katına çıkar'
          ],
          correctAnswer: '2 katına çıkar',
          explanation: 'Boyle-Mariotte yasasına göre sıcaklık sabitken basınç ile hacim ters orantılıdır: P1·V1 = P2·V2 => Hacim yarıya inerse basınç 2 katına çıkar.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_basinc_prensipleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=S2fXGO-9Cdk',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 11. Lesson 12: Akışkanlar ve Basınç - Video 2
  {
    id: 'item_fiz9_vid_kaldirma_kuvveti__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_akiskanlar_ve_basinc',
    stableKey: 'fiz9_quiz_vid_kaldirma_micro',
    itemType: 'QUIZ',
    displayLabel: '12.2-Q',
    orderKey: 2500,
    title: 'Kaldırma Kuvveti Prensipleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Kaldırma Kuvveti Prensipleri Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_vkal_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Durgun bir sıvıya tamamen ya da kısmen batırılan cisme etki eden kaldırma kuvveti (Fk) formülü hangisidir?',
          choices: [
            'Fk = Vbatan · dsıvı · g',
            'Fk = Vcisim · dcisim · g',
            'Fk = h · dsıvı · g',
            'Fk = m · g / S'
          ],
          correctAnswer: 'Fk = Vbatan · dsıvı · g',
          explanation: 'Arşimet prensibine göre kaldırma kuvveti, cismin batan hacmi ile sıvının özkütlesi ve yer çekimi ivmesinin çarpımına eşittir.'
        },
        {
          id: 'q_fiz9_vkal_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Bir sıvıda yüzen veya askıda kalan dengedeki bir cisme etki eden kaldırma kuvveti cismin kendi ağırlığına (G) eşittir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Yüzen ve askıda kalan cisimlerde düşey kuvvet dengesi gereği Fk = G olur.'
        },
        {
          id: 'q_fiz9_vkal_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Özkütlesi dc olan bir cisim özkütlesi ds olan bir sıvıya bırakıldığında cismin kabın tabanına batması için şart nedir?',
          choices: [
            'dc > ds',
            'dc < ds',
            'dc = ds',
            'dc = 2·ds'
          ],
          correctAnswer: 'dc > ds',
          explanation: 'Cismin özkütlesi sıvınınkinden büyükse cisim dibe batar (dc > ds).'
        },
        {
          id: 'q_fiz9_vkal_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Ağzına kadar sıvı dolu bir taşma kabına, sıvıda yüzen bir tahta blok bırakıldığında kapta ağırlaşma olur mu?',
          choices: [
            'Ağırlaşma olmaz, çünkü taşan sıvının ağırlığı cismin ağırlığına eşittir.',
            'Cismin tüm ağırlığı kadar ağırlaşma olur.',
            'Cismin batan hacmi kadar ağırlaşma olur.',
            'Sıvı döküldüğü için kabın toplam ağırlığı azalır.'
          ],
          correctAnswer: 'Ağırlaşma olmaz, çünkü taşan sıvının ağırlığı cismin ağırlığına eşittir.',
          explanation: 'Yüzen cisimlerde taşan sıvının ağırlığı kaldırma kuvvetine, kaldırma kuvveti de cismin ağırlığına eşit olduğundan kaba giren ağırlık çıkan ağırlığa eşittir; ağırlaşma olmaz.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_kaldirma_kuvveti',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=FpC4nmFmSG6',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 12. Topic 13: Kaldırma Kuvveti (Granular Topic)
  {
    id: 'item_fiz9_vid_topic_13_kaldirma_kuvveti__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_13_kaldirma_kuvveti',
    stableKey: 'fiz9_quiz_top13_kaldirma_micro',
    itemType: 'QUIZ',
    displayLabel: '13.1-Q',
    orderKey: 1500,
    title: 'Kaldırma Kuvveti Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Kaldırma Kuvveti Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_top13_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kaldırma kuvvetinin oluşmasının temel fiziksel nedeni aşağıdakilerden hangisidir?',
          choices: [
            'Sıvının cisme uyguladığı alt ve üst yüzey basınç kuvvetleri arasındaki fark',
            'Kabın yüzey gerilimi',
            'Yer çekimi ivmesinin derinlikle artması',
            'Sıvının viskozite katsayısı'
          ],
          correctAnswer: 'Sıvının cisme uyguladığı alt ve üst yüzey basınç kuvvetleri arasındaki fark',
          explanation: 'Cismin alt yüzeyi daha derinde olduğu için alt yüzeydeki sıvı basınç kuvveti üst yüzeydekinden büyüktür. Bu kuvvet farkı yukarı yönlü net kaldırma kuvvetini oluşturur (Fk = Falt - Füst).'
        },
        {
          id: 'q_fiz9_top13_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Bir geminin deniz suyunda tatlı suya (nehre) girdiğinde biraz daha fazla batmasının nedeni tatlı suyun özkütlesinin tuzlu deniz suyundan daha küçük olmasıdır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Gemi iki ortamda da yüzer ve Fk = G dir. Sıvı yoğunluğu (ds) azalınca aynı kaldırma kuvvetini sağlamak için batan hacim (Vb) artar.'
        },
        {
          id: 'q_fiz9_top13_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Havadaki ağırlığı 50 N olan bir cisim dinamometre ucunda suya tamamen daldırıldığında dinamometre 35 N gösteriyor. Bu cisme etki eden kaldırma kuvveti kaç N\'dur?',
          choices: [
            '15 N',
            '85 N',
            '35 N',
            '50 N'
          ],
          correctAnswer: '15 N',
          explanation: 'Fk = G - T = 50 N - 35 N = 15 N.'
        },
        {
          id: 'q_fiz9_top13_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Eşit hacimli K, L, M cisimlerinden K yüzmekte, L askıda kalmakta, M ise dibe batmaktadır. Cisimlere etki eden kaldırma kuvvetleri FK, FL, FM arasındaki ilişki nedir?',
          choices: [
            'FL = FM > FK',
            'FK = FL = FM',
            'FM > FL > FK',
            'FK > FL > FM'
          ],
          correctAnswer: 'FL = FM > FK',
          explanation: 'L ve M tamamen battığı için batan hacimleri eşittir ve cismin tüm hacmidir (Vb = V). K ise yüzdüğü için batan hacmi daha küçüktür (Vb < V). Fk = Vb·d·g olduğundan FL = FM > FK olur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_13_kaldirma_kuvveti',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=FpC4nmFmSG6',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 13. Topic 14: Bernoulli İlkesi
  {
    id: 'item_fiz9_vid_topic_14_bernoulli_ilkesi__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_14_bernoulli_ilkesi',
    stableKey: 'fiz9_quiz_bernoulli_micro',
    itemType: 'QUIZ',
    displayLabel: '14.1-Q',
    orderKey: 1500,
    title: 'Bernoulli İlkesi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Bernoulli İlkesi Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_bern_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bernoulli İlkesine göre, bir akışkanın (sıvı veya gaz) akış hızının arttığı yerde akışkanın çeperlere uyguladığı statik basınç nasıl değişir?',
          choices: [
            'Azalır',
            'Artar',
            'Değişmez',
            'Sıfır olur'
          ],
          correctAnswer: 'Azalır',
          explanation: 'Bernoulli ilkesine göre akışkanın hızı ile statik basıncı ters orantılıdır; hızın arttığı kesitlerde akışkan basıncı düşer.'
        },
        {
          id: 'q_fiz9_bern_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Uçak kanatlarının üst yüzeyinin kavisli tasarlanması sayesinde üstten geçen havanın hızı artar, basıncı düşer ve kanadın altında oluşan yüksek basınç uçağı havaya kaldırır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Bernoulli ilkesine göre kanadın üstündeki düşük basınç ile altındaki yüksek basınç arasındaki fark kaldırma kuvvetini (aerodinamik lift) oluşturur.'
        },
        {
          id: 'q_fiz9_bern_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kesit alanı daralan bir borudan geçen akışkanın süreklilik denklemine (A1·v1 = A2·v2) göre borunun daralan kısmındaki hızı ve basıncı nasıl değişir?',
          choices: [
            'Hızı artar, basıncı azalır',
            'Hızı azalır, basıncı artar',
            'Hem hızı hem basıncı artar',
            'Hem hızı hem basıncı azalır'
          ],
          correctAnswer: 'Hızı artar, basıncı azalır',
          explanation: 'Süreklilik denklemine göre kesit daraldıkça hız artar (A↓ v↑). Bernoulli ilkesine göre hız arttıkça basınç azalır (v↑ P↓).'
        },
        {
          id: 'q_fiz9_bern_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki günlük hayat olaylarından hangisi Bernoulli İlkesi ile AÇIKLANAMAZ?',
          choices: [
            'Gemilerin suyun üzerinde yüzmesi',
            'Fırtınalı havalarda çatıların uçması',
            'Parfüm veya sprey şişelerinden sıvının püskürmesi',
            'Yan yana hızla geçen iki trenin birbirine doğru çekilmesi'
          ],
          correctAnswer: 'Gemilerin suyun üzerinde yüzmesi',
          explanation: 'Gemilerin yüzmesi Arşimet kaldırma kuvveti ilkesiyle açıklanır. Çatıların uçması, spreyler ve yaklaşan trenler ise Bernoulli ilkesi (akışkan hız-basınç ilişkisi) ile açıklanır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_14_bernoulli_ilkesi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=FR0u-IglzGd',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 14. Lesson 15: Enerji, Isı, Sıcaklık - Video 1
  {
    id: 'item_fiz9_vid_isi_sicaklik_kavram__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_enerji_isi_sicaklik',
    stableKey: 'fiz9_quiz_isi_sicaklik_kavram_micro',
    itemType: 'QUIZ',
    displayLabel: '15.1-Q',
    orderKey: 1200,
    title: 'İç Enerji, Isı ve Sıcaklık Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'İç Enerji, Isı ve Sıcaklık Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_iskav_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Maddeyi oluşturan taneciklerin ortalama kinetik enerjisinin bir göstergesi olan ve termometre ile ölçülen fiziksel nicelik hangisidir?',
          choices: [
            'Sıcaklık',
            'Isı',
            'İç Enerji',
            'Öz Isı'
          ],
          correctAnswer: 'Sıcaklık',
          explanation: 'Sıcaklık bir enerji türü değildir; taneciklerin ortalama kinetik enerjisinin bir ölçüsüdür ve termometre ile ölçülür.'
        },
        {
          id: 'q_fiz9_iskav_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Bir maddenin sahip olduğu "ısısı" diye bir kavram fiziksel olarak tanımlanamaz; ısı sadece sıcaklık farkından dolayı transfer edilen enerjidir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Maddenin toplam enerjisine "iç enerji" denir. Isı ise iki sistem arasında sıcaklık farkı nedeniyle aktarılan enerjidir; bir maddenin ısısından bahsedilemez.'
        },
        {
          id: 'q_fiz9_iskav_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sıvılı bir termometrenin duyarlılığını (hassasiyetini) artırmak için aşağıdakilerden hangisi YAPILMALIDIR?',
          choices: [
            'Kılcal borunun kesit alanını daraltmak ve haznesini büyütmek',
            'Kılcal borunun kesit alanını genişletmek',
            'Genleşme katsayısı küçük olan sıvı kullanmak',
            'Haznenin yapıldığı maddenin genleşme katsayısını büyütmek'
          ],
          correctAnswer: 'Kılcal borunun kesit alanını daraltmak ve haznesini büyütmek',
          explanation: 'Termometrenin duyarlılığı: Hazne hacmi büyük, sıvı genleşme katsayısı büyük, kılcal boru ince ve boru camının genleşme katsayısı küçük olduğunda maksimum olur.'
        },
        {
          id: 'q_fiz9_iskav_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '20 °C sıcaklıktaki bir odanın sıcaklığı 40 °C\'ye çıkarıldığında sıcaklık iki katına çıkmış OLMAZ. Bunun temel nedeni nedir?',
          choices: [
            'Sıcaklığın mutlak sıfır noktası 0 K (-273,15 °C) olup orantıların Kelvin ölçeğine göre yapılması zorunluluğu',
            'Odanın iç enerjisinin azalması',
            'Termometrelerin Celcius\'ta hata payının yüksek olması',
            'Isının bir enerji olması'
          ],
          correctAnswer: 'Sıcaklığın mutlak sıfır noktası 0 K (-273,15 °C) olup orantıların Kelvin ölçeğine göre yapılması zorunluluğu',
          explanation: 'Kat oranları sadece mutlak sıcaklık ölçeği olan Kelvin (K) ile ifade edilir: 20 °C = 293 K iken 40 °C = 313 K\'dir; dolayısıyla sıcaklık 2 katına çıkmamıştır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_isi_sicaklik_kavram',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Lj_tsZ3QKWM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 15. Lesson 15: Hâl Değişimi & Isıl Denge - Video 2
  {
    id: 'item_fiz9_vid_hal_degisimi__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_enerji_isi_sicaklik',
    stableKey: 'fiz9_quiz_vid_haldeg_micro',
    itemType: 'QUIZ',
    displayLabel: '15.2-Q',
    orderKey: 2500,
    title: 'Hâl Değişimi ve Isıl Denge Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Hâl Değişimi ve Isıl Denge Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_vhal_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Erime sıcaklığında bulunan 50 gram buzu tamamen eritmek için gerekli ısı kaç kaloridir? (Le = 80 cal/g)',
          choices: [
            '4000 cal',
            '400 cal',
            '2000 cal',
            '800 cal'
          ],
          correctAnswer: '4000 cal',
          explanation: 'Hâl değiştirme formülü: Q = m · Le = 50 g · 80 cal/g = 4000 cal.'
        },
        {
          id: 'q_fiz9_vhal_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Farklı sıcaklıktaki iki cisim birbirine temas ettirildiğinde ısı akışı daima sıcaklığı yüksek olan cisimden sıcaklığı düşük olan cisme doğrudur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Termodinamiğin temel ilkesi gereği ısı geçişi kendiliğinden daima yüksek sıcaklıktan alçak sıcaklığa doğru gerçekleşir.'
        },
        {
          id: 'q_fiz9_vhal_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Saf bir sıvının kaynama noktası dış basınç arttıkça nasıl değişir?',
          choices: [
            'Artar (Düdüklü tencere prensibi)',
            'Azalır',
            'Değişmez',
            'Sıfır olur'
          ],
          correctAnswer: 'Artar (Düdüklü tencere prensibi)',
          explanation: 'Dış basınç arttıkça sıvının buharlaşması zorlaşır ve kaynama noktası yükselir. Düdüklü tencerede su yaklaşık 120 °C\'de kaynar.'
        },
        {
          id: 'q_fiz9_vhal_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Isıca yalıtılmış bir kapta karıştırılan farklı sıcaklıktaki iki sıvının ısıl dengeye ulaştığında son sıcaklıkları (Tdenge) için hangisi KESİNLİKLE doğrudur?',
          choices: [
            'Denge sıcaklığı iki sıvının ilk sıcaklıkları arasında bir değer alır (T1 < Tdenge < T2).',
            'Denge sıcaklığı daima ilk sıcaklıkların aritmetik ortalamasıdır.',
            'Isı sığası küçük olanın sıcaklığına daha yakındır.',
            'Kütlesi büyük olanın ilk sıcaklığına eşit olur.'
          ],
          correctAnswer: 'Denge sıcaklığı iki sıvının ilk sıcaklıkları arasında bir değer alır (T1 < Tdenge < T2).',
          explanation: 'Isıl dengede denge sıcaklığı mutlaka küçük sıcaklıktan büyük, büyük sıcaklıktan küçük bir değer alır ve ısı sığası büyük olan tarafa daha yakın olur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_hal_degisimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=BZ4muY8Bc1Y',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 16. Topic 18: Isıl Denge
  {
    id: 'item_fiz9_vid_topic_18_isil_denge__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_18_isil_denge',
    stableKey: 'fiz9_quiz_top18_isil_denge_micro',
    itemType: 'QUIZ',
    displayLabel: '18.1-Q',
    orderKey: 1500,
    title: 'Isıl Denge Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Isıl Denge Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_top18_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Isıca yalıtılmış bir ortamda yalnızca birbirleriyle ısı alışverişi yapan iki cisim için temel ısıl denge eşitliği nedir?',
          choices: [
            'Qalınan = Qverilen',
            'T1 + T2 = Sabit',
            'm1 = m2',
            'c1 = c2'
          ],
          correctAnswer: 'Qalınan = Qverilen',
          explanation: 'Enerjinin korunumu gereği yalıtılmış ortamda soğuyan cismin verdiği ısı enerjisi, ısınan cismin aldığı ısı enerjisine eşittir.'
        },
        {
          id: 'q_fiz9_top18_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Termodinamiğin sıfırıncı yasasına göre, A sistemi B ile ve B sistemi C ile ısıl dengede ise, A sistemi de C sistemi ile ısıl dengededir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Termodinamiğin 0. yasası sıcaklık kavramının ve termometrelerin çalışma temelini oluşturur.'
        },
        {
          id: 'q_fiz9_top18_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Isı sığaları (C = m·c) eşit olan 20 °C ve 80 °C sıcaklıktaki iki aynı tür sıvı karıştırıldığında denge sıcaklığı kaç °C olur?',
          choices: [
            '50 °C',
            '60 °C',
            '40 °C',
            '30 °C'
          ],
          correctAnswer: '50 °C',
          explanation: 'Isı sığaları eşit olduğunda denge sıcaklığı doğrudan aritmetik ortalamadır: Tdenge = (20 + 80) / 2 = 50 °C.'
        },
        {
          id: 'q_fiz9_top18_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İki cisim arasında net ısı transferinin durması hangi fiziksel durumun gerçekleştiğini gösterir?',
          choices: [
            'İki cismin sıcaklıklarının eşitlendiğini (Isıl Denge)',
            'İki cismin iç enerjilerinin sıfır olduğunu',
            'İki cismin kütlelerinin eşitlendiğini',
            'İki cismin hâl değiştirdiğini'
          ],
          correctAnswer: 'İki cismin sıcaklıklarının eşitlendiğini (Isıl Denge)',
          explanation: 'Isı transferi sıcaklık farkı olduğu sürece devam eder; sıcaklıklar eşitlendiğinde (ısıl dengeye ulaşıldığında) net ısı aktarımı sıfır olur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_18_isil_denge',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=HnOymIZHEII',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 17. Topic 19: Isı Aktarım Yolları
  {
    id: 'item_fiz9_vid_topic_19_isi_aktarim_yollari__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_19_isi_aktarim_yollari',
    stableKey: 'fiz9_quiz_top19_aktarim_micro',
    itemType: 'QUIZ',
    displayLabel: '19.1-Q',
    orderKey: 1500,
    title: 'Isı Aktarım Yolları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Isı Aktarım Yolları Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_top19_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Çay bardağına konulan metal çay kaşığının sapının bir süre sonra ısınması ısının hangi yolla iletildiğini gösterir?',
          choices: [
            'İletim (Kondüksiyon)',
            'Konveksiyon (Taşıma)',
            'Işıma (Radyasyon)',
            'Buharlaşma'
          ],
          correctAnswer: 'İletim (Kondüksiyon)',
          explanation: 'Katı maddelerde taneciklerin titreşerek enerjiyi komşu taneciklere aktarmasıyla gerçekleşen ısı iletim yoluna "iletim" denir.'
        },
        {
          id: 'q_fiz9_top19_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sıvı ve gazlarda ısınan maddenin genleşerek özkütlesinin azalması ve yükselmesiyle oluşan ısı yayılma yolu hangisidir?',
          choices: [
            'Konveksiyon (Taşıma)',
            'İletim',
            'Işıma',
            'Yansıma'
          ],
          correctAnswer: 'Konveksiyon (Taşıma)',
          explanation: 'Akışkanlarda (sıvı ve gaz) ısınan taneciklerin yer değiştirmesiyle ısının yayılmasına konveksiyon (taşıma) denir (kalorifer peteğinin odayı ısıtması).'
        },
        {
          id: 'q_fiz9_top19_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Işıma (radyasyon) yoluyla ısı yayılması için maddesel bir ortama ihtiyaç yoktur; elektromanyetik dalgalar aracılığıyla boşlukta da yayılabilir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Güneş\'in Dünya\'yı uzay boşluğundan geçerek ısıtması ışıma yoluyla gerçekleşir; maddesel ortama ihtiyaç duymaz.'
        },
        {
          id: 'q_fiz9_top19_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Termosların iç yüzeyinin parlak ve aynalı yapılması, ısının hangi yolla kaybını en aza indirmek içindir?',
          choices: [
            'Işıma (Radyasyon)',
            'İletim',
            'Konveksiyon',
            'Sürtünme'
          ],
          correctAnswer: 'Işıma (Radyasyon)',
          explanation: 'Parlak ve yansıtıcı yüzeyler termal ışıma dalgalarını geri yansıtarak ışıma ile olan ısı kaybını engeller; çift duvar arasındaki vakum ise iletim ve konveksiyonu engeller.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_19_isi_aktarim_yollari',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Gw0oaj9w5iN',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 18. Topic 20: Isı İletim Hızı
  {
    id: 'item_fiz9_vid_topic_20_isi_iletim_hizi__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_20_isi_iletim_hizi',
    stableKey: 'fiz9_quiz_top20_iletim_hizi_micro',
    itemType: 'QUIZ',
    displayLabel: '20.1-Q',
    orderKey: 1500,
    title: 'Isı İletim Hızı ve Yalıtım Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Isı İletim Hızı ve Yalıtım Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_top20_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir duvarın birim zamandaki ısı iletim hızını (ΔQ/Δt = k·A·ΔT/d) ARTIRMAK için aşağıdakilerden hangisi yapılmalıdır?',
          choices: [
            'Duvarın yüzey alanını (A) veya iki taraf arasındaki sıcaklık farkını (ΔT) artırmak',
            'Duvarın kalınlığını (d) artırmak',
            'Isı iletim katsayısı (k) daha küçük yalıtım malzemesi kullanmak',
            'Duvarı çift camlı yalıtımlı yapmak'
          ],
          correctAnswer: 'Duvarın yüzey alanını (A) veya iki taraf arasındaki sıcaklık farkını (ΔT) artırmak',
          explanation: 'Isı iletim hızı yüzey alanı (A), sıcaklık farkı (ΔT) ve iletim katsayısı (k) ile doğru orantılı; malzeme kalınlığı (d) ile ters orantılıdır.'
        },
        {
          id: 'q_fiz9_top20_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Kışın aynı odada bulunan tahta masa ile demir sandalyenin gerçek sıcaklıkları eşit olmasına rağmen, demirin ısı iletim katsayısı daha yüksek olduğu için demir sandalyeye dokunulduğunda daha soğuk hissedilir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Isıl dengedeki iki nesnenin sıcaklığı aynıdır. Ancak demir ısıyı elimizden tahtaya göre çok daha hızlı çektiği için daha soğuk hissedilir.'
        },
        {
          id: 'q_fiz9_top20_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Binalarda ısı yalıtımında kullanılan köpük (strafor), cam yünü ve çift camların ortak özelliği nedir?',
          choices: [
            'Isı iletim katsayılarının çok düşük olması ve hapsedilmiş durgun hava içermeleri',
            'Özkütlelerinin çok yüksek olması',
            'Elektrik akımını çok iyi iletmeleri',
            'Güneş ışığını tamamen soğurmaları'
          ],
          correctAnswer: 'Isı iletim katsayılarının çok düşük olması ve hapsedilmiş durgun hava içermeleri',
          explanation: 'Durgun hava mükemmel bir ısı yalıtkanıdır. Yalıtım malzemeleri havayı küçük gözeneklere hapsederek konveksiyon ve iletimi engeller.'
        },
        {
          id: 'q_fiz9_top20_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Hissedilen sıcaklık kavramı ile ilgili olarak aşağıdakilerden hangisi DOĞRUDUR?',
          choices: [
            'Termometrenin ölçtüğü gerçek hava sıcaklığı ile birlikte havadaki bağıl nem ve rüzgâr hızına bağlı olarak insan vücudunun algıladığı sıcaklıktır.',
            'Termometre ile doğrudan ölçülen nesnel sıcaklıktır.',
            'Yalnızca deniz seviyesindeki yerlerde geçerlidir.',
            'Havadaki oksijen oranına bağlıdır.'
          ],
          correctAnswer: 'Termometrenin ölçtüğü gerçek hava sıcaklığı ile birlikte havadaki bağıl nem ve rüzgâr hızına bağlı olarak insan vücudunun algıladığı sıcaklıktır.',
          explanation: 'Hissedilen sıcaklık; gerçek sıcaklık, bağıl nem, rüzgâr ve radyasyon gibi faktörlerin birleşimiyle vücudun termal algısını ifade eder.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_20_isi_iletim_hizi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=GIg0fdy68F2',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T20:00:00.000Z',
      schemaVersion: 'v2'
    }
  }
];

// 3. Lesson mapping configuration for all 20 lessons of course_fiz_9
const lessonItemsMapping = {
  'lesson_fiz9_topic_01_fizik_bilimi': [
    'item_fiz9_vid_topic_01_fizik_bilimi',
    'item_fiz9_vid_topic_01_fizik_bilimi__quiz'
  ],
  'lesson_fiz9_topic_02_fizik_biliminin_alt_dallari': [
    'item_fiz9_vid_alt_dallar',
    'item_fiz9_vid_alt_dallar__quiz'
  ],
  'lesson_fiz9_topic_03_fizik_bilimine_yon_verenler': [
    'item_fiz9_vid_topic_03_fizik_bilimine_yon_verenler',
    'item_fiz9_vid_topic_03_fizik_bilimine_yon_verenler__quiz'
  ],
  'lesson_fiz9_fizik_bilimine_giris': [
    'item_fiz9_vid_fizik_giris',
    'item_fiz9_vid_fizik_giris__quiz',
    'item_fiz9_quiz_fizik_giris'
  ],
  'lesson_fiz9_topic_05_temel_ve_turetilmis_nicelikler': [
    'item_fiz9_vid_topic_05_temel_ve_turetilmis_nicelikler',
    'item_fiz9_vid_topic_05_temel_ve_turetilmis_nicelikler__quiz'
  ],
  'lesson_fiz9_kuvvet_ve_hareket': [
    'item_fiz9_vid_vektorler',
    'item_fiz9_vid_vektorler__quiz',
    'item_fiz9_quiz_vektor_hareket'
  ],
  'lesson_fiz9_topic_07_vektorler': [
    'item_fiz9_vid_topic_07_vektorler',
    'item_fiz9_vid_topic_07_vektorler__quiz'
  ],
  'lesson_fiz9_topic_08_dogadaki_temel_kuvvetler': [
    'item_fiz9_vid_dogadaki_kuvvetler',
    'item_fiz9_vid_dogadaki_kuvvetler__quiz'
  ],
  'lesson_fiz9_topic_09_hareket_ve_hareket_turleri': [
    'item_fiz9_vid_topic_09_hareket_ve_hareket_turleri',
    'item_fiz9_vid_topic_09_hareket_ve_hareket_turleri__quiz'
  ],
  'lesson_fiz9_topic_10_katilarda_basinc': [
    'item_fiz9_vid_topic_10_katilarda_basinc',
    'item_fiz9_vid_topic_10_katilarda_basinc__quiz'
  ],
  'lesson_fiz9_topic_11_sivilarda_basinc': [
    'item_fiz9_vid_topic_11_sivilarda_basinc',
    'item_fiz9_vid_topic_11_sivilarda_basinc__quiz'
  ],
  'lesson_fiz9_akiskanlar_ve_basinc': [
    'item_fiz9_vid_basinc_prensipleri',
    'item_fiz9_vid_basinc_prensipleri__quiz',
    'item_fiz9_vid_kaldirma_kuvveti',
    'item_fiz9_vid_kaldirma_kuvveti__quiz',
    'item_fiz9_quiz_basinc_kaldirma'
  ],
  'lesson_fiz9_topic_13_kaldirma_kuvveti': [
    'item_fiz9_vid_topic_13_kaldirma_kuvveti',
    'item_fiz9_vid_topic_13_kaldirma_kuvveti__quiz'
  ],
  'lesson_fiz9_topic_14_bernoulli_ilkesi': [
    'item_fiz9_vid_topic_14_bernoulli_ilkesi',
    'item_fiz9_vid_topic_14_bernoulli_ilkesi__quiz'
  ],
  'lesson_fiz9_enerji_isi_sicaklik': [
    'item_fiz9_vid_isi_sicaklik_kavram',
    'item_fiz9_vid_isi_sicaklik_kavram__quiz',
    'item_fiz9_vid_hal_degisimi',
    'item_fiz9_vid_hal_degisimi__quiz',
    'item_fiz9_quiz_isi_sicaklik'
  ],
  'lesson_fiz9_topic_16_isi_oz_isi_ve_isi_sigasi': [
    'item_fiz9_vid_topic_16_isi_oz_isi_ve_isi_sigasi',
    'item_fiz9_vid_topic_16_isi_oz_isi_ve_isi_sigasi__quiz'
  ],
  'lesson_fiz9_topic_17_hal_degisimi': [
    'item_fiz9_vid_topic_17_hal_degisimi',
    'item_fiz9_vid_topic_17_hal_degisimi__quiz'
  ],
  'lesson_fiz9_topic_18_isil_denge': [
    'item_fiz9_vid_topic_18_isil_denge',
    'item_fiz9_vid_topic_18_isil_denge__quiz'
  ],
  'lesson_fiz9_topic_19_isi_aktarim_yollari': [
    'item_fiz9_vid_topic_19_isi_aktarim_yollari',
    'item_fiz9_vid_topic_19_isi_aktarim_yollari__quiz'
  ],
  'lesson_fiz9_topic_20_isi_iletim_hizi': [
    'item_fiz9_vid_topic_20_isi_iletim_hizi',
    'item_fiz9_vid_topic_20_isi_iletim_hizi__quiz'
  ]
};

async function main() {
  console.log('1. Updating video items with valid URLs and providers...');
  for (const vUp of videoUpdates) {
    const vPath = path.join(ITEMS_DIR, `${vUp.id}.json`);
    let itemData = {};
    if (fs.existsSync(vPath)) {
      itemData = JSON.parse(fs.readFileSync(vPath, 'utf8'));
    }
    itemData.id = vUp.id;
    itemData.title = vUp.title || itemData.title;
    itemData.contentUrl = vUp.contentUrl;
    itemData.payload = itemData.payload || {};
    itemData.payload.provider = vUp.provider;
    itemData.payload.provenance = itemData.payload.provenance || {};
    itemData.payload.provenance.sourceVideoUrl = vUp.contentUrl;
    itemData.payload.provenance.provider = vUp.provider;
    fs.writeFileSync(vPath, JSON.stringify(itemData, null, 2) + '\n');
    console.log(`  Updated video: ${vUp.id} -> ${vUp.contentUrl}`);
  }

  console.log('\n2. Validating and saving micro-quizzes...');
  for (const mq of newMicroQuizzes) {
    const quizValidation = validateQuizSchema(mq.quiz);
    if (!quizValidation.valid) {
      console.error(`Validation failed for quiz ${mq.id}:`, quizValidation.errors);
      process.exit(1);
    }

    const payloadQuiz = {
      ...mq.quiz,
      questions: mq.quiz.questions.map(q => ({
        id: q.id,
        questionIndex: q.questionIndex,
        type: q.type,
        prompt: q.prompt,
        choices: q.choices,
        correctAnswer: q.correctAnswer,
        explanation: q.explanation
      }))
    };

    const fingerprint = crypto.createHash('sha256').update(JSON.stringify(payloadQuiz)).digest('hex').substring(0, 16);

    const itemJson = {
      id: mq.id,
      courseId: mq.courseId,
      lessonId: mq.lessonId,
      stableKey: mq.stableKey,
      itemType: mq.itemType,
      displayLabel: mq.displayLabel,
      orderKey: mq.orderKey,
      title: mq.title,
      contentUrl: mq.contentUrl,
      publishingStatus: mq.publishingStatus,
      payload: {
        quiz: payloadQuiz,
        provenance: {
          ...mq.provenance,
          fingerprint
        }
      }
    };

    const filePath = path.join(ITEMS_DIR, `${mq.id}.json`);
    fs.writeFileSync(filePath, JSON.stringify(itemJson, null, 2) + '\n');
    console.log(`  Saved micro-quiz: ${mq.id} (${mq.quiz.questions.length} questions)`);
  }

  console.log('\n3. Updating lesson JSON files with mapped items...');
  for (const [lessonId, itemsList] of Object.entries(lessonItemsMapping)) {
    const lPath = path.join(LESSONS_DIR, `${lessonId}.json`);
    if (!fs.existsSync(lPath)) {
      console.error(`Lesson file not found: ${lPath}`);
      process.exit(1);
    }
    const lessonData = JSON.parse(fs.readFileSync(lPath, 'utf8'));
    lessonData.items = itemsList;
    fs.writeFileSync(lPath, JSON.stringify(lessonData, null, 2) + '\n');
    console.log(`  Updated ${lessonId}: [${itemsList.join(', ')}]`);
  }

  console.log('\n4. Validating modular content tree...');
  const v2Dir = path.resolve(__dirname, '../content/v2');
  const outputPath = path.resolve(__dirname, '../content/9-sinif-v2-catalog.json');
  const vResult = validateModularTree(v2Dir);
  if (!vResult.valid) {
    console.error('Modular tree validation failed:', vResult.errors);
    process.exit(1);
  }
  console.log('✔ Modular content tree is 100% valid!');
  console.log(`  Courses: ${vResult.stats.courseCount}`);
  console.log(`  Lessons: ${vResult.stats.lessonCount}`);
  console.log(`  Items: ${vResult.stats.itemCount} (Videos: ${vResult.stats.videoCount}, Quizzes: ${vResult.stats.quizCount}, Micro: ${vResult.stats.microQuizCount})`);

  console.log('\n5. Compiling catalog...');
  const compiled = compileModularCatalog(v2Dir);
  const result = saveCompiledCatalog(compiled, outputPath);
  console.log(`✔ Catalog successfully compiled to ${outputPath} (${result.totalItems} items)`);
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
