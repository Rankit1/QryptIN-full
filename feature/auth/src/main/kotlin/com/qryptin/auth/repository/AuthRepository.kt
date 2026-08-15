package com.qryptin.auth.repository

// ─────────────────────────────────────────────────────────────
//  AuthRepository
//
//  Abstracts OTP request/verification. Today: MockAuthRepository
//  simulates network latency and accepts any well-formed 6-digit
//  code (no real SMS gateway yet). Later: swap in an
//  implementation that calls the Authentication Service /
//  SMS gateway — ViewModel code calling this interface is
//  unaffected by that swap.
// ─────────────────────────────────────────────────────────────
interface AuthRepository {

    /** Simulates triggering an OTP send to [phoneNumber]. */
    suspend fun requestOtp(phoneNumber: String): Result<Unit>

    /** Simulates verifying [code] for [phoneNumber]. */
    suspend fun verifyOtp(phoneNumber: String, code: String): Result<Boolean>
}
