package com.qryptin.auth.repository.local

import com.qryptin.auth.repository.AuthRepository
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────
//  MockAuthRepository
//
//  FOR CURRENT DEVELOPMENT ONLY. Simulates SMS OTP request/verify
//  with realistic latency so loading states can be built/tested
//  now. There is no real gateway behind this — any syntactically
//  valid 6-digit code is accepted. Replace with a real
//  Authentication Service client later; AuthRepository is the
//  seam, so nothing else in the app needs to change.
// ─────────────────────────────────────────────────────────────
class MockAuthRepository : AuthRepository {

    override suspend fun requestOtp(phoneNumber: String): Result<Unit> {
        delay(1200)
        return Result.success(Unit)
    }

    override suspend fun verifyOtp(phoneNumber: String, code: String): Result<Boolean> {
        delay(1200)
        return if (code.length == 6 && code.all { it.isDigit() }) {
            Result.success(true)
        } else {
            Result.success(false)
        }
    }
}
