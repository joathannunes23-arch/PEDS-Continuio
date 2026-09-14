package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundCategory
import com.example.model.Tonality
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TonesScreen
import com.example.ui.theme.BlofeldCyan
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SerumPurple
import com.example.ui.theme.SynthBorderGlow
import com.example.ui.theme.SynthBorderSubtle
import com.example.ui.theme.SynthChassisSurface
import com.example.ui.theme.SynthDarkBackground
import com.example.ui.theme.SynthPanelSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class WorshipTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    TONES("Tones", Icons.Default.Tune),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun WorshipMainScreen(
    viewModel: WorshipViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(WorshipTab.HOME) }
    var isPerformanceModeOpen by remember { mutableStateOf(false) }

    val activeNote by viewModel.activeRootNote.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val tonality by viewModel.tonality.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val statusNotification by viewModel.statusNotification.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusNotification) {
        statusNotification?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusNotification()
        }
    }

    if (isPerformanceModeOpen) {
        PerformanceScreen(
            viewModel = viewModel,
            onClose = { isPerformanceModeOpen = false }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SynthDarkBackground,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                // Persistent Floating Mini-Player when navigating other tabs or playing
                AnimatedVisibility(
                    visible = currentTab != WorshipTab.HOME || isPlaying,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SynthChassisSurface)
                            .border(1.dp, SynthBorderGlow, RoundedCornerShape(12.dp))
                            .clickable { currentTab = WorshipTab.HOME }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (tonality == Tonality.MAJOR) MintGreen.copy(alpha = 0.2f) else SerumPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeNote.symbol,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (tonality == Tonality.MAJOR) MintGreen else SerumPurple
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = currentPreset.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${activeNote.symbol} ${tonality.label} • ${currentPreset.category.displayName}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.togglePadPlay() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) MintGreen else BlofeldCyan)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Parar" else "Tocar",
                                        tint = SynthDarkBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4-Tab Navigation Bar
                NavigationBar(
                    containerColor = SynthDarkBackground,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SynthBorderSubtle)
                ) {
                    WorshipTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BlofeldCyan,
                                selectedTextColor = BlofeldCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = Color(0xFF16253B)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                WorshipTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToLibrarySection = { category ->
                            viewModel.selectedCategory.value = category
                            currentTab = WorshipTab.LIBRARY
                        },
                        onOpenPerformanceMode = { isPerformanceModeOpen = true }
                    )
                }
                WorshipTab.LIBRARY -> {
                    LibraryScreen(viewModel = viewModel)
                }
                WorshipTab.TONES -> {
                    TonesScreen(viewModel = viewModel)
                }
                WorshipTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
