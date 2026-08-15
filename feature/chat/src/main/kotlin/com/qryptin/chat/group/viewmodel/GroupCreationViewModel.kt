package com.qryptin.chat.group.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.ChatModule
import com.qryptin.chat.data.repository.RoomChatRepository
import com.qryptin.chat.data.repository.RoomGroupRepository
import com.qryptin.chat.group.model.GroupPermissions
import com.qryptin.chat.group.model.GroupUiState
import com.qryptin.chat.group.repository.GroupRepository
import com.qryptin.chat.repository.ChatRepository
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.repository.ContactsRepository
import com.qryptin.core.search.SmartSearch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  GroupCreationViewModel
//  Uses RoomGroupRepository — groups persist across restarts.
//  When a group is created, a ConversationEntity is also written
//  so it appears in the home chat list immediately.
// ─────────────────────────────────────────────────────────────
class GroupCreationViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository    : GroupRepository = RoomGroupRepository(application)
    private val chatRepository     : ChatRepository  = ChatModule.provideChatRepository(application)
    private val contactsRepository                   = ContactsModule.provideContactsRepository(application)

    private val _uiState   = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val _navEvent  = MutableSharedFlow<GroupNavEvent>()
    val navEvent: SharedFlow<GroupNavEvent> = _navEvent

    private var allContacts: List<Contact> = emptyList()

    init {
        viewModelScope.launch {
            contactsRepository.observeContacts().collectLatest { contacts ->
                allContacts = contacts.filter { it.isOnQryptIN }
                _uiState.update { it.copy(availableContacts = filtered(it.searchQuery)) }
            }
        }
    }

    // ── Screen 1: Select Participants ────────────────────────

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, availableContacts = filtered(query)) }
    }

    private fun filtered(query: String): List<Contact> {
        val q = query.trim()
        return if (q.isBlank()) allContacts
               else allContacts.filter { SmartSearch.matches(q, it.displayName, it.phone) }
    }

    fun onContactToggled(contact: Contact) {
        _uiState.update { state ->
            val updated = if (contact in state.selectedMembers)
                              state.selectedMembers - contact
                          else
                              state.selectedMembers + contact
            state.copy(selectedMembers = updated)
        }
    }

    fun onRemoveMember(contact: Contact) {
        _uiState.update { it.copy(selectedMembers = it.selectedMembers - contact) }
    }

    // ── Screen 2: Group Details ──────────────────────────────

    fun onGroupNameChanged(name: String) {
        if (name.length <= 50) _uiState.update { it.copy(groupName = name) }
    }

    fun onGroupDescriptionChanged(desc: String) {
        if (desc.length <= 120) _uiState.update { it.copy(groupDescription = desc) }
    }

    fun onGroupPhotoPicked(uri: String?) {
        _uiState.update { it.copy(groupPhotoUri = uri) }
    }

    // ── Screen 3: Permissions ────────────────────────────────

    fun onPermissionChanged(updated: GroupPermissions) {
        _uiState.update { it.copy(permissions = updated) }
    }

    // ── Create ───────────────────────────────────────────────

    fun createGroup() {
        val state = _uiState.value
        if (state.isCreating || state.selectedMembers.isEmpty()) return

        _uiState.update { it.copy(isCreating = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                groupRepository.createGroup(
                    name        = state.groupName.trim(),
                    description = state.groupDescription.trim(),
                    photoUri    = state.groupPhotoUri,
                    members     = state.selectedMembers,
                    permissions = state.permissions,
                    createdBy   = "me",
                )
            }.onSuccess { group ->
                _uiState.update {
                    it.copy(
                        isCreating       = false,
                        createdGroupId   = group.groupId,
                        createdGroupName = group.groupName,
                    )
                }
                _navEvent.emit(GroupNavEvent.NavigateToSuccess)
            }.onFailure { err ->
                _uiState.update { it.copy(isCreating = false, errorMessage = err.message) }
            }
        }
    }

    fun onOpenGroupClicked() {
        val chatId = _uiState.value.createdGroupId ?: return
        viewModelScope.launch {
            _navEvent.emit(GroupNavEvent.NavigateToConversation(chatId))
        }
    }
}

sealed interface GroupNavEvent {
    data object NavigateToSuccess : GroupNavEvent
    data class NavigateToConversation(val chatId: String) : GroupNavEvent
}
