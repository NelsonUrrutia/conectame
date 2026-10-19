package com.nelsonurrutia.conectame.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Same slots as the light scheme, for phones in dark mode. Text is #E6E6E6 (13.6:1 on the
// background, 11.2:1 on cards and dialogs); the meeting colors themselves don't change.
private val DarkColorScheme = darkColorScheme(
    primary = ActionBlueDark,
    onPrimary = Color(0xFF0D3C6E),
    primaryContainer = Color(0xFF0D3C6E),
    onPrimaryContainer = Color(0xFFD6E6F8),
    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF263238),
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFDDE3E7),
    tertiary = Color(0xFFB0BEC5),
    onTertiary = Color(0xFF263238),
    tertiaryContainer = Color(0xFF37474F),
    onTertiaryContainer = Color(0xFFDDE3E7),
    background = AppBackgroundDark,
    onBackground = Color(0xFFE6E6E6),
    surface = AppBackgroundDark,
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFFC4C4C4),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF4A4A4A),
    surfaceBright = Color(0xFF383838),
    surfaceDim = AppBackgroundDark,
    surfaceContainerLowest = Color(0xFF2C2C2C), // cards (white in light mode)
    surfaceContainerLow = Color(0xFF242424),
    surfaceContainer = Color(0xFF282828),
    surfaceContainerHigh = Color(0xFF2C2C2C), // dialogs
    surfaceContainerHighest = Color(0xFF333333),
    surfaceTint = Color.Transparent,
    error = Color(0xFFEF9A9A), // Delete buttons: 7.9:1 on the background, 6.2:1 for their text
    onError = Color(0xFF5F1111),
)

// Every color is set on purpose: Material's defaults are purple-tinted
// (dialogs, menus, pressed states…), which clashed with the gray/blue look.
private val LightColorScheme = lightColorScheme(
    primary = ActionBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E6F8),
    onPrimaryContainer = Color(0xFF0D3C6E),
    secondary = Color(0xFF546E7A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE3E7),
    onSecondaryContainer = Color(0xFF263238),
    tertiary = Color(0xFF546E7A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDE3E7),
    onTertiaryContainer = Color(0xFF263238),
    background = AppBackground,
    onBackground = Color(0xFF1C1C1C),
    surface = AppBackground,
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFDADADA),
    onSurfaceVariant = Color(0xFF454545),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFC4C4C4),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFD9D9D9),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F7F7),
    surfaceContainer = Color(0xFFF2F2F2),
    surfaceContainerHigh = Color.White, // dialogs
    surfaceContainerHighest = Color.White,
    surfaceTint = Color.Transparent,
    error = Color(0xFFC62828), // Delete buttons
    onError = Color.White,
)

@Composable
fun ConnectMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    // The app's "Text size" setting (1 = Default)
    textScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalTextScale provides textScale) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = if (textScale == 1f) Typography else Typography.scaled(textScale),
            content = content
        )
    }
}