package com.qryptin.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.settings.data.SettingsRepository
import com.qryptin.settings.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.themeMode,
        repository.messaging,
        repository.calls,
        repository.privacy,
        repository.storage,
        repository.notifications,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettingsUiState(
            themeMode     = values[0] as ThemeMode,
            messaging     = values[1] as MessagingSettings,
            calls         = values[2] as CallSettings,
            privacy       = values[3] as PrivacySettings,
            storage       = values[4] as StorageSettings,
            notifications = values[5] as NotificationSettings,
            isLoading     = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState(isLoading = true))

    fun setThemeMode(mode: ThemeMode)              = viewModelScope.launch { repository.setThemeMode(mode) }

    fun setReadReceipts(enabled: Boolean)          = viewModelScope.launch { repository.setReadReceipts(enabled) }
    fun setTypingIndicators(enabled: Boolean)      = viewModelScope.launch { repository.setTypingIndicators(enabled) }
    fun setEnterToSend(enabled: Boolean)           = viewModelScope.launch { repository.setEnterToSend(enabled) }
    fun setMediaAutoDownload(mode: MediaAutoDownload) = viewModelScope.launch { repository.setMediaAutoDownload(mode) }

    fun setRingtoneEnabled(enabled: Boolean)       = viewModelScope.launch { repository.setRingtoneEnabled(enabled) }
    fun setVibrateOnRing(enabled: Boolean)         = viewModelScope.launch { repository.setVibrateOnRing(enabled) }
    fun setLowDataMode(enabled: Boolean)           = viewModelScope.launch { repository.setLowDataMode(enabled) }
    fun setCallWaiting(enabled: Boolean)           = viewModelScope.launch { repository.setCallWaiting(enabled) }

    fun setLastSeenVisible(enabled: Boolean)       = viewModelScope.launch { repository.setLastSeenVisible(enabled) }
    fun setProfilePhotoVisible(enabled: Boolean)   = viewModelScope.launch { repository.setProfilePhotoVisible(enabled) }
    fun setReadReceiptsLinked(enabled: Boolean)    = viewModelScope.launch { repository.setReadReceiptsLinked(enabled) }
    fun setScreenSecurityLock(enabled: Boolean)    = viewModelScope.launch { repository.setScreenSecurityLock(enabled) }

    fun setAutoDeleteOldMedia(enabled: Boolean)    = viewModelScope.launch { repository.setAutoDeleteOldMedia(enabled) }
    fun setMediaCacheLimitMb(limit: Int)           = viewModelScope.launch { repository.setMediaCacheLimitMb(limit) }

    fun setMessageNotifications(enabled: Boolean)  = viewModelScope.launch { repository.setMessageNotifications(enabled) }
    fun setCallNotifications(enabled: Boolean)     = viewModelScope.launch { repository.setCallNotifications(enabled) }
    fun setGroupNotifications(enabled: Boolean)    = viewModelScope.launch { repository.setGroupNotifications(enabled) }
    fun setNotificationSound(enabled: Boolean)     = viewModelScope.launch { repository.setNotificationSound(enabled) }
    fun setNotificationPreview(enabled: Boolean)   = viewModelScope.launch { repository.setNotificationPreview(enabled) }
}
