package com.nelsonurrutia.conectame.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Main color for action buttons (Save, Add, Close…): Material Blue 800.
 * 4.6:1 on the gray background and 5.75:1 for white text on it (WCAG AA needs 4.5:1).
 */
val ActionBlue = Color(0xFF1565C0)

/** Light gray used behind every screen. */
val AppBackground = Color(0xFFE6E6E6)

/**
 * Dark gray behind every screen in dark mode (not pure black, which smears for some low-vision
 * users). Keep in sync with `app_background` in values-night/colors.xml.
 */
val AppBackgroundDark = Color(0xFF1C1C1C)

/**
 * Action buttons in dark mode: Material Blue 200, so they stay the same "blue" as in light mode.
 * 9.7:1 on the dark background and 6.4:1 for the dark blue text on it.
 */
val ActionBlueDark = Color(0xFF90CAF9)
