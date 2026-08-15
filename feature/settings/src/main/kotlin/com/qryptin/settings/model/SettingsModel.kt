package com.qryptin.settings.model

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class MediaAutoDownload { WIFI_ONLY, ALWAYS, NEVER }

data class MessagingSettings(
    val readReceipts        : Boolean = true,
    val typingIndicators    : Boolean = true,
    val enterToSend         : Boolean = false,
    val mediaAutoDownload   : MediaAutoDownload = MediaAutoDownload.WIFI_ONLY,
)

data class CallSettings(
    val ringtoneEnabled      : Boolean = true,
    val vibrateOnRing        : Boolean = true,
    val lowDataMode          : Boolean = false,
    val callWaiting          : Boolean = true,
)

data class PrivacySettings(
    val lastSeenVisible      : Boolean = true,
    val profilePhotoVisible  : Boolean = true,
    val readReceiptsLinked   : Boolean = true,
    val screenSecurityLock   : Boolean = false,
    val blockedContactsCount : Int     = 0,
)

data class StorageSettings(
    val autoDeleteOldMedia   : Boolean = false,
    val mediaCacheLimitMb    : Int     = 2048,
)

data class NotificationSettings(
    val messageNotifications : Boolean = true,
    val callNotifications    : Boolean = true,
    val groupNotifications   : Boolean = true,
    val notificationSound    : Boolean = true,
    val notificationPreview  : Boolean = true,
)

data class SettingsUiState(
    val themeMode      : ThemeMode           = ThemeMode.LIGHT,
    val messaging      : MessagingSettings    = MessagingSettings(),
    val calls          : CallSettings         = CallSettings(),
    val privacy        : PrivacySettings      = PrivacySettings(),
    val storage         : StorageSettings      = StorageSettings(),
    val notifications  : NotificationSettings = NotificationSettings(),
    val isLoading      : Boolean              = true,
)

/** Shown on the Privacy > Active Sessions sub-screen. */
data class ActiveSession(
    val id          : String,
    val deviceName  : String,
    val lastActive  : String,
    val isCurrent   : Boolean,
)

/** Shown on the Privacy > PQ Status sub-screen. */
data class PqStatus(
    val keyExchangeAlgorithm : String = "CRYSTALS-Kyber-1024",
    val signatureAlgorithm   : String = "CRYSTALS-Dilithium-5",
    val keysRotatedAt        : Long   = 0L,
    val isActive             : Boolean = true,
)
