package com.nexussms

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nexussms.data.database.AppSecuritySettingsDao
import com.nexussms.ui.screens.MainScreen
import com.nexussms.ui.theme.NexusSMSTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appSecuritySettingsDao: AppSecuritySettingsDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by appSecuritySettingsDao.getSecuritySettings().collectAsState(initial = null)

            LaunchedEffect(settings?.disableScreenshots) {
                if (settings?.disableScreenshots == true) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            NexusSMSTheme {
                MainScreen()
            }
        }
    }
}
