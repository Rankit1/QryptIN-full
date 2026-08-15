package com.qryptin.chat.group.model

import com.qryptin.contacts.model.Contact
import com.qryptin.core.search.SmartSearch

// ─────────────────────────────────────────────────────────────
//  GroupUiState
//  Single immutable state object that drives all four screens
//  of the group creation wizard. The ViewModel exposes this as
//  a StateFlow; each screen consumes only the slice it needs.
// ─────────────────────────────────────────────────────────────
data class GroupUiState(

    // ── Screen 1 — Select Participants ──────────────────────
    val availableContacts : List<Contact> = emptyList(),
    val selectedMembers   : List<Contact> = emptyList(),
    val searchQuery       : String        = "",

    // ── Screen 2 — Group Details ────────────────────────────
    val groupName         : String        = "",
    val groupDescription  : String        = "",
    val groupPhotoUri     : String?       = null,

    // ── Screen 3 — Permissions ──────────────────────────────
    val permissions       : GroupPermissions = GroupPermissions(),

    // ── Screen 4 / result ───────────────────────────────────
    val createdGroupId    : String?       = null,
    val createdGroupName  : String        = "",

    // ── Cross-cutting ────────────────────────────────────────
    val isCreating        : Boolean       = false,
    val errorMessage      : String?       = null,
) {
    val filteredContacts: List<Contact>
        get() {
            val q = searchQuery.trim()
            return if (q.isBlank()) availableContacts
                   else availableContacts.filter { SmartSearch.matches(q, it.displayName, it.phone) }
        }

    val canProceedFromDetails: Boolean
        get() = selectedMembers.isNotEmpty()   // name optional per spec
}
