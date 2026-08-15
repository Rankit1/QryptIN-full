package com.qryptin.contacts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.model.*
import com.qryptin.contacts.repository.ContactsRepository
import com.qryptin.core.search.SmartSearch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

// ─────────────────────────────────────────────────────────────
//  ContactsViewModel
//  Single source of truth for the Contacts screen. Backed by
//  ContactsRepository (Room), so saved contacts persist across
//  app restarts and the list updates live as contacts are added.
//
//  IMPORTANT — search field binding bug fix:
//  The TextField's `value` must always reflect exactly what the
//  user typed, with zero delay. Only the *filtering* should be
//  debounced. Previously this ViewModel exposed a single
//  `searchQuery` inside uiState that was itself the debounced
//  value — so the TextField was bound to a value that lagged
//  behind every keystroke. That's what produced "typing doesn't
//  show up" and "deleting a letter looks like it shifts instead
//  of removing": the box was always rendering the *previous*
//  debounced state, not what was actually on screen a moment ago.
//
//  Fix: `rawSearchQuery` is the immediate, ungated value the
//  TextField binds to. It updates synchronously on every
//  keystroke. A *separate* debounced derivative of the same flow
//  is used only to drive the (potentially expensive) filtering
//  pass over the contact list — it never feeds back into what's
//  displayed in the input field.
// ─────────────────────────────────────────────────────────────
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsModule.provideContactsRepository(application)

    /** Immediate, ungated query — this is what the TextField is bound to. */
    private val rawSearchQuery = MutableStateFlow("")

    private val _selectedTab = MutableStateFlow(ContactTab.ALL)
    private val _isLoading = MutableStateFlow(true)

    private val allContacts: StateFlow<List<Contact>> =
        repository.observeContacts()
            .map { contacts -> contacts.sortedBy { it.displayName.lowercase() } }
            .onEach { _isLoading.value = false }
            .catch { _isLoading.value = false; emit(emptyList()) } // failure isolation
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Debounced *only* for the filtering pass — never bound to the TextField directly. */
    private val debouncedQueryForFiltering: Flow<String> =
        rawSearchQuery.debounce(200).onStart { emit(rawSearchQuery.value) }

    val uiState: StateFlow<ContactsUiState> = combine(
        allContacts,
        _selectedTab,
        rawSearchQuery,               // <- drives what's displayed in the search box
        debouncedQueryForFiltering,    // <- drives what's actually filtered
        _isLoading,
    ) { contacts, tab, displayedQuery, filterQuery, loading ->
        val filtered = contacts
            .filter { contact ->
                when (tab) {
                    ContactTab.ALL     -> true
                    ContactTab.QRYPTIN -> contact.isOnQryptIN
                    ContactTab.INVITE  -> !contact.isOnQryptIN
                }
            }
            .filter { contact -> SmartSearch.matches(filterQuery, contact.displayName, contact.phone) }

        ContactsUiState(
            contacts     = filtered,
            isLoading    = loading,
            searchQuery  = displayedQuery,   // always the exact, immediate text the user typed
            selectedTab  = tab,
            errorMessage = null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContactsUiState(isLoading = true))

    val qryptinCount: Int get() = allContacts.value.count { it.isOnQryptIN }
    val inviteCount : Int get() = allContacts.value.count { !it.isOnQryptIN }

    fun refresh() { /* allContacts is a live Flow already; kept for API compatibility */ }

    // ─────────────────────────────────────────────────────────
    //  User actions
    // ─────────────────────────────────────────────────────────
    fun onSearchQueryChanged(query: String) {
        // Updates synchronously — every keystroke is reflected immediately.
        rawSearchQuery.value = query
    }

    fun onTabSelected(tab: ContactTab) {
        _selectedTab.value = tab
    }

    fun clearSearch() {
        rawSearchQuery.value = ""
    }

    // ─────────────────────────────────────────────────────────
    //  Derived helpers for UI
    // ─────────────────────────────────────────────────────────

    /** Groups the current filtered list by first letter, preserving sort. */
    fun groupedContacts(): Map<Char, List<Contact>> =
        uiState.value.contacts
            .sortedBy { it.displayName.lowercase() }
            .groupBy { it.indexChar }

    /** Ordered list of index characters for the scroll bar. */
    fun indexLetters(): List<Char> = groupedContacts().keys.toList()
}
