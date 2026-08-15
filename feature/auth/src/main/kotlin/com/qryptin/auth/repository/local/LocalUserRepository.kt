package com.qryptin.auth.repository.local

import com.qryptin.auth.local.UserDao
import com.qryptin.auth.local.UserEntity
import com.qryptin.auth.mapper.toDomain
import com.qryptin.auth.model.RegistrationInput
import com.qryptin.auth.model.UserProfile
import com.qryptin.auth.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  LocalUserRepository
//  Room-backed implementation of UserRepository. This is the
//  ONLY class in the app that should touch UserDao directly.
// ─────────────────────────────────────────────────────────────
class LocalUserRepository(
    private val userDao: UserDao,
    private val userApi: com.qryptin.auth.network.UserApi = com.qryptin.auth.network.RetrofitClient.userApi
) : UserRepository {

    override suspend fun findByPhone(phoneNumber: String): UserProfile? =
        userDao.findByPhone(phoneNumber)?.toDomain()

    override suspend fun findById(userId: String): UserProfile? =
        userDao.findById(userId)?.toDomain()

    override fun observeUser(userId: String): Flow<UserProfile?> =
        userDao.observeById(userId).map { it?.toDomain() }

    override suspend fun isQryptinIdAvailable(qryptinId: String, excludingUserId: String?): Boolean {
        val normalized = normalizeQryptinId(qryptinId)
        val count = if (excludingUserId != null) {
            userDao.countByQryptinIdExcluding(normalized, excludingUserId)
        } else {
            userDao.countByQryptinId(normalized)
        }
        return count == 0
    }

    override suspend fun registerUser(phoneNumber: String, input: RegistrationInput): UserProfile {
        val now = System.currentTimeMillis()
        val userId = UUID.randomUUID().toString()
        val entity = UserEntity(
            userId          = userId,
            qryptinId        = normalizeQryptinId(input.qryptinId),
            fullName          = input.fullName.trim(),
            phoneNumber        = phoneNumber,
            email                = input.email?.trim()?.ifBlank { null },
            bio                    = input.bio.trim(),
            profilePhotoUri         = input.profilePhotoUri,
            createdAt                = now,
            updatedAt                = now,
            isRegistered              = true,
            isVerified                  = false,
        )
        userDao.insert(entity)

        // Sync with backend
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                userApi.registerUser(
                    com.qryptin.auth.network.RegisterRequest(
                        phoneNumber  = phoneNumber,
                        fullName     = input.fullName.trim(),
                        id           = userId,
                        bio          = input.bio.trim(),
                        profilePhoto = input.profilePhotoUri
                    )
                )
            } catch (_: Exception) {
                // Non-fatal for local registration
            }
        }

        return entity.toDomain()
    }

    override suspend fun markVerified(userId: String) {
        val existing = userDao.findById(userId) ?: return
        userDao.update(existing.copy(isVerified = true, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateProfile(profile: UserProfile): UserProfile {
        val existing = userDao.findById(profile.userId)
            ?: error("Cannot update a profile that doesn't exist locally: ${profile.userId}")
        val updated = existing.copy(
            qryptinId        = normalizeQryptinId(profile.qryptinId),
            fullName          = profile.fullName.trim(),
            email                = profile.email?.trim()?.ifBlank { null },
            bio                    = profile.bio.trim(),
            profilePhotoUri         = profile.profilePhotoUri,
            updatedAt                = System.currentTimeMillis(),
        )
        userDao.update(updated)

        // Sync with backend
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                userApi.registerUser(
                    com.qryptin.auth.network.RegisterRequest(
                        phoneNumber = profile.phoneNumber,
                        fullName    = profile.fullName.trim(),
                        id          = profile.userId, // Using userId as id
                        bio         = profile.bio.trim(),
                        profilePhoto = profile.profilePhotoUri
                    )
                )
            } catch (_: Exception) {
                // Non-fatal
            }
        }

        return updated.toDomain()
    }

    companion object {
        /** Lowercase, no spaces — matches the RegisterScreen / QryptIN ID rules. */
        fun normalizeQryptinId(raw: String): String =
            raw.trim().lowercase().filter { !it.isWhitespace() }
    }
}
