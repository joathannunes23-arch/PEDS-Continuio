package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BlofeldCyan,
    onPrimary = Color(0xFF041829),
    primaryContainer = Color(0xFF0F3254),
    onPrimaryContainer = BlofeldCyanLight,

    secondary = SerumPurple,
    onSecondary = Color(0xFF260542),
    secondaryContainer = Color(0xFF45146D),
    onSecondaryContainer = SerumPurpleLight,

    tertiary = MintGreen,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = MintDark,
    onTertiaryContainer = MintGreen,

    background = SynthDarkBackground,
    onBackground = TextPrimary,
    surface = SynthChassisSurface,
    onSurface = TextPrimary,
    surfaceVariant = SynthChassisSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SynthBorderGlow,
    outlineVariant = SynthBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Synthesizers default to premium dark UI
    dynamicColor: Boolean = false, // Keep intentional synth palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
