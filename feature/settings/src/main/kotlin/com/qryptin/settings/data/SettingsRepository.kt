package com.qryptin.settings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qryptin.settings.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────
//  SettingsRepository
//
//  Single source of truth for every settings sub-screen.
//  Backed entirely by DataStore so toggles persist instantly and
//  survive process death — no Room needed for simple preferences.
//
//  Every read is wrapped with .catch so a corrupted/locked
//  DataStore file can't crash callers in other features (Issue 2's
//  failure-isolation requirement, applied at the repository seam).
// ─────────────────────────────────────────────────────────────
private val Context.settingsDataStore by preferencesDataStore(name = "qryptin_settings")

class SettingsRepository(context: Context) {

    private val appContext = context.applicationContext
    private val store get() = appContext.settingsDataStore

    private object Keys {
        val THEME_MODE             = stringPreferencesKey("theme_mode")

        val READ_RECEIPTS          = booleanPreferencesKey("read_receipts")
        val TYPING_INDICATORS      = booleanPreferencesKey("typing_indicators")
        val ENTER_TO_SEND          = booleanPreferencesKey("enter_to_send")
        val MEDIA_AUTO_DOWNLOAD    = stringPreferencesKey("media_auto_download")

        val RINGTONE_ENABLED       = booleanPreferencesKey("ringtone_enabled")
        val VIBRATE_ON_RING        = booleanPreferencesKey("vibrate_on_ring")
        val LOW_DATA_MODE          = booleanPreferencesKey("low_data_mode")
        val CALL_WAITING           = booleanPreferencesKey("call_waiting")

        val LAST_SEEN_VISIBLE      = booleanPreferencesKey("last_seen_visible")
        val PROFILE_PHOTO_VISIBLE  = booleanPreferencesKey("profile_photo_visible")
        val READ_RECEIPTS_LINKED   = booleanPreferencesKey("read_receipts_linked")
        val SCREEN_SECURITY_LOCK   = booleanPreferencesKey("screen_security_lock")

        val AUTO_DELETE_OLD_MEDIA  = booleanPreferencesKey("auto_delete_old_media")
        val MEDIA_CACHE_LIMIT_MB   = intPreferencesKey("media_cache_limit_mb")

        val MESSAGE_NOTIFICATIONS  = booleanPreferencesKey("message_notifications")
        val CALL_NOTIFICATIONS     = booleanPreferencesKey("call_notifications")
        val GROUP_NOTIFICATIONS    = booleanPreferencesKey("group_notifications")
        val NOTIFICATION_SOUND     = booleanPreferencesKey("notification_sound")
        val NOTIFICATION_PREVIEW   = booleanPreferencesKey("notification_preview")
    }

    val themeMode: Flow<ThemeMode> = store.data
        .map { prefs -> runCatching { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.LIGHT.name) }.getOrDefault(ThemeMode.LIGHT) }
        .catch { emit(ThemeMode.LIGHT) }

    val messaging: Flow<MessagingSettings> = store.data
        .map { prefs ->
            MessagingSettings(
                readReceipts      = prefs[Keys.READ_RECEIPTS] ?: true,
                typingIndicators  = prefs[Keys.TYPING_INDICATORS] ?: true,
                enterToSend       = prefs[Keys.ENTER_TO_SEND] ?: false,
                mediaAutoDownload = runCatching {
                    MediaAutoDownload.valueOf(prefs[Keys.MEDIA_AUTO_DOWNLOAD] ?: MediaAutoDownload.WIFI_ONLY.name)
                }.getOrDefault(MediaAutoDownload.WIFI_ONLY),
            )
        }
        .catch { emit(MessagingSettings()) }

    val calls: Flow<CallSettings> = store.data
        .map { prefs ->
            CallSettings(
                ringtoneEnabled = prefs[Keys.RINGTONE_ENABLED] ?: true,
                vibrateOnRing   = prefs[Keys.VIBRATE_ON_RING] ?: true,
                lowDataMode     = prefs[Keys.LOW_DATA_MODE] ?: false,
                callWaiting     = prefs[Keys.CALL_WAITING] ?: true,
            )
        }
        .catch { emit(CallSettings()) }

    val privacy: Flow<PrivacySettings> = store.data
        .map { prefs ->
            PrivacySettings(
                lastSeenVisible     = prefs[Keys.LAST_SEEN_VISIBLE] ?: true,
                profilePhotoVisible = prefs[Keys.PROFILE_PHOTO_VISIBLE] ?: true,
                readReceiptsLinked  = prefs[Keys.READ_RECEIPTS_LINKED] ?: true,
                screenSecurityLock  = prefs[Keys.SCREEN_SECURITY_LOCK] ?: false,
            )
        }
        .catch { emit(PrivacySettings()) }

    val storage: Flow<StorageSettings> = store.data
        .map { prefs ->
            StorageSettings(
                autoDeleteOldMedia = prefs[Keys.AUTO_DELETE_OLD_MEDIA] ?: false,
                mediaCacheLimitMb  = prefs[Keys.MEDIA_CACHE_LIMIT_MB] ?: 2048,
            )
        }
        .catch { emit(StorageSettings()) }

    val notifications: Flow<NotificationSettings> = store.data
        .map { prefs ->
            NotificationSettings(
                messageNotifications = prefs[Keys.MESSAGE_NOTIFICATIONS] ?: true,
                callNotifications    = prefs[Keys.CALL_NOTIFICATIONS] ?: true,
                groupNotifications   = prefs[Keys.GROUP_NOTIFICATIONS] ?: true,
                notificationSound    = prefs[Keys.NOTIFICATION_SOUND] ?: true,
                notificationPreview  = prefs[Keys.NOTIFICATION_PREVIEW] ?: true,
            )
        }
        .catch { emit(NotificationSettings()) }

    suspend fun setThemeMode(mode: ThemeMode) = safeEdit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setReadReceipts(enabled: Boolean)     = safeEdit { it[Keys.READ_RECEIPTS] = enabled }
    suspend fun setTypingIndicators(enabled: Boolean) = safeEdit { it[Keys.TYPING_INDICATORS] = enabled }
    suspend fun setEnterToSend(enabled: Boolean)      = safeEdit { it[Keys.ENTER_TO_SEND] = enabled }
    suspend fun setMediaAutoDownload(mode: MediaAutoDownload) = safeEdit { it[Keys.MEDIA_AUTO_DOWNLOAD] = mode.name }

    suspend fun setRingtoneEnabled(enabled: Boolean) = safeEdit { it[Keys.RINGTONE_ENABLED] = enabled }
    suspend fun setVibrateOnRing(enabled: Boolean)   = safeEdit { it[Keys.VIBRATE_ON_RING] = enabled }
    suspend fun setLowDataMode(enabled: Boolean)     = safeEdit { it[Keys.LOW_DATA_MODE] = enabled }
    suspend fun setCallWaiting(enabled: Boolean)     = safeEdit { it[Keys.CALL_WAITING] = enabled }

    suspend fun setLastSeenVisible(enabled: Boolean)     = safeEdit { it[Keys.LAST_SEEN_VISIBLE] = enabled }
    suspend fun setProfilePhotoVisible(enabled: Boolean) = safeEdit { it[Keys.PROFILE_PHOTO_VISIBLE] = enabled }
    suspend fun setReadReceiptsLinked(enabled: Boolean)  = safeEdit { it[Keys.READ_RECEIPTS_LINKED] = enabled }
    suspend fun setScreenSecurityLock(enabled: Boolean)  = safeEdit { it[Keys.SCREEN_SECURITY_LOCK] = enabled }

    suspend fun setAutoDeleteOldMedia(enabled: Boolean) = safeEdit { it[Keys.AUTO_DELETE_OLD_MEDIA] = enabled }
    suspend fun setMediaCacheLimitMb(limit: Int)        = safeEdit { it[Keys.MEDIA_CACHE_LIMIT_MB] = limit }

    suspend fun setMessageNotifications(enabled: Boolean) = safeEdit { it[Keys.MESSAGE_NOTIFICATIONS] = enabled }
    suspend fun setCallNotifications(enabled: Boolean)    = safeEdit { it[Keys.CALL_NOTIFICATIONS] = enabled }
    suspend fun setGroupNotifications(enabled: Boolean)   = safeEdit { it[Keys.GROUP_NOTIFICATIONS] = enabled }
    suspend fun setNotificationSound(enabled: Boolean)    = safeEdit { it[Keys.NOTIFICATION_SOUND] = enabled }
    suspend fun setNotificationPreview(enabled: Boolean)  = safeEdit { it[Keys.NOTIFICATION_PREVIEW] = enabled }

    /** Wraps every write so a DataStore I/O failure can't propagate into a crash. */
    private suspend fun safeEdit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        runCatching { store.edit { block(it) } }
    }
}
