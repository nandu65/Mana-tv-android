package com.m3uplayer.tv.sports.data.providers

import com.m3uplayer.tv.sports.data.fixtures.DevSportsFixtures
import com.m3uplayer.tv.sports.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SonyLIVAdapter : ISportsProviderAdapter {
    override val provider: SportsProvider = SportsProvider.SONYLIV

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val catalogueUrl = "https://raw.githubusercontent.com/drmlive/sliv-live-events/main/sonyliv.json"

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
                val matchesArray = json.optJSONArray("matches") ?: return@withContext DevSportsFixtures.events.filter { it.provider == SportsProvider.SONYLIV }

                val events = mutableListOf<SportsEvent>()
                for (i in 0 until matchesArray.length()) {
                    val obj = matchesArray.getJSONObject(i)
                    val contentId = obj.optString("contentId", i.toString())
                    val categoryStr = obj.optString("event_category", "Cricket")
                    val isLive = obj.optBoolean("isLive", false)
                    val matchName = obj.optString("match_name", "SonyLIV Live Event")
                    val eventName = obj.optString("event_name", "SonyLIV Tournament")
                    val poster = obj.optString("src", "")
                    val streamUrl = obj.optString("video_url", obj.optString("dai_url", obj.optString("pub_url", "")))

                    val isTokenValid = validateTokenExpiration(streamUrl)
                    val isPlayable = streamUrl.isNotBlank() && isLive && isTokenValid

                    events.add(
                        SportsEvent(
                            id = "sonyliv_$contentId",
                            provider = SportsProvider.SONYLIV,
                            title = matchName,
                            tournamentName = eventName,
                            category = normalizeCategory(categoryStr),
                            status = if (isLive) EventStatus.LIVE else EventStatus.UPCOMING,
                            startTimeEpochMs = System.currentTimeMillis(),
                            posterUrl = if (poster.isNotBlank()) poster else null,
                            streamSupportedInApp = isPlayable,
                            streamUrl = if (isPlayable) streamUrl else null,
                            externalWebUrl = "https://www.sonyliv.com/sports",
                            externalAppDeepLink = "sonyliv://live/$contentId"
                        )
                    )
                }

                if (events.isNotEmpty()) events else DevSportsFixtures.events.filter { it.provider == SportsProvider.SONYLIV }
            }
        } catch (_: Exception) {
            DevSportsFixtures.events.filter { it.provider == SportsProvider.SONYLIV }
        }
    }

    override suspend fun fetchChannels(): List<SportsChannel> = withContext(Dispatchers.IO) {
        DevSportsFixtures.channels.filter { it.provider == SportsProvider.SONYLIV }
    }

    private fun normalizeCategory(cat: String): SportsCategory {
        val lower = cat.lowercase()
        return when {
            lower.contains("cricket") -> SportsCategory.CRICKET
            lower.contains("football") -> SportsCategory.FOOTBALL
            lower.contains("tennis") -> SportsCategory.TENNIS
            lower.contains("racing") -> SportsCategory.RACING
            else -> SportsCategory.OTHER
        }
    }

    private fun validateTokenExpiration(url: String): Boolean {
        if (url.isBlank()) return false
        val regex = Regex("""exp=(\d+)""")
        val match = regex.find(url)
        if (match != null) {
            val expSec = match.groupValues[1].toLongOrNull() ?: return true
            val nowSec = System.currentTimeMillis() / 1000
            return expSec > nowSec
        }
        return true
    }
}
