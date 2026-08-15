package com.qryptin.chat.group.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.qryptin.contacts.model.Contact
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  GroupContactRow
//  Single selectable contact row used in SelectParticipants.
// ─────────────────────────────────────────────────────────────
@Composable
fun GroupContactRow(
    contact    : Contact,
    isSelected : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
    ) {
        Box {
            QryptAvatar(name = contact.displayName, avatarUrl = contact.avatarUrl, size = 46.dp)
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter   = scaleIn(spring(Spring.DampingRatioMediumBouncy)),
                exit    = scaleOut(),
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(contact.displayName, style = TypoBodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(contact.phone, style = TypoBodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Checkbox(
            checked         = isSelected,
            onCheckedChange = { onClick() },
            colors          = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  SelectedMembersBar
//  Sticky bottom strip on SelectParticipants — shows avatars
//  with remove buttons, count label.
// ─────────────────────────────────────────────────────────────
@Composable
fun SelectedMembersBar(
    selected : List<Contact>,
    onRemove : (Contact) -> Unit,
    modifier : Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = selected.isNotEmpty(),
        enter   = slideInVertically { it } + fadeIn(),
        exit    = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            modifier      = Modifier
                .fillMaxWidth()
                .shadow(8.dp),
            color         = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH, vertical = 12.dp)) {
                Text(
                    "Selected (${selected.size})",
                    style = TypoLabelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(selected, key = { it.id }) { contact ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                QryptAvatar(name = contact.displayName, size = 44.dp)
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                        .clickable { onRemove(contact) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Remove ${contact.displayName}",
                                        tint     = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(10.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                contact.displayName.split(" ").first(),
                                style    = TypoLabelSmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  MembersPreviewRow
//  Horizontal scroll of member avatars + count — used in
//  CreateGroupDetailsScreen.
// ─────────────────────────────────────────────────────────────
@Composable
fun MembersPreviewRow(
    members  : List<Contact>,
    modifier : Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "${members.size} member${if (members.size != 1) "s" else ""}",
            style = TypoLabelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            items(members.take(8), key = { it.id }) { contact ->
                QryptAvatar(
                    name     = contact.displayName,
                    size     = 32.dp,
                    modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                )
            }
            if (members.size > 8) {
                item {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "+${members.size - 8}",
                            style = TypoLabelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  PermissionToggleRow
// ─────────────────────────────────────────────────────────────
@Composable
fun PermissionToggleRow(
    icon     : androidx.compose.ui.graphics.vector.ImageVector,
    title    : String,
    subtitle : String?   = null,
    checked  : Boolean,
    onToggle : (Boolean) -> Unit,
    modifier : Modifier  = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TypoBodyLarge, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, style = TypoBodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  GroupTopBar — shared top app bar component for the wizard
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupWizardTopBar(
    title       : String,
    onBack      : () -> Unit,
    actionLabel : String?    = null,
    onAction    : (() -> Unit)? = null,
    actionEnabled: Boolean   = true,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        title = { Text(title, style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground) },
        actions = {
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction, enabled = actionEnabled) {
                    Text(
                        actionLabel,
                        style = TypoTitleMedium,
                        color = if (actionEnabled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}
