package com.qryptin.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.ChatModule
import com.qryptin.chat.model.Chat
import com.qryptin.chat.model.ChatListFilter
import com.qryptin.chat.model.ChatListUiState
import com.qryptin.chat.repository.ChatRepository
import com.qryptin.core.search.SmartSearch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  ChatListViewModel
//  Uses ChatRepository — all data persists locally and syncs with backend.
// ─────────────────────────────────────────────────────────────
@OptIn(FlowPreview::class)
class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository = ChatModule.provideChatRepository(application)

    /** Immediate, ungated query — this is what the TextField is bound to. */
    private val rawSearchQuery = MutableStateFlow("")

    private val _selectedFilter = MutableStateFlow(ChatListFilter.ALL)
    private val _isLoading = MutableStateFlow(true)
    private val _isFabMenuOpen = MutableStateFlow(false)
    private val _selectedChatIds = MutableStateFlow(emptySet<String>())

    private val allChats: StateFlow<List<Chat>> =
        repository.observeChats()
            .onEach { _isLoading.value = false }
            .catch { _isLoading.value = false; emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Debounced *only* for the filtering pass — never bound to the TextField directly. */
    private val debouncedQueryForFiltering: Flow<String> =
        rawSearchQuery.debounce(200).onStart { emit(rawSearchQuery.value) }

    val uiState: StateFlow<ChatListUiState> = combine(
        allChats,
        _selectedFilter,
        rawSearchQuery,
        debouncedQueryForFiltering,
        _isLoading,
        _isFabMenuOpen,
        _selectedChatIds
    ) { args: Array<Any> ->
        val chats = args[0] as List<Chat>
        val filter = args[1] as ChatListFilter
        val displayedQuery = args[2] as String
        val filterQuery = args[3] as String
        val loading = args[4] as Boolean
        val fabOpen = args[5] as Boolean
        val selectedIds = args[6] as Set<String>

        val query = filterQuery.trim()
        val filtered = chats
            .filterNot { it.isArchived }
            .filter { chat ->
                when (filter) {
                    ChatListFilter.ALL    -> true
                    ChatListFilter.UNREAD -> chat.unreadCount > 0
                    ChatListFilter.GROUPS -> chat.isGroup
                    ChatListFilter.PINNED -> chat.isPinned
                }
            }
            .filter { chat ->
                query.isBlank() || SmartSearch.matches(query, chat.title) ||
                        chat.lastMessage?.text?.contains(query, ignoreCase = true) == true
            }

        ChatListUiState(
            chats          = filtered,
            searchQuery    = displayedQuery,
            selectedFilter = filter,
            isLoading      = loading,
            isFabMenuOpen  = fabOpen,
            selectedChatIds = selectedIds,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatListUiState(isLoading = true))

    fun onSearchQueryChanged(query: String) {
        rawSearchQuery.value = query
    }

    fun onFilterSelected(filter: ChatListFilter) {
        _selectedFilter.value = filter
    }

    // ── FAB menu ───────────────────────────────────────────────

    fun onFabClick()     = _isFabMenuOpen.update { true }
    fun dismissFabMenu() = _isFabMenuOpen.update { false }

    // ── Long-press multi-select + swipe actions ────────────────

    fun onChatLongPressed(chatId: String) {
        _selectedChatIds.update { it + chatId }
    }

    fun onChatSelectionToggled(chatId: String) {
        _selectedChatIds.update { if (chatId in it) it - chatId else it + chatId }
    }

    fun clearSelection() = _selectedChatIds.update { emptySet() }

    fun togglePin(chatId: String)   = viewModelScope.launch { repository.togglePin(chatId) }
    fun toggleMute(chatId: String)  = viewModelScope.launch { repository.toggleMute(chatId) }
    fun archiveChat(chatId: String) = viewModelScope.launch { repository.toggleArchive(chatId) }
    fun deleteChat(chatId: String)  = viewModelScope.launch { repository.deleteChatLocally(chatId) }

    fun applyBulkAction(action: (String) -> Unit) {
        _selectedChatIds.value.forEach(action)
        clearSelection()
    }
}
