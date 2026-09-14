package com.example.ui.screens

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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.example.model.MinorScaleType
import com.example.model.RootNote
import com.example.model.Tonality
import com.example.model.WorshipVoicing
import com.example.ui.WorshipViewModel
import com.example.ui.components.SynthPadButton
import com.example.ui.components.SynthSlider
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
fun TonesScreen(
    viewModel: WorshipViewModel,
    modifier: Modifier = Modifier
) {
    val tonality by viewModel.tonality.collectAsState()
    val minorScaleType by viewModel.minorScaleType.collectAsState()
    val activeNote by viewModel.activeRootNote.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val voicing by viewModel.worshipVoicing.collectAsState()
    val octaveShift by viewModel.octaveShift.collectAsState()
    val tuningBaseHz by viewModel.tuningBaseHz.collectAsState()
    val fineDetune by viewModel.fineDetuneCents.collectAsState()

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
                    text = "TONES & HARMONIA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Transposição Dinâmica • Modo Maior / Menor • Voicings de Louvor",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // BIG TONALITY SWITCHER (MAJOR vs MINOR)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, if (tonality == Tonality.MAJOR) MintGreen else SerumPurple, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SynthChassisSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "MODO TONALIDADE (TRANSPOSIÇÃO EM TEMPO REAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // MAJOR BUTTON
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (tonality == Tonality.MAJOR) Color(0xFF064E3B) else SynthPanelSurface)
                                .border(
                                    width = if (tonality == Tonality.MAJOR) 2.dp else 1.dp,
                                    color = if (tonality == Tonality.MAJOR) MintGreen else SynthBorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setTonality(Tonality.MAJOR) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "MAJOR (MAIOR)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (tonality == Tonality.MAJOR) MintGreen else TextSecondary
                                )
                                Text(
                                    text = "Brilho & Celebração",
                                    fontSize = 10.sp,
                                    color = if (tonality == Tonality.MAJOR) TextPrimary else TextMuted
                                )
                            }
                        }

                        // MINOR BUTTON
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (tonality == Tonality.MINOR) Color(0xFF3B0764) else SynthPanelSurface)
                                .border(
                                    width = if (tonality == Tonality.MINOR) 2.dp else 1.dp,
                                    color = if (tonality == Tonality.MINOR) SerumPurple else SynthBorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setTonality(Tonality.MINOR) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "MINOR (MENOR)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (tonality == Tonality.MINOR) SerumPurple else TextSecondary
                                )
                                Text(
                                    text = "Profundidade & Quebranto",
                                    fontSize = 10.sp,
                                    color = if (tonality == Tonality.MINOR) TextPrimary else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (tonality == Tonality.MAJOR)
                            "O modo MAIOR produz harmonia aberta (fundamental, 2ª/9ª, 5ª e oitava) com sustentação contínua."
                        else
                            "O modo MENOR transpõe automaticamente a terça menor e a escala em tempo real sem interrupção do som.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // ESCALA MENOR ESCOLHA (HARMONIC MINOR vs MELODIC MINOR vs NATURAL)
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
                            text = "TIPO DE ESCALA MENOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = minorScaleType.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SerumPurpleLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MinorScaleType.values().forEach { scaleType ->
                        val isSelected = minorScaleType == scaleType
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF221538) else SynthChassisSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SerumPurple else SynthBorderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setMinorScaleType(scaleType) }
                                .padding(12.dp)
                                .testTag("scale_type_${scaleType.name}")
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) SerumPurple else TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = scaleType.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = scaleType.description,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // WORSHIP VOICING SELECTOR
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
                        text = "VOICING DE ADORAÇÃO (ABERTURA HARMÔNICA)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    WorshipVoicing.values().forEach { v ->
                        val isSelected = voicing == v
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF132A3E) else SynthChassisSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) BlofeldCyan else SynthBorderSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setWorshipVoicing(v) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = v.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BlofeldCyan else TextPrimary
                                )
                                if (isSelected) {
                                    Text(
                                        text = "ATIVO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MintGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // OCTAVE SHIFT & PITCH TUNING
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
                        text = "TRANSPOSIÇÃO DE OITAVA & AFINAÇÃO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Octave Buttons (-1, 0, +1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair(-1, "-1 (Sub Grave)"),
                            Pair(0, "0 (Normal)"),
                            Pair(1, "+1 (Agudo)")
                        ).forEach { (oct, label) ->
                            val isSelected = octaveShift == oct
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BlofeldCyan.copy(alpha = 0.2f) else SynthPanelSurface)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) BlofeldCyan else SynthBorderSubtle,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setOctaveShift(oct) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BlofeldCyan else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 440Hz vs 432Hz switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Frequência de Concerto",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (tuningBaseHz == 440f) BlofeldCyan.copy(alpha = 0.25f) else SynthPanelSurface)
                                    .border(1.dp, if (tuningBaseHz == 440f) BlofeldCyan else SynthBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setTuningBaseHz(440f) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("440 Hz (Padrão)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (tuningBaseHz == 432f) MintGreen.copy(alpha = 0.25f) else SynthPanelSurface)
                                    .border(1.dp, if (tuningBaseHz == 432f) MintGreen else SynthBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setTuningBaseHz(432f) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("432 Hz (Paz / Cura)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fine Detune
                    SynthSlider(
                        label = "Microtonal Detune",
                        value = fineDetune,
                        onValueChange = { viewModel.setFineDetuneCents(it) },
                        valueRange = -50f..50f,
                        displayValue = "${fineDetune.toInt()} cents",
                        accentColor = MintGreen
                    )
                }
            }
        }

        // QUICK PAD TRIGGER GRID
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SELECIONAR NOTA FUNDAMENTAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val row1 = listOf(RootNote.C, RootNote.C_SHARP, RootNote.D, RootNote.D_SHARP)
                val row2 = listOf(RootNote.E, RootNote.F, RootNote.F_SHARP, RootNote.G)
                val row3 = listOf(RootNote.G_SHARP, RootNote.A, RootNote.A_SHARP, RootNote.B)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    }
}
