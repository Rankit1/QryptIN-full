package com.qryptin.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.ui.components.ChatSearchBar
import com.qryptin.chat.viewmodel.GroupCreationNavEvent
import com.qryptin.chat.viewmodel.GroupCreationViewModel
import com.qryptin.contacts.model.Contact
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  GroupCreationScreen
//  Single-screen flow: pick from saved contacts, name the
//  group, optionally set an avatar, then create. Selected
//  contacts show as removable chips above the search field.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreationScreen(
    onBack             : () -> Unit,
    onGroupCreated     : (chatId: String) -> Unit,
    viewModel          : GroupCreationViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is GroupCreationNavEvent.NavigateToConversation -> onGroupCreated(event.chatId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                title = { Text("New Group", style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            GroupAvatarAndName(
                groupName     = uiState.groupName,
                onNameChange  = viewModel::onGroupNameChanged,
                avatarUri     = uiState.groupAvatarUri,
                onAvatarClick = { viewModel.onGroupAvatarPicked("mock://group-avatar") },
            )

            if (uiState.selectedContacts.isNotEmpty()) {
                SelectedContactsRow(
                    contacts = uiState.selectedContacts,
                    onRemove = viewModel::onContactToggled,
                )
            }

            ChatSearchBar(
                query         = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClear       = { viewModel.onSearchQueryChanged("") },
                placeholder   = "Search contacts…",
                modifier      = Modifier.padding(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceSM),
            )

            Text(
                "${uiState.selectedContacts.size} selected (minimum 2)",
                style    = TypoLabelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = QryptDimens.PaddingScreenH, vertical = 4.dp),
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.availableContacts, key = { it.id }) { contact ->
                    SelectableContactRow(
                        contact    = contact,
                        isSelected = contact in uiState.selectedContacts,
                        onClick    = { viewModel.onContactToggled(contact) },
                    )
                }
            }

            QryptPrimaryButton(
                text      = if (uiState.isCreating) "Creating…" else "Create Group",
                onClick   = viewModel::createGroup,
                enabled   = uiState.canCreate,
                isLoading = uiState.isCreating,
                modifier  = Modifier.padding(QryptDimens.PaddingScreenH),
            )
        }
    }
}

@Composable
private fun GroupAvatarAndName(
    groupName     : String,
    onNameChange  : (String) -> Unit,
    avatarUri     : String?,
    onAvatarClick : () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(QryptDimens.PaddingScreenH),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarUri == null) {
                Icon(Icons.Rounded.PhotoCamera, contentDescription = "Set group photo", tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(16.dp))
        OutlinedTextField(
            value         = groupName,
            onValueChange = onNameChange,
            placeholder   = { Text("Group name", style = TypoBodyLarge) },
            singleLine    = true,
            modifier      = Modifier.weight(1f),
            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun SelectedContactsRow(contacts: List<Contact>, onRemove: (Contact) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = QryptDimens.PaddingScreenH),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(contacts, key = { it.id }) { contact ->
            AssistChip(
                onClick = { onRemove(contact) },
                label   = { Text(contact.displayName, style = TypoLabelMedium) },
                colors  = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            )
        }
    }
}

@Composable
private fun SelectableContactRow(contact: Contact, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QryptAvatar(name = contact.displayName, size = 44.dp)
        Spacer(Modifier.width(QryptDimens.SpaceMD))
        Text(contact.displayName, style = TypoBodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Checkbox(checked = isSelected, onCheckedChange = { onClick() }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
    }
}
