package com.qryptin.chat.model

import com.qryptin.contacts.model.Contact

// ─────────────────────────────────────────────────────────────
//  GroupCreationUiState
//  Drives GroupCreationScreen — contact multi-select, group name,
//  group avatar placeholder, create action.
// ─────────────────────────────────────────────────────────────
data class GroupCreationUiState(
    val availableContacts : List<Contact> = emptyList(),
    val selectedContacts  : List<Contact> = emptyList(),
    val groupName         : String        = "",
    val groupAvatarUri    : String?       = null,
    val searchQuery       : String        = "",
    val isCreating        : Boolean       = false,
    val errorMessage      : String?       = null,
) {
    val canCreate: Boolean get() =
        groupName.isNotBlank() && selectedContacts.size >= 2 && !isCreating
}

/** Selection mode for the generic [com.qryptin.chat.ui.screens.ContactSelectionScreen]. */
enum class ContactSelectionMode {
    SINGLE,
    MULTI,
}

data class ContactSelectionUiState(
    val mode              : ContactSelectionMode = ContactSelectionMode.MULTI,
    val availableContacts : List<Contact>         = emptyList(),
    val selectedContacts  : List<Contact>         = emptyList(),
    val searchQuery       : String                = "",
    val isLoading         : Boolean               = true,
)
