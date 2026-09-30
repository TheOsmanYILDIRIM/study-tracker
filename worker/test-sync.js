/**
 * Test script to simulate the entire Veli <-> Öğrenci Cloudflare Worker Sync cycle.
 * Tests both v1 unified sync and v2 granular segregated KV architecture.
 */

import worker, { normalizeSession, shouldReplaceSession } from './worker.js';

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

  // Adım 13: Görev 4C1 - Session Winner & Legacy Timestamp Fallback Testleri
  console.log('1️⃣3️⃣ Görev 4C1: Session Winner & Legacy Timestamp Fallback Testleri...');

  // 1. normalizeSession Legacy Timestamp Fallbacks
  const normA = normalizeSession({ id: 's1', updatedAt: 500, startTime: 100, endTime: 400 });
  if (normA.updatedAt !== 500) throw new Error(`normalizeSession A failed: expected 500 got ${normA.updatedAt}`);

  const normB = normalizeSession({ id: 's2', startTime: 100, endTime: 400 });
  if (normB.updatedAt !== 400) throw new Error(`normalizeSession B failed: expected 400 got ${normB.updatedAt}`);

  const normC = normalizeSession({ id: 's3', startTime: 100, endTime: null });
  if (normC.updatedAt !== 100) throw new Error(`normalizeSession C failed: expected 100 got ${normC.updatedAt}`);

  const normD = normalizeSession({ id: 's4' });
  if (normD.updatedAt !== 0) throw new Error(`normalizeSession D failed: expected 0 got ${normD.updatedAt}`);

  // 2. Pure shouldReplaceSession Test Cases (A - F)
  // CASE A: Normal progression
  if (!shouldReplaceSession({ updatedAt: 100, isCompleted: false }, { updatedAt: 200, isCompleted: true })) {
    throw new Error('CASE A failed: newer timestamp must win');
  }

  // CASE B: Critical stale completion (stale completed must NOT overwrite newer active)
  if (shouldReplaceSession({ updatedAt: 300, isCompleted: false }, { updatedAt: 100, isCompleted: true })) {
    throw new Error('CASE B failed: stale completed must NOT overwrite newer active');
  }

  // CASE C: Stale active
  if (shouldReplaceSession({ updatedAt: 300, isCompleted: true }, { updatedAt: 100, isCompleted: false })) {
    throw new Error('CASE C failed: stale active must NOT overwrite newer completed');
  }

  // CASE D: Equal timestamp completion advancement
  if (!shouldReplaceSession({ updatedAt: 200, isCompleted: false }, { updatedAt: 200, isCompleted: true })) {
    throw new Error('CASE D failed: equal timestamp completion advancement must win');
  }

  // CASE E: Equal timestamp regression
  if (shouldReplaceSession({ updatedAt: 200, isCompleted: true }, { updatedAt: 200, isCompleted: false })) {
    throw new Error('CASE E failed: equal timestamp regression must be rejected');
  }

  // CASE F: Same timestamp / same state
  if (shouldReplaceSession({ updatedAt: 200, isCompleted: true }, { updatedAt: 200, isCompleted: true })) {
    throw new Error('CASE F failed: identical timestamp & state should not replace');
  }

  console.log('   ✅ normalizeSession ve shouldReplaceSession pure unit testleri (CASE A-F) başarılı.');

  // 3. Gerçek KV End-to-End Stale Session Testi
  const sessTestCode = 'ST-TEST-2026-SESS-9988';
  const sessPair = await mockFetch('POST', '/api/pair', { familyCode: sessTestCode });
  const sessAdminToken = sessPair.data.adminToken;

  // 1. Initial session: updatedAt=300, isCompleted=false
  await mockFetch('POST', `/api/sync?code=${sessTestCode}`, {
    senderRole: 'CHILD',
    sessions: [{ id: 'sess_stale_test', occurrenceId: 'occ_test', durationMin: 10, isCompleted: false, updatedAt: 300 }]
  }, { 'X-Sender-Role': 'CHILD' });

  let sessSync1 = (await mockFetch('GET', `/api/v2/sync?code=${sessTestCode}`)).data;
  let storedSess = sessSync1.sessions?.find(s => s.id === 'sess_stale_test');
  if (!storedSess || storedSess.updatedAt !== 300 || storedSess.isCompleted !== false) {
    throw new Error('Initial session kaydedilemedi!');
  }

  // 2. Stale incoming: updatedAt=100, isCompleted=true
  await mockFetch('POST', `/api/sync?code=${sessTestCode}`, {
    senderRole: 'CHILD',
    sessions: [{ id: 'sess_stale_test', occurrenceId: 'occ_test', durationMin: 5, isCompleted: true, updatedAt: 100 }]
  }, { 'X-Sender-Role': 'CHILD' });

  let sessSync2 = (await mockFetch('GET', `/api/v2/sync?code=${sessTestCode}`)).data;
  storedSess = sessSync2.sessions?.find(s => s.id === 'sess_stale_test');
  if (storedSess.updatedAt !== 300 || storedSess.isCompleted !== false) {
    throw new Error('GÜVENLİK/STALE AÇIĞI: Stale completed session daha yeni server session kaydını ezdi!');
  }
  console.log('   ✅ KV End-to-End: Stale completed session (updatedAt=100) server kaydını (updatedAt=300) ezemedi.');

  // 3. Newer incoming: updatedAt=400, isCompleted=true
  await mockFetch('POST', `/api/sync?code=${sessTestCode}`, {
    senderRole: 'CHILD',
    sessions: [{ id: 'sess_stale_test', occurrenceId: 'occ_test', durationMin: 20, isCompleted: true, updatedAt: 400 }]
  }, { 'X-Sender-Role': 'CHILD' });

  let sessSync3 = (await mockFetch('GET', `/api/v2/sync?code=${sessTestCode}`)).data;
  storedSess = sessSync3.sessions?.find(s => s.id === 'sess_stale_test');
  if (storedSess.updatedAt !== 400 || storedSess.isCompleted !== true) {
    throw new Error('Newer session (updatedAt=400) kabul edilmedi!');
  }
  // 14. Görev 5B1: Parent Authoritative Revision Precondition & Lost-Update Hardening Tests
  console.log('1️⃣4️⃣ Görev 5B1: Parent Revision Precondition & Stale Write Guard Testleri...');
  const revTestCode = 'ST-REV1-2026-TEST-1001';
  const revTestPair = await mockFetch('POST', '/api/pair', { familyCode: revTestCode });
  const revTestAdminToken = revTestPair.data.adminToken;
  if (!revTestAdminToken) throw new Error('revTestAdminToken oluşturulamadı!');

  // 1. Initial Plan (title = ORIGINAL)
  const initialPlanPayload = {
    familyCode: revTestCode,
    plan: { planId: 'plan_orig', weekId: '2026-W38', title: 'ORIGINAL' },
    tasks: [{ taskId: 'task_orig', title: 'ORIGINAL', plannedMinutes: 30 }],
    occurrences: [{ id: 'occ_orig', planId: 'task_orig', subject: 'ORIGINAL', targetDurationMin: 30 }]
  };
  const initRes = await mockFetch('POST', `/api/sync?code=${revTestCode}`, initialPlanPayload, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': revTestAdminToken
  });
  if (!initRes.ok) throw new Error(`Initial plan yükleme başarısız: ${JSON.stringify(initRes.data)}`);

  // 2. GET baseRevision
  const baseSync = (await mockFetch('GET', `/api/sync?code=${revTestCode}`)).data;
  const baseRevision = Number(baseSync.revision || baseSync.meta?.revision || 0);
  const baseTitle = baseSync.plan?.title || baseSync.occurrences?.[0]?.subject;
  console.log(`   Base snapshot alındı (revision=${baseRevision}, planTitle='${baseTitle}')`);

  // 3. Parent A: expectedRevision = baseRevision, title = PARENT_A_NEW yazar
  const parentAPayload = {
    familyCode: revTestCode,
    expectedRevision: baseRevision,
    plan: { planId: 'plan_orig', weekId: '2026-W38', title: 'PARENT_A_NEW' },
    tasks: [{ taskId: 'task_orig', title: 'PARENT_A_NEW', plannedMinutes: 40 }],
    occurrences: [{ id: 'occ_orig', planId: 'task_orig', subject: 'PARENT_A_NEW', targetDurationMin: 40 }]
  };
  const parentARes = await mockFetch('POST', `/api/sync?code=${revTestCode}`, parentAPayload, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': revTestAdminToken
  });
  if (!parentARes.ok) throw new Error(`Parent A yazması başarısız: ${JSON.stringify(parentARes.data)}`);

  // 4. GET: A değişikliğinin mevcut olduğunu ve revision'ın ilerlediğini doğrula
  const syncAfterA = (await mockFetch('GET', `/api/sync?code=${revTestCode}`)).data;
  const revisionAfterA = Number(syncAfterA.revision || syncAfterA.meta?.revision || 0);
  const titleAfterA = syncAfterA.plan?.title || syncAfterA.occurrences?.[0]?.subject;
  if (titleAfterA !== 'PARENT_A_NEW' || revisionAfterA !== baseRevision + 1) {
    throw new Error(`Parent A doğrulaması başarısız: title=${titleAfterA}, revision=${revisionAfterA}`);
  }
  console.log(`   ✅ Parent A planı yazdı (expectedRevision=${baseRevision} -> newRevision=${revisionAfterA}, title='${titleAfterA}')`);

  // 5. Parent B: A'yı hiç görmemiş stale snapshot ile (expectedRevision = baseRevision, title = PARENT_B_STALE) yazar -> 409 REVISION_CONFLICT dönmeli
  const parentBStalePayload = {
    familyCode: revTestCode,
    expectedRevision: baseRevision,
    plan: { planId: 'plan_orig', weekId: '2026-W38', title: 'PARENT_B_STALE' },
    tasks: [{ taskId: 'task_orig', title: 'PARENT_B_STALE', plannedMinutes: 20 }],
    occurrences: [{ id: 'occ_orig', planId: 'task_orig', subject: 'PARENT_B_STALE', targetDurationMin: 20 }]
  };
  const parentBStaleRes = await mockFetch('POST', `/api/sync?code=${revTestCode}`, parentBStalePayload, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': revTestAdminToken
  });
  if (parentBStaleRes.status !== 409 || parentBStaleRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`Stale Parent B yazması 409 REVISION_CONFLICT ile engellenmedi! status=${parentBStaleRes.status}, data=${JSON.stringify(parentBStaleRes.data)}`);
  }
  if (parentBStaleRes.data.currentRevision !== revisionAfterA) {
    throw new Error(`Conflict yanıtındaki currentRevision (${parentBStaleRes.data.currentRevision}) beklenen (${revisionAfterA}) ile uyuşmuyor!`);
  }
  console.log(`   ✅ Stale Parent B yazması 409 REVISION_CONFLICT ile engellendi (currentRevision=${parentBStaleRes.data.currentRevision}).`);

  // 6. GET: Conflict sonrası server state'in hiç bozulmadığını ve revision'ın ilerlemediğini doğrula
  const syncAfterStale = (await mockFetch('GET', `/api/sync?code=${revTestCode}`)).data;
  const revisionAfterStale = Number(syncAfterStale.revision || syncAfterStale.meta?.revision || 0);
  const titleAfterStale = syncAfterStale.plan?.title || syncAfterStale.occurrences?.[0]?.subject;
  if (titleAfterStale !== 'PARENT_A_NEW' || revisionAfterStale !== revisionAfterA) {
    throw new Error(`Conflict sonrası state korunamadı! title=${titleAfterStale}, revision=${revisionAfterStale}`);
  }
  console.log(`   ✅ Conflict sonrası server state korundu (title='${titleAfterStale}', revision=${revisionAfterStale}).`);

  // 7. Fresh Retry Testi: Parent B güncel revision ile tekrar dener
  const parentBFreshPayload = {
    familyCode: revTestCode,
    expectedRevision: revisionAfterA,
    plan: { planId: 'plan_orig', weekId: '2026-W38', title: 'PARENT_B_FRESH' },
    tasks: [{ taskId: 'task_orig', title: 'PARENT_B_FRESH', plannedMinutes: 20 }],
    occurrences: [{ id: 'occ_orig', planId: 'task_orig', subject: 'PARENT_B_FRESH', targetDurationMin: 20 }]
  };
  const parentBFreshRes = await mockFetch('POST', `/api/sync?code=${revTestCode}`, parentBFreshPayload, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': revTestAdminToken
  });
  if (!parentBFreshRes.ok) {
    throw new Error(`Parent B fresh retry başarısız: ${JSON.stringify(parentBFreshRes.data)}`);
  }
  const syncAfterFresh = (await mockFetch('GET', `/api/sync?code=${revTestCode}`)).data;
  const revisionAfterFresh = Number(syncAfterFresh.revision || syncAfterFresh.meta?.revision || 0);
  const titleAfterFresh = syncAfterFresh.plan?.title || syncAfterFresh.occurrences?.[0]?.subject;
  if (titleAfterFresh !== 'PARENT_B_FRESH' || revisionAfterFresh !== revisionAfterA + 1) {
    throw new Error(`Fresh retry sonrası doğrulama başarısız! title=${titleAfterFresh}, revision=${revisionAfterFresh}`);
  }
  console.log(`   ✅ Fresh retry başarılı (title='${titleAfterFresh}', revision=${revisionAfterFresh}).`);

  // 8. Legacy Migration Testi: expectedRevision göndermeyen Parent yazması
  const legacyParentPayload = {
    familyCode: revTestCode,
    plan: { planId: 'plan_orig', weekId: '2026-W38', title: 'PARENT_C_LEGACY' },
    tasks: [{ taskId: 'task_orig', title: 'PARENT_C_LEGACY', plannedMinutes: 25 }],
    occurrences: [{ id: 'occ_orig', planId: 'task_orig', subject: 'PARENT_C_LEGACY', targetDurationMin: 25 }]
  };
  const legacyRes = await mockFetch('POST', `/api/sync?code=${revTestCode}`, legacyParentPayload, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': revTestAdminToken
  });
  if (!legacyRes.ok) {
    throw new Error(`Legacy parent yazması reddedildi: ${JSON.stringify(legacyRes.data)}`);
  }
  const syncAfterLegacy = (await mockFetch('GET', `/api/sync?code=${revTestCode}`)).data;
  const revisionAfterLegacy = Number(syncAfterLegacy.revision || syncAfterLegacy.meta?.revision || 0);
  const titleAfterLegacy = syncAfterLegacy.plan?.title || syncAfterLegacy.occurrences?.[0]?.subject;
  if (titleAfterLegacy !== 'PARENT_C_LEGACY' || revisionAfterLegacy !== revisionAfterFresh + 1) {
    throw new Error(`Legacy parent sonrası doğrulama başarısız! title=${titleAfterLegacy}, revision=${revisionAfterLegacy}`);
  }
  console.log('   ✅ legacy parent without expectedRevision remains allowed during migration phase');
  console.log(`   ✅ Legacy write başarılı (title='${titleAfterLegacy}', revision=${revisionAfterLegacy}).`);

  // 9. Invalid expectedRevision Validasyon Testleri
  const invalidNegRes = await mockFetch('POST', `/api/sync?code=${revTestCode}`, {
    ...parentAPayload,
    expectedRevision: -1
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': revTestAdminToken });
  if (invalidNegRes.status !== 400 || invalidNegRes.data.error !== 'INVALID_EXPECTED_REVISION') {
    throw new Error(`expectedRevision=-1 için 400 INVALID_EXPECTED_REVISION dönmedi! status=${invalidNegRes.status}`);
  }

  const invalidStrRes = await mockFetch('POST', `/api/sync?code=${revTestCode}`, {
    ...parentAPayload,
    expectedRevision: 'abc'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': revTestAdminToken });
  if (invalidStrRes.status !== 400 || invalidStrRes.data.error !== 'INVALID_EXPECTED_REVISION') {
    throw new Error(`expectedRevision="abc" için 400 INVALID_EXPECTED_REVISION dönmedi! status=${invalidStrRes.status}`);
  }

  const syncAfterInvalid = (await mockFetch('GET', `/api/sync?code=${revTestCode}`)).data;
  if ((syncAfterInvalid.plan?.title || syncAfterInvalid.occurrences?.[0]?.subject) !== 'PARENT_C_LEGACY' || Number(syncAfterInvalid.revision || 0) !== revisionAfterLegacy) {
    throw new Error('Invalid expectedRevision istekleri sunucu durumunu bozdu!');
  }
  console.log('   ✅ Geçersiz expectedRevision (-1, "abc") istekleri 400 INVALID_EXPECTED_REVISION ile engellendi ve server state korundu.\n');

  // 15. Görev 5B2A: Plan & Task Revision Preconditions & Hardening Tests
  console.log('1️⃣5️⃣ Görev 5B2A: Plan & Task Revision Preconditions & Hardening Testleri...');
  const taskRevCode = 'ST-5B2A-2026-TEST-5501';
  const taskRevPair = await mockFetch('POST', '/api/pair', { familyCode: taskRevCode });
  const taskRevAdminToken = taskRevPair.data.adminToken;
  if (!taskRevAdminToken) throw new Error('taskRevAdminToken oluşturulamadı!');

  // Initial setup: create plan with 4 tasks
  const initialTaskPlan = {
    familyCode: taskRevCode,
    plan: { planId: 'plan_5b2a', weekId: '2026-W39', title: '5B2A Initial' },
    tasks: [
      { taskId: 'task_5b2a_1', title: 'Task 1', plannedMinutes: 30 },
      { taskId: 'task_5b2a_2', title: 'Task 2', plannedMinutes: 45 },
      { taskId: 'task_5b2a_3', title: 'Task 3', plannedMinutes: 20 },
      { taskId: 'task_5b2a_4', title: 'Task 4', plannedMinutes: 15 }
    ],
    occurrences: [
      { id: 'occ_5b2a_1', planId: 'task_5b2a_1', subject: 'Task 1', targetDurationMin: 30 },
      { id: 'occ_5b2a_2', planId: 'task_5b2a_2', subject: 'Task 2', targetDurationMin: 45 },
      { id: 'occ_5b2a_3', planId: 'task_5b2a_3', subject: 'Task 3', targetDurationMin: 20 },
      { id: 'occ_5b2a_4', planId: 'task_5b2a_4', subject: 'Task 4', targetDurationMin: 15 }
    ]
  };

  const initTaskRes = await mockFetch('POST', `/api/sync?code=${taskRevCode}`, initialTaskPlan, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': taskRevAdminToken
  });
  if (!initTaskRes.ok) throw new Error(`5B2A initial plan yükleme başarısız: ${JSON.stringify(initTaskRes.data)}`);

  const syncInit = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const rev0 = Number(syncInit.revision || syncInit.meta?.revision || 0);
  console.log(`   Initial 5B2A snapshot alındı (revision=${rev0})`);

  // A: PATCH_TASK fresh success +1
  console.log('   A. PATCH_TASK fresh success (+1)...');
  const patchTaskFreshRes = await mockFetch('POST', `/api/sync?code=${taskRevCode}`, {
    action: 'PATCH_TASK',
    expectedRevision: rev0,
    patchTask: { id: 'occ_5b2a_1', subject: 'Task 1 Fresh Updated', targetDurationMin: 50 }
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (!patchTaskFreshRes.ok || !patchTaskFreshRes.data.success) {
    throw new Error(`PATCH_TASK fresh başarısız: ${JSON.stringify(patchTaskFreshRes.data)}`);
  }
  const taskSyncAfterA = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterA = Number(taskSyncAfterA.revision || taskSyncAfterA.meta?.revision || 0);
  const occ1AfterA = taskSyncAfterA.occurrences?.find(o => o.id === 'occ_5b2a_1');
  if (revAfterA !== rev0 + 1 || occ1AfterA?.subject !== 'Task 1 Fresh Updated' || occ1AfterA?.targetDurationMin !== 50) {
    throw new Error(`Test A doğrulaması başarısız: revision=${revAfterA}, occ1=${JSON.stringify(occ1AfterA)}`);
  }
  console.log(`   ✅ Test A başarılı: PATCH_TASK fresh revision=${revAfterA}, subject='${occ1AfterA.subject}'`);

  // B: PATCH_TASK stale 409, unchanged
  console.log('   B. PATCH_TASK stale 409 (unchanged)...');
  const patchTaskStaleRes = await mockFetch('POST', `/api/sync?code=${taskRevCode}`, {
    action: 'PATCH_TASK',
    expectedRevision: rev0, // stale
    patchTask: { id: 'occ_5b2a_1', subject: 'Task 1 Stale Attempt', targetDurationMin: 99 }
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (patchTaskStaleRes.status !== 409 || patchTaskStaleRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`PATCH_TASK stale 409 dönmedi: ${JSON.stringify(patchTaskStaleRes)}`);
  }
  if (patchTaskStaleRes.data.currentRevision !== revAfterA) {
    throw new Error(`PATCH_TASK stale currentRevision uyuşmuyor: ${patchTaskStaleRes.data.currentRevision}`);
  }
  const taskSyncAfterB = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterB = Number(taskSyncAfterB.revision || taskSyncAfterB.meta?.revision || 0);
  const occ1AfterB = taskSyncAfterB.occurrences?.find(o => o.id === 'occ_5b2a_1');
  if (revAfterB !== revAfterA || occ1AfterB?.subject !== 'Task 1 Fresh Updated') {
    throw new Error(`Test B doğrulaması başarısız: state bozuldu! rev=${revAfterB}`);
  }
  console.log('   ✅ Test B başarılı: PATCH_TASK stale 409 REVISION_CONFLICT döndü, revision ve state değişmedi.');

  // C: DELETE_TASK stale 409, task remains, tombstone absent, revision unchanged
  console.log('   C. DELETE_TASK stale 409 (task remains, tombstone absent, revision unchanged)...');
  const deleteTaskStaleRes = await mockFetch('POST', `/api/sync?code=${taskRevCode}`, {
    action: 'DELETE_TASK',
    expectedRevision: rev0, // stale
    deleteTaskId: 'occ_5b2a_2'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (deleteTaskStaleRes.status !== 409 || deleteTaskStaleRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`DELETE_TASK stale 409 dönmedi: ${JSON.stringify(deleteTaskStaleRes)}`);
  }
  const taskSyncAfterC = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterC = Number(taskSyncAfterC.revision || taskSyncAfterC.meta?.revision || 0);
  const occ2Present = (taskSyncAfterC.occurrences || []).some(o => o.id === 'occ_5b2a_2');
  const tombstone2Present = (taskSyncAfterC.deletedOccurrences || taskSyncAfterC.meta?.tombstones || []).includes('occ_5b2a_2');
  if (revAfterC !== revAfterA || !occ2Present || tombstone2Present) {
    throw new Error(`Test C doğrulaması başarısız: rev=${revAfterC}, occ2Present=${occ2Present}, tombstone2Present=${tombstone2Present}`);
  }
  console.log('   ✅ Test C başarılı: DELETE_TASK stale 409 döndü, task silinmedi, tombstone eklenmedi, revision değişmedi.');

  // D: DELETE_TASK fresh success, removed, +1
  console.log('   D. DELETE_TASK fresh success (removed, +1)...');
  const deleteTaskFreshRes = await mockFetch('POST', `/api/sync?code=${taskRevCode}`, {
    action: 'DELETE_TASK',
    expectedRevision: revAfterA, // fresh
    deleteTaskId: 'occ_5b2a_2'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (!deleteTaskFreshRes.ok) {
    throw new Error(`DELETE_TASK fresh başarısız: ${JSON.stringify(deleteTaskFreshRes.data)}`);
  }
  const taskSyncAfterD = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterD = Number(taskSyncAfterD.revision || taskSyncAfterD.meta?.revision || 0);
  const occ2PresentAfterD = (taskSyncAfterD.occurrences || []).some(o => o.id === 'occ_5b2a_2');
  const tombstone2PresentAfterD = (taskSyncAfterD.deletedOccurrences || taskSyncAfterD.meta?.tombstones || []).includes('occ_5b2a_2');
  if (revAfterD !== revAfterA + 1 || occ2PresentAfterD || !tombstone2PresentAfterD) {
    throw new Error(`Test D doğrulaması başarısız: rev=${revAfterD}, occ2Present=${occ2PresentAfterD}, tombstone=${tombstone2PresentAfterD}`);
  }
  console.log(`   ✅ Test D başarılı: DELETE_TASK fresh silindi, tombstone eklendi, revision=${revAfterD}`);

  // E: handlePlanAndTasks POST/PUT fresh +1, stale 409
  console.log('   E. handlePlanAndTasks POST/PUT fresh +1, stale 409...');
  const v2PutStaleRes = await mockFetch('PUT', `/api/v2/plan?code=${taskRevCode}`, {
    expectedRevision: revAfterA, // stale
    plan: { planId: 'plan_5b2a', title: '5B2A Stale PUT' }
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (v2PutStaleRes.status !== 409 || v2PutStaleRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`v2 PUT stale 409 dönmedi: ${JSON.stringify(v2PutStaleRes)}`);
  }

  const v2PutFreshRes = await mockFetch('PUT', `/api/v2/plan?code=${taskRevCode}`, {
    expectedRevision: revAfterD, // fresh
    plan: { planId: 'plan_5b2a', title: '5B2A Fresh PUT' }
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (!v2PutFreshRes.ok) {
    throw new Error(`v2 PUT fresh başarısız: ${JSON.stringify(v2PutFreshRes.data)}`);
  }
  const taskSyncAfterE = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterE = Number(taskSyncAfterE.revision || taskSyncAfterE.meta?.revision || 0);
  if (revAfterE !== revAfterD + 1 || taskSyncAfterE.plan?.title !== '5B2A Fresh PUT') {
    throw new Error(`Test E doğrulaması başarısız: rev=${revAfterE}, title=${taskSyncAfterE.plan?.title}`);
  }
  console.log(`   ✅ Test E başarılı: handlePlanAndTasks PUT stale 409 engellendi, fresh +1 (revision=${revAfterE})`);

  // F: v2 PATCH stale 409, fresh +1
  console.log('   F. v2 PATCH stale 409, fresh +1...');
  const v2PatchStaleRes = await mockFetch('PATCH', `/api/v2/tasks/occ_5b2a_3?code=${taskRevCode}`, {
    expectedRevision: revAfterD, // stale
    subject: 'Task 3 Stale Patch'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (v2PatchStaleRes.status !== 409 || v2PatchStaleRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`v2 PATCH stale 409 dönmedi: ${JSON.stringify(v2PatchStaleRes)}`);
  }

  const v2Patch404Res = await mockFetch('PATCH', `/api/v2/tasks/occ_non_existent?code=${taskRevCode}`, {
    expectedRevision: revAfterE,
    subject: 'Does Not Exist'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });
  if (v2Patch404Res.status !== 404) {
    throw new Error(`v2 PATCH 404 dönmedi: ${v2Patch404Res.status}`);
  }

  const v2PatchFreshRes = await mockFetch('PATCH', `/api/v2/tasks/occ_5b2a_3?code=${taskRevCode}`, {
    expectedRevision: revAfterE, // fresh
    subject: 'Task 3 Fresh Patched',
    targetDurationMin: 60
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (!v2PatchFreshRes.ok) {
    throw new Error(`v2 PATCH fresh başarısız: ${JSON.stringify(v2PatchFreshRes.data)}`);
  }
  const taskSyncAfterF = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterF = Number(taskSyncAfterF.revision || taskSyncAfterF.meta?.revision || 0);
  const occ3AfterF = taskSyncAfterF.occurrences?.find(o => o.id === 'occ_5b2a_3');
  if (revAfterF !== revAfterE + 1 || occ3AfterF?.subject !== 'Task 3 Fresh Patched' || occ3AfterF?.targetDurationMin !== 60) {
    throw new Error(`Test F doğrulaması başarısız: rev=${revAfterF}, occ3=${JSON.stringify(occ3AfterF)}`);
  }
  console.log(`   ✅ Test F başarılı: v2 PATCH stale 409 ve 404 korundu, fresh +1 (revision=${revAfterF})`);

  // G: v2 DELETE using chosen transport stale 409/no mutation, fresh +1
  console.log('   G. v2 DELETE (?expectedRevision=...) stale 409/no mutation, fresh +1...');
  const v2DeleteStaleRes = await mockFetch('DELETE', `/api/v2/tasks/occ_5b2a_4?code=${taskRevCode}&expectedRevision=${revAfterE}`, null, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': taskRevAdminToken
  });

  if (v2DeleteStaleRes.status !== 409 || v2DeleteStaleRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`v2 DELETE stale 409 dönmedi: ${JSON.stringify(v2DeleteStaleRes)}`);
  }

  const taskSyncAfterGStale = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterGStale = Number(taskSyncAfterGStale.revision || taskSyncAfterGStale.meta?.revision || 0);
  const occ4Present = (taskSyncAfterGStale.occurrences || []).some(o => o.id === 'occ_5b2a_4');
  if (revAfterGStale !== revAfterF || !occ4Present) {
    throw new Error(`Test G stale doğrulaması başarısız: rev=${revAfterGStale}, occ4Present=${occ4Present}`);
  }

  const v2DeleteFreshRes = await mockFetch('DELETE', `/api/v2/tasks/occ_5b2a_4?code=${taskRevCode}&expectedRevision=${revAfterF}`, null, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': taskRevAdminToken
  });

  if (!v2DeleteFreshRes.ok) {
    throw new Error(`v2 DELETE fresh başarısız: ${JSON.stringify(v2DeleteFreshRes.data)}`);
  }
  const taskSyncAfterGFresh = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterGFresh = Number(taskSyncAfterGFresh.revision || taskSyncAfterGFresh.meta?.revision || 0);
  const occ4PresentAfterGFresh = (taskSyncAfterGFresh.occurrences || []).some(o => o.id === 'occ_5b2a_4');
  const tombstone4Present = (taskSyncAfterGFresh.deletedOccurrences || taskSyncAfterGFresh.meta?.tombstones || []).includes('occ_5b2a_4');
  if (revAfterGFresh !== revAfterF + 1 || occ4PresentAfterGFresh || !tombstone4Present) {
    throw new Error(`Test G fresh doğrulaması başarısız: rev=${revAfterGFresh}, occ4Present=${occ4PresentAfterGFresh}, tombstone4=${tombstone4Present}`);
  }
  console.log(`   ✅ Test G başarılı: v2 DELETE query transport stale 409 korundu, fresh +1 (revision=${revAfterGFresh})`);

  // H: one invalid expectedRevision on handlePlanAndTasks -> 400, state unchanged
  console.log('   H. Invalid expectedRevision on handlePlanAndTasks -> 400, state unchanged...');
  const invalidPutRes = await mockFetch('PUT', `/api/v2/plan?code=${taskRevCode}`, {
    expectedRevision: 'not-a-number',
    plan: { title: 'Invalid Plan' }
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (invalidPutRes.status !== 400 || invalidPutRes.data.error !== 'INVALID_EXPECTED_REVISION') {
    throw new Error(`Invalid expectedRevision 400 dönmedi: ${JSON.stringify(invalidPutRes)}`);
  }

  const invalidDeleteRes = await mockFetch('DELETE', `/api/v2/tasks/occ_5b2a_3?code=${taskRevCode}&expectedRevision=-1`, null, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': taskRevAdminToken
  });

  if (invalidDeleteRes.status !== 400 || invalidDeleteRes.data.error !== 'INVALID_EXPECTED_REVISION') {
    throw new Error(`Invalid expectedRevision DELETE 400 dönmedi: ${JSON.stringify(invalidDeleteRes)}`);
  }

  const taskSyncAfterH = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterH = Number(taskSyncAfterH.revision || taskSyncAfterH.meta?.revision || 0);
  if (revAfterH !== revAfterGFresh) {
    throw new Error(`Test H state bozuldu: rev=${revAfterH}, beklenen=${revAfterGFresh}`);
  }
  console.log('   ✅ Test H başarılı: 400 INVALID_EXPECTED_REVISION istekleri sunucu durumunu ve revizyonu değiştirmedi.');

  // I: one legacy no-expectedRevision mutation -> 200; log migration compatibility
  console.log('   I. Legacy no-expectedRevision mutation -> 200 (migration compatibility)...');
  const legacyTaskPatchRes = await mockFetch('PATCH', `/api/v2/tasks/occ_5b2a_3?code=${taskRevCode}`, {
    subject: 'Task 3 Legacy Mutated Without Revision'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': taskRevAdminToken });

  if (!legacyTaskPatchRes.ok) {
    throw new Error(`Legacy mutation başarısız: ${JSON.stringify(legacyTaskPatchRes.data)}`);
  }
  const taskSyncAfterI = (await mockFetch('GET', `/api/sync?code=${taskRevCode}`)).data;
  const revAfterI = Number(taskSyncAfterI.revision || taskSyncAfterI.meta?.revision || 0);
  const occ3AfterI = taskSyncAfterI.occurrences?.find(o => o.id === 'occ_5b2a_3');
  if (revAfterI !== revAfterGFresh + 1 || occ3AfterI?.subject !== 'Task 3 Legacy Mutated Without Revision') {
    throw new Error(`Test I doğrulaması başarısız: rev=${revAfterI}, occ3=${JSON.stringify(occ3AfterI)}`);
  }
  console.log('   ✅ legacy mutation without expectedRevision remains allowed during migration phase');
  console.log(`   ✅ Test I başarılı: Legacy mutation 200 ile uygulandı (revision=${revAfterI}).\n`);

  // 16. Görev 5B2B: Destructive Command Revision Preconditions & Monotonic Semantics Testleri
  console.log('1️⃣6️⃣ Görev 5B2B: Destructive Command Revision Preconditions & Monotonic Semantics Testleri...');
  const cmdRevCode = 'ST-5B2B-2026-TEST-7701';
  const cmdRevPair = await mockFetch('POST', '/api/pair', { familyCode: cmdRevCode });
  const cmdRevAdminToken = cmdRevPair.data.adminToken;
  if (!cmdRevAdminToken) throw new Error('cmdRevAdminToken oluşturulamadı!');

  // Initial setup: parent uploads plan with 2 tasks and quiz
  const initialCmdPlan = {
    familyCode: cmdRevCode,
    plan: { planId: 'plan_5b2b', weekId: '2026-W39', title: '5B2B Initial Plan' },
    tasks: [
      { taskId: 'task_5b2b_1', title: 'Task 1', plannedMinutes: 30 },
      { taskId: 'task_5b2b_2', title: 'Task 2', plannedMinutes: 45 }
    ],
    occurrences: [
      { id: 'occ_5b2b_1', planId: 'task_5b2b_1', subject: 'Math', targetDurationMin: 30 },
      { id: 'occ_5b2b_2', planId: 'task_5b2b_2', subject: 'Physics', targetDurationMin: 45 }
    ],
    quizzes: [
      { quizId: 'quiz_5b2b_1', title: 'Quiz 1', questions: [{ id: 'q1', text: '1+1=?' }], completed: false }
    ]
  };

  const initCmdRes = await mockFetch('POST', `/api/sync?code=${cmdRevCode}`, initialCmdPlan, {
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': cmdRevAdminToken
  });
  if (!initCmdRes.ok) throw new Error(`5B2B initial plan yükleme başarısız: ${JSON.stringify(initCmdRes.data)}`);

  // Child completes task 1 and submits quiz
  await mockFetch('POST', `/api/sync?code=${cmdRevCode}`, {
    senderRole: 'CHILD',
    occurrences: [{ id: 'occ_5b2b_1', completedDurationMin: 30, completedQuestionCount: 15, status: 'WAITING_REVIEW' }],
    sessions: [{ id: 'sess_5b2b_1', occurrenceId: 'occ_5b2b_1', durationMin: 30, isCompleted: true, updatedAt: 1000 }],
    screenshots: [{ id: 'ss_5b2b_1', sessionId: 'sess_5b2b_1', imageUrl: 'data:image/webp;base64,cmd_test' }],
    quizzes: [{ quizId: 'quiz_5b2b_1', completed: true, studentAnswers: { q1: '2' }, correctCount: 1 }]
  }, { 'X-Sender-Role': 'CHILD' });

  const syncInitCmd = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const cmdRev0 = Number(syncInitCmd.revision || syncInitCmd.meta?.revision || 0);
  const cmdResetAt0 = Number(syncInitCmd.resetAt || syncInitCmd.meta?.resetAt || 0);
  console.log(`   Initial 5B2B snapshot alındı (revision=${cmdRev0})`);

  // Test H: Invalid expectedRevision on RESET -> 400 and no mutation
  console.log('   H. Invalid expectedRevision -> 400 and no state mutation...');
  const invalidResetRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'RESET_ALL_PROGRESS',
    expectedRevision: -1
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (invalidResetRes.status !== 400 || invalidResetRes.data.error !== 'INVALID_EXPECTED_REVISION') {
    throw new Error(`RESET invalid expectedRevision 400 dönmedi: ${JSON.stringify(invalidResetRes)}`);
  }

  const syncAfterInvalidReset = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  if (Number(syncAfterInvalidReset.revision || 0) !== cmdRev0 || syncAfterInvalidReset.sessions?.length !== 1) {
    throw new Error('Test H: 400 invalid expectedRevision sunucu durumunu bozdu!');
  }
  console.log('   ✅ Test H başarılı: 400 INVALID_EXPECTED_REVISION no state mutation.');

  // Test B: RESET stale expectedRevision=cmdRev0-1 -> 409 REVISION_CONFLICT, state unchanged
  console.log('   B. RESET stale expectedRevision -> 409 (state/resetAt/revision unchanged)...');
  const staleResetRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'RESET_ALL_PROGRESS',
    expectedRevision: cmdRev0 > 0 ? cmdRev0 - 1 : 999
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (staleResetRes.status !== 409 || staleResetRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`RESET stale 409 dönmedi: ${JSON.stringify(staleResetRes)}`);
  }
  if (staleResetRes.data.currentRevision !== cmdRev0) {
    throw new Error(`RESET stale currentRevision uyuşmuyor: ${staleResetRes.data.currentRevision}`);
  }

  const syncAfterStaleReset = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const revAfterStaleReset = Number(syncAfterStaleReset.revision || syncAfterStaleReset.meta?.revision || 0);
  const resetAtAfterStaleReset = Number(syncAfterStaleReset.resetAt || syncAfterStaleReset.meta?.resetAt || 0);
  const occ1AfterStaleReset = syncAfterStaleReset.occurrences?.find(o => o.id === 'occ_5b2b_1');
  if (revAfterStaleReset !== cmdRev0 || resetAtAfterStaleReset !== cmdResetAt0 || occ1AfterStaleReset?.completedDurationMin !== 30 || syncAfterStaleReset.sessions?.length !== 1) {
    throw new Error('Test B doğrulaması başarısız: stale RESET durumu veya revizyonu değiştirdi!');
  }
  console.log(`   ✅ Test B başarılı: RESET stale 409 döndü, progress/state/resetAt/revision değişmedi.`);

  // Test A: RESET fresh expectedRevision=cmdRev0 -> success, revision=cmdRev0+1
  console.log('   A. RESET fresh expectedRevision -> success, revision +1...');
  const freshResetRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'RESET_ALL_PROGRESS',
    expectedRevision: cmdRev0
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (!freshResetRes.ok) {
    throw new Error(`RESET fresh başarısız: ${JSON.stringify(freshResetRes.data)}`);
  }

  const syncAfterFreshReset = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const cmdRevAfterA = Number(syncAfterFreshReset.revision || syncAfterFreshReset.meta?.revision || 0);
  const cmdResetAtAfterA = Number(syncAfterFreshReset.resetAt || syncAfterFreshReset.meta?.resetAt || 0);
  const cmdOcc1AfterA = syncAfterFreshReset.occurrences?.find(o => o.id === 'occ_5b2b_1');
  if (cmdRevAfterA !== cmdRev0 + 1 || cmdResetAtAfterA <= cmdResetAt0 || cmdOcc1AfterA?.completedDurationMin !== 0 || syncAfterFreshReset.sessions?.length !== 0) {
    throw new Error(`Test A doğrulaması başarısız: cmdRevAfterA=${cmdRevAfterA}, expected=${cmdRev0 + 1}, occ1=${JSON.stringify(cmdOcc1AfterA)}`);
  }
  console.log(`   ✅ Test A başarılı: RESET fresh tamamlandı, resetAt güncellendi, revision=${cmdRevAfterA}`);

  // Test D: stale WIPE -> 409, no state deletion, revision unchanged
  console.log('   D. WIPE stale expectedRevision -> 409 (no deletion/snapshot change/revision change)...');
  const staleWipeRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'WIPE',
    expectedRevision: cmdRev0 // stale (current is cmdRevAfterA)
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (staleWipeRes.status !== 409 || staleWipeRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`WIPE stale 409 dönmedi: ${JSON.stringify(staleWipeRes)}`);
  }
  if (staleWipeRes.data.currentRevision !== cmdRevAfterA) {
    throw new Error(`WIPE stale currentRevision uyuşmuyor: ${staleWipeRes.data.currentRevision}`);
  }

  const cmdSyncAfterStaleWipe = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const cmdRevAfterStaleWipe = Number(cmdSyncAfterStaleWipe.revision || cmdSyncAfterStaleWipe.meta?.revision || 0);
  if (cmdRevAfterStaleWipe !== cmdRevAfterA || cmdSyncAfterStaleWipe.tasks?.length !== 2) {
    throw new Error('Test D doğrulaması başarısız: stale WIPE durumu veya revizyonu değiştirdi!');
  }
  console.log(`   ✅ Test D başarılı: stale WIPE 409 engellendi, plan/state/revision korundu.`);

  // Test C: WIPE fresh expectedRevision=cmdRevAfterA -> success, revision=cmdRevAfterA+1 (NOT 1)
  console.log('   C. WIPE fresh expectedRevision -> success, revision +1 (not reset to 1)...');
  const cmdN = cmdRevAfterA;
  const freshWipeRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'WIPE',
    expectedRevision: cmdN
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (!freshWipeRes.ok) {
    throw new Error(`WIPE fresh başarısız: ${JSON.stringify(freshWipeRes.data)}`);
  }

  const syncAfterFreshWipe = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const cmdRevAfterWipe = Number(syncAfterFreshWipe.revision || syncAfterFreshWipe.meta?.revision || 0);
  if (cmdRevAfterWipe !== cmdN + 1 || cmdRevAfterWipe <= 1 || (syncAfterFreshWipe.tasks || []).length !== 0) {
    throw new Error(`Test C doğrulaması başarısız: cmdRevAfterWipe=${cmdRevAfterWipe}, expected=${cmdN + 1}`);
  }
  console.log(`   ✅ Test C başarılı: WIPE fresh tamamlandı, tasks silindi, revision=${cmdRevAfterWipe} (N+1, 1'e sıfırlanmadı)`);

  // Test F: stale RESTORE -> 409, wiped state and revision unchanged
  console.log('   F. RESTORE stale expectedRevision -> 409 (wiped state and revision unchanged)...');
  const staleRestoreRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'RESTORE',
    expectedRevision: cmdN // stale (current is cmdRevAfterWipe = N+1)
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (staleRestoreRes.status !== 409 || staleRestoreRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`RESTORE stale 409 dönmedi: ${JSON.stringify(staleRestoreRes)}`);
  }
  if (staleRestoreRes.data.currentRevision !== cmdRevAfterWipe) {
    throw new Error(`RESTORE stale currentRevision uyuşmuyor: ${staleRestoreRes.data.currentRevision}`);
  }

  const cmdSyncAfterStaleRestore = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const cmdRevAfterStaleRestore = Number(cmdSyncAfterStaleRestore.revision || cmdSyncAfterStaleRestore.meta?.revision || 0);
  if (cmdRevAfterStaleRestore !== cmdRevAfterWipe || (cmdSyncAfterStaleRestore.tasks || []).length !== 0) {
    throw new Error('Test F doğrulaması başarısız: stale RESTORE durumu veya revizyonu değiştirdi!');
  }
  console.log(`   ✅ Test F başarılı: stale RESTORE 409 engellendi, wiped durum ve revision korundu.`);

  // Test E & G: RESTORE fresh -> success, content restored, monotonic chain pre-wipe N -> wipe N+1 -> restore N+2
  console.log('   E & G. RESTORE fresh from wipe state & Monotonic Chain (N -> N+1 -> N+2)...');
  const freshRestoreRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'RESTORE',
    expectedRevision: cmdRevAfterWipe // N + 1
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (!freshRestoreRes.ok) {
    throw new Error(`RESTORE fresh başarısız: ${JSON.stringify(freshRestoreRes.data)}`);
  }

  const syncAfterFreshRestore = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const cmdRevAfterRestore = Number(syncAfterFreshRestore.revision || syncAfterFreshRestore.meta?.revision || 0);
  if (cmdRevAfterRestore !== cmdN + 2) {
    throw new Error(`Monotonic Chain doğrulaması başarısız: pre-wipe=${cmdN}, wipe=${cmdN+1}, restore=${cmdRevAfterRestore} (beklenen: ${cmdN+2})`);
  }
  if ((syncAfterFreshRestore.tasks || []).length !== 2 || (syncAfterFreshRestore.occurrences || []).length !== 2) {
    throw new Error(`Test E içerik doğrulaması başarısız: tasks=${syncAfterFreshRestore.tasks?.length}`);
  }
  console.log(`   ✅ Test E & G başarılı: Monotonic chain doğrulandı (pre-wipe=${cmdN} -> wipe=${cmdN+1} -> restore=${cmdRevAfterRestore}), içerik geri yüklendi.`);

  // Missing snapshot 404 test does not bump revision
  const noSnapPair = await mockFetch('POST', '/api/pair', { familyCode: 'ST-5B2B-NOSN-AP01-0001' });
  const pre404Sync = (await mockFetch('GET', `/api/sync?code=ST-5B2B-NOSN-AP01-0001`)).data;
  const pre404Rev = Number(pre404Sync.revision || 0);
  const noSnapRes = await mockFetch('POST', `/api/v2/commands?code=ST-5B2B-NOSN-AP01-0001`, {
    action: 'RESTORE'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': noSnapPair.data.adminToken });
  if (noSnapRes.status !== 404) {
    throw new Error(`Missing snapshot 404 dönmedi: ${noSnapRes.status}`);
  }
  const post404Sync = (await mockFetch('GET', `/api/sync?code=ST-5B2B-NOSN-AP01-0001`)).data;
  if (Number(post404Sync.revision || 0) !== pre404Rev) {
    throw new Error('Missing snapshot 404 revizyonu değiştirdi!');
  }
  console.log('   ✅ Missing snapshot 404 döndü ve revizyonu değiştirmedi.');

  // Test I: Legacy destructive command without expectedRevision succeeds during migration phase
  console.log('   I. Legacy destructive command without expectedRevision -> 200 (migration compatibility)...');
  const legacyResetRes = await mockFetch('POST', `/api/v2/commands?code=${cmdRevCode}`, {
    action: 'RESET_ALL_PROGRESS'
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });

  if (!legacyResetRes.ok) {
    throw new Error(`Legacy RESET başarısız: ${JSON.stringify(legacyResetRes.data)}`);
  }
  const syncAfterLegacyReset = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const cmdRevAfterLegacyReset = Number(syncAfterLegacyReset.revision || syncAfterLegacyReset.meta?.revision || 0);
  if (cmdRevAfterLegacyReset !== cmdRevAfterRestore + 1) {
    throw new Error(`Legacy RESET revizyon doğrulaması başarısız: rev=${cmdRevAfterLegacyReset}, beklenen=${cmdRevAfterRestore + 1}`);
  }
  console.log('   ✅ legacy destructive command without expectedRevision remains allowed during migration phase');
  console.log(`   ✅ Test I başarılı: Legacy destructive command 200 ile uygulandı (revision=${cmdRevAfterLegacyReset}).\n`);

  // Unified POST /api/sync destructive command revision parity tests
  console.log('   K. Unified POST /api/sync destructive actions revision parity tests...');
  const uniStaleResetRes = await mockFetch('POST', `/api/sync?code=${cmdRevCode}`, {
    senderRole: 'PARENT',
    action: 'RESET',
    expectedRevision: cmdRevAfterLegacyReset - 1
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });
  if (uniStaleResetRes.status !== 409 || uniStaleResetRes.data.error !== 'REVISION_CONFLICT') {
    throw new Error(`Unified POST /api/sync RESET stale 409 dönmedi: ${JSON.stringify(uniStaleResetRes)}`);
  }

  const uniFreshResetRes = await mockFetch('POST', `/api/sync?code=${cmdRevCode}`, {
    senderRole: 'PARENT',
    action: 'RESET',
    expectedRevision: cmdRevAfterLegacyReset
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });
  if (!uniFreshResetRes.ok) {
    throw new Error(`Unified POST /api/sync RESET fresh başarısız: ${JSON.stringify(uniFreshResetRes.data)}`);
  }
  const syncAfterUniReset = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const revAfterUniReset = Number(syncAfterUniReset.revision || syncAfterUniReset.meta?.revision || 0);
  if (revAfterUniReset !== cmdRevAfterLegacyReset + 1) {
    throw new Error(`Unified RESET revizyon başarısız: rev=${revAfterUniReset}`);
  }

  const uniFreshWipeRes = await mockFetch('POST', `/api/sync?code=${cmdRevCode}`, {
    senderRole: 'PARENT',
    action: 'WIPE',
    expectedRevision: revAfterUniReset
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });
  if (!uniFreshWipeRes.ok) {
    throw new Error(`Unified POST /api/sync WIPE fresh başarısız: ${JSON.stringify(uniFreshWipeRes.data)}`);
  }
  const syncAfterUniWipe = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const revAfterUniWipe = Number(syncAfterUniWipe.revision || syncAfterUniWipe.meta?.revision || 0);
  if (revAfterUniWipe !== revAfterUniReset + 1) {
    throw new Error(`Unified WIPE revizyon başarısız: rev=${revAfterUniWipe}`);
  }

  const uniFreshRestoreRes = await mockFetch('POST', `/api/sync?code=${cmdRevCode}`, {
    senderRole: 'PARENT',
    action: 'RESTORE',
    expectedRevision: revAfterUniWipe
  }, { 'X-Sender-Role': 'PARENT', 'X-Admin-Token': cmdRevAdminToken });
  if (!uniFreshRestoreRes.ok) {
    throw new Error(`Unified POST /api/sync RESTORE fresh başarısız: ${JSON.stringify(uniFreshRestoreRes.data)}`);
  }
  const syncAfterUniRestore = (await mockFetch('GET', `/api/sync?code=${cmdRevCode}`)).data;
  const revAfterUniRestore = Number(syncAfterUniRestore.revision || syncAfterUniRestore.meta?.revision || 0);
  if (revAfterUniRestore !== revAfterUniWipe + 1) {
    throw new Error(`Unified RESTORE monotonic revizyon başarısız: rev=${revAfterUniRestore}`);
  }
  console.log(`   ✅ Unified POST /api/sync destructive actions revision parity %100 başarılı (final revision=${revAfterUniRestore}).\n`);

  console.log('🎉 ========================================================');
  console.log('🎉 TÜM v2.0 SEGREGATED & COMMAND PATTERN TESTLERİ BAŞARIYLA GEÇTİ!');
  console.log('🎉 ========================================================');
}

runTest().catch((err) => {
  console.error('❌ Test sırasında hata:', err);
  process.exit(1);
});
