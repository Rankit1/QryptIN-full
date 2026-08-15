package com.qryptin.chat.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  ConversationDao
// ─────────────────────────────────────────────────────────────
@Dao
interface ConversationDao {

    // ── Reactive reads ────────────────────────────────────────

    /** All non-archived conversations, sorted by latest message then pinned. */
    @Query("""
        SELECT * FROM conversations
        WHERE isArchived = 0
        ORDER BY isPinned DESC, lastMessageTimestamp DESC, createdAt DESC
    """)
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ConversationEntity?

    /** Find an existing direct chat with a specific participant. */
    @Query("""
        SELECT * FROM conversations
        WHERE type = 'DIRECT'
          AND participantIdsJson LIKE '%' || :participantId || '%'
        LIMIT 1
    """)
    suspend fun findDirectChat(participantId: String): ConversationEntity?

    // ── Writes ────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        UPDATE conversations
        SET lastMessageText      = :text,
            lastMessageTimestamp = :timestamp,
            lastMessageSenderId  = :senderId,
            lastMessageType      = :type
        WHERE id = :conversationId
    """)
    suspend fun updateLastMessage(
        conversationId : String,
        text           : String?,
        timestamp      : Long,
        senderId       : String,
        type           : String,
    )

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun clearUnread(id: String)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :id")
    suspend fun incrementUnread(id: String)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun setPin(id: String, pinned: Boolean)

    @Query("UPDATE conversations SET isMuted = :muted WHERE id = :id")
    suspend fun setMute(id: String, muted: Boolean)

    @Query("UPDATE conversations SET isArchived = :archived WHERE id = :id")
    suspend fun setArchive(id: String, archived: Boolean)
}
