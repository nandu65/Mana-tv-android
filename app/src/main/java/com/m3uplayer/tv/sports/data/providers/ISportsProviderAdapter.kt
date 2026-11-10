package com.m3uplayer.tv.sports.data.providers

import com.m3uplayer.tv.sports.domain.model.SportsChannel
import com.m3uplayer.tv.sports.domain.model.SportsEvent
import com.m3uplayer.tv.sports.domain.model.SportsProvider

interface ISportsProviderAdapter {
    val provider: SportsProvider
    suspend fun isAvailable(): Boolean
    suspend fun fetchEvents(): List<SportsEvent>
    suspend fun fetchChannels(): List<SportsChannel>
}

