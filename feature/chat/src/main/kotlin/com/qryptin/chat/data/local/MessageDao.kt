package com.qryptin.chat.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  MessageDao
// ─────────────────────────────────────────────────────────────
@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    // Issue 7 — Manage Storage: every non-deleted message that carries a
    // local attachment, across all conversations, is the raw data set the
    // storage screen groups into Images / Videos / Documents / Audio.
    @Query("SELECT * FROM messages WHERE attachmentUri IS NOT NULL AND isDeleted = 0 ORDER BY timestamp DESC")
    fun observeAllMediaMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("UPDATE messages SET deliveryState = :state WHERE messageId = :messageId")
    suspend fun updateDeliveryState(messageId: String, state: String)

    @Query("UPDATE messages SET isDeleted = 1, text = NULL, attachmentUri = NULL WHERE messageId = :messageId")
    suspend fun softDelete(messageId: String)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun hardDelete(messageId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteAllForConversation(conversationId: String)

    // Get all messages received by a specific user (for backend sync)
    @Query("SELECT * FROM messages WHERE receiverId = :receiverId ORDER BY timestamp ASC")
    fun observeMessagesForReceiver(receiverId: String): Flow<List<MessageEntity>>
}
