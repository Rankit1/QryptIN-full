package com.qryptin.chat.group.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.qryptin.chat.group.ui.components.GroupWizardTopBar
import com.qryptin.chat.group.ui.components.MembersPreviewRow
import com.qryptin.chat.group.viewmodel.GroupCreationViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  CreateGroupDetailsScreen  — Step 2 of group creation wizard
// ─────────────────────────────────────────────────────────────
@Composable
fun CreateGroupDetailsScreen(
    viewModel         : GroupCreationViewModel,
    onBack            : () -> Unit,
    onNext            : () -> Unit,
    onCustomPermissions: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GroupWizardTopBar(
                title         = "Create Group",
                onBack        = onBack,
                actionLabel   = "Next",
                onAction      = onNext,
                actionEnabled = uiState.canProceedFromDetails,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = QryptDimens.PaddingScreenH),
            verticalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Group photo ──────────────────────────────────
            Box(
                modifier         = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .clickable { viewModel.onGroupPhotoPicked("mock://group-photo") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.PhotoCamera,
                        contentDescription = "Set group photo",
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                }
                if (uiState.groupPhotoUri != null) {
                    // In a real app: AsyncImage; here we show a filled circle
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.PhotoCamera,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }

            Text(
                "Tap to set group photo",
                style    = TypoBodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(4.dp))

            // ── Group name ───────────────────────────────────
            OutlinedTextField(
                value         = uiState.groupName,
                onValueChange = viewModel::onGroupNameChanged,
                label         = { Text("Group Name (optional)") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        "${uiState.groupName.length}/50",
                        modifier = Modifier.fillMaxWidth(),
                        style    = TypoLabelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )

            // ── Group description ────────────────────────────
            OutlinedTextField(
                value         = uiState.groupDescription,
                onValueChange = viewModel::onGroupDescriptionChanged,
                label         = { Text("Group Description (optional)") },
                minLines      = 3,
                maxLines      = 5,
                modifier      = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        "${uiState.groupDescription.length}/120",
                        modifier = Modifier.fillMaxWidth(),
                        style    = TypoLabelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )

            // ── Admin permissions card ───────────────────────
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Admin Permissions", style = TypoBodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            "Group admins can manage members, change group info, and moderate content.",
                            style = TypoBodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Custom permissions row ───────────────────────
            Surface(
                modifier      = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onCustomPermissions),
                color         = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shape          = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier  = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Custom Permissions", style = TypoBodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            "Control what members can do in this group",
                            style = TypoBodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── Selected members preview ─────────────────────
            MembersPreviewRow(
                members  = uiState.selectedMembers,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
