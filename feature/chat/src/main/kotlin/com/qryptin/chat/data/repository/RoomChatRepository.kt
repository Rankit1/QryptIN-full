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
import kotlinx.coroutines.flow.map
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  RoomChatRepository
//  Room-backed implementation of ChatRepository.
//  All data persists locally across app restarts.
//  No mock data, no hardcoded messages.
// ─────────────────────────────────────────────────────────────
class RoomChatRepository(context: Context) : ChatRepository {

    private val db              = ChatDatabase.getInstance(context)
    private val conversationDao = db.conversationDao()
    private val messageDao      = db.messageDao()

    // Per-conversation in-memory typing state — not persisted (intentionally ephemeral)
    private val typingStates = mutableMapOf<String, MutableStateFlow<Boolean>>()

    private fun typingFlow(chatId: String) =
        typingStates.getOrPut(chatId) { MutableStateFlow(false) }

    // ── Reactive reads ────────────────────────────────────────

    override fun observeChats(): Flow<List<Chat>> =
        conversationDao.observeAll().map { entities ->
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
        val entity = MessageEntity(
            messageId        = "msg_${UUID.randomUUID()}",
            conversationId   = chatId,
            senderId         = "me",
            receiverId       = chatId,
            senderName       = "You",
            messageType      = MessageType.TEXT.name,
            text             = text,
            timestamp        = System.currentTimeMillis(),
            deliveryState    = MessageStatus.SENT.name,
            isOutgoing       = true,
            replyToMessageId = replyToMessageId,
        )
        messageDao.insert(entity)
        conversationDao.updateLastMessage(
            conversationId = chatId,
            text           = text,
            timestamp      = entity.timestamp,
            senderId       = "me",
            type           = MessageType.TEXT.name,
        )
        return entity.toMessage()
    }

    override suspend fun receiveTextMessage(senderId: String, text: String): Message {
        val chatId = senderId // Simplified: chatId is the senderId for direct chats
        val entity = MessageEntity(
            messageId        = "msg_${UUID.randomUUID()}",
            conversationId   = chatId,
            senderId         = senderId,
            receiverId       = "me",
            senderName       = senderId, // Should look up contact name
            messageType      = MessageType.TEXT.name,
            text             = text,
            timestamp        = System.currentTimeMillis(),
            deliveryState    = MessageStatus.SENT.name,
            isOutgoing       = false,
        )
        
        // Ensure conversation exists
        val existing = conversationDao.getById(chatId)
        if (existing == null) {
            conversationDao.insert(
                ConversationEntity(
                    id = chatId,
                    type = ChatType.DIRECT.name,
                    title = senderId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        messageDao.insert(entity)
        conversationDao.updateLastMessage(
            conversationId = chatId,
            text           = text,
            timestamp      = entity.timestamp,
            senderId       = senderId,
            type           = MessageType.TEXT.name,
        )
        return entity.toMessage()
    }

    override suspend fun sendAttachmentMessage(
        chatId     : String,
        type       : MessageType,
        attachment : Attachment,
        caption    : String?,
    ): Message {
        val entity = MessageEntity(
            messageId      = "msg_${UUID.randomUUID()}",
            conversationId = chatId,
            senderId       = "me",
            receiverId     = chatId,
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
            text           = caption ?: attachment.fileName,
            timestamp      = entity.timestamp,
            senderId       = "me",
            type           = type.name,
        )
        return entity.toMessage()
    }

    // ── Chat-level mutations ──────────────────────────────────

    override suspend fun markChatAsRead(chatId: String) {
        conversationDao.clearUnread(chatId)
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
        val conv = conversationDao.getById(chatId) ?: return
        conversationDao.setPin(chatId, !conv.isPinned)
    }

    override suspend fun toggleMute(chatId: String) {
        val conv = conversationDao.getById(chatId) ?: return
        conversationDao.setMute(chatId, !conv.isMuted)
    }

    override suspend fun toggleArchive(chatId: String) {
        val conv = conversationDao.getById(chatId) ?: return
        conversationDao.setArchive(chatId, !conv.isArchived)
    }

    override suspend fun deleteChatLocally(chatId: String) {
        messageDao.deleteAllForConversation(chatId)
        conversationDao.deleteById(chatId)
    }

    // ── Creation ──────────────────────────────────────────────

    override suspend fun getOrCreateDirectChat(
        contactId   : String,
        contactName : String,
        avatarUrl   : String?,
    ): Chat {
        val existing = conversationDao.findDirectChat(contactId)
        if (existing != null) return existing.toChat()

        val entity = ConversationEntity(
            id                  = "chat_${contactId}_${System.currentTimeMillis()}",
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
        val idsJson = participantIds.joinToString(",", "[", "]") { "\"$it\"" }
        val entity = ConversationEntity(
            id                  = "chat_group_${UUID.randomUUID()}",
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
