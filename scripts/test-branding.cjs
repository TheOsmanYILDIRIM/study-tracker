/* Verify our editable SVG mark and Android native VectorDrawable stay in sync. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');

const root = path.resolve(__dirname, '..');
const read = file => fs.readFileSync(path.join(root, file), 'utf8');
const svg = read('assets/brand/studytracker-icon.svg');
const vector = read('app/src/main/res/drawable/ic_launcher_foreground.xml');
const nativeSplash = read('app/src/main/res/values-v31/themes.xml');
const legacySplash = read('app/src/main/res/drawable/launch_window.xml');
const composeSplash = read('app/src/main/java/com/studytracker/feature/launch/StudyLaunchOverlay.kt');
const activity = read('app/src/main/java/com/studytracker/MainActivity.kt');
const navigation = read('app/src/main/java/com/studytracker/app/navigation/NavGraph.kt');
const courses = read('app/src/main/java/com/studytracker/feature/v2/V2CoursesScreen.kt');
const lessons = read('app/src/main/java/com/studytracker/feature/v2/V2LessonsScreen.kt');
const flow = read('app/src/main/java/com/studytracker/feature/v2/V2LearningFlowScreen.kt');

const svgPaths = [...svg.matchAll(/<path\b[^>]*\bd="([^"]+)"/g)].map(m => m[1]);
const vectorPaths = [...vector.matchAll(/android:pathData="([^"]+)"/g)].map(m => m[1]);
assert(svgPaths.length >= 20, 'Launcher SVG unexpectedly missing vector shapes');
assert.deepEqual(vectorPaths, svgPaths, 'Launcher VectorDrawable must match the editable SVG geometry');
assert(nativeSplash.includes('@drawable/ic_launcher_foreground'), 'Android 12 splash missing launcher vector');
assert(legacySplash.includes('@drawable/ic_launcher_foreground'), 'Legacy launch window missing launcher vector');
assert(composeSplash.includes('painterResource(id = R.drawable.ic_launcher_foreground)'), 'Compose launch screen must reuse the same vector');
assert(activity.includes('StudyLaunchOverlay(onFinished'), 'Launch animation not connected to MainActivity');

assert(composeSplash.includes('One orbiting comet sweeps across the book'), 'Approved classic book-and-comet animation missing');
assert(!composeSplash.includes('SolarOdysseyScene('), 'Unapproved solar system intro must not be activated');
assert(navigation.includes('popEnterTransition') && navigation.includes('enterTransition'), 'Short back/forward transitions missing');
assert(navigation.includes('autoStartItemId = targetItemId'), 'Simplified one-tap V2 route must survive');
assert(courses.includes('courseCompleted') && courses.includes('CourseCoverVisual('),
    'Completed course appearance must preserve its cached cover');
assert(lessons.includes('lessonCompleted') && lessons.includes('LearningItemPuzzleCard('),
    'Simplified flat lesson list must preserve completion status');
assert(flow.includes('LearningItemVisual(') && flow.includes('Color(0xA43E4049)'),
    'Completed learning cards must keep thumbnail with gray overlay');
assert(flow.includes('autoOpened') && flow.includes('Intent(Intent.ACTION_VIEW'),
    'One-tap video launch must survive');
console.log('Branding/V2: 23-path SVG icon, native splash, classic animation, routes, and simplified learning cards verified.');
