package com.qryptin.core.designsystem.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────
//  BottomNavDestination
//  Single source of truth for the four bottom-nav tabs.
// ─────────────────────────────────────────────────────────────
enum class BottomNavDestination(
    val route        : String,
    val title        : String,
    val icon         : ImageVector,
    val selectedIcon : ImageVector,
) {
    HOME(
        route        = "chats",
        title        = "Chats",
        icon         = Icons.Rounded.ChatBubbleOutline,
        selectedIcon = Icons.Rounded.ChatBubble,
    ),
    CONTACTS(
        route        = "contacts",
        title        = "Contacts",
        icon         = Icons.Rounded.PeopleOutline,
        selectedIcon = Icons.Rounded.People,
    ),
    CALLS(
        route        = "calls",
        title        = "Calls",
        icon         = Icons.Outlined.Call,
        selectedIcon = Icons.Rounded.Call,
    ),
    SETTINGS(
        route        = "settings",
        title        = "Settings",
        icon         = Icons.Outlined.Settings,
        selectedIcon = Icons.Rounded.Settings,
    );

    companion object {
        fun fromRoute(route: String?): BottomNavDestination {
            return when {
                route == null -> HOME
                route.startsWith("chats") || route.startsWith("conversation") -> HOME
                route.startsWith("contacts") -> CONTACTS
                route.startsWith("calls") -> CALLS
                route.startsWith("settings") -> SETTINGS
                else -> HOME
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  BottomNavItem — DEPRECATED, kept only for source compatibility.
// ─────────────────────────────────────────────────────────────
@Deprecated(
    message = "Use BottomNavDestination — it now carries title/icon/selectedIcon directly.",
    replaceWith = ReplaceWith("BottomNavDestination"),
)
sealed class BottomNavItem(
    val route        : String,
    val title        : String,
    val icon         : ImageVector,
    val selectedIcon : ImageVector,
) {
    data object Chats : BottomNavItem(
        route        = "chats",
        title        = "Chats",
        icon         = Icons.Rounded.ChatBubbleOutline,
        selectedIcon = Icons.Rounded.ChatBubble,
    )
    data object Contacts : BottomNavItem(
        route        = "contacts",
        title        = "Contacts",
        icon         = Icons.Rounded.PeopleOutline,
        selectedIcon = Icons.Rounded.People,
    )
    data object Calls : BottomNavItem(
        route        = "calls",
        title        = "Calls",
        icon         = Icons.Outlined.Call,
        selectedIcon = Icons.Rounded.Call,
    )
    data object Settings : BottomNavItem(
        route        = "settings",
        title        = "Settings",
        icon         = Icons.Outlined.Settings,
        selectedIcon = Icons.Rounded.Settings,
    )

    companion object {
        val all: List<BottomNavItem> = listOf(Chats, Contacts, Calls, Settings)
    }
}

// ─────────────────────────────────────────────────────────────
//  QryptBottomNavBar
// ─────────────────────────────────────────────────────────────
@Composable
fun QryptBottomNavBar(
    selected : BottomNavDestination,
    onSelect : (BottomNavDestination) -> Unit,
    modifier : Modifier = Modifier,
) {
    NavigationBar(
        modifier       = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        BottomNavDestination.entries.forEach { dest ->
            val isSelected = dest == selected
            NavigationBarItem(
                selected = isSelected,
                onClick  = { if (!isSelected) onSelect(dest) },
                icon     = {
                    Icon(
                        imageVector        = if (isSelected) dest.selectedIcon else dest.icon,
                        contentDescription = dest.title,
                    )
                },
                label    = { Text(dest.title, style = TypoLabelSmall) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    indicatorColor      = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
