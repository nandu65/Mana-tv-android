package com.m3uplayer.tv.util

import java.util.concurrent.TimeUnit

object TimeUtils {

    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0) return "Never"
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp
        if (diffMillis < 0) return "Just now"

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> if (hours == 1L) "1 hour ago" else "${hours} hours ago"
            days < 30 -> if (days == 1L) "1 day ago" else "${days} days ago"
            else -> "Over a month ago"
        }
    }

    fun formatShortRelative(timestamp: Long): String {
        if (timestamp <= 0) return "Just now"
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp
        if (diffMillis < 0) return "now"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            else -> "${days}d"
        }
    }
}
