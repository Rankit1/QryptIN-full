package com.qryptin.auth.usecase

import com.qryptin.auth.model.PhoneCheckResult
import com.qryptin.auth.repository.UserRepository

class CheckPhoneUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(phoneNumber: String): PhoneCheckResult {
        val existing = userRepository.findByPhone(phoneNumber)
        return if (existing != null) {
            PhoneCheckResult.ExistingUser(existing)
        } else {
            PhoneCheckResult.NewUser
        }
    }
}
