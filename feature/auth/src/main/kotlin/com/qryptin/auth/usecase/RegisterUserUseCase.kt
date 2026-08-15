package com.qryptin.auth.usecase

import com.qryptin.auth.model.RegistrationInput
import com.qryptin.auth.model.UserProfile
import com.qryptin.auth.repository.UserRepository
import com.qryptin.auth.repository.local.LocalUserRepository

sealed class RegisterUserResult {
    data class Success(val profile: UserProfile) : RegisterUserResult()
    data class Failure(val message: String) : RegisterUserResult()
}

class RegisterUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(phoneNumber: String, input: RegistrationInput): RegisterUserResult {
        if (input.fullName.trim().length < 2) {
            return RegisterUserResult.Failure("Please enter your full name")
        }

        val normalizedId = LocalUserRepository.normalizeQryptinId(input.qryptinId)
        if (normalizedId.length < 3) {
            return RegisterUserResult.Failure("QryptIN ID must be at least 3 characters")
        }
        if (!normalizedId.matches(Regex("^[a-z0-9_]+$"))) {
            return RegisterUserResult.Failure("QryptIN ID can only contain letters, numbers, and underscores")
        }
        if (!userRepository.isQryptinIdAvailable(normalizedId)) {
            return RegisterUserResult.Failure("This QryptIN ID is already taken")
        }

        if (!input.email.isNullOrBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(input.email.trim()).matches()) {
            return RegisterUserResult.Failure("Please enter a valid email address")
        }

        val profile = userRepository.registerUser(phoneNumber, input.copy(qryptinId = normalizedId))
        return RegisterUserResult.Success(profile)
    }
}
