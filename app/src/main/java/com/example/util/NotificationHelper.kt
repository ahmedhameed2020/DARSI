package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.DarsiDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {
    const val CHANNEL_ID = "darsi_lesson_reminders"
    private const val CHANNEL_NAME = "Lesson Reminders & Completion"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming lessons and completion status"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showLessonFinishedNotification(
        context: Context,
        lessonId: Long,
        studentOrGroupName: String,
        subject: String,
        language: String = "en"
    ) {
        createNotificationChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_LESSON_ID", lessonId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            lessonId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(if (language == "ar") "انتهت الحصة" else "Lesson Finished")
            .setContentText(
                if (language == "ar")
                    "انتهت حصة $subject مع $studentOrGroupName. اضغط لتسجيل الحضور والملاحظات."
                else
                    "$studentOrGroupName's $subject lesson has finished. Tap to record attendance & notes."
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(lessonId.toInt(), notification)
    }

    fun showLessonReminderNotification(
        context: Context,
        lessonId: Long,
        studentOrGroupName: String,
        timeFormatted: String,
        location: String,
        language: String = "en"
    ) {
        createNotificationChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_LESSON_ID", lessonId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (lessonId + 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(
                if (language == "ar") "حصة قادمة: $studentOrGroupName"
                else "Upcoming Lesson: $studentOrGroupName"
            )
            .setContentText(
                if (language == "ar") "تبدأ الساعة $timeFormatted · $location"
                else "Starts at $timeFormatted · $location"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((lessonId + 10000).toInt(), notification)
    }

    fun showLeaveSoonNotification(
        context: Context,
        lessonId: Long,
        studentName: String,
        departureTimeFormatted: String,
        travelTimeMinutes: Int,
        location: String,
        language: String = "en"
    ) {
        createNotificationChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_LESSON_ID", lessonId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (lessonId + 20000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val locText = if (location.isNotBlank()) {
            if (language == "ar") " إلى $location" else " to $location"
        } else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle(
                if (language == "ar") "حان وقت التحرك إلى $studentName"
                else "Time to Leave for $studentName"
            )
            .setContentText(
                if (language == "ar")
                    "غادر بحلول $departureTimeFormatted (مدة الوصول $travelTimeMinutes دقيقة$locText)"
                else
                    "Leave by $departureTimeFormatted (Est. travel $travelTimeMinutes min$locText)"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((lessonId + 20000).toInt(), notification)
    }
}

class LessonReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lessonId = intent.getLongExtra("LESSON_ID", -1L)
                val language = DarsiDatabase.getInstance(context)
                    .tutorSettingsDao()
                    .getSettingsDirect()
                    ?.appLanguage ?: "en"
                val isArabic = language == "ar"
                val name = intent.getStringExtra("NAME") ?: if (isArabic) "الطالب" else "Student"
                val isFinished = intent.getBooleanExtra("IS_FINISHED", false)
                val subject = intent.getStringExtra("SUBJECT") ?: if (isArabic) "الحصة" else "Lesson"
                val timeFormatted = intent.getStringExtra("TIME") ?: ""
                val location = intent.getStringExtra("LOCATION") ?: if (isArabic) "حضوري" else "In-person"

                if (isFinished) {
                    NotificationHelper.showLessonFinishedNotification(
                        context, lessonId, name, subject, language
                    )
                } else {
                    NotificationHelper.showLessonReminderNotification(
                        context, lessonId, name, timeFormatted, location, language
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
