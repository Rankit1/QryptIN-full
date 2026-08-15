package com.qryptin.settings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.settings.model.MediaAutoDownload
import com.qryptin.settings.ui.components.*
import com.qryptin.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingSettingsScreen(
    onBack    : () -> Unit,
    viewModel : SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val messaging = uiState.messaging

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Messaging", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            item { SettingsSectionHeader("Chats") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Visibility, title = "Read receipts",
                        subtitle = "Let others see when you've read their messages",
                        checked = messaging.readReceipts, onCheckedChange = viewModel::setReadReceipts,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = "Typing indicators", subtitle = "Show \"typing…\" to your contacts",
                        checked = messaging.typingIndicators, onCheckedChange = viewModel::setTypingIndicators,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Keyboard, title = "Enter key sends message",
                        subtitle = "Otherwise enter adds a new line",
                        checked = messaging.enterToSend, onCheckedChange = viewModel::setEnterToSend,
                    )
                }
            }

            item { SettingsSectionHeader("Media") }
            item {
                SettingsCard {
                    Text(
                        "Auto-download media",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp),
                    )
                    MediaAutoDownload.values().forEach { option ->
                        SettingsSwitchRow(
                            icon = Icons.Rounded.Download,
                            title = option.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            checked = messaging.mediaAutoDownload == option,
                            onCheckedChange = { if (it) viewModel.setMediaAutoDownload(option) },
                        )
                    }
                }
            }
        }
    }
}
