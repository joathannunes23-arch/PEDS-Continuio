package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.LooperState
import com.example.audio.WorshipMetronome
import com.example.model.ArpPattern
import com.example.model.FilterType
import com.example.model.LfoDestination
import com.example.model.SoundCategory
import com.example.ui.WorshipViewModel
import com.example.ui.components.ModWheelController
import com.example.ui.components.PitchBendWheelController
import com.example.ui.components.SynthSlider
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.BlofeldCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SerumPurple
import com.example.ui.theme.SerumPurpleLight
import com.example.ui.theme.SynthBorderGlow
import com.example.ui.theme.SynthBorderSubtle
import com.example.ui.theme.SynthChassisSurface
import com.example.ui.theme.SynthChassisSurfaceVariant
import com.example.ui.theme.SynthDarkBackground
import com.example.ui.theme.SynthPanelSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: WorshipViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Recorder state
    val isRecording by viewModel.performanceRecorder.isRecording.collectAsState()
    val recSeconds by viewModel.performanceRecorder.elapsedSeconds.collectAsState()
    val lastRecordedFile by viewModel.performanceRecorder.lastSavedFilePath.collectAsState()

    // Looper state
    val looperState by viewModel.worshipLooper.state.collectAsState()
    val currentBar by viewModel.worshipLooper.currentBar.collectAsState()
    val loopProgress by viewModel.worshipLooper.loopProgress.collectAsState()

    // Metronome state
    val metronomeBpm by viewModel.metronome.bpm.collectAsState()
    val isMetronomeActive by viewModel.metronome.isActive.collectAsState()
    val currentBeat by viewModel.metronome.currentBeat.collectAsState()

    // Synth parameters
    val cutoffHz by viewModel.cutoffHz.collectAsState()
    val resonance by viewModel.resonance.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    val isArpEnabled by viewModel.isArpEnabled.collectAsState()
    val arpSpeed by viewModel.arpSpeedSec.collectAsState()
    val arpPattern by viewModel.arpPattern.collectAsState()

    val lfoRate by viewModel.lfoRateHz.collectAsState()
    val lfoDepth by viewModel.lfoDepth.collectAsState()
    val lfoDest by viewModel.lfoDest.collectAsState()

    // Effects
    val reverbWet by viewModel.reverbWet.collectAsState()
    val reverbDecay by viewModel.reverbDecay.collectAsState()
    val shimmerLevel by viewModel.shimmerLevel.collectAsState()
    val delayTime by viewModel.delayTimeMs.collectAsState()
    val delayFeedback by viewModel.delayFeedback.collectAsState()
    val chorusDepth by viewModel.chorusDepth.collectAsState()
    val pan360 by viewModel.pan360.collectAsState()

    // Performance sliders
    val pitchBend by viewModel.pitchBend.collectAsState()
    val modWheel by viewModel.modWheel.collectAsState()

    // Category volumes
    val volPads by viewModel.volumePads.collectAsState()
    val volAmbient by viewModel.volumeAmbient.collectAsState()
    val volStrings by viewModel.volumeStrings.collectAsState()
    val volProgressive by viewModel.volumeProgressive.collectAsState()
    val volFx by viewModel.volumeWorshipFx.collectAsState()
    val volChoral by viewModel.volumeChoral.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SynthDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SCREEN TITLE
        item {
            Column {
                Text(
                    text = "CONFIGURAÇÕES & MOTOR DE SÍNTESE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Arpejador • LFO • Filtros • Efeitos de Igreja • Gravador WAV",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // 1. GRAVADOR DE PERFORMANCE AO VIVO (ATÉ 5 MINUTOS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (isRecording) Color.Red else SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecording) Color.Red else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GRAVADOR DE PERFORMANCE (WAV / ATÉ 5 MIN)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                        }

                        val mins = recSeconds / 60
                        val secs = recSeconds % 60
                        Text(
                            text = "%02d:%02d / 05:00".format(mins, secs),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) Color.Red else BlofeldCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Start/Stop Record Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRecording) Color(0xFF7F1D1D) else SynthPanelSurface)
                                .border(1.dp, if (isRecording) Color.Red else SynthBorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { viewModel.toggleRecording() }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = if (isRecording) Color.White else Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRecording) "PARAR GRAVAÇÃO" else "INICIAR GRAVAÇÃO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Export / Share Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (lastRecordedFile != null) BlofeldCyan.copy(alpha = 0.2f) else SynthChassisSurfaceVariant)
                                .border(1.dp, if (lastRecordedFile != null) BlofeldCyan else SynthBorderSubtle, RoundedCornerShape(10.dp))
                                .clickable(enabled = lastRecordedFile != null) {
                                    viewModel.shareLastRecording(context)
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = if (lastRecordedFile != null) BlofeldCyan else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EXPORTAR WAV",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastRecordedFile != null) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. LOOPER DE 8 BARRAS (COMPASSOS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOOPER SIMPLES (8 COMPASSOS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )

                        Text(
                            text = when (looperState) {
                                LooperState.RECORDING -> "GRAVANDO COMPASSO $currentBar / 8"
                                LooperState.PLAYING -> "TOCANDO COMPASSO $currentBar / 8"
                                LooperState.IDLE -> "PRONTO"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (looperState) {
                                LooperState.RECORDING -> Color.Red
                                LooperState.PLAYING -> MintGreen
                                LooperState.IDLE -> TextMuted
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { loopProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (looperState == LooperState.RECORDING) Color.Red else MintGreen,
                        trackColor = SynthPanelSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Gravar 8 barras
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (looperState == LooperState.RECORDING) Color(0xFF7F1D1D) else SynthPanelSurface)
                                .clickable { viewModel.startLooperRecord() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "GRAVAR 8 BARRAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Play/Stop loop
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (looperState == LooperState.PLAYING) MintGreen.copy(alpha = 0.2f) else SynthPanelSurface)
                                .clickable { viewModel.toggleLooperPlay() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (looperState == LooperState.PLAYING) "PAUSAR" else "TOCAR LOOP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (looperState == LooperState.PLAYING) MintGreen else TextSecondary
                            )
                        }

                        // Limpar
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SynthPanelSurface)
                                .clickable { viewModel.clearLooper() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LIMPAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // 3. METRÔNOMO DE ADORAÇÃO (4 TEMPOS DE WORSHIP)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "METRÔNOMO DE WORSHIP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )

                        // 4 LEDs Visual Beater
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..4).forEach { b ->
                                val isBeat = isMetronomeActive && currentBeat == b
                                val ledColor = if (b == 1) AmberWarm else MintGreen
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isBeat) ledColor else Color(0xFF1E293B))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 Tempos de adoração obrigatórios (60, 72, 80, 92 BPM)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WorshipMetronome.PRESETS.forEach { preset ->
                            val isSelected = metronomeBpm == preset.bpm
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BlofeldCyan.copy(alpha = 0.25f) else SynthPanelSurface)
                                    .border(1.dp, if (isSelected) BlofeldCyan else SynthBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setMetronomeBpm(preset.bpm) }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = preset.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) BlofeldCyan else TextPrimary
                                    )
                                    Text(
                                        text = preset.description.take(8) + "...",
                                        fontSize = 8.sp,
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SynthSlider(
                            label = "BPM",
                            value = metronomeBpm.toFloat(),
                            onValueChange = { viewModel.setMetronomeBpm(it.toInt()) },
                            valueRange = 40f..160f,
                            displayValue = "$metronomeBpm BPM",
                            accentColor = AmberWarm,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Tap Tempo Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SynthPanelSurface)
                                .clickable { viewModel.tapMetronome() }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("TAP", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BlofeldCyan)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Toggle Metronome Click
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMetronomeActive) MintGreen else SynthPanelSurface)
                                .clickable { viewModel.toggleMetronome() }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = if (isMetronomeActive) "LIGADO" else "LIGAR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isMetronomeActive) SynthDarkBackground else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 4. ARPEJADOR LENTO (0.5s – 4s COM 4 PADRÕES)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ARPEJADOR LENTO (0.5s - 4s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Padrões suaves para bases contínuas",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = isArpEnabled,
                            onCheckedChange = { viewModel.setArpEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintGreen,
                                checkedTrackColor = Color(0xFF065F46)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 Padrões de Arpejo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ArpPattern.values().forEach { pattern ->
                            val isSelected = arpPattern == pattern
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SerumPurple.copy(alpha = 0.25f) else SynthPanelSurface)
                                    .border(1.dp, if (isSelected) SerumPurple else SynthBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setArpPattern(pattern) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pattern.label.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SerumPurpleLight else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SynthSlider(
                        label = "Velocidade do Ciclo (Tempo por Nota)",
                        value = arpSpeed,
                        onValueChange = { viewModel.setArpSpeed(it) },
                        valueRange = 0.5f..4.0f,
                        displayValue = "%.2fs".format(arpSpeed),
                        accentColor = SerumPurple
                    )
                }
            }
        }

        // 5. LFO (TREMOLO, VIBRATO, WAH)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "LFO (MODULAÇÃO DE BAIXA FREQUÊNCIA)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Destino LFO (Tremolo, Vibrato, Wah)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LfoDestination.values().forEach { dest ->
                            val isSelected = lfoDest == dest
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PastelPink.copy(alpha = 0.25f) else SynthPanelSurface)
                                    .border(1.dp, if (isSelected) PastelPink else SynthBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setLfoDestination(dest) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dest.label.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PastelPink else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SynthSlider(
                        label = "Taxa do LFO (Rate)",
                        value = lfoRate,
                        onValueChange = { viewModel.setLfoRate(it) },
                        valueRange = 0.1f..6.0f,
                        displayValue = "%.1f Hz".format(lfoRate),
                        accentColor = PastelPink
                    )

                    SynthSlider(
                        label = "Profundidade (Depth)",
                        value = lfoDepth,
                        onValueChange = { viewModel.setLfoDepth(it) },
                        valueRange = 0.0f..1.0f,
                        displayValue = "${(lfoDepth * 100).toInt()}%",
                        accentColor = PastelPink
                    )
                }
            }
        }

        // 6. FILTRO (LP, HP, BP) COM RESSONÂNCIA
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "FILTRO MULTIMODO ANALÓGICO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterType.values().forEach { type ->
                            val isSelected = filterType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BlofeldCyan.copy(alpha = 0.25f) else SynthPanelSurface)
                                    .border(1.dp, if (isSelected) BlofeldCyan else SynthBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setFilterType(type) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.label.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BlofeldCyan else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SynthSlider(
                        label = "Frequência de Corte (Cutoff)",
                        value = cutoffHz,
                        onValueChange = { viewModel.setCutoff(it) },
                        valueRange = 200f..12000f,
                        displayValue = "${cutoffHz.toInt()} Hz",
                        accentColor = BlofeldCyan
                    )

                    SynthSlider(
                        label = "Ressonância (Q)",
                        value = resonance,
                        onValueChange = { viewModel.setResonance(it) },
                        valueRange = 0.5f..4.0f,
                        displayValue = "%.2f".format(resonance),
                        accentColor = BlofeldCyan
                    )
                }
            }
        }

        // 7. EFEITOS DE IGREJA (CHURCH FX): REVERB + DELAY + CHORUS + PAN 360°
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "EFEITOS DE IGREJA (CHURCH FX RACK)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reverb
                    SynthSlider(
                        label = "Reverb Catedral (Wet)",
                        value = reverbWet,
                        onValueChange = { viewModel.setReverbWet(it) },
                        displayValue = "${(reverbWet * 100).toInt()}%",
                        accentColor = MintGreen
                    )
                    SynthSlider(
                        label = "Decaimento do Reverb (Decay)",
                        value = reverbDecay,
                        onValueChange = { viewModel.setReverbDecay(it) },
                        displayValue = "${(reverbDecay * 100).toInt()}%",
                        accentColor = MintGreen
                    )
                    SynthSlider(
                        label = "Shimmer Octave Up ✨",
                        value = shimmerLevel,
                        onValueChange = { viewModel.setShimmerLevel(it) },
                        displayValue = "${(shimmerLevel * 100).toInt()}%",
                        accentColor = AmberWarm
                    )

                    // Delay
                    SynthSlider(
                        label = "Delay Estéreo (Tempo)",
                        value = delayTime,
                        onValueChange = { viewModel.setDelayTime(it) },
                        valueRange = 100f..800f,
                        displayValue = "${delayTime.toInt()} ms",
                        accentColor = BlofeldCyan
                    )
                    SynthSlider(
                        label = "Feedback do Delay",
                        value = delayFeedback,
                        onValueChange = { viewModel.setDelayFeedback(it) },
                        displayValue = "${(delayFeedback * 100).toInt()}%",
                        accentColor = BlofeldCyan
                    )

                    // Chorus
                    SynthSlider(
                        label = "Chorus Estéreo (Abertura)",
                        value = chorusDepth,
                        onValueChange = { viewModel.setChorusDepth(it) },
                        displayValue = "${(chorusDepth * 100).toInt()}%",
                        accentColor = SerumPurple
                    )

                    // Pan 360
                    SynthSlider(
                        label = "Panoramização 360° (L <-> R)",
                        value = pan360,
                        onValueChange = { viewModel.setPan360(it) },
                        valueRange = -1.0f..1.0f,
                        displayValue = when {
                            pan360 < -0.1f -> "L ${(-pan360 * 100).toInt()}%"
                            pan360 > 0.1f -> "R ${(pan360 * 100).toInt()}%"
                            else -> "CENTRO"
                        },
                        accentColor = MintGreen
                    )
                }
            }
        }

        // 8. CONTROLES DE PITCH BEND & MOD WHEEL TÁTEIS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CONTROLES DE PERFORMANCE (PITCH BEND & MOD WHEEL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PitchBendWheelController(
                            semitones = pitchBend,
                            onValueChange = { viewModel.setPitchBend(it) }
                        )

                        ModWheelController(
                            value = modWheel,
                            onValueChange = { viewModel.setModWheel(it) }
                        )

                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Arraste para cima/baixo:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "• Pitch Bend: +/- 2 semitons",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• Mod Wheel: abre filtro e shimmer",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 9. VOLUME POR BANCO / SEÇÃO
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "VOLUME INDIVIDUAL POR SEÇÃO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SynthSlider("Pads", volPads, { viewModel.setCategoryVolume(SoundCategory.PADS, it) }, accentColor = SerumPurple)
                    SynthSlider("Ambient", volAmbient, { viewModel.setCategoryVolume(SoundCategory.AMBIENT, it) }, accentColor = BlofeldCyan)
                    SynthSlider("Strings", volStrings, { viewModel.setCategoryVolume(SoundCategory.STRINGS, it) }, accentColor = PastelPink)
                    SynthSlider("Progressive", volProgressive, { viewModel.setCategoryVolume(SoundCategory.PROGRESSIVE, it) }, accentColor = MintGreen)
                    SynthSlider("Worship FX", volFx, { viewModel.setCategoryVolume(SoundCategory.WORSHIP_FX, it) }, accentColor = AmberWarm)
                    SynthSlider("Choral Pads", volChoral, { viewModel.setCategoryVolume(SoundCategory.CHORAL, it) }, accentColor = BlofeldCyan)
                }
            }
        }
    }
}
