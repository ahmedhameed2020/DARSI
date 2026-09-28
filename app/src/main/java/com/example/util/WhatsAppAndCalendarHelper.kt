package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {
    fun openChat(context: Context, phone: String, message: String = "") {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "").replace("+", "")
        if (cleanPhone.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val url = if (encodedMsg.isEmpty()) {
                "https://wa.me/$cleanPhone"
            } else {
                "https://wa.me/$cleanPhone?text=$encodedMsg"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialPhone(context: Context, phone: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch phone dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun createLessonReminderMessage(
        studentName: String,
        subject: String,
        timeString: String,
        location: String,
        language: String = "en"
    ): String {
        val locationSuffix = if (location.isNotBlank()) {
            if (language == "ar") " في $location" else " ($location)"
        } else {
            ""
        }
        return if (language == "ar") {
            "مرحبًا $studentName، تذكير لطيف بموعد حصة $subject اليوم الساعة $timeString$locationSuffix. نراك قريبًا!"
        } else {
            "Hello $studentName, this is a friendly reminder for our $subject lesson scheduled today at $timeString$locationSuffix. See you soon!"
        }
    }

    fun createPaymentReminderMessage(
        studentName: String,
        amountDue: Double,
        currency: String,
        language: String = "en"
    ): String {
        val formattedAmount = CurrencyUtils.format(amountDue, currency)
        return if (language == "ar") {
            "مرحبًا $studentName، تذكير لطيف بالمبلغ المستحق وقدره $formattedAmount مقابل الدروس. شكرًا لك."
        } else {
            "Hello $studentName, this is a gentle reminder regarding the outstanding balance of $formattedAmount for our lessons. Thank you for your support!"
        }
    }
}

object GoogleCalendarHelper {
    fun exportLessonToCalendar(
        context: Context,
        title: String,
        description: String,
        location: String,
        startEpochMillis: Long,
        endEpochMillis: Long
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startEpochMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endEpochMillis)
                putExtra(CalendarContract.Events.TITLE, "Darsi: $title")
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch Calendar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
