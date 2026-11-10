package com.m3uplayer.tv.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.ui.PlayerView
import com.m3uplayer.tv.domain.model.Channel
import com.m3uplayer.tv.player.AspectRatioMode
import com.m3uplayer.tv.player.PlayerManager
import com.m3uplayer.tv.ui.components.ChannelLogo
import com.m3uplayer.tv.ui.theme.*
import kotlinx.coroutines.delay

enum class PlayerControlAction {
    INFO,
    QUALITY,
    AUDIO,
    SUBTITLES,
    ASPECT,
    PLAY_PAUSE
}

data class PlayerControlButton(
    val action: PlayerControlAction,
    val label: String,
    val icon: ImageVector
)

@Composable
fun PlayerScreen(
    playerManager: PlayerManager,
    channelList: List<Channel>,
    onToggleFavorite: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiPrefs = LocalUiPreferences.current
    val playerState by playerManager.playerState.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var focusedControlIndex by remember { mutableIntStateOf(0) }
    var interactionTick by remember { mutableIntStateOf(0) }

    var showQualityDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAspectDialog by remember { mutableStateOf(false) }
    var showStreamInfoDialog by remember { mutableStateOf(false) }

    val isAnyDialogOpen = showQualityDialog || showAudioDialog || showSubtitleDialog || showAspectDialog || showStreamInfoDialog

    // Build the list of active horizontal action controls dynamically
    val availableControls = remember(playerState.audioTracks, playerState.subtitles, playerState.selectedQuality, playerState.aspectRatioMode, playerState.isPlaying) {
        val list = mutableListOf<PlayerControlButton>()
        list.add(PlayerControlButton(PlayerControlAction.INFO, "Info", Icons.Default.Info))
        list.add(PlayerControlButton(PlayerControlAction.QUALITY, playerState.selectedQuality?.label ?: "Quality", Icons.Default.HighQuality))
        if (playerState.audioTracks.size > 1) {
            list.add(PlayerControlButton(PlayerControlAction.AUDIO, playerState.selectedAudioTrack?.label ?: "Audio", Icons.AutoMirrored.Filled.VolumeUp))
        }
        if (playerState.subtitles.isNotEmpty()) {
            list.add(PlayerControlButton(PlayerControlAction.SUBTITLES, playerState.selectedSubtitle?.label ?: "Subtitles", Icons.Default.Subtitles))
        }
        list.add(PlayerControlButton(PlayerControlAction.ASPECT, playerState.aspectRatioMode.label, Icons.Default.AspectRatio))
        list.add(PlayerControlButton(
            PlayerControlAction.PLAY_PAUSE,
            if (playerState.isPlaying) "Pause" else "Play",
            if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow
        ))
        list
    }

    // Keep focused index within valid bounds
    LaunchedEffect(availableControls.size) {
        if (focusedControlIndex >= availableControls.size) {
            focusedControlIndex = (availableControls.size - 1).coerceAtLeast(0)
        }
    }

    // Auto-hide controls after 5 seconds of inactivity when playing and no modal is open
    LaunchedEffect(showControls, playerState.isPlaying, isAnyDialogOpen, interactionTick) {
        if (showControls && !isAnyDialogOpen && playerState.isPlaying && playerState.errorMessage == null) {
            delay(5000)
            showControls = false
        }
    }

    BackHandler {
        if (isAnyDialogOpen) {
            showQualityDialog = false
            showAudioDialog = false
            showSubtitleDialog = false
            showAspectDialog = false
            showStreamInfoDialog = false
        } else if (showControls) {
            showControls = false
        } else {
            playerManager.stop()
            onBack()
        }
    }

    fun selectNextChannel() {
        val current = playerState.currentChannel ?: return
        val idx = channelList.indexOfFirst { it.id == current.id }
        if (idx != -1 && idx < channelList.size - 1) {
            playerManager.playChannel(channelList[idx + 1])
        } else if (channelList.isNotEmpty()) {
            playerManager.playChannel(channelList.first())
        }
    }

    fun selectPrevChannel() {
        val current = playerState.currentChannel ?: return
        val idx = channelList.indexOfFirst { it.id == current.id }
        if (idx > 0) {
            playerManager.playChannel(channelList[idx - 1])
        } else if (channelList.isNotEmpty()) {
            playerManager.playChannel(channelList.last())
        }
    }

    fun executeControlAction(action: PlayerControlAction) {
        when (action) {
            PlayerControlAction.INFO -> showStreamInfoDialog = true
            PlayerControlAction.QUALITY -> showQualityDialog = true
            PlayerControlAction.AUDIO -> showAudioDialog = true
            PlayerControlAction.SUBTITLES -> showSubtitleDialog = true
            PlayerControlAction.ASPECT -> showAspectDialog = true
            PlayerControlAction.PLAY_PAUSE -> playerManager.togglePlayPause()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    interactionTick++
                    if (isAnyDialogOpen) {
                        // Modal is active, let modal handle inner navigation
                        return@onKeyEvent false
                    }

                    if (!showControls) {
                        // CONTROLS NOT VISIBLE: LEFT/RIGHT = Channel Switching
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                selectPrevChannel()
                                true
                            }
                            Key.DirectionRight -> {
                                selectNextChannel()
                                true
                            }
                            Key.DirectionCenter, Key.Enter, Key.DirectionUp, Key.DirectionDown -> {
                                showControls = true
                                true
                            }
                            Key.MediaPlayPause, Key.Spacebar -> {
                                playerManager.togglePlayPause()
                                showControls = true
                                true
                            }
                            else -> false
                        }
                    } else {
                        // CONTROLS VISIBLE: LEFT/RIGHT = Navigate between visible controls
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                if (focusedControlIndex > 0) {
                                    focusedControlIndex--
                                } else {
                                    focusedControlIndex = availableControls.size - 1
                                }
                                true
                            }
                            Key.DirectionRight -> {
                                if (focusedControlIndex < availableControls.size - 1) {
                                    focusedControlIndex++
                                } else {
                                    focusedControlIndex = 0
                                }
                                true
                            }
                            Key.DirectionCenter, Key.Enter -> {
                                if (availableControls.isNotEmpty()) {
                                    val ctrl = availableControls[focusedControlIndex.coerceIn(0, availableControls.size - 1)]
                                    executeControlAction(ctrl.action)
                                }
                                true
                            }
                            Key.DirectionUp, Key.DirectionDown -> {
                                // Keep controls alive and visible
                                true
                            }
                            Key.MediaPlayPause, Key.Spacebar -> {
                                playerManager.togglePlayPause()
                                true
                            }
                            else -> false
                        }
                    }
                } else false
            }
    ) {
        // Fullscreen ExoPlayer View with Aspect Ratio mode
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.getPlayer()
                    useController = false
                    resizeMode = playerState.aspectRatioMode.resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.resizeMode = playerState.aspectRatioMode.resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (playerState.isBuffering && playerState.errorMessage == null) {
            CircularProgressIndicator(
                color = uiPrefs.accentColor.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error Dialog Overlay
        playerState.errorMessage?.let { errorMsg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE60D0F14))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = LiveRed,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Playback Error",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            playerState.currentChannel?.let { playerManager.playChannel(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = uiPrefs.accentColor.primary)
                    ) {
                        Text("Retry Stream")
                    }
                }
            }
        }

        // Top & Bottom TV Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Channel Info Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xF2000000), Color(0x99000000), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 28.dp, vertical = 20.dp)
                ) {
                    playerState.currentChannel?.let { channel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ChannelLogo(
                                    logoUrl = channel.logo,
                                    name = channel.name,
                                    size = 56.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "NOW PLAYING",
                                            color = uiPrefs.accentColor.glow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        channel.channelNumber?.let {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "CH $it",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(Color(0xFF1E2433), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = channel.name,
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = channel.group,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Favorite Toggle & Live Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        onToggleFavorite(channel.id, channel.isFavorite)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (channel.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Favorite",
                                        tint = if (channel.isFavorite) FavoriteYellow else Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xCCDC2626), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Player Controls Bar with High-Contrast D-Pad Focus
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x99000000), Color(0xF2000000))
                            )
                        )
                        .padding(horizontal = 28.dp, vertical = 22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // High-contrast Horizontal Player Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            availableControls.forEachIndexed { index, ctrl ->
                                val isFocused = focusedControlIndex == index

                                val scale by animateFloatAsState(
                                    targetValue = if (isFocused) 1.08f else 1.0f,
                                    label = "control_scale"
                                )

                                val bgModifier = if (isFocused) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                uiPrefs.accentColor.primary,
                                                uiPrefs.accentColor.glow
                                            )
                                        )
                                    )
                                } else {
                                    Modifier.background(Color(0xF0131824))
                                }

                                Box(
                                    modifier = Modifier
                                        .scale(scale)
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(bgModifier)
                                        .border(
                                            width = if (isFocused) 3.dp else 1.5.dp,
                                            color = if (isFocused) Color.White else Color(0xFF334155),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            focusedControlIndex = index
                                            executeControlAction(ctrl.action)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = ctrl.icon,
                                            contentDescription = ctrl.label,
                                            tint = if (isFocused) Color.White else Color(0xFF93C5FD),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = ctrl.label,
                                            color = if (isFocused) Color.White else Color(0xFFF1F5F9),
                                            fontSize = 13.sp,
                                            fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Remote Cheatsheet Hint
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "◀ / ▶ Navigate Controls   ● Activate / Open   ⮌ Hide Controls (Left/Right to switch channels)",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Mana TV",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Stream Information Dialog
        if (showStreamInfoDialog) {
            Dialog(onDismissRequest = { showStreamInfoDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(480.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111624))
                        .border(2.dp, uiPrefs.accentColor.primary, RoundedCornerShape(18.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = uiPrefs.accentColor.glow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Stream Information", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showStreamInfoDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val info = playerState.streamInfo
                        val infoList = listOf(
                            "Channel Name" to (playerState.currentChannel?.name ?: "Unknown"),
                            "Resolution" to (info.resolution ?: "Unknown"),
                            "Video Codec" to (info.videoCodec ?: "Unknown"),
                            "Audio Codec" to (info.audioCodec ?: "Unknown"),
                            "Bitrate" to (info.bitrate ?: "Unknown"),
                            "Frame Rate" to (info.frameRate ?: "Unknown"),
                            "Buffer Duration" to (info.bufferedDuration ?: "Unknown"),
                            "Aspect Ratio" to playerState.aspectRatioMode.label
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            infoList.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF182032), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp)
                                    Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quality Selection Dialog
        if (showQualityDialog) {
            Dialog(onDismissRequest = { showQualityDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(440.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111624))
                        .border(2.dp, uiPrefs.accentColor.primary, RoundedCornerShape(18.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Stream Quality", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showQualityDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                val isAutoSelected = playerState.selectedQuality == null
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isAutoSelected) uiPrefs.accentColor.primary else Color(0xFF182032))
                                        .border(
                                            width = if (isAutoSelected) 2.dp else 1.dp,
                                            color = if (isAutoSelected) Color.White else Color(0xFF334155),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            playerManager.setQuality(null)
                                            showQualityDialog = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Auto (Adaptive)",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = if (isAutoSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            "Automatically selects best quality",
                                            color = if (isAutoSelected) Color(0xFFE2E8F0) else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (isAutoSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            items(playerState.availableQualities) { quality ->
                                val isSelected = playerState.selectedQuality?.id == quality.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) uiPrefs.accentColor.primary else Color(0xFF182032))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            playerManager.setQuality(quality)
                                            showQualityDialog = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            quality.label,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (quality.bitrate > 0) {
                                            Text(
                                                "${quality.bitrate / 1000} kbps",
                                                color = if (isSelected) Color(0xFFE2E8F0) else TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Audio Track Selection Dialog
        if (showAudioDialog) {
            Dialog(onDismissRequest = { showAudioDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(440.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111624))
                        .border(2.dp, uiPrefs.accentColor.primary, RoundedCornerShape(18.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Audio Tracks", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showAudioDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(playerState.audioTracks) { track ->
                                val isSelected = playerState.selectedAudioTrack?.id == track.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) uiPrefs.accentColor.primary else Color(0xFF182032))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            playerManager.setAudioTrack(track)
                                            showAudioDialog = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        track.label,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subtitle Track Selection Dialog
        if (showSubtitleDialog) {
            Dialog(onDismissRequest = { showSubtitleDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(440.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111624))
                        .border(2.dp, uiPrefs.accentColor.primary, RoundedCornerShape(18.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Subtitles", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showSubtitleDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            item {
                                val isOffSelected = playerState.selectedSubtitle == null
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isOffSelected) uiPrefs.accentColor.primary else Color(0xFF182032))
                                        .border(
                                            width = if (isOffSelected) 2.dp else 1.dp,
                                            color = if (isOffSelected) Color.White else Color(0xFF334155),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            playerManager.setSubtitleTrack(null)
                                            showSubtitleDialog = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Off", color = Color.White, fontSize = 15.sp, fontWeight = if (isOffSelected) FontWeight.Bold else FontWeight.Medium)
                                    if (isOffSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            items(playerState.subtitles) { sub ->
                                val isSelected = playerState.selectedSubtitle?.id == sub.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) uiPrefs.accentColor.primary else Color(0xFF182032))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            playerManager.setSubtitleTrack(sub)
                                            showSubtitleDialog = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(sub.label, color = Color.White, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Aspect Ratio Selection Dialog
        if (showAspectDialog) {
            Dialog(onDismissRequest = { showAspectDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(440.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111624))
                        .border(2.dp, uiPrefs.accentColor.primary, RoundedCornerShape(18.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Aspect Ratio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showAspectDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        AspectRatioMode.values().forEach { mode ->
                            val isSelected = playerState.aspectRatioMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) uiPrefs.accentColor.primary else Color(0xFF182032))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        playerManager.setAspectRatio(mode)
                                        showAspectDialog = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mode.label, color = Color.White, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
