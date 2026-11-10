package com.m3uplayer.tv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.m3uplayer.tv.data.model.ChannelEntity
import com.m3uplayer.tv.data.model.HiddenCategoryEntity
import com.m3uplayer.tv.data.model.HiddenChannelEntity
import com.m3uplayer.tv.data.model.PlaylistEntity
import com.m3uplayer.tv.data.model.RecentlyWatchedEntity

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        RecentlyWatchedEntity::class,
        HiddenChannelEntity::class,
        HiddenCategoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun recentlyWatchedDao(): RecentlyWatchedDao
    abstract fun hiddenDao(): HiddenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "m3u_player_tv.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
