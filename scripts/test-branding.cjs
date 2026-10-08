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
const solarSvg = read('assets/brand/studytracker-solar-odyssey.svg');
const solarCompose = read('app/src/main/java/com/studytracker/feature/launch/SolarOdysseyScene.kt');

const svgPaths = [...svg.matchAll(/<path\b[^>]*\bd="([^"]+)"/g)].map(m => m[1]);
const vectorPaths = [...vector.matchAll(/android:pathData="([^"]+)"/g)].map(m => m[1]);
assert(svgPaths.length >= 20, 'Launcher SVG unexpectedly missing vector shapes');
assert.deepEqual(vectorPaths, svgPaths, 'Launcher VectorDrawable must match the editable SVG geometry');
assert(nativeSplash.includes('@drawable/ic_launcher_foreground'), 'Android 12 splash missing launcher vector');
assert(legacySplash.includes('@drawable/ic_launcher_foreground'), 'Legacy launch window missing launcher vector');
assert(composeSplash.includes('painterResource(id = R.drawable.ic_launcher_foreground)'), 'Compose launch screen must reuse the same vector');
assert(activity.includes('StudyLaunchOverlay(onFinished'), 'Launch animation not connected to MainActivity');
// Never mistake raster keyframes for real animated SVG.
assert(solarSvg.startsWith('<svg') && solarSvg.includes('</svg>'), 'Solar scene must be an actual SVG document');
for (const token of ['@keyframes reveal', '@keyframes spin', '@keyframes sunPulse',
                     'id="orbits"', 'id="sun-rise"', 'class="cometFlight"',
                     'id="brand-mark"', 'id="saturn"', 'id="jupiter"']) {
    // Planets in the SVG are identified by gradients, not separate raster assets.
    if (token.startsWith('id="saturn"') || token.startsWith('id="jupiter"')) {
        const grad = token.includes('saturn') ? 'id="saturn"' : 'id="jupiter"';
        assert(solarSvg.includes(grad), `Missing planet gradient: ${grad}`);
    } else {
        assert(solarSvg.includes(token), `Missing animated vector feature: ${token}`);
    }
}
assert(!/<image\\b/i.test(solarSvg), 'Solar SVG must not embed a bitmap');
assert(composeSplash.includes('SolarOdysseyScene('), 'Compose launch must use native solar-system scene');
assert(solarCompose.includes('drawPlanet(') && solarCompose.includes('drawArc('), 'Android solar system must draw planets and comet natively');
assert(solarCompose.includes('pointOnOrbit('), 'Native planets must move along mathematical orbits');
console.log('Branding: SVG/native mark geometry and solar-system animated launch wiring verified.');
