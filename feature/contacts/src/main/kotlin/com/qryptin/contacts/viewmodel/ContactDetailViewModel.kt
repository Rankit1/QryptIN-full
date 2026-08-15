package com.qryptin.contacts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.repository.ContactDetail
import com.qryptin.contacts.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  ContactDetailViewModel
//  Loads a single saved contact's full detail (registry name,
//  nickname, email, save mode, invite method) for the
//  read-only Contact Detail screen.
// ─────────────────────────────────────────────────────────────
class ContactDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsModule.provideContactsRepository(application)

    private val _contact = MutableStateFlow<ContactDetail?>(null)
    val contact: StateFlow<ContactDetail?> = _contact.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load(contactId: String) {
        val id = contactId.toLongOrNull() ?: run {
            _isLoading.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _contact.value   = repository.getContactDetail(id)
            _isLoading.value = false
        }
    }
}
