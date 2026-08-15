package com.qryptin.contacts.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  Domain models
// ─────────────────────────────────────────────────────────────

/**
 * A single contact entry, sourced from the local Room database.
 * [isOnQryptIN] drives the invite-vs-badge indicator.
 * [avatarUrl] is currently unused for local contacts but kept for
 * a future profile-photo / QryptIN-avatar feature.
 */
data class Contact(
    val id           : String,
    val displayName  : String,
    val phone        : String,
    val avatarUrl    : String? = null,
    val isOnQryptIN  : Boolean = false,
    val isBlocked    : Boolean = false,
    val lastSeen     : String? = null,           // "Online" | "Last seen today at …" | null
    val mutualGroups : Int     = 0,
    val createdAt    : Long    = 0L,             // epoch millis — when the contact was saved
) {
    /** First character used for the alphabetical index bar. */
    val indexChar: Char get() =
        displayName.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'

    /** Two-letter initials shown when no avatarUrl. */
    val initials: String get() =
        displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "?" }

    /** Human-readable "Added <date>" string shown on the contact card. */
    val formattedCreatedAt: String get() =
        if (createdAt <= 0L) ""
        else "Added " + SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(createdAt))
}

/** Which tab is selected on the Contacts screen. */
enum class ContactTab(val label: String) {
    ALL("All"),
    QRYPTIN("QryptIN"),
    INVITE("Invite"),
}

/** UI state for the Contacts screen. */
data class ContactsUiState(
    val contacts      : List<Contact> = emptyList(),
    val searchQuery   : String        = "",
    val selectedTab   : ContactTab    = ContactTab.ALL,
    val isLoading     : Boolean       = true,
    val errorMessage  : String?       = null,
)
