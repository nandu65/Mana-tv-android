package com.m3uplayer.tv.sports.data.providers

import com.m3uplayer.tv.sports.data.fixtures.DevSportsFixtures
import com.m3uplayer.tv.sports.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class FanCodeAdapter : ISportsProviderAdapter {
    override val provider: SportsProvider = SportsProvider.FANCODE

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val catalogueUrl = "https://raw.githubusercontent.com/drmlive/fancode-live-events/main/fancode.json"

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
                val matchesArray = json.optJSONArray("matches") ?: return@withContext DevSportsFixtures.events.filter { it.provider == SportsProvider.FANCODE }

                val events = mutableListOf<SportsEvent>()
                for (i in 0 until matchesArray.length()) {
                    val obj = matchesArray.getJSONObject(i)
                    val matchId = obj.optString("match_id", i.toString())
                    val categoryStr = obj.optString("event_category", "Cricket")
                    val title = obj.optString("match_name", obj.optString("title", "FanCode Event"))
                    val tournament = obj.optString("event_name", "FanCode Tournament")
                    val poster = obj.optString("src", "")
                    val team1 = obj.optString("team_1", "")
                    val team2 = obj.optString("team_2", "")
                    val statusStr = obj.optString("status", "UPCOMING")
                    val startTimeStr = obj.optString("startTime", "")

                    val category = normalizeCategory(categoryStr)
                    val status = normalizeStatus(statusStr)
                    val startTime = parseStartTime(startTimeStr)

                    val streamUrl = obj.optString("stream_url", obj.optString("dai_url", obj.optString("video_url", "")))
                    val isPlayable = streamUrl.isNotBlank() && status == EventStatus.LIVE

                    events.add(
                        SportsEvent(
                            id = "fancode_$matchId",
                            provider = SportsProvider.FANCODE,
                            title = title,
                            tournamentName = tournament,
                            category = category,
                            status = status,
                            startTimeEpochMs = startTime,
                            posterUrl = if (poster.isNotBlank()) poster else null,
                            homeTeam = if (team1.isNotBlank()) TeamInfo(name = team1, shortName = team1) else null,
                            awayTeam = if (team2.isNotBlank()) TeamInfo(name = team2, shortName = team2) else null,
                            streamSupportedInApp = isPlayable,
                            streamUrl = if (isPlayable) streamUrl else null,
                            externalWebUrl = "https://www.fancode.com",
                            externalAppDeepLink = "fancode://match/$matchId"
                        )
                    )
                }

                if (events.isNotEmpty()) events else DevSportsFixtures.events.filter { it.provider == SportsProvider.FANCODE }
            }
        } catch (_: Exception) {
            DevSportsFixtures.events.filter { it.provider == SportsProvider.FANCODE }
        }
    }

    override suspend fun fetchChannels(): List<SportsChannel> = withContext(Dispatchers.IO) {
        DevSportsFixtures.channels.filter { it.provider == SportsProvider.FANCODE }
    }

    private fun normalizeCategory(cat: String): SportsCategory {
        val lower = cat.lowercase()
        return when {
            lower.contains("cricket") -> SportsCategory.CRICKET
            lower.contains("football") || lower.contains("soccer") -> SportsCategory.FOOTBALL
            lower.contains("racing") || lower.contains("f1") -> SportsCategory.RACING
            lower.contains("tennis") -> SportsCategory.TENNIS
            lower.contains("basketball") -> SportsCategory.BASKETBALL
            else -> SportsCategory.OTHER
        }
    }

    private fun normalizeStatus(status: String): EventStatus {
        return when (status.uppercase()) {
            "LIVE" -> EventStatus.LIVE
            "COMPLETED", "ENDED", "FINISHED" -> EventStatus.COMPLETED
            else -> EventStatus.UPCOMING
        }
    }

    private fun parseStartTime(str: String): Long {
        if (str.isBlank()) return System.currentTimeMillis()
        return try {
            val format = SimpleDateFormat("hh:mm:ss a dd-MM-yyyy", Locale.US)
            format.parse(str)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
