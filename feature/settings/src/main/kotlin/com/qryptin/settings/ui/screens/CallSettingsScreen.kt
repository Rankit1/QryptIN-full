package com.qryptin.settings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.settings.ui.components.*
import com.qryptin.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallSettingsScreen(
    onBack    : () -> Unit,
    viewModel : SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val calls = uiState.calls

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Calls", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            item { SettingsSectionHeader("Ringing") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.VolumeUp, title = "Ringtone",
                        checked = calls.ringtoneEnabled, onCheckedChange = viewModel::setRingtoneEnabled,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Vibration, title = "Vibrate on incoming call",
                        checked = calls.vibrateOnRing, onCheckedChange = viewModel::setVibrateOnRing,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = "Call waiting", subtitle = "Get notified of a second incoming call",
                        checked = calls.callWaiting, onCheckedChange = viewModel::setCallWaiting,
                    )
                }
            }

            item { SettingsSectionHeader("Network") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.NetworkCheck, title = "Low data mode",
                        subtitle = "Reduce call quality to save mobile data",
                        checked = calls.lowDataMode, onCheckedChange = viewModel::setLowDataMode,
                    )
                }
            }
        }
    }
}
