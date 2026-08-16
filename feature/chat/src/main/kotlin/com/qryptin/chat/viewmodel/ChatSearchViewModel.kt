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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
@OptIn(FlowPreview::class)
class ChatSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository = ChatModule.provideChatRepository(application)

    /** Immediate, ungated query — this is what the TextField is bound to. */
    private val rawSearchQuery = MutableStateFlow("")

    private val _scope = MutableStateFlow(ChatSearchScope.ALL)
    private val _isSearching = MutableStateFlow(false)

    val uiState: StateFlow<ChatSearchUiState> = combine(
        rawSearchQuery,
        rawSearchQuery.debounce(300), // Debounce for expensive search
        _scope,
        _isSearching
    ) { query, debouncedQuery, scope, searching ->
        // Note: Actual results are still computed in a side job triggered by debouncedQuery
        ChatSearchUiState(
            query = query,
            scope = scope,
            isSearching = searching
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatSearchUiState())

    private val _results = MutableStateFlow<List<ChatSearchResult>>(emptyList())
    // Combine base UI state with the background-calculated results
    val fullUiState: StateFlow<ChatSearchUiState> = combine(uiState, _results) { state, res ->
        state.copy(results = res)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatSearchUiState())

    init {
        // Reactive search trigger
        viewModelScope.launch {
            rawSearchQuery
                .debounce(300)
                .collectLatest { query ->
                    performSearch(query, _scope.value)
                }
        }
        
        // Also trigger when scope changes
        viewModelScope.launch {
            _scope.collectLatest { scope ->
                performSearch(rawSearchQuery.value, scope)
            }
        }
    }

    fun onQueryChanged(query: String) {
        rawSearchQuery.value = query
    }

    fun onScopeChanged(scope: ChatSearchScope) {
        _scope.value = scope
    }

    private suspend fun performSearch(query: String, scope: ChatSearchScope) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _results.value = emptyList()
            _isSearching.value = false
            return
        }

        _isSearching.value = true
        val chats = repository.observeChats().first()
        val results = mutableListOf<ChatSearchResult>()

        chats.forEach { chat ->
            if (scope == ChatSearchScope.ALL || scope == ChatSearchScope.CHATS) {
                if (SmartSearch.matches(trimmed, chat.title)) {
                    results += ChatSearchResult(
                        chatId = chat.id,
                        chatTitle = chat.title,
                        snippet = chat.lastMessage?.text.orEmpty(),
                        scope = ChatSearchScope.CHATS,
                        timestamp = chat.lastMessage?.timestamp ?: 0L,
                    )
                }
            }

            if (scope == ChatSearchScope.ALL || scope == ChatSearchScope.MESSAGES || scope == ChatSearchScope.FILES) {
                val messages = repository.observeMessages(chat.id).first()
                messages.forEach { message ->
                    val matchesText = message.text?.contains(trimmed, ignoreCase = true) == true
                    val matchesFile = message.attachment?.fileName?.contains(trimmed, ignoreCase = true) == true

                    val wantsFiles = scope == ChatSearchScope.FILES
                    val wantsMessages = scope == ChatSearchScope.ALL || scope == ChatSearchScope.MESSAGES

                    if ((wantsFiles && matchesFile) || (wantsMessages && (matchesText || matchesFile))) {
                        results += ChatSearchResult(
                            chatId = chat.id,
                            chatTitle = chat.title,
                            snippet = message.text ?: message.attachment?.fileName.orEmpty(),
                            scope = if (matchesFile) ChatSearchScope.FILES else ChatSearchScope.MESSAGES,
                            timestamp = message.timestamp,
                        )
                    }
                }
            }
        }

        _results.value = results.sortedByDescending { r -> r.timestamp }
        _isSearching.value = false
    }
}
