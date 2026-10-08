# Matematik 9 — sıfırdan içerik planı (transkript kanıtlı hazırlık)

Durum: **Kaynaklar incelendi, plan hazır; canlı V2 kataloğu ve öğrenci ilerlemesi değiştirilmedi.**

## Kaynak doğrulaması

Kullanıcının yüklediği `9.SINIF VİDEO DERS KİTABI KONU ANLATIM.zip` arşivi incelendi: İlyas Güneş/Nurtaç Kozak karışık 60 video için 60 Türkçe `.vtt` altyazı; eksik indeks yok. 64.720 zaman kodlu konuşma bloğu ve 3.566.940 normalize karakter çıkarıldı. Metinler otomatik altyazı kaynaklıdır; matematiksel sembollerde ASR hataları olabilir. Doğrulama: [LiseDers transcript_evidence.json](https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi/blob/main/data/video_playlists/PLSYiXUktJiZeqUJyNFUgFHwOUNydbC-II/transcript_evidence.json).

Tam transkriptler GitHub'ın herkese açık kaynak dosyalarına kopyalanmaz; ZIP/normalize paket ayrı girdi olarak tutulur. Yeniden üretilebilir import: [LiseDers `scripts/import_ytdlnis_subtitles.py`](https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi/blob/main/scripts/import_ytdlnis_subtitles.py) `--complete-index-archive` ile 60/60 indeks doğrulaması. Çıktıdaki `transcripts/manifest.json` ve zaman kodlu `*.cues.json` dosyaları analiz için kullanılır.

## Müfredat ve video mimarisi

Kaynak playlist sırası korunur. `content/math9/playlist_course_plan.json` içindeki 16 ders grubu ve 60 sabit video ID'si temel alınır. Aşağıdaki 8 tema, mevcut transcript anahtar sözcük sayımlarıyla kontrol edildi:

| Tema | Playlist indeksleri | Video sayısı | Zaman kodlu konuşma blokları | Doğrulama sinyali |
|---|---:|---:|---:|---|
| Üslü Sayılar | 1–7 | 7 | 5.752 | üslü: 372 |
| Köklü Sayılar | 8–11 | 4 | 3.068 | köklü: 150 |
| Nicelikler ve Değişimler | 12–25 | 14 | 16.202 | fonksiyon: 2.749, doğrusal: 951, mutlak değer: 280 |
| Geometrik Şekiller | 26–38 | 13 | 14.486 | açı: 2.390, üçgen: 1.034 |
| Algoritma ve Bilişim | 39–45 | 7 | 8.122 | algoritma: 286 |
| İstatistiksel Araştırma Süreci | 46–54 | 9 | 9.294 | istatistik: 900 |
| Veriden Olasılığa | 55–59 | 5 | 3.150 | olasılık: 270 |
| Genel Tekrar | 60 | 1 | 4.646 | çok konulu tekrar |

Sözcük sıklığı **alt kazanım kanıtı değildir**. Özellikle 12–25 arasındaki fonksiyon serisini doğrusal/mutlak değer/eşitsizlik derslerine ayırmak için her videonun zaman damgalı içeriği ve MEB kazanımı ayrı doğrulanacak.

## Uygulama aşamaları

1. **Kaynak kilidi:** 60 video ID'si, sırası, öğretmen, URL, transkript SHA/fingerprint, altyazı dili ve hata raporu doğrulanır. Ham VTT yalnız kontrollü girdi/artifact; uygulama kataloglarına tam transkript gömülmez.
2. **Öğrenme tasarımı:** Her video için alt konu, önceki bilgi, öğrenme hedefi, 2–5 dakikalık ölçme noktası, konu kazanımı ve zaman aralığı oluşturulur. Matematik sembolleri ve ASR hataları denetlenir. Yanlışlanamayan otomatik eşleşmeler `needs_review` kalır.
3. **V2 modüler yeni sürüm:** `course_mat_9` üst ders kimliği korunabilir; ancak **yeni içerik sürümünün lesson/item kimlikleri eski kimliklerle çakışmamalı**. Eski matematik lesson/item/quiz kaynakları silinmeden sürümlü arşivlenir. Yeni akışta ders grubu → VIDEO → içerikle uyumlu mikro QUIZ → ilerleme geçişi kurulur. Quizler yeni transkript kanıtına ve zaman aralığına bağlanır.
4. **Katalog doğrulama:** Derleyici, kaynak ID referansları, 60/60 video bütünlüğü, her yeni VIDEO'nun gerçek URL'si, QUIZ kanıtı, müfredat kaynakları, tekrarlı build idempotence, offline/KV senkronizasyon test edilir.
5. **Geçiş ve dağıtım:** Eski öğrenci tamamlanmaları **yeni videoları izlemiş sayılmaz**. Tarihsel ilerleme/deneme kayıtları korunur, yeni içerik sürümü ayrı ilerler. Canlı Cloudflare KV seed/diff/apply, geri alma ve cihaz doğrulaması ancak başarılı testlerden sonra yapılır.

## Sert sınırlar

- Diğer dersleri, aile kodlarını, öğrenci ilerlemesini ve eski ölçme kayıtlarını değiştirme/silme.
- Eski transkript kaynaklı QUIZ'i yeni videoya kanıtsız taşımama.
- Öğretmen bilgisini video bazında koru; playlistin tamamına İlyas Güneş etiketi koyma.
- Ham transkriptin mevcut olması quiz doğruluğunu tek başına garanti etmez.
- Bu plan **uygulamaya alınmış bir ders kataloğu değildir**; canlı yayımlama ayrı onay ve test gerektirir.

## Bir sonraki somut geliştirme

LiseDers'teki normalize ZIP ve kanıt manifesti üzerinden her video için kazanım-zaman aralığı haritası üret; özellikle 12–25 videolarını doğrusal fonksiyon, mutlak değer ve eşitsizlik alt konularına gerçek zaman kodlarıyla ayır. Ardından yeni V2 modüler katalog derleyicisine taslak sürüm ekle ve mevcut matematik içerikleriyle diff oluştur.
