package com.qryptin.contacts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.contacts.repository.ContactDetail
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  EditContactViewModel
//  Issue 3 fix: contacts previously had no way to edit their
//  nickname, phone number, or email after being saved. This
//  loads the existing contact, lets the user change those
//  fields, and persists the update to Room via ContactsRepository.
//  ContactsRepository.observeContacts() is a Flow, so the
//  contacts list and detail screen recompose automatically once
//  the update lands — no manual refresh needed anywhere.
// ─────────────────────────────────────────────────────────────
class EditContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsModule.provideContactsRepository(application)

    private val _contact = MutableStateFlow<ContactDetail?>(null)
    val contact: StateFlow<ContactDetail?> = _contact.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private var contactId: Long? = null

    fun load(id: String) {
        val parsedId = id.toLongOrNull()
        contactId = parsedId
        if (parsedId == null) {
            _isLoading.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _contact.value   = repository.getContactDetail(parsedId)
            _isLoading.value = false
        }
    }

    fun save(nickname: String, phone: String, email: String) {
        val id = contactId
        if (id == null) {
            _saveError.value = "Contact not found"
            return
        }
        if (phone.isBlank()) {
            _saveError.value = "Phone number can't be empty"
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            val success = repository.updateContact(
                id       = id,
                nickname = nickname,
                phone    = phone,
                email    = email,
            )
            _isSaving.value = false
            if (success) {
                _contact.value = repository.getContactDetail(id)
                _saveSuccess.value = true
            } else {
                _saveError.value = "Couldn't update contact. Please try again."
            }
        }
    }

    fun consumeSaveSuccess() { _saveSuccess.value = false }
    fun dismissError() { _saveError.value = null }
}
