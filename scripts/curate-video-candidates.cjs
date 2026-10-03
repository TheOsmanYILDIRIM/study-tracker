#!/usr/bin/env node
/**
 * Curate existing video-candidates.json WITHOUT doing any network search.
 * Goal: continuity — prefer one narrator/channel per course.
 *
 * Usage:
 *   node scripts/curate-video-candidates.cjs
 */
const fs=require("fs");
const path=require("path");

const ROOT=path.resolve(__dirname,"..");
const CANDIDATES=path.join(ROOT,"content","v2","video-candidates.json");
const OUT=path.join(ROOT,"content","v2","video-candidates.curated.json");

const POLICY={
  "Matematik":{
    primary:["mert hoca"],
    secondary:["rehber matematik","tonguc 9 sinif","tonguç 9 sınıf"]
  },
  "Fizik":{
    primary:["ozcan aykin fizik","özcan aykın fizik"],
    secondary:["vip fizik","fizikfinito","altug gunes fizik","altuğ güneş fizik"]
  },
  "Kimya":{
    primary:["kimya adasi","kimya adası"],
    secondary:["meschemy kimya","tonguc 9 sinif","tonguç 9 sınıf"]
  },
  "Biyoloji":{
    primary:["dr biyoloji"],
    secondary:["biosem","tonguc 9 sinif","tonguç 9 sınıf"]
  },
  "Cografya":{
    primary:["cografyanin kodlari","coğrafyanın kodları"],
    secondary:["tonguc 9 sinif","tonguç 9 sınıf"]
  },
  "Tarih":{
    primary:["mehmet celal ozyildiz","mehmet celal özyıldız"],
    secondary:[]
  },
  "TDE":{
    primary:["rustu hoca","rüştü hoca"],
    secondary:["tonguc 9 sinif","tonguç 9 sınıf"]
  },
  "İngilizce":{
    primary:["teacher efe"],
    secondary:[]
  },
  "Almanca":{
    primary:[],
    secondary:[]
  }
};

function norm(s=""){
  return String(s).normalize("NFKD").replace(/[\u0300-\u036f]/g,"")
    .toLocaleLowerCase("tr-TR").replace(/[^a-z0-9çğıöşü]+/gi," ").trim();
}
function toks(s=""){
  return new Set(norm(s).split(/\s+/).filter(x=>x.length>2 && !["sinif","konu","anlatimi","ders","tema","yeni","mufredat"].includes(x)));
}
function overlap(a,b){
  const A=toks(a),B=toks(b); if(!A.size||!B.size)return 0;
  let n=0; for(const x of A)if(B.has(x))n++;
  return n/Math.max(A.size,B.size);
}
function matchChannel(channel,names){
  const c=norm(channel);
  return names.some(n=>c.includes(norm(n)));
}
function quality(topic,c){
  if(!c||!c.url)return -999;
  let s=Number(c.score)||0;
  s+=overlap(topic.title,c.title)*8;
  const title=norm(c.title);
  if(title.includes("9 sinif"))s+=1.5;
  if(title.includes("yeni mufredat")||title.includes("maarif"))s+=0.8;
  const d=Number(c.duration)||0;
  if(d>=180&&d<=5400)s+=0.4;
  return s;
}

if(!fs.existsSync(CANDIDATES)){
  console.error("Missing "+CANDIDATES);
  process.exit(2);
}
const data=JSON.parse(fs.readFileSync(CANDIDATES,"utf8"));
const topics=Array.isArray(data.topics)?data.topics:[];

// For courses with no explicit primary (currently German), infer one dominant
// channel from strong existing candidates so continuity still wins.
const inferred={};
for(const course of Object.keys(POLICY)){
  if(POLICY[course].primary.length)continue;
  const counts=new Map();
  for(const t of topics.filter(x=>x.courseName===course)){
    const pool=[...(t.candidates||[])];
    if(t.selected)pool.push(t.selected);
    for(const c of pool){
      if(quality(t,c)<2.5)continue;
      const key=String(c.channel||"").trim(); if(!key)continue;
      counts.set(key,(counts.get(key)||0)+1);
    }
  }
  inferred[course]=[...counts.entries()].sort((a,b)=>b[1]-a[1])[0]?.[0]||null;
}

const curated=[];
const summary={};
for(const t of topics){
  const p=POLICY[t.courseName]||{primary:[],secondary:[]};
  const pool=[];
  const seen=new Set();
  for(const c of [t.selected,...(t.candidates||[])].filter(Boolean)){
    if(!c.url||seen.has(c.url))continue;
    seen.add(c.url); pool.push(c);
  }
  let tier="none";
  let allowed=[];
  if(p.primary.length){
    allowed=pool.filter(c=>matchChannel(c.channel,p.primary));
    if(allowed.length)tier="primary";
    else{
      allowed=pool.filter(c=>matchChannel(c.channel,p.secondary));
      if(allowed.length)tier="secondary";
    }
  }else if(inferred[t.courseName]){
    allowed=pool.filter(c=>norm(c.channel)===norm(inferred[t.courseName]));
    if(allowed.length)tier="inferred-primary";
  }

  // Never force a random outside-channel video just to fill the lesson.
  const ranked=allowed.map(c=>({...c,_q:quality(t,c)})).sort((a,b)=>b._q-a._q);
  const best=ranked[0]||null;
  const selected=best&&best._q>=2.8
    ? {...best,score:Math.round(best._q*100)/100,source:"curated-existing-candidates",channelTier:tier}
    : null;
  if(selected)delete selected._q;

  curated.push({...t,selected,curation:{
    policyPrimary:p.primary,
    policySecondary:p.secondary,
    inferredPrimary:inferred[t.courseName]||null,
    channelTier:tier,
    status:selected?"selected":"needs_review"
  }});

  const s=summary[t.courseName] ||= {total:0,selected:0,unresolved:0,channels:{}};
  s.total++;
  if(selected){
    s.selected++;
    s.channels[selected.channel]=(s.channels[selected.channel]||0)+1;
  }else s.unresolved++;
}

fs.writeFileSync(OUT,JSON.stringify({...data,generatedAt:new Date().toISOString(),curationVersion:"continuity-v1",topics:curated},null,2)+"\n");
console.log(JSON.stringify({ok:true,out:OUT,inferred,summary},null,2));
