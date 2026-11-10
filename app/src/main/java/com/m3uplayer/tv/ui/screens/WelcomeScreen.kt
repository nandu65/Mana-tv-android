package com.m3uplayer.tv.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.m3uplayer.tv.R
import com.m3uplayer.tv.ui.theme.LocalUiPreferences

@Composable
fun WelcomeScreen(
    onOpenAddPlaylist: () -> Unit,
    onLoadSample: () -> Unit,
    welcomeAudioVolume: Int = 150,
    welcomeAudioEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiPrefs = LocalUiPreferences.current

    // Video Player: Plays welcome_video.mp4 (Muted)
    val videoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = RawResourceDataSource.buildRawResourceUri(R.raw.welcome_video)
            setMediaItem(MediaItem.fromUri(videoUri))
            volume = 0f // Mute video's internal audio track completely
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
            prepare()
        }
    }

    // Audio Player: Plays welcome_audio.mp3 simultaneously with runtime volume boost
    val audioPlayer = remember {
        if (!welcomeAudioEnabled || welcomeAudioVolume <= 0) {
            null
        } else {
            try {
                MediaPlayer.create(context, R.raw.welcome_audio)?.apply {
                    isLooping = false
                    val baseVol = (welcomeAudioVolume.coerceAtMost(100) / 100f)
                    setVolume(baseVol, baseVol)

                    // If volume > 100%, apply LoudnessEnhancer for the runtime +50% boost without re-encoding
                    if (welcomeAudioVolume > 100) {
                        try {
                            val enhancer = android.media.audiofx.LoudnessEnhancer(audioSessionId)
                            val boostmB = ((welcomeAudioVolume - 100) * 6).coerceIn(0, 800)
                            enhancer.setTargetGain(boostmB)
                            enhancer.enabled = true
                        } catch (_: Exception) {}
                    }
                    start()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    // Lifecycle cleanup: stop and release both players on disposal
    DisposableEffect(Unit) {
        onDispose {
            try {
                videoPlayer.stop()
                videoPlayer.release()
            } catch (_: Exception) {}
            try {
                audioPlayer?.stop()
                audioPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Fullscreen 16:9 Video Presentation (Aspect ratio preserved without stretching)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = videoPlayer
                    useController = false
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient vignette overlay at the bottom for action legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0x99050811),
                            Color(0xE6050811)
                        )
                    )
                )
        )

        // Welcome TV D-Pad Action Controls Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WelcomeTvButton(
                    text = "Add M3U Playlist",
                    icon = Icons.Default.Add,
                    glowColor = uiPrefs.accentColor.glow,
                    primaryColor = uiPrefs.accentColor.primary,
                    onClick = onOpenAddPlaylist
                )

                WelcomeTvButton(
                    text = "Try Sample Demo",
                    icon = Icons.Default.AutoAwesome,
                    glowColor = uiPrefs.accentColor.glow,
                    primaryColor = uiPrefs.accentColor.primary,
                    onClick = onLoadSample
                )
            }
        }
    }
}

@Composable
private fun WelcomeTvButton(
    text: String,
    icon: ImageVector,
    glowColor: Color,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scaleAnim)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isFocused) primaryColor
                else Color(0xCC111827)
            )
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                color = if (isFocused) glowColor else Color(0x44FFFFFF),
                shape = RoundedCornerShape(14.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.White else Color(0xFFE2E8F0),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = text,
                color = if (isFocused) Color.White else Color(0xFFE2E8F0),
                fontSize = 16.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}
