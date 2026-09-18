#!/data/data/com.termux/files/usr/bin/env node

/**
 * StudyTracker Parenting & AI Cloud Administration CLI
 * Zero-dependency Node.js CLI tool for Cloudflare KV Sync, plan creation, task editing & student reviewing.
 */

const fs = require('fs');
const path = require('path');
const { loadConfig, setFamilyCode } = require('../lib/config');
const { fetchFamilyData, pushFamilyData, pairFamily } = require('../lib/api');
const { parseDSL, exportToDSL } = require('../lib/dsl-parser');
const { colors, statusBadge, printHeader, renderDashboard } = require('../lib/renderer');

function printUsage() {
  console.log(`
${colors.bold}${colors.brightCyan}StudyTracker CLI - Kullanım Rehberi${colors.reset}

${colors.bold}Temel Komutlar:${colors.reset}
  ${colors.green}studytracker-cli status${colors.reset}                     Güncel durumu, dersleri ve onay bekleyenleri listeler
  ${colors.green}studytracker-cli status --json${colors.reset}              Tüm veriyi ham JSON olarak çıktılar (AI & Otomasyon)
  
${colors.bold}Haftalık Plan Yönetimi (Veli / AI):${colors.reset}
  ${colors.green}studytracker-cli plan show${colors.reset}                  Mevcut haftalık planı DSL formatında gösterir
  ${colors.green}studytracker-cli plan apply --file <dosya>${colors.reset}   DSL dosyasından yeni planı buluta ve öğrenciye yükler
  ${colors.green}studytracker-cli plan set "<DSL Metni>"${colors.reset}      Doğrudan DSL metnini buluta yükler
  
${colors.bold}Öğrenci Çalışmalarını İnceleme & Onay:${colors.reset}
  ${colors.green}studytracker-cli review list${colors.reset}                Onay bekleyen öğrenci oturumlarını listeler
  ${colors.green}studytracker-cli approve <sessionId|occKey>${colors.reset}  Çalışmayı onaylar ve takımyıldızını parlatır
  ${colors.green}studytracker-cli reject <sessionId|occKey> --note "..."${colors.reset} Çalışmayı reddeder ve öğrenciye not iletir
  
${colors.bold}Ders / Görev İşlemleri:${colors.reset}
  ${colors.green}studytracker-cli task add --title "..." --day "Pzt" --min 35 [--video "..."]${colors.reset}  Tek ders ekler
  ${colors.green}studytracker-cli task edit <occKey> [--title "..."] [--min 40] [--video "..."]${colors.reset} Dersi düzenler
  ${colors.green}studytracker-cli task delete <occKey>${colors.reset}       Dersi programdan ve buluttan kalıcı siler
  
${colors.bold}Sıfırlama & Yapılandırma:${colors.reset}
  ${colors.green}studytracker-cli reset [--full]${colors.reset}              İlerlemeyi sıfırlar (veya --full ile temiz masa)
  ${colors.green}studytracker-cli config set-code <ST-XXXX>${colors.reset}   Varsayılan aile kodunu kaydeder
  ${colors.green}studytracker-cli config get-code${colors.reset}             Aktif aile kodunu gösterir

${colors.dim}Global parametreler: --code <ST-XXXX>, --json${colors.reset}
`);
}

function parseArgs(args) {
  const result = {
    command: args[0],
    subcommand: args[1],
    options: {},
    positionals: []
  };

  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (arg.startsWith('--')) {
      const key = arg.slice(2);
      const next = args[i + 1];
      if (next && !next.startsWith('--')) {
        result.options[key] = next;
        i++;
      } else {
        result.options[key] = true;
      }
    } else if (arg.startsWith('-')) {
      const key = arg.slice(1);
      result.options[key] = true;
    } else {
      result.positionals.push(arg);
    }
  }

  return result;
}

// Helper to resolve occurrence by index (1-based), exact ID, or subject substring
function findOccurrence(occurrences, target) {
  if (!occurrences || occurrences.length === 0 || !target) return null;
  const num = parseInt(target, 10);
  if (!isNaN(num) && num >= 1 && num <= occurrences.length) {
    return occurrences[num - 1];
  }
  const exact = occurrences.find(o => o.id === target || o.planId === target);
  if (exact) return exact;
  const lower = target.toLowerCase();
  return occurrences.find(o => (o.subject && o.subject.toLowerCase().includes(lower)) || o.id.toLowerCase().includes(lower)) || null;
}

async function main() {
  const parsed = parseArgs(process.argv.slice(2));
  const cmd = parsed.positionals[0];
  const sub = parsed.positionals[1];
  const config = loadConfig();
  const familyCode = parsed.options.code || config.familyCode || 'ST-2026';

  if (!cmd || cmd === 'help' || parsed.options.help || parsed.options.h) {
    printUsage();
    return;
  }

  try {
    switch (cmd) {
      // 1. STATUS
      case 'status':
      case 'get': {
        const data = await fetchFamilyData(familyCode);
        if (parsed.options.json) {
          console.log(JSON.stringify(data, null, 2));
        } else {
          renderDashboard(data);
        }
        break;
      }

      // 2. PLAN
      case 'plan': {
        const data = await fetchFamilyData(familyCode);

        if (sub === 'show') {
          if (!data.plan && (!data.occurrences || data.occurrences.length === 0)) {
            console.log(`${colors.yellow}Bulutta aktif bir plan bulunmuyor (${familyCode}).${colors.reset}`);
            return;
          }
          const dsl = exportToDSL(data.plan, data.tasks, data.occurrences);
          if (parsed.options.json) {
            console.log(JSON.stringify({ plan: data.plan, tasks: data.tasks, dsl }, null, 2));
          } else {
            console.log(`${colors.bold}${colors.brightCyan}=== HAFTALIK PLAN DSL (${data.plan?.weekId || 'Aktif'}) ===${colors.reset}\n`);
            console.log(dsl);
          }
        } else if (sub === 'apply' || sub === 'set') {
          let dslText = '';
          const potentialFile = parsed.options.file || parsed.positionals[2];
          if (potentialFile && fs.existsSync(path.resolve(process.cwd(), potentialFile))) {
            const filePath = path.resolve(process.cwd(), potentialFile);
            dslText = fs.readFileSync(filePath, 'utf8');
          } else if (parsed.positionals[2]) {
            dslText = parsed.positionals.slice(2).join(' ');
          } else {
            console.error(`${colors.red}Hata: --file <dosya_yolu> veya inline DSL metni belirtilmelidir.${colors.reset}`);
            process.exit(1);
          }

          console.log(`${colors.cyan}⏳ Plan ayrıştırılıyor ve Cloudflare KV'ye yükleniyor (${familyCode})...${colors.reset}`);
          const parsedPlan = parseDSL(dslText, familyCode);
          
          // Reconcile existing occurrence statuses (preserve APPROVED / WAITING_REVIEW)
          const prevOccMap = new Map((data.occurrences || []).map(o => [o.id, o]));
          const mergedOccurrences = parsedPlan.occurrences.map(newOcc => {
            const prev = prevOccMap.get(newOcc.id) || 
                         (data.occurrences || []).find(o => o.planId === newOcc.planId && o.date === newOcc.date);
            if (!prev) return newOcc;
            return {
              ...newOcc,
              status: (prev.status === 'APPROVED' || prev.status === 'WAITING_REVIEW') ? prev.status : newOcc.status,
              completedDurationMin: prev.completedDurationMin || 0,
              completedQuestionCount: prev.completedQuestionCount || 0,
              studentNote: prev.studentNote || null,
              parentNote: prev.parentNote || ''
            };
          });

          // Preserve existing completed sessions, screenshots, reviews and quizzes
          const payload = {
            familyCode,
            plan: parsedPlan.plan,
            tasks: parsedPlan.tasks,
            occurrences: mergedOccurrences,
            sessions: data.sessions || [],
            screenshots: data.screenshots || [],
            reviews: data.reviews || [],
            quizzes: data.quizzes || []
          };

          await pushFamilyData(payload, familyCode, 'PARENT');
          console.log(`${colors.green}✔ Plan başarıyla yüklendi!${colors.reset}`);
          console.log(`  • Hafta      : ${parsedPlan.plan.weekId}`);
          console.log(`  • Ders Sayısı: ${parsedPlan.occurrences.length}`);
          console.log(`  • Şablonlar  : ${parsedPlan.tasks.length}`);
          console.log(`\n${colors.dim}Öğrenci ve Veli uygulamaları açıldığında veya yenilendiğinde otomatik senkronize olacaktır.${colors.reset}`);
        } else {
          console.log(`${colors.red}Bilinmeyen alt komut: plan ${sub}. Kullanım: plan show | plan apply --file <path>${colors.reset}`);
        }
        break;
      }

      // 3. REVIEW / APPROVE / REJECT
      case 'review': {
        const data = await fetchFamilyData(familyCode);
        if (sub === 'list') {
          const waiting = (data.occurrences || []).filter(o => o.status === 'WAITING_REVIEW');
          const sessions = (data.sessions || []).filter(s => s.isCompleted);
          console.log(`\n${colors.bold}${colors.brightYellow}🔍 Onay Bekleyen Dersler (${waiting.length}):${colors.reset}`);
          for (let i = 0; i < waiting.length; i++) {
            const occ = waiting[i];
            console.log(`  [${i + 1}] ${colors.bold}${occ.id}${colors.reset} : ${colors.brightWhite}${occ.subject}${colors.reset} (${occ.date || 'Haftalık'})`);
            if (occ.studentNote) console.log(`      Öğrenci Notu: ${colors.italic}${occ.studentNote}${colors.reset}`);
          }
          if (sessions.length > 0) {
            console.log(`\n${colors.bold}📸 Oturum Kayıtları (${sessions.length}):${colors.reset}`);
            for (const s of sessions) {
              console.log(`  • ID: ${s.id} | Ders: ${s.occurrenceId} | Süre: ${s.durationMin} dk | Not: ${s.notes || '-'}`);
            }
          }
        } else {
          console.log(`${colors.red}Kullanım: studytracker-cli review list${colors.reset}`);
        }
        break;
      }

      case 'approve': {
        const target = parsed.positionals[1];
        if (!target) {
          console.error(`${colors.red}Hata: Onaylanacak ders sıra numarası veya ID'si belirtilmeli (Örn: approve 1 veya approve 2026-W38_MON_mat_1)${colors.reset}`);
          process.exit(1);
        }

        const data = await fetchFamilyData(familyCode);
        const occ = findOccurrence(data.occurrences, target);

        if (!occ) {
          console.error(`${colors.red}Hata: Ders bulunamadı: ${target}${colors.reset}`);
          process.exit(1);
        }

        occ.status = 'APPROVED';
        occ.parentNote = parsed.options.note || 'Tebrikler! Çalışman onaylandı.';
        occ.completedQuestionCount = Math.max(occ.completedQuestionCount || 0, occ.targetQuestionCount || 1);

        data.reviews = data.reviews || [];
        const existingRevIdx = data.reviews.findIndex(r => r.sessionId === occ.id || r.sessionId === occ.planId);
        const reviewRecord = {
          id: `rev_${Date.now()}`,
          familyCode,
          sessionId: occ.id,
          isApproved: true,
          rejectionReason: null,
          parentRating: 5,
          feedbackNote: parsed.options.note || 'Harika çalışma!',
          reviewedAt: Date.now()
        };
        if (existingRevIdx !== -1) {
          data.reviews[existingRevIdx] = reviewRecord;
        } else {
          data.reviews.push(reviewRecord);
        }

        await pushFamilyData(data, familyCode, 'PARENT');
        console.log(`${colors.green}✔ '${occ.subject}' [${occ.id}] başarıyla ONAYLANDI ve buluta işlendi.${colors.reset}`);
        console.log(`${colors.dim}Öğrenci uygulamasında takımyıldızı animasyonu tetiklenecektir.${colors.reset}`);
        break;
      }

      case 'reject': {
        const target = parsed.positionals[1];
        const note = parsed.options.note;
        if (!target) {
          console.error(`${colors.red}Hata: Reddedilecek ders sıra numarası veya ID'si belirtilmeli.${colors.reset}`);
          process.exit(1);
        }
        if (!note) {
          console.error(`${colors.red}Hata: Reddetme sebebi için --note "..." belirtilmelidir.${colors.reset}`);
          process.exit(1);
        }

        const data = await fetchFamilyData(familyCode);
        const occ = findOccurrence(data.occurrences, target);

        if (!occ) {
          console.error(`${colors.red}Hata: Ders bulunamadı: ${target}${colors.reset}`);
          process.exit(1);
        }

        occ.status = 'PENDING';
        occ.parentNote = note;

        data.reviews = data.reviews || [];
        const existingRevIdx = data.reviews.findIndex(r => r.sessionId === occ.id || r.sessionId === occ.planId);
        const reviewRecord = {
          id: `rev_${Date.now()}`,
          familyCode,
          sessionId: occ.id,
          isApproved: false,
          rejectionReason: note,
          parentRating: 1,
          feedbackNote: note,
          reviewedAt: Date.now()
        };
        if (existingRevIdx !== -1) {
          data.reviews[existingRevIdx] = reviewRecord;
        } else {
          data.reviews.push(reviewRecord);
        }

        await pushFamilyData(data, familyCode, 'PARENT');
        console.log(`${colors.brightRed}✔ '${occ.subject}' [${occ.id}] REDDEDİLDİ ve öğrenciye geri bildirim notu iletildi.${colors.reset}`);
        break;
      }

      // 4. TASK (LIST, ADD, EDIT, DELETE)
      case 'task': {
        const data = await fetchFamilyData(familyCode);
        data.occurrences = data.occurrences || [];
        data.tasks = data.tasks || [];

        if (sub === 'list') {
          if (data.occurrences.length === 0) {
            console.log(`${colors.yellow}Mevcut planda ders bulunmuyor.${colors.reset}`);
            return;
          }
          console.log(`\n${colors.bold}${colors.brightCyan}📋 Plandaki Dersler (${data.occurrences.length}):${colors.reset}\n`);
          data.occurrences.forEach((occ, idx) => {
            const num = `[${(idx + 1).toString().padStart(2, ' ')}]`;
            const dayBadge = occ.date ? `[${occ.date}]` : '[HAFTA]';
            const vid = occ.youtubeUrl ? '🎬 ' : '   ';
            const dur = `${occ.targetDurationMin || 30} dk`;
            const q = occ.targetQuestionCount > 0 ? ` • ${occ.targetQuestionCount} Soru` : '';
            const status = statusBadge(occ.status);
            console.log(`  ${colors.brightYellow}${num}${colors.reset} ${colors.cyan}${dayBadge.padEnd(8, ' ')}${colors.reset} ${vid}${colors.bold}${occ.subject.padEnd(42, ' ')}${colors.reset} ${colors.dim}(${dur}${q})${colors.reset} ${status}`);
            console.log(`       ${colors.dim}ID: ${occ.id}${occ.youtubeUrl ? ` | URL: ${occ.youtubeUrl}` : ''}${colors.reset}`);
          });
          console.log(`\n${colors.dim}İpucu: Bir dersi silmek için 'studytracker-cli task delete <numara>', düzenlemek için 'studytracker-cli task edit <numara>' kullanabilirsiniz.${colors.reset}`);
        } else if (sub === 'add') {
          const title = parsed.options.title;
          const day = (parsed.options.day || 'MON').toUpperCase();
          const min = parseInt(parsed.options.min || '30', 10);
          const video = parsed.options.video || null;
          const note = parsed.options.note || '';
          const questions = parseInt(parsed.options.questions || '0', 10);

          if (!title) {
            console.error(`${colors.red}Hata: --title "Ders Adı" parametresi zorunludur.${colors.reset}`);
            process.exit(1);
          }

          const taskId = `task_${Date.now().toString().slice(-4)}`;
          const weekId = data.plan?.weekId || '2026-W38';
          const occKey = `${weekId}_${day}_${taskId}`;

          data.tasks.push({
            taskId,
            title,
            kind: day ? 'DAILY' : 'WEEKLY',
            contentType: video ? 'VIDEO' : 'OTHER',
            youtubeUrl: video,
            plannedMinutes: min,
            targetMode: questions > 0 ? 'QUESTIONS' : null,
            targetCount: questions > 0 ? questions : null,
            targetMinutes: min,
            reviewRequired: true,
            active: true
          });

          data.occurrences.push({
            id: occKey,
            familyCode,
            date: day,
            planId: taskId,
            subject: title,
            topic: day ? 'DAILY' : 'WEEKLY',
            targetDurationMin: min,
            targetQuestionCount: questions,
            completedDurationMin: 0,
            completedQuestionCount: 0,
            status: 'PENDING',
            parentNote: note,
            weekId,
            orderIndex: data.occurrences.length,
            studentNote: null,
            youtubeUrl: video,
            updatedAt: Date.now()
          });

          await pushFamilyData(data, familyCode, 'PARENT');
          console.log(`${colors.green}✔ Yeni ders eklendi: '${title}' (${day} - ${min} dk) [ID: ${occKey}]${colors.reset}`);
        } else if (sub === 'edit') {
          const target = parsed.positionals[2];
          if (!target) {
            console.error(`${colors.red}Hata: Düzenlenecek ders numarası veya ID'si belirtilmelidir (Örn: task edit 3 --min 40).${colors.reset}`);
            process.exit(1);
          }

          const occ = findOccurrence(data.occurrences, target);
          if (!occ) {
            console.error(`${colors.red}Hata: Ders bulunamadı: ${target}${colors.reset}`);
            process.exit(1);
          }

          if (parsed.options.title) occ.subject = parsed.options.title;
          if (parsed.options.min) occ.targetDurationMin = parseInt(parsed.options.min, 10);
          if (parsed.options.video !== undefined) occ.youtubeUrl = parsed.options.video || null;
          if (parsed.options.note !== undefined) occ.parentNote = parsed.options.note;
          if (parsed.options.day) occ.date = parsed.options.day.toUpperCase();
          if (parsed.options.questions !== undefined) occ.targetQuestionCount = parseInt(parsed.options.questions, 10);

          // Sync task template
          const taskTmpl = data.tasks.find(t => t.taskId === occ.planId);
          if (taskTmpl) {
            if (parsed.options.title) taskTmpl.title = parsed.options.title;
            if (parsed.options.min) taskTmpl.plannedMinutes = parseInt(parsed.options.min, 10);
            if (parsed.options.video !== undefined) taskTmpl.youtubeUrl = parsed.options.video || null;
          }

          await pushFamilyData(data, familyCode, 'PARENT');
          console.log(`${colors.green}✔ Ders başarıyla güncellendi: '${occ.subject}' [${occ.id}]${colors.reset}`);
        } else if (sub === 'delete') {
          const target = parsed.positionals[2];
          if (!target) {
            console.error(`${colors.red}Hata: Silinecek ders numarası veya ID'si belirtilmelidir (Örn: task delete 5 veya task delete 2026-W38_MON_mat_1).${colors.reset}`);
            process.exit(1);
          }

          const occ = findOccurrence(data.occurrences, target);
          if (!occ) {
            console.error(`${colors.red}Hata: Belirtilen numara veya ID ile eşleşen ders bulunamadı: ${target}${colors.reset}`);
            process.exit(1);
          }

          const deletedSubject = occ.subject;
          const deletedId = occ.id;
          const deletedPlanId = occ.planId;

          // Remove occurrence
          data.occurrences = data.occurrences.filter(o => o.id !== deletedId);

          // Clean template if no other occurrence references it
          const stillUsed = data.occurrences.some(o => o.planId === deletedPlanId);
          if (!stillUsed) {
            data.tasks = data.tasks.filter(t => t.taskId !== deletedPlanId);
          }

          await pushFamilyData(data, familyCode, 'PARENT');
          console.log(`${colors.green}✔ Ders programdan ve buluttan kalıcı olarak silindi: '${deletedSubject}' [${deletedId}]${colors.reset}`);
          console.log(`  Kalan ders sayısı: ${data.occurrences.length}`);
          console.log(`${colors.dim}Veli ve Öğrenci uygulamaları eşitlendiğinde bu ders otomatik olarak silinecektir.${colors.reset}`);
        } else {
          console.log(`${colors.red}Kullanım: studytracker-cli task list | add | edit | delete ...${colors.reset}`);
        }
        break;
      }

      // 5. RESET
      case 'reset': {
        const isFull = parsed.options.full === true;
        const data = await fetchFamilyData(familyCode);

        if (isFull) {
          data.plan = null;
          data.tasks = [];
          data.occurrences = [];
          data.sessions = [];
          data.reviews = [];
          data.quizzes = [];
          data.action = 'WIPE';
          await pushFamilyData(data, familyCode, 'PARENT');
          console.log(`${colors.brightRed}✔ Tüm plan, dersler ve testler tamamen temizlendi (Temiz Masa).${colors.reset}`);
        } else {
          for (const occ of (data.occurrences || [])) {
            occ.status = 'PENDING';
            occ.completedDurationMin = 0;
            occ.completedQuestionCount = 0;
            occ.studentNote = null;
          }
          data.sessions = [];
          data.reviews = [];
          data.action = 'RESET';
          await pushFamilyData(data, familyCode, 'PARENT');
          console.log(`${colors.green}✔ Öğrenci çalışma ilerlemeleri ve süreleri sıfırlandı (Plan korundu).${colors.reset}`);
        }
        break;
      }

      // 6. CONFIG
      case 'config': {
        if (sub === 'set-code') {
          const newCode = parsed.positionals[2];
          if (!newCode) {
            console.error(`${colors.red}Hata: Yeni aile kodu belirtilmeli (Örn: config set-code ST-4821)${colors.reset}`);
            process.exit(1);
          }
          const saved = setFamilyCode(newCode);
          console.log(`${colors.green}✔ Varsayılan aile kodu kaydedildi: ${saved}${colors.reset}`);
        } else if (sub === 'get-code') {
          console.log(`Aktif Aile Kodu: ${colors.bold}${colors.brightCyan}${config.familyCode}${colors.reset}`);
        } else {
          console.log(JSON.stringify(config, null, 2));
        }
        break;
      }

      default:
        console.error(`${colors.red}Bilinmeyen komut: ${cmd}${colors.reset}`);
        printUsage();
        process.exit(1);
    }
  } catch (err) {
    console.error(`\n${colors.brightRed}❌ Hata:${colors.reset} ${err.message}`);
    process.exit(1);
  }
}

main();
