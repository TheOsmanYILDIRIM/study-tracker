package com.studytracker.feature.launch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Native, bitmap-free Compose counterpart to assets/brand/studytracker-solar-odyssey.svg.
 * The book emblem is displayed separately using the existing Android VectorDrawable;
 * the backdrop draws the orrery, planetary bodies, sunlight, stellar dust and comet.
 */
@Composable
internal fun SolarOdysseyScene(
    appearance: Float,
    orbitPhase: Float,
    twinklePhase: Float
) {
    val stars = remember {
        List(72) { i ->
            SolarDust(
                x = ((i * 83 + 37) % 353) / 360f,
                y = ((i * 139 + 71) % 699) / 720f,
                size = when (i % 7) { 0 -> 1.8f; 1 -> 1.15f; else -> .72f },
                seed = i * .61f
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w * .50f, h * .465f)
        val sun = Offset(w * .50f, h * .785f)
        val unit = min(w / 360f, h / 640f)
        val t = orbitPhase * (2f * PI.toFloat())

        // Galaxy clouds. Atmospheric light is vector drawn (no web view or image assets).
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x774E38C8), Color(0x334F239D), Color.Transparent),
                center = Offset(w * .44f, h * .41f), radius = w * 1.05f
            ),
            center = Offset(w * .44f, h * .41f), radius = w * 1.05f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x666639B9), Color.Transparent),
                center = Offset(w * .27f, h * .80f), radius = w * .72f
            ),
            center = Offset(w * .27f, h * .80f), radius = w * .72f
        )

        // Twinkling deterministic sky.
        stars.forEachIndexed { index, dot ->
            val pos = Offset(w * dot.x, h * dot.y)
            val pulse = .38f + .55f *
                ((1f + sin((twinklePhase + dot.seed).toDouble()).toFloat()) * .5f)
            val starColor = when (index % 4) {
                0 -> Color(0xFFFFE8B1)
                1 -> Color(0xFF87EFFF)
                else -> Color(0xFFC7A5FF)
            }
            if (index % 8 == 0) {
                drawSolarSpark(pos, (4f + index % 3) * unit, starColor, pulse)
            } else {
                drawCircle(starColor, dot.size * unit, pos, alpha = pulse)
            }
        }

        // Stellar sunrise / energy well at the bottom of the opening screen.
        val solarRadius = w * .64f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFFDF8D6), Color(0xEFFFBB62),
                    Color(0xBBCE6AB8), Color(0x226D3ACD), Color.Transparent
                ),
                center = sun, radius = solarRadius
            ),
            radius = solarRadius, center = sun,
            alpha = .75f + .25f * appearance
        )
        drawCircle(Color(0xFFFFEBA7), 12f * unit, sun, alpha = appearance)
        drawCircle(Color(0xFFFFFFFF), 5f * unit, sun, alpha = appearance)
        val rayStroke = 1.6f * unit
        val rays = listOf(
            Offset(0f, -w * .58f), Offset(w * .39f, -w * .34f),
            Offset(-w * .39f, -w * .34f), Offset(w * .48f, 0f),
            Offset(-w * .48f, 0f), Offset(w * .33f, w * .28f),
            Offset(-w * .33f, w * .28f)
        )
        rays.forEach { distance ->
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xCFFFF0AE), Color.Transparent),
                    start = sun, end = sun + distance
                ),
                start = sun, end = sun + distance,
                strokeWidth = rayStroke, alpha = appearance
            )
        }
        // Light cone shoots upward from the sun beneath the logo.
        val beam = Path().apply {
            moveTo(sun.x, sun.y)
            lineTo(w * .38f, h * .35f)
            lineTo(w * .62f, h * .35f)
            close()
        }
        drawPath(
            path = beam,
            brush = Brush.verticalGradient(
                .30f to Color.Transparent, 1f to Color(0x33FFF0B5)
            ),
            alpha = appearance
        )
        // Luminous concentric equatorial circles.
        listOf(.21f, .34f, .48f).forEachIndexed { index, scale ->
            val r = w * scale
            drawOval(
                color = if (index % 2 == 0) Color(0xFFFFE3A0) else Color(0xFFBBA0FF),
                topLeft = Offset(sun.x - r, sun.y - r * .21f),
                size = Size(r * 2f, r * .42f),
                style = Stroke(width = (2f - index * .28f) * unit),
                alpha = appearance * (1f - index * .16f)
            )
        }

        // Three solar-system orbits around the book.
        val orbits = listOf(
            Triple(w * .40f, w * .25f, -17f),
            Triple(w * .57f, w * .38f, 23f),
            Triple(w * .45f, w * .52f, 57f)
        )
        orbits.forEachIndexed { index, (radiusX, radiusY, tilt) ->
            rotate(tilt, pivot = center) {
                drawOval(
                    color = if (index == 1) Color(0xFFFFD89A) else Color(0xFF89DFF5),
                    topLeft = Offset(center.x - radiusX, center.y - radiusY),
                    size = Size(radiusX * 2f, radiusY * 2f),
                    style = Stroke(width = 1.2f * unit),
                    alpha = (.25f + appearance * .48f) * (1f - index * .14f)
                )
            }
        }

        // Individual planets follow different orbits, including Jupiter and Saturn.
        val jupiter = pointOnOrbit(center, w * .53f, w * .36f, -2.50f - t * .27f)
        drawPlanet(jupiter, w * .093f, Color(0xFFFFE7B1), Color(0xFFAF7189), Color(0xFF30225B), unit)
        rotate(-13f, pivot = jupiter) {
            for (stripe in -1..1) {
                drawLine(
                    Color(0xFFB98387),
                    start = jupiter + Offset(-w * .053f, stripe * w * .025f),
                    end = jupiter + Offset(w * .053f, (stripe - .22f) * w * .025f),
                    strokeWidth = 3f * unit, alpha = .39f
                )
            }
        }

        val saturn = pointOnOrbit(center, w * .48f, w * .30f, -.54f + t * .24f)
        drawPlanet(saturn, w * .075f, Color(0xFFFFEEBA), Color(0xFFDBAC8C), Color(0xFF593A7B), unit)
        rotate(-23f, pivot = saturn) {
            drawOval(
                color = Color(0xFFEFCE94),
                topLeft = Offset(saturn.x - w * .108f, saturn.y - w * .026f),
                size = Size(w * .216f, w * .052f),
                style = Stroke(width = 3.8f * unit),
                alpha = .93f
            )
            drawOval(
                color = Color(0xFFAD96F7),
                topLeft = Offset(saturn.x - w * .112f, saturn.y - w * .029f),
                size = Size(w * .224f, w * .058f),
                style = Stroke(width = 1.1f * unit),
                alpha = .93f
            )
        }

        val earth = pointOnOrbit(center, w * .45f, w * .46f, 1.27f + t * .38f)
        drawPlanet(earth, w * .051f, Color(0xFFCDF5FF), Color(0xFF347DC5), Color(0xFF122452), unit)
        drawOval(
            color = Color(0xFF62E2B4),
            topLeft = earth + Offset(-w * .034f, -w * .012f),
            size = Size(w * .027f, w * .015f),
            alpha = .8f
        )

        val mars = pointOnOrbit(center, w * .40f, w * .25f, 3.10f - t * .48f)
        drawPlanet(mars, w * .028f, Color(0xFFFFD9A1), Color(0xFFD28372), Color(0xFF5A325B), unit)

        // Sweep an intense comet around the solar system, leaving graduated arcs.
        val cometAngle = orbitPhase * 360f - 115f
        for (i in 0..5) {
            drawArc(
                color = when {
                    i < 2 -> Color(0xFF8968F7)
                    i < 4 -> Color(0xFF52D5F3)
                    else -> Color(0xFFFFDCA4)
                },
                startAngle = cometAngle - 62f + i * 7f,
                sweepAngle = 17f,
                useCenter = false,
                topLeft = Offset(center.x - w * .46f, center.y - w * .31f),
                size = Size(w * .92f, w * .62f),
                style = Stroke(width = (2f + i * .20f) * unit, cap = StrokeCap.Round),
                alpha = appearance * (.2f + i * .12f)
            )
        }
        val head = pointOnOrbit(center, w * .46f, w * .31f, (cometAngle - 3f) * PI.toFloat() / 180f)
        drawSolarSpark(head, 8.8f * unit, Color(0xFFFFF1B7), appearance)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x99FFF3BB), Color.Transparent), center = head, radius = 18f * unit
            ),
            center = head, radius = 18f * unit, alpha = appearance
        )

        // Atmospheric clouds around the equatorial horizon.
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x665D36AC), Color.Transparent),
                center = Offset(0f, h), radius = w * .6f
            ),
            center = Offset(0f, h), radius = w * .6f
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x665D48BE), Color.Transparent),
                center = Offset(w, h), radius = w * .6f
            ),
            center = Offset(w, h), radius = w * .6f
        )
    }
}

private data class SolarDust(
    val x: Float, val y: Float, val size: Float, val seed: Float
)

private fun pointOnOrbit(center: Offset, rx: Float, ry: Float, angle: Float): Offset =
    Offset(
        center.x + rx * cos(angle.toDouble()).toFloat(),
        center.y + ry * sin(angle.toDouble()).toFloat()
    )

private fun DrawScope.drawPlanet(
    center: Offset, radius: Float, light: Color, mid: Color, dark: Color, unit: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(light, mid, dark),
            center = center + Offset(-radius * .33f, -radius * .30f),
            radius = radius * 1.65f
        ),
        center = center, radius = radius
    )
    drawCircle(
        color = light,
        radius = radius + .8f * unit,
        center = center,
        style = Stroke(width = .75f * unit),
        alpha = .46f
    )
}

private fun DrawScope.drawSolarSpark(
    center: Offset, radius: Float, color: Color, alpha: Float
) {
    val star = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x + radius * .16f, center.y - radius * .16f, center.x + radius, center.y)
        quadraticBezierTo(center.x + radius * .16f, center.y + radius * .16f, center.x, center.y + radius)
        quadraticBezierTo(center.x - radius * .16f, center.y + radius * .16f, center.x - radius, center.y)
        quadraticBezierTo(center.x - radius * .16f, center.y - radius * .16f, center.x, center.y - radius)
        close()
    }
    drawPath(star, color = color, alpha = alpha)
}
