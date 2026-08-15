package com.qryptin.chat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────
//  ConversationEntity
//  Persists every chat thread (direct or group) in Room.
//  The lastMessage* columns are a denormalised preview so the
//  home list doesn't need a JOIN on every recomposition.
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id               : String,

    /** "DIRECT" or "GROUP" */
    val type             : String,

    /** Contact name (DIRECT) or group name (GROUP) */
    val title            : String,

    val avatarUrl        : String? = null,

    /** JSON array of participant/member IDs, e.g. ["user_abc","user_xyz"] */
    val participantIdsJson: String = "[]",

    val isPinned         : Boolean = false,
    val isMuted          : Boolean = false,
    val isArchived       : Boolean = false,

    val unreadCount      : Int     = 0,

    // ── Denormalised last-message preview ─────────────────────
    val lastMessageText      : String? = null,
    val lastMessageTimestamp : Long    = 0L,
    val lastMessageSenderId  : String? = null,
    val lastMessageType      : String  = "TEXT",   // matches MessageType enum name

    val createdAt        : Long = System.currentTimeMillis(),
)
