package com.qryptin.contacts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.auth.model.CountryCode
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.model.AddContactUiState
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.model.InviteMethod
import com.qryptin.contacts.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  Navigation events emitted as one-shot SharedFlow
// ─────────────────────────────────────────────────────────────
sealed class AddContactNavEvent {
    /** Number found in QryptIN → navigate to existing-user detail screen. */
    object NavigateToExistingContact : AddContactNavEvent()
    /** Number NOT found → navigate to non-QryptIN contact detail screen. */
    object NavigateToNonQryptINContact : AddContactNavEvent()
    /** Details filled (either flow) → navigate to the appropriate review screen. */
    object NavigateToReview : AddContactNavEvent()
    /** Non-QryptIN details filled → navigate to non-QryptIN review screen. */
    object NavigateToNonQryptINReview : AddContactNavEvent()
    /** Saved → navigate to success screen. */
    object NavigateToSuccess : AddContactNavEvent()
    /** User pressed "View Contact" on success screen → pop back to contacts list. */
    object NavigateToContactsList : AddContactNavEvent()
    /** Non-QryptIN contact saved → open the system SMS/Email app with the invite pre-filled. */
    data class LaunchInvite(
        val method : InviteMethod,
        val phone  : String,
        val email  : String,
    ) : AddContactNavEvent()
}

// ─────────────────────────────────────────────────────────────
//  AddContactViewModel
// ─────────────────────────────────────────────────────────────
class AddContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsModule.provideContactsRepository(application)

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<AddContactNavEvent>()
    val navEvent = _navEvent.asSharedFlow()

    // ── Computed helpers ─────────────────────────────────────

    /** True when enough digits have been entered for the selected country. */
    val isPhoneValid: Boolean
        get() {
            val digits = _uiState.value.phoneNumber.filter { it.isDigit() }
            return digits.length >= 7
        }

    /** True when email invite method can be selected (email must be provided). */
    val isEmailInviteEnabled: Boolean
        get() = _uiState.value.email.isNotBlank()

    // ─────────────────────────────────────────────────────────
    //  Screen 1 — Phone entry
    // ─────────────────────────────────────────────────────────

    fun onCountrySelected(country: CountryCode) =
        _uiState.update { it.copy(selectedCountry = country) }

    fun onPhoneNumberChanged(value: String) {
        val sanitised = value.filter { it.isDigit() || it == ' ' || it == '-' }
        _uiState.update { it.copy(phoneNumber = sanitised, lookupError = null) }
    }

    fun onKeypadDigit(digit: String) {
        val current = _uiState.value.phoneNumber
        if (current.filter { it.isDigit() }.length >= 15) return
        _uiState.update { it.copy(phoneNumber = current + digit, lookupError = null) }
    }

    fun onKeypadBackspace() {
        val current = _uiState.value.phoneNumber
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(phoneNumber = current.dropLast(1), lookupError = null) }
        }
    }

    /**
     * Triggered by the "Next" button on Screen 1.
     * Looks up the FULL international number (country code + digits)
     * against the QryptIN global registry (mock_users.json) and routes
     * to the correct flow based on the result.
     */
    fun onPhoneLookup() {
        if (!isPhoneValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUp = true, lookupError = null) }
            val state           = _uiState.value
            
            // Standardize format to +XXXXXXXXXXXX (e.g. +918777014529) to match backend
            val dial = state.selectedCountry.dial
            val digits = state.phoneNumber.filter { it.isDigit() }
            val fullPhone = "$dial$digits"
            
            val match = repository.lookupQryptUser(fullPhone)
            if (match != null) {
                // ── Existing QryptIN user ──────────────────────
                _uiState.update {
                    it.copy(
                        isLookingUp          = false,
                        friendId             = match.id,
                        serverName           = match.name,
                        displayName          = match.name,
                        email                = match.email,
                        lookupError          = null,
                        isExistingQryptINUser = true,
                        isNonQryptINUser     = false,
                    )
                }
                _navEvent.emit(AddContactNavEvent.NavigateToExistingContact)
            } else {
                // ── Non-QryptIN user ───────────────────────────
                // Show inline alert on the phone screen first,
                // then the user clicks Next to proceed.
                _uiState.update {
                    it.copy(
                        isLookingUp          = false,
                        isNonQryptINUser     = true,
                        isExistingQryptINUser = false,
                        displayName          = "",    // no auto-fetch
                        serverName           = "",
                        lookupError          = null,  // not an error — handled by alert card
                    )
                }
                // Do NOT navigate yet — the phone screen shows the alert card.
                // Navigation happens when the user explicitly clicks Next.
            }
        }
    }

    /**
     * Called when the user clicks Next on the phone screen AFTER seeing
     * the "not found" alert card. Navigates to AddNonQryptINContactScreen.
     */
    fun onProceedToNonQryptINContact() {
        viewModelScope.launch {
            _navEvent.emit(AddContactNavEvent.NavigateToNonQryptINContact)
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Screen 2a — Contact details (existing QryptIN user)
    // ─────────────────────────────────────────────────────────

    fun onEditNameToggle() =
        _uiState.update { it.copy(isEditingName = !it.isEditingName, isNicknameMode = false) }

    fun onNicknameModeToggle() =
        _uiState.update { it.copy(isNicknameMode = !it.isNicknameMode, isEditingName = false) }

    fun onDisplayNameChanged(value: String) =
        _uiState.update { it.copy(displayName = value) }

    fun onSaveOptionSelected(option: ContactSaveOption) =
        _uiState.update { it.copy(saveOption = option) }

    fun onEmailChanged(value: String) {
        _uiState.update {
            // If email is cleared and EMAIL is selected, revert to SMS
            val updatedEmail = value
            val updatedMethod = if (updatedEmail.isBlank() && it.inviteMethod == InviteMethod.EMAIL)
                InviteMethod.SMS
            else
                it.inviteMethod
            it.copy(email = updatedEmail, inviteMethod = updatedMethod)
        }
    }

    /** "Next" on Screen 2a (existing user) → navigate to review. */
    fun onProceedToReview() {
        viewModelScope.launch {
            _navEvent.emit(AddContactNavEvent.NavigateToReview)
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Screen 2b — Contact details (non-QryptIN user)
    // ─────────────────────────────────────────────────────────

    /** "Next" on Screen 2b (non-QryptIN user) → navigate to non-QryptIN review. */
    fun onProceedToNonQryptINReview() {
        viewModelScope.launch {
            _navEvent.emit(AddContactNavEvent.NavigateToNonQryptINReview)
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Invite method selection (non-QryptIN review screen)
    // ─────────────────────────────────────────────────────────

    fun onInviteMethodSelected(method: InviteMethod) {
        // Guard: do not allow EMAIL if email is blank
        if (method == InviteMethod.EMAIL && _uiState.value.email.isBlank()) return
        _uiState.update { it.copy(inviteMethod = method) }
    }

    // ─────────────────────────────────────────────────────────
    //  Screen 3a — Review & Save (existing QryptIN user)
    // ─────────────────────────────────────────────────────────

    fun onSaveContact() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            val state     = _uiState.value
            
            // Standardize format to +XXXXXXXXXXXX (e.g. +918777014529) to match backend
            val dial = state.selectedCountry.dial
            val digits = state.phoneNumber.filter { it.isDigit() }
            val fullPhone = "$dial$digits"
            
            val success   = repository.saveExistingQryptContact(
                friendId          = state.friendId,
                phone             = fullPhone,
                originalQryptName = state.serverName,
                effectiveName     = state.displayName.ifBlank { state.serverName },
                email             = state.email,
                saveOption        = state.saveOption,
            )
            if (success) {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
                _navEvent.emit(AddContactNavEvent.NavigateToSuccess)
            } else {
                _uiState.update {
                    it.copy(isSaving = false, saveError = "Failed to save contact. Please try again.")
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Screen 3b — Review & Save (non-QryptIN user)
    // ─────────────────────────────────────────────────────────

    fun onSaveNonQryptINContact() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            val state     = _uiState.value
            
            // Standardize format to +XXXXXXXXXXXX (e.g. +918777014529) to match backend
            val dial = state.selectedCountry.dial
            val digits = state.phoneNumber.filter { it.isDigit() }
            val fullPhone = "$dial$digits"

            val saved = repository.saveNonQryptContact(
                phone        = fullPhone,
                displayName  = state.displayName,
                email        = state.email,
                saveOption   = state.saveOption,
                inviteMethod = state.inviteMethod,
            )

            if (!saved) {
                _uiState.update {
                    it.copy(isSaving = false, saveError = "Failed to save contact. Please try again.")
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = false, isSaved = true) }

            // Hand off to the UI layer to open the real SMS/Email app —
            // launching an Activity intent belongs at the UI layer, not
            // inside the repository/ViewModel.
            _navEvent.emit(
                AddContactNavEvent.LaunchInvite(
                    method = state.inviteMethod,
                    phone  = fullPhone,
                    email  = state.email,
                )
            )
            _navEvent.emit(AddContactNavEvent.NavigateToSuccess)
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Screen 4 — Success
    // ─────────────────────────────────────────────────────────

    fun onViewContact() {
        viewModelScope.launch {
            _navEvent.emit(AddContactNavEvent.NavigateToContactsList)
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Utility
    // ─────────────────────────────────────────────────────────

    val formattedPhone: String
        get() {
            val dial = _uiState.value.selectedCountry.dial
            val digits = _uiState.value.phoneNumber.filter { it.isDigit() }
            return "$dial$digits"
        }

    val effectiveDisplayName: String
        get() = _uiState.value.displayName.ifBlank { _uiState.value.serverName }

    val avatarInitials: String
        get() = effectiveDisplayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "?" }
}
