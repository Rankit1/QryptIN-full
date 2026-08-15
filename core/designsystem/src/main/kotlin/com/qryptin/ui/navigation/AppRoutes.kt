package com.qryptin.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PeopleOutline
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Centralized Route constants for the entire app.
 */
object AppRoutes {
    // Root segments
    const val AUTH = "auth"
    const val MAIN = "main"

    // Auth module routes
    object Auth {
        const val GRAPH = "auth_graph"
        const val SPLASH = "splash"
        const val PHONE_INPUT = "phone_input"
        const val OTP_VERIFY = "otp_verify"
        const val USER_FOUND = "user_found"
        const val REGISTER = "register"
    }

    // Main/Feature routes
    object Chat {
        const val CHATS = "chats"
        const val CONVERSATION = "conversation/{chatId}"
        const val CREATE_GROUP = "create_group_graph"
        const val CONTACT_SELECTION = "contact_selection/{mode}"
        const val CHAT_SEARCH = "chat_search"
        const val CALL = "call/{peerName}/{peerAvatar}"
        const val VIDEO_CALL = "video_call/{peerName}/{peerAvatar}"
        const val MEDIA_PREVIEW = "media_preview"

        fun conversation(chatId: String) = "conversation/$chatId"
    }

    object Contacts {
        const val CONTACTS = "contacts"
        const val CONTACT_DETAIL = "contacts/{contactId}"
        const val EDIT_CONTACT = "contacts/{contactId}/edit"
        const val ADD_CONTACT_PHONE = "contacts/add/phone"
        const val ADD_CONTACT_EXISTING = "contacts/add/existing"
        const val ADD_CONTACT_REVIEW = "contacts/add/review"
        const val ADD_CONTACT_SUCCESS = "contacts/add/success"
        const val ADD_CONTACT_NON_QRYPTIN = "contacts/add/non-qryptin"
        const val ADD_CONTACT_NON_QRYPTIN_REVIEW = "contacts/add/non-qryptin/review"

        fun contactDetail(id: String) = "contacts/$id"
        fun editContact(id: String) = "contacts/$id/edit"
    }

    object Calls {
        const val CALLS_HOME = "calls"
        const val SELECT_CONTACT = "calls/select_contact"
        const val VOICE_CALL = "calls/voice/{contactId}/{contactName}"
        const val VIDEO_CALL = "calls/video/{contactId}/{contactName}"

        fun voiceCall(contactId: String, contactName: String) =
            "calls/voice/$contactId/${contactName.replace(" ", "_")}"

        fun videoCall(contactId: String, contactName: String) =
            "calls/video/$contactId/${contactName.replace(" ", "_")}"
    }

    object Settings {
        const val SETTINGS = "settings"
    }
}

/**
 * Sealed class for Bottom Navigation Items.
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    data object Chats : BottomNavItem(
        route = AppRoutes.Chat.CHATS,
        title = "Chats",
        icon = Icons.Rounded.ChatBubbleOutline,
        selectedIcon = Icons.Rounded.ChatBubble
    )

    data object Contacts : BottomNavItem(
        route = AppRoutes.Contacts.CONTACTS,
        title = "Contacts",
        icon = Icons.Rounded.PeopleOutline,
        selectedIcon = Icons.Rounded.People
    )

    data object Calls : BottomNavItem(
        route = AppRoutes.Calls.CALLS_HOME,
        title = "Calls",
        icon = Icons.Outlined.Call,
        selectedIcon = Icons.Rounded.Call
    )

    data object Settings : BottomNavItem(
        route = AppRoutes.Settings.SETTINGS,
        title = "Settings",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Rounded.Settings
    )

    companion object {
        val items = listOf(Chats, Contacts, Calls, Settings)

        /**
         * Safely finds a BottomNavItem for a given route.
         * Handles nested routes by checking if the route starts with the item's route.
         */
        fun fromRoute(route: String?): BottomNavItem? {
            if (route == null) return null
            // First try exact match
            items.find { it.route == route }?.let { return it }
            
            // Then try prefix match for nested routes
            return when {
                route.startsWith("conversation") -> Chats
                route.startsWith("contacts") -> Contacts
                route.startsWith("calls") -> Calls
                route.startsWith("settings") -> Settings
                else -> null
            }
        }
    }
}
