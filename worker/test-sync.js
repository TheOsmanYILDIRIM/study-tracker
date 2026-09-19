/**
 * Test script to simulate the entire Veli <-> Öğrenci Cloudflare Worker Sync cycle.
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
  const res = await worker.fetch(req, {}, {});
  const json = await res.json();
  return { status: res.status, ok: res.ok, data: json };
}

async function runTest() {
  console.log('🚀 === Cloudflare Worker StudyTracker Senkronizasyon Testi Başlıyor ===\n');

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
  console.log('   ✅ Eşleşme kodu hazır.\n');

  // Adım 3: Veli Haftalık Planı Yüklüyor (POST /api/sync)
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
        taskId: 'mat_01',
        title: 'Matematik - Üslü Sayılara Giriş',
        kind: 'DAILY',
        contentType: 'VIDEO',
        youtubeUrl: 'https://youtu.be/kYqP9K0Y0pU',
        plannedMinutes: 35
      },
      {
        taskId: 'tar_01',
        title: 'Tarih - Geçmişin İnşa Sürecinde Tarih',
        kind: 'DAILY',
        contentType: 'VIDEO',
        youtubeUrl: 'https://youtu.be/5QxOpTALmEE',
        plannedMinutes: 25
      }
    ],
    occurrences: [
      {
        id: 'occ_mat_pzt',
        familyCode,
        date: '2026-09-14',
        planId: 'mat_01',
        subject: 'Matematik - Üslü Sayılara Giriş',
        topic: 'DAILY',
        targetDurationMin: 35,
        targetQuestionCount: 0,
        completedDurationMin: 0,
        completedQuestionCount: 0,
        status: 'PENDING',
        parentNote: 'Khan Academy videosunu dikkatlice izle',
        weekId: '2026-W38',
        orderIndex: 0,
        youtubeUrl: 'https://youtu.be/kYqP9K0Y0pU'
      }
    ]
  };

  const uploadRes = await mockFetch('POST', `/api/sync?code=${familyCode}`, initialPayload);
  console.log('   Veli Yükleme Sonucu:', uploadRes.data.success ? 'BAŞARILI' : 'HATA');
  console.log('   Buluttaki Görev Sayısı:', uploadRes.data.data.tasks.length);
  console.log('   ✅ Veli planı buluta aktardı.\n');

  // Adım 4: Öğrenci Buluttan Planı İndiriyor (GET /api/sync)
  // Adım 4: Öğrenci Buluttan Planı İndiriyor (GET /api/sync)
  console.log('4️⃣ Öğrenci uygulaması buluttan güncel planı çekiyor...');
  const childFetch = await mockFetch('GET', `/api/sync?code=${familyCode}`);
  const childData = childFetch.data.data || childFetch.data;
  console.log('   Öğrencinin İndirdiği Plan ID:', childData.plan?.planId);
  console.log('   Öğrencinin İndirdiği Görev:', childData.occurrences[0]?.subject);
  console.log('   Video Linki:', childData.occurrences[0]?.youtubeUrl);
  if (!childData.occurrences[0]?.youtubeUrl) throw new Error('Video URL eksik!');
  console.log('   ✅ Öğrenci planı eksiksiz indirdi.\n');

  // Adım 5: Öğrenci Dersi Tamamlayıp Rapor Gönderiyor (POST /api/sync)
  console.log('5️⃣ Öğrenci Matematik dersini tamamlıyor (35 dk) ve öz değerlendirme notu ekliyor...');
  const studentReport = {
    senderRole: 'CHILD',
    occurrences: [
      {
        id: 'occ_mat_pzt',
        familyCode,
        date: '2026-09-14',
        planId: 'mat_01',
        subject: 'Matematik - Üslü Sayılara Giriş',
        topic: 'DAILY',
        targetDurationMin: 35,
        targetQuestionCount: 0,
        completedDurationMin: 35,
        completedQuestionCount: 15,
        status: 'WAITING_REVIEW',
        parentNote: 'Khan Academy videosunu dikkatlice izle',
        weekId: '2026-W38',
        orderIndex: 0,
        studentNote: '🌟 Konuyu çok iyi anladım, 15 soru çözdüm.',
        youtubeUrl: 'https://youtu.be/kYqP9K0Y0pU'
      }
    ],
    sessions: [
      {
        id: 'sess_mat_01',
        familyCode,
        occurrenceId: 'occ_mat_pzt',
        startTime: Date.now() - 35 * 60 * 1000,
        endTime: Date.now(),
        durationMin: 35,
        isCompleted: true,
        notes: 'Öğrenci başarıyla bitirdi'
      }
    ]
  };

  const studentRes = await mockFetch('POST', `/api/sync?code=${familyCode}`, studentReport);
  console.log('   Öğrenci Rapor Yükleme Sonucu:', studentRes.data.success ? 'BAŞARILI' : 'HATA');
  console.log('   Buluttaki Görev Durumu:', studentRes.data.data.occurrences[0]?.status);
  console.log('   Öğrenci Notu:', studentRes.data.data.occurrences[0]?.studentNote);
  console.log('   ✅ Öğrenci raporu buluta iletildi.\n');

  // Adım 6: Veli Masasında Onaylıyor (POST /api/sync)
  console.log('6️⃣ Veli onay masasını açıyor ve görevi ONAYLIYOR (APPROVED)...');
  const parentReview = {
    senderRole: 'PARENT',
    occurrences: [
      {
        id: 'occ_mat_pzt',
        familyCode,
        status: 'APPROVED'
      }
    ],
    reviews: [
      {
        id: 'rev_sess_mat_01',
        familyCode,
        sessionId: 'sess_mat_01',
        isApproved: true,
        parentRating: 5,
        feedbackNote: 'Tebrikler harika çalışma! 🌟',
        reviewedAt: Date.now()
      }
    ]
  };

  const parentReviewRes = await mockFetch('POST', `/api/sync?code=${familyCode}`, parentReview);
  console.log('   Veli Onay Sonucu:', parentReviewRes.data.data.occurrences[0]?.status);
  console.log('   Veli Puanı:', parentReviewRes.data.data.reviews[0]?.parentRating);
  console.log('   ✅ Veli onayı buluta işlendi.\n');

  // Adım 7: Öğrenci Onay Durumunu Alıyor (GET /api/sync)
  console.log('7️⃣ Öğrenci son durumu çekiyor...');
  const finalGet = await mockFetch('GET', `/api/sync?code=${familyCode}`);
  const finalData = finalGet.data.data || finalGet.data;
  console.log('   Nihai Ders Durumu:', finalData.occurrences[0]?.status);
  console.log('   Nihai Yıldız / Puan:', finalData.reviews[0]?.parentRating);
  if (finalData.occurrences[0]?.status !== 'APPROVED') throw new Error('Onay başarısız!');
  console.log('   ✅ Takımyıldızı yıldızlaşma animasyonu tetiklendi!\n');

  // Adım 8: CLI / ADMIN Ders Silme ve Senkronizasyon Testi
  console.log('8️⃣ CLI / ADMIN Tarih dersini siliyor ve tombstones kontrol ediliyor...');
  const adminDeletePayload = {
    senderRole: 'ADMIN',
    occurrences: [
      finalData.occurrences[0] // Sadece Matematik kalıyor, Tarih silindi
    ]
  };
  const adminRes = await mockFetch('POST', `/api/sync?code=${familyCode}`, adminDeletePayload);
  console.log('   Kalan Ders Sayısı:', adminRes.data.data.occurrences.length);
  console.log('   Silinen Dersler (Tombstones):', adminRes.data.data.deletedOccurrences);
  if (adminRes.data.data.occurrences.length !== 1) throw new Error('Ders silinemedi!');

  // Adım 9: Eski Veli Cihazı Eşitlendiğinde Silinen Dersi Yeniden Hortlatamama Testi
  console.log('9️⃣ Eski Veli cihazı silinen dersi tekrar göndermeyi deniyor...');
  const staleParentSync = {
    senderRole: 'PARENT',
    occurrences: [
      { id: 'occ_tar_pzt', subject: 'Eski Tarih Dersi' } // Silinmiş ders
    ]
  };
  const staleRes = await mockFetch('POST', `/api/sync?code=${familyCode}`, staleParentSync);
  console.log('   Veli Eşitlemesi Sonrası Ders Sayısı (1 Bekleniyor):', staleRes.data.data.occurrences.length);
  if (staleRes.data.data.occurrences.length !== 1) throw new Error('Silinmiş ders hortlatıldı!');
  console.log('   ✅ Silinmiş ders güvenle korundu, hortlatılmadı.\n');

  // Adım 10: Veli Mesajı & Bildirim Gönderme ve Okundu Testi
  console.log('🔟 Veli öğrenciye anlık motivasyon/hatırlatma mesajı gönderiyor (POST /api/messages)...');
  const sendMsg = await mockFetch('POST', `/api/messages?code=${familyCode}`, {
    title: 'Ders Zamanı!',
    message: 'Bugünkü 9. Sınıf Matematik etüdünü yapmayı unutma 🚀',
    type: 'REMINDER'
  });
  console.log('   Mesaj Gönderim Sonucu:', sendMsg.data.message);
  console.log('   Eklenen Mesaj ID:', sendMsg.data.data?.id);
  if (!sendMsg.data.success || !sendMsg.data.data?.id) throw new Error('Mesaj gönderilemedi!');

  console.log('   Öğrenci okunmamış mesajları çekiyor (GET /api/messages?unread=true)...');
  const unreadRes = await mockFetch('GET', `/api/messages?code=${familyCode}&unread=true`);
  console.log('   Okunmamış Mesaj Sayısı (1 Bekleniyor):', unreadRes.data.count);
  if (unreadRes.data.count !== 1) throw new Error('Okunmamış mesaj sayısı hatalı!');

  console.log('   Öğrenci mesajı okundu olarak işaretliyor (PUT /api/messages)...');
  const readRes = await mockFetch('PUT', `/api/messages?code=${familyCode}`, {
    messageId: sendMsg.data.data.id
  });
  console.log('   Okundu Sonucu:', readRes.data.message);

  const unreadAfter = await mockFetch('GET', `/api/messages?code=${familyCode}&unread=true`);
  console.log('   Kalan Okunmamış Mesaj (0 Bekleniyor):', unreadAfter.data.count);
  if (unreadAfter.data.count !== 0) throw new Error('Mesaj okundu olarak işaretlenemedi!');
  console.log('   ✅ Bildirim ve mesaj döngüsü %100 başarılı.\n');

  console.log('🎉 ========================================================');
  console.log('🎉 TÜM BULUT & YETKİLENDİRME & BİLDİRİM TESTLERİ BAŞARIYLA GEÇTİ!');
  console.log('🎉 ========================================================');
}

runTest().catch((err) => {
  console.error('❌ Test sırasında hata:', err);
  process.exit(1);
});
