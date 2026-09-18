/**
 * Terminal UI / ANSI Formatter for StudyTracker CLI
 */

const colors = {
  reset: '\x1b[0m',
  bold: '\x1b[1m',
  dim: '\x1b[2m',
  italic: '\x1b[3m',
  underline: '\x1b[4m',

  // Colors
  cyan: '\x1b[36m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  red: '\x1b[31m',
  magenta: '\x1b[35m',
  blue: '\x1b[34m',
  white: '\x1b[37m',
  gray: '\x1b[90m',

  // Bright
  brightCyan: '\x1b[96m',
  brightGreen: '\x1b[92m',
  brightYellow: '\x1b[93m',
  brightRed: '\x1b[91m',
  brightWhite: '\x1b[97m',

  // Backgrounds
  bgDark: '\x1b[48;5;234m',
  bgCyan: '\x1b[46m',
  bgRed: '\x1b[41m'
};

function statusBadge(status) {
  switch (status) {
    case 'APPROVED':
      return `${colors.green}✔ [ONAYLANDI]${colors.reset}`;
    case 'WAITING_REVIEW':
      return `${colors.brightYellow}⏳ [İNCELEME BEKLİYOR]${colors.reset}`;
    case 'ACTIVE':
      return `${colors.brightCyan}⚡ [CANLI ÇALIŞIYOR]${colors.reset}`;
    case 'REJECTED':
      return `${colors.brightRed}❌ [REDDEDİLDİ / TEKRAR]${colors.reset}`;
    default:
      return `${colors.gray}○ [BEKLEMEDE]${colors.reset}`;
  }
}

function printHeader(familyCode, planWeekId) {
  console.log(`\n${colors.cyan}╔════════════════════════════════════════════════════════════════════╗${colors.reset}`);
  console.log(`${colors.cyan}║   ${colors.brightWhite}${colors.bold}🎓 StudyTracker Parenting & AI Cloud Administration CLI${colors.reset}${colors.cyan}         ║${colors.reset}`);
  console.log(`${colors.cyan}║   ${colors.yellow}Aile Kodu: ${colors.brightWhite}${familyCode}${colors.reset}   ${colors.gray}|${colors.reset}   ${colors.green}Hafta: ${colors.brightWhite}${planWeekId || 'Aktif Plan Yok'}${colors.reset}${colors.cyan}                      ║${colors.reset}`);
  console.log(`${colors.cyan}╚════════════════════════════════════════════════════════════════════╝${colors.reset}\n`);
}

function renderDashboard(data) {
  const familyCode = data.familyCode || 'ST-2026';
  const plan = data.plan;
  const occurrences = data.occurrences || [];
  const sessions = data.sessions || [];
  const reviews = data.reviews || [];
  const quizzes = data.quizzes || [];

  printHeader(familyCode, plan?.weekId);

  // 1. Stats Summary
  const totalOcc = occurrences.length + quizzes.length;
  const approvedOcc = occurrences.filter(o => o.status === 'APPROVED').length + quizzes.filter(q => q.completed).length;
  const waitingReviewOcc = occurrences.filter(o => o.status === 'WAITING_REVIEW').length;
  const rejectedOcc = occurrences.filter(o => o.status === 'REJECTED' || (o.parentNote && o.parentNote.toLowerCase().includes('reddedil'))).length;
  const percent = totalOcc > 0 ? Math.round((approvedOcc / totalOcc) * 100) : 0;

  console.log(`${colors.bold}📊 İlerleme Özeti:${colors.reset}`);
  console.log(`  • Toplam Görev: ${colors.brightWhite}${totalOcc}${colors.reset} (Ders: ${occurrences.length}, Test: ${quizzes.length})`);
  console.log(`  • Onaylanan   : ${colors.green}${approvedOcc}${colors.reset} / ${totalOcc} (${colors.bold}${percent}%${colors.reset})`);
  console.log(`  • İncelenecek : ${waitingReviewOcc > 0 ? colors.brightYellow : colors.gray}${waitingReviewOcc} oturum${colors.reset}`);
  if (rejectedOcc > 0) {
    console.log(`  • Reddedilen  : ${colors.brightRed}${rejectedOcc} görev${colors.reset}`);
  }
  console.log('');

  // 2. Day by Day Tasks Table
  const dayNames = {
    'MON': 'Pazartesi',
    'TUE': 'Salı',
    'WED': 'Çarşamba',
    'THU': 'Perşembe',
    'FRI': 'Cuma',
    'SAT': 'Cumartesi',
    'SUN': 'Pazar',
    '': 'Haftalık Hedefler'
  };

  const dayOrder = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN', ''];

  for (const dayCode of dayOrder) {
    const dayOccs = occurrences.filter(o => (o.date || '') === dayCode);
    if (dayOccs.length === 0) continue;

    console.log(`${colors.bold}${colors.brightCyan}📅 ${dayNames[dayCode] || dayCode} (${dayOccs.length} Ders):${colors.reset}`);
    for (const occ of dayOccs) {
      const duration = occ.targetDurationMin ? `${occ.targetDurationMin} dk` : '';
      const questions = occ.targetQuestionCount > 0 ? `${occ.targetQuestionCount} Soru` : '';
      const meta = [duration, questions].filter(Boolean).join(' • ');

      console.log(`  ${statusBadge(occ.status)} ${colors.brightWhite}${occ.subject}${colors.reset} ${colors.gray}(${meta})${colors.reset}`);
      if (occ.youtubeUrl) {
        console.log(`    ${colors.red}🎬 Video:${colors.reset} ${colors.dim}${occ.youtubeUrl}${colors.reset}`);
      }
      if (occ.parentNote) {
        console.log(`    ${colors.yellow}💬 Veli Yönergesi:${colors.reset} ${occ.parentNote}`);
      }
      if (occ.studentNote) {
        console.log(`    ${colors.cyan}📝 Öğrenci Notu:${colors.reset} ${colors.italic}${occ.studentNote}${colors.reset}`);
      }
    }
    console.log('');
  }

  // 3. Waiting Reviews List (Exclude already reviewed sessions)
  const reviewedSessionIds = new Set(reviews.map(r => r.sessionId));
  const pendingSessions = sessions.filter(s => s.isCompleted && !reviewedSessionIds.has(s.id));
  if (pendingSessions.length > 0) {
    console.log(`${colors.bold}${colors.brightYellow}🔍 Onay Bekleyen Öğrenci Oturumları (${pendingSessions.length}):${colors.reset}`);
    for (const s of pendingSessions) {
      console.log(`  • ID: ${colors.yellow}${s.id}${colors.reset} | Ders: ${colors.brightWhite}${s.occurrenceId}${colors.reset} | Süre: ${s.durationMin} dk`);
      if (s.notes) console.log(`    Öğrenci Notu: ${colors.italic}${s.notes}${colors.reset}`);
    }
    console.log('');
  }
}

module.exports = {
  colors,
  statusBadge,
  printHeader,
  renderDashboard
};
