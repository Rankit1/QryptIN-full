package com.qryptin.chat.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.qryptin.chat.model.Attachment
import com.qryptin.chat.model.MessageType
import java.io.File
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  MediaStorage
//
//  Root cause of "Cannot send images/documents/media/files":
//  AttachmentBottomSheet reported a MessageType back to
//  ChatConversationScreen, but `onAttachmentTypePicked` was an
//  empty lambda — no Android picker was ever launched, so nothing
//  was actually attached. See ChatConversationScreen for the
//  picker wiring; this object handles the other missing half —
//  persisting the picked file so it survives app restarts.
//
//  A raw content:// Uri from a picker is only readable for as
//  long as the grant lasts (often just the current process). To
//  satisfy "persist locally" / "reopen media later", we copy the
//  bytes into this app's private files dir once, up front, and
//  store messages against that durable local copy instead.
// ─────────────────────────────────────────────────────────────
object MediaStorage {

    private const val MEDIA_DIR = "chat_media"

    /**
     * Copies [sourceUri] into app-private storage and returns an [Attachment]
     * describing the durable local copy. Returns null if the source can't be
     * read (e.g. the picker grant already expired) — callers should surface
     * a "couldn't attach that file" message rather than crash.
     */
    fun persist(context: Context, sourceUri: Uri, type: MessageType): Attachment? {
        return runCatching {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(sourceUri) ?: defaultMimeFor(type)
            val originalName = queryDisplayName(context, sourceUri) ?: defaultNameFor(type)

            val mediaDir = File(context.filesDir, MEDIA_DIR).apply { mkdirs() }
            val safeName = "${UUID.randomUUID()}_$originalName"
            val destFile = File(mediaDir, safeName)

            resolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            Attachment(
                localUri  = Uri.fromFile(destFile).toString(),
                fileName  = originalName,
                mimeType  = mimeType,
                sizeBytes = destFile.length(),
            )
        }.getOrNull()
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        }
    }.getOrNull()

    private fun defaultMimeFor(type: MessageType): String = when (type) {
        MessageType.IMAGE    -> "image/*"
        MessageType.VIDEO    -> "video/*"
        MessageType.AUDIO    -> "audio/*"
        MessageType.DOCUMENT -> "application/octet-stream"
        else                 -> "application/octet-stream"
    }

    private fun defaultNameFor(type: MessageType): String = when (type) {
        MessageType.IMAGE    -> "image_${System.currentTimeMillis()}.jpg"
        MessageType.VIDEO    -> "video_${System.currentTimeMillis()}.mp4"
        MessageType.AUDIO    -> "audio_${System.currentTimeMillis()}.m4a"
        MessageType.DOCUMENT -> "document_${System.currentTimeMillis()}"
        else                 -> "file_${System.currentTimeMillis()}"
    }
}
