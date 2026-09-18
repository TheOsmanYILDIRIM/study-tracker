/**
 * StudyTracker DSL Parser & Serializer for Node.js
 * Mirrors Android Kotlin's SimplePlanParser.kt
 */

const URL_REGEX = /(https?:\/\/[^\s|"'>)]+|(?:\bwww\.|\bm\.youtube\.com\/|youtube\.com\/|youtu\.be\/)[^\s|"'>)]+)/i;

function extractUrl(text) {
  if (!text) return null;
  const match = text.match(URL_REGEX);
  if (!match) return null;
  let url = match[1].trim();
  while (url && (url.endsWith(')') || url.endsWith(']') || url.endsWith('.') || url.endsWith(',') || url.endsWith(';'))) {
    url = url.slice(0, -1).trim();
  }
  if (!url) return null;
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    return `https://${url}`;
  }
  return url;
}

const DAY_MAP = {
  'PAZARTESI': 'MON',
  'PZT': 'MON',
  'MON': 'MON',
  'MONDAY': 'MON',
  'SALI': 'TUE',
  'SAL': 'TUE',
  'TUE': 'TUE',
  'TUESDAY': 'TUE',
  'CARSAMBA': 'WED',
  'ÇARŞAMBA': 'WED',
  'ÇAR': 'WED',
  'CAR': 'WED',
  'WED': 'WED',
  'WEDNESDAY': 'WED',
  'PERSEMBE': 'THU',
  'PERŞEMBE': 'THU',
  'PER': 'THU',
  'THU': 'THU',
  'THURSDAY': 'THU',
  'CUMA': 'FRI',
  'CUM': 'FRI',
  'FRI': 'FRI',
  'FRIDAY': 'FRI',
  'CUMARTESI': 'SAT',
  'CMT': 'SAT',
  'SAT': 'SAT',
  'SATURDAY': 'SAT',
  'PAZAR': 'SUN',
  'PAZ': 'SUN',
  'SUN': 'SUN',
  'SUNDAY': 'SUN'
};

/**
 * Parse text DSL to Plan, Tasks, Occurrences
 */
function parseDSL(rawText, defaultFamilyCode = 'ST-2026') {
  const cleanLines = rawText
    .replace(/```[a-zA-Z]*/g, '')
    .replace(/```/g, '')
    .split('\n')
    .map(l => l.trim())
    .filter(l => l.length > 0 && !l.startsWith('#') && !l.startsWith('//') && !l.startsWith('---'));

  let weekId = '2026-W38';
  let weekStartDate = '2026-09-14';
  let childId = 'child_1';
  let currentSection = 0; // 0 = HEADER, 1 = TASKS, 2 = DAYS, 3 = WEEKLY

  const taskDefMap = new Map(); // taskId -> taskObj
  const dailyMap = new Map(); // dateCode -> list of taskIds
  const weeklyList = []; // list of weekly taskObjs

  for (const line of cleanLines) {
    const upper = line.toUpperCase();

    if (upper === '[DERSLER]' || upper === '[TASKS]' || upper === '[DERS_TANIMLARI]') {
      currentSection = 1;
      continue;
    }
    if (upper === '[GUNLER]' || upper === '[DAYS]' || upper === '[GUNLUK_PROGRAM]') {
      currentSection = 2;
      continue;
    }
    if (upper === '[HAFTALIK]' || upper === '[WEEKLY]' || upper === '[HAFTALIK_HEDEFLER]') {
      currentSection = 3;
      continue;
    }

    if (currentSection === 0) {
      const eqIdx = line.indexOf('=');
      if (eqIdx !== -1) {
        const k = line.slice(0, eqIdx).trim();
        const v = line.slice(eqIdx + 1).trim();
        const keyUpper = k.toUpperCase();
        if (keyUpper.startsWith('HAFTA') || keyUpper === 'WEEK_ID') weekId = v;
        if (keyUpper.startsWith('BASLANGIC') || keyUpper.startsWith('START_DATE')) weekStartDate = v;
        if (keyUpper.startsWith('OGRENCI') || keyUpper === 'CHILD_ID') childId = v;
      }
    } else if (currentSection === 1) {
      // mat_1 = Matematik Video | 40 dk | https://...
      const eqIdx = line.indexOf('=');
      if (eqIdx !== -1) {
        const idPart = line.slice(0, eqIdx).trim();
        const rest = line.slice(eqIdx + 1).trim();
        const taskId = idPart.toLowerCase();
        const parts = rest.split('|').map(s => s.trim());
        const title = parts[0] || taskId;
        let plannedMinutes = 30;
        let youtubeUrl = extractUrl(rest);
        let targetCount = null;
        let targetMode = null;

        for (let i = 1; i < parts.length; i++) {
          const p = parts[i];
          const minMatch = p.match(/(\d+)\s*(dk|min|dakika)/i);
          const qMatch = p.match(/(\d+)\s*(soru|questions?|q)/i);
          if (minMatch) {
            plannedMinutes = parseInt(minMatch[1], 10);
          } else if (qMatch) {
            targetCount = parseInt(qMatch[1], 10);
            targetMode = 'QUESTIONS';
          }
        }

        taskDefMap.set(taskId, {
          taskId,
          title,
          kind: 'DAILY',
          contentType: youtubeUrl ? 'VIDEO' : 'OTHER',
          youtubeUrl,
          plannedMinutes,
          targetMode,
          targetCount,
          targetMinutes: plannedMinutes,
          reviewRequired: true,
          active: true
        });
      }
    } else if (currentSection === 2) {
      // Pazartesi = mat_1, anki_ing, kitap
      const eqIdx = line.indexOf('=');
      if (eqIdx !== -1) {
        const dayPart = line.slice(0, eqIdx).trim();
        const tasksPart = line.slice(eqIdx + 1).trim();
        const normalizedDay = DAY_MAP[dayPart.toUpperCase()] || dayPart.toUpperCase();
        const taskRefs = tasksPart.split(',').map(s => s.trim().toLowerCase()).filter(s => s.length > 0);
        dailyMap.set(normalizedDay, taskRefs);
      }
    } else if (currentSection === 3) {
      // deneme = Hafta Sonu Mini Deneme | 45 dk | 25 Soru
      const eqIdx = line.indexOf('=');
      if (eqIdx !== -1) {
        const idPart = line.slice(0, eqIdx).trim();
        const rest = line.slice(eqIdx + 1).trim();
        const taskId = idPart.toLowerCase();
        const parts = rest.split('|').map(s => s.trim());
        const title = parts[0] || taskId;
        let plannedMinutes = 45;
        let targetCount = null;
        let targetMode = null;
        let youtubeUrl = extractUrl(rest);

        for (let i = 1; i < parts.length; i++) {
          const p = parts[i];
          const minMatch = p.match(/(\d+)\s*(dk|min|dakika)/i);
          const qMatch = p.match(/(\d+)\s*(soru|questions?|q)/i);
          if (minMatch) plannedMinutes = parseInt(minMatch[1], 10);
          if (qMatch) {
            targetCount = parseInt(qMatch[1], 10);
            targetMode = 'QUESTIONS';
          }
        }

        weeklyList.push({
          taskId,
          title,
          kind: 'WEEKLY',
          contentType: youtubeUrl ? 'VIDEO' : 'OTHER',
          youtubeUrl,
          plannedMinutes,
          targetMode,
          targetCount,
          targetMinutes: plannedMinutes,
          reviewRequired: true,
          active: true
        });
      }
    }
  }

  // Build tasks list
  const tasks = [...taskDefMap.values(), ...weeklyList];

  // Build occurrences list
  const occurrences = [];
  let order = 0;

  // Daily occurrences
  for (const [dayCode, taskRefs] of dailyMap.entries()) {
    for (const ref of taskRefs) {
      const template = taskDefMap.get(ref);
      const title = template ? template.title : ref;
      const plannedMinutes = template ? template.plannedMinutes : 30;
      const youtubeUrl = template ? template.youtubeUrl : null;
      const targetCount = template ? template.targetCount : null;
      const occKey = `${weekId}_${dayCode}_${ref}`;

      occurrences.push({
        id: occKey,
        familyCode: defaultFamilyCode,
        date: dayCode,
        planId: ref,
        subject: title,
        topic: 'DAILY',
        targetDurationMin: plannedMinutes,
        targetQuestionCount: targetCount || 0,
        completedDurationMin: 0,
        completedQuestionCount: 0,
        status: 'PENDING',
        parentNote: '',
        weekId: weekId,
        orderIndex: order++,
        studentNote: null,
        youtubeUrl: youtubeUrl,
        updatedAt: Date.now()
      });
    }
  }

  // Weekly occurrences
  for (const w of weeklyList) {
    const occKey = `${weekId}_WEEKLY_${w.taskId}`;
    occurrences.push({
      id: occKey,
      familyCode: defaultFamilyCode,
      date: '',
      planId: w.taskId,
      subject: w.title,
      topic: 'WEEKLY',
      targetDurationMin: w.plannedMinutes,
      targetQuestionCount: w.targetCount || 0,
      completedDurationMin: 0,
      completedQuestionCount: 0,
      status: 'PENDING',
      parentNote: '',
      weekId: weekId,
      orderIndex: order++,
      studentNote: null,
      youtubeUrl: w.youtubeUrl,
      updatedAt: Date.now()
    });
  }

  const plan = {
    planId: `plan_${weekId}`,
    weekId,
    weekStartDate,
    childId,
    timezone: 'Europe/Istanbul',
    updatedAt: new Date().toISOString(),
    rawJson: JSON.stringify({ weekId, weekStartDate, childId, tasksCount: tasks.length })
  };

  return { plan, tasks, occurrences };
}

/**
 * Export Plan & Tasks to DSL String
 */
function exportToDSL(plan, tasks, occurrences) {
  let out = '';
  out += `HAFTA = ${plan?.weekId || '2026-W38'}\n`;
  out += `BASLANGIC = ${plan?.weekStartDate || '2026-09-14'}\n`;
  out += `OGRENCI = ${plan?.childId || 'child_1'}\n\n`;

  out += `[DERSLER]\n`;
  const dailyTasks = (tasks || []).filter(t => t.kind === 'DAILY');
  for (const t of dailyTasks) {
    let line = `${t.taskId} = ${t.title} | ${t.plannedMinutes} dk`;
    if (t.targetCount) line += ` | ${t.targetCount} Soru`;
    if (t.youtubeUrl) line += ` | ${t.youtubeUrl}`;
    out += `${line}\n`;
  }

  out += `\n[GUNLER]\n`;
  const dayGroups = {};
  const dayOrder = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];
  const dayLabels = {
    'MON': 'Pazartesi',
    'TUE': 'Sali',
    'WED': 'Carsamba',
    'THU': 'Persembe',
    'FRI': 'Cuma',
    'SAT': 'Cumartesi',
    'SUN': 'Pazar'
  };

  for (const d of dayOrder) dayGroups[d] = [];

  for (const occ of (occurrences || [])) {
    if (occ.topic === 'DAILY' && occ.date && dayGroups[occ.date]) {
      dayGroups[occ.date].push(occ.planId || occ.id);
    }
  }

  for (const d of dayOrder) {
    const list = dayGroups[d];
    if (list && list.length > 0) {
      out += `${dayLabels[d]} = ${list.join(', ')}\n`;
    }
  }

  const weeklyTasks = (tasks || []).filter(t => t.kind === 'WEEKLY');
  if (weeklyTasks.length > 0) {
    out += `\n[HAFTALIK]\n`;
    for (const w of weeklyTasks) {
      let line = `${w.taskId} = ${w.title} | ${w.plannedMinutes} dk`;
      if (w.targetCount) line += ` | ${w.targetCount} Soru`;
      if (w.youtubeUrl) line += ` | ${w.youtubeUrl}`;
      out += `${line}\n`;
    }
  }

  return out;
}

module.exports = {
  parseDSL,
  exportToDSL,
  extractUrl
};
