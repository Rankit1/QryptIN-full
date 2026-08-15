package com.qryptin.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.model.ContactSelectionMode
import com.qryptin.chat.model.ContactSelectionUiState
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.repository.ContactsRepository
import com.qryptin.core.search.SmartSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  ContactSelectionViewModel
//  Generic contact picker backing ContactSelectionScreen — used
//  wherever the chat module needs a contact (or several), e.g.
//  starting a new direct chat, or sharing a contact as an
//  attachment. Single vs. multi selection is set via setMode.
// ─────────────────────────────────────────────────────────────
class ContactSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsModule.provideContactsRepository(application)

    private val _uiState = MutableStateFlow(ContactSelectionUiState())
    val uiState: StateFlow<ContactSelectionUiState> = _uiState.asStateFlow()

    private var allContacts: List<Contact> = emptyList()

    init {
        viewModelScope.launch {
            repository.observeContacts().collectLatest { contacts ->
                allContacts = contacts.filter { it.isOnQryptIN }.sortedBy { it.displayName.lowercase() }
                applySearch()
            }
        }
    }

    fun setMode(mode: ContactSelectionMode) = _uiState.update { it.copy(mode = mode) }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applySearch()
    }

    private fun applySearch() {
        val query = _uiState.value.searchQuery.trim()
        val filtered = if (query.isBlank()) allContacts
                       else allContacts.filter { SmartSearch.matches(query, it.displayName, it.phone) }
        _uiState.update { it.copy(availableContacts = filtered, isLoading = false) }
    }

    fun onContactToggled(contact: Contact) {
        _uiState.update { state ->
            val selected = when (state.mode) {
                ContactSelectionMode.SINGLE -> listOf(contact)
                ContactSelectionMode.MULTI  ->
                    if (contact in state.selectedContacts) state.selectedContacts - contact
                    else state.selectedContacts + contact
            }
            state.copy(selectedContacts = selected)
        }
    }
}
