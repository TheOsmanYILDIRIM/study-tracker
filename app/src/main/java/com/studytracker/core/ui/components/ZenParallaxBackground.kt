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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.studytracker.R
import com.studytracker.core.ui.theme.ZenMoonGold
import com.studytracker.core.ui.theme.ZenSkyCyan
import kotlin.math.sin
import kotlin.random.Random

/**
 * Gökyüzündeki kağıt kesim takımyıldızı koordinatları (xRatio, yRatio, 0.0f - 1.0f).
 */
data class ZenConstellationStar(
    val id: Int,
    val xRatio: Float,
    val yRatio: Float,
    val baseRadius: Float = 3.0f
)

val ZenConstellationStars = listOf(
    ZenConstellationStar(0, 0.20f, 0.12f, 3.5f),
    ZenConstellationStar(1, 0.35f, 0.08f, 3.2f),
    ZenConstellationStar(2, 0.50f, 0.06f, 4.0f),
    ZenConstellationStar(3, 0.68f, 0.09f, 3.4f),
    ZenConstellationStar(4, 0.82f, 0.13f, 3.8f),
    ZenConstellationStar(5, 0.15f, 0.22f, 3.0f),
    ZenConstellationStar(6, 0.30f, 0.18f, 3.6f),
    ZenConstellationStar(7, 0.60f, 0.16f, 3.2f),
    ZenConstellationStar(8, 0.75f, 0.20f, 3.5f),
    ZenConstellationStar(9, 0.88f, 0.25f, 3.0f),
    ZenConstellationStar(10, 0.25f, 0.28f, 3.2f),
    ZenConstellationStar(11, 0.42f, 0.24f, 3.5f),
    ZenConstellationStar(12, 0.58f, 0.26f, 3.0f),
    ZenConstellationStar(13, 0.70f, 0.30f, 3.3f),
    ZenConstellationStar(14, 0.18f, 0.36f, 2.8f),
    ZenConstellationStar(15, 0.82f, 0.35f, 3.1f),
    ZenConstellationStar(16, 0.48f, 0.14f, 4.2f),
    ZenConstellationStar(17, 0.64f, 0.22f, 3.4f),
    ZenConstellationStar(18, 0.38f, 0.32f, 3.0f),
    ZenConstellationStar(19, 0.55f, 0.34f, 3.2f)
)

private data class AmbientFirefly(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val isGold: Boolean,
    val pulseSpeed: Float
)

/**
 * Tek Parça Masal Arka Planı + Canlı Renk & Parlaklık Fullenmesi + Görev Uçan Yıldız Sistemi:
 * - Tek bir yüksek kaliteli masal gecesi görseli (bg_zen_night.webp).
 * - Görevler tamamlandıkça (progressRatio 0f -> 1f):
 *   - Arka plan görselinin alpha ve renk doygunluğu artar (ColorMatrix saturation & brightness).
 *   - Üstteki koruyucu okuma scrim katmanı açılarak muazzam canlılık ve parıltı sunar.
 *   - Gökyüzündeki altın takımyıldızları ve stardust parçacıkları kademeli olarak ışıldar.
 * - Görev bitirildiğinde görev kartından gökyüzüne altın-cyan kuyruklu yıldız uçar ve patlama halkasıyla hedef yıldızı aydınlatır.
 */
@Composable
fun ZenParallaxBackground(
    modifier: Modifier = Modifier,
    completedTasksCount: Int = 0,
    totalTasksCount: Int = 1,
    progressOverride: Float? = null,
    flyingStarTrigger: Long = 0L,
    scrimColor: Color = Color(0x66081420)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenSingleImageParallax")

    // Hafif ve büyüleyici süzülme / nefes alma hareketi (120 FPS GPU graphicsLayer)
    val floatX by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgFloatX"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgFloatY"
    )

    // Gökyüzü nefes alma / parıldama ritmi
    val shimmerPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerPulse"
    )

    // Görev tamamlama ilerleme oranı (0.0f = Loş/Sakin Gece, 1.0f = Işıl Işıl Dolu/Canlı Gece)
    val effectiveProgress = remember(completedTasksCount, totalTasksCount, progressOverride) {
        if (progressOverride != null) {
            progressOverride.coerceIn(0f, 1f)
        } else if (totalTasksCount <= 0) {
            0.5f
        } else {
            (completedTasksCount.toFloat() / totalTasksCount.coerceAtLeast(1)).coerceIn(0f, 1f)
        }
    }

    // İlerleme durumuna göre dinamik görsel parametreleri
    val bgAlpha = 0.55f + 0.45f * effectiveProgress // 0.55f (loş) -> 1.0f (tam parlak)
    val scrimAlpha = (0.55f - 0.35f * effectiveProgress).coerceIn(0.15f, 0.65f) // 0.55f -> 0.20f (okuma koruması açılır)
    val colorSaturation = 0.85f + 0.45f * effectiveProgress // 0.85f -> 1.30f (renkler canlanır)

    val colorMatrix = remember(colorSaturation) {
        ColorMatrix().apply {
            setToSaturation(colorSaturation)
        }
    }

    // --- UÇAN YILDIZ (FLYING COMET & BURST) ANİMASYON DURUMU ---
    val flyingStarAnim = remember { Animatable(0f) }
    var burstTargetIndex by remember { mutableStateOf(0) }
    var burstAnimTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(flyingStarTrigger) {
        if (flyingStarTrigger > 0L) {
            val starCount = ZenConstellationStars.size
            burstTargetIndex = ((effectiveProgress * starCount).toInt()).coerceIn(0, starCount - 1)
            flyingStarAnim.snapTo(0f)
            burstAnimTrigger = false
            flyingStarAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
            burstAnimTrigger = true
        }
    }

    // Patlama halkası animasyonu (Burst Ring)
    val burstRingProgress by animateFloatAsState(
        targetValue = if (burstAnimTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "burstRingProgress"
    )

    // Ambient Yüzen Ateşböcekleri
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
        // --- 1. TEK PARÇA MASAL GECESİ ARKA PLANI ---
        Image(
            painter = painterResource(id = R.drawable.bg_zen_night),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(colorMatrix),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = floatX
                    translationY = floatY
                    scaleX = 1.08f
                    scaleY = 1.08f
                    alpha = bgAlpha
                }
        )

        // --- 2. DİNAMİK GÖKYÜZÜ YILDIZLARI, UÇAN KUYRUKLU YILDIZ & PATLAMA TUVALİ ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = floatX * 0.8f
                    translationY = floatY * 0.8f
                }
        ) {
            val width = size.width
            val height = size.height

            // A. İlerlemeye Göre Açılan / Parlayan Takımyıldızı Noktaları
            val totalStars = ZenConstellationStars.size
            val activeStarsCount = (effectiveProgress * totalStars).toInt().coerceIn(0, totalStars)

            for (i in 0 until totalStars) {
                val star = ZenConstellationStars[i]
                val starX = star.xRatio * width
                val starY = star.yRatio * height
                val isActive = i < activeStarsCount

                val starAlpha = if (isActive) {
                    (0.70f + 0.30f * shimmerPulse)
                } else {
                    (0.15f + 0.10f * shimmerPulse) * effectiveProgress.coerceAtLeast(0.2f)
                }

                val auraRadius = if (isActive) star.baseRadius * (3.5f + 1.2f * effectiveProgress) else star.baseRadius * 1.8f
                val coreRadius = if (isActive) star.baseRadius * (1.2f + 0.4f * effectiveProgress) else star.baseRadius * 0.8f

                // Altın Işıltı Aurası
                drawCircle(
                    color = ZenMoonGold.copy(alpha = starAlpha * 0.55f),
                    radius = auraRadius,
                    center = Offset(starX, starY)
                )
                // Parlak Çekirdek
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    radius = coreRadius,
                    center = Offset(starX, starY)
                )
            }

            // B. Uçan Kuyruklu Yıldız (Karttan Gökyüzüne Fırlayan Parçacık)
            val flyP = flyingStarAnim.value
            if (flyP > 0f && flyP < 1f) {
                val targetStar = ZenConstellationStars[burstTargetIndex]
                val startX = width * 0.5f
                val startY = height * 0.80f // Kart bölgesinden çıkış
                val endX = targetStar.xRatio * width
                val endY = targetStar.yRatio * height

                // Parabolik süzülüş
                val currentX = startX + (endX - startX) * flyP
                val arcHeight = height * 0.18f * sin(flyP * Math.PI.toFloat())
                val currentY = startY + (endY - startY) * flyP - arcHeight

                // Işıltılı Kuyruk Parçacıkları (Tail Sparks)
                for (tail in 1..5) {
                    val tailP = (flyP - tail * 0.035f).coerceAtLeast(0f)
                    val tailX = startX + (endX - startX) * tailP
                    val tailArc = height * 0.18f * sin(tailP * Math.PI.toFloat())
                    val tailY = startY + (endY - startY) * tailP - tailArc
                    drawCircle(
                        color = ZenSkyCyan.copy(alpha = (1f - tail * 0.18f) * (1f - flyP)),
                        radius = (6f - tail).coerceAtLeast(1.5f),
                        center = Offset(tailX, tailY)
                    )
                }

                // Yıldız Başı / Çekirdek (Golden Head)
                drawCircle(
                    color = ZenMoonGold.copy(alpha = 0.85f),
                    radius = 11f,
                    center = Offset(currentX, currentY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = Offset(currentX, currentY)
                )
            }

            // C. Hedefe Ulaşma Patlaması (Target Star Ignition Burst Ring)
            if (burstRingProgress > 0f && burstRingProgress < 1f) {
                val targetStar = ZenConstellationStars[burstTargetIndex]
                val targetX = targetStar.xRatio * width
                val targetY = targetStar.yRatio * height

                val ringRadius = 12f + burstRingProgress * 55f
                val ringAlpha = (1f - burstRingProgress) * 0.90f

                // Dış süpernova halkası
                drawCircle(
                    color = ZenMoonGold.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = Offset(targetX, targetY)
                )
                // İç süper ışıltı çekirdeği
                drawCircle(
                    color = Color.White.copy(alpha = (1f - burstRingProgress)),
                    radius = 10f * (1f - burstRingProgress),
                    center = Offset(targetX, targetY)
                )
            }

            // D. Ambient Ateşböcekleri (İlerleme arttıkça daha canlı ve parlak)
            for (f in fireflies) {
                val cx = f.xRatio * width
                val cy = f.yRatio * height
                val col = if (f.isGold) ZenMoonGold else ZenSkyCyan
                val alpha = (shimmerPulse * f.pulseSpeed * (0.40f + 0.60f * effectiveProgress)).coerceIn(0.15f, 1.0f)

                drawCircle(
                    color = col.copy(alpha = alpha * 0.40f),
                    radius = f.radius * (2.2f + 1.2f * effectiveProgress),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = f.radius,
                    center = Offset(cx, cy)
                )
            }
        }

        // --- 3. DİNAMİK OKUMA KORUMA SCIRIM KATMANI (İlerleme Arttıkça Hafifler) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF081420).copy(alpha = scrimAlpha))
        )
    }
}


