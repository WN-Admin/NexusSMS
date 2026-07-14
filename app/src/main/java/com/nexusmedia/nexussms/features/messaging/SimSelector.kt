package com.nexusmedia.nexussms.features.messaging

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimSelector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class SimInfo(
        val slotIndex: Int,
        val displayName: String,
        val phoneNumber: String?,
        val subscriptionId: Int,
        val carrierName: String
    )

    fun getAvailableSims(): List<SimInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return emptyList()

        return try {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList ?: return emptyList()
            activeSubscriptions.map { sub ->
                SimInfo(
                    slotIndex = sub.simSlotIndex,
                    displayName = sub.displayName?.toString() ?: "SIM ${sub.simSlotIndex + 1}",
                    phoneNumber = sub.number,
                    subscriptionId = sub.subscriptionId,
                    carrierName = sub.carrierName?.toString() ?: ""
                )
            }
        } catch (e: SecurityException) {
            Timber.w(e, "No permission to read SIM info")
            emptyList()
        }
    }

    fun getSmsManagerForSim(simInfo: SimInfo): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            telephonyManager.createForSubscriptionId(simInfo.subscriptionId)
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }
}
