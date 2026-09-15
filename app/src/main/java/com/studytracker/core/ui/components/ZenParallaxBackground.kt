package com.studytracker.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.studytracker.R
import com.studytracker.core.ui.theme.ZenMoonGold
import com.studytracker.core.ui.theme.ZenSkyCyan
import kotlin.math.sin
import kotlin.random.Random

/**
 * Gökyüzündeki gerçek kağıt kesim yıldız koordinatları (xRatio, yRatio, 0.0f - 1.0f).
 * Orijinal görselden pikseller taranarak çıkarılmıştır.
 */
data class ZenConstellationStar(
    val id: Int,
    val xRatio: Float,
    val yRatio: Float,
    val baseRadius: Float = 2.5f
)

val ZenConstellationStars = listOf(
    ZenConstellationStar(0, 0.1185f, 0.4513f, 3.2f),
    ZenConstellationStar(1, 0.6523f, 0.1490f, 3.0f),
    ZenConstellationStar(2, 0.5755f, 0.0916f, 3.5f),
    ZenConstellationStar(3, 0.5924f, 0.1424f, 2.8f),
    ZenConstellationStar(4, 0.4128f, 0.1017f, 3.0f),
    ZenConstellationStar(5, 0.8854f, 0.2951f, 3.4f),
    ZenConstellationStar(6, 0.4727f, 0.1185f, 2.6f),
    ZenConstellationStar(7, 0.6302f, 0.1025f, 3.1f),
    ZenConstellationStar(8, 0.2878f, 0.1265f, 3.3f),
    ZenConstellationStar(9, 0.5365f, 0.1344f, 2.9f),
    ZenConstellationStar(10, 0.1602f, 0.1751f, 3.0f),
    ZenConstellationStar(11, 0.2005f, 0.1562f, 2.7f),
    ZenConstellationStar(12, 0.3424f, 0.1126f, 3.2f),
    ZenConstellationStar(13, 0.6784f, 0.2209f, 2.8f),
    ZenConstellationStar(14, 0.5872f, 0.1730f, 2.9f),
    ZenConstellationStar(15, 0.5898f, 0.2951f, 3.1f),
    ZenConstellationStar(16, 0.1497f, 0.4898f, 2.5f),
    ZenConstellationStar(17, 0.6888f, 0.1250f, 3.0f),
    ZenConstellationStar(18, 0.7487f, 0.1359f, 3.4f),
    ZenConstellationStar(19, 0.4818f, 0.0872f, 3.0f)
)

private data class AmbientFirefly(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val isGold: Boolean,
    val pulseSpeed: Float
)

/**
 * Katman A (Yıldızsız Temiz Gökyüzü) + Katman B (İzole Yıldızlar) ve Görev Tamamlama Uçan Yıldız Sistemi:
 * - Katman A: bg_zen_layer1_sky.webp (Pürüzsüz yıldızsız gökyüzü, ay & bulutlar)
 * - Katman B: bg_zen_stars_isolated.webp (Orijinal görsel ile piksel piksel örtüşen izole şeffaf yıldız katmanı)
 * - Tamamlanan görev sayısına göre dinamik parıldayan gökyüzü yıldızları.
 * - Görev bitirildiğinde ekrandan gökyüzüne uçan kuyruklu yıldız & patlama ışıltısı efekti.
 */
@Composable
fun ZenParallaxBackground(
    modifier: Modifier = Modifier,
    completedTasksCount: Int = 0,
    totalTasksCount: Int = 1,
    flyingStarTrigger: Long = 0L,
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

    // Plane 4: Foreground Lake Shore (OPPOSITE: Right -> Left)
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

    // Gökyüzü genel yıldız parıltısı (Nefes alma / Shimmer efekti)
    val starTwinklePulse by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinklePulse"
    )

    // Öğrenci görev ilerleme oranına göre gökyüzü temel parlaklığı (%35 loş taban + %65 tamamlanan görevler)
    val progressRatio = remember(completedTasksCount, totalTasksCount) {
        if (totalTasksCount <= 0) 0.5f else (completedTasksCount.toFloat() / totalTasksCount.coerceAtLeast(1)).coerceIn(0f, 1f)
    }
    val effectiveStarsAlpha = remember(progressRatio, starTwinklePulse) {
        (0.35f + 0.65f * progressRatio) * starTwinklePulse
    }

    // --- UÇAN YILDIZ (FLYING COMET & BURST) ANİMASYON DURUMU ---
    val flyingStarAnim = remember { Animatable(0f) }
    var burstTargetIndex by remember { mutableStateOf(0) }
    var burstAnimTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(flyingStarTrigger) {
        if (flyingStarTrigger > 0L) {
            burstTargetIndex = (completedTasksCount - 1).coerceIn(0, ZenConstellationStars.size - 1)
            flyingStarAnim.snapTo(0f)
            burstAnimTrigger = false
            flyingStarAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
            )
            burstAnimTrigger = true
        }
    }

    // Patlama halkası animasyonu (Burst Ring)
    val burstRingProgress by animateFloatAsState(
        targetValue = if (burstAnimTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = LinearOutSlowInEasing),
        label = "burstRingProgress"
    )

    // Ambient Floating Fireflies
    val fireflies = remember {
        val rand = Random(1337)
        List(24) {
            AmbientFirefly(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat() * 0.85f + 0.05f,
                radius = rand.nextFloat() * 2.0f + 1.2f,
                isGold = rand.nextBoolean(),
                pulseSpeed = rand.nextFloat() * 0.7f + 0.6f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // --- KATMAN A: Yıldızsız Temiz Gökyüzü (Sky & Moon) ---
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

        // --- KATMAN B: İzole Yıldızlar & Takımyıldızlar (Piksel Piksel Tam Örtüşen Alpha Katmanı) ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_stars_isolated),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = skyX
                    translationY = skyY
                    scaleX = 1.08f
                    scaleY = 1.08f
                    alpha = effectiveStarsAlpha.coerceIn(0f, 1f)
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

        // --- PLANE 4: Foreground Lake Shore & Reeds ---
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

        // --- PLANE 5: Dinamik Görev Yıldızları, Uçan Kuyruklu Yıldız & Patlama Tuvali ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = skyX
                    translationY = skyY
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Tamamlanan Görevlere Ait Yıldızları Ekstra Parlat ve Hale Çiz (Active Constellation Nodes)
            val activeStarsCount = completedTasksCount.coerceIn(0, ZenConstellationStars.size)
            for (i in 0 until activeStarsCount) {
                val star = ZenConstellationStars[i]
                val starX = star.xRatio * width
                val starY = star.yRatio * height

                // Işıltı aurası (Golden celestial glow)
                drawCircle(
                    color = ZenMoonGold.copy(alpha = 0.55f * starTwinklePulse),
                    radius = star.baseRadius * 4.5f,
                    center = Offset(starX, starY)
                )
                // Parlak çekirdek
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = star.baseRadius * 1.5f,
                    center = Offset(starX, starY)
                )
            }

            // 2. Uçan Kuyruklu Yıldız (Flying Particle from Task to Sky Star)
            val flyP = flyingStarAnim.value
            if (flyP > 0f && flyP < 1f) {
                val targetStar = ZenConstellationStars[burstTargetIndex]
                val startX = width * 0.5f
                val startY = height * 0.75f // Kart hizasından kalkış
                val endX = targetStar.xRatio * width
                val endY = targetStar.yRatio * height

                // Yay şeklinde parabolik uçuş rotası
                val currentX = startX + (endX - startX) * flyP
                val arcHeight = height * 0.15f * sin(flyP * Math.PI.toFloat())
                val currentY = startY + (endY - startY) * flyP - arcHeight

                // Işıltılı Kuyruk Parçacıkları (Tail Sparks)
                for (tail in 1..4) {
                    val tailP = (flyP - tail * 0.04f).coerceAtLeast(0f)
                    val tailX = startX + (endX - startX) * tailP
                    val tailArc = height * 0.15f * sin(tailP * Math.PI.toFloat())
                    val tailY = startY + (endY - startY) * tailP - tailArc
                    drawCircle(
                        color = ZenSkyCyan.copy(alpha = (1f - tail * 0.22f) * (1f - flyP)),
                        radius = (5f - tail).coerceAtLeast(1.5f),
                        center = Offset(tailX, tailY)
                    )
                }

                // Yıldız Başı / Çekirdek (Golden Head)
                drawCircle(
                    color = ZenMoonGold.copy(alpha = 0.8f),
                    radius = 9f,
                    center = Offset(currentX, currentY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = Offset(currentX, currentY)
                )
            }

            // 3. Hedefe Ulaşma Patlaması (Target Star Ignition Burst Ring)
            if (burstRingProgress > 0f && burstRingProgress < 1f) {
                val targetStar = ZenConstellationStars[burstTargetIndex]
                val targetX = targetStar.xRatio * width
                val targetY = targetStar.yRatio * height

                val ringRadius = 10f + burstRingProgress * 45f
                val ringAlpha = (1f - burstRingProgress) * 0.85f

                // Dış halka dalgası
                drawCircle(
                    color = ZenMoonGold.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = Offset(targetX, targetY)
                )
                // İç süper parlama
                drawCircle(
                    color = Color.White.copy(alpha = (1f - burstRingProgress)),
                    radius = 8f * (1f - burstRingProgress),
                    center = Offset(targetX, targetY)
                )
            }

            // 4. Ambient Yüzen Ateşböcekleri (Ambient Fireflies)
            for (f in fireflies) {
                val cx = f.xRatio * width
                val cy = f.yRatio * height
                val col = if (f.isGold) ZenMoonGold else ZenSkyCyan
                val alpha = (starTwinklePulse * f.pulseSpeed).coerceIn(0.15f, 0.9f)

                drawCircle(
                    color = col.copy(alpha = alpha * 0.35f),
                    radius = f.radius * 2.8f,
                    center = Offset(cx, cy)
                )
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

