package com.nelsonurrutia.conectame

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** What happened when we tried to open a meeting. The UI decides how to show it. */
enum class JoinResult { OPENED, BAD_LINK, ZOOM_MISSING }

object ZoomLauncher {

    private const val ZOOM_PACKAGE = "us.zoom.videomeetings"

    /**
     * Opens the Zoom app straight into the meeting, using Zoom's deep link:
     *   zoomus://zoom.us/join?confno=<id>&pwd=<passcode>&uname=<display name>
     * Note: Zoom ignores `uname` when the person is signed in to a Zoom account.
     */
    fun join(context: Context, meeting: Meeting): JoinResult {
        val info = ZoomLinkParser.parse(meeting.link) ?: return JoinResult.BAD_LINK

        val uri = Uri.Builder()
            .scheme("zoomus")
            .authority("zoom.us")
            .path("join")
            .appendQueryParameter("action", "join")
            .appendQueryParameter("confno", info.meetingNumber)
            .apply {
                // A passcode typed by the user wins over the one embedded in the link.
                val pwd = meeting.password.trim().ifEmpty { info.password.orEmpty() }
                if (pwd.isNotEmpty()) appendQueryParameter("pwd", pwd)
                if (meeting.displayName.isNotBlank()) appendQueryParameter("uname", meeting.displayName.trim())
            }
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            JoinResult.OPENED
        } catch (e: ActivityNotFoundException) {
            JoinResult.ZOOM_MISSING
        }
    }

    fun openPlayStore(context: Context) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$ZOOM_PACKAGE"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(market)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$ZOOM_PACKAGE"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
