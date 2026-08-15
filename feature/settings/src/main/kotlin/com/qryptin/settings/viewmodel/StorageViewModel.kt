package com.qryptin.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.media.MediaCategory
import com.qryptin.chat.media.MediaItem
import com.qryptin.chat.media.MediaRepository
import com.qryptin.chat.media.RoomMediaRepository
import com.qryptin.chat.media.StorageBreakdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  StorageViewModel
//  Issue 7 — Manage Storage. Drives StorageManageScreen (overview
//  + per-category breakdown) and StorageCategoryScreen (browse /
//  delete individual media within one category).
//
//  `cacheTick` exists because clearing the OS cache directory
//  doesn't change anything in Room, so nothing would otherwise
//  make observeStorageBreakdown() recompute cacheBytes after a
//  "Clear cache" tap — bumping the tick forces a recombination.
// ─────────────────────────────────────────────────────────────
class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MediaRepository = RoomMediaRepository(application)

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val cacheTick = MutableStateFlow(0)

    val breakdown: StateFlow<StorageBreakdown> = combine(
        repository.observeStorageBreakdown(),
        cacheTick,
    ) { summary, _ -> summary }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StorageBreakdown())

    fun mediaFor(category: MediaCategory): StateFlow<List<MediaItem>> =
        repository.observeMedia(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteMedia(item: MediaItem) {
        viewModelScope.launch {
            _isBusy.value = true
            val success = repository.deleteMedia(item)
            _isBusy.value = false
            _message.value = if (success) "Deleted \"${item.fileName}\"" else "Couldn't delete that file"
        }
    }

    fun clearCategory(category: MediaCategory) {
        viewModelScope.launch {
            _isBusy.value = true
            val count = repository.clearCategory(category)
            _isBusy.value = false
            _message.value = if (count > 0) "Cleared $count item${if (count == 1) "" else "s"}" else "Nothing to clear"
        }
    }

    fun clearAllMedia() {
        viewModelScope.launch {
            _isBusy.value = true
            val count = repository.clearAllMedia()
            _isBusy.value = false
            _message.value = if (count > 0) "Cleared $count item${if (count == 1) "" else "s"}" else "Nothing to clear"
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _isBusy.value = true
            val freed = repository.clearCache()
            _isBusy.value = false
            cacheTick.value += 1
            _message.value = "Cache cleared (${formatBytes(freed)} freed)"
        }
    }

    fun consumeMessage() { _message.value = null }
}

/** Human-readable byte formatting shared by the storage screens (e.g. "128 MB", "1.4 GB"). */
fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) "${value.toInt()} ${units[unitIndex]}"
           else "%.1f %s".format(value, units[unitIndex])
}
