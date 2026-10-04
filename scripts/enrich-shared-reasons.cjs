#!/usr/bin/env node
const fs = require("fs");
const path = require("path");
const { readJson, writeJson, ITEMS } = require("./curate-and-fix-videos.cjs");

const SPECIFIC_REASONS = {
  "https://www.youtube.com/watch?v=NBhw_SkvV8E": "Şiir bilgisi, nazım birimi/ölçü/kafiye ve edebi sanatlar (söz sanatları) ortak video kapsamı",
  "https://www.youtube.com/watch?v=hm1tQdSExLA": "Kendini tanıtma, adını söyleme ve günlük temel Almanca iletişim kalıpları ortak video kapsamı",
  "https://www.youtube.com/watch?v=POJqXZr5CW8": "Aile bireyleri (die Familie) ve meslekler (die Berufe) 3. ünite ortak video kapsamı",
  "https://www.youtube.com/watch?v=dkwbOGhBedc": "Sıvılarda basınç, basınç kuvveti ve Pascal prensibi ortak video kapsamı",
  "https://www.youtube.com/watch?v=XJASq7FFjOA": "İlk Çağ uygarlıklarında hukuk, inanç, bilim ve sanat anlayışları ortak video kapsamı",
  "https://www.youtube.com/watch?v=mM4ZCvBetDg": "Hikaye türleri (olay ve durum hikayesi) ile hikaye karakter analizi ortak video kapsamı"
};

const files = fs.readdirSync(ITEMS).filter(f => f.endsWith(".json"));
for (const f of files) {
  const p = path.join(ITEMS, f);
  const item = readJson(p);
  const url = item.contentUrl || item.payload?.provenance?.sourceVideoUrl;
  if (url && SPECIFIC_REASONS[url]) {
    item.payload ||= {};
    item.payload.provenance ||= {};
    item.payload.provenance.sharedSource = true;
    item.payload.provenance.sharedSourceReason = SPECIFIC_REASONS[url];
    writeJson(p, item);
  }
}
console.log("Specific sharedSource reasons updated.");
