# QryptIN — Auth Flow

Jetpack Compose authentication module implementing the full onboarding flow from the design spec.

---

## Screens

| Screen | File | Description |
|---|---|---|
| Splash | `SplashScreen.kt` | Logo icon fade-in/out |
| Logo Animation | `LogoAnimationScreen.kt` | Letter-by-letter typewriter + tagline |
| Phone Input | `PhoneInputScreen.kt` | Country picker + phone field |
| OTP Verify | `OtpVerifyScreen.kt` | 6-box OTP input + resend timer |
| Register | `RegisterScreen.kt` | Name + confirmed phone → Sign Up |

---

## Architecture

```
auth/
├── build.gradle.kts
├── model/
│   └── AuthModel.kt          — CountryCode, AuthUser, AuthUiState, country list
├── viewmodel/
│   └── AuthViewModel.kt      — all auth state + business logic
├── navigation/
│   └── AuthNavGraph.kt       — NavHost + route constants
└── ui/
    ├── components/
    │   └── AuthComponents.kt — OtpInputRow, CountryCodeSelector, SecurityBadge, AuthScaffold
    └── screens/
        ├── SplashScreen.kt
        ├── LogoAnimationScreen.kt
        ├── PhoneInputScreen.kt
        ├── OtpVerifyScreen.kt
        └── RegisterScreen.kt
```

---

## Integration

### 1. Add to NavHost in your app

```kotlin
// In your root NavHost or Activity:
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "auth") {
        navigation(route = "auth", startDestination = AuthRoutes.SPLASH) {
            // Delegate to auth nav graph
        }
        composable("home") { HomeScreen() }
    }
}
```

Or simply call `AuthNavHost` at the activity level:

```kotlin
setContent {
    QryptINTheme {
        AuthNavHost(
            onAuthComplete = {
                // Replace auth graph with main app graph
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        )
    }
}
```

### 2. Font setup

Add to `res/font/`:
- `poppins_regular.ttf`
- `poppins_medium.ttf`
- `poppins_semibold.ttf`
- `poppins_bold.ttf`

Download from [Google Fonts — Poppins](https://fonts.google.com/specimen/Poppins)

### 3. Logo drawable

Add `res/drawable/ic_qryptin_logo.xml` (or `.webp`) — the peacock-Q logo from the brand.

### 4. Dark mode

Fully supported. Uses `QryptINTheme(darkTheme = isSystemInDarkTheme())`. All colors
resolve via `MaterialTheme.colorScheme` and `MaterialTheme.qrypt` extended tokens.

---

## OTP Component Notes

- **Auto-focus**: First box focuses automatically on screen entry.
- **Auto-advance**: Typing a digit moves focus to the next box automatically.
- **Backspace**: Clears current box and moves focus back.
- **Paste**: Detects strings > 1 char, distributes digits across all boxes.
- **Error state**: All boxes turn red border with error message below.

---

## Demo OTP values (remove in production)

| OTP entered | Result |
|---|---|
| `000000` | Existing user → goes to Home |
| Any other 6 digits | New user → goes to Register |

---

## Removing the mock network layer

In `AuthViewModel`, replace the `delay()` calls with real API calls:

```kotlin
fun submitPhone(onSuccess: () -> Unit) {
    viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        try {
            val result = authRepository.sendOtp(fullPhone)
            _uiState.value = AuthUiState.Success
            onSuccess()
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.message ?: "Network error")
        }
    }
}
```

Inject `AuthRepository` via Hilt or manual DI as needed.
