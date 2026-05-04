package com.nexussms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nexussms.ui.screens.MainScreen
import com.nexussms.ui.theme.NexusSMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexusSMSTheme {
                MainScreen()
            }
        }
    }
}
