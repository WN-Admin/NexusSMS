package com.nexusmedia.nexussms.features.mms

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.nexusmedia.nexussms.features.messaging.MessagingPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

@Singleton
class MmsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messagingPreferences: MessagingPreferences
) {
    fun carrierLimitBytes(): Int = messagingPreferences.mmsCarrierSizeLimitKb * 1024

    /**
     * Scales and compresses an image so it fits within the carrier MMS size limit.
     */
    fun compressImageForMms(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val original = BitmapFactory.decodeStream(input) ?: return null
                var quality = 85
                var scale = 1.0f
                var bytes: ByteArray
                val limit = carrierLimitBytes()
                do {
                    val width = max(1, (original.width * scale).roundToInt())
                    val height = max(1, (original.height * scale).roundToInt())
                    val scaled = Bitmap.createScaledBitmap(original, width, height, true)
                    val stream = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                    if (scaled != original) scaled.recycle()
                    bytes = stream.toByteArray()
                    quality -= 10
                    scale *= 0.85f
                } while (bytes.size > limit && quality > 30)
                if (bytes.size > limit) {
                    Timber.w("MMS image still over limit after compression: ${bytes.size} > $limit")
                    return null
                }
                bytes
            }
        } catch (e: Exception) {
            Timber.e(e, "MMS image compression failed")
            null
        }
    }

    fun isOverCarrierLimit(sizeBytes: Int): Boolean = sizeBytes > carrierLimitBytes()
}
