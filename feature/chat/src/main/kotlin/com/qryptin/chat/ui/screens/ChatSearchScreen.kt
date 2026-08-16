package com.qryptin.chat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.model.ChatSearchResult
import com.qryptin.chat.model.ChatSearchScope
import com.qryptin.chat.ui.components.ChatSearchBar
import com.qryptin.chat.viewmodel.ChatSearchViewModel
import com.qryptin.core.designsystem.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  ChatSearchScreen
//  Unified live search across chat titles, message text, and
//  file attachment names, scoped by tab.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSearchScreen(
    onBack         : () -> Unit,
    onResultClick  : (chatId: String) -> Unit,
    viewModel      : ChatSearchViewModel = viewModel(),
) {
    val uiState by viewModel.fullUiState.collectAsState()
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                ChatSearchBar(
                    query         = uiState.query,
                    onQueryChange = viewModel::onQueryChanged,
                    onClear       = { viewModel.onQueryChanged("") },
                    modifier      = Modifier.weight(1f).focusRequester(focusRequester).padding(end = 8.dp),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            LazyRow(
                contentPadding = PaddingValues(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceSM),
                horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceSM),
            ) {
                items(ChatSearchScope.entries.toList()) { scope ->
                    FilterChip(
                        selected = scope == uiState.scope,
                        onClick  = { viewModel.onScopeChanged(scope) },
                        label    = { Text(scope.label, style = TypoLabelMedium) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            selectedLabelColor     = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }

            when {
                uiState.isSearching -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                uiState.query.isBlank() -> EmptySearchState("Search chats, messages, and files")
                uiState.results.isEmpty() -> EmptySearchState("No results for \"${uiState.query}\"")
                else -> LazyColumn {
                    items(uiState.results, key = { "${it.chatId}_${it.timestamp}" }) { result ->
                        SearchResultRow(result, onClick = { onResultClick(result.chatId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(result: ChatSearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QryptAvatar(name = result.chatTitle, size = 40.dp)
        Spacer(Modifier.width(QryptDimens.SpaceMD))
        Column(modifier = Modifier.weight(1f)) {
            Text(result.chatTitle, style = TypoBodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(result.snippet, style = TypoBodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        Text(
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(result.timestamp)),
            style = TypoLabelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptySearchState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.SearchOff,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(message, style = TypoBodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
