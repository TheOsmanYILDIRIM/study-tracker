const fs = require('fs');
const path = require('path');
const os = require('os');

const CONFIG_DIR = process.env.STUDYTRACKER_CONFIG_DIR || path.join(os.homedir(), '.config', 'studytracker');
const CONFIG_FILE = process.env.STUDYTRACKER_CONFIG_FILE || path.join(CONFIG_DIR, 'config.json');

const DEFAULT_CONFIG = {
  workerUrl: 'https://studytracker-sync.osman13241429.workers.dev',
  familyCode: '',
  adminToken: '',
  childId: 'child_1',
  author: 'Parenting AI',
  lastKnownServerRevision: null
};

const DEFAULT_STAGING_WORKER_URL = 'https://studytracker-v2-staging.osman13241429.workers.dev';

function ensureConfigDir() {
  if (!fs.existsSync(CONFIG_DIR)) {
    fs.mkdirSync(CONFIG_DIR, { recursive: true });
  }
}

function parseRevision(val) {
  if (val === null || val === undefined) return null;
  if (typeof val === 'boolean' || typeof val === 'object') return null;
  if (typeof val === 'string' && val.trim() === '') return null;
  const num = Number(val);
  if (!Number.isFinite(num) || !Number.isInteger(num) || num < 0) {
    return null;
  }
  return num;
}

function loadConfig(options = {}) {
  ensureConfigDir();
  let cfg = { ...DEFAULT_CONFIG };
  if (fs.existsSync(CONFIG_FILE)) {
    try {
      const raw = fs.readFileSync(CONFIG_FILE, 'utf8');
      const parsed = JSON.parse(raw);
      cfg = { ...cfg, ...parsed };
    } catch (e) {
      // ignore parse error
    }
  } else {
    saveConfig(DEFAULT_CONFIG);
  }
  if (options.staging || process.env.STUDYTRACKER_STAGING === 'true' || process.env.STUDY_STAGING === 'true') {
    cfg.workerUrl = process.env.STUDYTRACKER_STAGING_WORKER_URL || DEFAULT_STAGING_WORKER_URL;
    if (process.env.STUDYTRACKER_STAGING_FAMILY_CODE) {
      cfg.familyCode = process.env.STUDYTRACKER_STAGING_FAMILY_CODE.toUpperCase().trim();
    }
    if (process.env.STUDYTRACKER_STAGING_ADMIN_TOKEN) {
      cfg.adminToken = process.env.STUDYTRACKER_STAGING_ADMIN_TOKEN.trim();
    }
    cfg.isStaging = true;
  } else {
    if (process.env.STUDYTRACKER_WORKER_URL) {
      cfg.workerUrl = process.env.STUDYTRACKER_WORKER_URL.trim();
    } else if (process.env.STUDYTRACKER_API_URL) {
      cfg.workerUrl = process.env.STUDYTRACKER_API_URL.trim();
    }
    if (process.env.STUDYTRACKER_ADMIN_TOKEN) cfg.adminToken = process.env.STUDYTRACKER_ADMIN_TOKEN.trim();
    if (process.env.STUDYTRACKER_FAMILY_CODE) {
      cfg.familyCode = process.env.STUDYTRACKER_FAMILY_CODE.toUpperCase().trim();
    } else if (process.env.STUDY_FAMILY_CODE) {
      cfg.familyCode = process.env.STUDY_FAMILY_CODE.toUpperCase().trim();
    }
  }
  cfg.lastKnownServerRevision = parseRevision(cfg.lastKnownServerRevision);
  return cfg;
}

function saveConfig(cfg) {
  ensureConfigDir();
  fs.writeFileSync(CONFIG_FILE, JSON.stringify(cfg, null, 2), 'utf8');
}

function getLastKnownServerRevision() {
  const cfg = loadConfig();
  return parseRevision(cfg.lastKnownServerRevision);
}

function setLastKnownServerRevision(rev) {
  if (rev === null || rev === undefined) {
    const cfg = loadConfig();
    cfg.lastKnownServerRevision = null;
    saveConfig(cfg);
    return null;
  }
  const parsed = parseRevision(rev);
  if (parsed === null) {
    return null;
  }
  const cfg = loadConfig();
  cfg.lastKnownServerRevision = parsed;
  saveConfig(cfg);
  return parsed;
}

function setAdminToken(token) {
  const cfg = loadConfig();
  cfg.adminToken = (token || '').trim();
  saveConfig(cfg);
  return cfg.adminToken;
}

function setFamilyCode(code) {
  const cfg = loadConfig();
  const clean = (code || '').toUpperCase().trim();
  if (clean !== cfg.familyCode) {
    cfg.lastKnownServerRevision = null;
  }
  cfg.familyCode = clean;
  saveConfig(cfg);
  return cfg.familyCode;
}

module.exports = {
  CONFIG_FILE,
  loadConfig,
  saveConfig,
  setFamilyCode,
  setAdminToken,
  parseRevision,
  getLastKnownServerRevision,
  setLastKnownServerRevision
};

