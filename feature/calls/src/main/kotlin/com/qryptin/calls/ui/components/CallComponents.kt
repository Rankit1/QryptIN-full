package com.qryptin.calls.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.qryptin.calls.model.*

// ─────────────────────────────────────────────────────────────
//  CallAvatar  — initials circle, colour derived from name
// ─────────────────────────────────────────────────────────────
private val avatarColors = listOf(
    Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFFFF9800),
    Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFF009688),
    Color(0xFF795548), Color(0xFF607D8B),
)

private fun avatarColor(name: String) =
    avatarColors[name.hashCode().and(0x7fffffff) % avatarColors.size]

@Composable
fun CallAvatar(name: String, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }.ifEmpty { "?" }
    Box(
        modifier         = modifier.size(size).clip(CircleShape).background(avatarColor(name)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text      = initials,
            color     = Color.White,
            fontSize  = (size.value * 0.35f).sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  CallStatusChip  — direction icon + label
// ─────────────────────────────────────────────────────────────
@Composable
fun CallStatusChip(type: CallType, direction: CallDirection, modifier: Modifier = Modifier) {
    val (icon, tint, label) = when (direction) {
        CallDirection.OUTGOING -> Triple(Icons.Rounded.CallMade,     Color(0xFF4CAF50), "Outgoing")
        CallDirection.INCOMING -> Triple(Icons.Rounded.CallReceived, Color(0xFF2196F3), "Incoming")
        CallDirection.MISSED   -> Triple(Icons.Rounded.CallMissed,   Color(0xFFF44336), "Missed")
    }
    val typeSuffix = if (type == CallType.VIDEO) "Video Call" else "Voice Call"
    Row(
        modifier         = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Text(
            text  = "$label $typeSuffix",
            style = MaterialTheme.typography.bodySmall,
            color = if (direction == CallDirection.MISSED)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (direction == CallDirection.MISSED) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  CallActionButton  — circular icon button (call / video)
// ─────────────────────────────────────────────────────────────
@Composable
fun CallActionButton(
    icon      : ImageVector,
    onClick   : () -> Unit,
    tint      : Color    = MaterialTheme.colorScheme.primary,
    size      : Dp       = 38.dp,
    modifier  : Modifier = Modifier,
) {
    IconButton(
        onClick  = onClick,
        modifier = modifier.size(size),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ─────────────────────────────────────────────────────────────
//  SecureInfoBanner  — E2E encrypted reminder
// ─────────────────────────────────────────────────────────────
@Composable
fun SecureInfoBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Rounded.Shield,
                contentDescription = "Encrypted",
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(18.dp),
            )
            Text(
                text  = "All calls are end-to-end encrypted on QryptIN.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  BottomSecurityCard
// ─────────────────────────────────────────────────────────────
@Composable
fun BottomSecurityCard(modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Rounded.Shield,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(22.dp).padding(top = 2.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Your calls are private and secure",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "QryptIN calls are end-to-end encrypted and not stored on our servers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  OngoingCallBar  — persistent mini-bar during active call
// ─────────────────────────────────────────────────────────────
@Composable
fun OngoingCallBar(
    contactName : String,
    durationSecs: Int,
    onTap       : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    val mins = durationSecs / 60
    val secs = durationSecs % 60
    Surface(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onTap),
        color     = MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PulseDot()
                Column {
                    Text(contactName, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Text(
                        "%02d:%02d".format(mins, secs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
            Text("Tap to return", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun PulseDot() {
    val inf = rememberInfiniteTransition(label = "pulse")
    val scale by inf.animateFloat(
        initialValue   = 0.8f,
        targetValue    = 1.2f,
        animationSpec  = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label          = "scale",
    )
    Box(
        modifier = Modifier.size(10.dp).scale(scale).clip(CircleShape).background(Color.White),
    )
}

// ─────────────────────────────────────────────────────────────
//  PulseRing  — animated ring for voice/video call screens
// ─────────────────────────────────────────────────────────────
@Composable
fun PulseRing(color: Color = Color.White, modifier: Modifier = Modifier) {
    val inf  = rememberInfiniteTransition(label = "ring")
    val scale by inf.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.5f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label         = "ring_scale",
    )
    val alpha by inf.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label         = "ring_alpha",
    )
    Box(
        modifier = modifier.scale(scale).border(2.dp, color.copy(alpha = alpha), CircleShape),
    )
}

// ─────────────────────────────────────────────────────────────
//  CallHistoryCard  — single call log row
// ─────────────────────────────────────────────────────────────
@Composable
fun CallHistoryCard(
    call        : CallModel,
    onVoiceCall : () -> Unit,
    onVideoCall : () -> Unit,
    onDelete    : () -> Unit,
    onCallAgain : () -> Unit,
    timestamp   : String,
    modifier    : Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier  = modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape     = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            CallAvatar(name = call.contactName)

            Spacer(Modifier.width(12.dp))

            // Name + status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = call.contactName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                CallStatusChip(type = call.type, direction = call.direction)
            }

            // Timestamp
            Text(
                text  = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.width(4.dp))

            // Quick action: voice
            CallActionButton(
                icon    = Icons.Rounded.Call,
                onClick = onVoiceCall,
            )
            // Quick action: video
            CallActionButton(
                icon    = Icons.Rounded.Videocam,
                onClick = onVideoCall,
            )

            // Overflow menu
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.MoreVert, null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text    = { Text("Call Again") },
                        onClick = { menuExpanded = false; onCallAgain() },
                        leadingIcon = { Icon(Icons.Rounded.Call, null) },
                    )
                    DropdownMenuItem(
                        text    = { Text("Send Message") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Rounded.Message, null) },
                    )
                    DropdownMenuItem(
                        text    = { Text("View Contact") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Rounded.Person, null) },
                    )
                    DropdownMenuItem(
                        text    = { Text("Block Contact") },
                        onClick = { menuExpanded = false },
                        leadingIcon = { Icon(Icons.Rounded.Block, null) },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text    = { Text("Delete from History", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDelete() },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    )
                }
            }
        }
        HorizontalDivider(
            modifier  = Modifier.padding(start = 76.dp),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        )
    }
}
