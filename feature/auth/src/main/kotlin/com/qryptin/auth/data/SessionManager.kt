package com.qryptin.auth.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────
//  DataStore instance — one per process, scoped to applicationContext
// ─────────────────────────────────────────────────────────────
private const val SESSION_DATASTORE_NAME = "qryptin_session"

private val Context.sessionDataStore by preferencesDataStore(name = SESSION_DATASTORE_NAME)

// ─────────────────────────────────────────────────────────────
//  SessionManager
//
//  Persists the user's authentication state locally so that the
//  app remembers a logged-in user across process death / restarts.
//  Session is cleared ONLY on explicit logout, app data wipe, or
//  uninstall/reinstall — never just because the app was closed.
// ─────────────────────────────────────────────────────────────
class SessionManager(context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val IS_LOGGED_IN  = booleanPreferencesKey("is_logged_in")
        val PHONE_NUMBER  = stringPreferencesKey("phone_number")
        val USERNAME      = stringPreferencesKey("username")
    }

    /** Reactive stream of the logged-in state — true once a session has been saved. */
    val isLoggedInFlow: Flow<Boolean> =
        appContext.sessionDataStore.data.map { prefs -> prefs[Keys.IS_LOGGED_IN] ?: false }

    /** Reactive stream of the persisted phone number (empty string when none saved). */
    val phoneNumberFlow: Flow<String> =
        appContext.sessionDataStore.data.map { prefs -> prefs[Keys.PHONE_NUMBER] ?: "" }

    /** Reactive stream of the persisted username (empty string when none saved). */
    val usernameFlow: Flow<String> =
        appContext.sessionDataStore.data.map { prefs -> prefs[Keys.USERNAME] ?: "" }

    /**
     * Persists a successful login locally.
     * Called once after OTP verification (existing user) or after
     * registration completes (new user).
     */
    suspend fun saveSession(phoneNumber: String, username: String = "") {
        appContext.sessionDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.PHONE_NUMBER] = phoneNumber
            if (username.isNotBlank()) {
                prefs[Keys.USERNAME] = username
            }
        }
    }

    /**
     * Clears the local session. Called on explicit logout.
     * After this call, [isLoggedIn] returns false and the app
     * routes back to the authentication flow on next launch.
     */
    suspend fun clearSession() {
        appContext.sessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /** One-shot check — true if a session is currently persisted. */
    suspend fun isLoggedIn(): Boolean = isLoggedInFlow.first()

    /** One-shot read of the persisted phone number, or null if never saved. */
    suspend fun getPhoneNumber(): String? = phoneNumberFlow.first().ifBlank { null }

    /** One-shot read of the persisted username, or null if never saved / not provided. */
    suspend fun getUsername(): String? = usernameFlow.first().ifBlank { null }
}
