package com.m3uplayer.tv.data.parser

import com.m3uplayer.tv.data.model.ChannelEntity
import java.net.URI
import java.util.regex.Pattern

object M3uParser {

    private val ATTR_PATTERN_CACHE = mutableMapOf<String, Pattern>()

    private fun extractAttribute(line: String, attrName: String): String? {
        val pattern = ATTR_PATTERN_CACHE.getOrPut(attrName) {
            Pattern.compile("""$attrName=["']([^"']*)["']|$attrName=([^\s,]+)""", Pattern.CASE_INSENSITIVE)
        }
        val matcher = pattern.matcher(line)
        if (matcher.find()) {
            return (matcher.group(1) ?: matcher.group(2))?.trim()
        }
        return null
    }

    private fun generateStableId(tvgId: String?, name: String, url: String): String {
        if (!tvgId.isNullOrBlank()) {
            return "tvg_${tvgId.trim().lowercase()}"
        }
        val raw = "${name.trim().lowercase()}__${url.trim().lowercase()}"
        var hash = 0
        for (ch in raw) {
            hash = (hash shl 5) - hash + ch.code
        }
        return "ch_${Math.abs(hash).toString(36)}"
    }

    fun parse(content: String, playlistId: String): List<ChannelEntity> {
        if (content.isBlank()) return emptyList()

        val lines = content.lines()
        val channels = mutableListOf<ChannelEntity>()
        val seenUrls = mutableSetOf<String>()

        var currentExtinf: String? = null
        var currentGroupFromTag: String? = null

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                currentExtinf = line
                continue
            }

            if (line.startsWith("#EXTGRP:", ignoreCase = true)) {
                currentGroupFromTag = line.substring(8).trim()
                continue
            }

            if (line.startsWith("#")) {
                continue
            }

            // Stream URL line
            if (line.startsWith("http://", ignoreCase = true) ||
                line.startsWith("https://", ignoreCase = true) ||
                line.startsWith("rtmp://", ignoreCase = true) ||
                line.startsWith("rtsp://", ignoreCase = true) ||
                line.contains("://") ||
                line.endsWith(".m3u8", ignoreCase = true) ||
                line.endsWith(".mp4", ignoreCase = true)
            ) {
                if (seenUrls.contains(line)) {
                    currentExtinf = null
                    currentGroupFromTag = null
                    continue
                }

                var tvgId: String? = null
                var tvgName: String? = null
                var tvgLogo: String? = null
                var groupTitle: String? = null
                var tvgLanguage: String? = null
                var tvgCountry: String? = null
                var tvgChno: String? = null
                var displayName: String? = null

                currentExtinf?.let { ext ->
                    tvgId = extractAttribute(ext, "tvg-id")
                    tvgName = extractAttribute(ext, "tvg-name")
                    tvgLogo = extractAttribute(ext, "tvg-logo") ?: extractAttribute(ext, "logo")
                    groupTitle = extractAttribute(ext, "group-title") ?: extractAttribute(ext, "group")
                    tvgLanguage = extractAttribute(ext, "tvg-language") ?: extractAttribute(ext, "language")
                    tvgCountry = extractAttribute(ext, "tvg-country") ?: extractAttribute(ext, "country")
                    tvgChno = extractAttribute(ext, "tvg-chno") ?: extractAttribute(ext, "chno")

                    val commaIdx = ext.lastIndexOf(',')
                    if (commaIdx != -1 && commaIdx < ext.length - 1) {
                        displayName = ext.substring(commaIdx + 1).trim()
                    }
                }

                if (groupTitle.isNullOrBlank() && !currentGroupFromTag.isNullOrBlank()) {
                    groupTitle = currentGroupFromTag
                }

                // Name Fallback
                var resolvedName = tvgName?.takeIf { it.isNotBlank() }
                    ?: displayName?.takeIf { it.isNotBlank() }

                if (resolvedName.isNullOrBlank()) {
                    resolvedName = try {
                        val uri = URI(line)
                        uri.path?.split('/')?.lastOrNull()?.takeIf { it.isNotBlank() } ?: uri.host ?: "Channel ${channels.size + 1}"
                    } catch (_: Exception) {
                        "Channel ${channels.size + 1}"
                    }
                }

                val resolvedGroup = if (!groupTitle.isNullOrBlank()) groupTitle!!.trim() else "Other"
                val resolvedLogo = tvgLogo?.takeIf { it.isNotBlank() }?.trim()

                val stableId = generateStableId(tvgId, resolvedName, line)

                channels.add(
                    ChannelEntity(
                        id = stableId,
                        playlistId = playlistId,
                        name = resolvedName,
                        url = line,
                        logo = resolvedLogo,
                        groupTitle = resolvedGroup,
                        language = tvgLanguage?.trim(),
                        country = tvgCountry?.trim(),
                        channelNumber = tvgChno?.trim(),
                        tvgId = tvgId?.trim(),
                        isFavorite = false
                    )
                )

                seenUrls.add(line)
                currentExtinf = null
                currentGroupFromTag = null
            }
        }

        return channels
    }
}
