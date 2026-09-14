package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tonality
import com.example.ui.theme.BlofeldCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SerumPurple
import com.example.ui.theme.SynthBorderGlow
import com.example.ui.theme.SynthChassisSurfaceVariant
import com.example.ui.theme.SynthDarkBackground
import com.example.ui.theme.SynthPanelSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TonalityHeaderButton(
    tonality: Tonality,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMajor = tonality == Tonality.MAJOR
    val activeColor by animateColorAsState(
        targetValue = if (isMajor) MintGreen else SerumPurple,
        label = "tonalityColor"
    )
    val containerBg by animateColorAsState(
        targetValue = if (isMajor) Color(0xFF064E3B) else Color(0xFF3B0764),
        label = "tonalityBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isMajor) BlofeldCyan else PastelPink,
        label = "tonalityBorder"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(containerBg)
            .border(1.5.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = activeColor)
            ) {
                onToggle()
            }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("tonality_toggle_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
            Column {
                Text(
                    text = if (isMajor) "MAJOR (MAIOR)" else "MINOR (MENOR)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
            }
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Alternar Tonalidade",
                tint = activeColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SynthSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    displayValue: String? = null,
    accentColor: Color = BlofeldCyan,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Text(
                text = displayValue ?: "%.2f".format(value),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = SynthPanelSurface
            ),
            modifier = Modifier.height(34.dp)
        )
    }
}

@Composable
fun ModWheelController(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String = "MOD",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(52.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SynthChassisSurfaceVariant)
            .border(1.dp, SynthBorderGlow, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = -dragAmount / 130f
                    val nextVal = (value + delta).coerceIn(0f, 1f)
                    onValueChange(nextVal)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Active filled level
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(value.coerceIn(0.05f, 1f))
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(PastelPink, SerumPurple)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(value * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun PitchBendWheelController(
    semitones: Float,
    onValueChange: (Float) -> Unit,
    label: String = "PITCH",
    modifier: Modifier = Modifier
) {
    // Pitch bend returns toward 0 when released
    Box(
        modifier = modifier
            .width(52.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SynthChassisSurfaceVariant)
            .border(1.dp, SynthBorderGlow, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { onValueChange(0f) },
                    onDragCancel = { onValueChange(0f) }
                ) { change, dragAmount ->
                    change.consume()
                    val delta = -dragAmount / 60f
                    val nextVal = (semitones + delta).coerceIn(-2f, 2f)
                    onValueChange(nextVal)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Center resting line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(BlofeldCyan)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (semitones >= 0) "+%.1f".format(semitones) else "%.1f".format(semitones),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (semitones != 0f) MintGreen else TextMuted
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
    }
}
