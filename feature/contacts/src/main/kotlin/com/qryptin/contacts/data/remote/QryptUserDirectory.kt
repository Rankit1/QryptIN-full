package com.qryptin.contacts.data.remote

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

// ─────────────────────────────────────────────────────────────
//  QryptUser
//  A single record from the simulated global QryptIN registry.
// ─────────────────────────────────────────────────────────────
data class QryptUser(
    val name  : String,
    val phone : String,
    val email : String,
)

// ─────────────────────────────────────────────────────────────
//  QryptUserDirectory
//
//  Simulated global QryptIN user registry, backed entirely by
//  app/src/main/assets/mock_users.json — no hardcoded user data
//  lives in Kotlin source anywhere in the app.
//
//  The person maintaining this project can update the registry
//  simply by editing/regenerating mock_users.json (e.g. via an
//  Excel/CSV export) without touching any code.
// ─────────────────────────────────────────────────────────────
class QryptUserDirectory(context: Context) {

    private val appContext = context.applicationContext
    private val loadMutex   = Mutex()
    private var cache       : List<QryptUser>? = null

    /**
     * Loads (and caches) every record from mock_users.json.
     * Safe to call repeatedly — parsing only happens once per process.
     */
    private suspend fun loadUsers(): List<QryptUser> {
        cache?.let { return it }
        return loadMutex.withLock {
            cache?.let { return it }
            val parsed = withContext(Dispatchers.IO) {
                val json = appContext.assets.open(ASSET_FILE_NAME)
                    .bufferedReader()
                    .use { it.readText() }
                val array = JSONArray(json)
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        add(
                            QryptUser(
                                name  = obj.optString("name"),
                                phone = obj.optString("phone"),
                                email = obj.optString("email"),
                            )
                        )
                    }
                }
            }
            cache = parsed
            parsed
        }
    }

    /**
     * Looks up a user by phone number. [fullPhoneDigits] should contain
     * only digits (country code + local number, no '+', spaces, or dashes).
     *
     * @return the matching [QryptUser] if this number is registered on
     *         QryptIN, or null if it is not (→ non-QryptIN user flow).
     */
    suspend fun lookupByPhone(fullPhoneDigits: String): QryptUser? {
        val target = fullPhoneDigits.filter { it.isDigit() }
        if (target.isEmpty()) return null
        return loadUsers().firstOrNull { user ->
            user.phone.filter { it.isDigit() } == target
        }
    }

    companion object {
        private const val ASSET_FILE_NAME = "mock_users.json"
    }
}
