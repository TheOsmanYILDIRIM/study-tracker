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
    val baseAlpha: Float
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
    val bgAlpha = 0.45f + 0.55f * effectiveProgress // 0.45f (loş) -> 1.0f (tam aydınlık)
    val scrimAlpha = (0.52f - 0.38f * effectiveProgress).coerceIn(0.10f, 0.60f) // 0.52f -> 0.14f (okuma koruması incelir)
    val colorSaturation = 0.80f + 0.55f * effectiveProgress // 0.80f -> 1.35f (renkler doygunlaşır)

    val colorMatrix = remember(colorSaturation) {
        ColorMatrix().apply {
            setToSaturation(colorSaturation)
        }
    }

    // Ambient Yüzen Ateşböcekleri
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
                    (0.70f + 0.30f * shimmerPulse)
                } else {
                    (0.12f + 0.08f * shimmerPulse) * effectiveProgress.coerceAtLeast(0.15f)
                }

                val auraRadius = if (isActive) star.baseRadius * (3.5f + 1.2f * effectiveProgress) else star.baseRadius * 1.6f
                val coreRadius = if (isActive) star.baseRadius * (1.2f + 0.4f * effectiveProgress) else star.baseRadius * 0.7f

                // Altın Işıltı Aurası
                drawCircle(
                    color = ZenMoonGold.copy(alpha = starAlpha * 0.50f),
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
                val alpha = (shimmerPulse * f.pulseSpeed * (0.35f + 0.65f * effectiveProgress)).coerceIn(0.10f, 0.95f)

                drawCircle(
                    color = col.copy(alpha = alpha * 0.35f),
                    radius = f.radius * (2.0f + 1.0f * effectiveProgress),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.8f),
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
 * Metinlerin ve kartların EN ÖNÜNDE (foreground) süzülen, aşırı sönük (ambient dim) kayan yıldızlar
 * ve parıltılı toz zerrecikleri katmanı.
 *
 * - Okuma konforunu ve kart tıklamalarını ASLA bozmaz.
 * - Çok düşük opaklık (0.08f - 0.22f) ile metinlerin üzerinden büyüleyici bir zarafetle geçer.
 */
@Composable
fun ZenForegroundStarOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenForegroundShootingStars")

    // Kayan Yıldız 1: Üst-Sol'dan Sağ-Aşağı (Periyodik süzülüş)
    val star1Time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star1Time"
    )

    // Kayan Yıldız 2: Sağ-Üst'ten Sol-Aşağı (Farklı faz ve süre)
    val star2Time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star2Time"
    )

    // Kayan Yıldız 3: Orta Bölge Hızlı & Çok İnce Çizgi
    val star3Time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star3Time"
    )

    // Faint Stardust Floating (Hafif yukarı ve yana salınım)
    val dustDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dustDrift"
    )

    val dustParticles = remember {
        val rand = Random(4242)
        List(18) {
            ForegroundDustParticle(
                startXRatio = rand.nextFloat(),
                startYRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 1.5f + 0.8f,
                isGold = rand.nextBoolean(),
                driftSpeed = rand.nextFloat() * 0.6f + 0.4f,
                baseAlpha = rand.nextFloat() * 0.10f + 0.08f // Aşırı sönük (0.08 - 0.18)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // --- 1. SÖNÜK KAYAN YILDIZ 1 ---
        // Zaman dilimi: 0.10f .. 0.35f arası uçar, geri kalan zamanda gizlidir
        if (star1Time in 0.08f..0.32f) {
            val progress = (star1Time - 0.08f) / 0.24f
            val startX = w * 0.05f
            val startY = h * 0.12f
            val travelDistX = w * 0.75f
            val travelDistY = h * 0.28f

            val headX = startX + travelDistX * progress
            val headY = startY + travelDistY * progress

            val tailLength = 90f
            val tailX = headX - tailLength * 0.85f
            val tailY = headY - tailLength * 0.35f

            // Girişte ve çıkışta yumuşak sönümlenme (Peak alpha: ~0.20f)
            val fade = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f)
            val starAlpha = (0.22f * fade).coerceIn(0f, 0.22f)

            if (starAlpha > 0.01f) {
                // Kuyruk İzi (Soft Gradient Trail)
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, ZenSkyCyan.copy(alpha = starAlpha * 0.6f), Color.White.copy(alpha = starAlpha)),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 1.6f,
                    cap = StrokeCap.Round
                )
                // Yıldız Başı Noktası
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha * 0.9f),
                    radius = 2.0f,
                    center = Offset(headX, headY)
                )
            }
        }

        // --- 2. SÖNÜK KAYAN YILDIZ 2 ---
        // Zaman dilimi: 0.45f .. 0.72f arası
        if (star2Time in 0.45f..0.72f) {
            val progress = (star2Time - 0.45f) / 0.27f
            val startX = w * 0.92f
            val startY = h * 0.25f
            val travelDistX = -w * 0.70f
            val travelDistY = h * 0.24f

            val headX = startX + travelDistX * progress
            val headY = startY + travelDistY * progress

            val tailLength = 80f
            val tailX = headX + tailLength * 0.85f
            val tailY = headY - tailLength * 0.32f

            val fade = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f)
            val starAlpha = (0.18f * fade).coerceIn(0f, 0.18f)

            if (starAlpha > 0.01f) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, ZenMoonGold.copy(alpha = starAlpha * 0.6f), Color.White.copy(alpha = starAlpha)),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 1.4f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha * 0.85f),
                    radius = 1.8f,
                    center = Offset(headX, headY)
                )
            }
        }

        // --- 3. SÖNÜK KAYAN YILDIZ 3 (Alt/Orta Bölge Hızlı Geçiş) ---
        // Zaman dilimi: 0.65f .. 0.88f
        if (star3Time in 0.65f..0.88f) {
            val progress = (star3Time - 0.65f) / 0.23f
            val startX = w * 0.20f
            val startY = h * 0.50f
            val travelDistX = w * 0.65f
            val travelDistY = h * 0.22f

            val headX = startX + travelDistX * progress
            val headY = startY + travelDistY * progress

            val tailLength = 70f
            val tailX = headX - tailLength * 0.85f
            val tailY = headY - tailLength * 0.30f

            val fade = sin(progress * Math.PI.toFloat()).coerceIn(0f, 1f)
            val starAlpha = (0.16f * fade).coerceIn(0f, 0.16f)

            if (starAlpha > 0.01f) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, ZenSkyCyan.copy(alpha = starAlpha * 0.5f), Color.White.copy(alpha = starAlpha)),
                        start = Offset(tailX, tailY),
                        end = Offset(headX, headY)
                    ),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY),
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha * 0.8f),
                    radius = 1.6f,
                    center = Offset(headX, headY)
                )
            }
        }

        // --- 4. ÖN PLANDA YAVAŞÇA YÜZEN AŞIRI SÖNÜK PARILTI TOZLARI (FOREGROUND STARDUST) ---
        for (d in dustParticles) {
            val driftOffset = (dustDrift * d.driftSpeed) * 30f
            val cx = (d.startXRatio * w + sin(dustDrift * 6.28f * d.driftSpeed) * 15f) % w
            val cy = (d.startYRatio * h - driftOffset + h) % h

            val col = if (d.isGold) ZenMoonGold else ZenSkyCyan
            val alpha = d.baseAlpha * (0.8f + 0.2f * sin(dustDrift * 3.14f * d.driftSpeed))

            drawCircle(
                color = col.copy(alpha = alpha * 0.6f),
                radius = d.radius * 1.8f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = d.radius,
                center = Offset(cx, cy)
            )
        }
    }
}



