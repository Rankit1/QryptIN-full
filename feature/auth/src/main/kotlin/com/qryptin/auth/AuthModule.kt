package com.qryptin.auth

import android.content.Context
import com.qryptin.auth.local.AuthDatabase
import com.qryptin.auth.repository.AuthRepository
import com.qryptin.auth.repository.DeviceRepository
import com.qryptin.auth.repository.DeviceRepositoryImpl
import com.qryptin.auth.repository.SessionRepository
import com.qryptin.auth.repository.UserRepository
import com.qryptin.auth.repository.local.LocalSessionRepository
import com.qryptin.auth.repository.local.LocalUserRepository
import com.qryptin.auth.usecase.CheckPhoneUseCase
import com.qryptin.auth.usecase.RegisterUserUseCase
import com.qryptin.auth.usecase.VerifyOtpUseCase
import com.qryptin.auth.repository.firebase.FirebaseAuthRepository
import android.app.Activity

// ─────────────────────────────────────────────────────────────
//  AuthModule
//
//  Manual dependency-injection wiring for feature:auth (no
//  Hilt/Dagger in this project yet). Everything is process-wide
//  singleton, mirroring AuthDatabase.getInstance(). This is the
//  ONLY place that decides which concrete repository
//  implementation (local vs, later, remote) backs each
//  interface -- every ViewModel depends on the interfaces only.
// ─────────────────────────────────────────────────────────────
object AuthModule {

    @Volatile private var userRepository: UserRepository? = null
    @Volatile private var sessionRepository: SessionRepository? = null
    @Volatile private var authRepository: AuthRepository? = null
    @Volatile private var deviceRepository: DeviceRepository? = null

    fun provideUserRepository(context: Context): UserRepository =
        userRepository ?: synchronized(this) {
            userRepository ?: LocalUserRepository(
                AuthDatabase.getInstance(context).userDao()
            ).also { userRepository = it }
        }

    fun provideDeviceRepository(): DeviceRepository =
        deviceRepository ?: synchronized(this) {
            deviceRepository ?: DeviceRepositoryImpl().also { deviceRepository = it }
        }

    fun provideSessionRepository(context: Context): SessionRepository =
        sessionRepository ?: synchronized(this) {
            sessionRepository ?: LocalSessionRepository(
                sessionDao   = AuthDatabase.getInstance(context).sessionDao(),
                authStateDao = AuthDatabase.getInstance(context).authStateDao(),
            ).also { sessionRepository = it }
        }

    fun provideAuthRepository(context: Context): AuthRepository =
        authRepository ?: synchronized(this) {
            authRepository ?: FirebaseAuthRepository().also { authRepository = it }
        }

    fun provideCheckPhoneUseCase(context: Context) =
        CheckPhoneUseCase(provideUserRepository(context))

    fun provideRegisterUserUseCase(context: Context) =
        RegisterUserUseCase(provideUserRepository(context))

    fun provideVerifyOtpUseCase(context: Context) =
        VerifyOtpUseCase(
            authRepository = provideAuthRepository(context),
        )
    
    fun setActivityForAuth(activity: Activity, context: Context) {
        val repo = provideAuthRepository(context)  // ensures it's initialized
        (repo as? FirebaseAuthRepository)?.setActivity(activity)
    }

    // Let signup screen pass user name:
    fun setUserNameForAuth(name: String) {
        (authRepository as? FirebaseAuthRepository)?.pendingUserName = name
    }   
}
