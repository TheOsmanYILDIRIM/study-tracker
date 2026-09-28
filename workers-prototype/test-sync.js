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
  console.log('2️⃣ Veli için güvenli test Aile Kodu oluşturuluyor...');
  const pair = await mockFetch('POST', '/api/pair', { familyCode: 'ST-TEST-2026-SYNC-8821' });
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

  // Adım 5.5: GÜVENLİK TESTLERİ (Child Sahte Review / Auth Kontrolleri)
  console.log('5.5️⃣ Güvenlik Testi: Child sahte review saldırısı ve Veli auth denetimi...');
  
  // A. Child doğrudan /api/v2/reviews üzerinden onaylamaya çalışıyor -> 403 dönmeli
  const childDirectReview = await mockFetch('POST', `/api/v2/reviews?code=${familyCode}`, {
    taskId: 'occ_mat_pzt',
    isApproved: true,
    parentRating: 5,
    feedbackNote: 'Kendime 5 yıldız veriyorum'
  }, { 'X-Sender-Role': 'CHILD' });
  if (childDirectReview.status !== 403) throw new Error('Child doğrudan review çağrısı 403 ile engellenmedi!');
  console.log('   ✅ CHILD doğrudan /api/v2/reviews çağrısı 403 ile engellendi.');

  // B. Veli token olmadan /api/v2/reviews çağrısı -> 401 dönmeli
  const parentNoTokenReview = await mockFetch('POST', `/api/v2/reviews?code=${familyCode}`, {
    taskId: 'occ_mat_pzt',
    isApproved: true,
    parentRating: 5
  }, { 'X-Sender-Role': 'PARENT' });
  if (parentNoTokenReview.status !== 401) throw new Error('Veli admin token olmadan /api/v2/reviews 401 dönmedi!');
  console.log('   ✅ PARENT token olmadan /api/v2/reviews 401 ile engellendi.');

  // C. Child unified POST /api/sync ile sahte review enjekte etmeye çalışıyor -> review yok sayılmalı, progress bozulmamalı
  const childFakeSyncReview = await mockFetch('POST', `/api/sync?code=${familyCode}`, {
    senderRole: 'CHILD',
    occurrences: [{
      id: 'occ_mat_pzt',
      completedDurationMin: 40,
      completedQuestionCount: 20,
      status: 'WAITING_REVIEW'
    }],
    reviews: [{
      id: 'rev_fake_child',
      sessionId: 'occ_mat_pzt',
      occurrenceKey: 'occ_mat_pzt',
      isApproved: true,
      feedbackNote: 'Sahte Veli Onayı'
    }]
  }, { 'X-Sender-Role': 'CHILD' });
  
  const checkAfterFake = await mockFetch('GET', `/api/v2/sync?code=${familyCode}`);
  const occAfterFake = checkAfterFake.data.occurrences?.find(o => o.id === 'occ_mat_pzt');
  const reviewsAfterFake = checkAfterFake.data.reviews || [];
  if (occAfterFake?.status === 'APPROVED') throw new Error('GÜVENLİK AÇIĞI: Child sahte review ile görevi onayladı!');
  if (reviewsAfterFake.some(r => r.id === 'rev_fake_child')) throw new Error('GÜVENLİK AÇIĞI: Child sahte review kaydı KV ye yazıldı!');
  if (occAfterFake?.completedDurationMin !== 40 || occAfterFake?.completedQuestionCount !== 20) {
    throw new Error('Child progress (süre/soru) yazımı başarısız oldu!');
  }
  console.log('   ✅ CHILD unified POST /api/sync üzerinden sahte review gönderdiğinde review yok sayıldı ve status APPROVED olmadı.');
  console.log('   ✅ Normal CHILD progress (40 dk, 20 soru) başarıyla korundu ve güncellendi.');

  // D. Veli token olmadan unified POST /api/sync ile review göndermeye çalışıyor -> 401 dönmeli
  const parentNoTokenSync = await mockFetch('POST', `/api/sync?code=${familyCode}`, {
    senderRole: 'PARENT',
    reviews: [{ id: 'rev_parent_unauth', sessionId: 'occ_mat_pzt', isApproved: true }]
  }, { 'X-Sender-Role': 'PARENT' });
  if (parentNoTokenSync.status !== 401) throw new Error('Veli admin token olmadan unified sync 401 dönmedi!');
  console.log('   ✅ PARENT token olmadan unified sync review write 401 döndü.\n');

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
  console.log('9️⃣ Mesaj/Bildirim Auth Güvenlik Testi & Veli bildirim döngüsü...');

  // A. SYSTEM spoof saldırısı (X-Sender-Role: SYSTEM, token yok) -> 403/401 ile engellenmeli
  const systemSpoofMsg = await mockFetch('POST', `/api/v2/messages?code=${familyCode}`, {
    title: 'Sahte Sistem Bildirimi',
    message: 'Ben sistemim',
    type: 'ALERT'
  }, { 'X-Sender-Role': 'SYSTEM' });
  if (systemSpoofMsg.status !== 403 && systemSpoofMsg.status !== 401) {
    throw new Error('GÜVENLİK AÇIĞI: SYSTEM rolü spoofing engellenmedi!');
  }
  console.log('   ✅ SYSTEM spoof (token yok) 403 ile engellendi.');

  // B. CHILD mesaj yazma denemesi (X-Sender-Role: CHILD) -> 403 dönmeli
  const childWriteMsg = await mockFetch('POST', `/api/v2/messages?code=${familyCode}`, {
    title: 'Öğrenci Mesajı',
    message: 'Öğrenci mesaj yazamaz',
    type: 'REMINDER'
  }, { 'X-Sender-Role': 'CHILD' });
  if (childWriteMsg.status !== 403) throw new Error('Child mesaj yazma isteği 403 ile engellenmedi!');
  console.log('   ✅ CHILD mesaj yazma isteği 403 ile engellendi.');

  // C. PARENT token olmadan mesaj gönderme -> 401 dönmeli
  const parentNoTokenMsg = await mockFetch('POST', `/api/v2/messages?code=${familyCode}`, {
    title: 'Yetkisiz Veli Mesajı',
    message: 'Token yok',
    type: 'REMINDER'
  }, { 'X-Sender-Role': 'PARENT' });
  if (parentNoTokenMsg.status !== 401) throw new Error('Veli admin token olmadan mesaj yazma 401 dönmedi!');
  console.log('   ✅ PARENT token olmadan mesaj yazma 401 ile engellendi.');

  // D. Yetkili PARENT doğru token ile mesaj gönderiyor -> Başarılı olmalı
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

  // Adım 11: Görev 3B Regression Test: RESET & WIPE Sonrası Stale Child Guard
  console.log('1️⃣1️⃣ Görev 3B: RESET Sonrası Stale Child Guard & Acknowledged Child Testi...');
  const resPair = await mockFetch('POST', '/api/pair', { familyCode: 'ST-TEST-2026-RESU-8899' });
  const resCode = resPair.data.familyCode;
  const resAdminToken = resPair.data.adminToken;

  // 1. Veli plan ve test yüklüyor
  await mockFetch('POST', `/api/sync?code=${resCode}`, {
    senderRole: 'PARENT',
    plan: { planId: 'plan_res', weekId: '2026-W38' },
    tasks: [{ taskId: 'task_res', title: 'Fizik Test', plannedMinutes: 30 }],
    occurrences: [{ id: 'occ_res', planId: 'task_res', subject: 'Fizik', targetDurationMin: 30 }],
    quizzes: [{ quizId: 'quiz_res', title: 'Fizik Quizi', questions: [], completed: false }]
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': resAdminToken });

  // 2. Child çalışma verilerini gönderiyor (session, screenshot, quiz submission, progress)
  await mockFetch('POST', `/api/sync?code=${resCode}`, {
    senderRole: 'CHILD',
    occurrences: [{ id: 'occ_res', completedDurationMin: 30, completedQuestionCount: 15, status: 'WAITING_REVIEW' }],
    sessions: [{ id: 'sess_res_1', occurrenceId: 'occ_res', durationMin: 30, isCompleted: true, updatedAt: 1000 }],
    screenshots: [{ id: 'ss_res_1', sessionId: 'sess_res_1', imageUrl: 'data:image/webp;base64,sample' }],
    quizzes: [{ quizId: 'quiz_res', completed: true, studentAnswers: { q1: 'A' }, correctCount: 1 }],
    reviews: [{ id: 'rev_fake_res', sessionId: 'occ_res', isApproved: true }]
  }, { 'X-Sender-Role': 'CHILD' });

  // 3. Veli RESET_ALL_PROGRESS yapıyor
  await mockFetch('POST', `/api/v2/commands?code=${resCode}`, { action: 'RESET_ALL_PROGRESS' }, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': resAdminToken
  });

  // 4. Worker'da sıfırlandığını ve resetAt alındığını doğrula
  const syncAfterReset = (await mockFetch('GET', `/api/v2/sync?code=${resCode}`)).data;
  const serverResetAt = Number(syncAfterReset.resetAt || syncAfterReset.data?.resetAt || 0);
  if (serverResetAt <= 0) throw new Error('serverResetAt üretilmedi veya sıfır döndü!');
  const occList = syncAfterReset.occurrences || syncAfterReset.data?.occurrences || [];
  const occAfterReset = occList.find(o => o.id === 'occ_res');
  if (occAfterReset?.completedDurationMin !== 0) throw new Error('Reset sonrası progress sıfırlanmadı!');
  if (syncAfterReset.sessions?.length !== 0) throw new Error('Reset sonrası sessions temizlenmedi!');
  if (syncAfterReset.screenshots?.length !== 0) throw new Error('Reset sonrası screenshots temizlenmedi!');
  if (syncAfterReset.quizzes?.[0]?.completed !== false) throw new Error('Reset sonrası quiz sıfırlanmadı!');

  // 5. Henüz RESET'ten habersiz stale child cihazı (clientLastResetAt: 0) eski yerel DB'sini tekrar POST ediyor
  const stalePostRes = await mockFetch('POST', `/api/sync?code=${resCode}`, {
    senderRole: 'CHILD',
    clientLastResetAt: 0,
    occurrences: [{ id: 'occ_res', completedDurationMin: 30, completedQuestionCount: 15, status: 'WAITING_REVIEW' }],
    sessions: [{ id: 'sess_res_1', occurrenceId: 'occ_res', durationMin: 30, isCompleted: true, updatedAt: 1000 }],
    screenshots: [{ id: 'ss_res_1', sessionId: 'sess_res_1', imageUrl: 'data:image/webp;base64,sample' }],
    quizzes: [{ quizId: 'quiz_res', completed: true, studentAnswers: { q1: 'A' }, correctCount: 1 }],
    reviews: [{ id: 'rev_fake_res', sessionId: 'occ_res', isApproved: true }]
  }, { 'X-Sender-Role': 'CHILD' });

  // 6. GET sync ile stale verinin dirilmediğini doğrula
  const syncAfterStaleUpload = (await mockFetch('GET', `/api/v2/sync?code=${resCode}`)).data;
  const occResurrected = syncAfterStaleUpload.occurrences?.find(o => o.id === 'occ_res');
  const sessResurrected = syncAfterStaleUpload.sessions || [];
  const ssResurrected = syncAfterStaleUpload.screenshots || [];
  const quizResurrected = syncAfterStaleUpload.quizzes || [];
  const revResurrected = syncAfterStaleUpload.reviews || [];

  if (sessResurrected.length !== 0) throw new Error('GÜVENLİK AÇIĞI: Stale session dirildi!');
  if (ssResurrected.length !== 0) throw new Error('GÜVENLİK AÇIĞI: Stale screenshot dirildi!');
  if (occResurrected?.completedDurationMin !== 0) throw new Error('GÜVENLİK AÇIĞI: Stale occurrence progress dirildi!');
  if (quizResurrected[0]?.completed === true) throw new Error('GÜVENLİK AÇIĞI: Stale quiz submission dirildi!');
  if (revResurrected.length !== 0) throw new Error('GÜVENLİK AÇIĞI: Fake review dirildi!');
  console.log('   ✅ Stale child upload (clientLastResetAt=0) reset sonrası hiçbir eski veriyi diriltemedi.');

  // 7. Reset epoch'u kabul eden güncel child (clientLastResetAt = serverResetAt) yeni çalışma gönderiyor
  const ackPostRes = await mockFetch('POST', `/api/sync?code=${resCode}`, {
    senderRole: 'CHILD',
    clientLastResetAt: serverResetAt,
    occurrences: [{ id: 'occ_res', completedDurationMin: 25, completedQuestionCount: 10, status: 'WAITING_REVIEW' }],
    sessions: [{ id: 'sess_new_1', occurrenceId: 'occ_res', durationMin: 25, isCompleted: true, updatedAt: Date.now() }]
  }, { 'X-Sender-Role': 'CHILD' });

  const syncAfterAck = (await mockFetch('GET', `/api/v2/sync?code=${resCode}`)).data;
  const occAfterAck = syncAfterAck.occurrences?.find(o => o.id === 'occ_res');
  const sessAfterAck = syncAfterAck.sessions || [];
  if (occAfterAck?.completedDurationMin !== 25) throw new Error('Reset sonrası güncel child progress kabul edilmedi!');
  if (sessAfterAck.length !== 1 || sessAfterAck[0].id !== 'sess_new_1') throw new Error('Reset sonrası güncel session kabul edilmedi!');
  console.log('   ✅ Reset epoch kabul edildiğinde (clientLastResetAt=serverResetAt) yeni child progress (25 dk) başarıyla kabul edildi.\n');

  // Adım 12: WIPE Sonrası Stale Child Guard Testi
  console.log('1️⃣2️⃣ Görev 3B: WIPE Sonrası Stale Child Guard Testi...');
  await mockFetch('POST', `/api/v2/commands?code=${resCode}`, { action: 'WIPE' }, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': resAdminToken
  });

  const syncAfterWipeCheck = (await mockFetch('GET', `/api/v2/sync?code=${resCode}`)).data;
  const serverWipeAt = Number(syncAfterWipeCheck.resetAt || syncAfterWipeCheck.data?.resetAt || 0);

  // Stale child eski veriyi POST ediyor (eski reset timestamp ile)
  await mockFetch('POST', `/api/sync?code=${resCode}`, {
    senderRole: 'CHILD',
    clientLastResetAt: serverResetAt, // eski reset zamanı (WIPE'tan önceki)
    occurrences: [{ id: 'occ_res', completedDurationMin: 30, completedQuestionCount: 15, status: 'WAITING_REVIEW' }],
    sessions: [{ id: 'sess_res_1', occurrenceId: 'occ_res', durationMin: 30, isCompleted: true }]
  }, { 'X-Sender-Role': 'CHILD' });

  const syncAfterStaleWipe = (await mockFetch('GET', `/api/v2/sync?code=${resCode}`)).data;
  if ((syncAfterStaleWipe.tasks || []).length !== 0) throw new Error('Wipe sonrası tasks geri oluştu!');
  if ((syncAfterStaleWipe.occurrences || []).length !== 0) throw new Error('Wipe sonrası occurrences geri oluştu!');
  if ((syncAfterStaleWipe.sessions || []).length !== 0) throw new Error('Wipe sonrası sessions geri oluştu!');
  console.log('   ✅ WIPE sonrası stale child gönderimi silinen plan ve oturumları diriltemedi.\n');

  console.log('🎉 ========================================================');
  console.log('🎉 TÜM v2.0 SEGREGATED & COMMAND PATTERN TESTLERİ BAŞARIYLA GEÇTİ!');
  console.log('🎉 ========================================================');
}

runTest().catch((err) => {
  console.error('❌ Test sırasında hata:', err);
  process.exit(1);
});
