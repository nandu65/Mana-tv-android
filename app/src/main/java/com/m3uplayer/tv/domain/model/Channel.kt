package com.m3uplayer.tv.domain.model

data class Channel(
    val id: String,
    val name: String,
    val url: String = "",
    val logo: String? = null,
    val group: String = "Other",
    val language: String? = null,
    val country: String? = null,
    val channelNumber: String? = null,
    val tvgId: String? = null,
    val isFavorite: Boolean = false,
    val playlistName: String? = null,
    // EPG-ready attributes (displayed only when metadata is available)
    val currentProgram: String? = null,
    val programStartTime: String? = null,
    val programEndTime: String? = null,
    val programProgress: Float? = null // 0.0f to 1.0f
)

data class Playlist(
    val id: String,
    val name: String,
    val sourceType: String, // "url", "file", "sample"
    val sourceUrl: String? = null,
    val channelCount: Int = 0,
    val lastRefreshed: Long = System.currentTimeMillis()
)
