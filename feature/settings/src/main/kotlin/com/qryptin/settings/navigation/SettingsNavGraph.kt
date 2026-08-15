package com.qryptin.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.qryptin.settings.ui.screens.*
import com.qryptin.ui.navigation.AppRoutes
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination

object SettingsRoutes {
    val SETTINGS_HOME            = AppRoutes.Settings.SETTINGS
    const val EDIT_PROFILE       = "settings_edit_profile"
    const val MESSAGING          = "settings_messaging"
    const val CALLS              = "settings_calls"
    const val PRIVACY            = "settings_privacy"
    const val ACTIVE_SESSIONS    = "settings_active_sessions"
    const val PQ_STATUS          = "settings_pq_status"
    const val STORAGE            = "settings_storage"
    const val STORAGE_MANAGE     = "settings_storage_manage"
    const val STORAGE_CATEGORY   = "settings_storage_category/{category}"
    fun storageCategoryRoute(category: com.qryptin.chat.media.MediaCategory) = "settings_storage_category/${category.name}"
    const val NOTIFICATIONS      = "settings_notifications"
}

fun NavGraphBuilder.settingsGraph(
    navController  : NavHostController,
    currentNavDest : BottomNavDestination,
    onNavSelected  : (BottomNavDestination) -> Unit,
    onLoggedOut    : () -> Unit,
) {
    composable(SettingsRoutes.SETTINGS_HOME) {
        SettingsHomeScreen(
            currentNavDest = currentNavDest,
            onNavSelected  = onNavSelected,
            onOpenEditProfile          = { navController.navigate(SettingsRoutes.EDIT_PROFILE) },
            onOpenMessagingSettings    = { navController.navigate(SettingsRoutes.MESSAGING) },
            onOpenCallSettings         = { navController.navigate(SettingsRoutes.CALLS) },
            onOpenPrivacySettings      = { navController.navigate(SettingsRoutes.PRIVACY) },
            onOpenStorageSettings      = { navController.navigate(SettingsRoutes.STORAGE) },
            onOpenNotificationSettings = { navController.navigate(SettingsRoutes.NOTIFICATIONS) },
            onLogOut                   = onLoggedOut,
        )
    }

    composable(SettingsRoutes.EDIT_PROFILE) {
        EditProfileScreen(onBack = { navController.popBackStack() })
    }

    composable(SettingsRoutes.MESSAGING) {
        MessagingSettingsScreen(onBack = { navController.popBackStack() })
    }

    composable(SettingsRoutes.CALLS) {
        CallSettingsScreen(onBack = { navController.popBackStack() })
    }

    composable(SettingsRoutes.PRIVACY) {
        PrivacySettingsScreen(
            onBack         = { navController.popBackStack() },
            onOpenSessions = { navController.navigate(SettingsRoutes.ACTIVE_SESSIONS) },
            onOpenPqStatus = { navController.navigate(SettingsRoutes.PQ_STATUS) },
        )
    }

    composable(SettingsRoutes.ACTIVE_SESSIONS) {
        ActiveSessionsScreen(onBack = { navController.popBackStack() })
    }

    composable(SettingsRoutes.PQ_STATUS) {
        PqStatusScreen(onBack = { navController.popBackStack() })
    }

    composable(SettingsRoutes.STORAGE) {
        StorageSettingsScreen(
            onBack           = { navController.popBackStack() },
            onOpenManageStorage = { navController.navigate(SettingsRoutes.STORAGE_MANAGE) },
        )
    }

    composable(SettingsRoutes.STORAGE_MANAGE) {
        StorageManageScreen(
            onBack         = { navController.popBackStack() },
            onOpenCategory = { category -> navController.navigate(SettingsRoutes.storageCategoryRoute(category)) },
        )
    }

    composable(
        route     = SettingsRoutes.STORAGE_CATEGORY,
        arguments = listOf(androidx.navigation.navArgument("category") { type = androidx.navigation.NavType.StringType }),
    ) { backStackEntry ->
        val categoryName = backStackEntry.arguments?.getString("category")
        val category = com.qryptin.chat.media.MediaCategory.entries
            .firstOrNull { it.name == categoryName } ?: com.qryptin.chat.media.MediaCategory.IMAGES
        StorageCategoryScreen(
            category = category,
            onBack   = { navController.popBackStack() },
        )
    }

    composable(SettingsRoutes.NOTIFICATIONS) {
        NotificationSettingsScreen(onBack = { navController.popBackStack() })
    }
}
