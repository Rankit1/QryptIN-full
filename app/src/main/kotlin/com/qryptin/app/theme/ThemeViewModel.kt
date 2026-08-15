package com.qryptin.app.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// ─────────────────────────────────────────────────────────────
//  ThemeViewModel
//  Exposes the persisted theme choice as app-wide UI state.
//  Scoped to MainActivity (the single Activity), so it lives for
//  the whole app session and every recomposition of the root
//  QryptINTheme call reacts to it immediately.
// ─────────────────────────────────────────────────────────────
class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ThemeRepository(application)

    val uiState: StateFlow<AppThemeState> = repository.themeMode
        .map { mode -> AppThemeState(themeMode = mode, isLoading = false) }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = AppThemeState(isLoading = true),
        )
}
