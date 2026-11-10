package com.m3uplayer.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m3uplayer.tv.domain.model.Channel
import com.m3uplayer.tv.ui.components.ChannelCard
import com.m3uplayer.tv.ui.theme.*

@Composable
fun SearchScreen(
    searchResults: List<Channel>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiPrefs = LocalUiPreferences.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(uiPrefs.theme.bgMain)
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Global Search: channel, number, group, country, tvg-id...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = uiPrefs.accentColor.primary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = uiPrefs.accentColor.primary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = uiPrefs.theme.bgCard,
                    unfocusedContainerColor = uiPrefs.theme.bgCard
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (searchQuery.isNotBlank()) {
            Text(
                text = "Results across all playlists (${searchResults.size})",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No channels found matching \"$searchQuery\"",
                        color = TextSecondary,
                        fontSize = 15.sp
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
                    items(searchResults) { channel ->
                        ChannelCard(
                            channel = channel,
                            showPlaylistBadge = true,
                            onSelect = onSelectChannel,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = uiPrefs.accentColor.glow,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Type channel name, number, country, or category",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
