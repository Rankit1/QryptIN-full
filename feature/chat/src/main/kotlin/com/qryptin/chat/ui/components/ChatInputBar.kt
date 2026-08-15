package com.qryptin.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ChatInputBar
//  Bottom composer for ChatConversationScreen. Mic morphs into
//  send the moment the draft is non-blank; attachment + emoji
//  open their respective bottom sheets via the callbacks.
// ─────────────────────────────────────────────────────────────
@Composable
fun ChatInputBar(
    draftText      : String,
    onDraftChange  : (String) -> Unit,
    onSend         : () -> Unit,
    onEmojiClick   : () -> Unit,
    onAttachClick  : () -> Unit,
    onMicClick     : () -> Unit = {},
    modifier       : Modifier = Modifier,
) {
    val showSend = draftText.isNotBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        IconButton(onClick = onEmojiClick) {
            Icon(Icons.Rounded.EmojiEmotions, contentDescription = "Emoji", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextFieldWrapper(
                    value         = draftText,
                    onValueChange = onDraftChange,
                    modifier      = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 10.dp),
                )
                IconButton(onClick = onAttachClick) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        AnimatedContent(targetState = showSend, label = "mic_send_swap") { isSend ->
            FilledIconButton(
                onClick = { if (isSend) onSend() else onMicClick() },
                colors  = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    imageVector        = if (isSend) Icons.Rounded.Send else Icons.Rounded.Mic,
                    contentDescription = if (isSend) "Send" else "Record voice message",
                    tint               = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

/**
 * Thin wrapper around BasicTextField with a placeholder, kept local to this
 * file so ChatInputBar has no dependency on any specific TextField styling
 * elsewhere in the app.
 */
@Composable
private fun BasicTextFieldWrapper(
    value         : String,
    onValueChange : (String) -> Unit,
    modifier      : Modifier = Modifier,
) {
    Box(modifier = modifier) {
        androidx.compose.foundation.text.BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            textStyle     = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush   = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("Message", style = TypoBodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                innerTextField()
            },
        )
    }
}
