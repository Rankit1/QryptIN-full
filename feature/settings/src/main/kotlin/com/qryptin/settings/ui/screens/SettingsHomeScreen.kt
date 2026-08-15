package com.qryptin.settings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.settings.ui.components.*
import com.qryptin.settings.viewmodel.LogoutViewModel
import com.qryptin.settings.viewmodel.ProfileViewModel
import com.qryptin.settings.viewmodel.SettingsViewModel
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination
import com.qryptin.core.designsystem.ui.theme.QryptBottomNavBar
import com.qryptin.core.designsystem.ui.theme.QryptINSafeScaffold

// ─────────────────────────────────────────────────────────────
//  SettingsHomeScreen
//  Issue 1 fix: the whole screen is one LazyColumn, so every
//  section is reachable no matter how many settings get added,
//  and scroll state survives recomposition (state, not Column).
//  Issue 6 fix: the header now reads the live Profile (avatar,
//  display name, username, bio) from ProfileViewModel instead of
//  a static "Edit Profile" placeholder row, and updates instantly
//  after a profile edit since both screens share the same
//  DataStore-backed flow in ProfileRepository.
// ─────────────────────────────────────────────────────────────
private data class SettingsDestination(
    val icon     : androidx.compose.ui.graphics.vector.ImageVector,
    val title    : String,
    val subtitle : String,
    val onClick  : () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    currentNavDest         : BottomNavDestination,
    onNavSelected          : (BottomNavDestination) -> Unit,
    onOpenEditProfile       : () -> Unit,
    onOpenMessagingSettings : () -> Unit,
    onOpenCallSettings      : () -> Unit,
    onOpenPrivacySettings   : () -> Unit,
    onOpenStorageSettings   : () -> Unit,
    onOpenNotificationSettings : () -> Unit,
    onLogOut                : () -> Unit,
    viewModel               : SettingsViewModel = viewModel(),
    profileViewModel         : ProfileViewModel = viewModel(),
    logoutViewModel          : LogoutViewModel  = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    // Issue 1 fix: logout now requires explicit confirmation via a
    // premium warning dialog before the session is actually cleared.
    var showLogoutDialog by remember { mutableStateOf(false) }

    val destinations = listOf(
        SettingsDestination(Icons.Rounded.Chat, "Messaging", "Read receipts, media, enter-to-send", onOpenMessagingSettings),
        SettingsDestination(Icons.Rounded.Call, "Calls", "Ringtone, vibration, low data mode", onOpenCallSettings),
        SettingsDestination(Icons.Rounded.Lock, "Privacy", "Last seen, sessions, post-quantum status", onOpenPrivacySettings),
        SettingsDestination(Icons.Rounded.Storage, "Storage & Data", "Manage media cache and downloads", onOpenStorageSettings),
        SettingsDestination(Icons.Rounded.Notifications, "Notifications", "Messages, calls, and groups", onOpenNotificationSettings),
    )

    // Issue 2 fix: safe-area-aware scaffold for the Settings tab.
    QryptINSafeScaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.SemiBold) }) },
        bottomBar = { QryptBottomNavBar(selected = currentNavDest, onSelect = onNavSelected) },
    ) { innerPadding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding  = PaddingValues(bottom = SettingsListBottomPadding, top = 8.dp),
        ) {
            item {
                ProfileHeaderCard(
                    profile = profileState.profile,
                    onClick = onOpenEditProfile,
                )
            }

            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsCard {
                    ThemeModeRow(
                        selected   = uiState.themeMode,
                        onSelected = viewModel::setThemeMode,
                    )
                }
            }

            item { SettingsSectionHeader("Preferences") }
            items(destinations) { destination ->
                SettingsCard {
                    SettingsNavRow(
                        icon     = destination.icon,
                        title    = destination.title,
                        subtitle = destination.subtitle,
                        onClick  = destination.onClick,
                    )
                }
            }

            item { SettingsSectionHeader("Account") }
            item {
                SettingsCard {
                    SettingsNavRow(
                        icon    = Icons.Rounded.Logout,
                        title   = "Log out",
                        onClick = { showLogoutDialog = true },
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                // Clears the local auth session (SessionManager/DataStore)
                // first, then hands off to the nav-level callback which
                // clears the back stack and routes to PhoneInputScreen.
                logoutViewModel.logout(onComplete = onLogOut)
            },
        )
    }
}

@Composable
private fun ThemeModeRow(
    selected   : com.qryptin.settings.model.ThemeMode,
    onSelected : (com.qryptin.settings.model.ThemeMode) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Theme", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.qryptin.settings.model.ThemeMode.values().forEach { mode ->
                FilterChip(
                    selected = selected == mode,
                    onClick  = { onSelected(mode) },
                    label    = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }
    }
}
