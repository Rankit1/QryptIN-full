package com.qryptin.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.model.ConversationSheet
import com.qryptin.chat.model.MediaPreviewItem
import com.qryptin.chat.ui.components.ChatInputBar
import com.qryptin.chat.ui.components.EmojiPicker
import com.qryptin.chat.ui.components.MessageBubble
import com.qryptin.chat.ui.components.StickerGrid
import com.qryptin.chat.ui.components.TypingIndicatorDots
import com.qryptin.chat.ui.screens.sheets.AttachmentBottomSheet
import com.qryptin.chat.viewmodel.ConversationViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ChatConversationScreen
//  The encrypted 1:1 / group messaging surface. Loaded by chatId
//  via LaunchedEffect, matching the ContactDetailScreen pattern
//  already used by feature:contacts.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    chatId         : String,
    onBack         : () -> Unit,
    onVoiceCall    : (peerName: String, peerAvatarUrl: String?) -> Unit,
    onVideoCall    : (peerName: String, peerAvatarUrl: String?) -> Unit,
    viewModel      : ConversationViewModel = viewModel(),
) {
    LaunchedEffect(chatId) { viewModel.load(chatId) }

    val uiState by viewModel.uiState.collectAsState()
    val chat = uiState.chat
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // ── Real media pickers (Issue 4 fix) ────────────────────────
    // Each launcher hands back a content:// Uri, which MediaStorage copies
    // into app-private storage so the attachment survives app restarts.
    val imageLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.onPickerLaunched()
        val attachment = com.qryptin.chat.media.MediaStorage.persist(context, uri, com.qryptin.chat.model.MessageType.IMAGE)
        viewModel.onMediaPicked(com.qryptin.chat.model.MessageType.IMAGE, attachment)
    }
    val videoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.onPickerLaunched()
        val attachment = com.qryptin.chat.media.MediaStorage.persist(context, uri, com.qryptin.chat.model.MessageType.VIDEO)
        viewModel.onMediaPicked(com.qryptin.chat.model.MessageType.VIDEO, attachment)
    }
    val documentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.onPickerLaunched()
        val attachment = com.qryptin.chat.media.MediaStorage.persist(context, uri, com.qryptin.chat.model.MessageType.DOCUMENT)
        viewModel.onMediaPicked(com.qryptin.chat.model.MessageType.DOCUMENT, attachment)
    }
    val pickerLauncher = remember(imageLauncher, videoLauncher, documentLauncher) {
        AttachmentPickerLauncher(
            onImage    = { imageLauncher.launch("image/*") },
            onVideo    = { videoLauncher.launch("video/*") },
            onDocument = {
                documentLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/zip",
                        "application/vnd.android.package-archive",
                        "audio/*",
                        "*/*",
                    )
                )
            },
        )
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.lastIndex)
    }

    // Issue 2 fix: safe-area-aware scaffold — also keeps the message
    // input bar clear of the on-screen keyboard (IME) and nav bar.
    QryptINSafeScaffold(
        topBar = {
            ConversationTopBar(
                title       = chat?.title ?: "",
                avatarUrl   = chat?.avatarUrl,
                isOnline    = chat?.isOnline == true,
                isTyping    = uiState.isPeerTyping,
                isGroup     = chat?.isGroup == true,
                onBack      = onBack,
                onVoiceCall = { onVoiceCall(chat?.title.orEmpty(), chat?.avatarUrl) },
                onVideoCall = { onVideoCall(chat?.title.orEmpty(), chat?.avatarUrl) },
            )
        },
        bottomBar = {
            Column {
                uiState.replyTarget?.let { reply ->
                    ReplyPreviewBar(senderName = reply.senderName, text = reply.text.orEmpty(), onCancel = viewModel::cancelReply)
                }
                ChatInputBar(
                    draftText     = uiState.draftText,
                    onDraftChange = viewModel::onDraftTextChanged,
                    onSend        = viewModel::sendDraft,
                    onEmojiClick  = { viewModel.openSheet(ConversationSheet.EMOJI) },
                    onAttachClick = { viewModel.openSheet(ConversationSheet.ATTACHMENT) },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    state           = listState,
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceSM),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message        = message,
                            showSenderName = chat?.isGroup == true,
                            onLongPress    = { viewModel.onMessageLongPressed(message.id) },
                        )
                    }
                }
            }
        }
    }

    uiState.selectedMessageId?.let { messageId ->
        val message = uiState.messages.firstOrNull { it.id == messageId }
        if (message != null) {
            MessageActionSheet(
                isOutgoing        = message.isOutgoing,
                onDismiss         = viewModel::clearMessageSelection,
                onReply           = { viewModel.onReplyTo(message) },
                onDeleteForMe     = { viewModel.deleteForMe(messageId) },
                onDeleteForEveryone = { viewModel.deleteForEveryone(messageId) },
                onReact           = { emoji -> viewModel.react(messageId, emoji) },
            )
        }
    }

    when (uiState.activeSheet) {
        ConversationSheet.ATTACHMENT -> AttachmentBottomSheet(
            onDismiss = viewModel::dismissSheet,
            onAttachmentTypePicked = { type -> pickerLauncher.launch(type) },
        )
        ConversationSheet.STICKER -> StickerPickerSheet(
            onDismiss        = viewModel::dismissSheet,
            onStickerPicked  = viewModel::onStickerPicked,
        )
        ConversationSheet.EMOJI -> EmojiPickerSheet(
            onDismiss      = viewModel::dismissSheet,
            onEmojiPicked  = viewModel::onEmojiPicked,
        )
        ConversationSheet.NONE -> Unit
    }

    if (uiState.isAttaching) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }

    uiState.pendingAttachment?.let { attachment ->
        val isVideo = uiState.pendingType == com.qryptin.chat.model.MessageType.VIDEO
        MediaPreviewScreen(
            items  = listOf(MediaPreviewItem(uri = attachment.localUri, isVideo = isVideo)),
            onBack = viewModel::cancelPendingAttachment,
            onSend = { caption -> viewModel.confirmPendingAttachment(caption) },
        )
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Surfaced via the attachment failure path (e.g. picker grant expired mid-copy).
            // A Snackbar host can replace this once one is threaded through the screen.
        }
    }
}

private class AttachmentPickerLauncher(
    val onImage    : () -> Unit,
    val onVideo    : () -> Unit,
    val onDocument : () -> Unit,
) {
    fun launch(type: com.qryptin.chat.model.MessageType) {
        when (type) {
            com.qryptin.chat.model.MessageType.IMAGE    -> onImage()
            com.qryptin.chat.model.MessageType.VIDEO    -> onVideo()
            com.qryptin.chat.model.MessageType.DOCUMENT -> onDocument()
            // Audio/sticker/system aren't offered from the attachment sheet today.
            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationTopBar(
    title       : String,
    avatarUrl   : String?,
    isOnline    : Boolean,
    isTyping    : Boolean,
    isGroup     : Boolean,
    onBack      : () -> Unit,
    onVoiceCall : () -> Unit,
    onVideoCall : () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QryptAvatar(name = title, avatarUrl = avatarUrl, size = 38.dp, isOnline = isOnline && !isGroup)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(title, style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                    when {
                        isTyping -> Row(verticalAlignment = Alignment.CenterVertically) {
                            TypingIndicatorDots(modifier = Modifier.padding(end = 4.dp))
                            Text("typing…", style = TypoBodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        isOnline && !isGroup -> Text("Online", style = TypoBodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else -> SecureStatusBadge()
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onVoiceCall) { Icon(Icons.Rounded.Call, contentDescription = "Voice call", tint = MaterialTheme.colorScheme.onBackground) }
            IconButton(onClick = onVideoCall) { Icon(Icons.Rounded.Videocam, contentDescription = "Video call", tint = MaterialTheme.colorScheme.onBackground) }
            IconButton(onClick = {}) { Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onBackground) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun ReplyPreviewBar(senderName: String, text: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Reply, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Replying to $senderName", style = TypoLabelSmall.copy(), color = MaterialTheme.colorScheme.primary)
            Text(text, style = TypoBodySmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Rounded.Close, contentDescription = "Cancel reply", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageActionSheet(
    isOutgoing          : Boolean,
    onDismiss           : () -> Unit,
    onReply             : () -> Unit,
    onDeleteForMe       : () -> Unit,
    onDeleteForEveryone : () -> Unit,
    onReact             : (String) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf("👍", "❤️", "😂", "😮", "😢", "🙏").forEach { emoji ->
                    Text(
                        text     = emoji,
                        style    = TypoTitleMedium,
                        modifier = Modifier.padding(8.dp).clickable { onReact(emoji) },
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ActionRow(Icons.Rounded.Reply, "Reply", onReply)
            ActionRow(Icons.Rounded.Delete, "Delete for me", onDeleteForMe)
            if (isOutgoing) {
                ActionRow(Icons.Rounded.Delete, "Delete for everyone", onDeleteForEveryone, isDestructive = true)
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon          : androidx.compose.ui.graphics.vector.ImageVector,
    label         : String,
    onClick       : () -> Unit,
    isDestructive : Boolean = false,
) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = TypoBodyLarge, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickerPickerSheet(onDismiss: () -> Unit, onStickerPicked: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        StickerGrid(recentlyUsed = emptyList(), onStickerSelected = { onStickerPicked(it); onDismiss() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiPickerSheet(onDismiss: () -> Unit, onEmojiPicked: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        EmojiPicker(onEmojiSelected = onEmojiPicked)
    }
}
