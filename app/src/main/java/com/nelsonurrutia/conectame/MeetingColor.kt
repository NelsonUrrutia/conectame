package com.nelsonurrutia.conectame

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

/**
 * Colors a meeting button can have. Saved by [name], so the order or shade can change later
 * without breaking saved meetings.
 *
 * Every shade keeps its relative luminance between 0.135 and 0.183, which is the only range that
 * meets WCAG AA in both themes at once:
 *  - white text and pictures on the button: at least 4.5:1 (all are 5.2:1 or more)
 *  - the button's edge against the light (#E6E6E6) and dark (#1C1C1C) backgrounds: at least 3:1
 * When adding a color, keep its hue and adjust its lightness until it lands in that range.
 */
enum class MeetingColor(val color: Color, @StringRes val label: Int) {
    GREEN(Color(0xFF1B7F3B), R.string.color_green),
    BLUE(Color(0xFF166BCB), R.string.color_blue),
    PURPLE(Color(0xFF9D36DC), R.string.color_purple),
    RED(Color(0xFFC62828), R.string.color_red),
    ORANGE(Color(0xFFB85000), R.string.color_orange),
    TEAL(Color(0xFF007A6B), R.string.color_teal),
    PINK(Color(0xFFD01868), R.string.color_pink),
    BROWN(Color(0xFF8E6154), R.string.color_brown);

    companion object {
        val DEFAULT = GREEN

        fun fromName(name: String?): MeetingColor = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
