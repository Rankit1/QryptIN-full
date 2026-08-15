package com.qryptin.core.designsystem.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
//  QryptINSafeScaffold / SafeScreenContainer
//
//  Issue 2 fix — safe area + responsive device layout.
//
//  Edge-to-edge is enabled app-wide (MainActivity.enableEdgeToEdge()),
//  which means every screen draws behind the status bar, nav bar,
//  and any notch/camera cutout unless it explicitly insets its
//  content. Plain Material3 `Scaffold` already defaults its content
//  padding to `WindowInsets.safeDrawing`, but that default is easy
//  to silently lose (e.g. a future `contentWindowInsets` override,
//  or a screen that never used Scaffold in the first place). These
//  two wrappers make the safe-area behaviour explicit and give every
//  screen — with or without a Scaffold — a single, reusable way to
//  stay clear of the status bar, navigation bar, and display cutout.
//
//  • QryptINSafeScaffold — drop-in replacement for `Scaffold` for
//    screens with a topBar/bottomBar/FAB (chats, contacts, calls,
//    settings, conversations, profile, etc.)
//  • SafeScreenContainer — a plain full-bleed Box for screens that
//    don't use a Scaffold at all (e.g. custom immersive layouts)
//    but still have interactive/text content near the edges.
// ─────────────────────────────────────────────────────────────

/**
 * Drop-in replacement for [Scaffold] that always insets its content by
 * [WindowInsets.safeDrawing] (status bar + navigation bar + display
 * cutout), regardless of future call-site changes. Mirrors the subset
 * of [Scaffold]'s parameters actually used across QryptIN's screens.
 */
@Composable
fun QryptINSafeScaffold(
    modifier                     : Modifier = Modifier,
    topBar                       : @Composable () -> Unit = {},
    bottomBar                    : @Composable () -> Unit = {},
    snackbarHost                 : @Composable () -> Unit = {},
    floatingActionButton         : @Composable () -> Unit = {},
    floatingActionButtonPosition : FabPosition = FabPosition.End,
    containerColor               : Color = MaterialTheme.colorScheme.background,
    contentColor                 : Color = contentColorFor(containerColor),
    content                      : @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier                     = modifier,
        topBar                       = topBar,
        bottomBar                    = bottomBar,
        snackbarHost                 = snackbarHost,
        floatingActionButton         = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor               = containerColor,
        contentColor                 = contentColor,
        contentWindowInsets          = WindowInsets.safeDrawing,
        content                      = content,
    )
}

/**
 * Full-bleed container for screens that don't need a Scaffold (no
 * top/bottom bars) but still render content that shouldn't sit under
 * the status bar, navigation bar, or a display cutout — e.g. a close
 * button pinned to the top of a media viewer, or action buttons
 * pinned to the bottom of a full-screen call UI.
 *
 * Purely decorative full-bleed screens (splash/logo animations with
 * everything centered) can keep using a plain `Box` — there's nothing
 * near an edge to protect there.
 */
@Composable
fun SafeScreenContainer(
    modifier : Modifier = Modifier,
    content  : @Composable () -> Unit,
) {
    Box(
        modifier = modifier.safeDrawingPadding(),
    ) {
        content()
    }
}
