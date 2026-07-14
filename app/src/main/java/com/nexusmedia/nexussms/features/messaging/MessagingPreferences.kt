package com.nexusmedia.nexussms.features.messaging

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagingPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var sendDelaySeconds: Int
        get() = prefs.getInt(KEY_SEND_DELAY, 0)
        set(value) = prefs.edit().putInt(KEY_SEND_DELAY, value.coerceIn(0, 9)).apply()

    var groupChatMode: String
        get() = prefs.getString(KEY_GROUP_MODE, GROUP_MODE_MMS) ?: GROUP_MODE_MMS
        set(value) = prefs.edit().putString(KEY_GROUP_MODE, value).apply()

    var mobileNumber: String
        get() = prefs.getString(KEY_MOBILE_NUMBER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MOBILE_NUMBER, value.trim()).apply()

    var mmsCarrierSizeLimitKb: Int
        get() = prefs.getInt(KEY_MMS_SIZE_LIMIT_KB, 300)
        set(value) = prefs.edit().putInt(KEY_MMS_SIZE_LIMIT_KB, value.coerceAtLeast(50)).apply()

    var mmsApnName: String
        get() = prefs.getString(KEY_MMS_APN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MMS_APN, value).apply()

    var mmsWifiFix: Boolean
        get() = prefs.getBoolean(KEY_MMS_WIFI_FIX, false)
        set(value) = prefs.edit().putBoolean(KEY_MMS_WIFI_FIX, value).apply()

    var convertLongSmsToMmsThreshold: Int
        get() = prefs.getInt(KEY_SMS_TO_MMS_THRESHOLD, 0)
        set(value) = prefs.edit().putInt(KEY_SMS_TO_MMS_THRESHOLD, value.coerceIn(0, 3)).apply()

    companion object {
        const val PREFS_NAME = "messaging_prefs"
        const val GROUP_MODE_MMS = "MMS"
        const val GROUP_MODE_SMS = "SMS"
        private const val KEY_SEND_DELAY = "send_delay_seconds"
        private const val KEY_GROUP_MODE = "group_chat_mode"
        private const val KEY_MOBILE_NUMBER = "mobile_number"
        private const val KEY_MMS_SIZE_LIMIT_KB = "mms_size_limit_kb"
        private const val KEY_MMS_APN = "mms_apn"
        private const val KEY_MMS_WIFI_FIX = "mms_wifi_fix"
        private const val KEY_SMS_TO_MMS_THRESHOLD = "sms_to_mms_threshold"
    }
}
