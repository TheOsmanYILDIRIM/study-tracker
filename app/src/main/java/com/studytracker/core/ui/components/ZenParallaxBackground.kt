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
import kotlin.math.sin
import kotlin.random.Random

/**
 * Gökyüzündeki takımyıldızı koordinatları (xRatio, yRatio, 0.0f - 1.0f).
 */
data class ZenConstellationStar(
    val id: Int,
    val xRatio: Float,
    val yRatio: Float,
    val baseRadius: Float = 3.2f
)

val ZenConstellationStars = listOf(
    ZenConstellationStar(0, 0.18f, 0.10f, 3.8f),
    ZenConstellationStar(1, 0.32f, 0.07f, 3.4f),
    ZenConstellationStar(2, 0.50f, 0.05f, 4.2f),
    ZenConstellationStar(3, 0.68f, 0.08f, 3.6f),
    ZenConstellationStar(4, 0.84f, 0.12f, 4.0f),
    ZenConstellationStar(5, 0.14f, 0.20f, 3.2f),
    ZenConstellationStar(6, 0.28f, 0.16f, 3.8f),
    ZenConstellationStar(7, 0.60f, 0.14f, 3.4f),
    ZenConstellationStar(8, 0.74f, 0.18f, 3.8f),
    ZenConstellationStar(9, 0.88f, 0.24f, 3.2f),
    ZenConstellationStar(10, 0.22f, 0.26f, 3.4f),
    ZenConstellationStar(11, 0.40f, 0.22f, 3.8f),
    ZenConstellationStar(12, 0.58f, 0.24f, 3.2f),
    ZenConstellationStar(13, 0.72f, 0.28f, 3.6f),
    ZenConstellationStar(14, 0.16f, 0.34f, 3.0f),
    ZenConstellationStar(15, 0.84f, 0.33f, 3.4f),
    ZenConstellationStar(16, 0.48f, 0.12f, 4.4f),
    ZenConstellationStar(17, 0.66f, 0.20f, 3.6f),
    ZenConstellationStar(18, 0.36f, 0.30f, 3.2f),
    ZenConstellationStar(19, 0.54f, 0.32f, 3.4f)
)

private data class AmbientFirefly(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val isGold: Boolean,
    val pulseSpeed: Float
)

/**
 * Masal Arka Planı + Görev-Yıldız Dönüşüm Sistemi:
 * - Görevler tamamlandıkça gökyüzünde birer birer altın yıldızlar doğar.
 * - Görev bitirildiğinde (flyingStarTrigger):
 *   1. Görev kartından yukarıya görkemli bir kuyruklu yıldız fırlar.
 *   2. Gökyüzündeki takımyıldızı yuvasına ulaştığında süpernova patlamasıyla hedef yıldızı aydınlatır.
 *   3. O yıldız gökyüzünde kalıcı olarak ışıldamaya başlar.
 * - Toplam tamamlanan görev oranıyla gökyüzü gece karanlığından ışıltılı, aydınlık bir geceye döner.
 */
@Composable
fun ZenParallaxBackground(
    modifier: Modifier = Modifier,
    completedTasksCount: Int = 0,
    totalTasksCount: Int = 1,
    flyingStarTrigger: Long = 0L
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenSingleImageParallax")

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
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerPulse"
    )

    // Görev tamamlama ilerleme oranı (0.0f = Loş Gece, 1.0f = Işıl Işıl Aydınlık Gece)
    val effectiveProgress = remember(completedTasksCount, totalTasksCount) {
        if (totalTasksCount <= 0) {
            0.5f
        } else {
            (completedTasksCount.toFloat() / totalTasksCount.coerceAtLeast(1)).coerceIn(0f, 1f)
        }
    }

    // Görevler bittikçe gökyüzü aydınlanır ve renkler canlanır
    val bgAlpha = 0.45f + 0.55f * effectiveProgress
    val scrimAlpha = (0.50f - 0.40f * effectiveProgress).coerceIn(0.08f, 0.55f)
    val colorSaturation = 0.85f + 0.55f * effectiveProgress

    val colorMatrix = remember(colorSaturation) {
        ColorMatrix().apply {
            setToSaturation(colorSaturation)
        }
    }

    // --- GÖREV TAMAMLANDIĞINDA YILDIZ DÖNÜŞÜMÜ & UÇUŞ ANİMASYONU ---
    val flyingStarAnim = remember { Animatable(0f) }
    var burstTargetIndex by remember { mutableStateOf(0) }
    var burstAnimTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(flyingStarTrigger) {
        if (flyingStarTrigger > 0L) {
            val totalStars = ZenConstellationStars.size
            burstTargetIndex = (completedTasksCount - 1).coerceIn(0, totalStars - 1)
            burstAnimTrigger = false
            flyingStarAnim.snapTo(0f)
            // Görevden gökyüzüne süzülüş (1.3 saniye)
            flyingStarAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing)
            )
            // Gökyüzündeki yıldıza varışta süpernova patlaması
            burstAnimTrigger = true
        }
    }

    // Patlama halkası animasyonu (Supernova Burst Ring)
    val burstRingProgress by animateFloatAsState(
        targetValue = if (burstAnimTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 850, easing = LinearOutSlowInEasing),
        label = "burstRingProgress"
    )

    // Ambient Yüzen Ateşböcekleri
    val fireflies = remember {
        val rand = Random(1337)
        List(22) {
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

        // --- 2. DİNAMİK GÖKYÜZÜ TAKIMYILDIZLARI, GÖREV DÖNÜŞÜM UÇUŞU & DOĞUŞ PATLAMASI ---
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

            // A. Görev Sayısına Göre Gökyüzünde Yanan Altın Takımyıldızları
            val totalStars = ZenConstellationStars.size
            val activeStarsCount = completedTasksCount.coerceIn(0, totalStars)

            for (i in 0 until totalStars) {
                val star = ZenConstellationStars[i]
                val starX = star.xRatio * width
                val starY = star.yRatio * height
                val isIgnited = i < activeStarsCount

                if (isIgnited) {
                    // Görev tamamlandığında gökte doğan ve parıldayan altın yıldız
                    val starAlpha = (0.80f + 0.20f * shimmerPulse)
                    val auraRadius = star.baseRadius * (4.2f + 1.2f * effectiveProgress)
                    val coreRadius = star.baseRadius * 1.3f

                    // Dış Altın Parıltı Aurası
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
                    // 4 Köşeli Elmas Işıltı
                    val glint = star.baseRadius * 2.2f * shimmerPulse
                    drawLine(Color.White.copy(alpha = starAlpha * 0.85f), Offset(starX - glint, starY), Offset(starX + glint, starY), strokeWidth = 1.2f)
                    drawLine(Color.White.copy(alpha = starAlpha * 0.85f), Offset(starX, starY - glint), Offset(starX, starY + glint), strokeWidth = 1.2f)
                } else {
                    // Henüz tamamlanmamış görevlerin gökyüzündeki loş bekleme yuvaları
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = star.baseRadius * 0.9f,
                        center = Offset(starX, starY)
                    )
                }
            }

            // B. Görev Tamamlandığında Görevden Gökyüzüne Fırlayan Kuyruklu Yıldız (Transformation Flight)
            val flyP = flyingStarAnim.value
            if (flyP > 0f && flyP < 1f) {
                val targetStar = ZenConstellationStars[burstTargetIndex]
                val startX = width * 0.5f
                val startY = height * 0.85f // Alt görev alanından çıkış
                val endX = targetStar.xRatio * width
                val endY = targetStar.yRatio * height

                // Yumuşak parabolik yay çizerek süzülüş
                val currentX = startX + (endX - startX) * flyP
                val arcHeight = height * 0.22f * sin(flyP * Math.PI.toFloat())
                val currentY = startY + (endY - startY) * flyP - arcHeight

                // Işıltılı Kuyruk Hüzmesi (Glowing Cyan-Gold Trail)
                for (tail in 1..7) {
                    val tailP = (flyP - tail * 0.032f).coerceAtLeast(0f)
                    val tailX = startX + (endX - startX) * tailP
                    val tailArc = height * 0.22f * sin(tailP * Math.PI.toFloat())
                    val tailY = startY + (endY - startY) * tailP - tailArc
                    val tailAlpha = (1f - tail * 0.14f) * (1f - flyP * 0.3f)

                    drawCircle(
                        color = ZenSkyCyan.copy(alpha = tailAlpha * 0.75f),
                        radius = (8f - tail * 0.9f).coerceAtLeast(2.0f),
                        center = Offset(tailX, tailY)
                    )
                    drawCircle(
                        color = ZenMoonGold.copy(alpha = tailAlpha * 0.50f),
                        radius = (5f - tail * 0.6f).coerceAtLeast(1.5f),
                        center = Offset(tailX, tailY)
                    )
                }

                // Yıldız Başı / Çekirdeği (Radiant Head)
                drawCircle(
                    color = ZenMoonGold.copy(alpha = 0.95f),
                    radius = 12f,
                    center = Offset(currentX, currentY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6.5f,
                    center = Offset(currentX, currentY)
                )
                // Uçan Yıldız Çapraz Işıltısı
                drawLine(Color.White, Offset(currentX - 14f, currentY), Offset(currentX + 14f, currentY), strokeWidth = 1.8f)
                drawLine(Color.White, Offset(currentX, currentY - 14f), Offset(currentX, currentY + 14f), strokeWidth = 1.8f)
            }

            // C. Gökyüzündeki Yıldıza Ulaşma & Doğuş Süpernova Patlaması (Supernova Ignition Ring)
            if (burstRingProgress > 0f && burstRingProgress < 1f) {
                val targetStar = ZenConstellationStars[burstTargetIndex]
                val targetX = targetStar.xRatio * width
                val targetY = targetStar.yRatio * height

                val ringRadius = 10f + burstRingProgress * 65f
                val ringAlpha = (1f - burstRingProgress) * 0.95f

                // Dış süpernova halkası
                drawCircle(
                    color = ZenMoonGold.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = Offset(targetX, targetY)
                )
                // Cyan iç şok dalgası
                drawCircle(
                    color = ZenSkyCyan.copy(alpha = ringAlpha * 0.7f),
                    radius = ringRadius * 0.65f,
                    center = Offset(targetX, targetY)
                )
                // İç süper parlak çekirdek patlaması
                drawCircle(
                    color = Color.White.copy(alpha = (1f - burstRingProgress)),
                    radius = 12f * (1f - burstRingProgress),
                    center = Offset(targetX, targetY)
                )
            }

            // D. Ambient Ateşböcekleri (İlerleme arttıkça daha canlı)
            for (f in fireflies) {
                val cx = f.xRatio * width
                val cy = f.yRatio * height
                val col = if (f.isGold) ZenMoonGold else ZenSkyCyan
                val alpha = (shimmerPulse * f.pulseSpeed * (0.35f + 0.65f * effectiveProgress)).coerceIn(0.15f, 0.95f)

                drawCircle(
                    color = col.copy(alpha = alpha * 0.40f),
                    radius = f.radius * (2.0f + 1.0f * effectiveProgress),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.85f),
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





