package com.qryptin.auth.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qryptin.auth.ui.screens.*
import com.qryptin.auth.viewmodel.AuthViewModel

// ─────────────────────────────────────────────────────────────
//  Route definitions
// ─────────────────────────────────────────────────────────────
object AuthRoutes {
    const val SPLASH       = "splash"
    const val PHONE_INPUT  = "phone_input"
    const val OTP_VERIFY   = "otp_verify"
    const val USER_FOUND   = "user_found"
    const val REGISTER     = "register"
    const val HOME         = "home"          // hand-off to main graph
}

// ─────────────────────────────────────────────────────────────
//  Auth Navigation Host
//
//  Corrected flow (matches the QryptIN auth spec exactly):
//
//   SPLASH (welcome/logo animation, works for both logged-in and
//           logged-out users)
//     -> if already logged in: onAuthComplete() straight away
//     -> else: PHONE_INPUT
//
//   PHONE_INPUT --(send OTP)--> OTP_VERIFY
//     --(OTP verified, THEN database check)-->
//         existing user -> USER_FOUND -> onAuthComplete
//         new user      -> REGISTER   -> onAuthComplete
//
//  The database lookup only ever runs *after* OTP verification —
//  never before — per the corrected requirements.
// ─────────────────────────────────────────────────────────────
@Composable
fun AuthNavHost(
    navController : NavHostController = rememberNavController(),
    onAuthComplete: () -> Unit,         // called when user reaches home
) {
    val vm: AuthViewModel = viewModel()

    // ── Session check ───────────────────────────────────────
    // Kicked off immediately so it resolves well before the splash
    // animation (~1.9s) finishes. null = "still checking".
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        isLoggedIn = vm.isLoggedIn()
    }

    NavHost(
        navController    = navController,
        startDestination = AuthRoutes.SPLASH,
        enterTransition  = { fadeIn(tween(270)) + slideInHorizontally { (it * 0.06f).toInt() } },
        exitTransition   = { fadeOut(tween(180)) + slideOutHorizontally { -(it * 0.06f).toInt() } },
        popEnterTransition  = { fadeIn(tween(270)) + slideInHorizontally { -(it * 0.06f).toInt() } },
        popExitTransition   = { fadeOut(tween(180)) + slideOutHorizontally { (it * 0.06f).toInt() } },
    ) {

        composable(
            route           = AuthRoutes.SPLASH,
            enterTransition = { fadeIn(tween(0)) },
            exitTransition  = { fadeOut(tween(400)) },
        ) {
            SplashScreen(
                onFinished = {
                    // Session already known to be valid -> skip the whole
                    // auth flow and hand off straight to the main app.
                    if (isLoggedIn == true) {
                        onAuthComplete()
                    } else {
                        navController.navigate(AuthRoutes.PHONE_INPUT) {
                            popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AuthRoutes.PHONE_INPUT) {
            // Fresh start every time this is reached (e.g. after logout).
            LaunchedEffect(Unit) { vm.resetFlow() }

            PhoneInputScreen(
                viewModel = vm,
                onOtpSent = { navController.navigate(AuthRoutes.OTP_VERIFY) },
            )
        }

        composable(AuthRoutes.OTP_VERIFY) {
            OtpVerifyScreen(
                viewModel      = vm,
                onExistingUser = { navController.navigate(AuthRoutes.USER_FOUND) },
                onNewUser      = { navController.navigate(AuthRoutes.REGISTER) },
                onBack         = { navController.popBackStack() },
            )
        }

        composable(AuthRoutes.USER_FOUND) {
            // Session is already active by the time this screen shows
            // (started right after the DB check in submitOtp()).
            UserFoundScreen(
                viewModel = vm,
                onNext    = { onAuthComplete() },
                onBack    = { navController.popBackStack() },
            )
        }

        composable(AuthRoutes.REGISTER) {
            RegisterScreen(
                viewModel    = vm,
                onRegistered = { onAuthComplete() },
                onBack       = { navController.popBackStack() },
            )
        }
    }
}
