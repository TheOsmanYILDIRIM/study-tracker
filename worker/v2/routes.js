/**
 * StudyTracker V2 Router & HTTP Handlers (/api/v3/* namespace)
 * Exposes V2 curriculum, content versioning, puzzle reordering, prerequisites,
 * idempotent append-only attempts, and progress analytics.
 */

import { createV2Storage } from './storage.js';
import { CurriculumEngine } from './curriculum.js';

const CORS_HEADERS = {
  'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Family-Code, X-Sender-Role, X-Admin-Token',
  'Content-Type': 'application/json; charset=utf-8'
};

const json = (data, status = 200) => new Response(JSON.stringify(data), { status, headers: CORS_HEADERS });
const error = (msg, status = 400) => json({ success: false, error: msg }, status);

export async function handleV2Request(request, env, inMemoryStore, familyCode, role, hasAdmin) {
  const url = new URL(request.url);
  const path = url.pathname;
  const method = request.method;

  const storage = createV2Storage(env, inMemoryStore);
  const engine = new CurriculumEngine(storage);

  const isParent = ['PARENT', 'ADMIN', 'CLI', 'PARENTING_AI'].includes(role) || hasAdmin;

  try {
    // 0. HEALTH CHECK
    if (path === '/api/v3/health' && method === 'GET') {
      const isStaging = Boolean(env?.STAGING === 'true' || env?.STAGING === true || env?.ENVIRONMENT === 'staging');
      const environment = env?.ENVIRONMENT || (isStaging ? 'staging' : 'production');
      return json({
        success: true,
        status: 'ok',
        version: '2.0.0',
        schemaVersion: 'v2',
        storageBackend: storage.type,
        environment,
        staging: isStaging,
        database: env?.DB_NAME || (storage.type === 'd1' ? 'studytracker-v2-staging' : null),
        revision: env?.BUILD_REVISION || 'unknown',
        timestamp: Date.now()
      });
    }

    // 1. GET /api/v3/catalog or /api/v3/curriculum - Full tree view
    if ((path === '/api/v3/catalog' || path === '/api/v3/curriculum') && method === 'GET') {
      const courseId = url.searchParams.get('courseId');
      const statusParam = url.searchParams.get('status'); // 'active' | 'draft' | 'archived' | 'all'
      const includeArchived = url.searchParams.get('includeArchived') === 'true' || statusParam === 'all' || statusParam === 'archived';
      const includeDrafts = url.searchParams.get('includeDrafts') === 'true' || statusParam === 'all' || statusParam === 'draft';
      
      const effectiveStatusFilter = statusParam ? statusParam : (includeArchived || includeDrafts ? 'all' : 'active');

      const courses = await engine.getCourses(familyCode, includeArchived);
      const targetCourses = courseId ? courses.filter(c => c.id === courseId) : courses;

      const tree = await Promise.all(targetCourses.map(async course => {
        const lessons = await engine.getLessons(familyCode, course.id, includeArchived);
        const enrichedLessons = await Promise.all(lessons.map(async lesson => {
          const items = await engine.getItems(familyCode, lesson.id, includeArchived, effectiveStatusFilter);
          return { ...lesson, items };
        }));
        return { ...course, lessons: enrichedLessons };
      }));

      return json({ success: true, storageType: storage.type, familyCode, curriculum: tree });
    }

    // 1b. POST /api/v3/catalog/import - Parent/admin bulk catalog upsert
    if (path === '/api/v3/catalog/import' && method === 'POST') {
      if (!hasAdmin) return error('Admin authorization required', 403);

      const body = await request.json().catch(() => ({}));
      if (body.schemaVersion !== 'v2' || !Array.isArray(body.courses)) {
        return error('Invalid V2 catalog manifest', 400);
      }

      let courseCount = 0;
      let lessonCount = 0;
      let itemCount = 0;
      let createdItems = 0;
      let updatedItems = 0;
      let identicalItems = 0;

      for (const courseInput of body.courses) {
        await engine.createCourse(familyCode, {
          id: courseInput.id,
          title: courseInput.title,
          subject: courseInput.subject,
          gradeLevel: courseInput.gradeLevel || 9,
          description: courseInput.description || '',
          visual: courseInput.visual || null,
          orderKey: courseInput.orderKey
        });
        courseCount++;

        for (const lessonInput of (courseInput.lessons || [])) {
          await engine.createLesson(familyCode, {
            id: lessonInput.id,
            courseId: courseInput.id,
            title: lessonInput.title,
            orderKey: lessonInput.orderKey
          });
          lessonCount++;

          for (const itemInput of (lessonInput.items || [])) {
            const existing = await engine.getItem(familyCode, itemInput.id);
            const desiredTitle = itemInput.title || itemInput.displayLabel;
            const desiredUrl = itemInput.contentUrl ?? null;
            const desiredPayload = itemInput.payload || {};
            const desiredStatus = itemInput.publishingStatus || 'active';

            if (!existing) {
              await engine.createItem(familyCode, {
                id: itemInput.id,
                lessonId: lessonInput.id,
                itemType: itemInput.itemType,
                displayLabel: itemInput.displayLabel,
                stableKey: itemInput.stableKey || itemInput.id,
                title: desiredTitle,
                contentUrl: desiredUrl,
                payload: desiredPayload,
                orderKey: itemInput.orderKey,
                publishingStatus: desiredStatus
              });
              createdItems++;
            } else {
              const current = existing.currentVersion || {};
              const contentChanged =
                (current.title || existing.displayLabel) !== desiredTitle ||
                (current.contentUrl ?? null) !== desiredUrl ||
                JSON.stringify(current.payload || {}) !== JSON.stringify(desiredPayload);

              if (contentChanged) {
                await engine.updateItemContent(familyCode, itemInput.id, {
                  title: desiredTitle,
                  contentUrl: desiredUrl,
                  payload: desiredPayload,
                  changelog: 'Catalog file import',
                  publishingStatus: desiredStatus
                });
                updatedItems++;
              } else {
                if ((existing.publishingStatus || 'active') !== desiredStatus) {
                  await engine.updateItemStatus(familyCode, itemInput.id, desiredStatus);
                }
                identicalItems++;
              }
            }
            itemCount++;
          }
        }
      }

      return json({
        success: true,
        familyCode,
        imported: {
          courses: courseCount,
          lessons: lessonCount,
          items: itemCount,
          createdItems,
          updatedItems,
          identicalItems
        }
      });
    }

    // 2. COURSES
    if (path === '/api/v3/courses') {
      if (method === 'GET') {
        const courses = await engine.getCourses(familyCode, url.searchParams.get('includeArchived') === 'true');
        return json({ success: true, courses });
      }
      if (method === 'POST') {
        const body = await request.json().catch(() => ({}));
        const course = await engine.createCourse(familyCode, body);
        return json({ success: true, course }, 201);
      }
    }

    if (path.startsWith('/api/v3/courses/') && path.endsWith('/archive') && method === 'POST') {
      const parts = path.split('/');
      const courseId = parts[parts.length - 2];
      const course = await engine.archiveCourse(familyCode, courseId);
      return json({ success: true, course });
    }

    // 3. LESSONS
    if (path === '/api/v3/lessons') {
      if (method === 'GET') {
        const courseId = url.searchParams.get('courseId');
        const lessons = await engine.getLessons(familyCode, courseId, url.searchParams.get('includeArchived') === 'true');
        return json({ success: true, lessons });
      }
      if (method === 'POST') {
        const body = await request.json().catch(() => ({}));
        const lesson = await engine.createLesson(familyCode, body);
        return json({ success: true, lesson }, 201);
      }
    }

    if (path.startsWith('/api/v3/lessons/') && path.endsWith('/archive') && method === 'POST') {
      const parts = path.split('/');
      const lessonId = parts[parts.length - 2];
      const lesson = await engine.archiveLesson(familyCode, lessonId);
      return json({ success: true, lesson });
    }

    // 4. LEARNING ITEMS
    if (path === '/api/v3/items') {
      if (method === 'GET') {
        const lessonId = url.searchParams.get('lessonId');
        const statusParam = url.searchParams.get('status');
        const includeArchived = url.searchParams.get('includeArchived') === 'true' || statusParam === 'all' || statusParam === 'archived';
        const items = await engine.getItems(familyCode, lessonId, includeArchived, statusParam || (includeArchived ? 'all' : 'active'));
        return json({ success: true, items });
      }
      if (method === 'POST') {
        const body = await request.json().catch(() => ({}));
        const item = await engine.createItem(familyCode, body);
        return json({ success: true, item }, 201);
      }
    }

    // Learning item single get
    const itemMatch = path.match(/^\/api\/v3\/items\/([^/]+)$/);
    if (itemMatch && method === 'GET') {
      const itemId = itemMatch[1];
      const item = await engine.getItem(familyCode, itemId);
      if (!item) return error(`Item not found: ${itemId}`, 404);
      return json({ success: true, item });
    }

    // PATCH /api/v3/items/:itemId/content -> Creates NEW version without changing item_id
    const contentMatch = path.match(/^\/api\/v3\/items\/([^/]+)\/content$/);
    if (contentMatch && method === 'PATCH') {
      const itemId = contentMatch[1];
      const body = await request.json().catch(() => ({}));
      const item = await engine.updateItemContent(familyCode, itemId, body);
      return json({ success: true, item, newVersion: item.currentVersion });
    }

    // PATCH/PUT /api/v3/items/:itemId/status -> Updates publishing status without changing item_id
    const statusMatch = path.match(/^\/api\/v3\/items\/([^/]+)\/status$/);
    if (statusMatch && (method === 'PATCH' || method === 'PUT')) {
      const itemId = statusMatch[1];
      const body = await request.json().catch(() => ({}));
      const pubStatus = body.status || body.publishingStatus;
      if (!pubStatus) return error('status is required', 400);
      const item = await engine.updateItemStatus(familyCode, itemId, pubStatus);
      return json({ success: true, item });
    }

    // POST /api/v3/items/:itemId/review -> Review workflow (approve / reject / replace_content)
    const reviewMatch = path.match(/^\/api\/v3\/items\/([^/]+)\/review$/);
    if (reviewMatch && method === 'POST') {
      const itemId = reviewMatch[1];
      const body = await request.json().catch(() => ({}));
      const item = await engine.getItem(familyCode, itemId);
      if (!item) return error(`Item not found: ${itemId}`, 404);

      const action = (body.action || '').toUpperCase();
      const currentVer = item.currentVersion || {};
      const currentPayload = currentVer.payload || {};
      const currentProv = currentPayload.provenance || {};
      const reviewHistory = Array.isArray(currentProv.reviewHistory) ? [...currentProv.reviewHistory] : [];
      const timestamp = new Date().toISOString();

      if (action === 'APPROVE') {
        reviewHistory.push({
          action: 'APPROVE',
          reviewer: body.reviewer || role || 'reviewer',
          note: body.note || 'Approved via review workflow',
          timestamp
        });
        const updatedPayload = {
          ...currentPayload,
          provenance: {
            ...currentProv,
            reviewStatus: 'verified',
            reviewedOverride: true,
            reviewedBy: body.reviewer || role || 'reviewer',
            reviewedAt: timestamp,
            reviewNotes: body.note || null,
            reviewHistory
          }
        };
        const updatedItem = await engine.updateItemContent(familyCode, itemId, {
          title: currentVer.title,
          contentUrl: currentVer.contentUrl,
          payload: updatedPayload,
          changelog: `Approved: ${body.note || 'Verified content'}`,
          publishingStatus: 'active'
        });
        return json({ success: true, item: updatedItem });
      } else if (action === 'REJECT') {
        if (!body.reason) return error('reason is required for reject', 400);
        reviewHistory.push({
          action: 'REJECT',
          reviewer: body.reviewer || role || 'reviewer',
          reason: body.reason,
          timestamp
        });
        const updatedPayload = {
          ...currentPayload,
          provenance: {
            ...currentProv,
            reviewStatus: 'rejected',
            reviewedOverride: true,
            reviewedBy: body.reviewer || role || 'reviewer',
            reviewedAt: timestamp,
            rejectReason: body.reason,
            reviewHistory
          }
        };
        const updatedItem = await engine.updateItemContent(familyCode, itemId, {
          title: currentVer.title,
          contentUrl: currentVer.contentUrl,
          payload: updatedPayload,
          changelog: `Rejected: ${body.reason}`,
          publishingStatus: 'draft' // unpublish active content without deleting attempts
        });
        return json({ success: true, item: updatedItem });
      } else if (action === 'REPLACE_CONTENT') {
        const replacement = body.content || body;
        reviewHistory.push({
          action: 'REPLACE_CONTENT',
          reviewer: body.reviewer || role || 'reviewer',
          note: body.note || 'Replaced content via review workflow',
          timestamp
        });
        const updatedPayload = {
          ...(replacement.payload || currentPayload),
          provenance: {
            ...(replacement.payload?.provenance || currentProv),
            reviewStatus: 'verified',
            reviewedOverride: true,
            reviewedBy: body.reviewer || role || 'reviewer',
            reviewedAt: timestamp,
            reviewNotes: body.note || null,
            reviewHistory
          }
        };
        const updatedItem = await engine.updateItemContent(familyCode, itemId, {
          title: replacement.title || currentVer.title,
          contentUrl: replacement.contentUrl !== undefined ? replacement.contentUrl : currentVer.contentUrl,
          payload: updatedPayload,
          changelog: replacement.changelog || body.note || 'Replaced content version',
          publishingStatus: replacement.publishingStatus || 'active'
        });
        return json({ success: true, item: updatedItem });
      } else {
        return error(`Invalid review action: ${action}. Must be APPROVE, REJECT, or REPLACE_CONTENT`, 400);
      }
    }

    // POST /api/v3/items/:itemId/reorder -> Puzzle reordering
    const reorderMatch = path.match(/^\/api\/v3\/items\/([^/]+)\/reorder$/);
    if (reorderMatch && method === 'POST') {
      const itemId = reorderMatch[1];
      const body = await request.json().catch(() => ({}));
      const item = await engine.reorderItem(familyCode, itemId, body);
      return json({ success: true, item });
    }

    // POST /api/v3/items/:itemId/archive
    const archiveMatch = path.match(/^\/api\/v3\/items\/([^/]+)\/archive$/);
    if (archiveMatch && method === 'POST') {
      const itemId = archiveMatch[1];
      const item = await engine.archiveItem(familyCode, itemId);
      return json({ success: true, item });
    }

    // 5. PREREQUISITES
    if (path === '/api/v3/prerequisites') {
      if (method === 'GET') {
        const itemId = url.searchParams.get('itemId');
        const prereqs = await storage.getPrerequisites(familyCode, itemId);
        return json({ success: true, prerequisites: prereqs });
      }
      if (method === 'POST') {
        const body = await request.json().catch(() => ({}));
        const prereq = await engine.addPrerequisite(familyCode, body);
        return json({ success: true, prerequisite: prereq }, 201);
      }
      if (method === 'DELETE') {
        const body = await request.json().catch(() => ({}));
        const itemId = body.itemId || url.searchParams.get('itemId');
        const requiredItemId = body.requiredItemId || url.searchParams.get('requiredItemId');
        const res = await engine.removePrerequisite(familyCode, itemId, requiredItemId);
        return json({ success: true, removed: res.removed });
      }
    }

    // 6. ATTEMPTS (Append-only & Idempotent)
    if (path === '/api/v3/attempts') {
      if (method === 'GET') {
        const studentId = url.searchParams.get('studentId') || null;
        const itemId = url.searchParams.get('itemId') || null;
        const limit = url.searchParams.get('limit') ? parseInt(url.searchParams.get('limit'), 10) : 50;
        const attempts = await engine.getAttempts(familyCode, { studentId, itemId, limit });
        return json({ success: true, attempts });
      }
      if (method === 'POST') {
        const body = await request.json().catch(() => ({}));
        if (Array.isArray(body.attempts)) {
          // Batch attempts
          const results = [];
          for (const att of body.attempts) {
            const res = await engine.recordAttempt(familyCode, att);
            results.push(res);
          }
          return json({ success: true, batchResults: results });
        } else {
          const res = await engine.recordAttempt(familyCode, body);
          return json({ success: true, ...res }, res.duplicate ? 200 : 201);
        }
      }
    }

    // 7. DETERMINISTIC PROGRESS & ANALYTICS
    if (path === '/api/v3/analytics/progress' || path === '/api/v3/progress') {
      if (method === 'GET') {
        const studentId = url.searchParams.get('studentId') || 'student_default';
        const courseId = url.searchParams.get('courseId') || null;
        const analytics = await engine.getProgressAnalytics(familyCode, studentId, courseId);
        return json({ success: true, data: analytics });
      }
    }

    // 8. BOUNDED AI CONTEXT EXPORT
    if (path === '/api/v3/export-context') {
      if (method === 'GET') {
        const studentId = url.searchParams.get('studentId') || 'student_default';
        const courseId = url.searchParams.get('courseId') || null;
        const context = await engine.exportContext(familyCode, studentId, courseId);
        return json({ success: true, context });
      }
    }

    return error(`V2 Endpoint not found: ${path}`, 404);

  } catch (err) {
    console.error(`[V2 Router Error] ${path}:`, err);
    return error(err.message || 'Internal V2 Error', 400);
  }
}
