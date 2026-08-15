package com.qryptin.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qryptin.chat.model.Message
import com.qryptin.chat.model.MessageType
import com.qryptin.core.designsystem.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  MessageBubble
//  Renders one message in ChatConversationScreen — outgoing
//  bubbles align right with the primary-blue "encrypted" accent,
//  incoming bubbles align left in a neutral surface tone.
// ─────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message       : Message,
    showSenderName: Boolean = false,
    onLongPress   : () -> Unit = {},
    modifier      : Modifier = Modifier,
) {
    if (message.type == MessageType.SYSTEM) {
        SystemMessageRow(message, modifier)
        return
    }

    val isOutgoing = message.isOutgoing
    val bubbleColor = if (isOutgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isOutgoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(
        topStart    = QryptDimens.RadiusMedium,
        topEnd      = QryptDimens.RadiusMedium,
        bottomStart = if (isOutgoing) QryptDimens.RadiusMedium else 4.dp,
        bottomEnd   = if (isOutgoing) 4.dp else QryptDimens.RadiusMedium,
    )

    Row(
        modifier          = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (showSenderName && !isOutgoing) {
                Text(
                    text  = message.senderName,
                    style = TypoLabelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(2.dp))
            }

            message.replyToMessageId?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(contentColor.copy(alpha = 0.10f))
                        .padding(6.dp),
                ) {
                    Text("Replying to a message", style = TypoBodySmall, color = contentColor.copy(alpha = 0.8f))
                }
                Spacer(Modifier.height(4.dp))
            }

            if (message.isDeleted) {
                Text(
                    text  = "This message was deleted",
                    style = TypoBodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = contentColor.copy(alpha = 0.6f),
                )
            } else {
                when (message.type) {
                    MessageType.TEXT     -> Text(message.text.orEmpty(), style = TypoBodyLarge, color = contentColor)
                    MessageType.IMAGE    -> ImageBubbleContent(message, contentColor)
                    MessageType.VIDEO    -> VideoBubbleContent(message, contentColor)
                    MessageType.AUDIO    -> AudioBubbleContent(message, contentColor)
                    MessageType.DOCUMENT -> DocumentBubbleContent(message, contentColor)
                    MessageType.STICKER  -> StickerBubbleContent(message)
                    MessageType.SYSTEM   -> Unit
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text  = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                    style = TypoLabelSmall,
                    color = contentColor.copy(alpha = 0.7f),
                )
                if (isOutgoing) {
                    Spacer(Modifier.width(4.dp))
                    DeliveryStatusIcon(message.status, modifier = Modifier.size(12.dp))
                }
            }

            if (message.reactions.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(message.reactions.joinToString(" ") { it.emoji }, style = TypoBodySmall)
            }
        }
    }
}

@Composable
private fun ImageBubbleContent(message: Message, contentColor: Color) {
    Column {
        AsyncImage(
            model              = message.attachment?.localUri,
            contentDescription = "Image",
            modifier           = Modifier
                .widthIn(max = 240.dp)
                .heightIn(max = 240.dp)
                .clip(RoundedCornerShape(QryptDimens.RadiusSmall)),
        )
        val caption = message.text
        if (!caption.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(caption, style = TypoBodyLarge, color = contentColor)
        }
    }
}

@Composable
private fun VideoBubbleContent(message: Message, contentColor: Color) {
    Box(
        modifier = Modifier
            .widthIn(max = 240.dp)
            .heightIn(max = 240.dp)
            .clip(RoundedCornerShape(QryptDimens.RadiusSmall))
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play video", tint = Color.White, modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun AudioBubbleContent(message: Message, contentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            modifier         = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(contentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play voice message", tint = contentColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        val durationSec = ((message.attachment?.durationMs ?: 0L) / 1000).toInt()
        Text("Voice message · 0:${durationSec.toString().padStart(2, '0')}", style = TypoBodyMedium, color = contentColor)
    }
}

@Composable
private fun DocumentBubbleContent(message: Message, contentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(Icons.Rounded.InsertDriveFile, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(message.attachment?.fileName ?: "Document", style = TypoBodyMedium, color = contentColor, fontWeight = FontWeight.Medium)
            Text(formatFileSize(message.attachment?.sizeBytes ?: 0L), style = TypoLabelSmall, color = contentColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun StickerBubbleContent(message: Message) {
    Text(
        text  = message.attachment?.localUri ?: "🎉",
        style = androidx.compose.ui.text.TextStyle(fontSize = 48.sp),
    )
}

@Composable
private fun SystemMessageRow(message: Message, modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(message.text.orEmpty(), style = TypoLabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "—"
    val kb = bytes / 1024.0
    return if (kb < 1024) "%.0f KB".format(kb) else "%.1f MB".format(kb / 1024.0)
}
