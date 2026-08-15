package com.qryptin.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.model.CallConnectionState
import com.qryptin.chat.model.CallType
import com.qryptin.chat.ui.components.CallActionButton
import com.qryptin.chat.ui.components.SecureConnectionPulse
import com.qryptin.chat.viewmodel.CallViewModel
import com.qryptin.core.designsystem.ui.theme.QryptAvatar
import com.qryptin.core.designsystem.ui.theme.SecureStatusBadge
import com.qryptin.core.designsystem.ui.theme.TypoBodyLarge
import com.qryptin.core.designsystem.ui.theme.TypoDisplaySmall

// ─────────────────────────────────────────────────────────────
//  CallScreen — voice call.
//  Dark, full-bleed surface (calling UIs read better on a near-
//  black background regardless of the app's light theme) with
//  the QryptPrimary blue used for the secure-connection pulse
//  and any "active" action button state.
// ─────────────────────────────────────────────────────────────
@Composable
fun CallScreen(
    peerName      : String,
    peerAvatarUrl : String?,
    onEndCall     : () -> Unit,
    viewModel     : CallViewModel = viewModel(),
) {
    LaunchedEffect(peerName) { viewModel.start(peerName, peerAvatarUrl, CallType.VOICE) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.connectionState) {
        if (uiState.connectionState == CallConnectionState.ENDED) onEndCall()
    }

    Scaffold(containerColor = Color(0xFF0B0F19)) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.3f))

            Box(contentAlignment = Alignment.Center) {
                if (uiState.connectionState != CallConnectionState.CONNECTED) {
                    SecureConnectionPulse()
                }
                QryptAvatar(name = peerName, avatarUrl = peerAvatarUrl, size = 120.dp)
            }

            Spacer(Modifier.height(24.dp))
            Text(peerName, style = TypoDisplaySmall, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(statusLabel(uiState), style = TypoBodyLarge, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            SecureStatusBadge(label = "End-to-end ready")

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                CallActionButton(if (uiState.isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic, "Mute", viewModel::toggleMute, isActive = uiState.isMuted)
                CallActionButton(Icons.Rounded.Dialpad, "Keypad", viewModel::toggleKeypad, isActive = uiState.isKeypadOpen)
                CallActionButton(Icons.Rounded.VolumeUp, "Speaker", viewModel::toggleSpeaker, isActive = uiState.isSpeakerOn)
            }

            Spacer(Modifier.height(28.dp))

            CallActionButton(Icons.Rounded.CallEnd, "End", viewModel::endCall, isDestructive = true)

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun statusLabel(uiState: com.qryptin.chat.model.CallUiState): String = when (uiState.connectionState) {
    CallConnectionState.DIALING    -> "Dialing…"
    CallConnectionState.RINGING    -> "Ringing…"
    CallConnectionState.CONNECTING -> "Establishing secure connection…"
    CallConnectionState.CONNECTED  -> uiState.formattedDuration
    CallConnectionState.ENDED      -> "Call ended"
}
