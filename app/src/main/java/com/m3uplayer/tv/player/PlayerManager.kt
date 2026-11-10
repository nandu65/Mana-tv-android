package com.m3uplayer.tv.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import com.m3uplayer.tv.domain.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VideoQuality(
    val id: String,
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Int = 0,
    val isAuto: Boolean = false
)

data class AudioTrackOption(
    val id: String,
    val label: String,
    val language: String?,
    val channelCount: Int = 2,
    val isSelected: Boolean = false
)

data class SubtitleOption(
    val id: String,
    val label: String,
    val language: String?,
    val isSelected: Boolean = false
)

enum class AspectRatioMode(val label: String, val resizeMode: Int) {
    FIT("Fit to Screen", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    FILL("Fill (Zoom)", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    ORIGINAL("Original (Stretch)", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

data class StreamInfo(
    val resolution: String? = null,
    val videoCodec: String? = null,
    val audioCodec: String? = null,
    val bitrate: String? = null,
    val frameRate: String? = null,
    val bufferedDuration: String? = null
)

data class PlayerState(
    val currentChannel: Channel? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null,
    val availableQualities: List<VideoQuality> = emptyList(),
    val selectedQuality: VideoQuality? = null,
    val audioTracks: List<AudioTrackOption> = emptyList(),
    val selectedAudioTrack: AudioTrackOption? = null,
    val subtitles: List<SubtitleOption> = emptyList(),
    val selectedSubtitle: SubtitleOption? = null,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val streamInfo: StreamInfo = StreamInfo()
)

class PlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: createPlayer().also { exoPlayer = it }
    }

    private fun createPlayer(): ExoPlayer {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("M3UPlayerTV/1.0 (Android TV)")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.value = _playerState.value.copy(isBuffering = true, errorMessage = null)
                    }
                    Player.STATE_READY -> {
                        _playerState.value = _playerState.value.copy(
                            isBuffering = false,
                            isPlaying = player.isPlaying,
                            errorMessage = null
                        )
                        updateStreamDiagnostics()
                    }
                    Player.STATE_ENDED -> {
                        _playerState.value = _playerState.value.copy(isBuffering = false, isPlaying = false)
                    }
                    Player.STATE_IDLE -> {
                        _playerState.value = _playerState.value.copy(isBuffering = false)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                if (isPlaying) updateStreamDiagnostics()
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                updateStreamDiagnostics()
            }

            override fun onTracksChanged(tracks: Tracks) {
                val qualities = mutableListOf<VideoQuality>()
                val audios = mutableListOf<AudioTrackOption>()
                val subs = mutableListOf<SubtitleOption>()

                var currentAudioSelected: AudioTrackOption? = null
                var currentSubSelected: SubtitleOption? = null

                for (group in tracks.groups) {
                    when (group.type) {
                        C.TRACK_TYPE_VIDEO -> {
                            for (i in 0 until group.length) {
                                if (group.isTrackSupported(i)) {
                                    val format = group.getTrackFormat(i)
                                    val height = format.height
                                    val width = format.width
                                    val bitrate = format.bitrate
                                    val label = when {
                                        height >= 2160 -> "4K Ultra HD (${height}p)"
                                        height >= 1440 -> "1440p QHD"
                                        height >= 1080 -> "1080p Full HD"
                                        height >= 720 -> "720p HD"
                                        height >= 540 -> "540p"
                                        height >= 480 -> "480p SD"
                                        height > 0 -> "${height}p"
                                        bitrate > 0 -> "${bitrate / 1000} kbps"
                                        else -> "Stream Track ${i + 1}"
                                    }

                                    qualities.add(
                                        VideoQuality(
                                            id = "${width}x${height}_${bitrate}_$i",
                                            label = label,
                                            width = width,
                                            height = height,
                                            bitrate = bitrate,
                                            isAuto = false
                                        )
                                    )
                                }
                            }
                        }

                        C.TRACK_TYPE_AUDIO -> {
                            for (i in 0 until group.length) {
                                if (group.isTrackSupported(i)) {
                                    val format = group.getTrackFormat(i)
                                    val lang = format.language?.let { Locale(it).displayLanguage } ?: format.language ?: "Audio ${i + 1}"
                                    val channels = format.channelCount
                                    val channelText = if (channels > 2) " (${channels}ch Surround)" else ""
                                    val option = AudioTrackOption(
                                        id = "audio_${group.mediaTrackGroup.id}_$i",
                                        label = "$lang$channelText",
                                        language = format.language,
                                        channelCount = channels,
                                        isSelected = group.isTrackSelected(i)
                                    )
                                    audios.add(option)
                                    if (group.isTrackSelected(i)) {
                                        currentAudioSelected = option
                                    }
                                }
                            }
                        }

                        C.TRACK_TYPE_TEXT -> {
                            for (i in 0 until group.length) {
                                if (group.isTrackSupported(i)) {
                                    val format = group.getTrackFormat(i)
                                    val lang = format.language?.let { Locale(it).displayLanguage } ?: format.label ?: "Subtitle ${i + 1}"
                                    val option = SubtitleOption(
                                        id = "sub_${group.mediaTrackGroup.id}_$i",
                                        label = lang,
                                        language = format.language,
                                        isSelected = group.isTrackSelected(i)
                                    )
                                    subs.add(option)
                                    if (group.isTrackSelected(i)) {
                                        currentSubSelected = option
                                    }
                                }
                            }
                        }
                    }
                }

                val finalQualities = if (qualities.isNotEmpty()) {
                    qualities.distinctBy { if (it.height > 0) it.height else it.bitrate }.sortedByDescending { it.height }
                } else {
                    listOf(
                        VideoQuality("preset_1080", "1080p Full HD", 1920, 1080),
                        VideoQuality("preset_720", "720p HD", 1280, 720),
                        VideoQuality("preset_480", "480p SD", 854, 480),
                        VideoQuality("preset_360", "360p", 640, 360)
                    )
                }

                _playerState.value = _playerState.value.copy(
                    availableQualities = finalQualities,
                    audioTracks = audios,
                    selectedAudioTrack = currentAudioSelected ?: audios.firstOrNull(),
                    subtitles = subs,
                    selectedSubtitle = currentSubSelected
                )

                updateStreamDiagnostics()
            }

            override fun onPlayerError(error: PlaybackException) {
                val readableError = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                        "Network timeout or stream server is offline."
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                        "Audio/Video codec unsupported on this device."
                    else ->
                        "Unable to play this stream. The stream may be offline or unavailable."
                }
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = readableError
                )
            }
        })

        return player
    }

    private fun updateStreamDiagnostics() {
        val player = exoPlayer ?: return
        val format = player.videoFormat
        val audioFormat = player.audioFormat

        val resolution = if (format != null && format.width > 0 && format.height > 0) {
            "${format.width} × ${format.height}"
        } else null

        val videoCodec = format?.sampleMimeType?.let { mime ->
            when {
                mime.contains("avc", ignoreCase = true) -> "H.264 (AVC)"
                mime.contains("hevc", ignoreCase = true) || mime.contains("hvc", ignoreCase = true) -> "H.265 (HEVC)"
                mime.contains("av01", ignoreCase = true) || mime.contains("av1", ignoreCase = true) -> "AV1"
                mime.contains("vp9", ignoreCase = true) -> "VP9"
                else -> mime.substringAfter("/")
            }
        }

        val audioCodec = audioFormat?.sampleMimeType?.let { mime ->
            when {
                mime.contains("mp4a", ignoreCase = true) || mime.contains("aac", ignoreCase = true) -> "AAC"
                mime.contains("ac3", ignoreCase = true) -> "Dolby Digital (AC-3)"
                mime.contains("eac3", ignoreCase = true) -> "Dolby Digital Plus (E-AC-3)"
                mime.contains("mpeg", ignoreCase = true) -> "MP3 / MPEG"
                else -> mime.substringAfter("/")
            }
        }

        val bitrate = if (format != null && format.bitrate > 0) {
            String.format(Locale.US, "%.1f Mbps", format.bitrate / 1000000.0)
        } else null

        val frameRate = if (format != null && format.frameRate > 0) {
            "${format.frameRate.toInt()} FPS"
        } else null

        val bufferSec = if (player.totalBufferedDuration > 0) {
            String.format(Locale.US, "%.1f sec", player.totalBufferedDuration / 1000.0)
        } else null

        _playerState.value = _playerState.value.copy(
            streamInfo = StreamInfo(
                resolution = resolution,
                videoCodec = videoCodec,
                audioCodec = audioCodec,
                bitrate = bitrate,
                frameRate = frameRate,
                bufferedDuration = bufferSec
            )
        )
    }

    fun playChannel(channel: Channel) {
        val player = getPlayer()
        _playerState.value = PlayerState(
            currentChannel = channel,
            isBuffering = true,
            errorMessage = null,
            availableQualities = emptyList(),
            selectedQuality = null,
            audioTracks = emptyList(),
            selectedAudioTrack = null,
            subtitles = emptyList(),
            selectedSubtitle = null,
            aspectRatioMode = _playerState.value.aspectRatioMode,
            streamInfo = StreamInfo()
        )

        val mediaItem = MediaItem.Builder()
            .setUri(channel.url.trim())
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun setQuality(quality: VideoQuality?) {
        val player = exoPlayer ?: return
        if (quality == null || quality.isAuto) {
            val newParams = player.trackSelectionParameters.buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .clearVideoSizeConstraints()
                .build()
            player.trackSelectionParameters = newParams
            _playerState.value = _playerState.value.copy(selectedQuality = null)
        } else {
            val tracks = player.currentTracks
            var overrideApplied = false

            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_VIDEO) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        if (format.height == quality.height && format.width == quality.width) {
                            val override = TrackSelectionOverride(group.mediaTrackGroup, i)
                            val newParams = player.trackSelectionParameters.buildUpon()
                                .setOverrideForType(override)
                                .build()
                            player.trackSelectionParameters = newParams
                            overrideApplied = true
                            break
                        }
                    }
                    if (overrideApplied) break
                }
            }

            if (!overrideApplied && quality.height > 0) {
                val newParams = player.trackSelectionParameters.buildUpon()
                    .setMaxVideoSize(
                        if (quality.width > 0) quality.width else Int.MAX_VALUE,
                        quality.height
                    )
                    .build()
                player.trackSelectionParameters = newParams
            }

            _playerState.value = _playerState.value.copy(selectedQuality = quality)
        }
        updateStreamDiagnostics()
    }

    fun setAudioTrack(option: AudioTrackOption?) {
        val player = exoPlayer ?: return
        if (option == null) {
            val params = player.trackSelectionParameters.buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                .build()
            player.trackSelectionParameters = params
            _playerState.value = _playerState.value.copy(selectedAudioTrack = null)
            return
        }

        val tracks = player.currentTracks
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    val id = "audio_${group.mediaTrackGroup.id}_$i"
                    if (id == option.id) {
                        val override = TrackSelectionOverride(group.mediaTrackGroup, i)
                        val params = player.trackSelectionParameters.buildUpon()
                            .setOverrideForType(override)
                            .build()
                        player.trackSelectionParameters = params
                        _playerState.value = _playerState.value.copy(selectedAudioTrack = option)
                        return
                    }
                }
            }
        }
    }

    fun setSubtitleTrack(option: SubtitleOption?) {
        val player = exoPlayer ?: return
        if (option == null) {
            // Turn off subtitles
            val params = player.trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .build()
            player.trackSelectionParameters = params
            _playerState.value = _playerState.value.copy(selectedSubtitle = null)
            return
        }

        val tracks = player.currentTracks
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    val id = "sub_${group.mediaTrackGroup.id}_$i"
                    if (id == option.id) {
                        val override = TrackSelectionOverride(group.mediaTrackGroup, i)
                        val params = player.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .setOverrideForType(override)
                            .build()
                        player.trackSelectionParameters = params
                        _playerState.value = _playerState.value.copy(selectedSubtitle = option)
                        return
                    }
                }
            }
        }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _playerState.value = _playerState.value.copy(aspectRatioMode = mode)
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun stop() {
        exoPlayer?.stop()
        _playerState.value = PlayerState(aspectRatioMode = _playerState.value.aspectRatioMode)
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
