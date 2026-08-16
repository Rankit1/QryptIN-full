package com.qryptin.chat.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  ConversationDao
// ─────────────────────────────────────────────────────────────
@Dao
interface ConversationDao {

    // ── Reactive reads ────────────────────────────────────────

    /** All non-archived conversations for the current owner, sorted by latest message then pinned. */
    @Query("""
        SELECT * FROM conversations
        WHERE isArchived = 0 AND ownerId = :ownerId
        ORDER BY isPinned DESC, lastMessageTimestamp DESC, createdAt DESC
    """)
    fun observeAll(ownerId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id AND ownerId = :ownerId LIMIT 1")
    fun observeById(id: String, ownerId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id AND ownerId = :ownerId LIMIT 1")
    suspend fun getById(id: String, ownerId: String): ConversationEntity?

    /** Find an existing direct chat with a specific participant for the current owner. */
    @Query("""
        SELECT * FROM conversations
        WHERE type = 'DIRECT' AND ownerId = :ownerId
          AND participantIdsJson LIKE '%' || :participantId || '%'
        LIMIT 1
    """)
    suspend fun findDirectChat(participantId: String, ownerId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getByIdRaw(id: String): ConversationEntity?

    // ── Writes ────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id AND ownerId = :ownerId")
    suspend fun deleteById(id: String, ownerId: String)

    @Query("""
        UPDATE conversations
        SET lastMessageText      = :text,
            lastMessageTimestamp = :timestamp,
            lastMessageSenderId  = :senderId,
            lastMessageType      = :type
        WHERE id = :conversationId AND ownerId = :ownerId
    """)
    suspend fun updateLastMessage(
        conversationId : String,
        ownerId        : String,
        text           : String?,
        timestamp      : Long,
        senderId       : String,
        type           : String,
    )

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id AND ownerId = :ownerId")
    suspend fun clearUnread(id: String, ownerId: String)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :id AND ownerId = :ownerId")
    suspend fun incrementUnread(id: String, ownerId: String)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id AND ownerId = :ownerId")
    suspend fun setPin(id: String, ownerId: String, pinned: Boolean)

    @Query("UPDATE conversations SET isMuted = :muted WHERE id = :id AND ownerId = :ownerId")
    suspend fun setMute(id: String, ownerId: String, muted: Boolean)

    @Query("UPDATE conversations SET isArchived = :archived WHERE id = :id AND ownerId = :ownerId")
    suspend fun setArchive(id: String, ownerId: String, archived: Boolean)
}
