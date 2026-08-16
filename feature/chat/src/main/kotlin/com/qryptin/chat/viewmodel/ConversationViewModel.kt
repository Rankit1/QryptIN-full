package com.qryptin.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.ChatModule
import com.qryptin.chat.model.Attachment
import com.qryptin.chat.model.ConversationSheet
import com.qryptin.chat.model.ConversationUiState
import com.qryptin.chat.model.Message
import com.qryptin.chat.model.MessageType
import com.qryptin.chat.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.qryptin.chat.data.local.ChatDatabase
import com.qryptin.chat.data.local.MessageEntity

// ─────────────────────────────────────────────────────────────
//  ConversationViewModel
//  Backed by ChatRepository — messages persist locally and syncs with backend.
// ─────────────────────────────────────────────────────────────
class ConversationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository = ChatModule.provideChatRepository(application)

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null

    init {
        // ViewModel initialized
    }

    fun load(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            repository.markChatAsRead(chatId)
        }

        viewModelScope.launch {
            combine(
                repository.observeChats(),
                repository.observeMessages(chatId),
                repository.observeTypingState(chatId),
            ) { chats, messages, isTyping ->
                Triple(chats.firstOrNull { it.id == chatId }, messages, isTyping)
            }.collect { (chat, messages, isTyping) ->
                _uiState.update {
                    it.copy(
                        chat         = chat,
                        messages     = messages,
                        isPeerTyping = isTyping,
                        isLoading    = false,
                    )
                }
            }
        }
    }

    // ── Composing ──────────────────────────────────────────────

    fun onDraftTextChanged(text: String) = _uiState.update { it.copy(draftText = text) }

    fun sendDraft() {
        val chatId = currentChatId ?: return
        val text   = uiState.value.draftText.trim()
        if (text.isEmpty()) return

        val replyId = uiState.value.replyTarget?.id
        _uiState.update { it.copy(draftText = "", replyTarget = null) }

        viewModelScope.launch {
            repository.sendTextMessage(chatId, text, replyId)
        }
    }

    fun sendAttachment(type: MessageType, attachment: Attachment, caption: String?) {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            repository.sendAttachmentMessage(chatId, type, attachment, caption)
        }
        dismissSheet()
    }

    // ── Real media picker flow ──────────────────────────────────
    //  AttachmentBottomSheet → Android picker → copy locally → stage here →
    //  MediaPreviewScreen (caption + confirm) → sendAttachment.

    /** Called once a picker intent returns a Uri and [MediaStorage] has copied it locally. */
    fun onMediaPicked(type: MessageType, attachment: Attachment?) {
        _uiState.update { it.copy(isAttaching = false) }
        if (attachment == null) {
            _uiState.update { it.copy(errorMessage = "Couldn't attach that file. Please try again.") }
            return
        }
        _uiState.update {
            it.copy(pendingAttachment = attachment, pendingType = type, activeSheet = ConversationSheet.NONE)
        }
    }

    /** Marks that a picker was launched, so the UI can show a brief loading state while the copy runs. */
    fun onPickerLaunched() {
        _uiState.update { it.copy(activeSheet = ConversationSheet.NONE, isAttaching = true) }
    }

    fun confirmPendingAttachment(caption: String) {
        val type = uiState.value.pendingType ?: return
        val attachment = uiState.value.pendingAttachment ?: return
        _uiState.update { it.copy(pendingAttachment = null, pendingType = null) }
        sendAttachment(type, attachment, caption.ifBlank { null })
    }

    fun cancelPendingAttachment() {
        _uiState.update { it.copy(pendingAttachment = null, pendingType = null) }
    }

    fun dismissErrorMessage() = _uiState.update { it.copy(errorMessage = null) }

    // ── Sheets ────────────────────────────────────────────────

    fun openSheet(sheet: ConversationSheet)  = _uiState.update { it.copy(activeSheet = sheet) }
    fun dismissSheet()                        = _uiState.update { it.copy(activeSheet = ConversationSheet.NONE) }

    fun onEmojiPicked(emoji: String) {
        _uiState.update { it.copy(draftText = it.draftText + emoji) }
    }

    fun onStickerPicked(stickerId: String) {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            repository.sendAttachmentMessage(
                chatId     = chatId,
                type       = MessageType.STICKER,
                attachment = Attachment(
                    localUri  = stickerId,
                    fileName  = stickerId,
                    mimeType  = "image/webp",
                    sizeBytes = 0L,
                ),
            )
        }
        dismissSheet()
    }

    // ── Per-message actions ─────────────────────────────────────

    fun onMessageLongPressed(messageId: String) = _uiState.update { it.copy(selectedMessageId = messageId) }
    fun clearMessageSelection()                  = _uiState.update { it.copy(selectedMessageId = null) }

    fun onReplyTo(message: Message) = _uiState.update { it.copy(replyTarget = message, selectedMessageId = null) }
    fun cancelReply()                = _uiState.update { it.copy(replyTarget = null) }

    fun deleteForMe(messageId: String) {
        val chatId = currentChatId ?: return
        clearMessageSelection()
        viewModelScope.launch { repository.deleteMessageForMe(chatId, messageId) }
    }

    fun deleteForEveryone(messageId: String) {
        val chatId = currentChatId ?: return
        clearMessageSelection()
        viewModelScope.launch { repository.deleteMessageForEveryone(chatId, messageId) }
    }

    fun react(messageId: String, emoji: String) {
        val chatId = currentChatId ?: return
        clearMessageSelection()
        viewModelScope.launch { repository.addReaction(chatId, messageId, emoji, senderId = "me") }
    }
}

