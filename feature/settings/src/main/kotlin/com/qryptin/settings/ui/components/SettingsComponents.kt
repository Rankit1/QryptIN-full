package com.qryptin.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qryptin.settings.model.Profile

// ─────────────────────────────────────────────────────────────
//  Shared building blocks for every settings screen so each one
//  is composed of LazyColumn items rather than a fixed Column —
//  the actual fix for Issue 1 (settings couldn't scroll fully).
// ─────────────────────────────────────────────────────────────

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text     = title.uppercase(),
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column { content() }
    }
}

// ─────────────────────────────────────────────────────────────
//  Issue 6 fix: the settings header used to be a static "Edit
//  Profile" row with a generic icon — it never reflected what the
//  user had actually set. This reads live from ProfileRepository
//  (via SettingsViewModel/ProfileViewModel's shared DataStore
//  flow) so the moment a profile edit is saved, this card updates
//  instantly — no manual refresh, no stale placeholder.
// ─────────────────────────────────────────────────────────────
@Composable
fun ProfileHeaderCard(profile: Profile, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                if (profile.avatarUri != null) {
                    AsyncImage(
                        model = profile.avatarUri,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                val name = profile.displayName.ifBlank { "Set up your profile" }
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                if (profile.username.isNotBlank()) {
                    Text(
                        "@${profile.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                val bioOrHint = profile.bio.ifBlank { "Name, username, bio, and photo" }
                Text(
                    bioOrHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }

            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsNavRow(
    icon        : ImageVector,
    title       : String,
    subtitle    : String? = null,
    onClick     : () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsSwitchRow(
    icon        : ImageVector? = null,
    title       : String,
    subtitle    : String? = null,
    checked     : Boolean,
    onCheckedChange : (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Bottom padding so the last item never sits behind the bottom navbar. */
val SettingsListBottomPadding = 96.dp
