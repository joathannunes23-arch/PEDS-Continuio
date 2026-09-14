package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetLibrary
import com.example.model.Preset
import com.example.model.RootNote
import com.example.model.SoundCategory
import com.example.model.Tonality
import com.example.ui.WorshipViewModel
import com.example.ui.components.SynthPadButton
import com.example.ui.components.SynthSlider
import com.example.ui.components.TonalityHeaderButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.BlofeldCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PastelPink
import com.example.ui.theme.SerumPurple
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
fun HomeScreen(
    viewModel: WorshipViewModel,
    onNavigateToLibrarySection: (SoundCategory) -> Unit,
    onOpenPerformanceMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeNote by viewModel.activeRootNote.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val tonality by viewModel.tonality.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val masterVol by viewModel.masterVolume.collectAsState()
    val waveform by viewModel.visualizerWaveform.collectAsState()
    val rms by viewModel.visualizerRms.collectAsState()
    val isRecording by viewModel.performanceRecorder.isRecording.collectAsState()
    val recSeconds by viewModel.performanceRecorder.elapsedSeconds.collectAsState()

    val notesList = RootNote.values().toList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SynthDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER: Branding & Prominent MAJOR / MINOR button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WORSHIP",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PADS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BlofeldCyan,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = "Ambientes & Pads de Louvor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }

                // Botão grande MAJOR / MINOR no canto superior direito (requisito explícito)
                TonalityHeaderButton(
                    tonality = tonality,
                    onToggle = { viewModel.toggleTonality() }
                )
            }
        }

        // WAVEFORM & AURA VISUALIZER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SynthBorderGlow, RoundedCornerShape(16.dp)),
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) MintGreen else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "PAD ATIVO: ${activeNote.symbol} ${tonality.label}" else "PAD EM PAUSA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlaying) MintGreen else TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Performance Fullscreen Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SynthPanelSurface)
                                .clickable { onOpenPerformanceMode() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("open_performance_mode_button")
                        ) {
                            Text(
                                text = "MODO PALCO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BlofeldCyanLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    WaveformVisualizer(
                        waveform = waveform,
                        rms = rms,
                        isPlaying = isPlaying,
                        tonality = tonality,
                        heightDp = 75
                    )

                    // Current Preset Ribbon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SynthChassisSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentPreset.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${currentPreset.category.displayName} • ${currentPreset.author}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.togglePadPlay() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) MintGreen else BlofeldCyan)
                                .testTag("main_play_stop_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Parar Pad" else "Tocar Pad",
                                tint = SynthDarkBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // QUICK PLAY: 5 presets mais usados
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUICK PLAY (5 MAIS USADOS)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Toque rápido",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(PresetLibrary.quickPlayPresets) { preset ->
                        val isCurrent = currentPreset.id == preset.id
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) Color(0xFF1E2640) else SynthChassisSurface)
                                .border(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) BlofeldCyan else SynthBorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.applyPreset(preset)
                                    if (!isPlaying) viewModel.togglePadPlay(activeNote)
                                }
                                .padding(12.dp)
                                .testTag("quick_play_${preset.id}")
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) MintGreen else TextMuted)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = preset.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) BlofeldCyan else TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = preset.category.displayName,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 12-TONE INTERACTIVE SYNTH PAD MATRIX
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MATRIZ DE PADS (12 NOTAS FUNDAMENTAIS)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Transposição Contínua",
                        fontSize = 11.sp,
                        color = MintGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4x3 Grid of 12 notes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val row1 = listOf(RootNote.C, RootNote.C_SHARP, RootNote.D, RootNote.D_SHARP)
                    val row2 = listOf(RootNote.E, RootNote.F, RootNote.F_SHARP, RootNote.G)
                    val row3 = listOf(RootNote.G_SHARP, RootNote.A, RootNote.A_SHARP, RootNote.B)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row1.forEach { note ->
                            SynthPadButton(
                                note = note,
                                isActive = isPlaying && activeNote == note,
                                tonality = tonality,
                                onClick = { viewModel.togglePadPlay(note) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row2.forEach { note ->
                            SynthPadButton(
                                note = note,
                                isActive = isPlaying && activeNote == note,
                                tonality = tonality,
                                onClick = { viewModel.togglePadPlay(note) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row3.forEach { note ->
                            SynthPadButton(
                                note = note,
                                isActive = isPlaying && activeNote == note,
                                tonality = tonality,
                                onClick = { viewModel.togglePadPlay(note) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // CARDS GRANDES DE CADA SEÇÃO (Requisito: "Cards grandes de cada seção com ícone")
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SEÇÕES DE SONORIDADES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val sections = listOf(
                    Triple(SoundCategory.PADS, Icons.Default.Audiotrack, SerumPurple),
                    Triple(SoundCategory.AMBIENT, Icons.Default.Nightlight, BlofeldCyan),
                    Triple(SoundCategory.STRINGS, Icons.Default.MusicNote, PastelPink),
                    Triple(SoundCategory.PROGRESSIVE, Icons.Default.Speed, MintGreen),
                    Triple(SoundCategory.WORSHIP_FX, Icons.Default.GraphicEq, AmberWarm),
                    Triple(SoundCategory.CHORAL, Icons.Default.RecordVoiceOver, BlofeldCyanLight)
                )

                // 2 columns grid for the 6 sections
                for (i in sections.indices step 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val first = sections[i]
                        SectionCard(
                            category = first.first,
                            icon = first.second,
                            accentColor = first.third,
                            onClick = { onNavigateToLibrarySection(first.first) },
                            modifier = Modifier.weight(1f)
                        )

                        if (i + 1 < sections.size) {
                            val second = sections[i + 1]
                            SectionCard(
                                category = second.first,
                                icon = second.second,
                                accentColor = second.third,
                                onClick = { onNavigateToLibrarySection(second.first) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // QUICK VOLUME & LIVE PERFORMANCE RECORDER
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = BlofeldCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MASTER VOLUME",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        // Quick Record button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRecording) Color(0xFF7F1D1D) else SynthPanelSurface)
                                .clickable { viewModel.toggleRecording() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("home_record_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isRecording) Color.Red else TextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRecording) "GRAVANDO (${recSeconds}s)" else "GRAVAR WAV",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRecording) Color.White else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SynthSlider(
                        label = "",
                        value = masterVol,
                        onValueChange = { viewModel.setMasterVolume(it) },
                        displayValue = "${(masterVol * 100).toInt()}%",
                        accentColor = BlofeldCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    category: SoundCategory,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SynthBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("section_card_${category.name}"),
        colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.displayName,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "8 BANCOS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }

            Column {
                Text(
                    text = category.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = category.description,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

val BlofeldCyanLight = Color(0xFF7DD3FC)
