package com.nelsonurrutia.conectame

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * A meeting on the phone's home screen. Preferably a big button ([MeetingWidget], about 2/3 of the
 * screen wide with the meeting's name); on phones that can't add widgets from an app, a small 1x1
 * shortcut icon instead. Either way, tapping it opens the app's countdown and then Zoom.
 *
 * Android only lets apps *ask* to add these (the phone shows its own prompt) and never lets them
 * remove one, so turning it off turns the big button into "Tap to choose a meeting" and greys out
 * a small shortcut.
 */
object MeetingShortcuts {

    const val ACTION_JOIN = "com.nelsonurrutia.conectame.action.JOIN_MEETING"
    const val EXTRA_MEETING_ID = "meeting_id"

    fun isSupported(context: Context): Boolean =
        MeetingWidget.isSupported(context) || ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    /** Call after a meeting is saved; `before` is the saved version from before the edit (null if new). */
    fun sync(context: Context, before: Meeting?, after: Meeting) {
        if (MeetingWidget.isSupported(context)) {
            when {
                after.homeShortcut && !MeetingWidget.hasButton(context, after.id) -> MeetingWidget.requestPin(context, after)
                !after.homeShortcut && before?.homeShortcut == true -> MeetingWidget.unassign(context, after.id)
            }
            MeetingWidget.refreshAll(context) // new name or color
            syncExistingSmallShortcut(context, before, after)
        } else {
            syncSmallShortcut(context, before, after)
        }
    }

    /** Call when a meeting is deleted. */
    fun onDeleted(context: Context, meeting: Meeting) {
        MeetingWidget.unassign(context, meeting.id)
        if (pinnedIds(context).contains(meeting.id)) {
            disable(context, meeting.id, context.getString(R.string.shortcut_meeting_deleted))
        }
    }

    /** Keeps a small shortcut added by an earlier version in step, without asking for a new one. */
    private fun syncExistingSmallShortcut(context: Context, before: Meeting?, after: Meeting) {
        if (!pinnedIds(context).contains(after.id)) return
        if (after.homeShortcut) {
            val shortcut = build(context, after)
            ShortcutManagerCompat.enableShortcuts(context, listOf(shortcut))
            ShortcutManagerCompat.updateShortcuts(context, listOf(shortcut))
        } else if (before?.homeShortcut == true) {
            disable(context, after.id, context.getString(R.string.shortcut_turned_off))
        }
    }

    private fun syncSmallShortcut(context: Context, before: Meeting?, after: Meeting) {
        val onHomeScreen = pinnedIds(context).contains(after.id)
        when {
            after.homeShortcut -> {
                val shortcut = build(context, after)
                ShortcutManagerCompat.enableShortcuts(context, listOf(shortcut)) // in case it was turned off before
                if (onHomeScreen) {
                    ShortcutManagerCompat.updateShortcuts(context, listOf(shortcut)) // new name or color
                } else {
                    ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
                }
            }
            before?.homeShortcut == true && onHomeScreen ->
                disable(context, after.id, context.getString(R.string.shortcut_turned_off))
        }
    }

    private fun disable(context: Context, id: String, message: String) {
        ShortcutManagerCompat.disableShortcuts(context, listOf(id), message)
    }

    private fun pinnedIds(context: Context): Set<String> =
        ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED).map { it.id }.toSet()

    private fun build(context: Context, meeting: Meeting): ShortcutInfoCompat {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(ACTION_JOIN)
            .putExtra(EXTRA_MEETING_ID, meeting.id)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return ShortcutInfoCompat.Builder(context, meeting.id)
            .setShortLabel(meeting.title)
            .setLongLabel(meeting.title)
            .setIcon(IconCompat.createWithAdaptiveBitmap(icon(context, meeting)))
            .setIntent(intent)
            .build()
    }

    /** Adaptive icon (108dp canvas): the meeting's color with its white picture in the middle. */
    private fun icon(context: Context, meeting: Meeting): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (108 * density).toInt()
        // 48x43dp: its corners stay inside the 66dp circle every launcher shape shows.
        val width = (48 * density).toInt()
        val height = width * 50 / 56
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val color = meeting.color.color.toArgb()
        canvas.drawColor(color)
        val icon = meeting.icon.takeUnless { it == MeetingIcon.NONE } ?: MeetingIcon.CAMERA // an icon can't be empty
        icon.draw(context, canvas, color, (size - width) / 2, (size - height) / 2, width, height)
        return bitmap
    }
}
