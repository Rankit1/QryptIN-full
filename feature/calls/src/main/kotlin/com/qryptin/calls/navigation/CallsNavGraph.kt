package com.qryptin.calls.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.qryptin.calls.model.CallModel
import com.qryptin.calls.model.CallType
import com.qryptin.calls.ui.screens.*
import com.qryptin.calls.viewmodel.ActiveCallViewModel
import com.qryptin.calls.viewmodel.CallsViewModel
import com.qryptin.ui.navigation.AppRoutes
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination

// ─────────────────────────────────────────────────────────────
//  CallsRoutes - Using centralized AppRoutes
// ─────────────────────────────────────────────────────────────
object CallsRoutes {
    val CALLS_HOME         = AppRoutes.Calls.CALLS_HOME
    val SELECT_CONTACT     = AppRoutes.Calls.SELECT_CONTACT
    val VOICE_CALL         = AppRoutes.Calls.VOICE_CALL
    val VIDEO_CALL         = AppRoutes.Calls.VIDEO_CALL

    fun voiceCall(contactId: String, contactName: String) =
        AppRoutes.Calls.voiceCall(contactId, contactName)

    fun videoCall(contactId: String, contactName: String) =
        AppRoutes.Calls.videoCall(contactId, contactName)
}

fun NavGraphBuilder.callsGraph(
    navController     : NavHostController,
    currentNavDest    : BottomNavDestination   = BottomNavDestination.CALLS,
    onNavSelected     : (BottomNavDestination) -> Unit = {},
) {
    composable(CallsRoutes.CALLS_HOME) {
        val viewModel: CallsViewModel = viewModel()
        CallsHomeScreen(
            viewModel      = viewModel,
            currentNavDest = currentNavDest,
            onNavSelected  = onNavSelected,
            onNewCall = {
                navController.navigate(CallsRoutes.SELECT_CONTACT) {
                    launchSingleTop = true
                }
            },
            onVoiceCall = { call ->
                navController.navigate(CallsRoutes.voiceCall(call.contactId, call.contactName)) {
                    launchSingleTop = true
                }
            },
            onVideoCall = { call ->
                navController.navigate(CallsRoutes.videoCall(call.contactId, call.contactName)) {
                    launchSingleTop = true
                }
            },
        )
    }

    composable(CallsRoutes.SELECT_CONTACT) {
        SelectContactForCallScreen(
            onBack   = { navController.popBackStack() },
            onCall   = { contact, type ->
                val route = if (type == CallType.VOICE)
                    CallsRoutes.voiceCall(contact.id, contact.displayName)
                else
                    CallsRoutes.videoCall(contact.id, contact.displayName)
                navController.navigate(route) { launchSingleTop = true }
            },
        )
    }

    composable(
        route     = CallsRoutes.VOICE_CALL,
        arguments = listOf(
            navArgument("contactId")   { type = NavType.StringType },
            navArgument("contactName") { type = NavType.StringType },
        ),
    ) { backStack ->
        val contactId   = backStack.arguments?.getString("contactId")   ?: ""
        val contactName = backStack.arguments?.getString("contactName")?.replace("_", " ") ?: ""
        val callVm: ActiveCallViewModel = viewModel()
        VoiceCallScreen(
            contactId      = contactId,
            contactName    = contactName,
            contactAvatar  = null,
            viewModel      = callVm,
            onNavigateBack = { navController.popBackStack() },
        )
    }

    composable(
        route     = CallsRoutes.VIDEO_CALL,
        arguments = listOf(
            navArgument("contactId")   { type = NavType.StringType },
            navArgument("contactName") { type = NavType.StringType },
        ),
    ) { backStack ->
        val contactId   = backStack.arguments?.getString("contactId")   ?: ""
        val contactName = backStack.arguments?.getString("contactName")?.replace("_", " ") ?: ""
        val callVm: ActiveCallViewModel = viewModel()
        VideoCallScreen(
            contactId      = contactId,
            contactName    = contactName,
            contactAvatar  = null,
            viewModel      = callVm,
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
