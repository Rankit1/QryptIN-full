package com.qryptin.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.chat.ChatModule
import com.qryptin.chat.model.ChatSearchResult
import com.qryptin.chat.model.ChatSearchScope
import com.qryptin.chat.model.ChatSearchUiState
import com.qryptin.chat.repository.ChatRepository
import com.qryptin.core.search.SmartSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  ChatSearchViewModel
//  Unified search across chat titles and message text. Live
//  filtering as the query changes, scoped by ChatSearchScope.
//  File-attachment search reuses the same message scan, keyed
//  on attachment file names.
//  Issue 8 fix: chat-title matching now goes through SmartSearch
//  (same fuzzy/partial/case-insensitive engine as contacts and
//  calls) instead of a strict contains() check. Message/file text
//  search stays a plain substring match — free-text content isn't
//  the same shape of query as a person's name.
//  Issue 7 fix: was reading from FakeChatRepository (deleted),
//  so search never saw real persisted chats/messages. Now backed
//  by RoomChatRepository like every other chat ViewModel.
// ─────────────────────────────────────────────────────────────
class ChatSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository = ChatModule.provideChatRepository(application)

    private val _uiState = MutableStateFlow(ChatSearchUiState())
    val uiState: StateFlow<ChatSearchUiState> = _uiState.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        runSearch()
    }

    fun onScopeChanged(scope: ChatSearchScope) {
        _uiState.update { it.copy(scope = scope) }
        runSearch()
    }

    private fun runSearch() {
        searchJob?.cancel()
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val scope   = _uiState.value.scope
            val chats   = repository.observeChats().first()
            val results = mutableListOf<ChatSearchResult>()

            chats.forEach { chat ->
                if (scope == ChatSearchScope.ALL || scope == ChatSearchScope.CHATS) {
                    if (SmartSearch.matches(query, chat.title)) {
                        results += ChatSearchResult(
                            chatId    = chat.id,
                            chatTitle = chat.title,
                            snippet   = chat.lastMessage?.text.orEmpty(),
                            scope     = ChatSearchScope.CHATS,
                            timestamp = chat.lastMessage?.timestamp ?: 0L,
                        )
                    }
                }

                if (scope == ChatSearchScope.ALL || scope == ChatSearchScope.MESSAGES || scope == ChatSearchScope.FILES) {
                    val messages = repository.observeMessages(chat.id).first()
                    messages.forEach { message ->
                        val matchesText = message.text?.contains(query, ignoreCase = true) == true
                        val matchesFile = message.attachment?.fileName?.contains(query, ignoreCase = true) == true

                        val wantsFiles    = scope == ChatSearchScope.FILES
                        val wantsMessages = scope == ChatSearchScope.ALL || scope == ChatSearchScope.MESSAGES

                        if ((wantsFiles && matchesFile) || (wantsMessages && (matchesText || matchesFile))) {
                            results += ChatSearchResult(
                                chatId    = chat.id,
                                chatTitle = chat.title,
                                snippet   = message.text ?: message.attachment?.fileName.orEmpty(),
                                scope     = if (matchesFile) ChatSearchScope.FILES else ChatSearchScope.MESSAGES,
                                timestamp = message.timestamp,
                            )
                        }
                    }
                }
            }

            _uiState.update {
                it.copy(results = results.sortedByDescending { r -> r.timestamp }, isSearching = false)
            }
        }
    }
}
