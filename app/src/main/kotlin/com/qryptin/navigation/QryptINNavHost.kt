package com.qryptin.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qryptin.auth.navigation.AuthNavHost
import com.qryptin.calls.navigation.callsGraph
import com.qryptin.chat.ChatModule
import com.qryptin.chat.navigation.chatGraph
import com.qryptin.contacts.navigation.contactsGraph
import com.qryptin.settings.navigation.settingsGraph
import com.qryptin.ui.navigation.AppRoutes
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination
import kotlinx.coroutines.launch

@Composable
fun QryptINNavHost() {
    val rootNav = rememberNavController()
    NavHost(navController = rootNav, startDestination = AppRoutes.AUTH) {
        composable(AppRoutes.AUTH) {
            AuthNavHost(
                onAuthComplete = {
                    rootNav.navigate(AppRoutes.MAIN) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoutes.MAIN) {
            MainNavHost(onLogout = {
                rootNav.navigate(AppRoutes.AUTH) {
                    popUpTo(AppRoutes.MAIN) { inclusive = true }
                }
            })
        }
    }
}

@Composable
private fun MainNavHost(onLogout: () -> Unit) {
    val mainNav        = rememberNavController()
    val context        = LocalContext.current
    val scope          = rememberCoroutineScope()
    val chatRepository = remember { ChatModule.provideChatRepository(context) }

    // Dynamic current destination handling for Bottom Bar highlighting
    val navBackStackEntry by mainNav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Match route to BottomNavDestination safely
    val currentBottomNavDest = remember(currentRoute) {
        BottomNavDestination.fromRoute(currentRoute)
    }

    // Helper: navigate to a tab destination
    fun navToTab(route: String) {
        mainNav.navigate(route) {
            popUpTo(mainNav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    fun onNavSelected(dest: BottomNavDestination) {
        val route = when (dest) {
            BottomNavDestination.HOME     -> AppRoutes.Chat.CHATS
            BottomNavDestination.CONTACTS -> AppRoutes.Contacts.CONTACTS
            BottomNavDestination.CALLS    -> AppRoutes.Calls.CALLS_HOME
            BottomNavDestination.SETTINGS -> AppRoutes.Settings.SETTINGS
        }
        navToTab(route)
    }

    NavHost(navController = mainNav, startDestination = AppRoutes.Chat.CHATS) {

        // ── Chats ──────────────────────────────────────────────
        chatGraph(
            navController  = mainNav,
            currentDest    = currentBottomNavDest,
            onNavSelected  = ::onNavSelected,
            chatRepository = chatRepository,
        )

        // ── Contacts ───────────────────────────────────────────
        contactsGraph(
            navController = mainNav,
            currentDest   = currentBottomNavDest,
            onNavSelected = ::onNavSelected,
            onChatClick   = { contact ->
                scope.launch {
                    val chat = chatRepository.getOrCreateDirectChat(
                        contactId   = contact.friendId ?: contact.phone,
                        contactName = contact.displayName,
                        avatarUrl   = contact.avatarUrl,
                    )
                    mainNav.navigate(AppRoutes.Chat.conversation(chat.id)) {
                        popUpTo(AppRoutes.Contacts.CONTACTS) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
        )

        // ── Calls ──────────────────────────────────────────────
        callsGraph(
            navController  = mainNav,
            currentNavDest = currentBottomNavDest,
            onNavSelected  = ::onNavSelected,
        )

        // ── Settings ───────────────────────────────────────────
        settingsGraph(
            navController  = mainNav,
            currentNavDest = currentBottomNavDest,
            onNavSelected  = ::onNavSelected,
            onLoggedOut    = onLogout,
        )
    }
}
