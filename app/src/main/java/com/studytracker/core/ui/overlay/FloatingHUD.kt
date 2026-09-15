package com.studytracker.core.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studytracker.core.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FloatingHUDView(
    elapsedSeconds: Long,
    screenshotCount: Int,
    isFinishing: Boolean,
    isPaused: Boolean = false,
    onSingleTapCapture: () -> Unit,
    onLongPressFinish: () -> Unit,
    onTogglePause: () -> Unit
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
            .clip(CircleShape)
            .background(if (isPaused) Color(0xFF261808).copy(alpha = 0.98f) else Color(0xFF0C061C).copy(alpha = 0.98f))
            .border(1.5.dp, if (isPaused) ZomoAmber.copy(alpha = 0.8f) else ZomoPurplePrimary.copy(alpha = 0.6f), CircleShape)
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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Action Button: Single tap -> Capture, Long hold -> Finish
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                                flashAlpha = 0.6f
                                onSingleTapCapture()
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(150)
                                    flashAlpha = 0f
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawArc(
                        color = ZomoNeonMint,
                        startAngle = -90f,
                        sweepAngle = progress.value * 360f,
                        useCenter = false,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Icon(
                    imageVector = if (isPressing || isFinishing) Icons.Default.Stop else Icons.Default.CameraAlt,
                    contentDescription = "Kanıt Al / Bitir",
                    tint = if (isPressing) ZomoNeonMint else if (isPaused) ZomoAmber else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Elapsed time & Screenshot counter
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = if (isPaused) ZomoAmber else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isPaused) {
                        Surface(
                            shape = CircleShape,
                            color = ZomoAmberContainer
                        ) {
                            Text(
                                text = "MOLA",
                                color = ZomoAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "📸 $screenshotCount kanıt",
                    color = ZomoNeonMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Pause / Resume Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isPaused) ZomoAmber else Color(0x33A855F7))
                    .clickable { onTogglePause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Devam Et" else "Duraklat",
                    tint = if (isPaused) Color(0xFF451A03) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
