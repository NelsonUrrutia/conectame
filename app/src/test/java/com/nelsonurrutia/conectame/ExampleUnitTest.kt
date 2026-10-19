package com.nelsonurrutia.conectame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ZoomLinkParserTest {

    @Test
    fun parsesFullInviteLink() {
        val info = ZoomLinkParser.parse("https://us02web.zoom.us/j/85012345678?pwd=AbC123.1")
        assertEquals(ZoomJoinInfo("85012345678", "AbC123.1"), info)
    }

    @Test
    fun parsesLinkWithoutPassword() {
        assertEquals(ZoomJoinInfo("85012345678", null), ZoomLinkParser.parse("https://zoom.us/j/85012345678"))
    }

    @Test
    fun parsesPlainMeetingIdWithSpaces() {
        assertEquals(ZoomJoinInfo("85012345678", null), ZoomLinkParser.parse(" 850 1234 5678 "))
    }

    @Test
    fun rejectsGarbage() {
        assertNull(ZoomLinkParser.parse(""))
        assertNull(ZoomLinkParser.parse("hello"))
        assertNull(ZoomLinkParser.parse("12345"))
    }
}
