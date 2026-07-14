package com.nexusmedia.nexussms.features.messaging

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpamReportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("spam_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun reportSpam(conversationId: String, messageContent: String) {
        val reports = getSpamReports().toMutableList()
        reports.add(
            mapOf(
                "conversationId" to conversationId,
                "content" to messageContent,
                "timestamp" to System.currentTimeMillis()
            )
        )
        prefs.edit().putString("reports", gson.toJson(reports)).apply()
    }

    fun getSpamReports(): List<Map<String, Any>> {
        val json = prefs.getString("reports", "[]") ?: "[]"
        return try {
            gson.fromJson(json, object : TypeToken<List<Map<String, Any>>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearReports() {
        prefs.edit().remove("reports").apply()
    }
}
