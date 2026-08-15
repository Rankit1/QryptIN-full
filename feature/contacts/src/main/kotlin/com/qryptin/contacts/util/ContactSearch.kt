package com.qryptin.contacts.util

// ─────────────────────────────────────────────────────────────
//  ContactSearch
//
//  Issue 6 fix — the old search did `name.contains(query, ignoreCase=true)`
//  which is too strict: it fails on mixed case typos like "ArJuniYER",
//  on queries with no space ("arjuniyer" vs "Arjun Iyer"), and on
//  whitespace/trim edge cases. This utility normalizes both sides the
//  same way before matching, and matches each name *token* individually
//  so substrings like "iyer" or "jun" still hit.
// ─────────────────────────────────────────────────────────────
object ContactSearch {

    /** Lowercases and strips all whitespace, so "Arjun Iyer" -> "arjuniyer". */
    fun normalize(value: String): String =
        value.lowercase().filter { !it.isWhitespace() }

    /** Digits-only projection of a phone number, e.g. "+91 98765-43210" -> "919876543210". */
    fun digitsOnly(value: String): String = value.filter { it.isDigit() }

    /**
     * True if [query] matches [name] or [phone] using:
     *  - whitespace/case-insensitive substring match against the full
     *    normalized name (handles "arjuniyer" matching "Arjun Iyer")
     *  - substring match against each individual name token (handles
     *    "iyer", "jun" matching part of a multi-word name)
     *  - substring match anywhere in the digits-only phone number
     *    (handles "98765", "3210", "987654" etc., not just a prefix)
     */
    fun matches(query: String, name: String, phone: String): Boolean {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return true

        val normalizedQuery = normalize(trimmedQuery)
        if (normalizedQuery.isNotEmpty()) {
            val normalizedName = normalize(name)
            if (normalizedName.contains(normalizedQuery)) return true

            val tokenHit = name.split(Regex("\\s+"))
                .any { token -> normalize(token).contains(normalizedQuery) }
            if (tokenHit) return true
        }

        val digitsQuery = digitsOnly(trimmedQuery)
        if (digitsQuery.isNotEmpty() && digitsOnly(phone).contains(digitsQuery)) return true

        return false
    }
}
