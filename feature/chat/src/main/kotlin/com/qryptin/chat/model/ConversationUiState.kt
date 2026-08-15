package com.qryptin.chat.model

// ─────────────────────────────────────────────────────────────
//  ConversationUiState
//  Single source of truth flowing from ConversationViewModel.
// ─────────────────────────────────────────────────────────────

/** Which bottom sheet (if any) is currently presented over the conversation. */
enum class ConversationSheet {
    NONE,
    ATTACHMENT,
    STICKER,
    EMOJI,
}

data class ConversationUiState(
    val chat              : Chat?            = null,
    val messages          : List<Message>    = emptyList(),
    val draftText         : String           = "",
    val replyTarget       : Message?         = null,
    val selectedMessageId : String?          = null,
    val activeSheet       : ConversationSheet = ConversationSheet.NONE,
    val isPeerTyping      : Boolean          = false,
    val isLoading         : Boolean          = true,
    val errorMessage      : String?          = null,
    /** Set once a picker has copied a file locally; shown via MediaPreviewScreen before sending. */
    val pendingAttachment : Attachment?      = null,
    val pendingType       : MessageType?     = null,
    val isAttaching       : Boolean          = false,
) {
    /** Mic button swaps to send the moment there is non-blank draft text. */
    val showSendIcon: Boolean get() = draftText.isNotBlank()
}
