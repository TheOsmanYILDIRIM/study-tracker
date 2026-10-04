#!/usr/bin/env node
/**
 * Fetch Turkish transcripts for every selected/active YouTube VIDEO item.
 *
 * Exact yt-dlp flow:
 *   yt-dlp --skip-download --write-subs --write-auto-subs --sub-langs "tr,tr-*"
 *
 * Raw subtitle files are cached under content/v2/transcripts/ (gitignored).
 * Item JSON receives provenance metadata + fingerprint.
 *
 * Usage:
 *   node scripts/fetch-v2-transcripts.cjs
 *   node scripts/fetch-v2-transcripts.cjs --only-missing
 */
const fs=require("fs");
const path=require("path");
const cp=require("child_process");
const crypto=require("crypto");

const ROOT=path.resolve(__dirname,"..");
const ITEMS=path.join(ROOT,"content","v2","items");
const TRANSCRIPTS=path.join(ROOT,"content","v2","transcripts");
fs.mkdirSync(TRANSCRIPTS,{recursive:true});
const ONLY_MISSING=process.argv.includes("--only-missing");
const CONCURRENCY=Math.max(1,Math.min(12,Number(process.env.TRANSCRIPT_CONCURRENCY||6)));

function readJson(p){return JSON.parse(fs.readFileSync(p,"utf8"));}
function writeJson(p,o){fs.writeFileSync(p,JSON.stringify(o,null,2)+"\n");}
function isYoutube(url=""){return /(?:youtube\.com\/watch\?v=|youtu\.be\/)/i.test(url);}
function sha16(s){return crypto.createHash("sha256").update(s).digest("hex").slice(0,16);}
function cleanVtt(text){
  return String(text)
    .replace(/^WEBVTT.*$/gmi,"")
    .replace(/^Kind:.*$/gmi,"")
    .replace(/^Language:.*$/gmi,"")
    .replace(/^\d+$/gm,"")
    .replace(/^\d{2}:\d{2}:\d{2}\.\d{3}\s+-->.*$/gm,"")
    .replace(/<[^>]+>/g,"")
    .replace(/&nbsp;/g," ")
    .replace(/&amp;/g,"&")
    .split(/\r?\n/)
    .map(x=>x.trim())
    .filter(Boolean)
    .filter((x,i,a)=>i===0||x!==a[i-1])
    .join("\n")
    .trim();
}
function listSubtitleFiles(prefix){
  return fs.readdirSync(TRANSCRIPTS)
    .filter(f=>f.startsWith(prefix+".") && /\.vtt$/i.test(f))
    .map(f=>path.join(TRANSCRIPTS,f));
}
function chooseSubtitle(files){
  const score=f=>{
    const n=path.basename(f).toLowerCase();
    let s=0;
    if(/\.tr\.vtt$/.test(n))s+=10;
    if(/\.tr-[a-z0-9-]+\.vtt$/.test(n))s+=8;
    if(/\.vtt$/.test(n))s+=1;
    return s;
  };
  return [...files].sort((a,b)=>score(b)-score(a))[0]||null;
}

const files=fs.readdirSync(ITEMS).filter(f=>f.endsWith(".json"));
const videoJobs=[];
const summary={totalVideos:0,youtubeVideos:0,fetched:0,reused:0,missing:0,failed:0,details:[]};

for(const f of files){
  const p=path.join(ITEMS,f);
  const item=readJson(p);
  if(item.itemType!=="VIDEO")continue;
  summary.totalVideos++;
  const url=String(item.contentUrl||"");
  if(!isYoutube(url))continue;
  summary.youtubeVideos++;
  videoJobs.push({p,item,url});
}

function runOne(job){
  const {p,item,url}=job;
  item.payload ||= {};
  item.payload.provenance ||= {};
  const prov=item.payload.provenance;

  const existingPath=prov.transcriptPath ? path.join(ROOT,prov.transcriptPath) : null;
  if(ONLY_MISSING && existingPath && fs.existsSync(existingPath) && prov.transcriptFingerprint && prov.sourceVideoUrl === url){
    summary.reused++;
    summary.details.push({itemId:item.id,status:"reused",path:prov.transcriptPath});
    return;
  }

  const outPrefix=item.id;
  const tmpl=path.join(TRANSCRIPTS,outPrefix+".%(language)s.%(ext)s");
  for(const old of listSubtitleFiles(outPrefix)){
    try{fs.unlinkSync(old);}catch{}
  }

  const args=[
    "--skip-download",
    "--write-subs",
    "--write-auto-subs",
    "--sub-langs","tr,tr-*",
    "--sub-format","vtt",
    "--no-playlist",
    "--quiet",
    "--no-warnings",
    "-o",tmpl,
    url
  ];

  try{
    cp.execFileSync("yt-dlp",args,{stdio:["ignore","pipe","pipe"],timeout:120000,encoding:"utf8"});
    const subtitle=chooseSubtitle(listSubtitleFiles(outPrefix));
    if(!subtitle){
      prov.transcriptLanguage="tr";
      prov.transcriptKind="missing";
      prov.transcriptPath=null;
      prov.transcriptFingerprint=null;
      prov.reviewStatus="needs_review";
      prov.reviewNote="YouTube videosunda erişilebilir Türkçe altyazı/otomatik altyazı bulunamadı.";
      writeJson(p,item);
      summary.missing++;
      summary.details.push({itemId:item.id,status:"missing"});
      return;
    }

    const raw=fs.readFileSync(subtitle,"utf8");
    const normalized=cleanVtt(raw);
    if(!normalized){
      prov.transcriptLanguage="tr";
      prov.transcriptKind="empty";
      prov.transcriptPath=path.relative(ROOT,subtitle);
      prov.transcriptFingerprint=null;
      prov.reviewStatus="needs_review";
      prov.reviewNote="Türkçe altyazı dosyası indirildi ancak normalize edilmiş transcript boş.";
      writeJson(p,item);
      summary.missing++;
      summary.details.push({itemId:item.id,status:"empty",path:prov.transcriptPath});
      return;
    }

    const txtPath=path.join(TRANSCRIPTS,outPrefix+".tr.txt");
    fs.writeFileSync(txtPath,normalized+"\n");
    const kind=/auto|orig|asr/i.test(path.basename(subtitle)) ? "auto" : "subtitle";
    prov.sourceVideoUrl=url;
    prov.transcriptLanguage="tr";
    prov.transcriptKind=kind;
    prov.transcriptPath=path.relative(ROOT,txtPath);
    prov.transcriptRawPath=path.relative(ROOT,subtitle);
    prov.transcriptFingerprint=sha16(normalized);
    if(prov.reviewStatus==="needs_review" && prov.reviewNote && /altyazı|transcript/i.test(prov.reviewNote)){
      delete prov.reviewNote;
    }
    writeJson(p,item);
    summary.fetched++;
    summary.details.push({itemId:item.id,status:"fetched",kind,path:prov.transcriptPath,fingerprint:prov.transcriptFingerprint});
  }catch(e){
    prov.transcriptLanguage="tr";
    prov.transcriptKind="error";
    prov.transcriptPath=null;
    prov.transcriptFingerprint=null;
    prov.reviewStatus="needs_review";
    prov.reviewNote="yt-dlp transcript indirme hatası; uydurma transcript üretilmedi.";
    writeJson(p,item);
    summary.failed++;
    summary.details.push({itemId:item.id,status:"error",message:String(e.message||e).slice(0,200)});
  }
}

async function worker(queue){
  while(true){
    const job=queue.shift();
    if(!job)return;
    runOne(job);
  }
}

(async()=>{
const queue=[...videoJobs];
await Promise.all(Array.from({length:Math.min(CONCURRENCY,queue.length||1)},()=>worker(queue)));

const manifest=path.join(TRANSCRIPTS,"manifest.json");
fs.writeFileSync(manifest,JSON.stringify({...summary,generatedAt:new Date().toISOString()},null,2)+"\n");
console.log(JSON.stringify({...summary,concurrency:CONCURRENCY,manifest:path.relative(ROOT,manifest)},null,2));
})().catch(err=>{console.error(err);process.exit(1);});
