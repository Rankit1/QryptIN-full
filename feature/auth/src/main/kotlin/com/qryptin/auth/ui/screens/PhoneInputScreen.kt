package com.qryptin.auth.ui.screens

import android.app.Activity
import com.qryptin.auth.AuthModule
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qryptin.auth.model.AuthUiState
import com.qryptin.auth.ui.components.*
import com.qryptin.auth.viewmodel.AuthViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  PhoneInputScreen
//  Collects the phone number and sends the OTP. The local user
//  DB lookup happens later, after OTP verification, per the
//  corrected QryptIN auth flow (phone -> OTP -> DB check). A
//  brief "Sending OTP..." overlay covers the request.
// ─────────────────────────────────────────────────────────────
@Composable
fun PhoneInputScreen(
    viewModel : AuthViewModel,
    onOtpSent : () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val isSending = uiState is AuthUiState.Loading

    fun submit() {
        keyboard?.hide()
        viewModel.submitPhone(onSent = { viewModel.resetState(); onOtpSent() })
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
        AuthModule.setActivityForAuth(context as Activity, context)
    }

    AuthScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Main form (stays laid out under the "sending OTP" overlay so
            //    the transition feels smooth rather than a hard screen swap) ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(56.dp))

                AuthHeader(
                    title    = "Welcome to QryptIN",
                    subtitle = "Enter your phone number to continue",
                )

                Spacer(Modifier.height(40.dp))

                PhoneField(
                    viewModel      = viewModel,
                    focusRequester = focusRequester,
                    onDone         = { submit() },
                )

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = viewModel.phoneError != null,
                    enter   = fadeIn() + slideInVertically { -10 },
                    exit    = fadeOut(),
                ) {
                    viewModel.phoneError?.let { err ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.padding(start = 4.dp),
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline, null,
                                tint     = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(err, style = TypoBodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                QryptPrimaryButton(
                    text      = "Continue",
                    onClick   = { submit() },
                    enabled   = viewModel.isPhoneValid,
                    isLoading = isSending,
                )

                Spacer(Modifier.height(20.dp))
                SecurityBadge()
                Spacer(Modifier.height(24.dp))
                TermsText()
                Spacer(Modifier.height(40.dp))
            }

            // ── "Sending OTP..." overlay ───────────────────────────────
            AnimatedVisibility(
                visible  = isSending,
                enter    = fadeIn(tween(200)),
                exit     = fadeOut(tween(150)),
                modifier = Modifier.fillMaxSize(),
            ) {
                SendingOtpOverlay()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Sending OTP overlay — shown while the OTP is requested
// ─────────────────────────────────────────────────────────────
@Composable
private fun SendingOtpOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.PhonelinkLock,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            CircularProgressIndicator(
                modifier    = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color       = MaterialTheme.colorScheme.primary,
            )
            Text("Sending OTP...", style = TypoTitleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "Please wait while we send a verification code",
                style = TypoBodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Auth Header — reused across screens
// ─────────────────────────────────────────────────────────────
@Composable
fun AuthHeader(
    title   : String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = title,
            style = TypoDisplaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text  = subtitle,
            style = TypoBodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Phone Field (country picker + number input)
// ─────────────────────────────────────────────────────────────
@Composable
fun PhoneField(
    viewModel     : AuthViewModel,
    focusRequester: FocusRequester,
    onDone        : () -> Unit,
    modifier      : Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Phone Number",
            style = TypoLabelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Country code
            CountryCodeSelector(
                selected   = viewModel.selectedCountry,
                onSelected = viewModel::onCountrySelected,
                modifier   = Modifier.height(IntrinsicSize.Min),
            )

            // Number text field
            OutlinedTextField(
                value          = viewModel.phoneNumber,
                onValueChange  = viewModel::onPhoneChanged,
                modifier       = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder    = { Text("Enter phone number", style = TypoBodyMedium) },
                singleLine     = true,
                isError        = viewModel.phoneError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction    = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { onDone() }),
                shape          = RoundedCornerShape(QryptDimens.RadiusSmall),
                colors         = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor     = MaterialTheme.colorScheme.error,
                ),
                textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Terms & Conditions text
// ─────────────────────────────────────────────────────────────
@Composable
fun TermsText(modifier: Modifier = Modifier) {
    val linkColor = MaterialTheme.colorScheme.primary
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text  = "By continuing, you agree to our ",
            style = TypoBodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = "Terms of Service",
            style = TypoBodySmall,
            color = linkColor,
        )
        Text(
            text  = " & ",
            style = TypoBodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = "Privacy Policy",
            style = TypoBodySmall,
            color = linkColor,
        )
    }
}
