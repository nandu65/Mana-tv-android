package com.m3uplayer.tv.data.local

import androidx.room.*
import com.m3uplayer.tv.data.model.HiddenCategoryEntity
import com.m3uplayer.tv.data.model.HiddenChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenDao {
    @Query("SELECT * FROM hidden_channels ORDER BY hiddenAt DESC")
    fun getHiddenChannels(): Flow<List<HiddenChannelEntity>>

    @Query("SELECT * FROM hidden_categories ORDER BY hiddenAt DESC")
    fun getHiddenCategories(): Flow<List<HiddenCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideChannel(channel: HiddenChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideCategory(category: HiddenCategoryEntity)

    @Query("DELETE FROM hidden_channels WHERE channelId = :channelId")
    suspend fun unhideChannel(channelId: String)

    @Query("DELETE FROM hidden_categories WHERE categoryName = :categoryName")
    suspend fun unhideCategory(categoryName: String)

    @Query("DELETE FROM hidden_channels")
    suspend fun unhideAllChannels()

    @Query("DELETE FROM hidden_categories")
    suspend fun unhideAllCategories()
}
