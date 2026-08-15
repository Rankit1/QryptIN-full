package com.qryptin.chat.repository

import com.qryptin.chat.model.Chat
import com.qryptin.chat.model.ChatType
import com.qryptin.chat.model.Message
import com.qryptin.chat.model.MessageStatus
import com.qryptin.chat.model.MessageType

// ─────────────────────────────────────────────────────────────
//  MockChatData
//  Seed data for FakeChatRepository. Names line up with the
//  registered numbers in FakeContactsRepository so the chat
//  list, contacts list, and "Add Contact" lookup all agree on
//  who's who while the real backend isn't wired up yet.
// ─────────────────────────────────────────────────────────────
internal object MockChatData {

    private var msgCounter = 0

    private fun minutesAgo(minutes: Long) = System.currentTimeMillis() - minutes * 60_000L
    private fun hoursAgo(hours: Long)     = System.currentTimeMillis() - hours * 3_600_000L
    private fun daysAgo(days: Long)       = System.currentTimeMillis() - days * 86_400_000L

    fun seedChats(): List<Chat> = listOf(
        Chat(
            id             = "chat_rahul",
            type           = ChatType.DIRECT,
            title          = "Rahul Sharma",
            participantIds = listOf("user_rahul"),
            unreadCount    = 2,
            isOnline       = true,
            lastMessage    = textMessage("chat_rahul", "user_rahul", "Rahul Sharma", "Hey, are we still on for the call later?", minutesAgo(4), isOutgoing = false),
        ),
        Chat(
            id             = "chat_priya",
            type           = ChatType.DIRECT,
            title          = "Priya Kapoor",
            participantIds = listOf("user_priya"),
            unreadCount    = 0,
            isOnline       = false,
            isPinned       = true,
            lastMessage    = textMessage("chat_priya", "me", "You", "Sent the documents, let me know once you've reviewed.", minutesAgo(38), isOutgoing = true).copy(status = MessageStatus.READ),
        ),
        Chat(
            id             = "chat_design_team",
            type           = ChatType.GROUP,
            title          = "Design Team",
            participantIds = listOf("user_rahul", "user_priya", "user_arjun"),
            unreadCount    = 5,
            lastMessage    = textMessage("chat_design_team", "user_arjun", "Arjun Mehta", "Pushed the new mesh-gradient variants to the shared drive.", hoursAgo(1), isOutgoing = false),
        ),
        Chat(
            id             = "chat_debasmita",
            type           = ChatType.DIRECT,
            title          = "Debasmita Roy",
            participantIds = listOf("user_debasmita"),
            unreadCount    = 0,
            isMuted        = true,
            lastMessage    = textMessage("chat_debasmita", "user_debasmita", "Debasmita Roy", "Loved the peacock-feather icon, looks great", hoursAgo(5), isOutgoing = false),
        ),
        Chat(
            id             = "chat_arjun",
            type           = ChatType.DIRECT,
            title          = "Arjun Mehta",
            participantIds = listOf("user_arjun"),
            unreadCount    = 0,
            lastMessage    = textMessage("chat_arjun", "me", "You", "Sounds good!", daysAgo(1), isOutgoing = true).copy(status = MessageStatus.DELIVERED),
        ),
        Chat(
            id             = "chat_rupa",
            type           = ChatType.DIRECT,
            title          = "Rupa Devi",
            participantIds = listOf("user_rupa"),
            unreadCount    = 0,
            isArchived     = true,
            lastMessage    = textMessage("chat_rupa", "user_rupa", "Rupa Devi", "Photo", daysAgo(3), isOutgoing = false, type = MessageType.IMAGE),
        ),
    )

    fun seedMessages(chatId: String): List<Message> {
        val peerName = when (chatId) {
            "chat_rahul"       -> "Rahul Sharma"
            "chat_priya"       -> "Priya Kapoor"
            "chat_debasmita"   -> "Debasmita Roy"
            "chat_arjun"       -> "Arjun Mehta"
            "chat_rupa"        -> "Rupa Devi"
            "chat_design_team" -> "Arjun Mehta"
            else               -> "Contact"
        }
        val peerId = when (chatId) {
            "chat_rahul"       -> "user_rahul"
            "chat_priya"       -> "user_priya"
            "chat_debasmita"   -> "user_debasmita"
            "chat_arjun"       -> "user_arjun"
            "chat_rupa"        -> "user_rupa"
            "chat_design_team" -> "user_arjun"
            else               -> "user_unknown"
        }

        return listOf(
            textMessage(chatId, peerId, peerName, "Hey! Got a minute?", hoursAgo(2), isOutgoing = false)
                .copy(status = MessageStatus.READ),
            textMessage(chatId, "me", "You", "Sure, what's up?", hoursAgo(2), isOutgoing = true)
                .copy(status = MessageStatus.READ),
            textMessage(chatId, peerId, peerName, "Wanted to check on the QryptIN rollout — encryption module still on track?", hoursAgo(1), isOutgoing = false)
                .copy(status = MessageStatus.READ),
            textMessage(chatId, "me", "You", "Yep, key-exchange interfaces are stubbed, UI layer doesn't depend on real crypto yet.", minutesAgo(50), isOutgoing = true)
                .copy(status = MessageStatus.READ),
            textMessage(chatId, peerId, peerName, "Perfect, that unblocks the calling UI work too.", minutesAgo(10), isOutgoing = false)
                .copy(status = MessageStatus.DELIVERED),
        )
    }

    private fun textMessage(
        chatId      : String,
        senderId    : String,
        senderName  : String,
        text        : String,
        timestamp   : Long,
        isOutgoing  : Boolean,
        type        : MessageType = MessageType.TEXT,
    ) = Message(
        id          = "msg_${chatId}_${++msgCounter}_${timestamp}",
        chatId      = chatId,
        senderId    = senderId,
        senderName  = senderName,
        type        = type,
        text        = text,
        timestamp   = timestamp,
        isOutgoing  = isOutgoing,
        status      = if (isOutgoing) MessageStatus.SENT else MessageStatus.READ,
    )
}
