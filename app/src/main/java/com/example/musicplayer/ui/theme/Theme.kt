package com.example.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

/**
 * The app is dark-only by design (per reference). We still wire a
 * dark ColorScheme through Material3 so standard components (sliders,
 * switches) inherit the right accent, but most UI uses AppColors
 * directly for full control over the custom look.
 */
private val AppDarkColorScheme = darkColorScheme(
    primary = AppColors.Purple,
    onPrimary = AppColors.TextPrimary,
    secondary = AppColors.Blue,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceAlt,
    outline = AppColors.Border,
    error = AppColors.AccentRed
)

@Composable
fun MusicPlayerTheme(
    // Theme setting is exposed for the Settings > Appearance screen later,
    // but defaults to dark per the reference's design constraint.
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = AppDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            it.statusBarColor = AppColors.Background.toArgb()
            it.navigationBarColor = AppColors.Background.toArgb()
        }
    }

    // Phase 3.5 fix: this app's UI must always render left-to-right,
    // regardless of the device's system language. Compose otherwise
    // derives LayoutDirection from the current Locale, which mirrors
    // every Row/alignment/icon in the app when the system language is
    // Persian, Arabic, or any other RTL language — that mirroring is
    // what produced the broken layout on the test device.
    //
    // This only overrides directional *layout* (which side things sit
    // on, row order, alignment, icon auto-mirroring). It does NOT
    // affect how individual Text composables render bidirectional
    // content — Compose's text layer still runs the Unicode
    // bidirectional algorithm on the string content itself, so Persian
    // text typed into the app later (track titles, lyrics, etc.) still
    // renders correctly right-to-left within its own text run.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
