package com.m3uplayer.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m3uplayer.tv.domain.model.Channel
import com.m3uplayer.tv.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChannelCard(
    channel: Channel,
    isSelected: Boolean = false,
    onSelect: (Channel) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onLongClick: ((Channel) -> Unit)? = null,
    showPlaylistBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiPrefs = LocalUiPreferences.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        label = "card_scale"
    )

    val borderColor = when {
        isFocused -> uiPrefs.accentColor.primary
        isSelected -> uiPrefs.accentColor.glow
        else -> BorderSubtle
    }

    val backgroundColor = when {
        isFocused -> uiPrefs.theme.bgCardHover
        isSelected -> Color(0xFF1B263B)
        else -> uiPrefs.theme.bgCard
    }

    Box(
        modifier = modifier
            .scale(scale)
            .width(uiPrefs.cardSize.width)
            .height(uiPrefs.cardSize.height)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onSelect(channel) },
                onLongClick = { onLongClick?.invoke(channel) }
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Channel Logo + Channel Number + Favorite Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiPrefs.showLogos) {
                        ChannelLogo(
                            logoUrl = channel.logo,
                            name = channel.name,
                            size = if (uiPrefs.cardSize == CardSize.COMPACT) 36.dp else 42.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    channel.channelNumber?.let { chNo ->
                        Text(
                            text = "CH $chNo",
                            color = uiPrefs.accentColor.glow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0xFF0F1522), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite(channel.id, channel.isFavorite) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (channel.isFavorite) FavoriteYellow else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom Section: Channel Name, Category/Country, EPG (if present)
            Column {
                Text(
                    text = channel.name,
                    color = if (isFocused) Color.White else TextPrimary,
                    fontSize = if (uiPrefs.cardSize == CardSize.COMPACT) 12.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Optional EPG metadata if available
                channel.currentProgram?.let { prog ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = prog,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    channel.programProgress?.let { progVal ->
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { progVal },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp)),
                            color = uiPrefs.accentColor.primary,
                            trackColor = Color(0xFF222B3D),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showPlaylistBadge && channel.playlistName != null) {
                            "${channel.playlistName} • ${channel.group}"
                        } else {
                            channel.group
                        },
                        color = TextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .background(Color(0xFF0C0F17), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )

                    channel.country?.let {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it.uppercase(),
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
