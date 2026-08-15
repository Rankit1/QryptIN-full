package com.qryptin.chat.model

// ─────────────────────────────────────────────────────────────
//  Domain models — feature/chat
//
//  These are deliberately backend-agnostic: nothing here assumes
//  Firebase, Supabase, or any specific transport. ChatRepository
//  is the only seam that talks to a real implementation later.
// ─────────────────────────────────────────────────────────────

/** Whether a chat thread is one-to-one or a group. */
enum class ChatType {
    DIRECT,
    GROUP,
}

/** Content type carried by a single [Message]. */
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    STICKER,
    SYSTEM,
}

/** Delivery lifecycle of an outgoing message, drives the tick icon in [MessageBubble]. */
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
}

/**
 * A file/media attachment carried by a [MessageType.IMAGE] / [VIDEO] / [AUDIO] / [DOCUMENT]
 * message. [localUri] points at the mock/local resource for now; a future backend swaps this
 * for a CDN URL without touching the UI layer.
 */
data class Attachment(
    val localUri      : String,
    val fileName      : String,
    val mimeType      : String,
    val sizeBytes     : Long,
    val durationMs     : Long? = null,    // audio / video only
    val widthPx       : Int?  = null,     // image / video only
    val heightPx      : Int?  = null,
)

/** A single emoji-style reaction left on a message, keyed by sender. */
data class Reaction(
    val emoji     : String,
    val senderId  : String,
)

/**
 * A single chat message. [encryptedPayload] is a placeholder hook — see
 * [com.qryptin.chat.crypto.SecureMessagePayload] — so that swapping in real
 * end-to-end encryption later does not require touching this model's shape.
 */
data class Message(
    val id               : String,
    val chatId           : String,
    val senderId         : String,
    val senderName       : String,
    val type             : MessageType,
    val text             : String?       = null,
    val attachment       : Attachment?   = null,
    val status           : MessageStatus = MessageStatus.SENT,
    val timestamp        : Long,
    val replyToMessageId : String?       = null,
    val isOutgoing       : Boolean,
    val isDeleted        : Boolean       = false,
    val reactions        : List<Reaction> = emptyList(),
)

/**
 * A chat thread — either direct or group. The list/preview surface (last message,
 * unread count, mute/pin state) lives here; full message history is loaded
 * separately by [com.qryptin.chat.repository.ChatRepository.observeMessages].
 */
data class Chat(
    val id              : String,
    val type            : ChatType,
    val title           : String,            // contact name for DIRECT, group name for GROUP
    val avatarUrl       : String?    = null,
    val participantIds  : List<String> = emptyList(),
    val lastMessage     : Message?   = null,
    val unreadCount     : Int        = 0,
    val isPinned        : Boolean    = false,
    val isMuted         : Boolean    = false,
    val isArchived      : Boolean    = false,
    val isOnline        : Boolean    = false,
    val isTyping        : Boolean    = false,
    val isSecure        : Boolean    = true, // drives SecureStatusBadge
) {
    val isGroup: Boolean get() = type == ChatType.GROUP

    /** Two-letter initials shown when [avatarUrl] is null. */
    val initials: String get() =
        title.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "?" }
}

/** Direction/kind of an in-progress or historical call. */
enum class CallType {
    VOICE,
    VIDEO,
}

enum class CallDirection {
    OUTGOING,
    INCOMING,
}

/** Connection lifecycle surfaced by [com.qryptin.chat.viewmodel.CallViewModel]. */
enum class CallConnectionState {
    DIALING,
    RINGING,
    CONNECTING,
    CONNECTED,
    ENDED,
}
