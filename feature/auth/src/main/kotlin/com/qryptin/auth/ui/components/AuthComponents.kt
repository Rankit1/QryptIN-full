package com.qryptin.auth.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qryptin.auth.model.CommonCountryCodes
import com.qryptin.auth.model.CountryCode
import com.qryptin.core.designsystem.ui.theme.TypoBodySmall
import com.qryptin.core.designsystem.ui.theme.TypoBodyMedium
import com.qryptin.core.designsystem.ui.theme.TypoBodyLarge
import com.qryptin.core.designsystem.ui.theme.TypoTitleMedium
import com.qryptin.core.designsystem.ui.theme.TypoDisplaySmall
import com.qryptin.core.designsystem.ui.theme.QryptINSafeScaffold
import com.qryptin.core.designsystem.ui.theme.QryptDimens

// ─────────────────────────────────────────────────────────────
//  OTP Input Row — 6 individual digit boxes
// ─────────────────────────────────────────────────────────────
@Composable
fun OtpInputRow(
    digits       : List<String>,
    onDigitChange: (index: Int, value: String) -> Unit,
    onPaste      : (String) -> Unit,
    isError      : Boolean,
    modifier     : Modifier = Modifier,
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val keyboard        = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    // Responsive box sizing: compute box width from available space so
    // all 6 boxes + gaps always fit inside the row (fixes box #6 clipping
    // on narrower screens).
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val boxCount = digits.size
        val minGap = 6.dp
        val maxGap = 10.dp
        val maxBoxWidth = 48.dp
        val minBoxWidth = 32.dp

        val idealBoxWidth = (maxWidth - maxGap * (boxCount - 1)) / boxCount
        val boxWidth = idealBoxWidth.coerceIn(minBoxWidth, maxBoxWidth)
        val actualGap = if (boxWidth >= maxBoxWidth) {
            ((maxWidth - boxWidth * boxCount) / (boxCount - 1)).coerceIn(minGap, maxGap)
        } else {
            maxGap
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(actualGap, Alignment.CenterHorizontally),
        ) {
            digits.forEachIndexed { index, digit ->
                OtpDigitBox(
                    value          = digit,
                    isFocused      = false,
                    isError        = isError,
                    boxWidth       = boxWidth,
                    focusRequester = focusRequesters[index],
                    onValueChange  = { newVal ->
                        when {
                            // Paste detection — if string > 1 char
                            newVal.length > 1 -> {
                                onPaste(newVal)
                                focusRequesters.lastOrNull()?.requestFocus()
                            }
                            newVal.isNotEmpty() -> {
                                onDigitChange(index, newVal)
                                if (index < boxCount - 1) focusRequesters[index + 1].requestFocus()
                                else keyboard?.hide()
                            }
                            else -> {
                                // Backspace
                                onDigitChange(index, "")
                                if (index > 0) focusRequesters[index - 1].requestFocus()
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun OtpDigitBox(
    value         : String,
    isFocused     : Boolean,
    isError       : Boolean,
    boxWidth      : Dp,
    focusRequester: FocusRequester,
    onValueChange : (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var localFocus by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            isError    -> MaterialTheme.colorScheme.error
            localFocus -> MaterialTheme.colorScheme.primary
            value.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else       -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(150),
        label = "otp_border",
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isError    -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            localFocus -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            value.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else       -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(150),
        label = "otp_bg",
    )

    BasicTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier
            .size(width = boxWidth, height = 54.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { localFocus = it.isFocused }
            .clip(RoundedCornerShape(QryptDimens.RadiusMedium))
            .background(bgColor)
            .border(
                width = if (localFocus) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(QryptDimens.RadiusMedium),
            ),
        singleLine          = true,
        textStyle           = TypoDisplaySmall.copy(
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurface,
        ),
        keyboardOptions     = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction    = ImeAction.Next,
        ),
        cursorBrush         = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource   = interactionSource,
        decorationBox       = { inner ->
            Box(contentAlignment = Alignment.Center) { inner() }
        },
    )
}

// ─────────────────────────────────────────────────────────────
//  Country Code Selector
// ─────────────────────────────────────────────────────────────
@Composable
fun CountryCodeSelector(
    selected   : CountryCode,
    onSelected : (CountryCode) -> Unit,
    modifier   : Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    // Trigger row
    Surface(
        onClick      = { showDialog = true },
        modifier     = modifier,
        shape        = RoundedCornerShape(QryptDimens.RadiusSmall),
        color        = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border       = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier             = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(selected.flag, style = TypoBodyLarge)
            Text(
                text  = selected.dial,
                style = TypoBodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector        = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(16.dp),
            )
        }
    }

    if (showDialog) {
        CountryPickerDialog(
            current    = selected,
            onSelected = {
                onSelected(it)
                showDialog = false
            },
            onDismiss  = { showDialog = false },
        )
    }
}

@Composable
private fun CountryPickerDialog(
    current   : CountryCode,
    onSelected: (CountryCode) -> Unit,
    onDismiss : () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) CommonCountryCodes
        else CommonCountryCodes.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.dial.contains(query)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier  = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f),
            shape     = RoundedCornerShape(QryptDimens.RadiusCard),
            color     = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(QryptDimens.SpaceXXL)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text("Select Country", style = TypoTitleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(QryptDimens.SpaceLG))

                // Search
                OutlinedTextField(
                    value          = query,
                    onValueChange  = { query = it },
                    modifier       = Modifier.fillMaxWidth(),
                    placeholder    = { Text("Search country or code", style = TypoBodyMedium) },
                    leadingIcon    = {
                        Icon(Icons.Rounded.Search, contentDescription = null,
                            modifier = Modifier.size(20.dp))
                    },
                    trailingIcon   = if (query.isNotEmpty()) {{
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = null,
                                modifier = Modifier.size(18.dp))
                        }
                    }} else null,
                    shape          = RoundedCornerShape(QryptDimens.RadiusSmall),
                    singleLine     = true,
                    colors         = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )

                Spacer(Modifier.height(QryptDimens.SpaceMD))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(filtered) { country ->
                        val isSelected = country.iso == current.iso
                        Surface(
                            onClick = { onSelected(country) },
                            shape   = RoundedCornerShape(QryptDimens.RadiusSmall),
                            color   = if (isSelected)
                                          MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                      else Color.Transparent,
                        ) {
                            Row(
                                modifier             = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = QryptDimens.SpaceLG, vertical = 12.dp),
                                verticalAlignment    = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
                            ) {
                                Text(country.flag, style = TypoBodyLarge)
                                Text(
                                    text     = country.name,
                                    style    = TypoBodyMedium,
                                    modifier = Modifier.weight(1f),
                                    color    = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text  = country.dial,
                                    style = TypoBodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint        = MaterialTheme.colorScheme.primary,
                                        modifier    = Modifier.size(18.dp),
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

// ─────────────────────────────────────────────────────────────
//  Security Badge — "All calls / messages are E2E encrypted"
// ─────────────────────────────────────────────────────────────
@Composable
fun SecurityBadge(
    text    : String = "All messages are end-to-end encrypted",
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(QryptDimens.RadiusSmall),
        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
    ) {
        Row(
            modifier             = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector        = Icons.Rounded.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp),
            )
            Text(
                text  = text,
                style = TypoBodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Auth Screen Scaffold — gradient bg + keyboard handling
// ─────────────────────────────────────────────────────────────
@Composable
fun AuthScaffold(
    modifier : Modifier = Modifier,
    topBar   : @Composable () -> Unit = {},
    content  : @Composable () -> Unit,
) {
    // Gradient from existing GradientBackground component is used at app level;
    // here we just provide a clean Material surface container for the auth flow.
    // Issue 2 fix: QryptINSafeScaffold guarantees status bar / navigation bar /
    // display cutout insets are respected instead of relying on Scaffold defaults.
    QryptINSafeScaffold(
        modifier          = modifier,
        containerColor    = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = QryptDimens.PaddingScreenH),
        ) {
            topBar()
            content()
        }
    }
}
