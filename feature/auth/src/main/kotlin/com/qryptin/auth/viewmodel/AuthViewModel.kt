package com.qryptin.auth.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.auth.AuthModule
import com.qryptin.auth.model.*
import com.qryptin.auth.repository.local.LocalUserRepository
import com.qryptin.auth.usecase.RegisterUserResult
import com.qryptin.auth.usecase.VerifyOtpResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  AuthViewModel
//
//  Drives the entire auth flow: phone -> (existing user | new
//  user registration) -> OTP -> session. All persistence goes
//  through the repository/use-case layer in AuthModule -- this
//  class holds only UI state, never touches Room/DataStore
//  directly.
// ─────────────────────────────────────────────────────────────
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val checkPhoneUseCase    by lazy { AuthModule.provideCheckPhoneUseCase(appContext) }
    private val registerUserUseCase  by lazy { AuthModule.provideRegisterUserUseCase(appContext) }
    private val verifyOtpUseCase     by lazy { AuthModule.provideVerifyOtpUseCase(appContext) }
    private val userRepository       by lazy { AuthModule.provideUserRepository(appContext) }
    private val sessionRepository    by lazy { AuthModule.provideSessionRepository(appContext) }
    private val authRepository       by lazy { AuthModule.provideAuthRepository(appContext) }
    private val deviceRepository     by lazy { AuthModule.provideDeviceRepository() }

    // ── Phone input state ────────────────────────────────────
    var selectedCountry by mutableStateOf(CommonCountryCodes.first())
        private set

    var phoneNumber by mutableStateOf("")
        private set

    var phoneError by mutableStateOf<String?>(null)
        private set

    // ── Result of the phone lookup, consumed by nav to route ──
    // Existing user profile if found; the *pending* userId/phone for
    // the current flow (set for both existing + newly-registered users)
    // so the OTP step always has a userId to verify against.
    var foundUser by mutableStateOf<UserProfile?>(null)
        private set

    var pendingUserId by mutableStateOf<String?>(null)
        private set

    // ── OTP state ────────────────────────────────────────────
    var otpDigits by mutableStateOf(List(6) { "" })
        private set

    var otpError by mutableStateOf<String?>(null)
        private set

    var resendCountdown by mutableStateOf(30)
        private set

    var canResend by mutableStateOf(false)
        private set

    // ── Registration state ────────────────────────────────────
    var fullName by mutableStateOf("")
        private set
    var bio by mutableStateOf("")
        private set
    var qryptinId by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var profilePhotoUri by mutableStateOf<Uri?>(null)
        private set

    var nameError by mutableStateOf<String?>(null)
        private set
    var registrationError by mutableStateOf<String?>(null)
        private set

    var qryptinIdAvailability by mutableStateOf<QryptinIdAvailability>(QryptinIdAvailability.Idle)
        private set
    private var qryptinIdCheckJob: kotlinx.coroutines.Job? = null

    // ── Global UI state ───────────────────────────────────────
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // ── Derived ───────────────────────────────────────────────
    val fullPhone: String get() {
        val dial = selectedCountry.dial
        val digits = phoneNumber.trim().filter { it.isDigit() }
        return "$dial$digits"
    }
    val isPhoneValid: Boolean get() = phoneNumber.trim().length >= 7

    // ─────────────────────────────────────────────────────────
    //  Phone input actions
    // ─────────────────────────────────────────────────────────
    fun onPhoneChanged(value: String) {
        phoneNumber = value.filter { it.isDigit() }
        phoneError  = null
    }

    fun onCountrySelected(country: CountryCode) {
        selectedCountry = country
    }

    /**
     * Validates the phone number and sends the OTP. The corrected QryptIN
     * flow checks the database only *after* OTP verification succeeds, so
     * this step never touches UserRepository -- it just requests the code
     * and hands off to OTP entry. [onSent] fires once the code has gone out.
     */
    fun submitPhone(onSent: () -> Unit) {
        if (!isPhoneValid) {
            phoneError = "Please enter a valid phone number"
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(900) // simulate the "sending OTP..." network call
            
            val result = authRepository.requestOtp(fullPhone)
            
            result.onSuccess {
                startResendTimer()
                _uiState.value = AuthUiState.Success
                onSent()
            }.onFailure { e ->
                _uiState.value = AuthUiState.Idle
                phoneError = e.message ?: "Failed to send OTP. Please try again."
                android.util.Log.e("AuthViewModel", "OTP Request Failed for $fullPhone", e)
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  OTP actions
    // ─────────────────────────────────────────────────────────
    fun onOtpDigitChanged(index: Int, value: String) {
        val cleaned = value.filter { it.isDigit() }.take(1)
        otpDigits  = otpDigits.toMutableList().also { it[index] = cleaned }
        otpError   = null
    }

    fun onOtpPaste(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(6).padEnd(6, ' ')
        otpDigits  = digits.map { it.toString().trim() }
        otpError   = null
    }

    val isOtpComplete: Boolean get() = otpDigits.all { it.isNotEmpty() }

    fun submitOtp(onExistingUser: () -> Unit, onNewUser: () -> Unit) {
        if (!isOtpComplete) { otpError = "Please enter all 6 digits"; return }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val code = otpDigits.joinToString("")

            when (val otpResult = verifyOtpUseCase(fullPhone, code)) {
                is VerifyOtpResult.Failure -> {
                    _uiState.value = AuthUiState.Idle
                    otpError = otpResult.message
                }
                VerifyOtpResult.Success -> {
                    // Database check happens ONLY after OTP is verified.
                    when (val phoneCheck = checkPhoneUseCase(fullPhone)) {
                        is PhoneCheckResult.ExistingUser -> {
                            foundUser     = phoneCheck.profile
                            pendingUserId = phoneCheck.profile.userId
                            userRepository.markVerified(phoneCheck.profile.userId)
                            
                            android.util.Log.d("AuthViewModel", "Starting session for EXISTING user: ${phoneCheck.profile.userId}")
                            sessionRepository.startSession(phoneCheck.profile.userId, fullPhone)

                            // Register current device with backend
                            deviceRepository.registerDevice(
                                userId = phoneCheck.profile.userId,
                                deviceToken = "mock_fcm_token",
                                model = android.os.Build.MODEL,
                                osVersion = android.os.Build.VERSION.RELEASE
                            )

                            _uiState.value = AuthUiState.Success
                            onExistingUser()
                        }
                        PhoneCheckResult.NewUser -> {
                            foundUser     = null
                            pendingUserId = null
                            _uiState.value = AuthUiState.Success
                            onNewUser()
                        }
                    }
                }
            }
        }
    }

    fun resendOtp() {
        if (!canResend) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.requestOtp(fullPhone)
            _uiState.value = AuthUiState.Idle
            otpDigits      = List(6) { "" }
            otpError       = null
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        canResend       = false
        resendCountdown = 30
        viewModelScope.launch {
            while (resendCountdown > 0) {
                delay(1000)
                resendCountdown--
            }
            canResend = true
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Registration actions
    // ─────────────────────────────────────────────────────────
    fun onFullNameChanged(value: String) { fullName = value; nameError = null; registrationError = null }
    fun onBioChanged(value: String) { bio = value }
    fun onEmailChanged(value: String) { email = value; registrationError = null }
    fun onProfilePhotoSelected(uri: Uri?) { profilePhotoUri = uri }

    fun onQryptinIdChanged(value: String) {
        qryptinId = value
        registrationError = null
        val normalized = LocalUserRepository.normalizeQryptinId(value)

        qryptinIdCheckJob?.cancel()
        if (normalized.isEmpty()) {
            qryptinIdAvailability = QryptinIdAvailability.Idle
            return
        }
        if (normalized.length < 3) {
            qryptinIdAvailability = QryptinIdAvailability.Invalid("Must be at least 3 characters")
            return
        }
        if (!normalized.matches(Regex("^[a-z0-9_]+$"))) {
            qryptinIdAvailability = QryptinIdAvailability.Invalid("Only letters, numbers, and underscores")
            return
        }

        qryptinIdAvailability = QryptinIdAvailability.Checking
        qryptinIdCheckJob = viewModelScope.launch {
            delay(400) // debounce
            val available = userRepository.isQryptinIdAvailable(normalized)
            qryptinIdAvailability = if (available) {
                QryptinIdAvailability.Available
            } else {
                QryptinIdAvailability.Taken("This QryptIN ID is already taken")
            }
        }
    }

    val isRegistrationValid: Boolean
        get() = fullName.trim().length >= 2 &&
            qryptinIdAvailability is QryptinIdAvailability.Available

    /** Registers the user locally and starts their session (OTP already verified). */
    fun submitRegistration(onRegistered: () -> Unit) {
        if (fullName.trim().length < 2) {
            nameError = "Please enter your full name"
            return
        }
        if (qryptinIdAvailability !is QryptinIdAvailability.Available) {
            registrationError = "Please choose a valid, available QryptIN ID"
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val input = RegistrationInput(
                fullName          = fullName,
                bio                = bio,
                qryptinId           = qryptinId,
                email                = email.ifBlank { null },
                profilePhotoUri       = profilePhotoUri?.toString(),
            )
            when (val result = registerUserUseCase(fullPhone, input)) {
                is RegisterUserResult.Success -> {
                    foundUser     = result.profile
                    pendingUserId = result.profile.userId
                    // OTP was already verified before this screen was ever
                    // shown (corrected flow: OTP -> DB check -> Register),
                    // so registration completes the login right here.
                    userRepository.markVerified(result.profile.userId)
                    
                    android.util.Log.d("AuthViewModel", "Starting session for NEWLY REGISTERED user: ${result.profile.userId}")
                    sessionRepository.startSession(result.profile.userId, fullPhone)

                    // Register current device with backend
                    deviceRepository.registerDevice(
                        userId = result.profile.userId,
                        deviceToken = "mock_fcm_token",
                        model = android.os.Build.MODEL,
                        osVersion = android.os.Build.VERSION.RELEASE
                    )

                    _uiState.value = AuthUiState.Success
                    onRegistered()
                }
                is RegisterUserResult.Failure -> {
                    _uiState.value = AuthUiState.Idle
                    registrationError = result.message
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Session helpers (used by SplashScreen / navigation)
    // ─────────────────────────────────────────────────────────

    /** One-shot check used by AuthNavHost to decide the start destination. */
    suspend fun isLoggedIn(): Boolean = sessionRepository.isLoggedInNow()

    /** Clears the persisted session -- call on explicit logout. */
    suspend fun logOut() = sessionRepository.endSession()

    // ─────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    /** Clears all in-memory flow state -- call when returning to PhoneInputScreen fresh. */
    fun resetFlow() {
        phoneNumber = ""
        phoneError = null
        foundUser = null
        pendingUserId = null
        otpDigits = List(6) { "" }
        otpError = null
        fullName = ""
        bio = ""
        qryptinId = ""
        email = ""
        profilePhotoUri = null
        nameError = null
        registrationError = null
        qryptinIdAvailability = QryptinIdAvailability.Idle
        _uiState.value = AuthUiState.Idle
    }
}
