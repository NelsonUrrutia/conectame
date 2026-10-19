package com.nelsonurrutia.conectame

import android.content.Context
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * Pictures a meeting button can have, so a meeting can be recognized without reading its name.
 * All are drawn like the app icon's grandma: white shapes on a "screen" ([base]), plus optional
 * [details] (eyes, smiles, outlines) tinted with the button's color so they look cut out of the white.
 * Saved by [name], like [MeetingColor].
 */
enum class MeetingIcon(@DrawableRes val base: Int?, @DrawableRes val details: Int?, @StringRes val label: Int) {
    CAMERA(R.drawable.ic_join_camera, null, R.string.icon_camera),
    FAMILY(R.drawable.ic_family_base, R.drawable.ic_family_details, R.string.icon_family),
    DOCTOR(R.drawable.ic_doctor_base, R.drawable.ic_doctor_details, R.string.icon_doctor),
    SUIT(R.drawable.ic_suit_base, R.drawable.ic_suit_details, R.string.icon_suit),
    MEETING(R.drawable.ic_meeting_base, null, R.string.icon_meeting),
    HEART(R.drawable.ic_heart_base, null, R.string.icon_heart),

    /** No picture: the button shows only its name. */
    NONE(null, null, R.string.icon_none);

    /**
     * Draws the picture into a View-world canvas (home-screen button and shortcut icon), keeping its
     * proportions (the camera is square, the others are 56x50). [NONE] draws nothing.
     */
    fun draw(context: Context, canvas: Canvas, color: Int, left: Int, top: Int, width: Int, height: Int) {
        val baseDrawable = base?.let { ContextCompat.getDrawable(context, it) } ?: return
        val scale = minOf(width.toFloat() / baseDrawable.intrinsicWidth, height.toFloat() / baseDrawable.intrinsicHeight)
        val w = (baseDrawable.intrinsicWidth * scale).toInt()
        val h = (baseDrawable.intrinsicHeight * scale).toInt()
        val l = left + (width - w) / 2
        val t = top + (height - h) / 2
        baseDrawable.setBounds(l, t, l + w, t + h)
        baseDrawable.draw(canvas)
        details?.let { ContextCompat.getDrawable(context, it) }?.mutate()?.apply {
            setTint(color)
            setBounds(l, t, l + w, t + h)
            draw(canvas)
        }
    }

    companion object {
        /** Also used for meetings saved with the old "Grandma" picture (the grandma is now only the app icon). */
        val DEFAULT = CAMERA

        fun fromName(name: String?): MeetingIcon = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
