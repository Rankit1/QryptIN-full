package com.qryptin.contacts.ui.screens.addcontact

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.model.InviteMethod
import com.qryptin.contacts.viewmodel.AddContactViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ReviewNonQryptINContactScreen — Screen 3b
//  Shows a preview of the contact and lets the user choose
//  how to send the invite (SMS / Email) before saving.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewNonQryptINContactScreen(
    viewModel : AddContactViewModel,
    onBack    : () -> Unit,
    onCancel  : () -> Unit,
    onSaved   : () -> Unit,
) {
    val uiState      by viewModel.uiState.collectAsState()
    val initials      = viewModel.avatarInitials
    val fullPhone     = viewModel.formattedPhone
    val emailEnabled  = viewModel.isEmailInviteEnabled

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
                        text      = "Save & Invite",
                        onClick   = { viewModel.onSaveNonQryptINContact(); onSaved() },
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

            // ── Contact preview ───────────────────────────────
            Text(
                "Contact Preview",
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
                    horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
                ) {
                    ContactAvatar(initials = initials, size = 52)

                    Column {
                        Text(
                            uiState.displayName,
                            style = TypoTitleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            fullPhone,
                            style = TypoBodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (uiState.email.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Email,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    uiState.email,
                                    style = TypoBodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // ── Save-in summary ───────────────────────────────
            Text(
                "Save in",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ReviewSaveOptionCard(saveOption = uiState.saveOption)

            // ── Invite via ────────────────────────────────────
            Text(
                "Invite via",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            InviteMethodSelector(
                selected     = uiState.inviteMethod,
                emailEnabled = emailEnabled,
                onSelect     = { viewModel.onInviteMethodSelected(it) },
            )

            if (!emailEnabled) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "Add an email on the previous screen to enable Email invite.",
                        style = TypoBodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(Modifier.height(QryptDimens.SpaceLG))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  InviteMethodSelector
//  Two segmented option cards: SMS | Email
// ─────────────────────────────────────────────────────────────
@Composable
private fun InviteMethodSelector(
    selected     : InviteMethod,
    emailEnabled : Boolean,
    onSelect     : (InviteMethod) -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceSM),
    ) {
        InviteMethod.entries.forEach { method ->
            val isSelected = selected == method
            val isEnabled  = method == InviteMethod.SMS || emailEnabled

            InviteMethodCard(
                method     = method,
                isSelected = isSelected,
                isEnabled  = isEnabled,
                onSelect   = { if (isEnabled) onSelect(method) },
                modifier   = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun InviteMethodCard(
    method     : InviteMethod,
    isSelected : Boolean,
    isEnabled  : Boolean,
    onSelect   : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    val borderColor = when {
        !isEnabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        isSelected -> MaterialTheme.colorScheme.primary
        else       -> MaterialTheme.colorScheme.outline
    }
    val bgColor = when {
        !isEnabled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
        else       -> MaterialTheme.colorScheme.surface
    }
    val contentAlpha = if (isEnabled) 1f else 0.38f

    val icon: ImageVector = when (method) {
        InviteMethod.SMS   -> Icons.Rounded.Sms
        InviteMethod.EMAIL -> Icons.Rounded.Email
    }

    OutlinedCard(
        onClick  = onSelect,
        modifier = modifier,
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.outlinedCardColors(containerColor = bgColor),
        border   = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
        enabled = isEnabled,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(QryptDimens.SpaceMD),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(QryptDimens.SpaceXS),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = if (isEnabled) (if (isSelected) 0.18f else 0.10f) else 0.05f,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
                        modifier           = Modifier.size(20.dp),
                    )
                }
            }

            Text(
                text  = method.label,
                style = TypoBodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            )

            RadioButton(
                selected = isSelected,
                onClick  = null,
                enabled  = isEnabled,
                colors   = RadioButtonDefaults.colors(
                    selectedColor   = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline.copy(alpha = contentAlpha),
                ),
            )

            if (!isEnabled) {
                Text(
                    text  = "No email added",
                    style = TypoLabelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ReviewSaveOptionCard  (non-QryptIN review)
// ─────────────────────────────────────────────────────────────
@Composable
private fun ReviewSaveOptionCard(saveOption: ContactSaveOption) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        ),
        border = BorderStroke(
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
