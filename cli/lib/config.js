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
  if (!fs.existsSync(CONFIG_FILE)) {
    saveConfig(DEFAULT_CONFIG);
    return { ...DEFAULT_CONFIG };
  }
  try {
    const raw = fs.readFileSync(CONFIG_FILE, 'utf8');
    const parsed = JSON.parse(raw);
    return { ...DEFAULT_CONFIG, ...parsed };
  } catch (e) {
    return { ...DEFAULT_CONFIG };
  }
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
