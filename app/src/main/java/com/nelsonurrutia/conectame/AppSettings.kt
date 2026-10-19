package com.nelsonurrutia.conectame

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.core.graphics.drawable.toDrawable
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import com.nelsonurrutia.conectame.ui.theme.AppBackground
import com.nelsonurrutia.conectame.ui.theme.AppBackgroundDark
import com.nelsonurrutia.conectame.ui.theme.ConnectMeTheme

/** "Screen colors" in Settings: follow the phone's dark mode, or always light / dark. */
enum class ThemeMode(@StringRes val label: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark);

    companion object {
        val DEFAULT = SYSTEM
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/**
 * "Text size" in Settings. [scale] multiplies the phone's own font size, so Large is bigger than
 * whatever the phone uses. Only text inside the app changes, not the home-screen button.
 */
enum class TextSize(val scale: Float, @StringRes val label: Int) {
    DEFAULT(1f, R.string.text_size_default),
    LARGE(1.25f, R.string.text_size_large);

    companion object {
        fun fromName(name: String?): TextSize = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/** App-wide choices, saved on the phone. Compose state, so every open screen updates right away. */
object AppSettings {

    private val themeMode = mutableStateOf<ThemeMode?>(null)

    fun themeMode(context: Context): ThemeMode =
        themeMode.value ?: ThemeMode.fromName(prefs(context).getString(KEY_THEME, null)).also { themeMode.value = it }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        prefs(context).edit { putString(KEY_THEME, mode.name) }
        themeMode.value = mode
    }

    private val textSize = mutableStateOf<TextSize?>(null)

    fun textSize(context: Context): TextSize =
        textSize.value ?: TextSize.fromName(prefs(context).getString(KEY_TEXT_SIZE, null)).also { textSize.value = it }

    fun setTextSize(context: Context, size: TextSize) {
        prefs(context).edit { putString(KEY_TEXT_SIZE, size.name) }
        textSize.value = size
    }

    private fun prefs(context: Context) = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private const val KEY_THEME = "theme_mode"
    private const val KEY_TEXT_SIZE = "text_size"
}

/**
 * The app's theme with the chosen screen colors and text size. Also sets the status bar icons and
 * the window background to match, since they follow the phone's setting on their own.
 */
@Composable
fun ComponentActivity.AppTheme(content: @Composable () -> Unit) {
    val dark = when (AppSettings.themeMode(this)) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    DisposableEffect(dark) {
        val bars = if (dark) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        window.setBackgroundDrawable((if (dark) AppBackgroundDark else AppBackground).toArgb().toDrawable())
        onDispose {}
    }
    ConnectMeTheme(
        darkTheme = dark,
        dynamicColor = false,
        textScale = AppSettings.textSize(this).scale,
        content = content,
    )
}
