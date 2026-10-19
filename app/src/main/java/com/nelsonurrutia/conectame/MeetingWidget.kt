package com.nelsonurrutia.conectame

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit

/**
 * Big home-screen button for one meeting (about 2/3 of the screen wide, resizable): the meeting's
 * color, its picture and its name. Tapping it opens the app's countdown and then Zoom.
 *
 * Which meeting each button opens is stored here (widget id -> meeting id). A button without a
 * meeting (added from the widget list, or whose meeting was deleted/turned off) shows
 * "Tap to choose a meeting".
 */
class MeetingWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { render(context, manager, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        prefs(context).edit { appWidgetIds.forEach { remove(key(it)) } }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // The phone added the button we asked for: remember which meeting it belongs to.
        if (intent.action == ACTION_PINNED) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val meetingId = intent.getStringExtra(MeetingShortcuts.EXTRA_MEETING_ID)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && meetingId != null) assign(context, widgetId, meetingId)
        }
    }

    companion object {
        private const val ACTION_PINNED = "com.nelsonurrutia.conectame.action.WIDGET_PINNED"
        private const val UNASSIGNED_GRAY = 0xFF5F6368.toInt()

        /** Android 8+ and a launcher that lets apps add widgets (older phones get the small shortcut). */
        fun isSupported(context: Context): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

        /** Ask the phone to add a big button for [meeting] (it shows its own "Add widget?" prompt). */
        fun requestPin(context: Context, meeting: Meeting) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val callback = PendingIntent.getBroadcast(
                context,
                meeting.id.hashCode(),
                Intent(context, MeetingWidget::class.java)
                    .setAction(ACTION_PINNED)
                    .putExtra(MeetingShortcuts.EXTRA_MEETING_ID, meeting.id),
                // Mutable so the phone can add the new widget's id to it.
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
            AppWidgetManager.getInstance(context).requestPinAppWidget(component(context), null, callback)
        }

        fun hasButton(context: Context, meetingId: String): Boolean = widgetIdsFor(context, meetingId).isNotEmpty()

        /** Link a button to a meeting and redraw it. */
        fun assign(context: Context, widgetId: Int, meetingId: String) {
            prefs(context).edit { putString(key(widgetId), meetingId) }
            render(context, AppWidgetManager.getInstance(context), widgetId)
        }

        /** The meeting's buttons go back to "Tap to choose a meeting" (deleted or turned off). */
        fun unassign(context: Context, meetingId: String) {
            val ids = widgetIdsFor(context, meetingId)
            prefs(context).edit { ids.forEach { remove(key(it)) } }
            refreshAll(context)
        }

        /** Redraw every button, e.g. after a meeting's name or color changed. */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(component(context)).forEach { render(context, manager, it) }
        }

        private fun render(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val meetingId = prefs(context).getString(key(widgetId), null)
            val meeting = meetingId?.let { MeetingRepository(context).find(it) }
            val views = RemoteViews(context.packageName, R.layout.widget_meeting)

            val color = meeting?.color?.color?.toArgb() ?: UNASSIGNED_GRAY
            views.setInt(R.id.widget_background, "setColorFilter", color)
            val photo = meeting?.let { MeetingPhotos.load(context, it) }
            val icon = meeting?.icon ?: MeetingIcon.DEFAULT
            if (photo == null && icon == MeetingIcon.NONE) {
                views.setViewVisibility(R.id.widget_icon, View.GONE) // "No picture": just the name
            } else {
                views.setViewVisibility(R.id.widget_icon, View.VISIBLE)
                views.setImageViewBitmap(R.id.widget_icon, photo?.let { MeetingPhotos.circle(it) } ?: picture(context, icon, color))
            }

            if (meeting != null) {
                views.setTextViewText(R.id.widget_title, meeting.title)
                views.setTextViewText(R.id.widget_subtitle, context.getString(R.string.tap_to_join))
                views.setContentDescription(R.id.widget_root, context.getString(R.string.join_meeting_a11y, meeting.title))
                val join = Intent(context, MainActivity::class.java)
                    .setAction(MeetingShortcuts.ACTION_JOIN)
                    .putExtra(MeetingShortcuts.EXTRA_MEETING_ID, meeting.id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                views.setOnClickPendingIntent(R.id.widget_root, activityIntent(context, widgetId, join))
            } else {
                // Not linked to a meeting: gray, and a tap asks which meeting to use.
                views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                views.setTextViewText(R.id.widget_subtitle, context.getString(R.string.widget_choose))
                views.setContentDescription(R.id.widget_root, context.getString(R.string.widget_choose))
                val choose = Intent(context, WidgetConfigActivity::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                views.setOnClickPendingIntent(R.id.widget_root, activityIntent(context, widgetId, choose))
            }
            manager.updateAppWidget(widgetId, views)
        }

        /**
         * The meeting's picture for this button: white shapes, with the details (eyes, smile, face
         * outline) in the button's own color so they look cut out of the white, like on the app icon.
         */
        private fun picture(context: Context, icon: MeetingIcon, color: Int): Bitmap {
            val density = context.resources.displayMetrics.density
            val width = (56 * density).toInt()
            val height = (50 * density).toInt()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            icon.draw(context, Canvas(bitmap), color, 0, 0, width, height)
            return bitmap
        }

        private fun activityIntent(context: Context, widgetId: Int, intent: Intent) =
            PendingIntent.getActivity(
                context,
                widgetId, // one per button, so each opens its own meeting
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun widgetIdsFor(context: Context, meetingId: String): List<Int> {
            val manager = AppWidgetManager.getInstance(context)
            return manager.getAppWidgetIds(component(context)).filter {
                prefs(context).getString(key(it), null) == meetingId
            }
        }

        private fun component(context: Context) = ComponentName(context, MeetingWidget::class.java)
        private fun prefs(context: Context) = context.getSharedPreferences("meeting_widgets", Context.MODE_PRIVATE)
        private fun key(widgetId: Int) = "widget_$widgetId"
    }
}
