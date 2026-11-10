package com.m3uplayer.tv.util

import android.content.Context
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * ThumbnailCacheManager
 * Automatically caps thumbnail storage to 80 MB and purges any cached
 * image/thumbnail file older than 7 days from device storage.
 */
object ThumbnailCacheManager {
    private const val TAG = "ThumbnailCache"
    const val MAX_AGE_DAYS = 7L
    private const val SEVEN_DAYS_MS = MAX_AGE_DAYS * 24L * 60L * 60L * 1000L
    private const val MAX_DISK_CACHE_BYTES = 80L * 1024L * 1024L // 80 MB cap to protect TV storage

    fun initialize(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, "image_cache")
            val httpCacheDir = File(context.cacheDir, "image_http_cache")

            val okHttpClient = OkHttpClient.Builder()
                .cache(Cache(httpCacheDir, 40L * 1024L * 1024L))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val imageLoader = ImageLoader.Builder(context)
                .memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizePercent(0.20)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir)
                        .maxSizeBytes(MAX_DISK_CACHE_BYTES)
                        .build()
                }
                .okHttpClient(okHttpClient)
                .respectCacheHeaders(false)
                .build()

            Coil.setImageLoader(imageLoader)
            Log.d(TAG, "Coil ImageLoader initialized with 80MB disk cache limit and 7-day TTL policy")

            // Automatically clean up any thumbnails older than 7 days
            cleanExpiredThumbnails(context)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize ThumbnailCacheManager", e)
        }
    }

    fun cleanExpiredThumbnails(context: Context) {
        Thread {
            try {
                val now = System.currentTimeMillis()
                val targetDirs = listOf(
                    File(context.cacheDir, "image_cache"),
                    File(context.cacheDir, "image_http_cache"),
                    File(context.cacheDir, "coil")
                )

                var deletedCount = 0
                var freedBytes = 0L

                targetDirs.forEach { dir ->
                    if (dir.exists() && dir.isDirectory) {
                        dir.walkTopDown().forEach { file ->
                            if (file.isFile) {
                                val age = now - file.lastModified()
                                if (age > SEVEN_DAYS_MS) {
                                    val size = file.length()
                                    if (file.delete()) {
                                        deletedCount++
                                        freedBytes += size
                                    }
                                }
                            }
                        }
                    }
                }
                if (deletedCount > 0) {
                    Log.d(TAG, "Thumbnail cache cleanup: purged $deletedCount files older than 7 days (${freedBytes / 1024} KB freed)")
                } else {
                    Log.d(TAG, "Thumbnail cache cleanup: all thumbnails are within 7-day freshness window")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error cleaning thumbnail cache", e)
            }
        }.start()
    }
}
