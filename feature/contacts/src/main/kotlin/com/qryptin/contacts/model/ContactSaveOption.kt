package com.qryptin.contacts.model

// ─────────────────────────────────────────────────────────────
//  ContactSaveOption
//  Drives the two-card selector on AddExistingQryptINContactScreen.
// ─────────────────────────────────────────────────────────────
enum class ContactSaveOption(
    val title      : String,
    val description: String,
) {
    SYNCED_WITH_SIM(
        title       = "Synced with SIM (Default)",
        description = "Save in QryptIN and sync to your SIM.",
    ),
    QRYPTIN_ONLY(
        title       = "Saved in QryptIN Contacts",
        description = "Save only inside QryptIN app. Will not sync to SIM.",
    ),
}
