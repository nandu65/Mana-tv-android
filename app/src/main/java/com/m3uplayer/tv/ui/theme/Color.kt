package com.m3uplayer.tv.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppTheme(val label: String, val bgMain: Color, val bgCard: Color, val bgCardHover: Color) {
    DARK("Dark Modern", Color(0xFF0D0F14), Color(0xFF151821), Color(0xFF1E2433)),
    OLED_BLACK("OLED Pure Black", Color(0xFF000000), Color(0xFF0F0F11), Color(0xFF1A1A1E)),
    BLUE("Midnight Blue", Color(0xFF0A1124), Color(0xFF111C38), Color(0xFF1E2E56))
}

enum class AccentColor(val label: String, val primary: Color, val glow: Color) {
    BLUE("Electric Blue", Color(0xFF3B82F6), Color(0xFF60A5FA)),
    PURPLE("Vibrant Purple", Color(0xFF8B5CF6), Color(0xFFA78BFA)),
    GREEN("Emerald Green", Color(0xFF10B981), Color(0xFF34D399)),
    RED("Ruby Red", Color(0xFFEF4444), Color(0xFFF87171))
}

enum class CardSize(val label: String, val width: Dp, val height: Dp) {
    COMPACT("Compact", 150.dp, 110.dp),
    NORMAL("Normal", 180.dp, 130.dp),
    LARGE("Large", 215.dp, 150.dp)
}

data class UiPreferences(
    val theme: AppTheme = AppTheme.DARK,
    val accentColor: AccentColor = AccentColor.BLUE,
    val cardSize: CardSize = CardSize.NORMAL,
    val showLogos: Boolean = true
)

val BorderSubtle = Color(0xFF1F2430)
val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val FavoriteYellow = Color(0xFFFACC15)
val LiveRed = Color(0xFFEF4444)

// Defaults
val BgMain = Color(0xFF0D0F14)
val BgCard = Color(0xFF151821)
val BgCardHover = Color(0xFF1E2433)
val AccentBlue = Color(0xFF3B82F6)
val AccentBlueGlow = Color(0xFF60A5FA)
