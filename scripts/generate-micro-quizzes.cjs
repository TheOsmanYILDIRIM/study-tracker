/**
 * Deterministic generation of transcript-grounded micro-quizzes in modular content/v2
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { validateQuizSchema } = require('../cli/lib/v2-quiz');
const { validateModularTree, compileModularCatalog, saveCompiledCatalog } = require('../cli/lib/v2-modular');

const microQuizzes = [
  {
    id: 'item_mat9_vid_araliklar_gosterim__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_araliklar_kumeler',
    stableKey: 'mat9_quiz_araliklar_gosterim_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Gerçek Sayı Aralıkları & Gösterim Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Gerçek Sayı Aralıkları & Gösterim Mikro Testi',
      questions: [
        {
          id: 'q_mat9_araliklar_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sayı doğrusunda 3 ile 7 arasındaki gerçek sayılardan 3 dahil, 7 dahil değilse bu aralık küme ve parantez sembolleriyle nasıl gösterilir?',
          choices: [
            '[3, 7)',
            '(3, 7]',
            '(3, 7)',
            '[3, 7]'
          ],
          correctAnswer: '[3, 7)',
          explanation: 'Sayı doğrusunda dahil olan sınır köşeli parantez "[" ile, dahil olmayan sınır normal parantez ")" ile gösterilir.'
        },
        {
          id: 'q_mat9_araliklar_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Gerçek sayılar kümesinde (-∞, 5] aralığı 5 ve 5\'ten küçük tüm gerçek sayıları ifade eder.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Sonsuz eksi yönü (-∞ ve 5 dahil 5] gösterimi, 5 ve 5\'ten küçük tüm gerçek sayıları kapsar.'
        },
        {
          id: 'q_mat9_araliklar_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '(-2, 4] yarı açık aralığında bulunan TAM SAYILARIN toplamı kaçtır?',
          choices: [
            '9',
            '7',
            '10',
            '4'
          ],
          correctAnswer: '9',
          explanation: '-2 açık olduğu için dahil edilmez, 4 kapalı olduğu için dahil edilir; tam sayılar {-1, 0, 1, 2, 3, 4} olup toplamları 9\'dur.'
        },
        {
          id: 'q_mat9_araliklar_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sayı doğrusunda bir noktanın içinin boş bırakılması o noktanın aralığa dahil olduğunu mu yoksa olmadığını mı gösterir?',
          choices: [
            'Aralığa dahil olmadığını (açık aralık)',
            'Aralığa dahil olduğunu (kapalı aralık)',
            'Yalnızca pozitif olduğunu',
            'Sıfır noktası olduğunu'
          ],
          correctAnswer: 'Aralığa dahil olmadığını (açık aralık)',
          explanation: 'Sayı doğrusunda içi boş çember noktanın aralığa dahil olmadığını, içi dolu çember ise dahil olduğunu belirtir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_araliklar_gosterim',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=TNw7eEas9Oo',
      transcriptLanguage: 'tr',
      transcriptKind: 'auto',
      transcriptFingerprint: 'd43ff753c7d5a6e2',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },
  {
    id: 'item_mat9_vid_aralik_farki__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_araliklar_kumeler',
    stableKey: 'mat9_quiz_aralik_farki_micro',
    itemType: 'QUIZ',
    displayLabel: '2.2-Q',
    orderKey: 2500,
    title: 'Aralık Farkı ve Eşitsizlik Problemleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Aralık Farkı ve Eşitsizlik Problemleri Mikro Testi',
      questions: [
        {
          id: 'q_mat9_fark_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'A = [-3, 5] ve B = [1, 8) aralıkları verildiğinde A \\ B (A fark B) aralığı aşağıdakilerden hangisidir?',
          choices: [
            '[-3, 1)',
            '[-3, 1]',
            '(1, 5]',
            '[-3, 8)'
          ],
          correctAnswer: '[-3, 1)',
          explanation: '1 elemanı B kümesinde bulunduğu için A fark B kümesinden çıkarılır ve 1 noktasında açık parantez oluşur: [-3, 1).'
        },
        {
          id: 'q_mat9_fark_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'A = (2, 6) ve B = [4, 7] ise A ∩ B (A kesişim B) kümesi [4, 6) yarı açık aralığıdır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Her iki kümede de bulunan ortak bölge 4 (dahil) ile 6 (dahil değil) arasıdır, yani [4, 6).'
        },
        {
          id: 'q_mat9_fark_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'A = [-2, 7] ve B = (3, 10] aralıkları için A ∪ B (A birleşim B) kümesi hangisidir?',
          choices: [
            '[-2, 10]',
            '[-2, 3]',
            '(3, 7]',
            '(-2, 10)'
          ],
          correctAnswer: '[-2, 10]',
          explanation: 'Birleşim kümesi her iki aralığın kapladığı en geniş aralık olup [-2, 10] kapalı aralığıdır.'
        },
        {
          id: 'q_mat9_fark_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'A = [0, 4] ve B = (2, 5] verildiğinde B \\ A (B fark A) aralığı nedir?',
          choices: [
            '(4, 5]',
            '[4, 5]',
            '(2, 4)',
            '[0, 2]'
          ],
          correctAnswer: '(4, 5]',
          explanation: '4 elemanı A kümesinde olduğu için B\'den 4 ve öncesi atılır, geriye (4, 5] kalır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_aralik_farki',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=drPKmUSKYCI',
      transcriptLanguage: 'tr',
      transcriptKind: 'auto',
      transcriptFingerprint: '86848e26a7a2a378',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },
  {
    id: 'item_tar9_vid_birey_toplum__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_gecmisin_insasi',
    stableKey: 'tar9_quiz_birey_toplum_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Tarih Öğrenmenin Bireye ve Topluma Faydaları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarih Öğrenmenin Bireye ve Topluma Faydaları Mikro Testi',
      questions: [
        {
          id: 'q_tar9_birey_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih öğrenmenin bireye kazandırdığı en temel becerilerden biri olan "tarihsel empati" ne anlama gelir?',
          choices: [
            'Geçmişteki olayları o dönemin şartlarını ve zihniyetini dikkate alarak değerlendirmek',
            'Geçmişteki tüm savaşları günümüz ahlak kurallarıyla yargılamak',
            'Tarihi olayları günümüzün teknolojik imkanlarıyla canlandırmak',
            'Geçmişte yaşanan acıları unutarak geleceğe bakmak'
          ],
          correctAnswer: 'Geçmişteki olayları o dönemin şartlarını ve zihniyetini dikkate alarak değerlendirmek',
          explanation: 'Tarihsel empati, geçmişte yaşamış insanların kararlarını dönemin koşulları, inançları ve imkanları çerçevesinde anlama becerisidir.'
        },
        {
          id: 'q_tar9_birey_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Tarih bilimi, toplumların ortak hafızasını ve milli kimlik bilincini güçlendirerek birlik ve beraberliğe katkı sağlar.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Tarih bilimi toplumsal belleği oluşturur, ortak değer ve bilinç kazandırarak sosyal dayanışmayı artırır.'
        },
        {
          id: 'q_tar9_birey_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi tarihin araştırma alanına giren konulardan biri DEĞİLDİR?',
          choices: [
            'Doğa olaylarının fiziksel ve kimyasal kanunlarını laboratuvarda ispatlamak',
            'İnsan topluluklarının geçmişteki siyasi, kültürel ve ekonomik faaliyetleri',
            'Geçmişte yaşanan antlaşmalar ve savaşların neden-sonuç ilişkisi',
            'Uygarlıkların bıraktığı somut ve yazılı kültürel miras'
          ],
          correctAnswer: 'Doğa olaylarının fiziksel ve kimyasal kanunlarını laboratuvarda ispatlamak',
          explanation: 'Tarih insan faaliyetlerini inceler; doğa kanunlarını laboratuvarda denemek pozitif doğa bilimlerinin (fizik, kimya) alanıdır.'
        },
        {
          id: 'q_tar9_birey_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih biliminin araştırma yönteminde pozitif bilimlerden (fizik, kimya) en temel farkı nedir?',
          choices: [
            'Deney ve gözlem yapılamaması, olayların birebir tekrarlanamaz olması',
            'Hiçbir kanıt ve belgeye ihtiyaç duymaması',
            'Yalnızca geleceğe dair tahminlerde bulunması',
            'Matematiksel formüllerle kesin hesaplar yapılması'
          ],
          correctAnswer: 'Deney ve gözlem yapılamaması, olayların birebir tekrarlanamaz olması',
          explanation: 'Tarihi olaylar geçmişte yaşanıp bitmiştir; tekrarlanamaz ve laboratuvar ortamında deneyi yapılamaz.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_birey_toplum',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=5QxOpTALmEE',
      transcriptLanguage: 'tr',
      transcriptKind: 'auto',
      transcriptFingerprint: 'a6c06e699d659fd4',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },
  {
    id: 'item_tar9_vid_olay_olgu__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_gecmisin_insasi',
    stableKey: 'tar9_quiz_olay_olgu_micro',
    itemType: 'QUIZ',
    displayLabel: '1.2-Q',
    orderKey: 2500,
    title: 'Tarihin Doğası & Olay-Olgu Ayrımı Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarihin Doğası & Olay-Olgu Ayrımı Mikro Testi',
      questions: [
        {
          id: 'q_tar9_olay_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarihte "Olay" (Vak\'a) ile "Olgu" (Vakıa) arasındaki temel fark nedir?',
          choices: [
            'Olay kısa süreli ve somuttur; olgu ise uzun süreli, süreçsel ve soyuttur',
            'Olay sadece savaşları, olgu ise sadece barış antlaşmalarını kapsar',
            'Olaylar belgesiz incelenir, olgular ise sadece arkeolojiye dayanır',
            'Olaylar gelecekte gerçekleşir, olgular geçmişte kalmıştır'
          ],
          correctAnswer: 'Olay kısa süreli ve somuttur; olgu ise uzun süreli, süreçsel ve soyuttur',
          explanation: 'Tarihi olay (örn. Malazgirt Savaşı) kısa sürede başlayıp biten somut gelişmedir; olgu (örn. Anadolu\'nun Türkleşmesi) uzun bir süreçtir.'
        },
        {
          id: 'q_tar9_olay_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: '"1453 yılında İstanbul\'un Fethi" bir tarihi olay iken, "İslamiyet\'in yayılması" bir tarihi olgudur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'İstanbul\'un Fethi belirli bir tarihte gerçekleşen olaydır; İslamiyet\'in yayılması ise yüzyıllara yayılan bir olgudur.'
        },
        {
          id: 'q_tar9_olay_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi bir "Tarihi Olgu" örneğidir?',
          choices: [
            'Anadolu\'nun Türkleşmesi süreci',
            'Malazgirt Meydan Muharebesi (1071)',
            'Lozan Barış Antlaşması\'nın imzalanması',
            'Cumhuriyet\'in İlanı (29 Ekim 1923)'
          ],
          correctAnswer: 'Anadolu\'nun Türkleşmesi süreci',
          explanation: 'Anadolu\'nun Türkleşmesi uzun zamana yayılan, çok boyutlu ve genel bir süreç (olgu) niteliği taşır.'
        },
        {
          id: 'q_tar9_olay_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarihi olaylar incelenirken neden-sonuç zincirinde dikkat edilmesi gereken en önemli kural nedir?',
          choices: [
            'Bir olayın sonucunun, kendisinden sonraki olayın nedeni olabilmesi',
            'Tüm olayların tek bir dini nedene bağlanması',
            'Sonuçların nedenlerden önce gerçekleştiğinin varsayılması',
            'Olayların sadece kazanan devletin gözüyle değerlendirilmesi'
          ],
          correctAnswer: 'Bir olayın sonucunun, kendisinden sonraki olayın nedeni olabilmesi',
          explanation: 'Tarihsel determinizm ve süreklilik gereği, bir olayın sonucu sonraki gelişmelerin tetikleyicisi ve nedeni olur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_olay_olgu',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=5QxOpTALmEE',
      transcriptLanguage: 'tr',
      transcriptKind: 'auto',
      transcriptFingerprint: 'a6c06e699d659fd4',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },
  {
    id: 'item_tar9_vid_kaynak_turleri__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_kaynaklar_takvimler',
    stableKey: 'tar9_quiz_kaynak_turleri_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Tarihin Metodu ve Kaynak Türleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarihin Metodu ve Kaynak Türleri Mikro Testi',
      questions: [
        {
          id: 'q_tar9_kaynak_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih araştırmalarında "Birinci Elden (Ana) Kaynak" ne demektir?',
          choices: [
            'Olayın geçtiği döneme ait olan veya olaya bizzat tanıklık etmiş kaynaklar',
            'Olaydan yüzyıllar sonra yazılmış ders kitapları',
            'Tarihi roman ve sinema filmleri',
            'İnternet forumlarında paylaşılan yorumlar'
          ],
          correctAnswer: 'Olayın geçtiği döneme ait olan veya olaya bizzat tanıklık etmiş kaynaklar',
          explanation: 'Birinci elden kaynaklar olayın yaşandığı çağda oluşturulmuş kitabe, para, ferman, antlaşma metni ve hatırat gibi doğrudan belgelerdir.'
        },
        {
          id: 'q_tar9_kaynak_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih metodolojisinde "5T" yönteminin doğru sıralaması hangisidir?',
          choices: [
            'Tarama (Kaynak arama) -> Tasnif (Sınıflandırma) -> Tahlil (Çözümleme) -> Tenkit (Eleştiri) -> Terkip (Sentez)',
            'Tenkit -> Terkip -> Tasnif -> Tarama -> Tahlil',
            'Tasnif -> Tarama -> Terkip -> Tahlil -> Tenkit',
            'Tahlil -> Tenkit -> Tarama -> Tasnif -> Terkip'
          ],
          correctAnswer: 'Tarama (Kaynak arama) -> Tasnif (Sınıflandırma) -> Tahlil (Çözümleme) -> Tenkit (Eleştiri) -> Terkip (Sentez)',
          explanation: 'Tarih araştırması sırasıyla Tarama, Tasnif, Tahlil, Tenkit (iç/dış eleştiri) ve Terkip (birleştirme/yazım) aşamalarından oluşur.'
        },
        {
          id: 'q_tar9_kaynak_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Tarihi bir belgenin yazarının güvenirliği, yazılış yeri ve tarihinin doğrulanması "Dış Tenkit" aşamasında yapılır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Dış tenkitte eserin adı, yazarı, basım yeri ve orijinalliği incelenir; iç tenkitte ise metnin içeriğinin doğruluğu test edilir.'
        },
        {
          id: 'q_tar9_kaynak_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarihi olayları ders verme, ahlak ve vatan sevgisi aşılama amacıyla kahramanlıkları öne çıkararak anlatan tarih yazıcılığı türü hangisidir?',
          choices: [
            'Öğretici (Pragmatik) Tarih',
            'Rivayetçi (Hikâyeci) Tarih',
            'Kronik Tarih',
            'Sosyal Tarih'
          ],
          correctAnswer: 'Öğretici (Pragmatik) Tarih',
          explanation: 'Thukydides ile başlayan Öğretici/Pragmatik tarih, topluma fayda sağlama ve milli/ahlaki değerleri güçlendirme amacı güder.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_kaynak_turleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=lqEZ19uwyas',
      transcriptLanguage: 'tr',
      transcriptKind: 'auto',
      transcriptFingerprint: '53825f72d2e3bf29',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },
  {
    id: 'item_tar9_vid_yardimci_bilimler__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_kaynaklar_takvimler',
    stableKey: 'tar9_quiz_yardimci_bilimler_micro',
    itemType: 'QUIZ',
    displayLabel: '2.2-Q',
    orderKey: 2500,
    title: 'Tarihe Yardımcı Bilimler ve Takvimler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarihe Yardımcı Bilimler ve Takvimler Mikro Testi',
      questions: [
        {
          id: 'q_tar9_yardimci_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Eski paraları, sikkeleri ve madalyonları inceleyerek devletlerin ekonomik durumu ve hükümdarları hakkında bilgi veren yardımcı bilim dalı hangisidir?',
          choices: [
            'Nümizmatik (Meskukat)',
            'Paleografya',
            'Epigrafi',
            'Heraldik'
          ],
          correctAnswer: 'Nümizmatik (Meskukat)',
          explanation: 'Nümizmatik para ve sikkeleri inceler; hükümdar ismi, devletin gücü ve ekonomik refahı hakkında doğrudan bilgi sunar.'
        },
        {
          id: 'q_tar9_yardimci_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Türklerin tarih boyunca kullandığı takvimlerden hangisi "Ay Yılı" (354 gün) esasına dayanır?',
          choices: [
            'Hicri Takvim',
            '12 Hayvanlı Türk Takvimi',
            'Celali Takvim',
            'Miladi Takvim'
          ],
          correctAnswer: 'Hicri Takvim',
          explanation: 'Türklerin İslamiyet\'i kabulüyle kullanmaya başladığı Hicri takvim, ayın dünya etrafındaki dönüşünü (354 gün) esas alır.'
        },
        {
          id: 'q_tar9_yardimci_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Kitabeleri, anıtlar üzerindeki yazıtları ve mezar taşlarını inceleyen bilim dalı Epigrafi\'dir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Epigrafi (Yazıt Bilimi), taş ve metal kitabeler üzerindeki yazıları inceler; Orhun Abideleri bunun en somut örneğidir.'
        },
        {
          id: 'q_tar9_yardimci_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Büyük Selçuklu Sultanı Melikşah adına Ömer Hayyam başkanlığındaki heyet tarafından hazırlanan güneş yılı esaslı takvim hangisidir?',
          choices: [
            'Celali Takvimi',
            'Rumi Takvim',
            '12 Hayvanlı Türk Takvimi',
            'Hicri Takvim'
          ],
          correctAnswer: 'Celali Takvimi',
          explanation: 'Celali Takvimi, Büyük Selçuklu Devleti\'nde tarım ve mali işleri düzenlemek için Sultan Celaleddin Melikşah adına hazırlanmıştır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_yardimci_bilimler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=lqEZ19uwyas',
      transcriptLanguage: 'tr',
      transcriptKind: 'auto',
      transcriptFingerprint: '53825f72d2e3bf29',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },
  {
    id: 'item_tde9_vid_soz_sanatlari__quiz',
    courseId: 'course_tde_9',
    lessonId: 'lesson_tde9_metin_ve_anlam',
    stableKey: 'tde9_quiz_soz_sanatlari_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Edebiyatın Güzel Sanatlarla İlişkisi ve Malzemesi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Edebiyatın Güzel Sanatlarla İlişkisi ve Malzemesi Mikro Testi',
      questions: [
        {
          id: 'q_tde9_malzeme_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Edebiyatı müzik, resim ve heykel gibi diğer güzel sanat dallarından ayıran en temel fark nedir?',
          choices: [
            'Kullandığı malzemenin dil (sözcükler) olması',
            'İnsanda estetik zevk uyandırmayı amaçlaması',
            'Duygu ve düşüncelerden beslenmesi',
            'Kendi içinde içsel bir uyum taşıması'
          ],
          correctAnswer: 'Kullandığı malzemenin dil (sözcükler) olması',
          explanation: 'Tüm güzel sanatlar estetik amaç taşır; ancak edebiyatın ana malzemesi dildir (sözcükler), resmin boya, heykelin mermer/taş, müziğin sestir.'
        },
        {
          id: 'q_tde9_malzeme_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Edebiyat; insanın duygu, düşünce ve hayallerini sözün büyüsü veya yazının kalıcılığıyla estetik bir zevk uyandıracak şekilde ifade etme sanatıdır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Edebiyat, duygu ve düşüncelerin dille estetik biçimde aktarıldığı sözlü ve yazılı sanat dalıdır.'
        },
        {
          id: 'q_tde9_malzeme_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Güzel sanatlar ve edebiyatın ortak amacı aşağıdakilerden hangisidir?',
          choices: [
            'İnsanda güzellik duygusu, estetik haz ve coşku uyandırmak',
            'Bilimsel formülleri deneyle kanıtlamak',
            'Topluma sadece somut maddi kazanç sağlamak',
            'Tarihi belgeleri arşivlemek'
          ],
          correctAnswer: 'İnsanda güzellik duygusu, estetik haz ve coşku uyandırmak',
          explanation: 'Tüm sanat dallarının temel gayesi insanda estetik bir algı, güzellik duygusu ve içsel bir heyecan yaratmaktır.'
        },
        {
          id: 'q_tde9_malzeme_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Müzik sanatında notaların uyumu, edebiyat sanatında neyin uyumuna karşılık gelir?',
          choices: [
            'Sözcüklerin ve ahengin uyumuna',
            'Fırça darbelerinin renk tonuna',
            'Mermere verilen kütlesel şekle',
            'Sahne ışıklarının parlaklığına'
          ],
          correctAnswer: 'Sözcüklerin ve ahengin uyumuna',
          explanation: 'Müzikteki nota dizilimi gibi, edebiyatta da sözcüklerin ve seslerin ritmik/ahenkli dizilimi edebi değeri ortaya çıkarır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tde9_vid_soz_sanatlari',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=NBhw_SkvV8E',
      transcriptLanguage: 'tr',
      transcriptKind: 'auto',
      transcriptFingerprint: '26358ad4bf4d3465',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  }
];

function generateMicroQuizzes(v2Dir = path.resolve(__dirname, '../content/v2')) {
  const itemsDir = path.join(v2Dir, 'items');
  const lessonsDir = path.join(v2Dir, 'lessons');

  let totalNewQuestions = 0;

  for (const qData of microQuizzes) {
    const val = validateQuizSchema(qData.quiz);
    if (!val.valid) {
      throw new Error(`Quiz schema validation failed for ${qData.id}: ${val.errors.join(', ')}`);
    }

    const itemObj = {
      id: qData.id,
      courseId: qData.courseId,
      lessonId: qData.lessonId,
      stableKey: qData.stableKey,
      itemType: qData.itemType,
      displayLabel: qData.displayLabel,
      orderKey: qData.orderKey,
      title: qData.title,
      contentUrl: qData.contentUrl,
      publishingStatus: qData.publishingStatus,
      payload: {
        quiz: val.normalizedQuiz,
        provenance: qData.provenance
      }
    };

    const itemFilePath = path.join(itemsDir, `${qData.id}.json`);
    fs.writeFileSync(itemFilePath, JSON.stringify(itemObj, null, 2) + '\n', 'utf8');

    totalNewQuestions += val.normalizedQuiz.questions.length;

    // Update the lesson file to insert the quiz directly after its source video
    const lessonFilePath = path.join(lessonsDir, `${qData.lessonId}.json`);
    const lessonObj = JSON.parse(fs.readFileSync(lessonFilePath, 'utf8'));

    const items = lessonObj.items || [];
    if (!items.includes(qData.id)) {
      const vidIdx = items.indexOf(qData.provenance.derivedFromItemId);
      if (vidIdx !== -1) {
        items.splice(vidIdx + 1, 0, qData.id);
      } else {
        items.push(qData.id);
      }
      lessonObj.items = items;
      fs.writeFileSync(lessonFilePath, JSON.stringify(lessonObj, null, 2) + '\n', 'utf8');
    }
  }

  // Validate the modular tree
  const validation = validateModularTree(v2Dir);
  if (!validation.valid) {
    throw new Error(`Validation failed after micro-quiz injection: ${validation.errors.join('; ')}`);
  }

  // Compile and save monolithic catalog artifact
  const compiled = compileModularCatalog(v2Dir);
  const compiledPath = path.resolve(v2Dir, '../9-sinif-v2-catalog.json');
  const saveResult = saveCompiledCatalog(compiled, compiledPath);

  return {
    microQuizzesCreated: microQuizzes.length,
    totalNewQuestions,
    validation,
    compiledPath: saveResult.path,
    totalItems: saveResult.totalItems
  };
}

if (require.main === module) {
  const res = generateMicroQuizzes();
  console.log('✅ Micro-quizzes generated & compiled successfully:');
  console.log(JSON.stringify(res, null, 2));
}

module.exports = {
  microQuizzes,
  generateMicroQuizzes
};
