package com.m3uplayer.tv.data.repository

import com.m3uplayer.tv.data.local.AppDatabase
import com.m3uplayer.tv.data.model.HiddenCategoryEntity
import com.m3uplayer.tv.data.model.HiddenChannelEntity
import com.m3uplayer.tv.data.model.PlaylistEntity
import com.m3uplayer.tv.data.model.RecentlyWatchedEntity
import com.m3uplayer.tv.data.parser.M3uParser
import com.m3uplayer.tv.domain.model.Channel
import com.m3uplayer.tv.domain.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

class PlaylistRepository(private val db: AppDatabase) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return db.playlistDao().getAllPlaylists().map { list -> list.map { it.toDomain() } }
    }

    fun getChannels(playlistId: String): Flow<List<Channel>> {
        return combine(
            db.channelDao().getChannelsByPlaylist(playlistId),
            db.hiddenDao().getHiddenChannels(),
            db.hiddenDao().getHiddenCategories()
        ) { channels, hiddenChannels, hiddenCategories ->
            val hiddenChannelIds = hiddenChannels.map { it.channelId }.toSet()
            val hiddenCategoryNames = hiddenCategories.map { it.categoryName.lowercase() }.toSet()

            channels
                .filter { !hiddenChannelIds.contains(it.id) && !hiddenCategoryNames.contains(it.groupTitle.lowercase()) }
                .map { it.toDomain() }
        }
    }

    fun getFavorites(): Flow<List<Channel>> {
        return combine(
            db.channelDao().getAllFavorites(),
            db.hiddenDao().getHiddenChannels()
        ) { favorites, hiddenChannels ->
            val hiddenIds = hiddenChannels.map { it.channelId }.toSet()
            favorites
                .filter { !hiddenIds.contains(it.id) }
                .distinctBy { it.url.trim().ifEmpty { it.name } }
                .map { it.toDomain() }
        }
    }

    fun getRecentlyWatched(): Flow<List<Channel>> {
        return db.recentlyWatchedDao().getRecentlyWatched().map { list ->
            list.map { it.toDomainChannel() }
        }
    }

    fun getHiddenChannels(): Flow<List<HiddenChannelEntity>> {
        return db.hiddenDao().getHiddenChannels()
    }

    fun getHiddenCategories(): Flow<List<HiddenCategoryEntity>> {
        return db.hiddenDao().getHiddenCategories()
    }

    fun searchGlobal(query: String): Flow<List<Channel>> {
        return combine(
            db.channelDao().searchAllChannels(query),
            db.playlistDao().getAllPlaylists(),
            db.hiddenDao().getHiddenChannels(),
            db.hiddenDao().getHiddenCategories()
        ) { channels, playlists, hiddenChannels, hiddenCategories ->
            val playlistMap = playlists.associate { it.id to it.name }
            val hiddenChannelIds = hiddenChannels.map { it.channelId }.toSet()
            val hiddenCategoryNames = hiddenCategories.map { it.categoryName.lowercase() }.toSet()

            channels
                .filter { !hiddenChannelIds.contains(it.id) && !hiddenCategoryNames.contains(it.groupTitle.lowercase()) }
                .map { entity ->
                    entity.toDomain(playlistName = playlistMap[entity.playlistId] ?: "Playlist")
                }
        }
    }

    suspend fun loadPlaylistFromUrl(name: String, url: String): Result<Playlist> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url.trim())
                .header("User-Agent", "M3UPlayerTV/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to download playlist (HTTP ${response.code})"))
            }

            val content = response.body?.string() ?: return@withContext Result.failure(IOException("Empty response body"))
            val playlistId = "pl_${UUID.randomUUID().toString().take(8)}"
            val channels = M3uParser.parse(content, playlistId)

            if (channels.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No valid channels found in the M3U playlist."))
            }

            val playlistEntity = PlaylistEntity(
                id = playlistId,
                name = name.ifBlank { "Remote Playlist" },
                sourceType = "url",
                sourceUrl = url.trim(),
                channelCount = channels.size,
                lastRefreshed = System.currentTimeMillis()
            )

            db.playlistDao().insertPlaylist(playlistEntity)
            db.channelDao().insertChannels(channels)

            Result.success(playlistEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadPlaylistFromContent(name: String, content: String, sourceType: String): Result<Playlist> = withContext(Dispatchers.IO) {
        try {
            val playlistId = "pl_${UUID.randomUUID().toString().take(8)}"
            val channels = M3uParser.parse(content, playlistId)

            if (channels.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No valid channels found in the M3U content."))
            }

            val playlistEntity = PlaylistEntity(
                id = playlistId,
                name = name.ifBlank { "Local Playlist" },
                sourceType = sourceType,
                sourceUrl = null,
                channelCount = channels.size,
                lastRefreshed = System.currentTimeMillis()
            )

            db.playlistDao().insertPlaylist(playlistEntity)
            db.channelDao().insertChannels(channels)

            Result.success(playlistEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshPlaylist(playlistId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val playlist = db.playlistDao().getPlaylistById(playlistId)
                ?: return@withContext Result.failure(IllegalStateException("Playlist not found"))

            if (playlist.sourceUrl.isNullOrBlank()) {
                return@withContext Result.failure(IllegalStateException("Local file playlists cannot be refreshed automatically."))
            }

            // Capture existing favorite URLs to restore after refresh
            val existingChannels = db.channelDao().getChannelsByPlaylist(playlistId).firstOrNull() ?: emptyList()
            val favoriteUrls = existingChannels.filter { it.isFavorite }.map { it.url.trim() }.toSet()

            val request = Request.Builder()
                .url(playlist.sourceUrl)
                .header("User-Agent", "M3UPlayerTV/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Refresh failed with HTTP ${response.code}"))
            }

            val content = response.body?.string() ?: return@withContext Result.failure(IOException("Empty body"))
            val newChannels = M3uParser.parse(content, playlistId)

            if (newChannels.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Refreshed playlist contains no channels. Old channels were kept."))
            }

            // Map and restore favorite flags
            val updatedChannels = newChannels.map { ch ->
                if (favoriteUrls.contains(ch.url.trim())) {
                    ch.copy(isFavorite = true)
                } else {
                    ch
                }
            }

            db.channelDao().deleteChannelsByPlaylist(playlistId)
            db.channelDao().insertChannels(updatedChannels)
            db.playlistDao().insertPlaylist(
                playlist.copy(
                    channelCount = updatedChannels.size,
                    lastRefreshed = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(channelId: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        db.channelDao().setFavorite(channelId, !currentFavorite)
    }

    suspend fun addRecentlyWatched(channel: Channel, playlistId: String) = withContext(Dispatchers.IO) {
        db.recentlyWatchedDao().insertWatched(
            RecentlyWatchedEntity(
                channelId = channel.id,
                playlistId = playlistId,
                name = channel.name,
                url = channel.url,
                logo = channel.logo,
                groupTitle = channel.group,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeRecentlyWatched(channelId: String) = withContext(Dispatchers.IO) {
        db.recentlyWatchedDao().removeWatched(channelId)
    }

    suspend fun hideChannel(channel: Channel) = withContext(Dispatchers.IO) {
        db.hiddenDao().hideChannel(
            HiddenChannelEntity(
                channelId = channel.id,
                name = channel.name,
                url = channel.url,
                groupTitle = channel.group
            )
        )
    }

    suspend fun hideCategory(categoryName: String) = withContext(Dispatchers.IO) {
        db.hiddenDao().hideCategory(
            HiddenCategoryEntity(
                categoryName = categoryName
            )
        )
    }

    suspend fun unhideChannel(channelId: String) = withContext(Dispatchers.IO) {
        db.hiddenDao().unhideChannel(channelId)
    }

    suspend fun unhideCategory(categoryName: String) = withContext(Dispatchers.IO) {
        db.hiddenDao().unhideCategory(categoryName)
    }

    suspend fun unhideAllChannels() = withContext(Dispatchers.IO) {
        db.hiddenDao().unhideAllChannels()
    }

    suspend fun unhideAllCategories() = withContext(Dispatchers.IO) {
        db.hiddenDao().unhideAllCategories()
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        db.channelDao().deleteChannelsByPlaylist(playlistId)
        db.playlistDao().deletePlaylist(playlistId)
    }

    suspend fun clearFavorites() = withContext(Dispatchers.IO) {
        db.channelDao().clearAllFavorites()
    }

    suspend fun clearRecentlyWatched() = withContext(Dispatchers.IO) {
        db.recentlyWatchedDao().clearAll()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        db.playlistDao().deleteAllPlaylists()
        db.channelDao().deleteAllChannels()
        db.recentlyWatchedDao().clearAll()
        db.hiddenDao().unhideAllChannels()
        db.hiddenDao().unhideAllCategories()
    }
}
