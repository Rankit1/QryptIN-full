package com.qryptin.contacts.ui.screens.addcontact

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qryptin.auth.model.CommonCountryCodes
import com.qryptin.auth.model.CountryCode
import com.qryptin.contacts.viewmodel.AddContactViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  AddContactPhoneScreen — Screen 1
//  Routes to the existing-user or non-QryptIN flow based on
//  lookup result.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactPhoneScreen(
    viewModel : AddContactViewModel,
    onBack    : () -> Unit,
    onCancel  : () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCountryPicker by remember { mutableStateOf(false) }
    val phoneFocusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // Mobile keyboard fix: autofocus the phone field and explicitly show
    // the system numeric/phone keyboard as soon as this screen appears.
    LaunchedEffect(Unit) {
        phoneFocusRequester.requestFocus()
        keyboard?.show()
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
                        "Enter Phone Number",
                        style = TypoTitleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    TextButton(onClick = onCancel) {
                        Text(
                            "Cancel",
                            style = TypoBodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = QryptDimens.PaddingScreenH),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(QryptDimens.SpaceXL))

            // ── Subtitle ──────────────────────────────────────
            Text(
                text      = "Enter the phone number\nof the contact",
                style     = TypoBodyLarge,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(QryptDimens.SpaceXL))

            // ── Phone input field ─────────────────────────────
            PhoneInputRow(
                countryCode     = uiState.selectedCountry,
                phoneNumber     = uiState.phoneNumber,
                onPhoneChanged  = { viewModel.onPhoneNumberChanged(it) },
                onCountryClick  = { showCountryPicker = true },
                focusRequester  = phoneFocusRequester,
                onNext          = {
                    keyboard?.hide()
                    if (uiState.isNonQryptINUser) {
                        viewModel.onProceedToNonQryptINContact()
                    } else {
                        viewModel.onPhoneLookup()
                    }
                },
            )

            // ── Non-QryptIN informational alert ───────────────
            AnimatedVisibility(
                visible = uiState.isNonQryptINUser,
                enter   = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit    = fadeOut(tween(200)) + shrinkVertically(tween(200)),
            ) {
                Spacer(Modifier.height(QryptDimens.SpaceMD))
                NonQryptINAlertCard()
            }

            Spacer(Modifier.height(QryptDimens.SpaceLG))

            // ── Next button ───────────────────────────────────
            // When non-QryptIN user is detected, "Next" navigates to the
            // non-QryptIN details screen instead of triggering another lookup.
            QryptPrimaryButton(
                text      = "Next",
                onClick   = {
                    if (uiState.isNonQryptINUser) {
                        viewModel.onProceedToNonQryptINContact()
                    } else {
                        viewModel.onPhoneLookup()
                    }
                },
                enabled   = viewModel.isPhoneValid && !uiState.isLookingUp,
                isLoading = uiState.isLookingUp,
                modifier  = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(QryptDimens.SpaceLG))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(Modifier.height(QryptDimens.SpaceMD))

            // ── Numeric keypad ────────────────────────────────
            NumericKeypad(
                onDigit     = { viewModel.onKeypadDigit(it) },
                onBackspace = { viewModel.onKeypadBackspace() },
            )
        }
    }

    // ── Country picker dialog ─────────────────────────────────
    if (showCountryPicker) {
        CountryPickerDialog(
            selected  = uiState.selectedCountry,
            onSelect  = { country ->
                viewModel.onCountrySelected(country)
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  NonQryptINAlertCard
//  Informational-only. No action buttons inside.
// ─────────────────────────────────────────────────────────────
@Composable
private fun NonQryptINAlertCard() {
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
//  PhoneInputRow
// ─────────────────────────────────────────────────────────────
@Composable
private fun PhoneInputRow(
    countryCode    : CountryCode,
    phoneNumber    : String,
    onPhoneChanged : (String) -> Unit,
    onCountryClick : () -> Unit,
    focusRequester : FocusRequester,
    onNext         : () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
        colors   = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(modifier = Modifier.padding(QryptDimens.SpaceMD)) {
            Text(
                "Phone Number",
                style = TypoLabelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(QryptDimens.SpaceXS))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceSM),
            ) {
                // Country chip
                Surface(
                    onClick = onCountryClick,
                    shape   = RoundedCornerShape(QryptDimens.RadiusSmall),
                    color   = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(countryCode.flag, fontSize = 18.sp)
                        Text(
                            countryCode.dial,
                            style = TypoBodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Select country",
                            modifier = Modifier.size(16.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Real, focusable phone number input — opens the system
                // numeric/phone keyboard automatically (mobile keyboard fix).
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value           = phoneNumber,
                        onValueChange   = { onPhoneChanged(it.filter { c -> c.isDigit() }) },
                        modifier        = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine      = true,
                        textStyle       = TypoBodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush     = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.primary,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction    = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(onNext = { onNext() }),
                        decorationBox   = { innerTextField ->
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text  = "Enter phone number",
                                    style = TypoBodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                )
                            }
                            innerTextField()
                        },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  NumericKeypad
// ─────────────────────────────────────────────────────────────
@Composable
private fun NumericKeypad(
    onDigit    : (String) -> Unit,
    onBackspace: () -> Unit,
) {
    val rows = listOf(
        listOf("1\n", "2\nABC", "3\nDEF"),
        listOf("4\nGHI", "5\nJKL", "6\nMNO"),
        listOf("7\nPQRS", "8\nTUV", "9\nWXYZ"),
        listOf("* #", "0\n+", "⌫"),
    )

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { key ->
                    val isBackspace = key == "⌫"
                    val digit = key.lines().first().trim()
                    val sub   = key.lines().getOrNull(1)?.trim().orEmpty()

                    Surface(
                        onClick  = {
                            if (isBackspace) onBackspace()
                            else onDigit(digit)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape    = RoundedCornerShape(QryptDimens.RadiusSmall),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (isBackspace) {
                                Icon(
                                    Icons.Rounded.Backspace,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp),
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text  = digit,
                                        style = TypoTitleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (sub.isNotEmpty()) {
                                        Text(
                                            text  = sub,
                                            style = TypoLabelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  CountryPickerDialog
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryPickerDialog(
    selected  : CountryCode,
    onSelect  : (CountryCode) -> Unit,
    onDismiss : () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        CommonCountryCodes.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.dial.contains(query)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f),
            shape  = RoundedCornerShape(QryptDimens.RadiusCard),
            color  = MaterialTheme.colorScheme.surface,
        ) {
            Column {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(QryptDimens.SpaceMD),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Select Country", style = TypoTitleMedium, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    placeholder   = { Text("Search country…", style = TypoBodyMedium) },
                    leadingIcon   = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine    = true,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = QryptDimens.SpaceMD),
                    shape = RoundedCornerShape(QryptDimens.RadiusSmall),
                )

                Spacer(Modifier.height(QryptDimens.SpaceSM))

                LazyColumn {
                    items(filtered, key = { it.iso }) { country ->
                        val isSelected = country.iso == selected.iso
                        ListItem(
                            headlineContent = {
                                Text(
                                    "${country.flag}  ${country.name}",
                                    style = TypoBodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingContent = {
                                Text(
                                    country.dial,
                                    style = TypoBodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            modifier = Modifier.clickable { onSelect(country) },
                            colors   = ListItemDefaults.colors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent,
                            ),
                        )
                        HorizontalDivider(
                            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}
