package com.nexusmedia.nexussms.features.notifications

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerContactNotificationSettings @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getVibratePattern(conversationId: String): LongArray? {
        val raw = prefs.getString("${KEY_VIBRATE}_$conversationId", null) ?: return null
        return raw.split(",").map { it.trim().toLong() }.toLongArray()
    }

    fun setVibratePattern(conversationId: String, pattern: LongArray?) {
        if (pattern == null) {
            prefs.edit().remove("${KEY_VIBRATE}_$conversationId").apply()
        } else {
            prefs.edit().putString("${KEY_VIBRATE}_$conversationId", pattern.joinToString(",")).apply()
        }
    }

    fun getRingtone(conversationId: String): String? =
        prefs.getString("${KEY_RINGTONE}_$conversationId", null)

    fun setRingtone(conversationId: String, uri: String?) {
        if (uri == null) {
            prefs.edit().remove("${KEY_RINGTONE}_$conversationId").apply()
        } else {
            prefs.edit().putString("${KEY_RINGTONE}_$conversationId", uri).apply()
        }
    }

    fun getPrivacyLevel(conversationId: String): String =
        prefs.getString("${KEY_PRIVACY}_$conversationId", PRIVACY_FULL) ?: PRIVACY_FULL

    fun setPrivacyLevel(conversationId: String, level: String) {
        prefs.edit().putString("${KEY_PRIVACY}_$conversationId", level).apply()
    }

    fun isRepeatNotification(conversationId: String): Boolean =
        prefs.getBoolean("${KEY_REPEAT}_$conversationId", false)

    fun setRepeatNotification(conversationId: String, repeat: Boolean) {
        prefs.edit().putBoolean("${KEY_REPEAT}_$conversationId", repeat).apply()
    }

    fun getDisplayHashVibratePattern(conversationId: String): LongArray {
        val hash = conversationId.hashCode().toLong()
        return longArrayOf(
            0L,
            100L + (kotlin.math.abs(hash) % 200L),
            50L + (kotlin.math.abs(hash) % 150L),
            100L + (kotlin.math.abs(hash + 1) % 200L)
        )
    }

    companion object {
        const val PREFS_NAME = "notification_prefs"
        const val KEY_VIBRATE = "vibrate"
        const val KEY_RINGTONE = "ringtone"
        const val KEY_PRIVACY = "privacy"
        const val KEY_REPEAT = "repeat"

        const val PRIVACY_FULL = "FULL"
        const val PRIVACY_HIDDEN = "HIDDEN"
        const val PRIVACY_NONE = "NONE"
    }
}
