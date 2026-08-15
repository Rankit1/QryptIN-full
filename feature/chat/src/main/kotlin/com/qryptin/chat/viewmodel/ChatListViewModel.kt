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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  ChatListViewModel
//  Uses ChatRepository — all data persists locally and syncs with backend.
// ─────────────────────────────────────────────────────────────
class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository = ChatModule.provideChatRepository(application)

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var allChats: List<Chat> = emptyList()

    init {
        observeChats()
    }

    private fun observeChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeChats().collectLatest { chats ->
                allChats = chats
                applyFilters()
            }
        }
    }

    // ── Search + filter ───────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterSelected(filter: ChatListFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.trim()

        val filtered = allChats
            .filterNot { it.isArchived }
            .filter { chat ->
                when (state.selectedFilter) {
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

        _uiState.update { it.copy(chats = filtered, isLoading = false) }
    }

    // ── FAB menu ───────────────────────────────────────────────

    fun onFabClick()     = _uiState.update { it.copy(isFabMenuOpen = true) }
    fun dismissFabMenu() = _uiState.update { it.copy(isFabMenuOpen = false) }

    // ── Long-press multi-select + swipe actions ────────────────

    fun onChatLongPressed(chatId: String) {
        _uiState.update { it.copy(selectedChatIds = it.selectedChatIds + chatId) }
    }

    fun onChatSelectionToggled(chatId: String) {
        _uiState.update { state ->
            val updated = if (chatId in state.selectedChatIds) state.selectedChatIds - chatId
                          else state.selectedChatIds + chatId
            state.copy(selectedChatIds = updated)
        }
    }

    fun clearSelection() = _uiState.update { it.copy(selectedChatIds = emptySet()) }

    fun togglePin(chatId: String)   = viewModelScope.launch { repository.togglePin(chatId) }
    fun toggleMute(chatId: String)  = viewModelScope.launch { repository.toggleMute(chatId) }
    fun archiveChat(chatId: String) = viewModelScope.launch { repository.toggleArchive(chatId) }
    fun deleteChat(chatId: String)  = viewModelScope.launch { repository.deleteChatLocally(chatId) }

    fun applyBulkAction(action: (String) -> Unit) {
        uiState.value.selectedChatIds.forEach(action)
        clearSelection()
    }
}
