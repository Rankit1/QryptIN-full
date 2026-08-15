package com.qryptin.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.chat.model.CallConnectionState
import com.qryptin.chat.model.CallType
import com.qryptin.chat.ui.components.CallActionButton
import com.qryptin.chat.ui.components.SecureConnectionPulse
import com.qryptin.chat.viewmodel.CallViewModel
import com.qryptin.core.designsystem.ui.theme.QryptAvatar
import com.qryptin.core.designsystem.ui.theme.SecureStatusBadge
import com.qryptin.core.designsystem.ui.theme.TypoBodyMedium

// ─────────────────────────────────────────────────────────────
//  VideoCallScreen
//  Remote video area fills the screen (a mock placeholder until
//  real RTC lands); self-preview is a small draggable tile.
//  Same QryptPrimary-blue accent convention as CallScreen — no
//  separate "video call" colour.
// ─────────────────────────────────────────────────────────────
@Composable
fun VideoCallScreen(
    peerName      : String,
    peerAvatarUrl : String?,
    onEndCall     : () -> Unit,
    viewModel     : CallViewModel = viewModel(),
) {
    LaunchedEffect(peerName) { viewModel.start(peerName, peerAvatarUrl, CallType.VIDEO) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.connectionState) {
        if (uiState.connectionState == CallConnectionState.ENDED) onEndCall()
    }

    var selfPreviewOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    Scaffold(containerColor = Color(0xFF0B0F19)) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // ── Remote video area (mock placeholder) ───────────────
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uiState.connectionState != CallConnectionState.CONNECTED) {
                    SecureConnectionPulse()
                }
                QryptAvatar(name = peerName, avatarUrl = peerAvatarUrl, size = 120.dp)
            }

            // ── Top status row ──────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(peerName, style = TypoBodyMedium, color = Color.White)
                Spacer(Modifier.height(4.dp))
                SecureStatusBadge(label = statusLabel(uiState))
            }

            // ── Draggable self preview ──────────────────────────────
            Box(
                modifier = Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(selfPreviewOffset.x.toInt(), selfPreviewOffset.y.toInt()) }
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(width = 100.dp, height = 140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.DarkGray)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            selfPreviewOffset += dragAmount
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isVideoOn) {
                    Text("You", style = TypoBodyMedium, color = Color.White.copy(alpha = 0.7f))
                } else {
                    VideoOffIcon()
                }
            }

            // ── Bottom action row ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CallActionButton(if (uiState.isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic, "Mute", viewModel::toggleMute, isActive = uiState.isMuted)
                CallActionButton(if (uiState.isVideoOn) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff, "Video", viewModel::toggleVideo, isActive = !uiState.isVideoOn)
                CallActionButton(Icons.Rounded.FlipCameraAndroid, "Flip", viewModel::switchCamera)
                CallActionButton(Icons.Rounded.VolumeUp, "Speaker", viewModel::toggleSpeaker, isActive = uiState.isSpeakerOn)
                CallActionButton(Icons.Rounded.CallEnd, "End", viewModel::endCall, isDestructive = true)
            }
        }
    }
}

@Composable
private fun VideoOffIcon() {
    androidx.compose.material3.Icon(Icons.Rounded.VideocamOff, contentDescription = "Camera off", tint = Color.White.copy(alpha = 0.6f))
}

private fun statusLabel(uiState: com.qryptin.chat.model.CallUiState): String = when (uiState.connectionState) {
    CallConnectionState.DIALING    -> "Dialing"
    CallConnectionState.RINGING    -> "Ringing"
    CallConnectionState.CONNECTING -> "Securing connection"
    CallConnectionState.CONNECTED  -> uiState.formattedDuration
    CallConnectionState.ENDED      -> "Call ended"
}
