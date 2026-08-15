package com.qryptin.contacts.ui.screens.addcontact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.viewmodel.AddContactViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ReviewContactScreen — Screen 3
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewContactScreen(
    viewModel : AddContactViewModel,
    onBack    : () -> Unit,
    onCancel  : () -> Unit,
    onSaved   : () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val effectiveName = uiState.displayName.ifBlank { uiState.serverName }
    val initials      = viewModel.avatarInitials
    val fullPhone     = viewModel.formattedPhone

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                title = {
                    Text(
                        "Review & Save",
                        style = TypoTitleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", style = TypoBodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Surface(
                color           = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = QryptDimens.PaddingScreenH,
                            vertical   = QryptDimens.SpaceMD,
                        ),
                ) {
                    if (uiState.saveError != null) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier              = Modifier.padding(bottom = 8.dp),
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text  = uiState.saveError ?: "",
                                style = TypoBodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    QryptPrimaryButton(
                        text      = "Save",
                        onClick   = { viewModel.onSaveContact(); onSaved() },
                        enabled   = !uiState.isSaving,
                        isLoading = uiState.isSaving,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = QryptDimens.PaddingScreenH),
            verticalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
        ) {

            Spacer(Modifier.height(QryptDimens.SpaceSM))

            // ── Section label ─────────────────────────────────
            Text(
                "Contact Preview",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // ── Contact preview card ──────────────────────────
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
                colors   = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier              = Modifier.padding(QryptDimens.SpaceMD),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
                ) {
                    // Avatar
                    ContactAvatar(initials = initials, size = 52)

                    Column {
                        Text(
                            effectiveName,
                            style = TypoTitleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            fullPhone,
                            style = TypoBodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Save-in label ─────────────────────────────────
            Text(
                "Save in",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // ── Save option summary ───────────────────────────
            ReviewSaveOptionCard(saveOption = uiState.saveOption)

            // ── Email section ─────────────────────────────────
            Text(
                "Email Address",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
                colors   = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier              = Modifier.padding(QryptDimens.SpaceMD),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceSM),
                ) {
                    Icon(
                        Icons.Rounded.Email,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    if (uiState.email.isBlank()) {
                        Column {
                            Text(
                                "name@example.com",
                                style = TypoBodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                            Text(
                                "(Optional)",
                                style = TypoBodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                        }
                    } else {
                        Text(
                            uiState.email,
                            style = TypoBodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(Modifier.height(QryptDimens.SpaceLG))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ReviewSaveOptionCard
// ─────────────────────────────────────────────────────────────
@Composable
private fun ReviewSaveOptionCard(saveOption: ContactSaveOption) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(QryptDimens.SpaceMD),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
        ) {
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (saveOption) {
                            ContactSaveOption.SYNCED_WITH_SIM -> Icons.Rounded.SimCard
                            ContactSaveOption.QRYPTIN_ONLY    -> Icons.Rounded.PhoneAndroid
                        },
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column {
                Text(
                    saveOption.title,
                    style = TypoBodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    saveOption.description,
                    style = TypoBodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ContactAvatar — initials circle (shared / reusable)
// ─────────────────────────────────────────────────────────────
@Composable
fun ContactAvatar(
    initials : String,
    size     : Int = 48,
) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape    = RoundedCornerShape(50),
        color    = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text     = initials,
                style    = TypoTitleMedium.copy(
                    fontSize   = (size * 0.35f).sp,
                    fontWeight = FontWeight.Bold,
                ),
                color    = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
