package com.qryptin.chat.group.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qryptin.chat.group.ui.screens.CreateGroupDetailsScreen
import com.qryptin.chat.group.ui.screens.CustomPermissionsScreen
import com.qryptin.chat.group.ui.screens.GroupCreatedScreen
import com.qryptin.chat.group.ui.screens.SelectParticipantsScreen
import com.qryptin.chat.group.viewmodel.GroupCreationViewModel
import com.qryptin.chat.group.viewmodel.GroupNavEvent

// ─────────────────────────────────────────────────────────────
//  GroupRoutes
// ─────────────────────────────────────────────────────────────
object GroupRoutes {
    const val GRAPH               = "group_creation_graph"
    const val SELECT_PARTICIPANTS = "group/select_participants"
    const val GROUP_DETAILS       = "group/details"
    const val PERMISSIONS         = "group/permissions"
    const val SUCCESS             = "group/success"
}

// ─────────────────────────────────────────────────────────────
//  groupCreationGraph
//  Nested nav graph embedded inside chatGraph.
//  A single GroupCreationViewModel is scoped to the graph's
//  back-stack entry so all 4 screens share state without
//  passing args through navigation.
// ─────────────────────────────────────────────────────────────
fun NavGraphBuilder.groupCreationGraph(
    navController  : NavController,
    onGroupCreated : (chatId: String) -> Unit,
) {
    navigation(
        startDestination = GroupRoutes.SELECT_PARTICIPANTS,
        route            = GroupRoutes.GRAPH,
    ) {
        composable(GroupRoutes.SELECT_PARTICIPANTS) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(GroupRoutes.GRAPH) }
            val viewModel: GroupCreationViewModel = viewModel(graphEntry)

            SelectParticipantsScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() },
                onNext    = { navController.navigate(GroupRoutes.GROUP_DETAILS) },
            )
        }

        composable(GroupRoutes.GROUP_DETAILS) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(GroupRoutes.GRAPH) }
            val viewModel: GroupCreationViewModel = viewModel(graphEntry)

            CreateGroupDetailsScreen(
                viewModel           = viewModel,
                onBack              = { navController.popBackStack() },
                onNext              = { navController.navigate(GroupRoutes.PERMISSIONS) },
                onCustomPermissions = { navController.navigate(GroupRoutes.PERMISSIONS) },
            )
        }

        composable(GroupRoutes.PERMISSIONS) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(GroupRoutes.GRAPH) }
            val viewModel: GroupCreationViewModel = viewModel(graphEntry)

            LaunchedEffect(Unit) {
                viewModel.navEvent.collect { event ->
                    when (event) {
                        is GroupNavEvent.NavigateToSuccess ->
                            navController.navigate(GroupRoutes.SUCCESS) {
                                popUpTo(GroupRoutes.SELECT_PARTICIPANTS) { inclusive = false }
                            }
                        is GroupNavEvent.NavigateToConversation ->
                            onGroupCreated(event.chatId)
                    }
                }
            }

            CustomPermissionsScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() },
                onDone    = { viewModel.createGroup() },
            )
        }

        composable(GroupRoutes.SUCCESS) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(GroupRoutes.GRAPH) }
            val viewModel: GroupCreationViewModel = viewModel(graphEntry)

            LaunchedEffect(Unit) {
                viewModel.navEvent.collect { event ->
                    if (event is GroupNavEvent.NavigateToConversation) {
                        onGroupCreated(event.chatId)
                    }
                }
            }

            GroupCreatedScreen(
                viewModel   = viewModel,
                onOpenGroup = { viewModel.onOpenGroupClicked() },
            )
        }
    }
}
