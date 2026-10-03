const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { validateQuizSchema } = require('../cli/lib/v2-quiz');
const { validateModularTree, compileModularCatalog, saveCompiledCatalog } = require('../cli/lib/v2-modular');

const ITEMS_DIR = path.join(__dirname, '../content/v2/items');
const LESSONS_DIR = path.join(__dirname, '../content/v2/lessons');

// 1. Video items update map
const videoUpdates = [
  {
    id: 'item_kim9_vid_kimya_bilimi',
    title: 'Kimya Bilimi, Günlük Hayat ve Alt Disiplinleri',
    contentUrl: 'https://www.youtube.com/watch?v=2MItYrH-QPc',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_guvenlik_sembolleri',
    title: 'Laboratuvar Güvenlik Kuralları ve Uyarı Piktogramları',
    contentUrl: 'https://www.youtube.com/watch?v=vM33JUDUUUM',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik',
    title: 'Kimyasal Maddelerin Kullanımı ve Güvenlik',
    contentUrl: 'https://www.youtube.com/watch?v=vM33JUDUUUM',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_atom_modelleri',
    title: 'Atom Modelleri: Dalton, Thomson, Rutherford ve Bohr',
    contentUrl: 'https://www.youtube.com/watch?v=vi02jMBOkgg',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_periyodik_ozellikler',
    title: 'Modern Atom Teorisi ve Periyodik Özelliklerin Değişimi',
    contentUrl: 'https://www.youtube.com/watch?v=5wTqs5bYmIM',
    provider: 'Kimya Adası'
  },
  {
    id: 'item_kim9_vid_topic_04_atom_orbitalleri_ve_elektron_dizilimi',
    title: 'Atom Orbitalleri ve Elektron Dizilimi',
    contentUrl: 'https://www.youtube.com/watch?v=IZPnDJ13BWM',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_05_periyodik_tabloda_yer_bulma',
    title: 'Periyodik Tabloda Yer Bulma',
    contentUrl: 'https://www.youtube.com/watch?v=GpeYdemF6yI',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_06_periyodik_ozellikler',
    title: 'Periyodik Özellikler',
    contentUrl: 'https://www.youtube.com/watch?v=5wTqs5bYmIM',
    provider: 'Kimya Adası'
  },
  {
    id: 'item_kim9_vid_topic_07_metalik_bag',
    title: 'Metalik Bağ',
    contentUrl: 'https://www.youtube.com/watch?v=r80DLJNK-Tg',
    provider: 'Kimya Adası'
  },
  {
    id: 'item_kim9_vid_topic_08_iyonik_bag',
    title: 'İyonik Bağ',
    contentUrl: 'https://www.youtube.com/watch?v=aHEdCmKnCPo',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_09_kovalent_bag',
    title: 'Kovalent Bağ',
    contentUrl: 'https://www.youtube.com/watch?v=aJzqsKeB2Zs',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_10_lewis_nokta_yapisi',
    title: 'Lewis Nokta Yapısı',
    contentUrl: 'https://www.youtube.com/watch?v=JlB31tyAEgU',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_11_molekul_polarligi_ve_apolarligi',
    title: 'Molekül Polarlığı ve Apolarlığı',
    contentUrl: 'https://www.youtube.com/watch?v=7DmnzxY73Ik',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_12_bilesiklerin_adlandirilmasi',
    title: 'Bileşiklerin Adlandırılması',
    contentUrl: 'https://www.youtube.com/watch?v=1LcHGeLKs5A',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_bag_turleri',
    title: 'Güçlü Etkileşimler: İyonik, Kovalent ve Metalik Bağ',
    contentUrl: 'https://www.youtube.com/watch?v=aHEdCmKnCPo',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_zayif_etkilesimler',
    title: 'Moleküller Arası Zayıf Etkileşimler ve Hidrojen Bağı',
    contentUrl: 'https://www.youtube.com/watch?v=d1_esDqj9KA',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_14_katilar_ve_ozellikleri',
    title: 'Katılar ve Özellikleri',
    contentUrl: 'https://www.youtube.com/watch?v=0a4EwSXfhjA',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_topic_15_sivilar_ve_ozellikleri',
    title: 'Sıvılar ve Özellikleri',
    contentUrl: 'https://www.youtube.com/watch?v=m0SkOXAPJTo',
    provider: 'Kimyanın Kimyası'
  },
  {
    id: 'item_kim9_vid_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik',
    title: 'Nanoparçacıklar ve Ekolojik Sürdürülebilirlik',
    contentUrl: 'https://www.youtube.com/watch?v=JXZTb8lRnFU',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_katilar_ve_sivilar',
    title: 'Katı Türleri (Amorf, Kristal) ve Sıvılarda Viskozite',
    contentUrl: 'https://www.youtube.com/watch?v=0a4EwSXfhjA',
    provider: 'Meschemy Kimya'
  },
  {
    id: 'item_kim9_vid_yesil_kimya',
    title: 'Nanoparçacıklar, Ekolojik Sürdürülebilirlik ve Yeşil Kimya',
    contentUrl: 'https://www.youtube.com/watch?v=xgb_cd16Bfg',
    provider: 'Meschemy Kimya'
  }
];

// 2. Micro quizzes definition for missing video items
const microQuizzes = [
  // 1. Güvenlik Sembolleri (in lesson_kim9_kimya_bilimi_guvenlik)
  {
    id: 'item_kim9_vid_guvenlik_sembolleri__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_kimya_bilimi_guvenlik',
    stableKey: 'kim9_quiz_guvenlik_sembolleri_micro',
    itemType: 'QUIZ',
    displayLabel: '1.2-Q',
    orderKey: 2500,
    title: 'Güvenlik Piktogramları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Güvenlik Piktogramları Mikro Testi',
      questions: [
        {
          id: 'q_kim9_guvsem_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Üzerinde alev simgesi bulunan kimyasal güvenlik piktogramı ile alevin ortasında \'O\' (oksijen) harfi bulunan piktogram sırasıyla ne anlama gelir?',
          choices: [
            'Yanıcı Madde - Yakıcı (Oksitleyici) Madde',
            'Yakıcı Madde - Yanıcı Madde',
            'Patlayıcı Madde - Aşındırıcı Madde',
            'Zehirli Madde - Tahriş Edici Madde'
          ],
          correctAnswer: 'Yanıcı Madde - Yakıcı (Oksitleyici) Madde',
          explanation: 'Düz alev sembolü \'Yanıcı\' (flammable), ortasında daire/O harfi bulunan alev ise oksijen kaynağı olan \'Yakıcı/Oksitleyici\' (oxidizing) maddeleri gösterir.'
        },
        {
          id: 'q_kim9_guvsem_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tüpten dökülen sıvının bir metal yüzeyi ve bir eli aşındırdığını gösteren güvenlik piktogramının adı nedir?',
          choices: [
            'Aşındırıcı (Korozif) Madde',
            'Radyoaktif Madde',
            'Biyolojik Tehlike',
            'Patlayıcı Madde'
          ],
          correctAnswer: 'Aşındırıcı (Korozif) Madde',
          explanation: 'Korozif (aşındırıcı) piktogramı asit ve baz gibi cildi ve temas ettiği metalleri tahrip eden kimyasallarda bulunur.'
        },
        {
          id: 'q_kim9_guvsem_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Laboratuvarda derişik bir asit çözeltisi seyreltilirken, aşırı ısı açığa çıkıp sıçrama yapmaması için asidin üzerine doğrudan su dökülmelidir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Asit üzerine asla su eklenmez! Ekzotermik tepkime sonucu sıçrama olmaması için her zaman suyun içine yavaşça ve karıştırılarak asit eklenir.'
        },
        {
          id: 'q_kim9_guvsem_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Kurumuş ağaç ve ölü balık görseli içeren piktogram, maddenin çevreye ve sucul ekosisteme zararlı olduğunu belirtir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Çevreye zararlı piktogramı, atıkların doğrudan doğaya veya kanalizasyona bırakılmaması gerektiğini simgeler.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_guvenlik_sembolleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=vM33JUDUUUM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 2. Kimyasal Maddelerin Kullanımı ve Güvenlik (lesson_kim9_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik)
  {
    id: 'item_kim9_vid_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik',
    stableKey: 'kim9_quiz_kimyasal_kullanim_guvenlik_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Kimyasal Maddelerin Güvenli Kullanımı Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Kimyasal Maddelerin Güvenli Kullanımı Mikro Testi',
      questions: [
        {
          id: 'q_kim9_kullanim_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kimyasal maddelerin tehlikeleri, saklama koşulları, ilk yardım tedbirleri ve bertaraf yöntemlerini ayrıntılı açıklayan 16 maddelik belgeye ne ad verilir?',
          choices: [
            'Güvenlik Bilgi Formu (GBF / SDS)',
            'Periyodik Tablo Çizelgesi',
            'Laboratuvar Raporu',
            'Kimyasal Reçete Belgesi'
          ],
          correctAnswer: 'Güvenlik Bilgi Formu (GBF / SDS)',
          explanation: 'Güvenlik Bilgi Formu (SDS/GBF), kimyasal maddelerin taşınması, depolanması ve acil müdahale bilgilerini içeren resmi güvenlik belgesidir.'
        },
        {
          id: 'q_kim9_kullanim_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Ev temizliğinde çamaşır suyu (NaOCl) ile tuz ruhu (HCl) kesinlikle birbirine karıştırılmamalıdır. Bu iki kimyasal karıştığında açığa çıkan ölümcül zehirli gaz hangisidir?',
          choices: [
            'Klor gazı (Cl₂)',
            'Oksijen gazı (O₂)',
            'Azot gazı (N₂)',
            'Helyum gazı (He)'
          ],
          correctAnswer: 'Klor gazı (Cl₂)',
          explanation: 'NaOCl + 2HCl → NaCl + H₂O + Cl₂ tepkimesi sonucu son derece toksik ve boğucu olan Klor (Cl₂) gazı açığa çıkar.'
        },
        {
          id: 'q_kim9_kullanim_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Kurşun (Pb), Cıva (Hg) ve Kadmiyum (Cd) gibi ağır metaller insan vücudunda ve besin zincirinde birikerek nörolojik ve organ hasarlarına yol açar.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Ağır metaller biyolojik olarak parçalanamaz ve vücuttan kolay atılamaz; biyo-birikim yaparak toksik etkiler üretir.'
        },
        {
          id: 'q_kim9_kullanim_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Laboratuvarda kullanılan tüm organik ve inorganik kimyasal atıklar çevreye zarar vermemesi için doğrudan lavaboya bol suyla dökülerek uzaklaştırılmalıdır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Kimyasal atıklar asla lavaboya dökülmez! Türlerine göre (organik, asidik, ağır metal vb.) ayrılmış özel atık toplama bidonlarında biriktirilmelidir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=vM33JUDUUUM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 3. Periyodik Özellikler (in lesson_kim9_atom_ve_periyodik_sistem)
  {
    id: 'item_kim9_vid_periyodik_ozellikler__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_atom_ve_periyodik_sistem',
    stableKey: 'kim9_quiz_periyodik_ozellikler_micro',
    itemType: 'QUIZ',
    displayLabel: '2.2-Q',
    orderKey: 2500,
    title: 'Periyodik Özelliklerin Değişimi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Periyodik Özelliklerin Değişimi Mikro Testi',
      questions: [
        {
          id: 'q_kim9_peroz_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Periyodik sistemde aynı periyotta soldan sağa doğru gidildikçe atom yarıçapı nasıl değişir ve bunun temel sebebi nedir?',
          choices: [
            'Azalır; çünkü çekirdekteki proton sayısı ve çekim gücü artar.',
            'Artar; çünkü elektron sayısı artar.',
            'Değişmez; çünkü katman sayısı aynıdır.',
            'Önce artar, sonra azalır.'
          ],
          correctAnswer: 'Azalır; çünkü çekirdekteki proton sayısı ve çekim gücü artar.',
          explanation: 'Aynı periyotta katman sayısı sabitken proton sayısı arttığı için elektron başına düşen çekim kuvveti artar ve atom büzülerek çapı küçülür.'
        },
        {
          id: 'q_kim9_peroz_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aynı periyottaki A grubu elementlerinin 1. iyonlaşma enerjileri sıralamasında görülen "küresel simetri sapması" hangi gruplar arasında gerçekleşir?',
          choices: [
            '2A > 3A ve 5A > 6A (Üç aşağı beş yukarı)',
            '1A > 2A ve 3A > 4A',
            '7A > 8A ve 4A > 5A',
            '3A > 4A ve 6A > 7A'
          ],
          correctAnswer: '2A > 3A ve 5A > 6A (Üç aşağı beş yukarı)',
          explanation: '2A (s²) ve 5A (p³) grupları küresel simetriye sahip olduklarından daha kararlıdır; bu nedenle 1. iyonlaşma enerjileri 3A ve 6A\'dan büyüktür (1A < 3A < 2A < 4A < 6A < 5A < 7A < 8A).'
        },
        {
          id: 'q_kim9_peroz_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Elektronegatiflik, bağ elektronlarını çekme yeteneğidir ve periyodik sistemde elektronegatifliği en yüksek olan element Flor (F)\'dur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Florun elektronegatiflik değeri Pauling ölçeğinde 4.0 olarak kabul edilmiş en yüksek değerdir. Soygazlar ise genellikle bağ yapmadığı için elektronegatiflikleri ihmal edilir.'
        },
        {
          id: 'q_kim9_peroz_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Periyodik sistemde bir grupta yukarıdan aşağıya inildikçe metalik aktiflik (elektron verme kolaylığı) azalır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Aşağı inildikçe çap büyür, en dıştaki elektronu koparmak kolaylaşır; bu nedenle metallerde aktiflik aşağı doğru ARTAR.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_periyodik_ozellikler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=5wTqs5bYmIM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 4. Atom Orbitalleri ve Elektron Dizilimi (lesson_kim9_topic_04_atom_orbitalleri_ve_elektron_dizilimi)
  {
    id: 'item_kim9_vid_topic_04_atom_orbitalleri_ve_elektron_dizilimi__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_04_atom_orbitalleri_ve_elektron_dizilimi',
    stableKey: 'kim9_quiz_orbitaller_elektron_dizilimi_micro',
    itemType: 'QUIZ',
    displayLabel: '4.1-Q',
    orderKey: 1500,
    title: 'Atom Orbitalleri ve Elektron Dizilimi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Atom Orbitalleri ve Elektron Dizilimi Mikro Testi',
      questions: [
        {
          id: 'q_kim9_orb_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Elektronların orbitallere en düşük enerjili orbitalden başlanarak sırayla yerleşmesini ifade eden ilke hangisidir?',
          choices: [
            'Aufbau Kuralı',
            'Hund Kuralı',
            'Pauli Dışlama İlkesi',
            'Heisenberg Belirsizlik İlkesi'
          ],
          correctAnswer: 'Aufbau Kuralı',
          explanation: 'Aufbau (inşa etme) ilkesine göre elektronlar önce en düşük enerjili orbitallere (1s, 2s, 2p, 3s...) dolar.'
        },
        {
          id: 'q_kim9_orb_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir orbitalde zıt spinli (↑↓) en fazla iki elektron bulunabileceğini ve bir atomda hiçbir iki elektronun 4 kuantum sayısının aynı olamayacağını belirten ilke hangisidir?',
          choices: [
            'Pauli Dışlama İlkesi',
            'Hund Kuralı',
            'Moseley Yasası',
            'Bohr Postülatı'
          ],
          correctAnswer: 'Pauli Dışlama İlkesi',
          explanation: 'Wolfgang Pauli tarafından ortaya konan dışlama ilkesine göre bir orbital en fazla 2 elektron barındırabilir ve spinleri zıt olmalıdır.'
        },
        {
          id: 'q_kim9_orb_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Hund Kuralı\'na göre, eş enerjili p, d veya f orbitallerine elektronlar yerleşirken önce aynı spinle birer birer yerleşir, sonra ikinci elektronlar zıt spinle eşleşir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Hund kuralı, elektronların itme kuvvetini en aza indirmek için eş enerjili alt kabuklara önce paralel spinle tek tek yerleşmesini şart koşar.'
        },
        {
          id: 'q_kim9_orb_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Madelung-Klechkowski kuralına göre orbitallerin enerji sıralaması hangi toplam değere göre belirlenir?',
          choices: [
            '(n + ℓ) toplamı',
            '(n - ℓ) farkı',
            'Yalnızca baş kuantum sayısı (n)',
            'Yalnızca spin kuantum sayısı (ms)'
          ],
          correctAnswer: '(n + ℓ) toplamı',
          explanation: '(n + ℓ) değeri büyük olan orbitalin enerjisi daha yüksektir. Eşitlik durumunda n sayısı büyük olanın enerjisi fazladır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_04_atom_orbitalleri_ve_elektron_dizilimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=IZPnDJ13BWM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 5. Periyodik Tabloda Yer Bulma (lesson_kim9_topic_05_periyodik_tabloda_yer_bulma)
  {
    id: 'item_kim9_vid_topic_05_periyodik_tabloda_yer_bulma__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_05_periyodik_tabloda_yer_bulma',
    stableKey: 'kim9_quiz_yer_bulma_micro',
    itemType: 'QUIZ',
    displayLabel: '5.1-Q',
    orderKey: 1500,
    title: 'Periyodik Tabloda Yer Bulma Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Periyodik Tabloda Yer Bulma Mikro Testi',
      questions: [
        {
          id: 'q_kim9_yer_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Temel hâl elektron diziliminde en yüksek baş kuantum sayısı (n) elementin periyodik cetveldeki hangi özelliğini verir?',
          choices: [
            'Periyot numarasını',
            'Grup numarasını',
            'Nötron sayısını',
            'İyon yükünü'
          ],
          correctAnswer: 'Periyot numarasını',
          explanation: 'Elektron dizilimindeki en büyük baş kuantum sayısı (en yüksek enerji katmanı) periyot numarasını gösterir.'
        },
        {
          id: 'q_kim9_yer_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Atom numarası 15 olan Fosfor (₁₅P) elementinin periyodik tablodaki yeri nedir? (Elektron dizilimi: 1s² 2s² 2p⁶ 3s² 3p³)',
          choices: [
            '3. Periyot, 5A Grubu (15. Grup)',
            '2. Periyot, 5A Grubu',
            '3. Periyot, 3A Grubu',
            '4. Periyot, 5A Grubu'
          ],
          correctAnswer: '3. Periyot, 5A Grubu (15. Grup)',
          explanation: 'En yüksek baş kuantum sayısı 3 (3. Periyot), değerlik elektronları 3s² ve 3p³ toplamı 2+3=5 (5A Grubu).'
        },
        {
          id: 'q_kim9_yer_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Helyum (₂He) elementinin elektron dizilimi 1s² olup değerlik elektron sayısı 2 olmasına rağmen kimyasal benzerliği nedeniyle 8A (18. grup) asal gazlar grubunda yer alır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'He tek katmanını 2 elektronla doldurarak dubletini tamamlamış kararlı bir asal gazdır, bu yüzden 2A\'da değil 8A grubunda bulunur.'
        },
        {
          id: 'q_kim9_yer_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Son katman elektron dizilimi ...ns² np⁵ şeklinde biten elementler periyodik cetvelde hangi özel isimle adlandırılan grupta bulunur?',
          choices: [
            'Halojenler (7A Grubu)',
            'Alkali Metaller (1A Grubu)',
            'Toprak Alkali Metaller (2A Grubu)',
            'Kalkojenler (6A Grubu)'
          ],
          correctAnswer: 'Halojenler (7A Grubu)',
          explanation: 'Değerlik elektron sayısı 2 + 5 = 7 olan 7A grubu elementlerine Halojenler (tuz oluşturanlar: F, Cl, Br, I) denir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_05_periyodik_tabloda_yer_bulma',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=GpeYdemF6yI',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 6. Periyodik Özellikler Topic 06 (lesson_kim9_topic_06_periyodik_ozellikler)
  {
    id: 'item_kim9_vid_topic_06_periyodik_ozellikler__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_06_periyodik_ozellikler',
    stableKey: 'kim9_quiz_top06_periyodik_ozellikler_micro',
    itemType: 'QUIZ',
    displayLabel: '6.1-Q',
    orderKey: 1500,
    title: 'İyon Yarıçapı ve İyonlaşma Enerjisi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'İyon Yarıçapı ve İyonlaşma Enerjisi Mikro Testi',
      questions: [
        {
          id: 'q_kim9_top06_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Nötr bir atom elektron vererek pozitif yüklü bir katyona dönüştüğünde tanecik çapı nasıl değişir?',
          choices: [
            'Küçülür; çünkü elektron başına düşen çekirdek çekim kuvveti artar.',
            'Büyür; çünkü elektron verilmiştir.',
            'Değişmez; çünkü proton sayısı sabittir.',
            'Önce büyür sonra küçülür.'
          ],
          correctAnswer: 'Küçülür; çünkü elektron başına düşen çekirdek çekim kuvveti artar.',
          explanation: 'Katyon oluşurken elektron sayısı azalır, proton başına düşen çekim artar ve elektron bulutu büzülür: r(anyon) > r(nötr) > r(katyon).'
        },
        {
          id: 'q_kim9_top06_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'İzole gaz fazındaki nötr bir atomun bir elektron alarak negatif yüklü anyon oluşturması sırasındaki enerji değişimine "Elektron İlgisi" denir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'X(g) + e⁻ → X⁻(g) + Enerji süreci elektron ilgisini (Electron Affinity) tanımlar.'
        },
        {
          id: 'q_kim9_top06_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Periyodik tabloda elektron ilgisi en yüksek olan element hangisidir?',
          choices: [
            'Klor (Cl)',
            'Flor (F)',
            'Oksijen (O)',
            'Sodyum (Na)'
          ],
          correctAnswer: 'Klor (Cl)',
          explanation: 'Florun atom çapı çok küçük olup elektron itmesi fazla olduğundan elektron ilgisi Klor\'dan (Cl) biraz daha düşüktür; periyodik sistemde elektron ilgisi en büyük element Klor\'dur.'
        },
        {
          id: 'q_kim9_top06_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Bir atomdan art arda elektron koparılırken her zaman bir sonraki iyonlaşma enerjisi bir öncekinden daha büyüktür (IE₁ < IE₂ < IE₃...).',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Koparılan her elektrondan sonra tanecik çapı küçülür ve kalan elektronlara uygulanan çekim arttığından koparmak için daha çok enerji gerekir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_06_periyodik_ozellikler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=5wTqs5bYmIM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 7. Metalik Bağ (lesson_kim9_topic_07_metalik_bag)
  {
    id: 'item_kim9_vid_topic_07_metalik_bag__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_07_metalik_bag',
    stableKey: 'kim9_quiz_metalik_bag_micro',
    itemType: 'QUIZ',
    displayLabel: '7.1-Q',
    orderKey: 1500,
    title: 'Metalik Bağ ve Elektron Denizi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Metalik Bağ ve Elektron Denizi Mikro Testi',
      questions: [
        {
          id: 'q_kim9_metbag_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Metalik bağın oluşumu günümüzde hangi model ile açıklanır?',
          choices: [
            'Pozitif metal katyonları ile serbest hareket eden değerlik elektronlarının oluşturduğu "Elektron Denizi" modeli',
            'Metal atomları arasında kovalent bağ ortaklaşması',
            'Elektron alışverişi ile anyon-katyon kristali',
            'Van der Waals dipol çekimleri'
          ],
          correctAnswer: 'Pozitif metal katyonları ile serbest hareket eden değerlik elektronlarının oluşturduğu "Elektron Denizi" modeli',
          explanation: 'Metallerin değerlik elektronları serbestçe dolaşarak bir elektron denizi oluşturur ve pozitif katyonlar ile bu deniz arasındaki elektrostatik çekim metalik bağı kurar.'
        },
        {
          id: 'q_kim9_metbag_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki özelliklerden hangisi metallerdeki serbest elektron denizinin varlığı ile AÇIKLANAMAZ?',
          choices: [
            'Sulu çözeltilerinde asidik özellik göstermeleri',
            'Isı ve elektrik akımını çok iyi iletmeleri',
            'Yüzeylerinin parlak olması',
            'Darbe aldığında kırılmayıp tel ve levha hâline gelebilmeleri (haddeleme)'
          ],
          correctAnswer: 'Sulu çözeltilerinde asidik özellik göstermeleri',
          explanation: 'Isı-elektrik iletkenliği, parlaklık ve tel/levha olabilme elektron denizinin hareketliliği sayesindedir; asitlik ise kimyasal bir tepkime özelliğidir.'
        },
        {
          id: 'q_kim9_metbag_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Metalik bağ güçlü bir kimyasal etkileşim türüdür ve metallerin erime noktalarının genellikle yüksek olmasını sağlar.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'İyonik, kovalent ve metalik bağlar kimyasal (güçlü) etkileşimler sınıfındadır.'
        },
        {
          id: 'q_kim9_metbag_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Periyodik cetvelde aynı grupta yukarıdan aşağıya doğru inildikçe katyon yarıçapı büyüdüğü için metalik bağ kuvveti ve metallerin erime noktası genellikle azalır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Katyon yarıçapı küçüldükçe ve değerlik elektron sayısı arttıkça metalik bağ sağlamlığı artar; bu yüzden grupta aşağı doğru bağ zayıflar ve erime noktası düşer.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_07_metalik_bag',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=r80DLJNK-Tg',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 8. Lewis Nokta Yapısı (lesson_kim9_topic_10_lewis_nokta_yapisi)
  {
    id: 'item_kim9_vid_topic_10_lewis_nokta_yapisi__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_10_lewis_nokta_yapisi',
    stableKey: 'kim9_quiz_lewis_yapisi_micro',
    itemType: 'QUIZ',
    displayLabel: '10.1-Q',
    orderKey: 1500,
    title: 'Lewis Nokta Yapısı Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Lewis Nokta Yapısı Mikro Testi',
      questions: [
        {
          id: 'q_kim9_lew_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir elementin Lewis sembolü yazılırken element sembolünün etrafına hangi elektronlar nokta şeklinde yerleştirilir?',
          choices: [
            'Yalnızca değerlik (en dış katmandaki) elektronları',
            'Atomdaki tüm elektronlar',
            'Yalnızca çekirdekteki nötronlar',
            'Yalnızca iç katmanlardaki elektronlar'
          ],
          correctAnswer: 'Yalnızca değerlik (en dış katmandaki) elektronları',
          explanation: 'Lewis nokta yapısında yalnızca kimyasal bağ yapımına katılabilecek değerlik elektronları gösterilir.'
        },
        {
          id: 'q_kim9_lew_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Su (H₂O) molekülünün Lewis nokta yapısında toplam kaç tane bağlayıcı elektron çifti (bağ) ve kaç tane ortaklanmamış elektron çifti bulunur?',
          choices: [
            '2 bağlayıcı çift, 2 ortaklanmamış çift',
            '4 bağlayıcı çift, 0 ortaklanmamış çift',
            '1 bağlayıcı çift, 3 ortaklanmamış çift',
            '3 bağlayıcı çift, 1 ortaklanmamış çift'
          ],
          correctAnswer: '2 bağlayıcı çift, 2 ortaklanmamış çift',
          explanation: 'H₂O\'da Oksijen iki hidrojenle 2 tekli kovalent bağ yapar (2 bağlayıcı çift = 4 elektron) ve üzerinde 2 çift ortaklanmamış elektron (4 elektron) barındırır.'
        },
        {
          id: 'q_kim9_lew_3',
          type: 'TRUE_FALSE',
          questionIndex: 2,
          prompt: 'Azot molekülünde (N₂) iki azot atomu oktetini tamamlamak için aralarında 3 çift elektronu ortaklaşa kullanarak üçlü kovalent bağ (:N≡N:) oluşturur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Azot (5A) 3 elektrona ihtiyaç duyar; iki azot atomu 3\'er elektron ortaklaşarak güçlü bir üçlü kovalent bağ kurar.'
        },
        {
          id: 'q_kim9_lew_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Sodyum klorür (NaCl) bileşiğinin Lewis yapısında sodyum ve klor arasında elektron ortaklaşması gösteren çizgi (bağ) çizilir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'NaCl iyonik bir bileşiktir! Elektron ortaklaşması değil transferi vardır; Na⁺ ve [:Cl̈:]⁻ şeklinde köşeli parantez ve iyon yükleriyle gösterilir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_10_lewis_nokta_yapisi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=JlB31tyAEgU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 9. Molekül Polarlığı ve Apolarlığı (lesson_kim9_topic_11_molekul_polarligi_ve_apolarligi)
  {
    id: 'item_kim9_vid_topic_11_molekul_polarligi_ve_apolarligi__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_11_molekul_polarligi_ve_apolarligi',
    stableKey: 'kim9_quiz_molekul_polarligi_micro',
    itemType: 'QUIZ',
    displayLabel: '11.1-Q',
    orderKey: 1500,
    title: 'Molekül Polarlığı ve Apolarlığı Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Molekül Polarlığı ve Apolarlığı Mikro Testi',
      questions: [
        {
          id: 'q_kim9_pol_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Karbondioksit (CO₂) molekülünde C=O bağları polar kovalent olmasına rağmen CO₂ molekülünün apolar (kutupsuz) olmasının nedeni nedir?',
          choices: [
            'Molekülün doğrusal (lineer) ve simetrik yapıda olması, dipol momentlerin birbirini sıfırlaması (μ = 0)',
            'Karbon ve oksijenin aynı ametal olması',
            'Molekülde tekli bağ bulunması',
            'Oksijenin elektronegatifliğinin çok düşük olması'
          ],
          correctAnswer: 'Molekülün doğrusal (lineer) ve simetrik yapıda olması, dipol momentlerin birbirini sıfırlaması (μ = 0)',
          explanation: 'Doğrusal O=C=O molekülünde iki zıt yönlü dipol vektörü birbirini yok eder ve net dipol moment sıfır (apolar) olur.'
        },
        {
          id: 'q_kim9_pol_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki moleküllerden hangisi kalıcı dipol momentine sahip POLAR bir moleküldür?',
          choices: [
            'H₂O (Su)',
            'CH₄ (Metan)',
            'CCl₄ (Karbon tetraklorür)',
            'N₂ (Azot gazı)'
          ],
          correctAnswer: 'H₂O (Su)',
          explanation: 'Su molekülü kırık doğru geometrisine ve merkez atomda ortaklanmamış elektron çiftlerine sahip olduğundan asimetriktir ve polardır.'
        },
        {
          id: 'q_kim9_pol_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: '"Benzer benzeri çözer" ilkesine göre polar maddeler polar çözücülerde (su gibi), apolar maddeler ise apolar çözücülerde (benzen, CCl₄ gibi) iyi çözünür.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Maddelerin birbiri içinde çözünmesi moleküller arası benzer çekim kuvvetlerine dayanır.'
        },
        {
          id: 'q_kim9_pol_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Merkez atomunda ortaklanmamış değerlik elektron çifti bulunan moleküller (NH₃, H₂O gibi) daima apolar moleküllerdir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Merkez atomda ortaklanmamış elektron çifti bulunması yük simetrisini bozar ve molekülü POLAR yapar.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_11_molekul_polarligi_ve_apolarligi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=7DmnzxY73Ik',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 10. Bileşiklerin Adlandırılması (lesson_kim9_topic_12_bilesiklerin_adlandirilmasi)
  {
    id: 'item_kim9_vid_topic_12_bilesiklerin_adlandirilmasi__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_12_bilesiklerin_adlandirilmasi',
    stableKey: 'kim9_quiz_bilesik_adlandirma_micro',
    itemType: 'QUIZ',
    displayLabel: '12.1-Q',
    orderKey: 1500,
    title: 'Bileşiklerin Adlandırılması Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Bileşiklerin Adlandırılması Mikro Testi',
      questions: [
        {
          id: 'q_kim9_adn_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kovalent bağlı N₂O₅ bileşiğinin IUPAC kurallarına göre doğru sistematik adı hangisidir?',
          choices: [
            'Diazot pentaoksit',
            'Azot oksit',
            'Azot dioksit',
            'Diazot monooksit'
          ],
          correctAnswer: 'Diazot pentaoksit',
          explanation: 'Kovalent bileşiklerde Latince sayılar kullanılır: 2 Azot (Di-azot) + 5 Oksijen (penta-oksit) = Diazot pentaoksit.'
        },
        {
          id: 'q_kim9_adn_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Demir (Fe) farklı bileşiklerinde +2 ve +3 değerlik alabilen bir geçiş metalidir. Fe₂O₃ bileşiğinin doğru adı nedir?',
          choices: [
            'Demir(III) oksit',
            'Demir(II) oksit',
            'Didemir trioksit',
            'Demir oksit'
          ],
          correctAnswer: 'Demir(III) oksit',
          explanation: 'İyonik bileşiklerde metal değişken değerlikli ise parantez içinde Romen rakamıyla aldığı değerlik yazılır: Fe³⁺ olduğu için Demir(III) oksit.'
        },
        {
          id: 'q_kim9_adn_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Sabit değerlikli metallerin oluşturduğu iyonik bileşiklerin (örneğin MgCl₂) adlandırılmasında "Magnezyum diklorür" şeklinde Latince sayı ön ekleri kullanılır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'İyonik bileşiklerde mono, di, tri gibi sayılar KULLANILMAZ; bileşiğin adı doğrudan "Magnezyum klorür"dür.'
        },
        {
          id: 'q_kim9_adn_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yaygın kök iyonlardan olan (SO₄)²⁻ ve (NO₃)⁻ iyonlarının adları sırasıyla nedir?',
          choices: [
            'Sülfat - Nitrat',
            'Sülfit - Nitrit',
            'Sülfür - Nitrür',
            'Karbonat - Fosfat'
          ],
          correctAnswer: 'Sülfat - Nitrat',
          explanation: '(SO₄)²⁻ Sülfat kökü, (NO₃)⁻ ise Nitrat köküdür.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_12_bilesiklerin_adlandirilmasi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1LcHGeLKs5A',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 11. Güçlü Etkileşimler (in lesson_kim9_kimyasal_turler_etkilesim)
  {
    id: 'item_kim9_vid_bag_turleri__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_kimyasal_turler_etkilesim',
    stableKey: 'kim9_quiz_bag_turleri_micro',
    itemType: 'QUIZ',
    displayLabel: '13.1-Q',
    orderKey: 1500,
    title: 'Güçlü Etkileşimler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Güçlü Etkileşimler Mikro Testi',
      questions: [
        {
          id: 'q_kim9_bagtur_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kimyasal türleri bir arada tutan güçlü etkileşimler (kimyasal bağlar) hangi seçenekte eksiksiz verilmiştir?',
          choices: [
            'İyonik Bağ, Kovalent Bağ, Metalik Bağ',
            'Hidrojen Bağı, Dipol-Dipol, London Kuvvetleri',
            'İyonik Bağ, Hidrojen Bağı, Kovalent Bağ',
            'Metalik Bağ, Van der Waals Bağları'
          ],
          correctAnswer: 'İyonik Bağ, Kovalent Bağ, Metalik Bağ',
          explanation: 'Güçlü etkileşimler 3 tanedir: İyonik bağ, kovalent bağ ve metalik bağ.'
        },
        {
          id: 'q_kim9_bagtur_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Kimyasal bağların (güçlü etkileşimlerin) kopması veya oluşması sırasında meydana gelen enerji değişimi genellikle 40 kJ/mol\'den büyüktür.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Güçlü etkileşimlerdeki enerji değişimi ≥ 40 kJ/mol olup kimyasal değişimi temsil eder; zayıf etkileşimlerdeki enerji değişimi ise genellikle < 40 kJ/mol\'dür.'
        },
        {
          id: 'q_kim9_bagtur_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İki ametal atomu arasındaki kovalent bağ sayısı (tekli, ikili, üçlü) arttıkça bağ sağlamlığı ve bağ uzunluğu nasıl değişir?',
          choices: [
            'Bağ sağlamlığı artar, bağ uzunluğu kısalır.',
            'Bağ sağlamlığı azalır, bağ uzunluğu uzar.',
            'İkisi de artar.',
            'İkisi de değişmez.'
          ],
          correctAnswer: 'Bağ sağlamlığı artar, bağ uzunluğu kısalır.',
          explanation: 'Üçlü bağ tekli bağdan çok daha sağlamdır ve atomları birbirine daha yakın çeker (bağ boyu en kısadır).'
        },
        {
          id: 'q_kim9_bagtur_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'İyonik bağlı bileşikler oda sıcaklığında katı, sıvı veya gaz hâlde bulunabilirler.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'İyonik bağlar çok güçlü elektrostatik çekim oluşturduğundan iyonik bileşiklerin tamamı oda koşullarında katı kristal haldedir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_bag_turleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=aHEdCmKnCPo',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 12. Zayıf Etkileşimler (in lesson_kim9_kimyasal_turler_etkilesim)
  {
    id: 'item_kim9_vid_zayif_etkilesimler__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_kimyasal_turler_etkilesim',
    stableKey: 'kim9_quiz_zayif_etkilesimler_micro',
    itemType: 'QUIZ',
    displayLabel: '13.2-Q',
    orderKey: 2500,
    title: 'Zayıf Etkileşimler ve Hidrojen Bağı Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Zayıf Etkileşimler ve Hidrojen Bağı Mikro Testi',
      questions: [
        {
          id: 'q_kim9_zayif_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Hidrojen atomunun elektronegatifliği çok yüksek olan Flor (F), Oksijen (O) veya Azot (N) atomlarına bağlı olduğu moleküller arasında oluşan en güçlü zayıf etkileşim hangisidir?',
          choices: [
            'Hidrojen Bağı',
            'Dipol-Dipol Etkileşimi',
            'London Dispersiyon Kuvveti',
            'İyon-Dipol Etkileşimi'
          ],
          correctAnswer: 'Hidrojen Bağı',
          explanation: 'F, O, N atomlarına bağlı hidrojen içeren moleküller (H₂O, HF, NH₃) kendi aralarında hidrojen bağı oluşturur.'
        },
        {
          id: 'q_kim9_zayif_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Apolar moleküller ve soygaz atomları arasında anlık kutuplanmalar (geçici indüklenmiş dipoller) sonucu oluşan zayıf çekim kuvvetine ne ad verilir?',
          choices: [
            'London Dispersiyon Kuvvetleri',
            'Kovalent Bağ',
            'Dipol-Dipol Etkileşimi',
            'İyonik Etkileşim'
          ],
          correctAnswer: 'London Dispersiyon Kuvvetleri',
          explanation: 'Fritz London tarafından açıklanan bu kuvvetler tüm taneciklerde bulunur; apolar moleküllerdeki yegâne çekim türüdür.'
        },
        {
          id: 'q_kim9_zayif_3',
          type: 'TRUE_FALSE',
          questionIndex: 2,
          prompt: 'Bir moleküldeki toplam elektron sayısı arttıkça elektron bulutunun kutuplanabilirliği (polarize olabilirliği) artar ve London kuvvetleri güçlenir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Halojenlerde yukarıdan aşağıya inildikçe (F₂ gaz, Cl₂ gaz, Br₂ sıvı, I₂ katı) elektron sayısı ve London kuvvetleri artar, bu yüzden kaynama noktası yükselir.'
        },
        {
          id: 'q_kim9_zayif_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Suyun (H₂O) beklenenden çok daha yüksek bir kaynama noktasına (100 °C) sahip olmasının temel nedeni molekülleri arasında kurduğu güçlü hidrojen bağlarıdır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Hidrojen bağları dipol-dipol ve London kuvvetlerinden çok daha güçlü olduğundan sıvının buharlaşması için yüksek enerji gerekir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_zayif_etkilesimler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=d1_esDqj9KA',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 13. Katılar ve Özellikleri (lesson_kim9_topic_14_katilar_ve_ozellikleri)
  {
    id: 'item_kim9_vid_topic_14_katilar_ve_ozellikleri__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_14_katilar_ve_ozellikleri',
    stableKey: 'kim9_quiz_katilar_ozellikler_micro',
    itemType: 'QUIZ',
    displayLabel: '14.1-Q',
    orderKey: 1500,
    title: 'Katı Türleri ve Özellikleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Katı Türleri ve Özellikleri Mikro Testi',
      questions: [
        {
          id: 'q_kim9_kat_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tanecikleri belirli bir geometrik düzene göre dizilmeyen, sabit bir erime noktası bulunmayıp camsı geçiş sıcaklığına sahip olan katı türü hangisidir?',
          choices: [
            'Amorf Katılar',
            'Kristal Katılar',
            'İyonik Katılar',
            'Metalik Katılar'
          ],
          correctAnswer: 'Amorf Katılar',
          explanation: 'Cam, plastik, lastik, tereyağı ve mum gibi maddeler amorf katıdır; belirli erime noktaları yoktur, yumuşayarak erirler.'
        },
        {
          id: 'q_kim9_kat_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Elmas, grafit ve kuvars (SiO₂) hangi kristal katı sınıfına aittir?',
          choices: [
            'Kovalent Kristaller (Ağ Örgülü)',
            'Moleküler Kristaller',
            'İyonik Kristaller',
            'Metalik Kristaller'
          ],
          correctAnswer: 'Kovalent Kristaller (Ağ Örgülü)',
          explanation: 'Atomların üç boyutlu ağ örgüsü şeklinde kovalent bağlarla bağlandığı katılara kovalent kristal denir; erime noktaları olağanüstü yüksektir.'
        },
        {
          id: 'q_kim9_kat_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Buz (H₂O), kuru buz (CO₂) ve naftalin (C₁₀H₈) tanecikleri arasında zayıf etkileşimler bulunan moleküler kristallerdir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Moleküllerin London, dipol-dipol veya hidrojen bağlarıyla bir arada tutulduğu katılar moleküler kristal olup erime noktaları düşüktür.'
        },
        {
          id: 'q_kim9_kat_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Grafit, tabakalı yapısındaki serbest hareket edebilen pi elektronları sayesinde elektriği iletebilen istisnai bir ametal kovalent kristaldir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Elmas elektriği iletmezken grafit tabakalar arasındaki serbest elektronlar sayesinde elektriği iletir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_14_katilar_ve_ozellikleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=0a4EwSXfhjA',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 14. Sıvılar ve Özellikleri (lesson_kim9_topic_15_sivilar_ve_ozellikleri)
  {
    id: 'item_kim9_vid_topic_15_sivilar_ve_ozellikleri__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_15_sivilar_ve_ozellikleri',
    stableKey: 'kim9_quiz_sivilar_ozellikler_micro',
    itemType: 'QUIZ',
    displayLabel: '15.1-Q',
    orderKey: 1500,
    title: 'Sıvılar ve Özellikleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Sıvılar ve Özellikleri Mikro Testi',
      questions: [
        {
          id: 'q_kim9_siv_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sıvıların akmaya karşı gösterdiği iç dirence ne ad verilir ve sıcaklık arttıkça bu değer nasıl değişir?',
          choices: [
            'Viskozite denir; sıcaklık arttıkça azalır (akıcılık artar).',
            'Viskozite denir; sıcaklık arttıkça artar.',
            'Yüzey gerilimi denir; sıcaklık arttıkça artar.',
            'Buhar basıncı denir; sıcaklık arttıkça azalır.'
          ],
          correctAnswer: 'Viskozite denir; sıcaklık arttıkça azalır (akıcılık artar).',
          explanation: 'Viskozite akmaya karşı dirençtir. Sıcaklık arttığında moleküller arası bağlar gevşer, viskozite düşer ve sıvı daha kolay akar (örneğin sıcak bal veya ısıtılan motor yağı).'
        },
        {
          id: 'q_kim9_siv_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kapalı bir kaptaki saf sıvının denge buhar basıncını aşağıdakilerden hangisi ETKİLEMEZ?',
          choices: [
            'Sıvının miktarı ve kabın şekli/hacmi',
            'Sıvının sıcaklığı',
            'Sıvının cinsi (moleküller arası çekim)',
            'Sıvının saflığı (içinde uçucu olmayan katı çözünmesi)'
          ],
          correctAnswer: 'Sıvının miktarı ve kabın şekli/hacmi',
          explanation: 'Buhar basıncı sadece 3S kuralına bağlıdır: Sıcaklık, Sıvının cinsi, Saflık. Sıvı miktarı, kap hacmi ve açık hava basıncı buhar basıncını etkilemez.'
        },
        {
          id: 'q_kim9_siv_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Kaynama olayı, bir sıvının iç buhar basıncının bulunduğu ortamdaki açık hava (dış) basıncına eşitlendiği sıcaklıkta gerçekleşir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'P(buhar) = P(dış) olduğunda kaynama başlar; dış basınç düşerse (dağın tepesinde) kaynama noktası da düşer.'
        },
        {
          id: 'q_kim9_siv_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Aynı sıvı molekülleri arasındaki çekim kuvvetine Adezyon, sıvı molekülleri ile kap çeperi arasındaki çekim kuvvetine ise Kohezyon denir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Tam tersidir! Aynı cins moleküller arası çekim KOHEZYON (yüzey gerilimini oluşturan), farklı maddeler arası yapışma kuvveti ise ADEZYON\'dur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_15_sivilar_ve_ozellikleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=m0SkOXAPJTo',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 15. Nanoparçacıklar ve Ekolojik Sürdürülebilirlik (lesson_kim9_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik)
  {
    id: 'item_kim9_vid_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik',
    stableKey: 'kim9_quiz_nanoparcaciklar_micro',
    itemType: 'QUIZ',
    displayLabel: '16.1-Q',
    orderKey: 1500,
    title: 'Nanoparçacıklar ve Yeşil Sentez Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Nanoparçacıklar ve Yeşil Sentez Mikro Testi',
      questions: [
        {
          id: 'q_kim9_nano_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Nanoparçacıklar metrenin milyarda biri (1 - 100 nm) boyutundaki taneciklerdir. Maddeler nano boyuta indiğinde kimyasal reaktivitelerinin muazzam artmasının temel nedeni nedir?',
          choices: [
            'Yüzey alanı / hacim oranının aşırı derecede artması',
            'Kütle çekim kuvvetinin artması',
            'Atom numaralarının değişmesi',
            'Proton sayılarının azalması'
          ],
          correctAnswer: 'Yüzey alanı / hacim oranının aşırı derecede artması',
          explanation: 'Maddeler ufalandıkça toplam yüzey alanı katbekat artar ve yüzeydeki aktif atom sayısı arttığı için reaktiflik olağanüstü yükselir.'
        },
        {
          id: 'q_kim9_nano_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yeşil kimya kapsamında evsel atıklardan (nar kabuğu, çay atığı, meyve özütleri) gümüş nanoparçacık (AgNP) eldesinde bu bitkisel atıklar hangi rolde görev yapar?',
          choices: [
            'Doğal indirgeyici ve stabilize edici ajan olarak',
            'Yüksek sıcaklık fırını olarak',
            'Radyoaktif kaynak olarak',
            'Ağır metal kirleticisi olarak'
          ],
          correctAnswer: 'Doğal indirgeyici ve stabilize edici ajan olarak',
          explanation: 'Bitki ekstraktlarındaki polifenoller ve antioksidanlar, toksik kimyasal kullanmadan Ag⁺ iyonlarını Ag⁰ nano taneciklerine indirger.'
        },
        {
          id: 'q_kim9_nano_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Gümüş nanoparçacıklar güçlü antimikrobiyal ve antibakteriyel özellikleri sayesinde medikal pansuman örtülerinde ve spor giyim kumaşlarında yaygın kullanılır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'AgNP bakterilerin hücre duvarını ve DNA sentezini bozarak bakteri üremesini engeller.'
        },
        {
          id: 'q_kim9_nano_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Metal nanoparçacıkların su arıtma tesislerinde tutulamayarak sucul ekosistemlere ve nehirlere karışması su canlılarında biyolojik birikime ve ekotoksikolojik risklere yol açabilir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Nano boyuttaki tanecikler hücre zarlarından kolayca geçebilir ve besin zincirinde birikerek ekolojik tehdit oluşturabilir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=JXZTb8lRnFU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 16. Katılar ve Sıvılar (in lesson_kim9_maddenin_halleri_surdurulebilirlik)
  {
    id: 'item_kim9_vid_katilar_ve_sivilar__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_maddenin_halleri_surdurulebilirlik',
    stableKey: 'kim9_quiz_katilar_ve_sivilar_micro',
    itemType: 'QUIZ',
    displayLabel: '17.1-Q',
    orderKey: 1500,
    title: 'Katı ve Sıvı Halleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Katı ve Sıvı Halleri Mikro Testi',
      questions: [
        {
          id: 'q_kim9_katsiv_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Karayollarına sıcak asfalt dökülürken ziftin yüksek sıcaklıkta ısıtılmasının temel kimyasal gerekçesi nedir?',
          choices: [
            'Viskozitesini düşürerek akışkanlığını artırmak ve zemine kolay yayılmasını sağlamak',
            'Buhar basıncını sıfıra indirmek',
            'Kristal katı örgüsü oluşturmak',
            'Adezyon kuvvetini yok etmek'
          ],
          correctAnswer: 'Viskozitesini düşürerek akışkanlığını artırmak ve zemine kolay yayılmasını sağlamak',
          explanation: 'Sıcaklık arttıkça viskozite azalır, sıvı akıcı hale gelir ve yolu kolayca kaplar.'
        },
        {
          id: 'q_kim9_katsiv_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir kılcal boruda suyun içbükey (menisküs çukuru) oluşturarak boruda yükselmesi hangi kuvvet ilişkisini kanıtlar?',
          choices: [
            'Adezyon kuvveti > Kohezyon kuvveti',
            'Kohezyon kuvveti > Adezyon kuvveti',
            'Yalnızca yer çekimi kuvveti',
            'Viskozitenin sıfır olduğunu'
          ],
          correctAnswer: 'Adezyon kuvveti > Kohezyon kuvveti',
          explanation: 'Cam ile su arasındaki yapışma (adezyon), su moleküllerinin kendi arasındaki tutunmasından (kohezyon) büyük olduğunda sıvı camı ıslatır ve boruda yükselir.'
        },
        {
          id: 'q_kim9_katsiv_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Sabun ve deterjan gibi yüzey aktif maddeler suyun yüzey gerilimini düşürerek kirlerin içine suyun nüfuz etmesini ve temizliği kolaylaştırır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Yüzey aktif maddeler su molekülleri arasındaki hidrojen bağlarını zayıflatarak yüzey gerilimini düşürür.'
        },
        {
          id: 'q_kim9_katsiv_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Kristal katıların düzenli üç boyutlu bir geometrik örgü yapısı ve net bir erime noktası vardır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Kristal katılar (iyonik, kovalent, moleküler, metalik) düzenli birim hücrelerden oluşur ve belirli erime sıcaklığında erir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_katilar_ve_sivilar',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=0a4EwSXfhjA',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T21:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 17. Yeşil Kimya (in lesson_kim9_maddenin_halleri_surdurulebilirlik)
  {
    id: 'item_kim9_vid_yesil_kimya__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_maddenin_halleri_surdurulebilirlik',
    stableKey: 'kim9_quiz_yesil_kimya_micro',
    itemType: 'QUIZ',
    displayLabel: '17.2-Q',
    orderKey: 2500,
    title: 'Yeşil Kimya ve Sürdürülebilirlik Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Yeşil Kimya ve Sürdürülebilirlik Mikro Testi',
      questions: [
        {
          id: 'q_kim9_yesil_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yeşil Kimyanın (Green Chemistry) 12 temel ilkesinden ilki ve en önemlisi olan prensip hangisidir?',
          choices: [
            'Atıkların Oluşumunu Önleme (Prevention)',
            'Tüm kimyasalların üretimini durdurma',
            'Fosil yakıt tüketimini iki katına çıkarma',
            'Atıkları denizlere deşarj etme'
          ],
          correctAnswer: 'Atıkların Oluşumunu Önleme (Prevention)',
          explanation: 'Yeşil kimyanın bir numaralı kuralı "Atığı oluştuktan sonra temizlemek yerine en baştan oluşmasını engellemek"tir.'
        },
        {
          id: 'q_kim9_yesil_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kimyasal tepkimelerde reaksiyona giren tüm başlangıç atomlarının hedeflenen nihai üründe bulunma oranına ne ad verilir?',
          choices: [
            'Atom Ekonomisi',
            'Yüzde Verim',
            'Viskozite İndeksi',
            'Reaksiyon Entalpisi'
          ],
          correctAnswer: 'Atom Ekonomisi',
          explanation: 'Yüksek atom ekonomisi, yan ürün veya atık oluşturmadan başlangıç maddelerinin tamamının hedeflenen ürüne dönüşmesini hedefler.'
        },
        {
          id: 'q_kim9_yesil_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Yeşil kimya, zehirli organik çözücüler yerine su, süperkritik CO₂ gibi çevre dostu çözücülerin ve yenilenebilir hammaddelerin kullanılmasını teşvik eder.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Toksik uçucu organik çözücülerin ikame edilmesi yeşil kimyanın temel amaçlarındandır.'
        },
        {
          id: 'q_kim9_yesil_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Kimyasal süreçlerde enerji tüketimini azaltmak için oda sıcaklığı ve basıncında çalışan enzim/katalizörlerin kullanılması yeşil kimya ilkeleriyle uyumludur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Katalitik süreçler enerji tüketimini düşürerek ekolojik ayak izini azaltır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_yesil_kimya',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=xgb_cd16Bfg',
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

// 3. Lesson configurations: defining exact items array for all 17 lessons
const lessonConfigurations = {
  lesson_kim9_kimya_bilimi_guvenlik: [
    'item_kim9_vid_kimya_bilimi',
    'item_kim9_vid_kimya_bilimi__quiz',
    'item_kim9_vid_guvenlik_sembolleri',
    'item_kim9_vid_guvenlik_sembolleri__quiz',
    'item_kim9_quiz_kimya_guvenlik'
  ],
  lesson_kim9_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik: [
    'item_kim9_vid_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik',
    'item_kim9_vid_topic_02_kimyasal_maddelerin_kullanimi_ve_guvenlik__quiz'
  ],
  lesson_kim9_atom_ve_periyodik_sistem: [
    'item_kim9_vid_atom_modelleri',
    'item_kim9_vid_atom_modelleri__quiz',
    'item_kim9_vid_periyodik_ozellikler',
    'item_kim9_vid_periyodik_ozellikler__quiz',
    'item_kim9_quiz_atom_periyodik'
  ],
  lesson_kim9_topic_04_atom_orbitalleri_ve_elektron_dizilimi: [
    'item_kim9_vid_topic_04_atom_orbitalleri_ve_elektron_dizilimi',
    'item_kim9_vid_topic_04_atom_orbitalleri_ve_elektron_dizilimi__quiz'
  ],
  lesson_kim9_topic_05_periyodik_tabloda_yer_bulma: [
    'item_kim9_vid_topic_05_periyodik_tabloda_yer_bulma',
    'item_kim9_vid_topic_05_periyodik_tabloda_yer_bulma__quiz'
  ],
  lesson_kim9_topic_06_periyodik_ozellikler: [
    'item_kim9_vid_topic_06_periyodik_ozellikler',
    'item_kim9_vid_topic_06_periyodik_ozellikler__quiz'
  ],
  lesson_kim9_topic_07_metalik_bag: [
    'item_kim9_vid_topic_07_metalik_bag',
    'item_kim9_vid_topic_07_metalik_bag__quiz'
  ],
  lesson_kim9_topic_08_iyonik_bag: [
    'item_kim9_vid_topic_08_iyonik_bag',
    'item_kim9_vid_topic_08_iyonik_bag__quiz'
  ],
  lesson_kim9_topic_09_kovalent_bag: [
    'item_kim9_vid_topic_09_kovalent_bag',
    'item_kim9_vid_topic_09_kovalent_bag__quiz'
  ],
  lesson_kim9_topic_10_lewis_nokta_yapisi: [
    'item_kim9_vid_topic_10_lewis_nokta_yapisi',
    'item_kim9_vid_topic_10_lewis_nokta_yapisi__quiz'
  ],
  lesson_kim9_topic_11_molekul_polarligi_ve_apolarligi: [
    'item_kim9_vid_topic_11_molekul_polarligi_ve_apolarligi',
    'item_kim9_vid_topic_11_molekul_polarligi_ve_apolarligi__quiz'
  ],
  lesson_kim9_topic_12_bilesiklerin_adlandirilmasi: [
    'item_kim9_vid_topic_12_bilesiklerin_adlandirilmasi',
    'item_kim9_vid_topic_12_bilesiklerin_adlandirilmasi__quiz'
  ],
  lesson_kim9_kimyasal_turler_etkilesim: [
    'item_kim9_vid_bag_turleri',
    'item_kim9_vid_bag_turleri__quiz',
    'item_kim9_vid_zayif_etkilesimler',
    'item_kim9_vid_zayif_etkilesimler__quiz',
    'item_kim9_quiz_baglar_etkilesim'
  ],
  lesson_kim9_topic_14_katilar_ve_ozellikleri: [
    'item_kim9_vid_topic_14_katilar_ve_ozellikleri',
    'item_kim9_vid_topic_14_katilar_ve_ozellikleri__quiz'
  ],
  lesson_kim9_topic_15_sivilar_ve_ozellikleri: [
    'item_kim9_vid_topic_15_sivilar_ve_ozellikleri',
    'item_kim9_vid_topic_15_sivilar_ve_ozellikleri__quiz'
  ],
  lesson_kim9_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik: [
    'item_kim9_vid_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik',
    'item_kim9_vid_topic_16_nanoparcaciklar_ve_ekolojik_surdurulebilirlik__quiz'
  ],
  lesson_kim9_maddenin_halleri_surdurulebilirlik: [
    'item_kim9_vid_katilar_ve_sivilar',
    'item_kim9_vid_katilar_ve_sivilar__quiz',
    'item_kim9_vid_yesil_kimya',
    'item_kim9_vid_yesil_kimya__quiz',
    'item_kim9_quiz_maddenin_halleri'
  ]
};

function runUpdate() {
  console.log('--- 1. Updating Video Items for course_kim_9 ---');
  for (const v of videoUpdates) {
    const filePath = path.join(ITEMS_DIR, `${v.id}.json`);
    let item = {};
    if (fs.existsSync(filePath)) {
      item = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    }
    item.id = v.id;
    item.courseId = 'course_kim_9';
    item.title = v.title;
    item.contentUrl = v.contentUrl;
    item.itemType = 'VIDEO';
    item.publishingStatus = 'active';
    item.payload = item.payload || {};
    item.payload.provider = v.provider;
    item.payload.provenance = item.payload.provenance || {
      sourceRef: '10-Projects/1_ay_kimya_video_rehberi.md',
      sourceSection: v.title,
      schemaVersion: 'v2',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: new Date().toISOString()
    };
    item.payload.provenance.sourceVideoUrl = v.contentUrl;
    fs.writeFileSync(filePath, JSON.stringify(item, null, 2) + '\n', 'utf8');
    console.log(`Updated video item: ${v.id} -> ${v.contentUrl}`);
  }

  console.log('\n--- 2. Creating / Updating Micro Quizzes for course_kim_9 ---');
  for (const mq of microQuizzes) {
    const quizPayload = {
      quizTitle: mq.quiz.quizTitle,
      questions: mq.quiz.questions,
      questionCount: mq.quiz.questions.length,
      schemaVersion: 'v2-quiz'
    };

    // Validate quiz schema
    const validation = validateQuizSchema(quizPayload);
    if (!validation.valid) {
      console.error(`Validation failed for quiz ${mq.id}:`, validation.errors);
      process.exit(1);
    }

    const itemObj = {
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
        quiz: quizPayload,
        provenance: {
          derivedFromItemId: mq.provenance.derivedFromItemId,
          sourceVideoUrl: mq.provenance.sourceVideoUrl,
          transcriptLanguage: mq.provenance.transcriptLanguage || 'tr',
          transcriptKind: mq.provenance.transcriptKind || 'subtitle',
          generatedBy: 'gemini',
          reviewStatus: 'verified',
          reviewedOverride: true,
          importedAt: mq.provenance.importedAt || new Date().toISOString(),
          schemaVersion: 'v2',
          fingerprint: crypto.createHash('sha256').update(JSON.stringify(quizPayload)).digest('hex').slice(0, 16)
        }
      }
    };

    const filePath = path.join(ITEMS_DIR, `${mq.id}.json`);
    fs.writeFileSync(filePath, JSON.stringify(itemObj, null, 2) + '\n', 'utf8');
    console.log(`Saved micro quiz: ${mq.id} (${quizPayload.questions.length} Qs)`);
  }

  console.log('\n--- 3. Updating Lesson References ---');
  for (const [lessonId, itemIds] of Object.entries(lessonConfigurations)) {
    const lessonPath = path.join(LESSONS_DIR, `${lessonId}.json`);
    if (!fs.existsSync(lessonPath)) {
      console.error(`Lesson file not found: ${lessonPath}`);
      process.exit(1);
    }
    const lesson = JSON.parse(fs.readFileSync(lessonPath, 'utf8'));
    lesson.items = itemIds;
    fs.writeFileSync(lessonPath, JSON.stringify(lesson, null, 2) + '\n', 'utf8');
    console.log(`Updated lesson ${lessonId} -> ${itemIds.length} items`);
  }

  console.log('\n--- 4. Validating Modular Tree ---');
  const V2_DIR = path.join(__dirname, '../content/v2');
  const treeValidation = validateModularTree(V2_DIR);
  console.log('Validation results:', treeValidation);
  if (!treeValidation.valid) {
    console.error('Tree validation failed:', treeValidation.errors);
    process.exit(1);
  }

  console.log('\n--- 5. Compiling V2 Catalog ---');
  const outputPath = path.join(__dirname, '../content/9-sinif-v2-catalog.json');
  const catalog = compileModularCatalog(V2_DIR);
  saveCompiledCatalog(catalog, outputPath);
  console.log('V2 Catalog compilation complete!');
}

runUpdate();
