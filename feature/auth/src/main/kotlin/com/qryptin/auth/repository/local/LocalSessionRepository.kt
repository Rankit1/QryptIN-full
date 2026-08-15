package com.qryptin.auth.repository.local

import com.qryptin.auth.local.AuthStateDao
import com.qryptin.auth.local.AuthStateEntity
import com.qryptin.auth.local.SessionDao
import com.qryptin.auth.local.SessionEntity
import com.qryptin.auth.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  LocalSessionRepository
//  Room-backed implementation of SessionRepository, writing to
//  both `sessions` (history) and `auth_state` (fast singleton
//  snapshot read on every app cold start).
// ─────────────────────────────────────────────────────────────
class LocalSessionRepository(
    private val sessionDao   : SessionDao,
    private val authStateDao : AuthStateDao,
) : SessionRepository {

    override val isLoggedIn: Flow<Boolean> =
        authStateDao.observe().map { it?.isLoggedIn == true }

    override suspend fun isLoggedInNow(): Boolean =
        isLoggedIn.first()

    override suspend fun currentUserId(): String? =
        authStateDao.get()?.takeIf { it.isLoggedIn }?.currentUserId

    override suspend fun startSession(userId: String, phoneNumber: String) {
        val now = System.currentTimeMillis()

        sessionDao.deactivateAllSessions()
        sessionDao.insert(
            SessionEntity(
                sessionId    = UUID.randomUUID().toString(),
                userId        = userId,
                phoneNumber    = phoneNumber,
                // Mock local token — replaced by a real server-issued
                // token once AuthRepository talks to a real backend.
                token           = UUID.randomUUID().toString(),
                createdAt        = now,
                isActive          = true,
            )
        )
        authStateDao.upsert(
            AuthStateEntity(
                isLoggedIn         = true,
                currentUserId       = userId,
                currentPhoneNumber   = phoneNumber,
                updatedAt             = now,
            )
        )
    }

    override suspend fun endSession() {
        sessionDao.deactivateAllSessions()
        authStateDao.clear()
    }
}
