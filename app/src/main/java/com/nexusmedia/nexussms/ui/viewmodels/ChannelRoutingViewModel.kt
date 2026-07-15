package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.nexusmedia.nexussms.features.messaging.ChannelRoutingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChannelRoutingViewModel @Inject constructor(
    val routingManager: ChannelRoutingManager
) : ViewModel()
