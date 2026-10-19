package com.focusflow.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FocusFlowTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    val dynamicColorScheme = darkColorScheme(
        primary = FocusAccent,
        onPrimary = FocusBackground,
        secondary = FocusSecondary,
        onSecondary = FocusWhite,
        background = FocusBackground,
        onBackground = FocusWhite,
        surface = FocusPanel,
        onSurface = FocusWhite,
        surfaceVariant = FocusPanelLight,
        onSurfaceVariant = FocusSecondary
    )

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = Typography,
        content = content
    )
}
