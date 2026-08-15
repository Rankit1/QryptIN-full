package com.qryptin.contacts.model

// ─────────────────────────────────────────────────────────────
//  InviteMethod
//  Determines how the invitation will be sent after saving
//  a non-QryptIN contact.
// ─────────────────────────────────────────────────────────────
enum class InviteMethod(val label: String) {
    SMS("SMS"),
    EMAIL("Email"),
}
