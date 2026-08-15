package com.qryptin.core.designsystem.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qryptin.core.designsystem.ui.theme.*

@Composable
fun QryptPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = TypoTitleMedium)
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  QryptAvatar
//  Shared circular avatar — photo, or deterministic-colour
//  initials when there's no avatarUrl. Used by both Contacts
//  and Chat so a person's avatar looks identical everywhere
//  they show up in the app.
// ─────────────────────────────────────────────────────────────
@Composable
fun QryptAvatar(
    name        : String,
    avatarUrl   : String?  = null,
    size        : androidx.compose.ui.unit.Dp = 48.dp,
    isOnline    : Boolean  = false,
    modifier    : Modifier = Modifier,
) {
    val initials = remember(name) {
        name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "?" }
    }
    val backgroundColor = remember(name) { avatarColorFor(name) }

    Box(modifier = modifier) {
        Box(
            modifier         = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (avatarUrl == null) backgroundColor else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model              = avatarUrl,
                    contentDescription = name,
                    modifier           = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Text(
                    text  = initials,
                    style = TypoLabelMedium.copy(fontWeight = FontWeight.Bold, fontSize = (size.value / 3).sp),
                    color = Color.White,
                )
            }
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size / 4)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(OnlinePresenceGreen),
            )
        }
    }
}

/** Deterministic avatar colour derived from a name so it stays stable across renders. */
private fun avatarColorFor(name: String): Color {
    val palette = listOf(
        Color(0xFF6B5EE4), Color(0xFF00897B), Color(0xFFE53935),
        Color(0xFF5E35B1), Color(0xFF1E88E5), Color(0xFF43A047),
        Color(0xFFFB8C00), Color(0xFF8E24AA), Color(0xFF00ACC1),
        Color(0xFF3949AB),
    )
    val index = name.hashCode().mod(palette.size).let { if (it < 0) it + palette.size else it }
    return palette[index]
}

/**
 * Presence dots are conventionally green regardless of brand colour — this is the
 * one intentional exception. Every *accent* (active states, badges, CTAs) stays
 * on [QryptPrimary] blue; this is purely an online/offline status convention.
 */
private val OnlinePresenceGreen = Color(0xFF00C853)

// ─────────────────────────────────────────────────────────────
//  SecureStatusBadge
//  Small pill used wherever the app needs to surface "this is
//  encrypted / this connection is secure." Always rendered in
//  QryptPrimary blue, never green — keeps a single accent colour
//  across the whole app instead of a separate "secure = green"
//  convention.
// ─────────────────────────────────────────────────────────────
@Composable
fun SecureStatusBadge(
    modifier : Modifier = Modifier,
    label    : String   = "Secured",
) {
    Row(
        modifier            = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector        = Icons.Rounded.Lock,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(12.dp),
        )
        Text(
            text  = label,
            style = TypoLabelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
