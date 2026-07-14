package com.nexusmedia.nexussms.features.notifications

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusmedia.nexussms.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloatificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var currentOverlay: View? = null
    private val isShowing = AtomicBoolean(false)

    private val prefs = context.getSharedPreferences("floatification_prefs", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean("floatification_enabled", false)
        set(value) = prefs.edit().putBoolean("floatification_enabled", value).apply()

    var durationMs: Long
        get() = prefs.getLong("floatification_duration", 3000L)
        set(value) = prefs.edit().putLong("floatification_duration", value.coerceIn(2000L, 180000L)).apply()

    fun showFloatification(senderName: String, messagePreview: String, conversationId: String) {
        if (!enabled) return
        if (isShowing.getAndSet(true)) return

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                x = 0
                y = 0
            }

            val overlayView = createOverlayView(senderName, messagePreview, conversationId)

            overlayView.setOnTouchListener { view, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    val x = event.rawX
                    val y = event.rawY
                    if (y < 100) {
                        dismissOverlay()
                        openChat(conversationId)
                    }
                }
                true
            }

            windowManager.addView(overlayView, params)
            currentOverlay = overlayView

            animateIn(overlayView)

            handler.postDelayed({
                dismissOverlay()
            }, durationMs)

        } catch (e: Exception) {
            Timber.e(e, "Failed to show floatification")
            isShowing.set(false)
        }
    }

    private fun createOverlayView(senderName: String, messagePreview: String, conversationId: String): View {
        val density = context.resources.displayMetrics.density
        val padding = (16 * density).toInt()
        val cornerRadius = (16 * density).toInt()

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding / 2)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#E8303030"))
                this.cornerRadius = cornerRadius.toFloat()
            }
        }

        val senderTextView = TextView(context).apply {
            text = senderName
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, (4 * density).toInt())
        }

        val messageTextView = TextView(context).apply {
            text = messagePreview
            setTextColor(android.graphics.Color.parseColor("#B0B0B0"))
            textSize = 13f
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, 0, 0, (8 * density).toInt())
        }

        val hintView = TextView(context).apply {
            text = "Tap to open"
            setTextColor(android.graphics.Color.parseColor("#35AAD8"))
            textSize = 11f
        }

        layout.addView(senderTextView)
        layout.addView(messageTextView)
        layout.addView(hintView)

        return layout
    }

    private fun animateIn(view: View) {
        view.translationY = -view.resources.displayMetrics.heightPixels.toFloat()
        view.animate()
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun dismissOverlay() {
        if (!isShowing.compareAndSet(true, false)) return
        val overlay = currentOverlay ?: return
        try {
            overlay.animate()
                .translationY(-overlay.resources.displayMetrics.heightPixels.toFloat())
                .setDuration(250)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        try {
                            windowManager.removeViewImmediate(overlay)
                        } catch (_: Exception) {}
                    }
                })
                .start()
        } catch (e: Exception) {
            try { windowManager.removeViewImmediate(overlay) } catch (_: Exception) {}
        }
        currentOverlay = null
    }

    private fun openChat(conversationId: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = Uri.parse("nexussms://chat/$conversationId")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open chat from floatification")
        }
    }
}
