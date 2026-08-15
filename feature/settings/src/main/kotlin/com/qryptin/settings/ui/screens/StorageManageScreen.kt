package com.qryptin.settings.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.media.MediaCategory
import com.qryptin.chat.media.StorageBreakdown
import com.qryptin.settings.ui.components.SettingsCard
import com.qryptin.settings.ui.components.SettingsSectionHeader
import com.qryptin.settings.viewmodel.StorageViewModel
import com.qryptin.settings.viewmodel.formatBytes

// ─────────────────────────────────────────────────────────────
//  StorageManageScreen
//
//  Issue 7 fix: "Manage storage" previously had no destination at
//  all (its onClick was a TODO comment) — this is the full screen
//  it now opens. Shows received images/videos/documents/audio with
//  live counts and sizes read straight from Room via
//  RoomMediaRepository, plus OS cache size, with clear-cache,
//  clear-category, and clear-all actions. Tapping a category opens
//  StorageCategoryScreen for browsing and deleting individual files.
// ─────────────────────────────────────────────────────────────
private data class CategoryRow(
    val category : MediaCategory,
    val icon     : ImageVector,
    val label    : String,
)

private val categoryRows = listOf(
    CategoryRow(MediaCategory.IMAGES, Icons.Rounded.Image, "Images"),
    CategoryRow(MediaCategory.VIDEOS, Icons.Rounded.VideoFile, "Videos"),
    CategoryRow(MediaCategory.DOCUMENTS, Icons.Rounded.Description, "Documents & downloads"),
    CategoryRow(MediaCategory.AUDIO, Icons.Rounded.AudioFile, "Audio"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManageScreen(
    onBack           : () -> Unit,
    onOpenCategory   : (MediaCategory) -> Unit,
    viewModel        : StorageViewModel = viewModel(),
) {
    val breakdown by viewModel.breakdown.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var confirmClearCache by remember { mutableStateOf(false) }
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
                title          = { Text("Manage storage", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
        ) {
            item { StorageOverviewCard(breakdown) }

            item { SettingsSectionHeader("Media") }
            items(categoryRows) { row ->
                SettingsCard {
                    MediaCategoryRow(
                        row      = row,
                        count    = breakdown.countFor(row.category),
                        bytes    = breakdown.bytesFor(row.category),
                        onClick  = { onOpenCategory(row.category) },
                    )
                }
            }

            item { SettingsSectionHeader("Cache") }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(Icons.Rounded.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cached files", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                formatBytes(breakdown.cacheBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(enabled = !isBusy && breakdown.cacheBytes > 0, onClick = { confirmClearCache = true }) {
                            Text("Clear")
                        }
                    }
                }
            }

            item { SettingsSectionHeader("Danger zone") }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clear all media", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Removes every received image, video, document, and audio file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(
                            enabled = !isBusy && breakdown.mediaBytes > 0,
                            onClick = { confirmClearAll = true },
                            colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) { Text("Clear all") }
                    }
                }
            }
        }
    }

    if (confirmClearCache) {
        AlertDialog(
            onDismissRequest = { confirmClearCache = false },
            title    = { Text("Clear cache?") },
            text     = { Text("This frees up ${formatBytes(breakdown.cacheBytes)} of temporary files. Your chats and media are not affected.") },
            confirmButton = {
                TextButton(onClick = { confirmClearCache = false; viewModel.clearCache() }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { confirmClearCache = false }) { Text("Cancel") } },
        )
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title    = { Text("Clear all media?") },
            text     = { Text("This permanently deletes every received image, video, document, and audio file on this device. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmClearAll = false; viewModel.clearAllMedia() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Clear all") }
            },
            dismissButton = { TextButton(onClick = { confirmClearAll = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun StorageOverviewCard(breakdown: StorageBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Total storage used", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatBytes(breakdown.totalBytes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(16.dp))

            val segments = listOf(
                breakdown.imagesBytes to MaterialTheme.colorScheme.primary,
                breakdown.videosBytes to MaterialTheme.colorScheme.tertiary,
                breakdown.documentsBytes to MaterialTheme.colorScheme.secondary,
                breakdown.audioBytes to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                breakdown.cacheBytes to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            StorageBar(segments = segments, total = breakdown.totalBytes)
        }
    }
}

@Composable
private fun StorageBar(segments: List<Pair<Long, Color>>, total: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(6.dp)),
    ) {
        if (total <= 0L) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
            )
        } else {
            segments.forEach { (bytes, color) ->
                if (bytes > 0) {
                    Box(
                        modifier = Modifier
                            .weight((bytes.toFloat() / total).coerceAtLeast(0.001f))
                            .fillMaxHeight()
                            .background(color),
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaCategoryRow(row: CategoryRow, count: Int, bytes: Long, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(row.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(row.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (count == 0) "No files" else "$count file${if (count == 1) "" else "s"} · ${formatBytes(bytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
