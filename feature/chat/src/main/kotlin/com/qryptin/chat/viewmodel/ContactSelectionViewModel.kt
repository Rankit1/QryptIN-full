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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  ContactSelectionViewModel
//  Generic contact picker backing ContactSelectionScreen — used
//  wherever the chat module needs a contact (or several), e.g.
//  starting a new direct chat, or sharing a contact as an
//  attachment. Single vs. multi selection is set via setMode.
// ─────────────────────────────────────────────────────────────
@OptIn(FlowPreview::class)
class ContactSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsModule.provideContactsRepository(application)

    /** Immediate, ungated query — this is what the TextField is bound to. */
    private val rawSearchQuery = MutableStateFlow("")

    private val _mode = MutableStateFlow(ContactSelectionMode.SINGLE)
    private val _selectedContacts = MutableStateFlow(emptyList<Contact>())
    private val _isLoading = MutableStateFlow(true)

    private val allContacts: StateFlow<List<Contact>> =
        repository.observeContacts()
            .map { contacts -> contacts.filter { it.isOnQryptIN }.sortedBy { it.displayName.lowercase() } }
            .onEach { _isLoading.value = false }
            .catch { _isLoading.value = false; emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Debounced *only* for the filtering pass — never bound to the TextField directly. */
    private val debouncedQueryForFiltering: Flow<String> =
        rawSearchQuery.debounce(200).onStart { emit(rawSearchQuery.value) }

    val uiState: StateFlow<ContactSelectionUiState> = combine(
        allContacts,
        _mode,
        _selectedContacts,
        rawSearchQuery,
        debouncedQueryForFiltering,
        _isLoading,
    ) { args: Array<Any> ->
        val contacts = args[0] as List<Contact>
        val mode = args[1] as ContactSelectionMode
        val selected = args[2] as List<Contact>
        val displayedQuery = args[3] as String
        val filterQuery = args[4] as String
        val loading = args[5] as Boolean

        val query = filterQuery.trim()
        val filtered = if (query.isBlank()) contacts
                       else contacts.filter { SmartSearch.matches(query, it.displayName, it.phone) }

        ContactSelectionUiState(
            mode              = mode,
            availableContacts = filtered,
            selectedContacts  = selected,
            searchQuery       = displayedQuery,
            isLoading         = loading,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContactSelectionUiState(isLoading = true))

    fun setMode(mode: ContactSelectionMode) {
        _mode.value = mode
    }

    fun onSearchQueryChanged(query: String) {
        rawSearchQuery.value = query
    }

    fun onContactToggled(contact: Contact) {
        _selectedContacts.update { currentSelected ->
            when (_mode.value) {
                ContactSelectionMode.SINGLE -> listOf(contact)
                ContactSelectionMode.MULTI  ->
                    if (contact in currentSelected) currentSelected - contact
                    else currentSelected + contact
            }
        }
    }
}
