package com.example.calendareventcrud

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.util.TimeZone
import android.util.Log
import androidx.core.content.ContextCompat


fun getPrimaryCalendarId(context: Context): Long? {
    val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
    val cursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        "${CalendarContract.Calendars.IS_PRIMARY} = 1",
        null,
        null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            return it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
        }
    }
    return null
}
/*
fun addEventToCalendar(
    context: Context,
    calendarId: Long,
    title: String,
    description: String,
    location: String,
    startTime: Long,
    endTime: Long,
    isAllDay: Boolean = false
): Long? {
    val values = ContentValues().apply {
        put(CalendarContract.Events.CALENDAR_ID, calendarId)
        put(CalendarContract.Events.TITLE, title)
        put(CalendarContract.Events.DESCRIPTION, description)
        put(CalendarContract.Events.EVENT_LOCATION, location)
        put(CalendarContract.Events.DTSTART, startTime)
        put(CalendarContract.Events.DTEND, endTime)
        put(CalendarContract.Events.ALL_DAY, if (isAllDay) 1 else 0)
        put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
    }

    val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    return uri?.lastPathSegment?.toLong()
}


fun getEvents(context: Context, calendarId: Long): List<Map<String, String>> {
    val events = mutableListOf<Map<String, String>>()
    val projection = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DESCRIPTION,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND
    )

    val cursor = context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        "${CalendarContract.Events.CALENDAR_ID} = ?",
        arrayOf(calendarId.toString()),
        null
    )

    cursor?.use {
        while (it.moveToNext()) {
            val event = mapOf(
                "id" to it.getString(it.getColumnIndexOrThrow(CalendarContract.Events._ID)),
                "title" to it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)),
                "description" to it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)),
                "location" to it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)),
                "start" to it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)),
                "end" to it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
            )
            events.add(event)
        }
    }
    return events
}


fun updateEvent(
    context: Context,
    eventId: Long,
    title: String? = null,
    description: String? = null,
    location: String? = null,
    startTime: Long? = null,
    endTime: Long? = null
): Int {
    val values = ContentValues().apply {
        title?.let { put(CalendarContract.Events.TITLE, it) }
        description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
        location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
        startTime?.let { put(CalendarContract.Events.DTSTART, it) }
        endTime?.let { put(CalendarContract.Events.DTEND, it) }
    }

    return context.contentResolver.update(
        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
        values,
        null,
        null
    )
}

 */


fun deleteEvent(context: Context, eventId: Long): Int {
    return context.contentResolver.delete(
        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
        null,
        null
    )
}


data class CalendarEvent(
    val id: Long? = null,
    val calendarId: Long,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean = false,
    val timeZone: String = TimeZone.getDefault().id
)

object CalendarUtils {

    fun hasCalendarPermissions(context: Context): Boolean {
        val permission =
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR)
        return permission == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun addEventToCalendar(context: Context, event: CalendarEvent): Long? {
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, event.calendarId)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DTSTART, event.startTime)
            put(CalendarContract.Events.DTEND, event.endTime)
            put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
            put(CalendarContract.Events.EVENT_TIMEZONE, event.timeZone)
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment?.toLong()
        } catch (e: Exception) {
            Log.e("CalendarCRUD", "Error inserting event: ${e.message}", e)
            null
        }
    }

    fun getEvents(context: Context, calendarId: Long): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_TIMEZONE
        )

        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events.CALENDAR_ID} = ?",
            arrayOf(calendarId.toString()),
            "${CalendarContract.Events.DTSTART} ASC LIMIT 100" // Optional: Limit and order
        )

        cursor?.use {
            while (it.moveToNext()) {
                val event = CalendarEvent(
                    id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID)),
                    calendarId = calendarId,
                    title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)),
                    location = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)),
                    startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)),
                    endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)),
                    isAllDay = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)) == 1,
                    timeZone = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_TIMEZONE))
                )
                events.add(event)
            }
        }
        return events
    }

    fun getAllCalendarEvents(context: Context): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.EVENT_LOCATION
        )

        val uri = CalendarContract.Events.CONTENT_URI
        val selection = null // You can apply a filter if needed
        val selectionArgs = null
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC" // Sort by start date

        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                val description = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
                val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                val calendarId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID))
                val location = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))

                events.add(CalendarEvent(id = id, title = title, description = description, startTime = startTime, endTime = endTime, calendarId = calendarId, location = location))
            }
        }

        return events
    }

    fun getPersonalAndGlobalEvents(context: Context): Pair<List<CalendarEvent>, List<CalendarEvent>> {
        val personalEvents = mutableListOf<CalendarEvent>()
        val globalEvents = mutableListOf<CalendarEvent>()

        val calendarProjection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        val eventProjection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.CALENDAR_ID
        )

        val calendarCursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            calendarProjection,
            null,
            null,
            null
        )

        val calendarMap = mutableMapOf<Long, Boolean>() // Map calendarId to isPersonal flag

        calendarCursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                val accountName = it.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME))
                val displayName = it.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))

                // Mark as personal if account name is user's email, otherwise global
                val isPersonal = accountName.contains("@") && !displayName.equals("Holidays", ignoreCase = true)
                calendarMap[id] = isPersonal
            }
        }

        val eventCursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            eventProjection,
            null,
            null,
            null
        )

        eventCursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                val calendarId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID))
                val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

                val event = CalendarEvent(
                    id = id,
                    calendarId = calendarId,
                    title = title,
                    startTime = startTime,
                    endTime = endTime
                )

                if (calendarMap[calendarId] == true) {
                    personalEvents.add(event)
                } else {
                    globalEvents.add(event)
                }
            }
        }

        return Pair(personalEvents, globalEvents)
    }


    fun updateEvent(context: Context, event: CalendarEvent): Int {
        if (event.id == null) throw IllegalArgumentException("Event ID is required for updating")

        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DTSTART, event.startTime)
            put(CalendarContract.Events.DTEND, event.endTime)
            put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
            put(CalendarContract.Events.EVENT_TIMEZONE, event.timeZone)
        }

        return context.contentResolver.update(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id),
            values,
            null,
            null
        )
    }

    fun deleteEvent(context: Context, eventId: Long): Int {
        return context.contentResolver.delete(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
            null,
            null
        )
    }
}
