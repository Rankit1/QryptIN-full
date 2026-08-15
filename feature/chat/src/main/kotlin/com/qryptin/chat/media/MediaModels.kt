package com.qryptin.chat.media

// ─────────────────────────────────────────────────────────────
//  MediaModels
//  Issue 7 — Manage Storage. These describe every locally-stored
//  attachment (already persisted to app-private storage by
//  MediaStorage.persist) grouped by category, plus a size summary
//  used to drive the storage breakdown UI.
// ─────────────────────────────────────────────────────────────

enum class MediaCategory {
    IMAGES,
    VIDEOS,
    DOCUMENTS,
    AUDIO,
}

data class MediaItem(
    val messageId      : String,
    val conversationId : String,
    val category       : MediaCategory,
    val fileName       : String,
    val localUri       : String,
    val mimeType       : String,
    val sizeBytes      : Long,
    val timestamp      : Long,
)

data class StorageBreakdown(
    val imagesBytes    : Long = 0L,
    val videosBytes     : Long = 0L,
    val documentsBytes  : Long = 0L,
    val audioBytes      : Long = 0L,
    val cacheBytes      : Long = 0L,
    val imagesCount     : Int  = 0,
    val videosCount     : Int  = 0,
    val documentsCount  : Int  = 0,
    val audioCount      : Int  = 0,
) {
    val mediaBytes : Long get() = imagesBytes + videosBytes + documentsBytes + audioBytes
    val totalBytes : Long get() = mediaBytes + cacheBytes

    fun bytesFor(category: MediaCategory): Long = when (category) {
        MediaCategory.IMAGES    -> imagesBytes
        MediaCategory.VIDEOS    -> videosBytes
        MediaCategory.DOCUMENTS -> documentsBytes
        MediaCategory.AUDIO     -> audioBytes
    }

    fun countFor(category: MediaCategory): Int = when (category) {
        MediaCategory.IMAGES    -> imagesCount
        MediaCategory.VIDEOS    -> videosCount
        MediaCategory.DOCUMENTS -> documentsCount
        MediaCategory.AUDIO     -> audioCount
    }
}
