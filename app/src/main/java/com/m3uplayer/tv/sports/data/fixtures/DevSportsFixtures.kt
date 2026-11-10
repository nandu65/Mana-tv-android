package com.m3uplayer.tv.sports.data.fixtures

import com.m3uplayer.tv.sports.domain.model.EventStatus
import com.m3uplayer.tv.sports.domain.model.SportsCategory
import com.m3uplayer.tv.sports.domain.model.SportsChannel
import com.m3uplayer.tv.sports.domain.model.SportsEvent
import com.m3uplayer.tv.sports.domain.model.SportsProvider
import com.m3uplayer.tv.sports.domain.model.TeamInfo

/**
 * DEVELOPMENT FIXTURES ONLY
 * Clearly labelled test data used exclusively for UI development and preview
 * when live provider upstream feeds are unreachable or unconfigured.
 */
object DevSportsFixtures {

    val events = listOf(
        // --- FANCODE ---
        SportsEvent(
            id = "dev_fancode_live_1",
            provider = SportsProvider.FANCODE,
            title = "Barbados Royals vs Trinbago Knight Riders",
            tournamentName = "Caribbean Premier League 2025",
            category = SportsCategory.CRICKET,
            status = EventStatus.LIVE,
            startTimeEpochMs = System.currentTimeMillis() - 45 * 60 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "Barbados Royals", shortName = "BR", score = "162/4 (17.2)"),
            awayTeam = TeamInfo(name = "Trinbago Knight Riders", shortName = "TKR", score = "178/6 (20.0)"),
            venue = "Kensington Oval, Bridgetown",
            streamSupportedInApp = true,
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            externalWebUrl = "https://www.fancode.com",
            externalAppDeepLink = "fancode://match/dev_live_1"
        ),
        SportsEvent(
            id = "dev_fancode_upcoming_1",
            provider = SportsProvider.FANCODE,
            title = "Surrey vs Yorkshire",
            tournamentName = "Vitality Blast T20",
            category = SportsCategory.CRICKET,
            status = EventStatus.UPCOMING,
            startTimeEpochMs = System.currentTimeMillis() + 4 * 3600 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1531415074968-036ba1b575da?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "Surrey", shortName = "SUR"),
            awayTeam = TeamInfo(name = "Yorkshire", shortName = "YOR"),
            venue = "The Oval, London",
            streamSupportedInApp = false,
            externalWebUrl = "https://www.fancode.com",
            externalAppDeepLink = "fancode://match/dev_up_1"
        ),

        // --- SONYLIV ---
        SportsEvent(
            id = "dev_sonyliv_live_1",
            provider = SportsProvider.SONYLIV,
            title = "Arsenal vs Manchester City",
            tournamentName = "UEFA Champions League",
            category = SportsCategory.FOOTBALL,
            status = EventStatus.LIVE,
            startTimeEpochMs = System.currentTimeMillis() - 30 * 60 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "Arsenal FC", shortName = "ARS", score = "1"),
            awayTeam = TeamInfo(name = "Manchester City", shortName = "MCI", score = "1"),
            venue = "Emirates Stadium, London",
            streamSupportedInApp = true,
            streamUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            externalWebUrl = "https://www.sonyliv.com/sports",
            externalAppDeepLink = "sonyliv://sports/dev_live_1"
        ),
        SportsEvent(
            id = "dev_sonyliv_upcoming_1",
            provider = SportsProvider.SONYLIV,
            title = "Real Madrid vs Bayern Munich",
            tournamentName = "UEFA Champions League Semi-Final",
            category = SportsCategory.FOOTBALL,
            status = EventStatus.UPCOMING,
            startTimeEpochMs = System.currentTimeMillis() + 24 * 3600 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "Real Madrid", shortName = "RMA"),
            awayTeam = TeamInfo(name = "Bayern Munich", shortName = "BAY"),
            venue = "Santiago Bernabéu, Madrid",
            streamSupportedInApp = false,
            externalWebUrl = "https://www.sonyliv.com/sports",
            externalAppDeepLink = "sonyliv://sports/dev_up_1"
        ),
        SportsEvent(
            id = "dev_sonyliv_completed_1",
            provider = SportsProvider.SONYLIV,
            title = "Novak Djokovic vs Carlos Alcaraz",
            tournamentName = "Australian Open Final",
            category = SportsCategory.TENNIS,
            status = EventStatus.COMPLETED,
            startTimeEpochMs = System.currentTimeMillis() - 48 * 3600 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "Novak Djokovic", score = "3"),
            awayTeam = TeamInfo(name = "Carlos Alcaraz", score = "2"),
            streamSupportedInApp = false,
            externalWebUrl = "https://www.sonyliv.com"
        ),

        // --- TATA PLAY ---
        SportsEvent(
            id = "dev_tataplay_live_1",
            provider = SportsProvider.TATAPLAY,
            title = "Monaco Grand Prix - Main Race",
            tournamentName = "Formula 1 World Championship 2025",
            category = SportsCategory.RACING,
            status = EventStatus.LIVE,
            startTimeEpochMs = System.currentTimeMillis() - 50 * 60 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "Verstappen (Red Bull)", score = "Lap 42/78"),
            awayTeam = TeamInfo(name = "Leclerc (Ferrari)", score = "+1.824s"),
            venue = "Circuit de Monaco, Monte Carlo",
            streamSupportedInApp = true,
            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            externalWebUrl = "https://www.tataplay.com",
            externalAppDeepLink = "tataplay://live/dev_f1"
        ),

        // --- WILLOW ---
        SportsEvent(
            id = "dev_willow_live_1",
            provider = SportsProvider.WILLOW,
            title = "England vs Australia",
            tournamentName = "The Ashes Test Series",
            category = SportsCategory.CRICKET,
            status = EventStatus.LIVE,
            startTimeEpochMs = System.currentTimeMillis() - 2 * 3600 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "England", shortName = "ENG", score = "284 & 142/3"),
            awayTeam = TeamInfo(name = "Australia", shortName = "AUS", score = "312"),
            venue = "Lord's Cricket Ground, London",
            streamSupportedInApp = true,
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            externalWebUrl = "https://www.willow.tv",
            externalAppDeepLink = "willow://live/dev_ashes"
        ),
        SportsEvent(
            id = "dev_willow_upcoming_1",
            provider = SportsProvider.WILLOW,
            title = "India vs South Africa - 1st ODI",
            tournamentName = "Bilateral Series 2025",
            category = SportsCategory.CRICKET,
            status = EventStatus.UPCOMING,
            startTimeEpochMs = System.currentTimeMillis() + 12 * 3600 * 1000,
            posterUrl = "https://images.unsplash.com/photo-1531415074968-036ba1b575da?auto=format&fit=crop&w=800&q=80",
            homeTeam = TeamInfo(name = "India", shortName = "IND"),
            awayTeam = TeamInfo(name = "South Africa", shortName = "SA"),
            venue = "Eden Gardens, Kolkata",
            streamSupportedInApp = true,
            externalWebUrl = "https://www.willow.tv",
            externalAppDeepLink = "willow://live/dev_ind_sa"
        )
    )

    val channels = listOf(
        SportsChannel(
            id = "dev_ch_fancode_live",
            provider = SportsProvider.FANCODE,
            name = "FanCode Live HD",
            logoUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=120&q=80",
            category = SportsCategory.CRICKET,
            currentProgram = "Caribbean Premier League Live",
            streamSupportedInApp = true,
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            externalWebUrl = "https://www.fancode.com"
        ),
        SportsChannel(
            id = "dev_ch_sony_sports_ten_1",
            provider = SportsProvider.SONYLIV,
            name = "Sony Sports Ten 1 HD",
            logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=120&q=80",
            category = SportsCategory.FOOTBALL,
            currentProgram = "UEFA Champions League Live",
            streamSupportedInApp = true,
            streamUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            externalWebUrl = "https://www.sonyliv.com"
        ),
        SportsChannel(
            id = "dev_ch_tata_sports_1",
            provider = SportsProvider.TATAPLAY,
            name = "Tata Play Sports HD",
            logoUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=120&q=80",
            category = SportsCategory.RACING,
            currentProgram = "Formula 1 Live Practice",
            streamSupportedInApp = true,
            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            externalWebUrl = "https://www.tataplay.com"
        ),
        SportsChannel(
            id = "dev_ch_willow_hd",
            provider = SportsProvider.WILLOW,
            name = "Willow Cricket HD",
            logoUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=120&q=80",
            category = SportsCategory.CRICKET,
            currentProgram = "The Ashes 2025 Live",
            streamSupportedInApp = true,
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            externalWebUrl = "https://www.willow.tv"
        )
    )
}

