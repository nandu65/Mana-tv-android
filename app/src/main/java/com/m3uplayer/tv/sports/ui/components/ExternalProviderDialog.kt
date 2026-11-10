package com.m3uplayer.tv.sports.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.m3uplayer.tv.sports.domain.model.SportsChannel
import com.m3uplayer.tv.sports.domain.model.SportsEvent
import com.m3uplayer.tv.ui.theme.LocalUiPreferences

@Composable
fun ExternalProviderDialog(
    itemTitle: String,
    providerName: String,
    providerColorHex: Long,
    appPackage: String?,
    webUrl: String,
    deepLink: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uiPrefs = LocalUiPreferences.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131B2C))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header badge
                Text(
                    text = providerName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(providerColorHex))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                Text(
                    text = itemTitle,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Text(
                    text = "This live stream is protected by $providerName authentication. You can open it directly in the official $providerName app or web portal.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val cancelInteractionSource = remember { MutableInteractionSource() }
                    val isCancelFocused by cancelInteractionSource.collectIsFocusedAsState()

                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCancelFocused) Color(0xFF334155) else Color(0xFF1E283C))
                            .border(if (isCancelFocused) 2.dp else 1.dp, if (isCancelFocused) Color.White else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                            .focusable(interactionSource = cancelInteractionSource)
                            .clickable(
                                interactionSource = cancelInteractionSource,
                                indication = null,
                                onClick = onDismiss
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val openInteractionSource = remember { MutableInteractionSource() }
                    val isOpenFocused by openInteractionSource.collectIsFocusedAsState()

                    // Open in Provider Button
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isOpenFocused) Color.White else Color(providerColorHex))
                            .border(if (isOpenFocused) 2.dp else 0.dp, Color.White, RoundedCornerShape(8.dp))
                            .focusable(interactionSource = openInteractionSource)
                            .clickable(
                                interactionSource = openInteractionSource,
                                indication = null
                            ) {
                                launchProvider(context, appPackage, webUrl, deepLink)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = if (isOpenFocused) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open $providerName",
                                color = if (isOpenFocused) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun launchProvider(context: Context, appPackage: String?, webUrl: String, deepLink: String?) {
    // Attempt 1: DeepLink with package
    if (!deepLink.isNullOrBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
            if (!appPackage.isNullOrBlank()) {
                intent.setPackage(appPackage)
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }

    // Attempt 2: Launch installed app by package
    if (!appPackage.isNullOrBlank()) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(appPackage)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
            return
        }
    }

    // Attempt 3: Open official web URL
    try {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        context.startActivity(webIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open browser on this TV device", Toast.LENGTH_SHORT).show()
    }
}

