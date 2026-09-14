package com.studytracker.core.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.ui.theme.EmeraldSuccess
import com.studytracker.core.ui.theme.PurpleActive
import kotlinx.coroutines.launch

@Composable
fun FloatingHUDView(
    elapsedSeconds: Long,
    screenshotCount: Int,
    isFinishing: Boolean,
    onSingleTapCapture: () -> Unit,
    onLongPressFinish: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var isPressing by remember { mutableStateOf(false) }
    var flashAlpha by remember { mutableStateOf(0f) }

    val formattedTime = remember(elapsedSeconds) {
        val mins = elapsedSeconds / 60
        val secs = elapsedSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Box(
        modifier = Modifier
            .shadow(12.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1E1B4B).copy(alpha = 0.92f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressing = true
                        val animationJob = coroutineScope.launch {
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
                            )
                            if (progress.value >= 0.99f) {
                                onLongPressFinish()
                            }
                        }
                        tryAwaitRelease()
                        isPressing = false
                        animationJob.cancel()
                        coroutineScope.launch {
                            progress.animateTo(0f, animationSpec = tween(200))
                        }
                    },
                    onTap = {
                        // Flash animation
                        flashAlpha = 0.6f
                        onSingleTapCapture()
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(150)
                            flashAlpha = 0f
                        }
                    }
                )
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Flash overlay for capture feedback
        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = flashAlpha))
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Circular progress indicator around icon
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Progress arc (fills as user holds)
                    drawArc(
                        color = EmeraldSuccess,
                        startAngle = -90f,
                        sweepAngle = progress.value * 360f,
                        useCenter = false,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Icon(
                    imageVector = if (isPressing || isFinishing) Icons.Default.Stop else Icons.Default.CameraAlt,
                    contentDescription = "Oturum Durumu",
                    tint = if (isPressing) EmeraldSuccess else PurpleActive,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Elapsed time & Screenshot counter
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = formattedTime,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "📸 $screenshotCount kare",
                    color = Color(0xFFA5B4FC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
