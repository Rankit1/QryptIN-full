package com.qryptin.auth.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qryptin.auth.R
import com.qryptin.core.designsystem.ui.theme.*
import kotlinx.coroutines.delay

private const val APP_NAME = "QryptIn"      // "Q"/"I" capital, rest lowercase — per spec
private const val TYPE_DELAY_MS = 90L

// ─────────────────────────────────────────────────────────────
//  SplashScreen — QryptIN welcome / splash screen
//
//  ONE continuous screen shown on every cold start, for BOTH
//  logged-in and logged-out users. It never repaints its own
//  background — the light tricolor / dark-blue gradient is
//  already painted app-wide by QryptINTheme (see AppBackground),
//  and MainActivity forces light mode here whenever the user is
//  not yet logged in.
//
//  Sequence:
//   1. The official QryptIN logo fades + scales in
//   2. It keeps a soft, continuous pulse/glow while up
//   3. "QryptIn" types out letter by letter beneath it:
//        Q, Qr, Qry, Qryp, Qrypt, QryptI, QryptIn
//   4. Tagline fades in, screen holds briefly, then hands off
//
//  The logo is tappable at any point: tapping instantly skips
//  the remaining animation and hands off via [onFinished], same
//  as letting it play out naturally (per spec section 8).
// ─────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(onFinished: () -> Unit) {

    var skipped      by remember { mutableStateOf(false) }
    var visibleChars by remember { mutableStateOf(0) }
    var showTagline   by remember { mutableStateOf(false) }

    // One-shot entrance: fade + scale up from 0 -> 1
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }

    // Continuous soft pulse/glow while the logo is on screen
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 0.96f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.9f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.30f,
        targetValue   = 0.65f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    fun skip() {
        if (!skipped) {
            skipped = true
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(550) // let the entrance animation settle first
        while (visibleChars < APP_NAME.length) {
            if (skipped) return@LaunchedEffect
            visibleChars++
            delay(TYPE_DELAY_MS)
        }
        delay(250)
        if (skipped) return@LaunchedEffect
        showTagline = true
        delay(700)
        if (!skipped) {
            skipped = true
            onFinished()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Soft glow behind the logo
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .alpha(glowAlpha * entrance.value)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                    androidx.compose.ui.graphics.Color.Transparent,
                                ),
                            ),
                            shape = CircleShape,
                        ),
                )

                // Official QryptIN logo — same asset, unmodified, in both
                // light and dark mode. Clickable to skip the animation.
                Image(
                    painter            = painterResource(R.drawable.ic_qryptin_logo),
                    contentDescription = "QryptIN",
                    modifier           = Modifier
                        .size(120.dp)
                        .scale(entrance.value * pulseScale)
                        .alpha(entrance.value * pulseAlpha)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = { skip() },
                        ),
                )
            }

            // "QryptIn" — types out letter by letter: Q, Qr, Qry, Qryp,
            // Qrypt, QryptI, QryptIn. "Qrypt" in primary (purple/indigo),
            // "In" in secondary (green) — matches both light & dark specs.
            Text(
                text = buildAnnotatedString {
                    val typed = APP_NAME.take(visibleChars)
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(typed.take(5))
                    }
                    if (typed.length > 5) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(typed.drop(5))
                        }
                    }
                },
                style = TypoDisplayLarge.copy(
                    fontSize      = 36.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = (-0.7).sp,
                ),
            )

            // Tagline
            Text(
                text     = "Secure. Private. Encrypted.",
                style    = TypoBodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(if (showTagline) 1f else 0f),
            )
        }
    }
}
