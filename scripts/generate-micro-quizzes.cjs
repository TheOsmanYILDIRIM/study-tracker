/**
 * Deterministic generation of transcript-grounded micro-quizzes in modular content/v2
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { validateQuizSchema } = require('../cli/lib/v2-quiz');
const { validateModularTree, compileModularCatalog, saveCompiledCatalog } = require('../cli/lib/v2-modular');

const microQuizzes = [
  // 1. Mat 9: Gerçek Sayı Aralıkları & Gösterim
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
      transcriptKind: 'subtitle',
      transcriptFingerprint: '8b626f71936a91a8',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 2. Mat 9: Aralık Farkı
  {
    id: 'item_mat9_vid_aralik_farki__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_araliklar_kumeler',
    stableKey: 'mat9_quiz_aralik_farki_micro',
    itemType: 'QUIZ',
    displayLabel: '2.2-Q',
    orderKey: 3500,
    title: 'Aralık Farkı ve Kesişimi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Aralık Farkı ve Kesişimi Mikro Testi',
      questions: [
        {
          id: 'q_mat9_arfark_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'A = [-3, 5) ve B = (1, 8] aralıkları veriliyor. A ∩ B kesişim kümesi aşağıdakilerden hangisidir?',
          choices: [
            '(1, 5)',
            '[1, 5)',
            '[-3, 8]',
            '(1, 8]'
          ],
          correctAnswer: '(1, 5)',
          explanation: 'Kesişim her iki aralığın ortak kısmıdır; sol sınırda 1 açık, sağ sınırda 5 açık olduğundan (1, 5) aralığıdır.'
        },
        {
          id: 'q_mat9_arfark_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'A = [-2, 6] ve B = [1, 4) aralıkları için A \\ B (A fark B) kümesinin gösterimi aşağıdakilerden hangisidir?',
          choices: [
            '[-2, 1) ∪ [4, 6]',
            '[-2, 1] ∪ (4, 6]',
            '[-2, 4]',
            '(1, 6]'
          ],
          correctAnswer: '[-2, 1) ∪ [4, 6]',
          explanation: 'B kümesindeki [1, 4) çıkarıldığında 1 noktası B\'de olduğu için A\\B\'de açık kalır, 4 noktası B\'de olmadığı için A\\B\'de kapalı kalır.'
        },
        {
          id: 'q_mat9_arfark_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'İki ayrık aralığın kesişim kümesi boş kümedir (∅).',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Ayrık kümelerin ve aralıkların ortak elemanı bulunmadığından kesişimleri boş kümedir.'
        },
        {
          id: 'q_mat9_arfark_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'R \\ [-4, 7) (Tüm gerçek sayılardan [-4, 7) aralığının çıkarılması) kümesi hangisidir?',
          choices: [
            '(-∞, -4) ∪ [7, ∞)',
            '(-∞, -4] ∪ (7, ∞)',
            '(-4, 7]',
            '(-∞, 7]'
          ],
          correctAnswer: '(-∞, -4) ∪ [7, ∞)',
          explanation: '-4 çıkarılan aralıkta dahil olduğu için geriye açık kalır; 7 çıkarılan aralıkta olmadığı için geriye kapalı [7 kalır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_aralik_farki',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=drPKmUSKYCI',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'fa715604932cb476',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 3. Tar 9: Birey ve Toplum
  {
    id: 'item_tar9_vid_birey_toplum__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_gecmisin_insasi',
    stableKey: 'tar9_quiz_birey_toplum_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Tarih, Birey ve Toplum Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarih, Birey ve Toplum Mikro Testi',
      questions: [
        {
          id: 'q_tar9_birey_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih biliminin konusu aşağıdakilerden hangisidir?',
          choices: [
            'Geçmişte yaşamış insan topluluklarının faaliyetleri, kültürleri ve birbirleriyle ilişkileri',
            'Yalnızca doğa olaylarının yeryüzündeki fiziksel sonuçları',
            'Gelecekte gerçekleşecek siyasi olayların kesin tahminleri',
            'Mitolojik efsanelerin ve kurgusal hikâyelerin edebi tahlili'
          ],
          correctAnswer: 'Geçmişte yaşamış insan topluluklarının faaliyetleri, kültürleri ve birbirleriyle ilişkileri',
          explanation: 'Tarih; geçmişteki insan faaliyetlerini yer ve zaman göstererek, belgelere dayanarak neden-sonuç ilişkisi içinde inceleyen sosyal bir bilimdir.'
        },
        {
          id: 'q_tar9_birey_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Tarihsel olaylar tekrarlanamaz ve laboratuvar ortamında deney-gözlem yöntemiyle incelenemez.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Tarih geçmişe ait olayları incelediği için fen bilimlerindeki gibi kontrollü deney veya gözlem yapılamaz; kaynak ve belgelere dayanılır.'
        },
        {
          id: 'q_tar9_birey_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih bilincinin bireye ve topluma kazandırdığı en temel fayda aşağıdakilerden hangisidir?',
          choices: [
            'Milli kimlik ve toplumsal hafıza oluşturarak geçmişten ders çıkarıp geleceği inşa etmek',
            'Tüm dünyada tek bir dil ve kültür oluşturmak',
            'Geçmişteki savaşları aynı şekilde yeniden yaşatmak',
            'Yalnızca kralların hayat hikâyelerini ezberlemek'
          ],
          correctAnswer: 'Milli kimlik ve toplumsal hafıza oluşturarak geçmişten ders çıkarıp geleceği inşa etmek',
          explanation: 'Tarih bilinci, bireyde aidiyet duygusu ve ortak hafıza oluşturarak toplumların bilinçli hareket etmesini sağlar.'
        },
        {
          id: 'q_tar9_birey_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih araştırmalarında olayların geçtiği dönemin koşullarını dikkate alarak günümüz yargılarıyla önyargılı yaklaşmama tutumuna ne ad verilir?',
          choices: [
            'Tarihsel empati ve anakronizmden kaçınma',
            'Determinizm',
            'Monarşik yaklaşım',
            'Pozitivist dogmatizm'
          ],
          correctAnswer: 'Tarihsel empati ve anakronizmden kaçınma',
          explanation: 'Tarihçi olayları değerlendirirken o dönemin şartlarını, zihniyetini ve değer yargılarını göz önüne almalıdır (anakronizm hatasından kaçınmalıdır).'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_birey_toplum',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Jm214v0e1qI',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '79f3890b713032ec',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 4. Tar 9: Olay ve Olgu
  {
    id: 'item_tar9_vid_olay_olgu__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_gecmisin_insasi',
    stableKey: 'tar9_quiz_olay_olgu_micro',
    itemType: 'QUIZ',
    displayLabel: '1.2-Q',
    orderKey: 3500,
    title: 'Tarihî Olay ve Olgu Karşılaştırması Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarihî Olay ve Olgu Karşılaştırması Mikro Testi',
      questions: [
        {
          id: 'q_tar9_olayolgu_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi "Tarihî Olay" (Vaka) kavramına bir örnektir?',
          choices: [
            'Malazgirt Meydan Muharebesi (1071)',
            'Anadolu\'nun Türkleşmesi süreci',
            'İslamiyet\'in yayılması',
            'Sanayileşme hareketleri'
          ],
          correctAnswer: 'Malazgirt Meydan Muharebesi (1071)',
          explanation: 'Olay; başlangıcı ve bitişi belli, kısa sürede gerçekleşen somut gelişmelerdir. Malazgirt Savaşı belirli bir tarihte gerçekleşmiş bir olaydır.'
        },
        {
          id: 'q_tar9_olayolgu_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarihî olguların en belirgin özelliği aşağıdakilerden hangisidir?',
          choices: [
            'Uzun bir süreçte meydana gelen, genel ve soyut değişimlerdir',
            'Günü ve saati tam olarak bilinen anlık patlamalardır',
            'Sadece tek bir kişinin kararıyla bir günde gerçekleşir',
            'Hiçbir zaman bir olayın sonucu olarak ortaya çıkmaz'
          ],
          correctAnswer: 'Uzun bir süreçte meydana gelen, genel ve soyut değişimlerdir',
          explanation: 'Olgu (vakıa); olayların sonucunda uzun vadede gerçekleşen, süreklilik gösteren genel tarihsel süreçlerdir.'
        },
        {
          id: 'q_tar9_olayolgu_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'İstanbul\'un Fethi bir "olay", bu fethin ardından Balkanlar\'ın fethi ve Osmanlı\'nın imparatorluk haline gelmesi ise bir "olgu"dur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: '1453 İstanbul Fethi somut bir olay; ardından gelişen imparatorluklaşma ve kültürel dönüşüm süreci bir olgudur.'
        },
        {
          id: 'q_tar9_olayolgu_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Olaylar biriciktir, tekrarlanamaz; olgular ise benzer şartlarda farklı coğrafyalarda tekrar yaşanabilir." ifadesi için ne söylenebilir?',
          choices: [
            'Kesinlikle doğrudur',
            'Tamamen yanlıştır, olaylar tekrarlanır',
            'Yalnızca ilkçağ tarihi için geçerlidir',
            'Tarihte olgu kavramı bulunmamaktadır'
          ],
          correctAnswer: 'Kesinlikle doğrudur',
          explanation: 'Tarihî olaylar kendine özgüdür ve tekrarlanamaz (örneğin Kurtuluş Savaşı); ancak milliyetçilik, kentleşme gibi olgular farklı yer ve zamanlarda tekrar edebilir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_olay_olgu',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Jm214v0e1qI',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '79f3890b713032ec',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 5. Tar 9: Kaynak Türleri
  {
    id: 'item_tar9_vid_kaynak_turleri__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_kaynaklar_takvimler',
    stableKey: 'tar9_quiz_kaynak_turleri_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Tarihî Kaynak Türleri ve Sınıflandırma Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarihî Kaynak Türleri ve Sınıflandırma Mikro Testi',
      questions: [
        {
          id: 'q_tar9_kaynak_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Olayın geçtiği döneme ait olan veya olayı bizzat yaşayan kişilerin bıraktığı belgelere ne ad verilir?',
          choices: [
            'Birinci elden (ana) kaynaklar',
            'İkinci elden kaynaklar',
            'Tarihsel romanlar',
            'Sözlü efsaneler'
          ],
          correctAnswer: 'Birinci elden (ana) kaynaklar',
          explanation: 'Olayın yaşandığı döneme ait ferman, kitabe, para, antlaşma metni gibi belgeler birinci elden kaynaktır.'
        },
        {
          id: 'q_tar9_kaynak_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi "Yazılı Kaynaklar" grubuna girer?',
          choices: [
            'Kadeş Barış Antlaşması tableti',
            'Tarih öncesi döneme ait cilalı taş balta',
            'Çömlek ve seramik kaplar',
            'Dede Korkut sözlü anlatıları'
          ],
          correctAnswer: 'Kadeş Barış Antlaşması tableti',
          explanation: 'Tablet üzerindeki çivi yazısı Kadeş Antlaşması\'nı yazılı bir tarihî kaynak yapar.'
        },
        {
          id: 'q_tar9_kaynak_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'İkinci elden kaynaklar, birinci elden kaynaklardan yararlanılarak olayın geçtiği dönemden sonra yazılan eserlerdir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Birinci el kaynakları inceleyerek yazılan modern tarih kitapları ve ansiklopediler ikinci elden kaynak niteliğindedir.'
        },
        {
          id: 'q_tar9_kaynak_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih öncesi (Prehistorik) devirlerin aydınlatılmasında en çok hangi kaynak türünden yararlanılır?',
          choices: [
            'Maddi (Arkeolojik / Kalıntı) kaynaklar',
            'Yazılı fermanlar ve kitabeler',
            'Resmî antlaşma metinleri',
            'Gazete ve dergi arşivleri'
          ],
          correctAnswer: 'Maddi (Arkeolojik / Kalıntı) kaynaklar',
          explanation: 'Tarih öncesi dönemde henüz yazı icat edilmediği için arkeolojik kazılarla çıkarılan maddi buluntular kullanılır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_kaynak_turleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=N8Z2h_r9qK0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '32ccc8dd260d8ebe',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 6. Tar 9: Yardımcı Bilimler
  {
    id: 'item_tar9_vid_yardimci_bilimler__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_kaynaklar_takvimler',
    stableKey: 'tar9_quiz_yardimci_bilimler_micro',
    itemType: 'QUIZ',
    displayLabel: '2.2-Q',
    orderKey: 3500,
    title: 'Tarihe Yardımcı Bilim Dalları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Tarihe Yardımcı Bilim Dalları Mikro Testi',
      questions: [
        {
          id: 'q_tar9_yardim_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Eski yazı çeşitlerini, alfabeleri ve bunların zaman içindeki değişimini inceleyen tarihe yardımcı bilim dalı hangisidir?',
          choices: [
            'Paleografya',
            'Nümizmatik',
            'Heraldik',
            'Epigrafi'
          ],
          correctAnswer: 'Paleografya',
          explanation: 'Paleografya eski yazı bilimidir. Epigrafi kitabeleri, Nümizmatik paraları inceler.'
        },
        {
          id: 'q_tar9_yardim_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Geçmişte basılmış madenî paraları (sikkeleri) inceleyerek dönemin ekonomik ve siyasi gücü hakkında bilgi veren bilim dalı hangisidir?',
          choices: [
            'Nümizmatik (Meskûkat)',
            'Kronoloji',
            'Etnografya',
            'Fitoloji'
          ],
          correctAnswer: 'Nümizmatik (Meskûkat)',
          explanation: 'Nümizmatik eski madenî paraları inceleyen bilim dalıdır.'
        },
        {
          id: 'q_tar9_yardim_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Anıtlar ve taş yazıtlar (Orhun Abideleri gibi) üzerindeki yazıları inceleyen bilim dalı hangisidir?',
          choices: [
            'Epigrafi (Kitabe Bilimi)',
            'Heraldik',
            'Antropoloji',
            'Filoloji'
          ],
          correctAnswer: 'Epigrafi (Kitabe Bilimi)',
          explanation: 'Epigrafi taş, mermer veya metal kitabeler üzerindeki yazıları inceleyen kitabe bilimidir.'
        },
        {
          id: 'q_tar9_yardim_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Devletlerin ve hanedanların armalarını, sembollerini inceleyen bilim dalı Heraldik\'tir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Heraldik arma bilimidir; devlet ve soylu aile armalarını inceler.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tar9_vid_yardimci_bilimler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=N8Z2h_r9qK0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '32ccc8dd260d8ebe',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 7. TDE 9: Söz Sanatları
  {
    id: 'item_tde9_vid_soz_sanatlari__quiz',
    courseId: 'course_tde_9',
    lessonId: 'lesson_tde9_metin_ve_anlam',
    stableKey: 'tde9_quiz_soz_sanatlari_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Edebî Sanatlar (Teşbih, İstiare, Teşhis) Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Edebî Sanatlar (Teşbih, İstiare, Teşhis) Mikro Testi',
      questions: [
        {
          id: 'q_tde9_sozsanat_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Ali aslan gibi cesurdur." cümlesindeki teşbih (benzetme) sanatında "aslan" hangi temel unsurdur?',
          choices: [
            'Kendisine benzetilen (güçlü unsur)',
            'Benzeyen (zayıf unsur)',
            'Benzetme yönü',
            'Benzetme edatı'
          ],
          correctAnswer: 'Kendisine benzetilen (güçlü unsur)',
          explanation: 'Teşbihte nitelikçe üstün ve güçlü olan varlığa kendisine benzetilen denir (aslan); benzeyen Ali\'dir.'
        },
        {
          id: 'q_tde9_sozsanat_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İnsan dışındaki canlı veya cansız varlıklara insani özellikler yükleme sanatına ne ad verilir?',
          choices: [
            'Teşhis (Kişileştirme)',
            'Tezat (Karşıtlık)',
            'Kinaye',
            'Tevriye'
          ],
          correctAnswer: 'Teşhis (Kişileştirme)',
          explanation: 'Teşhis (şahıslaştırma / kişileştirme) insan dışı varlıklara insana özgü nitelikler verme sanatıdır.'
        },
        {
          id: 'q_tde9_sozsanat_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Kişileştirme (teşhis) sanatının bulunduğu her yerde aynı zamanda kapalı istiare sanatı da vardır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Teşhis yapılan varlık insana benzetilip insan açıkça söylenmediği için her teşhis bir kapalı istiaredir.'
        },
        {
          id: 'q_tde9_sozsanat_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Birbirine zıt anlamlı kavram veya duyguların bir arada kullanılmasıyla oluşturulan edebî sanat hangisidir?',
          choices: [
            'Tezat (Zıtlık)',
            'Mecazımürsel',
            'Telmih',
            'Hüsnütalil'
          ],
          correctAnswer: 'Tezat (Zıtlık)',
          explanation: 'Tezat; aralarında zıtlık bulunan kavram ve durumların etkileyici bir anlatım için bir arada kullanılmasıdır (Örn: Ağlarım hatıra geldikçe gülüştüklerimiz).'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tde9_vid_soz_sanatlari',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=FqGgZz2-a-k',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'ce3abaffa6c6dffb',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T12:00:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 8. NEW: Fizik Giriş
  {
    id: 'item_fiz9_vid_fizik_giris__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_fizik_bilimine_giris',
    stableKey: 'fiz9_quiz_fizik_giris_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Fizik Bilimine Giriş ve Alt Dallar Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Fizik Bilimine Giriş ve Alt Dallar Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_giris_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Fiziğin alt dallarını hatırlamak için kullanılan "KAMYONET" kodlamasında "K" harfi hangi alt dalı temsil eder?',
          choices: [
            'Katıhal Fiziği',
            'Kuantum Mekaniği',
            'Kozmoloji',
            'Kimyasal Fizik'
          ],
          correctAnswer: 'Katıhal Fiziği',
          explanation: 'KAMYONET kodlaması: Katıhal, Atom, Mekanik, Yüksek enerji ve plazma, Optik, Nükleer, Elektromanyetizma, Termodinamik.'
        },
        {
          id: 'q_fiz9_giris_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Atom çekirdeğinin yapısını, radyoaktiviteyi, nükleer fisyon ve füzyon reaksiyonlarını inceleyen fiziğin alt dalı hangisidir?',
          choices: [
            'Nükleer Fizik (Çekirdek Fiziği)',
            'Atom Fiziği',
            'Katıhal Fiziği',
            'Termodinamik'
          ],
          correctAnswer: 'Nükleer Fizik (Çekirdek Fiziği)',
          explanation: 'Nükleer fizik atom çekirdeğindeki olayları (çekirdek reaksiyonları, radyasyon) inceler; Atom fiziği ise atomun yapısını ve elektron etkileşimlerini inceler.'
        },
        {
          id: 'q_fiz9_giris_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Isı, sıcaklık, iç enerji ve bunlar arasındaki enerji dönüşümlerini inceleyen fiziğin alt dalı Termodinamik\'tir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Termodinamik ısı ve sıcaklık ile ilgili tüm olayları ve ısı aktarım mekanizmalarını inceler.'
        },
        {
          id: 'q_fiz9_giris_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yarı iletken teknolojisi, nanoteknoloji, mikroçip ve güneş pillerinin geliştirilmesi öncelikli olarak fiziğin hangi alt dalının çalışma alanına girer?',
          choices: [
            'Katıhal Fiziği',
            'Optik',
            'Mekanik',
            'Yüksek Enerji ve Plazma Fiziği'
          ],
          correctAnswer: 'Katıhal Fiziği',
          explanation: 'Katıhal fiziği kristal ve katı maddelerin mikroskobik, elektriksel ve manyetik özelliklerini inceler.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_fizik_giris',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1Hg2eF907YA',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '950a6c9a5591fd65',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 9. NEW: Fizik Vektörler
  {
    id: 'item_fiz9_vid_vektorler__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_kuvvet_ve_hareket',
    stableKey: 'fiz9_quiz_vektorler_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Fiziksel Büyüklükler ve Vektörler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Fiziksel Büyüklükler ve Vektörler Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_vek_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Temel büyüklükleri kodlamak için kullanılan "KISAMUZ" kısaltmasında "S" harfi hangi büyüklüğü ifade eder ve SI birimi nedir?',
          choices: [
            'Sıcaklık - Kelvin',
            'Sıcaklık - Celcius',
            'Sürat - m/s',
            'Süre - Saniye'
          ],
          correctAnswer: 'Sıcaklık - Kelvin',
          explanation: 'KISAMUZ: Kütle(kg), Işık şiddeti(cd), Sıcaklık(Kelvin), Akım(Amper), Madde miktarı(mol), Uzunluk(m), Zaman(s). Sıcaklığın SI birimi Kelvin\'dir.'
        },
        {
          id: 'q_fiz9_vek_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir fiziksel büyüklüğün tanımlanabilmesi için sayı ve birimin yanında MUTLAKA yön ve doğrultunun da belirtilmesi gerekiyorsa bu büyüklük türü hangisidir?',
          choices: [
            'Vektörel Büyüklük',
            'Skaler Büyüklük',
            'Temel Büyüklük',
            'Türetilmiş Skaler Büyüklük'
          ],
          correctAnswer: 'Vektörel Büyüklük',
          explanation: 'Vektörel büyüklükler sayı, birim, doğrultu ve yön ile ifade edilen büyüklüklerdir (Örn: Kuvvet, hız, yer değiştirme, ağırlık).'
        },
        {
          id: 'q_fiz9_vek_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi SKALER bir büyüklüktür?',
          choices: [
            'Kütle',
            'Hız',
            'Kuvvet',
            'Yer değiştirme'
          ],
          correctAnswer: 'Kütle',
          explanation: 'Kütle yönü olmayan, yalnızca sayı ve birimle tam olarak tanımlanabilen skaler bir büyüklüktür.'
        },
        {
          id: 'q_fiz9_vek_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'İki vektörün eşit olabilmesi için doğrultularının, yönlerinin ve şiddetlerinin (büyüklüklerinin) tamamen aynı olması gerekir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Eşit vektörler yönü, doğrultusu ve büyüklüğü aynı olan vektörlerdir; başlangıç noktalarının farklı olması eşitliği bozmaz.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_vektorler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=7aVrdQ7uSQ4',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '532569ac8750adbf',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 10. NEW: Fizik Isı Öz Isı
  {
    id: 'item_fiz9_vid_topic_16_isi_oz_isi_ve_isi_sigasi__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_16_isi_oz_isi_ve_isi_sigasi',
    stableKey: 'fiz9_quiz_isi_oz_isi_micro',
    itemType: 'QUIZ',
    displayLabel: '16.1-Q',
    orderKey: 1500,
    title: 'Isı, Öz Isı ve Isı Sığası Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Isı, Öz Isı ve Isı Sığası Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_isikav_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '1 gram saf maddenin sıcaklığını 1 °C artırmak için verilmesi gereken ısı enerjisine ne ad verilir?',
          choices: [
            'Öz Isı (c)',
            'Isı Sığası (C)',
            'İç Enerji',
            'Erime Isısı'
          ],
          correctAnswer: 'Öz Isı (c)',
          explanation: 'Öz ısı (c) 1 gram maddenin sıcaklığını 1 °C değiştirmek için gereken ısıdır ve maddeler için ayırt edici özelliktir.'
        },
        {
          id: 'q_fiz9_isikav_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir maddenin kütlesi ile öz ısısının çarpımına (m · c) ne ad verilir ve bu büyüklük maddeler için ayırt edici midir?',
          choices: [
            'Isı Sığası (Kapasitesi) - Ayırt edici değildir',
            'Isı Sığası (Kapasitesi) - Ayırt edici bir özelliktir',
            'Öz Isı - Ayırt edici değildir',
            'İç Enerji - Ayırt edici bir özelliktir'
          ],
          correctAnswer: 'Isı Sığası (Kapasitesi) - Ayırt edici değildir',
          explanation: 'Isı sığası C = m · c formülüyle bulunur; kütleye bağlı olduğu için maddeler için ayırt edici bir özellik değildir.'
        },
        {
          id: 'q_fiz9_isikav_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Öz ısısı büyük olan maddeler, öz ısısı küçük olan maddelere göre daha geç ısınır ve daha geç soğur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Suyun öz ısısının yüksek olması nedeniyle denizlerin karalara göre geç ısınıp geç soğuması bu duruma örnektir.'
        },
        {
          id: 'q_fiz9_isikav_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Kütlesi m, öz ısısı c olan bir maddenin sıcaklığı ΔT kadar değiştirildiğinde aldığı veya verdiği ısı Q = m · c · ΔT formülü ile hesaplanır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Sıcaklık değişimi sırasında alınan/verilen ısı bağıntısı Q = m · c · ΔT\'dir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_16_isi_oz_isi_ve_isi_sigasi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Lj_tsZ3QKWM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '55aad1981b5f804a',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 11. NEW: Fizik Hâl Değişimi
  {
    id: 'item_fiz9_vid_topic_17_hal_degisimi__quiz',
    courseId: 'course_fiz_9',
    lessonId: 'lesson_fiz9_topic_17_hal_degisimi',
    stableKey: 'fiz9_quiz_hal_degisimi_micro',
    itemType: 'QUIZ',
    displayLabel: '17.1-Q',
    orderKey: 1500,
    title: 'Hâl Değişimi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Hâl Değişimi Mikro Testi',
      questions: [
        {
          id: 'q_fiz9_haldeg_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Saf bir maddenin hâl değişimi (erime, kaynama) süresince sıcaklığı nasıl değişir?',
          choices: [
            'Sabit kalır',
            'Sürekli artar',
            'Sürekli azalır',
            'Önce artar sonra azalır'
          ],
          correctAnswer: 'Sabit kalır',
          explanation: 'Saf maddeler hâl değiştirirken verilen enerji bağların koparılmasına (potansiyel enerjiye) harcanır, kinetik enerji ve sıcaklık sabit kalır.'
        },
        {
          id: 'q_fiz9_haldeg_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Erime sıcaklığındaki m gram katı bir maddenin tamamen sıvı hâle geçmesi için gereken ısı hangi formülle hesaplanır?',
          choices: [
            'Q = m · Le',
            'Q = m · c · ΔT',
            'Q = m · c',
            'Q = c · ΔT'
          ],
          correctAnswer: 'Q = m · Le',
          explanation: 'Hâl değişimi sırasında sıcaklık farkı olmadığından gereken ısı Q = m · L formülü ile hesaplanır (Le: erime ısısı).'
        },
        {
          id: 'q_fiz9_haldeg_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Saf maddeler için aynı dış basınç altında erime sıcaklığı ile donma sıcaklığı birbirine eşittir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Saf bir maddenin aynı basınç altında erime noktası ile donma noktası aynı sıcaklık değeridir (Örn: Su için 0 °C).'
        },
        {
          id: 'q_fiz9_haldeg_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yabancı madde eklenmesi (tuz atılması gibi) suyun donma noktasını nasıl etkiler?',
          choices: [
            'Donma noktasını düşürür (0 °C altına çeker)',
            'Donma noktasını yükseltir (0 °C üzerine çıkarır)',
            'Hiçbir etkisi olmaz',
            'Suyu anında buharlaştırır'
          ],
          correctAnswer: 'Donma noktasını düşürür (0 °C altına çeker)',
          explanation: 'Saf suya tuz veya antifriz ilavesi donma noktasını düşürerek kışın buzlanmayı geciktirir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_fiz9_vid_topic_17_hal_degisimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=BZ4muY8Bc1Y',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '751b67c63da7e410',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 12. NEW: Matematik Üslü Giriş
  {
    id: 'item_mat9_vid_uslu_giris__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_uslu_koklu',
    stableKey: 'mat9_quiz_uslu_giris_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Üslü Sayılara Giriş ve Temel Mantık Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Üslü Sayılara Giriş ve Temel Mantık Mikro Testi',
      questions: [
        {
          id: 'q_mat9_usgir_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '(-3)^4 ile -3^4 işlemlerinin sonuçları sırasıyla kaçtır?',
          choices: [
            '81 ve -81',
            '-81 ve 81',
            '81 ve 81',
            '-81 ve -81'
          ],
          correctAnswer: '81 ve -81',
          explanation: 'Parantezli çift kuvvet tabanın işaretini artı yapar (-3)^4 = +81; parantezsiz ifadede üs sadece 3\'e aittir -3^4 = -81\'dir.'
        },
        {
          id: 'q_mat9_usgir_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '(-1) sayısının 101. kuvveti ile 100. kuvvetinin toplamı [(-1)^101 + (-1)^100] kaçtır?',
          choices: [
            '0',
            '-2',
            '2',
            '1'
          ],
          correctAnswer: '0',
          explanation: '(-1)\'in tek kuvveti -1, çift kuvveti +1\'dir: (-1) + (+1) = 0.'
        },
        {
          id: 'q_mat9_usgir_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '5^3 ifadesinin açılımı ve değeri aşağıdakilerden hangisidir?',
          choices: [
            '5 · 5 · 5 = 125',
            '5 + 5 + 5 = 15',
            '3 · 3 · 3 · 3 · 3 = 243',
            '5 · 3 = 15'
          ],
          correctAnswer: '5 · 5 · 5 = 125',
          explanation: 'a^n ifadesi n tane a\'nın yan yana çarpımıdır. 5^3 = 5 · 5 · 5 = 125\'tir.'
        },
        {
          id: 'q_mat9_usgir_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Negatif bir gerçek sayının tek kuvvetleri daima negatiftir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Negatif tabanın tek sayıda çarpımı daima negatif sonuç verir (Örn: (-2)^3 = -8).'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_uslu_giris',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=ZAqla3j4kLQ',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'ec31c704e8e95f10',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 13. NEW: Matematik Üslü Kurallar
  {
    id: 'item_mat9_vid_uslu_kurallar__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_uslu_koklu',
    stableKey: 'mat9_quiz_uslu_kurallar_micro',
    itemType: 'QUIZ',
    displayLabel: '1.2-Q',
    orderKey: 2500,
    title: 'Üslü İfadelerde Çarpma ve Taban Kuralları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Üslü İfadelerde Çarpma ve Taban Kuralları Mikro Testi',
      questions: [
        {
          id: 'q_mat9_uskur_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tabanları aynı olan üslü sayılar çarpılırken (a^x · a^y) sonuç nasıl bulunur?',
          choices: [
            'Taban aynı kalır, üsler toplanır (a^(x+y))',
            'Tabanlar çarpılır, üsler toplanır',
            'Taban aynı kalır, üsler çarpılır (a^(x·y))',
            'Tabanlar toplanır, üs aynı kalır'
          ],
          correctAnswer: 'Taban aynı kalır, üsler toplanır (a^(x+y))',
          explanation: 'Tabanları aynı olan üslü ifadeler çarpılırken üsler cebirsel olarak toplanır.'
        },
        {
          id: 'q_mat9_uskur_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '2^5 · 3^5 çarpımının eşiti aşağıdakilerden hangisidir?',
          choices: [
            '6^5',
            '6^10',
            '5^5',
            '6^25'
          ],
          correctAnswer: '6^5',
          explanation: 'Üsleri eşit olan üslü ifadeler çarpılırken tabanlar çarpılır ve ortak üs yazılır: (2 · 3)^5 = 6^5.'
        },
        {
          id: 'q_mat9_uskur_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '(2^3)^4 üssün üssü ifadesinin değeri 2\'nin kaçıncı kuvvetidir?',
          choices: [
            '2^12',
            '2^7',
            '2^81',
            '2^64'
          ],
          correctAnswer: '2^12',
          explanation: 'Üssün üssü alınırken üsler birbiriyle çarpılır: (a^x)^y = a^(x·y) olduğundan 3 · 4 = 12, yani 2^12\'dir.'
        },
        {
          id: 'q_mat9_uskur_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: '(3^4)^2 ile 3^(4^2) ifadeleri aynı matematiksel değere sahiptir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: '(3^4)^2 = 3^8 iken 3^(4^2) = 3^16\'dır; kuvvet parantezsiz kule şeklinde yazıldığında yukarıdan aşağıya üs alınır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_uslu_kurallar',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=-OYMlnoW9gw',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'b7b70dc9fcea4c1f',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 14. NEW: Matematik Üslü Bölme
  {
    id: 'item_mat9_vid_uslu_bolme__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_uslu_koklu',
    stableKey: 'mat9_quiz_uslu_bolme_micro',
    itemType: 'QUIZ',
    displayLabel: '1.3-Q',
    orderKey: 3500,
    title: 'Üslü Sayılarda Bölme İşlemi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Üslü Sayılarda Bölme İşlemi Mikro Testi',
      questions: [
        {
          id: 'q_mat9_usbol_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tabanları aynı olan üslü sayılar bölünürken (a^x / a^y) üsler arasında hangi işlem yapılır?',
          choices: [
            'Payın üssünden paydanın üssü çıkarılır (a^(x-y))',
            'Pay ve paydanın üsleri toplanır',
            'Üsler birbirine bölünür',
            'Tabanlar birbirinden çıkarılır'
          ],
          correctAnswer: 'Payın üssünden paydanın üssü çıkarılır (a^(x-y))',
          explanation: 'Tabanları aynı olan üslü sayılarda bölme işlemi yapılırken payın üssünden paydanın üssü çıkarılır.'
        },
        {
          id: 'q_mat9_usbol_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '(3^10) / (3^4) işleminin sonucu kaçtır?',
          choices: [
            '3^6',
            '3^14',
            '1^6',
            '3^(2.5)'
          ],
          correctAnswer: '3^6',
          explanation: '3^(10 - 4) = 3^6\'dır.'
        },
        {
          id: 'q_mat9_usbol_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '(10^7) / (5^7) işleminin sonucu aşağıdakilerden hangisidir?',
          choices: [
            '2^7',
            '2^1',
            '2^14',
            '5^7'
          ],
          correctAnswer: '2^7',
          explanation: 'Üsleri aynı olan ifadeler bölünürken tabanlar bölünür ve ortak üs yazılır: (10 / 5)^7 = 2^7.'
        },
        {
          id: 'q_mat9_usbol_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '4^8 / 2^10 işleminin sonucu kaçtır?',
          choices: [
            '2^6 = 64',
            '2^8 = 256',
            '2^2 = 4',
            '2^14'
          ],
          correctAnswer: '2^6 = 64',
          explanation: '4^8 = (2^2)^8 = 2^16. Buradan (2^16) / (2^10) = 2^(16-10) = 2^6 = 64 bulunur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_uslu_bolme',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=956XViFFkOk',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '72a42480cb471e64',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 15. NEW: Matematik Üslü Negatif
  {
    id: 'item_mat9_vid_uslu_negatif__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_uslu_koklu',
    stableKey: 'mat9_quiz_uslu_negatif_micro',
    itemType: 'QUIZ',
    displayLabel: '1.4-Q',
    orderKey: 4500,
    title: 'Sıfır ve Negatif Üs Kuralları Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Sıfır ve Negatif Üs Kuralları Mikro Testi',
      questions: [
        {
          id: 'q_mat9_usneg_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sıfırdan farklı bir gerçek sayının sıfırıncı kuvveti (a^0) kaça eşittir?',
          choices: [
            '1',
            '0',
            'a',
            '-1'
          ],
          correctAnswer: '1',
          explanation: 'Sıfır hariç tüm gerçek sayıların sıfırıncı kuvveti 1\'dir (a^0 = 1, a ≠ 0).'
        },
        {
          id: 'q_mat9_usneg_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '2^(-3) ifadesinin değeri kaçtır?',
          choices: [
            '1/8',
            '-8',
            '-6',
            '1/6'
          ],
          correctAnswer: '1/8',
          explanation: 'a^(-n) = 1 / (a^n) kuralından 2^(-3) = 1 / (2^3) = 1/8\'dir.'
        },
        {
          id: 'q_mat9_usneg_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '(2/3)^(-2) kesirli üslü ifadesinin eşiti nedir?',
          choices: [
            '9/4',
            '4/9',
            '-4/9',
            '-9/4'
          ],
          correctAnswer: '9/4',
          explanation: 'Negatif üs kesri çarpmaya göre ters çevirir: (2/3)^(-2) = (3/2)^2 = 9/4.'
        },
        {
          id: 'q_mat9_usneg_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Negatif bir üs (a^(-n)), tabandaki sayının işaretini her zaman negatife çevirir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Negatif üs sayının işaretini eksi yapmaz; sadece sayıyı çarpmaya göre tersine çevirir (takla attırır).'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_uslu_negatif',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=uwl25uvO1sQ',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '71eb5b21eb0dd86f',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 16. NEW: Matematik Köklü Mantık
  {
    id: 'item_mat9_vid_koklu_mantik__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_uslu_koklu',
    stableKey: 'mat9_quiz_koklu_mantik_micro',
    itemType: 'QUIZ',
    displayLabel: '1.5-Q',
    orderKey: 5500,
    title: 'Köklü Sayı Mantığı ve Tanım Kümesi Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Köklü Sayı Mantığı ve Tanım Kümesi Mikro Testi',
      questions: [
        {
          id: 'q_mat9_kokman_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Derecesi yazılmayan bir köklü ifadede (√x) kök derecesi varsayılan olarak kaçtır?',
          choices: [
            '2 (Karekök)',
            '1',
            '0',
            '10'
          ],
          correctAnswer: '2 (Karekök)',
          explanation: 'Kök derecesi belirtilmediğinde kök derecesi 2 kabul edilir (karekök).'
        },
        {
          id: 'q_mat9_kokman_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Çift dereceli bir kökün (örneğin ⁴√x) gerçek sayılar kümesinde tanımlı olabilmesi için kök içindeki ifade x ne olmalıdır?',
          choices: [
            'Sıfır veya sıfırdan büyük (x ≥ 0)',
            'Kesinlikle negatif olmalıdır (x < 0)',
            'Yalnızca tek sayılar olmalıdır',
            'Yalnızca irrasyonel olmalıdır'
          ],
          correctAnswer: 'Sıfır veya sıfırdan büyük (x ≥ 0)',
          explanation: 'Gerçek sayılarda çift dereceli köklerin içi negatif olamaz; x ≥ 0 olmalıdır.'
        },
        {
          id: 'q_mat9_kokman_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Tek dereceli köklerin (örneğin ∛(-8)) içi negatif gerçek sayı olabilir ve sonuç bir gerçek sayıdır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Tek dereceli köklerin içi negatif olabilir; (-2)^3 = -8 olduğundan ∛(-8) = -2\'dir.'
        },
        {
          id: 'q_mat9_kokman_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '√(x^2) ifadesinin gerçek sayılardaki tam karşılığı aşağıdakilerden hangisidir?',
          choices: [
            '|x| (Mutlak Değer x)',
            'x',
            '-x',
            'x^2'
          ],
          correctAnswer: '|x| (Mutlak Değer x)',
          explanation: 'Çift dereceli kök dışına çıkan ifadeler pozitifliği garanti altına almak için mutlak değer içinde çıkar: √(x^2) = |x|.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_koklu_mantik',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=k9Gx8yy6xSo',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'c81aea17231c58a8',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 17. NEW: Matematik Rasyonel Üsler
  {
    id: 'item_mat9_vid_rasyonel_usler__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_uslu_koklu',
    stableKey: 'mat9_quiz_rasyonel_usler_micro',
    itemType: 'QUIZ',
    displayLabel: '1.6-Q',
    orderKey: 6500,
    title: 'Rasyonel Üsler ve Kökten Çıkarma Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Rasyonel Üsler ve Kökten Çıkarma Mikro Testi',
      questions: [
        {
          id: 'q_mat9_rasus_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'x^(a/b) rasyonel üslü ifadesi köklü biçimde nasıl yazılır?',
          choices: [
            'b. dereceden kök (x^a)',
            'a. dereceden kök (x^b)',
            'x^(a·b)',
            'b · √x^a'
          ],
          correctAnswer: 'b. dereceden kök (x^a)',
          explanation: 'Rasyonel üste pay içerideki üssü, payda ise kök derecesini belirtir: x^(a/b) = ᵇ√(x^a).'
        },
        {
          id: 'q_mat9_rasus_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '√50 sayısı a√b biçiminde kök dışına çıkarıldığında eşiti nedir?',
          choices: [
            '5√2',
            '2√5',
            '25√2',
            '10√5'
          ],
          correctAnswer: '5√2',
          explanation: '50 = 25 · 2 = 5^2 · 2 olduğundan √50 = 5√2 olarak kök dışına çıkar.'
        },
        {
          id: 'q_mat9_rasus_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '√72 ifadesinin a√b biçimindeki en sade yazılışı hangisidir?',
          choices: [
            '6√2',
            '3√8',
            '2√18',
            '8√3'
          ],
          correctAnswer: '6√2',
          explanation: '72 = 36 · 2 = 6^2 · 2 olduğundan √72 = 6√2\'dir.'
        },
        {
          id: 'q_mat9_rasus_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '∛54 (3. dereceden kök 54) ifadesinin kök dışına çıkarılmış biçimi hangisidir?',
          choices: [
            '3 ∛2',
            '2 ∛3',
            '9 ∛6',
            '6 ∛3'
          ],
          correctAnswer: '3 ∛2',
          explanation: '54 = 27 · 2 = 3^3 · 2 olduğundan ∛54 = 3 ∛2\'dir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_rasyonel_usler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=bXlsejMNF5w',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '701580fd7642654a',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 18. NEW: Matematik Sayı Kümeleri
  {
    id: 'item_mat9_vid_sayi_kumeleri__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_sayilar_islem_ozellikleri',
    stableKey: 'mat9_quiz_sayi_kumeleri_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Sayı Kümeleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Sayı Kümeleri Mikro Testi',
      questions: [
        {
          id: 'q_mat9_saykum_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Doğal sayılar kümesi (N) ile Sayma sayıları kümesi (N+) arasındaki tek fark hangi elemandır?',
          choices: [
            '0 (Sıfır)',
            '1',
            '-1',
            'Sonsuz'
          ],
          correctAnswer: '0 (Sıfır)',
          explanation: 'Doğal sayılar 0\'dan başlar (N = {0, 1, 2, ...}), sayma sayıları ise 1\'den başlar.'
        },
        {
          id: 'q_mat9_saykum_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İki tam sayının oranı şeklinde (a/b, b ≠ 0) yazılamayan, virgülden sonra devretmeksizin sonsuza giden sayılara ne ad verilir?',
          choices: [
            'İrrasyonel Sayılar (Q\' / I)',
            'Rasyonel Sayılar (Q)',
            'Tam Sayılar (Z)',
            'Doğal Sayılar (N)'
          ],
          correctAnswer: 'İrrasyonel Sayılar (Q\' / I)',
          explanation: 'π sayısı, e sayısı ve tam kare olmayan köklü sayılar (√2, √3) irrasyonel sayılardır.'
        },
        {
          id: 'q_mat9_saykum_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Gerçek (Reel) sayılar kümesi (R), rasyonel sayılar (Q) ile irrasyonel sayıların (Q\') birleşiminden oluşur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'R = Q ∪ Q\' olup sayı doğrusundaki tüm noktaları eksiksiz doldurur.'
        },
        {
          id: 'q_mat9_saykum_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: '√4 sayısı irrasyonel bir sayıdır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: '√4 = 2 olduğundan doğal, tam ve rasyonel bir sayıdır; irrasyonel değildir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_sayi_kumeleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=1GHVanjXGpU',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '426e959c263c3dbc',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 19. NEW: Matematik Algoritma Temelleri
  {
    id: 'item_mat9_vid_algoritma_temelleri__quiz',
    courseId: 'course_mat_9',
    lessonId: 'lesson_mat9_topic_14_algoritma_temelli_problemler',
    stableKey: 'mat9_quiz_algoritma_temelleri_micro',
    itemType: 'QUIZ',
    displayLabel: '14.1-Q',
    orderKey: 1500,
    title: 'Algoritma ve Akış Şeması Temelleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Algoritma ve Akış Şeması Temelleri Mikro Testi',
      questions: [
        {
          id: 'q_mat9_algo_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir problemin çözümü için tasarlanan, başlangıcı ve bitişi net olan adım adım işlem basamaklarına ne ad verilir?',
          choices: [
            'Algoritma',
            'Aksiyom',
            'Teorem',
            'Hipotez'
          ],
          correctAnswer: 'Algoritma',
          explanation: 'Algoritma belirli bir görevi yerine getirmek veya bir problemi çözmek için tanımlanan sonlu işlem basamakları dizisidir.'
        },
        {
          id: 'q_mat9_algo_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Akış şemalarında (Flowchart) "Karar / Koşul / Karşılaştırma" (Örn: Sayı > 0 mı?) işlemlerini göstermek için hangi geometrik şekil kullanılır?',
          choices: [
            'Eşkenar Dörtgen (Baklava Dilimi)',
            'Dikdörtgen',
            'Oval (Elips)',
            'Paralelkenar'
          ],
          correctAnswer: 'Eşkenar Dörtgen (Baklava Dilimi)',
          explanation: 'Akış şemalarında eşkenar dörtgen şart ve karar kutusudur; dikdörtgen işlem, oval ise başla/bitir için kullanılır.'
        },
        {
          id: 'q_mat9_algo_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Akış şemasında "Başla" ve "Bitir" adımlarını temsil etmek için elips / oval şekli kullanılır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Algoritmanın başlangıç ve bitiş noktaları oval (elips) sembolü ile gösterilir.'
        },
        {
          id: 'q_mat9_algo_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Algoritmada bir işlemin belirli bir şart sağlanana kadar tekrar tekrar yürütülmesini sağlayan yapıya ne ad verilir?',
          choices: [
            'Döngü (Loop)',
            'Atama',
            'Değişken',
            'Fonksiyon'
          ],
          correctAnswer: 'Döngü (Loop)',
          explanation: 'Döngü yapısı koşula bağlı tekrarlı işlemleri yöneten temel algoritma yapısıdır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_mat9_vid_algoritma_temelleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=WJXe0YVy8X0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '9b90a8e7ecbc81a2',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 20. NEW: Kimya Bilimi
  {
    id: 'item_kim9_vid_kimya_bilimi__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_kimya_bilimi_guvenlik',
    stableKey: 'kim9_quiz_kimya_bilimi_micro',
    itemType: 'QUIZ',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Kimya Disiplinleri ve Güvenlik Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Kimya Disiplinleri ve Güvenlik Mikro Testi',
      questions: [
        {
          id: 'q_kim9_giris_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Simyanın (Alşimi) bir bilim dalı kabul edilmemesinin en temel nedeni aşağıdakilerden hangisidir?',
          choices: [
            'Teorik temellerinin olmaması ve sınama-yanılmaya dayanması',
            'Hiçbir araç gereç geliştirmemiş olması',
            'Yalnızca metallerle uğraşması',
            'Deneylerinde ateş kullanması'
          ],
          correctAnswer: 'Teorik temellerinin olmaması ve sınama-yanılmaya dayanması',
          explanation: 'Simya sistematik bilgi birikimi içermeyen ve teorik temellere dayanmayan bir uğraştır.'
        },
        {
          id: 'q_kim9_giris_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir su numunesindeki minerallerin türünü (nitel) ve miktarını (nicel) analiz eden kimya disiplini hangisidir?',
          choices: [
            'Analitik Kimya',
            'Organik Kimya',
            'Fizikokimya',
            'Biyokimya'
          ],
          correctAnswer: 'Analitik Kimya',
          explanation: 'Analitik kimya maddelerin kimyasal bileşenlerini nitel ve nicel olarak belirler.'
        },
        {
          id: 'q_kim9_giris_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Canlı organizmaların yapısındaki kimyasal maddeleri ve kimyasal süreçleri inceleyen disiplin Biyokimya\'dır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Canlı kimyasını, enzim ve protein etkileşimlerini inceleyen alan biyokimyadır.'
        },
        {
          id: 'q_kim9_giris_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Laboratuvarda üzerinde alev ve ortasında daire (O harfi) bulunan güvenlik uyarı işareti ne anlama gelir?',
          choices: [
            'Yakıcı / Oksitleyici Madde',
            'Yanıcı Madde',
            'Radyoaktif Madde',
            'Zehirli / Toksik Madde'
          ],
          correctAnswer: 'Yakıcı / Oksitleyici Madde',
          explanation: 'Üzerinde \'O\' harfi bulunan alev sembolü oksitleyici (yakıcı) maddeleri belirtir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_kimya_bilimi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Fj22h84a9qL',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'e17c0606ddfc4fc4',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 21. NEW: Kimya Atom Modelleri
  {
    id: 'item_kim9_vid_atom_modelleri__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_atom_ve_periyodik_sistem',
    stableKey: 'kim9_quiz_atom_modelleri_micro',
    itemType: 'QUIZ',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Atom Modelleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Atom Modelleri Mikro Testi',
      questions: [
        {
          id: 'q_kim9_atmod_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Atomu pozitif yüklü bir küre içinde homojen dağılmış elektronlardan oluşan "Üzümlü Kek"e benzeten bilim insanı kimdir?',
          choices: [
            'J. J. Thomson',
            'John Dalton',
            'Ernest Rutherford',
            'Niels Bohr'
          ],
          correctAnswer: 'J. J. Thomson',
          explanation: 'Thomson modeli üzümlü kek olarak bilinir; kek pozitif yükü, üzümler elektronları temsil eder.'
        },
        {
          id: 'q_kim9_atmod_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İnce altın levhaya alfa tanecikleri göndererek atomun büyük kısmının boşluk olduğunu ve pozitif yükün merkezdeki çekirdekte toplandığını keşfeden kimdir?',
          choices: [
            'Ernest Rutherford',
            'John Dalton',
            'Niels Bohr',
            'James Chadwick'
          ],
          correctAnswer: 'Ernest Rutherford',
          explanation: 'Rutherford alfa saçılması deneyiyle atom çekirdeğini keşfetmiş ve gezegen modelini önermiştir.'
        },
        {
          id: 'q_kim9_atmod_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Bohr atom modeline göre elektronlar çekirdek etrafında rastgele değil, belirli enerji düzeylerine (katman/yörünge) sahip dairesel yörüngelerde dolanır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Bohr modeli dairesel yörünge ve kuantize enerji seviyeleri kavramını getirmiştir.'
        },
        {
          id: 'q_kim9_atmod_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Dalton atom modelinde izotop atomların varlığı doğru bir şekilde açıklanmıştır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Dalton "bir elementin tüm atomları kütlece ve hacimce özdeştir" diyerek yanılmıştır; izotop kavramı o dönem bilinmiyordu.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_atom_modelleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Fj22h84a9qL',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '7cd92b2c6669ff39',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 22. NEW: Kimya İyonik Bağ
  {
    id: 'item_kim9_vid_topic_08_iyonik_bag__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_08_iyonik_bag',
    stableKey: 'kim9_quiz_iyonik_bag_micro',
    itemType: 'QUIZ',
    displayLabel: '8.1-Q',
    orderKey: 1500,
    title: 'İyonik Bağ Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'İyonik Bağ Mikro Testi',
      questions: [
        {
          id: 'q_kim9_iyonik_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İyonik bağ temel olarak hangi tür element atomları arasında ve hangi yolla oluşur?',
          choices: [
            'Metal ve ametal atomları arasında elektron alışverişiyle',
            'Ametal atomları arasında elektron ortaklaşmasıyla',
            'İki metal atomu arasında serbest elektron deniziyle',
            'Soygaz atomları arasında'
          ],
          correctAnswer: 'Metal ve ametal atomları arasında elektron alışverişiyle',
          explanation: 'Metal elektron vererek katyon, ametal elektron alarak anyon olur; oluşan elektrostatik çekim iyonik bağı kurar.'
        },
        {
          id: 'q_kim9_iyonik_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İyonik bileşiklerin fiziksel özellikleri hakkında hangisi DOĞRUDUR?',
          choices: [
            'Katı hâlde elektriği iletmezler, sıvı hâlde ve sulu çözeltilerinde iletirler',
            'Oda koşullarında daima sıvı hâldedirler',
            'Bağımsız moleküllerden oluşurlar',
            'Erime noktaları son derece düşüktür'
          ],
          correctAnswer: 'Katı hâlde elektriği iletmezler, sıvı hâlde ve sulu çözeltilerinde iletirler',
          explanation: 'İyonik katılarda iyonlar sabit örgüdedir; eriyik veya sulu çözeltide serbest hareket edebildiklerinden elektriği iletirler.'
        },
        {
          id: 'q_kim9_iyonik_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'İyonik bileşikler bağımsız moleküller hâlinde değil, düzenli kristal örgü yapısında bulunurlar.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'NaCl gibi iyonik katılar moleküler yapılı olmayıp birim hücrelerden oluşan kristal örgüye sahiptir.'
        },
        {
          id: 'q_kim9_iyonik_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'İyonik katılar sert fakat kırılgan bir yapıya sahiptir; darbe aldığında aynı yüklü iyonlar karşı karşıya gelerek kristali kırar.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'İyonik kristaller tel ve levha haline getirilemez, darbe aldığında kırılırlar.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_08_iyonik_bag',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Jm3KkQ1p4jM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'e0e330ee30a12087',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 23. NEW: Kimya Kovalent Bağ
  {
    id: 'item_kim9_vid_topic_09_kovalent_bag__quiz',
    courseId: 'course_kim_9',
    lessonId: 'lesson_kim9_topic_09_kovalent_bag',
    stableKey: 'kim9_quiz_kovalent_bag_micro',
    itemType: 'QUIZ',
    displayLabel: '9.1-Q',
    orderKey: 1500,
    title: 'Kovalent Bağ Türleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Kovalent Bağ Türleri Mikro Testi',
      questions: [
        {
          id: 'q_kim9_kov_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kovalent bağın oluşum mekanizması aşağıdakilerden hangisidir?',
          choices: [
            'Ametal atomlarının değerlik elektronlarını ortaklaşa kullanması',
            'Metal ve ametal arasında elektron alışverişi',
            'Soygazların elektron alması',
            'Proton transferi'
          ],
          correctAnswer: 'Ametal atomlarının değerlik elektronlarını ortaklaşa kullanması',
          explanation: 'Kovalent bağ ametallerin dublet veya oktete ulaşmak için elektronlarını ortak kullanmasıyla oluşur.'
        },
        {
          id: 'q_kim9_kov_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aynı iki ametal atomu arasında (örneğin O2, N2, Cl2) oluşan kovalent bağ türü hangisidir?',
          choices: [
            'Apolar Kovalent Bağ',
            'Polar Kovalent Bağ',
            'İyonik Bağ',
            'Hidrojen Bağı'
          ],
          correctAnswer: 'Apolar Kovalent Bağ',
          explanation: 'Aynı atomlar arasında elektronegatiflik farkı sıfır olduğundan kutupsuz (apolar) kovalent bağ oluşur.'
        },
        {
          id: 'q_kim9_kov_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Farklı ametal atomları arasında elektronların eşit olmayan çekimiyle oluşan bağa Polar Kovalent Bağ denir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'H2O, HCl, NH3 gibi farklı ametal atomları içeren moleküllerdeki bağlar polar kovalenttir.'
        },
        {
          id: 'q_kim9_kov_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Kovalent bağlı bileşikler bağımsız moleküler yapılara sahiptirler ve katı halde iyonik örgü oluşturmazlar.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Su (H2O), karbondioksit (CO2) gibi kovalent bileşikler moleküler yapılıdır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_kim9_vid_topic_09_kovalent_bag',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=Jm3KkQ1p4jM',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '895c0a3f6faf371f',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 24. NEW: Biyoloji Canlıların Ortak Özellikleri
  {
    id: 'item_biyo9_vid_topic_03_canlilarin_ortak_ozellikleri__quiz',
    courseId: 'course_biyo_9',
    lessonId: 'lesson_biyo9_topic_03_canlilarin_ortak_ozellikleri',
    stableKey: 'biyo9_quiz_canlilarin_ortak_ozellikleri_micro',
    itemType: 'QUIZ',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Canlıların Ortak Özellikleri Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Canlıların Ortak Özellikleri Mikro Testi',
      questions: [
        {
          id: 'q_biyo9_ortak_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Canlıların değişen çevre koşullarına rağmen iç ortamlarını dengede ve kararlı tutma yeteneğine ne ad verilir?',
          choices: [
            'Homeostazi',
            'Anabolizma',
            'Adaptasyon',
            'Katabolizma'
          ],
          correctAnswer: 'Homeostazi',
          explanation: 'Homeostazi canlının iç dengesini kararlı tutmasıdır (vücut ısısı, kan şekeri, su dengesi vb.).'
        },
        {
          id: 'q_biyo9_ortak_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi istisnasız TÜM canlı hücrelerde bulunan ortak bir hücresel yapıdır?',
          choices: [
            'Ribozom',
            'Çekirdek',
            'Mitokondri',
            'Kloroplast'
          ],
          correctAnswer: 'Ribozom',
          explanation: 'Ribozom zarsız bir organel olup tüm prokaryot ve ökaryot hücrelerde protein sentezini gerçekleştirir.'
        },
        {
          id: 'q_biyo9_ortak_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Canlılarda gerçekleşen yapım (özümleme/anabolizma) ve yıkım (yadımlama/katabolizma) olaylarının tamamına Metabolizma denir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Metabolizma canlıdaki tüm biyokimyasal reaksiyonların toplamıdır.'
        },
        {
          id: 'q_biyo9_ortak_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Üreme olayı bir bireyin yaşamını sürdürebilmesi için zorunlu bir olaydır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'FALSE',
          explanation: 'Üreme bireyin yaşaması için değil, türün neslinin devamı için zorunludur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_biyo9_vid_topic_03_canlilarin_ortak_ozellikleri',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=F0k9y9a3xL0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '0243614dcf9148b2',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 25. NEW: Biyoloji Karbonhidratlar
  {
    id: 'item_biyo9_vid_topic_05_karbonhidratlar__quiz',
    courseId: 'course_biyo_9',
    lessonId: 'lesson_biyo9_topic_05_karbonhidratlar',
    stableKey: 'biyo9_quiz_karbonhidratlar_micro',
    itemType: 'QUIZ',
    displayLabel: '5.1-Q',
    orderKey: 1500,
    title: 'Karbonhidratlar Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Karbonhidratlar Mikro Testi',
      questions: [
        {
          id: 'q_biyo9_karb_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Hücrede birinci dereceden öncelikli enerji kaynağı olarak kullanılan organik molekül grubu hangisidir?',
          choices: [
            'Karbonhidratlar',
            'Proteinler',
            'Yağlar (Lipitler)',
            'Nükleik Asitler'
          ],
          correctAnswer: 'Karbonhidratlar',
          explanation: 'Karbonhidratlar parçalanması kolay olduğu için hücrenin ilk başvurduğu enerji kaynağıdır.'
        },
        {
          id: 'q_biyo9_karb_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İki glikoz molekülünün birleşmesiyle oluşan disakkarit hangisidir ve arasındaki bağın adı nedir?',
          choices: [
            'Maltoz - Glikozit bağı',
            'Sükroz - Peptit bağı',
            'Laktoz - Ester bağı',
            'Nişasta - Fosfodiester bağı'
          ],
          correctAnswer: 'Maltoz - Glikozit bağı',
          explanation: 'Glikoz + Glikoz -> Maltoz + H2O (Arpa şekeri), kurulan bağ glikozit bağıdır.'
        },
        {
          id: 'q_biyo9_karb_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Bitkilerde fotosentez sonucu üretilen fazla glikoz lökoplastlarda Nişasta olarak depolanır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Bitkisel depo polisakkariti nişastadır; hayvanlarda ve mantarlarda ise glikojendir.'
        },
        {
          id: 'q_biyo9_karb_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Mantarların hücre çeperinde ve böceklerin dış iskeletinde bulunan, yapısında azot (N) elementi barındıran polisakkarit hangisidir?',
          choices: [
            'Kitin',
            'Selüloz',
            'Glikojen',
            'Nişasta'
          ],
          correctAnswer: 'Kitin',
          explanation: 'Kitin azot içeren yapısal bir polisakkarittir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_biyo9_vid_topic_05_karbonhidratlar',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=kY0p71h3aL0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'aa74139f85af8015',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 26. NEW: Biyoloji Proteinler
  {
    id: 'item_biyo9_vid_topic_07_proteinler__quiz',
    courseId: 'course_biyo_9',
    lessonId: 'lesson_biyo9_topic_07_proteinler',
    stableKey: 'biyo9_quiz_proteinler_micro',
    itemType: 'QUIZ',
    displayLabel: '7.1-Q',
    orderKey: 1500,
    title: 'Proteinler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Proteinler Mikro Testi',
      questions: [
        {
          id: 'q_biyo9_prot_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Proteinlerin yapı birimi (monomeri) nedir ve bu monomerler hangi bağ ile birbirine bağlanır?',
          choices: [
            'Amino asit - Peptit bağı',
            'Glikoz - Glikozit bağı',
            'Yağ asidi - Ester bağı',
            'Nükleotit - Hidrojen bağı'
          ],
          correctAnswer: 'Amino asit - Peptit bağı',
          explanation: 'Proteinler amino asitlerin peptit bağlarıyla birleşmesinden oluşur.'
        },
        {
          id: 'q_biyo9_prot_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Yüksek sıcaklık, pH değişimi veya basınç etkisiyle proteinlerin 3 boyutlu yapısının bozulup işlevsizleşmesine ne ad verilir?',
          choices: [
            'Denatürasyon',
            'Renatürasyon',
            'Hidroliz',
            'Dehidrasyon'
          ],
          correctAnswer: 'Denatürasyon',
          explanation: 'Proteinlerin doğal katlanma yapısının bozulmasına denatürasyon denir.'
        },
        {
          id: 'q_biyo9_prot_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Protein sentezi tüm canlı hücrelerde DNA şifresine uygun olarak ribozom organelinde gerçekleşir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Proteinler genetik şifreye (DNA/mRNA) göre ribozomda sentezlenir.'
        },
        {
          id: 'q_biyo9_prot_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Antikorlar, hemoglobin, saç ve tırnaktaki keratin molekülleri protein yapılı bileşiklerdir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Savunmada antikor, gaz taşımada hemoglobin, yapıda keratin protein yapılıdır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_biyo9_vid_topic_07_proteinler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=F0k9y9a3xL0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'b5ebaf2a8faa15c2',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 27. NEW: Biyoloji Enzimler
  {
    id: 'item_biyo9_vid_topic_08_enzimler__quiz',
    courseId: 'course_biyo_9',
    lessonId: 'lesson_biyo9_topic_08_enzimler',
    stableKey: 'biyo9_quiz_enzimler_micro',
    itemType: 'QUIZ',
    displayLabel: '8.1-Q',
    orderKey: 1500,
    title: 'Enzimler Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Enzimler Mikro Testi',
      questions: [
        {
          id: 'q_biyo9_enz_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Enzimlerin biyokimyasal tepkimelerdeki temel fonksiyonu nedir?',
          choices: [
            'Aktivasyon enerjisini düşürerek tepkimeyi hızlandırmak',
            'Tepkimeyi başlatmak',
            'Tepkime sonunda tükenmek',
            'Tepkime ürününün miktarını artırmak'
          ],
          correctAnswer: 'Aktivasyon enerjisini düşürerek tepkimeyi hızlandırmak',
          explanation: 'Enzimler biyolojik katalizörlerdir; aktivasyon enerjisini düşürerek tepkimeyi hızlandırırlar.'
        },
        {
          id: 'q_biyo9_enz_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Enzimin etki ettiği maddeye ne ad verilir ve enzim ile bu madde arasındaki ilişki neye benzetilir?',
          choices: [
            'Substrat - Anahtar-Kilit Uyumu',
            'Apoenzim - Mıknatıs Çekimi',
            'Kofaktör - Yapboz Parçası',
            'Koenzim - Kimyasal Bağ'
          ],
          correctAnswer: 'Substrat - Anahtar-Kilit Uyumu',
          explanation: 'Enzimin aktif bölgesine bağlanan maddeye substrat denir ve anahtar-kilit uyumu vardır.'
        },
        {
          id: 'q_biyo9_enz_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Enzimler kimyasal tepkimelerden hiçbir değişikliğe uğramadan çıkarlar ve tekrar tekrar kullanılabilirler.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Enzimler tepkimede harcanmaz, değişmeden çıkar ve tekrar kullanılır.'
        },
        {
          id: 'q_biyo9_enz_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Sıcaklık optimum değerin çok üzerine (örneğin 60 °C) çıkarıldığında enzim yapısındaki protein kalıcı olarak denatüre olur.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Yüksek sıcaklık protein yapıyı bozar ve enzim aktivitesini geri dönüşsüz olarak durdurur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_biyo9_vid_topic_08_enzimler',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=F0k9y9a3xL0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'a7e85c308adb2be2',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 28. NEW: Coğrafya Harita Bilgisi
  {
    id: 'item_cog9_vid_harita_bilgisi__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_04_mekanin_aynasi_haritalar',
    stableKey: 'cog9_quiz_harita_bilgisi_micro',
    itemType: 'QUIZ',
    displayLabel: '4.1-Q',
    orderKey: 1500,
    title: 'Harita Bilgisi ve Projeksiyonlar Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Harita Bilgisi ve Projeksiyonlar Mikro Testi',
      questions: [
        {
          id: 'q_cog9_harita_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir çizimin harita özelliği taşıyabilmesi için gereken 3 temel unsur nedir?',
          choices: [
            'Kuşbakışı görünüş, belirli bir ölçek, düzleme aktarılmış olma',
            'Renkli çizim, büyük boyut, pusula',
            'Kabartma tekniği, koordinat çizgileri, lejant',
            'Sadece yer isimleri, ölçek, fotoğraf'
          ],
          correctAnswer: 'Kuşbakışı görünüş, belirli bir ölçek, düzleme aktarılmış olma',
          explanation: 'Harita tanımı: Kuşbakışı olarak, belli bir ölçek dahilinde düzleme aktarılan çizimlerdir.'
        },
        {
          id: 'q_cog9_harita_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Ekvator ve çevresini en az hata ve bozulma ile haritaya aktarmak için hangi projeksiyon yöntemi tercih edilir?',
          choices: [
            'Silindirik Projeksiyon',
            'Konik Projeksiyon',
            'Düzlem Projeksiyon',
            'Parçalı Projeksiyon'
          ],
          correctAnswer: 'Silindirik Projeksiyon',
          explanation: 'Silindirik projeksiyon Ekvator çevresinde en az bozulmayı verir; kutuplara gidildikçe bozulma artar.'
        },
        {
          id: 'q_cog9_harita_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Orta kuşak ülkelerinin (örneğin Türkiye) harita çizimlerinde bozulmayı en aza indirmek için Konik Projeksiyon yöntemi kullanılır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Konik projeksiyon 30°-60° enlemleri arasındaki Orta Kuşak bölgelerini en az hata ile çizer.'
        },
        {
          id: 'q_cog9_harita_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Büyük ölçekli haritalar, küçük ölçekli haritalara göre daha dar bir alanı gösterir ve ayrıntıyı gösterme gücü daha fazladır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Büyük ölçekte payda küçüktür, küçültme azdır, ayrıntı fazladır, bozulma azdır.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_harita_bilgisi',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=F0k9y9a3xL0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: '92ed5bed2a0c5a22',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 29. NEW: Coğrafya Türkiye Konumu
  {
    id: 'item_cog9_vid_topic_05_turkiye_nin_cografi_konumu__quiz',
    courseId: 'course_cog_9',
    lessonId: 'lesson_cog9_topic_05_turkiye_nin_cografi_konumu',
    stableKey: 'cog9_quiz_turkiyenin_cografi_konumu_micro',
    itemType: 'QUIZ',
    displayLabel: '5.1-Q',
    orderKey: 1500,
    title: 'Türkiye\'nin Coğrafi Konumu Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Türkiye\'nin Coğrafi Konumu Mikro Testi',
      questions: [
        {
          id: 'q_cog9_trkonum_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Türkiye\'nin mutlak (matematik) konumu hangi paralel ve meridyen dereceleri arasındadır?',
          choices: [
            '36°-42° Kuzey Paralelleri, 26°-45° Doğu Meridyenleri',
            '26°-45° Kuzey Paralelleri, 36°-42° Doğu Meridyenleri',
            '36°-42° Güney Paralelleri, 26°-45° Batı Meridyenleri',
            '30°-40° Kuzey Paralelleri, 20°-40° Doğu Meridyenleri'
          ],
          correctAnswer: '36°-42° Kuzey Paralelleri, 26°-45° Doğu Meridyenleri',
          explanation: 'Türkiye 36°-42° Kuzey enlemleri ile 26°-45° Doğu boylamları arasında yer alır.'
        },
        {
          id: 'q_cog9_trkonum_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Türkiye\'de dört mevsimin belirgin olarak yaşanması ve Akdeniz iklim kuşağında yer alması Türkiye\'nin hangi özelliğinin sonucudur?',
          choices: [
            'Orta Kuşak\'ta (Ilıman Kuşak) yer almasının',
            'Üç tarafının denizlerle çevrili olmasının',
            'Asya ve Avrupa arasında köprü olmasının',
            'Ortalama yükseltisinin fazla olmasının'
          ],
          correctAnswer: 'Orta Kuşak\'ta (Ilıman Kuşak) yer almasının',
          explanation: 'Dört mevsimin belirgin yaşanması Orta Kuşak\'ta bulunmanın mutlak konum sonucudur.'
        },
        {
          id: 'q_cog9_trkonum_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Türkiye\'nin en doğusu (Iğdır - 45° D) ile en batısı (Gökçeada - 26° D) arasındaki yerel saat farkı 76 dakikadır (19 meridyen x 4 dk).',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: '45 - 26 = 19 meridyen farkı vardır. 19 x 4 = 76 dakika yerel saat farkı oluşur.'
        },
        {
          id: 'q_cog9_trkonum_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Türkiye\'de batıdan doğuya doğru gidildikçe sıcaklıkların genel olarak azalması göreceli konum (yükselti) ile açıklanır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Aynı enlemde batıdan doğuya sıcaklığın düşmesi yükseltinin artmasından kaynaklanan göreceli konum özelliğidir.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_cog9_vid_topic_05_turkiye_nin_cografi_konumu',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=F0k9y9a3xL0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'b0e60d4115d77a24',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  },

  // 30. NEW: TDE Hikâye Unsurları
  {
    id: 'item_tde9_vid_sozcukte_anlam_isim_sifat__quiz',
    courseId: 'course_tde_9',
    lessonId: 'lesson_tde9_dilin_yapisi_ve_roman',
    stableKey: 'tde9_quiz_hikaye_unsurlari_micro',
    itemType: 'QUIZ',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Hikâyenin Unsurları ve Planı Mikro Testi',
    contentUrl: null,
    publishingStatus: 'active',
    quiz: {
      quizTitle: 'Hikâyenin Unsurları ve Planı Mikro Testi',
      questions: [
        {
          id: 'q_tde9_hikplan_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Klasik olay hikâyesinin (Maupassant tarzı) bölümleri hangi sıra ile gerçekleşir?',
          choices: [
            'Serim - Düğüm - Çözüm',
            'Düğüm - Serim - Çözüm',
            'Giriş - Çözüm - Düğüm',
            'Serim - Çözüm - Düğüm'
          ],
          correctAnswer: 'Serim - Düğüm - Çözüm',
          explanation: 'Hikâyenin planı; kişilerin ve yerin tanıtıldığı serim, merakın doruğa çıktığı düğüm ve sonucun açıklandığı çözüm bölümüdür.'
        },
        {
          id: 'q_tde9_hikplan_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Anlatıcının olayları bir kamera sessizliğiyle, yalnızca gördüklerini tarafsızca aktardığı bakış açısı hangisidir?',
          choices: [
            'Gözlemci (Müşahit) Bakış Açısı',
            'İlahi (Hâkim / Tanrısal) Bakış Açısı',
            'Kahraman (Ben) Bakış Açısı',
            'Çoğulcu Bakış Açısı'
          ],
          correctAnswer: 'Gözlemci (Müşahit) Bakış Açısı',
          explanation: 'Gözlemci bakış açısında anlatıcı kahramanların aklından geçenleri bilmez; sadece dışarıdan gördüklerini 3. kişi ağzıyla aktarır.'
        },
        {
          id: 'q_tde9_hikplan_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'İlahi (Hâkim) bakış açısında anlatıcı, kahramanların iç dünyasını, zihninden geçenleri ve geçmiş/gelecekteki tüm detayları bilir.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Hâkim anlatıcı her şeye vakıftır ve kahramanların duygularını, düşüncelerini bilir.'
        },
        {
          id: 'q_tde9_hikplan_4',
          questionIndex: 3,
          type: 'TRUE_FALSE',
          prompt: 'Hikâyenin 4 temel yapı unsuru; Olay, Kişi (Şahıs Kadrosu), Mekân (Yer) ve Zaman\'dır.',
          choices: [
            'TRUE',
            'FALSE'
          ],
          correctAnswer: 'TRUE',
          explanation: 'Olay örgüsü, şahıs kadrosu, mekân ve zaman hikâyenin vazgeçilmez 4 yapı unsurudur.'
        }
      ],
      questionCount: 4,
      schemaVersion: 'v2-quiz'
    },
    provenance: {
      derivedFromItemId: 'item_tde9_vid_sozcukte_anlam_isim_sifat',
      sourceVideoUrl: 'https://www.youtube.com/watch?v=F0k9y9a3xL0',
      transcriptLanguage: 'tr',
      transcriptKind: 'subtitle',
      transcriptFingerprint: 'b942c35900634519',
      generatedBy: 'gemini',
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-03T18:30:00.000Z',
      schemaVersion: 'v2'
    }
  }
];

function generateMicroQuizzes() {
  const repoRoot = path.resolve(__dirname, '..');
  const v2Dir = path.join(repoRoot, 'content', 'v2');
  const itemsDir = path.join(v2Dir, 'items');
  const lessonsDir = path.join(v2Dir, 'lessons');

  let totalNewQuestions = 0;

  for (const qData of microQuizzes) {
    const val = validateQuizSchema(qData.quiz);
    if (!val.valid) {
      throw new Error(`Invalid quiz schema for ${qData.id}: ${val.errors.join('; ')}`);
    }

    qData.provenance.fingerprint = val.fingerprint;

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
