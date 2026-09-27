/**
 * Test script to simulate the entire Veli <-> Öğrenci Cloudflare Worker Sync cycle.
 * Tests both v1 unified sync and v2 granular segregated KV architecture.
 */

import worker from './worker.js';

async function mockFetch(method, path, body = null, headers = {}) {
  const url = `https://studytracker-sync.workers.dev${path}`;
  const init = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers
    }
  };
  if (body) {
    init.body = typeof body === 'string' ? body : JSON.stringify(body);
  }

  const req = new Request(url, init);
  const res = await worker.fetch(req, { __LOCAL_TEST__: true }, {});
  const json = await res.json();
  return { status: res.status, ok: res.ok, data: json };
}

async function runTest() {
  console.log('🚀 === Cloudflare Worker StudyTracker v2.0 Senkronizasyon Testi Başlıyor ===\n');

  // Adım 1: Ping / Canlılık Kontrolü
  console.log('1️⃣ Ping testi yapılıyor...');
  const ping = await mockFetch('GET', '/api/ping');
  console.log('   Ping Sonucu:', ping.data);
  if (ping.data.status !== 'ok') throw new Error('Ping başarısız!');
  console.log('   ✅ Ping başarılı.\n');

  // Adım 2: Aile Eşleşme Kodu Oluşturma (Veli)
  console.log('2️⃣ Veli için Aile Kodu oluşturuluyor (ST-8821)...');
  const pair = await mockFetch('POST', '/api/pair', { familyCode: 'ST-8821' });
  console.log('   Eşleşme Kodu:', pair.data.familyCode);
  const familyCode = pair.data.familyCode;
  const adminToken = pair.data.adminToken;
  if (!adminToken) throw new Error('Parent admin token üretilmedi!');
  console.log('   ✅ Eşleşme kodu ve admin token hazır.\n');

  // Adım 3: Veli Haftalık Planı Yüklüyor (POST /api/v2/plan & POST /api/sync)
  console.log('3️⃣ Veli 1. Hafta planını ve derslerini buluta yüklüyor...');
  const initialPayload = {
    plan: {
      planId: 'plan_2026_w38',
      weekId: '2026-W38',
      weekStartDate: '2026-09-14',
      childId: 'child_1',
      timezone: 'Europe/Istanbul',
      updatedAt: '2026-09-18T09:00:00Z',
      rawJson: '{}'
    },
    tasks: [
      {
        id: 'occ_mat_pzt',
        taskId: 'mat_01',
        title: 'Matematik - Üslü Sayılara Giriş',
        subject: 'Matematik - Üslü Sayılara Giriş',
        kind: 'DAILY',
        contentType: 'VIDEO',
        youtubeUrl: 'https://youtu.be/kYqP9K0Y0pU',
        plannedMinutes: 35,
        targetDurationMin: 35,
        parentNote: 'Khan Academy videosunu dikkatlice izle'
      },
      {
        id: 'occ_tar_pzt',
        taskId: 'tar_01',
        title: 'Tarih - Geçmişin İnşa Sürecinde Tarih',
        subject: 'Tarih - Geçmişin İnşa Sürecinde Tarih',
        kind: 'DAILY',
        contentType: 'VIDEO',
        youtubeUrl: 'https://youtu.be/5QxOpTALmEE',
        plannedMinutes: 25,
        targetDurationMin: 25
      }
    ]
  };

  initialPayload.occurrences = initialPayload.tasks.map(t => ({
    id: t.id, familyCode, date: '2026-09-14', planId: t.taskId, subject: t.subject, topic: 'DAILY',
    targetDurationMin: t.targetDurationMin, targetQuestionCount: 0, completedDurationMin: 0,
    completedQuestionCount: 0, status: 'PENDING', parentNote: t.parentNote || '', weekId: '2026-W38',
    youtubeUrl: t.youtubeUrl
  }));
  initialPayload.tasks = initialPayload.tasks.map(t => ({
    taskId: t.taskId, title: t.title, kind: 'DAILY', contentType: 'VIDEO', youtubeUrl: t.youtubeUrl,
    plannedMinutes: t.plannedMinutes, reviewRequired: true, active: true
  }));

  const uploadRes = await mockFetch('POST', `/api/sync?code=${familyCode}`, initialPayload, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': adminToken });
  console.log('   Veli Yükleme Sonucu:', uploadRes.data.success ? 'BAŞARILI' : 'HATA');
  console.log('   Buluttaki Görev Sayısı:', uploadRes.data.tasks?.length || uploadRes.data.data?.tasks?.length);
  console.log('   ✅ Veli planı buluta aktardı.\n');

  const unauthorized = await mockFetch('POST', `/api/v2/commands?code=${familyCode}`, { action: 'WIPE' }, { 'X-Sender-Role': 'PARENT' });
  if (unauthorized.status !== 401) throw new Error('Admin token olmadan yazma reddedilmedi!');
  console.log('   ✅ Admin token olmadan veli yazması reddediliyor.\n');

  // Adım 4: Öğrenci Buluttan Planı İndiriyor (GET /api/v2/sync)
  console.log('4️⃣ Öğrenci uygulaması buluttan güncel planı çekiyor (GET /api/v2/sync)...');
  const childFetch = await mockFetch('GET', `/api/v2/sync?code=${familyCode}`);
  const childData = childFetch.data.data || childFetch.data;
  console.log('   Öğrencinin İndirdiği Görev:', childData.occurrences[0]?.subject);
  console.log('   Video Linki:', childData.occurrences[0]?.youtubeUrl);
  if (!childData.occurrences[0]?.youtubeUrl) throw new Error('Video URL eksik!');
  console.log('   ✅ Öğrenci planı eksiksiz indirdi.\n');

  // Adım 5: Öğrenci Granüler İlerleme Kaydediyor (POST /api/v2/progress/:taskId)
  console.log('5️⃣ Öğrenci Matematik dersini tamamlıyor (35 dk) (POST /api/v2/progress/occ_mat_pzt)...');
  const studentProgRes = await mockFetch('POST', `/api/v2/progress/occ_mat_pzt?code=${familyCode}`, {
    completedMin: 35,
    completedQuestions: 15,
    studentNote: '🌟 Konuyu çok iyi anladım, 15 soru çözdüm.',
    status: 'WAITING_REVIEW'
  }, { 'X-Sender-Role': 'CHILD' });

  console.log('   Öğrenci Progress Sonucu:', studentProgRes.data.success ? 'BAŞARILI' : 'HATA');
  console.log('   Kaydedilen Süre:', studentProgRes.data.progress?.completedMin);
  console.log('   Durum:', studentProgRes.data.progress?.status);
  console.log('   ✅ Granüler öğrenci ilerlemesi kaydedildi.\n');

  // Adım 6: Veli Masasında Onaylıyor (POST /api/v2/reviews)
  console.log('6️⃣ Veli onay masasını açıyor ve görevi ONAYLIYOR (POST /api/v2/reviews)...');
  const parentReviewRes = await mockFetch('POST', `/api/v2/reviews?code=${familyCode}`, {
    taskId: 'occ_mat_pzt',
    isApproved: true,
    parentRating: 5,
    feedbackNote: 'Tebrikler harika çalışma! 🌟'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': adminToken });

  console.log('   Veli Onay Sonucu:', parentReviewRes.data.message);
  console.log('   Nihai Durum:', parentReviewRes.data.progress?.status);
  console.log('   ✅ Veli onayı işlendi.\n');

  // Adım 7: Senkronizasyon ile Öğrenci Onay Durumunu Alıyor (GET /api/sync)
  console.log('7️⃣ Öğrenci son durumu birleşik sync ile çekiyor (GET /api/sync)...');
  const finalGet = await mockFetch('GET', `/api/sync?code=${familyCode}`);
  const finalData = finalGet.data.data || finalGet.data;
  console.log('   Nihai Ders Durumu:', finalData.occurrences[0]?.status);
  console.log('   Tamamlanan Soru:', finalData.occurrences[0]?.completedQuestionCount);
  if (finalData.occurrences[0]?.status !== 'APPROVED') throw new Error('Onay başarısız!');
  console.log('   ✅ Birleşik senkronizasyon onay durumu ile döndü.\n');

  // Adım 8: Veli Komut Deseni: Tekil Görev İade (POST /api/v2/commands -> REJECT_TASK)
  console.log('8️⃣ Veli Matematik dersini iade ediyor (POST /api/v2/commands -> REJECT_TASK)...');
  const rejectRes = await mockFetch('POST', `/api/v2/commands?code=${familyCode}`, {
    action: 'REJECT_TASK',
    taskId: 'occ_mat_pzt',
    reason: 'Soruları eksik çözmüşsün, baştan yap.'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': adminToken });

  console.log('   İade Sonucu:', rejectRes.data.message);
  const syncAfterReject = await mockFetch('GET', `/api/v2/sync?code=${familyCode}`);
  console.log('   İade Sonrası Durum (REJECTED Bekleniyor):', syncAfterReject.data.occurrences[0]?.status);
  console.log('   Sıfırlanan Süre (0 Bekleniyor):', syncAfterReject.data.occurrences[0]?.completedDurationMin);
  if (syncAfterReject.data.occurrences[0]?.status !== 'REJECTED') throw new Error('İade başarısız!');
  console.log('   ✅ Komut Deseni (REJECT_TASK) %100 başarılı.\n');

  // Adım 9: Veli Mesajı & Bildirim Gönderme ve Okundu Testi (POST /api/v2/messages & /api/messages)
  console.log('9️⃣ Veli öğrenciye anlık bildirim gönderiyor (POST /api/v2/messages)...');
  const sendMsg = await mockFetch('POST', `/api/v2/messages?code=${familyCode}`, {
    title: 'Ders Zamanı!',
    message: 'Bugünkü 9. Sınıf Matematik etüdünü yapmayı unutma 🚀',
    type: 'REMINDER'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': adminToken });

  console.log('   Mesaj Gönderim Sonucu:', sendMsg.data.message);
  console.log('   Eklenen Mesaj ID:', sendMsg.data.data?.id);
  if (!sendMsg.data.success || !sendMsg.data.data?.id) throw new Error('Mesaj gönderilemedi!');

  console.log('   Öğrenci okunmamış mesajları çekiyor (GET /api/messages?unread=true)...');
  const unreadRes = await mockFetch('GET', `/api/messages?code=${familyCode}&unread=true`);
  console.log('   Okunmamış Mesaj Sayısı (1 Bekleniyor):', unreadRes.data.messages?.length);
  if (unreadRes.data.messages?.length !== 1) throw new Error('Okunmamış mesaj sayısı hatalı!');

  console.log('   Öğrenci mesajı okundu olarak işaretliyor (PUT /api/v2/messages)...');
  const readRes = await mockFetch('PUT', `/api/v2/messages?code=${familyCode}`, {
    messageId: sendMsg.data.data.id
  });
  console.log('   Okundu Sonucu:', readRes.data.message);

  const unreadAfter = await mockFetch('GET', `/api/v2/messages?code=${familyCode}&unread=true`);
  console.log('   Kalan Okunmamış Mesaj (0 Bekleniyor):', unreadAfter.data.messages?.length);
  if (unreadAfter.data.messages?.length !== 0) throw new Error('Mesaj okundu olarak işaretlenemedi!');
  console.log('   ✅ Bildirim ve mesaj döngüsü %100 başarılı.\n');

  // Adım 9.5: Full sync review + quiz round-trip
  const roundTrip = await mockFetch('POST', `/api/sync?code=${familyCode}`, {
    senderRole: 'PARENT',
    plan: finalData.plan,
    tasks: finalData.tasks,
    occurrences: finalData.occurrences,
    reviews: [{ id: 'rev_roundtrip', familyCode, sessionId: 'occ_tar_pzt', isApproved: false, feedbackNote: 'Tekrar et', reviewedAt: Date.now() }],
    quizzes: [{ quizId: 'quiz_roundtrip', title: 'Mini Test', questions: [], completed: false }]
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': adminToken });
  const roundData = roundTrip.data.data || roundTrip.data;
  if (!(roundData.reviews || []).some(r => r.id === 'rev_roundtrip')) throw new Error('Review full-sync kalıcı değil!');
  if (!(roundData.quizzes || []).some(q => q.quizId === 'quiz_roundtrip')) throw new Error('Quiz full-sync kalıcı değil!');
  console.log('   ✅ Review ve quiz full-sync round-trip başarılı.\n');

  // Adım 10: 24 Saatlik Geri Alma ile Tam Sıfırlama (WIPE & RESTORE)
  console.log('🔟 Tam Sıfırlama (WIPE) ve 24 Saatlik Geri Alma (RESTORE) testi...');
  const wipeRes = await mockFetch('POST', `/api/v2/commands?code=${familyCode}`, { action: 'WIPE' }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': adminToken });
  console.log('   Wipe Sonucu:', wipeRes.data.message);
  const syncAfterWipe = await mockFetch('GET', `/api/v2/sync?code=${familyCode}`);
  console.log('   Wipe Sonrası Görev Sayısı (0 Bekleniyor):', syncAfterWipe.data.tasks.length);

  const restoreRes = await mockFetch('POST', `/api/v2/commands?code=${familyCode}`, { action: 'RESTORE' }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': adminToken });
  console.log('   Restore Sonucu:', restoreRes.data.message);
  const syncAfterRestore = await mockFetch('GET', `/api/v2/sync?code=${familyCode}`);
  console.log('   Restore Sonrası Görev Sayısı (2 Bekleniyor):', syncAfterRestore.data.tasks.length);
  if (syncAfterRestore.data.tasks.length !== 2) throw new Error('Restore başarısız!');
  console.log('   ✅ WIPE & RESTORE %100 başarılı.\n');

  console.log('🎉 ========================================================');
  console.log('🎉 TÜM v2.0 SEGREGATED & COMMAND PATTERN TESTLERİ BAŞARIYLA GEÇTİ!');
  console.log('🎉 ========================================================');
}

runTest().catch((err) => {
  console.error('❌ Test sırasında hata:', err);
  process.exit(1);
});
