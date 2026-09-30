const { loadConfig } = require('./config');
const { makeRequest } = require('./api');

function getV2Headers(familyCode, role = 'PARENT') {
  const config = loadConfig();
  const headers = {
    'X-Family-Code': familyCode,
    'X-Sender-Role': role
  };
  if (config.adminToken) {
    headers['X-Admin-Token'] = config.adminToken;
  }
  return headers;
}

// 1. CATALOG / CURRICULUM
async function fetchV2Catalog(overrideCode, courseId = null) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil. Önce config set-code kullanın.');
  let url = `${config.workerUrl}/api/v3/catalog?code=${encodeURIComponent(code)}`;
  if (courseId) url += `&courseId=${encodeURIComponent(courseId)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

// 2. COURSES
async function listV2Courses(overrideCode, includeArchived = false) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/courses?code=${encodeURIComponent(code)}&includeArchived=${includeArchived}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

async function createV2Course(overrideCode, courseData) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/courses?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) }, courseData);
}

async function archiveV2Course(overrideCode, courseId) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/courses/${encodeURIComponent(courseId)}/archive?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) });
}

// 3. LESSONS
async function listV2Lessons(overrideCode, courseId = null, includeArchived = false) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  let url = `${config.workerUrl}/api/v3/lessons?code=${encodeURIComponent(code)}&includeArchived=${includeArchived}`;
  if (courseId) url += `&courseId=${encodeURIComponent(courseId)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

async function createV2Lesson(overrideCode, lessonData) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/lessons?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) }, lessonData);
}

async function archiveV2Lesson(overrideCode, lessonId) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/lessons/${encodeURIComponent(lessonId)}/archive?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) });
}

// 4. LEARNING ITEMS
async function listV2Items(overrideCode, lessonId = null, includeArchived = false) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  let url = `${config.workerUrl}/api/v3/items?code=${encodeURIComponent(code)}&includeArchived=${includeArchived}`;
  if (lessonId) url += `&lessonId=${encodeURIComponent(lessonId)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

async function getV2Item(overrideCode, itemId) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/items/${encodeURIComponent(itemId)}?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

async function createV2Item(overrideCode, itemData) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/items?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) }, itemData);
}

async function updateV2ItemContent(overrideCode, itemId, contentData) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/items/${encodeURIComponent(itemId)}/content?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'PATCH', headers: getV2Headers(code) }, contentData);
}

async function reorderV2Item(overrideCode, itemId, reorderData) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/items/${encodeURIComponent(itemId)}/reorder?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) }, reorderData);
}

async function archiveV2Item(overrideCode, itemId) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/items/${encodeURIComponent(itemId)}/archive?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) });
}

// 5. PREREQUISITES
async function addV2Prerequisite(overrideCode, prereqData) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/prerequisites?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code) }, prereqData);
}

async function removeV2Prerequisite(overrideCode, itemId, requiredItemId) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/prerequisites?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'DELETE', headers: getV2Headers(code) }, { itemId, requiredItemId });
}

async function listV2Prerequisites(overrideCode, itemId = null) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  let url = `${config.workerUrl}/api/v3/prerequisites?code=${encodeURIComponent(code)}`;
  if (itemId) url += `&itemId=${encodeURIComponent(itemId)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

// 6. ATTEMPTS
async function recordV2Attempt(overrideCode, attemptData, senderRole = 'CLIENT') {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  const url = `${config.workerUrl}/api/v3/attempts?code=${encodeURIComponent(code)}`;
  return await makeRequest(url, { method: 'POST', headers: getV2Headers(code, senderRole) }, attemptData);
}

async function listV2Attempts(overrideCode, { studentId = null, itemId = null, limit = 50 } = {}) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  let url = `${config.workerUrl}/api/v3/attempts?code=${encodeURIComponent(code)}&limit=${limit}`;
  if (studentId) url += `&studentId=${encodeURIComponent(studentId)}`;
  if (itemId) url += `&itemId=${encodeURIComponent(itemId)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

// 7. PROGRESS & ANALYTICS
async function getV2Progress(overrideCode, studentId = 'student_default', courseId = null) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  let url = `${config.workerUrl}/api/v3/analytics/progress?code=${encodeURIComponent(code)}&studentId=${encodeURIComponent(studentId)}`;
  if (courseId) url += `&courseId=${encodeURIComponent(courseId)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

// 8. BOUNDED AI CONTEXT EXPORT
async function exportV2Context(overrideCode, studentId = 'student_default', courseId = null) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil.');
  let url = `${config.workerUrl}/api/v3/export-context?code=${encodeURIComponent(code)}&studentId=${encodeURIComponent(studentId)}`;
  if (courseId) url += `&courseId=${encodeURIComponent(courseId)}`;
  return await makeRequest(url, { method: 'GET', headers: getV2Headers(code) });
}

module.exports = {
  fetchV2Catalog,
  listV2Courses,
  createV2Course,
  archiveV2Course,
  listV2Lessons,
  createV2Lesson,
  archiveV2Lesson,
  listV2Items,
  getV2Item,
  createV2Item,
  updateV2ItemContent,
  reorderV2Item,
  archiveV2Item,
  addV2Prerequisite,
  removeV2Prerequisite,
  listV2Prerequisites,
  recordV2Attempt,
  listV2Attempts,
  getV2Progress,
  exportV2Context
};
