package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val readableDateFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun formatTime(epochMillis: Long): String {
        return timeFormat.format(Date(epochMillis))
    }

    fun formatDate(epochMillis: Long): String {
        return dateFormat.format(Date(epochMillis))
    }

    fun formatReadableDate(epochMillis: Long): String {
        return readableDateFormat.format(Date(epochMillis))
    }

    fun formatMonthYear(epochMillis: Long): String {
        return monthYearFormat.format(Date(epochMillis))
    }

    fun formatTodayDateString(): String {
        return dateFormat.format(Date())
    }

    fun getStartOfDayEpoch(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDayEpoch(calendar: Calendar = Calendar.getInstance()): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getCountdownString(startEpochMillis: Long, endEpochMillis: Long): String {
        val now = System.currentTimeMillis()
        if (now in startEpochMillis..endEpochMillis) {
            val remainingMin = TimeUnit.MILLISECONDS.toMinutes(endEpochMillis - now)
            return "In progress · ends in ${remainingMin}m"
        }
        if (now > endEpochMillis) {
            return "Finished"
        }
        val diffMillis = startEpochMillis - now
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            diffMinutes < 1 -> "Starting now"
            diffMinutes < 60 -> "Starts in $diffMinutes min"
            diffHours < 24 -> "Starts in $diffHours hr ${diffMinutes % 60}m"
            diffDays == 1L -> "Tomorrow at ${formatTime(startEpochMillis)}"
            else -> "In $diffDays days"
        }
    }

    fun getGreeting(tutorName: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        val nameClean = tutorName.trim().ifEmpty { "Teacher" }
        return "$timeGreeting, $nameClean"
    }

    fun getDaysOfWeekForDate(selectedDate: Date): List<Pair<Date, Boolean>> {
        val cal = Calendar.getInstance()
        cal.time = selectedDate
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // Gulf week starts on Sunday
        val today = Calendar.getInstance()

        val list = mutableListOf<Pair<Date, Boolean>>()
        for (i in 0..6) {
            val d = cal.time
            val isToday = (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
            list.add(Pair(d, isToday))
            cal.add(Calendar.DAY_OF_WEEK, 1)
        }
        return list
    }
}

object CurrencyUtils {
    fun format(amount: Double, currency: String = "QAR"): String {
        return if (amount % 1.0 == 0.0) {
            "$currency ${amount.toInt()}"
        } else {
            String.format(Locale.US, "$currency %.2f", amount)
        }
    }
}
