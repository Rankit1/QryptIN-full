package com.qryptin.chat.repository

import com.qryptin.chat.model.Attachment
import com.qryptin.chat.model.Chat
import com.qryptin.chat.model.ChatType
import com.qryptin.chat.model.Message
import com.qryptin.chat.model.MessageStatus
import com.qryptin.chat.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  FakeChatRepository
//
//  Singleton, in-memory ChatRepository implementation. Simulates
//  network/DB latency and the sent -> delivered -> read lifecycle
//  so every screen in the module has something real to react to.
//  Replace with a Room + remote-backed implementation later —
//  ViewModels only ever see the ChatRepository interface.
// ─────────────────────────────────────────────────────────────
object FakeChatRepository : ChatRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _chats = MutableStateFlow(MockChatData.seedChats())
    private val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val messageStores = mutableMapOf<String, MutableStateFlow<List<Message>>>()
    private val typingStores  = mutableMapOf<String, MutableStateFlow<Boolean>>()

    private fun messagesFlow(chatId: String): MutableStateFlow<List<Message>> =
        messageStores.getOrPut(chatId) { MutableStateFlow(MockChatData.seedMessages(chatId)) }

    private fun typingFlow(chatId: String): MutableStateFlow<Boolean> =
        typingStores.getOrPut(chatId) { MutableStateFlow(false) }

    // ── Reactive reads ────────────────────────────────────────

    override fun observeChats(): Flow<List<Chat>> =
        chats.map { list ->
            list.sortedWith(
                compareByDescending<Chat> { it.isPinned }
                    .thenByDescending { it.lastMessage?.timestamp ?: 0L }
            )
        }

    override fun observeMessages(chatId: String): Flow<List<Message>> =
        messagesFlow(chatId).asStateFlow()

    override fun observeTypingState(chatId: String): Flow<Boolean> =
        typingFlow(chatId).asStateFlow()

    // ── Sending ───────────────────────────────────────────────

    override suspend fun sendTextMessage(chatId: String, text: String, replyToMessageId: String?): Message {
        val message = Message(
            id               = "msg_${chatId}_${System.currentTimeMillis()}",
            chatId           = chatId,
            senderId         = "me",
            senderName       = "You",
            type             = MessageType.TEXT,
            text             = text,
            status           = MessageStatus.SENDING,
            timestamp        = System.currentTimeMillis(),
            replyToMessageId = replyToMessageId,
            isOutgoing       = true,
        )
        appendMessage(chatId, message)
        simulateDeliveryLifecycle(chatId, message.id)
        return message
    }

    override suspend fun sendAttachmentMessage(
        chatId     : String,
        type       : MessageType,
        attachment : Attachment,
        caption    : String?,
    ): Message {
        val message = Message(
            id          = "msg_${chatId}_${System.currentTimeMillis()}",
            chatId      = chatId,
            senderId    = "me",
            senderName  = "You",
            type        = type,
            text        = caption,
            attachment  = attachment,
            status      = MessageStatus.SENDING,
            timestamp   = System.currentTimeMillis(),
            isOutgoing  = true,
        )
        appendMessage(chatId, message)
        simulateDeliveryLifecycle(chatId, message.id)
        return message
    }

    override suspend fun receiveTextMessage(senderId: String, text: String): Message {
        val chat = getOrCreateDirectChat(senderId, senderId, null)
        val message = Message(
            id         = "msg_${chat.id}_${System.currentTimeMillis()}",
            chatId     = chat.id,
            senderId   = senderId,
            senderName = chat.title,
            type       = MessageType.TEXT,
            text       = text,
            status     = MessageStatus.SENT,
            timestamp  = System.currentTimeMillis(),
            isOutgoing = false,
        )
        appendMessage(chat.id, message)
        return message
    }

    private fun appendMessage(chatId: String, message: Message) {
        val flow = messagesFlow(chatId)
        flow.value = flow.value + message
        updateChatPreview(chatId, message)
    }

    private fun updateChatPreview(chatId: String, message: Message) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) chat.copy(lastMessage = message) else chat
        }
    }

    /** SENDING -> SENT -> DELIVERED -> READ, with a short peer-typing flicker beforehand. */
    private fun simulateDeliveryLifecycle(chatId: String, messageId: String) {
        scope.launch {
            delay(400)
            updateMessageStatus(chatId, messageId, MessageStatus.SENT)
            delay(700)
            updateMessageStatus(chatId, messageId, MessageStatus.DELIVERED)

            typingFlow(chatId).value = true
            delay(1_600)
            typingFlow(chatId).value = false

            delay(300)
            updateMessageStatus(chatId, messageId, MessageStatus.READ)
        }
    }

    private fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
        val flow = messagesFlow(chatId)
        flow.value = flow.value.map { if (it.id == messageId) it.copy(status = status) else it }
        _chats.value = _chats.value.map { chat ->
            val last = chat.lastMessage
            if (chat.id == chatId && last != null && last.id == messageId) {
                chat.copy(lastMessage = last.copy(status = status))
            } else chat
        }
    }

    // ── Chat-level mutations ──────────────────────────────────

    override suspend fun markChatAsRead(chatId: String) {
        delay(150)
        _chats.value = _chats.value.map { if (it.id == chatId) it.copy(unreadCount = 0) else it }
    }

    override suspend fun setLocalTyping(chatId: String, isTyping: Boolean) {
        // In a real backend this would publish a presence event to the peer.
        // Locally there's nothing to flip — kept as a no-op seam for that wiring.
    }

    override suspend fun deleteMessageForMe(chatId: String, messageId: String) {
        delay(150)
        val flow = messagesFlow(chatId)
        flow.value = flow.value.filterNot { it.id == messageId }
    }

    override suspend fun deleteMessageForEveryone(chatId: String, messageId: String) {
        delay(250)
        val flow = messagesFlow(chatId)
        flow.value = flow.value.map {
            if (it.id == messageId) it.copy(isDeleted = true, text = null, attachment = null) else it
        }
    }

    override suspend fun addReaction(chatId: String, messageId: String, emoji: String, senderId: String) {
        delay(100)
        val flow = messagesFlow(chatId)
        flow.value = flow.value.map { message ->
            if (message.id != messageId) return@map message
            val withoutSenderReaction = message.reactions.filterNot { it.senderId == senderId }
            message.copy(reactions = withoutSenderReaction + com.qryptin.chat.model.Reaction(emoji, senderId))
        }
    }

    override suspend fun togglePin(chatId: String) {
        delay(100)
        _chats.value = _chats.value.map { if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it }
    }

    override suspend fun toggleMute(chatId: String) {
        delay(100)
        _chats.value = _chats.value.map { if (it.id == chatId) it.copy(isMuted = !it.isMuted) else it }
    }

    override suspend fun toggleArchive(chatId: String) {
        delay(100)
        _chats.value = _chats.value.map { if (it.id == chatId) it.copy(isArchived = !it.isArchived) else it }
    }

    override suspend fun deleteChatLocally(chatId: String) {
        delay(150)
        _chats.value = _chats.value.filterNot { it.id == chatId }
        messageStores.remove(chatId)
        typingStores.remove(chatId)
    }

    // ── Creation flows ────────────────────────────────────────

    override suspend fun getOrCreateDirectChat(contactId: String, contactName: String, avatarUrl: String?): Chat {
        delay(200)
        val existing = _chats.value.firstOrNull { it.type == ChatType.DIRECT && it.participantIds.contains(contactId) }
        if (existing != null) return existing

        val chat = Chat(
            id             = "chat_$contactId",
            type           = ChatType.DIRECT,
            title          = contactName,
            avatarUrl      = avatarUrl,
            participantIds = listOf(contactId),
        )
        _chats.value = listOf(chat) + _chats.value
        return chat
    }

    override suspend fun createGroupChat(name: String, participantIds: List<String>, avatarUri: String?): Chat {
        delay(400)
        val chat = Chat(
            id             = "chat_group_${System.currentTimeMillis()}",
            type           = ChatType.GROUP,
            title          = name,
            avatarUrl      = avatarUri,
            participantIds = participantIds,
            lastMessage    = Message(
                id          = "msg_system_${System.currentTimeMillis()}",
                chatId      = "",
                senderId    = "system",
                senderName  = "System",
                type        = MessageType.SYSTEM,
                text        = "Group created",
                timestamp   = System.currentTimeMillis(),
                isOutgoing  = false,
            ),
        )
        _chats.value = listOf(chat.copy(lastMessage = chat.lastMessage?.copy(chatId = chat.id))) + _chats.value
        messagesFlow(chat.id).value = listOf(chat.lastMessage!!)
        return chat
    }
}
