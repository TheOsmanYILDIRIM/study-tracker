# 📱 StudyTracker — Öğrenci & Veli Çalışma ve Denetim Ekosistemi

StudyTracker, lise öğrencileri için planlı ders çalışma, gerçek zamanlı kanıt doğrulama, soru çözümü takibi ve veli onay süreçlerini yöneten modern bir Android ve bulut senkronizasyon platformudur.

---

## 🏛️ Kardeş Proje ve Müfredat Deposu (Curriculum Source of Truth)

Bu projenin kullandığı MEB Türkiye Yüzyılı Maarif Modeli (TYMM) 9. Sınıf yıllık zümre planları, video rehberleri ve Anki desteleri açık müfredat depomuzda barındırılmaktadır:

> 🔗 **Müfredat Deposu:** [https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi](https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi)  
> 📁 **Depo İçi Kılavuzlar:** [`content/curriculum/`](content/curriculum/)  
> 📖 **Kitap Bağlantıları:** [`content/curriculum/meb_kitap_baglantilari.md`](content/curriculum/meb_kitap_baglantilari.md) (Resmî MEB PDF'leri doğrudan indirme bağlantıları)

---

## 🏗️ Sistem Mimarisi ve Bileşenler

1. **Android İki Flavor Modeli:**
   - **Öğrenci Uygulaması (`com.studytracker.child`):** Görev listesi, zamanlayıcı, net çalışma süresi sayacı, kanıt ekran görüntüleri ve interaktif Maarif Modeli testleri.
   - **Veli Uygulaması (`com.studytracker.parent`):** Plan oluşturma, çalışma onaylama/reddetme, test ekleme ve admin token yönetimi.
2. **Cloudflare Worker & KV Senkronizasyonu (`worker/`):**
   - Multi-tenant sharded senkronizasyon motoru.
   - Eventual consistency ve idempotency güvencesi.
3. **Müfredat Kataloğu (`content/`):**
   - 9 Temel ders, 35 ünite, 114 öğrenme öğesi, 35 deterministik QUIZ ve doğrulanmış video kayıtları (`content/9-sinif-v2-catalog.json`).
4. **StudyTracker CLI (`cli/`):**
   - Plan derleme, içerik paketleme, katalog doğrulama ve senkronizasyon kontrol araçları.

---

## 📋 Hızlı Başlangıç

### Müfredat Kataloğunu Doğrulama
```bash
node cli/bin/studytracker.js validate
```

### Cloudflare Worker Dağıtımı
```bash
cd worker && npm run deploy
```

### Android APK Derleme
Proje Android CI/CD iş akışları ile GitHub Actions üzerinde derlenmektedir:
`.github/workflows/build-apk.yml`
