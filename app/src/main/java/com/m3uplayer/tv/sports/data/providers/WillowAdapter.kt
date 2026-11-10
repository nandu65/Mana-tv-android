package com.m3uplayer.tv.sports.data.providers

import com.m3uplayer.tv.sports.data.fixtures.DevSportsFixtures
import com.m3uplayer.tv.sports.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WillowAdapter : ISportsProviderAdapter {
    override val provider: SportsProvider = SportsProvider.WILLOW

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val catalogueUrl = "https://raw.githubusercontent.com/drmlive/willow-live-events/main/willow.json"

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(catalogueUrl).head().build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun fetchEvents(): List<SportsEvent> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(catalogueUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                val body = response.body?.string() ?: throw Exception("Empty body")
                val json = JSONObject(body)
                val matchesArray = json.optJSONArray("matches") ?: return@withContext DevSportsFixtures.events.filter { it.provider == SportsProvider.WILLOW }

                val events = mutableListOf<SportsEvent>()
                for (i in 0 until matchesArray.length()) {
                    val obj = matchesArray.getJSONObject(i)
                    val matchId = obj.optString("match_id", i.toString())
                    val title = obj.optString("title", obj.optString("match_name", "Willow Cricket Match"))
                    val eventName = obj.optString("event_name", "International Cricket")
                    val poster = obj.optString("src", "")
                    val streamUrl = obj.optString("stream_url", "")
                    val isLive = obj.optString("status", "").equals("LIVE", ignoreCase = true)

                    events.add(
                        SportsEvent(
                            id = "willow_$matchId",
                            provider = SportsProvider.WILLOW,
                            title = title,
                            tournamentName = eventName,
                            category = SportsCategory.CRICKET,
                            status = if (isLive) EventStatus.LIVE else EventStatus.UPCOMING,
                            startTimeEpochMs = System.currentTimeMillis(),
                            posterUrl = if (poster.isNotBlank()) poster else null,
                            streamSupportedInApp = streamUrl.isNotBlank(),
                            streamUrl = if (streamUrl.isNotBlank()) streamUrl else null,
                            externalWebUrl = "https://www.willow.tv",
                            externalAppDeepLink = "willow://live"
                        )
                    )
                }

                if (events.isNotEmpty()) events else DevSportsFixtures.events.filter { it.provider == SportsProvider.WILLOW }
            }
        } catch (_: Exception) {
            DevSportsFixtures.events.filter { it.provider == SportsProvider.WILLOW }
        }
    }

    override suspend fun fetchChannels(): List<SportsChannel> = withContext(Dispatchers.IO) {
        DevSportsFixtures.channels.filter { it.provider == SportsProvider.WILLOW }
    }
}
