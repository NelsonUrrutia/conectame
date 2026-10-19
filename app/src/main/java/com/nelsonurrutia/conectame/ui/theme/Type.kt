package com.nelsonurrutia.conectame.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.nelsonurrutia.conectame.R

/**
 * Atkinson Hyperlegible, by the Braille Institute: designed so letters that are easy to confuse
 * (I/l/1, O/0, b/d) look clearly different for people with low vision. SIL Open Font License,
 * see third_party/atkinson-hyperlegible/OFL.txt.
 */
val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

private fun style(size: TextUnit, lineHeight: TextUnit, bold: Boolean = false, spacing: TextUnit = 0.15.sp) =
    TextStyle(
        fontFamily = AtkinsonHyperlegible,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = size,
        lineHeight = lineHeight,
        letterSpacing = spacing,
    )

/**
 * Larger than Material's defaults, for older eyes: body text is 18sp (default 14–16sp) and nothing
 * is smaller than 16sp. Everything still grows with the phone's font-size setting.
 */
val Typography = Typography(
    displayLarge = style(48.sp, 56.sp, bold = true, spacing = 0.sp),
    displayMedium = style(42.sp, 50.sp, bold = true, spacing = 0.sp),
    displaySmall = style(36.sp, 44.sp, bold = true, spacing = 0.sp),
    headlineLarge = style(34.sp, 42.sp, bold = true, spacing = 0.sp),
    headlineMedium = style(30.sp, 38.sp, bold = true, spacing = 0.sp),
    headlineSmall = style(26.sp, 34.sp, bold = true, spacing = 0.sp),
    titleLarge = style(24.sp, 32.sp, spacing = 0.sp),
    titleMedium = style(20.sp, 28.sp, bold = true),
    titleSmall = style(18.sp, 24.sp, bold = true),
    bodyLarge = style(18.sp, 26.sp),
    bodyMedium = style(18.sp, 26.sp),
    bodySmall = style(16.sp, 22.sp),
    labelLarge = style(18.sp, 24.sp, bold = true),
    labelMedium = style(16.sp, 22.sp, bold = true),
    labelSmall = style(16.sp, 22.sp, bold = true),
)

/**
 * The app's own "Text size" setting (1 = Default), on top of the phone's font size. Applied to the
 * sp sizes themselves rather than to the font scale: Android grows big text much less than small
 * text when the font scale goes up, so headlines would barely change.
 */
val LocalTextScale = staticCompositionLocalOf { 1f }

/** [Typography] with every size and line height multiplied by [scale]. */
fun Typography.scaled(scale: Float): Typography {
    fun TextStyle.scaled() = copy(fontSize = fontSize * scale, lineHeight = lineHeight * scale)
    return copy(
        displayLarge = displayLarge.scaled(),
        displayMedium = displayMedium.scaled(),
        displaySmall = displaySmall.scaled(),
        headlineLarge = headlineLarge.scaled(),
        headlineMedium = headlineMedium.scaled(),
        headlineSmall = headlineSmall.scaled(),
        titleLarge = titleLarge.scaled(),
        titleMedium = titleMedium.scaled(),
        titleSmall = titleSmall.scaled(),
        bodyLarge = bodyLarge.scaled(),
        bodyMedium = bodyMedium.scaled(),
        bodySmall = bodySmall.scaled(),
        labelLarge = labelLarge.scaled(),
        labelMedium = labelMedium.scaled(),
        labelSmall = labelSmall.scaled(),
    )
}

/** For text sizes set directly instead of through the typography: `fontSize = 18.scaledSp`. */
val Int.scaledSp: TextUnit
    @Composable @ReadOnlyComposable get() = (this * LocalTextScale.current).sp
