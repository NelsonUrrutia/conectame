package com.nelsonurrutia.conectame

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

/**
 * Saves meetings on the phone (SharedPreferences, stored as JSON).
 * [meetings] is a Compose state list, so the UI updates automatically when it changes.
 */
class MeetingRepository(context: Context) {

    private val prefs = context.getSharedPreferences("meetings", Context.MODE_PRIVATE)

    val meetings = mutableStateListOf<Meeting>().apply { addAll(load()) }

    fun find(id: String): Meeting? = meetings.firstOrNull { it.id == id }

    fun save(meeting: Meeting) {
        val index = meetings.indexOfFirst { it.id == meeting.id }
        if (index >= 0) meetings[index] = meeting else meetings.add(meeting)
        persist()
    }

    fun delete(id: String) {
        meetings.removeAll { it.id == id }
        persist()
    }

    private fun load(): List<Meeting> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Meeting(
                id = o.getString("id"),
                title = o.optString("title"),
                link = o.optString("link"),
                password = o.optString("password"),
                displayName = o.optString("displayName"),
                color = MeetingColor.fromName(o.optString("color")),
                icon = MeetingIcon.fromName(o.optString("icon")),
                photo = o.optLong("photo", 0L),
                homeShortcut = o.optBoolean("homeShortcut", false),
            )
        }
    }

    private fun persist() {
        val array = JSONArray()
        meetings.forEach { m ->
            array.put(
                JSONObject()
                    .put("id", m.id)
                    .put("title", m.title)
                    .put("link", m.link)
                    .put("password", m.password)
                    .put("displayName", m.displayName)
                    .put("color", m.color.name)
                    .put("icon", m.icon.name)
                    .put("photo", m.photo)
                    .put("homeShortcut", m.homeShortcut)
            )
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object {
        const val KEY = "meetings_json"
    }
}
