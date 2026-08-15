package com.qryptin.contacts.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.model.ContactTab
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ContactsSearchBar
// ─────────────────────────────────────────────────────────────

/**
 * Prominent search bar with animated clear button and keyboard handling.
 * Mirrors the OutlinedTextField style used throughout the auth flow.
 */
@Composable
fun ContactsSearchBar(
    query        : String,
    onQueryChange: (String) -> Unit,
    onClear      : () -> Unit,
    modifier     : Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.fillMaxWidth(),
        placeholder   = {
            Text(
                "Search contacts…",
                style = TypoBodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector        = Icons.Rounded.Search,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter   = scaleIn(tween(150)) + fadeIn(tween(150)),
                exit    = scaleOut(tween(150)) + fadeOut(tween(150)),
            ) {
                IconButton(onClick = {
                    onClear()
                    keyboard?.hide()
                }) {
                    Icon(
                        imageVector        = Icons.Rounded.Cancel,
                        contentDescription = "Clear search",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(18.dp),
                    )
                }
            }
        },
        singleLine      = true,
        shape           = RoundedCornerShape(QryptDimens.RadiusSmall),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor     = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor   = MaterialTheme.colorScheme.outline,
            focusedContainerColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
    )
}

// ─────────────────────────────────────────────────────────────
//  ContactTabRow
// ─────────────────────────────────────────────────────────────

/**
 * Segmented tab row for All / QryptIN / Invite tabs.
 * Counts are shown as coloured badges on the QryptIN and Invite tabs.
 */
@Composable
fun ContactTabRow(
    selectedTab     : ContactTab,
    qryptinCount    : Int,
    inviteCount     : Int,
    onTabSelected   : (ContactTab) -> Unit,
    modifier        : Modifier = Modifier,
) {
    val selectedIndex = ContactTab.entries.indexOf(selectedTab)

    // Fixed, equal-width TabRow (not ScrollableTabRow) — with only 3 tabs,
    // ScrollableTabRow sizes tabs to their content and left-aligns them,
    // which is what produced the broken/uneven placement. TabRow spreads
    // all tabs evenly across the full available width.
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier         = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        containerColor   = Color.Transparent,
        contentColor     = MaterialTheme.colorScheme.primary,
        divider          = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
        indicator        = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedIndex])
                        .padding(horizontal = 20.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        },
    ) {
        ContactTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val count    = when (tab) {
                ContactTab.QRYPTIN -> qryptinCount
                ContactTab.INVITE  -> inviteCount
                else               -> null
            }

            val textColor by animateColorAsState(
                targetValue = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                label = "tab_text_color",
            )

            Tab(
                selected = selected,
                onClick  = { onTabSelected(tab) },
                text     = {
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text  = tab.label,
                            style = if (selected)
                                TypoLabelMedium.copy(fontWeight = FontWeight.SemiBold)
                            else
                                TypoLabelMedium,
                            color = textColor,
                        )
                        if (count != null) {
                            ContactCountBadge(count = count, active = selected)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun ContactCountBadge(count: Int, active: Boolean) {
    val bg   = if (active) MaterialTheme.colorScheme.primary
               else        MaterialTheme.colorScheme.surfaceVariant
    val text = if (active) MaterialTheme.colorScheme.onPrimary
               else        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = CircleShape,
        color = bg,
    ) {
        Text(
            text     = count.toString(),
            style    = TypoLabelSmall,
            color    = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  ContactListHeader  (alphabetical section separator)
// ─────────────────────────────────────────────────────────────

@Composable
fun ContactSectionHeader(letter: Char, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = letter.toString(),
            style    = TypoLabelMedium.copy(fontWeight = FontWeight.Bold),
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(20.dp),
        )
        HorizontalDivider(
            modifier  = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp,
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  ContactRow
// ─────────────────────────────────────────────────────────────

/**
 * A single contact list item. Shows:
 *  - Coloured avatar (initials fallback)
 *  - Online indicator dot
 *  - Name + last seen / phone
 *  - Invite button OR message icon based on [isOnQryptIN]
 */
@Composable
fun ContactRow(
    contact     : Contact,
    onClick     : () -> Unit,
    onInvite    : () -> Unit,
    onChatClick : ((Contact) -> Unit)? = null,
    modifier    : Modifier = Modifier,
) {
    Surface(
        onClick           = onClick,
        modifier          = modifier.fillMaxWidth(),
        color             = Color.Transparent,
    ) {
        Row(
            modifier             = Modifier
                .fillMaxWidth()
                .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 10.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
        ) {
            // Avatar
            ContactAvatar(contact = contact)

            // Text block
            Column(
                modifier          = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text     = contact.displayName,
                    style    = TypoBodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Phone number — always shown, regardless of QryptIN status.
                Text(
                    text     = contact.phone,
                    style    = TypoBodySmall,
                    color    = if (contact.lastSeen == "Online")
                                   MaterialTheme.colorScheme.primary
                               else
                                   MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Secondary line: presence (if any) else save timestamp.
                val secondaryLine = contact.lastSeen?.takeIf { it != "Online" }
                    ?: contact.formattedCreatedAt.takeIf { it.isNotBlank() }
                if (secondaryLine != null) {
                    Text(
                        text  = secondaryLine,
                        style = TypoBodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (contact.mutualGroups > 0) {
                    Text(
                        text  = "${contact.mutualGroups} mutual group${if (contact.mutualGroups > 1) "s" else ""}",
                        style = TypoBodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            // Action indicator
            if (contact.isOnQryptIN) {
                if (onChatClick != null) {
                    ChatIconButton(onClick = { onChatClick(contact) })
                } else {
                    QryptINBadge()
                }
            } else {
                InviteButton(onClick = onInvite)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ContactAvatar
// ─────────────────────────────────────────────────────────────

@Composable
private fun ContactAvatar(contact: Contact, modifier: Modifier = Modifier) {
    val avatarColor = remember(contact.id) { avatarColorFor(contact.id) }

    Box(modifier = modifier) {
        // Avatar circle
        Box(
            modifier          = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment  = Alignment.Center,
        ) {
            Text(
                text  = contact.initials,
                style = TypoLabelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = Color.White,
            )
        }

        // Online dot
        if (contact.lastSeen == "Online") {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .align(Alignment.BottomEnd),
            )
        }
    }
}

/** Deterministic avatar colour derived from contact id to keep it stable. */
private fun avatarColorFor(id: String): Color {
    val palette = listOf(
        Color(0xFF6B5EE4), Color(0xFF00897B), Color(0xFFE53935),
        Color(0xFF5E35B1), Color(0xFF1E88E5), Color(0xFF43A047),
        Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
        Color(0xFF3949AB),
    )
    return palette[id.hashCode().mod(palette.size).let { if (it < 0) it + palette.size else it }]
}

private val OnlineGreen = Color(0xFF00C853)

// ─────────────────────────────────────────────────────────────
//  QryptIN presence badge
// ─────────────────────────────────────────────────────────────

@Composable
private fun QryptINBadge() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
    ) {
        Icon(
            imageVector        = Icons.Rounded.Lock,
            contentDescription = "On QryptIN",
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier
                .padding(6.dp)
                .size(16.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Chat icon button (replaces badge when chat navigation is wired)
// ─────────────────────────────────────────────────────────────

@Composable
private fun ChatIconButton(onClick: () -> Unit) {
    Surface(
        onClick           = onClick,
        shape             = CircleShape,
        color             = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
    ) {
        Icon(
            imageVector        = Icons.Rounded.Chat,
            contentDescription = "Open chat",
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier
                .padding(8.dp)
                .size(18.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Invite button
// ─────────────────────────────────────────────────────────────

@Composable
private fun InviteButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(QryptDimens.RadiusSmall),
        color   = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border  = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier             = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector        = Icons.Rounded.PersonAdd,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.secondary,
                modifier           = Modifier.size(14.dp),
            )
            Text(
                text  = "Invite",
                style = TypoLabelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  AlphabetScrollBar
// ─────────────────────────────────────────────────────────────

/**
 * Vertical strip of letters along the right edge.
 * Tapping a letter invokes [onLetterSelected] so the parent can scroll the list.
 */
@Composable
fun AlphabetScrollBar(
    letters         : List<Char>,
    activeIndex     : Char?,
    onLetterSelected: (Char) -> Unit,
    modifier        : Modifier = Modifier,
) {
    Column(
        modifier              = modifier,
        verticalArrangement   = Arrangement.spacedBy(1.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        letters.forEach { letter ->
            val isActive = letter == activeIndex
            Text(
                text      = letter.toString(),
                style     = TypoLabelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color     = if (isActive) MaterialTheme.colorScheme.primary
                            else          MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier  = Modifier
                    .clickable { onLetterSelected(letter) }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SaveOptionCard
// ─────────────────────────────────────────────────────────────
@Composable
fun SaveOptionCard(
    option    : ContactSaveOption,
    isSelected: Boolean,
    onSelect  : () -> Unit,
) {
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    val bgColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    else
        MaterialTheme.colorScheme.surface

    OutlinedCard(
        onClick  = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.outlinedCardColors(containerColor = bgColor),
        border   = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(QryptDimens.SpaceMD),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 0.2f else 0.12f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (option) {
                            ContactSaveOption.SYNCED_WITH_SIM -> Icons.Rounded.SimCard
                            ContactSaveOption.QRYPTIN_ONLY    -> Icons.Rounded.PhoneAndroid
                        },
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    option.title,
                    style = TypoBodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    option.description,
                    style = TypoBodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Radio
            RadioButton(
                selected = isSelected,
                onClick  = onSelect,
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  New Contact FAB
// ─────────────────────────────────────────────────────────────

@Composable
fun NewContactFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick            = onClick,
        modifier           = modifier,
        containerColor     = MaterialTheme.colorScheme.primary,
        contentColor       = MaterialTheme.colorScheme.onPrimary,
        icon               = {
            Icon(
                imageVector        = Icons.Rounded.PersonAdd,
                contentDescription = null,
            )
        },
        text               = { Text("New Contact", style = TypoLabelMedium) },
    )
}

// ─────────────────────────────────────────────────────────────
//  Empty state
// ─────────────────────────────────────────────────────────────

@Composable
fun ContactsEmptyState(
    query     : String,
    selectedTab : com.qryptin.contacts.model.ContactTab,
    modifier  : Modifier = Modifier,
) {
    val (icon, title, body) = when {
        query.isNotBlank() -> Triple(
            Icons.Rounded.SearchOff,
            "No results for \"$query\"",
            "Try a different name or phone number",
        )
        selectedTab == com.qryptin.contacts.model.ContactTab.QRYPTIN -> Triple(
            Icons.Rounded.Lock,
            "No QryptIN contacts yet",
            "Invite your friends to join QryptIN",
        )
        selectedTab == com.qryptin.contacts.model.ContactTab.INVITE -> Triple(
            Icons.Rounded.PersonAdd,
            "Everyone's already on QryptIN!",
            "All your contacts are already using QryptIN",
        )
        else -> Triple(
            Icons.Rounded.PeopleOutline,
            "No contacts yet",
            "Your phone contacts will appear here",
        )
    }

    Column(
        modifier              = modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier
                    .padding(20.dp)
                    .size(40.dp),
            )
        }
        Text(
            text  = title,
            style = TypoTitleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text  = body,
            style = TypoBodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
