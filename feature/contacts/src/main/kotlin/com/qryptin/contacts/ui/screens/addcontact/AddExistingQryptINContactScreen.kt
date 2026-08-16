package com.qryptin.contacts.ui.screens.addcontact

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.ui.components.SaveOptionCard
import com.qryptin.contacts.viewmodel.AddContactViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  AddExistingQryptINContactScreen — Screen 2
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExistingQryptINContactScreen(
    viewModel : AddContactViewModel,
    onBack    : () -> Unit,
    onCancel  : () -> Unit,
) {
    val uiState       by viewModel.uiState.collectAsState()
    val nameFocus      = remember { FocusRequester() }
    val nicknameFocus  = remember { FocusRequester() }

    LaunchedEffect(uiState.isEditingName) {
        if (uiState.isEditingName) nameFocus.requestFocus()
    }
    LaunchedEffect(uiState.isNicknameMode) {
        if (uiState.isNicknameMode) nicknameFocus.requestFocus()
    }

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
                        "Contact Details",
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
                color     = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                QryptPrimaryButton(
                    text     = "Next",
                    onClick  = { viewModel.onProceedToReview() },
                    enabled  = uiState.displayName.isNotBlank() || uiState.serverName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = QryptDimens.PaddingScreenH,
                            vertical   = QryptDimens.SpaceMD,
                        ),
                )
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

            // ── "Contact found" banner ────────────────────────
            ContactFoundBanner()

            // ── Contact name card ─────────────────────────────
            ContactNameCard(
                serverName     = uiState.serverName,
                displayName    = uiState.displayName,
                isEditing      = uiState.isEditingName,
                isNicknameMode = uiState.isNicknameMode,
                nameFocus      = nameFocus,
                nicknameFocus  = nicknameFocus,
                onEditToggle   = { viewModel.onEditNameToggle() },
                onNicknameToggle = { viewModel.onNicknameModeToggle() },
                onDisplayNameChanged = { viewModel.onDisplayNameChanged(it) },
            )

            // ── Save-in label ─────────────────────────────────
            Text(
                "Save in",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = QryptDimens.SpaceXS),
            )

            // ── Save option cards ─────────────────────────────
            ContactSaveOption.entries.forEach { option ->
                SaveOptionCard(
                    option     = option,
                    isSelected = uiState.saveOption == option,
                    onSelect   = { viewModel.onSaveOptionSelected(option) },
                )
            }

            // ── Email field ───────────────────────────────────
            EmailSection(
                email    = uiState.email,
                onChange = { viewModel.onEmailChanged(it) },
            )

            Spacer(Modifier.height(QryptDimens.SpaceLG))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ContactFoundBanner
// ─────────────────────────────────────────────────────────────
@Composable
private fun ContactFoundBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier              = Modifier.padding(QryptDimens.SpaceMD),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column {
                Text(
                    "Contact found in QryptIN",
                    style      = TypoBodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color      = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    "We found a registered name for this number.",
                    style = TypoBodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ContactNameCard
// ─────────────────────────────────────────────────────────────
@Composable
private fun ContactNameCard(
    serverName           : String,
    displayName          : String,
    isEditing            : Boolean,
    isNicknameMode       : Boolean,
    nameFocus            : FocusRequester,
    nicknameFocus        : FocusRequester,
    onEditToggle         : () -> Unit,
    onNicknameToggle     : () -> Unit,
    onDisplayNameChanged : (String) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(QryptDimens.SpaceMD)) {

            // Section label
            Text(
                "Contact Name",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(QryptDimens.SpaceXS))

            // Name row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value         = displayName,
                        onValueChange = onDisplayNameChanged,
                        modifier      = Modifier
                            .weight(1f)
                            .focusRequester(nameFocus),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction      = ImeAction.Done,
                        ),
                        shape = RoundedCornerShape(QryptDimens.RadiusSmall),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                        textStyle = TypoTitleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    )
                } else {
                    Text(
                        text     = displayName.ifBlank { serverName },
                        style    = TypoTitleMedium,
                        color    = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }

                TextButton(onClick = onEditToggle) {
                    Text(
                        if (isEditing) "Done" else "Edit",
                        style = TypoBodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.height(QryptDimens.SpaceXS))

            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp,
            )

            Spacer(Modifier.height(QryptDimens.SpaceSM))

            // Lock icon row
            Row(
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
                )
                Column {
                    Text(
                        "This is the name registered with QryptIN.\nYou can give a nickname for your reference.",
                        style = TypoBodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick      = onNicknameToggle,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            "Give a nickname",
                            style = TypoBodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // Nickname input (expanded when toggled)
            AnimatedVisibility(
                visible = isNicknameMode,
                enter   = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit    = fadeOut(tween(150)) + shrinkVertically(tween(150)),
            ) {
                Column {
                    Spacer(Modifier.height(QryptDimens.SpaceSM))
                    OutlinedTextField(
                        value         = displayName,
                        onValueChange = onDisplayNameChanged,
                        label         = { Text("Nickname", style = TypoBodySmall) },
                        placeholder   = { Text("e.g. Maa, Office, Sister", style = TypoBodySmall) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .focusRequester(nicknameFocus),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction      = ImeAction.Done,
                        ),
                        shape  = RoundedCornerShape(QryptDimens.RadiusSmall),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This nickname is only visible to you.",
                        style = TypoBodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  EmailSection
// ─────────────────────────────────────────────────────────────
@Composable
private fun EmailSection(
    email   : String,
    onChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Email Address (Optional)",
            style = TypoLabelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value         = email,
            onValueChange = onChange,
            placeholder   = { Text("name@example.com", style = TypoBodyMedium) },
            leadingIcon   = {
                Icon(
                    Icons.Rounded.Email,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
            colors   = OutlinedTextFieldDefaults.colors(
                focusedBorderColor  = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
        Text(
            "Add an email address (optional)\nWe'll save this email with the contact in QryptIN.",
            style = TypoBodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
