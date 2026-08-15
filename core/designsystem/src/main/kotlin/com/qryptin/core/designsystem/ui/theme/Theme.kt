package com.qryptin.core.designsystem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

data class QryptColors(
    val primaryGradient: List<Color>,
    val surfaceGradient: List<Color>,
)

val LocalQryptColors = staticCompositionLocalOf {
    QryptColors(
        primaryGradient = listOf(QryptPrimary, QryptPrimaryDark),
        surfaceGradient = listOf(QryptSurface, QryptBackground)
    )
}

val MaterialTheme.qrypt: QryptColors
    @Composable
    @ReadOnlyComposable
    get() = LocalQryptColors.current

private val DarkColorScheme = darkColorScheme(
    primary = QryptPrimary,
    secondary = QryptSecondary,
    // Transparent: Scaffold/Surface containers that use
    // colorScheme.background as their default containerColor let the
    // global AppBackground (painted once in QryptINTheme below) show
    // through everywhere, instead of every screen needing its own copy.
    background = Color.Transparent,
    surface = QryptSurface.copy(alpha = 0.88f),
    error = QryptError,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = QryptPrimary,
    secondary = QryptSecondary,
    background = Color.Transparent,
    surface = Color.White.copy(alpha = 0.92f),
    error = QryptError,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
)

@Composable
fun QryptINTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val qryptColors = QryptColors(
        primaryGradient = if (darkTheme) listOf(QryptPrimaryDark, QryptPrimary) 
                          else listOf(QryptPrimary, QryptPrimaryLight),
        surfaceGradient = if (darkTheme) listOf(QryptSurface, QryptBackground)
                          else listOf(Color.White, Color(0xFFF1F5F9))
    )

    CompositionLocalProvider(
        LocalQryptColors provides qryptColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
        ) {
            // Single, centralized background layer for the ENTIRE app.
            // Every screen sits on top of this — nothing below needs to
            // (or should) paint its own background image.
            Box(modifier = Modifier.fillMaxSize()) {
                AppBackground(darkTheme = darkTheme)
                content()
            }
        }
    }
}
