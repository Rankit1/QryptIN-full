package com.qryptin.calls.engine

import com.qryptin.calls.model.ActiveCallState
import com.qryptin.calls.model.CallType
import kotlinx.coroutines.flow.StateFlow

// ─────────────────────────────────────────────────────────────
//  SecureCallEngine
//  Interface that separates call signalling from UI.
//  Implement with WebRTC / Agora / Twilio when ready.
// ─────────────────────────────────────────────────────────────
interface SecureCallEngine {
    val callState: StateFlow<ActiveCallState>
    suspend fun placeCall(contactId: String, contactName: String, type: CallType)
    suspend fun answerCall()
    suspend fun endCall()
    suspend fun toggleMute()
    suspend fun toggleSpeaker()
    suspend fun toggleCamera()
    suspend fun switchCamera()
}

// ─────────────────────────────────────────────────────────────
//  EncryptionManager  (stub — wire real E2E later)
// ─────────────────────────────────────────────────────────────
interface EncryptionManager {
    fun generateSessionKey(): ByteArray
    fun encryptFrame(frame: ByteArray, key: ByteArray): ByteArray
    fun decryptFrame(frame: ByteArray, key: ByteArray): ByteArray
}

// ─────────────────────────────────────────────────────────────
//  RTCSessionManager  (stub — wire WebRTC SDP/ICE later)
// ─────────────────────────────────────────────────────────────
interface RTCSessionManager {
    suspend fun createOffer(): String
    suspend fun createAnswer(offer: String): String
    suspend fun addIceCandidate(candidate: String)
}
