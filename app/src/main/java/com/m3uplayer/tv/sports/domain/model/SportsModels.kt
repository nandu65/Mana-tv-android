package com.m3uplayer.tv.sports.domain.model

enum class SportsProvider(val displayName: String, val badgeColorHex: Long, val appPackage: String?, val defaultWebUrl: String) {
    ALL("All Providers", 0xFF3B82F6, null, ""),
    FANCODE("FanCode", 0xFFF97316, "com.fancode.android", "https://www.fancode.com"),
    SONYLIV("SonyLIV", 0xFF0EA5E9, "com.sonyliv", "https://www.sonyliv.com"),
    TATAPLAY("Tata Play", 0xFF8B5CF6, "com.ryzmedia.tatasky", "https://www.tataplay.com"),
    WILLOW("Willow", 0xFF10B981, "tv.willow", "https://www.willow.tv")
}

enum class SportsCategory(val displayName: String) {
    CRICKET("Cricket"),
    FOOTBALL("Football"),
    RACING("Racing"),
    TENNIS("Tennis"),
    BASKETBALL("Basketball"),
    OTHER("Other")
}

enum class EventStatus {
    LIVE, UPCOMING, COMPLETED
}

data class TeamInfo(
    val name: String,
    val shortName: String? = null,
    val logoUrl: String? = null,
    val score: String? = null
)

data class SportsEvent(
    val id: String,
    val provider: SportsProvider,
    val title: String,
    val tournamentName: String,
    val category: SportsCategory,
    val status: EventStatus,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val posterUrl: String? = null,
    val thumbnailUrl: String? = null,
    val homeTeam: TeamInfo? = null,
    val awayTeam: TeamInfo? = null,
    val venue: String? = null,
    val streamSupportedInApp: Boolean = true,
    val streamUrl: String? = null,
    val externalWebUrl: String,
    val externalAppDeepLink: String? = null,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)

data class SportsChannel(
    val id: String,
    val provider: SportsProvider,
    val name: String,
    val logoUrl: String? = null,
    val category: SportsCategory = SportsCategory.OTHER,
    val currentProgram: String? = null,
    val streamSupportedInApp: Boolean = true,
    val streamUrl: String? = null,
    val externalWebUrl: String? = null,
    val externalAppDeepLink: String? = null
)

