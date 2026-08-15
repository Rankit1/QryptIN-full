package com.qryptin.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qryptin.chat.model.Attachment
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  AttachmentMenuItem
//  One tile in AttachmentBottomSheet: image / video / document /
//  camera / location / contact. Icon background tints with
//  QryptPrimary blue at low alpha, kept consistent for every
//  option instead of WhatsApp's per-type colour palette.
// ─────────────────────────────────────────────────────────────
@Composable
fun AttachmentMenuItem(
    icon     : ImageVector,
    label    : String,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit,
) {
    Column(
        modifier            = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier         = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = TypoLabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────
//  AttachmentCard
//  Generic file-attachment card — icon, name, size, and either
//  a download affordance (incoming, not yet fetched) or a
//  progress indicator (sending/uploading).
// ─────────────────────────────────────────────────────────────
@Composable
fun AttachmentCard(
    attachment        : Attachment,
    icon               : ImageVector,
    isDownloaded       : Boolean = true,
    uploadProgress     : Float?  = null,   // null => no in-flight transfer
    modifier           : Modifier = Modifier,
    onClick            : () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(QryptDimens.RadiusSmall))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(QryptDimens.RadiusSmall)),
            contentAlignment = Alignment.Center,
        ) {
            if (uploadProgress != null) {
                CircularProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier.size(22.dp),
                    color    = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(attachment.fileName, style = TypoBodyMedium.copy(fontWeight = FontWeight.Medium), maxLines = 1)
            Text(formatBytes(attachment.sizeBytes), style = TypoLabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (!isDownloaded && uploadProgress == null) {
            IconButton(onClick = onClick) {
                Icon(androidx.compose.material.icons.Icons.Rounded.FileDownload, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "—"
    val kb = bytes / 1024.0
    return if (kb < 1024) "%.0f KB".format(kb) else "%.1f MB".format(kb / 1024.0)
}
