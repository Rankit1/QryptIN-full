package com.qryptin.app.theme

import com.qryptin.settings.model.ThemeMode

// ─────────────────────────────────────────────────────────────
//  AppThemeState
//  App-wide theme state read by MainActivity to drive
//  QryptINTheme(darkTheme = ...) at the root of the composition
//  tree, so every screen (chats, contacts, calls, settings,
//  auth, profile, conversation) re-themes instantly together.
// ─────────────────────────────────────────────────────────────
data class AppThemeState(
    val themeMode : ThemeMode = ThemeMode.LIGHT,
    val isLoading : Boolean   = true,
)
