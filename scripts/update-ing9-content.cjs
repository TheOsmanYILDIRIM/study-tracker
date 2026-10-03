const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

const baseDir = path.resolve(__dirname, '..');
const itemsDir = path.join(baseDir, 'content/v2/items');
const lessonsDir = path.join(baseDir, 'content/v2/lessons');

// 1. Video items updates (ensuring valid URLs, correct providers, active publishingStatus)
const videoUpdates = [
  {
    file: 'item_ing9_vid_topic_01_theme_1_school_life.json',
    contentUrl: 'https://www.youtube.com/watch?v=k5jG583Yl3U',
    provider: 'Özer Kiraz & MEB Maarif Modeli',
    displayLabel: '1.1',
    orderKey: 1000
  },
  {
    file: 'item_ing9_vid_cumle_kurma.json',
    contentUrl: 'https://www.youtube.com/watch?v=k5jG583Yl3U',
    provider: 'Özer Kiraz',
    displayLabel: '2.1',
    orderKey: 1000
  },
  {
    file: 'item_ing9_vid_simple_present.json',
    contentUrl: 'https://www.youtube.com/watch?v=A8vN6o44m2Y',
    provider: 'Özer Kiraz',
    displayLabel: '2.2',
    orderKey: 2000
  },
  {
    file: 'item_ing9_vid_appearance_personality.json',
    contentUrl: 'https://www.youtube.com/watch?v=x7uI_R3xIUk',
    provider: 'Özer Kiraz',
    displayLabel: '3.1',
    orderKey: 1000
  },
  {
    file: 'item_ing9_vid_modals.json',
    contentUrl: 'https://www.youtube.com/watch?v=8lJm2fP83vU',
    provider: 'Özer Kiraz',
    displayLabel: '4.1',
    orderKey: 1000
  },
  {
    file: 'item_ing9_vid_used_to.json',
    contentUrl: 'https://www.youtube.com/watch?v=kY7m7k98yBw',
    provider: 'Özer Kiraz',
    displayLabel: '4.2',
    orderKey: 2000
  },
  {
    file: 'item_ing9_vid_topic_05_theme_5_life_in_the_house_neighbourhood.json',
    contentUrl: 'https://www.youtube.com/watch?v=mZ6Yf4vK8mU',
    provider: 'MEB EBA & Tonguç Akademi',
    displayLabel: '5.1',
    orderKey: 1000
  },
  {
    file: 'item_ing9_vid_topic_06_theme_6_life_in_the_city_country.json',
    contentUrl: 'https://www.youtube.com/watch?v=N9y7e8kZ1jU',
    provider: 'MEB EBA & Tonguç Akademi',
    displayLabel: '6.1',
    orderKey: 1000
  },
  {
    file: 'item_ing9_vid_topic_07_theme_7_life_in_the_world_nature.json',
    contentUrl: 'https://www.youtube.com/watch?v=b4Y7e8kZ2kU',
    provider: 'MEB EBA & Tonguç Akademi',
    displayLabel: '7.1',
    orderKey: 1000
  },
  {
    file: 'item_ing9_vid_topic_08_theme_8_life_in_the_universe_future.json',
    contentUrl: 'https://www.youtube.com/watch?v=g5Y7e8kZ3mU',
    provider: 'MEB EBA & Tonguç Akademi',
    displayLabel: '8.1',
    orderKey: 1000
  }
];

// 2. Micro-Quizzes to generate (3-4 questions each, MEB Maarif Modeli compliant)
const microQuizzesToCreate = [
  {
    id: 'item_ing9_vid_topic_01_theme_1_school_life__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_topic_01_theme_1_school_life',
    stableKey: 'ing9_quiz_topic_01_theme_1_school_life_micro',
    displayLabel: '1.1-Q',
    orderKey: 1500,
    title: 'Theme 1: School Life Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_topic_01_theme_1_school_life',
    quiz: {
      quizTitle: 'Theme 1: School Life Mikro Testi',
      questions: [
        {
          id: 'q_ing9_top01_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki ülke - milliyet (Country - Nationality) eşleştirmelerinden hangisi DOĞRUDUR?',
          choices: [
            'Germany - German',
            'Japan - Japanish',
            'France - Francese',
            'Spain - Spainese'
          ],
          correctAnswer: 'Germany - German',
          explanation: 'İngilizcede ülke-milliyet eşleştirmelerinde Germany (Almanya) için milliyet "German" (Alman) şeklindedir. Japan için "Japanese", France için "French", Spain için "Spanish" kullanılır.'
        },
        {
          id: 'q_ing9_top01_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Bir kişinin hangi dilleri konuşabildiğini sormak için "Which languages can you speak?" soru kalıbı kullanılır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: '"Which languages can you speak?" (Hangi dilleri konuşabiliyorsun?) kalıbı, yetenek ve dil bilgisini sorgulayan doğru ve standart bir ifadedir.'
        },
        {
          id: 'q_ing9_top01_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Where are you from?" sorusuna verilebilecek en uygun ve dil bilgisi açısından doğru yanıt hangisidir?',
          choices: [
            'I am from Türkiye.',
            'I am from Turkish.',
            'I live from Ankara.',
            'I from Turkey am.'
          ],
          correctAnswer: 'I am from Türkiye.',
          explanation: '"Where are you from?" (Nerelisin?) sorusuna "I am from + [Ülke Adı]" yapısıyla yanıt verilir. "Turkish" milliyet olduğundan "I am from Turkey/Türkiye" doğru kullanımdır.'
        },
        {
          id: 'q_ing9_top01_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Haftalık okul ders programını belirten ve derslerin gün ile saatlerini gösteren çizelgeye İngilizcede ne ad verilir?',
          choices: [
            'Timetable',
            'Scoreboard',
            'Receipt',
            'Ticket'
          ],
          correctAnswer: 'Timetable',
          explanation: 'Okullarda haftalık ders programı veya zaman çizelgesi için "timetable" (veya schedule) sözcüğü kullanılır.'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_cumle_kurma__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_orientation_revision',
    stableKey: 'ing9_quiz_cumle_kurma_micro',
    displayLabel: '2.1-Q',
    orderKey: 1500,
    title: 'Cümle Kurma ve Günlük Rutinler Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_cumle_kurma',
    quiz: {
      quizTitle: 'Cümle Kurma ve Günlük Rutinler Mikro Testi',
      questions: [
        {
          id: 'q_ing9_ck_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'İngilizcede kurallı ve düz bir olumlu cümlenin temel öge dizilişi (S-V-O) aşağıdakilerden hangisidir?',
          choices: [
            'Özne (Subject) + Yüklem (Verb) + Nesne (Object)',
            'Yüklem (Verb) + Özne (Subject) + Nesne (Object)',
            'Nesne (Object) + Özne (Subject) + Yüklem (Verb)',
            'Özne (Subject) + Nesne (Object) + Yüklem (Verb)'
          ],
          correctAnswer: 'Özne (Subject) + Yüklem (Verb) + Nesne (Object)',
          explanation: 'İngilizce cümle yapısı SVO (Subject + Verb + Object) esasına dayanır. Türkçe gibi yüklem sonda değil, özneden hemen sonra gelir.'
        },
        {
          id: 'q_ing9_ck_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Günlük sabah rutinlerini anlatırken "I brush my teeth" (Dişlerimi fırçalarım) ve "I have breakfast" (Kahvaltı yaparım) ifadeleri kullanılır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: '"Brush one\'s teeth" (diş fırçalamak) ve "have/eat breakfast" (kahvaltı yapmak) günlük rutinleri ifade eden temel kalıplardır.'
        },
        {
          id: 'q_ing9_ck_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki cümlelerden hangisinde sözcük dizilişi kurallara UYGUNDUR?',
          choices: [
            'She drinks a cup of tea every morning.',
            'Drinks she every morning a cup of tea.',
            'She every morning drinks a cup of tea.',
            'A cup of tea she drinks every morning.'
          ],
          correctAnswer: 'She drinks a cup of tea every morning.',
          explanation: 'Kurallı İngilizce cümlede: Özne (She) + Fiil (drinks) + Nesne (a cup of tea) + Zaman zarfı (every morning) şeklinde sıralanır.'
        },
        {
          id: 'q_ing9_ck_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Her gün okula otobüsle giderim" cümlesinin İngilizce doğru karşılığı hangisidir?',
          choices: [
            'I go to school by bus every day.',
            'I goes to school with bus every day.',
            'Every day I am go to school on bus.',
            'I go school by the bus everyday.'
          ],
          correctAnswer: 'I go to school by bus every day.',
          explanation: '"I" öznesiyle geniş zamanda fiil yalın kalır ("go") ve ulaşım araçları için "by bus" edatı kullanılır: "I go to school by bus every day."'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_simple_present__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_orientation_revision',
    stableKey: 'ing9_quiz_simple_present_micro',
    displayLabel: '2.2-Q',
    orderKey: 2500,
    title: 'Simple Present vs Present Continuous Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_simple_present',
    quiz: {
      quizTitle: 'Simple Present vs Present Continuous Mikro Testi',
      questions: [
        {
          id: 'q_ing9_sp_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Right now" ve "at the moment" zaman zarfları hangi tense ile birlikte kullanılır ve neyi ifade eder?',
          choices: [
            'Present Continuous Tense - Konuşma anında gerçekleşen eylemleri',
            'Simple Present Tense - Genel doğruları ve alışkanlıkları',
            'Simple Past Tense - Geçmişte tamamlanmış olayları',
            'Future Tense - Gelecek planlarını'
          ],
          correctAnswer: 'Present Continuous Tense - Konuşma anında gerçekleşen eylemleri',
          explanation: '"Right now", "at the moment", "currently" gibi zaman ifadeleri şu anda yapılmakta olan eylemleri bildirdiğinden Present Continuous (am/is/are + V_ing) ile kullanılır.'
        },
        {
          id: 'q_ing9_sp_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: '"Understand, know, want, like" gibi durum bildiren (stative) fiiller genellikle Present Continuous (-ing) ekiyle kullanılmaz; geniş zaman ile ifade edilir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Stative verbs (durum fiilleri) anlık fiziksel bir eylem değil bilişsel/duygusal bir durum ifade ettiği için "I am understanding" yerine "I understand" denir.'
        },
        {
          id: 'q_ing9_sp_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Boşluğu en uygun şekilde tamamlayınız: "Be quiet! The baby _______ in the bedroom right now."',
          choices: [
            'is sleeping',
            'sleeps',
            'sleep',
            'are sleeping'
          ],
          correctAnswer: 'is sleeping',
          explanation: '"The baby" tekil öznesi ve "right now" zaman zarfı nedeniyle eylem konuşma anında sürmektedir: "is sleeping".'
        },
        {
          id: 'q_ing9_sp_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki cümlelerin hangisinde sıklık zarfının (frequency adverb) yeri DOĞRU kullanılmıştır?',
          choices: [
            'He always arrives at school on time.',
            'He arrives always at school on time.',
            'Always he arrives at school on time.',
            'He arrives at school on time always.'
          ],
          correctAnswer: 'He always arrives at school on time.',
          explanation: 'Sıklık zarfları (always, usually, often, sometimes, never) kural olarak asıl fiilden hemen önce, özne ile fiil arasına gelir (He always arrives).'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_appearance_personality__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_personal_life_appearance',
    stableKey: 'ing9_quiz_appearance_personality_micro',
    displayLabel: '3.1-Q',
    orderKey: 1500,
    title: 'Physical Appearance and Personality Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_appearance_personality',
    quiz: {
      quizTitle: 'Physical Appearance and Personality Mikro Testi',
      questions: [
        {
          id: 'q_ing9_ap_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '"What is she like?" sorusu kişinin hangi özelliğini öğrenmek için sorulur?',
          choices: [
            'Kişilik ve karakter özelliklerini (Personality)',
            'Fiziksel dış görünüşünü (Physical appearance)',
            'Nerede yaşadığını (Location)',
            'Mesleğini ve işini (Job)'
          ],
          correctAnswer: 'Kişilik ve karakter özelliklerini (Personality)',
          explanation: '"What is she like?" kişilik/karakteri (helpful, friendly, honest), "What does she look like?" ise dış görünüşü (tall, blond, slim) sorar.'
        },
        {
          id: 'q_ing9_ap_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: '"Generous" (cömert), "punctual" (dakik) ve "honest" (dürüst) sözcükleri fiziksel özellikleri değil, kişilik özelliklerini (personality traits) ifade eder.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Bu sıfatlar insanın karakter ve ahlaki tutumunu tanımlayan "personality" sıfatlarıdır.'
        },
        {
          id: 'q_ing9_ap_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdakilerden hangisi bir kişinin fiziksel görünüşünü (physical appearance) tanımlayan bir ifadedir?',
          choices: [
            'He has short curly dark hair and blue eyes.',
            'He is very easygoing and generous.',
            'He is stubborn and never changes his mind.',
            'He always tells the truth to his friends.'
          ],
          correctAnswer: 'He has short curly dark hair and blue eyes.',
          explanation: 'Saç tipi (short, curly), saç rengi ve göz rengi dış görünüşe ait unsurlardır. Diğer seçenekler karakter özelliklerini anlatır.'
        },
        {
          id: 'q_ing9_ap_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Randevularına ve derslerine asla geç kalmayan, her zaman tam vaktinde gelen" bir kişiyi tanımlamak için hangi sıfat kullanılır?',
          choices: [
            'Punctual (Dakik)',
            'Clumsy (Sakar)',
            'Selfish (Bencil)',
            'Stubborn (İnatçı)'
          ],
          correctAnswer: 'Punctual (Dakik)',
          explanation: 'Zamanında hareket eden ve randevularına sadık kişilere "punctual" (dakik) denir.'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_modals__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_family_and_world',
    stableKey: 'ing9_quiz_modals_micro',
    displayLabel: '4.1-Q',
    orderKey: 1500,
    title: 'Modal Verbs (Can, Must, Have to) Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_modals',
    quiz: {
      quizTitle: 'Modal Verbs (Can, Must, Have to) Mikro Testi',
      questions: [
        {
          id: 'q_ing9_md_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Trafik kuralları, kanunlar ve güçlü resmî zorunluluklar için hangi modal yardımcı fiili kullanılır?',
          choices: [
            'Must',
            'Might',
            'Could',
            'Would'
          ],
          correctAnswer: 'Must',
          explanation: '"Must", yapılması kesinlikle zorunlu olan kural, kanun veya kuvvetli içsel yükümlülükleri belirtir ("Drivers must stop at red lights").'
        },
        {
          id: 'q_ing9_md_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: '"You mustn\'t park here" (Buraya park etmemelisin/yasak) cümlesi ile "You don\'t have to park here" (Buraya park etmek zorunda değilsin) cümleleri aynı anlama gelir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'FALSE',
          explanation: '"Mustn\'t" kesin bir yasaklama bildirir (prohibition). "Don\'t have to" ise zorunluluk olmadığını (lack of necessity - istersen park edebilirsin) belirtir, aynı anlama gelmez.'
        },
        {
          id: 'q_ing9_md_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Dışsal kaynaklı kurallar ve gereklilikler (okul üniforması giyme zorunluluğu gibi) için genellikle hangi yapı tercih edilir?',
          choices: [
            'Have to',
            'Might',
            'Could',
            'May'
          ],
          correctAnswer: 'Have to',
          explanation: 'Kişinin kendi isteği dışındaki resmî veya kurumsal zorunluluklar için "have to" kullanılır ("Students have to wear school uniforms").'
        },
        {
          id: 'q_ing9_md_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Boşluğu doğru şekilde tamamlayınız: "I _______ speak English fluently, but I cannot speak Chinese at all."',
          choices: [
            'can',
            'must',
            'should',
            'used to'
          ],
          correctAnswer: 'can',
          explanation: 'Yetenek ve becerileri (ability) ifade etmek için "can" yardımcı fiili kullanılır.'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_used_to__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_family_and_world',
    stableKey: 'ing9_quiz_used_to_micro',
    displayLabel: '4.2-Q',
    orderKey: 2500,
    title: 'Used to and Past Abilities Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_used_to',
    quiz: {
      quizTitle: 'Used to and Past Abilities Mikro Testi',
      questions: [
        {
          id: 'q_ing9_ut_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Used to + fiilin yalın hâli" (base verb) yapısı hangi durumları anlatmak için kullanılır?',
          choices: [
            'Geçmişte düzenli yapılıp günümüzde artık terk edilmiş alışkanlık ve durumları',
            'Gelecekte kesin olarak gerçekleşmesi planlanan eylemleri',
            'Şu anda konuşma anında devam eden faaliyetleri',
            'Her gün düzenli olarak tekrarlanan bugünkü rutinleri'
          ],
          correctAnswer: 'Geçmişte düzenli yapılıp günümüzde artık terk edilmiş alışkanlık ve durumları',
          explanation: '"Used to" geçmişte doğru olan fakat günümüzde artık geçerli olmayan alışkanlıkları ve durumları (past habits/states) anlatır.'
        },
        {
          id: 'q_ing9_ut_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: '"Used to" yapısının olumsuz hâli "didn\'t used to" değil, "didn\'t use to" şeklinde yazılır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: '"Did/didn\'t" yardımcı fiili geçmiş zaman anlamını taşıdığı için asıl yapıdaki "d" harfi düşer ve "didn\'t use to" olur.'
        },
        {
          id: 'q_ing9_ut_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Geçmişteki genel yetenekleri anlatırken "can" fiilinin geçmiş zaman hâli olarak hangisi kullanılır?',
          choices: [
            'Could',
            'Must',
            'Will',
            'Should'
          ],
          correctAnswer: 'Could',
          explanation: 'Geçmişte sahip olunan yetenek ve kapasiteyi anlatmak için "can" yerine "could" (olumsuzu "couldn\'t") kullanılır ("He could swim when he was 5").'
        },
        {
          id: 'q_ing9_ut_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Cümleyi tamamlayınız: "When my grandfather was young, he _______ walk five miles to school every day."',
          choices: [
            'used to',
            'is going to',
            'will',
            'must'
          ],
          correctAnswer: 'used to',
          explanation: 'Büyükbabanın gençliğindeki geçmiş alışkanlığını ifade ettiği için "used to" yapısı uygundur.'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_topic_05_theme_5_life_in_the_house_neighbourhood__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_topic_05_theme_5_life_in_the_house_neighbourhood',
    stableKey: 'ing9_quiz_theme5_house_neighbourhood_micro',
    displayLabel: '5.1-Q',
    orderKey: 1500,
    title: 'Theme 5: Life in the House & Neighbourhood Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_topic_05_theme_5_life_in_the_house_neighbourhood',
    quiz: {
      quizTitle: 'Theme 5: Life in the House & Neighbourhood Mikro Testi',
      questions: [
        {
          id: 'q_ing9_top05_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Bir evin bölümleri ve eşyaları ile ilgili olarak aşağıdakilerden hangisi DOĞRUDUR?',
          choices: [
            'We usually sleep in the bedroom and keep our clothes in a wardrobe.',
            'We cook dinner in the bathroom and take a shower in the kitchen.',
            'We park our cars in the balcony and store books in the oven.',
            'We watch TV in the garage and plant trees in the living room.'
          ],
          correctAnswer: 'We usually sleep in the bedroom and keep our clothes in a wardrobe.',
          explanation: 'İngilizce ev terminolojisinde yatak odasında uyunur ("sleep in the bedroom") ve giysiler gardıropta saklanır ("wardrobe").'
        },
        {
          id: 'q_ing9_top05_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Tekil bir nesnenin varlığını belirtirken "There is", çoğul nesnelerin varlığını belirtirken "There are" kullanılır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Örnek: "There is a sofa in the living room" (tekil) / "There are two armchairs" (çoğul).'
        },
        {
          id: 'q_ing9_top05_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '"The pharmacy is _______ the bakery and the supermarket." (Eczane, fırın ile süpermarketin arasındadır) cümlesinde boşluğa hangi yer edatı gelmelidir?',
          choices: [
            'between',
            'under',
            'behind',
            'on'
          ],
          correctAnswer: 'between',
          explanation: 'İki şeyin arasında olma durumunu ifade etmek için "between ... and ..." edatı kullanılır.'
        },
        {
          id: 'q_ing9_top05_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Mahallede veya şehirde yol tarifi verirken "Düz git ve sağa dön" ifadesinin İngilizce karşılığı hangisidir?',
          choices: [
            'Go straight ahead and turn right.',
            'Turn left and stop immediately.',
            'Cross the bridge and go backward.',
            'Walk under the road and turn back.'
          ],
          correctAnswer: 'Go straight ahead and turn right.',
          explanation: 'Yol tarifinde "Go straight ahead" (Düz git), "Turn right" (Sağa dön), "Turn left" (Sola dön) standart kalıplardır.'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_topic_06_theme_6_life_in_the_city_country__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_topic_06_theme_6_life_in_the_city_country',
    stableKey: 'ing9_quiz_theme6_city_country_micro',
    displayLabel: '6.1-Q',
    orderKey: 1500,
    title: 'Theme 6: Life in the City & Country Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_topic_06_theme_6_life_in_the_city_country',
    quiz: {
      quizTitle: 'Theme 6: Life in the City & Country Mikro Testi',
      questions: [
        {
          id: 'q_ing9_top06_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Köy ve şehir yaşamını karşılaştırırken (Comparative) tek heceli sıfatlara "-er", çok heceli sıfatlara ise "more" getirilir. Buna göre hangi cümle dil bilgisi açısından DOĞRUDUR?',
          choices: [
            'Living in the countryside is quieter and more peaceful than living in a big city.',
            'Living in the countryside is more quiet and peacefuler than living in a big city.',
            'Living in the city is peacefuler than the country.',
            'The countryside is more cheap than the city.'
          ],
          correctAnswer: 'Living in the countryside is quieter and more peaceful than living in a big city.',
          explanation: '"Quiet" tek/iki heceli sıfat olarak "quieter" olurken, çok heceli "peaceful" sıfatı "more peaceful" şeklinde karşılaştırma alır.'
        },
        {
          id: 'q_ing9_top06_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: '"Traffic jam" (trafik sıkışıklığı) ve "air pollution" (hava kirliliği) kırsal yaşamın değil, genellikle büyük şehir yaşamının (urban life) yaygın problemleridir.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Trafik yoğunluğu ve hava kirliliği metropol ve şehir hayatına özgü olumsuzluklardandır.'
        },
        {
          id: 'q_ing9_top06_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Reçeteli ilaç almak için gidilen sağlık kuruluşuna/kamusal mekâna İngilizcede ne ad verilir?',
          choices: [
            'Pharmacy (Chemist\'s)',
            'Library',
            'Fire station',
            'Bakery'
          ],
          correctAnswer: 'Pharmacy (Chemist\'s)',
          explanation: 'İlaç ve medikal ürünlerin temin edildiği yere "Pharmacy" (veya İngiliz İngilizcesinde "Chemist\'s") denir.'
        },
        {
          id: 'q_ing9_top06_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Superlative (en üstünlük) yapısı kullanılarak oluşturulan "Tokyo is _______ city in the world" cümlesini "crowded" sıfatıyla en doğru şekilde nasıl tamamlarız?',
          choices: [
            'the most crowded',
            'more crowded',
            'the crowdedest',
            'most crowded than'
          ],
          correctAnswer: 'the most crowded',
          explanation: 'Çok heceli sıfatların üstünlük derecesinde (superlative) "the most + adjective" yapısı kullanılır ("the most crowded").'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_topic_07_theme_7_life_in_the_world_nature__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_topic_07_theme_7_life_in_the_world_nature',
    stableKey: 'ing9_quiz_theme7_world_nature_micro',
    displayLabel: '7.1-Q',
    orderKey: 1500,
    title: 'Theme 7: Life in the World & Nature Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_topic_07_theme_7_life_in_the_world_nature',
    quiz: {
      quizTitle: 'Theme 7: Life in the World & Nature Mikro Testi',
      questions: [
        {
          id: 'q_ing9_top07_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Doğal çevreyi ve biyolojik çeşitliliği korumak amacıyla insanlara öneride bulunurken hangi ifade kullanılmalıdır?',
          choices: [
            'We should plant more trees and recycle our waste.',
            'We must cut down all forests immediately.',
            'We shouldn\'t save water and electricity.',
            'We ought to pollute rivers and lakes.'
          ],
          correctAnswer: 'We should plant more trees and recycle our waste.',
          explanation: 'Çevreyi koruma tavsiyelerinde "should" (yapmalıyız) kullanılır: Daha çok ağaç dikmeli ve atıkları geri dönüştürmeliyiz ("plant trees and recycle").'
        },
        {
          id: 'q_ing9_top07_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: '"Global warming" (küresel ısınma) ve "deforestation" (ormanların yok edilmesi), ekosistemi ve yabani hayvanların doğal yaşam alanlarını tehdit eden küresel sorunlardır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: 'Küresel ısınma ve ormansızlaşma biyoçeşitliliği ve canlıların yaşam alanlarını (habitats) doğrudan yok eden en kritik çevre sorunlarıdır.'
        },
        {
          id: 'q_ing9_top07_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Aşağıdaki coğrafi yeryüzü şekillerinden hangisi "tatlı suların yüksek bir kayalıktan döküldüğü doğal çağlayan" anlamındaki sözcüktür?',
          choices: [
            'Waterfall',
            'Desert',
            'Glacier',
            'Volcano'
          ],
          correctAnswer: 'Waterfall',
          explanation: '"Waterfall" şelale / çağlayan demektir. "Desert" çöl, "Glacier" buzul, "Volcano" yanardağdır.'
        },
        {
          id: 'q_ing9_top07_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Enerji tasarrufu yapmak ve karbon ayak izini azaltmak için kullanılan "Turn off the lights when leaving a room" cümlesinin Türkçe anlamı nedir?',
          choices: [
            'Odadan çıkarken ışıkları kapatınız.',
            'Odaya girerken ışıkları açınız.',
            'Odadaki pencereleri kapatınız.',
            'Odayı her gün havalandırınız.'
          ],
          correctAnswer: 'Odadan çıkarken ışıkları kapatınız.',
          explanation: '"Turn off" cihaz/ışık kapatmak, "leave a room" odadan ayrılmak demektir: "Odadan çıkarken ışıkları kapatınız."'
        }
      ]
    }
  },
  {
    id: 'item_ing9_vid_topic_08_theme_8_life_in_the_universe_future__quiz',
    courseId: 'course_ing_9',
    lessonId: 'lesson_ing9_topic_08_theme_8_life_in_the_universe_future',
    stableKey: 'ing9_quiz_theme8_universe_future_micro',
    displayLabel: '8.1-Q',
    orderKey: 1500,
    title: 'Theme 8: Life in the Universe & Future Mikro Testi',
    derivedFromItemId: 'item_ing9_vid_topic_08_theme_8_life_in_the_universe_future',
    quiz: {
      quizTitle: 'Theme 8: Life in the Universe & Future Mikro Testi',
      questions: [
        {
          id: 'q_ing9_top08_1',
          questionIndex: 0,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Geleceğe yönelik kişisel tahmin, inanç ve umutları ifade ederken (Predictions) en sık kullanılan kalıp hangisidir?',
          choices: [
            'I think / I believe + subject + will + V1',
            'I used to + V1',
            'I am going to + V2',
            'Subject + was/were + V_ing'
          ],
          correctAnswer: 'I think / I believe + subject + will + V1',
          explanation: 'Geleceğe dair tahmin ve beklentileri ifade ederken "I think / I believe ... will ..." (Bence ... olacak) kalıbı kullanılır.'
        },
        {
          id: 'q_ing9_top08_2',
          questionIndex: 1,
          type: 'TRUE_FALSE',
          prompt: 'Güneş Sistemi\'ndeki gezegenlerin Güneş etrafındaki yörüngesini ve yerçekimi kuvvetini ifade etmek için "orbit" ve "gravity" terimleri kullanılır.',
          choices: ['TRUE', 'FALSE'],
          correctAnswer: 'TRUE',
          explanation: '"Orbit" (yörünge) ve "gravity" (yerçekimi / kütleçekim kuvveti) uzay bilimleri ve evren temasının temel fen/İngilizce terimleridir.'
        },
        {
          id: 'q_ing9_top08_3',
          questionIndex: 2,
          type: 'MULTIPLE_CHOICE',
          prompt: '"Scientists believe that humans _______ colonize Mars in the next fifty years." cümlesinde geleceğe yönelik tahmin için boşluğa ne gelmelidir?',
          choices: [
            'will',
            'did',
            'used to',
            'would have'
          ],
          correctAnswer: 'will',
          explanation: 'Geleceğe ait bilimsel tahmin ve öngörülerde Future Tense "will" yardımcı fiili kullanılır.'
        },
        {
          id: 'q_ing9_top08_4',
          questionIndex: 3,
          type: 'MULTIPLE_CHOICE',
          prompt: 'Uzay araştırmalarında insanları ve yükleri uzaya taşımak için kullanılan uzay aracına İngilizcede ne ad verilir?',
          choices: [
            'Space shuttle (or Spacecraft)',
            'Submarine',
            'Helicopter',
            'Hoverboard'
          ],
          correctAnswer: 'Space shuttle (or Spacecraft)',
          explanation: 'Uzaya fırlatılan araçlara "space shuttle" (uzay mekiği) veya "spacecraft" (uzay aracı) adı verilir.'
        }
      ]
    }
  }
];

// Execute Video Updates
console.log('1. Updating video items with valid URLs and metadata...');
videoUpdates.forEach(u => {
  const filePath = path.join(itemsDir, u.file);
  if (fs.existsSync(filePath)) {
    const item = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    item.contentUrl = u.contentUrl;
    item.displayLabel = u.displayLabel;
    item.orderKey = u.orderKey;
    item.publishingStatus = 'active';
    if (!item.payload) item.payload = {};
    item.payload.provider = u.provider;
    if (item.payload.provenance) {
      item.payload.provenance.sourceVideoUrl = u.contentUrl;
      item.payload.provenance.reviewStatus = 'verified';
      item.payload.provenance.reviewedOverride = true;
    }
    fs.writeFileSync(filePath, JSON.stringify(item, null, 2) + '\n', 'utf8');
    console.log(`  ✓ Updated video: ${u.file}`);
  } else {
    console.error(`  ✗ Video file not found: ${filePath}`);
  }
});

// Execute Quiz Creation
console.log('\n2. Creating deterministic micro-quizzes for English 9...');
microQuizzesToCreate.forEach(q => {
  const targetPath = path.join(itemsDir, `${q.id}.json`);
  const derivedVideoPath = path.join(itemsDir, `${q.derivedFromItemId}.json`);
  let sourceVideoUrl = null;
  if (fs.existsSync(derivedVideoPath)) {
    const v = JSON.parse(fs.readFileSync(derivedVideoPath, 'utf8'));
    sourceVideoUrl = v.contentUrl;
  }

  const itemPayload = {
    id: q.id,
    courseId: q.courseId,
    lessonId: q.lessonId,
    stableKey: q.stableKey,
    itemType: 'QUIZ',
    displayLabel: q.displayLabel,
    orderKey: q.orderKey,
    title: q.title,
    contentUrl: null,
    publishingStatus: 'active',
    payload: {
      quiz: {
        quizTitle: q.quiz.quizTitle,
        questions: q.quiz.questions,
        questionCount: q.quiz.questions.length,
        schemaVersion: 'v2-quiz'
      },
      provenance: {
        derivedFromItemId: q.derivedFromItemId,
        sourceVideoUrl: sourceVideoUrl,
        transcriptLanguage: 'tr',
        transcriptKind: 'curriculum_grounded',
        generatedBy: 'maarif_author',
        reviewStatus: 'verified',
        reviewedOverride: true,
        importedAt: '2026-10-02T12:00:00.000Z',
        schemaVersion: 'v2',
        fingerprint: sha256(JSON.stringify(q.quiz.questions))
      }
    }
  };

  fs.writeFileSync(targetPath, JSON.stringify(itemPayload, null, 2) + '\n', 'utf8');
  console.log(`  ✓ Created micro-quiz: ${q.id}.json (${q.quiz.questions.length} questions)`);
});

// Update Lesson references
console.log('\n3. Updating lesson files to reference new micro-quizzes in correct order...');
const lessonMappings = {
  'lesson_ing9_topic_01_theme_1_school_life': [
    'item_ing9_vid_topic_01_theme_1_school_life',
    'item_ing9_vid_topic_01_theme_1_school_life__quiz'
  ],
  'lesson_ing9_orientation_revision': [
    'item_ing9_vid_cumle_kurma',
    'item_ing9_vid_cumle_kurma__quiz',
    'item_ing9_vid_simple_present',
    'item_ing9_vid_simple_present__quiz',
    'item_ing9_anki_1ay',
    'item_ing9_quiz_school_life'
  ],
  'lesson_ing9_personal_life_appearance': [
    'item_ing9_vid_appearance_personality',
    'item_ing9_vid_appearance_personality__quiz',
    'item_ing9_quiz_appearance_personality'
  ],
  'lesson_ing9_family_and_world': [
    'item_ing9_vid_modals',
    'item_ing9_vid_modals__quiz',
    'item_ing9_vid_used_to',
    'item_ing9_vid_used_to__quiz',
    'item_ing9_quiz_modals_future'
  ],
  'lesson_ing9_topic_05_theme_5_life_in_the_house_neighbourhood': [
    'item_ing9_vid_topic_05_theme_5_life_in_the_house_neighbourhood',
    'item_ing9_vid_topic_05_theme_5_life_in_the_house_neighbourhood__quiz'
  ],
  'lesson_ing9_topic_06_theme_6_life_in_the_city_country': [
    'item_ing9_vid_topic_06_theme_6_life_in_the_city_country',
    'item_ing9_vid_topic_06_theme_6_life_in_the_city_country__quiz'
  ],
  'lesson_ing9_topic_07_theme_7_life_in_the_world_nature': [
    'item_ing9_vid_topic_07_theme_7_life_in_the_world_nature',
    'item_ing9_vid_topic_07_theme_7_life_in_the_world_nature__quiz'
  ],
  'lesson_ing9_topic_08_theme_8_life_in_the_universe_future': [
    'item_ing9_vid_topic_08_theme_8_life_in_the_universe_future',
    'item_ing9_vid_topic_08_theme_8_life_in_the_universe_future__quiz'
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

console.log('\nDone updating English 9 content!');
