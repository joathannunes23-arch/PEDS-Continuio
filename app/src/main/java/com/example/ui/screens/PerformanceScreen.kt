package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RootNote
import com.example.model.Tonality
import com.example.ui.WorshipViewModel
import com.example.ui.components.SynthPadButton
import com.example.ui.components.SynthSlider
import com.example.ui.components.TonalityHeaderButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.BlofeldCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SerumPurple
import com.example.ui.theme.SynthBorderGlow
import com.example.ui.theme.SynthChassisSurface
import com.example.ui.theme.SynthDarkBackground
import com.example.ui.theme.SynthPanelSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PerformanceScreen(
    viewModel: WorshipViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeNote by viewModel.activeRootNote.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val tonality by viewModel.tonality.collectAsState()
    val minorScaleType by viewModel.minorScaleType.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val masterVol by viewModel.masterVolume.collectAsState()
    val waveform by viewModel.visualizerWaveform.collectAsState()
    val rms by viewModel.visualizerRms.collectAsState()
    val octaveShift by viewModel.octaveShift.collectAsState()

    val activeGlowColor = if (tonality == Tonality.MAJOR) MintGreen else SerumPurple

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SynthDarkBackground)
            .padding(16.dp)
            .testTag("performance_mode_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: Clean Minimal Stage Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit Performance Mode Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SynthPanelSurface)
                        .testTag("exit_performance_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sair do Modo Palco",
                        tint = TextPrimary
                    )
                }

                // Preset Name & Octave
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentPreset.name.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = BlofeldCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Oitava ${if (octaveShift >= 0) "+$octaveShift" else "$octaveShift"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Big MAJOR / MINOR switch
                TonalityHeaderButton(
                    tonality = tonality,
                    onToggle = { viewModel.toggleTonality() }
                )
            }

            // BIG CENTRAL READOUT (Active Key, Tonality, Visualizer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isPlaying) "${activeNote.symbol} ${tonality.label}" else "PAD EM PAUSA",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPlaying) activeGlowColor else TextMuted,
                    letterSpacing = 2.sp
                )

                if (isPlaying) {
                    Text(
                        text = if (tonality == Tonality.MAJOR) "Escala Maior Aberta (Worship Standard)" else minorScaleType.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                WaveformVisualizer(
                    waveform = waveform,
                    rms = rms,
                    isPlaying = isPlaying,
                    tonality = tonality,
                    heightDp = 70
                )
            }

            // OVERSIZED 12 PAD BUTTONS (STAGE MATRIX)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val row1 = listOf(RootNote.C, RootNote.C_SHARP, RootNote.D, RootNote.D_SHARP)
                val row2 = listOf(RootNote.E, RootNote.F, RootNote.F_SHARP, RootNote.G)
                val row3 = listOf(RootNote.G_SHARP, RootNote.A, RootNote.A_SHARP, RootNote.B)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row1.forEach { note ->
                        SynthPadButton(
                            note = note,
                            isActive = isPlaying && activeNote == note,
                            tonality = tonality,
                            onClick = { viewModel.togglePadPlay(note) },
                            modifier = Modifier.weight(1f),
                            heightDp = 80
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row2.forEach { note ->
                        SynthPadButton(
                            note = note,
                            isActive = isPlaying && activeNote == note,
                            tonality = tonality,
                            onClick = { viewModel.togglePadPlay(note) },
                            modifier = Modifier.weight(1f),
                            heightDp = 80
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row3.forEach { note ->
                        SynthPadButton(
                            note = note,
                            isActive = isPlaying && activeNote == note,
                            tonality = tonality,
                            onClick = { viewModel.togglePadPlay(note) },
                            modifier = Modifier.weight(1f),
                            heightDp = 80
                        )
                    }
                }
            }

            // BOTTOM STAGE CONTROLS: Master Volume & Stop / Play
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SynthChassisSurface)
                    .border(1.dp, SynthBorderGlow, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SynthSlider(
                    label = "VOLUME",
                    value = masterVol,
                    onValueChange = { viewModel.setMasterVolume(it) },
                    displayValue = "${(masterVol * 100).toInt()}%",
                    accentColor = BlofeldCyan,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { viewModel.togglePadPlay() },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) MintGreen else BlofeldCyan)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Parar" else "Tocar",
                        tint = SynthDarkBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
