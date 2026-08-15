package com.qryptin.auth.repository

import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  SessionRepository
//
//  Abstracts session persistence. Today: Room (sessions +
//  auth_state tables) via LocalSessionRepository. Later: the
//  same interface backs a cloud Authentication Service session
//  (refresh tokens, multi-device, forced logout) without any
//  caller (ViewModel/UI) needing to change.
// ─────────────────────────────────────────────────────────────
interface SessionRepository {

    /** Reactive "is a session currently active" flag — drives Splash/nav decisions. */
    val isLoggedIn: Flow<Boolean>

    suspend fun isLoggedInNow(): Boolean

    suspend fun currentUserId(): String?

    /** Starts a new active session for [userId], deactivating any previous one. */
    suspend fun startSession(userId: String, phoneNumber: String)

    /** Clears the active session (logout). User profile data in UserRepository is untouched. */
    suspend fun endSession()
}
