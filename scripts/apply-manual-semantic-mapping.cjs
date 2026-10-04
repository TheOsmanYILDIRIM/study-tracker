#!/usr/bin/env node
/**
 * scripts/apply-manual-semantic-mapping.cjs
 *
 * Mechanical application of content/v2/transcript-corpus/manual-semantic-mapping.json.
 * Updates items, transcripts, quiz provenances, and generates
 * content/v2/transcript-corpus/manual-semantic-application-report.json.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..');
const MAPPING_FILE = path.join(ROOT, 'content', 'v2', 'transcript-corpus', 'manual-semantic-mapping.json');
const VIDEO_INDEX_FILE = path.join(ROOT, 'content', 'v2', 'transcript-corpus', 'chunks', 'video-index.json');
const ITEMS_DIR = path.join(ROOT, 'content', 'v2', 'items');
const TRANSCRIPTS_DIR = path.join(ROOT, 'content', 'v2', 'transcripts');
const REPORT_FILE = path.join(ROOT, 'content', 'v2', 'transcript-corpus', 'manual-semantic-application-report.json');

const COURSE_TO_SUBJECT_DIR = {
  fiz9: 'fizik',
  kim9: 'kimya',
  biyo9: 'biyoloji',
  cog9: 'cografya',
  tde9: 'tde',
  ing9: 'ingilizce',
  alm9: 'almanca',
  mat9: 'matematik'
};

const COURSE_TO_SOURCE_IDENTITY = {
  fiz9: { provider: 'Özcan Aykın FİZİK', teacher: 'Özcan Aykın' },
  kim9: { provider: 'Meschemy Kimya', teacher: 'Meschemy Kimya' },
  biyo9: { provider: 'Dr. Biyoloji', teacher: 'Dr. Biyoloji' },
  cog9: { provider: 'Coğrafyanın Kodları', teacher: 'Yunus Hoca' },
  tde9: { provider: 'Deniz Hoca', teacher: 'Deniz Hoca' },
  ing9: { provider: 'Ms. Jasmin ELT', teacher: 'Ms. Jasmin ELT' },
  alm9: { provider: 'tonguç 9. SINIF', teacher: 'Tonguç Akademi' },
  mat9: { provider: 'Rehber Matematik', teacher: 'Mehmet Hoca' }
};

const SHARED_SOURCE_REASONS = {
  '1Hg2eF907YA': 'Fizik bilimine giriş, doğa olayları ve fiziğin alt dalları (KAMYONET) ortak video kapsamı',
  '3pRu3T0Q03U': 'Maddenin hâl değişimi (erime, donma, buharlaşma, yoğuşma) ve hâl değişim grafikleri ortak video kapsamı',
  'XQHH2VV1Bbk': 'Sıvıların kaldırma kuvveti, Arşimet prensibi ve yüzme/askıda kalma/batma koşulları ortak video kapsamı',
  'CRg4b_g1IXc': 'Isı iletim yolları (iletim, konveksiyon, ışıma) ve ısı iletim hızı ortak video kapsamı',
  '7aVrdQ7uSQ4': 'Temel ve türetilmiş büyüklükler (KISA MUZ) ile skaler ve vektörel nicelikler ortak video kapsamı',
  '3uhN8JlukLg': 'Kimya laboratuvarı güvenlik kuralları, uyarı piktogramları ve kimyasal madde güvenliği ortak video kapsamı',
  '5wTqs5bYmIM': 'Atom çapı, yarıçapı ve periyodik sistemdeki değişim eğilimleri ortak video kapsamı',
  'xJ6i_sC7Mcc': 'Kovalent bağ oluşumu, Lewis nokta yapıları ve molekül polarlığı/apolarlığı ortak video kapsamı',
  'glDg6lFPsag': 'Maddenin fiziksel halleri: katılar (amorf, kristal) ve sıvılar (viskozite, buharlaşma) ortak video kapsamı',
  'V0SzzIeniHc': 'Biyoloji bilimi, bilimsel yöntem aşamaları (hipotez, teori, kanun) ve bilim etiği ortak video kapsamı',
  'JqVImq7hFjk': 'Canlıların ortak özellikleri ve hücresel yaşamın temelleri ortak video kapsamı',
  'rit0LlD2Fnw': 'Canlıların sınıflandırılması ve âlemler ortak video kapsamı',
  'NlHi5ndIjpA': 'Doğa ve insan etkileşimi ile coğrafya öğrenmenin önemi ortak video kapsamı',
  '1-AJn6S0syU': 'Coğrafya biliminin konusu, bölümleri, tarihsel gelişimi ve niçin coğrafya öğrenmeliyiz ünitesi ortak video kapsamı',
  'UYlpTrZfgA8': 'Doğal afet türleri, tehlike ve risk analizi ile bütüncül afet yönetimi evreleri ortak video kapsamı',
  'MTYC1fG5Z7Q': 'Almanca 0-20 arası sayılar (Die Zahlen), alfabe ve telefon numarası kalıpları ortak video kapsamı',
  '8ktg5DqzffM': 'Almanca okul eşyaları (Die Schulsachen) ve artikeller (der, die, das / ein, eine, kein) ortak video kapsamı',
  'jBQ6PMMdPDk': 'Şiir ve şiir unsurları (nazım birimi, ölçü, kafiye/redif, ahenk) konu anlatımı ortak video kapsamı',
  '50sc1KH0blc': 'MEB 9. sınıf Nevruz belgeseli dinleme/izleme ve belgeseli infografiğe dönüştürme atölye ünitesi ortak video kapsamı',
  'JPGuz-gG_3A': 'Biyografi ve otobiyografi tür özellikleri, tezkire geleneği ve otobiyografi yazma ilkeleri ortak video kapsamı',
  't3MFxbdUS34': 'Gerçek sayıların işlem özellikleri (değişme, birleşme, dağılma) ve cebirsel ispat yöntemleri ortak video kapsamı',
  'k0yL5iVZiAQ': 'Geometrik dönüşümler: öteleme, yansıma ve dönme dönüşümleri konu anlatımı ortak video kapsamı',
  '5QxOpTALmEE': 'Tarihin doğası, olay ve olgu ayrımı ile tarih öğrenmenin bireye ve topluma faydaları ortak video kapsamı',
  '7mvoe1vHDqo': 'Comprehensive 44-minute Middle Ages lecture by Mehmet Celal ÖZYILDIZ covering Kavimler Göçü (kitlesel göçler), Feodalizm and medieval governance/armies, and scholastic thought / cultural exchange.',
  'ptTEW1Ys6Ok': 'Tarım devrimi, yerleşik yaşama geçiş ve ilk Mezopotamya uygarlıkları ortak video kapsamı',
  'v8WWf5qwFwg': 'Tarihsel bilginin üretim aşamaları, tarihe yardımcı bilim dalları ve Türklerin kullandığı takvimler ortak video kapsamı',
  'XJASq7FFjOA': 'İlk Çağ medeniyetlerinde hukuk kuralları, inanç, bilim ve sanat anlayışları ortak video kapsamı'
};

function sha16(s) {
  return crypto.createHash('sha256').update(s).digest('hex').slice(0, 16);
}

function main() {
  const mapping = JSON.parse(fs.readFileSync(MAPPING_FILE, 'utf8'));
  const videoIndex = JSON.parse(fs.readFileSync(VIDEO_INDEX_FILE, 'utf8'));

  const videoMap = new Map();
  for (const v of videoIndex.videos) {
    videoMap.set(v.videoId, v);
  }

  // First pass: determine target URLs for all items in the catalog
  const mapEntries = mapping.decisions.filter(d => d.decision === 'MAP');
  const mapById = new Map(mapEntries.map(m => [m.itemId, m]));

  const itemFiles = fs.readdirSync(ITEMS_DIR).filter(f => f.endsWith('.json') && !f.includes('__quiz'));
  const targetUrlMap = new Map();

  for (const f of itemFiles) {
    const item = JSON.parse(fs.readFileSync(path.join(ITEMS_DIR, f), 'utf8'));
    if (item.itemType !== 'VIDEO') continue;
    let url = item.contentUrl;
    if (mapById.has(item.id)) {
      url = `https://www.youtube.com/watch?v=${mapById.get(item.id).videoId}`;
    }
    if (url) {
      const list = targetUrlMap.get(url) || [];
      list.push(item.id);
      targetUrlMap.set(url, list);
    }
  }

  let mapAppliedCount = 0;
  let keepCurrentCount = 0;
  let mixedNeedsSplitCount = 0;
  let transcriptFilesCopiedCount = 0;
  let quizProvenanceUpdatesCount = 0;

  const changedItemIdsByCourse = {};
  const reportEntries = [];
  const courseCounts = {};

  for (const decision of mapping.decisions) {
    const courseId = decision.course;
    if (!courseCounts[courseId]) {
      courseCounts[courseId] = {
        total: 0,
        MAP: 0,
        KEEP_CURRENT: 0,
        MIXED_NEEDS_SPLIT: 0,
        changedRuntimeItems: 0,
        transcriptFilesCopied: 0,
        quizProvenanceUpdated: 0
      };
    }
    courseCounts[courseId].total++;

    const itemFilePath = path.join(ITEMS_DIR, `${decision.itemId}.json`);
    if (!fs.existsSync(itemFilePath)) {
      throw new Error(`Item file not found: ${itemFilePath}`);
    }

    const item = JSON.parse(fs.readFileSync(itemFilePath, 'utf8'));
    const oldUrl = item.contentUrl || null;

    if (decision.decision === 'KEEP_CURRENT' || decision.decision === 'MIXED_NEEDS_SPLIT') {
      if (decision.decision === 'KEEP_CURRENT') {
        keepCurrentCount++;
        courseCounts[courseId].KEEP_CURRENT++;
      } else {
        mixedNeedsSplitCount++;
        courseCounts[courseId].MIXED_NEEDS_SPLIT++;
      }

      // Check if this unmutated item shares its URL with another item (e.g. rit0LlD2Fnw)
      // If so, make sure its sharedSource metadata is aligned
      const urlList = targetUrlMap.get(oldUrl) || [];
      const isShared = urlList.length > 1;
      const vMatch = oldUrl ? oldUrl.match(/(?:v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/) : null;
      const vId = vMatch ? vMatch[1] : null;

      if (isShared && vId && SHARED_SOURCE_REASONS[vId]) {
        item.payload = item.payload || {};
        item.payload.provenance = item.payload.provenance || {};
        item.payload.provenance.sharedSource = true;
        item.payload.provenance.sharedSourceReason = SHARED_SOURCE_REASONS[vId];
        fs.writeFileSync(itemFilePath, JSON.stringify(item, null, 2) + '\n', 'utf8');

        const quizFilePath = path.join(ITEMS_DIR, `${decision.itemId}__quiz.json`);
        if (fs.existsSync(quizFilePath)) {
          const qItem = JSON.parse(fs.readFileSync(quizFilePath, 'utf8'));
          qItem.payload = qItem.payload || {};
          qItem.payload.provenance = qItem.payload.provenance || {};
          qItem.payload.provenance.sharedSource = true;
          qItem.payload.provenance.sharedSourceReason = SHARED_SOURCE_REASONS[vId];
          fs.writeFileSync(quizFilePath, JSON.stringify(qItem, null, 2) + '\n', 'utf8');
        }
      }

      reportEntries.push({
        itemId: decision.itemId,
        course: decision.course,
        decision: decision.decision,
        runtimeItemChanged: false,
        oldUrl,
        newUrl: oldUrl,
        selectedVideoId: null,
        transcriptCopied: false,
        quizProvenanceUpdated: false,
        verdict: decision.verdict,
        note: decision.note
      });
      continue;
    }

    if (decision.decision !== 'MAP') {
      throw new Error(`Unknown decision type: ${decision.decision} on item ${decision.itemId}`);
    }

    // MAP decision handling
    mapAppliedCount++;
    courseCounts[courseId].MAP++;
    courseCounts[courseId].changedRuntimeItems++;

    if (!changedItemIdsByCourse[courseId]) {
      changedItemIdsByCourse[courseId] = [];
    }
    changedItemIdsByCourse[courseId].push(decision.itemId);

    const videoId = decision.videoId;
    const newUrl = `https://www.youtube.com/watch?v=${videoId}`;
    const vMeta = videoMap.get(videoId);
    if (!vMeta) {
      throw new Error(`Video ID ${videoId} not found in video index`);
    }

    const identity = COURSE_TO_SOURCE_IDENTITY[courseId];
    if (!identity) {
      throw new Error(`No source identity defined for course ${courseId}`);
    }

    const sDir = COURSE_TO_SUBJECT_DIR[courseId];
    const rawCorpusPath = path.join(ROOT, vMeta.rawPath);
    const cleanCorpusPath = path.join(ROOT, vMeta.cleanPath);

    if (!fs.existsSync(rawCorpusPath) || !fs.existsSync(cleanCorpusPath)) {
      throw new Error(`Corpus transcript files missing for ${videoId}: raw=${rawCorpusPath}, clean=${cleanCorpusPath}`);
    }

    // Copy transcripts into content/v2/transcripts/<itemId>.tr.txt and <itemId>.tr.tr.vtt
    const destCleanRel = `content/v2/transcripts/${decision.itemId}.tr.txt`;
    const destRawRel = `content/v2/transcripts/${decision.itemId}.tr.tr.vtt`;
    const destCleanAbs = path.join(ROOT, destCleanRel);
    const destRawAbs = path.join(ROOT, destRawRel);

    const cleanContent = fs.readFileSync(cleanCorpusPath, 'utf8');
    const rawContent = fs.readFileSync(rawCorpusPath, 'utf8');

    fs.writeFileSync(destCleanAbs, cleanContent, 'utf8');
    fs.writeFileSync(destRawAbs, rawContent, 'utf8');

    transcriptFilesCopiedCount += 2;
    courseCounts[courseId].transcriptFilesCopied += 2;

    const transcriptFingerprint = sha16(cleanContent.trim());
    const isShared = (targetUrlMap.get(newUrl) || []).length > 1;
    const sharedReason = isShared
      ? (SHARED_SOURCE_REASONS[videoId] || `${vMeta.title} (${decision.note})`)
      : undefined;

    const isAutoSub = /auto|orig|asr/i.test(path.basename(vMeta.rawPath));
    const transcriptKind = isAutoSub ? 'auto' : 'subtitle';

    // Update item object
    item.contentUrl = newUrl;
    item.publishingStatus = 'active';

    item.payload = item.payload || {};
    item.payload.provider = identity.provider;
    item.payload.teacher = identity.teacher;
    item.payload.verifiedLectureTitle = vMeta.title;
    item.payload.reviewStatus = 'verified';
    item.payload.reviewedOverride = true;

    item.payload.provenance = item.payload.provenance || {};
    item.payload.provenance.sourceVideoUrl = newUrl;
    item.payload.provenance.provider = identity.provider;
    item.payload.provenance.teacher = identity.teacher;
    item.payload.provenance.verifiedLectureTitle = vMeta.title;
    item.payload.provenance.reviewStatus = 'verified';
    item.payload.provenance.reviewedOverride = true;
    item.payload.provenance.evidence = decision.note;
    item.payload.provenance.transcriptLanguage = 'tr';
    item.payload.provenance.transcriptKind = transcriptKind;
    item.payload.provenance.transcriptPath = destCleanRel;
    item.payload.provenance.transcriptRawPath = destRawRel;
    item.payload.provenance.transcriptFingerprint = transcriptFingerprint;
    if (isShared) {
      item.payload.provenance.sharedSource = true;
      item.payload.provenance.sharedSourceReason = sharedReason;
    } else {
      delete item.payload.provenance.sharedSource;
      delete item.payload.provenance.sharedSourceReason;
    }

    if (item.payload.provenance.reviewNote && /altyazı|transcript/i.test(item.payload.provenance.reviewNote)) {
      delete item.payload.provenance.reviewNote;
    }

    fs.writeFileSync(itemFilePath, JSON.stringify(item, null, 2) + '\n', 'utf8');

    // Update linked micro-quiz if exists
    let quizUpdated = false;
    const quizFilePath = path.join(ITEMS_DIR, `${decision.itemId}__quiz.json`);
    if (fs.existsSync(quizFilePath)) {
      const qItem = JSON.parse(fs.readFileSync(quizFilePath, 'utf8'));
      qItem.payload = qItem.payload || {};
      qItem.payload.provenance = qItem.payload.provenance || {};

      qItem.payload.provenance.derivedFromItemId = decision.itemId;
      qItem.payload.provenance.sourceVideoUrl = newUrl;
      qItem.payload.provenance.transcriptLanguage = 'tr';
      qItem.payload.provenance.transcriptKind = transcriptKind;
      qItem.payload.provenance.transcriptPath = destCleanRel;
      qItem.payload.provenance.transcriptFingerprint = transcriptFingerprint;
      qItem.payload.provenance.verifiedLectureTitle = vMeta.title;
      qItem.payload.provenance.reviewStatus = 'verified';
      qItem.payload.provenance.reviewedOverride = true;

      if (isShared) {
        qItem.payload.provenance.sharedSource = true;
        qItem.payload.provenance.sharedSourceReason = sharedReason;
      } else {
        delete qItem.payload.provenance.sharedSource;
        delete qItem.payload.provenance.sharedSourceReason;
      }

      fs.writeFileSync(quizFilePath, JSON.stringify(qItem, null, 2) + '\n', 'utf8');
      quizUpdated = true;
      quizProvenanceUpdatesCount++;
      courseCounts[courseId].quizProvenanceUpdated++;
    }

    reportEntries.push({
      itemId: decision.itemId,
      course: decision.course,
      decision: 'MAP',
      runtimeItemChanged: true,
      oldUrl,
      newUrl,
      selectedVideoId: videoId,
      transcriptCopied: true,
      quizProvenanceUpdated: quizUpdated,
      verdict: decision.verdict,
      note: decision.note
    });
  }

  const report = {
    generatedAt: new Date().toISOString(),
    totalItems: mapping.decisions.length,
    summaryCounts: {
      mapAppliedCount,
      keepCurrentCount,
      mixedNeedsSplitCount,
      transcriptFilesCopiedCount,
      quizProvenanceUpdatesCount
    },
    changedItemIdsByCourse,
    courseCounts,
    decisions: reportEntries
  };

  fs.writeFileSync(REPORT_FILE, JSON.stringify(report, null, 2) + '\n', 'utf8');
  console.log(`✅ Report generated at ${path.relative(ROOT, REPORT_FILE)}`);
  console.log(`- MAP applied: ${mapAppliedCount}`);
  console.log(`- KEEP_CURRENT: ${keepCurrentCount}`);
  console.log(`- MIXED_NEEDS_SPLIT: ${mixedNeedsSplitCount}`);
  console.log(`- Transcript files copied: ${transcriptFilesCopiedCount}`);
  console.log(`- Quiz provenance updates: ${quizProvenanceUpdatesCount}`);
}

if (require.main === module) {
  main();
}

module.exports = { main };
