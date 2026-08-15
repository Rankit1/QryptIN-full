package com.qryptin.calls.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qryptin.calls.model.CallState
import com.qryptin.calls.model.CallType
import com.qryptin.calls.ui.components.CallAvatar
import com.qryptin.calls.ui.components.PulseRing
import com.qryptin.calls.viewmodel.ActiveCallViewModel

// ─────────────────────────────────────────────────────────────
//  VoiceCallScreen
// ─────────────────────────────────────────────────────────────
@Composable
fun VoiceCallScreen(
    contactId     : String,
    contactName   : String,
    contactAvatar : String?,
    viewModel     : ActiveCallViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.callState.collectAsStateWithLifecycle()

    // Trigger call on first composition
    LaunchedEffect(contactId) {
        viewModel.startCall(contactId, contactName, contactAvatar, CallType.VOICE)
    }

    // Navigate away when call ends
    LaunchedEffect(state.state) {
        if (state.state == CallState.IDLE) onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF1565C0)))
            ),
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(60.dp))

            // Lock/encryption badge
            AnimatedEncryptionBadge()

            Spacer(Modifier.height(32.dp))

            // Avatar with pulse rings
            Box(contentAlignment = Alignment.Center) {
                if (state.state != CallState.CONNECTED) {
                    PulseRing(modifier = Modifier.size(130.dp))
                    PulseRing(modifier = Modifier.size(105.dp))
                }
                CallAvatar(name = contactName, size = 88.dp)
            }

            Spacer(Modifier.height(24.dp))

            // Contact name
            Text(
                text       = contactName,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )

            Spacer(Modifier.height(8.dp))

            // Status label
            AnimatedContent(
                targetState = callStatusLabel(state.state, state.durationSecs),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "status",
            ) { label ->
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }

            Spacer(Modifier.weight(1f))

            // Call controls
            VoiceCallControls(
                isMuted     = state.isMuted,
                isSpeaker   = state.isSpeakerOn,
                onMute      = viewModel::toggleMute,
                onSpeaker   = viewModel::toggleSpeaker,
                onEnd       = { viewModel.endCall(); onNavigateBack() },
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun AnimatedEncryptionBadge() {
    val inf = rememberInfiniteTransition(label = "enc")
    val alpha by inf.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label         = "enc_alpha",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Icon(Icons.Rounded.Lock, null, tint = Color(0xFF80DEEA).copy(alpha = alpha), modifier = Modifier.size(14.dp))
        Text("End-to-end encrypted", color = Color.White.copy(alpha = alpha), fontSize = 12.sp)
    }
}

@Composable
private fun VoiceCallControls(
    isMuted   : Boolean,
    isSpeaker : Boolean,
    onMute    : () -> Unit,
    onSpeaker : () -> Unit,
    onEnd     : () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Top row: mute + speaker
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CallControlButton(
                icon    = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                label   = if (isMuted) "Unmute" else "Mute",
                active  = isMuted,
                onClick = onMute,
            )
            CallControlButton(
                icon    = if (isSpeaker) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeDown,
                label   = "Speaker",
                active  = isSpeaker,
                onClick = onSpeaker,
            )
            CallControlButton(
                icon    = Icons.Rounded.Dialpad,
                label   = "Keypad",
                onClick = {},
            )
        }

        Spacer(Modifier.height(32.dp))

        // End call
        IconButton(
            onClick  = onEnd,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFF44336)),
        ) {
            Icon(Icons.Rounded.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun CallControlButton(
    icon    : ImageVector,
    label   : String,
    active  : Boolean  = false,
    onClick : () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        IconButton(
            onClick  = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else Color.White.copy(alpha = 0.15f)),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = if (active) Color(0xFF1A237E) else Color.White,
                modifier           = Modifier.size(24.dp),
            )
        }
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

private fun callStatusLabel(state: CallState, durationSecs: Int): String = when (state) {
    CallState.CALLING    -> "Calling…"
    CallState.RINGING    -> "Ringing…"
    CallState.CONNECTING -> "Connecting…"
    CallState.CONNECTED  -> {
        val m = durationSecs / 60; val s = durationSecs % 60
        "%02d:%02d".format(m, s)
    }
    CallState.ENDED      -> "Call ended"
    CallState.FAILED     -> "Call failed"
    CallState.IDLE       -> ""
}
