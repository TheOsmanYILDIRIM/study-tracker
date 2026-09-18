# StudyTracker Parenting & AI Cloud Administration CLI

Node.js tabanlı, sıfır harici bağımlılıklı (zero-dependency) StudyTracker Cloudflare KV Yönetim ve Ebeveyn/AI Otomasyon Aracı.

## 🚀 Kurulum & Erişim
CLI aracı `/data/data/com.termux/files/usr/bin/studytracker-cli` ve `study-cli` olarak sistem PATH'ine bağlanmıştır.

```bash
# Durumu kontrol et
studytracker-cli status

# AI veya otomasyon için ham JSON çıktısı
studytracker-cli status --json
```

## 📋 Temel Komutlar

### 1. Durum Gözlemleme
```bash
studytracker-cli status [--code <ST-XXXX>] [--json]
```

### 2. Haftalık Plan Yönetimi (Veli / AI)
```bash
# Mevcut planı DSL formatında göster
studytracker-cli plan show

# Dosyadan yeni plan yükle (Veli rolüyle Cloudflare KV'ye yazar)
studytracker-cli plan apply --file sample_plan_w38.txt

# Doğrudan DSL metni ile plan tanımla
studytracker-cli plan set "HAFTA = 2026-W38 ... [DERSLER] ... [GUNLER] ..."
```

### 3. Öğrenci Çalışmalarını İnceleme & Onaylama
```bash
# Onay bekleyen oturumları listele
studytracker-cli review list

# Öğrencinin tamamladığı dersi/oturumu onayla (Öğrenci uygulamasında takımyıldızı parlar)
studytracker-cli approve <sessionId|occKey> [--note "Tebrikler!"]

# Öğrencinin çalışmasını reddet ve yönerge notu gönder
studytracker-cli reject <sessionId|occKey> --note "Eksik soru çözülmüş, tekrar et."
```

### 4. Tekil Ders / Görev İşlemleri
```bash
# Yeni ders ekle
studytracker-cli task add --title "Matematik Soru" --day "MON" --min 35 --video "https://..."

# Dersi düzenle
studytracker-cli task edit 2026-W38_MON_mat_1 --title "Yeni Başlık" --min 40

# Dersi sil
studytracker-cli task delete 2026-W38_MON_mat_1
```

### 5. Sıfırlama & Yapılandırma
```bash
# Sadece ilerlemeyi sıfırla (Plan korunur)
studytracker-cli reset

# Tüm planı, dersleri ve testleri komple sil (Temiz masa)
studytracker-cli reset --full

# Varsayılan aile kodunu değiştir
studytracker-cli config set-code ST-4821
```
