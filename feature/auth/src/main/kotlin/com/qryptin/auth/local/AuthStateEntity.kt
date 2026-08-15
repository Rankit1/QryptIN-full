package com.qryptin.auth.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────
//  AuthStateEntity
//
//  Singleton row (always id = SINGLETON_ID) holding the app's
//  current "am I logged in" snapshot. Kept separate from
//  SessionEntity so navigation (SplashScreen / AuthNavHost) can
//  do one cheap, single-row read instead of querying + filtering
//  the sessions table on every cold start.
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "auth_state")
data class AuthStateEntity(
    @PrimaryKey
    val id             : Int = SINGLETON_ID,

    val isLoggedIn       : Boolean = false,

    val currentUserId     : String? = null,

    val currentPhoneNumber : String? = null,

    val updatedAt            : Long = 0L,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
