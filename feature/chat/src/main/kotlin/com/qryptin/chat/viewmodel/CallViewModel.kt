package com.qryptin.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.model.CallConnectionState
import com.qryptin.chat.model.CallType
import com.qryptin.chat.model.CallUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  CallViewModel
//  Shared by CallScreen (voice) and VideoCallScreen. Mocks the
//  WebRTC-ready connection lifecycle described in the spec —
//  no real RTC wiring: DIALING -> RINGING -> CONNECTING ->
//  CONNECTED, then a ticking duration counter. Real signalling
//  drops in later behind the same UI state shape.
// ─────────────────────────────────────────────────────────────
class CallViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private var loaded = false

    fun start(peerName: String, peerAvatarUrl: String?, callType: CallType) {
        if (loaded) return
        loaded = true

        _uiState.update {
            it.copy(
                peerName        = peerName,
                peerAvatarUrl   = peerAvatarUrl,
                callType        = callType,
                connectionState = CallConnectionState.DIALING,
                isVideoOn       = callType == CallType.VIDEO,
            )
        }
        simulateConnection()
    }

    private fun simulateConnection() {
        viewModelScope.launch {
            delay(600)
            _uiState.update { it.copy(connectionState = CallConnectionState.RINGING) }
            delay(1_400)
            _uiState.update { it.copy(connectionState = CallConnectionState.CONNECTING) }
            delay(900)
            _uiState.update { it.copy(connectionState = CallConnectionState.CONNECTED) }
            tickDuration()
        }
    }

    private fun tickDuration() {
        viewModelScope.launch {
            while (isActive && uiState.value.connectionState == CallConnectionState.CONNECTED) {
                delay(1_000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun toggleMute()       = _uiState.update { it.copy(isMuted = !it.isMuted) }
    fun toggleSpeaker()    = _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    fun toggleVideo()      = _uiState.update { it.copy(isVideoOn = !it.isVideoOn) }
    fun switchCamera()     = _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    fun toggleKeypad()     = _uiState.update { it.copy(isKeypadOpen = !it.isKeypadOpen) }

    fun endCall() {
        _uiState.update { it.copy(connectionState = CallConnectionState.ENDED) }
    }
}
