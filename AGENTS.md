# Antigravity Developer & Engineering Guidelines (agy + opencode ortak — tek kaynak)

Bu dosya, Antigravity asistanının tüm projelerde ve çalışma ortamında izleyeceği temel mühendislik standartlarını, düşünce süreçlerini ve ortam kurallarını tanımlar.
`agy` ve `opencode` AYNI kuralları ve AYNI havuzu kullanır. Bu dosya `~/AGENTS.md`, `~/CLAUDE.md`, `~/GEMINI.md`, `/storage/emulated/0/Documents/AGY-Vault/AGENTS.md`, `CLAUDE.md`, `GEMINI.md` konumlarında birebir aynı olmak ZORUNDADIR (Sync Rule). Home tarafında CLAUDE/GEMINI symlink, vault tarafında kopya tutulur (sdcard symlink desteklemez). Kurallar şişirilmez, öz ve net tutulur.

---

## 0. Paylaşılan Bilgi Vault'u (AGY-Vault Wiki)

- **Konum:** `/storage/emulated/0/Documents/AGY-Vault` (agy CLI wiki'si burada). `~/agy-vault` bu dizine symlink'dir — aynı fiziksel havuz.
- Bu vault, **agy ve opencode tarafından ortak** kullanılır; iki asistan da aynı bilgi tabanını paylaşır.
- opencode bu vault'a `references.agy-vault` ile bağlıdır (`~/.config/opencode/opencode.jsonc` içinde tanımlı). İlgili bir görev (CLI komutu, OKF standardı, proje notu) geldiğinde buradaki notları oku/güncelle.
- Yapı: `10-Projects/` (proje notları), `20-Areas/` (süreklilik alanları), `30-Resources/` (kaynaklar: OKF standardı, CLI cheatsheet vb.), `99-Meta/` (şablonlar).
- **Beyin ayrı oda, atölye ayrı oda:** Vault; notların, kuralların, kararların, sözlüğün ve proje özetlerinin yaşadığı yerdir. Kodun kendisi doğrudan vault köküne saçılmaz.
- **Proje iskeleti:** Her projenin kendi atölye klasöründe `AGENTS.md`/`CLAUDE.md`, `notes.md` (dump & mimari), `reports/` (araştırmalar), `backlog.md` (yarım kalan işler) ve `constants` dosyası bulunur.

## 0.1 Otomatik Wiki Geliştirme (Self-Evolving Vault)

Bu vault **kendiliğinden büyür**; sadece okunmaz. agy ve opencode, kullanıcı ayrıca istemeden de iş akışında öğrendikleri kalıcı bilgiyi otomatik olarak vault'a yazar:

- **Ne zaman yazılır:** yeni/keşfedilen `agy` CLI komutu veya davranışı, OKF standardı güncellemesi, çözülen hata/cihaz özelliği, kullanıcı tercihi, proje kararı, tekrar kullanılacak snippet/cheatsheet.
- **Nasıl yazılır:** `agy-vault <kategori> "<başlık>" [içerik]` scripti kullanılır (PATH'te: `/data/data/com.termux/files/usr/bin/agy-vault`).
  - Kategoriler: `projects`→`10-Projects`, `areas`→`20-Areas`, `resources`→`30-Resources`, `meta`→`99-Meta`, `inbox`→`00-Inbox`.
  - Dosya yoksa oluşturur (frontmatter + başlık), varsa `updated` tarihini tazeler ve tarihli bir kayıt ekler.
- **Kural:** Her iki asistan da aynı vault'a yazar; çakışma olmasın diye nota tarihli giriş eklenir, mevcut notlar üzerine yazılmaz.
- Kısa notlar için `agy-vault inbox "<başlık>" "<tek cümle>"` ile hızlı yakalanır, sonra uygun klasöre taşınır.

---

## 1. Temel Çalışma İlkeleri ve Düşünce Süreci (Thinking & Execution Loop)

Her görevde aşağıdaki sistematik döngü uygulanır:
1. **Analiz (Understand & Discover):** Kullanıcının hedefini tam olarak anla, gereksinimleri netleştir. Mevcut kod tabanını, bağımlılıkları ve dosya yapısını analiz et.
2. **Planlama (Plan & Architecture):** Karmaşık görevlerde adım adım bir plan oluştur. SOLID, DRY ve KISS ilkelerine uygun temiz mimariyi hedefle.
3. **Uygulama (Clean Implementation):**
   - Tam, çalışan, eksiksiz kod yaz (yer tutucu veya yarım bırakılmış kodlar kesinlikle kullanılmaz).
   - Tip güvenliği (TypeScript, Python Type Hints, Go typing vb.) ve katı hata yakalama mekanizmaları kullan.
   - Güvenlik açıklarına (OWASP Top 10: SQLi, XSS, Path Traversal, Insecure Deserialization vb.) karşı önlemleri baştan al.
4. **Doğrulama ve Test (Verify & Test):** Yapılan değişiklikleri lint, derleme, birim test veya simülasyon komutlarıyla doğrula. Hata varsa kendi kendini düzelt (self-correct).
5. **Belgeleme ve İletişim (Document & Report):** Kullanıcıya yapılan değişiklikleri, dosya bağlantılarını (`[file.ext](file:///path)`) ve sonraki adımları net, GitHub Markdown formatında sun.

**Ajan brief formatı (Sınır çiz, yol çizme):** Her alt-ajan brief'i 5 parçadan oluşur: *Hedef* (ne olursa bitti sayılır) + *Eldekiler/Ham Veri* + *Sert Sınırlar* (dokunma listesi) + *Ölçü* + *Serbestlik* (hata görürse söyleme inisiyatifi).

### 1.1 Derinlemesine Test ve Doğrulama Standartları (Anti-Macro Fallacy & No Silent Fallbacks)
- **Makro Metrik Yanılgısı Yasağı (Anti-Macro Fallacy):** Testler yalnızca toplam dosya sayısı veya genel satır adedi gibi kaba toplamlarla geçiştirilemez. Her tablo, veri kümesi, API ve model için EN AZ bir gerçek kaydın kritik semantik kolonları (`> 0`, `not null`, doğru tip ve birim) açıkça `assert` edilmelidir. Makro sayaçlar veri bütünlüğü kanıtı değildir.
- **Sıfır Sessiz Fallback Kuralı (No Silent Fallback):** Veritabanı, servis veya dosya okumasında veri bulunamadığında ya da `NULL` döndüğünde kodun sessizce sahte varsayılan sayılar (hardcoded fallback: `if (val <= 0) val = 61.2`) uydurarak hatayı gizlemesi KESİNLİKLE YASAKTIR. Eksik veri geliştirme/denetim aşamasında açıkça `N/A`, `NULL` veya hata fırlatmalıdır; aksi takdirde otomatik ve görsel testler veri mimarisindeki çöküşleri yakalayamaz.
- **Varlık - Kayıt Mutabakatı (Asset-to-Registry Reconciliation):** Projedeki statik varlıklar (SVG, ikon, ses, şablon, dosya) ile koddaki enum, subtype veya rota listeleri mutabakat kontrolünden (`ls assets/ | wc` vs `grep subtypes | wc`) geçirilmelidir. Klasörde 9 SVG varken arayüzde 3 alt tip tanımlı bırakılamaz.
- **Anomali ve 0-Bayt Dosya Sorgulama (Oddity Investigation):** 0-bayt dosya, boş dönen sorgu veya teknik standarda aykırı bir isimlendirme (örneğin Socket Weld 2000#) görüldüğünde körü körüne koda dahil edilmez; standardın aslı (ASME, DIN, ISO vb.) araştırılarak kök anomali sorgulanır ve çözülür.

### 1.2 Alt-Ajan Görevlendirme ve Şüpheci Denetim Protokolü (Adversarial Subagent Verification)
- **Kontrol Listesi İllüzyonu Yasağı (Anti-Checklist Vanity):** Bir alt ajanın oluşturduğu markdown kontrol listesindeki `[x]` işaretleri veya "tamamlandı/kusursuz" raporları asla kanıt sayılamaz. Alt ajanın "kodu yazmış olması", o kodun "doğru veri ürettiği" anlamına gelmez.
- **Şüpheci Çapraz Denetim (Adversarial Verification):** Ana ajan, alt ajanın tamamladığını beyan ettiği her veri/kod bloğunu doğrudan ham CLI komutlarıyla (`sqlite3 db "SELECT ..."`, `diff`, `curl`, `jq`) bağımsız olarak test etmeden, kolon doluluklarını ve çıktı doğruluğunu kendi gözleriyle görmeden alt-ajan çıktısını onaylayamaz.

---

## 2. Termux ve Android Linux Ortam Kuralları

Bu sistem bir **Termux / Android Linux** ortamında çalışmaktadır. Komutlar ve araçlar çalıştırılırken aşağıdaki kurallara kesinlikle uyulmalıdır:
- **PATH Yönetimi:** Gerekli tüm binary'ler `/data/data/com.termux/files/usr/bin` altında bulunur. Komutlarda ortam değişkenleri gerektiğinde bu PATH dikkate alınmalıdır.
- **Paket Yöneticisi:** Paket kurulumları için `pkg` veya `apt` kullanılır. `sudo` veya `systemctl` (systemd) varsayılan olarak bulunmaz, servisler bağımsız daemon veya arka plan süreçleri olarak yönetilir.
- **Araçlar & CLI:** GitHub CLI (`gh` / `/data/data/com.termux/files/usr/bin/gh`) kuruludur. Komut çıktısı token optimizasyonu için **RTK** (`rtk` / `/data/data/com.termux/files/usr/bin/rtk`) kuruludur; `git`, `cargo`, `npm`, `pytest`, `gh`, `diff`, `ls` gibi komutlar çalıştırılırken çıktıyı %60-%90 filtrelemek için `rtk <komut>` ön eki öncelikli kullanılmalıdır. Büyük dosya ve proje keşiflerinde token israfını önlemek için **jCodeMunch** (`jcodemunch` CLI ve MCP) kullanılarak dosyanın tamamı yerine sadece AST sembolleri/taslakları (`outline`, `search`, `get`, `map`) çekilmelidir.
- **Native CLI önceliği:** Veri işleme, metin manipülasyonu, JSON sorgulama veya dosya incelemelerinde Termux native araçları (`jq`, `fd`, `rg`, `curl`, `tree`, `bat`, `sqlite3`, `esbuild` vb.) kullanılır. Karmaşık scriptler yerine Unix pipeline'ları (`curl | jq`) tercih edilir.
- **Mimari (ARM64 / AArch64):** Derleme veya bağımlılık yükleme işlemlerinde ARM64 mimarisine uygun native kütüphaneler ve prebuild paketler tercih edilmelidir.
- **Kaynak Kullanımı:** Bellek ve CPU kullanımında verimli olunmalı, gereksiz ağır arka plan işlemleri sınırlandırılmalıdır.

### 2.1 Dil ve Çalışma Zamanı Optimizasyonu (Python vs Node.js Standartları)
- **Node.js Önceliği (I/O, MCP, Web, JSON):** Node.js V8 motoru ARM64 üzerinde çok yüksek I/O performansı ve düşük bellek gecikmesi sunar. MCP sunucuları, JSON-RPC iletişimleri, REST/WebSocket API'leri, web scraping ve CLI yardımcılarında derleme gerektirmeyen saf JavaScript/Node.js araçları Python'a göre öncelikli tercih edilmelidir.
- **Python Standartları (Stdlib-First & Termal Koruma):**
  - Ağır kütüphaneler (Pandas, PyTorch, ağır ORM'ler) yerine standart kütüphane (`sqlite3`, `json`, `dataclasses`, `pathlib`, `re`, `urllib.request`) ve Rust tabanlı CLI araçları (`ruff`, `rtk`) kullanılmalıdır.
  - Python komutları ve testleri her zaman `py-cool` (`nice -n 15 taskset -c 0-5 python3`) ile çalıştırılmalıdır.
  - C/Rust uzantısı derleme gerektiren tekerleksiz (non-wheel) Python paketlerinden Termux ortamında kesinlikle kaçınılmalıdır.

---

## 3. Kod Kalitesi ve Güvenlik Standartları

- **Modülerlik & Yeniden Kullanılabilirlik:** Fonksiyonlar tek bir sorumluluğa (Single Responsibility) sahip olmalı, mantık katmanları (Controller, Service, Repository vb.) net ayrılmalıdır.
- **Defensive Programming:** Tüm dış girdiler (API parametreleri, dosya okumaları, kullanıcı girdileri) doğrulanmalı (Zod, Pydantic, Joi vb.), `undefined`/`null` durumları güvenli ele alınmalıdır.
- **Hata Yönetimi:** Hatalar sessizce yutulmamalı (`catch {}`), anlamlı loglar ve uygun hata tipleri ile fırlatılmalı veya yönetilmelidir.
- **Sır Güvenliği:** API anahtarları, şifreler veya hassas veriler asla kodun içine sabit olarak (hardcoded) yazılmaz; `.env` ve ortam değişkenleri kullanılır.

---

## 4. Android & APK Derleme ve İmzalama Standartları (Sabit Keystore & CI/CD Zorunluluğu)

- **Telefonda / Termux'ta Asla Yerel Gradle Derlemesi Yapmama Kuralı:** Mobil cihazın aşırı ısınmasını, bellek tükenmesini ve ortam kirliliğini önlemek amacıyla Termux içinde `./gradlew compile...`, `./gradlew assemble...` veya yerel Gradle derlemesi **kesinlikle çalıştırılmamalıdır**. Derleme, paketleme ve APK üretimi her zaman GitHub'a pushlanarak **GitHub Actions CI/CD** ortamında yapılmalıdır.
- **Sabit İmza Kuralı (Üst Üste APK Kurulumu):** Tüm Android projelerinde (hem Debug hem Release build'lerinde) **sabit, kalıcı bir imzalama anahtarı (keystore)** kullanılmalıdır.
- **Sebep:** CI/CD ortamında (GitHub Actions) her derlemede rastgele anahtar üretilirse Android cihazlar `"İmza uyuşmazlığı / Uygulama yüklenemedi"` hatası verir ve kullanıcı eski uygulamayı silmeden yeni sürümü güncelleyemez.
- **Uygulama:** Her Android projesinde `signingConfigs` bloğu tanımlanmalı, `antigravity.keystore` (veya proje keystore'u) hem `release` hem `debug` derleme tiplerine atanmalıdır.

---

## 5. Avenox "Bitti" Protokolü ve Diff İncelemesi

- **No Termux API / Bildirim Yok:** `termux-api` kurulmaz ve Android sistem bildirimleri tetiklenmez.
- **"Bitti"nin Tanımı ve Slop Engeli:** Ajanın veya alt-ajanın "bitti" demesine körü körüne güvenilmez. Yapılan her kod değişikliği `git diff` ve bağımsız CLI kontrolleri ile doğrulanır. Test suite'in yeşil yanması (`PASS`) tek başına yeterli değildir; testlerin yapılan değişikliği ve veritabanının derinliğini gerçekten kapsayıp kapsamadığı denetlenir. Vibe coding ve sahte checklist onayları yasaktır.
- **Zorunlu Dinamik Backlog & Log İş Akışı (Kesinti & Bağlantı Güvencesi):**
  1. **İşe Başlarken (`backlog.md` WIP Bildirimi):** Asistan bir göreve veya alt maddeye başlayacağı an, projenin `backlog.md` dosyasında ilgili maddeyi `[WIP / Yapılıyor: <Görev>]` olarak işaretlemek ZORUNDADIR.
  2. **İş Tamamlandığında (`log.md` Aktarımı):** Görev başarıyla tamamlanıp testlerle doğrulandığında, madde `backlog.md` dosyasından kaldırılır (veya tamamlandı olarak mühürlenir) ve projenin `log.md` dosyasına tarihli ve kısa bir özetle (`[YYYY-MM-DD] Yapıldı: ...`) işlenir.
  3. **Kesintisiz Devamlılık Güvencesi:** İnternet kopması, oturumun sıfırlanması veya ortam kesintilerinde asistan projeye girer girmez `backlog.md` dosyasındaki aktif `WIP` maddesini görerek doğrudan kaldığı yerden işe devam eder; mükerrer iş yapmaz.
- **Git Operatörü Kuralı (agy sıkı kuralı — agy tarafında zorunlu):** agy'de ana ajan doğrudan `git add/commit/push` ÇALIŞTIRAMAZ; tüm git süreci `git-operator` subagent'ına devredilir ve push sonrası `gh run watch` ile CI takibi yapılır. opencode oturumlarında bu kural referans alınır; kullanıcı açıkça istemedikçe commit/push yapılmaz, sadece `git status/diff` ile doğrulama yapılır.

---

## 6. Jetpack Compose Çoklu Ekran & Görsel Geri Bildirim Standartları (Visual Matrix)

- **Çoklu Ekran Test Zorunluluğu (Multi-Device Matrix):** Compose UI tasarımları tek bir ekran boyutunda test edilip bırakılamaz. Mutlaka şu matriste doğrulanmalıdır:
  1. **Compact / Dar Ekranlar (320dp - 360dp):** Giriş seviyesi veya dar ekranlı Android cihazlar.
  2. **Standart Ekranlar (392dp - 411dp):** Modern Android amiral gemisi cihazlar (Pixel, Samsung Galaxy).
  3. **Erişilebilirlik & Büyük Yazı Tipi (`fontScale = 1.25x - 1.3x`):** Kullanıcı font boyutunu büyüttüğünde buton ve rozetlerin (badge) üst üste binmesini engelleme.
- **Responsive TopBar & Dock Kuralı:** TopBar ve Input Dock bileşenlerinde sabit genişlikli metin veya rozetler kullanılmamalı; dar ekranlarda otomatik olarak kompakt/ikonik moda (`⚡ %75` gibi) geçmeli, taşma (`TextOverflow.Ellipsis`) ve `weight` koruması bulunmalıdır.
- **Görsel Çıktıların Saklanması:** Render edilen tüm PNG görsel çıktıları kullanıcının doğrudan erişebilmesi için `~/storage/documents/` (`/sdcard/Documents/`) altına kopyalanmalı ve agent tarafından multimodal olarak analiz edilmelidir.

---

## 7. Arka Plan Süreç Denetimi, Temizlik ve Termal Koruma Kuralı

- **Zorunlu Süreç Denetimi ve Temizliği (Her Yanıttan Önce):** Asistan, her yanıtı tamamlamadan önce arka planda askıda kalmış, zombie olmuş veya yetim kalmış süreçleri denetlemeli ve gereksiz olanları temizlemelidir (`/data/data/com.termux/files/usr/bin/agy-clean` disk ve önbellek temizliği yapar; opencode log/tool-output/cache kalemlerini de kapsar).
- **KESİNLİKLE Genel `pkill -f agy` veya `agy.va39` Kapatmama Kuralı:** Kullanıcının terminalde çalıştırdığı interaktif `agy` CLI oturumu da sistemde `agy.va39` olarak çalışır. Körlemesine `pkill -9 -f agy.va39` veya `kill` çalıştırmak kullanıcının aktif terminal CLI oturumunu kapatır. Sadece web sunucusunun kendi başlattığı ve durdurulmak istenen child PID'si (`activeChildProcess.pid`) veya açıkça zombie olmuş `<defunct>` süreçler hedeflenmelidir.
- **Varsayılan %50 CPU Sınırı (Isınma Koruması):** Termux ortamında hem CLI (`agy` / `agy-cool`, `opencode` / `opencode-cool`) hem de web arayüzü sunucusu (`agy-web` / `server.js`) üzerinden başlatılan tüm AGY/OpenCode oturumları, mobil cihazın aşırı ısınmasını önlemek için varsayılan olarak **verimli çekirdeklere sabitleme + nice** (`taskset -c 0-5 nice -n 15`, `opencode-limit`/`agy-limit`) altında çalıştırılmalıdır. `cpulimit -l 50` aktif polling ile ısıttığı için kullanılmaz.

---

## 8. Zorunlu AGY-Vault Eşleme ve Entegrasyon Kuralı (Vault Synchronization)

- **Zorunlu Kural:** Asistan (Antigravity — agy ve opencode), çalışma ortamında yapılan **tüm altyapı, MCP sunucusu, skill, yeni kütüphane/repo entegrasyonları veya proje mimarisi değişikliklerini** anında `/storage/emulated/0/Documents/AGY-Vault/` altındaki ilgili dizinlere (`10-Projects/`, `20-Areas/` veya `30-Resources/`) kaydetmek ve güncellemek zorundadır.
- **Değişiklik Takibi:**
  1. Yeni bir MCP sunucusu veya Skill eklendiğinde/kaldırıldığında: `30-Resources/<Entegrasyon_Adi>.md` oluşturulur/güncellenir.
  2. Yeni bir projeye başlandığında veya önemli bir mimari karar alındığında: `10-Projects/<Proje_Adi>.md` güncellenir.
  3. Sistem/ortam düzeyinde bir standart değiştiğinde: `20-Areas/` güncellenir.
- **Amaç:** Ajanın oturumları yeniden başlasa dahi kalıcı bilgi kaybı yaşanmaması ve tüm sistem mimarisinin insan-ajan ortak hafızasında (OKF Vault) eksiksiz senkron kalmasıdır.

---

## 9. Qwen MCP Danışmanlık Kuralı (agy'den — karmaşık işlerde zorunlu)

- **Zengin context ile danışma:** Karmaşık algoritmalar, mimari tasarım, procedural generation ve çetrefilli hata ayıklama süreçlerinde `qwen` MCP aracı (`qwen_chat`) üzerinden Qwen modeline danışılır. Asla yüzeysel prompt gönderilmez; kaynak kod, veri yapıları, teknik kısıtlar ve uç durumlar eksiksiz aktarılır.
