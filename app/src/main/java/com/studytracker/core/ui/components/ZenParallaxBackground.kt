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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.studytracker.R
import com.studytracker.core.ui.theme.ZenSkyCyan
import com.studytracker.core.ui.theme.ZenMoonGold
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val isGold: Boolean,
    val speedFactor: Float
)

/**
 * 2.5D / 3D Multi-Layered Parallax Background for Zen Paper Cutout Night theme.
 * Composes 3 distinct depth planes moving in opposing sinusoidal drifts on GPU RenderNode:
 * 1. Sky & Celestial Moon Layer (Deep Background)
 * 2. Misty Silhouette Mountain Ridges (Midground)
 * 3. Pine Forest & Village Silhouettes (Foreground)
 * 4. Ambient Floating Stardust & Fireflies (Interactive Depth Particle Canvas)
 * 5. Translucent Reading Scrim (45% opacity for maximum artwork visibility & high contrast)
 */
@Composable
fun ZenParallaxBackground(
    modifier: Modifier = Modifier,
    scrimColor: Color = Color(0x73080D1A)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Zen3DParallax")

    // Layer 1: Sky & Moon drift (Slow, deep)
    val skyX by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skyX"
    )
    val skyY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skyY"
    )

    // Layer 2: Midground Mountain ridges drift (Opposite X-direction, moderate speed)
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
        initialValue = 9f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mountainY"
    )

    // Layer 3: Foreground Forest & Village drift (Fastest opposite movement)
    val fgX by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fgX"
    )
    val fgY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fgY"
    )

    // Layer 4: Twinkling Stardust Alpha
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    // Deterministic random floating star positions
    val starParticles = remember {
        val rand = Random(42)
        List(24) {
            StarParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat() * 0.75f, // Mostly in upper sky
                radius = rand.nextFloat() * 2.2f + 1.2f,
                isGold = rand.nextBoolean(),
                speedFactor = rand.nextFloat() * 0.8f + 0.5f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Plane 1: Far Sky & Moon
        Image(
            painter = painterResource(id = R.drawable.bg_layer_sky),
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

        // Plane 2: Midground Mountain Ridges
        Image(
            painter = painterResource(id = R.drawable.bg_layer_mountains),
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

        // Plane 3: Foreground Forest & Cozy Village
        Image(
            painter = painterResource(id = R.drawable.bg_layer_foreground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = fgX
                    translationY = fgY
                    scaleX = 1.12f
                    scaleY = 1.12f
                }
        )

        // Plane 4: Ambient Floating Stardust & Fireflies Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = skyX * 0.5f
                    translationY = skyY * 0.5f
                }
        ) {
            val width = size.width
            val height = size.height
            for (p in starParticles) {
                val cx = p.xRatio * width
                val cy = p.yRatio * height
                val col = if (p.isGold) ZenMoonGold else ZenSkyCyan
                val alpha = (starAlpha * p.speedFactor).coerceIn(0.1f, 1f)

                // Soft outer glow
                drawCircle(
                    color = col.copy(alpha = alpha * 0.35f),
                    radius = p.radius * 2.8f,
                    center = Offset(cx, cy)
                )
                // Core bright pinpoint
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = p.radius,
                    center = Offset(cx, cy)
                )
            }
        }

        // Plane 5: 20% More Translucent Scrim for 100% Readability and Vibrant Art
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
        )
    }
}
