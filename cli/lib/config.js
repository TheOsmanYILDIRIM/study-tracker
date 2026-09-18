const fs = require('fs');
const path = require('path');
const os = require('os');

const CONFIG_DIR = path.join(os.homedir(), '.config', 'studytracker');
const CONFIG_FILE = path.join(CONFIG_DIR, 'config.json');

const DEFAULT_CONFIG = {
  workerUrl: 'https://studytracker-sync.osman13241429.workers.dev',
  familyCode: 'ST-2026',
  childId: 'child_1',
  author: 'Parenting AI'
};

function ensureConfigDir() {
  if (!fs.existsSync(CONFIG_DIR)) {
    fs.mkdirSync(CONFIG_DIR, { recursive: true });
  }
}

function loadConfig() {
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
  if (process.env.STUDYTRACKER_FAMILY_CODE) {
    cfg.familyCode = process.env.STUDYTRACKER_FAMILY_CODE.toUpperCase().trim();
  } else if (process.env.STUDY_FAMILY_CODE) {
    cfg.familyCode = process.env.STUDY_FAMILY_CODE.toUpperCase().trim();
  }
  return cfg;
}

function saveConfig(cfg) {
  ensureConfigDir();
  fs.writeFileSync(CONFIG_FILE, JSON.stringify(cfg, null, 2), 'utf8');
}

function setFamilyCode(code) {
  const cfg = loadConfig();
  cfg.familyCode = code.toUpperCase().trim();
  saveConfig(cfg);
  return cfg.familyCode;
}

module.exports = {
  CONFIG_FILE,
  loadConfig,
  saveConfig,
  setFamilyCode
};
