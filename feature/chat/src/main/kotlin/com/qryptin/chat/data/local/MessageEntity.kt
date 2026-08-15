package com.qryptin.chat.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────
//  MessageEntity
//  One row per chat message, referencing its parent conversation.
//  deliveryState stores the MessageStatus enum name.
// ─────────────────────────────────────────────────────────────
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity        = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns  = ["conversationId"],
            onDelete      = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("conversationId"), Index("timestamp")],
)
data class MessageEntity(
    @PrimaryKey
    val messageId        : String,

    val conversationId   : String,

    val senderId         : String,
    val receiverId       : String,
    val senderName       : String,

    /** Matches MessageType enum name: TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, STICKER, SYSTEM */
    val messageType      : String = "TEXT",

    val text             : String? = null,

    /** Epoch millis */
    val timestamp        : Long,

    /** Matches MessageStatus enum name: SENDING, SENT, DELIVERED, READ, FAILED */
    val deliveryState    : String = "SENT",

    val isOutgoing       : Boolean,
    val isDeleted        : Boolean = false,

    /** messageId this message replies to, or null */
    val replyToMessageId : String? = null,

    /** Attachment local URI, or null */
    val attachmentUri    : String? = null,
    val attachmentName   : String? = null,
    val attachmentMime   : String? = null,
)
