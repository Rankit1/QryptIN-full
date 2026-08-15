package com.qryptin.auth.repository

import com.qryptin.auth.model.RegistrationInput
import com.qryptin.auth.model.UserProfile
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  UserRepository
//
//  Abstracts WHERE user profiles live. Today: Room only
//  (LocalUserRepository). Later: an implementation of this same
//  interface can call the User Service microservice instead
//  (or fall back to a local cache) — nothing outside this
//  package needs to change.
// ─────────────────────────────────────────────────────────────
interface UserRepository {

    /** Null if no user is registered against this phone number yet. */
    suspend fun findByPhone(phoneNumber: String): UserProfile?

    suspend fun findById(userId: String): UserProfile?

    fun observeUser(userId: String): Flow<UserProfile?>

    /** True if [qryptinId] is free to use (normalized before checking). */
    suspend fun isQryptinIdAvailable(qryptinId: String, excludingUserId: String? = null): Boolean

    /** Creates a new local user from a completed registration form. Returns the saved profile. */
    suspend fun registerUser(phoneNumber: String, input: RegistrationInput): UserProfile

    /** Marks a user as OTP-verified (called after successful OTP check). */
    suspend fun markVerified(userId: String)

    suspend fun updateProfile(profile: UserProfile): UserProfile
}
