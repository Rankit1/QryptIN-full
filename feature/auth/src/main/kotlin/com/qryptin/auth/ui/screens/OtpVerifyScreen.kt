package com.qryptin.auth.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.qryptin.auth.model.AuthUiState
import com.qryptin.auth.ui.components.*
import com.qryptin.auth.viewmodel.AuthViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  OtpVerifyScreen
//  Verifies the entered code, THEN checks the local database
//  (corrected order) to decide whether to route to the existing
//  user's welcome-back screen or to Registration.
// ─────────────────────────────────────────────────────────────
@Composable
fun OtpVerifyScreen(
    viewModel      : AuthViewModel,
    onExistingUser : () -> Unit,
    onNewUser      : () -> Unit,
    onBack         : () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    AuthScaffold(
        topBar = {
            Spacer(Modifier.height(12.dp))
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint               = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(20.dp))

            // Header
            AuthHeader(
                title    = "Verify your number",
                subtitle = buildAnnotatedString {
                    append("We have sent a 6-digit OTP to\n")
                    withStyle(SpanStyle(
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )) {
                        append(viewModel.fullPhone)
                    }
                }.toString(),
            )

            Spacer(Modifier.height(40.dp))

            // OTP row
            OtpInputRow(
                digits        = viewModel.otpDigits,
                onDigitChange = viewModel::onOtpDigitChanged,
                onPaste       = viewModel::onOtpPaste,
                isError       = viewModel.otpError != null,
                modifier      = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(16.dp))

            // Error
            AnimatedVisibility(
                visible = viewModel.otpError != null,
                enter   = fadeIn() + slideInVertically { -10 },
                exit    = fadeOut(),
            ) {
                Text(
                    text      = viewModel.otpError ?: "",
                    style     = TypoBodySmall,
                    color     = MaterialTheme.colorScheme.error,
                    modifier  = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Resend row
            ResendOtpRow(
                countdown = viewModel.resendCountdown,
                canResend = viewModel.canResend,
                onResend  = viewModel::resendOtp,
            )

            Spacer(Modifier.height(40.dp))

            // CTA
            QryptPrimaryButton(
                text      = "Verify OTP",
                onClick   = {
                    viewModel.submitOtp(
                        onExistingUser = { viewModel.resetState(); onExistingUser() },
                        onNewUser      = { viewModel.resetState(); onNewUser() },
                    )
                },
                enabled   = viewModel.isOtpComplete,
                isLoading = uiState is AuthUiState.Loading,
            )

            Spacer(Modifier.height(20.dp))
            SecurityBadge(text = "OTP is used only for verification")
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Resend OTP row
// ─────────────────────────────────────────────────────────────
@Composable
private fun ResendOtpRow(
    countdown: Int,
    canResend: Boolean,
    onResend : () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        if (!canResend) {
            Text(
                text  = "Resend OTP in ",
                style = TypoBodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Animated countdown
            AnimatedContent(
                targetState   = countdown,
                transitionSpec = {
                    slideInVertically { it } togetherWith slideOutVertically { -it }
                },
                label = "countdown",
            ) { count ->
                Text(
                    text  = "00:${count.toString().padStart(2, '0')}",
                    style = TypoBodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            Text(
                text  = "Didn't receive the OTP? ",
                style = TypoBodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick      = onResend,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text  = "Resend",
                    style = TypoBodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}