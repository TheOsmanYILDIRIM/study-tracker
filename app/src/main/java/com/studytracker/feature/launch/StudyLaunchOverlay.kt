package com.studytracker.feature.launch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.R
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Brief first-launch animation. The real NavHost is composed underneath so
 * Room data has time to emit without presenting a misleading empty state.
 * Uses the same vector as the adaptive launcher icon; no bitmap or network I/O.
 */
@Composable
fun StudyLaunchOverlay(onFinished: () -> Unit) {
    val latestOnFinished by rememberUpdatedState(onFinished)
    val entrance = remember { Animatable(0f) }
    val magic = rememberInfiniteTransition(label = "launch-magic")
    val orbitDegrees by magic.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )
    val twinklePhase by magic.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )

    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = tween(650, easing = FastOutSlowInEasing)
        )
        delay(440)
        latestOnFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0823),
                        Color(0xFF201654),
                        Color(0xFF0B1237)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.46f)
            val orbitWidth = min(size.width * 0.40f, 235.dp.toPx())
            val orbitHeight = orbitWidth * 0.75f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x665C57CC), Color(0x004641B0)),
                    center = center,
                    radius = orbitWidth * 1.65f
                ),
                center = center,
                radius = orbitWidth * 1.65f
            )

            // Deterministic star field; no per-frame allocations of bitmap assets.
            val starPositions = listOf(
                .12f to .16f, .24f to .31f, .76f to .18f, .89f to .29f,
                .13f to .51f, .86f to .54f, .25f to .70f, .75f to .77f,
                .42f to .11f, .64f to .84f, .18f to .86f, .81f to .91f
            )
            starPositions.forEachIndexed { index, pair ->
                val brightness = 0.50f + 0.44f *
                    ((1f + sin((twinklePhase + index * 0.73f).toDouble()).toFloat()) / 2f)
                val point = Offset(size.width * pair.first, size.height * pair.second)
                if (index % 3 == 0) {
                    drawMagicStar(point, (5 + index % 4).dp.toPx(), Color(0xFFFFE28E), brightness)
                } else {
                    drawCircle(
                        color = if (index % 2 == 0) Color(0xFFFFE28E) else Color(0xFF8EF4FC),
                        radius = (1.7f + index % 3).dp.toPx(),
                        center = point,
                        alpha = brightness
                    )
                }
            }

            // One orbiting comet sweeps across the book, then exits with the overlay.
            drawArc(
                color = Color(0x3349E7EC),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - orbitWidth, center.y - orbitHeight),
                size = Size(orbitWidth * 2f, orbitHeight * 2f),
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0x008D6DFF),
                        Color(0xFFAD83FF),
                        Color(0xFF70F7EC),
                        Color(0x00FFE69B)
                    ),
                    center = center
                ),
                startAngle = orbitDegrees - 225f,
                sweepAngle = 135f,
                useCenter = false,
                topLeft = Offset(center.x - orbitWidth, center.y - orbitHeight),
                size = Size(orbitWidth * 2f, orbitHeight * 2f),
                style = Stroke(width = 3.3.dp.toPx(), cap = StrokeCap.Round)
            )
            val cometRadians = Math.toRadians((orbitDegrees - 90f).toDouble())
            val cometPoint = Offset(
                center.x + orbitWidth * cos(cometRadians).toFloat(),
                center.y + orbitHeight * sin(cometRadians).toFloat()
            )
            drawMagicStar(cometPoint, 10.dp.toPx(), Color(0xFFFFE6A2), 1f)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            val value = entrance.value
            Box(
                modifier = Modifier
                    .size(216.dp)
                    .graphicsLayer {
                        val pop = 0.78f + value * 0.22f
                        scaleX = pop
                        scaleY = pop
                        rotationZ = (1f - value) * -7f
                        translationY = (1f - value) * 26.dp.toPx()
                        alpha = 0.74f + 0.26f * value
                    }
                    .clip(RoundedCornerShape(46.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0E0C32), Color(0xFF2A206A), Color(0xFF151147))
                        )
                    )
                    .border(
                        width = 1.4.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFAD87F2), Color(0xFF4DE6DC), Color(0xFF6C50BA))
                        ),
                        shape = RoundedCornerShape(46.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "StudyTracker, büyülü kitap ve kayan yıldız",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "StudyTracker",
                color = Color(0xFFF3EEFF),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    alpha = entrance.value
                    translationY = (1f - entrance.value) * 8.dp.toPx()
                }
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "Her ders yeni bir keşif",
                color = Color(0xFFC8C7EF),
                fontSize = 13.sp,
                modifier = Modifier.graphicsLayer { alpha = entrance.value }
            )
        }
    }
}

private fun DrawScope.drawMagicStar(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x + radius * .18f, center.y - radius * .18f, center.x + radius, center.y)
        quadraticBezierTo(center.x + radius * .18f, center.y + radius * .18f, center.x, center.y + radius)
        quadraticBezierTo(center.x - radius * .18f, center.y + radius * .18f, center.x - radius, center.y)
        quadraticBezierTo(center.x - radius * .18f, center.y - radius * .18f, center.x, center.y - radius)
        close()
    }
    drawPath(path = path, color = color, alpha = alpha)
}
