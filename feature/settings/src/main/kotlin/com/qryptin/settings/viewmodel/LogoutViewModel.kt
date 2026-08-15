package com.qryptin.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.auth.AuthModule
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  LogoutViewModel
//
//  Performs the actual session clear via feature:auth's
//  SessionRepository (Room-backed sessions + auth_state tables).
//  That single call is what backs the "active authentication
//  token removed" and "remembered authentication state cleared"
//  guarantees shown in the logout confirmation dialog.
//
//  User profile data (UserEntity) is intentionally left alone --
//  per the logout spec, local chats/contacts/profile persist and
//  only the auth/session state is cleared, so returning to the
//  same phone number is recognized as an existing user again.
//
//  Deliberately scoped to :feature:settings (which already
//  depends on :feature:auth) rather than reusing AuthViewModel,
//  so the settings screen doesn't need to reach into an
//  auth-graph-scoped ViewModel to perform a simple, one-shot
//  clear operation.
// ─────────────────────────────────────────────────────────────
class LogoutViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = AuthModule.provideSessionRepository(application)

    /**
     * Clears the local auth session and invokes [onComplete] once
     * persistence has finished -- callers should navigate to the
     * auth flow (with the back stack cleared) inside [onComplete].
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.endSession()
            onComplete()
        }
    }
}
