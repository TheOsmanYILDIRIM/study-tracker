#!/usr/bin/env node
const fs = require("fs");
const path = require("path");
const { fetchTranscriptForUrl, readJson, writeJson, ITEMS, ROOT } = require("./curate-and-fix-videos.cjs");

const TRANSCRIPTS = path.join(ROOT, "content", "v2", "transcripts");

// 1. item_tar9_vid_orta_cag_ticaret_yollari
{
  const id = "item_tar9_vid_orta_cag_ticaret_yollari";
  const p = path.join(ITEMS, id + ".json");
  const item = readJson(p);
  const url = "https://www.youtube.com/watch?v=7mvoe1vHDqo";
  item.contentUrl = url;
  item.payload.teacher = "Mehmet Celal ÖZYILDIZ";
  item.payload.provider = "Benim Hocam";
  item.payload.verifiedLectureTitle = "12) Orta Çağ Medeniyetleri ve Ticaret Yolları";
  item.payload.provenance.teacher = "Mehmet Celal ÖZYILDIZ";
  item.payload.provenance.sharedSource = true;
  item.payload.provenance.sharedSourceReason = "Orta Çağ medeniyetleri, devlet yapıları ve ticaret yolları ortak video kapsamı";
  writeJson(p, item);

  const qPath = path.join(ITEMS, id + "__quiz.json");
  if (fs.existsSync(qPath)) {
    const qItem = readJson(qPath);
    qItem.payload.provenance.sourceVideoUrl = url;
    qItem.payload.provenance.transcriptFingerprint = item.payload.provenance.transcriptFingerprint;
    writeJson(qPath, qItem);
  }
}

// 2. item_tar9_vid_topic_08_eski_cag_da_inanc_bilim_ve_sanat
{
  const id = "item_tar9_vid_topic_08_eski_cag_da_inanc_bilim_ve_sanat";
  const p = path.join(ITEMS, id + ".json");
  const item = readJson(p);
  const url = "https://www.youtube.com/watch?v=XJASq7FFjOA";
  item.contentUrl = url;
  item.payload.teacher = "Mehmet Celal ÖZYILDIZ";
  item.payload.provider = "Benim Hocam";
  item.payload.verifiedLectureTitle = "8) İlk Çağ Uygarlıkları: Hukuk, İnanç, Bilim ve Sanat";
  item.payload.provenance.sourceVideoUrl = url;
  item.payload.provenance.teacher = "Mehmet Celal ÖZYILDIZ";
  item.payload.provenance.sharedSource = true;
  item.payload.provenance.sharedSourceReason = "İlk Çağ uygarlıklarında hukuk, inanç, bilim ve sanat ortak video kapsamı";
  
  // transcript
  const res = fetchTranscriptForUrl(id, url);
  if (res) {
    item.payload.provenance.transcriptPath = res.transcriptPath;
    item.payload.provenance.transcriptRawPath = res.transcriptRawPath;
    item.payload.provenance.transcriptFingerprint = res.transcriptFingerprint;
  }
  writeJson(p, item);

  const qPath = path.join(ITEMS, id + "__quiz.json");
  if (fs.existsSync(qPath)) {
    const qItem = readJson(qPath);
    qItem.payload.provenance.sourceVideoUrl = url;
    qItem.payload.provenance.transcriptFingerprint = item.payload.provenance.transcriptFingerprint;
    writeJson(qPath, qItem);
  }
}

// 3. item_tar9_vid_topic_13_orta_cag_medeniyet_havzalarinda_bilim_kultur_ve_sana
{
  const id = "item_tar9_vid_topic_13_orta_cag_medeniyet_havzalarinda_bilim_kultur_ve_sana";
  const p = path.join(ITEMS, id + ".json");
  const item = readJson(p);
  const url = "https://www.youtube.com/watch?v=7mvoe1vHDqo";
  item.contentUrl = url;
  item.payload.teacher = "Mehmet Celal ÖZYILDIZ";
  item.payload.provider = "Benim Hocam";
  item.payload.verifiedLectureTitle = "12) Orta Çağ Medeniyet Havzalarında Bilim, Kültür ve Sanat";
  item.payload.provenance.sourceVideoUrl = url;
  item.payload.provenance.teacher = "Mehmet Celal ÖZYILDIZ";
  item.payload.provenance.sharedSource = true;
  item.payload.provenance.sharedSourceReason = "Orta Çağ medeniyet havzaları, bilim, kültür ve sanat ortak video kapsamı";
  
  // transcript
  const res = fetchTranscriptForUrl(id, url);
  if (res) {
    item.payload.provenance.transcriptPath = res.transcriptPath;
    item.payload.provenance.transcriptRawPath = res.transcriptRawPath;
    item.payload.provenance.transcriptFingerprint = res.transcriptFingerprint;
  }
  writeJson(p, item);

  const qPath = path.join(ITEMS, id + "__quiz.json");
  if (fs.existsSync(qPath)) {
    const qItem = readJson(qPath);
    qItem.payload.provenance.sourceVideoUrl = url;
    qItem.payload.provenance.transcriptFingerprint = item.payload.provenance.transcriptFingerprint;
    writeJson(qPath, qItem);
  }
}

console.log("Canonical History teachers and quizzes synchronized successfully.");
