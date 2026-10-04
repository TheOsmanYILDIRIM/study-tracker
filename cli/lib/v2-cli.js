const fs = require('fs');
const path = require('path');
const v2Api = require('./v2-api');
const { validateSeed, diffSeed, applySeed } = require('./v2-seed');
const { validateModularTree, compileModularCatalog, saveCompiledCatalog } = require('./v2-modular');
const { analyzeMigration, planMigration, applyMigration } = require('./v2-migrate');
const { listReviewItems, showReviewItem, approveReviewItem, rejectReviewItem, replaceContentReviewItem } = require('./v2-review');
const { validateQuizSchema, attachQuizToItem, createQuizItem, loadQuizFile } = require('./v2-quiz');
const { runV2Doctor } = require('./v2-doctor');
const { colors } = require('./renderer');

function renderV2Usage() {
  console.log(`
${colors.bold}${colors.brightCyan}StudyTracker CLI - V2 Ölçme, Müfredat & İçerik Yönetimi${colors.reset}

${colors.bold}Müfredat & Katalog Görünümü:${colors.reset}
  ${colors.green}studytracker-cli v2 catalog [--course <id>] [--json]${colors.reset}       Tüm müfredat ağacını görüntüler
  ${colors.green}studytracker-cli v2 course list [--all] [--json]${colors.reset}             Dersleri listeler
  ${colors.green}studytracker-cli v2 course create --title "..." --subject "..." [--grade 9]${colors.reset} Ders oluşturur
  ${colors.green}studytracker-cli v2 lesson list [--course <id>] [--json]${colors.reset}     Üniteleri/konuları listeler
  ${colors.green}studytracker-cli v2 lesson create --course <id> --title "..."${colors.reset} Ünite/konu oluşturur

${colors.bold}Öğrenme Öğeleri (Video / Quiz / Anki):${colors.reset}
  ${colors.green}studytracker-cli v2 item list [--lesson <id>] [--json]${colors.reset}       Öğeleri listeler
  ${colors.green}studytracker-cli v2 item get <id> [--json]${colors.reset}                   Öğe detay ve tüm sürüm geçmişini görüntüler
  ${colors.green}studytracker-cli v2 item create --lesson <id> --type VIDEO|QUIZ|ANKI --label "..." --title "..." [--url "..."]${colors.reset}
  ${colors.green}studytracker-cli v2 item update-content <itemId> --title "..." [--url "..."] [--changelog "..."]${colors.reset} (Yeni sürüm üretir)
  ${colors.green}studytracker-cli v2 item insert-before <targetId> --lesson <id> --type ... --label "..." --title "..."${colors.reset}
  ${colors.green}studytracker-cli v2 item insert-after <targetId> --lesson <id> --type ... --label "..." --title "..."${colors.reset}
  ${colors.green}studytracker-cli v2 item reorder <itemId> --before <targetId> | --after <targetId>${colors.reset}
  ${colors.green}studytracker-cli v2 item archive <itemId>${colors.reset}                     Öğeyi arşivler (geçmiş silinmez)

${colors.bold}İçerik İnceleme & Onay İş Akışı (Review Workflow):${colors.reset}
  ${colors.green}studytracker-cli v2 review list [--status needs_review|draft|verified] [--course <id>] [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 review show <stable_key> [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 review approve <stable_key> [--note "..."] [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 review reject <stable_key> --reason "..." [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 review replace-content <stable_key> --file CONTENT.json [--note "..."] [--json]${colors.reset}

${colors.bold}Quiz Yazarlık & İçe Aktarma (Deterministic Quiz Authoring):${colors.reset}
  ${colors.green}studytracker-cli v2 quiz validate --file <quiz.json> [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 quiz attach --item <stable_key|id> --file <quiz.json> [--note "..."] [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 quiz create --lesson <id|key> --label <label> --stable-key <key> --file <quiz.json> [--before <target>] [--after <target>] [--json]${colors.reset}

${colors.bold}Tohum Kataloğu & İçerik Dağıtımı (Seed & Modular):${colors.reset}
  ${colors.green}studytracker-cli v2 modular compile [--dir <path>] [--output <path>] [--json]${colors.reset} Modüler içerik ağacını derler
  ${colors.green}studytracker-cli v2 modular validate [--dir <path>] [--json]${colors.reset}         Modüler içerik referanslarını doğrular
  ${colors.green}studytracker-cli v2 seed validate [--file <path>] [--json]${colors.reset}           Tohum dosyasını ve denetim kurallarını doğrular
  ${colors.green}studytracker-cli v2 seed diff [--file <path>] [--json]${colors.reset}               Tohum ile aktif sunucu kataloğunu karşılaştırır
  ${colors.green}studytracker-cli v2 seed apply [--file <path>] [--dry-run] [--json]${colors.reset}  Tohumu sunucuya kayıpsız uygular

${colors.bold}V1 -> V2 Güvenli Geçiş Eşleyicisi (Migration):${colors.reset}
  ${colors.green}studytracker-cli v2 migrate-v1 analyze [--json]${colors.reset}             V1 ve V2 eşleşme güven analizini çıkarır
  ${colors.green}studytracker-cli v2 migrate-v1 plan [--output FILE] [--json]${colors.reset}  Yalnızca exact/high kayıtlar için geçiş planı üretir
  ${colors.green}studytracker-cli v2 migrate-v1 apply --plan FILE [--dry-run] [--json]${colors.reset} Geçiş planını dtm. attempt olarak uygular

${colors.bold}Staging Teşhisi & Sistem Durumu (Doctor):${colors.reset}
  ${colors.green}studytracker-cli v2 doctor [--seed <path>] [--json]${colors.reset}          API v3, depolama backend, katalog ve seed drift kontrolü

${colors.bold}Ön Koşul & Bağımlılık Yönetimi:${colors.reset}
  ${colors.green}studytracker-cli v2 prereq add --item <id> --requires <id> [--min-score 70]${colors.reset}
  ${colors.green}studytracker-cli v2 prereq remove --item <id> --requires <id>${colors.reset}
  ${colors.green}studytracker-cli v2 prereq list [--item <id>] [--json]${colors.reset}

${colors.bold}Ölçme & İlerleme & Dış AI Entegrasyonu:${colors.reset}
  ${colors.green}studytracker-cli v2 attempts list [--student <id>] [--item <id>] [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 attempts record --student <id> --item <id> [--score 100] [--duration 600]${colors.reset}
  ${colors.green}studytracker-cli v2 progress [--student <id>] [--course <id>] [--json]${colors.reset}
  ${colors.green}studytracker-cli v2 export-context [--student <id>] [--course <id>]${colors.reset} (Dış AI analiz paketi)
`);
}

async function handleV2Command(parsed, familyCode) {
  const isJson = Boolean(parsed.options.json);
  const v2Cmd = parsed.positionals[1]; // e.g. 'course', 'lesson', 'item', 'catalog', 'progress', etc.
  const action = parsed.positionals[2]; // e.g. 'list', 'create', 'get', 'archive', etc.

  if (!v2Cmd || v2Cmd === 'help') {
    renderV2Usage();
    return;
  }

  switch (v2Cmd) {
    // CATALOG
    case 'catalog':
    case 'curriculum': {
      const courseId = parsed.options.course || null;
      const res = await v2Api.fetchV2Catalog(familyCode, courseId);
      if (isJson) {
        console.log(JSON.stringify(res, null, 2));
      } else {
        console.log(`\n${colors.bold}${colors.brightCyan}=== V2 MÜFREDAT AĞACI (${familyCode}) ===${colors.reset}\n`);
        (res.curriculum || []).forEach(c => {
          console.log(`${colors.bold}${colors.green}📚 [${c.id}] ${c.title} (${c.subject} - Grade ${c.gradeLevel})${colors.reset}`);
          (c.lessons || []).forEach(l => {
            console.log(`   ${colors.yellow}📖 [${l.id}] ${l.title}${colors.reset} (Order: ${l.orderKey})`);
            (l.items || []).forEach(i => {
              const v = i.currentVersion;
              console.log(`      ${colors.cyan}▶ [${i.displayLabel}] (${i.itemType})${colors.reset} ${v?.title || ''} [ID: ${i.id}, Order: ${i.orderKey}]`);
            });
          });
        });
      }
      break;
    }

    // COURSE
    case 'course':
    case 'courses': {
      if (action === 'list' || !action) {
        const res = await v2Api.listV2Courses(familyCode, parsed.options.all === true);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== DERSLER (${familyCode}) ===${colors.reset}\n`);
          (res.courses || []).forEach(c => {
            console.log(`- ${colors.green}[${c.id}]${colors.reset} ${c.title} (${c.subject}) ${c.isArchived ? colors.dim + '[ARCHIVED]' + colors.reset : ''}`);
          });
        }
      } else if (action === 'create') {
        const title = parsed.options.title || parsed.positionals[3];
        const subject = parsed.options.subject || title;
        const gradeLevel = parsed.options.grade || parsed.options.gradeLevel || 9;
        const description = parsed.options.desc || parsed.options.description || '';
        const id = parsed.options.id || null;
        if (!title || !subject) throw new Error('--title ve --subject zorunludur.');
        const res = await v2Api.createV2Course(familyCode, { id, title, subject, gradeLevel, description });
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`${colors.green}✅ Ders başarıyla oluşturuldu:${colors.reset} [${res.course.id}] ${res.course.title}`);
        }
      } else if (action === 'archive') {
        const id = parsed.positionals[3] || parsed.options.id;
        if (!id) throw new Error('Arşivlenecek ders ID belirtilmelidir.');
        const res = await v2Api.archiveV2Course(familyCode, id);
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.yellow}📦 Ders arşivlendi:${colors.reset} ${id}`);
      }
      break;
    }

    // LESSON
    case 'lesson':
    case 'lessons': {
      if (action === 'list' || !action) {
        const courseId = parsed.options.course || null;
        const res = await v2Api.listV2Lessons(familyCode, courseId, parsed.options.all === true);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== ÜNİTELER / KONULAR (${familyCode}) ===${colors.reset}\n`);
          (res.lessons || []).forEach(l => {
            console.log(`- ${colors.yellow}[${l.id}]${colors.reset} ${l.title} (Course: ${l.courseId}, Order: ${l.orderKey})`);
          });
        }
      } else if (action === 'create') {
        const courseId = parsed.options.course || parsed.options.courseId;
        const title = parsed.options.title || parsed.positionals[3];
        const id = parsed.options.id || null;
        if (!courseId || !title) throw new Error('--course ve --title zorunludur.');
        const res = await v2Api.createV2Lesson(familyCode, { id, courseId, title });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✅ Ünite oluşturuldu:${colors.reset} [${res.lesson.id}] ${res.lesson.title}`);
      } else if (action === 'archive') {
        const id = parsed.positionals[3] || parsed.options.id;
        if (!id) throw new Error('Arşivlenecek ünite ID belirtilmelidir.');
        const res = await v2Api.archiveV2Lesson(familyCode, id);
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.yellow}📦 Ünite arşivlendi:${colors.reset} ${id}`);
      }
      break;
    }

    // LEARNING ITEM
    case 'item':
    case 'items': {
      if (action === 'list' || !action) {
        const lessonId = parsed.options.lesson || parsed.options.lessonId || null;
        const res = await v2Api.listV2Items(familyCode, lessonId, parsed.options.all === true);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== ÖĞRENME ÖĞELERİ (${familyCode}) ===${colors.reset}\n`);
          (res.items || []).forEach(i => {
            console.log(`- ${colors.cyan}[${i.displayLabel}]${colors.reset} ${i.currentVersion?.title || ''} (Type: ${i.itemType}, ID: ${i.id}, Order: ${i.orderKey})`);
          });
        }
      } else if (action === 'get') {
        const itemId = parsed.positionals[3] || parsed.options.id;
        if (!itemId) throw new Error('Öğe ID belirtilmelidir.');
        const res = await v2Api.getV2Item(familyCode, itemId);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          const item = res.item;
          console.log(`\n${colors.bold}${colors.brightCyan}=== ÖĞE DETAYI: ${item.displayLabel} ===${colors.reset}`);
          console.log(`ID: ${item.id}`);
          console.log(`Stable Key: ${item.stableKey}`);
          console.log(`Type: ${item.itemType}`);
          console.log(`Order Key: ${item.orderKey}`);
          console.log(`Current Version: ${item.currentVersionId}`);
          console.log(`\n${colors.bold}Sürüm Geçmişi:${colors.reset}`);
          (item.versions || []).forEach(v => {
            console.log(`  v${v.versionNumber}: ${v.title} [${v.contentUrl || 'No URL'}] (${new Date(v.createdAt).toLocaleDateString()}) - ${v.changelog}`);
          });
        }
      } else if (action === 'create') {
        const lessonId = parsed.options.lesson || parsed.options.lessonId;
        const itemType = (parsed.options.type || 'VIDEO').toUpperCase();
        const displayLabel = parsed.options.label || parsed.options.displayLabel;
        const title = parsed.options.title || displayLabel;
        const contentUrl = parsed.options.url || parsed.options.contentUrl || null;
        const payload = parsed.options.payload ? (typeof parsed.options.payload === 'string' ? JSON.parse(parsed.options.payload) : parsed.options.payload) : null;
        const stableKey = parsed.options.stableKey || null;
        const id = parsed.options.id || null;
        if (!lessonId || !displayLabel) throw new Error('--lesson ve --label zorunludur.');
        const res = await v2Api.createV2Item(familyCode, { id, lessonId, itemType, displayLabel, title, contentUrl, payload, stableKey });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✅ Öğe oluşturuldu:${colors.reset} [${res.item.displayLabel}] ${res.item.id} (Order: ${res.item.orderKey})`);
      } else if (action === 'insert-before' || action === 'insert-after') {
        const targetItemId = parsed.positionals[3] || parsed.options.target;
        const lessonId = parsed.options.lesson || parsed.options.lessonId;
        const itemType = (parsed.options.type || 'QUIZ').toUpperCase();
        const displayLabel = parsed.options.label || parsed.options.displayLabel;
        const title = parsed.options.title || displayLabel;
        const contentUrl = parsed.options.url || null;
        const payload = parsed.options.payload ? JSON.parse(parsed.options.payload) : null;
        const position = action === 'insert-before' ? 'before' : 'after';
        if (!targetItemId || !lessonId || !displayLabel) throw new Error('targetId, --lesson ve --label zorunludur.');
        const res = await v2Api.createV2Item(familyCode, {
          lessonId, itemType, displayLabel, title, contentUrl, payload, position, targetItemId
        });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✅ Modüler Öğe Araya Eklendi (${position} ${targetItemId}):${colors.reset} [${res.item.displayLabel}] (Order: ${res.item.orderKey})`);
      } else if (action === 'update-content') {
        const itemId = parsed.positionals[3] || parsed.options.id;
        const title = parsed.options.title;
        const contentUrl = parsed.options.url || parsed.options.contentUrl;
        const payload = parsed.options.payload ? JSON.parse(parsed.options.payload) : undefined;
        const changelog = parsed.options.changelog || 'CLI content update';
        if (!itemId) throw new Error('itemId zorunludur.');
        const res = await v2Api.updateV2ItemContent(familyCode, itemId, { title, contentUrl, payload, changelog });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✅ Yeni içerik sürümü oluşturuldu:${colors.reset} [${res.item.id}] ${res.item.currentVersionId}`);
      } else if (action === 'reorder') {
        const itemId = parsed.positionals[3] || parsed.options.id;
        let position = null;
        let targetItemId = null;
        if (parsed.options.before) {
          position = 'before';
          targetItemId = parsed.options.before;
        } else if (parsed.options.after) {
          position = 'after';
          targetItemId = parsed.options.after;
        }
        const orderKey = parsed.options.order ? parseFloat(parsed.options.order) : null;
        const res = await v2Api.reorderV2Item(familyCode, itemId, { position, targetItemId, orderKey });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✅ Sıralama güncellendi:${colors.reset} [${res.item.id}] New OrderKey: ${res.item.orderKey}`);
      } else if (action === 'archive') {
        const itemId = parsed.positionals[3] || parsed.options.id;
        const res = await v2Api.archiveV2Item(familyCode, itemId);
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.yellow}📦 Öğe arşivlendi:${colors.reset} ${itemId}`);
      }
      break;
    }

    // PREREQUISITES
    case 'prereq':
    case 'prerequisites': {
      if (action === 'add') {
        const itemId = parsed.options.item || parsed.positionals[3];
        const requiredItemId = parsed.options.requires || parsed.positionals[4];
        const minScore = parsed.options.minScore || parsed.options.score ? parseFloat(parsed.options.minScore || parsed.options.score) : null;
        if (!itemId || !requiredItemId) throw new Error('--item ve --requires zorunludur.');
        const res = await v2Api.addPrerequisite(familyCode, { itemId, requiredItemId, minScore });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✅ Ön koşul eklendi:${colors.reset} ${itemId} -> requires ${requiredItemId} (minScore: ${minScore || 'Yok'})`);
      } else if (action === 'remove') {
        const itemId = parsed.options.item || parsed.positionals[3];
        const requiredItemId = parsed.options.requires || parsed.positionals[4];
        if (!itemId || !requiredItemId) throw new Error('--item ve --requires zorunludur.');
        const res = await v2Api.removeV2Prerequisite(familyCode, itemId, requiredItemId);
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.yellow}🗑️ Ön koşul silindi:${colors.reset} ${itemId} -> ${requiredItemId}`);
      } else if (action === 'list' || !action) {
        const itemId = parsed.options.item || null;
        const res = await v2Api.listV2Prerequisites(familyCode, itemId);
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== ÖN KOŞULLAR (${familyCode}) ===${colors.reset}\n`);
          (res.prerequisites || []).forEach(p => {
            console.log(`- ${colors.cyan}${p.itemId}${colors.reset} ➡️ requires ${colors.green}${p.requiredItemId}${colors.reset} (minScore: ${p.minScore ?? 'Yok'})`);
          });
        }
      }
      break;
    }

    // ATTEMPTS
    case 'attempts':
    case 'attempt': {
      if (action === 'list' || !action) {
        const studentId = parsed.options.student || null;
        const itemId = parsed.options.item || null;
        const limit = parsed.options.limit ? parseInt(parsed.options.limit, 10) : 50;
        const res = await v2Api.listV2Attempts(familyCode, { studentId, itemId, limit });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== DENEME / ÇALIŞMA KAYITLARI (${familyCode}) ===${colors.reset}\n`);
          (res.attempts || []).forEach(a => {
            console.log(`- [${a.status}] ${a.itemId} (Ver: ${a.versionId}, Öğrenci: ${a.studentId}, Skor: ${a.score ?? '-'}, Süre: ${a.durationSeconds}s)`);
          });
        }
      } else if (action === 'record') {
        const studentId = parsed.options.student || 'student_default';
        const itemId = parsed.options.item || parsed.positionals[3];
        const versionId = parsed.options.version || null;
        const status = (parsed.options.status || 'COMPLETED').toUpperCase();
        const score = parsed.options.score ? parseFloat(parsed.options.score) : null;
        const durationSeconds = parsed.options.duration ? parseInt(parsed.options.duration, 10) : 0;
        const clientAttemptId = parsed.options.attemptId || `cli_att_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`;
        if (!itemId) throw new Error('--item zorunludur.');
        const res = await v2Api.recordV2Attempt(familyCode, {
          clientAttemptId, studentId, itemId, versionId, status, score, durationSeconds
        });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✅ Deneme kaydedildi:${colors.reset} [${res.attempt.id}] (Status: ${res.attempt.status}, Duplicate: ${res.duplicate})`);
      }
      break;
    }

    // PROGRESS & ANALYTICS
    case 'progress':
    case 'analytics': {
      const studentId = parsed.options.student || 'student_default';
      const courseId = parsed.options.course || null;
      const res = await v2Api.getV2Progress(familyCode, studentId, courseId);
      if (isJson) {
        console.log(JSON.stringify(res, null, 2));
      } else {
        console.log(`\n${colors.bold}${colors.brightCyan}=== ÖĞRENCİ İLERLEME & ÖLÇME RAPORU (${studentId}) ===${colors.reset}\n`);
        (res.data?.courses || []).forEach(c => {
          console.log(`${colors.bold}${colors.green}📚 ${c.title} - İlerleme: %${c.completionPercentage} (${c.completedItems}/${c.totalItems} öge tamamlandı)${colors.reset}`);
          if (c.averageMasteryScore !== null) console.log(`   Ortalama Başarı Skoru: %${c.averageMasteryScore}`);
          (c.lessons || []).forEach(l => {
            console.log(`   ${colors.yellow}📖 ${l.title} (%${l.completionPercentage})${colors.reset}`);
            (l.items || []).forEach(i => {
              const statusSymbol = i.isCompleted ? '✅' : i.isUnlocked ? '🔓' : '🔒';
              console.log(`      ${statusSymbol} [${i.displayLabel}] ${i.itemType} (Skor: ${i.bestScore ?? '-'}, Deneme: ${i.attemptCount})`);
            });
          });
        });
      }
      break;
    }

    // EXPORT-CONTEXT (External AI bounded package)
    case 'export-context': {
      const studentId = parsed.options.student || 'student_default';
      const courseId = parsed.options.course || null;
      const res = await v2Api.exportV2Context(familyCode, studentId, courseId);
      // Export context always outputs deterministic JSON
      console.log(JSON.stringify(res.context, null, 2));
      break;
    }

    // SEED CATALOG MANAGEMENT
    case 'seed': {
      const seedFile = parsed.options.file || null;
      if (action === 'validate') {
        const res = validateSeed(seedFile);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== TOHUM KATALOĞU DOĞRULAMA RAPORU ===${colors.reset}\n`);
          if (res.valid) {
            console.log(`${colors.green}✔ Tohum dosyası geçerli (${res.stats.courseCount} ders, ${res.stats.lessonCount} ünite, ${res.stats.itemCount} öge)${colors.reset}`);
          } else {
            console.log(`${colors.red}❌ Doğrulama başarısız (${res.errors.length} hata):${colors.reset}`);
            res.errors.forEach(e => console.log(`  - ${colors.red}${e}${colors.reset}`));
          }
          if (res.warnings.length > 0) {
            console.log(`\n${colors.yellow}⚠️ Tespit Edilen Denetim Uyarıları (${res.warnings.length}):${colors.reset}`);
            res.warnings.forEach(w => console.log(`  - [${w.stableKey || w.url}] ${w.warning}`));
          }
          if (res.explicitExceptions && res.explicitExceptions.length > 0) {
            console.log(`\n${colors.cyan}ℹ️ Onaylanmış / Yapısal İstisnalar (${res.explicitExceptions.length}):${colors.reset}`);
            res.explicitExceptions.forEach(ex => console.log(`  - [${ex.type}] [${ex.url || ex.stableKey}] ${ex.description || ex.reason}`));
          }
        }
      } else if (action === 'diff') {
        const res = await diffSeed(familyCode, seedFile);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== TOHUM KATALOĞU FARK RAPORU (${familyCode}) ===${colors.reset}\n`);
          console.log(`- Oluşturulacak Dersler: ${colors.green}${res.summary.coursesToCreate}${colors.reset}`);
          console.log(`- Oluşturulacak Üniteler: ${colors.green}${res.summary.lessonsToCreate}${colors.reset}`);
          console.log(`- Oluşturulacak Öğeler: ${colors.green}${res.summary.itemsToCreate}${colors.reset}`);
          console.log(`- Güncellenecek İçerikler (Yeni Versiyon): ${colors.yellow}${res.summary.itemsToUpdateContent}${colors.reset}`);
          console.log(`- Değişmeyen Öğeler (No-op): ${colors.dim}${res.summary.itemsIdentical}${colors.reset}`);
          console.log(`- Sunucudaki Ekstra Öğeler (Kayıpsız Korunur): ${colors.cyan}${res.summary.extraRemoteItems}${colors.reset}`);
        }
      } else if (action === 'apply') {
        const dryRun = Boolean(parsed.options['dry-run'] || parsed.options.dryRun);
        const res = await applySeed(familyCode, seedFile, { dryRun });
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          if (dryRun) {
            console.log(`${colors.yellow}ℹ️ Seed Apply DRY-RUN modunda tamamlandı.${colors.reset}`);
            console.log(`  Oluşturulacak: ${res.diff.summary.itemsToCreate} öge | Güncellenecek: ${res.diff.summary.itemsToUpdateContent} öge`);
          } else {
            console.log(`${colors.green}✔ Tohum kataloğu başarıyla uygulandı (${familyCode})!${colors.reset}`);
            console.log(`  • Dersler: ${res.summary.coursesCreated} oluşturuldu`);
            console.log(`  • Üniteler: ${res.summary.lessonsCreated} oluşturuldu`);
            console.log(`  • Öğeler: ${res.summary.itemsCreated} oluşturuldu`);
            console.log(`  • Sürüm Güncellemeleri: ${res.summary.itemsUpdated} yeni versiyon üretildi`);
            console.log(`  • Korunan Ekstra Öğeler: ${res.summary.extraRemotePreserved}`);
          }
        }
      } else {
        console.error(`${colors.red}Kullanım: studytracker-cli v2 seed validate | diff | apply [--file <path>] [--dry-run] [--json]${colors.reset}`);
      }
      break;
    }

    // V1 -> V2 MIGRATION MAPPER
    case 'migrate-v1':
    case 'migrate': {
      if (action === 'analyze') {
        const res = await analyzeMigration(familyCode);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== V1 -> V2 GEÇİŞ ANALİZ RAPORU (${familyCode}) ===${colors.reset}\n`);
          console.log(`Toplam V1 Kaydı: ${res.totalV1Records}`);
          console.log(`- Tam Eşleşen (Exact): ${colors.green}${res.counts.exact}${colors.reset}`);
          console.log(`- Yüksek Güven (High): ${colors.green}${res.counts.high}${colors.reset}`);
          console.log(`- İnceleme Gerekli (Medium - Otomatik Uygulanmaz): ${colors.yellow}${res.counts.medium}${colors.reset}`);
          console.log(`- Eşleşmeyen (Unmatched - Otomatik Uygulanmaz): ${colors.red}${res.counts.unmatched}${colors.reset}`);
        }
      } else if (action === 'plan') {
        const res = await planMigration(familyCode);
        if (parsed.options.output || parsed.options.out) {
          const outPath = path.resolve(process.cwd(), parsed.options.output || parsed.options.out);
          fs.writeFileSync(outPath, JSON.stringify(res, null, 2), 'utf8');
          console.log(`${colors.green}✔ Geçiş planı kaydedildi:${colors.reset} ${outPath}`);
        }
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else if (!parsed.options.output && !parsed.options.out) {
          console.log(`\n${colors.bold}${colors.brightCyan}=== V1 -> V2 GEÇİŞ PLANI (${familyCode}) ===${colors.reset}\n`);
          console.log(`- Planlanan Girişim Sayısı (Attempts): ${colors.green}${res.stats.totalPlanned}${colors.reset}`);
          console.log(`- Atlanan / İnceleme Bekleyen: ${colors.yellow}${res.stats.totalSkipped}${colors.reset}`);
          console.log(`\n${colors.dim}Uygulamak için: studytracker-cli v2 migrate-v1 apply --plan <dosya> [--dry-run]${colors.reset}`);
        }
      } else if (action === 'apply') {
        const planFile = parsed.options.plan || parsed.options.file;
        const dryRun = Boolean(parsed.options['dry-run'] || parsed.options.dryRun);
        let planObj;
        if (planFile) {
          const planPath = path.resolve(process.cwd(), planFile);
          planObj = JSON.parse(fs.readFileSync(planPath, 'utf8'));
        } else {
          // Direct plan generation in memory
          planObj = await planMigration(familyCode);
        }
        const res = await applyMigration(familyCode, planObj, { dryRun });
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          if (dryRun) {
            console.log(`${colors.yellow}ℹ️ V1 -> V2 Geçiş DRY-RUN modunda tamamlandı (${res.plannedAttemptsCount} girişim planlandı).${colors.reset}`);
          } else {
            console.log(`${colors.green}✔ V1 -> V2 Geçişi başarıyla tamamlandı!${colors.reset}`);
            console.log(`  • Kaydedilen Girişimler: ${res.summary.recordedCount}`);
            console.log(`  • Yinelenen / Zaten Kayıtlı: ${res.summary.duplicateCount}`);
            console.log(`  • Hatalar: ${res.summary.errorCount}`);
          }
        }
      } else {
        console.error(`${colors.red}Kullanım: studytracker-cli v2 migrate-v1 analyze | plan | apply [--plan <dosya>] [--dry-run] [--json]${colors.reset}`);
      }
      break;
    }

    // CONTENT REVIEW WORKFLOW
    case 'review': {
      if (action === 'list' || !action) {
        const res = await listReviewItems(familyCode, {
          status: parsed.options.status,
          courseId: parsed.options.course
        });
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== İÇERİK İNCELEME LİSTESİ (${familyCode}) ===${colors.reset}`);
          console.log(`Filtre: ${colors.yellow}${res.filter.status}${colors.reset} | Toplam: ${res.total}\n`);
          res.items.forEach(i => {
            const statusColor = i.publishingStatus === 'active' ? colors.green : (i.publishingStatus === 'draft' ? colors.yellow : colors.dim);
            const revColor = i.reviewStatus === 'verified' ? colors.green : (i.reviewStatus === 'rejected' ? colors.red : colors.yellow);
            console.log(`• ${colors.bold}[${i.stableKey}]${colors.reset} (${i.displayLabel}) ${i.title}`);
            console.log(`  Ders: ${i.courseTitle} | Durum: ${statusColor}${i.publishingStatus}${colors.reset} | İnceleme: ${revColor}${i.reviewStatus}${colors.reset} | Sürüm: v${i.versionCount}`);
            if (i.contentUrl) console.log(`  URL: ${colors.cyan}${i.contentUrl}${colors.reset}`);
            if (i.auditWarnings.length > 0) {
              console.log(`  ${colors.red}⚠ Uyarılar: ${i.auditWarnings.join('; ')}${colors.reset}`);
            }
          });
        }
      } else if (action === 'show') {
        const target = parsed.positionals[3] || parsed.options.item || parsed.options.key;
        if (!target) throw new Error('İncelenecek stable_key veya item ID belirtilmelidir.');
        const res = await showReviewItem(familyCode, target);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== İÇERİK DETAYI: ${res.item.stableKey} ===${colors.reset}`);
          console.log(`ID (Değişmez): ${colors.green}${res.item.id}${colors.reset}`);
          console.log(`Stable Key:    ${colors.green}${res.item.stableKey}${colors.reset}`);
          console.log(`Ders & Ünite:  ${res.course.title} -> ${res.lesson.title}`);
          console.log(`Tür & Etiket:  ${res.item.itemType} (${res.item.displayLabel})`);
          console.log(`Yayın Durumu:  ${colors.bold}${res.item.publishingStatus}${colors.reset}`);
          console.log(`İnceleme:      ${colors.yellow}${res.provenance.reviewStatus}${colors.reset} (Override: ${res.provenance.reviewedOverride})`);
          console.log(`Aktif Sürüm:   ${res.currentVersion.id} (v${res.currentVersion.versionNumber})`);
          console.log(`Başlık:        ${res.currentVersion.title}`);
          console.log(`URL:           ${res.currentVersion.contentUrl || 'Yok'}`);
          if (res.reviewHistory.length > 0) {
            console.log(`\n${colors.bold}İnceleme Geçmişi:${colors.reset}`);
            res.reviewHistory.forEach(h => {
              console.log(`  [${h.timestamp}] ${h.action} by ${h.reviewer}: ${h.note || h.reason || ''}`);
            });
          }
        }
      } else if (action === 'approve') {
        const target = parsed.positionals[3] || parsed.options.item || parsed.options.key;
        if (!target) throw new Error('Onaylanacak stable_key veya item ID belirtilmelidir.');
        const res = await approveReviewItem(familyCode, target, { note: parsed.options.note });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✔ ${res.message}${colors.reset}`);
      } else if (action === 'reject') {
        const target = parsed.positionals[3] || parsed.options.item || parsed.options.key;
        const reason = parsed.options.reason;
        if (!target || !reason) throw new Error('Reddedilecek stable_key ve --reason zorunludur.');
        const res = await rejectReviewItem(familyCode, target, { reason });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.yellow}✔ ${res.message}${colors.reset}`);
      } else if (action === 'replace-content' || action === 'replace') {
        const target = parsed.positionals[3] || parsed.options.item || parsed.options.key;
        const file = parsed.options.file;
        if (!target || !file) throw new Error('stable_key ve --file <CONTENT.json> zorunludur.');
        const res = await replaceContentReviewItem(familyCode, target, { filePath: file, note: parsed.options.note });
        if (isJson) console.log(JSON.stringify(res, null, 2));
        else console.log(`${colors.green}✔ ${res.message}${colors.reset}`);
      } else {
        console.error(`${colors.red}Kullanım: studytracker-cli v2 review list | show <key> | approve <key> | reject <key> --reason ... | replace-content <key> --file CONTENT.json${colors.reset}`);
      }
      break;
    }

    // QUIZ AUTHORING & IMPORT WORKFLOW
    case 'quiz': {
      if (action === 'validate') {
        const file = parsed.options.file || parsed.positionals[3];
        if (!file) throw new Error('--file <quiz.json> belirtilmelidir.');
        const res = validateQuizSchema(file);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          if (res.valid) {
            console.log(`${colors.green}✔ Quiz şeması geçerli!${colors.reset}`);
            console.log(`  • Başlık: ${res.normalizedQuiz.quizTitle}`);
            console.log(`  • Toplam Soru: ${res.stats.questionCount} (Çoktan Seçmeli: ${res.stats.multipleChoiceCount}, D/Y: ${res.stats.trueFalseCount})`);
            console.log(`  • Deterministik Fingerprint: ${colors.cyan}${res.fingerprint}${colors.reset}`);
          } else {
            console.error(`${colors.red}❌ Quiz şeması geçersiz:${colors.reset}`);
            res.errors.forEach(e => console.error(`  - ${e}`));
            process.exitCode = 1;
          }
        }
      } else if (action === 'attach') {
        const target = parsed.options.item || parsed.positionals[3];
        const file = parsed.options.file;
        if (!target || !file) throw new Error('--item <key|id> ve --file <quiz.json> zorunludur.');
        const res = await attachQuizToItem(familyCode, target, file, { note: parsed.options.note });
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`${colors.green}✔ Quiz başarıyla öğeye eklendi!${colors.reset}`);
          console.log(`  • Öğe ID (Değişmez): ${res.itemId} [${res.stableKey}]`);
          console.log(`  • Yeni Sürüm: v${res.versionNumber}`);
          console.log(`  • Fingerprint: ${res.fingerprint}`);
        }
      } else if (action === 'create') {
        const lesson = parsed.options.lesson;
        const label = parsed.options.label || parsed.options.displayLabel;
        const stableKey = parsed.options['stable-key'] || parsed.options.stableKey;
        const file = parsed.options.file;
        const before = parsed.options.before;
        const after = parsed.options.after;
        const title = parsed.options.title;
        if (!lesson || !label || !stableKey || !file) {
          throw new Error('--lesson <id|key>, --label <label>, --stable-key <key>, --file <quiz.json> zorunludur.');
        }
        const res = await createQuizItem(familyCode, {
          lessonIdOrKey: lesson,
          displayLabel: label,
          stableKey,
          quizFilePathOrObject: file,
          title,
          beforeTarget: before,
          afterTarget: after
        });
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`${colors.green}✔ Quiz öğesi başarıyla oluşturuldu!${colors.reset}`);
          console.log(`  • Öğe ID: ${res.item.id} [${res.item.stableKey}] (Etiket: ${res.item.displayLabel})`);
          console.log(`  • Sıra Değeri (OrderKey): ${res.item.orderKey}`);
          console.log(`  • Fingerprint: ${res.fingerprint}`);
        }
      } else {
        console.error(`${colors.red}Kullanım: studytracker-cli v2 quiz validate --file <dosya> | attach --item <key> --file <dosya> | create --lesson <id> --label <label> --stable-key <key> --file <dosya> [--before/--after <target>]${colors.reset}`);
      }
      break;
    }

    // MODULAR CONTENT SOURCE & COMPILER
    case 'modular':
    case 'compile': {
      const v2Dir = parsed.options.dir ? path.resolve(process.cwd(), parsed.options.dir) : path.resolve(__dirname, '../../content/v2');
      const outputPath = parsed.options.output ? path.resolve(process.cwd(), parsed.options.output) : path.resolve(__dirname, '../../content/9-sinif-v2-catalog.json');

      if (action === 'validate') {
        const res = validateModularTree(v2Dir);
        if (isJson) {
          console.log(JSON.stringify(res, null, 2));
        } else {
          console.log(`\n${colors.bold}${colors.brightCyan}=== MODÜLER İÇERİK AĞACI DOĞRULAMA ===${colors.reset}`);
          console.log(`Kaynak: ${v2Dir}`);
          if (res.valid) {
            console.log(`${colors.green}✔ Modüler içerik ağacı %100 geçerli!${colors.reset}`);
            console.log(`  • Dersler: ${res.stats.courseCount}`);
            console.log(`  • Üniteler: ${res.stats.lessonCount}`);
            console.log(`  • Toplam Öğe: ${res.stats.itemCount} (Video: ${res.stats.videoCount}, Quiz: ${res.stats.quizCount} [Mikro: ${res.stats.microQuizCount}], Anki: ${res.stats.ankiCount})`);
          } else {
            console.log(`${colors.red}✖ Doğrulama Hataları (${res.errors.length}):${colors.reset}`);
            res.errors.forEach(e => console.log(`  - ${e}`));
          }
          if (res.warnings.length > 0) {
            console.log(`\n${colors.yellow}⚠ Uyarılar (${res.warnings.length}):${colors.reset}`);
            res.warnings.forEach(w => console.log(`  - ${w.warning || JSON.stringify(w)}`));
          }
        }
      } else {
        // default compile
        const validation = validateModularTree(v2Dir);
        if (!validation.valid) {
          if (isJson) console.log(JSON.stringify({ success: false, errors: validation.errors }, null, 2));
          else {
            console.error(`${colors.red}✖ Doğrulama başarısız oldu, derleme durduruldu:${colors.reset}`);
            validation.errors.forEach(e => console.error(`  - ${e}`));
          }
          process.exit(1);
        }

        const compiled = compileModularCatalog(v2Dir);
        const result = saveCompiledCatalog(compiled, outputPath);
        if (isJson) {
          console.log(JSON.stringify({ success: true, ...result, stats: validation.stats }, null, 2));
        } else {
          console.log(`\n${colors.green}✔ Modüler katalog başarıyla derlendi!${colors.reset}`);
          console.log(`  • Hedef: ${result.path}`);
          console.log(`  • Ders: ${result.courseCount} | Ünite: ${result.totalLessons} | Öğe: ${result.totalItems}`);
          console.log(`  • Boyut: ${(result.bytes / 1024).toFixed(1)} KB`);
        }
      }
      break;
    }

    // DOCTOR & STAGING DIAGNOSTICS
    case 'doctor': {
      const seedFile = parsed.options.seed || parsed.options.file;
      const res = await runV2Doctor(familyCode, { seedFile });
      if (isJson) {
        console.log(JSON.stringify(res, null, 2));
      } else {
        console.log(`\n${colors.bold}${colors.brightCyan}=== STUDYTRACKER V2 DOKTOR & TEŞHİS RAPORU (${familyCode}) ===${colors.reset}\n`);
        const hColor = res.checks.healthEndpoint?.status === 'PASS' ? colors.green : colors.red;
        console.log(`1. Cloudflare Worker API v3: ${hColor}${res.checks.healthEndpoint?.status}${colors.reset} (Backend: ${colors.bold}${res.storageBackend || 'unknown'}${colors.reset}, v${res.checks.healthEndpoint?.version || '2.0.0'})`);
        
        const cColor = res.checks.catalog?.status === 'PASS' ? colors.green : colors.red;
        console.log(`2. Katalog Durumu:           ${cColor}${res.checks.catalog?.status}${colors.reset} (${res.catalogSummary.courseCount} ders, ${res.catalogSummary.lessonCount} ünite, ${res.catalogSummary.itemCount} öğe: ${res.catalogSummary.activeItems} aktif / ${res.catalogSummary.draftItems} taslak)`);
        
        const aColor = res.checks.attempts?.status === 'PASS' ? colors.green : colors.red;
        console.log(`3. Ölçme (Attempts) Servisi: ${aColor}${res.checks.attempts?.status}${colors.reset}`);

        const dColor = res.seedDrift?.inSync ? colors.green : colors.yellow;
        console.log(`4. Tohum Sapması (Drift):    ${dColor}${res.checks.seedDrift?.status || 'UNKNOWN'}${colors.reset} (Oluşturulacak: ${res.seedDrift?.itemsToCreate || 0}, Güncellenecek: ${res.seedDrift?.itemsToUpdate || 0}, Korunan Manuel İnceleme: ${res.seedDrift?.reviewedOverridesPreserved || 0})`);

        if (res.recommendations.length > 0) {
          console.log(`\n${colors.bold}Öneriler & Teşhis Notları:${colors.reset}`);
          res.recommendations.forEach(r => console.log(`  💡 ${r}`));
        } else {
          console.log(`\n${colors.green}✔ Tüm V2 servisleri ve içerik mimarisi staging için hazır durumda!${colors.reset}`);
        }
      }
      break;
    }

    default:
      console.error(`${colors.red}Bilinmeyen v2 komutu: ${v2Cmd}${colors.reset}`);
      renderV2Usage();
  }
}

module.exports = {
  handleV2Command,
  renderV2Usage
};
