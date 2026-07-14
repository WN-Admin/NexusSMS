package com.nexusmedia.nexussms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.features.security.BiometricAuthManager
import com.nexusmedia.nexussms.features.security.SessionManager
import com.nexusmedia.nexussms.ui.screens.AppLockScreen
import com.nexusmedia.nexussms.ui.screens.MainScreen
import com.nexusmedia.nexussms.ui.theme.NexusSMSTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    companion object {
        const val EXTRA_OPEN_CONVERSATION_ID = "extra_open_conversation_id"
    }

    @Inject lateinit var appSecuritySettingsDao: AppSecuritySettingsDao
    @Inject lateinit var biometricAuthManager: BiometricAuthManager
    @Inject lateinit var sessionManager: SessionManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRequiredPermissions()
        setContent {
            val settings by appSecuritySettingsDao.getSecuritySettings().collectAsState(initial = null)
            var isAuthenticated by remember { mutableStateOf(false) }
            var sessionChecked by remember { mutableStateOf(false) }

            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                sessionManager.endSession()
                            }
                            isAuthenticated = false
                            sessionChecked = false
                        }
                        Lifecycle.Event.ON_START -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                val active = sessionManager.isSessionActive()
                                if (active) {
                                    isAuthenticated = true
                                }
                                sessionChecked = true
                            }
                        }
                        else -> Unit
                    }
                }
                ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
                onDispose {
                    ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
                }
            }

            LaunchedEffect(settings, sessionChecked) {
                if (settings != null && !isAuthenticated) {
                    val needsLock = settings!!.requireBiometricOnStartup || settings!!.appLockEnabled
                    if (needsLock) {
                        if (settings!!.requireBiometricOnStartup && biometricAuthManager.isBiometricAvailable()) {
                            biometricAuthManager.showBiometricPrompt(
                                activity = this@MainActivity,
                                title = "Unlock NexusSMS",
                                subtitle = "Authenticate to continue",
                                onSuccess = {
                                    isAuthenticated = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        sessionManager.startSession()
                                    }
                                    Timber.d("Biometric unlock on startup succeeded")
                                },
                                onError = {
                                    Timber.d("Biometric unlock on startup failed, showing PIN")
                                }
                            )
                        } else if (settings!!.appLockEnabled && settings!!.appLockValue != null) {
                            // PIN required but no biometric - AppLockScreen handles this below.
                        } else {
                            isAuthenticated = true
                        }
                    } else {
                        isAuthenticated = true
                    }
                }
            }

            LaunchedEffect(settings?.disableScreenshots) {
                if (settings?.disableScreenshots == true) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            NexusSMSTheme {
                val needsLock = settings != null && (settings!!.requireBiometricOnStartup || settings!!.appLockEnabled)
                if (needsLock && !isAuthenticated) {
                    AppLockScreen(onAuthenticated = {
                        isAuthenticated = true
                        CoroutineScope(Dispatchers.IO).launch {
                            sessionManager.startSession()
                        }
                    })
                } else {
                    MainScreen()
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECEIVE_SMS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
