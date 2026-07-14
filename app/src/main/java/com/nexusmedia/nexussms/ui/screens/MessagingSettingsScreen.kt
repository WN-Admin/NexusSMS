package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nexusmedia.nexussms.R
import com.nexusmedia.nexussms.features.messaging.MessagingPreferences
import com.nexusmedia.nexussms.ui.viewmodels.MessagingSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingSettingsScreen(
    navController: NavController,
    viewModel: MessagingSettingsViewModel = hiltViewModel()
) {
    val sendDelay by remember { mutableFloatStateOf(viewModel.preferences.sendDelaySeconds.toFloat()) }
    var currentSendDelay by remember { mutableFloatStateOf(sendDelay) }
    var groupMode by remember { mutableStateOf(viewModel.preferences.groupChatMode) }
    var mobileNumber by remember { mutableStateOf(viewModel.preferences.mobileNumber) }
    var mmsSizeLimit by remember { mutableStateOf(viewModel.preferences.mmsCarrierSizeLimitKb.toString()) }
    var mmsApn by remember { mutableStateOf(viewModel.preferences.mmsApnName) }
    var mmsWifiFix by remember { mutableStateOf(viewModel.preferences.mmsWifiFix) }
    var smsToMmsThreshold by remember { mutableFloatStateOf(viewModel.preferences.convertLongSmsToMmsThreshold.toFloat()) }
    var currentSmsToMms by remember { mutableFloatStateOf(smsToMmsThreshold) }
    var groupModeExpanded by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.messaging_settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { SettingsSection(title = "Send Settings") }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.send_delay_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.send_delay_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = currentSendDelay,
                            onValueChange = { currentSendDelay = it },
                            onValueChangeFinished = {
                                viewModel.preferences.sendDelaySeconds = currentSendDelay.toInt()
                            },
                            valueRange = 0f..9f,
                            steps = 8,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${currentSendDelay.toInt()}s",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                SettingsItem(
                    title = "Send with Enter",
                    subtitle = "Send message on Enter key instead of newline",
                    onClick = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.group_chat_mode))
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = groupModeExpanded,
                    onExpandedChange = { groupModeExpanded = it }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .clickable { groupModeExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.group_mode_title),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (groupMode == MessagingPreferences.GROUP_MODE_MMS)
                                    stringResource(R.string.group_mode_mms)
                                else stringResource(R.string.group_mode_sms),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupModeExpanded)
                    }
                    ExposedDropdownMenu(
                        expanded = groupModeExpanded,
                        onDismissRequest = { groupModeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.group_mode_mms)) },
                            onClick = {
                                groupMode = MessagingPreferences.GROUP_MODE_MMS
                                viewModel.preferences.groupChatMode = MessagingPreferences.GROUP_MODE_MMS
                                groupModeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.group_mode_sms)) },
                            onClick = {
                                groupMode = MessagingPreferences.GROUP_MODE_SMS
                                viewModel.preferences.groupChatMode = MessagingPreferences.GROUP_MODE_SMS
                                groupModeExpanded = false
                            }
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.mobile_number_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.mobile_number_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = {
                            mobileNumber = it
                            viewModel.preferences.mobileNumber = it
                        },
                        placeholder = { Text("+1 555 123 4567") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = stringResource(R.string.mms_settings))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.mms_size_limit_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mmsSizeLimit,
                        onValueChange = { value ->
                            mmsSizeLimit = value.filter { it.isDigit() }
                            val parsed = mmsSizeLimit.toIntOrNull()
                            if (parsed != null) {
                                viewModel.preferences.mmsCarrierSizeLimitKb = parsed
                            }
                        },
                        suffix = { Text("KB") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.mms_apn_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.mms_apn_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mmsApn,
                        onValueChange = {
                            mmsApn = it
                            viewModel.preferences.mmsApnName = it
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            mmsWifiFix = !mmsWifiFix
                            viewModel.preferences.mmsWifiFix = mmsWifiFix
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.mms_wifi_fix_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.mms_wifi_fix_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = mmsWifiFix,
                        onCheckedChange = {
                            mmsWifiFix = it
                            viewModel.preferences.mmsWifiFix = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sms_to_mms_threshold_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.sms_to_mms_threshold_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Off",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = currentSmsToMms,
                            onValueChange = { currentSmsToMms = it },
                            onValueChangeFinished = {
                                viewModel.preferences.convertLongSmsToMmsThreshold = currentSmsToMms.toInt()
                            },
                            valueRange = 0f..3f,
                            steps = 2,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = if (currentSmsToMms.toInt() == 0) "Off" else "${currentSmsToMms.toInt()} parts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(60.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Floatification")
            }

            item {
                val floatPrefs = context.getSharedPreferences("floatification_prefs", android.content.Context.MODE_PRIVATE)
                var floatEnabled by remember { mutableStateOf(floatPrefs.getBoolean("floatification_enabled", false)) }
                var floatDuration by remember { mutableFloatStateOf(floatPrefs.getLong("floatification_duration", 3000L).toFloat()) }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                floatEnabled = !floatEnabled
                                floatPrefs.edit().putBoolean("floatification_enabled", floatEnabled).apply()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.floatification),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.floatification_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = floatEnabled,
                            onCheckedChange = {
                                floatEnabled = it
                                floatPrefs.edit().putBoolean("floatification_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    if (floatEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.floatification_duration),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Slider(
                                    value = floatDuration,
                                    onValueChange = { floatDuration = it },
                                    onValueChangeFinished = {
                                        floatPrefs.edit().putLong("floatification_duration", floatDuration.toLong()).apply()
                                    },
                                    valueRange = 2000f..180000f,
                                    steps = 0,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${(floatDuration / 1000).toInt()}s",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
