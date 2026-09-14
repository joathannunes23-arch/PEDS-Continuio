package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.Tonality
import com.example.ui.theme.BlofeldCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SerumPurple
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    waveform: FloatArray,
    rms: Float,
    isPlaying: Boolean,
    tonality: Tonality,
    modifier: Modifier = Modifier,
    heightDp: Int = 90
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientWave")
    val phaseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnim"
    )

    val primaryWaveColor = if (tonality == Tonality.MAJOR) MintGreen else SerumPurple
    val secondaryWaveColor = if (tonality == Tonality.MAJOR) BlofeldCyan else PastelPink

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // Soft glowing aura circle in the center
            val auraAlpha = if (isPlaying) (0.15f + (rms * 0.35f).coerceAtMost(0.45f)) else 0.05f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryWaveColor.copy(alpha = auraAlpha),
                        secondaryWaveColor.copy(alpha = auraAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(width / 2f, centerY),
                    radius = width * 0.45f
                )
            )

            // Flowing ambient sine waves
            val ambientPath = Path()
            val pointsCount = 64
            for (i in 0..pointsCount) {
                val x = (i.toFloat() / pointsCount) * width
                val waveOffset = sin(phaseAnim + (i.toFloat() / pointsCount) * 8.0).toFloat()
                val waveOffset2 = sin(phaseAnim * 0.7 + (i.toFloat() / pointsCount) * 4.0).toFloat()
                val amp = if (isPlaying) (12f + rms * 25f) else 4f
                val y = centerY + (waveOffset * 0.6f + waveOffset2 * 0.4f) * amp

                if (i == 0) ambientPath.moveTo(x, y) else ambientPath.lineTo(x, y)
            }

            drawPath(
                path = ambientPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        secondaryWaveColor.copy(alpha = 0.3f),
                        primaryWaveColor.copy(alpha = 0.5f),
                        secondaryWaveColor.copy(alpha = 0.3f)
                    )
                ),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Live Synth Waveform Line
            if (waveform.isNotEmpty() && isPlaying) {
                val livePath = Path()
                val livePoints = waveform.size
                for (i in 0 until livePoints) {
                    val x = (i.toFloat() / (livePoints - 1)) * width
                    val rawSample = waveform[i]
                    val y = centerY + (rawSample * (height * 0.42f)).coerceIn(-height * 0.45f, height * 0.45f)

                    if (i == 0) livePath.moveTo(x, y) else livePath.lineTo(x, y)
                }

                drawPath(
                    path = livePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primaryWaveColor.copy(alpha = 0.8f),
                            secondaryWaveColor.copy(alpha = 1.0f),
                            primaryWaveColor.copy(alpha = 0.8f)
                        )
                    ),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                // Static subtle resting line
                drawLine(
                    color = Color(0x3364748B),
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}
