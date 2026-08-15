package com.qryptin.chat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.model.ContactSelectionMode
import com.qryptin.chat.ui.components.ChatSearchBar
import com.qryptin.chat.viewmodel.ContactSelectionViewModel
import com.qryptin.contacts.model.Contact
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ContactSelectionScreen
//  Generic picker — used for starting a new direct chat
//  (SINGLE) or sharing a contact as an attachment (also
//  SINGLE today; MULTI is wired for future multi-share).
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSelectionScreen(
    mode       : ContactSelectionMode,
    title      : String = "Select Contact",
    onBack     : () -> Unit,
    onConfirm  : (List<Contact>) -> Unit,
    viewModel  : ContactSelectionViewModel = viewModel(),
) {
    LaunchedEffect(mode) { viewModel.setMode(mode) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                title = { Text(title, style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground) },
                actions = {
                    if (mode == ContactSelectionMode.MULTI && uiState.selectedContacts.isNotEmpty()) {
                        TextButton(onClick = { onConfirm(uiState.selectedContacts) }) {
                            Text("Done (${uiState.selectedContacts.size})")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ChatSearchBar(
                query         = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClear       = { viewModel.onSearchQueryChanged("") },
                placeholder   = "Search contacts…",
                modifier      = Modifier.padding(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceSM),
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn {
                    items(uiState.availableContacts, key = { it.id }) { contact ->
                        val isSelected = contact in uiState.selectedContacts
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onContactToggled(contact)
                                    if (mode == ContactSelectionMode.SINGLE) onConfirm(listOf(contact))
                                }
                                .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            QryptAvatar(name = contact.displayName, avatarUrl = contact.avatarUrl, size = 44.dp)
                            Spacer(Modifier.width(QryptDimens.SpaceMD))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.displayName, style = TypoBodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                contact.lastSeen?.let {
                                    Text(it, style = TypoBodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (mode == ContactSelectionMode.MULTI && isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
