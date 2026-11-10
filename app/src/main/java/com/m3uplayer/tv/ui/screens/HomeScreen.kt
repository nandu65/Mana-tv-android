package com.m3uplayer.tv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.m3uplayer.tv.R
import com.m3uplayer.tv.domain.model.Channel
import com.m3uplayer.tv.domain.model.Playlist
import com.m3uplayer.tv.ui.components.CategoryNav
import com.m3uplayer.tv.ui.components.CategorySidebar
import com.m3uplayer.tv.ui.components.ChannelCard
import com.m3uplayer.tv.ui.theme.*
import com.m3uplayer.tv.util.TimeUtils

@Composable
fun HomeScreen(
    playlists: List<Playlist>,
    activePlaylist: Playlist?,
    channels: List<Channel>,
    favorites: List<Channel>,
    recentlyWatched: List<Channel>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onHideChannel: (Channel) -> Unit,
    onHideCategory: (String) -> Unit,
    onRemoveRecentlyWatched: (String) -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onRefreshPlaylist: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSports: () -> Unit = {},
    onOpenAddPlaylist: () -> Unit,
    onOpenSettings: () -> Unit,
    onLoadSample: () -> Unit,
    welcomeAudioVolume: Int = 150,
    welcomeAudioEnabled: Boolean = true,
    isRefreshing: Boolean = false,
    isDefaultSeeding: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiPrefs = LocalUiPreferences.current
    var showPlaylistSwitcherDialog by remember { mutableStateOf(false) }
    var contextChannel by remember { mutableStateOf<Channel?>(null) }
    var contextCategoryToHide by remember { mutableStateOf<String?>(null) }
    var favoriteCategoryFilter by remember { mutableStateOf("All") }

    if (playlists.isEmpty()) {
        if (isDefaultSeeding) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF090B10)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(54.dp), strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Loading default Mana TV playlist...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "https://iptv-org.github.io/iptv/index.m3u",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            return
        }

        WelcomeScreen(
            onOpenAddPlaylist = onOpenAddPlaylist,
            onLoadSample = onLoadSample,
            welcomeAudioVolume = welcomeAudioVolume,
            welcomeAudioEnabled = welcomeAudioEnabled,
            modifier = modifier
        )
        return
    }

    // Build Category items dynamically
    val categoryNavList = remember(channels, favorites, recentlyWatched) {
        val groups = channels.groupBy { it.group }
        val list = mutableListOf(
            CategoryNav("All Channels", channels.size, Icons.Default.Tv),
            CategoryNav("Favorites", favorites.size, Icons.Default.Star)
        )
        if (recentlyWatched.isNotEmpty()) {
            list.add(CategoryNav("Continue Watching", recentlyWatched.size, Icons.Default.History))
        }
        groups.forEach { (grp, chList) ->
            list.add(CategoryNav(grp, chList.size, Icons.Default.Folder))
        }
        list
    }

    val displayChannels = remember(selectedCategory, channels, favorites, recentlyWatched, favoriteCategoryFilter) {
        when (selectedCategory) {
            "All Channels" -> channels
            "Favorites" -> {
                if (favoriteCategoryFilter == "All") favorites
                else favorites.filter { it.group.equals(favoriteCategoryFilter, ignoreCase = true) }
            }
            "Continue Watching" -> recentlyWatched
            else -> channels.filter { it.group == selectedCategory }
        }
    }

    val favoriteCategories = remember(favorites) {
        listOf("All") + favorites.map { it.group }.distinct().sorted()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(uiPrefs.theme.bgMain)
    ) {
        // TV Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090C12))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: App Logo, Title, and relative updated time indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Mana TV Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Mana TV",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                activePlaylist?.let { pl ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${TimeUtils.formatShortRelative(pl.lastRefreshed)}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Right: Live Sports + Playlist Switcher + Search + Add + Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Sports Button
                Button(
                    onClick = onOpenSports,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Live Sports",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Sports",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Playlist Switcher button
                Button(
                    onClick = { showPlaylistSwitcherDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161E2E)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Playlists",
                        tint = uiPrefs.accentColor.glow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activePlaylist?.name ?: "Switch Playlist",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
                IconButton(onClick = onOpenAddPlaylist) {
                    Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = Color.White)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        // Main 2-Column Content: Sidebar + Channel Grid
        Row(modifier = Modifier.fillMaxSize()) {
            CategorySidebar(
                categories = categoryNavList,
                selectedCategory = selectedCategory,
                onSelectCategory = {
                    onSelectCategory(it)
                    favoriteCategoryFilter = "All"
                },
                onHideCategory = { catName ->
                    contextCategoryToHide = catName
                },
                onOpenSports = onOpenSports
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Continue Watching Section (Top row on "All Channels")
                if (recentlyWatched.isNotEmpty() && selectedCategory == "All Channels") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTINUE WATCHING",
                            color = uiPrefs.accentColor.glow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hold OK for options",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(recentlyWatched) { channel ->
                            ChannelCard(
                                channel = channel,
                                onSelect = onSelectChannel,
                                onToggleFavorite = onToggleFavorite,
                                onLongClick = { contextChannel = it }
                            )
                        }
                    }
                }

                // Favorites Quick Row on "All Channels"
                if (favorites.isNotEmpty() && selectedCategory == "All Channels") {
                    Text(
                        text = "FAVORITES (${favorites.size})",
                        color = FavoriteYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(favorites) { channel ->
                            ChannelCard(
                                channel = channel,
                                onSelect = onSelectChannel,
                                onToggleFavorite = onToggleFavorite,
                                onLongClick = { contextChannel = it }
                            )
                        }
                    }
                }

                // Favorites Category Filter Chips when inside Favorites view
                if (selectedCategory == "Favorites" && favoriteCategories.size > 2) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(favoriteCategories) { cat ->
                            val isChipSelected = favoriteCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChipSelected) uiPrefs.accentColor.primary else Color(0xFF161E2E))
                                    .clickable { favoriteCategoryFilter = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isChipSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Channel Grid Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedCategory (${displayChannels.size})",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hold OK to Favorite / Hide",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (displayChannels.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selectedCategory) {
                                "Favorites" -> "No favorite channels added yet."
                                "Continue Watching" -> "No recently watched channels."
                                else -> "No channels in this category."
                            },
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    val gridWidth = when (uiPrefs.cardSize) {
                        CardSize.COMPACT -> 145.dp
                        CardSize.NORMAL -> 175.dp
                        CardSize.LARGE -> 210.dp
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(gridWidth),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayChannels) { channel ->
                            ChannelCard(
                                channel = channel,
                                onSelect = onSelectChannel,
                                onToggleFavorite = onToggleFavorite,
                                onLongClick = { contextChannel = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // Channel Context Options Dialog (Long-Press)
    contextChannel?.let { ch ->
        Dialog(onDismissRequest = { contextChannel = null }) {
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(uiPrefs.theme.bgCard)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = ch.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${ch.group} • Channel Options",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle Favorite Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF161E2E))
                            .clickable {
                                onToggleFavorite(ch.id, ch.isFavorite)
                                contextChannel = null
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (ch.isFavorite) Icons.Default.StarBorder else Icons.Default.Star,
                            contentDescription = null,
                            tint = FavoriteYellow
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (ch.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hide Channel Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF161E2E))
                            .clickable {
                                onHideChannel(ch)
                                contextChannel = null
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = LiveRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hide Channel",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Restore anytime in Settings → Hidden Channels",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (recentlyWatched.any { it.id == ch.id }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Remove from Continue Watching
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF161E2E))
                                .clickable {
                                    onRemoveRecentlyWatched(ch.id)
                                    contextChannel = null
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Remove from Continue Watching",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { contextChannel = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Category Hide Confirmation Dialog
    contextCategoryToHide?.let { catName ->
        Dialog(onDismissRequest = { contextCategoryToHide = null }) {
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(uiPrefs.theme.bgCard)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Hide Category?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hide all channels in \"$catName\"? You can restore this category anytime in Settings → Hidden Categories.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { contextCategoryToHide = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onHideCategory(catName)
                                contextCategoryToHide = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                        ) {
                            Text("Hide Category")
                        }
                    }
                }
            }
        }
    }

    // Playlist Switcher Modal Dialog
    if (showPlaylistSwitcherDialog) {
        Dialog(onDismissRequest = { showPlaylistSwitcherDialog = false }) {
            Box(
                modifier = Modifier
                    .width(520.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(uiPrefs.theme.bgCard)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = uiPrefs.accentColor.glow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Switch Playlist",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showPlaylistSwitcherDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(playlists) { pl ->
                            val isActive = pl.id == activePlaylist?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) Color(0xFF182338) else Color(0xFF121622))
                                    .clickable {
                                        onSelectPlaylist(pl.id)
                                        showPlaylistSwitcherDialog = false
                                    }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pl.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Active",
                                                color = uiPrefs.accentColor.glow,
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
                                        text = "${pl.channelCount} channels • Last updated: ${TimeUtils.formatRelativeTime(pl.lastRefreshed)}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (pl.sourceType == "url") {
                                        IconButton(
                                            onClick = { onRefreshPlaylist(pl.id) }
                                        ) {
                                            if (isRefreshing) {
                                                CircularProgressIndicator(
                                                    color = uiPrefs.accentColor.glow,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Refresh Playlist",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (!isActive) {
                                        Button(
                                            onClick = {
                                                onSelectPlaylist(pl.id)
                                                showPlaylistSwitcherDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = uiPrefs.accentColor.primary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Switch", fontSize = 12.sp)
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active Playlist",
                                            tint = uiPrefs.accentColor.glow,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                showPlaylistSwitcherDialog = false
                                onOpenAddPlaylist()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = uiPrefs.accentColor.glow),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Another Playlist")
                        }
                    }
                }
            }
        }
    }
}
