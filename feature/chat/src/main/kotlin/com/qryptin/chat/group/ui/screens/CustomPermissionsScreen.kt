package com.qryptin.chat.group.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qryptin.chat.group.model.GroupPermissions
import com.qryptin.chat.group.ui.components.GroupWizardTopBar
import com.qryptin.chat.group.ui.components.PermissionToggleRow
import com.qryptin.chat.group.viewmodel.GroupCreationViewModel
import com.qryptin.core.designsystem.ui.theme.QryptDimens
import com.qryptin.core.designsystem.ui.theme.TypoBodySmall
import com.qryptin.core.designsystem.ui.theme.TypoTitleMedium

// ─────────────────────────────────────────────────────────────
//  CustomPermissionsScreen  — Step 3 of group creation wizard
// ─────────────────────────────────────────────────────────────
@Composable
fun CustomPermissionsScreen(
    viewModel : GroupCreationViewModel,
    onBack    : () -> Unit,
    onDone    : () -> Unit,   // triggers group creation
) {
    val uiState by viewModel.uiState.collectAsState()
    val perms = uiState.permissions

    fun update(block: GroupPermissions.() -> GroupPermissions) {
        viewModel.onPermissionChanged(perms.block())
    }

    Scaffold(
        topBar = {
            GroupWizardTopBar(
                title       = "Custom Permissions",
                onBack      = onBack,
                actionLabel = if (uiState.isCreating) "Creating…" else "Create",
                onAction    = onDone,
                actionEnabled = !uiState.isCreating,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH, vertical = 12.dp)) {
                Text("Custom Permissions (Members)", style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                Text(
                    "These settings can be changed later",
                    style = TypoBodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            PermissionToggleRow(
                icon     = Icons.Rounded.Message,
                title    = "Send messages",
                checked  = perms.canSendMessages,
                onToggle = { update { copy(canSendMessages = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.PersonAdd,
                title    = "Add members",
                checked  = perms.canAddMembers,
                onToggle = { update { copy(canAddMembers = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.PersonRemove,
                title    = "Remove members",
                subtitle = "Members can remove others",
                checked  = perms.canRemoveMembers,
                onToggle = { update { copy(canRemoveMembers = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.Edit,
                title    = "Change group info",
                subtitle = "Name, photo, and description",
                checked  = perms.canChangeGroupInfo,
                onToggle = { update { copy(canChangeGroupInfo = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.PushPin,
                title    = "Pin messages",
                checked  = perms.canPinMessages,
                onToggle = { update { copy(canPinMessages = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.PermMedia,
                title    = "Send media",
                subtitle = "Images, videos, documents",
                checked  = perms.canSendMedia,
                onToggle = { update { copy(canSendMedia = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.HowToReg,
                title    = "Approve new members",
                subtitle = "Admin must approve join requests",
                checked  = perms.canApproveNewMembers,
                onToggle = { update { copy(canApproveNewMembers = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.Screenshot,
                title    = "Block screenshots",
                subtitle = "Prevent screenshots in this chat",
                checked  = perms.blockScreenshot,
                onToggle = { update { copy(blockScreenshot = it) } },
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            PermissionToggleRow(
                icon     = Icons.Rounded.NotificationsOff,
                title    = "Mute notifications for members",
                subtitle = "Silence notifications by default",
                checked  = perms.muteNotificationsForAll,
                onToggle = { update { copy(muteNotificationsForAll = it) } },
            )

            Spacer(Modifier.height(24.dp))

            // Loading indicator when creating
            if (uiState.isCreating) {
                Box(
                    modifier         = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.errorMessage?.let { err ->
                Text(
                    err,
                    style    = TypoBodySmall,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH),
                )
            }
        }
    }
}
