package com.nelsonurrutia.conectame

import java.util.UUID

/** One saved Zoom meeting, shown as a big button on the home screen. */
data class Meeting(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val link: String,
    val password: String,
    val displayName: String,
    val color: MeetingColor = MeetingColor.DEFAULT,
    val icon: MeetingIcon = MeetingIcon.DEFAULT,
    /** When the meeting's photo was saved (0 = no photo, show [icon]). Also makes the app reload a changed photo. */
    val photo: Long = 0L,
    /** Whether this meeting also has its own icon on the phone's home screen. */
    val homeShortcut: Boolean = false,
)

val Meeting.hasPhoto: Boolean get() = photo != 0L

/** Meeting ID + passcode extracted from whatever the user pasted (a full link or just the number). */
data class ZoomJoinInfo(val meetingNumber: String, val password: String?)

object ZoomLinkParser {

    private val linkNumber = Regex("""/(?:j|w|wc/join|s)/(\d{9,11})""")
    private val pwdParam = Regex("""[?&]pwd=([^&#\s]+)""")

    /**
     * Accepts things like:
     *  - https://us02web.zoom.us/j/85012345678?pwd=AbC123
     *  - zoommtg://zoom.us/join?confno=85012345678&pwd=AbC123
     *  - 850 1234 5678
     * Returns null if no meeting ID can be found.
     */
    fun parse(input: String): ZoomJoinInfo? {
        val text = input.trim()
        if (text.isEmpty()) return null

        val number = linkNumber.find(text)?.groupValues?.get(1)
            ?: Regex("""confno=(\d{9,11})""").find(text)?.groupValues?.get(1)
            ?: text.filter { it.isDigit() }.takeIf { it.length in 9..11 && text.none { c -> c.isLetter() } }
            ?: return null

        val pwd = pwdParam.find(text)?.groupValues?.get(1)
        return ZoomJoinInfo(number, pwd)
    }
}
