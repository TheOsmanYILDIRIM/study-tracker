#!/usr/bin/env node
/**
 * Topic-granular V2 curriculum rebuild.
 *
 * Source of truth for annual topics lives outside this repo on the Termux device:
 *   ~/projects/lise1-ogrenme-programi/data/defterdoldur_tum_dersler_9al.json
 *
 * This script:
 *  1) derives instructional topic lessons (not weeks),
 *  2) preserves existing item ids/stableKeys,
 *  3) produces yt-dlp candidates for missing video coverage,
 *  4) applies selected direct YouTube URLs conservatively,
 *  5) compiles the modular tree back to the portable catalog.
 *
 * Usage:
 *   node scripts/rebuild-topic-catalog.cjs blueprint
 *   node scripts/rebuild-topic-catalog.cjs candidates
 *   node scripts/rebuild-topic-catalog.cjs apply
 */
const fs = require("fs");
const path = require("path");
const cp = require("child_process");
const crypto = require("crypto");

const ROOT = path.resolve(__dirname, "..");
const V2 = path.join(ROOT, "content", "v2");
const COURSES = path.join(V2, "courses");
const LESSONS = path.join(V2, "lessons");
const ITEMS = path.join(V2, "items");
const BLUEPRINT = path.join(V2, "topic-blueprint.json");
const CANDIDATES = path.join(V2, "video-candidates.json");
const DEFAULT_SOURCE = path.join(
  process.env.HOME || "/data/data/com.termux/files/home",
  "projects/lise1-ogrenme-programi/data/defterdoldur_tum_dersler_9al.json"
);

const COURSE_CONFIG = {
  "Matematik": { id: "course_mat_9", slug: "mat9", search: "9. sınıf matematik yeni müfredat", channelHints: ["Mert Hoca","Rehber Matematik","tonguç 9. SINIF"] },
  "Fizik": { id: "course_fiz_9", slug: "fiz9", search: "9. sınıf fizik yeni müfredat", channelHints: ["Özcan Aykın","VIP Fizik","tonguç 9. SINIF"] },
  "Kimya": { id: "course_kim_9", slug: "kim9", search: "9. sınıf kimya yeni müfredat", channelHints: ["Kimya Adası","Meschemy Kimya","tonguç 9. SINIF"] },
  "Biyoloji": { id: "course_biyo_9", slug: "biyo9", search: "9. sınıf biyoloji yeni müfredat", channelHints: ["Dr. Biyoloji","Biosem","tonguç 9. SINIF"] },
  "Cografya": { id: "course_cog_9", slug: "cog9", search: "9. sınıf coğrafya yeni müfredat", channelHints: ["Coğrafyanın Kodları","tonguç 9. SINIF"] },
  "Tarih": { id: "course_tar_9", slug: "tar9", search: "9. sınıf tarih yeni müfredat Mehmet Celal ÖZYILDIZ", channelHints: ["Mehmet Celal ÖZYILDIZ"], requireChannel: "mehmet celal" },
  "TDE": { id: "course_tde_9", slug: "tde9", search: "9. sınıf Türk dili ve edebiyatı yeni müfredat", channelHints: ["Rüştü Hoca","tonguç 9. SINIF"] },
  "İngilizce": { id: "course_ing_9", slug: "ing9", search: "9. sınıf İngilizce MEB", channelHints: ["Teacher Efe"] },
  "Almanca": { id: "course_alm_9", slug: "alm9", search: "9. sınıf Almanca MEB A1", channelHints: [] }
};

const SKIP_RE = /(sınav|tatil|genel tekrar|dönem sonu|okul temelli|sosyal aktivite|revision|orientation)/i;
const ACTIVE_STATES = new Set(["active"]);

function readJson(p) { return JSON.parse(fs.readFileSync(p, "utf8")); }
function writeJson(p, obj) { fs.mkdirSync(path.dirname(p), {recursive:true}); fs.writeFileSync(p, JSON.stringify(obj, null, 2)+"\n"); }
function norm(s="") {
  return String(s).normalize("NFKD").replace(/[\u0300-\u036f]/g,"")
    .toLocaleLowerCase("tr-TR").replace(/[^a-z0-9çğıöşü]+/gi," ").trim();
}
function slug(s="") {
  return norm(s).replace(/[ç]/g,"c").replace(/[ğ]/g,"g").replace(/[ı]/g,"i")
    .replace(/[ö]/g,"o").replace(/[ş]/g,"s").replace(/[ü]/g,"u")
    .replace(/[^a-z0-9]+/g,"_").replace(/^_+|_+$/g,"").slice(0,52) || "topic";
}
function tokens(s="") {
  return new Set(norm(s).split(/\s+/).filter(x => x.length > 2 && !["sinif","konu","anlatimi","ders","tema","yeni","mufredat"].includes(x)));
}
function similarity(a,b) {
  const A=tokens(a), B=tokens(b);
  if(!A.size||!B.size) return 0;
  let inter=0; for(const x of A) if(B.has(x)) inter++;
  return inter / Math.max(A.size,B.size);
}
function sha16(s){return crypto.createHash("sha256").update(s).digest("hex").slice(0,16);}

function isInstructional(row) {
  const t = [row?.tema,row?.konu,row?.tarih].filter(Boolean).join(" ");
  if (!String(row?.konu||"").trim()) return false;
  if (SKIP_RE.test(t)) return false;
  return true;
}

function cleanTopic(course,row) {
  let k = String(row.konu||"").replace(/\s+/g," ").trim();
  if (course === "Almanca") {
    k = k.replace(/(^|:)\s*Jetzt seid ihr dran!?\s*(?=:|$)/gi," ").replace(/\s*:\s*:/g,":").trim();
    k = k.replace(/^[:\s]+|[:\s]+$/g,"");
  }
  return k;
}

function deriveBlueprint(source) {
  const out = { schemaVersion:"topic-blueprint-v1", generatedAt:new Date().toISOString(), sourcePath:source, courses:[] };
  const raw = readJson(source);
  for (const [courseName,cfg] of Object.entries(COURSE_CONFIG)) {
    const rows = Array.isArray(raw[courseName]) ? raw[courseName] : [];
    const topics=[]; const byKey=new Map();
    for (const row of rows) {
      if (!isInstructional(row)) continue;
      const title=cleanTopic(courseName,row);
      if (!title) continue;
      const theme=String(row.tema||"").trim();
      const key=norm(theme+"|"+title);
      let t=byKey.get(key);
      if(!t){
        t={
          courseName,
          courseId:cfg.id,
          courseSlug:cfg.slug,
          ordinal:topics.length+1,
          title,
          theme,
          weeks:[],
          kazanims:[],
          sourceTopics:[]
        };
        byKey.set(key,t); topics.push(t);
      }
      if(Number.isFinite(Number(row.hafta_no))) t.weeks.push(Number(row.hafta_no));
      if(row.kazanim && !t.kazanims.includes(row.kazanim)) t.kazanims.push(row.kazanim);
      if(row.konu && !t.sourceTopics.includes(row.konu)) t.sourceTopics.push(row.konu);
    }
    // Merge exercise-only German fragments into the preceding same-theme lesson.
    const compact=[];
    for(const t of topics){
      const exerciseOnly = courseName==="Almanca" && /^(jetzt seid ihr dran|rund um den tag)$/i.test(norm(t.title));
      if(exerciseOnly && compact.length && norm(compact[compact.length-1].theme)===norm(t.theme)){
        compact[compact.length-1].weeks.push(...t.weeks);
        compact[compact.length-1].kazanims.push(...t.kazanims.filter(x=>!compact[compact.length-1].kazanims.includes(x)));
      } else compact.push(t);
    }
    compact.forEach((t,i)=>{ t.ordinal=i+1; t.weeks=[...new Set(t.weeks)].sort((a,b)=>a-b); });
    out.courses.push({courseName,courseId:cfg.id,courseSlug:cfg.slug,topics:compact});
  }
  return out;
}

function listCurrentCourseData(courseId){
  const coursePath=path.join(COURSES,courseId+".json");
  if(!fs.existsSync(coursePath)) return null;
  const course=readJson(coursePath);
  const lessons=(course.lessons||[]).map(id=>readJson(path.join(LESSONS,id+".json")));
  const items=[];
  for(const l of lessons) for(const id of l.items||[]) {
    const p=path.join(ITEMS,id+".json"); if(fs.existsSync(p)) items.push(readJson(p));
  }
  return {course,lessons,items};
}

function assignOldLessons(courseBp,current){
  const free=new Set(courseBp.topics.map((_,i)=>i));
  const map=new Map();
  for(const l of current.lessons){
    let best=-1,bestScore=-1;
    for(const i of free){
      const t=courseBp.topics[i];
      const sc=similarity(l.title+" "+(l.stableKey||""),t.title+" "+t.theme+" "+t.kazanims.join(" "));
      if(sc>bestScore){best=i;bestScore=sc;}
    }
    if(best>=0){ map.set(l.id,best); free.delete(best); }
  }
  return map;
}

function topicLessonId(courseBp,t,oldLessonMap,current){
  for(const [oldId,idx] of oldLessonMap.entries()) if(idx===t.ordinal-1) return oldId;
  return `lesson_${courseBp.courseSlug}_topic_${String(t.ordinal).padStart(2,"0")}_${slug(t.title)}`;
}

function bestTopicForItem(item,courseBp,oldLessonMap,current){
  // Preserve old lesson affinity as a strong prior, then refine by item title/payload.
  const oldIdx=oldLessonMap.get(item.lessonId);
  const payload = item.payload || {};
  const hay=[item.title,item.displayLabel,payload.topic,payload?.provenance?.sourceSection,payload?.provenance?.sourceRef].filter(Boolean).join(" ");
  let best=oldIdx ?? 0, bestScore=-1;
  courseBp.topics.forEach((t,i)=>{
    let sc=similarity(hay,t.title+" "+t.theme+" "+t.kazanims.join(" "));
    if(i===oldIdx) sc+=0.18;
    if(sc>bestScore){best=i;bestScore=sc;}
  });
  return best;
}

function ytCandidates(query,limit=5){
  const args=[
    `ytsearch${limit}:${query}`,
    "--flat-playlist","--skip-download",
    "--print","%(id)s\t%(title)s\t%(channel)s\t%(duration)s"
  ];
  try{
    const out=cp.execFileSync("yt-dlp",args,{encoding:"utf8",timeout:30000,stdio:["ignore","pipe","pipe"]});
    return out.split(/\r?\n/).filter(Boolean).map(line=>{
      const [id,title,channel,duration]=line.split("\t");
      return {id,title:title||"",channel:channel||"",duration:Number(duration)||null,url:id?`https://www.youtube.com/watch?v=${id}`:null};
    }).filter(x=>x.id);
  }catch(e){return [];}
}
function candidateScore(topic,c,cfg){
  let s=0;
  s += similarity(topic.title+" "+topic.theme,c.title)*10;
  const nt=norm(c.title), nc=norm(c.channel);
  if(/9\.?\s*sinif/.test(nt)||nt.includes("9 sinif")) s+=2.5;
  if(nt.includes("yeni mufredat")||nt.includes("maarif")) s+=1.2;
  if((cfg.channelHints||[]).some(h=>nc.includes(norm(h)))) s+=1.5;
  if(cfg.requireChannel && !nc.includes(cfg.requireChannel)) return -999;
  const d=c.duration||0;
  if(d>=240 && d<=5400) s+=0.5;
  return Math.round(s*100)/100;
}

function generateCandidates(bp){
  const out={schemaVersion:"video-candidates-v1",generatedAt:new Date().toISOString(),topics:[]};
  for(const c of bp.courses){
    const cfg=COURSE_CONFIG[c.courseName];
    const current=listCurrentCourseData(c.courseId);
    for(const t of c.topics){
      const existing=(current?.items||[]).filter(i=>i.itemType==="VIDEO" && i.contentUrl);
      const bestExisting=existing.map(i=>({i,score:similarity(i.title||"",t.title)})).sort((a,b)=>b.score-a.score)[0];
      if(bestExisting && bestExisting.score>=0.45){
        out.topics.push({courseName:c.courseName,ordinal:t.ordinal,title:t.title,existingItemId:bestExisting.i.id,selected:{url:bestExisting.i.contentUrl,title:bestExisting.i.title,channel:bestExisting.i.payload?.provider||"",score:99,source:"existing"},candidates:[]});
        continue;
      }
      const hint=(cfg.channelHints||[])[0]||"";
      const query=[cfg.search,t.title,hint].filter(Boolean).join(" ");
      const candidates=ytCandidates(query,5).map(x=>({...x,score:candidateScore(t,x,cfg)})).sort((a,b)=>b.score-a.score);
      const top=candidates[0]||null;
      out.topics.push({courseName:c.courseName,ordinal:t.ordinal,title:t.title,query,selected:top && top.score>=3.0 ? {...top,source:"yt-dlp"} : null,candidates});
    }
  }
  return out;
}

function apply(bp,cands){
  const candMap=new Map(cands.topics.map(x=>[`${x.courseName}#${x.ordinal}`,x]));
  let newLessons=0,newVideos=0,movedItems=0,activatedVideos=0;
  const referencedLessons=new Set();

  for(const courseBp of bp.courses){
    const current=listCurrentCourseData(courseBp.courseId);
    if(!current) continue;
    const oldLessonMap=assignOldLessons(courseBp,current);
    const topicLessonIds=courseBp.topics.map(t=>topicLessonId(courseBp,t,oldLessonMap,current));
    const buckets=new Map(topicLessonIds.map(id=>[id,[]]));

    // Move existing items without changing item identity.
    for(const item of current.items){
      const idx=bestTopicForItem(item,courseBp,oldLessonMap,current);
      const newLessonId=topicLessonIds[idx];
      if(item.lessonId!==newLessonId){item.lessonId=newLessonId;movedItems++;}
      writeJson(path.join(ITEMS,item.id+".json"),item);
      buckets.get(newLessonId).push(item.id);
    }

    // Ensure one video item per instructional topic, reusing matching existing videos when possible.
    for(const t of courseBp.topics){
      const lessonId=topicLessonIds[t.ordinal-1];
      const bucket=buckets.get(lessonId);
      const bucketVideos=bucket.map(id=>readJson(path.join(ITEMS,id+".json"))).filter(i=>i.itemType==="VIDEO");
      if(bucketVideos.length===0){
        const id=`item_${courseBp.courseSlug}_vid_topic_${String(t.ordinal).padStart(2,"0")}_${slug(t.title)}`;
        const stableKey=id.replace(/^item_/,"");
        const cm=candMap.get(`${courseBp.courseName}#${t.ordinal}`);
        const sel=cm?.selected||null;
        const verified=Boolean(sel && sel.score>=4.2);
        const item={
          id,courseId:courseBp.courseId,lessonId,stableKey,itemType:"VIDEO",
          displayLabel:`${t.ordinal}.1`,orderKey:1000,
          title:t.title,contentUrl:sel?.url||null,publishingStatus:verified?"active":"draft",
          payload:{
            provider:sel?.channel||null,
            topic:t.title,
            provenance:{
              sourceRef:"lise1-ogrenme-programi/data/defterdoldur_tum_dersler_9al.json",
              sourceWeeks:t.weeks,
              sourceTheme:t.theme,
              sourceTopic:t.sourceTopics,
              reviewStatus:verified?"verified":"needs_review",
              videoSearchScore:sel?.score??null,
              videoCandidateTitle:sel?.title||null,
              fingerprint:sha16([stableKey,t.title,sel?.url||""].join("|")),
              schemaVersion:"v2"
            }
          }
        };
        writeJson(path.join(ITEMS,id+".json"),item); bucket.unshift(id); newVideos++;
      } else {
        // Fill a missing URL on the first existing video only when candidate is strong.
        const first=bucketVideos[0];
        const cm=candMap.get(`${courseBp.courseName}#${t.ordinal}`);
        const sel=cm?.selected||null;
        if(!first.contentUrl && sel && sel.score>=4.2){
          first.contentUrl=sel.url;
          first.publishingStatus="active";
          first.title=first.title||t.title;
          first.payload=first.payload||{};
          first.payload.provider=sel.channel||first.payload.provider||null;
          first.payload.topic=t.title;
          first.payload.provenance={...(first.payload.provenance||{}),sourceWeeks:t.weeks,sourceTheme:t.theme,reviewStatus:"verified",videoSearchScore:sel.score,videoCandidateTitle:sel.title,fingerprint:sha16([first.stableKey,t.title,sel.url].join("|"))};
          writeJson(path.join(ITEMS,first.id+".json"),first); activatedVideos++;
        }
      }
    }

    // Normalize item order within each new lesson while preserving IDs.
    for(const t of courseBp.topics){
      const id=topicLessonIds[t.ordinal-1];
      referencedLessons.add(id);
      const refs=buckets.get(id);
      const objs=refs.map(x=>readJson(path.join(ITEMS,x+".json")));
      objs.sort((a,b)=>(a.orderKey||0)-(b.orderKey||0)||String(a.id).localeCompare(String(b.id)));
      objs.forEach((o,i)=>{o.orderKey=(i+1)*1000;writeJson(path.join(ITEMS,o.id+".json"),o);});
      const old=current.lessons.find(x=>x.id===id);
      const lesson={
        id,courseId:courseBp.courseId,
        stableKey:old?.stableKey||id,
        title:t.title,
        orderKey:t.ordinal*1000,
        items:objs.map(x=>x.id),
        sourceMeta:{weeks:t.weeks,theme:t.theme,sourceTopics:t.sourceTopics,kazanims:t.kazanims}
      };
      if(!old)newLessons++;
      writeJson(path.join(LESSONS,id+".json"),lesson);
    }
    current.course.lessons=topicLessonIds;
    writeJson(path.join(COURSES,courseBp.courseId+".json"),current.course);
  }

  // Delete only old lesson files that are no longer referenced by any course.
  for(const f of fs.readdirSync(LESSONS).filter(x=>x.endsWith(".json"))){
    const id=f.slice(0,-5);
    if(!referencedLessons.has(id)){
      let referenced=false;
      for(const cf of fs.readdirSync(COURSES).filter(x=>x.endsWith(".json"))){
        if((readJson(path.join(COURSES,cf)).lessons||[]).includes(id)){referenced=true;break;}
      }
      if(!referenced) fs.unlinkSync(path.join(LESSONS,f));
    }
  }
  return {newLessons,newVideos,movedItems,activatedVideos};
}

function compile(){
  const {validateModularTree,compileModularCatalog,saveCompiledCatalog}=require("../cli/lib/v2-modular");
  const validation=validateModularTree(V2);
  if(!validation.valid) throw new Error("Validation failed: "+validation.errors.join("; "));
  const compiled=compileModularCatalog(V2);
  saveCompiledCatalog(compiled,path.join(ROOT,"content","9-sinif-v2-catalog.json"));
  return validation.stats;
}

function main(){
  const mode=process.argv[2]||"blueprint";
  const source=process.env.STUDYTRACKER_ANNUAL_SOURCE||DEFAULT_SOURCE;
  if(mode==="blueprint"){
    if(!fs.existsSync(source)) throw new Error("Annual source not found: "+source);
    const bp=deriveBlueprint(source); writeJson(BLUEPRINT,bp);
    console.log(JSON.stringify({ok:true,mode,source,courses:bp.courses.length,topics:bp.courses.reduce((n,c)=>n+c.topics.length,0),output:BLUEPRINT},null,2));
    return;
  }
  if(!fs.existsSync(BLUEPRINT)) throw new Error("Run blueprint first");
  const bp=readJson(BLUEPRINT);
  if(mode==="candidates"){
    const cs=generateCandidates(bp); writeJson(CANDIDATES,cs);
    const selected=cs.topics.filter(x=>x.selected).length;
    console.log(JSON.stringify({ok:true,mode,topics:cs.topics.length,selected,unresolved:cs.topics.length-selected,output:CANDIDATES},null,2));
    return;
  }
  if(mode==="apply"){
    if(!fs.existsSync(CANDIDATES)) throw new Error("Run candidates first");
    const result=apply(bp,readJson(CANDIDATES));
    const stats=compile();
    console.log(JSON.stringify({ok:true,mode,...result,stats},null,2));
    return;
  }
  throw new Error("Unknown mode: "+mode);
}
if(require.main===module) main();
module.exports={deriveBlueprint,generateCandidates,apply};
