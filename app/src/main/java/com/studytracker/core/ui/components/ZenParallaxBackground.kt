package com.studytracker.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.studytracker.R
import com.studytracker.core.ui.theme.ZenMoonGold
import com.studytracker.core.ui.theme.ZenSkyCyan
import kotlin.random.Random

private data class AmbientFirefly(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val isGold: Boolean,
    val pulseSpeed: Float
)

/**
 * 4-Layer 3D Multi-Plane Parallax Background with Dynamic Twinkling & Glowing Lights:
 * 1. Sky & Moon Plane + Animated Pulsing Celestial Lights (bg_zen_lights_sky.webp)
 * 2. Mountains & Clouds Plane (Opposing Movement)
 * 3. Pine Forest Plane + Animated Flickering Tree Fireflies (bg_zen_lights_forest.webp)
 * 4. Foreground Lake Shore + Animated Shimmering Water Reflection (bg_zen_lights_lake.webp)
 * 5. Interactive Ambient Firefly & Stardust Particle Canvas
 * 6. High-contrast translucent reading scrim
 */
@Composable
fun ZenParallaxBackground(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color(0x66081420)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenMultiPlane3DParallax")

    // --- PARALLAX MOVEMENTS ---
    // Plane 1: Sky & Moon (Left -> Right)
    val skyX by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skyX"
    )
    val skyY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skyY"
    )

    // Plane 2: Mountains (OPPOSITE: Right -> Left)
    val mountainX by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 19000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mountainX"
    )
    val mountainY by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mountainY"
    )

    // Plane 3: Pine Forest (OPPOSITE: Left -> Right)
    val forestX by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "forestX"
    )
    val forestY by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "forestY"
    )

    // Plane 4: Foreground Lake Shore & Reeds (OPPOSITE: Right -> Left)
    val fgX by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fgX"
    )
    val fgY by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fgY"
    )

    // --- DYNAMIC LIGHT TWINKLING & GLOW ANIMATIONS ---
    // 1. Sky Stars & Moon Twinkle
    val skyLightsAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skyLightsAlpha"
    )

    // 2. Tree Fireflies & Forest Fairy Lights Flicker
    val forestLightsAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "forestLightsAlpha"
    )

    // 3. Lake Moon Reflection & Water Sparkles
    val lakeLightsAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2900, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lakeLightsAlpha"
    )

    // 4. Floating Fireflies Sparkle
    val fireflyPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fireflyPulse"
    )

    // Ambient Floating Fireflies
    val fireflies = remember {
        val rand = Random(1337)
        List(28) {
            AmbientFirefly(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat() * 0.85f + 0.05f,
                radius = rand.nextFloat() * 2.2f + 1.2f,
                isGold = rand.nextBoolean(),
                pulseSpeed = rand.nextFloat() * 0.7f + 0.6f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // --- PLANE 1: Sky & Moon ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_layer1_sky),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = skyX
                    translationY = skyY
                    scaleX = 1.08f
                    scaleY = 1.08f
                }
        )

        // --- PLANE 1 LIGHTS: Stars, Constellations & Moon Glow (Animated Twinkle) ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_lights_sky),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = skyX
                    translationY = skyY
                    scaleX = 1.08f
                    scaleY = 1.08f
                    alpha = skyLightsAlpha
                }
        )

        // --- PLANE 2: Mountains & Clouds (Opposite Drift) ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_layer2_mountains),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = mountainX
                    translationY = mountainY
                    scaleX = 1.10f
                    scaleY = 1.10f
                }
        )

        // --- PLANE 3: Pine Forest & Middle Lake ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_layer3_forest),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = forestX
                    translationY = forestY
                    scaleX = 1.12f
                    scaleY = 1.12f
                }
        )

        // --- PLANE 3 LIGHTS: Glowing Tree Fireflies & Forest Lanterns (Animated Flicker) ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_lights_forest),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = forestX
                    translationY = forestY
                    scaleX = 1.12f
                    scaleY = 1.12f
                    alpha = forestLightsAlpha
                }
        )

        // --- PLANE 4: Foreground Lake Shore, Reeds & Side Trees ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_layer4_foreground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = fgX
                    translationY = fgY
                    scaleX = 1.14f
                    scaleY = 1.14f
                }
        )

        // --- PLANE 4 LIGHTS: Water Moon Reflection & Sparkles (Animated Shimmer) ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_lights_lake),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = fgX
                    translationY = fgY
                    scaleX = 1.14f
                    scaleY = 1.14f
                    alpha = lakeLightsAlpha
                }
        )

        // --- PLANE 5: Ambient Drifting Fireflies & Stardust Particle Canvas ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = forestX * 0.6f
                    translationY = forestY * 0.6f
                }
        ) {
            val width = size.width
            val height = size.height
            for (f in fireflies) {
                val cx = f.xRatio * width
                val cy = f.yRatio * height
                val col = if (f.isGold) ZenMoonGold else ZenSkyCyan
                val alpha = (fireflyPulse * f.pulseSpeed).coerceIn(0.15f, 1f)

                // Soft firefly aura
                drawCircle(
                    color = col.copy(alpha = alpha * 0.45f),
                    radius = f.radius * 3.2f,
                    center = Offset(cx, cy)
                )
                // Bright inner core
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = f.radius,
                    center = Offset(cx, cy)
                )
            }
        }

        // --- PLANE 6: 40% Scrim for High Readability & Vibrant Art ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
        )
    }
}
