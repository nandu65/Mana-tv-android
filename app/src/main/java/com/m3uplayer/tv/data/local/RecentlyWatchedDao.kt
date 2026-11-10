package com.m3uplayer.tv.data.local

import androidx.room.*
import com.m3uplayer.tv.data.model.RecentlyWatchedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyWatchedDao {
    @Query("SELECT * FROM recently_watched ORDER BY watchedAt DESC LIMIT 10")
    fun getRecentlyWatched(): Flow<List<RecentlyWatchedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatched(item: RecentlyWatchedEntity)

    @Query("DELETE FROM recently_watched WHERE channelId = :channelId")
    suspend fun removeWatched(channelId: String)

    @Query("DELETE FROM recently_watched")
    suspend fun clearAll()
}
