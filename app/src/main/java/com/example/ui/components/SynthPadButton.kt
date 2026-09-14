package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RootNote
import com.example.model.Tonality
import com.example.ui.theme.BlofeldCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PadOffState
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SerumPurple
import com.example.ui.theme.SynthBorderGlow
import com.example.ui.theme.SynthBorderSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SynthPadButton(
    note: RootNote,
    isActive: Boolean,
    tonality: Tonality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 68
) {
    val infiniteTransition = rememberInfiniteTransition(label = "padPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActive) 1.035f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeGlowColor = if (tonality == Tonality.MAJOR) MintGreen else SerumPurple
    val activeBorderColor = if (tonality == Tonality.MAJOR) BlofeldCyan else PastelPink

    val targetBgColor = if (isActive) {
        if (tonality == Tonality.MAJOR) Color(0xFF0F3229) else Color(0xFF2C134B)
    } else {
        PadOffState
    }

    val animatedBg by animateColorAsState(targetValue = targetBgColor, label = "padBg")
    val animatedBorder by animateColorAsState(
        targetValue = if (isActive) activeBorderColor else SynthBorderSubtle,
        label = "padBorder"
    )

    val isAccidental = note.symbol.contains("#")

    Box(
        modifier = modifier
            .height(heightDp.dp)
            .scale(if (isActive) pulseScale else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBg)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = animatedBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .drawBehind {
                if (isActive) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeGlowColor.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            radius = size.maxDimension * 0.7f
                        )
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = activeGlowColor)
            ) {
                onClick()
            }
            .testTag("pad_note_${note.symbol}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = note.symbol,
                fontSize = if (isAccidental) 17.sp else 19.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isActive) TextPrimary else if (isAccidental) Color(0xFFCBD5E1) else TextPrimary
            )
            Text(
                text = if (isActive) {
                    if (tonality == Tonality.MAJOR) "MAJ" else "MIN"
                } else {
                    if (isAccidental) "alt" else "pad"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) activeGlowColor else TextMuted
            )
        }
    }
}
