#!/usr/bin/env node
/**
 * Export the modular V2 source tree as one portable JSON file for app/server transfer.
 * Source of truth stays under content/v2; this file is a transport artifact only.
 */
const fs = require('fs');
const path = require('path');
const { validateModularTree, compileModularCatalog } = require('../cli/lib/v2-modular');

function main() {
  const sourceDir = path.resolve(__dirname, '../content/v2');
  const outDir = path.resolve(__dirname, '../dist');
  const outPath = path.join(outDir, 'studytracker-v2-catalog.json');

  const validation = validateModularTree(sourceDir);
  if (!validation.valid) {
    console.error('V2 modular content validation failed:');
    validation.errors.forEach(err => console.error(' - ' + err));
    process.exit(1);
  }

  const catalog = compileModularCatalog(sourceDir);
  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(outPath, JSON.stringify(catalog, null, 2) + '\n', 'utf8');

  const lessonCount = catalog.courses.reduce((n, c) => n + (c.lessons || []).length, 0);
  const itemCount = catalog.courses.reduce(
    (n, c) => n + (c.lessons || []).reduce((m, l) => m + (l.items || []).length, 0),
    0
  );

  console.log(JSON.stringify({
    success: true,
    source: 'content/v2',
    artifact: outPath,
    schemaVersion: catalog.schemaVersion,
    courses: catalog.courses.length,
    lessons: lessonCount,
    items: itemCount,
    bytes: fs.statSync(outPath).size
  }, null, 2));
}

if (require.main === module) main();
module.exports = { main };
