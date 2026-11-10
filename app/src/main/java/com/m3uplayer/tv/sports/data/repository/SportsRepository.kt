package com.m3uplayer.tv.sports.data.repository

import com.m3uplayer.tv.sports.data.providers.*
import com.m3uplayer.tv.sports.domain.model.SportsChannel
import com.m3uplayer.tv.sports.domain.model.SportsEvent
import com.m3uplayer.tv.sports.domain.model.SportsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SportsRepository {

    private val adapters: Map<SportsProvider, ISportsProviderAdapter> = mapOf(
        SportsProvider.FANCODE to FanCodeAdapter(),
        SportsProvider.SONYLIV to SonyLIVAdapter(),
        SportsProvider.TATAPLAY to TataPlayAdapter(),
        SportsProvider.WILLOW to WillowAdapter()
    )

    private val _events = MutableStateFlow<List<SportsEvent>>(emptyList())
    val events: StateFlow<List<SportsEvent>> = _events.asStateFlow()

    private val _channels = MutableStateFlow<List<SportsChannel>>(emptyList())
    val channels: StateFlow<List<SportsChannel>> = _channels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _error.value = null
        try {
            val allEvents = mutableListOf<SportsEvent>()
            val allChannels = mutableListOf<SportsChannel>()

            for (adapter in adapters.values) {
                try {
                    allEvents.addAll(adapter.fetchEvents())
                    allChannels.addAll(adapter.fetchChannels())
                } catch (e: Exception) {
                    // Log and gracefully continue with remaining providers
                }
            }

            _events.value = allEvents
            _channels.value = allChannels
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Failed to refresh sports feeds"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun refreshProvider(provider: SportsProvider) = withContext(Dispatchers.IO) {
        val adapter = adapters[provider] ?: return@withContext
        try {
            val newEvents = adapter.fetchEvents()
            val newChannels = adapter.fetchChannels()

            _events.value = _events.value.filter { it.provider != provider } + newEvents
            _channels.value = _channels.value.filter { it.provider != provider } + newChannels
        } catch (e: Exception) {
            _error.value = "Failed to refresh ${provider.displayName}"
        }
    }
}

