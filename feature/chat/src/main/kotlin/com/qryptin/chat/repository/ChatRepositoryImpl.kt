package com.qryptin.chat.repository

import android.util.Base64
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
import org.json.JSONObject

class ChatRepositoryImpl(
    context: Context,
    private val sessionRepository: SessionRepository,
    private val api: MessageApi = ChatRetrofitClient.messageApi,
    private val wsManager: WebSocketManager = WebSocketManager()
) : ChatRepository {

    private val localRepository = RoomChatRepository(context, sessionRepository)
    private val gson = Gson()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun observeChats(): Flow<List<Chat>> = localRepository.observeChats()

    override fun observeMessages(chatId: String): Flow<List<Message>> = localRepository.observeMessages(chatId)

    override fun observeTypingState(chatId: String): Flow<Boolean> = localRepository.observeTypingState(chatId)

    override suspend fun sendTextMessage(chatId: String, text: String, replyToMessageId: String?): Message {
        val localMessage = localRepository.sendTextMessage(chatId, text, replyToMessageId)
        
        val userId = sessionRepository.currentUserId() 
        if (userId == null) {
            android.util.Log.e("ChatRepository", "ABORT SEND: Current user ID is null. Stomp requires real UUID.")
            return localMessage
        }

        // ── Standardize for Backend BYTEA columns ────────────────
        val base64Message = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        // Real-time send via WebSocket (Strictly following provided JSON structure)
        try {
            val wsPayload = JSONObject().apply {
                put("senderId", userId)
                put("receiverId", chatId) // chatId is the receiver UUID in QryptIN
                put("message", base64Message)
                put("timestamp", System.currentTimeMillis())
                put("status", "SENT")
            }.toString()
            
            android.util.Log.d("ChatRepository", "Sending WS Payload to /app/chat.send: $wsPayload")
            wsManager.sendMessage(wsPayload)
            
            // Only use REST as fallback if WS send fails or is disconnected
            // (Removed redundant immediate REST call to avoid double insertion)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Failed to send message via WebSocket, attempting REST fallback", e)
            try {
                api.sendMessage(SendMessageRequest(
                    senderId = userId, 
                    receiverId = chatId, 
                    message = base64Message,
                    status = "SENT"
                ))
            } catch (restEx: Exception) {
                android.util.Log.e("ChatRepository", "REST fallback also failed", restEx)
            }
        }
        
        return localMessage
    }

    override suspend fun receiveTextMessage(senderId: String, text: String): Message {
        val ownerId = sessionRepository.currentUserId() ?: "me"
        // When receiving from backend/ws, we may need to decode from Base64
        val decodedText = try {
            val decodedBytes = Base64.decode(text, Base64.DEFAULT)
            String(decodedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            text // Fallback to raw text if not valid Base64
        }
        return localRepository.receiveTextMessage(senderId, decodedText, ownerId)
    }

    override suspend fun sendAttachmentMessage(chatId: String, type: MessageType, attachment: Attachment, caption: String?): Message {
        val localMessage = localRepository.sendAttachmentMessage(chatId, type, attachment, caption)
        val receiverId = chatId.removePrefix("chat_").substringBefore("_")
        val userId = sessionRepository.currentUserId() ?: "me"
        
        val displayMsg = "[Attachment: ${type.name}] ${caption ?: ""}"
        val base64Message = Base64.encodeToString(displayMsg.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        try {
            api.sendMessage(SendMessageRequest(
                senderId = userId,
                receiverId = receiverId,
                message = base64Message
            ))
        } catch (e: Exception) {}
        
        return localMessage
    }

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
        android.util.Log.d("ChatRepository", "Connecting WebSocket for user: $userId to ${com.qryptin.auth.network.NetworkConfig.WS_URL}")
        wsManager.connect()
        wsManager.subscribeToChat(userId) { jsonMessage ->
            android.util.Log.d("ChatRepository", "Received WebSocket message for $userId: $jsonMessage")
            try {
                val message = gson.fromJson(jsonMessage, MessageResponse::class.java)
                val senderId = message.realSenderId
                val content = message.realMessage
                
                android.util.Log.d("ChatRepository", "Parsed message from $senderId: $content")

                if (senderId.isNotBlank()) {
                    android.util.Log.d("ChatRepository", "Launching receiveTextMessage for $senderId with content length: ${content.length}")
                    repositoryScope.launch {
                        try {
                            receiveTextMessage(senderId, content)
                            android.util.Log.d("ChatRepository", "Successfully processed incoming message from $senderId")
                        } catch (e: Exception) {
                            android.util.Log.e("ChatRepository", "Error in receiveTextMessage for $senderId", e)
                        }
                    }
                } else {
                    android.util.Log.w("ChatRepository", "Received message with empty senderId")
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Failed to parse incoming WS message: $jsonMessage", e)
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
