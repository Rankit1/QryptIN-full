package com.qryptin.calls.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.repository.ContactsRepository
import com.qryptin.core.search.SmartSearch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

// ─────────────────────────────────────────────────────────────
//  SelectContactForCallViewModel
//
//  Root cause of "Calls module shows No Contacts":
//  CallsNavGraph.callsGraph() declared `contacts: List<Contact> = emptyList()`
//  but QryptINNavHost never passed a real list in — it silently fell back
//  to the empty default every time. SelectContactForCallScreen also took
//  contacts as a static snapshot, so even if something were passed in,
//  newly-added contacts would never show up without a recomposition trigger.
//
//  Fix: pull contacts straight from the single source of truth
//  (ContactsRepository → Room → Flow) inside the Calls feature itself.
//  No more threading a list through three layers of nav graphs.
// ─────────────────────────────────────────────────────────────
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SelectContactForCallViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsModule.provideContactsRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allContacts: StateFlow<List<Contact>> =
        repository.observeContacts()
            .catch { emit(emptyList()) } // failure isolation: a Contacts DB error must not crash Calls
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isLoading: StateFlow<Boolean> =
        allContacts.map { false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val filteredContacts: StateFlow<List<Contact>> =
        combine(
            allContacts,
            _searchQuery.debounce(200).onStart { emit("") },
        ) { contacts, query -> filterContacts(contacts, query) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // ── Smart matching (shared with the Contacts feature's search engine) ─
    private fun filterContacts(contacts: List<Contact>, query: String): List<Contact> =
        contacts.filter { SmartSearch.matches(query, it.displayName, it.phone) }
}
