package com.qryptin.settings.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// ─────────────────────────────────────────────────────────────
//  SettingsDataStore
//  Single DataStore instance for ALL QryptIN settings.
//  Persists across app restarts, emulator restarts, nav changes.
// ─────────────────────────────────────────────────────────────

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "qryptin_settings")

object SettingsKeys {
    // ── Theme ────────────────────────────────────────────────
    val THEME_MODE          = stringPreferencesKey("theme_mode")   // LIGHT / DARK / AMOLED

    // ── Messaging ────────────────────────────────────────────
    val READ_RECEIPTS       = booleanPreferencesKey("read_receipts")
    val TYPING_INDICATORS   = booleanPreferencesKey("typing_indicators")
    val ONLINE_STATUS       = booleanPreferencesKey("online_status")
    val LAST_SEEN           = booleanPreferencesKey("last_seen")
    val SCREENSHOT_BLOCK    = booleanPreferencesKey("screenshot_block")
    val DISAPPEARING_MSGS   = booleanPreferencesKey("disappearing_msgs")
    val MSG_TIMER_SECONDS   = intPreferencesKey("msg_timer_seconds")    // 0 = off
    val AUTO_DOWNLOAD_MEDIA = booleanPreferencesKey("auto_download_media")
    val SAVE_TO_GALLERY     = booleanPreferencesKey("save_to_gallery")
    val FONT_SIZE           = stringPreferencesKey("font_size")         // SMALL/MEDIUM/LARGE
    val ENTER_IS_SEND       = booleanPreferencesKey("enter_is_send")
    val ARCHIVE_BEHAVIOR    = stringPreferencesKey("archive_behavior")  // KEEP / AUTO_UNARCHIVE

    // ── Calls ────────────────────────────────────────────────
    val LOW_BANDWIDTH       = booleanPreferencesKey("low_bandwidth")
    val HD_VIDEO            = booleanPreferencesKey("hd_video")
    val ECHO_CANCEL         = booleanPreferencesKey("echo_cancel")
    val NOISE_SUPPRESS      = booleanPreferencesKey("noise_suppress")
    val AUTO_SPEAKER        = booleanPreferencesKey("auto_speaker")
    val CALL_NOTIF          = booleanPreferencesKey("call_notif")
    val CALL_VIBRATE        = booleanPreferencesKey("call_vibrate")
    val MUTE_UNKNOWN        = booleanPreferencesKey("mute_unknown")

    // ── Contacts ─────────────────────────────────────────────
    val SYNC_DEVICE_CONTACTS= booleanPreferencesKey("sync_device_contacts")
    val SYNC_SIM_CONTACTS   = booleanPreferencesKey("sync_sim_contacts")
    val SHOW_ONLY_QRYPTIN   = booleanPreferencesKey("show_only_qryptin")
    val CONTACT_SORT        = stringPreferencesKey("contact_sort")      // FIRST_NAME / LAST_NAME

    // ── Security ─────────────────────────────────────────────
    val APP_LOCK            = booleanPreferencesKey("app_lock")
    val BIOMETRIC_LOCK      = booleanPreferencesKey("biometric_lock")
    val FINGERPRINT_UNLOCK  = booleanPreferencesKey("fingerprint_unlock")
    val SECURE_NOTIF        = booleanPreferencesKey("secure_notif")

    // ── Notifications ─────────────────────────────────────────
    val MSG_NOTIF           = booleanPreferencesKey("msg_notif")
    val GROUP_NOTIF         = booleanPreferencesKey("group_notif")
    val POPUP_NOTIF         = booleanPreferencesKey("popup_notif")
    val NOTIF_PREVIEW       = booleanPreferencesKey("notif_preview")
    val NOTIF_VIBRATE       = booleanPreferencesKey("notif_vibrate")

    // ── Storage ──────────────────────────────────────────────
    val AUTO_DELETE_MEDIA   = booleanPreferencesKey("auto_delete_media")
}

data class SettingsState(
    // Theme
    val themeMode           : ThemeMode     = ThemeMode.LIGHT,
    // Messaging
    val readReceipts        : Boolean       = true,
    val typingIndicators    : Boolean       = true,
    val onlineStatus        : Boolean       = true,
    val lastSeen            : Boolean       = true,
    val screenshotBlock     : Boolean       = false,
    val disappearingMsgs    : Boolean       = false,
    val msgTimerSeconds     : Int           = 0,
    val autoDownloadMedia   : Boolean       = true,
    val saveToGallery       : Boolean       = false,
    val fontSize            : FontSizeOption= FontSizeOption.MEDIUM,
    val enterIsSend         : Boolean       = false,
    val archiveBehavior     : ArchiveBehavior = ArchiveBehavior.KEEP,
    // Calls
    val lowBandwidth        : Boolean       = false,
    val hdVideo             : Boolean       = true,
    val echoCancel          : Boolean       = true,
    val noiseSuppress       : Boolean       = true,
    val autoSpeaker         : Boolean       = false,
    val callNotif           : Boolean       = true,
    val callVibrate         : Boolean       = true,
    val muteUnknown         : Boolean       = false,
    // Contacts
    val syncDeviceContacts  : Boolean       = false,
    val syncSimContacts     : Boolean       = false,
    val showOnlyQryptin     : Boolean       = false,
    val contactSort         : ContactSort   = ContactSort.FIRST_NAME,
    // Security
    val appLock             : Boolean       = false,
    val biometricLock       : Boolean       = false,
    val fingerprintUnlock   : Boolean       = false,
    val secureNotif         : Boolean       = false,
    // Notifications
    val msgNotif            : Boolean       = true,
    val groupNotif          : Boolean       = true,
    val popupNotif          : Boolean       = true,
    val notifPreview        : Boolean       = true,
    val notifVibrate        : Boolean       = true,
    // Storage
    val autoDeleteMedia     : Boolean       = false,
)

enum class ThemeMode { LIGHT, DARK, AMOLED }
enum class FontSizeOption { SMALL, MEDIUM, LARGE }
enum class ArchiveBehavior { KEEP, AUTO_UNARCHIVE }
enum class ContactSort { FIRST_NAME, LAST_NAME }

class SettingsDataStore(private val context: Context) {

    val settingsFlow: Flow<SettingsState> = context.settingsDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            SettingsState(
                themeMode           = ThemeMode.valueOf(prefs[SettingsKeys.THEME_MODE] ?: ThemeMode.LIGHT.name),
                readReceipts        = prefs[SettingsKeys.READ_RECEIPTS]     ?: true,
                typingIndicators    = prefs[SettingsKeys.TYPING_INDICATORS] ?: true,
                onlineStatus        = prefs[SettingsKeys.ONLINE_STATUS]     ?: true,
                lastSeen            = prefs[SettingsKeys.LAST_SEEN]         ?: true,
                screenshotBlock     = prefs[SettingsKeys.SCREENSHOT_BLOCK]  ?: false,
                disappearingMsgs    = prefs[SettingsKeys.DISAPPEARING_MSGS] ?: false,
                msgTimerSeconds     = prefs[SettingsKeys.MSG_TIMER_SECONDS] ?: 0,
                autoDownloadMedia   = prefs[SettingsKeys.AUTO_DOWNLOAD_MEDIA]?: true,
                saveToGallery       = prefs[SettingsKeys.SAVE_TO_GALLERY]   ?: false,
                fontSize            = FontSizeOption.valueOf(prefs[SettingsKeys.FONT_SIZE] ?: FontSizeOption.MEDIUM.name),
                enterIsSend         = prefs[SettingsKeys.ENTER_IS_SEND]     ?: false,
                archiveBehavior     = ArchiveBehavior.valueOf(prefs[SettingsKeys.ARCHIVE_BEHAVIOR] ?: ArchiveBehavior.KEEP.name),
                lowBandwidth        = prefs[SettingsKeys.LOW_BANDWIDTH]     ?: false,
                hdVideo             = prefs[SettingsKeys.HD_VIDEO]          ?: true,
                echoCancel          = prefs[SettingsKeys.ECHO_CANCEL]       ?: true,
                noiseSuppress       = prefs[SettingsKeys.NOISE_SUPPRESS]    ?: true,
                autoSpeaker         = prefs[SettingsKeys.AUTO_SPEAKER]      ?: false,
                callNotif           = prefs[SettingsKeys.CALL_NOTIF]        ?: true,
                callVibrate         = prefs[SettingsKeys.CALL_VIBRATE]      ?: true,
                muteUnknown         = prefs[SettingsKeys.MUTE_UNKNOWN]      ?: false,
                syncDeviceContacts  = prefs[SettingsKeys.SYNC_DEVICE_CONTACTS]?: false,
                syncSimContacts     = prefs[SettingsKeys.SYNC_SIM_CONTACTS] ?: false,
                showOnlyQryptin     = prefs[SettingsKeys.SHOW_ONLY_QRYPTIN] ?: false,
                contactSort         = ContactSort.valueOf(prefs[SettingsKeys.CONTACT_SORT] ?: ContactSort.FIRST_NAME.name),
                appLock             = prefs[SettingsKeys.APP_LOCK]          ?: false,
                biometricLock       = prefs[SettingsKeys.BIOMETRIC_LOCK]    ?: false,
                fingerprintUnlock   = prefs[SettingsKeys.FINGERPRINT_UNLOCK]?: false,
                secureNotif         = prefs[SettingsKeys.SECURE_NOTIF]      ?: false,
                msgNotif            = prefs[SettingsKeys.MSG_NOTIF]         ?: true,
                groupNotif          = prefs[SettingsKeys.GROUP_NOTIF]       ?: true,
                popupNotif          = prefs[SettingsKeys.POPUP_NOTIF]       ?: true,
                notifPreview        = prefs[SettingsKeys.NOTIF_PREVIEW]     ?: true,
                notifVibrate        = prefs[SettingsKeys.NOTIF_VIBRATE]     ?: true,
                autoDeleteMedia     = prefs[SettingsKeys.AUTO_DELETE_MEDIA] ?: false,
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) =
        context.settingsDataStore.edit { it[SettingsKeys.THEME_MODE] = mode.name }

    suspend fun setBoolean(key: Preferences.Key<Boolean>, value: Boolean) =
        context.settingsDataStore.edit { it[key] = value }

    suspend fun setInt(key: Preferences.Key<Int>, value: Int) =
        context.settingsDataStore.edit { it[key] = value }

    suspend fun setString(key: Preferences.Key<String>, value: String) =
        context.settingsDataStore.edit { it[key] = value }
}
