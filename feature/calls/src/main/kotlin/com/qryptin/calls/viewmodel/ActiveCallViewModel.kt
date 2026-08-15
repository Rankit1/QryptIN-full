package com.qryptin.calls.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.calls.data.repository.RoomCallRepository
import com.qryptin.calls.engine.MockCallEngine
import com.qryptin.calls.model.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ActiveCallViewModel(application: Application) : AndroidViewModel(application) {

    private val engine     = MockCallEngine()
    private val repository = RoomCallRepository(application)

    val callState: StateFlow<ActiveCallState> = engine.callState

    fun startCall(
        contactId   : String,
        contactName : String,
        contactAvatar: String?,
        type        : CallType,
    ) {
        viewModelScope.launch {
            engine.placeCall(contactId, contactName, type)
        }
    }

    fun endCall() {
        viewModelScope.launch {
            val state = engine.callState.value
            // Persist to history before clearing
            if (state.callId.isNotBlank()) {
                repository.saveCall(
                    CallModel(
                        callId        = state.callId,
                        contactId     = state.contactId,
                        contactName   = state.contactName,
                        contactAvatar = state.contactAvatar,
                        type          = state.type,
                        direction     = CallDirection.OUTGOING,
                        timestamp     = System.currentTimeMillis(),
                        durationSecs  = state.durationSecs,
                    )
                )
            }
            engine.endCall()
        }
    }

    fun toggleMute()     = viewModelScope.launch { engine.toggleMute() }
    fun toggleSpeaker()  = viewModelScope.launch { engine.toggleSpeaker() }
    fun toggleCamera()   = viewModelScope.launch { engine.toggleCamera() }
    fun switchCamera()   = viewModelScope.launch { engine.switchCamera() }
}
