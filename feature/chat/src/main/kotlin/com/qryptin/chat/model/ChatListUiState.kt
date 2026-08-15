package com.qryptin.chat.model

// ─────────────────────────────────────────────────────────────
//  ChatListUiState
//  Single source of truth flowing from ChatListViewModel.
// ─────────────────────────────────────────────────────────────

enum class ChatListFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    GROUPS("Groups"),
    PINNED("Pinned"),
}

data class ChatListUiState(
    val chats          : List<Chat>       = emptyList(),
    val searchQuery    : String           = "",
    val selectedFilter : ChatListFilter    = ChatListFilter.ALL,
    val isLoading      : Boolean          = true,
    val isFabMenuOpen  : Boolean          = false,
    val selectedChatIds: Set<String>      = emptySet(),
    val errorMessage   : String?          = null,
) {
    val isSelectionMode: Boolean get() = selectedChatIds.isNotEmpty()
}
