/**
 * StudyTracker V2 Modular Content Source Architecture
 * Provides compilation, decomposition, and validation for modular curriculum content.
 * 
 * Directory Structure:
 *   content/v2/catalog.json                 # top-level metadata + course refs
 *   content/v2/courses/<courseId>.json      # course metadata + lesson refs only
 *   content/v2/lessons/<lessonId>.json      # lesson metadata + item refs only
 *   content/v2/items/<itemId>.json          # one full VIDEO/QUIZ/ANKI object per file
 *   content/v2/transcripts/.gitkeep         # gitignored transcript cache
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { validateQuizSchema } = require('./v2-quiz');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

/**
 * Decompose a monolithic catalog JSON object into modular files in content/v2
 */
function decomposeCatalog(catalogObj, targetDir) {
  const v2Dir = path.resolve(targetDir);
  const coursesDir = path.join(v2Dir, 'courses');
  const lessonsDir = path.join(v2Dir, 'lessons');
  const itemsDir = path.join(v2Dir, 'items');
  const transcriptsDir = path.join(v2Dir, 'transcripts');

  [v2Dir, coursesDir, lessonsDir, itemsDir, transcriptsDir].forEach(dir => {
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
  });

  const gitkeepPath = path.join(transcriptsDir, '.gitkeep');
  if (!fs.existsSync(gitkeepPath)) {
    fs.writeFileSync(gitkeepPath, '', 'utf8');
  }

  const courseRefs = [];

  for (const course of catalogObj.courses || []) {
    courseRefs.push(course.id);
    const lessonRefs = [];

    for (const lesson of course.lessons || []) {
      lessonRefs.push(lesson.id);
      const itemRefs = [];

      for (const item of lesson.items || []) {
        itemRefs.push(item.id);

        const itemData = {
          id: item.id,
          courseId: course.id,
          lessonId: lesson.id,
          stableKey: item.stableKey,
          itemType: item.itemType,
          displayLabel: item.displayLabel,
          orderKey: item.orderKey,
          title: item.title,
          contentUrl: item.contentUrl !== undefined ? item.contentUrl : null,
          publishingStatus: item.publishingStatus || 'active',
          payload: item.payload || {}
        };

        const itemFilePath = path.join(itemsDir, `${item.id}.json`);
        fs.writeFileSync(itemFilePath, JSON.stringify(itemData, null, 2) + '\n', 'utf8');
      }

      const lessonData = {
        id: lesson.id,
        courseId: course.id,
        stableKey: lesson.stableKey,
        title: lesson.title,
        orderKey: lesson.orderKey,
        items: itemRefs
      };

      const lessonFilePath = path.join(lessonsDir, `${lesson.id}.json`);
      fs.writeFileSync(lessonFilePath, JSON.stringify(lessonData, null, 2) + '\n', 'utf8');
    }

    const courseData = {
      id: course.id,
      title: course.title,
      subject: course.subject,
      gradeLevel: course.gradeLevel,
      orderKey: course.orderKey,
      description: course.description || '',
      lessons: lessonRefs
    };

    const courseFilePath = path.join(coursesDir, `${course.id}.json`);
    fs.writeFileSync(courseFilePath, JSON.stringify(courseData, null, 2) + '\n', 'utf8');
  }

  const catalogData = {
    schemaVersion: catalogObj.schemaVersion || 'v2',
    generatedAt: catalogObj.generatedAt || new Date().toISOString(),
    gradeLevel: catalogObj.gradeLevel || '9. Sınıf',
    academicYear: catalogObj.academicYear || '2026-2027',
    targetCurriculum: catalogObj.targetCurriculum || 'MEB Türkiye Yüzyılı Maarif Modeli (9. Sınıf 2026-2027)',
    courses: courseRefs
  };

  const catalogFilePath = path.join(v2Dir, 'catalog.json');
  fs.writeFileSync(catalogFilePath, JSON.stringify(catalogData, null, 2) + '\n', 'utf8');

  return {
    catalogFilePath,
    coursesCount: courseRefs.length,
    lessonsCount: (catalogObj.courses || []).reduce((acc, c) => acc + (c.lessons || []).length, 0),
    itemsCount: (catalogObj.courses || []).reduce((acc, c) => acc + (c.lessons || []).reduce((a, l) => a + (l.items || []).length, 0), 0)
  };
}

/**
 * Validate modular source tree
 */
function validateModularTree(sourceDir) {
  const v2Dir = path.resolve(sourceDir);
  const catalogPath = path.join(v2Dir, 'catalog.json');
  const coursesDir = path.join(v2Dir, 'courses');
  const lessonsDir = path.join(v2Dir, 'lessons');
  const itemsDir = path.join(v2Dir, 'items');

  const errors = [];
  const warnings = [];

  if (!fs.existsSync(catalogPath)) {
    return {
      valid: false,
      errors: [`Root catalog file missing: ${catalogPath}`],
      warnings: [],
      stats: {}
    };
  }

  let catalogObj;
  try {
    catalogObj = JSON.parse(fs.readFileSync(catalogPath, 'utf8'));
  } catch (err) {
    return {
      valid: false,
      errors: [`Malformed catalog.json: ${err.message}`],
      warnings: [],
      stats: {}
    };
  }

  if (catalogObj.schemaVersion !== 'v2') {
    errors.push(`Invalid schemaVersion in catalog.json: expected "v2", got "${catalogObj.schemaVersion}"`);
  }

  const courseRefs = catalogObj.courses || catalogObj.courseRefs || [];
  if (!Array.isArray(courseRefs) || courseRefs.length === 0) {
    errors.push('catalog.json must specify a non-empty array of course references in "courses"');
  }

  const seenCourseIds = new Set();
  const seenLessonIds = new Set();
  const seenItemIds = new Set();
  const seenStableKeys = new Set();
  const validItemTypes = new Set(['VIDEO', 'QUIZ', 'ANKI']);

  const referencedCourseFiles = new Set();
  const referencedLessonFiles = new Set();
  const referencedItemFiles = new Set();

  let totalLessons = 0;
  let totalItems = 0;
  let videoCount = 0;
  let quizCount = 0;
  let ankiCount = 0;
  let microQuizCount = 0;

  for (const courseRef of courseRefs) {
    const courseId = typeof courseRef === 'string' ? courseRef : courseRef.id;
    if (!courseId) {
      errors.push(`Invalid course ref in catalog.json: ${JSON.stringify(courseRef)}`);
      continue;
    }

    if (seenCourseIds.has(courseId)) {
      errors.push(`Duplicate course id referenced in catalog.json: ${courseId}`);
    }
    seenCourseIds.add(courseId);

    const courseFile = path.join(coursesDir, `${courseId}.json`);
    referencedCourseFiles.add(`${courseId}.json`);

    if (!fs.existsSync(courseFile)) {
      errors.push(`Dangling course ref: file not found for course "${courseId}" at ${courseFile}`);
      continue;
    }

    let courseData;
    try {
      courseData = JSON.parse(fs.readFileSync(courseFile, 'utf8'));
    } catch (err) {
      errors.push(`Malformed course file for "${courseId}": ${err.message}`);
      continue;
    }

    if (courseData.id !== courseId) {
      errors.push(`Course ID mismatch in ${courseFile}: file defines id "${courseData.id}" but filename/ref is "${courseId}"`);
    }
    if (!courseData.title || !courseData.subject) {
      errors.push(`Course "${courseId}" missing required fields (title, subject)`);
    }

    const lessonRefs = courseData.lessons || courseData.lessonRefs || [];
    if (!Array.isArray(lessonRefs)) {
      errors.push(`Course "${courseId}" lessons must be an array`);
      continue;
    }

    for (const lessonRef of lessonRefs) {
      const lessonId = typeof lessonRef === 'string' ? lessonRef : lessonRef.id;
      if (!lessonId) {
        errors.push(`Invalid lesson ref in course "${courseId}": ${JSON.stringify(lessonRef)}`);
        continue;
      }

      if (seenLessonIds.has(lessonId)) {
        errors.push(`Duplicate lesson id referenced: ${lessonId}`);
      }
      seenLessonIds.add(lessonId);
      totalLessons++;

      const lessonFile = path.join(lessonsDir, `${lessonId}.json`);
      referencedLessonFiles.add(`${lessonId}.json`);

      if (!fs.existsSync(lessonFile)) {
        errors.push(`Dangling lesson ref: file not found for lesson "${lessonId}" in course "${courseId}" at ${lessonFile}`);
        continue;
      }

      let lessonData;
      try {
        lessonData = JSON.parse(fs.readFileSync(lessonFile, 'utf8'));
      } catch (err) {
        errors.push(`Malformed lesson file for "${lessonId}": ${err.message}`);
        continue;
      }

      if (lessonData.id !== lessonId) {
        errors.push(`Lesson ID mismatch in ${lessonFile}: file defines id "${lessonData.id}" but ref is "${lessonId}"`);
      }
      if (lessonData.courseId && lessonData.courseId !== courseId) {
        errors.push(`Wrong parent ref in lesson "${lessonId}": points to courseId "${lessonData.courseId}", but referenced from "${courseId}"`);
      }
      if (!lessonData.title) {
        errors.push(`Lesson "${lessonId}" missing required title`);
      }

      const itemRefs = lessonData.items || lessonData.itemRefs || [];
      if (!Array.isArray(itemRefs)) {
        errors.push(`Lesson "${lessonId}" items must be an array`);
        continue;
      }

      let prevOrderKey = -Infinity;

      for (const itemRef of itemRefs) {
        const itemId = typeof itemRef === 'string' ? itemRef : itemRef.id;
        if (!itemId) {
          errors.push(`Invalid item ref in lesson "${lessonId}": ${JSON.stringify(itemRef)}`);
          continue;
        }

        if (seenItemIds.has(itemId)) {
          errors.push(`Duplicate item id: ${itemId}`);
        }
        seenItemIds.add(itemId);
        totalItems++;

        const itemFile = path.join(itemsDir, `${itemId}.json`);
        referencedItemFiles.add(`${itemId}.json`);

        if (!fs.existsSync(itemFile)) {
          errors.push(`Dangling item ref: file not found for item "${itemId}" in lesson "${lessonId}" at ${itemFile}`);
          continue;
        }

        let itemData;
        try {
          itemData = JSON.parse(fs.readFileSync(itemFile, 'utf8'));
        } catch (err) {
          errors.push(`Malformed item file for "${itemId}": ${err.message}`);
          continue;
        }

        if (itemData.id !== itemId) {
          errors.push(`Item ID mismatch in ${itemFile}: file defines id "${itemData.id}" but ref is "${itemId}"`);
        }
        if (itemData.lessonId && itemData.lessonId !== lessonId) {
          errors.push(`Wrong parent ref in item "${itemId}": points to lessonId "${itemData.lessonId}", but referenced from "${lessonId}"`);
        }
        if (itemData.courseId && itemData.courseId !== courseId) {
          errors.push(`Wrong parent ref in item "${itemId}": points to courseId "${itemData.courseId}", but expected "${courseId}"`);
        }

        if (!itemData.stableKey) {
          errors.push(`Item "${itemId}" missing required stableKey`);
        } else {
          if (seenStableKeys.has(itemData.stableKey)) {
            errors.push(`Duplicate stableKey: "${itemData.stableKey}" on item "${itemId}"`);
          }
          seenStableKeys.add(itemData.stableKey);
        }

        if (!validItemTypes.has(itemData.itemType)) {
          errors.push(`Item "${itemId}" has invalid itemType: "${itemData.itemType}"`);
        }

        if (typeof itemData.orderKey !== 'number') {
          errors.push(`Item "${itemId}" orderKey must be a number`);
        } else {
          if (itemData.orderKey <= prevOrderKey) {
            warnings.push({
              itemId,
              warning: `Item orderKey (${itemData.orderKey}) is not strictly greater than previous item orderKey (${prevOrderKey}) in lesson "${lessonId}"`
            });
          }
          prevOrderKey = itemData.orderKey;
        }

        if (!itemData.displayLabel) {
          errors.push(`Item "${itemId}" missing displayLabel`);
        }

        if (itemData.itemType === 'VIDEO') {
          videoCount++;
        } else if (itemData.itemType === 'QUIZ') {
          quizCount++;
          if (itemData.payload?.provenance?.derivedFromItemId) {
            microQuizCount++;
          }
          // Validate Quiz payload schema
          if (itemData.payload?.quiz) {
            const quizVal = validateQuizSchema(itemData.payload.quiz);
            if (!quizVal.valid) {
              errors.push(`Item "${itemId}" has invalid quiz payload: ${quizVal.errors.join('; ')}`);
            }
          }
        } else if (itemData.itemType === 'ANKI') {
          ankiCount++;
        }
      }
    }
  }

  // Check for unreferenced / orphaned files in modular directory
  if (fs.existsSync(coursesDir)) {
    fs.readdirSync(coursesDir).filter(f => f.endsWith('.json')).forEach(f => {
      if (!referencedCourseFiles.has(f)) {
        warnings.push({ warning: `Unreferenced course file in courses/: ${f}` });
      }
    });
  }

  if (fs.existsSync(lessonsDir)) {
    fs.readdirSync(lessonsDir).filter(f => f.endsWith('.json')).forEach(f => {
      if (!referencedLessonFiles.has(f)) {
        warnings.push({ warning: `Unreferenced lesson file in lessons/: ${f}` });
      }
    });
  }

  if (fs.existsSync(itemsDir)) {
    fs.readdirSync(itemsDir).filter(f => f.endsWith('.json')).forEach(f => {
      if (!referencedItemFiles.has(f)) {
        warnings.push({ warning: `Unreferenced item file in items/: ${f}` });
      }
    });
  }

  return {
    valid: errors.length === 0,
    errors,
    warnings,
    stats: {
      courseCount: seenCourseIds.size,
      lessonCount: totalLessons,
      itemCount: totalItems,
      videoCount,
      quizCount,
      ankiCount,
      microQuizCount,
      warningCount: warnings.length
    }
  };
}

/**
 * Compile modular source tree into monolithic catalog JSON object
 */
function compileModularCatalog(sourceDir) {
  const v2Dir = path.resolve(sourceDir);
  const validation = validateModularTree(v2Dir);
  if (!validation.valid) {
    const err = new Error(`Modular catalog validation failed with ${validation.errors.length} error(s):\n  - ${validation.errors.join('\n  - ')}`);
    err.validationErrors = validation.errors;
    throw err;
  }

  const catalogPath = path.join(v2Dir, 'catalog.json');
  const coursesDir = path.join(v2Dir, 'courses');
  const lessonsDir = path.join(v2Dir, 'lessons');
  const itemsDir = path.join(v2Dir, 'items');

  const catalogMeta = JSON.parse(fs.readFileSync(catalogPath, 'utf8'));
  const courseRefs = catalogMeta.courses || catalogMeta.courseRefs || [];

  const compiledCourses = [];

  for (const courseRef of courseRefs) {
    const courseId = typeof courseRef === 'string' ? courseRef : courseRef.id;
    const courseFile = path.join(coursesDir, `${courseId}.json`);
    const courseData = JSON.parse(fs.readFileSync(courseFile, 'utf8'));

    const lessonRefs = courseData.lessons || courseData.lessonRefs || [];
    const compiledLessons = [];

    for (const lessonRef of lessonRefs) {
      const lessonId = typeof lessonRef === 'string' ? lessonRef : lessonRef.id;
      const lessonFile = path.join(lessonsDir, `${lessonId}.json`);
      const lessonData = JSON.parse(fs.readFileSync(lessonFile, 'utf8'));

      const itemRefs = lessonData.items || lessonData.itemRefs || [];
      const compiledItems = [];

      for (const itemRef of itemRefs) {
        const itemId = typeof itemRef === 'string' ? itemRef : itemRef.id;
        const itemFile = path.join(itemsDir, `${itemId}.json`);
        const itemData = JSON.parse(fs.readFileSync(itemFile, 'utf8'));

        // Build clean item object for compiled catalog
        const cleanItem = {
          id: itemData.id,
          stableKey: itemData.stableKey,
          itemType: itemData.itemType,
          displayLabel: itemData.displayLabel,
          orderKey: itemData.orderKey,
          title: itemData.title,
          contentUrl: itemData.contentUrl !== undefined ? itemData.contentUrl : null,
          publishingStatus: itemData.publishingStatus || 'active',
          payload: itemData.payload || {}
        };

        compiledItems.push(cleanItem);
      }

      // Sort items deterministically by orderKey
      compiledItems.sort((a, b) => a.orderKey - b.orderKey);

      const cleanLesson = {
        id: lessonData.id,
        stableKey: lessonData.stableKey,
        title: lessonData.title,
        orderKey: lessonData.orderKey,
        items: compiledItems
      };

      compiledLessons.push(cleanLesson);
    }

    compiledLessons.sort((a, b) => a.orderKey - b.orderKey);

    const cleanCourse = {
      id: courseData.id,
      title: courseData.title,
      subject: courseData.subject,
      gradeLevel: courseData.gradeLevel,
      orderKey: courseData.orderKey,
      description: courseData.description || '',
      lessons: compiledLessons
    };

    compiledCourses.push(cleanCourse);
  }

  compiledCourses.sort((a, b) => a.orderKey - b.orderKey);

  const compiledCatalog = {
    schemaVersion: catalogMeta.schemaVersion || 'v2',
    generatedAt: catalogMeta.generatedAt || new Date().toISOString(),
    gradeLevel: catalogMeta.gradeLevel || '9. Sınıf',
    academicYear: catalogMeta.academicYear || '2026-2027',
    targetCurriculum: catalogMeta.targetCurriculum || 'MEB Türkiye Yüzyılı Maarif Modeli (9. Sınıf 2026-2027)',
    courses: compiledCourses
  };

  return compiledCatalog;
}

/**
 * Save compiled catalog to target path deterministically
 */
function saveCompiledCatalog(compiledCatalog, outputPath) {
  const targetPath = path.resolve(outputPath);
  const jsonText = JSON.stringify(compiledCatalog, null, 2) + '\n';
  fs.writeFileSync(targetPath, jsonText, 'utf8');
  return {
    path: targetPath,
    bytes: Buffer.byteLength(jsonText, 'utf8'),
    courseCount: compiledCatalog.courses.length,
    totalLessons: compiledCatalog.courses.reduce((acc, c) => acc + c.lessons.length, 0),
    totalItems: compiledCatalog.courses.reduce((acc, c) => acc + c.lessons.reduce((a, l) => a + l.items.length, 0), 0)
  };
}

module.exports = {
  decomposeCatalog,
  validateModularTree,
  compileModularCatalog,
  saveCompiledCatalog
};
