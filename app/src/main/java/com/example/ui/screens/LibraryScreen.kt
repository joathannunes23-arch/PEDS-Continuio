package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SoundFontManager
import com.example.model.Preset
import com.example.model.SoundCategory
import com.example.ui.WorshipViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: WorshipViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val presets by viewModel.displayedPresets.collectAsState()
    val activeNote by viewModel.activeRootNote.collectAsState()
    val installedSf2 by viewModel.soundFontManager.installedSoundFonts.collectAsState()

    val context = LocalContext.current
    var showSf2HelpDialog by remember { mutableStateOf(false) }

    // File Picker for SF2 SoundFont Import
    val sf2Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "soundfont.sf2"
            viewModel.importSoundFont(uri, fileName)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SynthDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // SCREEN TITLE & SF2 ACTIONS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SOUND LIBRARY",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "6 Seções • 48 Presets • Importador SF2",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Help Info SF2
                    IconButton(
                        onClick = { showSf2HelpDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SynthPanelSurface)
                            .testTag("sf2_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ajuda SF2",
                            tint = BlofeldCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Import SF2 Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SerumPurple.copy(alpha = 0.2f))
                            .border(1.dp, SerumPurple, RoundedCornerShape(20.dp))
                            .clickable {
                                // Request .sf2 or any file
                                sf2Launcher.launch("*/*")
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("import_sf2_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Importar SF2",
                                tint = SerumPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "IMPORTAR .SF2",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // SEARCH BAR
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Buscar timbre, autor ou descrição...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SynthChassisSurface,
                    unfocusedContainerColor = SynthChassisSurface,
                    focusedBorderColor = BlofeldCyan,
                    unfocusedBorderColor = SynthBorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input")
            )
        }

        // 6 SECTION TABS / FILTER CHIPS
        item {
            val categories = listOf(
                Pair(SoundCategory.PADS, Icons.Default.Audiotrack),
                Pair(SoundCategory.AMBIENT, Icons.Default.Nightlight),
                Pair(SoundCategory.STRINGS, Icons.Default.MusicNote),
                Pair(SoundCategory.PROGRESSIVE, Icons.Default.Speed),
                Pair(SoundCategory.WORSHIP_FX, Icons.Default.GraphicEq),
                Pair(SoundCategory.CHORAL, Icons.Default.RecordVoiceOver)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { (cat, icon) ->
                    val isSelected = selectedCategory == cat
                    val chipColor = when (cat) {
                        SoundCategory.PADS -> SerumPurple
                        SoundCategory.AMBIENT -> BlofeldCyan
                        SoundCategory.STRINGS -> PastelPink
                        SoundCategory.PROGRESSIVE -> MintGreen
                        SoundCategory.WORSHIP_FX -> AmberWarm
                        SoundCategory.CHORAL -> BlofeldCyanLight
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedCategory.value = cat },
                        label = {
                            Text(
                                text = cat.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = cat.displayName,
                                tint = if (isSelected) chipColor else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.2f),
                            selectedLabelColor = TextPrimary,
                            containerColor = SynthChassisSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) chipColor else SynthBorderSubtle,
                            selectedBorderColor = chipColor,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("filter_chip_${cat.name}")
                    )
                }
            }
        }

        // SECTION BANNER DESCRIPTION
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SynthChassisSurfaceVariant)
                    .border(1.dp, SynthBorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MintGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedCategory.description,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // PRESETS LIST
        if (presets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum preset encontrado para a busca.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(presets) { preset ->
                val isCurrent = currentPreset.id == preset.id

                PresetItemCard(
                    preset = preset,
                    isCurrent = isCurrent,
                    isPlaying = isPlaying && isCurrent,
                    onSelect = {
                        viewModel.applyPreset(preset)
                        if (!isPlaying) viewModel.togglePadPlay(activeNote)
                    },
                    onTogglePlay = {
                        if (isCurrent) {
                            viewModel.togglePadPlay(activeNote)
                        } else {
                            viewModel.applyPreset(preset)
                            viewModel.togglePadPlay(activeNote)
                        }
                    }
                )
            }
        }
    }

    // SF2 HELP DIALOG
    if (showSf2HelpDialog) {
        AlertDialog(
            onDismissRequest = { showSf2HelpDialog = false },
            title = {
                Text(
                    text = "Como Importar Bancos SoundFont (.sf2)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = SoundFontManager.SF2_INSTRUCTIONS,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    if (installedSf2.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bancos SoundFont instalados:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlofeldCyan
                        )
                        installedSf2.forEach { sf ->
                            Text(
                                text = "• ${sf.displayName} (${(sf.sizeBytes / 1024 / 1024)} MB)",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSf2HelpDialog = false }) {
                    Text("Entendi", color = BlofeldCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SynthChassisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PresetItemCard(
    preset: Preset,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onTogglePlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) BlofeldCyan else SynthBorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .testTag("preset_item_${preset.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFF151F33) else SynthChassisSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) BlofeldCyan else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SynthPanelSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Banco ${preset.bankIndex}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Por ${preset.author}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SerumPurpleLight
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = preset.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Synth tags
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SynthTag("Cutoff ${(preset.cutoffHz / 1000).toInt()}k")
                    SynthTag("Reverb ${(preset.reverbWet * 100).toInt()}%")
                    if (preset.shimmerLevel > 0.4f) SynthTag("Shimmer ✨", MintGreen)
                    if (preset.isArpEnabled) SynthTag("Arp ⚡", AmberWarm)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { onTogglePlay() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) MintGreen else if (isCurrent) BlofeldCyan else SynthPanelSurface)
                    .testTag("preset_play_${preset.id}")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Parar" else "Tocar",
                    tint = if (isPlaying || isCurrent) SynthDarkBackground else TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SynthTag(text: String, tagColor: Color = TextMuted) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SynthChassisSurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = tagColor
        )
    }
}
