# LiseDers → mevcut Matematik 9 videolarını değiştir

Bu entegrasyonda **yeni ders veya VIDEO item eklenmez**. Kaynak depo: [LiseDers](https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi). Mevcut StudyTracker 9. sınıf Matematik derslerinin `id`, `stableKey`, `lessonId`, `orderKey`, `displayLabel`, başlık, quiz soruları ve öğrenci ilerlemesi korunur; onaylanan kartlarda yalnız oynatılan video kaynağı ve onun kaynak/öğretmen metaverisi değişir.

## Manuel değerlendirme
1. [LiseDers playlist kaydı](https://github.com/TheOsmanYILDIRIM/lise1-ogrenme-programi/tree/main/data/video_playlists/PLSYiXUktJiZeqUJyNFUgFHwOUNydbC-II) içinden `topic-matches.json` ve `playlist-catalog.json` dosyalarını aç.
2. [StudyTracker video replacement Action](https://github.com/TheOsmanYILDIRIM/study-tracker/actions/workflows/import-liseders-playlist.yml) üzerinden **dry-run** çalıştırıp `math9-video-replacement-candidates.csv` dosyasını indir. Bu tabloda her **mevcut** video kartı, adaylar, mevcut öğretmen ve ilişik quiz sayısı gösterilir.
3. Her kart için videoyu gerçekten izleyerek **aynı alt konuyu** anlattığını ve yanında bulunan mikro testlerin yeni anlatımla uyumlu olduğunu kontrol et. Özellikle genel başlıklı çok parçalı `Nicelikler ve Değişimler` videolarında playlist sırasını konu kanıtı sayma. Playlistte İlyas Güneş dışında Nurtaç Kozak da bulunuyor.
4. Yalnız doğruladığın kartları `content/v2/playlist-imports/ilyas-gunes-mat9.replacements.json` dosyasına ekle. **Eşleşme uydurma.**

Örnek sözleşme (gerçekten incelenip onaylanmadan `reviewed`/ `quiz_reviewed` alanları `true` yapılmamalı):

```json
{
  "schema_version": 1,
  "mode": "replace_existing_only",
  "playlist_id": "PLSYiXUktJiZeqUJyNFUgFHwOUNydbC-II",
  "approvals": [
    {
      "target_item_id": "item_mat9_vid_uslu_giris",
      "expected_old_url": "https://www.youtube.com/watch?v=FtJE835vtoM",
      "video_id": "mHq3Dz1Kyw4",
      "reviewed": false,
      "reviewed_contents": false,
      "reason": "Videoyu izledikten sonra hangi alt konuları kapsadığını buraya yaz",
      "quiz_reviewed": false,
      "quiz_review_note": "Mevcut quiz sorularının yeni anlatımla örtüştüğünü kontrol edip gerekçelendir"
    }
  ]
}
```

`expected_old_url` eski kaynağın eşzamanlı değişmediğini garanti eder. Başka öğretmen için `allow_other_teacher: true`, ders dışı öneri için `cross_topic_override: true` ve `cross_topic_evidence` ile **gerekçeli** manuel onay gerekir.

## GitHub Actions
- **Push:** `Review and replace existing Math 9 videos` dry-run çalıştırır, **yayınlama yapmaz**.
- **Actions → Run workflow → `apply_replacements=false`:** İnceleme tablosu ve doğrulama raporu üretir.
- **Actions → Run workflow → `apply_replacements=true`:** Yalnız `reviewed:true`, `reviewed_contents:true` ve bağlı quiz için `quiz_reviewed:true` koşullarını taşıyan önceden onaylanmış **mevcut video kartlarını** günceller. Ardından V2 kataloğu derleyip CLI testlerinden geçirir; uygunsa commit oluşturur.

Komut satırı eşdeğeri:

```bash
python scripts/replace-liseders-math-videos.py \
  --mapping /path/to/topic-matches.json \
  --approvals content/v2/playlist-imports/ilyas-gunes-mat9.replacements.json \
  --review-csv /tmp/math9-review.csv --report /tmp/math9-review.json
# İnsan onayı sonrası:
python scripts/replace-liseders-math-videos.py \
  --mapping /path/to/topic-matches.json \
  --approvals content/v2/playlist-imports/ilyas-gunes-mat9.replacements.json --apply
node scripts/compile-v2-catalog.cjs
node cli/test-v2.js
```

## Quiz, transkript ve canlı uygulama sınırları
YouTube GitHub runner'da altyazı erişimini bot kontrolüyle engellediği için yeni videoların transkriptleri doğrulanmış değildir. Var olan quiz dosyaları **değiştirilmez**; `transcript_grounded` quizlerin eski videoya ait tarihsel `sourceVideoUrl`/`transcriptFingerprint` alanları korunur ve raporda **historical_quiz_provenance** uyarısı üretilir. Uyum onayı bu kaynak farkını ortadan kaldırmaz; ileride yeniden transkript-temellendirme ayrı iştir.

Repo kataloğunda güncelleme yapılması, Cloudflare KV'deki canlı aile içeriğinin otomatik değiştiği anlamına gelmez. Cihazlara sunum için ayrıca mevcut **V2 catalog seed/diff/apply** akışı ve cihaz doğrulaması gerekir; ilerleme silinmemelidir.

Eski `scripts/import-liseders-playlist.py` yalnız tarihsel *yeni kart ekleme* modu içindir; **bu öğretmen değiştirme işi için kullanılmaz**.
