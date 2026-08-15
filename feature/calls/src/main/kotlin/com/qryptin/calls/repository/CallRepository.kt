package com.qryptin.calls.repository

import com.qryptin.calls.model.CallModel
import com.qryptin.calls.model.CallType
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  CallRepository
//  Interface kept backend-agnostic so a real WebRTC / signalling
//  implementation can be swapped in later without touching the UI.
// ─────────────────────────────────────────────────────────────
interface CallRepository {

    /** Reactive stream of all call history, newest first. */
    fun observeAll(): Flow<List<CallModel>>

    /** Live-filtered stream matching name or phone. */
    fun search(query: String): Flow<List<CallModel>>

    /** Persist a finished call record. */
    suspend fun saveCall(call: CallModel)

    /** Update the duration of an existing call after it ends. */
    suspend fun updateDuration(callId: String, durationSecs: Int)

    /** Remove a single entry from local history. */
    suspend fun delete(callId: String)
}
