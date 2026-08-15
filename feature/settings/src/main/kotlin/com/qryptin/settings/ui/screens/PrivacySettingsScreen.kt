package com.qryptin.settings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.settings.model.ActiveSession
import com.qryptin.settings.model.PqStatus
import com.qryptin.settings.ui.components.*
import com.qryptin.settings.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBack             : () -> Unit,
    onOpenSessions     : () -> Unit,
    onOpenPqStatus     : () -> Unit,
    viewModel          : SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val privacy = uiState.privacy

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Privacy", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            item { SettingsSectionHeader("Visibility") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Visibility, title = "Last seen",
                        checked = privacy.lastSeenVisible, onCheckedChange = viewModel::setLastSeenVisible,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        icon = Icons.Rounded.AccountCircle, title = "Profile photo",
                        checked = privacy.profilePhotoVisible, onCheckedChange = viewModel::setProfilePhotoVisible,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = "Link read receipts to last seen",
                        checked = privacy.readReceiptsLinked, onCheckedChange = viewModel::setReadReceiptsLinked,
                    )
                }
            }

            item { SettingsSectionHeader("Security") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Lock, title = "Screen security lock",
                        subtitle = "Require biometrics to reopen QryptIN",
                        checked = privacy.screenSecurityLock, onCheckedChange = viewModel::setScreenSecurityLock,
                    )
                    HorizontalDivider()
                    SettingsNavRow(
                        icon = Icons.Rounded.Devices, title = "Active sessions",
                        subtitle = "Devices currently signed in", onClick = onOpenSessions,
                    )
                    HorizontalDivider()
                    SettingsNavRow(
                        icon = Icons.Rounded.Shield, title = "Post-quantum encryption",
                        subtitle = "Kyber / Dilithium key status", onClick = onOpenPqStatus,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionsScreen(onBack: () -> Unit) {
    // Mock data for now — wiring this to a real device/session registry
    // is a separate backend task once QryptIN has multi-device support.
    val sessions = remember {
        listOf(
            ActiveSession("s1", "This device — Android", "Active now", isCurrent = true),
            ActiveSession("s2", "Pixel 7 — Android", "Last active 2 days ago", isCurrent = false),
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Active sessions", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            items(sessions, key = { it.id }) { session ->
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(session.deviceName, style = MaterialTheme.typography.bodyLarge)
                            Text(session.lastActive, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (session.isCurrent) {
                            AssistChip(onClick = {}, label = { Text("This device") })
                        } else {
                            TextButton(onClick = { /* TODO: revoke session once backend exists */ }) { Text("Log out") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PqStatusScreen(onBack: () -> Unit) {
    val status = remember { PqStatus(keysRotatedAt = System.currentTimeMillis()) }
    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Post-quantum encryption", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            if (status.isActive) "Post-quantum encryption active" else "Post-quantum encryption inactive",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
            item { SettingsSectionHeader("Algorithms") }
            item {
                SettingsCard {
                    InfoRow("Key exchange", status.keyExchangeAlgorithm)
                    HorizontalDivider()
                    InfoRow("Digital signatures", status.signatureAlgorithm)
                    HorizontalDivider()
                    InfoRow("Keys last rotated", formatTimestamp(status.keysRotatedAt))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(epochMillis))
