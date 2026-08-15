package com.qryptin.calls.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.calls.model.*
import com.qryptin.calls.ui.components.*
import com.qryptin.calls.viewmodel.CallsViewModel
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination
import com.qryptin.core.designsystem.ui.theme.QryptBottomNavBar
import com.qryptin.core.designsystem.ui.theme.QryptINSafeScaffold
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsHomeScreen(
    viewModel           : CallsViewModel        = viewModel(),
    currentNavDest      : BottomNavDestination  = BottomNavDestination.CALLS,
    onNavSelected       : (BottomNavDestination) -> Unit = {},
    onNewCall           : () -> Unit = {},
    onVoiceCall         : (CallModel) -> Unit = {},
    onVideoCall         : (CallModel) -> Unit = {},
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Issue 2 fix: safe-area-aware scaffold for the Calls tab.
    QryptINSafeScaffold(
        topBar    = {
            CallsTopBar(
                searchQuery    = searchQuery,
                onSearchChange = viewModel::onSearchQueryChanged,
                onNewCall      = onNewCall,
            )
        },
        bottomBar = {
            QryptBottomNavBar(selected = currentNavDest, onSelect = onNavSelected)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier          = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding    = PaddingValues(bottom = 16.dp),
            ) {
                // E2E banner
                item {
                    SecureInfoBanner(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                if (uiState.groups.isEmpty()) {
                    item {
                        EmptyCallsState(modifier = Modifier.padding(top = 48.dp))
                    }
                } else {
                    uiState.groups.forEach { group ->
                        // Section header
                        item(key = "header_${group.label}") {
                            Text(
                                text     = group.label,
                                style    = MaterialTheme.typography.labelMedium,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start  = 16.dp,
                                    top    = 20.dp,
                                    bottom = 4.dp,
                                ),
                            )
                        }

                        // Call rows inside a card
                        item(key = "group_card_${group.label}") {
                            Card(
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape     = RoundedCornerShape(16.dp),
                                colors    = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                elevation = CardDefaults.cardElevation(1.dp),
                            ) {
                                group.calls.forEach { call ->
                                    CallHistoryCard(
                                        call        = call,
                                        timestamp   = formatTimestamp(call.timestamp, group.label),
                                        onVoiceCall = { onVoiceCall(call) },
                                        onVideoCall = { onVideoCall(call) },
                                        onDelete    = { viewModel.deleteCall(call.callId) },
                                        onCallAgain = { onVoiceCall(call) },
                                    )
                                }
                            }
                        }
                    }

                    // Bottom security card
                    item {
                        BottomSecurityCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Top bar with title, search, and new-call button
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallsTopBar(
    searchQuery    : String,
    onSearchChange : (String) -> Unit,
    onNewCall      : () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
    ) {
        // Title row
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text       = "Calls",
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
            )
            TextButton(
                onClick = onNewCall,
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Rounded.AddIcCall, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("New Call", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }

        // Search field
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            modifier      = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder   = { Text("Search by name or phone number") },
            leadingIcon   = { Icon(Icons.Rounded.Search, null) },
            trailingIcon  = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Rounded.Close, null) } }
            } else null,
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Empty state
// ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyCallsState(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Rounded.PhoneDisabled,
            null,
            modifier = Modifier.size(64.dp),
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Text(
            "No calls yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Your QryptIN call history will appear here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Timestamp formatting
// ─────────────────────────────────────────────────────────────
private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
private val dayFormat  = SimpleDateFormat("EEE", Locale.getDefault())

private fun formatTimestamp(epochMillis: Long, group: String): String =
    when (group) {
        "Recent"    -> timeFormat.format(Date(epochMillis))
        "Yesterday" -> "Yesterday"
        else        -> dayFormat.format(Date(epochMillis))
    }
