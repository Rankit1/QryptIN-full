package com.qryptin.chat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qryptin.chat.model.Chat
import com.qryptin.chat.model.ChatListFilter
import com.qryptin.chat.model.MessageStatus
import com.qryptin.core.designsystem.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  ChatSearchBar
//  Same visual language as feature:contacts' ContactsSearchBar
//  so search feels identical across the two tabs.
// ─────────────────────────────────────────────────────────────
@Composable
fun ChatSearchBar(
    query         : String,
    onQueryChange : (String) -> Unit,
    onClear       : () -> Unit,
    modifier      : Modifier = Modifier,
    placeholder   : String   = "Search chats, contacts, documents…",
) {
    val keyboard = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.fillMaxWidth(),
        placeholder   = {
            Text(placeholder, style = TypoBodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        },
        leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter   = scaleIn(tween(150)) + fadeIn(tween(150)),
                exit    = scaleOut(tween(150)) + fadeOut(tween(150)),
            ) {
                IconButton(onClick = { onClear(); keyboard?.hide() }) {
                    Icon(Icons.Rounded.Cancel, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine      = true,
        shape           = RoundedCornerShape(QryptDimens.RadiusSmall),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
            focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
    )
}

// ─────────────────────────────────────────────────────────────
//  ChatFilterRow — All / Unread / Groups / Pinned chips
// ─────────────────────────────────────────────────────────────
@Composable
fun ChatFilterRow(
    selected   : ChatListFilter,
    onSelect   : (ChatListFilter) -> Unit,
    modifier   : Modifier = Modifier,
) {
    LazyRow(
        modifier            = modifier,
        horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceSM),
        contentPadding      = PaddingValues(horizontal = QryptDimens.PaddingScreenH),
    ) {
        items(ChatListFilter.entries.toList()) { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick  = { onSelect(filter) },
                label    = { Text(filter.label, style = TypoLabelMedium) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    selectedLabelColor     = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  TypingIndicatorDots — three bouncing dots, used in ChatCard
//  previews and the conversation header/footer.
// ─────────────────────────────────────────────────────────────
@Composable
fun TypingIndicatorDots(
    modifier : Modifier = Modifier,
    dotColor : androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val delayMs = index * 150
            val scale by transition.animateFloat(
                initialValue  = 0.4f,
                targetValue   = 1f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(600, delayMillis = delayMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = scale)),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  DeliveryStatusIcon — sending / sent / delivered / read ticks.
//  Read uses the primary blue tint; everything else is neutral —
//  there is no separate "read = green" convention here.
// ─────────────────────────────────────────────────────────────
@Composable
fun DeliveryStatusIcon(status: MessageStatus, modifier: Modifier = Modifier) {
    val (icon, tint) = when (status) {
        MessageStatus.SENDING   -> Icons.Rounded.Schedule to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.SENT      -> Icons.Rounded.Check to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.DELIVERED -> Icons.Rounded.DoneAll to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.READ      -> Icons.Rounded.DoneAll to MaterialTheme.colorScheme.primary
        MessageStatus.FAILED    -> Icons.Rounded.ErrorOutline to MaterialTheme.colorScheme.error
    }
    Icon(icon, contentDescription = status.name, tint = tint, modifier = modifier.size(14.dp))
}

// ─────────────────────────────────────────────────────────────
//  ChatCard — single row on ChatListScreen.
// ─────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatCard(
    chat        : Chat,
    isSelected  : Boolean,
    onClick     : () -> Unit,
    onLongPress : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceSM + 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QryptAvatar(name = chat.title, avatarUrl = chat.avatarUrl, isOnline = chat.isOnline, size = 52.dp)

        Spacer(Modifier.width(QryptDimens.SpaceMD))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.isPinned) {
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = "Pinned",
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp).padding(end = 4.dp),
                    )
                }
                Text(
                    text       = chat.title,
                    style      = TypoTitleMedium.copy(fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Medium),
                    color      = MaterialTheme.colorScheme.onBackground,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f, fill = false),
                )
                if (chat.isMuted) {
                    Icon(
                        Icons.Rounded.NotificationsOff,
                        contentDescription = "Muted",
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp).padding(start = 6.dp),
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.isTyping) {
                    TypingIndicatorDots(modifier = Modifier.padding(end = 6.dp))
                    Text("typing…", style = TypoBodyMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                } else {
                    val lastMessage = chat.lastMessage
                    if (lastMessage != null && lastMessage.isOutgoing) {
                        DeliveryStatusIcon(lastMessage.status, modifier = Modifier.padding(end = 4.dp))
                    }
                    Text(
                        text     = lastMessage?.let { previewTextFor(it) } ?: "No messages yet",
                        style    = TypoBodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }

        Spacer(Modifier.width(QryptDimens.SpaceSM))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text  = chat.lastMessage?.timestamp?.let { formatTimestamp(it) } ?: "",
                style = TypoBodySmall,
                color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            if (chat.unreadCount > 0) {
                Box(
                    modifier         = Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        style = TypoLabelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
        }
    }
}

private fun previewTextFor(message: com.qryptin.chat.model.Message): String {
    if (message.isDeleted) return "This message was deleted"
    val prefix = if (message.senderId == "me") "You: " else ""
    return prefix + when (message.type) {
        com.qryptin.chat.model.MessageType.TEXT     -> message.text.orEmpty()
        com.qryptin.chat.model.MessageType.IMAGE    -> "Photo"
        com.qryptin.chat.model.MessageType.VIDEO    -> "Video"
        com.qryptin.chat.model.MessageType.AUDIO    -> "Voice message"
        com.qryptin.chat.model.MessageType.DOCUMENT -> message.attachment?.fileName ?: "Document"
        com.qryptin.chat.model.MessageType.STICKER  -> "Sticker"
        com.qryptin.chat.model.MessageType.SYSTEM   -> message.text.orEmpty()
    }
}

private fun formatTimestamp(millis: Long): String {
    val now = java.util.Calendar.getInstance()
    val then = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    return if (now.get(java.util.Calendar.DAY_OF_YEAR) == then.get(java.util.Calendar.DAY_OF_YEAR) &&
        now.get(java.util.Calendar.YEAR) == then.get(java.util.Calendar.YEAR)
    ) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}
