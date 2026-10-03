const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function sha(obj) {
  return crypto.createHash('sha256').update(JSON.stringify(obj)).digest('hex').substring(0, 16);
}

const itemsDir = path.resolve(__dirname, '../content/v2/items');
const lessonsDir = path.resolve(__dirname, '../content/v2/lessons');

const quizzes = [
  {
    id: "item_tde9_vid_edebiyat_guzel_sanatlar__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_metin_ve_anlam",
    stableKey: "tde9_quiz_edebiyat_guzel_sanatlar_micro",
    displayLabel: "1.2-Q",
    orderKey: 2500,
    title: "Edebiyatın Güzel Sanatlarla İlişkisi Mikro Testi",
    derivedFrom: "item_tde9_vid_edebiyat_guzel_sanatlar",
    questions: [
      {
        id: "q_tde9_edeb_sanat_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Güzel sanatlar sınıflandırılırken edebiyat ve müzik, kullandıkları malzeme (ses ve dil) bakımından hangi sanat grubunda yer alır?",
        choices: [
          "İşitsel (Fonetik) Sanatlar",
          "Görsel (Plastik) Sanatlar",
          "Dramatik (Ritmik) Sanatlar",
          "Uygulamalı (Pratik) Sanatlar"
        ],
        correctAnswer: "İşitsel (Fonetik) Sanatlar",
        explanation: "Edebiyat ve müzik malzemesi ses ve söz olan işitsel (fonetik) sanatlar grubunda yer alır. Resim ve heykel görsel, tiyatro ve dans ise dramatik sanatlardandır."
      },
      {
        id: "q_tde9_edeb_sanat_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Edebiyat; tarihi olayları, insan psikolojisini ve toplumsal olguları incelerken tarih, psikoloji ve sosyoloji gibi bilim dallarıyla doğrudan ilişki içindedir.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Edebiyat insanı ve toplumu konu edindiği için psikoloji, sosyoloji, tarih ve felsefe gibi beşerî bilimlerden faydalanır ve onlara kaynaklık eder."
      },
      {
        id: "q_tde9_edeb_sanat_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "Aşağıdaki metin türü eşleştirmelerinden hangisinde sanat metni (edebî metin) ile öğretici metin bir arada doğru verilmiştir?",
        choices: [
          "Şiir - Makale",
          "Roman - Hikâye",
          "Fıkra - Deneme",
          "Mektup - Biyografi"
        ],
        correctAnswer: "Şiir - Makale",
        explanation: "Şiir estetik zevk uyandırmayı amaçlayan kurmaca bir sanat metnidir; makale ise bilgi verme ve kanıtlama amacı güden öğretici bir metindir."
      },
      {
        id: "q_tde9_edeb_sanat_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Edebiyatın güzel sanatlar arasındaki en belirleyici temel malzemesi aşağıdakilerden hangisidir?",
        choices: [
          "Dil (Sözcükler)",
          "Boya ve Renk",
          "Hareket ve Beden",
          "Taş ve Mermer"
        ],
        correctAnswer: "Dil (Sözcükler)",
        explanation: "Edebiyatın ana malzemesi dildir. Duygu, düşünce ve hayaller dil aracılığıyla estetik biçimde ifade edilir."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_02_sozun_inceligi_siiri_betimleme_paragrafina_donusturm__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_02_sozun_inceligi_siiri_betimleme_paragrafina_donusturm",
    stableKey: "tde9_quiz_topic_02_siiri_betimleme_micro",
    displayLabel: "2.1-Q",
    orderKey: 1500,
    title: "Şiiri Betimleme Paragrafına Dönüştürme Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_02_sozun_inceligi_siiri_betimleme_paragrafina_donusturm",
    questions: [
      {
        id: "q_tde9_top02_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Bir şiirdeki imge ve duyguları düz yazıya aktarırken yazarın kişisel izlenimlerini ve duygu dünyasını kattığı betimleme türü hangisidir?",
        choices: [
          "İzlenimsel (Sanatsal) Betimleme",
          "Açıklayıcı (Nesnel) Betimleme",
          "Tartışmacı Betimleme",
          "Öyküleyici Çözümleme"
        ],
        correctAnswer: "İzlenimsel (Sanatsal) Betimleme",
        explanation: "İzlenimsel betimlemede amaç nesneleri olduğu gibi değil, yazarın ruhunda bıraktığı izlenimlerle ve mecazlı dille aktarmaktır."
      },
      {
        id: "q_tde9_top02_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Şiiri betimleme paragrafına dönüştürürken şiirdeki ahenk ve ölçü kuralları birebir korunmak zorundadır.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "FALSE",
        explanation: "Şiir düz yazıya (betimleme paragrafına) aktarılırken vezin ve kafiye gibi şiire özgü şekil unsurları terkedilir; anlam, atmosfer ve imgeler düz yazı cümleleriyle ifade edilir."
      },
      {
        id: "q_tde9_top02_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "Şiirden nesir (betimleme) paragrafı oluşturma sürecinde ilk yapılması gereken aşama aşağıdakilerden hangisidir?",
        choices: [
          "Şiirin ana temasını, duygusunu ve imge dünyasını doğru kavramak",
          "Hemen son cümleyi yazarak sonucu belirlemek",
          "Şiirdeki her dizeyi kelime kelime alt alta dizmek",
          "Şiirin kafiye şemasını ezberlemek"
        ],
        correctAnswer: "Şiirin ana temasını, duygusunu ve imge dünyasını doğru kavramak",
        explanation: "Dönüştürme sürecinin ilk adımı metni derinlemesine okuyup tema, ana fikir, duygu ve kullanılan imgeleri doğru anlamlandırmaktır."
      },
      {
        id: "q_tde9_top02_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Duyuların birbiriyle kaynaştırılmasıyla (örneğin 'tatlı bir tebessüm', 'acı bir çığlık') yapılan anlatım zenginliğine ne ad verilir?",
        choices: [
          "Duyu aktarımı",
          "Benzetme edatı",
          "Abartma (Mübalağa)",
          "Tekrir"
        ],
        correctAnswer: "Duyu aktarımı",
        explanation: "Bir duyuya ait kavramın başka bir duyu alanına aktarılmasına (tatma duyusunun işitmeye aktarılması vb.) duyu aktarımı denir."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_03_sozun_inceligi_mulakat_dinleme_izleme__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_03_sozun_inceligi_mulakat_dinleme_izleme",
    stableKey: "tde9_quiz_topic_03_mulakat_micro",
    displayLabel: "3.1-Q",
    orderKey: 1500,
    title: "Mülakat Dinleme ve İnceleme Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_03_sozun_inceligi_mulakat_dinleme_izleme",
    questions: [
      {
        id: "q_tde9_top03_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Alanında tanınmış veya uzman bir kişiyle belirli bir konu etrafında önceden planlanmış soru-cevap şeklinde gerçekleşen türe ne ad verilir?",
        choices: [
          "Mülakat (Görüşme)",
          "Eleştiri",
          "Otobiyografi",
          "Günlük"
        ],
        correctAnswer: "Mülakat (Görüşme)",
        explanation: "Mülakat; uzman, yetkili veya ünlü bir kimseyle belirli bir amaç ve plan dahilinde yapılan görüşmeyi ve bu görüşmenin metne dökülmüş halini ifade eder."
      },
      {
        id: "q_tde9_top03_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Mülakat türünde görüşmeyi yapan yazar, görüşülen kişinin sözlerini kendi şahsi yorumlarıyla değiştirerek aktarma hakkına sahiptir.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "FALSE",
        explanation: "Mülakatta en temel ilke tarafsızlıktır. Görüşülen kişinin sözleri tahrif edilmeden, olduğu gibi ve aslına sadık kalarak aktarılmalıdır."
      },
      {
        id: "q_tde9_top03_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "Edebiyatımızda mülakat türünün ilk yetkin örneklerinden biri olan 'Diyorlar ki' adlı eserin yazarı aşağıdakilerden hangisidir?",
        choices: [
          "Ruşen Eşref Ünaydın",
          "Ahmet Rasim",
          "Nurullah Ataç",
          "Cenap Şahabettin"
        ],
        correctAnswer: "Ruşen Eşref Ünaydın",
        explanation: "Ruşen Eşref Ünaydın'ın dönemin ünlü edipleriyle yaptığı mülakatları içeren 'Diyorlar ki' eseri, Türk edebiyatında mülakat türünün başyapıtlarındandır."
      },
      {
        id: "q_tde9_top03_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Başarılı ve nitelikli bir mülakat dinleme/izleme sürecinde dikkat edilmesi gereken en önemli özellik nedir?",
        choices: [
          "Soruların konunun uzmanlık alanına uygunluğu ve verilen yanıtların tutarlılığı",
          "Görüşmecinin sürekli kendi fikirlerini savunması",
          "Mülakatın sadece eğlenceli ve mizahi olması",
          "Görüşmenin önceden hazırlıksız ve plansız yapılması"
        ],
        correctAnswer: "Soruların konunun uzmanlık alanına uygunluğu ve verilen yanıtların tutarlılığı",
        explanation: "Nitelikli bir mülakatta sorular hedefe yönelik, uzmanlık alanıyla ilgili olmalı ve cevaplar dikkatle dinlenip tutarlılık açısından analiz edilmelidir."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_04_sozun_inceligi_deneme_ve_fikir_gelistirme_konusmasi__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_04_sozun_inceligi_deneme_ve_fikir_gelistirme_konusmasi",
    stableKey: "tde9_quiz_topic_04_deneme_fikir_micro",
    displayLabel: "4.1-Q",
    orderKey: 1500,
    title: "Deneme ve Fikir Geliştirme Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_04_sozun_inceligi_deneme_ve_fikir_gelistirme_konusmasi",
    questions: [
      {
        id: "q_tde9_top04_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Yazarın herhangi bir konudaki kişisel görüş ve duygularını, kesin kurallara ve kanıtlama zorunluluğuna bağlı kalmadan samimi bir üslupla dile getirdiği tür hangisidir?",
        choices: [
          "Deneme",
          "Makale",
          "Fıkra",
          "Biyografi"
        ],
        correctAnswer: "Deneme",
        explanation: "Deneme; yazarın kendi kendisiyle konuşur gibi yazdığı, iddialarını bilimsel olarak ispatlama zorunluluğu bulunmayan öznel ve serbest düşünce yazısıdır."
      },
      {
        id: "q_tde9_top04_2",
        questionIndex: 1,
        type: "MULTIPLE_CHOICE",
        prompt: "Dünya edebiyatında deneme türünün kurucusu ve öncüsü sayılan yazar kimdir?",
        choices: [
          "Montaigne",
          "Bacon",
          "Rousseau",
          "Voltaire"
        ],
        correctAnswer: "Montaigne",
        explanation: "16. yüzyıl Fransız yazarı Michel de Montaigne 'Denemeler' (Essais) adlı eseriyle bu türün dünya edebiyatındaki kurucusu kabul edilir."
      },
      {
        id: "q_tde9_top04_3",
        questionIndex: 2,
        type: "TRUE_FALSE",
        prompt: "Türk edebiyatında Nurullah Ataç, 'Deneme benin ülkesidir.' sözüyle denemenin son derece öznel bir tür olduğunu vurgulamıştır.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Nurullah Ataç, denemenin yazarına özgü kişiselliğini ve öznel bakış açısını vurgulamak için 'Deneme benin ülkesidir.' ifadesini kullanmıştır."
      },
      {
        id: "q_tde9_top04_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Fikir geliştirme konuşması hazırlayan bir öğrencinin konuşmasını inandırıcı kılmak için aşağıdakilerden hangisinden yararlanması beklenir?",
        choices: [
          "Örnekleme, karşılaştırma ve mantıksal gerekçelendirme",
          "Konudan bağımsız rastgele iddialar sıralama",
          "Dinleyicilerin sorularını cevapsız bırakma",
          "Yalnızca süslü ve anlaşılmaz sözcükler seçme"
        ],
        correctAnswer: "Örnekleme, karşılaştırma ve mantıksal gerekçelendirme",
        explanation: "Bir düşünceyi aktarırken ve savunurken mantıklı gerekçeler, karşılaştırmalar ve somut örnekler sunmak konuşmanın ikna gücünü artırır."
      }
    ]
  },
  {
    id: "item_tde9_vid_hikaye_turleri__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_anlam_arayisi_hikaye",
    stableKey: "tde9_quiz_hikaye_turleri_micro",
    displayLabel: "2.1-Q",
    orderKey: 1500,
    title: "Olay ve Durum Hikâyesi Mikro Testi",
    derivedFrom: "item_tde9_vid_hikaye_turleri",
    questions: [
      {
        id: "q_tde9_hikayetur_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Maupassant tarzı (klasik olay) hikâyesinin en belirgin yapısal özelliği aşağıdakilerden hangisidir?",
        choices: [
          "Serim, düğüm ve çözüm planına sıkı sıkıya bağlı olması ve merak unsurunun ön planda tutulması",
          "Günlük hayattan bir kesiti durağan biçimde yansıtması",
          "Olay örgüsünün bulunmaması ve yalnızca psikolojik tahliller yapılması",
          "Şiirsel bir üslupla kafiyeli cümleler kurulması"
        ],
        correctAnswer: "Serim, düğüm ve çözüm planına sıkı sıkıya bağlı olması ve merak unsurunun ön planda tutulması",
        explanation: "Olay (Maupassant) hikâyelerinde belirgin bir olay zinciri, merak unsuru, serim-düğüm-çözüm bölümleri ve beklenmedik bir son yer alır."
      },
      {
        id: "q_tde9_hikayetur_2",
        questionIndex: 1,
        type: "MULTIPLE_CHOICE",
        prompt: "Türk edebiyatında durum (kesit / Çehov tarzı) hikâyeciliğinin en önemli iki öncüsü kimlerdir?",
        choices: [
          "Memduh Şevket Esendal - Sait Faik Abasıyanık",
          "Ömer Seyfettin - Refik Halid Karay",
          "Namık Kemal - Şinasi",
          "Halit Ziya Uşaklıgil - Mehmet Rauf"
        ],
        correctAnswer: "Memduh Şevket Esendal - Sait Faik Abasıyanık",
        explanation: "Memduh Şevket Esendal durum hikâyesinin ilk örneklerini vermiş, Sait Faik Abasıyanık ise bu türün en güçlü ve özgün temsilcisi olmuştur."
      },
      {
        id: "q_tde9_hikayetur_3",
        questionIndex: 2,
        type: "TRUE_FALSE",
        prompt: "Durum hikâyelerinde merak unsuru ve heyecan en yüksek seviyededir; hikâye mutlaka şaşırtıcı bir olayla son bulur.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "FALSE",
        explanation: "Durum hikâyelerinde amaç merak uyandırmak veya şaşırtmak değil; hayatın içinden bir anı, bir ruh halini ve insanlık durumunu hissettirmektir."
      },
      {
        id: "q_tde9_hikayetur_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Olay hikâyelerinde olayların gelişiminde gerilimin tırmandığı ve çatışmaların düğümlendiği bölüm hangisidir?",
        choices: [
          "Düğüm bölümü",
          "Serim bölümü",
          "Çözüm bölümü",
          "Ön deyiş"
        ],
        correctAnswer: "Düğüm bölümü",
        explanation: "Serim bölümünde kişiler ve mekân tanıtılır, düğüm bölümünde olaylar gelişir ve merak/çatışma doruğa ulaşır, çözümde ise sonuçlanır."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_06_anlam_arayisi_hikaye_karakteri_sunumu__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_06_anlam_arayisi_hikaye_karakteri_sunumu",
    stableKey: "tde9_quiz_topic_06_karakter_sunumu_micro",
    displayLabel: "6.1-Q",
    orderKey: 1500,
    title: "Hikâye Karakteri ve Tip Analizi Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_06_anlam_arayisi_hikaye_karakteri_sunumu",
    questions: [
      {
        id: "q_tde9_top06_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Edebî metinlerde belli bir zümreyi, toplumsal sınıfı veya genel bir insan özelliğini (örneğin cimrilik, kıskançlık) abartılı şekilde temsil eden kahramanlara ne ad verilir?",
        choices: [
          "Tip",
          "Karakter",
          "Anlatıcı",
          "Gözlemci"
        ],
        correctAnswer: "Tip",
        explanation: "Tip; kendine has bireysel özelliklerden ziyade belirli bir zümrenin, mesleğin veya insanlık zaafının ortak temsilcisi olan şablon kahramandır."
      },
      {
        id: "q_tde9_top06_2",
        questionIndex: 1,
        type: "MULTIPLE_CHOICE",
        prompt: "Kendine özgü derinliği olan, olaylar karşısında değişebilen, çelişkileri ve karmaşık psikolojisiyle ele alınan kahraman türü hangisidir?",
        choices: [
          "Karakter",
          "Tip",
          "Arketip",
          "Statik figür"
        ],
        correctAnswer: "Karakter",
        explanation: "Karakter; çok boyutlu, kendine has iç dünyası olan ve olaylar sürecinde gelişim/değişim gösterebilen özgün kahramandır."
      },
      {
        id: "q_tde9_top06_3",
        questionIndex: 2,
        type: "TRUE_FALSE",
        prompt: "Bir hikâye karakteri sunulurken karakterin yalnızca boyu, giyimi gibi fiziksel özellikleri anlatılmalı; iç çatışmaları ve psikolojik yönü göz ardı edilmelidir.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "FALSE",
        explanation: "Karakter analizi hem fiziksel portreyi (dış görünüş) hem de ruhsal portreyi (psikolojik durum, duygu, çatışma ve motivasyonlar) bütüncül olarak içermelidir."
      },
      {
        id: "q_tde9_top06_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Hikâye kahramanının aklından geçenleri doğrudan kendi cümleleriyle ve mantıksal bir sıra gözeterek okuyucuya aktarma tekniğine ne ad verilir?",
        choices: [
          "İç konuşma (Monolog)",
          "Bilinç akışı",
          "Gösterme",
          "Özetleme"
        ],
        correctAnswer: "İç konuşma (Monolog)",
        explanation: "İç konuşma; kahramanın iç dünyasını, dil bilgisi kurallarına ve mantık bağına uygun şekilde kendi ağzından aktarmasıdır (bilinç akışında ise mantık bağı kopuk ve serbesttir)."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_07_anlam_arayisi_siir_dinleme_izleme__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_07_anlam_arayisi_siir_dinleme_izleme",
    stableKey: "tde9_quiz_topic_07_siir_dinleme_micro",
    displayLabel: "7.1-Q",
    orderKey: 1500,
    title: "Şiir Dinleme ve Ahenk Unsurları Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_07_anlam_arayisi_siir_dinleme_izleme",
    questions: [
      {
        id: "q_tde9_top07_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Şiirde dize sonlarında yazılışları, okunuşları, görevleri ve anlamları AYNI olan eklerin veya kelimelerin tekrarlanmasına ne ad verilir?",
        choices: [
          "Redif",
          "Kafiye (Uyak)",
          "Aliterasyon",
          "Asonans"
        ],
        correctAnswer: "Redif",
        explanation: "Dize sonlarında anlamı ve görevi aynı olan eklerin veya sözcüklerin tekrarlanmasına redif denir. Görevleri farklı fakat ses benzerliği olan kısımlar ise kafiyedir."
      },
      {
        id: "q_tde9_top07_2",
        questionIndex: 1,
        type: "MULTIPLE_CHOICE",
        prompt: "Şiir dizelerinde aynı ünsüz (sessiz) harflerin ahenk oluşturacak şekilde sıkça tekrarlanması sanatına ne ad verilir?",
        choices: [
          "Aliterasyon",
          "Asonans",
          "Seci",
          "Cinas"
        ],
        correctAnswer: "Aliterasyon",
        explanation: "Aynı ünsüz seslerin yinelenmesine aliterasyon; aynı ünlü seslerin yinelenmesine ise asonans adı verilir."
      },
      {
        id: "q_tde9_top07_3",
        questionIndex: 2,
        type: "TRUE_FALSE",
        prompt: "Şiir dinleme ve seslendirme sürecinde vurgu, tonlama ve duraklara dikkat etmek şiirin duygu ve anlam derinliğini iletmede belirleyici rol oynar.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Etkili bir şiir icrasında dizelerdeki duygunun dinleyiciye geçmesi için doğru nefes, tonlama, vurgu ve durak kullanımı esastır."
      },
      {
        id: "q_tde9_top07_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Dize sonlarında tek ses benzerliğine dayanan kafiye türü aşağıdakilerden hangisidir?",
        choices: [
          "Yarım kafiye",
          "Tam kafiye",
          "Zengin kafiye",
          "Cinaslı kafiye"
        ],
        correctAnswer: "Yarım kafiye",
        explanation: "Tek ses benzerliği yarım kafiye, iki ses benzerliği tam kafiye, üç veya daha fazla ses benzerliği zengin kafiye oluşturur."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_08_anlam_arayisi_siir_yazma__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_08_anlam_arayisi_siir_yazma",
    stableKey: "tde9_quiz_topic_08_siir_yazma_micro",
    displayLabel: "8.1-Q",
    orderKey: 1500,
    title: "Şiir Yazma ve İmge Kurgusu Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_08_anlam_arayisi_siir_yazma",
    questions: [
      {
        id: "q_tde9_top08_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Şairin dış dünyadan edindiği izlenimleri kendi hayal gücüyle yoğurarak oluşturduğu yeni, özgün ve soyut zihinsel tasarımlara ne ad verilir?",
        choices: [
          "İmge",
          "Ölçü",
          "Nazım birimi",
          "Matla"
        ],
        correctAnswer: "İmge",
        explanation: "İmge (hayal); şairin gerçekliği dönüştürerek dille oluşturduğu özgün çağrışımlar ve zihinsel tablolardır."
      },
      {
        id: "q_tde9_top08_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Çağdaş serbest şiir yazarken ölçü ve kafiye zorunluluğu bulunmasa da dizelerin iç ahengi, ritmi ve sözcük seçimi şiiriyet açısından kritik önem taşır.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Serbest şiir hece veya aruz kalıplarına bağlı kalmaz ancak iç ritim, ses uyumu ve imge zenginliği ile şiir niteliğini korur."
      },
      {
        id: "q_tde9_top08_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "Dörtlüklerden oluşan bir şiirde 'abab' veya 'abcb' şeklinde örülen kafiye düzenine ne ad verilir?",
        choices: [
          "Çapraz kafiye",
          "Düz kafiye",
          "Sarma kafiye",
          "Mani tipi kafiye"
        ],
        correctAnswer: "Çapraz kafiye",
        explanation: "1. ile 3. dizenin (a-a), 2. ile 4. dizenin (b-b) kendi arasında uyaklanması 'çapraz kafiye' düzenidir."
      },
      {
        id: "q_tde9_top08_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Şiir yazma sürecinde duygunun veya temanın etkileyici aktarılması için şairin kaçınması gereken durum hangisidir?",
        choices: [
          "Basmakalıp (klişe) ifadeleri aşırı kullanmak",
          "Özgün çağrışımlar üretmek",
          "Sözcüklerin yan ve mecaz anlamlarından faydalanmak",
          "Ses ve ahenk uyumunu gözetmek"
        ],
        correctAnswer: "Basmakalıp (klişe) ifadeleri aşırı kullanmak",
        explanation: "Şiirde özgünlük temel esastır; herkesçe bilinen sıradan ve klişe kalıpları tekrarlamak şiirin sanatsal gücünü zayıflatır."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_10_anlamin_yapi_taslari_mekanlari_karsilastirmali_konus__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_10_anlamin_yapi_taslari_mekanlari_karsilastirmali_konus",
    stableKey: "tde9_quiz_topic_10_mekan_karsilastirma_micro",
    displayLabel: "10.1-Q",
    orderKey: 1500,
    title: "Gezi Yazısı ve Mekân Karşılaştırma Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_10_anlamin_yapi_taslari_mekanlari_karsilastirmali_konus",
    questions: [
      {
        id: "q_tde9_top10_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Bir yazarın gezip gördüğü yerlerin tarihî, coğrafi, kültürel ve sosyal özelliklerini gözlemlerine dayanarak anlattığı edebî türe ne ad verilir?",
        choices: [
          "Gezi Yazısı (Seyahatname)",
          "Hatıra (Anı)",
          "Biyografi",
          "Günlük"
        ],
        correctAnswer: "Gezi Yazısı (Seyahatname)",
        explanation: "Gezi yazısı (seyahatname); gezilen görülen yerlerin kendine özgü yönlerinin canlı ve gözleme dayalı bir üslupla aktarıldığı türdür."
      },
      {
        id: "q_tde9_top10_2",
        questionIndex: 1,
        type: "MULTIPLE_CHOICE",
        prompt: "Türk edebiyatında 17. yüzyılda yazılmış ve 10 ciltten oluşan en ünlü anıtsal gezi eseri (Seyahatname) kime aittir?",
        choices: [
          "Evliya Çelebi",
          "Kâtip Çelebi",
          "Piri Reis",
          "Ahmet Mithat Efendi"
        ],
        correctAnswer: "Evliya Çelebi",
        explanation: "Evliya Çelebi'nin 'Seyahatnâme'si Osmanlı coğrafyasını ve komşu ülkeleri anlatan dünya çapında bir başyapıttır."
      },
      {
        id: "q_tde9_top10_3",
        questionIndex: 2,
        type: "TRUE_FALSE",
        prompt: "Mekânları karşılaştırmalı olarak anlatırken sadece mimari yapıları sıralamak yeterlidir; insanların yaşam tarzı ve mekânın ruhu önemsizdir.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "FALSE",
        explanation: "Mekân anlatımı ve karşılaştırmasında o mekânda yaşayan insanların kültürü, sosyal hayatı, gelenekleri ve mekânın insan üzerindeki psikolojik etkileri de ele alınmalıdır."
      },
      {
        id: "q_tde9_top10_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Gezi yazılarında yazarın anlatımını güçlendirmek için en sık başvurduğu anlatım biçimleri hangileridir?",
        choices: [
          "Betimleyici ve Öyküleyici Anlatım",
          "Yalnızca Tartışmacı Anlatım",
          "Didaktik Emredici Anlatım",
          "Dramatik Sahneleme"
        ],
        correctAnswer: "Betimleyici ve Öyküleyici Anlatım",
        explanation: "Gezi yazılarında mekânın görünümünü göz önünde canlandırmak için betimleme, gezilen anları ve yolculuğu aktarmak için öyküleme bir arada kullanılır."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_11_anlamin_yapi_taslari_belgesel_dinleme_izleme__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_11_anlamin_yapi_taslari_belgesel_dinleme_izleme",
    stableKey: "tde9_quiz_topic_11_belgesel_dinleme_micro",
    displayLabel: "11.1-Q",
    orderKey: 1500,
    title: "Belgesel Dinleme/İzleme ve Çözümleme Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_11_anlamin_yapi_taslari_belgesel_dinleme_izleme",
    questions: [
      {
        id: "q_tde9_top11_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Belgesel türü metin ve videoların kurmaca edebî eserlerden en temel farkı aşağıdakilerden hangisidir?",
        choices: [
          "Gerçek kişi, olay, belge ve somut verilere dayanması",
          "Yalnızca hayal ürünü karakterlere yer vermesi",
          "Kafiyeli ve ölçülü bir dille sunulması",
          "Sonucunun gizli tutulup izleyiciye bırakılması"
        ],
        correctAnswer: "Gerçek kişi, olay, belge ve somut verilere dayanması",
        explanation: "Belgeseller kurgusal değil, gerçeğe ve belgelere dayalıdır; araştırma, tanıklık ve somut kanıtlarla toplumu bilgilendirmeyi amaçlar."
      },
      {
        id: "q_tde9_top11_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Belgesel izlerken sunulan bilgilerin güvenirliğini sorgulamak ve kullanılan görsel-işitsel kanıtların tutarlılığını değerlendirmek eleştirel izlemenin gereğidir.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Eleştirel izleme becerisi, kaynakların doğruluğunu, tarafsızlığını ve tez-kanıt ilişkisini sorgulamayı gerektirir."
      },
      {
        id: "q_tde9_top11_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "Bir belgeselde konunun uzmanlarının görüşlerine yer verilmesi, metnin hangi boyutunu güçlendirir?",
        choices: [
          "İnandırıcılık ve Güvenilirlik",
          "Kurmaca gücünü",
          "Mizahi yönünü",
          "Gizem unsurunu"
        ],
        correctAnswer: "İnandırıcılık ve Güvenilirlik",
        explanation: "Alanında uzman kişilerin tanıklığı ve görüşleri (tanık gösterme), belgeselin aktardığı tezin güvenilirliğini ve kanıt değerini artırır."
      },
      {
        id: "q_tde9_top11_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Belgesellerde kullanılan dış ses (anlatıcı) ve görsel unsurların bir araya gelmesiyle oluşan anlatım yapısına ne ad verilir?",
        choices: [
          "Çok modlu (Multimodal) metin yapısı",
          "Salt monolog yapısı",
          "Geleneksel manzum yapı",
          "Tiyatro diyalog yapısı"
        ],
        correctAnswer: "Çok modlu (Multimodal) metin yapısı",
        explanation: "Görsel, işitsel, metinsel ve grafik unsurların bir arada anlam ürettiği metin ve medyalara çok modlu (multimodal) metin denir."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_12_anlamin_yapi_taslari_belgeseli_infografige_donusturm__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_12_anlamin_yapi_taslari_belgeseli_infografige_donusturm",
    stableKey: "tde9_quiz_topic_12_infografik_micro",
    displayLabel: "12.1-Q",
    orderKey: 1500,
    title: "İnfografik ve Görsel Metin Dönüştürme Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_12_anlamin_yapi_taslari_belgeseli_infografige_donusturm",
    questions: [
      {
        id: "q_tde9_top12_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Karmaşık bilgilerin, istatistiki verilerin ve süreçlerin grafikler, simgeler ve kısa metinlerle görselleştirildiği tasarımlara ne ad verilir?",
        choices: [
          "İnfografik (Bilgi Grafiği)",
          "Bibliyografya",
          "Dizin",
          "Monografi"
        ],
        correctAnswer: "İnfografik (Bilgi Grafiği)",
        explanation: "İnfografik; verilerin, akışların ve önemli bilgilerin görsel unsurlar, simgeler ve kısa açıklamalarla anlaşılır kılındığı görsel metin türüdür."
      },
      {
        id: "q_tde9_top12_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Bir metni veya belgeseli infografiğe dönüştürürken uzun paragraflar aynen korunmalı, görsel ve şema kullanılmamalıdır.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "FALSE",
        explanation: "İnfografiklerin temel amacı bilgiyi sadeleştirmek, anahtar noktaları öne çıkarmak ve görsel hiyerarşiyle hızlı kavranmasını sağlamaktır."
      },
      {
        id: "q_tde9_top12_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "İnfografik hazırlarken tasarımda bilginin önem sırasına göre renk ve boyut kullanımı hangi kavramla ifade edilir?",
        choices: [
          "Görsel Hiyerarşi",
          "Yazınsal Kurgu",
          "Tipografik Monotonluk",
          "Serim Düzeni"
        ],
        correctAnswer: "Görsel Hiyerarşi",
        explanation: "Görsel hiyerarşi; okuyucunun gözünün en önemli bilgiden ayrıntıya doğru yönlendirilmesini sağlayan boyutlandırma ve renk düzenidir."
      },
      {
        id: "q_tde9_top12_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Belgeselden infografik üretirken ilk yapılması gereken adım hangisidir?",
        choices: [
          "Temel verileri, ana fikirleri ve sayısal göstergeleri süzüp özetlemek",
          "Rastgele resimler seçmek",
          "Yalnızca arka plan rengine karar vermek",
          "Belgeselin tamamını metne döküp sayfayı doldurmak"
        ],
        correctAnswer: "Temel verileri, ana fikirleri ve sayısal göstergeleri süzüp özetlemek",
        explanation: "İnfografik oluşturmanın ilk adımı veri analizi ve içerik özetlemedir; sunulacak kilit noktalar ve veriler önceden belirlenmelidir."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_13_dilin_zenginligi_roman_tiyatro_ve_elestiri__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_13_dilin_zenginligi_roman_tiyatro_ve_elestiri",
    stableKey: "tde9_quiz_topic_13_roman_tiyatro_elestiri_micro",
    displayLabel: "13.1-Q",
    orderKey: 1500,
    title: "Roman, Tiyatro ve Eleştiri Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_13_dilin_zenginligi_roman_tiyatro_ve_elestiri",
    questions: [
      {
        id: "q_tde9_top13_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Bir sanat ya da edebiyat eserinin güçlü ve zayıf yönlerini, değerini belirli ölçütlere dayanarak ortaya koyan yazı türüne ne ad verilir?",
        choices: [
          "Eleştiri (Tenkit)",
          "Röportaj",
          "Anı",
          "Makale"
        ],
        correctAnswer: "Eleştiri (Tenkit)",
        explanation: "Eleştiri (tenkit); bir eserin sanatsal, biçimsel ve içeriksel özelliklerini tarafsız ölçütlerle değerlendiren ve okura rehberlik eden edebî türdür."
      },
      {
        id: "q_tde9_top13_2",
        questionIndex: 1,
        type: "MULTIPLE_CHOICE",
        prompt: "Tiyatro türünde oyuncuların sahnede birbirlerine söyledikleri karşılıklı sözlere ne ad verilir?",
        choices: [
          "Replik",
          "Monolog",
          "Tirat",
          "Jest"
        ],
        correctAnswer: "Replik",
        explanation: "Tiyatroda karşılıklı konuşmadaki her bir söze replik denir. Bir oyuncunun sahnede tek başına uzun konuşmasına tirat, kendi kendine konuşmasına monolog denir."
      },
      {
        id: "q_tde9_top13_3",
        questionIndex: 2,
        type: "TRUE_FALSE",
        prompt: "Roman türü, hikâyeye göre kişi kadrosunun genişliği, mekân ve zaman çeşitliliği ve olay örgüsünün karmaşıklığı bakımından daha kapsamlıdır.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Romanlar hikâyelere kıyasla daha uzun solukludur; yan olaylar, detaylı betimlemeler ve geniş karakter kadrosu barındırır."
      },
      {
        id: "q_tde9_top13_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Geleneksel tiyatro türlerinden olan 'Trajedi'nin temel özelliklerinden biri aşağıdakilerden hangisidir?",
        choices: [
          "Seyircide acıma ve korku duyguları uyandırarak ruhu arındırma (katarsis) amacı gütmesi",
          "Sıradan sokak kişilerinin kaba şakalarına dayanması",
          "Yalnızca dans ve müzikle sözsüz oynanması",
          "Acıklı olaylara kesinlikle yer verilmemesi"
        ],
        correctAnswer: "Seyircide acıma ve korku duyguları uyandırarak ruhu arındırma (katarsis) amacı gütmesi",
        explanation: "Trajedi; seyircide acıma ve korku duyguları uyandırarak ahlaki bir arınma (katarsis) sağlamayı hedefler; soylu kahramanlar ve kusursuz bir üslup kullanır."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_14_dilin_zenginligi_sosyal_medya_ve_edebi_dil_sunumu__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_14_dilin_zenginligi_sosyal_medya_ve_edebi_dil_sunumu",
    stableKey: "tde9_quiz_topic_14_sosyal_medya_dil_micro",
    displayLabel: "14.1-Q",
    orderKey: 1500,
    title: "Sosyal Medya ve Edebî Dil Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_14_dilin_zenginligi_sosyal_medya_ve_edebi_dil_sunumu",
    questions: [
      {
        id: "q_tde9_top14_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Sosyal medya dilinin edebî dilden en belirgin olumsuz farkı ve yarattığı kültürel risk nedir?",
        choices: [
          "Sözcüklerin kısaltılması, imla ve noktalama kurallarının hiçe sayılarak dilin zenginliğinin daraltılması",
          "Edebî eserlerin daha geniş kitlelerce okunmasını sağlaması",
          "Kelime hazinesini sürekli zenginleştirmesi",
          "Duygu ve düşünceleri en derin ve kusursuz biçimde ifade etmesi"
        ],
        correctAnswer: "Sözcüklerin kısaltılması, imla ve noktalama kurallarının hiçe sayılarak dilin zenginliğinin daraltılması",
        explanation: "Sosyal medyada yaygınlaşan aşırı kısaltmalar, yabancı özentisi kelimeler ve yazım kurallarının ihmali Türkçenin zengin ifade gücünü tehdit eder."
      },
      {
        id: "q_tde9_top14_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Edebî dil; yan anlamlar, mecazlar, deyimler ve ses uyumlarıyla dile derinlik ve estetik güç kazandırır.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Edebî dil, günlük konuşma dilinin olanaklarını sanatsal düzeyde zenginleştirerek kelimelere yeni anlam katmanları yükler."
      },
      {
        id: "q_tde9_top14_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "Sosyal medya dilini edebî dil açısından eleştiren bir sunum yapan öğrencinin vermesi gereken en doğru mesaj hangisidir?",
        choices: [
          "Dijital mecralarda iletişim kurarken de Türkçenin doğru kullanımına, zengin kelime dağarcığına ve imla kurallarına özen gösterilmelidir",
          "Sosyal medyada edebiyat tamamen yasaklanmalıdır",
          "Yalnızca tek heceli kelimelerle mesajlaşılmalıdır",
          "İmla kuralları yalnızca basılı kitaplar için geçerlidir"
        ],
        correctAnswer: "Dijital mecralarda iletişim kurarken de Türkçenin doğru kullanımına, zengin kelime dağarcığına ve imla kurallarına özen gösterilmelidir",
        explanation: "İletişimin kalitesi dil bilincine bağlıdır; dijital ortamda da dilimizi doğru, kurallı ve zengin kullanmak dilimizin geleceği için gereklidir."
      },
      {
        id: "q_tde9_top14_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Aşağıdakilerden hangisi dilin zenginliğini ve anlatım gücünü artıran temel ögelerden biri DEĞİLDİR?",
        choices: [
          "Yabancı dillerden rastgele türetilen ve dilin yapısını bozan kelimeler kullanmak",
          "Deyimler ve atasözlerinden yerinde faydalanmak",
          "Kelimelerin çağrışım ve yan anlamlarını bilmek",
          "Eş anlamlı ve zıt anlamlı sözcükleri yerli yerinde kullanmak"
        ],
        correctAnswer: "Yabancı dillerden rastgele türetilen ve dilin yapısını bozan kelimeler kullanmak",
        explanation: "Dile yabancı ve uyumsuz kelimelerin bilinçsizce sokulması dili zenginleştirmez, aksine dil kirliliğine ve yozlaşmaya yol açar."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_15_dilin_zenginligi_otobiyografi_dinleme_izleme__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_15_dilin_zenginligi_otobiyografi_dinleme_izleme",
    stableKey: "tde9_quiz_topic_15_otobiyografi_dinleme_micro",
    displayLabel: "15.1-Q",
    orderKey: 1500,
    title: "Otobiyografi Dinleme ve İnceleme Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_15_dilin_zenginligi_otobiyografi_dinleme_izleme",
    questions: [
      {
        id: "q_tde9_top15_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Bir kişinin kendi hayatını, yaşadığı dönemi ve anılarını bizzat kendisinin kaleme aldığı türe ne ad verilir?",
        choices: [
          "Otobiyografi (Öz Yaşam Öyküsü)",
          "Biyografi (Yaşam Öyküsü)",
          "Monografi",
          "Tezkire"
        ],
        correctAnswer: "Otobiyografi (Öz Yaşam Öyküsü)",
        explanation: "Kişinin kendi hayatını anlatması 'otobiyografi'dir; bir başkasının hayatını anlatması ise 'biyografi' olarak adlandırılır."
      },
      {
        id: "q_tde9_top15_2",
        questionIndex: 1,
        type: "MULTIPLE_CHOICE",
        prompt: "Otobiyografi metinlerinde anlatım genellikle hangi kişi ağzından yapılır?",
        choices: [
          "1. Tekil Kişi (Ben)",
          "3. Tekil Kişi (O)",
          "2. Çoğul Kişi (Siz)",
          "İlahi Anlatıcı (Hakim Bakış)"
        ],
        correctAnswer: "1. Tekil Kişi (Ben)",
        explanation: "Otobiyografi yazarın kendi deneyimlerini anlattığı tür olduğu için 'ben' (1. tekil kişi) anlatımı esastır."
      },
      {
        id: "q_tde9_top15_3",
        questionIndex: 2,
        type: "TRUE_FALSE",
        prompt: "Biyografi yazarı nesnel belgelere ve tanıklara dayanmak zorundayken, otobiyografi yazarının anlatımı doğası gereği daha öznel ve içtendir.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "TRUE",
        explanation: "Biyografi dışarıdan nesnel bir araştırmayla yazılırken, otobiyografi kişinin kendi hafızası, hisleri ve iç dünyasıyla şekillendiğinden özneldir."
      },
      {
        id: "q_tde9_top15_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Divan edebiyatında şairlerin ve önemli kişilerin hayatlarını anlatan biyografik eserlere ne ad verilir?",
        choices: [
          "Tezkire",
          "Münşeat",
          "Siyasetname",
          "Hamse"
        ],
        correctAnswer: "Tezkire",
        explanation: "Divan edebiyatındaki şair biyografilerine 'Tezkiretü'ş-Şuara' (Tezkire) adı verilir."
      }
    ]
  },
  {
    id: "item_tde9_vid_topic_16_dilin_zenginligi_otobiyografi_yazma__quiz",
    courseId: "course_tde_9",
    lessonId: "lesson_tde9_topic_16_dilin_zenginligi_otobiyografi_yazma",
    stableKey: "tde9_quiz_topic_16_otobiyografi_yazma_micro",
    displayLabel: "16.1-Q",
    orderKey: 1500,
    title: "Otobiyografi Yazma Becerisi Mikro Testi",
    derivedFrom: "item_tde9_vid_topic_16_dilin_zenginligi_otobiyografi_yazma",
    questions: [
      {
        id: "q_tde9_top16_1",
        questionIndex: 0,
        type: "MULTIPLE_CHOICE",
        prompt: "Öz yaşam öyküsü (otobiyografi) yazarken olayların ve dönemlerin aktarımında en çok tercih edilen sıralama yöntemi hangisidir?",
        choices: [
          "Kronolojik (Zaman Dizinsel) Sıra",
          "Rastgele ve karmaşık sıralama",
          "Yalnızca geleceğe dair planlar sıralaması",
          "Kelimelerin alfabetik sıralaması"
        ],
        correctAnswer: "Kronolojik (Zaman Dizinsel) Sıra",
        explanation: "Otobiyografide genellikle doğumdan başlanarak çocukluk, okul dönemi ve olgunluk aşamaları zaman sırasına (kronolojiye) göre anlatılır."
      },
      {
        id: "q_tde9_top16_2",
        questionIndex: 1,
        type: "TRUE_FALSE",
        prompt: "Otobiyografi yazarken yazarın sadece başarılarını övmesi, hiçbir zorluk ve hatadan bahsetmemesi anlatımın samimiyet ve inandırıcılığını artırır.",
        choices: ["TRUE", "FALSE"],
        correctAnswer: "FALSE",
        explanation: "Otobiyografide dürüstlük ve içtenlik esastır; yazar sadece başarılarını değil, karşılaştığı zorlukları ve insani yönlerini de samimiyetle aktarmalıdır."
      },
      {
        id: "q_tde9_top16_3",
        questionIndex: 2,
        type: "MULTIPLE_CHOICE",
        prompt: "Otobiyografi yazımında yazarın kendini tanıtırken iç dünyasını, karakter özelliklerini, sevinç ve hüzünlerini aktardığı betimleme türü hangisidir?",
        choices: [
          "Ruhsal (Psikolojik / İç) Portre",
          "Fiziksel Portre",
          "Mekânsal Tasnif",
          "Gözlemci Kayıt"
        ],
        correctAnswer: "Ruhsal (Psikolojik / İç) Portre",
        explanation: "İç dünya, ahlaki özellikler, duygular ve zihniyet ruhsal (psikolojik) portre ile betimlenir."
      },
      {
        id: "q_tde9_top16_4",
        questionIndex: 3,
        type: "MULTIPLE_CHOICE",
        prompt: "Bir otobiyografi metninin yazım sürecinde son aşama olan 'Gözden Geçirme ve Düzeltme' evresinde aşağıdakilerden hangisi yapılır?",
        choices: [
          "İmla, noktalama, anlatım bozuklukları ve mantık akışının denetlenip metnin son haline getirilmesi",
          "Metnin konusunun tamamen değiştirilmesi",
          "Konuyla alakasız başka bir yazarın biyografisinin kopyalanması",
          "Yazının tamamen silinip yeniden başlanması"
        ],
        correctAnswer: "İmla, noktalama, anlatım bozuklukları ve mantık akışının denetlenip metnin son haline getirilmesi",
        explanation: "Yazma sürecinin son basamağı metni dil, anlatım, imla ve tutarlılık açısından gözden geçirip tashih etmektir."
      }
    ]
  }
];

// Write quiz files
for (const q of quizzes) {
  const quizPayload = {
    quiz: {
      quizTitle: q.title,
      questions: q.questions,
      questionCount: q.questions.length,
      schemaVersion: "v2-quiz"
    },
    provenance: {
      derivedFromItemId: q.derivedFrom,
      generatedBy: "gemini",
      reviewStatus: "verified",
      reviewedOverride: true,
      importedAt: "2026-10-03T21:00:00.000Z",
      schemaVersion: "v2",
      fingerprint: sha(q.questions)
    }
  };

  const itemObj = {
    id: q.id,
    courseId: q.courseId,
    lessonId: q.lessonId,
    stableKey: q.stableKey,
    itemType: "QUIZ",
    displayLabel: q.displayLabel,
    orderKey: q.orderKey,
    title: q.title,
    contentUrl: null,
    publishingStatus: "active",
    payload: quizPayload
  };

  const itemPath = path.join(itemsDir, `${q.id}.json`);
  fs.writeFileSync(itemPath, JSON.stringify(itemObj, null, 2), "utf8");
  console.log(`Created quiz item: ${q.id}`);
}

// Update lesson files to reference the new quizzes
for (const q of quizzes) {
  const lessonPath = path.join(lessonsDir, `${q.lessonId}.json`);
  if (fs.existsSync(lessonPath)) {
    const lesson = JSON.parse(fs.readFileSync(lessonPath, "utf8"));
    if (!lesson.items.includes(q.id)) {
      const idx = lesson.items.indexOf(q.derivedFrom);
      if (idx !== -1) {
        lesson.items.splice(idx + 1, 0, q.id);
      } else {
        lesson.items.push(q.id);
      }
      fs.writeFileSync(lessonPath, JSON.stringify(lesson, null, 2), "utf8");
      console.log(`Updated lesson ${q.lessonId} with item ${q.id}`);
    }
  }
}
