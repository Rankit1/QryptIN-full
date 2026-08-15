package com.qryptin.calls.model

// ─────────────────────────────────────────────────────────────
//  CallType — voice or video
// ─────────────────────────────────────────────────────────────
enum class CallType { VOICE, VIDEO }

// ─────────────────────────────────────────────────────────────
//  CallDirection — who initiated
// ─────────────────────────────────────────────────────────────
enum class CallDirection { OUTGOING, INCOMING, MISSED }

// ─────────────────────────────────────────────────────────────
//  CallState — lifecycle of an active call
// ─────────────────────────────────────────────────────────────
enum class CallState {
    IDLE,
    CALLING,       // local user placed call, waiting for remote
    RINGING,       // incoming ring before answer
    CONNECTING,    // handshake / encryption negotiation
    CONNECTED,     // live call
    ENDED,
    FAILED,
}

// ─────────────────────────────────────────────────────────────
//  CallModel — domain object used throughout UI
// ─────────────────────────────────────────────────────────────
data class CallModel(
    val callId       : String,
    val contactId    : String,
    val contactName  : String,
    val contactAvatar: String? = null,
    val contactPhone : String  = "",
    val type         : CallType,
    val direction    : CallDirection,
    val timestamp    : Long,
    val durationSecs : Int    = 0,
    val isEncrypted  : Boolean = true,
)

// ─────────────────────────────────────────────────────────────
//  CallGroup — UI grouping (Recent / Yesterday / Earlier)
// ─────────────────────────────────────────────────────────────
data class CallGroup(
    val label : String,
    val calls  : List<CallModel>,
)

// ─────────────────────────────────────────────────────────────
//  ActiveCallState — state emitted during an active call
// ─────────────────────────────────────────────────────────────
data class ActiveCallState(
    val callId        : String       = "",
    val contactId     : String       = "",
    val contactName   : String       = "",
    val contactAvatar : String?      = null,
    val type          : CallType     = CallType.VOICE,
    val state         : CallState    = CallState.IDLE,
    val durationSecs  : Int          = 0,
    val isMuted       : Boolean      = false,
    val isSpeakerOn   : Boolean      = false,
    val isCameraOn    : Boolean      = true,
    val isFrontCamera : Boolean      = true,
    val isEncrypted   : Boolean      = true,
    val errorMessage  : String?      = null,
)

// ─────────────────────────────────────────────────────────────
//  CallsUiState — home screen state
// ─────────────────────────────────────────────────────────────
data class CallsUiState(
    val isLoading    : Boolean        = false,
    val searchQuery  : String         = "",
    val groups       : List<CallGroup> = emptyList(),
    val allCalls     : List<CallModel> = emptyList(),
)
