package com.qryptin.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.ui.components.ChatCard
import com.qryptin.chat.ui.components.ChatFilterRow
import com.qryptin.chat.ui.components.ChatSearchBar
import com.qryptin.chat.viewmodel.ChatListViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ChatListScreen
//  Home tab. Search + filter chips above a scrollable chat
//  list; FAB opens a bottom sheet with "New Contact" / "Make
//  Group"; long-press enters multi-select for pin/mute/archive/
//  delete actions on the selection toolbar. Replaces the old
//  core/designsystem HomeScreen placeholder.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    currentNavDestination    : BottomNavDestination,
    onNavDestinationSelected : (BottomNavDestination) -> Unit,
    onChatClick              : (String) -> Unit,
    onNewContactClick        : () -> Unit,
    onStartConversationClick : () -> Unit,
    onMakeGroupClick         : () -> Unit,
    onSearchClick            : () -> Unit,
    viewModel                : ChatListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Issue 2 fix: safe-area-aware scaffold for the Home/Chats tab.
    QryptINSafeScaffold(
        bottomBar = {
            QryptBottomNavBar(selected = currentNavDestination, onSelect = onNavDestinationSelected)
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                FloatingActionButton(
                    onClick        = viewModel::onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "New chat")
                }
            }
        },
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedChatIds.size,
                    onClear       = viewModel::clearSelection,
                    onPin         = { viewModel.applyBulkAction(viewModel::togglePin) },
                    onMute        = { viewModel.applyBulkAction(viewModel::toggleMute) },
                    onArchive     = { viewModel.applyBulkAction(viewModel::archiveChat) },
                    onDelete      = { viewModel.applyBulkAction(viewModel::deleteChat) },
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            if (!uiState.isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceSM),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text("QryptIN", style = TypoDisplaySmall, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search everything", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                ChatSearchBar(
                    query         = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onClear       = { viewModel.onSearchQueryChanged("") },
                    modifier      = Modifier.padding(horizontal = QryptDimens.PaddingScreenH),
                )

                Spacer(Modifier.height(QryptDimens.SpaceSM))

                ChatFilterRow(selected = uiState.selectedFilter, onSelect = viewModel::onFilterSelected)

                Spacer(Modifier.height(QryptDimens.SpaceSM))
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.chats.isEmpty() -> {
                    EmptyChatListState(onStartConversationClick = onStartConversationClick)
                }
                else -> {
                    LazyColumn {
                        items(uiState.chats, key = { it.id }) { chat ->
                            ChatCard(
                                chat        = chat,
                                isSelected  = chat.id in uiState.selectedChatIds,
                                onClick     = {
                                    if (uiState.isSelectionMode) viewModel.onChatSelectionToggled(chat.id)
                                    else onChatClick(chat.id)
                                },
                                onLongPress = { viewModel.onChatLongPressed(chat.id) },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }

    if (uiState.isFabMenuOpen) {
        FabMenuSheet(
            onDismiss         = viewModel::dismissFabMenu,
            onNewContactClick = { viewModel.dismissFabMenu(); onNewContactClick() },
            onMakeGroupClick  = { viewModel.dismissFabMenu(); onMakeGroupClick() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FabMenuSheet(
    onDismiss         : () -> Unit,
    onNewContactClick : () -> Unit,
    onMakeGroupClick  : () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            FabMenuRow(Icons.Rounded.GroupAdd, "Make Group", "Create a new group with selected contacts", onMakeGroupClick)
            FabMenuRow(Icons.Rounded.PersonAdd, "New Contact", "Add a new contact to your list", onNewContactClick)
        }
    }
}

@Composable
private fun FabMenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceMD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(QryptDimens.SpaceMD))
        Column {
            Text(title, style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = TypoBodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount : Int,
    onClear       : () -> Unit,
    onPin         : () -> Unit,
    onMute        : () -> Unit,
    onArchive     : () -> Unit,
    onDelete      : () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClear) { Icon(Icons.Rounded.Close, contentDescription = "Clear selection") }
        },
        title   = { Text("$selectedCount selected", style = TypoTitleMedium) },
        actions = {
            TextButton(onClick = onPin)     { Text("Pin") }
            TextButton(onClick = onMute)    { Text("Mute") }
            TextButton(onClick = onArchive) { Text("Archive") }
            TextButton(
                onClick = onDelete,
                colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("Delete") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun EmptyChatListState(onStartConversationClick: () -> Unit) {
    Column(
        modifier             = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.ChatBubbleOutline,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("No chats yet", style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(4.dp))
        Text(
            // Issue 3 fix: contacts already exist by this point — the old
            // copy/CTA sent people to "add a contact" again instead of
            // letting them message someone already in their list.
            "Pick a contact to start a secure conversation.",
            style     = TypoBodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        QryptPrimaryButton(text = "Start Conversation", onClick = onStartConversationClick, modifier = Modifier.fillMaxWidth(0.6f))
    }
}
