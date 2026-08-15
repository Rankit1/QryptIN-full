package com.qryptin.chat.repository

import com.qryptin.chat.model.Attachment
import com.qryptin.chat.model.Chat
import com.qryptin.chat.model.Message
import com.qryptin.chat.model.MessageType
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  ChatRepository
//
//  Interface-only contract for everything chat-related. Today
//  RoomChatRepository is the sole implementation — Room-backed,
//  data persists locally across app restarts. Swapping in a real
//  backend (own server / WebSocket / Supabase replacement) later
//  means writing a new implementation of this interface — no
//  ViewModel or screen code changes.
// ─────────────────────────────────────────────────────────────
interface ChatRepository {

    /** Reactive stream of every chat thread, newest activity first. */
    fun observeChats(): Flow<List<Chat>>

    /** Reactive stream of messages for a single thread, oldest first. */
    fun observeMessages(chatId: String): Flow<List<Message>>

    /** True while the peer (or any group member) is typing in [chatId]. */
    fun observeTypingState(chatId: String): Flow<Boolean>

    suspend fun sendTextMessage(chatId: String, text: String, replyToMessageId: String? = null): Message

    suspend fun receiveTextMessage(senderId: String, text: String): Message

    suspend fun sendAttachmentMessage(
        chatId     : String,
        type       : MessageType,
        attachment : Attachment,
        caption    : String?       = null,
    ): Message

    suspend fun markChatAsRead(chatId: String)

    suspend fun setLocalTyping(chatId: String, isTyping: Boolean)

    suspend fun deleteMessageForMe(chatId: String, messageId: String)

    suspend fun deleteMessageForEveryone(chatId: String, messageId: String)

    suspend fun addReaction(chatId: String, messageId: String, emoji: String, senderId: String)

    suspend fun togglePin(chatId: String)

    suspend fun toggleMute(chatId: String)

    suspend fun toggleArchive(chatId: String)

    suspend fun deleteChatLocally(chatId: String)

    /** Returns the existing direct chat with [contactId], or creates an empty thread for it. */
    suspend fun getOrCreateDirectChat(contactId: String, contactName: String, avatarUrl: String?): Chat

    suspend fun createGroupChat(name: String, participantIds: List<String>, avatarUri: String?): Chat
}
