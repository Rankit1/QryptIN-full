package com.qryptin.chat.navigation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.qryptin.chat.model.ContactSelectionMode
import com.qryptin.chat.repository.ChatRepository
import com.qryptin.chat.ui.screens.CallScreen
import com.qryptin.chat.ui.screens.ChatConversationScreen
import com.qryptin.chat.ui.screens.ChatListScreen
import com.qryptin.chat.ui.screens.ChatSearchScreen
import com.qryptin.chat.ui.screens.ContactSelectionScreen
import com.qryptin.chat.group.navigation.GroupRoutes
import com.qryptin.chat.group.navigation.groupCreationGraph
import com.qryptin.chat.ui.screens.MediaPreviewScreen
import com.qryptin.chat.ui.screens.VideoCallScreen
import com.qryptin.ui.navigation.AppRoutes
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

// ─────────────────────────────────────────────────────────────
//  ChatRoutes - Using centralized AppRoutes
// ─────────────────────────────────────────────────────────────
object ChatRoutes {
    val CHATS              = AppRoutes.Chat.CHATS
    val CONVERSATION       = AppRoutes.Chat.CONVERSATION
    val CREATE_GROUP       = GroupRoutes.GRAPH
    val CONTACT_SELECTION  = AppRoutes.Chat.CONTACT_SELECTION
    val CHAT_SEARCH        = AppRoutes.Chat.CHAT_SEARCH
    val CALL               = AppRoutes.Chat.CALL
    val VIDEO_CALL         = AppRoutes.Chat.VIDEO_CALL
    val MEDIA_PREVIEW      = AppRoutes.Chat.MEDIA_PREVIEW

    fun conversation(chatId: String) = AppRoutes.Chat.conversation(encode(chatId))

    fun contactSelection(mode: ContactSelectionMode) = "contact_selection/${mode.name}"

    fun call(peerName: String, peerAvatarUrl: String?) =
        "call/${encode(peerName)}/${encode(peerAvatarUrl ?: NONE_SENTINEL)}"

    fun videoCall(peerName: String, peerAvatarUrl: String?) =
        "video_call/${encode(peerName)}/${encode(peerAvatarUrl ?: NONE_SENTINEL)}"

    private const val NONE_SENTINEL = "__none__"

    internal fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
    internal fun decode(value: String): String = URLDecoder.decode(value, "UTF-8")
    internal fun decodeOrNull(value: String): String? = decode(value).takeIf { it != NONE_SENTINEL }
}

fun NavGraphBuilder.chatGraph(
    navController  : NavController,
    currentDest    : BottomNavDestination,
    onNavSelected  : (BottomNavDestination) -> Unit,
    chatRepository : ChatRepository,
) {
    composable(ChatRoutes.CHATS) {
        ChatListScreen(
            currentNavDestination    = currentDest,
            onNavDestinationSelected = onNavSelected,
            onChatClick              = { chatId -> navController.navigate(ChatRoutes.conversation(chatId)) },
            onNewContactClick        = { navController.navigate(AppRoutes.Contacts.ADD_CONTACT_PHONE) },
            onStartConversationClick = { navController.navigate(ChatRoutes.contactSelection(ContactSelectionMode.SINGLE)) },
            onMakeGroupClick         = { navController.navigate(ChatRoutes.CREATE_GROUP) },
            onSearchClick            = { navController.navigate(ChatRoutes.CHAT_SEARCH) },
        )
    }

    composable(
        route     = ChatRoutes.CONVERSATION,
        arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
    ) { entry ->
        val chatId = ChatRoutes.decode(entry.arguments?.getString("chatId").orEmpty())
        ChatConversationScreen(
            chatId      = chatId,
            onBack      = { navController.popBackStack() },
            onVoiceCall = { peerName, avatarUrl -> navController.navigate(ChatRoutes.call(peerName, avatarUrl)) },
            onVideoCall = { peerName, avatarUrl -> navController.navigate(ChatRoutes.videoCall(peerName, avatarUrl)) },
        )
    }

    groupCreationGraph(
        navController  = navController,
        onGroupCreated = { chatId ->
            navController.navigate(ChatRoutes.conversation(chatId)) {
                popUpTo(ChatRoutes.CHATS) { inclusive = false }
            }
        },
    )

    composable(
        route     = ChatRoutes.CONTACT_SELECTION,
        arguments = listOf(navArgument("mode") { type = NavType.StringType }),
    ) { entry ->
        val mode = ContactSelectionMode.valueOf(entry.arguments?.getString("mode") ?: ContactSelectionMode.SINGLE.name)
        val scope = rememberCoroutineScope()
        ContactSelectionScreen(
            mode      = mode,
            onBack    = { navController.popBackStack() },
            onConfirm = { selectedContacts ->
                // Issue 3/4/6 fix: picking a contact here used to just pop
                // back with no effect. It now creates (or reopens) a real,
                // persisted Room conversation and navigates straight into
                // it — the chat then shows up on the Home Chat list because
                // ChatListScreen observes the same conversation table.
                val contact = selectedContacts.firstOrNull()
                if (contact != null) {
                    scope.launch {
                        val chat = chatRepository.getOrCreateDirectChat(
                            contactId   = contact.friendId ?: contact.phone,
                            contactName = contact.displayName,
                            avatarUrl   = contact.avatarUrl,
                        )
                        navController.navigate(ChatRoutes.conversation(chat.id)) {
                            popUpTo(ChatRoutes.CHATS) { inclusive = false }
                        }
                    }
                } else {
                    navController.popBackStack()
                }
            },
        )
    }

    composable(ChatRoutes.CHAT_SEARCH) {
        ChatSearchScreen(
            onBack        = { navController.popBackStack() },
            onResultClick = { chatId ->
                navController.navigate(ChatRoutes.conversation(chatId)) {
                    popUpTo(ChatRoutes.CHATS) { inclusive = false }
                }
            },
        )
    }

    composable(
        route     = ChatRoutes.CALL,
        arguments = listOf(
            navArgument("peerName")  { type = NavType.StringType },
            navArgument("peerAvatar") { type = NavType.StringType },
        ),
    ) { entry ->
        CallScreen(
            peerName      = ChatRoutes.decode(entry.arguments?.getString("peerName").orEmpty()),
            peerAvatarUrl = ChatRoutes.decodeOrNull(entry.arguments?.getString("peerAvatar").orEmpty()),
            onEndCall     = { navController.popBackStack() },
        )
    }

    composable(
        route     = ChatRoutes.VIDEO_CALL,
        arguments = listOf(
            navArgument("peerName")  { type = NavType.StringType },
            navArgument("peerAvatar") { type = NavType.StringType },
        ),
    ) { entry ->
        VideoCallScreen(
            peerName      = ChatRoutes.decode(entry.arguments?.getString("peerName").orEmpty()),
            peerAvatarUrl = ChatRoutes.decodeOrNull(entry.arguments?.getString("peerAvatar").orEmpty()),
            onEndCall     = { navController.popBackStack() },
        )
    }

    composable(ChatRoutes.MEDIA_PREVIEW) {
        MediaPreviewScreen(
            items  = emptyList(),
            onBack = { navController.popBackStack() },
            onSend = { navController.popBackStack() },
        )
    }
}
