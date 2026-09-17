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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.studytracker.R
import com.studytracker.core.ui.theme.ZenMoonGold
import com.studytracker.core.ui.theme.ZenSkyCyan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Gökyüzündeki takımyıldızı koordinatları (xRatio, yRatio, 0.0f - 1.0f).
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

private data class ForegroundDustParticle(
    val startXRatio: Float,
    val startYRatio: Float,
    val radius: Float,
    val isGold: Boolean,
    val driftSpeed: Float,
    val phaseOffset: Float,
    val baseAlpha: Float,
    val hasSparkle: Boolean
)

/**
 * Tek Parça Masal Arka Planı:
 * - Görevler tamamlandıkça (progress = completed / total):
 *   - Arka plan görselinin alpha ve renk doygunluğu artar (ColorMatrix saturation & brightness).
 *   - Üstteki koruyucu okuma scrim katmanı açılarak arkaplan aydınlanır ve parıldar.
 *   - Gökyüzündeki altın takımyıldızları ve ateşböceği parçacıkları kademeli olarak canlanır.
 */
@Composable
fun ZenParallaxBackground(
    modifier: Modifier = Modifier,
    completedTasksCount: Int = 0,
    totalTasksCount: Int = 1,
    progressOverride: Float? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenSingleImageParallax")

    // Hafif süzülme / nefes alma hareketi (120 FPS GPU graphicsLayer)
    val floatX by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgFloatX"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgFloatY"
    )

    // Gökyüzü nefes alma ritmi
    val shimmerPulse by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerPulse"
    )

    // Görev tamamlama ilerleme oranı (0.0f = Loş Gece, 1.0f = Işıl Işıl Aydınlık Gece)
    val effectiveProgress = remember(completedTasksCount, totalTasksCount, progressOverride) {
        if (progressOverride != null) {
            progressOverride.coerceIn(0f, 1f)
        } else if (totalTasksCount <= 0) {
            0.5f
        } else {
            (completedTasksCount.toFloat() / totalTasksCount.coerceAtLeast(1)).coerceIn(0f, 1f)
        }
    }

    // Görevler tamamlandıkça arka plan belirginleşir ve aydınlanır
    val bgAlpha = 0.50f + 0.50f * effectiveProgress // 0.50f -> 1.0f (tam aydınlık)
    val scrimAlpha = (0.48f - 0.38f * effectiveProgress).coerceIn(0.08f, 0.55f) // 0.48f -> 0.10f
    val colorSaturation = 0.85f + 0.55f * effectiveProgress // 0.85f -> 1.40f

    val colorMatrix = remember(colorSaturation) {
        ColorMatrix().apply {
            setToSaturation(colorSaturation)
        }
    }

    // Ambient Yüzen Ateşböcekleri
    val fireflies = remember {
        val rand = Random(1337)
        List(26) {
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
                    scaleX = 1.06f
                    scaleY = 1.06f
                    alpha = bgAlpha
                }
        )

        // --- 2. DİNAMİK GÖKYÜZÜ YILDIZLARI TUVALİ ---
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

            // İlerlemeye Göre Açılan / Parlayan Takımyıldızı Noktaları
            val totalStars = ZenConstellationStars.size
            val activeStarsCount = (effectiveProgress * totalStars).toInt().coerceIn(0, totalStars)

            for (i in 0 until totalStars) {
                val star = ZenConstellationStars[i]
                val starX = star.xRatio * width
                val starY = star.yRatio * height
                val isActive = i < activeStarsCount

                val starAlpha = if (isActive) {
                    (0.75f + 0.25f * shimmerPulse)
                } else {
                    (0.20f + 0.15f * shimmerPulse) * effectiveProgress.coerceAtLeast(0.25f)
                }

                val auraRadius = if (isActive) star.baseRadius * (4.0f + 1.5f * effectiveProgress) else star.baseRadius * 2.0f
                val coreRadius = if (isActive) star.baseRadius * (1.4f + 0.5f * effectiveProgress) else star.baseRadius * 0.9f

                // Altın Işıltı Aurası
                drawCircle(
                    color = ZenMoonGold.copy(alpha = starAlpha * 0.60f),
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

            // Ambient Ateşböcekleri (İlerleme arttıkça daha canlı)
            for (f in fireflies) {
                val cx = f.xRatio * width
                val cy = f.yRatio * height
                val col = if (f.isGold) ZenMoonGold else ZenSkyCyan
                val alpha = (shimmerPulse * f.pulseSpeed * (0.45f + 0.55f * effectiveProgress)).coerceIn(0.20f, 1.0f)

                drawCircle(
                    color = col.copy(alpha = alpha * 0.45f),
                    radius = f.radius * (2.4f + 1.2f * effectiveProgress),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.9f),
                    radius = f.radius,
                    center = Offset(cx, cy)
                )
            }
        }

        // --- 3. DİNAMİK OKUMA KORUMA SCRIM KATMANI (İlerleme Arttıkça İnceleşir ve Aydınlanır) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF081420).copy(alpha = scrimAlpha))
        )
    }
}

/**
 * Metinlerin ve kartların EN ÖNÜNDE (foreground) süzülen, son derece IŞILTILI (radiant & luminous),
 * parlak kuyruklu kayan yıldızlar ve parıldayan 4 köşeli yıldız kristalleri katmanı.
 */
@Composable
fun ZenForegroundStarOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenForegroundShootingStars")

    // Kayan Yıldız 1: Üst-Sol'dan Sağ-Aşağı
    val star1Time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star1Time"
    )

    // Kayan Yıldız 2: Sağ-Üst'ten Sol-Aşağı (Altın Işıltı)
    val star2Time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star2Time"
    )

    // Kayan Yıldız 3: Orta Bölge Canlı Çapraz Hüzme
    val star3Time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star3Time"
    )

    // Genel Parıldama ve Kristal Işıltı Nabzı (Sparkle Pulse)
    val sparklePulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklePulse"
    )

    // Stardust Floating (Hafif yukarı ve yana salınım)
    val dustDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dustDrift"
    )

    val dustParticles = remember {
        val rand = Random(5555)
        List(24) {
            ForegroundDustParticle(
                startXRatio = rand.nextFloat(),
                startYRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 2.2f + 1.2f,
                isGold = rand.nextBoolean(),
                driftSpeed = rand.nextFloat() * 0.7f + 0.4f,
                phaseOffset = rand.nextFloat() * 6.28f,
                baseAlpha = rand.nextFloat() * 0.35f + 0.45f, // 0.45 - 0.80 arası parlak
                hasSparkle = rand.nextFloat() > 0.40f // %60 oranında 4 köşeli kristal parıltı
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // --- 1. PARLAK VE IŞILTILI KAYAN YILDIZ 1 (Cyan / Beyaz Işık Hüzmesi) ---
        if (star1Time in 0.06f..0.34f) {
            val progress = (star1Time - 0.06f) / 0.28f
            val startX = w * 0.02f
            val startY = h * 0.10f
            val travelDistX = w * 0.82f
            val travelDistY = h * 0.32f

            val headX = startX + travelDistX * progress
            val headY = startY + travelDistY * progress

            val tailLength = 140f
            val tailX = headX - tailLength * 0.88f
            val tailY = headY - tailLength * 0.34f

            val fade = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f)
            val starAlpha = (0.92f * fade).coerceIn(0f, 0.95f)

            if (starAlpha > 0.05f) {
                // A. Dış Parıltı Aurası (Glow Line)
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            ZenSkyCyan.copy(alpha = starAlpha * 0.35f),
                            ZenSkyCyan.copy(alpha = starAlpha * 0.75f)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 6.0f,
                    cap = StrokeCap.Round
                )
                // B. Keskin İç Işık Çizgisi (Beam Line)
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            ZenSkyCyan.copy(alpha = starAlpha * 0.7f),
                            Color.White.copy(alpha = starAlpha)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 2.4f,
                    cap = StrokeCap.Round
                )
                // C. Yıldız Başı Parlaması (Head Glint & Aura)
                drawCircle(
                    color = ZenSkyCyan.copy(alpha = starAlpha * 0.8f),
                    radius = 8.0f,
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    radius = 3.5f,
                    center = Offset(headX, headY)
                )
                // 4 Köşeli Işıltı Çaprazı (Cross Glint)
                val glintLen = 9.0f * fade
                drawLine(Color.White.copy(alpha = starAlpha * 0.9f), Offset(headX - glintLen, headY), Offset(headX + glintLen, headY), strokeWidth = 1.4f)
                drawLine(Color.White.copy(alpha = starAlpha * 0.9f), Offset(headX, headY - glintLen), Offset(headX, headY + glintLen), strokeWidth = 1.4f)
            }
        }

        // --- 2. PARLAK VE IŞILTILI KAYAN YILDIZ 2 (Altın Sarısı Işık Hüzmesi) ---
        if (star2Time in 0.42f..0.72f) {
            val progress = (star2Time - 0.42f) / 0.30f
            val startX = w * 0.96f
            val startY = h * 0.22f
            val travelDistX = -w * 0.78f
            val travelDistY = h * 0.28f

            val headX = startX + travelDistX * progress
            val headY = startY + travelDistY * progress

            val tailLength = 130f
            val tailX = headX + tailLength * 0.88f
            val tailY = headY - tailLength * 0.32f

            val fade = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f)
            val starAlpha = (0.88f * fade).coerceIn(0f, 0.92f)

            if (starAlpha > 0.05f) {
                // A. Altın Işıltı Aurası
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            ZenMoonGold.copy(alpha = starAlpha * 0.35f),
                            ZenMoonGold.copy(alpha = starAlpha * 0.75f)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 5.5f,
                    cap = StrokeCap.Round
                )
                // B. İç Işık Çizgisi
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            ZenMoonGold.copy(alpha = starAlpha * 0.7f),
                            Color.White.copy(alpha = starAlpha)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
                // C. Yıldız Başı
                drawCircle(
                    color = ZenMoonGold.copy(alpha = starAlpha * 0.8f),
                    radius = 7.5f,
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    radius = 3.2f,
                    center = Offset(headX, headY)
                )
                val glintLen = 8.5f * fade
                drawLine(Color.White.copy(alpha = starAlpha * 0.9f), Offset(headX - glintLen, headY), Offset(headX + glintLen, headY), strokeWidth = 1.3f)
                drawLine(Color.White.copy(alpha = starAlpha * 0.9f), Offset(headX, headY - glintLen), Offset(headX, headY + glintLen), strokeWidth = 1.3f)
            }
        }

        // --- 3. PARLAK VE IŞILTILI KAYAN YILDIZ 3 (Orta/Alt Ekran Hızlı Hüzme) ---
        if (star3Time in 0.60f..0.86f) {
            val progress = (star3Time - 0.60f) / 0.26f
            val startX = w * 0.15f
            val startY = h * 0.48f
            val travelDistX = w * 0.74f
            val travelDistY = h * 0.25f

            val headX = startX + travelDistX * progress
            val headY = startY + travelDistY * progress

            val tailLength = 110f
            val tailX = headX - tailLength * 0.86f
            val tailY = headY - tailLength * 0.30f

            val fade = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f)
            val starAlpha = (0.85f * fade).coerceIn(0f, 0.90f)

            if (starAlpha > 0.05f) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            ZenSkyCyan.copy(alpha = starAlpha * 0.30f),
                            Color.White.copy(alpha = starAlpha)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 4.5f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = starAlpha)),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 2.0f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    radius = 3.0f,
                    center = Offset(headX, headY)
                )
            }
        }

        // --- 4. ÖN PLANDA PARILDAYAN YILDIZ TOZLARI & 4 KÖŞELİ KRİSTAL IŞILTILAR ---
        for (d in dustParticles) {
            val driftOffset = (dustDrift * d.driftSpeed) * 35f
            val cx = (d.startXRatio * w + sin(dustDrift * 6.28f * d.driftSpeed + d.phaseOffset) * 20f) % w
            val cy = (d.startYRatio * h - driftOffset + h) % h

            val individualPulse = (sin(sparklePulse * 3.14f * 2f + d.phaseOffset) * 0.5f + 0.5f).coerceIn(0f, 1f)
            val col = if (d.isGold) ZenMoonGold else ZenSkyCyan
            val alpha = (d.baseAlpha * (0.60f + 0.40f * individualPulse)).coerceIn(0.20f, 0.95f)

            // Işıltı Aurası
            drawCircle(
                color = col.copy(alpha = alpha * 0.55f),
                radius = d.radius * (2.2f + 0.8f * individualPulse),
                center = Offset(cx, cy)
            )
            // Parlak Çekirdek
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = d.radius * 0.9f,
                center = Offset(cx, cy)
            )

            // 4 Köşeli Elmas Işıltı Çaprazı (Kristal Efekti)
            if (d.hasSparkle && individualPulse > 0.45f) {
                val sparkLen = (d.radius * 2.8f * individualPulse)
                val sparkAlpha = (alpha * 0.85f).coerceIn(0f, 1f)
                drawLine(
                    color = Color.White.copy(alpha = sparkAlpha),
                    start = Offset(cx - sparkLen, cy),
                    end = Offset(cx + sparkLen, cy),
                    strokeWidth = 1.2f
                )
                drawLine(
                    color = Color.White.copy(alpha = sparkAlpha),
                    start = Offset(cx, cy - sparkLen),
                    end = Offset(cx, cy + sparkLen),
                    strokeWidth = 1.2f
                )
            }
        }
    }
}




