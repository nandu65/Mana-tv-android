package com.m3uplayer.tv.data.local

import androidx.room.*
import com.m3uplayer.tv.data.model.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    fun getChannelsByPlaylist(playlistId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND groupTitle = :group")
    fun getChannelsByGroup(playlistId: String, group: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1")
    fun getAllFavorites(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE playlistId = :playlistId")
    fun getCategories(playlistId: String): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%' OR tvgId LIKE '%' || :query || '%' OR country LIKE '%' || :query || '%' OR language LIKE '%' || :query || '%' OR channelNumber LIKE '%' || :query || '%'")
    fun searchAllChannels(query: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun setFavorite(channelId: String, isFavorite: Boolean)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE url = :url")
    suspend fun setFavoriteByUrl(url: String, isFavorite: Boolean)

    @Query("UPDATE channels SET isFavorite = 0")
    suspend fun clearAllFavorites()

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: String)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
}
