package com.qryptin.app.theme

import android.app.Application
import com.qryptin.settings.data.SettingsRepository
import com.qryptin.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  ThemeRepository
//
//  Issue 4 fix: the persisted theme choice already lived in
//  SettingsRepository/DataStore (feature:settings), and the
//  Settings screen already let the user pick Light/Dark/System —
//  but MainActivity never read it, so QryptINTheme always fell
//  back to isSystemInDarkTheme() and the toggle had no visible
//  effect anywhere in the app.
//
//  This is a thin app-level wrapper around the existing
//  SettingsRepository rather than a second DataStore instance:
//  DataStore only allows a single active instance per file, so
//  reusing the same repository keeps theme_mode as the single
//  source of truth on disk instead of forking it.
// ─────────────────────────────────────────────────────────────
class ThemeRepository(application: Application) {

    private val settingsRepository = SettingsRepository(application)

    /** Persisted theme choice — survives app restarts and emulator restarts. */
    val themeMode: Flow<ThemeMode> = settingsRepository.themeMode
}
