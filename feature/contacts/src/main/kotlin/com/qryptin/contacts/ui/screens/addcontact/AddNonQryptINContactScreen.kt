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
//  AddNonQryptINContactScreen — Screen 2b
//  Shown when the entered phone number is NOT registered in QryptIN.
//  Name field is empty — user must fill it in manually.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNonQryptINContactScreen(
    viewModel : AddContactViewModel,
    onBack    : () -> Unit,
    onCancel  : () -> Unit,
) {
    val uiState   by viewModel.uiState.collectAsState()
    val nameFocus  = remember { FocusRequester() }

    LaunchedEffect(uiState.isEditingName) {
        if (uiState.isEditingName) nameFocus.requestFocus()
    }

    val canProceed = uiState.displayName.isNotBlank()

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
                color           = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                QryptPrimaryButton(
                    text     = "Next",
                    onClick  = { viewModel.onProceedToNonQryptINReview() },
                    enabled  = canProceed,
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

            // ── Top alert banner ──────────────────────────────
            NonQryptINDetailsBanner()

            // ── Name card ─────────────────────────────────────
            NonQryptINNameCard(
                displayName          = uiState.displayName,
                isEditing            = uiState.isEditingName,
                nameFocus            = nameFocus,
                onEditToggle         = { viewModel.onEditNameToggle() },
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
            NonQryptINEmailSection(
                email    = uiState.email,
                onChange = { viewModel.onEmailChanged(it) },
            )

            Spacer(Modifier.height(QryptDimens.SpaceLG))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  NonQryptINDetailsBanner
// ─────────────────────────────────────────────────────────────
@Composable
private fun NonQryptINDetailsBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier              = Modifier.padding(QryptDimens.SpaceMD),
            verticalAlignment     = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceSM),
        ) {
            Icon(
                imageVector        = Icons.Rounded.Info,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier
                    .size(20.dp)
                    .padding(top = 1.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = "No QryptIN user found with this number.",
                    style = TypoBodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text  = "You can still save the contact and invite them to join QryptIN.",
                    style = TypoBodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  NonQryptINNameCard
//  Unlike the existing-user flow, the name is NOT auto-fetched.
//  The field starts empty and is required.
// ─────────────────────────────────────────────────────────────
@Composable
private fun NonQryptINNameCard(
    displayName          : String,
    isEditing            : Boolean,
    nameFocus            : FocusRequester,
    onEditToggle         : () -> Unit,
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
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Contact Name",
                    style = TypoLabelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Required chip
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                ) {
                    Text(
                        text     = "Required",
                        style    = TypoLabelSmall,
                        color    = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(QryptDimens.SpaceSM))

            // Name input (always in editable mode for non-QryptIN)
            OutlinedTextField(
                value         = displayName,
                onValueChange = onDisplayNameChanged,
                modifier      = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocus),
                placeholder   = {
                    Text(
                        "e.g. Maa, Papa, Office, Rahul",
                        style = TypoBodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                },
                leadingIcon   = {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction      = ImeAction.Done,
                ),
                shape  = RoundedCornerShape(QryptDimens.RadiusSmall),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            )

            Spacer(Modifier.height(QryptDimens.SpaceXS))

            Text(
                "Enter a name or nickname for this contact.\nThis is only visible to you.",
                style = TypoBodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  NonQryptINEmailSection
//  Optional — but if provided, enables email invite later.
// ─────────────────────────────────────────────────────────────
@Composable
private fun NonQryptINEmailSection(
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
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
        Text(
            "Adding an email enables sending the invite via Email.\nWithout an email, only SMS invite is available.",
            style = TypoBodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
