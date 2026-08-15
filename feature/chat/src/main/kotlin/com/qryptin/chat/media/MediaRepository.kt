package com.qryptin.chat.media

import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  MediaRepository
//  Repository-pattern boundary for Issue 7 (Manage Storage), so
//  feature:settings depends on this interface rather than reaching
//  into feature:chat's Room database directly.
// ─────────────────────────────────────────────────────────────
interface MediaRepository {

    /** All locally-stored media, newest first. Reactive — updates as messages arrive/are deleted. */
    fun observeMedia(category: MediaCategory? = null): Flow<List<MediaItem>>

    /** Per-category byte totals + counts, plus the OS cache directory size. */
    fun observeStorageBreakdown(): Flow<StorageBreakdown>

    /** Deletes one media item's message row and its on-disk file. Returns true on success. */
    suspend fun deleteMedia(item: MediaItem): Boolean

    /** Deletes every item in [category]. Returns the number of items removed. */
    suspend fun clearCategory(category: MediaCategory): Int

    /** Deletes every locally-stored media item across all categories. Returns the number removed. */
    suspend fun clearAllMedia(): Int

    /** Wipes the app's OS-managed cache directory (thumbnails, temp files). Returns bytes freed. */
    suspend fun clearCache(): Long
}
