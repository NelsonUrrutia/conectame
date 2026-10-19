package com.nelsonurrutia.conectame

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.nelsonurrutia.conectame.ui.theme.AppBackground
import com.nelsonurrutia.conectame.ui.theme.AppBackgroundDark
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG AA for the meeting buttons, in both light and dark mode (see [MeetingColor]). */
class MeetingColorTest {

    private fun contrast(a: Color, b: Color): Double {
        val (dark, light) = listOf(a.luminance().toDouble(), b.luminance().toDouble()).sorted()
        return (light + 0.05) / (dark + 0.05)
    }

    @Test
    fun whiteTextIsReadableOnEveryColor() {
        MeetingColor.entries.forEach {
            val ratio = contrast(Color.White, it.color)
            assertTrue("${it.name}: white text is $ratio:1, needs 4.5:1", ratio >= 4.5)
        }
    }

    @Test
    fun everyButtonStandsOutFromBothBackgrounds() {
        MeetingColor.entries.forEach {
            val light = contrast(it.color, AppBackground)
            val dark = contrast(it.color, AppBackgroundDark)
            assertTrue("${it.name}: $light:1 on the light background, needs 3:1", light >= 3.0)
            assertTrue("${it.name}: $dark:1 on the dark background, needs 3:1", dark >= 3.0)
        }
    }
}
