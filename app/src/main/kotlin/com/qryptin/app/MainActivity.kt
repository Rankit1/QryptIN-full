package com.qryptin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.qryptin.auth.AuthModule
import com.qryptin.app.theme.ThemeViewModel
import com.qryptin.navigation.QryptINNavHost
import com.qryptin.settings.model.ThemeMode
import com.qryptin.core.designsystem.ui.theme.QryptINTheme
import androidx.lifecycle.lifecycleScope
import com.qryptin.chat.ChatModule
import com.qryptin.chat.repository.ChatRepositoryImpl
import com.qryptin.contacts.ContactsModule
import com.qryptin.contacts.repository.ContactsRepositoryImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  MainActivity — single-activity host for QryptIN
//
//  Responsibilities:
//  • Install splash screen (API 31+ native splash)
//  • Enable edge-to-edge display
//  • Read the persisted theme choice and apply QryptINTheme
//    app-wide (Issue 4 fix — previously the persisted choice
//    was never read, so the app just followed system dark mode
//    regardless of what the user picked in Settings)
//  • Force LIGHT MODE for anyone who is not logged in yet,
//    regardless of their persisted Light/Dark/System choice or
//    the device's system theme (auth-flow correction, section 6/7
//    of the QryptIN spec) — dark mode only ever applies once a
//    session is active, i.e. after a successful login.
//  • Hand off to QryptINNavHost (root navigation)
// ─────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sessionRepository = AuthModule.provideSessionRepository(applicationContext)
        val chatRepository = ChatModule.provideChatRepository(applicationContext)
        val contactsRepository = ContactsModule.provideContactsRepository(applicationContext)

        lifecycleScope.launch {
            sessionRepository.isLoggedIn.collectLatest { loggedIn ->
                if (loggedIn) {
                    val userId = sessionRepository.currentUserId()
                    if (userId != null) {
                        (chatRepository as? ChatRepositoryImpl)?.connectWebSocket(userId)
                        (chatRepository as? ChatRepositoryImpl)?.syncInbox(userId)
                        (contactsRepository as? ContactsRepositoryImpl)?.loadContactsFromServer(userId)
                    }
                } else {
                    (chatRepository as? ChatRepositoryImpl)?.disconnectWebSocket()
                }
            }
        }

        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeState by themeViewModel.uiState.collectAsState()

            // Reactive "is there an active session" flag. Starts as `false`
            // (safe default -> light mode) and flips once SessionRepository
            // confirms a logged-in session exists.
            val sessionRepository = remember { AuthModule.provideSessionRepository(applicationContext) }
            val isLoggedIn by produceState(initialValue = false, sessionRepository) {
                sessionRepository.isLoggedIn.collect { value = it }
            }

            val systemInDarkTheme = isSystemInDarkTheme()
            val requestedDarkTheme = when (themeState.themeMode) {
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
            }

            // Before login: always light, no matter what's persisted.
            // After login: honor the user's actual theme choice.
            val darkTheme = isLoggedIn && requestedDarkTheme

            QryptINTheme(darkTheme = darkTheme) {
                QryptINNavHost()
            }
        }
    }
}
