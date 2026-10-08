# 📖 MEB 9. Sınıf Resmî Öğrenci Ders Kitapları Doğrudan İndirme Bağlantıları

Bu doküman, MEB **Türkiye Yüzyılı Maarif Modeli (TYMM)** resmî portalında yayımlanan 9. Sınıf Öğrenci Ders Kitaplarının doğrudan indirme bağlantılarını ve dosya bilgilerini listeler.

> [!NOTE]
> **Hafif Depo İlkesi:** Git deposunun şişmesini önlemek ve hızlı klonlamayı sağlamak amacıyla büyük boyutlu PDF kitap dosyaları (~1.3 GB) depoda saklanmaz. İhtiyaç duyulduğunda bu bağlantılardan indirilebilir veya `scripts/download_student_textbooks.py` betiği ile yerel ortama otomatik çekilebilir.

---

## 🔗 Resmî Ders Kitapları Listesi

| Ders | Kitap Adı | Dosya Boyutu | Resmî İndirme Bağlantısı |
|---|---|---|---|
| 🧬 **Biyoloji** | Biyoloji 9. Sınıf Ders Kitabı | ~149 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/biyoloji-9-sinif-ders-kitabi.pdf) |
| 🌍 **Coğrafya** | Coğrafya 9. Sınıf Ders Kitabı | ~250 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/cografya-dersi-sinif-9-ders-kitabi.pdf) |
| 🕌 **Din Kültürü** | Din Kültürü ve Ahlak Bilgisi 9 | ~12 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/din-kulturu-ve-ahlak-bilgisi-9.pdf) |
| ⚡ **Fizik** | Fizik 9. Sınıf Ders Kitabı | ~53 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/fizik-dersi-9-sinif-ders-kitabi.pdf) |
| 🇬🇧 **İngilizce** | İngilizce 9. Sınıf Ders Kitabı | ~145 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/ingilizce-dersi-9-sinif-ders-kitabi.pdf) |
| 🧪 **Kimya** | Kimya 9. Sınıf Ders Kitabı | ~225 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/kimya-9sinif-ders-kitabi_20260908_105401_981.pdf) |
| 📐 **Matematik** | Matematik 9 (1. Kitap) | ~53 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/matematik-9sinif-ders-kitabi-1kitap_20260908_111051_343.pdf) |
| 📐 **Matematik** | Matematik 9 (2. Kitap) | ~60 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/matematik-9sinif-ders-kitabi-2kitap_20260908_111224_539.pdf) |
| 🏛️ **Tarih** | Tarih 9. Sınıf Ders Kitabı | ~240 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/tarih-9sinif-ders-kitabi_20260908_184825_403.pdf) |
| 📖 **Türk Dili ve Ed.** | Türk Dili ve Edebiyatı 9 | ~51 MB | [tymm.meb.gov.tr PDF](https://tymm.meb.gov.tr/assets/pdf/turk-dili-ve-edebiyati-9sinif-ders-kitabi_20260908_185914_237.pdf) |

---

## ⚡ Otomatik İndirme Betiği

Tüm kitapları tek komutla indirmek ve `pdftotext` ile metinlerini ayrıştırmak için:

```bash
python3 scripts/download_student_textbooks.py
```
