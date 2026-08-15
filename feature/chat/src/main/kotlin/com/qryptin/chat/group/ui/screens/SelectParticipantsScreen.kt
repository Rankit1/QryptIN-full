package com.qryptin.chat.group.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qryptin.chat.group.ui.components.GroupContactRow
import com.qryptin.chat.group.ui.components.GroupWizardTopBar
import com.qryptin.chat.group.ui.components.SelectedMembersBar
import com.qryptin.chat.group.viewmodel.GroupCreationViewModel
import com.qryptin.chat.ui.components.ChatSearchBar
import com.qryptin.core.designsystem.ui.theme.QryptDimens
import com.qryptin.core.designsystem.ui.theme.TypoBodySmall

// ─────────────────────────────────────────────────────────────
//  SelectParticipantsScreen  — Step 1 of group creation wizard
// ─────────────────────────────────────────────────────────────
@Composable
fun SelectParticipantsScreen(
    viewModel : GroupCreationViewModel,
    onBack    : () -> Unit,
    onNext    : () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GroupWizardTopBar(
                title         = "Select Participants",
                onBack        = onBack,
                actionLabel   = "Next",
                onAction      = onNext,
                actionEnabled = uiState.selectedMembers.isNotEmpty(),
            )
        },
        bottomBar = {
            SelectedMembersBar(
                selected = uiState.selectedMembers,
                onRemove = viewModel::onRemoveMember,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ChatSearchBar(
                query         = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClear       = { viewModel.onSearchQueryChanged("") },
                placeholder   = "Search by name or phone…",
                modifier      = Modifier
                    .padding(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceSM),
            )

            if (uiState.filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Text(
                        if (uiState.searchQuery.isBlank()) "No QryptIN contacts yet"
                        else "No results for \"${uiState.searchQuery}\"",
                        style = TypoBodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LazyColumn(
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(uiState.filteredContacts, key = { it.id }) { contact ->
                    GroupContactRow(
                        contact    = contact,
                        isSelected = contact in uiState.selectedMembers,
                        onClick    = { viewModel.onContactToggled(contact) },
                    )
                }
            }
        }
    }
}
