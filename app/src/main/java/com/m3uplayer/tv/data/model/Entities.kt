package com.m3uplayer.tv.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.m3uplayer.tv.domain.model.Channel
import com.m3uplayer.tv.domain.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sourceType: String,
    val sourceUrl: String?,
    val channelCount: Int,
    val lastRefreshed: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Playlist = Playlist(
        id = id,
        name = name,
        sourceType = sourceType,
        sourceUrl = sourceUrl,
        channelCount = channelCount,
        lastRefreshed = lastRefreshed
    )
}

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val name: String,
    val url: String,
    val logo: String?,
    val groupTitle: String,
    val language: String?,
    val country: String?,
    val channelNumber: String?,
    val tvgId: String?,
    val isFavorite: Boolean = false
) {
    fun toDomain(playlistName: String? = null): Channel = Channel(
        id = id,
        name = name,
        url = url,
        logo = logo,
        group = groupTitle,
        language = language,
        country = country,
        channelNumber = channelNumber,
        tvgId = tvgId,
        isFavorite = isFavorite,
        playlistName = playlistName
    )
}

@Entity(tableName = "recently_watched")
data class RecentlyWatchedEntity(
    @PrimaryKey val channelId: String,
    val playlistId: String,
    val name: String,
    val url: String,
    val logo: String?,
    val groupTitle: String,
    val watchedAt: Long = System.currentTimeMillis()
) {
    fun toDomainChannel(): Channel = Channel(
        id = channelId,
        name = name,
        url = url,
        logo = logo,
        group = groupTitle
    )
}

@Entity(tableName = "hidden_channels")
data class HiddenChannelEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val url: String,
    val groupTitle: String,
    val hiddenAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hidden_categories")
data class HiddenCategoryEntity(
    @PrimaryKey val categoryName: String,
    val hiddenAt: Long = System.currentTimeMillis()
)
