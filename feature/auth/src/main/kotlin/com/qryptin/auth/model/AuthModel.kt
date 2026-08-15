package com.qryptin.auth.model

// ─────────────────────────────────────────────────────────────
//  Domain model
// ─────────────────────────────────────────────────────────────

data class CountryCode(
    val name  : String,
    val dial  : String,   // e.g. "+91"
    val flag  : String,   // emoji flag
    val iso   : String,   // e.g. "IN"
)

data class AuthUser(
    val phone    : String,
    val name     : String = "",
    val isNew    : Boolean = false,
)

// ─────────────────────────────────────────────────────────────
//  UI State sealed classes
// ─────────────────────────────────────────────────────────────

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object Success : AuthUiState()
}

// ─────────────────────────────────────────────────────────────
//  Domain-level user profile — what the rest of the app (and,
//  later, a remote API response) is mapped into. UI/ViewModel
//  code should depend on this, never on the Room UserEntity
//  directly, so swapping local -> cloud storage never touches
//  screens.
// ─────────────────────────────────────────────────────────────
data class UserProfile(
    val userId           : String,
    val qryptinId         : String,
    val fullName           : String,
    val phoneNumber         : String,
    val email                 : String? = null,
    val bio                     : String = "",
    val profilePhotoUri         : String? = null,
    val isRegistered             : Boolean = false,
    val isVerified                 : Boolean = false,
)

/** Result of checking whether a phone number is already registered locally. */
sealed class PhoneCheckResult {
    data class ExistingUser(val profile: UserProfile) : PhoneCheckResult()
    object NewUser : PhoneCheckResult()
}

/** Everything collected on the Register screen, before it becomes a UserProfile. */
data class RegistrationInput(
    val fullName     : String,
    val bio           : String,
    val qryptinId      : String,
    val email           : String?,
    val profilePhotoUri  : String?,
)

sealed class QryptinIdAvailability {
    object Idle      : QryptinIdAvailability()
    object Checking  : QryptinIdAvailability()
    object Available : QryptinIdAvailability()
    data class Taken(val message: String) : QryptinIdAvailability()
    data class Invalid(val message: String) : QryptinIdAvailability()
}

// ─────────────────────────────────────────────────────────────
//  Popular country codes (extend as needed)
// ─────────────────────────────────────────────────────────────
val CommonCountryCodes = listOf(
    CountryCode("India",          "+91",  "🇮🇳", "IN"),
    CountryCode("United States",  "+1",   "🇺🇸", "US"),
    CountryCode("United Kingdom", "+44",  "🇬🇧", "GB"),
    CountryCode("United Arab Emirates", "+971", "🇦🇪", "AE"),
    CountryCode("Australia",      "+61",  "🇦🇺", "AU"),
    CountryCode("Canada",         "+1",   "🇨🇦", "CA"),
    CountryCode("Germany",        "+49",  "🇩🇪", "DE"),
    CountryCode("France",         "+33",  "🇫🇷", "FR"),
    CountryCode("Singapore",      "+65",  "🇸🇬", "SG"),
    CountryCode("Japan",          "+81",  "🇯🇵", "JP"),
    CountryCode("China",          "+86",  "🇨🇳", "CN"),
    CountryCode("Brazil",         "+55",  "🇧🇷", "BR"),
    CountryCode("South Africa",   "+27",  "🇿🇦", "ZA"),
    CountryCode("Nigeria",        "+234", "🇳🇬", "NG"),
    CountryCode("Pakistan",       "+92",  "🇵🇰", "PK"),
    CountryCode("Bangladesh",     "+880", "🇧🇩", "BD"),
    CountryCode("Indonesia",      "+62",  "🇮🇩", "ID"),
    CountryCode("Malaysia",       "+60",  "🇲🇾", "MY"),
    CountryCode("Nepal",          "+977", "🇳🇵", "NP"),
    CountryCode("Sri Lanka",      "+94",  "🇱🇰", "LK"),
    CountryCode("Kenya",          "+254", "🇰🇪", "KE"),
)
