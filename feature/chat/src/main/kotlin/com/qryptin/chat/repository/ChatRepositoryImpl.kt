package com.qryptin.chat.repository

import com.qryptin.chat.model.Attachment
import com.qryptin.chat.model.Chat
import com.qryptin.chat.model.Message
import com.qryptin.chat.model.MessageType
import com.qryptin.chat.network.MessageApi
import com.qryptin.chat.network.ChatRetrofitClient
import com.qryptin.chat.network.WebSocketManager
import com.qryptin.chat.network.SendMessageRequest
import com.qryptin.chat.network.InboxItemResponse
import com.qryptin.chat.network.MessageResponse
import kotlinx.coroutines.flow.Flow
import android.content.Context
import com.qryptin.auth.repository.SessionRepository
import com.qryptin.chat.data.repository.RoomChatRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRepositoryImpl(
    context: Context,
    private val sessionRepository: SessionRepository,
    private val api: MessageApi = ChatRetrofitClient.messageApi,
    private val wsManager: WebSocketManager = WebSocketManager()
) : ChatRepository {

    private val localRepository = RoomChatRepository(context)
    private val gson = Gson()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun observeChats(): Flow<List<Chat>> = localRepository.observeChats()

    override fun observeMessages(chatId: String): Flow<List<Message>> = localRepository.observeMessages(chatId)

    override fun observeTypingState(chatId: String): Flow<Boolean> = localRepository.observeTypingState(chatId)

    override suspend fun sendTextMessage(chatId: String, text: String, replyToMessageId: String?): Message {
        val localMessage = localRepository.sendTextMessage(chatId, text, replyToMessageId)
        
        val userId = sessionRepository.currentUserId() ?: "me"
        
        // Real-time send via WebSocket
        try {
            wsManager.sendMessage("{\"sender_id\": \"$userId\", \"receiver_id\": \"$chatId\", \"message\": \"$text\"}")
            
            // Also persist to backend
            api.sendMessage(SendMessageRequest(senderId = userId, receiverId = chatId, message = text))
        } catch (e: Exception) {
            // Log error
        }
        
        return localMessage
    }

    override suspend fun receiveTextMessage(senderId: String, text: String): Message =
        localRepository.receiveTextMessage(senderId, text)

    override suspend fun sendAttachmentMessage(chatId: String, type: MessageType, attachment: Attachment, caption: String?): Message =
        localRepository.sendAttachmentMessage(chatId, type, attachment, caption)

    override suspend fun markChatAsRead(chatId: String) = localRepository.markChatAsRead(chatId)

    override suspend fun setLocalTyping(chatId: String, isTyping: Boolean) = localRepository.setLocalTyping(chatId, isTyping)

    override suspend fun deleteMessageForMe(chatId: String, messageId: String) = localRepository.deleteMessageForMe(chatId, messageId)

    override suspend fun deleteMessageForEveryone(chatId: String, messageId: String) = localRepository.deleteMessageForEveryone(chatId, messageId)

    override suspend fun addReaction(chatId: String, messageId: String, emoji: String, senderId: String) = localRepository.addReaction(chatId, messageId, emoji, senderId)

    override suspend fun togglePin(chatId: String) = localRepository.togglePin(chatId)

    override suspend fun toggleMute(chatId: String) = localRepository.toggleMute(chatId)

    override suspend fun toggleArchive(chatId: String) = localRepository.toggleArchive(chatId)

    override suspend fun deleteChatLocally(chatId: String) = localRepository.deleteChatLocally(chatId)

    override suspend fun getOrCreateDirectChat(contactId: String, contactName: String, avatarUrl: String?): Chat =
        localRepository.getOrCreateDirectChat(contactId, contactName, avatarUrl)

    override suspend fun createGroupChat(name: String, participantIds: List<String>, avatarUri: String?): Chat =
        localRepository.createGroupChat(name, participantIds, avatarUri)

    // Integration helpers
    fun connectWebSocket(userId: String) {
        wsManager.connect()
        wsManager.subscribeToChat(userId) { jsonMessage ->
            try {
                val message = gson.fromJson(jsonMessage, MessageResponse::class.java)
                repositoryScope.launch {
                    localRepository.receiveTextMessage(message.senderId, message.message)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun disconnectWebSocket() {
        wsManager.disconnect()
    }

    suspend fun syncInbox(userId: String) {
        try {
            val response = api.getInbox(userId)
            if (response.isSuccessful) {
                // Sync with local DB
            }
        } catch (e: Exception) {}
    }
}
