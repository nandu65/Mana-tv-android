package com.m3uplayer.tv.sports.data.providers

import com.m3uplayer.tv.sports.data.fixtures.DevSportsFixtures
import com.m3uplayer.tv.sports.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TataPlayAdapter : ISportsProviderAdapter {
    override val provider: SportsProvider = SportsProvider.TATAPLAY

    private val localServerEndpoint = "http://localhost:8000/tataplay/playlist.php"

    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(localServerEndpoint).head().build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun fetchEvents(): List<SportsEvent> = withContext(Dispatchers.IO) {
        DevSportsFixtures.events.filter { it.provider == SportsProvider.TATAPLAY }
    }

    override suspend fun fetchChannels(): List<SportsChannel> = withContext(Dispatchers.IO) {
        try {
            if (isAvailable()) {
                val request = Request.Builder().url(localServerEndpoint).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: ""
                        val channels = parseM3U(body)
                        if (channels.isNotEmpty()) return@withContext channels
                    }
                }
            }
        } catch (_: Exception) {}

        DevSportsFixtures.channels.filter { it.provider == SportsProvider.TATAPLAY }
    }

    private fun parseM3U(content: String): List<SportsChannel> {
        val lines = content.lines()
        val channels = mutableListOf<SportsChannel>()
        var currentName = ""
        var currentLogo: String? = null

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(line)
                currentLogo = logoMatch?.groupValues?.get(1)
                val commaIdx = line.lastIndexOf(',')
                currentName = if (commaIdx != -1) line.substring(commaIdx + 1).trim() else "Tata Play Sports"
            } else if (line.startsWith("http://", ignoreCase = true) || line.startsWith("https://", ignoreCase = true)) {
                val lower = currentName.lowercase()
                if (lower.contains("sports") || lower.contains("cricket") || lower.contains("star")) {
                    channels.add(
                        SportsChannel(
                            id = "tataplay_${channels.size + 1}",
                            provider = SportsProvider.TATAPLAY,
                            name = currentName,
                            logoUrl = currentLogo,
                            category = SportsCategory.OTHER,
                            streamSupportedInApp = true,
                            streamUrl = line,
                            externalWebUrl = "https://watch.tataplay.com",
                            externalAppDeepLink = "tataplay://live"
                        )
                    )
                }
                currentName = ""
                currentLogo = null
            }
        }
        return channels
    }
}
