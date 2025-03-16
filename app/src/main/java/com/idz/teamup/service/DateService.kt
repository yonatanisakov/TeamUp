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
     * Checks if registration for an event is still open
     * @param deadlineString The registration deadline in format "dd/MM/yyyy HH:mm"
     * @return true if registration is still open, false if closed
     */
    fun isRegistrationOpen(deadlineString: String): Boolean {
        // If no deadline is set, registration is always open
        if (deadlineString.isBlank()) return true

        try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
            val deadline = dateFormat.parse(deadlineString) ?: return true
            val currentDate = Calendar.getInstance().time
            return currentDate.before(deadline)
        } catch (e: Exception) {
            Log.e("DateUtils", "Error parsing deadline: $deadlineString", e)
            return true  // Default to open if parsing fails
        }
    }

    /**
     * Calculates time remaining until deadline in a human-readable format
     * @param deadlineString The deadline in format "dd/MM/yyyy HH:mm"
     * @return String with remaining time (e.g., "2 days, 3 hours") or "Closed" if passed
     */
    fun getTimeUntilDeadline(deadlineString: String): String {
        if (deadlineString.isBlank()) return "No deadline"

        try {
            val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
            val deadline = dateFormat.parse(deadlineString) ?: return "Unknown"
            val currentDate = Calendar.getInstance().time

            if (currentDate.after(deadline)) return "Closed"

            val diffInMillis = deadline.time - currentDate.time
            val diffInSeconds = diffInMillis / 1000

            if (diffInSeconds < 60) return "Less than a minute"
            if (diffInSeconds < 3600) return "${diffInSeconds / 60} minutes"

            val hours = diffInSeconds / 3600
            if (hours < 24) return "$hours hours"

            val days = hours / 24
            return if (days == 1L) "1 day" else "$days days"
        } catch (e: Exception) {
            Log.e("DateUtils", "Error calculating time until deadline: $deadlineString", e)
            return "Unknown"
        }
    }
}