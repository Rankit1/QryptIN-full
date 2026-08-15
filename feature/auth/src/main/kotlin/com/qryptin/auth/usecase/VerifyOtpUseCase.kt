package com.qryptin.auth.usecase

import com.qryptin.auth.repository.AuthRepository

sealed class VerifyOtpResult {
    object Success : VerifyOtpResult()
    data class Failure(val message: String) : VerifyOtpResult()
}

// ─────────────────────────────────────────────────────────────
//  VerifyOtpUseCase
//
//  Verifies ONLY the entered OTP code — nothing else. It does not
//  know or care whether the phone number belongs to an existing
//  user or a brand-new one, and it never touches UserRepository /
//  SessionRepository.
//
//  This is intentional: the corrected QryptIN auth flow is
//      phone -> OTP request -> OTP verify -> THEN database check
//  so the database lookup (existing vs. new user) must happen
//  strictly *after* OTP verification succeeds, never before. The
//  caller (AuthViewModel) performs that lookup itself once this
//  use case returns Success, then starts the session for an
//  existing user, or routes to Register for a new one (session is
//  started there, once registration completes).
// ─────────────────────────────────────────────────────────────
class VerifyOtpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: String, code: String): VerifyOtpResult {
        val result = authRepository.verifyOtp(phoneNumber, code)

        val verified = result.getOrElse {
            return VerifyOtpResult.Failure("Something went wrong. Please try again.")
        }
        return if (verified) {
            VerifyOtpResult.Success
        } else {
            VerifyOtpResult.Failure("Incorrect OTP. Please try again.")
        }
    }
}
