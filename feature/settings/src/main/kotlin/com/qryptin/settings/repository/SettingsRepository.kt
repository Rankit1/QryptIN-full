package com.qryptin.settings.repository

import android.content.Context
import com.qryptin.settings.datastore.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  SettingsRepository
//  Thin wrapper around SettingsDataStore — single entry point
//  used by the ViewModel. Trivially swappable in tests.
// ─────────────────────────────────────────────────────────────
class SettingsRepository(context: Context) {

    private val store = SettingsDataStore(context)

    val settings: Flow<SettingsState> = store.settingsFlow

    // ── Theme ────────────────────────────────────────────────
    suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)

    // ── Messaging toggles ─────────────────────────────────────
    suspend fun setReadReceipts(v: Boolean)      = store.setBoolean(SettingsKeys.READ_RECEIPTS, v)
    suspend fun setTypingIndicators(v: Boolean)  = store.setBoolean(SettingsKeys.TYPING_INDICATORS, v)
    suspend fun setOnlineStatus(v: Boolean)      = store.setBoolean(SettingsKeys.ONLINE_STATUS, v)
    suspend fun setLastSeen(v: Boolean)          = store.setBoolean(SettingsKeys.LAST_SEEN, v)
    suspend fun setScreenshotBlock(v: Boolean)   = store.setBoolean(SettingsKeys.SCREENSHOT_BLOCK, v)
    suspend fun setDisappearingMsgs(v: Boolean)  = store.setBoolean(SettingsKeys.DISAPPEARING_MSGS, v)
    suspend fun setMsgTimerSeconds(v: Int)       = store.setInt(SettingsKeys.MSG_TIMER_SECONDS, v)
    suspend fun setAutoDownloadMedia(v: Boolean) = store.setBoolean(SettingsKeys.AUTO_DOWNLOAD_MEDIA, v)
    suspend fun setSaveToGallery(v: Boolean)     = store.setBoolean(SettingsKeys.SAVE_TO_GALLERY, v)
    suspend fun setFontSize(v: FontSizeOption)   = store.setString(SettingsKeys.FONT_SIZE, v.name)
    suspend fun setEnterIsSend(v: Boolean)       = store.setBoolean(SettingsKeys.ENTER_IS_SEND, v)
    suspend fun setArchiveBehavior(v: ArchiveBehavior) = store.setString(SettingsKeys.ARCHIVE_BEHAVIOR, v.name)

    // ── Call toggles ─────────────────────────────────────────
    suspend fun setLowBandwidth(v: Boolean)      = store.setBoolean(SettingsKeys.LOW_BANDWIDTH, v)
    suspend fun setHdVideo(v: Boolean)           = store.setBoolean(SettingsKeys.HD_VIDEO, v)
    suspend fun setEchoCancel(v: Boolean)        = store.setBoolean(SettingsKeys.ECHO_CANCEL, v)
    suspend fun setNoiseSuppress(v: Boolean)     = store.setBoolean(SettingsKeys.NOISE_SUPPRESS, v)
    suspend fun setAutoSpeaker(v: Boolean)       = store.setBoolean(SettingsKeys.AUTO_SPEAKER, v)
    suspend fun setCallNotif(v: Boolean)         = store.setBoolean(SettingsKeys.CALL_NOTIF, v)
    suspend fun setCallVibrate(v: Boolean)       = store.setBoolean(SettingsKeys.CALL_VIBRATE, v)
    suspend fun setMuteUnknown(v: Boolean)       = store.setBoolean(SettingsKeys.MUTE_UNKNOWN, v)

    // ── Contact toggles ──────────────────────────────────────
    suspend fun setSyncDeviceContacts(v: Boolean)= store.setBoolean(SettingsKeys.SYNC_DEVICE_CONTACTS, v)
    suspend fun setSyncSimContacts(v: Boolean)   = store.setBoolean(SettingsKeys.SYNC_SIM_CONTACTS, v)
    suspend fun setShowOnlyQryptin(v: Boolean)   = store.setBoolean(SettingsKeys.SHOW_ONLY_QRYPTIN, v)
    suspend fun setContactSort(v: ContactSort)   = store.setString(SettingsKeys.CONTACT_SORT, v.name)

    // ── Security toggles ─────────────────────────────────────
    suspend fun setAppLock(v: Boolean)           = store.setBoolean(SettingsKeys.APP_LOCK, v)
    suspend fun setBiometricLock(v: Boolean)     = store.setBoolean(SettingsKeys.BIOMETRIC_LOCK, v)
    suspend fun setFingerprintUnlock(v: Boolean) = store.setBoolean(SettingsKeys.FINGERPRINT_UNLOCK, v)
    suspend fun setSecureNotif(v: Boolean)       = store.setBoolean(SettingsKeys.SECURE_NOTIF, v)

    // ── Notification toggles ─────────────────────────────────
    suspend fun setMsgNotif(v: Boolean)          = store.setBoolean(SettingsKeys.MSG_NOTIF, v)
    suspend fun setGroupNotif(v: Boolean)        = store.setBoolean(SettingsKeys.GROUP_NOTIF, v)
    suspend fun setPopupNotif(v: Boolean)        = store.setBoolean(SettingsKeys.POPUP_NOTIF, v)
    suspend fun setNotifPreview(v: Boolean)      = store.setBoolean(SettingsKeys.NOTIF_PREVIEW, v)
    suspend fun setNotifVibrate(v: Boolean)      = store.setBoolean(SettingsKeys.NOTIF_VIBRATE, v)

    // ── Storage ──────────────────────────────────────────────
    suspend fun setAutoDeleteMedia(v: Boolean)   = store.setBoolean(SettingsKeys.AUTO_DELETE_MEDIA, v)
}
