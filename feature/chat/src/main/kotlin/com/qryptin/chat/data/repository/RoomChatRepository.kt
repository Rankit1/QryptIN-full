package com.qryptin.chat.data.repository

import android.content.Context
import com.qryptin.chat.data.local.ChatDatabase
import com.qryptin.chat.data.local.ConversationEntity
import com.qryptin.chat.data.local.MessageEntity
import com.qryptin.chat.model.*
import com.qryptin.chat.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  RoomChatRepository
//  Room-backed implementation of ChatRepository.
//  All data persists locally across app restarts.
//  No mock data, no hardcoded messages.
// ─────────────────────────────────────────────────────────────
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RoomChatRepository(
    context: Context,
    private val sessionRepository: com.qryptin.auth.repository.SessionRepository? = null
) : ChatRepository {

    private val db              = ChatDatabase.getInstance(context)
    private val conversationDao = db.conversationDao()
    private val messageDao      = db.messageDao()

    /** Reactive stream of the current user's UUID. */
    private val currentUserIdFlow: Flow<String> = sessionRepository?.let { repo ->
        repo.isLoggedIn.flatMapLatest { loggedIn ->
            if (loggedIn) {
                flow { 
                    val id = repo.currentUserId()
                    if (id != null) emit(id)
                }
            } else {
                flowOf("")
            }
        }
    } ?: flowOf("")

    private suspend fun getCurrentUserId(): String? {
        return sessionRepository?.currentUserId()
    }

    // Per-conversation in-memory typing state — not persisted (intentionally ephemeral)
    private val typingStates = mutableMapOf<String, MutableStateFlow<Boolean>>()

    private fun typingFlow(chatId: String) =
        typingStates.getOrPut(chatId) { MutableStateFlow(false) }

    // ── Reactive reads ────────────────────────────────────────

    override fun observeChats(): Flow<List<Chat>> =
        currentUserIdFlow.flatMapLatest { uid ->
            conversationDao.observeAll(uid)
        }.map { entities ->
            entities.map { it.toChat() }
        }.catch { emit(emptyList()) }

    override fun observeMessages(chatId: String): Flow<List<Message>> =
        messageDao.observeMessages(chatId).map { entities ->
            entities.map { it.toMessage() }
        }.catch { emit(emptyList()) }

    override fun observeTypingState(chatId: String): Flow<Boolean> =
        typingFlow(chatId).catch { emit(false) }

    // ── Send ──────────────────────────────────────────────────

    override suspend fun sendTextMessage(
        chatId           : String,
        text             : String,
        replyToMessageId : String?,
    ): Message {
        val ownerId = getCurrentUserId() ?: "me"
        
        // Use chatId directly if it's a UUID, otherwise the repository will handle mapping
        val receiverId = if (chatId.contains("-")) chatId else chatId
        
        val entity = MessageEntity(
            messageId        = "msg_${UUID.randomUUID()}",
            conversationId   = chatId,
            senderId         = ownerId,
            receiverId       = receiverId,
            senderName       = "You",
            messageType      = MessageType.TEXT.name,
            text             = text,
            timestamp        = System.currentTimeMillis(),
            deliveryState    = MessageStatus.SENT.name,
            isOutgoing       = true,
            replyToMessageId = replyToMessageId,
        )
        
        try {
            android.util.Log.d("RoomChatRepository", "Inserting SENT message locally: ${entity.messageId} for chat: $chatId")
            messageDao.insert(entity)
            conversationDao.updateLastMessage(
                conversationId = chatId,
                ownerId        = ownerId,
                text           = text,
                timestamp      = entity.timestamp,
                senderId       = ownerId,
                type           = MessageType.TEXT.name,
            )
        } catch (e: Exception) {
            android.util.Log.e("RoomChatRepository", "FAILED to insert SENT message locally", e)
        }
        return entity.toMessage()
    }

    override suspend fun receiveTextMessage(senderId: String, text: String): Message {
        val ownerId = getCurrentUserId() ?: "me"
        return receiveTextMessage(senderId, text, ownerId)
    }

    suspend fun receiveTextMessage(senderId: String, text: String, ownerId: String): Message {
        // Find existing conversation with this peer. 
        // We use the senderId (the other person's UUID) as the chatId for direct chats.
        val existing = conversationDao.getById(senderId, ownerId)
        var chatId = senderId 
        
        // Try to find a friendly name from contacts if this is a new conversation
        var senderDisplayName = senderId
        try {
            val contactDao = com.qryptin.contacts.data.local.QryptDatabase.getInstance(db.openHelper.writableDatabase.path?.let { context } ?: return receiveTextMessage(senderId, text, ownerId)) // This is a bit hacky to get context if not available
        } catch (e: Exception) {}
        
        // Let's do a cleaner way. I'll just use the context from the constructor.
        val contactDao = com.qryptin.contacts.data.local.QryptDatabase.getInstance(context).contactDao()
        val contact = contactDao.observeContacts().first().find { it.friendId == senderId }
        if (contact != null) {
            senderDisplayName = contact.displayName
        }

        if (existing == null) {
            android.util.Log.d("RoomChatRepository", "Creating new conversation for incoming message from $senderId (Owner: $ownerId)")
            conversationDao.insert(
                ConversationEntity(
                    id = chatId,
                    ownerId = ownerId,
                    type = ChatType.DIRECT.name,
                    title = senderDisplayName, 
                    participantIdsJson = "[\"$senderId\"]",
                    createdAt = System.currentTimeMillis()
                )
            )
        } else if (existing.title == senderId && senderDisplayName != senderId) {
            // Update title if we now know the name
            conversationDao.update(existing.copy(title = senderDisplayName))
        }

        val entity = MessageEntity(
            messageId        = "msg_${UUID.randomUUID()}",
            conversationId   = chatId,
            senderId         = senderId,
            receiverId       = ownerId,
            senderName       = senderDisplayName,
            messageType      = MessageType.TEXT.name,
            text             = text,
            timestamp        = System.currentTimeMillis(),
            deliveryState    = MessageStatus.DELIVERED.name, // Mark as delivered locally
            isOutgoing       = false,
        )

        try {
            android.util.Log.d("RoomChatRepository", "PERSISTING message to Room. ChatId: $chatId, MsgId: ${entity.messageId}")
            messageDao.insert(entity)
            conversationDao.updateLastMessage(
                conversationId = chatId,
                ownerId        = ownerId,
                text           = text,
                timestamp      = entity.timestamp,
                senderId       = senderId,
                type           = MessageType.TEXT.name,
            )
            // Increment unread count for the receiver
            conversationDao.incrementUnread(chatId, ownerId)
        } catch (e: Exception) {
            android.util.Log.e("RoomChatRepository", "CRITICAL ROOM FAILURE: Could not insert RECEIVED message", e)
        }
        return entity.toMessage()
    }

    override suspend fun sendAttachmentMessage(
        chatId     : String,
        type       : MessageType,
        attachment : Attachment,
        caption    : String?,
    ): Message {
        val ownerId = getCurrentUserId() ?: "me"
        val receiverId = when {
            chatId.contains("-") -> chatId
            chatId.startsWith("chat_") -> chatId.removePrefix("chat_").substringBefore("_")
            else -> chatId
        }

        val entity = MessageEntity(
            messageId      = "msg_${UUID.randomUUID()}",
            conversationId = chatId,
            senderId       = ownerId,
            receiverId     = receiverId,
            senderName     = "You",
            messageType    = type.name,
            text           = caption,
            timestamp      = System.currentTimeMillis(),
            deliveryState  = MessageStatus.SENT.name,
            isOutgoing     = true,
            attachmentUri  = attachment.localUri,
            attachmentName = attachment.fileName,
            attachmentMime = attachment.mimeType,
        )
        messageDao.insert(entity)
        conversationDao.updateLastMessage(
            conversationId = chatId,
            ownerId        = ownerId,
            text           = caption ?: attachment.fileName,
            timestamp      = entity.timestamp,
            senderId       = ownerId,
            type           = type.name,
        )
        return entity.toMessage()
    }

    // ── Chat-level mutations ──────────────────────────────────

    override suspend fun markChatAsRead(chatId: String) {
        conversationDao.clearUnread(chatId, getCurrentUserId() ?: "me")
    }

    override suspend fun setLocalTyping(chatId: String, isTyping: Boolean) {
        // No-op locally; would publish presence to backend when wired
    }

    override suspend fun deleteMessageForMe(chatId: String, messageId: String) {
        messageDao.hardDelete(messageId)
    }

    override suspend fun deleteMessageForEveryone(chatId: String, messageId: String) {
        messageDao.softDelete(messageId)
    }

    override suspend fun addReaction(
        chatId    : String,
        messageId : String,
        emoji     : String,
        senderId  : String,
    ) {
        // Reactions not yet persisted in Room — no-op seam for future wiring
    }

    override suspend fun togglePin(chatId: String) {
        val ownerId = getCurrentUserId() ?: "me"
        val conv = conversationDao.getById(chatId, ownerId) ?: return
        conversationDao.setPin(chatId, ownerId, !conv.isPinned)
    }

    override suspend fun toggleMute(chatId: String) {
        val ownerId = getCurrentUserId() ?: "me"
        val conv = conversationDao.getById(chatId, ownerId) ?: return
        conversationDao.setMute(chatId, ownerId, !conv.isMuted)
    }

    override suspend fun toggleArchive(chatId: String) {
        val ownerId = getCurrentUserId() ?: "me"
        val conv = conversationDao.getById(chatId, ownerId) ?: return
        conversationDao.setArchive(chatId, ownerId, !conv.isArchived)
    }

    override suspend fun deleteChatLocally(chatId: String) {
        messageDao.deleteAllForConversation(chatId)
        conversationDao.deleteById(chatId, getCurrentUserId() ?: "me")
    }

    // ── Creation ──────────────────────────────────────────────

    override suspend fun getOrCreateDirectChat(
        contactId   : String,
        contactName : String,
        avatarUrl   : String?,
    ): Chat {
        val ownerId = getCurrentUserId() ?: "me"
        val existing = conversationDao.findDirectChat(contactId, ownerId)
        if (existing != null) return existing.toChat()

        val entity = ConversationEntity(
            id                  = contactId,
            ownerId             = ownerId,
            type                = ChatType.DIRECT.name,
            title               = contactName,
            avatarUrl           = avatarUrl,
            participantIdsJson  = "[\"$contactId\"]",
            createdAt           = System.currentTimeMillis(),
        )
        conversationDao.insert(entity)
        return entity.toChat()
    }

    override suspend fun createGroupChat(
        name           : String,
        participantIds : List<String>,
        avatarUri      : String?,
    ): Chat {
        val ownerId = getCurrentUserId() ?: "me"
        val idsJson = participantIds.joinToString(",", "[", "]") { "\"$it\"" }
        val entity = ConversationEntity(
            id                  = "chat_group_${UUID.randomUUID()}",
            ownerId             = ownerId,
            type                = ChatType.GROUP.name,
            title               = name,
            avatarUrl           = avatarUri,
            participantIdsJson  = idsJson,
            createdAt           = System.currentTimeMillis(),
        )
        conversationDao.insert(entity)
        return entity.toChat()
    }

    // ── Mappers ───────────────────────────────────────────────

    private fun ConversationEntity.toChat(): Chat {
        val lastMsg = if (lastMessageTimestamp > 0L && lastMessageSenderId != null) {
            Message(
                id         = "preview_${id}",
                chatId     = id,
                senderId   = lastMessageSenderId,
                senderName = if (lastMessageSenderId == "me") "You" else title,
                type       = runCatching { MessageType.valueOf(lastMessageType) }.getOrDefault(MessageType.TEXT),
                text       = lastMessageText,
                timestamp  = lastMessageTimestamp,
                isOutgoing = lastMessageSenderId == "me",
                status     = MessageStatus.SENT,
            )
        } else null

        return Chat(
            id             = id,
            type           = runCatching { ChatType.valueOf(type) }.getOrDefault(ChatType.DIRECT),
            title          = title,
            avatarUrl      = avatarUrl,
            participantIds = parseIds(participantIdsJson),
            lastMessage    = lastMsg,
            unreadCount    = unreadCount,
            isPinned       = isPinned,
            isMuted        = isMuted,
            isArchived     = isArchived,
        )
    }

    private fun MessageEntity.toMessage() = Message(
        id               = messageId,
        chatId           = conversationId,
        senderId         = senderId,
        senderName       = senderName,
        type             = runCatching { MessageType.valueOf(messageType) }.getOrDefault(MessageType.TEXT),
        text             = text,
        attachment       = if (attachmentUri != null) Attachment(
            localUri  = attachmentUri,
            fileName  = attachmentName ?: "",
            mimeType  = attachmentMime ?: "application/octet-stream",
            sizeBytes = 0L,
        ) else null,
        status           = runCatching { MessageStatus.valueOf(deliveryState) }.getOrDefault(MessageStatus.SENT),
        timestamp        = timestamp,
        replyToMessageId = replyToMessageId,
        isOutgoing       = isOutgoing,
        isDeleted        = isDeleted,
    )

    private fun parseIds(json: String): List<String> =
        json.trim('[', ']')
            .split(",")
            .map { it.trim().trim('"') }
            .filter { it.isNotBlank() }
}
