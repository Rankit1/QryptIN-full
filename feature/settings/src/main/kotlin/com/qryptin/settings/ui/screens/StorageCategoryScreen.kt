package com.qryptin.settings.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.media.MediaCategory
import com.qryptin.chat.media.MediaItem
import com.qryptin.settings.viewmodel.StorageViewModel
import com.qryptin.settings.viewmodel.formatBytes
import java.text.DateFormat
import java.util.Date

// ─────────────────────────────────────────────────────────────
//  StorageCategoryScreen
//  Issue 7 fix — browse and delete individual media within one
//  category (Images / Videos / Documents & downloads / Audio).
//  Backed by StorageViewModel.mediaFor(category), which is a live
//  Room query, so a delete here instantly disappears from the
//  list and updates the Manage Storage totals — no manual refresh.
// ─────────────────────────────────────────────────────────────
private fun MediaCategory.title(): String = when (this) {
    MediaCategory.IMAGES    -> "Images"
    MediaCategory.VIDEOS    -> "Videos"
    MediaCategory.DOCUMENTS -> "Documents & downloads"
    MediaCategory.AUDIO     -> "Audio"
}

private fun MediaCategory.icon(): ImageVector = when (this) {
    MediaCategory.IMAGES    -> Icons.Rounded.Image
    MediaCategory.VIDEOS    -> Icons.Rounded.VideoFile
    MediaCategory.DOCUMENTS -> Icons.Rounded.Description
    MediaCategory.AUDIO     -> Icons.Rounded.AudioFile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCategoryScreen(
    category  : MediaCategory,
    onBack    : () -> Unit,
    viewModel : StorageViewModel = viewModel(),
) {
    val items by viewModel.mediaFor(category).collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingDelete by remember { mutableStateOf<MediaItem?>(null) }
    var confirmClearAll by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.title(), fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = { confirmClearAll = true }) { Text("Clear all") }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Rounded.FolderOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text("No ${category.title().lowercase()} yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding  = PaddingValues(vertical = 8.dp),
            ) {
                items(items, key = { it.messageId }) { item ->
                    MediaItemRow(
                        item      = item,
                        icon      = category.icon(),
                        onDelete  = { pendingDelete = item },
                    )
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title    = { Text("Delete file?") },
            text     = { Text("\"${item.fileName}\" will be permanently removed from this device.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteMedia(item); pendingDelete = null },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title    = { Text("Clear ${category.title().lowercase()}?") },
            text     = { Text("This permanently deletes all ${items.size} file${if (items.size == 1) "" else "s"} in this category.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmClearAll = false; viewModel.clearCategory(category) },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Clear all") }
            },
            dismissButton = { TextButton(onClick = { confirmClearAll = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MediaItemRow(item: MediaItem, icon: ImageVector, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDelete)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(item.fileName, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Text(
                "${formatBytes(item.sizeBytes)} · ${formatDate(item.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

private fun formatDate(timestampMs: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestampMs))
