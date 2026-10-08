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

const svgPaths = [...svg.matchAll(/<path\b[^>]*\bd="([^"]+)"/g)].map(m => m[1]);
const vectorPaths = [...vector.matchAll(/android:pathData="([^"]+)"/g)].map(m => m[1]);
assert(svgPaths.length >= 20, 'Launcher SVG unexpectedly missing vector shapes');
assert.deepEqual(vectorPaths, svgPaths, 'Launcher VectorDrawable must match the editable SVG geometry');
assert(nativeSplash.includes('@drawable/ic_launcher_foreground'), 'Android 12 splash missing launcher vector');
assert(legacySplash.includes('@drawable/ic_launcher_foreground'), 'Legacy launch window missing launcher vector');
assert(composeSplash.includes('painterResource(id = R.drawable.ic_launcher_foreground)'), 'Compose launch screen must reuse the same vector');
assert(activity.includes('StudyLaunchOverlay(onFinished'), 'Launch animation not connected to MainActivity');
console.log('Branding: SVG and Android path geometry match; both native and Compose splash references are wired.');
