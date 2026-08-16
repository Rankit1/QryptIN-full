package com.qryptin.contacts.model

import com.qryptin.auth.model.CountryCode
import com.qryptin.auth.model.CommonCountryCodes

// ─────────────────────────────────────────────────────────────
//  AddContactUiState
//  Single source of truth flowing from AddContactViewModel.
//  Covers both EXISTING QryptIN user flow and NON-QryptIN flow.
// ─────────────────────────────────────────────────────────────
data class AddContactUiState(

    // ── Phone screen ──────────────────────────────────────
    val selectedCountry     : CountryCode = CommonCountryCodes.first(), // default India (+91)
    val phoneNumber         : String      = "",
    val isLookingUp         : Boolean     = false,
    val lookupError         : String?     = null,

    // ── Flow discriminator ────────────────────────────────
    /** True once a lookup completes and the number is NOT on QryptIN. */
    val isNonQryptINUser    : Boolean     = false,
    /** True once a lookup completes and the number IS on QryptIN. */
    val isExistingQryptINUser: Boolean    = false,

    /** Backend UUID of the found friend (null if not found/mock). */
    val friendId            : String?     = null,

    // ── Contact details (populated after lookup for existing user) ──
    val serverName          : String      = "",   // name returned by QryptIN server (immutable)
    val displayName         : String      = "",   // local / nickname — what user sees/edits
    val isEditingName       : Boolean     = false,
    val isNicknameMode      : Boolean     = false,

    // ── Save options ──────────────────────────────────────
    val saveOption          : ContactSaveOption = ContactSaveOption.SYNCED_WITH_SIM,

    // ── Optional email ────────────────────────────────────
    val email               : String      = "",

    // ── Invite method (non-QryptIN flow only) ─────────────
    val inviteMethod        : InviteMethod = InviteMethod.SMS,

    // ── Save flow ─────────────────────────────────────────
    val isSaving            : Boolean     = false,
    val saveError           : String?     = null,
    val isSaved             : Boolean     = false,
)
