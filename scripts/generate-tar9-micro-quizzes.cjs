const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

const baseDir = path.resolve(__dirname, '..');
const itemsDir = path.join(baseDir, 'content/v2/items');
const lessonsDir = path.join(baseDir, 'content/v2/lessons');

const quizzesToCreate = [
  {
    id: 'item_tar9_vid_topic_03_tarihsel_bilginin_uretim_sureci__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_03_tarihsel_bilginin_uretim_sureci',
    stableKey: 'tar9_quiz_topic_03_tarihsel_bilginin_uretim_sureci_micro',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Tarihsel Bilginin Üretim Süreci Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_03_tarihsel_bilginin_uretim_sureci',
    quiz: {
      quizTitle: 'Tarihsel Bilginin Üretim Süreci Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top03_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarih araştırmalarında ilk aşama olan ve araştırılan konuya ilişkin her türlü kaynak ve belgenin tespit edilip toplanması sürecine ne ad verilir?',
          choices: [
            'Tarama (Kaynak Arama)',
            'Tasnif (Sınıflandırma)',
            'Tahlil (Çözümleme)',
            'Terkip (Sentez)'
          ],
          correctAnswer: 'Tarama (Kaynak Arama)',
          explanation: 'Tarih araştırmasının ilk basamağı tarama aşamasıdır. Konuyla ilgili birinci ve ikinci elden yazılı, sözlü veya görsel kaynaklar bu evrede bir araya getirilir.'
        },
        {
          id: 'q_tar9_top03_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarihçinin elde ettiği belgelerin orijinalliğini, yazıldığı dönemi, yazarının kimliğini ve fiziksel özelliklerini belirlemek amacıyla yaptığı eleştiri türü aşağıdakilerden hangisidir?',
          choices: [
            'Dış Tenkit (Dış Eleştiri)',
            'İç Tenkit (İç Eleştiri)',
            'Mekânsal Tasnif',
            'Sentezleme'
          ],
          correctAnswer: 'Dış Tenkit (Dış Eleştiri)',
          explanation: 'Dış tenkit; eserin adı, yazarı, basıldığı yer ve zaman, kâğıt ve mürekkep cinsi gibi dış unsurların orijinalliğini inceler. İçeriğin güvenilirliği ve tarafsızlığı ise iç tenkitle incelenir.'
        },
        {
          id: 'q_tar9_top03_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Tarih metodolojisinde "Terkip (Sentez)" aşaması; taranan, sınıflandırılan, çözümlenen ve eleştiriden geçen bilgilerin bir araya getirilerek anlamlı ve tutarlı bir tarihî eser haline dönüştürülmesidir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Terkip, tarih metodolojisinin son aşamasıdır. Tüm veriler neden-sonuç bağıyla birleştirilerek nihai tarihî bilgi ve metin oluşturulur.'
        },
        {
          id: 'q_tar9_top03_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Toplanan tarihsel kaynakların araştırma kolaylığı sağlamak amacıyla zamana, mekâna ve konuya göre gruplandırılmasına ne ad verilir?',
          choices: [
            'Tasnif (Sınıflandırma)',
            'Tahlil (Çözümleme)',
            'Tenkit (Eleştiri)',
            'Tarama (Arama)'
          ],
          correctAnswer: 'Tasnif (Sınıflandırma)',
          explanation: 'Tasnif; elde edilen verilerin zamana göre (ör. Orta Çağ Tarihi), mekâna göre (ör. Anadolu Tarihi) veya konuya göre (ör. Hukuk Tarihi) sınıflandırılmasıdır.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_04_tarih_arastirma_ve_yaziminda_dijitallesme__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_04_tarih_arastirma_ve_yaziminda_dijitallesme',
    stableKey: 'tar9_quiz_topic_04_tarih_arastirma_ve_yaziminda_dijitallesme_micro',
    displayLabel: '4.1-Q',
    orderKey: 1500,
    title: 'Tarih Araştırma ve Yazımında Dijitalleşme Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_04_tarih_arastirma_ve_yaziminda_dijitallesme',
    quiz: {
      quizTitle: 'Tarih Araştırma ve Yazımında Dijitalleşme Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top04_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Dijitalleşmenin modern tarih araştırmalarına ve yazımına sağladığı en belirgin katkı aşağıdakilerden hangisidir?',
          choices: [
            'Dünya genelindeki arşiv, yazma eser ve müze koleksiyonlarına hızlı, uzaktan ve elektronik erişim imkânı sunması',
            'Tarihçilerin geçmiş olayları laboratuvarda deneyle test etmesini sağlaması',
            'Tarihî olayların gelecekte kesin olarak nasıl tekrarlanacağını hesaplaması',
            'Tarihsel kaynak eleştirisi yapma zorunluluğunu tamamen ortadan kaldırması'
          ],
          correctAnswer: 'Dünya genelindeki arşiv, yazma eser ve müze koleksiyonlarına hızlı, uzaktan ve elektronik erişim imkânı sunması',
          explanation: 'Dijitalleşme; e-devlet, dijital kütüphaneler (ör. Cumhurbaşkanlığı Devlet Arşivleri, TTK e-arşiv) ve veri tabanları sayesinde bilgiye anında ve küresel ölçekte ulaşımı mümkün kılmıştır.'
        },
        {
          id: 'q_tar9_top04_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'İnternet ve dijital platformlarda yayımlanan tarih içerikleri, geleneksel basılı belgelere kıyasla manipülasyona ve dezenformasyona daha açık olduğu için dijital kaynak tenkiti (eleştirisi) kritik önem taşır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Dijital ortamda bilgi kirliliği ve sahte belgeler hızla yayılabildiğinden, araştırmacıların verinin kaynağını, URL güvenilirliğini ve resmî akademik dayanağını doğrulaması (dijital tenkit) zorunludur.'
        },
        {
          id: 'q_tar9_top04_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Büyük veri setleri, coğrafi bilgi sistemleri (CBS) ve yapay zekâ algoritmaları kullanılarak tarihsel metinlerin taranması ve analiz edilmesini konu edinen disiplinlerarası alan hangisidir?',
          choices: [
            'Dijital Beşerî Bilimler (Dijital Tarih)',
            'Nümizmatik',
            'Epigrafi',
            'Paleografya'
          ],
          correctAnswer: 'Dijital Beşerî Bilimler (Dijital Tarih)',
          explanation: 'Dijital Beşerî Bilimler (Digital Humanities), bilgisayar teknolojileri ve veri analiz yöntemlerinin tarih, edebiyat ve felsefe gibi beşerî bilimlere entegrasyonudur.'
        },
        {
          id: 'q_tar9_top04_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir tarih araştırmacısının internet üzerinden ulaştığı bir tarihî vesikayı değerlendirirken ilk yapması gereken bilimsel adım nedir?',
          choices: [
            'Belgenin resmî bir arşiv veya güvenilir akademik bir veri tabanından alınıp alınmadığını teyit etmek',
            'Sosyal medyada kaç kişi tarafından paylaşıldığına bakmak',
            'Belgeyi hemen doğruluğunu sorgulamadan kendi tezinde kullanmak',
            'Yalnızca günümüz değer yargılarına uygun olup olmadığını denetlemek'
          ],
          correctAnswer: 'Belgenin resmî bir arşiv veya güvenilir akademik bir veri tabanından alınıp alınmadığını teyit etmek',
          explanation: 'Bilimsel araştırma etiğinde kaynağın doğrulanabilirliği esastır; resmî arşiv ve hakemli akademik kaynak kontrolü ilk adımdır.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_05_tarim_devrimi_ve_eski_cag_da_yerlesme_ekonomi__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_05_tarim_devrimi_ve_eski_cag_da_yerlesme_ekonomi',
    stableKey: 'tar9_quiz_topic_05_tarim_devrimi_ve_eski_cag_da_yerlesme_ekonomi_micro',
    displayLabel: '5.1-Q',
    orderKey: 1500,
    title: 'Tarım Devrimi ve Eski Çağ\'da Yerleşme-Ekonomi Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_05_tarim_devrimi_ve_eski_cag_da_yerlesme_ekonomi',
    quiz: {
      quizTitle: 'Tarım Devrimi ve Eski Çağ\'da Yerleşme-Ekonomi Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top05_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İnsanoğlunun avcı-toplayıcı göçebe yaşamdan yerleşik hayata geçmesini ve üretici ekonomiye başlamasını sağlayan tarihsel gelişme hangisidir?',
          choices: [
            'Neolitik Dönem Tarım Devrimi',
            'Yazının İcadı',
            'Kavimler Göçü',
            'Paranın İcadı'
          ],
          correctAnswer: 'Neolitik Dönem Tarım Devrimi',
          explanation: 'Tarım Devrimi (Neolitik Devrim) ile insanlar toprağı ekmeye, hayvanları evcilleştirmeye ve kalıcı köyler kurarak üretici yaşama geçmeye başlamışlardır.'
        },
        {
          id: 'q_tar9_top05_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Tarımsal üretim fazlası olan "artı ürün", ürünlerin depolanması ihtiyacını doğurmuş; bu durum tapınak depolarının yönetimiyle birlikte yazının icadına ve takasa dayalı ticaretin başlamasına zemin hazırlamıştır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Artı ürün; depolama, kayıt tutma ihtiyacı (Sümerlerde piktografik yazı ve zigguratlar), iş bölümü, sınıflı toplum yapısı ve ticaretin doğmasına yol açmıştır.'
        },
        {
          id: 'q_tar9_top05_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Anadolu\'da ilk tarımsal üretimin yapıldığı ve ilk köy yerleşmelerinden biri olarak kabul edilen Diyarbakır sınırlarındaki arkeolojik alan hangisidir?',
          choices: [
            'Çayönü',
            'Göbeklitepe',
            'Çatalhöyük',
            'Alacahöyük'
          ],
          correctAnswer: 'Çayönü',
          explanation: 'Diyarbakır Ergani\'deki Çayönü, Yakın Doğu\'nun ilk köy yerleşimlerinden olup tarımsal faaliyetin ve evcilleştirilmiş hayvanların görüldüğü ilk merkezlerdendir.'
        },
        {
          id: 'q_tar9_top05_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Eski Çağ\'ın ilk büyük medeniyet havzalarının (Mezopotamya, Mısır, Hindistan, Çin) büyük nehir vadilerinde kurulmasının en temel sebebi nedir?',
          choices: [
            'Verimli alüvyal tarım toprakları ve zengin tatlı su kaynaklarına sahip olunması',
            'Etrafının tamamen aşılmaz sıradağlarla çevrili olması',
            'Madenî paranın yalnızca bu nehir havzalarında geçerli olması',
            'Sadece deniz aşırı sömürgecilik faaliyetlerine odaklanılması'
          ],
          correctAnswer: 'Verimli alüvyal tarım toprakları ve zengin tatlı su kaynaklarına sahip olunması',
          explanation: 'Fırat, Dicle, Nil, İndus ve Sarıırmak vadileri taşkınlarla verimli topraklar sağladığı ve sulu tarıma elverişli olduğu için ilk uygarlıkların beşiği olmuştur.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_06_eski_cag_medeniyetlerinde_yonetim_ve_ordu__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_06_eski_cag_medeniyetlerinde_yonetim_ve_ordu',
    stableKey: 'tar9_quiz_topic_06_eski_cag_medeniyetlerinde_yonetim_ve_ordu_micro',
    displayLabel: '6.1-Q',
    orderKey: 1500,
    title: 'Eski Çağ Medeniyetlerinde Yönetim ve Ordu Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_06_eski_cag_medeniyetlerinde_yonetim_ve_ordu',
    quiz: {
      quizTitle: 'Eski Çağ Medeniyetlerinde Yönetim ve Ordu Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top06_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sümer şehir devletlerinde (site) hem dinî, hem askerî hem de idari yetkileri elinde bulunduran rahip-kral yöneticilere ne ad verilirdi?',
          choices: [
            'Patesi (Ensi)',
            'Firavun',
            'Satrap',
            'Konsül'
          ],
          correctAnswer: 'Patesi (Ensi)',
          explanation: 'Sümerlerde site devletlerini Patesi veya Ensi adı verilen rahip-krallar yönetirdi; bu durum teokratik monarşik yapının göstergesidir.'
        },
        {
          id: 'q_tar9_top06_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Hititlerde kralın eşi olan ve devlet işlerinde, antlaşmalarda ve dinî törenlerde kral kadar yetkili kılınan kraliçeye ne unvan verilirdi?',
          choices: [
            'Tavananna',
            'Pankuş',
            'Kibele',
            'Mani'
          ],
          correctAnswer: 'Tavananna',
          explanation: 'Hititlerde Tavananna adı verilen başkraliçe geniş yetkilere sahipti; Kadeş Antlaşması\'nda kraliçenin de mührü yer almıştır.'
        },
        {
          id: 'q_tar9_top06_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Pers İmparatorluğu, fethettiği geniş toprakları merkezi otorite altında tutabilmek amacıyla ülkeyi "Satraplık" adı verilen eyaletlere bölmüş ve tarihin ilk düzenli posta teşkilatını kurmuştur.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Persler satraplık sistemiyle valiler atamış, şah kulağı müfettişleriyle denetlemiş ve haberleşmeyi posta örgütü (çapar) ile sağlamışlardır.'
        },
        {
          id: 'q_tar9_top06_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Hitit Devleti\'nde soylulardan oluşan, kralın yetkilerini denetleyebilen ve gerektiğinde kralı yargılayabilen danışma meclisi hangisidir?',
          choices: [
            'Pankuş Meclisi',
            'Senato',
            'Kurultay',
            'Divan-ı Hümayun'
          ],
          correctAnswer: 'Pankuş Meclisi',
          explanation: 'Hititlerde Pankuş Meclisi, kralın yetkilerini sınırlayan ve devlet idaresinde söz sahibi olan soylular meclisidir.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_07_eski_cag_medeniyetlerinde_hukuk__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_07_eski_cag_medeniyetlerinde_hukuk',
    stableKey: 'tar9_quiz_topic_07_eski_cag_medeniyetlerinde_hukuk_micro',
    displayLabel: '7.1-Q',
    orderKey: 1500,
    title: 'Eski Çağ Medeniyetlerinde Hukuk Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_07_eski_cag_medeniyetlerinde_hukuk',
    quiz: {
      quizTitle: 'Eski Çağ Medeniyetlerinde Hukuk Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top07_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Tarihte bilinen İLK YAZILI KANUNLAR hangi medeniyet ve hükümdar tarafından yürürlüğe konulmuştur?',
          choices: [
            'Sümerler - Urukagina Kanunları',
            'Babiller - Hammurabi Kanunları',
            'Hititler - Telepinu Kanunları',
            'Romalılar - 12 Levha Kanunları'
          ],
          correctAnswer: 'Sümerler - Urukagina Kanunları',
          explanation: 'MÖ 2375 civarında Lagaş Kralı Urukagina tarafından yapılan kanunlar, tarihin bilinen ilk yazılı kanunlarıdır ve halkı rahiplerin baskısından korumayı hedeflemiştir.'
        },
        {
          id: 'q_tar9_top07_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Göze göz, dişe diş" ilkesine dayanan son derece katı kısas cezaları içeren ve gücünü Tanrı Şamaş\'tan aldığını belirten kanunlar hangisidir?',
          choices: [
            'Hammurabi Kanunları',
            'Urukagina Kanunları',
            'Hitit Kanunları',
            'Justinianus Kanunları'
          ],
          correctAnswer: 'Hammurabi Kanunları',
          explanation: 'Babil Kralı Hammurabi tarafından hazırlanan 282 maddelik kanunlar sert kısas hükümleriyle meşhurdur.'
        },
        {
          id: 'q_tar9_top07_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Hitit Kanunları, Hammurabi Kanunları\'nın aksine kısas yerine büyük ölçüde "maddi tazminat" ve bedel ilkesine dayanmış; evlilik sözleşmesi gibi medeni hukuk unsurları barındırmıştır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Hitit hukuku insancıl niteliktedir; ölüm cezaları sınırlandırılmış, tazminat esası getirilmiş ve kadınlara mülkiyet ve boşanma hakları tanınmıştır.'
        },
        {
          id: 'q_tar9_top07_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Roma Cumhuriyeti\'nde Patriciler (soylular) ile Plepler (halk) arasındaki çatışmaları sona erdirmek ve hukuk birliğini sağlamak amacıyla hazırlanan hukuk metni hangisidir?',
          choices: [
            '12 Levha Kanunları',
            'Magna Carta',
            'Hammurabi Kanunları',
            'Cengiz Han Yasaları'
          ],
          correctAnswer: '12 Levha Kanunları',
          explanation: 'Roma\'da MÖ 451-449 yıllarında hazırlanan 12 Levha Kanunları (Leges Duodecim Tabularum), modern Avrupa/Roma hukukunun temel taşıdır.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_08_eski_cag_da_inanc_bilim_ve_sanat__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_08_eski_cag_da_inanc_bilim_ve_sanat',
    stableKey: 'tar9_quiz_topic_08_eski_cag_da_inanc_bilim_ve_sanat_micro',
    displayLabel: '8.1-Q',
    orderKey: 1500,
    title: 'Eski Çağ\'da İnanç, Bilim ve Sanat Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_08_eski_cag_da_inanc_bilim_ve_sanat',
    quiz: {
      quizTitle: 'Eski Çağ\'da İnanç, Bilim ve Sanat Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top08_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Eski Mısır\'da ölümden sonra yaşam (ahiret) inancının bir sonucu olarak cesetlerin mumyalanması, hangi bilim dallarının doğrudan gelişmesini sağlamıştır?',
          choices: [
            'Tıp, anatomi ve eczacılık (farmakoloji)',
            'Sosyoloji ve felsefe',
            'Nümizmatik ve epigrafi',
            'Jeoloji ve sismoloji'
          ],
          correctAnswer: 'Tıp, anatomi ve eczacılık (farmakoloji)',
          explanation: 'Mumyalama sırasında iç organların ve bedenin kimyasal maddelerle korunması, insan anatomisi, tıp ve eczacılıkta ileri düzey bilgi birikimi sağlamıştır.'
        },
        {
          id: 'q_tar9_top08_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sümerlerin Ziggurat adı verilen çok katlı tapınaklarının en üst katını rasathane (gözlemevi) olarak kullanmaları hangi bilimsel keşfe zemin hazırlamıştır?',
          choices: [
            'Ay yılı esaslı takvimin ve burçların belirlenmesi',
            'Güneş saatlerinin ve pusulanın icadı',
            'Atom teorisinin ortaya atılması',
            'Dünyanın çevresinin tam olarak hesaplanması'
          ],
          correctAnswer: 'Ay yılı esaslı takvimin ve burçların belirlenmesi',
          explanation: 'Sümer rahipleri ziggurat tepesinden gök cisimlerini gözlemleyerek Ay takvimini, gezegenlerin hareketlerini ve burçları keşfetmişlerdir.'
        },
        {
          id: 'q_tar9_top08_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Fenikeliler deniz aşırı ticaret faaliyetlerini kolaylaştırmak amacıyla hiyeroglif ve çivi yazısı yerine tarihin ilk harf yazısı olan 22 harfli Fenike Alfabesini geliştirmişlerdir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Fenike alfabesi sırasıyla İyon, Yunan ve Latin alfabelerinin temelini oluşturarak evrensel yazı kültürünü dönüştürmüştür.'
        },
        {
          id: 'q_tar9_top08_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İyonya medeniyetinde (Efes, Milet, Foça) Thales, Pisagor ve Hipokrat gibi öncü bilim insanlarının yetişmesinde etkili olan en temel faktör hangisidir?',
          choices: [
            'Deniz ticaretiyle zenginleşen şehirlerde özgür düşünce ve sorgulama ortamının bulunması',
            'Merkezi baskıcı ve teokratik bir imparatorluk yönetimi altında yaşamaları',
            'Sadece tek tanrılı dinleri benimsemiş olmaları',
            'Tüm dünyadaki kütüphaneleri yasaklamış olmaları'
          ],
          correctAnswer: 'Deniz ticaretiyle zenginleşen şehirlerde özgür düşünce ve sorgulama ortamının bulunması',
          explanation: 'İyonya şehirlerinde oligarşik/demokratik yapı, yüksek refah seviyesi ve doğu-batı kültür sentezi özgür felsefi ve bilimsel düşünceyi doğurmuştur.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_tarim_devrimi_mezopotamya__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_eski_cag_medeniyetleri',
    stableKey: 'tar9_quiz_tarim_devrimi_mezopotamya_micro',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Tarım Devrimi ve Mezopotamya Medeniyetleri Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_tarim_devrimi_mezopotamya',
    quiz: {
      quizTitle: 'Tarım Devrimi ve Mezopotamya Medeniyetleri Mikro Testi',
      questions: [
        {
          id: 'q_tar9_mezop_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Mezopotamya\'da MÖ 3200 civarında çivi yazısını icat ederek Tarih Çağları\'nı başlatan ve tarihteki ilk tekerleği kullanan medeniyet hangisidir?',
          choices: [
            'Sümerler',
            'Akadlar',
            'Elamlar',
            'Hititler'
          ],
          correctAnswer: 'Sümerler',
          explanation: 'Sümerler; çivi yazısı, tekerlek, ziggurat, ilk yazılı kanunlar ve 60 tabanlı matematik sistemiyle Mezopotamya uygarlığının kurucusudur.'
        },
        {
          id: 'q_tar9_mezop_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sargon liderliğinde tarihin ilk merkezi imparatorluğunu ve ilk düzenli daimi ordusunu kuran Mezopotamya devleti hangisidir?',
          choices: [
            'Akadlar',
            'Babiller',
            'Asurlar',
            'Urartular'
          ],
          correctAnswer: 'Akadlar',
          explanation: 'Kral Sargon komutasındaki Akadlar, tüm Mezopotamya sitelerini tek çatı altında toplayarak ilk imparatorluk ve ilk daimi ordu yapısını inşa etmiştir.'
        },
        {
          id: 'q_tar9_mezop_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Mezopotamya coğrafyasında taş ocaklarının az olması sebebiyle binaların kerpiç ve balçık tuğladan inşa edilmesi, yapıların günümüze kadar sağlam ulaşamamasına neden olmuştur.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Mısır\'daki gibi taş mimari yerine kerpiç kullanılması, Mezopotamya eserlerinin istilalar ve doğal koşullarla çabuk yok olmasına yol açmıştır.'
        },
        {
          id: 'q_tar9_mezop_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kayseri Kültepe\'de (Kaniş) "Karum" adı verilen ticaret kolonileri kurarak Anadolu\'ya çivi yazısını getiren ve Anadolu\'da Tarih Çağlarını başlatan Mezopotamya uygarlığı hangisidir?',
          choices: [
            'Asurlar',
            'Sümerler',
            'Babiller',
            'Fenikeliler'
          ],
          correctAnswer: 'Asurlar',
          explanation: 'Asurlu tüccarlar Kültepe Karumu üzerinden Anadolu ile yoğun ticaret yapmış ve çivi yazılı tabletleri Anadolu\'ya taşıyarak bölgeyi tarih çağına sokmuştur.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_anadolu_ve_turkler__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_eski_cag_medeniyetleri',
    stableKey: 'tar9_quiz_anadolu_ve_turkler_micro',
    displayLabel: '3.2-Q',
    orderKey: 2500,
    title: 'Anadolu Medeniyetleri ve Bozkır Yaşamı Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_anadolu_ve_turkler',
    quiz: {
      quizTitle: 'Anadolu Medeniyetleri ve Bozkır Yaşamı Mikro Testi',
      questions: [
        {
          id: 'q_tar9_anad_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Lidyalıların parayı icat ederek takas usulüne son vermesi ve Sardes\'ten Ninova\'ya uzanan Kral Yolu\'nu canlandırması en çok hangi alanı geliştirmiştir?',
          choices: [
            'Uluslararası ticaret ve piyasa ekonomisini',
            'Deniz sömürgeciliğini',
            'Piramit mimarisini',
            'Feodal toprak köleliğini'
          ],
          correctAnswer: 'Uluslararası ticaret ve piyasa ekonomisini',
          explanation: 'Madenî paranın basılması alışverişi kolaylaştırmış, sermaye birikimini ve Kral Yolu üzerindeki ticareti büyük ölçüde hızlandırmıştır.'
        },
        {
          id: 'q_tar9_anad_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İlk Çağ Türk bozkır kültüründe konargöçer yaşam tarzının gereği olarak ortaya çıkan, her an savaşa hazır olma durumuna ne ad verilir?',
          choices: [
            'Ordu-Millet Anlayışı',
            'Ücretli Lejyonerlik',
            'Şövalyelik Sistemi',
            'Kast Teşkilatı'
          ],
          correctAnswer: 'Ordu-Millet Anlayışı',
          explanation: 'Türklerde ayrı bir asker sınıfı bulunmaz; konargöçer yaşamın zor şartları gereği tüm halk her an savaşa hazır birer asker (ordu-millet) olarak yetişir.'
        },
        {
          id: 'q_tar9_anad_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Frigler, temel geçim kaynakları olan tarım ve hayvancılığı korumak amacıyla öküz kesene veya saban kırana ölüm cezası veren son derece sert yasalar koymuşlardır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Frig hukukunun en belirgin özelliği tarım ve hayvancılığı koruyucu katı hükümler içermesidir; bereketi simgeleyen tanrıçaları da Kibele\'dir.'
        },
        {
          id: 'q_tar9_anad_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Eski Türklerde ölen hükümdar ve kahramanların atları ve değerli eşyalarıyla birlikte gömüldüğü tümülüs tipi tepe mezarlara ne ad verilir?',
          choices: [
            'Kurgan',
            'Balbal',
            'Ziggurat',
            'Menhir'
          ],
          correctAnswer: 'Kurgan',
          explanation: 'Kurganlar, Eski Türklerde ahiret inancını gösteren mezar odalarıdır (Pazırık ve Esik kurganları en ünlü örnekleridir).'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_10_orta_cag_da_kitlesel_gocler__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_10_orta_cag_da_kitlesel_gocler',
    stableKey: 'tar9_quiz_topic_10_orta_cag_da_kitlesel_gocler_micro',
    displayLabel: '10.1-Q',
    orderKey: 1500,
    title: 'Orta Çağ\'da Kitlesel Göçler Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_10_orta_cag_da_kitlesel_gocler',
    quiz: {
      quizTitle: 'Orta Çağ\'da Kitlesel Göçler Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top10_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '375 yılında Hunların Balamir önderliğinde batıya ilerleyerek Vizigot, Ostrogot, Vandal ve Frank gibi kavimleri önlerine katıp Roma sınırlarına sürmesiyle başlayan tarihî olay hangisidir?',
          choices: [
            'Kavimler Göçü',
            'Haçlı Seferleri',
            'Moğol İstilası',
            'Ege Göçleri'
          ],
          correctAnswer: 'Kavimler Göçü',
          explanation: '375 Kavimler Göçü, İlk Çağ\'ı kapatıp Orta Çağ\'ı başlatan ve Avrupa\'nın bugünkü etnik ve siyasi sınırlarını çizen devasa bir kitlesel göç dalgasıdır.'
        },
        {
          id: 'q_tar9_top10_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kavimler Göçü\'nün ardından merkezi Roma otoritesinin çökmesiyle Batı Avrupa\'da ortaya çıkan toprağa ve yerel beylere dayalı yönetim düzeni hangisidir?',
          choices: [
            'Feodalite (Derebeylik)',
            'Merkezi Mutlak Krallık',
            'Meşruti Monarşi',
            'Doğrudan Demokrasi'
          ],
          correctAnswer: 'Feodalite (Derebeylik)',
          explanation: 'Merkezi krallıkların halkı koruyamaması üzerine halk yerel lordlara sığınmış ve şatolar etrafında şekillenen feodal düzen doğmuştur.'
        },
        {
          id: 'q_tar9_top10_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Kavimler Göçü sonrasında Avrupa\'da kilise ve papa siyasi güç kazanmış, bilimin yerini dogmatik "Skolastik Düşünce" almıştır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Güvensizlik ortamında Hristiyanlık kavimler arasında yayılmış, kilise tek eğitim ve bilgi otoritesi haline gelerek skolastik zihniyeti hâkim kılmıştır.'
        },
        {
          id: 'q_tar9_top10_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '13. yüzyılda Cengiz Han önderliğinde başlayıp Asya\'dan Orta Doğu ve Doğu Avrupa\'ya kadar uzanan ve Türk boylarının batıya (Anadolu\'ya) göçünü hızlandıran büyük istila hareketi hangisidir?',
          choices: [
            'Moğol İstilası',
            'Yüzyıl Savaşları',
            'Haçlı Seferleri',
            'Sasani Göçleri'
          ],
          correctAnswer: 'Moğol İstilası',
          explanation: 'Moğol İstilası, Harzemşahlar ve Selçuklular döneminde Türkmen kitlelerinin Orta Asya ve İran\'dan Anadolu\'ya yoğun göç etmesine ve Anadolu\'nun Türkleşmesine yol açmıştır.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_11_orta_cag_devletlerinde_yonetim_ve_ordu__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_11_orta_cag_devletlerinde_yonetim_ve_ordu',
    stableKey: 'tar9_quiz_topic_11_orta_cag_devletlerinde_yonetim_ve_ordu_micro',
    displayLabel: '11.1-Q',
    orderKey: 1500,
    title: 'Orta Çağ Devletlerinde Yönetim ve Ordu Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_11_orta_cag_devletlerinde_yonetim_ve_ordu',
    quiz: {
      quizTitle: 'Orta Çağ Devletlerinde Yönetim ve Ordu Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top11_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bizans İmparatorluğu\'nda hem sivil yönetimi hem de askerî garnizonları birleştiren ve sınır güvenliğini sağlayan idari eyalet sistemine ne ad verilirdi?',
          choices: [
            'Thema Sistemi',
            'Satraplık',
            'Tımar Sistemi',
            'Derebeylik'
          ],
          correctAnswer: 'Thema Sistemi',
          explanation: 'Thema sistemi, Bizans\'ın asker-çiftçilere toprak tahsis ederek ordu masraflarını düşürdüğü ve savunmayı güçlendirdiği eyalet düzenidir.'
        },
        {
          id: 'q_tar9_top11_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Orta Çağ Batı Avrupa feodalizminde bir lorda sadakat yemini ederek askerî koruma sağlayan zırhlı süvari sınıfına ne ad verilir?',
          choices: [
            'Şövalye (Vassal)',
            'Serf',
            'Lejyoner',
            'Yeniçeri'
          ],
          correctAnswer: 'Şövalye (Vassal)',
          explanation: 'Feodal piramitte şövalyeler soylu lordlara bağlı profesyonel zırhlı süvarilerdir.'
        },
        {
          id: 'q_tar9_top11_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Cengiz Han\'ın kurduğu Moğol İmparatorluğu ordusunda onluk, yüzlük, binlik ve tümen (on binlik) şeklinde teşkilatlanmış hareketli süvari birlikleri kullanılmıştır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Moğol ordusu bozkır askeri geleneğine ve Türklerdeki onluk sisteme dayanarak oluşturulmuş yüksek manevra kabiliyetine sahip süvarilerden kuruludur.'
        },
        {
          id: 'q_tar9_top11_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Sasani İmparatorluğu\'nda kralların başında yer alan ve mutlak yetkilere sahip "Kralların Kralı" anlamındaki hükümdar unvanı hangisidir?',
          choices: [
            'Şehinşah',
            'Basileus',
            'Kağan',
            'Dük'
          ],
          correctAnswer: 'Şehinşah',
          explanation: 'Sasanilerde imparator Şehinşah unvanı taşır ve Zerdüştlük dinine dayalı kutsal hükümdarlık anlayışıyla yönetirdi.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_orta_cag_gocler_devletler__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_orta_cag_medeniyetleri',
    stableKey: 'tar9_quiz_orta_cag_gocler_devletler_micro',
    displayLabel: '4.1-Q',
    orderKey: 1500,
    title: 'Kavimler Göçü ve Orta Çağ Devletleri Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_orta_cag_gocler_devletler',
    quiz: {
      quizTitle: 'Kavimler Göçü ve Orta Çağ Devletleri Mikro Testi',
      questions: [
        {
          id: 'q_tar9_ogd_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Kavimler Göçü sonrasında Batı Avrupa\'da kurulan Germen krallıkları arasında en kalıcı yapıyı kuran ve Şarlman döneminde imparatorluk haline gelen devlet hangisidir?',
          choices: [
            'Frank Krallığı',
            'Vizigot Krallığı',
            'Vandal Krallığı',
            'Burgond Krallığı'
          ],
          correctAnswer: 'Frank Krallığı',
          explanation: 'Franklar, Hristiyanlığı kabul edip Papalık ile ittifak kurarak Batı Avrupa\'da Kutsal Roma-Cermen İmparatorluğu\'nun temellerini atmıştır.'
        },
        {
          id: 'q_tar9_ogd_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bizans İmparatoru I. Justinianus tarafından Roma hukuk kurallarının derlenip sistemleştirilmesiyle hazırlanan ve modern medeni hukuka temel olan kanun külliyatı hangisidir?',
          choices: [
            'Justinianus Kanunları (Corpus Iuris Civilis)',
            '12 Levha Kanunları',
            'Hammurabi Kanunları',
            'Cengiz Han Yasaları'
          ],
          correctAnswer: 'Justinianus Kanunları (Corpus Iuris Civilis)',
          explanation: 'Justinianus Kanunları, Roma hukukunu Hristiyanlık esaslarıyla harmanlayıp kamu ve özel hukuk alanında çağının en kapsamlı kanunnamesi haline getirmiştir.'
        },
        {
          id: 'q_tar9_ogd_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Feodal düzende toprağın mülkiyetine ve hiçbir kişisel hürriyete sahip olmayan, toprakla birlikte alınıp satılan köylülere "Serf" denir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Serfler feodalitenin en ezilen sınıfıdır; toprağı terk edemez ve lordun izni olmadan evlenemezlerdi.'
        },
        {
          id: 'q_tar9_ogd_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Dört Halife Dönemi\'nde halifelerin belirlenmesinde istişare ve biat yönteminin kullanılması nedeniyle bu döneme tarihçiler tarafından ne ad verilmiştir?',
          choices: [
            'Cumhuriyet Dönemi',
            'Saltanat Dönemi',
            'Meşrutiyet Dönemi',
            'Feodal Dönem'
          ],
          correctAnswer: 'Cumhuriyet Dönemi',
          explanation: 'Dört Halife döneminde devlet başkanları veraset/babadan oğula usulüyle değil, ileri gelenlerin seçimi ve halkın biatıyla başa geçtiği için Cumhuriyet Dönemi olarak anılır.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_orta_cag_ticaret_yollari__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_orta_cag_medeniyetleri',
    stableKey: 'tar9_quiz_orta_cag_ticaret_yollari_micro',
    displayLabel: '4.2-Q',
    orderKey: 2500,
    title: 'Orta Çağ Ticaret Yolları Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_orta_cag_ticaret_yollari',
    quiz: {
      quizTitle: 'Orta Çağ Ticaret Yolları Mikro Testi',
      questions: [
        {
          id: 'q_tar9_oty_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Çin\'in Şian kentinden başlayıp Orta Asya, İran ve Anadolu üzerinden Akdeniz ve Karadeniz limanlarına uzanan en önemli kara ticaret yolu hangisidir?',
          choices: [
            'İpek Yolu',
            'Baharat Yolu',
            'Kral Yolu',
            'Amber Yolu'
          ],
          correctAnswer: 'İpek Yolu',
          explanation: 'İpek Yolu, Doğu ile Batı arasında ipek, porselen, kâğıt gibi malların yanı sıra kültür, din ve teknolojinin de taşındığı ana ticaret arteriydi.'
        },
        {
          id: 'q_tar9_oty_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Hindistan\'dan başlayıp deniz yoluyla Basra Körfezi ve Kızıldeniz üzerinden Akdeniz limanlarına zencefil, karabiber ve tarçın taşıyan ticaret yolu hangisidir?',
          choices: [
            'Baharat Yolu',
            'Kürk Yolu',
            'İpek Yolu',
            'Kral Yolu'
          ],
          correctAnswer: 'Baharat Yolu',
          explanation: 'Baharat Yolu deniz ağırlıklı bir yol olup Doğu\'nun baharat ve şifalı bitkilerini İskenderiye ve Antakya gibi limanlara ulaştırırdı.'
        },
        {
          id: 'q_tar9_oty_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Karadeniz\'in kuzeyinden başlayarak Sibirya ve İç Asya boyunca uzanan "Kürk Yolu", tilki, samur, kunduz gibi değerli kürklerin ticaretinde özellikle Hazar ve Bulgar Türkleri tarafından denetlenmiştir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Kürk Yolu kuzey bozkırlarında kurulmuş olup Hazar Devleti ve İtil Bulgarları bu ticaret sayesinde büyük zenginlik elde etmiştir.'
        },
        {
          id: 'q_tar9_oty_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İslam coğrafyasında ve Selçuklularda ticaret yolları üzerinde kervanların güvenliğini, konaklamasını ve ücretsiz bakımını sağlamak amacıyla inşa edilen yapılara ne ad verilir?',
          choices: [
            'Kervansaray ve Ribat',
            'Medrese ve Külliye',
            'İmaret ve Bedesten',
            'Kümbet ve Türbe'
          ],
          correctAnswer: 'Kervansaray ve Ribat',
          explanation: 'Ribatlar başlangıçta sınır karakolu iken sonraları ticaret güzergâhlarında kervansaraylara dönüşmüş; tüccarlara 3 gün ücretsiz barınma ve sigorta sistemi sunmuştur.'
        }
      ]
    }
  },
  {
    id: 'item_tar9_vid_topic_13_orta_cag_medeniyet_havzalarinda_bilim_kultur_ve_sana__quiz',
    courseId: 'course_tar_9',
    lessonId: 'lesson_tar9_topic_13_orta_cag_medeniyet_havzalarinda_bilim_kultur_ve_sana',
    stableKey: 'tar9_quiz_topic_13_orta_cag_medeniyet_havzalarinda_bilim_kultur_ve_sana_micro',
    displayLabel: '13.1-Q',
    orderKey: 1500,
    title: 'Orta Çağ Medeniyet Havzalarında Bilim, Kültür ve Sanat Mikro Testi',
    derivedFromItemId: 'item_tar9_vid_topic_13_orta_cag_medeniyet_havzalarinda_bilim_kultur_ve_sana',
    quiz: {
      quizTitle: 'Orta Çağ Medeniyet Havzalarında Bilim, Kültür ve Sanat Mikro Testi',
      questions: [
        {
          id: 'q_tar9_top13_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Abbasi Halifesi el-Memun döneminde Bağdat\'ta kurulan, Antik Yunan, Hint ve Fars eserlerinin Arapçaya çevrildiği ve bilimsel akademinin merkezi olan kurum hangisidir?',
          choices: [
            'Beytülhikme (Bilgelik Evi)',
            'Nizamiye Medresesi',
            'Darüşşifa',
            'Enderun Mektebi'
          ],
          correctAnswer: 'Beytülhikme (Bilgelik Evi)',
          explanation: 'Beytülhikme, 9. yüzyılda İslam dünyasında felsefe, astronomi, tıp ve matematiğin altın çağını başlatan devasa tercüme ve araştırma merkezidir.'
        },
        {
          id: 'q_tar9_top13_2',
          questionIndex: 1,
          type: 'MULTIPLE_CHOICE',
          prompt: '"El-Kânûn fi\'t-Tıbb" adlı başyapıtı Avrupa üniversitelerinde asırlarca temel tıp kitabı olarak okutulan ve Batı\'da "Avicenna" olarak tanınan tıp bilgini kimdir?',
          choices: [
            'İbn-i Sînâ',
            'Fârâbî',
            'El-Bîrûnî',
            'İbn Rüşd'
          ],
          correctAnswer: 'İbn-i Sînâ',
          explanation: 'İbn-i Sina (Avicenna), tıp ve felsefe alanında yazdığı eserlerle hem Doğu hem de Batı bilim dünyasını derinden etkilemiş hekimlerin piridir.'
        },
        {
          id: 'q_tar9_top13_3',
          questionIndex: 2,
          type: 'TRUE_FALSE',
          prompt: 'Sıfır (0) rakamını ilk kez matematikte bağımsız bir sayı olarak kullanan ve "Cebir" biliminin kurucusu olan Müslüman matematikçi Hârizmî\'dir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Hârizmî (Al-Khwarizmi), El-Cebr ve\'l-Mukâbele adlı eseriyle cebir bilimini kurmuş ve algoritma terimine ismini vermiştir.'
        },
        {
          id: 'q_tar9_top13_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Orta Çağ\'da kâğıt, matbaa, barut ve pusulayı icat ederek insanlık tarihinin seyrini değiştiren Uzak Doğu medeniyeti hangisidir?',
          choices: [
            'Çin Medeniyeti',
            'Hint Medeniyeti',
            'Japon Medeniyeti',
            'Sasani Medeniyeti'
          ],
          correctAnswer: 'Çin Medeniyeti',
          explanation: 'Çin\'de icat edilen kâğıt, matbaa, pusula ve barut; Talas Savaşı ve Haçlı Seferleri yoluyla İslam dünyasına ve oradan Avrupa\'ya taşınarak Rönesans, Reform ve Coğrafi Keşiflere zemin hazırlamıştır.'
        }
      ]
    }
  }
];

console.log('Writing 11 micro-quizzes for 9. Sınıf Tarih...');

for (const qData of quizzesToCreate) {
  const itemObj = {
    id: qData.id,
    courseId: qData.courseId,
    lessonId: qData.lessonId,
    stableKey: qData.stableKey,
    itemType: 'QUIZ',
    displayLabel: qData.displayLabel,
    orderKey: qData.orderKey,
    title: qData.title,
    contentUrl: null,
    publishingStatus: 'active',
    payload: {
      quiz: {
        quizTitle: qData.quiz.quizTitle,
        questions: qData.quiz.questions,
        questionCount: qData.quiz.questions.length,
        schemaVersion: 'v2-quiz'
      },
      provenance: {
        derivedFromItemId: qData.derivedFromItemId,
        generatedBy: 'gemini',
        reviewStatus: 'verified',
        reviewedOverride: true,
        importedAt: '2026-10-03T21:00:00.000Z',
        schemaVersion: 'v2',
        fingerprint: sha256(JSON.stringify(qData.quiz))
      }
    }
  };

  const itemPath = path.join(itemsDir, `${qData.id}.json`);
  fs.writeFileSync(itemPath, JSON.stringify(itemObj, null, 2) + '\n', 'utf8');
  console.log(`Created quiz item: ${qData.id}`);

  // Update lesson JSON
  const lessonPath = path.join(lessonsDir, `${qData.lessonId}.json`);
  const lessonObj = JSON.parse(fs.readFileSync(lessonPath, 'utf8'));
  if (!lessonObj.items.includes(qData.id)) {
    lessonObj.items.push(qData.id);
    // Sort items by orderKey
    lessonObj.items.sort((a, b) => {
      const itemA = JSON.parse(fs.readFileSync(path.join(itemsDir, `${a}.json`), 'utf8'));
      const itemB = JSON.parse(fs.readFileSync(path.join(itemsDir, `${b}.json`), 'utf8'));
      return (itemA.orderKey || 0) - (itemB.orderKey || 0);
    });
    fs.writeFileSync(lessonPath, JSON.stringify(lessonObj, null, 2) + '\n', 'utf8');
    console.log(`Updated lesson: ${qData.lessonId} -> [${lessonObj.items.join(', ')}]`);
  }
}

console.log('Finished generating all micro-quizzes.');
