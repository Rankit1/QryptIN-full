package com.qryptin.contacts.ui.screens.addcontact

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qryptin.core.designsystem.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────
//  ContactSavedScreen — Screen 4
// ─────────────────────────────────────────────────────────────
@Composable
fun ContactSavedScreen(
    onViewContact: () -> Unit,
) {
    // Trigger entrance animations after a short delay
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = QryptDimens.PaddingScreenH),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {

            Spacer(Modifier.weight(1f))

            // ── Success animation area ────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = scaleIn(
                    initialScale   = 0.5f,
                    animationSpec  = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium,
                    ),
                ) + fadeIn(tween(400)),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(220.dp),
                ) {
                    // Floating dot confetti
                    ConfettiDots()

                    // Green checkmark circle
                    CheckmarkCircle()
                }
            }

            Spacer(Modifier.height(QryptDimens.SpaceXL))

            // ── Text content ──────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(400, delayMillis = 200)) +
                          slideInVertically(tween(400, delayMillis = 200)) { it / 2 },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(QryptDimens.SpaceSM),
                ) {
                    Text(
                        "Contact Saved!",
                        style     = TypoDisplaySmall.copy(fontWeight = FontWeight.Bold),
                        color     = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "The contact has been saved successfully.",
                        style     = TypoBodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.weight(1.5f))

            // ── View Contact button ───────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(300, delayMillis = 350)) +
                          slideInVertically(tween(300, delayMillis = 350)) { it / 2 },
            ) {
                QryptPrimaryButton(
                    text     = "View Contact",
                    onClick  = onViewContact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = QryptDimens.SpaceXL),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  CheckmarkCircle  — pulsing green circle with white check
// ─────────────────────────────────────────────────────────────
@Composable
private fun CheckmarkCircle() {
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue   = 1f,
        targetValue    = 1.06f,
        animationSpec  = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    val green = MaterialTheme.colorScheme.secondary

    Surface(
        modifier = Modifier
            .size(100.dp)
            .scale(pulseScale),
        shape = RoundedCornerShape(50),
        color = green,
        shadowElevation = 8.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = "Success",
                tint     = Color.White,
                modifier = Modifier.size(52.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ConfettiDots — orbiting coloured dots
// ─────────────────────────────────────────────────────────────
@Composable
private fun ConfettiDots() {
    data class Dot(
        val angle    : Float,
        val radius   : Float,
        val size     : Float,
        val color    : Color,
        val phase    : Float,
    )

    val dotColors = listOf(
        Color(0xFF4CAF50),   // green
        Color(0xFF2196F3),   // blue
        Color(0xFFFF9800),   // orange
        Color(0xFFE91E63),   // pink
        Color(0xFF9C27B0),   // purple
        Color(0xFF00BCD4),   // cyan
    )

    val dots = remember {
        List(14) { i ->
            Dot(
                angle  = (i * 360f / 14f),
                radius = Random.nextFloat() * 30f + 55f,
                size   = Random.nextFloat() * 5f + 4f,
                color  = dotColors[i % dotColors.size],
                phase  = Random.nextFloat() * 2f,
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confettiTime",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        dots.forEach { dot ->
            val angle  = Math.toRadians((dot.angle + time * 40f).toDouble())
            val bob    = sin((time * 2f + dot.phase) * Math.PI).toFloat() * 6f
            val x      = cx + cos(angle).toFloat() * dot.radius
            val y      = cy + sin(angle).toFloat() * dot.radius + bob
            drawCircle(
                color  = dot.color,
                radius = dot.size,
                center = Offset(x, y),
                alpha  = 0.80f,
            )
        }
    }
}
