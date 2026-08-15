package com.qryptin.settings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.AutoDelete
import androidx.compose.material.icons.rounded.Storage
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
fun StorageSettingsScreen(
    onBack              : () -> Unit,
    onOpenManageStorage : () -> Unit,
    viewModel           : SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val storage = uiState.storage

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Storage & Data", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            item { SettingsSectionHeader("Media cache") }
            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("Cache limit: ${storage.mediaCacheLimitMb} MB", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = storage.mediaCacheLimitMb.toFloat(),
                            onValueChange = { viewModel.setMediaCacheLimitMb(it.toInt()) },
                            valueRange = 256f..8192f,
                            steps = 30,
                        )
                    }
                    HorizontalDivider()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.AutoDelete, title = "Auto-delete old media",
                        subtitle = "Free up space by removing media older than 30 days",
                        checked = storage.autoDeleteOldMedia, onCheckedChange = viewModel::setAutoDeleteOldMedia,
                    )
                }
            }

            item { SettingsSectionHeader("About") }
            item {
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Rounded.Storage, title = "Manage storage",
                        subtitle = "See space used by chats, calls, and media",
                        onClick = onOpenManageStorage,
                    )
                }
            }
        }
    }
}
