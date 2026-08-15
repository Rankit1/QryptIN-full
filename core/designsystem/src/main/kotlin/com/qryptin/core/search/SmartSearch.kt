package com.qryptin.core.search

// ─────────────────────────────────────────────────────────────
//  SmartSearch
//
//  Issue 8 fix — every search box in the app (contacts, calls,
//  chat search, group participant search) used to do a plain
//  `name.contains(query, ignoreCase = true)`, which fails on
//  anything but an exact-case, contiguous substring. Example
//  from the bug report: contact "rokto chushi" should still be
//  found by "rokto hus", "RokTO", "rok", "chus", "CHUSHI", "hus".
//
//  This lives in core:designsystem (not feature:contacts) so
//  every feature module — contacts, calls, chat, and chat's
//  group sub-feature — can depend on the exact same matching
//  logic instead of each screen reimplementing its own filter.
//
//  Matching rules:
//   - case-insensitive, whitespace-normalized
//   - the query is split into tokens; EVERY query token must be
//     found as a substring of the candidate name (as a whole) or
//     of one of the candidate's own tokens. This is what makes
//     "rokto hus" match "rokto chushi": "rokto" hits the first
//     name token, "hus" hits inside "chushi".
//   - phone numbers are matched digits-only, substring anywhere
//     (not just as a prefix), so "98765" or "3210" both hit.
// ─────────────────────────────────────────────────────────────
object SmartSearch {

    /** Lowercases and strips all whitespace, e.g. "Arjun Iyer" -> "arjuniyer". */
    fun normalize(value: String): String =
        value.lowercase().filter { !it.isWhitespace() }

    /** Digits-only projection of a phone number, e.g. "+91 98765-43210" -> "919876543210". */
    fun digitsOnly(value: String): String = value.filter { it.isDigit() }

    private fun tokensOf(value: String): List<String> =
        value.split(Regex("\\s+")).map { normalize(it) }.filter { it.isNotEmpty() }

    /**
     * True if every whitespace-separated token in [query] is found as a
     * substring somewhere in [name] — either against the full normalized
     * name or against one of its individual tokens — or, failing that, as
     * a substring of the digits-only [phone]. Blank queries always match.
     */
    fun matches(query: String, name: String, phone: String = ""): Boolean {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return true

        val queryTokens = tokensOf(trimmedQuery)
        if (queryTokens.isNotEmpty()) {
            val normalizedName = normalize(name)
            val nameTokens = tokensOf(name)

            val allTokensMatch = queryTokens.all { qt ->
                normalizedName.contains(qt) || nameTokens.any { nt -> nt.contains(qt) }
            }
            if (allTokensMatch) return true
        }

        val digitsQuery = digitsOnly(trimmedQuery)
        if (digitsQuery.isNotEmpty() && digitsOnly(phone).contains(digitsQuery)) return true

        return false
    }
}
