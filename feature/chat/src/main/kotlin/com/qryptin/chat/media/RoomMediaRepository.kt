package com.qryptin.chat.media

import android.content.Context
import android.net.Uri
import com.qryptin.chat.data.local.ChatDatabase
import com.qryptin.chat.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

// ─────────────────────────────────────────────────────────────
//  RoomMediaRepository
//  Issue 7 — Manage Storage. Reads every locally-persisted
//  attachment straight from the same Room database (ChatDatabase)
//  that stores messages — there's no separate "media table"; a
//  media item IS a message with an attachmentUri, grouped by its
//  messageType. File size is read live from disk (not cached in
//  Room) so it always reflects what's actually on the filesystem.
// ─────────────────────────────────────────────────────────────
class RoomMediaRepository(context: Context) : MediaRepository {

    private val appContext = context.applicationContext
    private val messageDao = ChatDatabase.getInstance(appContext).messageDao()

    override fun observeMedia(category: MediaCategory?): Flow<List<MediaItem>> =
        messageDao.observeAllMediaMessages().map { messages ->
            messages.mapNotNull { it.toMediaItem() }
                .filter { category == null || it.category == category }
        }

    override fun observeStorageBreakdown(): Flow<StorageBreakdown> =
        messageDao.observeAllMediaMessages().map { messages ->
            val items = messages.mapNotNull { it.toMediaItem() }
            StorageBreakdown(
                imagesBytes    = items.filter { it.category == MediaCategory.IMAGES }.sumOf { it.sizeBytes },
                videosBytes    = items.filter { it.category == MediaCategory.VIDEOS }.sumOf { it.sizeBytes },
                documentsBytes = items.filter { it.category == MediaCategory.DOCUMENTS }.sumOf { it.sizeBytes },
                audioBytes     = items.filter { it.category == MediaCategory.AUDIO }.sumOf { it.sizeBytes },
                cacheBytes     = currentCacheBytes(),
                imagesCount    = items.count { it.category == MediaCategory.IMAGES },
                videosCount    = items.count { it.category == MediaCategory.VIDEOS },
                documentsCount = items.count { it.category == MediaCategory.DOCUMENTS },
                audioCount     = items.count { it.category == MediaCategory.AUDIO },
            )
        }

    override suspend fun deleteMedia(item: MediaItem): Boolean = runCatching {
        deleteLocalFile(item.localUri)
        messageDao.hardDelete(item.messageId)
        true
    }.getOrDefault(false)

    override suspend fun clearCategory(category: MediaCategory): Int {
        val items = messageDao.observeAllMediaMessages().first()
            .mapNotNull { it.toMediaItem() }
            .filter { it.category == category }
        items.forEach { deleteMedia(it) }
        return items.size
    }

    override suspend fun clearAllMedia(): Int {
        val items = messageDao.observeAllMediaMessages().first().mapNotNull { it.toMediaItem() }
        items.forEach { deleteMedia(it) }
        return items.size
    }

    override suspend fun clearCache(): Long {
        val freed = currentCacheBytes()
        deleteDirContents(appContext.cacheDir)
        return freed
    }

    // ── Disk helpers ──────────────────────────────────────────

    private fun currentCacheBytes(): Long = dirSize(appContext.cacheDir)

    private fun dirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        return runCatching { dir.walkTopDown().filter { it.isFile }.sumOf { it.length() } }.getOrDefault(0L)
    }

    private fun deleteDirContents(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { it.deleteRecursively() }
    }

    private fun deleteLocalFile(uriString: String) {
        runCatching {
            val path = Uri.parse(uriString).path ?: return
            File(path).takeIf { it.exists() }?.delete()
        }
    }

    private fun MessageEntity.toMediaItem(): MediaItem? {
        val uri = attachmentUri ?: return null
        val category = when (messageType) {
            "IMAGE"    -> MediaCategory.IMAGES
            "VIDEO"    -> MediaCategory.VIDEOS
            "DOCUMENT" -> MediaCategory.DOCUMENTS
            "AUDIO"    -> MediaCategory.AUDIO
            else       -> return null
        }
        val file = runCatching { Uri.parse(uri).path?.let { File(it) } }.getOrNull()
        val size = file?.takeIf { it.exists() }?.length() ?: 0L
        return MediaItem(
            messageId      = messageId,
            conversationId = conversationId,
            category       = category,
            fileName       = attachmentName ?: file?.name ?: "file",
            localUri       = uri,
            mimeType       = attachmentMime ?: "application/octet-stream",
            sizeBytes      = size,
            timestamp      = timestamp,
        )
    }
}
