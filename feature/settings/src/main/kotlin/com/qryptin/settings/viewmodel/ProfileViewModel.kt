package com.qryptin.settings.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.settings.data.ProfileRepository
import com.qryptin.settings.model.EditProfileUiState
import com.qryptin.settings.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application)

    private val _isSaving = MutableStateFlow(false)
    private val _saveError = MutableStateFlow<String?>(null)
    private val _saveSuccess = MutableStateFlow(false)

    val uiState: StateFlow<EditProfileUiState> = combine(
        repository.profile.catch { emit(Profile()) },
        _isSaving, _saveError, _saveSuccess,
    ) { profile, saving, error, success ->
        EditProfileUiState(profile = profile, isLoading = false, isSaving = saving, saveError = error, saveSuccess = success)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EditProfileUiState(isLoading = true))

    fun saveProfile(displayName: String, username: String, bio: String, email: String) {
        if (displayName.isBlank()) {
            _saveError.value = "Display name can't be empty"
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            val result = repository.updateProfile(displayName, username, bio, email)
            _isSaving.value = false
            result.fold(
                onSuccess = { _saveSuccess.value = true },
                onFailure = { _saveError.value = "Couldn't save your profile. Please try again." },
            )
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            val result = repository.updateAvatar(uri)
            if (result.isFailure) {
                _saveError.value = "Couldn't update your profile photo. Please try again."
            }
        }
    }

    fun consumeSaveSuccess() { _saveSuccess.value = false }
    fun dismissError() { _saveError.value = null }
}
