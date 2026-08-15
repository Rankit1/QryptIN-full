package com.qryptin.auth.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

// ─────────────────────────────────────────────────────────────
//  LogoAnimationScreen
//  Icon fades in, then "QryptIN" types out letter by letter
// ─────────────────────────────────────────────────────────────

private const val APP_NAME = "QryptIn"
private const val TYPED_DELAY_MS = 80L
private const val CURSOR_BLINK_MS = 530L

@Composable
fun LogoAnimationScreen(onAnimationEnd: () -> Unit) {

    var iconAlpha    by remember { mutableStateOf(0f) }
    var iconScale    by remember { mutableStateOf(0.7f) }
    var visibleChars by remember { mutableStateOf(0) }
    var showCursor   by remember { mutableStateOf(true) }
    var taglineAlpha by remember { mutableStateOf(0f) }

    val iconAlphaAnim by animateFloatAsState(
        iconAlpha, tween(450), label = "icon_alpha")
    val iconScaleAnim by animateFloatAsState(
        iconScale,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "icon_scale",
    )
    val taglineAlphaAnim by animateFloatAsState(
        taglineAlpha, tween(500), label = "tagline_alpha")

    // Orchestrate the entire sequence
    LaunchedEffect(Unit) {
        // 1. Icon appears
        iconAlpha = 1f
        iconScale = 1f
        delay(600)

        // 2. Type out name
        while (visibleChars < APP_NAME.length) {
            visibleChars++
            delay(TYPED_DELAY_MS)
        }

        // 3. Blink cursor briefly
        delay(200)
        repeat(3) {
            showCursor = !showCursor
            delay(CURSOR_BLINK_MS)
        }
        showCursor = false

        // 4. Tagline fades in
        taglineAlpha = 1f
        delay(700)

        // 5. Transition
        onAnimationEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.spacedBy(24.dp),
        ) {
            // App icon
            Image(
                painter            = painterResource(R.drawable.ic_qryptin_logo),
                contentDescription = null,
                modifier           = Modifier
                    .size(100.dp)
                    .scale(iconScaleAnim)
                    .alpha(iconAlphaAnim),
            )

            // Animated name
            Text(
                text  = buildAnnotatedString {
                    val typed = APP_NAME.take(visibleChars)

                    // "Qrypt" — green primary
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(typed.take(5))
                    }
                    // "IN" — secondary/orange
                    if (typed.length > 5) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(typed.drop(5))
                        }
                    }
                    // Blinking cursor
                    if (showCursor) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("|")
                        }
                    }
                },
                style = TypoDisplayLarge.copy(
                    fontSize     = 38.sp,
                    fontWeight   = FontWeight.Bold,
                    letterSpacing = (-0.8).sp,
                ),
            )

            // Tagline
            Text(
                text     = "Secure. Private. Encrypted.",
                style    = TypoBodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(taglineAlphaAnim),
            )
        }
    }
}
