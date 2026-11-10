package com.m3uplayer.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.m3uplayer.tv.data.model.HiddenCategoryEntity
import com.m3uplayer.tv.data.model.HiddenChannelEntity
import com.m3uplayer.tv.domain.model.Playlist
import com.m3uplayer.tv.ui.theme.*
import com.m3uplayer.tv.util.TimeUtils

@Composable
fun SettingsScreen(
    playlists: List<Playlist>,
    activePlaylistId: String?,
    autoRefreshHours: Int,
    hiddenChannels: List<HiddenChannelEntity>,
    hiddenCategories: List<HiddenCategoryEntity>,
    uiPreferences: UiPreferences,
    welcomeAudioVolume: Int,
    onUpdateWelcomeAudioVolume: (Int) -> Unit,
    welcomeAudioEnabled: Boolean,
    onToggleWelcomeAudioEnabled: (Boolean) -> Unit,
    onUpdateUiPreferences: (UiPreferences) -> Unit,
    onSetAutoRefreshHours: (Int) -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onRefreshPlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onUnhideChannel: (String) -> Unit,
    onUnhideCategory: (String) -> Unit,
    onUnhideAllChannels: () -> Unit,
    onUnhideAllCategories: () -> Unit,
    onClearFavorites: () -> Unit,
    onClearRecentlyWatched: () -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showHiddenChannelsDialog by remember { mutableStateOf(false) }
    var showHiddenCategoriesDialog by remember { mutableStateOf(false) }
    var showVolumeDialog by remember { mutableStateOf(false) }
    val activePlaylist = playlists.find { it.id == activePlaylistId }

    val autoRefreshOptions = listOf(
        0 to "Off (Manual only)",
        1 to "Every 1 hour",
        3 to "Every 3 hours",
        6 to "Every 6 hours",
        12 to "Every 12 hours",
        24 to "Every 24 hours"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(uiPreferences.theme.bgMain)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Appearance & Customization Section
            item {
                Text(
                    text = "APPEARANCE & THEME",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme Selector
                    Column {
                        Text("Theme", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTheme.values().forEach { th ->
                                val isSel = uiPreferences.theme == th
                                Button(
                                    onClick = { onUpdateUiPreferences(uiPreferences.copy(theme = th)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) uiPreferences.accentColor.primary else Color(0xFF1E2433)
                                    )
                                ) {
                                    Text(th.label, color = if (isSel) Color.White else TextPrimary)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF222B3D))

                    // Accent Color Selector
                    Column {
                        Text("Accent Color", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AccentColor.values().forEach { acc ->
                                val isSel = uiPreferences.accentColor == acc
                                Button(
                                    onClick = { onUpdateUiPreferences(uiPreferences.copy(accentColor = acc)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) acc.primary else Color(0xFF1E2433)
                                    )
                                ) {
                                    Text(acc.label, color = Color.White)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF222B3D))

                    // Card Size & Logo Visibility
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Card Size", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CardSize.values().forEach { sz ->
                                    val isSel = uiPreferences.cardSize == sz
                                    Button(
                                        onClick = { onUpdateUiPreferences(uiPreferences.copy(cardSize = sz)) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSel) uiPreferences.accentColor.primary else Color(0xFF1E2433)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(sz.label, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Channel Logos", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onUpdateUiPreferences(uiPreferences.copy(showLogos = !uiPreferences.showLogos)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiPreferences.showLogos) uiPreferences.accentColor.primary else Color(0xFF1E2433)
                                )
                            ) {
                                Text(if (uiPreferences.showLogos) "Show Logos" else "Hide Logos")
                            }
                        }
                    }
                }
            }

            // Personalization: Welcome Experience & Audio
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PERSONALIZATION — WELCOME EXPERIENCE",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Welcome Audio Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Welcome Audio", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Play theme audio on welcome screen startup", color = TextSecondary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onToggleWelcomeAudioEnabled(!welcomeAudioEnabled) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (welcomeAudioEnabled) uiPreferences.accentColor.primary else Color(0xFF1E2433)
                            )
                        ) {
                            Text(if (welcomeAudioEnabled) "Enabled" else "Muted")
                        }
                    }

                    HorizontalDivider(color = Color(0xFF222B3D))

                    // Welcome Audio Volume (Range 0% - 200%, default 150%, step 10%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Welcome Audio Volume", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Adjust startup audio level (default 150% boost)", color = TextSecondary, fontSize = 11.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val next = (welcomeAudioVolume - 10).coerceAtLeast(0)
                                    onUpdateWelcomeAudioVolume(next)
                                },
                                enabled = welcomeAudioVolume > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("-10%", fontSize = 13.sp)
                            }

                            Button(
                                onClick = { showVolumeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = uiPreferences.accentColor.primary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("$welcomeAudioVolume%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val next = (welcomeAudioVolume + 10).coerceAtMost(200)
                                    onUpdateWelcomeAudioVolume(next)
                                },
                                enabled = welcomeAudioVolume < 200,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("+10%", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Hidden Channels & Categories Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "HIDDEN CONTENT",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hidden Channels (${hiddenChannels.size})", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Channels hidden from the home browser", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showHiddenChannelsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433))
                    ) {
                        Text("Manage Channels", color = uiPreferences.accentColor.glow)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hidden Categories (${hiddenCategories.size})", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Categories hidden from the left sidebar", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showHiddenCategoriesDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433))
                    ) {
                        Text("Manage Categories", color = uiPreferences.accentColor.glow)
                    }
                }
            }

            // Playlist Refresh Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PLAYLIST REFRESH & SYNC",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Refresh playlist", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Last updated: ${TimeUtils.formatRelativeTime(activePlaylist?.lastRefreshed ?: 0)}",
                                color = uiPreferences.accentColor.glow,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { activePlaylist?.let { onRefreshPlaylist(it.id) } },
                            enabled = activePlaylist?.sourceType == "url" && !isRefreshing,
                            colors = ButtonDefaults.buttonColors(containerColor = uiPreferences.accentColor.primary)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Refreshing...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Refresh Now")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF222B3D))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showIntervalDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto refresh", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            val currentLabel = autoRefreshOptions.find { it.first == autoRefreshHours }?.second ?: "Every 6 hours"
                            Text(currentLabel, color = TextSecondary, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showIntervalDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = uiPreferences.accentColor.glow)
                        ) {
                            Text("Change Interval")
                        }
                    }
                }
            }

            // All Playlists Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ALL PLAYLISTS (${playlists.size})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(playlists) { pl ->
                val isActive = pl.id == activePlaylistId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) Color(0xFF172033) else uiPreferences.theme.bgCard)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pl.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active",
                                    color = uiPreferences.accentColor.glow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color(0xFF1E2433), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${pl.channelCount} channels • ${pl.sourceType} • Last updated: ${TimeUtils.formatRelativeTime(pl.lastRefreshed)}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isActive) {
                            Button(
                                onClick = { onSelectPlaylist(pl.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = uiPreferences.accentColor.primary)
                            ) {
                                Text("Switch")
                            }
                        }
                        if (pl.sourceType == "url") {
                            IconButton(onClick = { onRefreshPlaylist(pl.id) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                            }
                        }
                        IconButton(onClick = { onDeletePlaylist(pl.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed)
                        }
                    }
                }
            }

            // Data & Actions Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DATA MANAGEMENT",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clear Marked Favorites", color = TextPrimary, fontSize = 14.sp)
                    Button(
                        onClick = onClearFavorites,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433))
                    ) {
                        Text("Clear", color = LiveRed)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clear Recently Watched Channels", color = TextPrimary, fontSize = 14.sp)
                    Button(
                        onClick = onClearRecentlyWatched,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433))
                    ) {
                        Text("Clear", color = LiveRed)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33DC2626))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reset All Data", color = LiveRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Deletes all stored playlists, favorites, and history", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = onClearAllData,
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                    ) {
                        Text("Reset All", color = Color.White)
                    }
                }
            }

            // About Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(uiPreferences.theme.bgCard)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.m3uplayer.tv.R.drawable.app_logo),
                        contentDescription = "Mana TV Logo",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mana TV (Android TV Native) v2.0.0",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Powered by AndroidX Media3 (ExoPlayer) with full HLS streaming, multi-audio, subtitle selection, and Room persistence. 100% local with zero tracking.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Manage Hidden Channels Modal
    if (showHiddenChannelsDialog) {
        Dialog(onDismissRequest = { showHiddenChannelsDialog = false }) {
            Box(
                modifier = Modifier
                    .width(500.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(uiPreferences.theme.bgCard)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hidden Channels (${hiddenChannels.size})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showHiddenChannelsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (hiddenChannels.isEmpty()) {
                        Text("No channels are currently hidden.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 24.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
                            items(hiddenChannels) { ch ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131822), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ch.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(ch.groupTitle, color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { onUnhideChannel(ch.channelId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = uiPreferences.accentColor.primary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Restore", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = onUnhideAllChannels,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433))
                            ) {
                                Text("Restore All Channels", color = uiPreferences.accentColor.glow)
                            }
                        }
                    }
                }
            }
        }
    }

    // Manage Hidden Categories Modal
    if (showHiddenCategoriesDialog) {
        Dialog(onDismissRequest = { showHiddenCategoriesDialog = false }) {
            Box(
                modifier = Modifier
                    .width(500.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(uiPreferences.theme.bgCard)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hidden Categories (${hiddenCategories.size})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showHiddenCategoriesDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (hiddenCategories.isEmpty()) {
                        Text("No categories are currently hidden.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 24.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
                            items(hiddenCategories) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131822), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat.categoryName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Button(
                                        onClick = { onUnhideCategory(cat.categoryName) },
                                        colors = ButtonDefaults.buttonColors(containerColor = uiPreferences.accentColor.primary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Restore", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = onUnhideAllCategories,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2433))
                            ) {
                                Text("Restore All Categories", color = uiPreferences.accentColor.glow)
                            }
                        }
                    }
                }
            }
        }
    }

    // Auto Refresh Interval Dialog
    if (showIntervalDialog) {
        Dialog(onDismissRequest = { showIntervalDialog = false }) {
            Box(
                modifier = Modifier
                    .width(400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(uiPreferences.theme.bgCard)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto Refresh Interval", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showIntervalDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    autoRefreshOptions.forEach { (hours, label) ->
                        val isSelected = autoRefreshHours == hours
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1D283A) else Color(0xFF131822))
                                .clickable {
                                    onSetAutoRefreshHours(hours)
                                    showIntervalDialog = false
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = uiPreferences.accentColor.glow, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Welcome Audio Volume Selection Dialog
    if (showVolumeDialog) {
        Dialog(onDismissRequest = { showVolumeDialog = false }) {
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(uiPreferences.theme.bgCard)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Welcome Audio Volume", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showVolumeDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val volumePresets = listOf(
                        0 to "0% (Muted)",
                        50 to "50% (Half volume)",
                        100 to "100% (Original playback level)",
                        150 to "150% (Default +50% Boost)",
                        180 to "180% (High Boost)",
                        200 to "200% (Maximum Configured Boost)"
                    )

                    volumePresets.forEach { (vol, label) ->
                        val isSelected = welcomeAudioVolume == vol
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1D283A) else Color(0xFF131822))
                                .clickable {
                                    onUpdateWelcomeAudioVolume(vol)
                                    showVolumeDialog = false
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = uiPreferences.accentColor.glow, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
