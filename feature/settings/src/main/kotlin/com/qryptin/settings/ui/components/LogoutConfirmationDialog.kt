package com.qryptin.settings.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────
//  LogoutConfirmationDialog
//
//  Issue 1 fix: logging out previously fired immediately with no
//  confirmation and no visible explanation of what gets cleared.
//  This is a premium Material 3 warning dialog — rounded corners,
//  dimmed/scrimmed background, a scale+fade entrance animation,
//  a warning icon, and a clear breakdown of exactly what happens
//  to the local session before the destructive action is taken.
// ─────────────────────────────────────────────────────────────
@Composable
fun LogoutConfirmationDialog(
    onDismiss : () -> Unit,
    onConfirm : () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress    = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        // Scale + fade entrance — starts slightly shrunk and pops
        // to full size, which reads as "premium" rather than a
        // flat instant appearance.
        var animatedIn by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { animatedIn = true }
        val scale by animateFloatAsState(
            targetValue   = if (animatedIn) 1f else 0.85f,
            animationSpec = tween(durationMillis = 260, easing = EaseOutBack),
            label         = "logout_dialog_scale",
        )

        Surface(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .scale(scale),
            shape    = RoundedCornerShape(28.dp),
            color    = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Warning icon in a soft error-tinted circle.
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.error,
                        modifier           = Modifier.size(32.dp),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text       = "Logout from QryptIN?",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text      = "You will need to verify your phone number and OTP again to access your account securely.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(20.dp))

                // Breakdown of exactly what a logout clears locally.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LogoutInfoRow(Icons.Rounded.Lock, "Local encrypted session will be cleared")
                    LogoutInfoRow(Icons.Rounded.Key, "Active authentication token removed")
                    LogoutInfoRow(Icons.Rounded.Fingerprint, "App lock session cleared")
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(14.dp),
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick  = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor   = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("Logout", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp),
        )
        Text(
            text  = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
