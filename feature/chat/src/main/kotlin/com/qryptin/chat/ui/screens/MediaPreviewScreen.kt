package com.qryptin.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qryptin.chat.model.MediaPreviewItem
import com.qryptin.core.designsystem.ui.theme.TypoBodyLarge

// ─────────────────────────────────────────────────────────────
//  MediaPreviewScreen
//  Full-bleed preview of one or more picked images/videos with
//  a caption field and a filmstrip for multi-selection, shown
//  before the attachment is actually sent.
// ─────────────────────────────────────────────────────────────
@Composable
fun MediaPreviewScreen(
    items      : List<MediaPreviewItem>,
    onBack     : () -> Unit,
    onSend     : (caption: String) -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var caption by remember { mutableStateOf("") }
    val current = items.getOrNull(currentIndex)

    Scaffold(containerColor = Color.Black) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                }
                Text("${currentIndex + 1} / ${items.size}", style = TypoBodyLarge, color = Color.White)
                Spacer(Modifier.width(48.dp))
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (current != null) {
                    if (current.isVideo) {
                        Box(
                            modifier = Modifier.fillMaxSize(0.9f).background(Color.DarkGray),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(56.dp))
                        }
                    } else {
                        AsyncImage(
                            model              = current.uri,
                            contentDescription = "Preview",
                            modifier           = Modifier.fillMaxSize(0.95f),
                        )
                    }
                }
            }

            if (items.size > 1) {
                LazyRow(contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(items) { item ->
                        val index = items.indexOf(item)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (index == currentIndex) Color.White.copy(alpha = 0.3f) else Color.DarkGray)
                                .clickableThumbnail { currentIndex = index },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value         = caption,
                    onValueChange = { caption = it },
                    placeholder   = { Text("Add a caption…", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor   = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { onSend(caption) },
                    colors  = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

private fun Modifier.clickableThumbnail(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
