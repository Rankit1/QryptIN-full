package com.qryptin.auth.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qryptin.auth.ui.components.AuthScaffold
import com.qryptin.auth.ui.components.SecurityBadge
import com.qryptin.auth.viewmodel.AuthViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  UserFoundScreen
//  "Welcome back" — shown after OTP verification finds this
//  phone number already registered. The session is already
//  active by the time this screen appears; tapping Continue just
//  hands off to Home/Chat.
// ─────────────────────────────────────────────────────────────
@Composable
fun UserFoundScreen(
    viewModel: AuthViewModel,
    onNext   : () -> Unit,
    onBack   : () -> Unit,
) {
    val user = viewModel.foundUser

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
            modifier             = Modifier.fillMaxSize(),
            horizontalAlignment  = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            ) {
                Text(
                    "User Found",
                    style    = TypoBodyMedium,
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Welcome back! \uD83D\uDC4B",
                style               = TypoDisplaySmall,
                color               = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "We found your account",
                style = TypoBodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))

            QryptAvatar(
                name      = user?.fullName ?: "?",
                avatarUrl = user?.profilePhotoUri,
                size      = 96.dp,
            )

            Spacer(Modifier.height(20.dp))

            Text(
                user?.fullName ?: "",
                style = TypoTitleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                viewModel.fullPhone,
                style = TypoBodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!user?.qryptinId.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "@${user?.qryptinId}",
                    style = TypoBodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.weight(1f))

            QryptPrimaryButton(
                text    = "Continue",
                onClick = onNext,
            )

            Spacer(Modifier.height(20.dp))
            SecurityBadge()
            Spacer(Modifier.height(40.dp))
        }
    }
}
