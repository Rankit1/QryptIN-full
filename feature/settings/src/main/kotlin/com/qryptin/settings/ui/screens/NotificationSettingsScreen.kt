package com.qryptin.settings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.settings.ui.components.*
import com.qryptin.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack    : () -> Unit,
    viewModel : SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val notifications = uiState.notifications

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Notifications", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            item { SettingsSectionHeader("Categories") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Chat, title = "Messages",
                        checked = notifications.messageNotifications, onCheckedChange = viewModel::setMessageNotifications,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Call, title = "Calls",
                        checked = notifications.callNotifications, onCheckedChange = viewModel::setCallNotifications,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Groups, title = "Groups",
                        checked = notifications.groupNotifications, onCheckedChange = viewModel::setGroupNotifications,
                    )
                }
            }

            item { SettingsSectionHeader("Sound & display") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.VolumeUp, title = "Notification sound",
                        checked = notifications.notificationSound, onCheckedChange = viewModel::setNotificationSound,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Visibility, title = "Show message preview",
                        subtitle = "Show message text in the notification",
                        checked = notifications.notificationPreview, onCheckedChange = viewModel::setNotificationPreview,
                    )
                }
            }
        }
    }
}
