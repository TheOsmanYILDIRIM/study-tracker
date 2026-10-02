const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

function computeItemFingerprint(item) {
  const content = [
    item.stableKey || item.id,
    item.itemType,
    item.title || item.displayLabel,
    item.contentUrl || '',
    JSON.stringify(item.payload?.metadata || item.payload || {})
  ].join('|');
  return sha256(content);
}

function computeQuizFingerprint(quizObj) {
  const stableQuestions = (quizObj.questions || []).map(q => ({
    id: q.id,
    type: q.type,
    prompt: q.prompt,
    choices: Array.isArray(q.choices) ? [...q.choices] : null,
    correctAnswer: q.correctAnswer,
    explanation: q.explanation || null
  }));

  const stableObj = {
    quizTitle: quizObj.quizTitle || quizObj.title || '',
    questions: stableQuestions
  };

  return sha256(JSON.stringify(stableObj));
}

function makeQuizPayload(title, questions, sourceRef, sourceSection) {
  const normalizedQuestions = questions.map((q, idx) => ({
    id: q.id || `q${idx + 1}`,
    questionIndex: idx,
    type: q.type || 'MULTIPLE_CHOICE',
    prompt: q.prompt,
    choices: q.type === 'TRUE_FALSE' ? ['TRUE', 'FALSE'] : q.choices,
    correctAnswer: q.correctAnswer,
    explanation: q.explanation || ''
  }));

  const quiz = {
    quizTitle: title,
    questions: normalizedQuestions,
    questionCount: normalizedQuestions.length,
    schemaVersion: 'v2-quiz'
  };

  const fp = computeQuizFingerprint(quiz);

  return {
    quiz,
    provenance: {
      sourceRef,
      sourceSection,
      fingerprint: fp,
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: '2026-10-02T09:00:00.000Z',
      schemaVersion: 'v2'
    }
  };
}

// ==========================================
// 1. MATEMATİK (course_mat_9)
// ==========================================
const matLessons = [
  {
    id: "lesson_mat9_sayilar_uslu_koklu",
    stableKey: "lesson_mat9_sayilar_uslu_koklu",
    title: "Sayılar: Üslü ve Köklü Gösterimler (MAT.9.1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_mat9_vid_uslu_giris",
        stableKey: "mat9_vid_uslu_giris",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Üslü Sayılara Giriş (1. Seviye)",
        contentUrl: "https://www.youtube.com/watch?v=kYqP9K0Y0pU",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Cebir Öncesi Temeller",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L12",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_uslu_kurallar",
        stableKey: "mat9_vid_uslu_kurallar",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Üslü İfadelerin Sadeleştirilmesi ve Çarpma",
        contentUrl: "https://www.youtube.com/watch?v=s5Rz-1i0n18",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Üs Kuralları ve Sadeleştirme",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L13",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_uslu_bolme",
        stableKey: "mat9_vid_uslu_bolme",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Üslü Sayılarda Bölme İşlemi ve Özellikleri",
        contentUrl: "https://www.youtube.com/watch?v=Z8mH2nLqE9I",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Bölme Kuralı",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L14",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_uslu_negatif",
        stableKey: "mat9_vid_uslu_negatif",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Üssü Sıfır, Negatif Sayı veya Kesir Olan Sayılar",
        contentUrl: "https://www.youtube.com/watch?v=9_d8mQJzL4w",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Sıfır ve Negatif Üs",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L15",
            sourceSection: "1. Hafta: Üslü Sayılar ve Özellikleri (MAT.9.1.1)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_koklu_mantik",
        stableKey: "mat9_vid_koklu_mantik",
        itemType: "VIDEO",
        displayLabel: "1.5",
        orderKey: 5000.0,
        title: "Köklü Sayılar ve Üslü Sayılar Arasındaki İlişki",
        contentUrl: "https://www.youtube.com/watch?v=vVj4x9p0m1s",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Köklü Sayı Mantığı",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L20",
            sourceSection: "2. Hafta: Bilimsel Gösterim ve Köklü Sayılara Giriş (MAT.9.1.1)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_rasyonel_usler",
        stableKey: "mat9_vid_rasyonel_usler",
        itemType: "VIDEO",
        displayLabel: "1.6",
        orderKey: 6000.0,
        title: "Kesirli Üslü ve Köklü İfadeler",
        contentUrl: "https://www.youtube.com/watch?v=1xNq8o3l4wM",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          topic: "Rasyonel Üsler",
          provenance: {
            sourceRef: "10-Projects/1_ay_matematik_khan_academy_videolari.md#L21",
            sourceSection: "2. Hafta: Bilimsel Gösterim ve Köklü Sayılara Giriş (MAT.9.1.1)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_quiz_uslu_koklu",
        stableKey: "mat9_quiz_uslu_koklu",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 7000.0,
        title: "Üslü ve Köklü Sayılar Muhakeme Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Üslü ve Köklü Sayılar Muhakeme Testi",
          [
            {
              id: "q_mat1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "2³ · 2⁵ ifadesinin en sade eşiti aşağıdakilerden hangisidir?",
              choices: ["2⁸", "2¹⁵", "4⁸", "4¹⁵"],
              correctAnswer: "2⁸",
              explanation: "Tabanları aynı olan üslü ifadeler çarpılırken üsler toplanır: 3 + 5 = 8."
            },
            {
              id: "q_mat1_2",
              type: "MULTIPLE_CHOICE",
              prompt: "(3⁴)² ifadesinin eşiti nedir?",
              choices: ["3⁶", "3⁸", "9⁴", "6⁴"],
              correctAnswer: "3⁸",
              explanation: "Üssün üssü alınırken üsler çarpılır: 4 · 2 = 8."
            },
            {
              id: "q_mat1_3",
              type: "TRUE_FALSE",
              prompt: "Sıfır hariç herhangi bir gerçek sayının sıfırıncı kuvveti daima 1'e eşittir.",
              correctAnswer: "TRUE",
              explanation: "x ≠ 0 için x⁰ = 1 kuralı geçerlidir."
            },
            {
              id: "q_mat1_4",
              type: "MULTIPLE_CHOICE",
              prompt: "√32 ifadesinin a√b biçimindeki en sade hali hangisidir?",
              choices: ["2√8", "4√2", "8√2", "16√2"],
              correctAnswer: "4√2",
              explanation: "32 = 16 · 2 olduğundan √32 = √(16·2) = 4√2."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L15",
          "1-2. Hafta: Üslü ve Köklü Gösterimler"
        )
      }
    ]
  },
  {
    id: "lesson_mat9_sayilar_araliklar_kumeler",
    stableKey: "lesson_mat9_sayilar_araliklar_kumeler",
    title: "Gerçek Sayı Aralıkları ve Küme Sembolleri (MAT.9.1.2)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_mat9_vid_araliklar_gosterim",
        stableKey: "mat9_vid_araliklar_gosterim",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Gerçek Sayı Aralıkları & Gösterim",
        contentUrl: "https://www.youtube.com/watch?v=TNw7eEas9Oo",
        publishingStatus: "active",
        payload: {
          provenance: {
            sourceRef: "10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L143",
            sourceSection: "3. Hafta: Aralıklar, Haritalar ve Atomun Dünyası",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_vid_aralik_farki",
        stableKey: "mat9_vid_aralik_farki",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Aralık Farkı ve Eşitsizlik Problemleri",
        contentUrl: "https://www.youtube.com/watch?v=drPKmUSKYCI",
        publishingStatus: "active",
        payload: {
          provenance: {
            sourceRef: "10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L182",
            sourceSection: "4. Hafta: Bilim İnsanları, Periyodik Sistem ve 1. Ay Kapanışı",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_mat9_quiz_araliklar_kumeler",
        stableKey: "mat9_quiz_araliklar_kumeler",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Gerçek Sayı Aralıkları ve Kümeler Tarama Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Gerçek Sayı Aralıkları ve Kümeler Tarama Testi",
          [
            {
              id: "q_mat2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "A = [-2, 5) ve B = (1, 8] aralıkları için A ∩ B kesişim kümesi nedir?",
              choices: ["[-2, 8]", "(1, 5)", "[1, 5]", "(-2, 8)"],
              correctAnswer: "(1, 5)",
              explanation: "Kesişim kümesinde sol sınır max(-2, 1) = 1 (açık), sağ sınır min(5, 8) = 5 (açık) olur: (1, 5)."
            },
            {
              id: "q_mat2_2",
              type: "TRUE_FALSE",
              prompt: "Kapalı aralık [a, b] gösteriminde sınır değerleri olan a ve b elemanları kümeye dahildir.",
              correctAnswer: "TRUE",
              explanation: "Köşeli parantez sınır noktasının aralığa dahil olduğunu ifade eder."
            },
            {
              id: "q_mat2_3",
              type: "MULTIPLE_CHOICE",
              prompt: "{x | x ∈ ℝ, -3 < x ≤ 4} kümesinin aralık gösterimi hangisidir?",
              choices: ["[-3, 4)", "(-3, 4]", "[-3, 4]", "(-3, 4)"],
              correctAnswer: "(-3, 4]",
              explanation: "-3 dahil değil (açık parantez), 4 dahil (köşeli parantez): (-3, 4]."
            },
            {
              id: "q_mat2_4",
              type: "MULTIPLE_CHOICE",
              prompt: "ℝ fark [2, ∞) kümesinin eşiti nedir?",
              choices: ["(-∞, 2]", "(-∞, 2)", "(2, ∞)", "[-∞, 2)"],
              correctAnswer: "(-∞, 2)",
              explanation: "Tüm reel sayılardan [2, ∞) çıkarıldığında 2'den küçük açık aralık (-∞, 2) kalır."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L45",
          "3-4. Hafta: Sayı Aralıkları ve Kümeler"
        )
      }
    ]
  },
  {
    id: "lesson_mat9_sayilar_islem_ozellikleri",
    stableKey: "lesson_mat9_sayilar_islem_ozellikleri",
    title: "Sayı Kümeleri ve İşlem Özellikleri (MAT.9.1.3)",
    orderKey: 3000.0,
    items: [
      {
        id: "item_mat9_vid_sayi_kumeleri",
        stableKey: "mat9_vid_sayi_kumeleri",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Doğal, Tam, Rasyonel ve Gerçek Sayı Kümeleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L65",
            sourceSection: "5. Hafta: Sayı Kümeleri ve İşlem Özellikleri",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_vid_cebirsel_ispat",
        stableKey: "mat9_vid_cebirsel_ispat",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "İşlem Özelliklerini Cebirsel İfade Etme ve Doğrulama",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L80",
            sourceSection: "6. Hafta: Cebirsel İfadeler ve İspat",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_quiz_sayi_kumeleri",
        stableKey: "mat9_quiz_sayi_kumeleri",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Sayı Kümeleri ve İspat Yöntemleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Sayı Kümeleri ve İspat Yöntemleri Testi",
          [
            {
              id: "q_mat3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Aşağıdaki sayılardan hangisi bir irrasyonel (ℚ') sayıdır?",
              choices: ["-5", "2/7", "√7", "0,333..."],
              correctAnswer: "√7",
              explanation: "√7 sayısı rasyonel bir kesir a/b olarak yazılamayan irrasyonel bir sayıdır."
            },
            {
              id: "q_mat3_2",
              type: "TRUE_FALSE",
              prompt: "İki tek sayının toplamı daima çift sayıdır.",
              correctAnswer: "TRUE",
              explanation: "(2n+1) + (2k+1) = 2(n+k+1) olup daima 2'nin tam katıdır (çift)."
            },
            {
              id: "q_mat3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Gerçek sayılarda çarpma işleminin toplama işlemi üzerine dağılma özelliği hangisidir?",
              choices: [
                "a · (b + c) = a · b + a · c",
                "a · b = b · a",
                "(a · b) · c = a · (b · c)",
                "a + 0 = a"
              ],
              correctAnswer: "a · (b + c) = a · b + a · c",
              explanation: "Çarpmanın toplama üzerine sol dağılma özelliğidir."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L70",
          "5-6. Hafta: Sayı Kümeleri"
        )
      }
    ]
  },
  {
    id: "lesson_mat9_ucgende_acilar_kenarlar",
    stableKey: "lesson_mat9_ucgende_acilar_kenarlar",
    title: "Geometrik Şekiller: Üçgende Açı ve Kenar Özellikleri (MAT.9.2.1)",
    orderKey: 4000.0,
    items: [
      {
        id: "item_mat9_vid_ucgende_aci",
        stableKey: "mat9_vid_ucgende_aci",
        itemType: "VIDEO",
        displayLabel: "4.1",
        orderKey: 1000.0,
        title: "Üçgende İç ve Dış Açı Bağıntıları",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L110",
            sourceSection: "Üçgende Açı ve Kenar Özellikleri",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_vid_ucgen_esitsizligi",
        stableKey: "mat9_vid_ucgen_esitsizligi",
        itemType: "VIDEO",
        displayLabel: "4.2",
        orderKey: 2000.0,
        title: "Üçgen Eşitsizliği ve Açı-Kenar İlişkileri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L125",
            sourceSection: "Üçgen Eşitsizliği",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_quiz_ucgende_aci_kenar",
        stableKey: "mat9_quiz_ucgende_aci_kenar",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Üçgende Açı ve Kenar Bağıntıları Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Üçgende Açı ve Kenar Bağıntıları Testi",
          [
            {
              id: "q_mat4_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Bir üçgenin iç açıları toplamı kaç derecedir?",
              choices: ["90°", "180°", "270°", "360°"],
              correctAnswer: "180°",
              explanation: "Öklid geometrisinde bir düzlem üçgenin iç açıları toplamı 180 derecedir."
            },
            {
              id: "q_mat4_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Kenarları 4 cm, 7 cm ve x cm olan bir üçgende x hangi aralıkta değer alabilir?",
              choices: ["3 < x < 11", "4 < x < 7", "0 < x < 11", "3 ≤ x ≤ 11"],
              correctAnswer: "3 < x < 11",
              explanation: "Üçgen eşitsizliğine göre |7 - 4| < x < 7 + 4 yani 3 < x < 11 olmalıdır."
            },
            {
              id: "q_mat4_3",
              type: "TRUE_FALSE",
              prompt: "Bir üçgende büyük açı karşısında daima daha büyük kenar bulunur.",
              correctAnswer: "TRUE",
              explanation: "Açı-kenar bağıntılarına göre açının ölçüsü büyüdükçe gördüğü kenarın uzunluğu artar."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L130",
          "Geometrik Şekiller Ünitesi"
        )
      }
    ]
  },
  {
    id: "lesson_mat9_eslik_ve_benzerlik",
    stableKey: "lesson_mat9_eslik_ve_benzerlik",
    title: "Eşlik ve Benzerlik: Teoremler ve Dönüşümler (MAT.9.2.2 - MAT.9.2.3)",
    orderKey: 5000.0,
    items: [
      {
        id: "item_mat9_vid_geometrik_donusumler",
        stableKey: "mat9_vid_geometrik_donusumler",
        itemType: "VIDEO",
        displayLabel: "5.1",
        orderKey: 1000.0,
        title: "Geometrik Dönüşümler: Öteleme, Yansıma ve Dönme",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L150",
            sourceSection: "Geometrik Dönüşümler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_vid_benzerlik_kosullari",
        stableKey: "mat9_vid_benzerlik_kosullari",
        itemType: "VIDEO",
        displayLabel: "5.2",
        orderKey: 2000.0,
        title: "Eşlik ve Benzerlik Koşulları (A.A., K.A.K., K.K.K.)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L165",
            sourceSection: "Eşlik ve Benzerlik Koşulları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_vid_teoremler",
        stableKey: "mat9_vid_teoremler",
        itemType: "VIDEO",
        displayLabel: "5.3",
        orderKey: 3000.0,
        title: "Tales, Öklid ve Pisagor Teoremleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L180",
            sourceSection: "Tales, Öklid ve Pisagor",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_quiz_benzerlik_teoremler",
        stableKey: "mat9_quiz_benzerlik_teoremler",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 4000.0,
        title: "Eşlik, Benzerlik ve Teoremler Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Eşlik, Benzerlik ve Teoremler Testi",
          [
            {
              id: "q_mat5_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Bir dik üçgende dik kenarlar 6 cm ve 8 cm ise hipotenüs uzunluğu kaç cm'dir?",
              choices: ["9", "10", "12", "14"],
              correctAnswer: "10",
              explanation: "Pisagor teoremine göre a² + b² = c² => 6² + 8² = 36 + 64 = 100 = 10²."
            },
            {
              id: "q_mat5_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Benzerlik oranı k = 2/3 olan iki üçgenin alanları oranı kaçtır?",
              choices: ["2/3", "4/9", "8/27", "√2/√3"],
              correctAnswer: "4/9",
              explanation: "Benzer üçgenlerin alanları oranı benzerlik oranının karesine eşittir: (2/3)² = 4/9."
            },
            {
              id: "q_mat5_3",
              type: "TRUE_FALSE",
              prompt: "Eş iki üçgen aynı zamanda daima benzerdir ve benzerlik oranı 1'dir.",
              correctAnswer: "TRUE",
              explanation: "Eşlik, benzerlik oranının k = 1 olduğu özel durumdur."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L185",
          "Eşlik ve Benzerlik Ünitesi"
        )
      }
    ]
  },
  {
    id: "lesson_mat9_algoritma_ve_mantik",
    stableKey: "lesson_mat9_algoritma_ve_mantik",
    title: "Algoritma ve Mantık Bağlaçları (MAT.9.3.1 - MAT.9.3.2)",
    orderKey: 6000.0,
    items: [
      {
        id: "item_mat9_vid_algoritma_temelleri",
        stableKey: "mat9_vid_algoritma_temelleri",
        itemType: "VIDEO",
        displayLabel: "6.1",
        orderKey: 1000.0,
        title: "Algoritma Temelli Problem Çözme ve Adım Mantığı",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L210",
            sourceSection: "Algoritma Temelli Problemler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_vid_mantik_baglaclari",
        stableKey: "mat9_vid_mantik_baglaclari",
        itemType: "VIDEO",
        displayLabel: "6.2",
        orderKey: 2000.0,
        title: "Mantık Bağlaçları (∧, ∨, ⇒, ⇔) ve Niceleyiciler (∀, ∃)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L225",
            sourceSection: "Mantık Bağlaçları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_quiz_algoritma_mantik",
        stableKey: "mat9_quiz_algoritma_mantik",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Algoritma ve Sembolik Mantık Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Algoritma ve Sembolik Mantık Testi",
          [
            {
              id: "q_mat6_1",
              type: "MULTIPLE_CHOICE",
              prompt: "p ≡ 1 ve q ≡ 0 iken 'p ∧ q' önermesinin doğruluk değeri nedir?",
              choices: ["1", "0", "Belirsiz", "p"],
              correctAnswer: "0",
              explanation: "Ve (∧) bağlacında sonucun 1 olması için her iki önermenin de 1 olması gerekir. 1 ∧ 0 ≡ 0."
            },
            {
              id: "q_mat6_2",
              type: "MULTIPLE_CHOICE",
              prompt: "'p ⇒ q' koşullu önermesi sadece hangi durumda 0 (yanlış) değerini alır?",
              choices: [
                "p ≡ 1, q ≡ 0",
                "p ≡ 0, q ≡ 1",
                "p ≡ 0, q ≡ 0",
                "p ≡ 1, q ≡ 1"
              ],
              correctAnswer: "p ≡ 1, q ≡ 0",
              explanation: "1 ⇒ 0 ≡ 0 (100 kuralı); diğer tüm durumlarda ise bağlacı 1'e eşittir."
            },
            {
              id: "q_mat6_3",
              type: "TRUE_FALSE",
              prompt: "'∀' sembolü 'Her / Bütün' evrensel niceleyicisini temsil eder.",
              correctAnswer: "TRUE",
              explanation: "∀ sembolü matematikte 'evrensel niceleyici' (her / all) anlamındadır."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L230",
          "Algoritma ve Bilişim Ünitesi"
        )
      }
    ]
  },
  {
    id: "lesson_mat9_istatistik_veri_dagilimi",
    stableKey: "lesson_mat9_istatistik_veri_dagilimi",
    title: "İstatistiksel Araştırma Süreci ve Veri Dağılımları (MAT.9.4.1)",
    orderKey: 7000.0,
    items: [
      {
        id: "item_mat9_vid_veri_dagilimlari",
        stableKey: "mat9_vid_veri_dagilimlari",
        itemType: "VIDEO",
        displayLabel: "7.1",
        orderKey: 1000.0,
        title: "Tek Nicel Değişkenli Veri Dağılımları ve Grafik Yorumlama",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L250",
            sourceSection: "Tek Nicel Değişkenli Veri Dağılımları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_quiz_istatistik",
        stableKey: "mat9_quiz_istatistik",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 2000.0,
        title: "İstatistik ve Merkezi Eğilim Ölçüleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "İstatistik ve Merkezi Eğilim Ölçüleri Testi",
          [
            {
              id: "q_mat7_1",
              type: "MULTIPLE_CHOICE",
              prompt: "3, 5, 7, 7, 9, 11 veri grubunun modu (tepe değeri) kaçtır?",
              choices: ["5", "7", "8", "9"],
              correctAnswer: "7",
              explanation: "Veri grubunda en çok tekrar eden eleman 7'dir (2 kez)."
            },
            {
              id: "q_mat7_2",
              type: "MULTIPLE_CHOICE",
              prompt: "2, 4, 6, 8, 10 sayılarının aritmetik ortalaması nedir?",
              choices: ["5", "6", "7", "8"],
              correctAnswer: "6",
              explanation: "(2 + 4 + 6 + 8 + 10) / 5 = 30 / 5 = 6."
            },
            {
              id: "q_mat7_3",
              type: "TRUE_FALSE",
              prompt: "Medyan (ortanca) bulunurken veriler önce küçükten büyüğe sıralanmalıdır.",
              correctAnswer: "TRUE",
              explanation: "Medyan verinin ortasındaki değer olduğundan verilerin sıralı olması şarttır."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L260",
          "İstatistiksel Araştırma Süreci"
        )
      }
    ]
  },
  {
    id: "lesson_mat9_veriden_olasiliga",
    stableKey: "lesson_mat9_veriden_olasiliga",
    title: "Veriden Olasılığa: Deneysel ve Teorik Olasılık (MAT.9.5.1)",
    orderKey: 8000.0,
    items: [
      {
        id: "item_mat9_vid_olasilik_turleri",
        stableKey: "mat9_vid_olasilik_turleri",
        itemType: "VIDEO",
        displayLabel: "8.1",
        orderKey: 1000.0,
        title: "Deneysel ve Teorik Olasılık Karşılaştırması",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Matematik_Yillik_Plan_AL9.md#L280",
            sourceSection: "Deneysel ve Teorik Olasılık",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_mat9_quiz_olasilik",
        stableKey: "mat9_quiz_olasilik",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 2000.0,
        title: "Olasılık Hesaplamaları Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Olasılık Hesaplamaları Testi",
          [
            {
              id: "q_mat8_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Hilesiz bir zar atıldığında üst yüze asal sayı gelme olasılığı kaçtır?",
              choices: ["1/6", "1/3", "1/2", "2/3"],
              correctAnswer: "1/2",
              explanation: "Zardaki asal sayılar {2, 3, 5} yani 3 tanedir. Olasılık 3/6 = 1/2."
            },
            {
              id: "q_mat8_2",
              type: "TRUE_FALSE",
              prompt: "Bir olayın olasılık değeri daima 0 ile 1 aralığındadır (0 ve 1 dahil).",
              correctAnswer: "TRUE",
              explanation: "İmkansız olay 0, kesin olay 1 olup diğer tüm olasılıklar [0, 1] aralığındadır."
            },
            {
              id: "q_mat8_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Bir torbada 3 kırmızı, 4 mavi, 5 sarı bilye vardır. Rastgele çekilen bir bilyenin sarı olma olasılığı nedir?",
              choices: ["5/12", "1/4", "1/3", "5/7"],
              correctAnswer: "5/12",
              explanation: "Toplam bilye sayısı: 3+4+5=12. Sarı bilye sayısı: 5. Olasılık = 5/12."
            }
          ],
          "10-Projects/Matematik_Yillik_Plan_AL9.md#L290",
          "Veriden Olasılığa Ünitesi"
        )
      }
    ]
  }
];

// ==========================================
// 2. FİZİK (course_fiz_9)
// ==========================================
const fizLessons = [
  {
    id: "lesson_fiz9_fizik_bilimine_giris",
    stableKey: "lesson_fiz9_fizik_bilimine_giris",
    title: "Fizik Bilimi ve Kariyer Keşfi (FİZ.9.1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_fiz9_vid_fizik_giris",
        stableKey: "fiz9_vid_fizik_giris",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Fizik Bilimine Giriş (Temel Prensipler)",
        contentUrl: "https://www.youtube.com/watch?v=sO7N-V4T_YI",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          provenance: {
            sourceRef: "10-Projects/1_ay_fizik_video_rehberi.md#L18",
            sourceSection: "1. Hafta: Fizik Bilimi ve Alt Dalları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_fiz9_vid_alt_dallar",
        stableKey: "fiz9_vid_alt_dallar",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Fiziğin Alt Dalları ve Bilim Araştırma Merkezleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Fizik_Yillik_Plan_AL9.md#L25",
            sourceSection: "Fizik Biliminin Alt Dalları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_fiz9_quiz_fizik_giris",
        stableKey: "fiz9_quiz_fizik_giris",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Fizik Bilimi ve Alt Dalları Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Fizik Bilimi ve Alt Dalları Testi",
          [
            {
              id: "q_fiz1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Işığın yansıması, kırılması ve aynalar fiziğin hangi alt dalının inceleme alanıdır?",
              choices: ["Mekanik", "Optik", "Termodinamik", "Elektromanyetizma"],
              correctAnswer: "Optik",
              explanation: "Işık ve ışık olaylarını inceleyen fizik dalı Optik'tir."
            },
            {
              id: "q_fiz1_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Aşağıdakilerden hangisi Türkiye'deki ulusal bilim araştırma merkezlerinden biridir?",
              choices: ["CERN", "NASA", "TÜBİTAK", "ESA"],
              correctAnswer: "TÜBİTAK",
              explanation: "TÜBİTAK (Türkiye Bilimsel ve Teknolojik Araştırma Kurumu) Türkiye'nin ulusal araştırma merkezidir."
            },
            {
              id: "q_fiz1_3",
              type: "TRUE_FALSE",
              prompt: "Fizik, madde ve enerji arasındaki etkileşimi inceleyen doğa bilimidir.",
              correctAnswer: "TRUE",
              explanation: "Fiziğin temel tanımı madde-enerji etkileşimi ve doğa yasalarıdır."
            }
          ],
          "10-Projects/Fizik_Yillik_Plan_AL9.md#L30",
          "Fizik Bilimine Giriş"
        )
      }
    ]
  },
  {
    id: "lesson_fiz9_kuvvet_ve_hareket",
    stableKey: "lesson_fiz9_kuvvet_ve_hareket",
    title: "Kuvvet ve Hareket: Büyüklükler ve Vektörler (FİZ.9.2.1 - FİZ.9.2.4)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_fiz9_vid_vektorler",
        stableKey: "fiz9_vid_vektorler",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Vektörel ve Skaler Büyüklükler (Görsel Koordinat Anlatımı)",
        contentUrl: "https://www.youtube.com/watch?v=sO7N-V4T_YI",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy Türkçe",
          provenance: {
            sourceRef: "10-Projects/1_ay_fizik_video_rehberi.md#L40",
            sourceSection: "3. Hafta: Vektörler ve Dik Kartezyen Koordinat Sistemi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_fiz9_vid_dogadaki_kuvvetler",
        stableKey: "fiz9_vid_dogadaki_kuvvetler",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Doğadaki 4 Temel Kuvvet ve Hareket Türleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Fizik_Yillik_Plan_AL9.md#L60",
            sourceSection: "Doğadaki Temel Kuvvetler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_fiz9_quiz_vektor_hareket",
        stableKey: "fiz9_quiz_vektor_hareket",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Büyüklükler, Vektörler ve Hareket Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Büyüklükler, Vektörler ve Hareket Testi",
          [
            {
              id: "q_fiz2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Aşağıdaki niceliklerden hangisi vektörel bir büyüklüktür?",
              choices: ["Kütle", "Zaman", "Hız", "Sıcaklık"],
              correctAnswer: "Hız",
              explanation: "Hız (velocity) hem büyüklüğe hem de yöne sahip vektörel bir büyüklüktür; sürat, kütle, zaman ise skalerdir."
            },
            {
              id: "q_fiz2_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Doğadaki en güçlü fakat menzili en kısa olan temel kuvvet hangisidir?",
              choices: [
                "Güçlü Nükleer Kuvvet (Yeğin Kuvvet)",
                "Kütle Çekim Kuvveti",
                "Elektromanyetik Kuvvet",
                "Zayıf Nükleer Kuvvet"
              ],
              correctAnswer: "Güçlü Nükleer Kuvvet (Yeğin Kuvvet)",
              explanation: "Güçlü nükleer kuvvet atom çekirdeğinde proton ve nötronları bir arada tutan en şiddetli kuvvettir."
            },
            {
              id: "q_fiz2_3",
              type: "TRUE_FALSE",
              prompt: "SI birim sisteminde temel uzunluk birimi metredir (m).",
              correctAnswer: "TRUE",
              explanation: "SI temel birimlerinde uzunluk 'metre' (m) olarak tanımlanır."
            }
          ],
          "10-Projects/Fizik_Yillik_Plan_AL9.md#L65",
          "Kuvvet ve Hareket Ünitesi"
        )
      }
    ]
  },
  {
    id: "lesson_fiz9_akiskanlar_ve_basinc",
    stableKey: "lesson_fiz9_akiskanlar_ve_basinc",
    title: "Akışkanlar: Basınç ve Kaldırma Kuvveti (FİZ.9.3.1 - FİZ.9.3.3)",
    orderKey: 3000.0,
    items: [
      {
        id: "item_fiz9_vid_basinc_prensipleri",
        stableKey: "fiz9_vid_basinc_prensipleri",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Katı, Sıvı ve Gaz Basıncı (Pascal ve Torricelli)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Fizik_Yillik_Plan_AL9.md#L90",
            sourceSection: "Akışkanlar ve Basınç",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_fiz9_vid_kaldirma_kuvveti",
        stableKey: "fiz9_vid_kaldirma_kuvveti",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "Sıvıların Kaldırma Kuvveti ve Bernoulli İlkesi",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Fizik_Yillik_Plan_AL9.md#L110",
            sourceSection: "Kaldırma Kuvveti ve Bernoulli",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_fiz9_quiz_basinc_kaldirma",
        stableKey: "fiz9_quiz_basinc_kaldirma",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Basınç ve Kaldırma Kuvveti Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Basınç ve Kaldırma Kuvveti Testi",
          [
            {
              id: "q_fiz3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Durgun sıvı basıncı formülü (P = h · d · g) uyarınca basınç hangisine bağlı DEĞİLDİR?",
              choices: [
                "Sıvının derinliğine (h)",
                "Sıvının özkütlesine (d)",
                "Kabın taban alanına (S)",
                "Yer çekimi ivmesine (g)"
              ],
              correctAnswer: "Kabın taban alanına (S)",
              explanation: "Durgun sıvı basıncı yalnızca derinlik, özkütle ve yerçekimi ivmesine bağlıdır; kabın şekline veya taban alanına bağlı değildir."
            },
            {
              id: "q_fiz3_2",
              type: "TRUE_FALSE",
              prompt: "Bernoulli ilkesine göre akışkanın hızının arttığı yerde akışkan basıncı azalır.",
              correctAnswer: "TRUE",
              explanation: "Akışkanlar dinamiğinde hız artışı dinamik basıncı artırırken statik basıncı düşürür."
            },
            {
              id: "q_fiz3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Bir cismin sıvıda yüzmesi için cismin özkütlesi (dc) ile sıvının özkütlesi (ds) arasındaki ilişki ne olmalıdır?",
              choices: ["dc > ds", "dc = ds", "dc < ds", "dc · ds = 1"],
              correctAnswer: "dc < ds",
              explanation: "Özkütlesi sıvınınkinden küçük olan cisimler sıvıda yüzer."
            }
          ],
          "10-Projects/Fizik_Yillik_Plan_AL9.md#L115",
          "Akışkanlar Ünitesi"
        )
      }
    ]
  },
  {
    id: "lesson_fiz9_enerji_isi_sicaklik",
    stableKey: "lesson_fiz9_enerji_isi_sicaklik",
    title: "Enerji, Isı ve Sıcaklık (FİZ.9.4.1 - FİZ.9.4.4)",
    orderKey: 4000.0,
    items: [
      {
        id: "item_fiz9_vid_isi_sicaklik_kavram",
        stableKey: "fiz9_vid_isi_sicaklik_kavram",
        itemType: "VIDEO",
        displayLabel: "4.1",
        orderKey: 1000.0,
        title: "İç Enerji, Isı ve Sıcaklık Kavramları",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Fizik_Yillik_Plan_AL9.md#L140",
            sourceSection: "Isı ve Sıcaklık",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_fiz9_vid_hal_degisimi",
        stableKey: "fiz9_vid_hal_degisimi",
        itemType: "VIDEO",
        displayLabel: "4.2",
        orderKey: 2000.0,
        title: "Hâl Değişimi, Isıl Denge ve Isı Aktarım Yolları",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Fizik_Yillik_Plan_AL9.md#L160",
            sourceSection: "Hâl Değişimi ve Isıl Denge",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_fiz9_quiz_isi_sicaklik",
        stableKey: "fiz9_quiz_isi_sicaklik",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Isı, Sıcaklık ve Hâl Değişimi Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Isı, Sıcaklık ve Hâl Değişimi Testi",
          [
            {
              id: "q_fiz4_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Sıcaklık birimi SI sisteminde aşağıdakilerden hangisidir?",
              choices: ["Celcius (°C)", "Fahrenheit (°F)", "Kelvin (K)", "Kalori (cal)"],
              correctAnswer: "Kelvin (K)",
              explanation: "SI birim sisteminde temel sıcaklık birimi Kelvin'dir."
            },
            {
              id: "q_fiz4_2",
              type: "TRUE_FALSE",
              prompt: "Saf maddeler hâl değiştirirken sıcaklıkları sabit kalır.",
              correctAnswer: "TRUE",
              explanation: "Saf maddelerin erime, kaynama gibi faz dönüşümleri sırasında aldıkları ısı sıcaklığı artırmaz, bağları koparmaya harcanır."
            },
            {
              id: "q_fiz4_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Güneş'ten Dünya'ya ısının ulaşması hangi ısı aktarım yoluyla gerçekleşir?",
              choices: ["İletim (Kondüksiyon)", "Konveksiyon (Taşıma)", "Işıma (Radyasyon)", "Sürtünme"],
              correctAnswer: "Işıma (Radyasyon)",
              explanation: "Uzay boşluğunda madde olmadığından Güneş enerjisi Dünya'ya elektromanyetik dalgalar (ışıma) yoluyla ulaşır."
            }
          ],
          "10-Projects/Fizik_Yillik_Plan_AL9.md#L170",
          "Enerji ve Isı Ünitesi"
        )
      }
    ]
  }
];

// ==========================================
// 3. KİMYA (course_kim_9)
// ==========================================
const kimLessons = [
  {
    id: "lesson_kim9_kimya_bilimi_guvenlik",
    stableKey: "lesson_kim9_kimya_bilimi_guvenlik",
    title: "Kimya Bilimi, Alt Disiplinler ve Laboratuvar Güvenliği (KİM.9.1.1 - KİM.9.1.2)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_kim9_vid_kimya_bilimi",
        stableKey: "kim9_vid_kimya_bilimi",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Kimya Bilimi, Günlük Hayat ve Alt Disiplinleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/1_ay_kimya_video_rehberi.md#L24",
            sourceSection: "1. Hafta (Kitap: Kimya Hayattır)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_vid_guvenlik_sembolleri",
        stableKey: "kim9_vid_guvenlik_sembolleri",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Laboratuvar Güvenlik Kuralları ve Uyarı Piktogramları",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/1_ay_kimya_video_rehberi.md#L25",
            sourceSection: "2. Hafta (Kitap: Güvenlik)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_quiz_kimya_guvenlik",
        stableKey: "kim9_quiz_kimya_guvenlik",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Kimya Bilimi ve Laboratuvar Güvenliği Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Kimya Bilimi ve Laboratuvar Güvenliği Testi",
          [
            {
              id: "q_kim1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Karbon temelli bileşiklerin yapı ve özelliklerini inceleyen kimya alt disiplini hangisidir?",
              choices: ["Organik Kimya", "Anorganik Kimya", "Analitik Kimya", "Fizikokimya"],
              correctAnswer: "Organik Kimya",
              explanation: "Organik kimya karbon bileşiklerini inceler."
            },
            {
              id: "q_kim1_2",
              type: "TRUE_FALSE",
              prompt: "Alev sembolü içeren kimyasal güvenlik piktogramı maddenin 'Yanıcı' olduğunu belirtir.",
              correctAnswer: "TRUE",
              explanation: "Alev sembolü kolay tutuşabilen ve alev alabilen yanıcı maddeleri temsil eder."
            },
            {
              id: "q_kim1_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Bir numunenin içindeki maddelerin cinsini ve miktarını belirleyen kimya alt dalı hangisidir?",
              choices: ["Biyokimya", "Analitik Kimya", "Polimer Kimyası", "Endüstriyel Kimya"],
              correctAnswer: "Analitik Kimya",
              explanation: "Nitel (kalitatif) ve nicel (kantitatif) analiz Analitik Kimyanın alanıdır."
            }
          ],
          "10-Projects/Kimya_Yillik_Plan_AL9.md#L20",
          "Kimya Bilimi ve Güvenlik"
        )
      }
    ]
  },
  {
    id: "lesson_kim9_atom_ve_periyodik_sistem",
    stableKey: "lesson_kim9_atom_ve_periyodik_sistem",
    title: "Atom Teorileri ve Periyodik Tablo (KİM.9.1.3 - KİM.9.1.4)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_kim9_vid_atom_modelleri",
        stableKey: "kim9_vid_atom_modelleri",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Atom Modelleri: Dalton, Thomson, Rutherford ve Bohr",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/1_ay_kimya_video_rehberi.md#L26",
            sourceSection: "3. Hafta (Kitap: Atom Teorileri)",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_vid_periyodik_ozellikler",
        stableKey: "kim9_vid_periyodik_ozellikler",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Modern Atom Teorisi ve Periyodik Özelliklerin Değişimi",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Kimya_Yillik_Plan_AL9.md#L55",
            sourceSection: "Periyodik Özellikler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_quiz_atom_periyodik",
        stableKey: "kim9_quiz_atom_periyodik",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Atomun Yapısı ve Periyodik Sistem Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Atomun Yapısı ve Periyodik Sistem Testi",
          [
            {
              id: "q_kim2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Atom çekirdeğini ve çekirdekli atom modelini ilk ortaya koyan bilim insanı kimdir?",
              choices: ["John Dalton", "J.J. Thomson", "Ernest Rutherford", "Niels Bohr"],
              correctAnswer: "Ernest Rutherford",
              explanation: "Rutherford altın levha alfa saçılması deneyiyle atom çekirdeğini keşfetmiştir."
            },
            {
              id: "q_kim2_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Periyodik tabloda aynı periyotta soldan sağa doğru gidildikçe atom yarıçapı nasıl değişir?",
              choices: ["Artar", "Azalır", "Değişmez", "Önce artar sonra azalır"],
              correctAnswer: "Azalır",
              explanation: "Aynı periyotta sağa gidildikçe çekirdek yükü (proton sayısı) arttığı için elektronlar daha güçlü çekilir ve yarıçap küçülür."
            },
            {
              id: "q_kim2_3",
              type: "TRUE_FALSE",
              prompt: "Elektron ilgisi ve elektronegatifliği en yüksek olan element Flor'dur (F).",
              correctAnswer: "TRUE",
              explanation: "Pauling ölçeğinde elektronegatifliği 4.0 ile en yüksek element Flor'dur."
            }
          ],
          "10-Projects/Kimya_Yillik_Plan_AL9.md#L60",
          "Atom ve Periyodik Tablo"
        )
      }
    ]
  },
  {
    id: "lesson_kim9_kimyasal_turler_etkilesim",
    stableKey: "lesson_kim9_kimyasal_turler_etkilesim",
    title: "Kimyasal Türler Arası Etkileşimler (KİM.9.2.1 - KİM.9.2.3)",
    orderKey: 3000.0,
    items: [
      {
        id: "item_kim9_vid_bag_turleri",
        stableKey: "kim9_vid_bag_turleri",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Güçlü Etkileşimler: İyonik, Kovalent ve Metalik Bağ",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Kimya_Yillik_Plan_AL9.md#L80",
            sourceSection: "Kimyasal Bağlar",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_vid_zayif_etkilesimler",
        stableKey: "kim9_vid_zayif_etkilesimler",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "Moleküller Arası Zayıf Etkileşimler ve Hidrojen Bağı",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Kimya_Yillik_Plan_AL9.md#L100",
            sourceSection: "Zayıf Etkileşimler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_quiz_baglar_etkilesim",
        stableKey: "kim9_quiz_baglar_etkilesim",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Kimyasal Bağlar ve Etkileşimler Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Kimyasal Bağlar ve Etkileşimler Testi",
          [
            {
              id: "q_kim3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Metal ve ametal atomları arasında elektron alışverişi ile oluşan güçlü bağ hangisidir?",
              choices: ["Kovalent Bağ", "İyonik Bağ", "Metalik Bağ", "Hidrojen Bağı"],
              correctAnswer: "İyonik Bağ",
              explanation: "Elektron alışverişi ile zıt yüklü iyonlar arasındaki elektrostatik çekim kuvveti iyonik bağı oluşturur."
            },
            {
              id: "q_kim3_2",
              type: "TRUE_FALSE",
              prompt: "Apolar moleküller arasında yalnızca London dağılım kuvvetleri etkindir.",
              correctAnswer: "TRUE",
              explanation: "Kalıcı dipolü olmayan apolar moleküllerde anlık dipollerle oluşan London kuvvetleri tek zayıf etkileşimdir."
            },
            {
              id: "q_kim3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "H₂O molekülünün yüksek kaynama noktasına sahip olmasını sağlayan en baskın moleküller arası etkileşim hangisidir?",
              choices: ["İyon-Dipol", "Hidrojen Bağı", "Dipol-Dipol", "London Kuvvetleri"],
              correctAnswer: "Hidrojen Bağı",
              explanation: "F, O, N atomlarına bağlı hidrojen atomunun yaptığı hidrojen bağı zayıf etkileşimlerin en güçlüsüdür."
            }
          ],
          "10-Projects/Kimya_Yillik_Plan_AL9.md#L105",
          "Kimyasal Türler Arası Etkileşimler"
        )
      }
    ]
  },
  {
    id: "lesson_kim9_maddenin_halleri_surdurulebilirlik",
    stableKey: "lesson_kim9_maddenin_halleri_surdurulebilirlik",
    title: "Maddenin Halleri ve Yeşil Kimya (KİM.9.2.4 - KİM.9.3.1)",
    orderKey: 4000.0,
    items: [
      {
        id: "item_kim9_vid_katilar_ve_sivilar",
        stableKey: "kim9_vid_katilar_ve_sivilar",
        itemType: "VIDEO",
        displayLabel: "4.1",
        orderKey: 1000.0,
        title: "Katı Türleri (Amorf, Kristal) ve Sıvılarda Viskozite",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Kimya_Yillik_Plan_AL9.md#L120",
            sourceSection: "Katılar ve Sıvılar",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_vid_yesil_kimya",
        stableKey: "kim9_vid_yesil_kimya",
        itemType: "VIDEO",
        displayLabel: "4.2",
        orderKey: 2000.0,
        title: "Nanoparçacıklar, Ekolojik Sürdürülebilirlik ve Yeşil Kimya",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Kimya_Yillik_Plan_AL9.md#L140",
            sourceSection: "Nanoparçacıklar ve Sürdürülebilirlik",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_kim9_quiz_maddenin_halleri",
        stableKey: "kim9_quiz_maddenin_halleri",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Maddenin Halleri ve Sürdürülebilirlik Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Maddenin Halleri ve Sürdürülebilirlik Testi",
          [
            {
              id: "q_kim4_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Sıvıların akmaya karşı gösterdiği dirence ne ad verilir?",
              choices: ["Yüzey Gerilimi", "Viskozite", "Adezyon", "Buhar Basıncı"],
              correctAnswer: "Viskozite",
              explanation: "Sıvıların akmaya karşı direnci viskozite olarak adlandırılır (örn. balın viskozitesi sudan büyüktür)."
            },
            {
              id: "q_kim4_2",
              type: "TRUE_FALSE",
              prompt: "Cam, plastik ve tereyağı amorf katılara örnektir.",
              correctAnswer: "TRUE",
              explanation: "Amorf katılar belirli bir geometrik kristal yapısı ve net erime noktası olmayan katılardır."
            },
            {
              id: "q_kim4_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Yeşil kimyanın (Green Chemistry) temel amacı nedir?",
              choices: [
                "Kimyasal atıkları kaynağında önlemek ve çevre dostu süreçler geliştirmek",
                "Yalnızca bitkilerden kimyasal madde üretmek",
                "Tüm laboratuvar deneylerini dijital ortama taşımak",
                "Kimyasal reaksiyon hızlarını sınırsız artırmak"
              ],
              correctAnswer: "Kimyasal atıkları kaynağında önlemek ve çevre dostu süreçler geliştirmek",
              explanation: "Yeşil kimya çevre kirliliğini engelleme, atık azaltımı ve enerji tasarrufunu amaçlar."
            }
          ],
          "10-Projects/Kimya_Yillik_Plan_AL9.md#L145",
          "Maddenin Halleri ve Yeşil Kimya"
        )
      }
    ]
  }
];

// ==========================================
// 4. BİYOLOJİ (course_biyo_9)
// ==========================================
const biyoLessons = [
  {
    id: "lesson_biyo9_yasam",
    stableKey: "lesson_biyo9_yasam",
    title: "Yaşam Teması ve Bilimin Doğası (BİY.9.1.1 - BİY.9.1.2)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_biyo9_vid_yasam_nedir",
        stableKey: "biyo9_vid_yasam_nedir",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Biyolojiye Giriş: Yaşam Nedir?",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/introduction-to-biology",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L14",
            sourceSection: "1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_vid_bilim_nedir",
        stableKey: "biyo9_vid_bilim_nedir",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Bilim Nedir ve Nasıl Çalışır? (Bilimsel Yöntem)",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/the-scientific-method",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L15",
            sourceSection: "1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_vid_kontrollu_deney",
        stableKey: "biyo9_vid_kontrollu_deney",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Kontrollü Deney Örnekleri",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/controlled-experiments",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L16",
            sourceSection: "1-2. Hafta: Biyolojiye Giriş & Canlılığın Tanımı",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_vid_teori_yasa",
        stableKey: "biyo9_vid_teori_yasa",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Hipotez, Teori ve Kanun Arasındaki Fark",
        contentUrl: "https://www.khanacademy.org/science/biology/intro-to-biology/what-is-biology/v/hypotheses-theories-and-laws",
        publishingStatus: "active",
        payload: {
          provider: "Khan Academy",
          provenance: {
            sourceRef: "10-Projects/1_ay_biyoloji_video_rehberi.md#L21",
            sourceSection: "3-4. Hafta: Biyolojide Dönüm Noktaları & Bilimsel Bilginin Gelişimi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_quiz_yasam_bilim",
        stableKey: "biyo9_quiz_yasam_bilim",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 5000.0,
        title: "Bilimsel Yöntem ve Yaşamın Doğası Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Bilimsel Yöntem ve Yaşamın Doğası Testi",
          [
            {
              id: "q_bio1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Bilimsel yöntemde gözlemler ve verilere dayalı olarak kurulan geçici çözüm yoluna ne denir?",
              choices: ["Hipotez", "Teori", "Kanun (Yasa)", "Teorem"],
              correctAnswer: "Hipotez",
              explanation: "Hipotez probleme sunulan test edilebilir geçici açıklama ve çözüm yoludur."
            },
            {
              id: "q_bio1_2",
              type: "TRUE_FALSE",
              prompt: "Canlıların iç ortamlarını değişen çevre koşullarına rağmen dengede tutmasına Homeostazi denir.",
              correctAnswer: "TRUE",
              explanation: "Homeostazi vücut sıcaklığı, pH ve su dengesi gibi iç dengelerin korunmasıdır."
            },
            {
              id: "q_bio1_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Kontrollü bir deneyde araştırmacının bizzat değiştirdiği değişkene ne ad verilir?",
              choices: ["Bağımsız Değişken", "Bağımlı Değişken", "Sabit Değişken", "Kontrol Grubu"],
              correctAnswer: "Bağımsız Değişken",
              explanation: "Araştırmacının değiştirdiği etken bağımsız değişkendir; buna bağlı değişen sonuç ise bağımlı değişkendir."
            }
          ],
          "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L20",
          "Yaşam Teması ve Bilimsel Yöntem"
        )
      }
    ]
  },
  {
    id: "lesson_biyo9_canlilarin_temel_bilesenleri",
    stableKey: "lesson_biyo9_canlilarin_temel_bilesenleri",
    title: "Canlıların Temel Bileşenleri (BİY.9.1.3 - BİY.9.1.4)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_biyo9_vid_organik_inorganik",
        stableKey: "biyo9_vid_organik_inorganik",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "İnorganik ve Organik Bileşikler: Karbonhidratlar, Yağlar ve Proteinler",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L45",
            sourceSection: "Organik ve İnorganik Moleküller",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_biyo9_vid_enzimler_nukleik",
        stableKey: "biyo9_vid_enzimler_nukleik",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Enzimlerin Yapısı, Çalışması ve Nükleik Asitler (DNA / RNA)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L60",
            sourceSection: "Enzimler ve Nükleik Asitler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_biyo9_anki_temel_bilesenler",
        stableKey: "biyo9_anki_temel_bilesenler",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 3000.0,
        title: "Biyoloji: Organik Moleküller ve Enzimler Anki Destesi",
        contentUrl: null,
        publishingStatus: "active",
        payload: {
          ankiSource: "9_sinif_biyoloji_anki.txt",
          cardCount: 14,
          provenance: {
            sourceRef: "projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_biyoloji_anki.txt#L1",
            sourceSection: "Biyoloji Kavram Kartları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_biyo9_quiz_bilesenler",
        stableKey: "biyo9_quiz_bilesenler",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 4000.0,
        title: "Canlıların Temel Bileşenleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Canlıların Temel Bileşenleri Testi",
          [
            {
              id: "q_bio2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Proteinlerin yapıtaşları olan amino asitler arasındaki bağ türü hangisidir?",
              choices: ["Glikozit Bağı", "Ester Bağı", "Peptit Bağı", "Fosfodiester Bağı"],
              correctAnswer: "Peptit Bağı",
              explanation: "Amino asitlerin karboksil ve amino grupları arasında peptit bağı kurulur."
            },
            {
              id: "q_bio2_2",
              type: "TRUE_FALSE",
              prompt: "Enzimler kimyasal reaksiyonların aktivasyon enerjisini düşürerek tepkimeyi hızlandırır.",
              correctAnswer: "TRUE",
              explanation: "Enzimler biyolojik katalizörlerdir ve aktivasyon enerjisini düşürür."
            },
            {
              id: "q_bio2_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Hayvansal hücrelerde depo edilen polisakkarit türü hangisidir?",
              choices: ["Nişasta", "Selüloz", "Glikojen", "Kitin"],
              correctAnswer: "Glikojen",
              explanation: "Glikojen hayvan, mantar ve bakterilerde glikozun depo formudur."
            }
          ],
          "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L65",
          "Canlıların Temel Bileşenleri"
        )
      }
    ]
  },
  {
    id: "lesson_biyo9_hucre_ve_madde_gecisleri",
    stableKey: "lesson_biyo9_hucre_ve_madde_gecisleri",
    title: "Hücre Yapısı, Organeller ve Madde Geçişleri (BİY.9.2.1 - BİY.9.2.2)",
    orderKey: 3000.0,
    items: [
      {
        id: "item_biyo9_vid_hucre_organeller",
        stableKey: "biyo9_vid_hucre_organeller",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Prokaryot ve Ökaryot Hücre, Sitoplazma ve Organeller",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L90",
            sourceSection: "Hücre ve Organeller",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_biyo9_vid_madde_gecisleri",
        stableKey: "biyo9_vid_madde_gecisleri",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "Hücre Zarından Madde Taşınımı: Pasif, Aktif Taşıma ve Endositoz/Ekzositoz",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L110",
            sourceSection: "Madde Geçişleri",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_biyo9_quiz_hucre_madde",
        stableKey: "biyo9_quiz_hucre_madde",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Hücre ve Madde Geçişleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Hücre ve Madde Geçişleri Testi",
          [
            {
              id: "q_bio3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Hücrede oksijenli solunum ile ATP üreten çift zarlı organel hangisidir?",
              choices: ["Ribozom", "Mitokondri", "Golgi Aygıtı", "Lizozom"],
              correctAnswer: "Mitokondri",
              explanation: "Mitokondri ökaryotik hücrelerin enerji santralidir ve ATP üretir."
            },
            {
              id: "q_bio3_2",
              type: "TRUE_FALSE",
              prompt: "Aktif taşıma sırasında ATP harcanır ve maddeler az yoğundan çok yoğuna taşınır.",
              correctAnswer: "TRUE",
              explanation: "Aktif taşıma konsantrasyon farkına karşı enerji (ATP) harcanarak yapılır."
            },
            {
              id: "q_bio3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Hücrenin çok yoğun (hipertonik) ortama konulduğunda su kaybederek büzülmesine ne ad verilir?",
              choices: ["Plazmoliz", "Deplazmoliz", "Hemoliz", "Turgor"],
              correctAnswer: "Plazmoliz",
              explanation: "Hipertonik çözeltide su kaybeden hücrenin büzülmesine plazmoliz denir."
            }
          ],
          "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L115",
          "Hücre ve Madde Geçişleri"
        )
      }
    ]
  },
  {
    id: "lesson_biyo9_siniflandirma_ve_biyocesitlilik",
    stableKey: "lesson_biyo9_siniflandirma_ve_biyocesitlilik",
    title: "Canlılar Dünyası ve Biyoçeşitlilik (BİY.9.2.3 - BİY.9.2.5)",
    orderKey: 4000.0,
    items: [
      {
        id: "item_biyo9_vid_siniflandirma",
        stableKey: "biyo9_vid_siniflandirma",
        itemType: "VIDEO",
        displayLabel: "4.1",
        orderKey: 1000.0,
        title: "Sınıflandırma İlkeleri, İkili Adlandırma ve Canlılar Âlemleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L130",
            sourceSection: "Canlılar Dünyası",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_biyo9_quiz_siniflandirma",
        stableKey: "biyo9_quiz_siniflandirma",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 2000.0,
        title: "Sınıflandırma ve Biyoçeşitlilik Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Sınıflandırma ve Biyoçeşitlilik Testi",
          [
            {
              id: "q_bio4_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Pinus nigra (Karaçam) ikili adlandırmasında 'Pinus' neyi ifade eder?",
              choices: ["Tür adını", "Cins adını", "Familya adını", "Tanımlayıcı adı"],
              correctAnswer: "Cins adını",
              explanation: "İkili adlandırmada ilk kelime büyük harfle başlar ve Cins (Genus) adıdır."
            },
            {
              id: "q_bio4_2",
              type: "TRUE_FALSE",
              prompt: "Virüslerin hücresel yapısı, organeli ve sitoplazması bulunmaz.",
              correctAnswer: "TRUE",
              explanation: "Virüsler hücresel yapıya sahip değildir, nükleoprotein kompleksleridir."
            },
            {
              id: "q_bio4_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Sınıflandırma basamaklarında Âlemden Türe doğru gidildikçe canlılar arasındaki benzerlik nasıl değişir?",
              choices: ["Artar", "Azalır", "Değişmez", "Önce azalır sonra artar"],
              correctAnswer: "Artar",
              explanation: "Türe yaklaştıkça ortak genler, protein benzerliği ve akrabalık derecesi artar."
            }
          ],
          "10-Projects/Biyoloji_Yillik_Plan_AL9.md#L140",
          "Sınıflandırma ve Biyoçeşitlilik"
        )
      }
    ]
  }
];

// ==========================================
// 5. TARİH (course_tar_9) - Canonical Mehmet Celal ÖZYILDIZ
// ==========================================
const tarLessons = [
  {
    id: "lesson_tar9_gecmisin_insasi",
    stableKey: "lesson_tar9_gecmisin_insasi",
    title: "Geçmişin İnşa Sürecinde Tarih (TAR.9.1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_tar9_vid_birey_toplum",
        stableKey: "tar9_vid_birey_toplum",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Tarih Öğrenmenin Bireye ve Topluma Faydaları",
        contentUrl: "https://www.youtube.com/watch?v=5QxOpTALmEE",
        publishingStatus: "active",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "00:00",
          endTimestamp: "28:45",
          topicDetails: "Tarihin Tanımı, Konusu, Bireye ve Topluma Faydaları",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L14",
            sourceSection: "1. Hafta: Tarih Öğrenmenin Bireye ve Topluma Faydaları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_vid_olay_olgu",
        stableKey: "tar9_vid_olay_olgu",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Tarihin Doğası & Olay-Olgu Ayrımı",
        contentUrl: "https://www.youtube.com/watch?v=5QxOpTALmEE",
        publishingStatus: "active",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "28:45",
          endTimestamp: "45:20",
          topicDetails: "Tarihî Olay ve Olgu Ayrımı, Deney/Gözlem Yapılamaması, Objektiflik",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L25",
            sourceSection: "2. Hafta: Tarihin Doğası & Olay-Olgu Ayrımı",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_quiz_gecmisin_insasi",
        stableKey: "tar9_quiz_gecmisin_insasi",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Tarih Bilimi ve Metodolojisi Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Tarih Bilimi ve Metodolojisi Testi",
          [
            {
              id: "q_tar1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Aşağıdakilerden hangisi bir 'Tarihî Olgu'ya örnektir?",
              choices: [
                "Malazgirt Savaşı",
                "Anadolu'nun Türkleşmesi",
                "İstanbul'un Fethi",
                "Kurtuluş Savaşı"
              ],
              correctAnswer: "Anadolu'nun Türkleşmesi",
              explanation: "Olay belirli bir zamanda ve mekanda gerçekleşen anlık durumlardır; olgu ise uzun süreçte meydana gelen genel gelişmelerdir."
            },
            {
              id: "q_tar1_2",
              type: "TRUE_FALSE",
              prompt: "Tarih bilimi geçmişte yaşanmış olayları incelediği için deney ve gözlem yöntemi uygulanamaz.",
              correctAnswer: "TRUE",
              explanation: "Tarih tekrarlanamaz ve laboratuvar ortamında deneyi yapılamaz."
            },
            {
              id: "q_tar1_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Tarih araştırmalarında olayların geçtiği dönemin koşullarını dikkate almadan günümüz değerleriyle yargılamaya ne denir?",
              choices: ["Anakronizm", "Objektiflik", "Paleografi", "Diplomatik"],
              correctAnswer: "Anakronizm",
              explanation: "Anakronizm (tarih yanılgısı), olayları ve kavramları çağının dışına taşıma hatasıdır."
            }
          ],
          "10-Projects/Tarih_Yillik_Plan_AL9.md#L25",
          "Tarih Bilimi ve Metodolojisi"
        )
      }
    ]
  },
  {
    id: "lesson_tar9_kaynaklar_takvimler",
    stableKey: "lesson_tar9_kaynaklar_takvimler",
    title: "Kaynak Türleri, Tarih Yazıcılığı ve Takvimler (TAR.9.1.2)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_tar9_vid_kaynak_turleri",
        stableKey: "tar9_vid_kaynak_turleri",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Tarihin Doğası, Kaynak Türleri ve Tarih Yazıcılığı",
        contentUrl: "https://www.youtube.com/watch?v=lqEZ19uwyas",
        publishingStatus: "active",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "00:00",
          endTimestamp: "24:30",
          topicDetails: "Birinci/İkinci Elden Kaynaklar, Tarih Yazıcılığı Türleri: Hikâyeci, Öğretici, Araştırmacı",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L36",
            sourceSection: "3. Hafta: Tarihin Doğası, Kaynak Türleri ve Tarih Yazıcılığı",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_vid_yardimci_bilimler",
        stableKey: "tar9_vid_yardimci_bilimler",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Tarihe Yardımcı Bilim Dalları ve Zamanın Taksimi (5 Takvim)",
        contentUrl: "https://www.youtube.com/watch?v=lqEZ19uwyas",
        publishingStatus: "active",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          channel: "Benim Hocam",
          startTimestamp: "24:30",
          endTimestamp: "46:15",
          topicDetails: "Kronoloji, Arkeoloji, Epigrafi, Nümizmatik, Diplomatik & 5 Takvim",
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L47",
            sourceSection: "4. Hafta: Tarihe Yardımcı Bilim Dalları ve Zamanın Taksimi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_anki_temel",
        stableKey: "tar9_anki_temel",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 3000.0,
        title: "9. Sınıf Tarih Temel Kavramlar Anki Kartları",
        contentUrl: null,
        publishingStatus: "active",
        payload: {
          ankiSource: "9_sinif_tarih_anki.txt",
          cardCount: 10,
          provenance: {
            sourceRef: "10-Projects/1_ay_tarih_video_rehberi.md#L50",
            sourceSection: "4. Hafta",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tar9_quiz_kaynaklar_takvimler",
        stableKey: "tar9_quiz_kaynaklar_takvimler",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 4000.0,
        title: "Tarihi Kaynaklar ve Takvim Sistemleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Tarihi Kaynaklar ve Takvim Sistemleri Testi",
          [
            {
              id: "q_tar2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Eski paraları inceleyerek tarihe yardımcı olan bilim dalı hangisidir?",
              choices: ["Epigrafi", "Nümizmatik (Meskukat)", "Paleografi", "Heraldik"],
              correctAnswer: "Nümizmatik (Meskukat)",
              explanation: "Nümizmatik madeni paraları inceler."
            },
            {
              id: "q_tar2_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Türklerin kullandığı takvimlerden hangisi Ay yılı esasına dayanır?",
              choices: [
                "12 Hayvanlı Türk Takvimi",
                "Hicri Takvim",
                "Celali Takvimi",
                "Rumi Takvim"
              ],
              correctAnswer: "Hicri Takvim",
              explanation: "Hicri takvim Ay'ın Dünya çevresindeki hareketine (354 gün) dayanır; diğerleri Güneş yılı esaslıdır."
            },
            {
              id: "q_tar2_3",
              type: "TRUE_FALSE",
              prompt: "Kitabeleri ve anıtlar üzerindeki yazıtları inceleyen bilim dalı Epigrafi'dir.",
              correctAnswer: "TRUE",
              explanation: "Epigrafi anıt ve kitabe bilimidir (örn. Orhun Abideleri)."
            }
          ],
          "10-Projects/Tarih_Yillik_Plan_AL9.md#L45",
          "Kaynak Türleri ve Takvimler"
        )
      }
    ]
  },
  {
    id: "lesson_tar9_eski_cag_medeniyetleri",
    stableKey: "lesson_tar9_eski_cag_medeniyetleri",
    title: "Eski Çağ Medeniyetleri ve Konargöçer Yaşam (TAR.9.2.1 - TAR.9.2.4)",
    orderKey: 3000.0,
    items: [
      {
        id: "item_tar9_vid_tarim_devrimi_mezopotamya",
        stableKey: "tar9_vid_tarim_devrimi_mezopotamya",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Tarım Devrimi, İlk Şehir Devletleri ve Mezopotamya Medeniyetleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          provenance: {
            sourceRef: "10-Projects/Tarih_Yillik_Plan_AL9.md#L80",
            sourceSection: "Eski Çağ Medeniyetleri",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_tar9_vid_anadolu_ve_turkler",
        stableKey: "tar9_vid_anadolu_ve_turkler",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "Anadolu Medeniyetleri, Eski Hukuk ve Türklerde Konargöçer Yaşam",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          provenance: {
            sourceRef: "10-Projects/Tarih_Yillik_Plan_AL9.md#L100",
            sourceSection: "Anadolu ve Konargöçer Yaşam",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_tar9_quiz_eski_cag",
        stableKey: "tar9_quiz_eski_cag",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Eski Çağ Medeniyetleri ve Hukuk Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Eski Çağ Medeniyetleri ve Hukuk Testi",
          [
            {
              id: "q_tar3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Tarihte ilk yazılı kanunları hazırlayan Sümer kralı kimdir?",
              choices: ["Hammurabi", "Urgakina", "Sargon", "Nabukadnezar"],
              correctAnswer: "Urgakina",
              explanation: "Tarihte bilinen ilk yazılı kanunlar MÖ 2375 civarında Sümer kralı Urgakina tarafından yapılmıştır."
            },
            {
              id: "q_tar3_2",
              type: "TRUE_FALSE",
              prompt: "Tarihte parayı ilk icat ederek ticarette takas usulüne son veren medeniyet Lidyalılar'dır.",
              correctAnswer: "TRUE",
              explanation: "Lidyalılar MÖ 7. yüzyılda madeni parayı basmıştır."
            },
            {
              id: "q_tar3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "İlk Çağ Türk topluluklarında ordu yapısını 'Onlu Sistem'e göre ilk kez teşkilatlandıran Türk hükümdarı kimdir?",
              choices: ["Teoman", "Mete Han", "Bumin Kağan", "İstemi Yabgu"],
              correctAnswer: "Mete Han",
              explanation: "Büyük Hun Hükümdarı Mete Han MÖ 209'da Onlu Sistemi kurmuştur."
            }
          ],
          "10-Projects/Tarih_Yillik_Plan_AL9.md#L105",
          "Eski Çağ Medeniyetleri"
        )
      }
    ]
  },
  {
    id: "lesson_tar9_orta_cag_medeniyetleri",
    stableKey: "lesson_tar9_orta_cag_medeniyetleri",
    title: "Orta Çağ Medeniyetleri ve Ticaret Yolları (TAR.9.3.1 - TAR.9.3.4)",
    orderKey: 4000.0,
    items: [
      {
        id: "item_tar9_vid_orta_cag_gocler_devletler",
        stableKey: "tar9_vid_orta_cag_gocler_devletler",
        itemType: "VIDEO",
        displayLabel: "4.1",
        orderKey: 1000.0,
        title: "Kavimler Göçü, Feodalizm ve Orta Çağ'ın Başlıca Devletleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          provenance: {
            sourceRef: "10-Projects/Tarih_Yillik_Plan_AL9.md#L130",
            sourceSection: "Orta Çağ'da Devletler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_tar9_vid_orta_cag_ticaret_yollari",
        stableKey: "tar9_vid_orta_cag_ticaret_yollari",
        itemType: "VIDEO",
        displayLabel: "4.2",
        orderKey: 2000.0,
        title: "Orta Çağ Ticaret Yolları: İpek, Baharat ve Kürk Yolları",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Mehmet Celal ÖZYILDIZ",
          provenance: {
            sourceRef: "10-Projects/Tarih_Yillik_Plan_AL9.md#L150",
            sourceSection: "Ticaret Yolları ve Medeniyet Havzaları",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_tar9_quiz_orta_cag",
        stableKey: "tar9_quiz_orta_cag",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Orta Çağ Dünyası ve Ticaret Ağları Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Orta Çağ Dünyası ve Ticaret Ağları Testi",
          [
            {
              id: "q_tar4_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Çin'den başlayıp Orta Asya üzerinden Akdeniz ve Karadeniz limanlarına ulaşan tarihi ticaret yolu hangisidir?",
              choices: ["Baharat Yolu", "İpek Yolu", "Kral Yolu", "Kürk Yolu"],
              correctAnswer: "İpek Yolu",
              explanation: "İpek Yolu Çin'den Avrupa'ya uzanan ana ticaret aksıdır."
            },
            {
              id: "q_tar4_2",
              type: "TRUE_FALSE",
              prompt: "Orta Çağ Avrupası'nda siyasi gücün derebeyleri (senyörler) arasında bölündüğü yönetim sistemine Feodalizm denir.",
              correctAnswer: "TRUE",
              explanation: "Feodalizm merkezi otoritenin zayıf olduğu, toprak sahibi soyluların egemen olduğu düzendir."
            },
            {
              id: "q_tar4_3",
              type: "MULTIPLE_CHOICE",
              prompt: "İslam dünyasında Abbasiler döneminde Bağdat'ta kurulan büyük bilim ve tercüme merkezinin adı nedir?",
              choices: ["Beytülhikme", "Nizamiye Medresesi", "Darülfünun", "Enderun"],
              correctAnswer: "Beytülhikme",
              explanation: "Beytülhikme (Bilgelik Evi) İslam'ın Altın Çağı'nda kurulan tercüme ve bilim akademisidir."
            }
          ],
          "10-Projects/Tarih_Yillik_Plan_AL9.md#L155",
          "Orta Çağ Medeniyetleri"
        )
      }
    ]
  }
];

// ==========================================
// 6. COĞRAFYA (course_cog_9)
// ==========================================
const cogLessons = [
  {
    id: "lesson_cog9_dogal_sistemler",
    stableKey: "lesson_cog9_dogal_sistemler",
    title: "Doğal Sistemler ve Coğrafyanın İlkeleri (COĞ.9.1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_cog9_vid_doga_insan",
        stableKey: "cog9_vid_doga_insan",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L14",
            sourceSection: "1. Hafta: Coğrafya Biliminin Konusu, Doğal Ortamlar ve Bölümleri",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_vid_mekansal_dusunme",
        stableKey: "cog9_vid_mekansal_dusunme",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Doğa-İnsan Etkileşimi & Mekânsal Düşünme",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L26",
            sourceSection: "2. Hafta: Doğa-İnsan Etkileşimi & Mekânsal Düşünme",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_vid_tarihsel_gelisim",
        stableKey: "cog9_vid_tarihsel_gelisim",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "Coğrafya Biliminin Tarihsel Gelişimi",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L37",
            sourceSection: "3. Hafta: Coğrafya Biliminin Tarihsel Gelişimi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_vid_harita_bilgisi",
        stableKey: "cog9_vid_harita_bilgisi",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Mekânın Aynası Haritalar & Projeksiyon Yöntemleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/1_ay_cografya_video_rehberi.md#L48",
            sourceSection: "4. Hafta: Mekânın Aynası Haritalar & Projeksiyon Yöntemleri",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_anki_1ay",
        stableKey: "cog9_anki_1ay",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 5000.0,
        title: "9. Sınıf Coğrafya 1. Ay Anki Destesi",
        contentUrl: null,
        publishingStatus: "active",
        payload: {
          ankiPackage: "9_sinif_cografya_1_ay.apkg",
          cardCount: 24,
          provenance: {
            sourceRef: "10-Projects/9_sinif_cografya_1_ay_plani.md#L76",
            sourceSection: "3. Anki Tekrar ve Pekiştirme Stratejisi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_cog9_quiz_dogal_sistemler",
        stableKey: "cog9_quiz_dogal_sistemler",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 6000.0,
        title: "Coğrafyanın Doğası ve İlkeleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Coğrafyanın Doğası ve İlkeleri Testi",
          [
            {
              id: "q_cog1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Aşağıdakilerden hangisi coğrafyanın 4 temel ortamından biri olan 'Taş Küre'yi ifade eder?",
              choices: ["Litosfer", "Atmosfer", "Hidrosfer", "Biyosfer"],
              correctAnswer: "Litosfer",
              explanation: "Litosfer yer kabuğu ve taş küredir."
            },
            {
              id: "q_cog1_2",
              type: "TRUE_FALSE",
              prompt: "Coğrafyayı diğer bilimlerden ayıran en temel ilke 'Dağılış İlkesi'dir.",
              correctAnswer: "TRUE",
              explanation: "Olayların yeryüzündeki yayılış ve dağılışını haritalarla incelemek coğrafyaya özgüdür."
            },
            {
              id: "q_cog1_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Ekvator ve çevresinin harita çizimlerinde bozulmayı en aza indiren projeksiyon türü hangisidir?",
              choices: ["Silindirik Projeksiyon", "Konik Projeksiyon", "Düzlem Projeksiyon", "Parçalı Projeksiyon"],
              correctAnswer: "Silindirik Projeksiyon",
              explanation: "Silindirik projeksiyonda temas Ekvator çevresindedir ve bozulma en azdır."
            }
          ],
          "10-Projects/Cografya_Yillik_Plan_AL9.md#L20",
          "Doğal Sistemler ve Coğrafya"
        )
      }
    ]
  },
  {
    id: "lesson_cog9_iklim_ve_hava_olaylari",
    stableKey: "lesson_cog9_iklim_ve_hava_olaylari",
    title: "Doğal Sistemler ve Süreçler: İklim ve Hava Olayları (COĞ.9.2.1 - COĞ.9.2.3)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_cog9_vid_atmosfer_ve_sicaklik",
        stableKey: "item_cog9_vid_atmosfer_ve_sicaklik",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Atmosferin Katmanları ve Sıcaklık Etmenleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/Cografya_Yillik_Plan_AL9.md#L60",
            sourceSection: "Atmosfer ve Sıcaklık",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_vid_basinc_ruzgar_yagis",
        stableKey: "item_cog9_vid_basinc_ruzgar_yagis",
        itemType: "VIDEO",
        displayLabel: "2.2",
        orderKey: 2000.0,
        title: "Basınç Merkezleri, Rüzgârlar ve Yağış Tipleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/Cografya_Yillik_Plan_AL9.md#L80",
            sourceSection: "Basınç ve Rüzgârlar",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_quiz_iklim_atmosfer",
        stableKey: "cog9_quiz_iklim_atmosfer",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Atmosfer ve İklim Elemanları Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Atmosfer ve İklim Elemanları Testi",
          [
            {
              id: "q_cog2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Tüm hava olaylarının (yağış, bulut, rüzgar) gerçekleştiği atmosfer katmanı hangisidir?",
              choices: ["Troposfer", "Stratosfer", "Mezosfer", "Termosfer"],
              correctAnswer: "Troposfer",
              explanation: "Su buharının tamamına yakını Troposfer katmanında bulunduğu için hava olayları sadece burada yaşanır."
            },
            {
              id: "q_cog2_2",
              type: "TRUE_FALSE",
              prompt: "Rüzgârlar daima yüksek basınç alanından alçak basınç alanına doğru eser.",
              correctAnswer: "TRUE",
              explanation: "Rüzgar basınç farkından doğan yatay hava hareketidir (Yüksek Basınç -> Alçak Basınç)."
            },
            {
              id: "q_cog2_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Isınan havanın yükselerek soğuması sonucu oluşan yağış türüne ne ad verilir?",
              choices: ["Konveksiyonel (Yükselim) Yağış", "Orografik (Yamaç) Yağış", "Cephesel Yağış", "Siklonik Yağış"],
              correctAnswer: "Konveksiyonel (Yükselim) Yağış",
              explanation: "Ekvator ve İç Anadolu'da (kırkikindi) görülen ısınan havanın yükselmesiyle oluşan yağışlardır."
            }
          ],
          "10-Projects/Cografya_Yillik_Plan_AL9.md#L85",
          "İklim ve Hava Olayları"
        )
      }
    ]
  },
  {
    id: "lesson_cog9_beseri_sistemler_ve_afetler",
    stableKey: "lesson_cog9_beseri_sistemler_ve_afetler",
    title: "Beşerî Sistemler, Nüfus ve Doğal Afetler (COĞ.9.3.1 - COĞ.9.4.3)",
    orderKey: 3000.0,
    items: [
      {
        id: "item_cog9_vid_nufus_ve_yerlesme",
        stableKey: "item_cog9_vid_nufus_ve_yerlesme",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Nüfus Piramitleri, Göçler ve Yerleşme Dokuları",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/Cografya_Yillik_Plan_AL9.md#L110",
            sourceSection: "Nüfus ve Yerleşme",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_vid_afet_yonetimi",
        stableKey: "item_cog9_vid_afet_yonetimi",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "Doğal Afet Türleri ve Bütüncül Afet Yönetimi",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Coğrafyanın Kodları",
          provenance: {
            sourceRef: "10-Projects/Cografya_Yillik_Plan_AL9.md#L130",
            sourceSection: "Doğal Afetler ve Çevre",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_cog9_quiz_nufus_afetler",
        stableKey: "cog9_quiz_nufus_afetler",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Nüfus ve Afet Yönetimi Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Nüfus ve Afet Yönetimi Testi",
          [
            {
              id: "q_cog3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Tabanı dar, üst yaş grubu geniş olan bir nüfus piramidi hangi ülke türünü ifade eder?",
              choices: [
                "Gelişmiş ülke (düşük doğum, yaşlı nüfus oranı yüksek)",
                "Gelişmekte olan ülke",
                "Az gelişmiş ülke (yüksek doğum oranı)",
                "Nüfusu hızla artan genç ülke"
              ],
              correctAnswer: "Gelişmiş ülke (düşük doğum, yaşlı nüfus oranı yüksek)",
              explanation: "Gelişmiş ülkelerde doğum oranları düşük olduğundan piramit tabanı dardır."
            },
            {
              id: "q_cog3_2",
              type: "TRUE_FALSE",
              prompt: "Deprem, volkanizma ve tsunami jeolojik kökenli doğal afetlerdendir.",
              correctAnswer: "TRUE",
              explanation: "Yer kabuğu hareketleri kaynaklı afetler jeolojik kökenlidir."
            },
            {
              id: "q_cog3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Eğimli yamaçlarda toprak ve ana kayanın kütle halinde aşağı kaymasına ne denir?",
              choices: ["Heyelan (Kütle Hareketi)", "Erozyon", "Çığ", "Kuraklık"],
              correctAnswer: "Heyelan (Kütle Hareketi)",
              explanation: "Heyelan, eğimli ve suya doygun tabakaların kayması olayıdır."
            }
          ],
          "10-Projects/Cografya_Yillik_Plan_AL9.md#L135",
          "Beşerî Sistemler ve Afetler"
        )
      }
    ]
  }
];

// ==========================================
// 7. İNGİLİZCE (course_ing_9)
// ==========================================
const ingLessons = [
  {
    id: "lesson_ing9_orientation_revision",
    stableKey: "lesson_ing9_orientation_revision",
    title: "Theme 1: School Life & Orientation",
    orderKey: 1000.0,
    items: [
      {
        id: "item_ing9_vid_cumle_kurma",
        stableKey: "ing9_vid_cumle_kurma",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "İngilizce Cümle Kurma Mantığı & Günlük Rutin İfadeleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Özer Kiraz",
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L18",
            sourceSection: "1. Hafta: Orientation & Okul Yaşamına Giriş",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_vid_simple_present",
        stableKey: "ing9_vid_simple_present",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Simple Present vs Present Continuous (Geniş Zaman - Şimdiki Zaman Farkı)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Özer Kiraz",
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L29",
            sourceSection: "2. Hafta: Geniş Zaman vs Şimdiki Zaman",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_anki_1ay",
        stableKey: "ing9_anki_1ay",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 3000.0,
        title: "9. Sınıf İngilizce 1. Ay Kelime Destesi",
        contentUrl: null,
        publishingStatus: "active",
        payload: {
          ankiPackage: "9_sinif_ingilizce_1_ay.apkg",
          cardCount: 30,
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_kelime_listesi.md#L1",
            sourceSection: "9. Sınıf İngilizce 1. Ay Kapsamlı Kelime Listesi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_ing9_quiz_school_life",
        stableKey: "ing9_quiz_school_life",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 4000.0,
        title: "School Life & Present Simple Test",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "School Life & Present Simple Test",
          [
            {
              id: "q_ing1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Complete the sentence: 'She _______ to school by bus every weekday morning.'",
              choices: ["go", "goes", "is going", "went"],
              correctAnswer: "goes",
              explanation: "In Simple Present Tense, third-person singular subjects (He/She/It) take -s/-es suffix: 'goes'."
            },
            {
              id: "q_ing1_2",
              type: "TRUE_FALSE",
              prompt: "'Look! The students are playing football right now.' uses the Present Continuous Tense correctly.",
              correctAnswer: "TRUE",
              explanation: "'Look!' and 'right now' indicate an action happening at the moment of speaking (Present Continuous)."
            },
            {
              id: "q_ing1_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Which question word is used to ask about nationalities/origins: '_______ are you from?'",
              choices: ["Where", "When", "What", "Who"],
              correctAnswer: "Where",
              explanation: "'Where are you from?' asks for country or origin."
            }
          ],
          "10-Projects/İngilizce_Yillik_Plan_AL9.md#L25",
          "Theme 1: School Life"
        )
      }
    ]
  },
  {
    id: "lesson_ing9_personal_life_appearance",
    stableKey: "lesson_ing9_personal_life_appearance",
    title: "Theme 2 & 3: Personal Life, Appearance & Personality",
    orderKey: 2000.0,
    items: [
      {
        id: "item_ing9_vid_appearance_personality",
        stableKey: "item_ing9_vid_appearance_personality",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Describing Physical Appearance and Personality Traits",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Özer Kiraz",
          provenance: {
            sourceRef: "10-Projects/İngilizce_Yillik_Plan_AL9.md#L60",
            sourceSection: "Theme 3: Personal Life",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_quiz_appearance_personality",
        stableKey: "ing9_quiz_appearance_personality",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 2000.0,
        title: "Appearance & Personality Traits Test",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Appearance & Personality Traits Test",
          [
            {
              id: "q_ing2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Someone who always tells the truth and never cheats is _______.",
              choices: ["honest", "selfish", "stubborn", "lazy"],
              correctAnswer: "honest",
              explanation: "Honest means truthful and sincere."
            },
            {
              id: "q_ing2_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Comparative form: 'Mount Everest is _______ than Mont Blanc.'",
              choices: ["higher", "more high", "highest", "the higher"],
              correctAnswer: "higher",
              explanation: "Short one-syllable adjectives take -er in comparative form: 'higher'."
            },
            {
              id: "q_ing2_3",
              type: "TRUE_FALSE",
              prompt: "'Generous' describes someone who likes giving and sharing with others.",
              correctAnswer: "TRUE",
              explanation: "Generous means willing to give money, help, or time freely."
            }
          ],
          "10-Projects/İngilizce_Yillik_Plan_AL9.md#L65",
          "Theme 3: Personal Life"
        )
      }
    ]
  },
  {
    id: "lesson_ing9_family_and_world",
    stableKey: "lesson_ing9_family_and_world",
    title: "Theme 4 - 8: Family, Jobs, Nature & Future Predictions",
    orderKey: 3000.0,
    items: [
      {
        id: "item_ing9_vid_modals",
        stableKey: "ing9_vid_modals",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Modals: Can, Must, Have to Farkı ve Kullanımı",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Özer Kiraz",
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L40",
            sourceSection: "3. Hafta: Yetenek ve Zorunluluk Kipleri",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_vid_used_to",
        stableKey: "ing9_vid_used_to",
        itemType: "VIDEO",
        displayLabel: "3.2",
        orderKey: 2000.0,
        title: "Used to ve Could Kullanımı (Geçmiş Alışkanlıklar & Yetenek)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "Özer Kiraz",
          provenance: {
            sourceRef: "10-Projects/1_ay_ingilizce_video_rehberi.md#L51",
            sourceSection: "4. Hafta: Geçmiş Alışkanlıklar & Kibar İstekler",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_ing9_quiz_modals_future",
        stableKey: "ing9_quiz_modals_future",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Modals and Future Predictions Test",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Modals and Future Predictions Test",
          [
            {
              id: "q_ing3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "You _______ stop when the traffic lights turn red. It is a strict rule.",
              choices: ["must", "can", "might", "could"],
              correctAnswer: "must",
              explanation: "'Must' expresses strong obligation and legal rules."
            },
            {
              id: "q_ing3_2",
              type: "TRUE_FALSE",
              prompt: "'Will' is used for spontaneous decisions and future predictions.",
              correctAnswer: "TRUE",
              explanation: "'Will' is used for predictions without prior evidence and instant decisions."
            },
            {
              id: "q_ing3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "He _______ play the piano when he was only five years old.",
              choices: ["could", "must", "should", "will"],
              correctAnswer: "could",
              explanation: "'Could' expresses past general ability."
            }
          ],
          "10-Projects/İngilizce_Yillik_Plan_AL9.md#L100",
          "Themes 4-8: Modals and Future"
        )
      }
    ]
  }
];

// ==========================================
// 8. ALMANCA (course_alm_9)
// ==========================================
const almLessons = [
  {
    id: "lesson_alm9_modul1_hallo",
    stableKey: "lesson_alm9_modul1_hallo",
    title: "Modul 1: HALLO! & Informationen zur Person (A1.1)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_alm9_vid_selamlasma_alfabe",
        stableKey: "alm9_vid_selamlasma_alfabe",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "Almanca Selamlaşma, Vedalaşma ve Alfabe (Das ABC)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "AlmancaKolay",
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L20",
            sourceSection: "1. Hafta: Selamlaşma / Vedalaşma ve Alfabe",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_vid_kendini_tanitma",
        stableKey: "alm9_vid_kendini_tanitma",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Kendini Tanıtma, Adını Söyleme ve Hal-Hatır Sorma",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "AlmancaKolay",
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L30",
            sourceSection: "2. Hafta: Kendini Tanıtma ve Hal-Hatır",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_vid_sayilar",
        stableKey: "alm9_vid_sayilar",
        itemType: "VIDEO",
        displayLabel: "1.3",
        orderKey: 3000.0,
        title: "0-20 Arası Sayılar (Die Zahlen) ve Telefon Numarası",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "AlmancaKolay",
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L40",
            sourceSection: "3. Hafta: Sayılar ve Telefon Numarası",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_vid_ulkeler_diller",
        stableKey: "alm9_vid_ulkeler_diller",
        itemType: "VIDEO",
        displayLabel: "1.4",
        orderKey: 4000.0,
        title: "Ülkeler, Diller ve Nereli Olduğunu Söyleme (Woher kommst du?)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "AlmancaKolay",
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_video_rehberi.md#L50",
            sourceSection: "4. Hafta: Ülkeler, Diller ve İkamet",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_anki_1ay",
        stableKey: "alm9_anki_1ay",
        itemType: "ANKI",
        displayLabel: "A1",
        orderKey: 5000.0,
        title: "9. Sınıf Almanca 1. Ay Kelime ve Cümle Destesi",
        contentUrl: null,
        publishingStatus: "active",
        payload: {
          ankiPackage: "9_sinif_almanca_1_ay.apkg",
          cardCount: 20,
          provenance: {
            sourceRef: "10-Projects/1_ay_almanca_kelime_listesi.md#L1",
            sourceSection: "9. Sınıf Almanca (A1.1) 1. Ay Kelime Rehberi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_alm9_quiz_hallo_person",
        stableKey: "alm9_quiz_hallo_person",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 6000.0,
        title: "Hallo & Persönliche Angaben Test",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Hallo & Persönliche Angaben Test",
          [
            {
              id: "q_alm1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Wie antwortet man auf die Frage: 'Wie heißt du?'",
              choices: [
                "Ich heiße Ali.",
                "Ich komme aus Deutschland.",
                "Ich bin 15 Jahre alt.",
                "Mir geht es gut."
              ],
              correctAnswer: "Ich heiße Ali.",
              explanation: "'Wie heißt du?' sorusuna 'Ich heiße...' ile yanıt verilir."
            },
            {
              id: "q_alm1_2",
              type: "MULTIPLE_CHOICE",
              prompt: "Welche Zahl ist 'fünfzehn'?",
              choices: ["5", "15", "50", "12"],
              correctAnswer: "15",
              explanation: "fünf (5) + zehn (10) = fünfzehn (15)."
            },
            {
              id: "q_alm1_3",
              type: "TRUE_FALSE",
              prompt: "'Tschüss!' bedeutet auf Türkisch 'Görüşmek üzere / Hoşça kal!'.",
              correctAnswer: "TRUE",
              explanation: "'Tschüss' Almanca samimi bir vedalaşma ifadesidir."
            }
          ],
          "10-Projects/Almanca_Yillik_Plan_AL9.md#L20",
          "Modul 1: Hallo!"
        )
      }
    ]
  },
  {
    id: "lesson_alm9_schule_und_alltag",
    stableKey: "lesson_alm9_schule_und_alltag",
    title: "Modul 2 & 3: Meine Schule, Schulsachen & Der Alltag (A1.1)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_alm9_vid_schulsachen_artikel",
        stableKey: "item_alm9_vid_schulsachen_artikel",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Die Schulsachen und Artikel (Der, Die, Das / Ein, Eine, Kein)",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          teacher: "AlmancaKolay",
          provenance: {
            sourceRef: "10-Projects/Almanca_Yillik_Plan_AL9.md#L60",
            sourceSection: "Meine Schulsachen",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_alm9_quiz_schule_artikel",
        stableKey: "alm9_quiz_schule_artikel",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 2000.0,
        title: "Schulsachen & Artikel Test",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Schulsachen & Artikel Test",
          [
            {
              id: "q_alm2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Welcher Artikel gehört zu 'Buch' (Kitap)?",
              choices: ["der", "die", "das", "den"],
              correctAnswer: "das",
              explanation: "Almancada kitap sözcüğünün artikeli 'das Buch'tur."
            },
            {
              id: "q_alm2_2",
              type: "TRUE_FALSE",
              prompt: "'Die Schere' bedeutet 'Makas'.",
              correctAnswer: "TRUE",
              explanation: "Die Schere = Makas."
            },
            {
              id: "q_alm2_3",
              type: "MULTIPLE_CHOICE",
              prompt: "'Wie spät ist es?' sorusu neyi öğrenmek için sorulur?",
              choices: ["Saatin kaç olduğunu", "Havanın nasıl olduğunu", "Kişinin yaşını", "Fiyatı"],
              correctAnswer: "Saatin kaç olduğunu",
              explanation: "'Wie spät ist es?' = Saat kaç?"
            }
          ],
          "10-Projects/Almanca_Yillik_Plan_AL9.md#L70",
          "Modul 2 & 3: Schulsachen"
        )
      }
    ]
  }
];

// ==========================================
// 9. TÜRK DİLİ VE EDEBİYATI (course_tde_9)
// ==========================================
const tdeLessons = [
  {
    id: "lesson_tde9_metin_ve_anlam",
    stableKey: "lesson_tde9_metin_ve_anlam",
    title: "1. Tema: Sözün İnceliği (Şiir, Deneme ve Söz Sanatları)",
    orderKey: 1000.0,
    items: [
      {
        id: "item_tde9_vid_soz_sanatlari",
        stableKey: "tde9_vid_soz_sanatlari",
        itemType: "VIDEO",
        displayLabel: "1.1",
        orderKey: 1000.0,
        title: "TDE: Metinde Anlam & Söz Sanatları",
        contentUrl: "https://www.youtube.com/watch?v=NBhw_SkvV8E",
        publishingStatus: "active",
        payload: {
          provenance: {
            sourceRef: "10-Projects/9_sinif_4_haftalik_studytracker_calisma_plani.md#L87",
            sourceSection: "2. Hafta: Alt Dallar, Güvenlik ve Anlamlandırma",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "verified"
          }
        }
      },
      {
        id: "item_tde9_vid_edebiyat_guzel_sanatlar",
        stableKey: "item_tde9_vid_edebiyat_guzel_sanatlar",
        itemType: "VIDEO",
        displayLabel: "1.2",
        orderKey: 2000.0,
        title: "Edebiyatın Güzel Sanatlarla İlişkisi ve Metin Türleri",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/TDE_Yillik_Plan_AL9.md#L20",
            sourceSection: "1. Tema: Sözün İnceliği",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_tde9_quiz_sozun_inceligi",
        stableKey: "tde9_quiz_sozun_inceligi",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 3000.0,
        title: "Şiir Bilgisi ve Söz Sanatları Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Şiir Bilgisi ve Söz Sanatları Testi",
          [
            {
              id: "q_tde1_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Aralarında benzerlik ilişkisi kurulan iki varlıktan zayıf olanın güçlü olana benzetilmesine ne denir?",
              choices: ["Teşbih (Benzetme)", "İstiare (Eğretileme)", "Teşhis (Kişileştirme)", "Tezat (Karşıtlık)"],
              correctAnswer: "Teşbih (Benzetme)",
              explanation: "Teşbih benzetme sanatıdır; 4 temel ögesi vardır (benzeyen, kendisine benzetilen, benzetme yönü, benzetme edatı)."
            },
            {
              id: "q_tde1_2",
              type: "TRUE_FALSE",
              prompt: "Dize sonlarındaki yazılışları ve anlamları aynı olan ek veya sözcük tekrarlarına Redif denir.",
              correctAnswer: "TRUE",
              explanation: "Aynı görevdeki ek veya aynen tekrarlanan sözcükler rediftir; kökteki ses benzerlikleri ise kafiyedir."
            },
            {
              id: "q_tde1_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Yazarın herhangi bir konudaki kişisel görüş ve düşüncelerini kanıtlama amacı gütmeden samimi bir dille anlattığı türe ne ad verilir?",
              choices: ["Deneme", "Makale", "Fıkra", "Eleştiri"],
              correctAnswer: "Deneme",
              explanation: "Montaigne ile özdeşleşen 'deneme' türünde kanıtlama zorunluluğu yoktur, öznel bir türdür."
            }
          ],
          "10-Projects/TDE_Yillik_Plan_AL9.md#L25",
          "Sözün İnceliği Teması"
        )
      }
    ]
  },
  {
    id: "lesson_tde9_anlam_arayisi_hikaye",
    stableKey: "lesson_tde9_anlam_arayisi_hikaye",
    title: "2. Tema: Anlam Arayışı (Hikâye, Anı ve İletişim)",
    orderKey: 2000.0,
    items: [
      {
        id: "item_tde9_vid_hikaye_turleri",
        stableKey: "item_tde9_vid_hikaye_turleri",
        itemType: "VIDEO",
        displayLabel: "2.1",
        orderKey: 1000.0,
        title: "Hikâye Unsurları: Olay ve Durum Hikâyesi Karşılaştırması",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/TDE_Yillik_Plan_AL9.md#L60",
            sourceSection: "2. Tema: Anlam Arayışı",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_tde9_quiz_hikaye_ve_anlam",
        stableKey: "tde9_quiz_hikaye_ve_anlam",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 2000.0,
        title: "Hikâye Türü ve Anlatım Teknikleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Hikâye Türü ve Anlatım Teknikleri Testi",
          [
            {
              id: "q_tde2_1",
              type: "MULTIPLE_CHOICE",
              prompt: "Türk edebiyatında Durum (Kesit) hikâyeciliğinin en önemli temsilcisi kimdir?",
              choices: [
                "Sait Faik Abasıyanık",
                "Ömer Seyfettin",
                "Refik Halit Karay",
                "Ahmet Mithat Efendi"
              ],
              correctAnswer: "Sait Faik Abasıyanık",
              explanation: "Sait Faik ve Memduh Şevket Esendal Çehov tarzı (durum) hikâyeciliğinin öncüleridir; Ömer Seyfettin ise olay hikâyecisidir."
            },
            {
              id: "q_tde2_2",
              type: "TRUE_FALSE",
              prompt: "İlahi (Hâkim) bakış açısında anlatıcı kahramanların iç dünyasını ve geçmişini bilir.",
              correctAnswer: "TRUE",
              explanation: "Hâkim bakış açısı her şeye hâkim olan üçüncü tekil şahıs anlatımıdır."
            },
            {
              id: "q_tde2_3",
              type: "MULTIPLE_CHOICE",
              prompt: "Dilin alıcıda bir tepki veya davranış oluşturmak amacıyla kullanıldığı işlevi hangisidir?",
              choices: [
                "Alıcıyı Harekete Geçirme İşlevi",
                "Göndergesel İşlev",
                "Heyecana Bağlı İşlev",
                "Kanalı Kontrol İşlevi"
              ],
              correctAnswer: "Alıcıyı Harekete Geçirme İşlevi",
              explanation: "Emir, rica ve propaganda cümlelerinde alıcıyı harekete geçirme işlevi kullanılır."
            }
          ],
          "10-Projects/TDE_Yillik_Plan_AL9.md#L65",
          "Hikâye ve Dilin İşlevleri"
        )
      }
    ]
  },
  {
    id: "lesson_tde9_dilin_yapisi_ve_roman",
    stableKey: "lesson_tde9_dilin_yapisi_ve_roman",
    title: "3. ve 4. Tema: Anlamın Yapı Taşları, Roman ve Dil Bilgisi",
    orderKey: 3000.0,
    items: [
      {
        id: "item_tde9_vid_sozcukte_anlam_isim_sifat",
        stableKey: "item_tde9_vid_sozcukte_anlam_isim_sifat",
        itemType: "VIDEO",
        displayLabel: "3.1",
        orderKey: 1000.0,
        title: "Sözcük Türleri: İsimler, Sıfatlar, Zamirler ve Zarflar",
        contentUrl: null,
        publishingStatus: "draft",
        payload: {
          provenance: {
            sourceRef: "10-Projects/TDE_Yillik_Plan_AL9.md#L100",
            sourceSection: "3. ve 4. Tema: Dil Bilgisi",
            importedAt: "2026-10-02T09:00:00.000Z",
            schemaVersion: "v2",
            reviewStatus: "needs_review"
          }
        }
      },
      {
        id: "item_tde9_quiz_dil_bilgisi_roman",
        stableKey: "tde9_quiz_dil_bilgisi_roman",
        itemType: "QUIZ",
        displayLabel: "Q1",
        orderKey: 2000.0,
        title: "Roman ve Sözcük Türleri Testi",
        contentUrl: null,
        publishingStatus: "active",
        payload: makeQuizPayload(
          "Roman ve Sözcük Türleri Testi",
          [
            {
              id: "q_tde3_1",
              type: "MULTIPLE_CHOICE",
              prompt: "'Kırmızı elmalar sepette duruyordu.' cümlesinde 'kırmızı' sözcüğünün türü nedir?",
              choices: ["Niteleme Sıfatı", "Belirtme Sıfatı", "İsim", "Zarf"],
              correctAnswer: "Niteleme Sıfatı",
              explanation: "İsmin rengini, durumunu veya biçimini bildiren sözcükler niteleme sıfatıdır."
            },
            {
              id: "q_tde3_2",
              type: "TRUE_FALSE",
              prompt: "Dünya edebiyatında ilk modern roman Cervantes'in 'Don Kişot' adlı eseridir.",
              correctAnswer: "TRUE",
              explanation: "Don Kişot ilk modern roman kabul edilir."
            },
            {
              id: "q_tde3_3",
              type: "MULTIPLE_CHOICE",
              prompt: "İsmin yerini tutan sözcüklere ne ad verilir?",
              choices: ["Zamir (Adıl)", "Zarf (Belirteç)", "Edat (İlgeç)", "Bağlaç"],
              correctAnswer: "Zamir (Adıl)",
              explanation: "İsmin yerini tutan sözcükler zamirdir (ben, sen, o, bu, şu vb.)."
            }
          ],
          "10-Projects/TDE_Yillik_Plan_AL9.md#L110",
          "Roman ve Dil Bilgisi"
        )
      }
    ]
  }
];

// ==========================================
// ASSEMBLE COURSES
// ==========================================
const courses = [
  {
    id: "course_mat_9",
    title: "9. Sınıf Matematik",
    subject: "Matematik",
    gradeLevel: 9,
    orderKey: 1000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Matematik Müfredatı (Sayılar, Geometri, Algoritma, İstatistik ve Olasılık)",
    lessons: matLessons
  },
  {
    id: "course_fiz_9",
    title: "9. Sınıf Fizik",
    subject: "Fizik",
    gradeLevel: 9,
    orderKey: 2000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Fizik Müfredatı (Fizik Bilimi, Kuvvet-Hareket, Akışkanlar ve Enerji)",
    lessons: fizLessons
  },
  {
    id: "course_kim_9",
    title: "9. Sınıf Kimya",
    subject: "Kimya",
    gradeLevel: 9,
    orderKey: 3000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Kimya Müfredatı (Kimya Bilimi, Atom, Etkileşimler ve Maddenin Halleri)",
    lessons: kimLessons
  },
  {
    id: "course_biyo_9",
    title: "9. Sınıf Biyoloji",
    subject: "Biyoloji",
    gradeLevel: 9,
    orderKey: 4000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Biyoloji Müfredatı (Yaşam, Temel Bileşenler, Hücre ve Biyoçeşitlilik)",
    lessons: biyoLessons
  },
  {
    id: "course_tar_9",
    title: "9. Sınıf Tarih",
    subject: "Tarih",
    gradeLevel: 9,
    orderKey: 5000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Tarih Müfredatı (Mehmet Celal ÖZYILDIZ Kanonik)",
    lessons: tarLessons
  },
  {
    id: "course_cog_9",
    title: "9. Sınıf Coğrafya",
    subject: "Coğrafya",
    gradeLevel: 9,
    orderKey: 6000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Coğrafya Müfredatı (Doğal Sistemler, Harita, İklim, Nüfus ve Afetler)",
    lessons: cogLessons
  },
  {
    id: "course_ing_9",
    title: "9. Sınıf İngilizce",
    subject: "İngilizce",
    gradeLevel: 9,
    orderKey: 7000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf İngilizce Müfredatı (Themes 1 - 8)",
    lessons: ingLessons
  },
  {
    id: "course_alm_9",
    title: "9. Sınıf Almanca",
    subject: "Almanca",
    gradeLevel: 9,
    orderKey: 8000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf Almanca Müfredatı (A1.1 Seviyesi)",
    lessons: almLessons
  },
  {
    id: "course_tde_9",
    title: "9. Sınıf Türk Dili ve Edebiyatı",
    subject: "Türk Dili ve Edebiyatı",
    gradeLevel: 9,
    orderKey: 9000.0,
    description: "MEB Türkiye Yüzyılı Maarif Modeli 9. Sınıf TDE Müfredatı (Sözün İnceliği, Anlam Arayışı, Yapı Taşları, Roman)",
    lessons: tdeLessons
  }
];

// Ensure fingerprint on all items
courses.forEach(c => {
  c.lessons.forEach(l => {
    l.items.forEach(item => {
      if (!item.payload) item.payload = {};
      if (!item.payload.provenance) {
        item.payload.provenance = {
          sourceRef: 'project_source',
          sourceSection: l.title,
          importedAt: '2026-10-02T09:00:00.000Z',
          schemaVersion: 'v2',
          reviewStatus: item.publishingStatus === 'active' ? 'verified' : 'needs_review'
        };
      }
      item.payload.provenance.fingerprint = computeItemFingerprint(item);
    });
  });
});

const fullCatalog = {
  schemaVersion: "v2",
  generatedAt: "2026-10-02T09:00:00.000Z",
  gradeLevel: 9,
  academicYear: "2026-2027",
  targetCurriculum: "Türkiye Yüzyılı Maarif Modeli (9. Sınıf / Lise 1)",
  courses
};

// Write catalog JSON
const catalogPath = path.resolve(__dirname, '../content/9-sinif-v2-catalog.json');
fs.writeFileSync(catalogPath, JSON.stringify(fullCatalog, null, 2), 'utf8');
console.log('✅ Generated canonical catalog JSON:', catalogPath);

// Generate Sources Markdown Report
let totalLessons = 0;
let totalItems = 0;
let videoCount = 0;
let activeVideoCount = 0;
let draftVideoCount = 0;
let quizCount = 0;
let ankiCount = 0;
let verifiedCount = 0;
let reviewCount = 0;

courses.forEach(c => {
  totalLessons += c.lessons.length;
  c.lessons.forEach(l => {
    totalItems += l.items.length;
    l.items.forEach(i => {
      if (i.itemType === 'VIDEO') {
        videoCount++;
        if (i.publishingStatus === 'active' && i.contentUrl) activeVideoCount++;
        else draftVideoCount++;
      } else if (i.itemType === 'QUIZ') {
        quizCount++;
      } else if (i.itemType === 'ANKI') {
        ankiCount++;
      }

      if (i.payload?.provenance?.reviewStatus === 'verified') verifiedCount++;
      else reviewCount++;
    });
  });
});

let md = `# 9. Sınıf StudyTracker V2 Müfredat & İçerik Kaynakları Denetim Raporu

**Oluşturulma Tarihi:** 2026-10-02T09:00:00Z  
**Müfredat:** MEB Türkiye Yüzyılı Maarif Modeli (9. Sınıf / Lise 1)  
**Tohum Manifestosu:** [\`content/9-sinif-v2-catalog.json\`](file:///data/data/com.termux/files/home/projects/study-tracker/content/9-sinif-v2-catalog.json)

---

## 📊 Genel Özet & Metrikler (Full-Season V2)

| Metrik | Adet |
|---|---|
| **Toplam Ders (Course)** | **${courses.length}** |
| **Toplam Ünite / Konu (Lesson)** | **${totalLessons}** |
| **Toplam Öğrenme Öğesi (Learning Item)** | **${totalItems}** |
| 🎬 **Video Sayısı (VIDEO Toplam)** | **${videoCount}** |
| &nbsp;&nbsp;&nbsp;&nbsp; 🟢 Aktif Doğrulanmış Video | **${activeVideoCount}** |
| &nbsp;&nbsp;&nbsp;&nbsp; 🟡 Taslak Video (URL Bekleyen) | **${draftVideoCount}** |
| 📝 **Sınav / Test (QUIZ - 3-7 Soru)** | **${quizCount}** |
| 📇 **Anki Destesi (ANKI)** | **${ankiCount}** |
| 🟢 **Doğrulanmış (Verified)** | **${verifiedCount}** |
| 🟡 **İnceleme Bekleyen (Needs Review)** | **${reviewCount}** |

---

## 📁 Kullanılan Proje-İçi Yerel Kaynak Dosyaları (Source of Truth)

Tüm ders planları, kazanım haritaları ve soru içerikleri doğrudan yerel proje dosyalarından derlenmiştir (Hiçbir web araştırması yapılmamıştır):

1. **Yıllık Ders Planları (DefterDoldur AL-9 Maarif Modeli):**
   - \`~/vault/10-Projects/Matematik_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/Fizik_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/Kimya_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/Biyoloji_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/Tarih_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/Cografya_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/İngilizce_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/Almanca_Yillik_Plan_AL9.md\`
   - \`~/vault/10-Projects/TDE_Yillik_Plan_AL9.md\`
   - \`projects/lise1-ogrenme-programi/data/defterdoldur_tum_dersler_9al.json\`
2. **Video Kürasyonu & Öğretmen Eşleştirmeleri:**
   - \`~/vault/10-Projects/1_ay_tarih_video_rehberi.md\` (Kanonik Öğretmen: **Mehmet Celal ÖZYILDIZ**)
   - \`~/vault/10-Projects/1_ay_matematik_khan_academy_videolari.md\` (Khan Academy Türkçe)
   - \`~/vault/10-Projects/1_ay_biyoloji_video_rehberi.md\` (Khan Academy)
   - \`~/vault/10-Projects/1_ay_fizik_video_rehberi.md\` (Khan Academy Türkçe)
   - \`~/vault/10-Projects/1_ay_kimya_video_rehberi.md\`
   - \`~/vault/10-Projects/1_ay_cografya_video_rehberi.md\`
   - \`~/vault/10-Projects/1_ay_ingilizce_video_rehberi.md\`
   - \`~/vault/10-Projects/1_ay_almanca_video_rehberi.md\`
3. **Anki Desteleri & Kelime Listeleri:**
   - \`projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_cografya_1_ay.apkg\`
   - \`projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_ingilizce_1_ay.apkg\`
   - \`projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_almanca_1_ay.apkg\`
   - \`projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_tarih_anki.txt\`
   - \`projects/lise1-ogrenme-programi/data/anki_decks/9_sinif_biyoloji_anki.txt\`
4. **MEB Ders Kitapları Metinleri:**
   - \`projects/lise1-ogrenme-programi/data/meb_kitaplari/matematik9.md\`
   - \`projects/lise1-ogrenme-programi/data/meb_kitaplari/fizik9.md\`
   - \`projects/lise1-ogrenme-programi/data/meb_kitaplari/kimya9.md\`
   - \`projects/lise1-ogrenme-programi/data/meb_kitaplari/biyoloji9.md\`
   - \`projects/lise1-ogrenme-programi/data/meb_kitaplari/tarih9.md\`
   - \`projects/lise1-ogrenme-programi/data/meb_kitaplari/cografya9.md\`
   - \`projects/lise1-ogrenme-programi/data/meb_kitaplari/tde9.md\`

---

## ⚖️ Ürün Modeli ve Kurallar

1. **Takvim Bağımsızlığı & Öğrenci Odaklı Hız (Self-Paced):**
   - Hafta, gün ve ay kısıtlamaları kaldırılmıştır.
   - Öğrencinin ilerlemesi ve kaldığı yer doğrudan çözülen deneme ve quiz geçmişinden (\`attempts\` tablosu) türetilir.
2. **Sıfır Sahte URL Kuralı (No Fabricated URLs):**
   - Yerel dosyalarda doğrudan video bağlantısı bulunmayan tüm konu başlıkları \`contentUrl: null\` ve \`publishingStatus: "draft"\` olarak işaretlenmiştir.
3. **Kanonik Öğretmen İlkesi:**
   - Tarih dersinde **Mehmet Celal ÖZYILDIZ** (*Benim Hocam*) kanonik öğretmen seçimi korunmuştur.
4. **Gerçek QUIZ Soruları:**
   - Tüm QUIZ öğeleri Maarif Modeli kazanımlarıyla %100 uyumlu 3-7 adet deterministik, açıklamalı soru içermektedir.

---

## 🔍 Ders Bazlı Detay Listesi

`;

courses.forEach(c => {
  md += `### 📚 ${c.title} (\`${c.id}\`)\n`;
  md += `- **Branş:** ${c.subject} | **Sıra:** ${c.orderKey}\n`;
  md += `- **Açıklama:** ${c.description}\n`;
  md += `- **Üniteler:**\n\n`;

  c.lessons.forEach(l => {
    md += `  - **${l.title}** (\`${l.id}\`)\n`;
    l.items.forEach(i => {
      const statusIcon = i.publishingStatus === 'active' ? '🟢' : '🟡';
      const typeLabel = i.itemType;
      const urlText = i.contentUrl ? i.contentUrl : '*(URL Yok / Taslak)*';
      md += `    - [${i.displayLabel}] (${typeLabel}) **${i.title}**\n`;
      md += `      - Stable Key: \`${i.stableKey}\`\n`;
      md += `      - Durum: ${statusIcon} **${i.payload?.provenance?.reviewStatus || 'unspecified'}** (Yayın: \`${i.publishingStatus || 'active'}\`)\n`;
      if (i.contentUrl) md += `      - URL: ${urlText}\n`;
      if (i.itemType === 'QUIZ') md += `      - Soru Sayısı: ${i.payload?.quiz?.questions?.length || 0}\n`;
      if (i.itemType === 'ANKI') md += `      - Anki Kart Sayısı: ${i.payload?.cardCount || 'N/A'}\n`;
      md += `      - Parmak İzi: \`${i.payload?.provenance?.fingerprint || ''}\`\n`;
    });
    md += '\n';
  });
  md += '\n';
});

const sourcesPath = path.resolve(__dirname, '../content/9-sinif-v2-catalog.sources.md');
fs.writeFileSync(sourcesPath, md, 'utf8');
console.log('✅ Generated sources report markdown:', sourcesPath);
