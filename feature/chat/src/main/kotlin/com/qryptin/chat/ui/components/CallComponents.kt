package com.qryptin.chat.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.qryptin.core.designsystem.ui.theme.TypoLabelSmall

// ─────────────────────────────────────────────────────────────
//  CallActionButton
//  Round icon button used on CallScreen / VideoCallScreen for
//  mute, speaker, video toggle, switch camera, keypad, end call.
//  "active" state always tints with QryptPrimary blue — there is
//  no separate green "connected" colour anywhere in the call UI.
// ─────────────────────────────────────────────────────────────
@Composable
fun CallActionButton(
    icon       : ImageVector,
    label      : String,
    onClick    : () -> Unit,
    isActive   : Boolean = false,
    isDestructive: Boolean = false,
    modifier   : Modifier = Modifier,
) {
    val containerColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        isActive      -> MaterialTheme.colorScheme.primary
        else          -> Color.White.copy(alpha = 0.16f)
    }
    val contentColor = Color.White

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier         = Modifier.size(56.dp).background(containerColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onClick) {
                Icon(icon, contentDescription = label, tint = contentColor)
            }
        }
        Text(label, style = TypoLabelSmall, color = Color.White.copy(alpha = 0.85f))
    }
}

// ─────────────────────────────────────────────────────────────
//  SecureConnectionPulse
//  Soft expanding ring behind the peer avatar while a call is
//  connecting — communicates "establishing a secure channel"
//  without any text. Always QryptPrimary blue.
// ─────────────────────────────────────────────────────────────
@Composable
fun SecureConnectionPulse(
    size     : androidx.compose.ui.unit.Dp = 160.dp,
    modifier : Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "secure_pulse")
    val scale by transition.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1.15f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label         = "pulse_scale",
    )
    val alpha by transition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.05f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label         = "pulse_alpha",
    )

    Box(
        modifier = modifier
            .size(size * scale)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), CircleShape),
    )
}
