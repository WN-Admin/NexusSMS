package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.nexusmedia.nexussms.features.messaging.MessagingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MessagingSettingsViewModel @Inject constructor(
    val preferences: MessagingPreferences
) : ViewModel()
