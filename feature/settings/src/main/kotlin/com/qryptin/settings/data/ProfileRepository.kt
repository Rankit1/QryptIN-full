package com.qryptin.settings.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qryptin.settings.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  ProfileRepository
//
//  Issue 3 fix — there was previously no profile editing path at
//  all (no EditProfileScreen, no ProfileRepository, no ProfileViewModel).
//  This persists display name / username / bio / email in DataStore,
//  and copies a picked avatar image into app-private storage (same
//  approach as MediaStorage in feature:chat) so it survives picker
//  permission expiry and process death, and is addressable everywhere
//  (chats, contacts, calls, settings, groups) via a single stable
//  file:// URI read from here.
// ─────────────────────────────────────────────────────────────
private val Context.profileDataStore by preferencesDataStore(name = "qryptin_profile")

class ProfileRepository(context: Context) {

    private val appContext = context.applicationContext
    private val store get() = appContext.profileDataStore
    private val avatarDir get() = File(appContext.filesDir, "avatar").apply { mkdirs() }

    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val USERNAME     = stringPreferencesKey("username")
        val BIO          = stringPreferencesKey("bio")
        val EMAIL        = stringPreferencesKey("email")
        val AVATAR_URI   = stringPreferencesKey("avatar_uri")
    }

    /** Reactive stream of the current profile; failure-isolated so a DataStore error never crashes other screens. */
    val profile: Flow<Profile> = store.data
        .map { prefs ->
            Profile(
                displayName = prefs[Keys.DISPLAY_NAME] ?: "",
                username    = prefs[Keys.USERNAME] ?: "",
                bio         = prefs[Keys.BIO] ?: "",
                email       = prefs[Keys.EMAIL] ?: "",
                avatarUri   = prefs[Keys.AVATAR_URI],
            )
        }
        .catch { emit(Profile()) }

    suspend fun updateProfile(displayName: String, username: String, bio: String, email: String): Result<Unit> =
        runCatching {
            store.edit { prefs ->
                prefs[Keys.DISPLAY_NAME] = displayName.trim()
                prefs[Keys.USERNAME]     = username.trim()
                prefs[Keys.BIO]          = bio.trim()
                prefs[Keys.EMAIL]        = email.trim()
            }
        }

    /**
     * Copies [sourceUri] (from a gallery picker) into app-private storage,
     * replacing any previously saved avatar, and persists the new local
     * URI so it can be read back across app restarts.
     */
    suspend fun updateAvatar(sourceUri: Uri): Result<String> = runCatching {
        val resolver = appContext.contentResolver

        val destFile = File(avatarDir, "${UUID.randomUUID()}.jpg")
        resolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not open the selected image")

        // Best-effort: remove older avatar files so storage doesn't grow unbounded.
        avatarDir.listFiles()?.forEach { existing ->
            if (existing.name != destFile.name) existing.delete()
        }

        val newUri = Uri.fromFile(destFile).toString()
        store.edit { prefs -> prefs[Keys.AVATAR_URI] = newUri }
        newUri
    }
}
