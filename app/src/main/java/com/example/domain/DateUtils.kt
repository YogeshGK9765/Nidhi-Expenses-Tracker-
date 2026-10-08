package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val standardDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    fun getCurrentTimeFormatted(): String {
        return timeFormat.format(Date())
    }

    fun getTodayStartMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getTodayEndMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun getStartOfWeekMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfMonthMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfYearMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun formatDateGroupHeader(millis: Long): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.timeInMillis = millis

        val isSameYear = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        val isToday = isSameYear && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        val isYesterday = isSameYear && calendar.get(Calendar.DAY_OF_YEAR) == (today.get(Calendar.DAY_OF_YEAR) - 1)

        return when {
            isToday -> "TODAY"
            isYesterday -> "YESTERDAY"
            isSameYear -> SimpleDateFormat("dd MMMM", Locale.getDefault()).format(calendar.time).uppercase()
            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time).uppercase()
        }
    }

    fun formatRelativeDateTime(dateMillis: Long, timeStr: String?): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        calendar.timeInMillis = dateMillis

        val isSameYear = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        val isToday = isSameYear && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        val isYesterday = isSameYear && calendar.get(Calendar.DAY_OF_YEAR) == (today.get(Calendar.DAY_OF_YEAR) - 1)

        val timePart = if (!timeStr.isNullOrBlank()) ", $timeStr" else ""

        return when {
            isToday -> "Today$timePart"
            isYesterday -> "Yesterday$timePart"
            else -> "${standardDateFormat.format(calendar.time)}$timePart"
        }
    }

    fun formatDateOnly(millis: Long): String {
        return standardDateFormat.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        return monthYearFormat.format(Date(millis))
    }

    fun formatYear(millis: Long): String {
        return yearFormat.format(Date(millis))
    }
}
