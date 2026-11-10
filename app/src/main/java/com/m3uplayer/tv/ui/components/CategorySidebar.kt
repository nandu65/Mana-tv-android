package com.m3uplayer.tv.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m3uplayer.tv.ui.theme.*

data class CategoryNav(
    val name: String,
    val count: Int,
    val icon: ImageVector = Icons.Default.Folder
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySidebar(
    categories: List<CategoryNav>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onHideCategory: ((String) -> Unit)? = null,
    onOpenSports: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiPrefs = LocalUiPreferences.current

    LazyColumn(
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(uiPrefs.theme.bgMain)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (onOpenSports != null) {
            item {
                val sportsInteractionSource = remember { MutableInteractionSource() }
                val isSportsFocused by sportsInteractionSource.collectIsFocusedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isSportsFocused) 1.03f else 1.0f,
                    label = "sports_sidebar_scale"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSportsFocused) Color(0xFFEF4444) else Color(0x33EF4444))
                        .border(
                            width = if (isSportsFocused) 1.5.dp else 1.dp,
                            color = if (isSportsFocused) Color.White else Color(0x88EF4444),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .focusable(interactionSource = sportsInteractionSource)
                        .clickable(
                            interactionSource = sportsInteractionSource,
                            indication = null
                        ) {
                            onOpenSports()
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Live Sports",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Live Sports",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        item {
            Text(
                text = "CATEGORIES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        items(categories) { cat ->
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            val isSelected = selectedCategory == cat.name

            val scale by animateFloatAsState(
                targetValue = if (isFocused) 1.03f else 1.0f,
                label = "sidebar_scale"
            )

            val bg = when {
                isFocused -> uiPrefs.accentColor.primary
                isSelected -> Color(0xFF1E283C)
                else -> Color.Transparent
            }

            val textColor = when {
                isFocused -> Color.White
                isSelected -> uiPrefs.accentColor.glow
                else -> TextPrimary
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .border(
                        width = if (isFocused) 1.5.dp else 0.dp,
                        color = if (isFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .focusable(interactionSource = interactionSource)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelectCategory(cat.name) },
                        onLongClick = {
                            if (cat.name != "All Channels" && cat.name != "Favorites" && cat.name != "Continue Watching") {
                                onHideCategory?.invoke(cat.name)
                            }
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        tint = if (isFocused) Color.White else if (cat.name == "Favorites") FavoriteYellow else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cat.name,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (cat.count > 0) {
                    Text(
                        text = "${cat.count}",
                        color = if (isFocused) Color.White else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                if (isFocused) Color(0x33000000) else Color(0xFF121622),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
