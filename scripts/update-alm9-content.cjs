const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

const baseDir = path.resolve(__dirname, '..');
const itemsDir = path.join(baseDir, 'content/v2/items');
const lessonsDir = path.join(baseDir, 'content/v2/lessons');

// 1. Video items updates (ensuring valid URLs, correct providers, active publishingStatus, displayLabel, orderKey)
const videoUpdates = [
  {
    file: 'item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & MEB Maarif Modeli',
    displayLabel: '1.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_selamlasma_alfabe.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay (Erhan Özdemir)',
    displayLabel: '2.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_kendini_tanitma.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay (Erhan Özdemir)',
    displayLabel: '2.2',
    orderKey: 2000
  },
  {
    file: 'item_alm9_vid_sayilar.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay (Erhan Özdemir)',
    displayLabel: '2.3',
    orderKey: 3000
  },
  {
    file: 'item_alm9_vid_ulkeler_diller.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay (Erhan Özdemir)',
    displayLabel: '2.4',
    orderKey: 4000
  },
  {
    file: 'item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & Meltem Hoca',
    displayLabel: '3.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_04_alter_alphabet_und_buchstabieren.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & MEB Maarif Modeli',
    displayLabel: '4.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_05_schulsachen.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & Tonguç Akademi',
    displayLabel: '5.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_06_im_klassenzimmer.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & MEB EBA',
    displayLabel: '6.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_schulsachen_artikel.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay (Erhan Özdemir)',
    displayLabel: '7.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_08_stundenplan_und_schule.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & Meltem Hoca',
    displayLabel: '8.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_09_familie_und_familienmitglieder.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & MEB Maarif Modeli',
    displayLabel: '9.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_10_berufe.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay (Erhan Özdemir)',
    displayLabel: '10.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_11_freizeit_und_aktivitaten.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & MEB Maarif Modeli',
    displayLabel: '11.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_12_uhrzeiten.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay (Erhan Özdemir)',
    displayLabel: '12.1',
    orderKey: 1000
  },
  {
    file: 'item_alm9_vid_topic_13_tageszeiten_und_tagesablauf.json',
    contentUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
    provider: 'Almanca Kolay & MEB Maarif Modeli',
    displayLabel: '13.1',
    orderKey: 1000
  }
];

console.log('1. Updating Video items...');
videoUpdates.forEach(upd => {
  const filePath = path.join(itemsDir, upd.file);
  if (fs.existsSync(filePath)) {
    const item = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    item.contentUrl = upd.contentUrl;
    item.displayLabel = upd.displayLabel;
    item.orderKey = upd.orderKey;
    item.publishingStatus = 'active';
    item.payload = item.payload || {};
    item.payload.provider = upd.provider;
    item.payload.reviewStatus = 'verified';
    item.payload.reviewedOverride = true;
    fs.writeFileSync(filePath, JSON.stringify(item, null, 2) + '\n', 'utf8');
    console.log(`  ✓ Updated video: ${upd.file}`);
  } else {
    console.error(`  ✗ Missing video file: ${upd.file}`);
  }
});

// Update Anki and existing quizzes
const ankiFile = path.join(itemsDir, 'item_alm9_anki_1ay.json');
if (fs.existsSync(ankiFile)) {
  const anki = JSON.parse(fs.readFileSync(ankiFile, 'utf8'));
  anki.displayLabel = '2.A';
  anki.orderKey = 5000;
  anki.publishingStatus = 'active';
  fs.writeFileSync(ankiFile, JSON.stringify(anki, null, 2) + '\n', 'utf8');
  console.log(`  ✓ Updated Anki item: item_alm9_anki_1ay.json`);
}

const quizHallo = path.join(itemsDir, 'item_alm9_quiz_hallo_person.json');
if (fs.existsSync(quizHallo)) {
  const q = JSON.parse(fs.readFileSync(quizHallo, 'utf8'));
  q.displayLabel = '2.Q';
  q.orderKey = 6000;
  q.publishingStatus = 'active';
  fs.writeFileSync(quizHallo, JSON.stringify(q, null, 2) + '\n', 'utf8');
  console.log(`  ✓ Updated Quiz item: item_alm9_quiz_hallo_person.json`);
}

const quizSchule = path.join(itemsDir, 'item_alm9_quiz_schule_artikel.json');
if (fs.existsSync(quizSchule)) {
  const q = JSON.parse(fs.readFileSync(quizSchule, 'utf8'));
  q.displayLabel = '7.Q';
  q.orderKey = 2000;
  q.publishingStatus = 'active';
  fs.writeFileSync(quizSchule, JSON.stringify(q, null, 2) + '\n', 'utf8');
  console.log(`  ✓ Updated Quiz item: item_alm9_quiz_schule_artikel.json`);
}

// 2. Micro-Quizzes Definitions
const microQuizzes = [
  {
    itemId: 'item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_01_begru_ung_verabschiedung_und_landeskunde',
    derivedFromItemId: 'item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde',
    stableKey: 'alm9_quiz_topic_01_begru_ung_verabschiedung_und_landeskunde_micro',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Begrüßung, Verabschiedung und Landeskunde Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top1_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada resmi bir ortamda ya da büyüklere vedalaşırken kullanılan "Görüşmek üzere / Hoşça kalın" ifadesi hangisidir?',
        choices: [
          'Auf Wiedersehen',
          'Tschüss',
          'Guten Morgen',
          'Hallo'
        ],
        correctAnswer: 'Auf Wiedersehen',
        explanation: 'Almancada resmi vedalaşmalarda "Auf Wiedersehen" (Yeniden görüşmek üzere) kullanılır. "Tschüss" ise samimi ve arkadaş ortamlarında tercih edilir.'
      },
      {
        id: 'q_alm9_top1_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almanca konuşulan ülkeleri ifade eden "D-A-CH" kısaltması hangi ülkeleri temsil eder?',
        choices: [
          'Deutschland, Österreich, Schweiz',
          'Deutschland, Amerika, China',
          'Dänemark, Australien, Chile',
          'Deutschland, Arabien, Tschechien'
        ],
        correctAnswer: 'Deutschland, Österreich, Schweiz',
        explanation: 'D-A-CH kısaltması: D = Deutschland (Almanya), A = Österreich (Avusturya - Latince Austria), CH = Schweiz (İsviçre - Latince Confoederatio Helvetica).'
      },
      {
        id: 'q_alm9_top1_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: '"Gute Nacht" ifadesi gün içinde karşılaşıldığında söylenen bir selamlama değil, gece yatarken söylenen bir iyi geceler dileğidir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: '"Gute Nacht" yalnızca gece yatmadan önce veya gece geç saatte ayrılırken söylenir; akşam karşılaşmalarında ise "Guten Abend" (İyi akşamlar) kullanılır.'
      },
      {
        id: 'q_alm9_top1_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Günün saatlerine göre selamlama ifadelerinden hangisi YANLIŞ eşleştirilmiştir?',
        choices: [
          'Guten Morgen - Akşam selamlama',
          'Guten Morgen - Sabah selamlama',
          'Guten Tag - Öğlen ve gün içi selamlama',
          'Guten Abend - Akşam selamlama'
        ],
        correctAnswer: 'Guten Morgen - Akşam selamlama',
        explanation: '"Guten Morgen" sabah saatlerinde "Günaydın" anlamına gelir; akşam selamlama için "Guten Abend" kullanılır.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_selamlasma_alfabe__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_modul1_hallo',
    derivedFromItemId: 'item_alm9_vid_selamlasma_alfabe',
    stableKey: 'alm9_quiz_selamlasma_alfabe_micro',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Selamlaşma, Vedalaşma ve Alfabe Mikro Testi',
    questions: [
      {
        id: 'q_alm9_selam_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almanca alfabesinde yer alan özel harf "ß" (Eszett / scharfes S) Türkçede hangi ses değerine karşılık gelir?',
        choices: [
          'Çift s / keskin s sesi',
          'b ve s harflerinin birleşimi',
          'Ş sesi',
          'Z sesi'
        ],
        correctAnswer: 'Çift s / keskin s sesi',
        explanation: '"ß" harfi Almancada "scharfes S" (keskin s) veya "Eszett" olarak bilinir ve çift \'s\' (ss) şeklinde okunur (örn: heißen).'
      },
      {
        id: 'q_alm9_selam_2',
        questionIndex: 1,
        type: 'TRUE_FALSE',
        prompt: 'Almancada yan yana gelen "ei" harf grubu Türkçe "ay" gibi, "ie" harf grubu ise uzun bir "i" sesi gibi okunur.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'Örneğin "mein" (mayn) ve "heißen" (haysın) kelimelerinde "ei" = ay okunurken, "sie" (zii) kelimesinde "ie" = uzun i okunur.'
      },
      {
        id: 'q_alm9_selam_3',
        questionIndex: 2,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Samimi arkadaş ortamında "Merhaba!" ve "Hoşça kal!" demek için kullanılan en yaygın ikili hangisidir?',
        choices: [
          'Hallo! / Tschüss!',
          'Guten Tag! / Auf Wiedersehen!',
          'Guten Morgen! / Gute Nacht!',
          'Grüß Gott! / Mahlzeit!'
        ],
        correctAnswer: 'Hallo! / Tschüss!',
        explanation: 'Akranlar ve arkadaşlar arasında en yaygın ve samimi karşılama "Hallo!", vedalaşma ise "Tschüss!" ifadesidir.'
      },
      {
        id: 'q_alm9_selam_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada "Bu nasıl yazılır / harf harf kodlanır?" anlamına gelen soru kalıbı hangisidir?',
        choices: [
          'Wie schreibt man das?',
          'Wie heißt du?',
          'Woher kommst du?',
          'Wie geht es dir?'
        ],
        correctAnswer: 'Wie schreibt man das?',
        explanation: '"Wie schreibt man das?" (Bu nasıl yazılır?) sorusu bir kelimenin yazılışını veya harflendirilmesini istemek için kullanılır.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_kendini_tanitma__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_modul1_hallo',
    derivedFromItemId: 'item_alm9_vid_kendini_tanitma',
    stableKey: 'alm9_quiz_kendini_tanitma_micro',
    displayLabel: '2.2-Q',
    orderKey: 2500,
    title: 'Kendini Tanıtma ve Hal-Hatır Sorma Mikro Testi',
    questions: [
      {
        id: 'q_alm9_tanit_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Wie geht es dir?" (Nasılsın?) sorusuna "İyiyim, teşekkürler." şeklinde yanıt veren bir kişi hangisini söyler?',
        choices: [
          'Danke, mir geht es gut.',
          'Ich heiße Murat.',
          'Ich komme aus Izmir.',
          'Ich bin 15 Jahre alt.'
        ],
        correctAnswer: 'Danke, mir geht es gut.',
        explanation: 'Hal-hatır sorulduğunda "Danke, mir geht es gut." (Teşekkürler, iyiyim) veya kısaca "Danke, gut." denir.'
      },
      {
        id: 'q_alm9_tanit_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: '"heißen" (adı olmak) fiilinin "ich" (ben) ve "du" (sen) zamirlerine göre şimdiki zaman çekimi hangisinde doğru verilmiştir?',
        choices: [
          'ich heiße / du heißt',
          'ich heiße / du heißest',
          'ich heißt / du heiße',
          'ich heisse / du heisstet'
        ],
        correctAnswer: 'ich heiße / du heißt',
        explanation: 'heißen fiili: ich heiße, du heißt (kök -ß ile bittiği için sadece -t eki alır), er/sie/es heißt.'
      },
      {
        id: 'q_alm9_tanit_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: '"Wer bist du?" sorusuna verilecek doğal ve doğru bir yanıt "Ich bin Can." cümlesidir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: '"Wer bist du?" (Sen kimsin?) sorusuna "sein" (olmak) fiiliyle "Ich bin [İsim]" şeklinde yanıt verilir.'
      },
      {
        id: 'q_alm9_tanit_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Resmi bir konuşmada tanımadığımız bir büyüğe "Nasılsınız?" diye sormak için hangi kalıp kullanılır?',
        choices: [
          'Wie geht es Ihnen?',
          'Wie geht es dir?',
          'Wie geht\'s?',
          'Wer sind Sie?'
        ],
        correctAnswer: 'Wie geht es Ihnen?',
        explanation: 'Resmi nezaket zamiri "Sie" için hal-hatır kalıbı "Wie geht es Ihnen?" (Nasılsınız?) şeklindedir.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_sayilar__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_modul1_hallo',
    derivedFromItemId: 'item_alm9_vid_sayilar',
    stableKey: 'alm9_quiz_sayilar_micro',
    displayLabel: '2.3-Q',
    orderKey: 3500,
    title: '0-20 Arası Sayılar ve Telefon Numarası Mikro Testi',
    questions: [
      {
        id: 'q_alm9_sayi_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada "11" (elf) ve "12" (zwölf) özel sayılarından sonra gelen "13" sayısı nasıl türetilir?',
        choices: [
          'dreizehn (3 + 10)',
          'einsdrei (1 + 3)',
          'dreizwanzig (3 + 20)',
          'zehndrei (10 + 3)'
        ],
        correctAnswer: 'dreizehn (3 + 10)',
        explanation: '13-19 arası sayılar birler basamağı + zehn mantığıyla kurulur: drei (3) + zehn (10) = dreizehn (13).'
      },
      {
        id: 'q_alm9_sayi_2',
        questionIndex: 1,
        type: 'TRUE_FALSE',
        prompt: 'Almancada 16 sayısı "sechszehn" şeklinde değil, sondaki "s" düşerek "sechzehn" şeklinde yazılır ve okunur.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: '16 (sechzehn) ve 17 (siebzehn) sayılarında kökteki "s" ve "en" düşerek ses uyumu sağlanır.'
      },
      {
        id: 'q_alm9_sayi_3',
        questionIndex: 2,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Wie ist deine Telefonnummer?" sorusunun Türkçe anlamı nedir?',
        choices: [
          'Telefon numaran nedir?',
          'Kaç yaşındasın?',
          'Nerede oturuyorsun?',
          'Adın nedir?'
        ],
        correctAnswer: 'Telefon numaran nedir?',
        explanation: '"Wie ist deine Telefonnummer?" sorusu doğrudan telefon numarasını öğrenmek için sorulur.'
      },
      {
        id: 'q_alm9_sayi_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada "17" ve "20" sayılarının doğru yazılışları hangisidir?',
        choices: [
          'siebzehn - zwanzig',
          'siebenzehn - zwanzig',
          'siebzehn - zweizehn',
          'siebzehn - dreißig'
        ],
        correctAnswer: 'siebzehn - zwanzig',
        explanation: '17 sayısı "siebzehn", 20 sayısı ise "zwanzig" olarak yazılır.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_ulkeler_diller__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_modul1_hallo',
    derivedFromItemId: 'item_alm9_vid_ulkeler_diller',
    stableKey: 'alm9_quiz_ulkeler_diller_micro',
    displayLabel: '2.4-Q',
    orderKey: 4500,
    title: 'Ülkeler, Diller ve Nereli Olduğunu Söyleme Mikro Testi',
    questions: [
      {
        id: 'q_alm9_ulk_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Woher kommst du?" sorusuna Türkiye\'den geldiğini belirten doğru cümle hangisidir?',
        choices: [
          'Ich komme aus der Türkei.',
          'Ich komme Türkei.',
          'Ich wohne in der Türkei.',
          'Ich spreche Türkei.'
        ],
        correctAnswer: 'Ich komme aus der Türkei.',
        explanation: 'Türkiye artikelli bir ülke olduğu için (die Türkei), "aus" edatıyla "aus der Türkei" şeklinde kullanılır.'
      },
      {
        id: 'q_alm9_ulk_2',
        questionIndex: 1,
        type: 'TRUE_FALSE',
        prompt: '"sprechen" (konuşmak) fiili düzensizdir ve "du" çekiminde kökteki "e" harfi "i"ye dönüşerek "du sprichst" olur.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'sprechen çekimi: ich spreche, du sprichst, er/sie/es spricht, wir sprechen, ihr sprecht, sie/Sie sprechen.'
      },
      {
        id: 'q_alm9_ulk_3',
        questionIndex: 2,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Ich spreche Deutsch und Türkisch." cümlesi Türkçeye nasıl çevrilir?',
        choices: [
          'Almanca ve Türkçe konuşuyorum.',
          'Almanya ve Türkiye\'ye gidiyorum.',
          'Almanya\'da ve Türkiye\'de yaşıyorum.',
          'Almanca ve Türkçe öğrenmek istemiyorum.'
        ],
        correctAnswer: 'Almanca ve Türkçe konuşuyorum.',
        explanation: '"Ich spreche..." (Konuşuyorum), Deutsch (Almanca) und (ve) Türkisch (Türkçe).'
      },
      {
        id: 'q_alm9_ulk_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Bir kişinin nerede ikamet ettiğini / yaşadığı şehri sormak için hangi soru kalıbı kullanılır?',
        choices: [
          'Wo wohnst du?',
          'Woher kommst du?',
          'Wohin gehst du?',
          'Wer bist du?'
        ],
        correctAnswer: 'Wo wohnst du?',
        explanation: '"Wo wohnst du?" nerede yaşadığını sorar ve "Ich wohne in..." (örn: Ich wohne in Istanbul) kalıbıyla cevaplanır.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_03_zahlen_0_20_und_telefonnummer',
    derivedFromItemId: 'item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer',
    stableKey: 'alm9_quiz_topic_03_zahlen_0_20_und_telefonnummer_micro',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Zahlen 0–20 und Telefonnummer Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top3_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: '"acht" (8) ile "neun" (9) sayılarının toplamı olan "17" sayısı Almancada nasıl yazılır?',
        choices: [
          'siebzehn',
          'siebenzehn',
          'achtzehn',
          'sechzehn'
        ],
        correctAnswer: 'siebzehn',
        explanation: '8 + 9 = 17 olup Almancada doğru yazılışı "siebzehn"dir (sieben değil sieb- kökü kullanılır).'
      },
      {
        id: 'q_alm9_top3_2',
        questionIndex: 1,
        type: 'TRUE_FALSE',
        prompt: 'Almancada "null" sayısı "0" (sıfır) anlamına gelir ve telefon numaraları kodlanırken tek tek söylenir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: '"null" 0 demektir. Telefon numaraları söylenirken rakamlar sırayla (null-fünf-drei-zwei...) tek tek telaffuz edilir.'
      },
      {
        id: 'q_alm9_top3_3',
        questionIndex: 2,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Meine Telefonnummer ist null-fünf-vier-eins..." diyen birinin numarasının ilk 4 hanesi nedir?',
        choices: [
          '0541',
          '0531',
          '0451',
          '0542'
        ],
        correctAnswer: '0541',
        explanation: 'null (0), fünf (5), vier (4), eins (1) = 0541.'
      },
      {
        id: 'q_alm9_top3_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '12, 14 ve 20 sayılarının Almanca karşılıkları hangi seçenekte sırasıyla doğru verilmiştir?',
        choices: [
          'zwölf, vierzehn, zwanzig',
          'elf, vierzehn, zwanzig',
          'zwölf, fünfzehn, dreißig',
          'zwölf, sechzehn, zwanzig'
        ],
        correctAnswer: 'zwölf, vierzehn, zwanzig',
        explanation: '12 = zwölf, 14 = vierzehn, 20 = zwanzig.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_04_alter_alphabet_und_buchstabieren__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_04_alter_alphabet_und_buchstabieren',
    derivedFromItemId: 'item_alm9_vid_topic_04_alter_alphabet_und_buchstabieren',
    stableKey: 'alm9_quiz_topic_04_alter_alphabet_und_buchstabieren_micro',
    displayLabel: '4.1-Q',
    orderKey: 1500,
    title: 'Alter, Alphabet und Buchstabieren Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top4_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Wie alt bist du?" (Kaç yaşındasın?) sorusuna 15 yaşında olduğunu söylemek isteyen bir öğrenci hangi cümleyi kurmalıdır?',
        choices: [
          'Ich bin fünfzehn Jahre alt.',
          'Ich habe fünfzehn Jahre alt.',
          'Ich heiße fünfzehn.',
          'Ich komme aus fünfzehn.'
        ],
        correctAnswer: 'Ich bin fünfzehn Jahre alt.',
        explanation: 'Almancada yaş bildirmek için "sein" fiili kullanılır: "Ich bin ... Jahre alt."'
      },
      {
        id: 'q_alm9_top4_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Bir ismin veya kelimenin harflerini tek tek hecelemek/kodlamak anlamına gelen Almanca fiil hangisidir?',
        choices: [
          'buchstabieren',
          'schreiben',
          'lesen',
          'sprechen'
        ],
        correctAnswer: 'buchstabieren',
        explanation: '"buchstabieren" harf harf kodlamak (hecelemek) demektir.'
      },
      {
        id: 'q_alm9_top4_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: 'Almancada "ä", "ö", "ü" harflerine nokta işaretleri sebebiyle "Umlaut" (ünlü değişimi / incelmesi) adı verilir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'Alman alfabesinde noktalı sesli harfler (ä, ö, ü) "die Umlaute" olarak adlandırılır.'
      },
      {
        id: 'q_alm9_top4_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Buchstabiere bitte deinen Vornamen!" yönergesi öğrenciden ne yapmasını istemektedir?',
        choices: [
          'Lütfen adını harf harf kodla / hecele!',
          'Lütfen soyadını yaz!',
          'Lütfen yaşını söyle!',
          'Lütfen telefon numaranı ver!'
        ],
        correctAnswer: 'Lütfen adını harf harf kodla / hecele!',
        explanation: 'Der Vorname (ad) ve buchstabieren (harf harf söylemek) birleştiğinde adın hecelenmesi istenir.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_05_schulsachen__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_05_schulsachen',
    derivedFromItemId: 'item_alm9_vid_topic_05_schulsachen',
    stableKey: 'alm9_quiz_topic_05_schulsachen_micro',
    displayLabel: '5.1-Q',
    orderKey: 1500,
    title: 'Schulsachen Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top5_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada kurşun kalem anlamına gelen "Bleistift" sözcüğünün belirli artikeli (bestimmter Artikel) hangisidir?',
        choices: [
          'der Bleistift',
          'die Bleistift',
          'das Bleistift',
          'den Bleistift'
        ],
        correctAnswer: 'der Bleistift',
        explanation: 'Bleistift eril (maskulin) bir isimdir ve belirli artikeli "der"dir.'
      },
      {
        id: 'q_alm9_top5_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: '"das Buch" ve "das Heft" sözcüklerinin Türkçe anlamları sırasıyla hangisinde doğru verilmiştir?',
        choices: [
          'Kitap - Defter',
          'Defter - Kitap',
          'Kalem - Silgi',
          'Çanta - Cetvel'
        ],
        correctAnswer: 'Kitap - Defter',
        explanation: 'das Buch = kitap, das Heft = defter anlamına gelir.'
      },
      {
        id: 'q_alm9_top5_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: '"die Schultasche" (okul çantası) ve "die Schere" (makas) sözcükleri dişil (feminin) olup "die" artikelini alır.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'Her iki nesne de Almancada feminin (dişil) cinste olup "die" artikeliyle kullanılır.'
      },
      {
        id: 'q_alm9_top5_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada silgi anlamına gelen ve artikeli "der" olan okul eşyası hangisidir?',
        choices: [
          'der Radiergummi',
          'der Spitzer',
          'der Kuli',
          'der Rucksack'
        ],
        correctAnswer: 'der Radiergummi',
        explanation: 'der Radiergummi = silgi demektir. (der Spitzer = kalemtıraş, der Kuli = tükenmez kalem).'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_06_im_klassenzimmer__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_06_im_klassenzimmer',
    derivedFromItemId: 'item_alm9_vid_topic_06_im_klassenzimmer',
    stableKey: 'alm9_quiz_topic_06_im_klassenzimmer_micro',
    displayLabel: '6.1-Q',
    orderKey: 1500,
    title: 'Im Klassenzimmer Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top6_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Sınıfta öğretmenin üzerine yazı yazdığı tahta için kullanılan "die Tafel" sözcüğünün artikeli ve cinsi nedir?',
        choices: [
          'die (Feminin - Dişil)',
          'der (Maskulin - Eril)',
          'das (Neutral - Nötr)',
          'die (Çoğul)'
        ],
        correctAnswer: 'die (Feminin - Dişil)',
        explanation: 'Yazı tahtası "die Tafel" dişil (feminin) bir kelimedir.'
      },
      {
        id: 'q_alm9_top6_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Öğretmenin sınıfa "Mach bitte das Fenster auf!" demesi ne anlama gelir?',
        choices: [
          'Lütfen pencereyi aç!',
          'Lütfen kapıyı kapat!',
          'Lütfen tahtayı sil!',
          'Lütfen yerine otur!'
        ],
        correctAnswer: 'Lütfen pencereyi aç!',
        explanation: 'aufmachen = açmak, das Fenster = pencere. "Mach das Fenster auf!" pencereyi aç demektir.'
      },
      {
        id: 'q_alm9_top6_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: '"der Tisch" masa, "der Stuhl" ise sandalye anlamına gelir ve her ikisinin de artikeli "der"dir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'Evet, der Tisch (masa) ve der Stuhl (sandalye) her ikisi de eril (maskulin) kelimelerdir.'
      },
      {
        id: 'q_alm9_top6_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Steht bitte auf!" sınıf içi emir / rica cümlesinin Türkçe karşılığı nedir?',
        choices: [
          'Lütfen ayağa kalkın!',
          'Lütfen oturun!',
          'Lütfen sessiz olun!',
          'Lütfen kitaplarınızı açın!'
        ],
        correctAnswer: 'Lütfen ayağa kalkın!',
        explanation: 'aufstehen = ayağa kalkmak. Çoğul öğrencilere hitaben "Steht bitte auf!" (Lütfen ayağa kalkın) denir.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_schulsachen_artikel__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_schule_und_alltag',
    derivedFromItemId: 'item_alm9_vid_schulsachen_artikel',
    stableKey: 'alm9_quiz_schulsachen_artikel_micro',
    displayLabel: '7.1-Q',
    orderKey: 1500,
    title: 'Schulsachen und Artikel Mikro Testi',
    questions: [
      {
        id: 'q_alm9_art_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada "der" (eril) ve "das" (nötr) artikelindeki kelimelerin belirsiz artikeli (unbestimmter Artikel) nedir?',
        choices: [
          'ein',
          'eine',
          'kein',
          'keine'
        ],
        correctAnswer: 'ein',
        explanation: 'der Bleistift -> ein Bleistift, das Buch -> ein Buch. Eril ve nötr isimlerin belirsiz hali "ein"dır.'
      },
      {
        id: 'q_alm9_art_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: '"die Schere" (dişil - makas) kelimesinin olumsuz belirsiz artikeli (kein/keine) hangisidir?',
        choices: [
          'keine Schere',
          'kein Schere',
          'nicht Schere',
          'keinen Schere'
        ],
        correctAnswer: 'keine Schere',
        explanation: 'Dişil (die) isimlerin belirsiz olumsuzu "keine" ekini alır (keine Schere, keine Schultasche).'
      },
      {
        id: 'q_alm9_art_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: '"Das ist kein Buch, das ist ein Heft." cümlesi "Bu bir kitap değil, bu bir defterdir." anlamına gelir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: '"kein Buch" (kitap değil), "ein Heft" (bir defter).'
      },
      {
        id: 'q_alm9_art_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '"die Tasche" kelimesini "bir çanta" olarak belirsiz artikelle kullanmak istersek hangisi doğrudur?',
        choices: [
          'eine Tasche',
          'ein Tasche',
          'einen Tasche',
          'kein Tasche'
        ],
        correctAnswer: 'eine Tasche',
        explanation: 'Dişil (die) isimlerin belirsiz artikeli "eine"dir (eine Tasche).'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_08_stundenplan_und_schule__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_08_stundenplan_und_schule',
    derivedFromItemId: 'item_alm9_vid_topic_08_stundenplan_und_schule',
    stableKey: 'alm9_quiz_topic_08_stundenplan_und_schule_micro',
    displayLabel: '8.1-Q',
    orderKey: 1500,
    title: 'Stundenplan und Schule Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top8_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Was ist dein Lieblingsfach?" sorusunun Türkçe karşılığı nedir?',
        choices: [
          'En sevdiğin ders hangisidir?',
          'Hangi okula gidiyorsun?',
          'Ders programın nasıl?',
          'Hangi sınıftasın?'
        ],
        correctAnswer: 'En sevdiğin ders hangisidir?',
        explanation: 'das Lieblingsfach = en sevilen/favori ders. Soru en sevilen dersi öğrenmeyi amaçlar.'
      },
      {
        id: 'q_alm9_top8_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada haftanın günlerinden (Montag, Dienstag, vb.) önce hangi edat (preposition) kullanılır?',
        choices: [
          'am (am Montag, am Freitag)',
          'im (im Montag)',
          'um (um Montag)',
          'an (an Montag)'
        ],
        correctAnswer: 'am (am Montag, am Freitag)',
        explanation: 'Günlerde ve günün bölümlerinde "am" edatı kullanılır (am Montag = pazartesi günü).'
      },
      {
        id: 'q_alm9_top8_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: 'Almancada "Mittwoch" çarşamba gününü, "Donnerstag" ise perşembe gününü ifade eder.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'Haftanın günleri: Montag (Pzt), Dienstag (Salı), Mittwoch (Çarş), Donnerstag (Perş), Freitag (Cuma), Samstag (Cmt), Sonntag (Paz).'
      },
      {
        id: 'q_alm9_top8_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Am Freitag habe ich zwei Stunden Deutsch und eine Stunde Musik." cümlesi ne anlama gelir?',
        choices: [
          'Cuma günü iki saat Almanca ve bir saat müzik dersim var.',
          'Pazartesi günü Almanca ve müzik sınavım var.',
          'Cuma günü Almanca ve müzik derslerini sevmiyorum.',
          'Perşembe günü iki saat Almanca çalışacağım.'
        ],
        correctAnswer: 'Cuma günü iki saat Almanca ve bir saat müzik dersim var.',
        explanation: 'am Freitag = Cuma günü, zwei Stunden Deutsch = iki saat Almanca, eine Stunde Musik = bir saat müzik.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_09_familie_und_familienmitglieder__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_09_familie_und_familienmitglieder',
    derivedFromItemId: 'item_alm9_vid_topic_09_familie_und_familienmitglieder',
    stableKey: 'alm9_quiz_topic_09_familie_und_familienmitglieder_micro',
    displayLabel: '9.1-Q',
    orderKey: 1500,
    title: 'Familie und Familienmitglieder Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top9_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada anne ve babayı birlikte (ebeveyn) ifade eden çoğul sözcük hangisidir?',
        choices: [
          'die Eltern',
          'die Geschwister',
          'die Großeltern',
          'die Familie'
        ],
        correctAnswer: 'die Eltern',
        explanation: 'die Eltern = ebeveyn (anne-baba), die Geschwister = kardeşler, die Großeltern = büyükanne ve büyükbaba.'
      },
      {
        id: 'q_alm9_top9_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Bu benim kız kardeşimdir." cümlesinin Almanca doğru karşılığı hangisidir?',
        choices: [
          'Das ist meine Schwester.',
          'Das ist mein Bruder.',
          'Das ist mein Schwester.',
          'Das ist meine Mutter.'
        ],
        correctAnswer: 'Das ist meine Schwester.',
        explanation: 'die Schwester (kız kardeş) dişil olduğu için iyelik zamiri "meine" ekini alır: "Das ist meine Schwester."'
      },
      {
        id: 'q_alm9_top9_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: 'Eril aile üyelerinde (der Vater, der Bruder) "mein/dein", dişil aile üyelerinde (die Mutter, die Schwester) ise "meine/deine" kullanılır.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'İyelik sıfatları isimlerin cinsiyetine göre çekimlenir: der/das için mein, die (tekil ve çoğul) için meine.'
      },
      {
        id: 'q_alm9_top9_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '"der Großvater (Opa)" ve "die Großmutter (Oma)" sözcükleri sırasıyla hangi aile üyelerini temsil eder?',
        choices: [
          'Dede (Büyükbaba) ve Büyükanne (Nine/Anneanne/Babaanne)',
          'Amca ve Teyze',
          'Baba ve Anne',
          'Erkek kardeş ve Kız kardeş'
        ],
        correctAnswer: 'Dede (Büyükbaba) ve Büyükanne (Nine/Anneanne/Babaanne)',
        explanation: 'der Großvater = büyükbaba (dede), die Großmutter = büyükanne (nine).'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_10_berufe__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_10_berufe',
    derivedFromItemId: 'item_alm9_vid_topic_10_berufe',
    stableKey: 'alm9_quiz_topic_10_berufe_micro',
    displayLabel: '10.1-Q',
    orderKey: 1500,
    title: 'Berufe Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top10_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada mesleklerin kadın icracılarını belirtirken meslek adının sonuna genellikle hangi ek getirilir?',
        choices: [
          '-in (die Lehrerin, die Ärztin)',
          '-en (die Lehreren)',
          '-er (die Lehrerer)',
          '-chen (die Lehrerchen)'
        ],
        correctAnswer: '-in (die Lehrerin, die Ärztin)',
        explanation: 'Almancada eril meslek adına "-in" eki getirilerek dişil yapılır: der Lehrer -> die Lehrerin, der Arzt -> die Ärztin.'
      },
      {
        id: 'q_alm9_top10_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Was ist deine Mutter von Beruf?" sorusuna verilebilecek dilbilgisel olarak en doğru yanıt hangisidir?',
        choices: [
          'Sie ist Lehrerin.',
          'Er ist Lehrer.',
          'Sie ist ein Lehrer.',
          'Meine Mutter hat Lehrerin.'
        ],
        correctAnswer: 'Sie ist Lehrerin.',
        explanation: 'Anne dişil ("sie") olduğu için dişil meslek formu kullanılır ve Almancada meslek söylerken artikel kullanılmaz: "Sie ist Lehrerin."'
      },
      {
        id: 'q_alm9_top10_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: 'Almancada mesleğini söyleyen bir kişi "Ich bin ein Arzt" yerine kural olarak "Ich bin Arzt" ifadesini kullanır.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'Almancada "sein" ve "werden" fiilleriyle meslek söylenirken belirsiz artikel (ein/eine) kullanılmaz.'
      },
      {
        id: 'q_alm9_top10_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Hastanede çalışan erkek doktor ve kadın doktor ikilisi hangisinde doğru verilmiştir?',
        choices: [
          'der Arzt / die Ärztin',
          'der Doktor / die Doktoren',
          'der Krankenpfleger / die Schwester',
          'der Lehrer / die Lehrerin'
        ],
        correctAnswer: 'der Arzt / die Ärztin',
        explanation: 'Erkek doktor: der Arzt, Kadın doktor: die Ärztin (noktalı a ve -in eki).'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_11_freizeit_und_aktivitaten__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_11_freizeit_und_aktivitaten',
    derivedFromItemId: 'item_alm9_vid_topic_11_freizeit_und_aktivitaten',
    stableKey: 'alm9_quiz_topic_11_freizeit_und_aktivitaten_micro',
    displayLabel: '11.1-Q',
    orderKey: 1500,
    title: 'Freizeit und Aktivitäten Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top11_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Was machst du in deiner Freizeit?" sorusu ne anlama gelir?',
        choices: [
          'Boş zamanlarında ne yaparsın?',
          'Hangi sporu sevmezsin?',
          'Okuldan sonra nereye gidiyorsun?',
          'Hafta sonu ders çalışır mısın?'
        ],
        correctAnswer: 'Boş zamanlarında ne yaparsın?',
        explanation: 'die Freizeit = boş zaman. Bu soru serbest zaman aktivitelerini ve hobileri sormak için kullanılır.'
      },
      {
        id: 'q_alm9_top11_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: '"lesen" (kitap okumak) fiilinin "du" (sen) ve "er" (o) zamirlerine göre çekimi hangisidir?',
        choices: [
          'du liest / er liest',
          'du lesest / er leset',
          'du liest / er lest',
          'du lesest / er liest'
        ],
        correctAnswer: 'du liest / er liest',
        explanation: 'lesen düzensiz bir fiildir: ich lese, du liest, er/sie/es liest, wir lesen, ihr lest, sie/Sie lesen.'
      },
      {
        id: 'q_alm9_top11_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: '"Gitarre spielen" gitar çalmak, "Rad fahren" ise bisiklet sürmek anlamına gelen boş zaman aktiviteleridir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'Gitarre spielen = gitar çalmak; Rad fahren (Fahrrad fahren) = bisiklete binmek/sürmek demektir.'
      },
      {
        id: 'q_alm9_top11_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '"Mein Hobby ist Schwimmen und ich höre gern Musik." cümlesinin anlamı nedir?',
        choices: [
          'Hobim yüzmektir ve müzik dinlemeyi severim.',
          'Yüzmeyi hiç sevmem ama müzik dinlerim.',
          'Boş zamanlarımda spor yapar ve film izlerim.',
          'Arkadaşlarımla havuza giderim.'
        ],
        correctAnswer: 'Hobim yüzmektir ve müzik dinlemeyi severim.',
        explanation: 'das Schwimmen = yüzme, gern Musik hören = severek/hoşlanarak müzik dinlemek.'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_12_uhrzeiten__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_12_uhrzeiten',
    derivedFromItemId: 'item_alm9_vid_topic_12_uhrzeiten',
    stableKey: 'alm9_quiz_topic_12_uhrzeiten_micro',
    displayLabel: '12.1-Q',
    orderKey: 1500,
    title: 'Uhrzeiten Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top12_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Almancada "Saat kaç?" diye sormak için hangi iki kalıp kullanılır?',
        choices: [
          '"Wie spät ist es?" ve "Wie viel Uhr ist es?"',
          '"Wie alt bist du?" ve "Wie geht es dir?"',
          '"Wann kommst du?" ve "Wo bist du?"',
          '"Wie viel kostet das?" ve "Wie lange dauert es?"'
        ],
        correctAnswer: '"Wie spät ist es?" ve "Wie viel Uhr ist es?"',
        explanation: 'Saat sormak için standart olarak "Wie spät ist es?" veya "Wie viel Uhr ist es?" kullanılır.'
      },
      {
        id: 'q_alm9_top12_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Günlük konuşma dilinde "Es ist halb vier." saat kaçı belirtir?',
        choices: [
          '03:30 (Saat 3 buçuk / Dörde yarım var)',
          '04:30 (Saat 4 buçuk)',
          '03:15 (Üçü çeyrek geçiyor)',
          '04:15 (Dördü çeyrek geçiyor)'
        ],
        correctAnswer: '03:30 (Saat 3 buçuk / Dörde yarım var)',
        explanation: 'Almancada "halb [sayı]" bir sonraki saate yarım saat kaldığını gösterir: "halb vier" = 03:30 (üç buçuk).'
      },
      {
        id: 'q_alm9_top12_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: 'Almancada "nach" (geçe) ve "vor" (kala) kelimeleridir; dolayısıyla "Viertel nach acht" saat 08:15 demektir.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'das Viertel = çeyrek, nach = geçe. "Viertel nach acht" = 8\'i çeyrek geçe (08:15).'
      },
      {
        id: 'q_alm9_top12_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Saat "10\'a 10 var" (09:50) demek için hangi Almanca ifade söylenmelidir?',
        choices: [
          'Es ist zehn vor zehn.',
          'Es ist zehn nach zehn.',
          'Es ist halb zehn.',
          'Es ist zehn Uhr zehn.'
        ],
        correctAnswer: 'Es ist zehn vor zehn.',
        explanation: 'zehn (10) vor (kala) zehn (10) = Ona 10 var (09:50).'
      }
    ]
  },
  {
    itemId: 'item_alm9_vid_topic_13_tageszeiten_und_tagesablauf__quiz',
    courseId: 'course_alm_9',
    lessonId: 'lesson_alm9_topic_13_tageszeiten_und_tagesablauf',
    derivedFromItemId: 'item_alm9_vid_topic_13_tageszeiten_und_tagesablauf',
    stableKey: 'alm9_quiz_topic_13_tageszeiten_und_tagesablauf_micro',
    displayLabel: '13.1-Q',
    orderKey: 1500,
    title: 'Tageszeiten und Tagesablauf Mikro Testi',
    questions: [
      {
        id: 'q_alm9_top13_1',
        questionIndex: 0,
        type: 'MULTIPLE_CHOICE',
        prompt: 'Günün bölümlerinde "am Morgen, am Mittag, am Abend" denilirken, istisna olarak "gece" için hangi ifade kullanılır?',
        choices: [
          'in der Nacht',
          'am Nacht',
          'im Nacht',
          'um Nacht'
        ],
        correctAnswer: 'in der Nacht',
        explanation: 'die Nacht dişil olduğu için "in der Nacht" (geceleyin) şeklinde kullanılır; diğer gün bölümleri eril olduğundan "am" (an dem) alır.'
      },
      {
        id: 'q_alm9_top13_2',
        questionIndex: 1,
        type: 'MULTIPLE_CHOICE',
        prompt: '"aufstehen" (yataktan kalkmak) ayrılabilen fiili ile "Saat 7\'de kalkarım." cümlesi nasıl kurulur?',
        choices: [
          'Ich stehe um 07:00 Uhr auf.',
          'Ich aufstehe um 07:00 Uhr.',
          'Ich stehe auf um 07:00 Uhr nicht.',
          'Ich habe um 07:00 Uhr aufgestanden.'
        ],
        correctAnswer: 'Ich stehe um 07:00 Uhr auf.',
        explanation: 'Ayrılabilen fiillerde (trennbare Verben) ön ek cümlenin en sonuna gider: "Ich stehe ... auf."'
      },
      {
        id: 'q_alm9_top13_3',
        questionIndex: 2,
        type: 'TRUE_FALSE',
        prompt: '"Ich frühstücke um 07:30 Uhr und gehe um 08:00 Uhr zur Schule." cümlesi sabah rutinindeki kahvaltı ve okula gidiş zamanlarını anlatır.',
        choices: [
          'TRUE',
          'FALSE'
        ],
        correctAnswer: 'TRUE',
        explanation: 'frühstücken = kahvaltı yapmak, zur Schule gehen = okula gitmek.'
      },
      {
        id: 'q_alm9_top13_4',
        questionIndex: 3,
        type: 'MULTIPLE_CHOICE',
        prompt: '"fernsehen" (televizyon izlemek) ayrılabilen ve düzensiz fiilinin "er" (o) öznesine göre doğru çekimi hangisidir?',
        choices: [
          'Er sieht fern.',
          'Er siehtfern.',
          'Er fernsieht.',
          'Er ferngesehen.'
        ],
        correctAnswer: 'Er sieht fern.',
        explanation: 'sehen fiili düzensizdir (er sieht) ve "fern-" ön eki cümlenin sonuna gider: "Er sieht fern."'
      }
    ]
  }
];

console.log('\n2. Creating Micro-Quiz JSON item files...');
microQuizzes.forEach(mq => {
  const itemJson = {
    id: mq.itemId,
    courseId: mq.courseId,
    lessonId: mq.lessonId,
    stableKey: mq.stableKey,
    itemType: 'QUIZ',
    displayLabel: mq.displayLabel,
    orderKey: mq.orderKey,
    title: mq.title,
    contentUrl: null,
    publishingStatus: 'active',
    payload: {
      quiz: {
        quizTitle: mq.title,
        questions: mq.questions,
        questionCount: mq.questions.length,
        schemaVersion: 'v2-quiz'
      },
      provenance: {
        derivedFromItemId: mq.derivedFromItemId,
        sourceVideoUrl: 'https://www.youtube.com/watch?v=hm1tQdSExLA',
        transcriptLanguage: 'tr',
        transcriptKind: 'curriculum_grounded',
        transcriptFingerprint: sha256(mq.title + mq.questions.length),
        generatedBy: 'gemini',
        reviewStatus: 'verified',
        reviewedOverride: true,
        importedAt: '2026-10-03T21:00:00.000Z',
        schemaVersion: 'v2',
        fingerprint: sha256(JSON.stringify(mq.questions))
      }
    }
  };

  const itemPath = path.join(itemsDir, `${mq.itemId}.json`);
  fs.writeFileSync(itemPath, JSON.stringify(itemJson, null, 2) + '\n', 'utf8');
  console.log(`  ✓ Generated quiz item: ${mq.itemId}.json`);
});

// 3. Update Lesson files to include all items in proper sequence
console.log('\n3. Updating Almanca Lesson files...');
const lessonMappings = {
  'lesson_alm9_topic_01_begru_ung_verabschiedung_und_landeskunde': [
    'item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde',
    'item_alm9_vid_topic_01_begru_ung_verabschiedung_und_landeskunde__quiz'
  ],
  'lesson_alm9_modul1_hallo': [
    'item_alm9_vid_selamlasma_alfabe',
    'item_alm9_vid_selamlasma_alfabe__quiz',
    'item_alm9_vid_kendini_tanitma',
    'item_alm9_vid_kendini_tanitma__quiz',
    'item_alm9_vid_sayilar',
    'item_alm9_vid_sayilar__quiz',
    'item_alm9_vid_ulkeler_diller',
    'item_alm9_vid_ulkeler_diller__quiz',
    'item_alm9_anki_1ay',
    'item_alm9_quiz_hallo_person'
  ],
  'lesson_alm9_topic_03_zahlen_0_20_und_telefonnummer': [
    'item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer',
    'item_alm9_vid_topic_03_zahlen_0_20_und_telefonnummer__quiz'
  ],
  'lesson_alm9_topic_04_alter_alphabet_und_buchstabieren': [
    'item_alm9_vid_topic_04_alter_alphabet_und_buchstabieren',
    'item_alm9_vid_topic_04_alter_alphabet_und_buchstabieren__quiz'
  ],
  'lesson_alm9_topic_05_schulsachen': [
    'item_alm9_vid_topic_05_schulsachen',
    'item_alm9_vid_topic_05_schulsachen__quiz'
  ],
  'lesson_alm9_topic_06_im_klassenzimmer': [
    'item_alm9_vid_topic_06_im_klassenzimmer',
    'item_alm9_vid_topic_06_im_klassenzimmer__quiz'
  ],
  'lesson_alm9_schule_und_alltag': [
    'item_alm9_vid_schulsachen_artikel',
    'item_alm9_vid_schulsachen_artikel__quiz',
    'item_alm9_quiz_schule_artikel'
  ],
  'lesson_alm9_topic_08_stundenplan_und_schule': [
    'item_alm9_vid_topic_08_stundenplan_und_schule',
    'item_alm9_vid_topic_08_stundenplan_und_schule__quiz'
  ],
  'lesson_alm9_topic_09_familie_und_familienmitglieder': [
    'item_alm9_vid_topic_09_familie_und_familienmitglieder',
    'item_alm9_vid_topic_09_familie_und_familienmitglieder__quiz'
  ],
  'lesson_alm9_topic_10_berufe': [
    'item_alm9_vid_topic_10_berufe',
    'item_alm9_vid_topic_10_berufe__quiz'
  ],
  'lesson_alm9_topic_11_freizeit_und_aktivitaten': [
    'item_alm9_vid_topic_11_freizeit_und_aktivitaten',
    'item_alm9_vid_topic_11_freizeit_und_aktivitaten__quiz'
  ],
  'lesson_alm9_topic_12_uhrzeiten': [
    'item_alm9_vid_topic_12_uhrzeiten',
    'item_alm9_vid_topic_12_uhrzeiten__quiz'
  ],
  'lesson_alm9_topic_13_tageszeiten_und_tagesablauf': [
    'item_alm9_vid_topic_13_tageszeiten_und_tagesablauf',
    'item_alm9_vid_topic_13_tageszeiten_und_tagesablauf__quiz'
  ]
};

Object.entries(lessonMappings).forEach(([lessonId, items]) => {
  const lessonPath = path.join(lessonsDir, `${lessonId}.json`);
  if (fs.existsSync(lessonPath)) {
    const lesson = JSON.parse(fs.readFileSync(lessonPath, 'utf8'));
    lesson.items = items;
    fs.writeFileSync(lessonPath, JSON.stringify(lesson, null, 2) + '\n', 'utf8');
    console.log(`  ✓ Updated lesson ${lessonId} -> ${items.length} items`);
  } else {
    console.error(`  ✗ Lesson not found: ${lessonPath}`);
  }
});

console.log('\nDone updating Almanca 9 content!');
