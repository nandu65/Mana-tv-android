package com.m3uplayer.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ChannelLogo(
    logoUrl: String?,
    name: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    var isError by remember(logoUrl) { mutableStateOf(false) }

    val initials = remember(name) {
        val clean = name.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim()
        val parts = clean.split(Regex("\\s+")).filter { it.isNotEmpty() }
        when {
            parts.isEmpty() -> "TV"
            parts.size == 1 -> parts[0].take(3).uppercase()
            else -> "${parts[0].first()}${parts[1].first()}".uppercase()
        }
    }

    val gradient = remember(name) {
        val hash = name.fold(0) { acc, c -> (acc shl 5) - acc + c.code }
        val palettes = listOf(
            listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
            listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6)),
            listOf(Color(0xFF701A75), Color(0xFFD946EF)),
            listOf(Color(0xFF831843), Color(0xFFEC4899)),
            listOf(Color(0xFF14532D), Color(0xFF22C55E)),
            listOf(Color(0xFF064E3B), Color(0xFF10B981)),
            listOf(Color(0xFF164E63), Color(0xFF06B6D4))
        )
        val selected = palettes[Math.abs(hash) % palettes.size]
        Brush.linearGradient(selected)
    }

    if (logoUrl.isNullOrBlank() || isError) {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (size < 40.dp) 11.sp else 14.sp
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF151821)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = logoUrl,
                contentDescription = "$name logo",
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit,
                onError = { isError = true }
            )
        }
    }
}
