package com.example.musicplayer.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.settings.AppPreferences
import com.example.musicplayer.data.settings.AppearanceOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Resolves the persisted [AppearanceOption] to the boolean
 * [MusicPlayerTheme] actually needs. Kept deliberately tiny and
 * separate from the Settings screen's own ViewModel so
 * [com.example.musicplayer.MainActivity] only depends on exactly what
 * it needs to pick a theme at the root of the composition.
 *
 * Light theme is intentionally never resolved to `darkTheme = false`
 * here: there is no complete, quality-checked light [AppColors]
 * palette yet (see Theme.kt), so exposing it would mean either an
 * unfinished light UI or a Light option that's secretly identical to
 * Dark — either way, a broken/fake option the Phase 5 spec explicitly
 * says not to ship. The Appearance screen itself only offers System
 * Default and Dark as selectable for the same reason.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    appPreferences: AppPreferences
) : ViewModel() {
    val isDarkTheme: StateFlow<Boolean> = appPreferences.appearance
        .map { true } // Dark and System Default both currently resolve to the app's one complete palette.
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}
