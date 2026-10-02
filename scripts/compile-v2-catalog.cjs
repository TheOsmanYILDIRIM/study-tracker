#!/usr/bin/env node
/**
 * Deterministic compiler script for StudyTracker V2 Catalog
 * Compiles content/v2 modular sources into content/9-sinif-v2-catalog.json
 */

const path = require('path');
const { validateModularTree, compileModularCatalog, saveCompiledCatalog } = require('../cli/lib/v2-modular');

function main() {
  const v2Dir = path.resolve(__dirname, '../content/v2');
  const outputPath = path.resolve(__dirname, '../content/9-sinif-v2-catalog.json');

  console.log('📦 Compiling StudyTracker V2 Modular Content...');
  console.log(`   Source tree: ${v2Dir}`);
  console.log(`   Target artifact: ${outputPath}`);

  const validation = validateModularTree(v2Dir);
  if (!validation.valid) {
    console.error(`❌ Validation failed with ${validation.errors.length} error(s):`);
    validation.errors.forEach(err => console.error(`   - ${err}`));
    process.exit(1);
  }

  if (validation.warnings.length > 0) {
    console.warn(`⚠️  Validation reported ${validation.warnings.length} warning(s):`);
    validation.warnings.forEach(w => console.warn(`   - ${w.warning || JSON.stringify(w)}`));
  }

  const compiled = compileModularCatalog(v2Dir);
  const result = saveCompiledCatalog(compiled, outputPath);

  console.log('\n✅ V2 Catalog successfully compiled:');
  console.log(`   - Courses: ${result.courseCount}`);
  console.log(`   - Lessons: ${result.totalLessons}`);
  console.log(`   - Items: ${result.totalItems} (Videos: ${validation.stats.videoCount}, Quizzes: ${validation.stats.quizCount} [Micro: ${validation.stats.microQuizCount}], Anki: ${validation.stats.ankiCount})`);
  console.log(`   - File size: ${(result.bytes / 1024).toFixed(1)} KB`);
}

if (require.main === module) {
  main();
}

module.exports = { main };
