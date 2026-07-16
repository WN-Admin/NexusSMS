package com.nexusmedia.nexussms.features.messaging

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class ChannelPriority(
    val platform: String,
    val priority: Int,
    val enabled: Boolean = true,
    val fallbackDelayMs: Long = 2000,
    val maxRetries: Int = 1
)

data class RoutingResult(
    val success: Boolean,
    val platformUsed: String?,
    val attempts: List<AttemptedChannel>,
    val error: String? = null
)

data class AttemptedChannel(
    val platform: String,
    val success: Boolean,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ContactRoutingConfig(
    val contactId: String,
    val channels: List<ChannelPriority>,
    val fallbackEnabled: Boolean = true,
    val autoSelectBest: Boolean = true,
    val lastUsedPlatform: String? = null,
    val lastUsedTime: Long = 0
)

data class ChannelStats(
    val totalAttempts: Long = 0,
    val successfulAttempts: Long = 0,
    val lastAttemptTime: Long = 0,
    val lastSuccessTime: Long = 0
) {
    val successRate: Double
        get() = if (totalAttempts > 0) successfulAttempts.toDouble() / totalAttempts else 0.0
}

@Singleton
class ChannelRouter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _routingHistory = MutableStateFlow<Map<String, List<AttemptedChannel>>>(emptyMap())
    val routingHistory: StateFlow<Map<String, List<AttemptedChannel>>> = _routingHistory.asStateFlow()

    private val _channelStats = MutableStateFlow<Map<String, ChannelStats>>(emptyMap())
    val channelStats: StateFlow<Map<String, ChannelStats>> = _channelStats.asStateFlow()

    private val defaultPriorities = listOf(
        ChannelPriority(platform = "MATRIX", priority = 1, fallbackDelayMs = 1000),
        ChannelPriority(platform = "TELEGRAM", priority = 2, fallbackDelayMs = 1500),
        ChannelPriority(platform = "DISCORD", priority = 3, fallbackDelayMs = 2000),
        ChannelPriority(platform = "MESSENGER", priority = 4, fallbackDelayMs = 2500),
        ChannelPriority(platform = "SMS", priority = 5, fallbackDelayMs = 0)
    )

    suspend fun routeMessage(
        contactId: String,
        message: String,
        config: ContactRoutingConfig? = null,
        sendViaPlatform: suspend (platform: String, message: String) -> Result<Unit>
    ): RoutingResult {
        val effectiveConfig = config ?: createDefaultConfig(contactId)
        val attempts = mutableListOf<AttemptedChannel>()

        val sortedChannels = effectiveConfig.channels
            .filter { it.enabled }
            .sortedBy { it.priority }

        for (channel in sortedChannels) {
            val maxAttempts = channel.maxRetries + 1
            for (attemptNum in 1..maxAttempts) {
                if (attempts.isNotEmpty() && channel.fallbackDelayMs > 0) {
                    delay(channel.fallbackDelayMs)
                }

                val result = try {
                    sendViaPlatform(channel.platform, message)
                } catch (e: Exception) {
                    Result.failure(e)
                }

                val attempt = AttemptedChannel(
                    platform = channel.platform,
                    success = result.isSuccess,
                    error = result.exceptionOrNull()?.message
                )
                attempts.add(attempt)

                updateChannelStats(channel.platform, result.isSuccess)

                if (result.isSuccess) {
                    Timber.i("ChannelRouter: message sent via %s (attempt %d/%d)", channel.platform, attemptNum, maxAttempts)
                    updateRoutingHistory(contactId, attempts)
                    updateLastUsedPlatform(contactId, channel.platform)
                    return RoutingResult(
                        success = true,
                        platformUsed = channel.platform,
                        attempts = attempts
                    )
                }

                Timber.w("ChannelRouter: %s attempt %d/%d failed: %s", channel.platform, attemptNum, maxAttempts, attempt.error ?: "unknown")

                if (attemptNum < maxAttempts) {
                    Timber.d("ChannelRouter: retrying %s in %dms", channel.platform, channel.fallbackDelayMs)
                }
            }

            if (!effectiveConfig.fallbackEnabled) {
                break
            }
        }

        updateRoutingHistory(contactId, attempts)
        val errorSummary = attempts.joinToString { "${it.platform}: ${it.error ?: "unknown"}" }
        Timber.e("ChannelRouter: all channels failed for %s: %s", contactId, errorSummary)

        return RoutingResult(
            success = false,
            platformUsed = null,
            attempts = attempts,
            error = "All channels failed: $errorSummary"
        )
    }

    fun getBestPlatformForContact(contactId: String): String? {
        val stats = _channelStats.value
        val history = _routingHistory.value[contactId] ?: emptyList()

        val lastSuccessful = history.lastOrNull { it.success }
        if (lastSuccessful != null) {
            return lastSuccessful.platform
        }

        return stats.entries
            .filter { it.key != "SMS" }
            .maxByOrNull { it.value.successRate }
            ?.key
    }

    fun getChannelStats(platform: String): ChannelStats {
        return _channelStats.value[platform] ?: ChannelStats()
    }

    fun getRoutingHistory(contactId: String): List<AttemptedChannel> {
        return _routingHistory.value[contactId] ?: emptyList()
    }

    fun createConfigForContact(
        contactId: String,
        availablePlatforms: List<String>,
        fallbackEnabled: Boolean = true
    ): ContactRoutingConfig {
        val channels = defaultPriorities.filter { it.platform in availablePlatforms }
        return ContactRoutingConfig(
            contactId = contactId,
            channels = channels,
            fallbackEnabled = fallbackEnabled,
            autoSelectBest = true
        )
    }

    private fun updateChannelStats(platform: String, success: Boolean) {
        val current = _channelStats.value.toMutableMap()
        val stats = current.getOrDefault(platform, ChannelStats())
        current[platform] = stats.copy(
            totalAttempts = stats.totalAttempts + 1,
            successfulAttempts = stats.successfulAttempts + if (success) 1 else 0,
            lastAttemptTime = System.currentTimeMillis(),
            lastSuccessTime = if (success) System.currentTimeMillis() else stats.lastSuccessTime
        )
        _channelStats.value = current
    }

    private fun updateRoutingHistory(contactId: String, attempts: List<AttemptedChannel>) {
        val current = _routingHistory.value.toMutableMap()
        current[contactId] = attempts
        _routingHistory.value = current
    }

    private fun updateLastUsedPlatform(contactId: String, platform: String) {
        val current = _routingHistory.value.toMutableMap()
        val existing = current[contactId]?.toMutableList() ?: mutableListOf()
        existing.add(AttemptedChannel(platform = platform, success = true, timestamp = System.currentTimeMillis()))
        current[contactId] = existing
        _routingHistory.value = current
    }

    private fun createDefaultConfig(contactId: String): ContactRoutingConfig {
        return ContactRoutingConfig(
            contactId = contactId,
            channels = defaultPriorities,
            fallbackEnabled = true,
            autoSelectBest = true
        )
    }
}
