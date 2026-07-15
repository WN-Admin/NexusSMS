package com.nexusmedia.nexussms.features.messaging

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRoutingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    val channelRouter: ChannelRouter,
    private val gson: Gson
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("channel_routing_prefs", Context.MODE_PRIVATE)
    }

    private val _globalFallbackEnabled = MutableStateFlow(prefs.getBoolean(KEY_GLOBAL_FALLBACK, true))
    val globalFallbackEnabled: Flow<Boolean> = _globalFallbackEnabled.asStateFlow()

    private val _defaultFallbackDelay = MutableStateFlow(prefs.getLong(KEY_DEFAULT_DELAY, 2000L))
    val defaultFallbackDelay: Flow<Long> = _defaultFallbackDelay.asStateFlow()

    fun getRoutingConfig(contactId: String): ContactRoutingConfig? {
        return try {
            val json = prefs.getString("$KEY_CONFIG_PREFIX$contactId", null) ?: return null
            gson.fromJson(json, ContactRoutingConfig::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load routing config for %s", contactId)
            null
        }
    }

    fun saveRoutingConfig(config: ContactRoutingConfig) {
        prefs.edit()
            .putString("$KEY_CONFIG_PREFIX${config.contactId}", gson.toJson(config))
            .apply()
    }

    fun deleteRoutingConfig(contactId: String) {
        prefs.edit()
            .remove("$KEY_CONFIG_PREFIX$contactId")
            .apply()
    }

    fun setGlobalFallbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLOBAL_FALLBACK, enabled).apply()
        _globalFallbackEnabled.value = enabled
    }

    fun setDefaultFallbackDelay(delayMs: Long) {
        prefs.edit().putLong(KEY_DEFAULT_DELAY, delayMs).apply()
        _defaultFallbackDelay.value = delayMs
    }

    suspend fun routeMessage(
        contactId: String,
        message: String,
        availablePlatforms: List<String>,
        sendViaPlatform: suspend (platform: String, message: String) -> Result<Unit>
    ): RoutingResult {
        val config = getRoutingConfig(contactId)
            ?: channelRouter.createConfigForContact(
                contactId,
                availablePlatforms,
                _globalFallbackEnabled.value
            )

        return channelRouter.routeMessage(contactId, message, config, sendViaPlatform)
    }

    fun getBestPlatform(contactId: String): String? {
        return channelRouter.getBestPlatformForContact(contactId)
    }

    fun getRoutingStats(): Map<String, ChannelStats> {
        return channelRouter.channelStats.value
    }

    companion object {
        private const val KEY_GLOBAL_FALLBACK = "global_fallback_enabled"
        private const val KEY_DEFAULT_DELAY = "default_fallback_delay"
        private const val KEY_CONFIG_PREFIX = "routing_config_"
    }
}
