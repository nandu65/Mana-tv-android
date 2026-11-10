package com.m3uplayer.tv.sports.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m3uplayer.tv.sports.domain.model.SportsProvider
import com.m3uplayer.tv.ui.theme.LocalUiPreferences

@Composable
fun ProviderFilterBar(
    selectedProvider: SportsProvider,
    onSelectProvider: (SportsProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    val providers = remember { SportsProvider.values().toList() }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(providers) { provider ->
            ProviderPill(
                provider = provider,
                isSelected = provider == selectedProvider,
                onSelect = { onSelectProvider(provider) }
            )
        }
    }
}

@Composable
private fun ProviderPill(
    provider: SportsProvider,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val uiPrefs = LocalUiPreferences.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        label = "pill_scale"
    )

    val bg = when {
        isFocused -> uiPrefs.accentColor.primary
        isSelected -> Color(0xFF1E283C)
        else -> Color(0xFF141C2C)
    }

    val border = when {
        isFocused -> Color.White
        isSelected -> uiPrefs.accentColor.glow
        else -> Color(0x22FFFFFF)
    }

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(if (isFocused) 2.dp else 1.dp, border, RoundedCornerShape(20.dp))
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (provider != SportsProvider.ALL) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(provider.badgeColorHex))
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = provider.displayName,
            color = if (isFocused || isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}

