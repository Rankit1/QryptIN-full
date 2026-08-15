package com.qryptin.calls.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qryptin.calls.model.CallState
import com.qryptin.calls.model.CallType
import com.qryptin.calls.ui.components.CallAvatar
import com.qryptin.calls.ui.components.PulseRing
import com.qryptin.calls.viewmodel.ActiveCallViewModel
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────
//  VideoCallScreen
// ─────────────────────────────────────────────────────────────
@Composable
fun VideoCallScreen(
    contactId     : String,
    contactName   : String,
    contactAvatar : String?,
    viewModel     : ActiveCallViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.callState.collectAsStateWithLifecycle()

    // Draggable local preview offset
    var localOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(contactId) {
        viewModel.startCall(contactId, contactName, contactAvatar, CallType.VIDEO)
    }
    LaunchedEffect(state.state) {
        if (state.state == CallState.IDLE) onNavigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Remote video placeholder ──────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)))),
            contentAlignment = Alignment.Center,
        ) {
            if (state.state != CallState.CONNECTED) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        PulseRing(modifier = Modifier.size(150.dp))
                        PulseRing(modifier = Modifier.size(120.dp))
                        CallAvatar(name = contactName, size = 90.dp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(contactName, style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = when (state.state) {
                            CallState.CALLING    -> "Video calling…"
                            CallState.CONNECTING -> "Connecting…"
                            else                 -> "Ringing…"
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                // Simulated remote video - solid dark with avatar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CallAvatar(name = contactName, size = 100.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(contactName, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            }
        }

        // ── Secure indicator ──────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(Icons.Rounded.Lock, null, tint = Color(0xFF80DEEA), modifier = Modifier.size(12.dp))
                Text("Encrypted", color = Color.White, fontSize = 11.sp)
                if (state.state == CallState.CONNECTED) {
                    val m = state.durationSecs / 60; val s = state.durationSecs % 60
                    Text("· %02d:%02d".format(m, s), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }

        // ── Draggable local preview ───────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .offset { IntOffset(localOffset.x.roundToInt(), localOffset.y.roundToInt()) }
                .size(width = 100.dp, height = 140.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Color(0xFF263238))
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        localOffset = Offset(localOffset.x + drag.x, localOffset.y + drag.y)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (!state.isCameraOn) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.VideocamOff, null, tint = Color.White.copy(alpha = 0.5f))
                    Text("Camera off", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            } else {
                Text("You", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }

        // ── Controls overlay ──────────────────────────────────
        Box(
            modifier         = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                .padding(bottom = 40.dp, top = 24.dp),
        ) {
            VideoCallControls(
                isMuted     = state.isMuted,
                isCameraOn  = state.isCameraOn,
                isSpeaker   = state.isSpeakerOn,
                onMute      = viewModel::toggleMute,
                onCamera    = viewModel::toggleCamera,
                onSpeaker   = viewModel::toggleSpeaker,
                onFlipCam   = viewModel::switchCamera,
                onEnd       = { viewModel.endCall(); onNavigateBack() },
            )
        }
    }
}

@Composable
private fun VideoCallControls(
    isMuted    : Boolean,
    isCameraOn : Boolean,
    isSpeaker  : Boolean,
    onMute     : () -> Unit,
    onCamera   : () -> Unit,
    onSpeaker  : () -> Unit,
    onFlipCam  : () -> Unit,
    onEnd      : () -> Unit,
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            VideoControlBtn(
                icon    = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                label   = if (isMuted) "Unmute" else "Mute",
                active  = isMuted,
                onClick = onMute,
            )
            VideoControlBtn(
                icon    = if (isCameraOn) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                label   = if (isCameraOn) "Camera" else "Off",
                active  = !isCameraOn,
                onClick = onCamera,
            )
            VideoControlBtn(
                icon    = if (isSpeaker) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeDown,
                label   = "Speaker",
                active  = isSpeaker,
                onClick = onSpeaker,
            )
            VideoControlBtn(
                icon    = Icons.Rounded.FlipCameraAndroid,
                label   = "Flip",
                onClick = onFlipCam,
            )
        }

        Spacer(Modifier.height(20.dp))

        // End call
        IconButton(
            onClick  = onEnd,
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFFF44336)),
        ) {
            Icon(Icons.Rounded.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun VideoControlBtn(
    icon    : ImageVector,
    label   : String,
    active  : Boolean = false,
    onClick : () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick  = onClick,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else Color.White.copy(alpha = 0.15f)),
        ) {
            Icon(
                icon, label,
                tint     = if (active) Color(0xFF1A237E) else Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}
