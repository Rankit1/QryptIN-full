package com.qryptin.contacts.repository

import com.qryptin.contacts.model.InviteMethod
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────
//  FakeContactsRepository
//  Simulates network and local-DB operations.
//  Replace with real Retrofit / Room implementation later.
// ─────────────────────────────────────────────────────────────
class FakeContactsRepository {

    // ── Simulated QryptIN user database ──────────────────────
    private val registeredNumbers: Map<String, String> = mapOf(
        "9876543210"  to "Rahul Sharma",
        "8765432109"  to "Priya Kapoor",
        "7654321098"  to "Debasmita Roy",
        "9812345678"  to "Arjun Mehta",
        "9000000000"  to "Rupa Devi",
        "9999999999"  to "Sundar Pichai",
    )

    /**
     * Checks whether [digitsOnly] (no spaces / dashes, no country code) belongs
     * to a registered QryptIN user.
     *
     * @return The registered display-name if found, null otherwise.
     */
    suspend fun lookupByPhone(digitsOnly: String): String? {
        delay(1_200) // simulate network latency
        return registeredNumbers[digitsOnly.trim()]
    }

    /**
     * Persists the contact locally (and optionally to the QryptIN cloud).
     * Returns true on success.
     */
    suspend fun saveContact(
        phone       : String,
        displayName : String,
        serverName  : String,
        email       : String,
        syncToSim   : Boolean,
    ): Boolean {
        delay(800) // simulate DB write
        // In production: write to Room, call REST endpoint, trigger SIM sync via ContentResolver
        return true
    }

    /**
     * Saves a non-QryptIN contact locally and optionally to SIM.
     * Returns true on success.
     */
    suspend fun saveNonQryptINContact(
        phone       : String,
        displayName : String,
        email       : String,
        syncToSim   : Boolean,
    ): Boolean {
        delay(800) // simulate DB write
        // In production: write to Room, trigger SIM sync via ContentResolver
        return true
    }

    /**
     * Simulates sending an SMS invitation to the given phone number.
     * Returns true on success.
     */
    suspend fun sendSmsInvite(phone: String): Boolean {
        delay(600) // simulate SMS gateway call
        // In production: launch SMS intent or call SMS API
        return true
    }

    /**
     * Simulates sending an email invitation to the given email address.
     * Returns true on success.
     */
    suspend fun sendEmailInvite(email: String): Boolean {
        delay(600) // simulate email send
        // In production: call email API or launch email intent
        return true
    }

    /**
     * Convenience: send invite via the selected [method].
     */
    suspend fun sendInvite(
        method : InviteMethod,
        phone  : String,
        email  : String,
    ): Boolean = when (method) {
        InviteMethod.SMS   -> sendSmsInvite(phone)
        InviteMethod.EMAIL -> sendEmailInvite(email)
    }
}
