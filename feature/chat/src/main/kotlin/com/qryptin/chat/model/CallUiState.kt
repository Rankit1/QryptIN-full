package com.qryptin.chat.model

// ─────────────────────────────────────────────────────────────
//  CallUiState
//  Shared by CallScreen (voice) and VideoCallScreen — the mock
//  WebRTC-ready surface described in the chat module spec.
//  No real RTC wiring yet; CallViewModel simulates the state
//  machine with delays so the UI is fully exercised.
// ─────────────────────────────────────────────────────────────
data class CallUiState(
    val peerName        : String              = "",
    val peerAvatarUrl   : String?              = null,
    val callType        : CallType             = CallType.VOICE,
    val connectionState : CallConnectionState  = CallConnectionState.DIALING,
    val elapsedSeconds  : Int                  = 0,
    val isMuted         : Boolean              = false,
    val isSpeakerOn     : Boolean              = false,
    val isVideoOn       : Boolean              = true,   // VIDEO calls only
    val isFrontCamera   : Boolean              = true,    // VIDEO calls only
    val isKeypadOpen    : Boolean              = false,
) {
    val formattedDuration: String get() {
        val m = elapsedSeconds / 60
        val s = elapsedSeconds % 60
        return "%d:%02d".format(m, s)
    }
}

// ─────────────────────────────────────────────────────────────
//  ChatSearchUiState
//  Drives ChatSearchScreen — unified search across chats,
//  contacts, messages, and files.
// ─────────────────────────────────────────────────────────────
enum class ChatSearchScope(val label: String) {
    ALL("All"),
    CHATS("Chats"),
    MESSAGES("Messages"),
    FILES("Files"),
}

data class ChatSearchResult(
    val chatId      : String,
    val chatTitle   : String,
    val snippet     : String,
    val scope       : ChatSearchScope,
    val timestamp   : Long,
)

data class ChatSearchUiState(
    val query       : String                = "",
    val scope       : ChatSearchScope        = ChatSearchScope.ALL,
    val results     : List<ChatSearchResult> = emptyList(),
    val isSearching : Boolean                = false,
)

// ─────────────────────────────────────────────────────────────
//  MediaPreviewUiState
//  Drives MediaPreviewScreen — caption + multi-item preview
//  before a media message is sent.
// ─────────────────────────────────────────────────────────────
data class MediaPreviewItem(
    val uri        : String,
    val isVideo    : Boolean = false,
)

data class MediaPreviewUiState(
    val items       : List<MediaPreviewItem> = emptyList(),
    val currentIndex: Int                    = 0,
    val caption     : String                 = "",
    val isSending   : Boolean                = false,
)
