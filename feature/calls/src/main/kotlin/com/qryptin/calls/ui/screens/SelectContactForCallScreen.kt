package com.qryptin.calls.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.calls.model.CallType
import com.qryptin.calls.ui.components.CallAvatar
import com.qryptin.calls.viewmodel.SelectContactForCallViewModel
import com.qryptin.contacts.model.Contact

// ─────────────────────────────────────────────────────────────
//  SelectContactForCallScreen
//  Shown when user taps "New Call". Displays all saved contacts,
//  live from Room via ContactsRepository, and lets the user pick
//  voice or video. Refreshes instantly as contacts are added —
//  no more relying on a stale list passed in from the nav graph.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectContactForCallScreen(
    onBack : () -> Unit,
    onCall : (Contact, CallType) -> Unit,
) {
    val viewModel: SelectContactForCallViewModel = viewModel()
    val filtered by viewModel.filteredContacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("New Call", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Search
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder   = { Text("Search contacts") },
                leadingIcon   = { Icon(Icons.Rounded.Search, null) },
                singleLine    = true,
                shape         = RoundedCornerShape(14.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
            )

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                filtered.isEmpty() && searchQuery.isBlank() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.PersonOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No saved contacts yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "Add a contact first to start a call",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                filtered.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No contacts found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(filtered, key = { it.id }) { contact ->
                            ContactCallRow(
                                contact     = contact,
                                onVoiceCall = { onCall(contact, CallType.VOICE) },
                                onVideoCall = { onCall(contact, CallType.VIDEO) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactCallRow(
    contact     : Contact,
    onVoiceCall : () -> Unit,
    onVideoCall : () -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVoiceCall)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CallAvatar(name = contact.displayName)

        Column(modifier = Modifier.weight(1f)) {
            Text(contact.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(contact.phone, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Voice call button
        FilledTonalIconButton(onClick = onVoiceCall) {
            Icon(Icons.Rounded.Call, "Voice Call", modifier = Modifier.size(18.dp))
        }
        // Video call button
        FilledTonalIconButton(onClick = onVideoCall) {
            Icon(Icons.Rounded.Videocam, "Video Call", modifier = Modifier.size(18.dp))
        }
    }
    HorizontalDivider(
        modifier  = Modifier.padding(start = 76.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}
