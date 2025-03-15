package com.idz.teamup.service

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateService {

    private const val DATE_FORMAT_PATTERN = "dd/MM/yyyy HH:mm"

    /**
     * Checks if a date/time string represents a time in the past
     * @param dateTimeString The date string in format "dd/MM/yyyy HH:mm"
     * @return true if the date is in the past, false otherwise or if parse fails
     */
    fun isPastEvent(dateTimeString: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
            val eventDate = dateFormat.parse(dateTimeString) ?: return false
            val currentDate = Calendar.getInstance().time
            return eventDate.before(currentDate)
        } catch (e: Exception) {
            Log.e("DateUtils", "Error parsing date: $dateTimeString", e)
            return false
        }
    }

    /**
     * Checks if a date/time string represents a time in the future
     * @param dateTimeString The date string in format "dd/MM/yyyy HH:mm"
     * @return true if the date is in the future, false otherwise or if parse fails
     */
    fun isFutureEvent(dateTimeString: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
            val eventDate = dateFormat.parse(dateTimeString) ?: return false
            val currentDate = Calendar.getInstance().time
            return eventDate.after(currentDate)
        } catch (e: Exception) {
            Log.e("DateUtils", "Error parsing date: $dateTimeString", e)
            return false
        }
    }

    /**
     * Parse a date/time string to Date object
     * @param dateTimeString The date string in format "dd/MM/yyyy HH:mm"
     * @return Date object or null if parsing fails
     */
    fun parseDate(dateTimeString: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
            dateFormat.parse(dateTimeString)
        } catch (e: Exception) {
            Log.e("DateUtils", "Error parsing date: $dateTimeString", e)
            null
        }
    }
}