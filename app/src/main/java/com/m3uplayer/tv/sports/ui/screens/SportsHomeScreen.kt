package com.m3uplayer.tv.sports.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m3uplayer.tv.sports.data.repository.SportsRepository
import com.m3uplayer.tv.sports.domain.model.*
import com.m3uplayer.tv.sports.ui.components.*
import com.m3uplayer.tv.ui.theme.LocalUiPreferences
import kotlinx.coroutines.launch

@Composable
fun SportsHomeScreen(
    sportsRepository: SportsRepository,
    onPlayStream: (streamUrl: String, title: String, logoUrl: String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiPrefs = LocalUiPreferences.current
    val coroutineScope = rememberCoroutineScope()

    val allEvents by sportsRepository.events.collectAsState()
    val allChannels by sportsRepository.channels.collectAsState()
    val isLoading by sportsRepository.isLoading.collectAsState()
    val error by sportsRepository.error.collectAsState()

    var selectedProvider by remember { mutableStateOf(SportsProvider.ALL) }

    // Dialog state for external fallback
    var dialogItem by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        if (allEvents.isEmpty()) {
            sportsRepository.refreshAll()
        }
    }

    // Filter events by selected provider
    val filteredEvents = remember(allEvents, selectedProvider) {
        if (selectedProvider == SportsProvider.ALL) allEvents
        else allEvents.filter { it.provider == selectedProvider }
    }

    val filteredChannels = remember(allChannels, selectedProvider) {
        if (selectedProvider == SportsProvider.ALL) allChannels
        else allChannels.filter { it.provider == selectedProvider }
    }

    // Section 1: Live Now
    val liveEvents = remember(filteredEvents) {
        filteredEvents.filter { it.status == EventStatus.LIVE }
    }

    // Section 2: Upcoming Matches
    val upcomingEvents = remember(filteredEvents) {
        filteredEvents
            .filter { it.status == EventStatus.UPCOMING }
            .sortedBy { it.startTimeEpochMs }
    }

    // Section 3: Cricket
    val cricketEvents = remember(filteredEvents) {
        filteredEvents.filter { it.category == SportsCategory.CRICKET }
    }

    // Section 4: Football
    val footballEvents = remember(filteredEvents) {
        filteredEvents.filter { it.category == SportsCategory.FOOTBALL }
    }

    // Section 5: Other Sports
    val otherSportsEvents = remember(filteredEvents) {
        filteredEvents.filter { it.category != SportsCategory.CRICKET && it.category != SportsCategory.FOOTBALL }
    }

    fun handleEventClick(event: SportsEvent) {
        if (event.status == EventStatus.COMPLETED) return
        if (event.status == EventStatus.LIVE && event.streamSupportedInApp && !event.streamUrl.isNullOrBlank()) {
            onPlayStream(event.streamUrl, event.title, event.posterUrl)
        } else {
            dialogItem = event
        }
    }

    fun handleChannelClick(channel: SportsChannel) {
        if (channel.streamSupportedInApp && !channel.streamUrl.isNullOrBlank()) {
            onPlayStream(channel.streamUrl, channel.name, channel.logoUrl)
        } else {
            dialogItem = channel
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(uiPrefs.theme.bgMain)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Header Row: Back button, Title & Provider filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141C2C))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE SPORTS",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "FanCode • SonyLIV • Tata Play • Willow",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Refresh Button
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                sportsRepository.refreshAll()
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141C2C))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = uiPrefs.accentColor.glow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Provider Filter Pills
                ProviderFilterBar(
                    selectedProvider = selectedProvider,
                    onSelectProvider = { selectedProvider = it }
                )
            }

            // Loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = uiPrefs.accentColor.primary)
                    }
                }
            }

            // Error state
            if (!isLoading && error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22EF4444))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = error ?: "Failed to load sports feeds",
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Empty state
            if (!isLoading && error == null && filteredEvents.isEmpty() && filteredChannels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matches or channels found for ${selectedProvider.displayName}",
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 1. Live Now Section
            if (liveEvents.isNotEmpty()) {
                item {
                    SectionHeader(title = "Live Now", count = liveEvents.size, isLive = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(liveEvents) { event ->
                            SportsEventCard(
                                event = event,
                                onSelectEvent = { handleEventClick(it) }
                            )
                        }
                    }
                }
            }

            // 2. Upcoming Matches Section
            if (upcomingEvents.isNotEmpty()) {
                item {
                    SectionHeader(title = "Upcoming Matches", count = upcomingEvents.size)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(upcomingEvents) { event ->
                            SportsEventCard(
                                event = event,
                                onSelectEvent = { handleEventClick(it) }
                            )
                        }
                    }
                }
            }

            // 3. Cricket Section
            if (cricketEvents.isNotEmpty()) {
                item {
                    SectionHeader(title = "Cricket Hub", count = cricketEvents.size)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cricketEvents) { event ->
                            SportsEventCard(
                                event = event,
                                onSelectEvent = { handleEventClick(it) }
                            )
                        }
                    }
                }
            }

            // 4. Football Section
            if (footballEvents.isNotEmpty()) {
                item {
                    SectionHeader(title = "Football Hub", count = footballEvents.size)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(footballEvents) { event ->
                            SportsEventCard(
                                event = event,
                                onSelectEvent = { handleEventClick(it) }
                            )
                        }
                    }
                }
            }

            // 5. Other Sports Section
            if (otherSportsEvents.isNotEmpty()) {
                item {
                    SectionHeader(title = "Other Sports", count = otherSportsEvents.size)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(otherSportsEvents) { event ->
                            SportsEventCard(
                                event = event,
                                onSelectEvent = { handleEventClick(it) }
                            )
                        }
                    }
                }
            }

            // 6. Sports Channels Section
            if (filteredChannels.isNotEmpty()) {
                item {
                    SectionHeader(title = "24/7 Sports Channels", count = filteredChannels.size)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredChannels) { ch ->
                            SportsChannelCard(
                                channel = ch,
                                onSelectChannel = { handleChannelClick(it) }
                            )
                        }
                    }
                }
            }
        }

        // External Provider Fallback Dialog
        dialogItem?.let { item ->
            when (item) {
                is SportsEvent -> {
                    ExternalProviderDialog(
                        itemTitle = item.title,
                        providerName = item.provider.displayName,
                        providerColorHex = item.provider.badgeColorHex,
                        appPackage = item.provider.appPackage,
                        webUrl = item.externalWebUrl,
                        deepLink = item.externalAppDeepLink,
                        onDismiss = { dialogItem = null }
                    )
                }
                is SportsChannel -> {
                    ExternalProviderDialog(
                        itemTitle = item.name,
                        providerName = item.provider.displayName,
                        providerColorHex = item.provider.badgeColorHex,
                        appPackage = item.provider.appPackage,
                        webUrl = item.externalWebUrl ?: item.provider.defaultWebUrl,
                        deepLink = item.externalAppDeepLink,
                        onDismiss = { dialogItem = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    isLive: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEF4444))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LIVE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = "$title ($count)",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

