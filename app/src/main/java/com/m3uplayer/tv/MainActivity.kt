package com.m3uplayer.tv

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.m3uplayer.tv.data.local.AppDatabase
import com.m3uplayer.tv.data.repository.PlaylistRepository
import com.m3uplayer.tv.domain.model.Channel
import com.m3uplayer.tv.player.PlayerManager
import com.m3uplayer.tv.sports.data.repository.SportsRepository
import com.m3uplayer.tv.sports.ui.screens.SportsHomeScreen
import com.m3uplayer.tv.ui.screens.*
import com.m3uplayer.tv.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    WELCOME,
    HOME,
    SPORTS,
    PLAYER,
    SEARCH,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    companion object {
        const val DEFAULT_PLAYLIST_URL = "https://iptv-org.github.io/iptv/index.m3u"
        const val DEFAULT_PLAYLIST_NAME = "Mana TV Channels"
    }

    private lateinit var database: AppDatabase
    private lateinit var repository: PlaylistRepository
    private lateinit var playerManager: PlayerManager

    private val PREFS_NAME = "m3u_player_tv_prefs"
    private val KEY_AUTO_REFRESH_HOURS = "auto_refresh_hours"
    private val KEY_THEME = "theme_setting"
    private val KEY_ACCENT = "accent_setting"
    private val KEY_CARD_SIZE = "card_size_setting"
    private val KEY_SHOW_LOGOS = "show_logos_setting"
    private val KEY_FIRST_RUN_COMPLETED = "first_run_completed"
    private val KEY_WELCOME_AUDIO_VOLUME = "welcome_audio_volume"
    private val KEY_WELCOME_AUDIO_ENABLED = "welcome_audio_enabled"

    private val sampleM3uContent = """
        #EXTM3U
        #EXTINF:-1 tvg-id="bbb.hls" tvg-name="Big Buck Bunny 1080p (HLS)" tvg-logo="https://upload.wikimedia.org/wikipedia/commons/c/c5/Big_buck_bunny_poster_big.jpg" group-title="Animation" tvg-country="US" tvg-language="eng" tvg-chno="1",Big Buck Bunny (HLS Live)
        https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8

        #EXTINF:-1 tvg-id="sintel.hls" tvg-name="Sintel Short Film (HLS)" tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Sintel_poster.jpg/800px-Sintel_poster.jpg" group-title="Movies" tvg-country="NL" tvg-language="eng" tvg-chno="2",Sintel Feature (HLS)
        https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8

        #EXTINF:-1 tvg-id="tears.hls" tvg-name="Tears of Steel (Sci-Fi HLS)" tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Tears_of_Steel_poster.jpg/800px-Tears_of_Steel_poster.jpg" group-title="Sci-Fi" tvg-country="NL" tvg-language="eng" tvg-chno="3",Tears of Steel 4K (HLS)
        https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8

        #EXTINF:-1 tvg-id="elephants.mp4" tvg-name="Elephants Dream" tvg-logo="https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Elephants_Dream_poster.jpg/800px-Elephants_Dream_poster.jpg" group-title="Animation" tvg-country="NL" tvg-language="eng" tvg-chno="4",Elephants Dream (Direct MP4)
        https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4

        #EXTINF:-1 tvg-id="forbiggerblazes.mp4" tvg-name="Chromecast Blaze Promo" tvg-logo="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg" group-title="Tech & Entertainment" tvg-country="US" tvg-language="eng" tvg-chno="5",For Bigger Blazes (HD)
        https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4

        #EXTINF:-1 tvg-id="forbiggerescapes.mp4" tvg-name="Chromecast Escapes" tvg-logo="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg" group-title="Documentary" tvg-country="US" tvg-language="eng" tvg-chno="6",For Bigger Escapes (HD)
        https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)
        repository = PlaylistRepository(database)
        playerManager = PlayerManager(this)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load saved appearance
        val savedTheme = try {
            AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name)
        } catch (_: Exception) { AppTheme.DARK }

        val savedAccent = try {
            AccentColor.valueOf(prefs.getString(KEY_ACCENT, AccentColor.BLUE.name) ?: AccentColor.BLUE.name)
        } catch (_: Exception) { AccentColor.BLUE }

        val savedCardSize = try {
            CardSize.valueOf(prefs.getString(KEY_CARD_SIZE, CardSize.NORMAL.name) ?: CardSize.NORMAL.name)
        } catch (_: Exception) { CardSize.NORMAL }

        val savedShowLogos = prefs.getBoolean(KEY_SHOW_LOGOS, true)

        setContent {
            var uiPreferences by remember {
                mutableStateOf(
                    UiPreferences(
                        theme = savedTheme,
                        accentColor = savedAccent,
                        cardSize = savedCardSize,
                        showLogos = savedShowLogos
                    )
                )
            }

            fun saveUiPreferences(newPrefs: UiPreferences) {
                uiPreferences = newPrefs
                prefs.edit()
                    .putString(KEY_THEME, newPrefs.theme.name)
                    .putString(KEY_ACCENT, newPrefs.accentColor.name)
                    .putString(KEY_CARD_SIZE, newPrefs.cardSize.name)
                    .putBoolean(KEY_SHOW_LOGOS, newPrefs.showLogos)
                    .apply()
            }

            M3UPlayerTVTheme(uiPreferences = uiPreferences) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = uiPreferences.theme.bgMain
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
                    var isFirstRunCompleted by remember {
                        mutableStateOf(prefs.getBoolean(KEY_FIRST_RUN_COMPLETED, false))
                    }
                    var welcomeAudioVolume by remember {
                        mutableStateOf(prefs.getInt(KEY_WELCOME_AUDIO_VOLUME, 150))
                    }
                    var welcomeAudioEnabled by remember {
                        mutableStateOf(prefs.getBoolean(KEY_WELCOME_AUDIO_ENABLED, true))
                    }

                    fun completeWelcome() {
                        isFirstRunCompleted = true
                        prefs.edit().putBoolean(KEY_FIRST_RUN_COMPLETED, true).apply()
                    }

                    fun updateWelcomeAudioVolume(vol: Int) {
                        welcomeAudioVolume = vol
                        prefs.edit().putInt(KEY_WELCOME_AUDIO_VOLUME, vol).apply()
                    }

                    fun toggleWelcomeAudioEnabled(enabled: Boolean) {
                        welcomeAudioEnabled = enabled
                        prefs.edit().putBoolean(KEY_WELCOME_AUDIO_ENABLED, enabled).apply()
                    }

                    var selectedCategory by remember { mutableStateOf("All Channels") }
                    var isAddDialogOpen by remember { mutableStateOf(false) }
                    var isDialogLoading by remember { mutableStateOf(false) }
                    var isRefreshing by remember { mutableStateOf(false) }
                    var dialogError by remember { mutableStateOf<String?>(null) }
                    var autoRefreshHours by remember {
                        mutableStateOf(prefs.getInt(KEY_AUTO_REFRESH_HOURS, 6))
                    }
                    var searchQuery by remember { mutableStateOf("") }

                    val playlists by repository.getAllPlaylists().collectAsState(initial = emptyList())
                    var activePlaylistId by remember { mutableStateOf<String?>(null) }
                    var isDefaultSeeding by remember { mutableStateOf(false) }

                    // Ensure default Mana TV playlist (https://iptv-org.github.io/iptv/index.m3u) is always present and primary
                    LaunchedEffect(Unit) {
                        val all = repository.getAllPlaylists().firstOrNull() ?: emptyList()
                        val defaultPlaylist = all.find { it.sourceUrl?.trim() == DEFAULT_PLAYLIST_URL }
                        if (defaultPlaylist == null) {
                            isDefaultSeeding = true
                            // Clean up legacy sample demo if it was the only one
                            val samplePl = all.find { it.sourceType == "sample" }
                            if (samplePl != null && all.size == 1) {
                                repository.deletePlaylist(samplePl.id)
                            }
                            val res = repository.loadPlaylistFromUrl(DEFAULT_PLAYLIST_NAME, DEFAULT_PLAYLIST_URL)
                            isDefaultSeeding = false
                            if (res.isSuccess) {
                                activePlaylistId = res.getOrNull()?.id
                                completeWelcome()
                            }
                        } else if (activePlaylistId == null) {
                            activePlaylistId = defaultPlaylist.id
                        }
                    }

                    fun loadDefaultManaTvPlaylist() {
                        lifecycleScope.launch {
                            isDialogLoading = true
                            dialogError = null
                            val result = repository.loadPlaylistFromUrl(DEFAULT_PLAYLIST_NAME, DEFAULT_PLAYLIST_URL)
                            isDialogLoading = false
                            if (result.isSuccess) {
                                activePlaylistId = result.getOrNull()?.id
                                completeWelcome()
                                isAddDialogOpen = false
                                currentScreen = Screen.HOME
                                Toast.makeText(this@MainActivity, "Loaded Mana TV Channels", Toast.LENGTH_SHORT).show()
                            } else {
                                dialogError = result.exceptionOrNull()?.message ?: "Failed to load default playlist"
                            }
                        }
                    }

                    // Auto-select first playlist if not set
                    LaunchedEffect(playlists) {
                        if (activePlaylistId == null && playlists.isNotEmpty()) {
                            activePlaylistId = playlists.first().id
                        }
                    }

                    // Auto-refresh trigger on startup
                    LaunchedEffect(activePlaylistId, autoRefreshHours) {
                        if (autoRefreshHours > 0 && activePlaylistId != null) {
                            val active = playlists.find { it.id == activePlaylistId }
                            if (active != null && active.sourceType == "url") {
                                val elapsedMillis = System.currentTimeMillis() - active.lastRefreshed
                                val intervalMillis = autoRefreshHours * 3600 * 1000L
                                if (elapsedMillis > intervalMillis) {
                                    isRefreshing = true
                                    val res = repository.refreshPlaylist(active.id)
                                    isRefreshing = false
                                    if (res.isSuccess) {
                                        Toast.makeText(this@MainActivity, "Playlist refreshed (${active.name})", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                    val channels by remember(activePlaylistId) {
                        if (activePlaylistId != null) {
                            repository.getChannels(activePlaylistId!!)
                        } else {
                            kotlinx.coroutines.flow.flowOf(emptyList())
                        }
                    }.collectAsState(initial = emptyList())

                    val favorites by repository.getFavorites().collectAsState(initial = emptyList())
                    val recentlyWatched by repository.getRecentlyWatched().collectAsState(initial = emptyList())
                    val hiddenChannels by repository.getHiddenChannels().collectAsState(initial = emptyList())
                    val hiddenCategories by repository.getHiddenCategories().collectAsState(initial = emptyList())

                    val searchResults by remember(searchQuery) {
                        if (searchQuery.isNotBlank()) {
                            repository.searchGlobal(searchQuery)
                        } else {
                            kotlinx.coroutines.flow.flowOf(emptyList())
                        }
                    }.collectAsState(initial = emptyList())

                    val activePlaylist = remember(activePlaylistId, playlists) {
                        playlists.find { it.id == activePlaylistId }
                    }

                    val sportsRepository = remember { SportsRepository() }

                    fun playChannel(channel: Channel) {
                        playerManager.playChannel(channel)
                        activePlaylistId?.let { plId ->
                            lifecycleScope.launch {
                                repository.addRecentlyWatched(channel, plId)
                            }
                        }
                        currentScreen = Screen.PLAYER
                    }

                    fun playSportsStream(streamUrl: String, title: String, logoUrl: String?) {
                        val sportsChannel = Channel(
                            id = "sports_${System.currentTimeMillis()}",
                            name = title,
                            url = streamUrl,
                            logo = logoUrl,
                            group = "Live Sports"
                        )
                        playerManager.playChannel(sportsChannel)
                        currentScreen = Screen.PLAYER
                    }

                    fun triggerRefreshPlaylist(playlistId: String) {
                        lifecycleScope.launch {
                            isRefreshing = true
                            val result = repository.refreshPlaylist(playlistId)
                            isRefreshing = false
                            if (result.isSuccess) {
                                val pl = playlists.find { it.id == playlistId }
                                Toast.makeText(this@MainActivity, "Playlist updated (${pl?.channelCount ?: 0} channels)", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Refresh failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    when (currentScreen) {
                        Screen.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    currentScreen = Screen.HOME
                                }
                            )
                        }

                        Screen.WELCOME -> {
                            WelcomeScreen(
                                onOpenAddPlaylist = {
                                    isAddDialogOpen = true
                                },
                                onLoadSample = {
                                    loadDefaultManaTvPlaylist()
                                },
                                welcomeAudioVolume = welcomeAudioVolume,
                                welcomeAudioEnabled = welcomeAudioEnabled
                            )
                        }

                        Screen.HOME -> {
                            HomeScreen(
                                playlists = playlists,
                                activePlaylist = activePlaylist,
                                channels = channels,
                                favorites = favorites,
                                recentlyWatched = recentlyWatched,
                                selectedCategory = selectedCategory,
                                onSelectCategory = { selectedCategory = it },
                                onSelectChannel = { playChannel(it) },
                                onToggleFavorite = { id, isFav ->
                                    lifecycleScope.launch {
                                        repository.toggleFavorite(id, isFav)
                                    }
                                },
                                onHideChannel = { ch ->
                                    lifecycleScope.launch {
                                        repository.hideChannel(ch)
                                        Toast.makeText(this@MainActivity, "Hidden: ${ch.name}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onHideCategory = { catName ->
                                    lifecycleScope.launch {
                                        repository.hideCategory(catName)
                                        selectedCategory = "All Channels"
                                        Toast.makeText(this@MainActivity, "Hidden Category: $catName", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onRemoveRecentlyWatched = { id ->
                                    lifecycleScope.launch {
                                        repository.removeRecentlyWatched(id)
                                    }
                                },
                                onSelectPlaylist = { newId ->
                                    activePlaylistId = newId
                                    selectedCategory = "All Channels"
                                },
                                onRefreshPlaylist = { id -> triggerRefreshPlaylist(id) },
                                onOpenSearch = { currentScreen = Screen.SEARCH },
                                onOpenSports = { currentScreen = Screen.SPORTS },
                                onOpenAddPlaylist = { isAddDialogOpen = true },
                                onOpenSettings = { currentScreen = Screen.SETTINGS },
                                onLoadSample = {
                                    loadDefaultManaTvPlaylist()
                                },
                                welcomeAudioVolume = welcomeAudioVolume,
                                welcomeAudioEnabled = welcomeAudioEnabled,
                                isRefreshing = isRefreshing,
                                isDefaultSeeding = isDefaultSeeding
                            )
                        }

                        Screen.SPORTS -> {
                            SportsHomeScreen(
                                sportsRepository = sportsRepository,
                                onPlayStream = { url, title, logo ->
                                    playSportsStream(url, title, logo)
                                },
                                onBack = { currentScreen = Screen.HOME }
                            )
                        }

                        Screen.PLAYER -> {
                            PlayerScreen(
                                playerManager = playerManager,
                                channelList = channels,
                                onToggleFavorite = { id, isFav ->
                                    lifecycleScope.launch {
                                        repository.toggleFavorite(id, isFav)
                                    }
                                },
                                onBack = { currentScreen = Screen.HOME }
                            )
                        }

                        Screen.SEARCH -> {
                            SearchScreen(
                                searchResults = searchResults,
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSelectChannel = { playChannel(it) },
                                onToggleFavorite = { id, isFav ->
                                    lifecycleScope.launch {
                                        repository.toggleFavorite(id, isFav)
                                    }
                                },
                                onBack = {
                                    searchQuery = ""
                                    currentScreen = Screen.HOME
                                }
                            )
                        }

                        Screen.SETTINGS -> {
                            SettingsScreen(
                                playlists = playlists,
                                activePlaylistId = activePlaylistId,
                                autoRefreshHours = autoRefreshHours,
                                hiddenChannels = hiddenChannels,
                                hiddenCategories = hiddenCategories,
                                uiPreferences = uiPreferences,
                                welcomeAudioVolume = welcomeAudioVolume,
                                onUpdateWelcomeAudioVolume = { updateWelcomeAudioVolume(it) },
                                welcomeAudioEnabled = welcomeAudioEnabled,
                                onToggleWelcomeAudioEnabled = { toggleWelcomeAudioEnabled(it) },
                                onUpdateUiPreferences = { saveUiPreferences(it) },
                                onSetAutoRefreshHours = { hours ->
                                    autoRefreshHours = hours
                                    prefs.edit().putInt(KEY_AUTO_REFRESH_HOURS, hours).apply()
                                },
                                onSelectPlaylist = { activePlaylistId = it },
                                onRefreshPlaylist = { id -> triggerRefreshPlaylist(id) },
                                onDeletePlaylist = { id ->
                                    lifecycleScope.launch {
                                        repository.deletePlaylist(id)
                                        if (activePlaylistId == id) {
                                            activePlaylistId = playlists.firstOrNull { it.id != id }?.id
                                        }
                                    }
                                },
                                onUnhideChannel = { id ->
                                    lifecycleScope.launch { repository.unhideChannel(id) }
                                },
                                onUnhideCategory = { name ->
                                    lifecycleScope.launch { repository.unhideCategory(name) }
                                },
                                onUnhideAllChannels = {
                                    lifecycleScope.launch { repository.unhideAllChannels() }
                                },
                                onUnhideAllCategories = {
                                    lifecycleScope.launch { repository.unhideAllCategories() }
                                },
                                onClearFavorites = {
                                    lifecycleScope.launch { repository.clearFavorites() }
                                },
                                onClearRecentlyWatched = {
                                    lifecycleScope.launch { repository.clearRecentlyWatched() }
                                },
                                onClearAllData = {
                                    lifecycleScope.launch {
                                        repository.clearAllData()
                                        activePlaylistId = null
                                        isFirstRunCompleted = false
                                        prefs.edit().putBoolean(KEY_FIRST_RUN_COMPLETED, false).apply()
                                    }
                                },
                                onBack = { currentScreen = Screen.HOME },
                                isRefreshing = isRefreshing
                            )
                        }
                    }

                    // Add Playlist Modal Dialog
                    AddPlaylistDialog(
                        isOpen = isAddDialogOpen,
                        isLoading = isDialogLoading,
                        errorMessage = dialogError,
                        onDismiss = {
                            isAddDialogOpen = false
                            dialogError = null
                        },
                        onSubmitUrl = { name, url ->
                            isDialogLoading = true
                            dialogError = null
                            lifecycleScope.launch {
                                val result = repository.loadPlaylistFromUrl(name, url)
                                isDialogLoading = false
                                if (result.isSuccess) {
                                    completeWelcome()
                                    activePlaylistId = result.getOrNull()?.id
                                    isAddDialogOpen = false
                                    currentScreen = Screen.HOME
                                } else {
                                    dialogError = result.exceptionOrNull()?.message ?: "Failed to load playlist"
                                }
                            }
                        },
                        onLoadSample = {
                            loadDefaultManaTvPlaylist()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}
