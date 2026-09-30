const v2Api = require('./v2-api');
const { colors } = require('./renderer');

function renderV2Usage() {
  console.log(`
${colors.bold}${colors.brightCyan}StudyTracker CLI - V2 Ölçme & Müfredat Komutları (Phase 1)${colors.reset}

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

    default:
      console.error(`${colors.red}Bilinmeyen v2 komutu: ${v2Cmd}${colors.reset}`);
      renderV2Usage();
  }
}

module.exports = {
  handleV2Command,
  renderV2Usage
};
