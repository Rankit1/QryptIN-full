package com.qryptin.calls.engine

import com.qryptin.calls.model.ActiveCallState
import com.qryptin.calls.model.CallState
import com.qryptin.calls.model.CallType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  MockCallEngine
//  Simulates a secure call locally without any real RTC.
//  Realistic state progression:
//    CALLING → CONNECTING → CONNECTED → (timer runs) → ENDED
//  Architecture is ready for a real SecureCallEngine impl.
// ─────────────────────────────────────────────────────────────
class MockCallEngine : SecureCallEngine {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _callState = MutableStateFlow(ActiveCallState())
    override val callState: StateFlow<ActiveCallState> = _callState.asStateFlow()

    private var timerJob : Job? = null
    private var connectJob: Job? = null

    override suspend fun placeCall(
        contactId   : String,
        contactName : String,
        type        : CallType,
    ) {
        val callId = "call_${UUID.randomUUID()}"
        _callState.update {
            ActiveCallState(
                callId       = callId,
                contactId    = contactId,
                contactName  = contactName,
                type         = type,
                state        = CallState.CALLING,
                isCameraOn   = type == CallType.VIDEO,
                isEncrypted  = true,
            )
        }

        // Simulate network progression
        connectJob = scope.launch {
            delay(1_500)
            _callState.update { it.copy(state = CallState.CONNECTING) }
            delay(1_200)
            _callState.update { it.copy(state = CallState.CONNECTED) }
            startTimer()
        }
    }

    override suspend fun answerCall() {
        _callState.update { it.copy(state = CallState.CONNECTING) }
        delay(800)
        _callState.update { it.copy(state = CallState.CONNECTED) }
        startTimer()
    }

    override suspend fun endCall() {
        timerJob?.cancel()
        connectJob?.cancel()
        _callState.update { it.copy(state = CallState.ENDED) }
        delay(600)
        _callState.update { ActiveCallState() }
    }

    override suspend fun toggleMute() =
        _callState.update { it.copy(isMuted = !it.isMuted) }

    override suspend fun toggleSpeaker() =
        _callState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }

    override suspend fun toggleCamera() =
        _callState.update { it.copy(isCameraOn = !it.isCameraOn) }

    override suspend fun switchCamera() =
        _callState.update { it.copy(isFrontCamera = !it.isFrontCamera) }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1_000)
                _callState.update { it.copy(durationSecs = it.durationSecs + 1) }
            }
        }
    }
}
