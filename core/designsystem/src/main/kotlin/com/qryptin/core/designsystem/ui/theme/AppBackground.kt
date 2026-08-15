package com.qryptin.core.designsystem.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.qryptin.core.designsystem.R

// ─────────────────────────────────────────────────────────────
//  AppBackground
//
//  Single source of truth for QryptIN's global app background.
//
//  • Light Mode  -> tricolor gradient (saffron -> white -> green)
//  • Dark Mode   -> deep blue gradient
//
//  This is painted ONCE, behind the root NavHost, inside
//  QryptINTheme (see Theme.kt). Individual screens never draw
//  their own background image — they simply let Scaffold's
//  transparent `background` color scheme value show this layer
//  through. This keeps the whole app's visual identity centrally
//  controlled and avoids hardcoding a background per screen.
// ─────────────────────────────────────────────────────────────

/** Resolves the correct background drawable for the active theme. */
fun appBackgroundRes(darkTheme: Boolean): Int =
    if (darkTheme) R.drawable.bg_dark_gradient else R.drawable.bg_light_tricolor

/**
 * Full-bleed themed background image. `ContentScale.Crop` guarantees the
 * gradient always fills the screen edge-to-edge without stretching or
 * distortion, on phones and tablets alike, regardless of aspect ratio.
 */
@Composable
fun AppBackground(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(id = appBackgroundRes(darkTheme)),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
