package com.qryptin.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.ChatModule
import com.qryptin.chat.model.Attachment
import com.qryptin.chat.model.GroupCreationUiState
import com.qryptin.chat.repository.ChatRepository
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.repository.ContactsRepository
import com.qryptin.core.search.SmartSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  GroupCreationViewModel
//  Pulls the existing saved-contact list from feature:contacts'
//  ContactsRepository (Room-backed) for selection, then hands
//  off to ChatRepository.createGroupChat on confirm.
// ─────────────────────────────────────────────────────────────
class GroupCreationViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository: ChatRepository = ChatModule.provideChatRepository(application)
    private val contactsRepository = ContactsModule.provideContactsRepository(application)

    private val _uiState = MutableStateFlow(GroupCreationUiState())
    val uiState: StateFlow<GroupCreationUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<GroupCreationNavEvent>()
    val navEvent: SharedFlow<GroupCreationNavEvent> = _navEvent

    private var allContacts: List<Contact> = emptyList()

    init {
        viewModelScope.launch {
            contactsRepository.observeContacts().collectLatest { contacts ->
                allContacts = contacts.filter { it.isOnQryptIN }
                applySearch()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applySearch()
    }

    private fun applySearch() {
        val query = _uiState.value.searchQuery.trim()
        val filtered = if (query.isBlank()) allContacts
                       else allContacts.filter { SmartSearch.matches(query, it.displayName, it.phone) }
        _uiState.update { it.copy(availableContacts = filtered) }
    }

    fun onContactToggled(contact: Contact) {
        _uiState.update { state ->
            val selected = if (contact in state.selectedContacts) state.selectedContacts - contact
                            else state.selectedContacts + contact
            state.copy(selectedContacts = selected)
        }
    }

    fun onGroupNameChanged(name: String) = _uiState.update { it.copy(groupName = name) }

    fun onGroupAvatarPicked(uri: String?) = _uiState.update { it.copy(groupAvatarUri = uri) }

    fun createGroup() {
        val state = _uiState.value
        if (!state.canCreate) return

        _uiState.update { it.copy(isCreating = true, errorMessage = null) }
        viewModelScope.launch {
            val chat = chatRepository.createGroupChat(
                name           = state.groupName.trim(),
                participantIds = state.selectedContacts.map { it.id },
                avatarUri      = state.groupAvatarUri,
            )
            _uiState.update { it.copy(isCreating = false) }
            _navEvent.emit(GroupCreationNavEvent.NavigateToConversation(chat.id))
        }
    }
}

sealed interface GroupCreationNavEvent {
    data class NavigateToConversation(val chatId: String) : GroupCreationNavEvent
}
