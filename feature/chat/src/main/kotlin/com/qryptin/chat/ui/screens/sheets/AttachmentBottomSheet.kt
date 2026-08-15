package com.qryptin.chat.ui.screens.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qryptin.chat.model.MessageType
import com.qryptin.chat.ui.components.AttachmentMenuItem

// ─────────────────────────────────────────────────────────────
//  AttachmentBottomSheet
//  image / video / document / camera / location / contact —
//  the six options from the spec's attachment menu. Each option
//  reports its MessageType back up to ChatConversationScreen;
//  wiring a real Android media-picker intent for each is the
//  next step once the rest of the module compiles end to end.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss               : () -> Unit,
    onAttachmentTypePicked  : (MessageType) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AttachmentMenuItem(Icons.Rounded.Photo, "Gallery") { onAttachmentTypePicked(MessageType.IMAGE); onDismiss() }
            AttachmentMenuItem(Icons.Rounded.Videocam, "Video") { onAttachmentTypePicked(MessageType.VIDEO); onDismiss() }
            AttachmentMenuItem(Icons.Rounded.InsertDriveFile, "Document") { onAttachmentTypePicked(MessageType.DOCUMENT); onDismiss() }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AttachmentMenuItem(Icons.Rounded.PhotoCamera, "Camera") { onAttachmentTypePicked(MessageType.IMAGE); onDismiss() }
            AttachmentMenuItem(Icons.Rounded.LocationOn, "Location") { onDismiss() }
            AttachmentMenuItem(Icons.Rounded.Person, "Contact") { onDismiss() }
        }
    }
}
