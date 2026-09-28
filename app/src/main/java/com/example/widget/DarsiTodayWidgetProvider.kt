package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.DarsiDatabase
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DarsiTodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = DarsiDatabase.getInstance(context)
                val todayStr = DateTimeUtils.formatTodayDateString()
                val lessons = db.lessonDao().getLessonsForDateDirect(todayStr)
                val now = System.currentTimeMillis()

                val upcomingLessons = lessons
                    .filter { !it.status.startsWith("CANCELLED") }
                    .sortedBy { it.startEpochMillis }

                val nextLesson = upcomingLessons.firstOrNull { it.startEpochMillis >= (now - 15 * 60 * 1000L) }
                    ?: upcomingLessons.firstOrNull()

                val laterLessons = if (nextLesson != null) {
                    upcomingLessons.filter { it.id != nextLesson.id }
                } else emptyList()

                var nextTitle = "No lessons scheduled"
                var nextSub = "Enjoy your free time"
                var nextTime = "--:--"
                var nextCountdown = ""
                var nextLessonId: Long = -1L

                if (nextLesson != null) {
                    nextLessonId = nextLesson.id
                    val shortLoc = when {
                        nextLesson.locationType == "ONLINE" -> "Online"
                        nextLesson.areaName.isNotBlank() -> nextLesson.areaName
                        nextLesson.locationLabel.isNotBlank() -> nextLesson.locationLabel
                        else -> ""
                    }
                    val locSuffix = if (shortLoc.isNotBlank()) " · $shortLoc" else ""

                    if (nextLesson.groupId != null) {
                        val grp = db.groupDao().getGroupByIdDirect(nextLesson.groupId)
                        nextTitle = grp?.name ?: "Group Lesson"
                        val groupArea = if (shortLoc.isBlank() && grp?.areaName?.isNotBlank() == true) " · ${grp.areaName}" else locSuffix
                        nextSub = "${grp?.grade ?: ""} · ${grp?.subject ?: ""}$groupArea"
                    } else if (nextLesson.studentId != null) {
                        val stu = db.studentDao().getStudentByIdDirect(nextLesson.studentId)
                        nextTitle = stu?.name ?: "Student"
                        val studentArea = if (shortLoc.isBlank() && stu?.areaName?.isNotBlank() == true) " · ${stu.areaName}" else locSuffix
                        nextSub = "${stu?.grade ?: ""} · ${stu?.subject ?: ""}$studentArea"
                    }
                    nextTime = DateTimeUtils.formatTime(nextLesson.startEpochMillis)
                    nextCountdown = "• " + DateTimeUtils.getCountdownString(nextLesson.startEpochMillis, nextLesson.endEpochMillis)
                }

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_darsi_today)

                    // Root click launches main activity
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val mainPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

                    // Next lesson card click deep links to specific lesson
                    if (nextLessonId > 0) {
                        val lessonIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("OPEN_LESSON_ID", nextLessonId)
                        }
                        val lessonPendingIntent = PendingIntent.getActivity(
                            context,
                            widgetId,
                            lessonIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_next_lesson_card, lessonPendingIntent)
                    }

                    views.setTextViewText(R.id.widget_next_name, nextTitle)
                    views.setTextViewText(R.id.widget_next_sub, nextSub)
                    views.setTextViewText(R.id.widget_next_time, nextTime)
                    views.setTextViewText(R.id.widget_next_countdown, nextCountdown)

                    // Later lessons
                    if (laterLessons.isNotEmpty()) {
                        views.setViewVisibility(R.id.widget_later_label, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_later_1, View.VISIBLE)
                        val l1 = laterLessons[0]
                        val name1 = getLessonDisplayName(db, l1)
                        views.setTextViewText(R.id.widget_later_1, "${DateTimeUtils.formatTime(l1.startEpochMillis)}  $name1")

                        if (laterLessons.size > 1) {
                            views.setViewVisibility(R.id.widget_later_2, View.VISIBLE)
                            val l2 = laterLessons[1]
                            val name2 = getLessonDisplayName(db, l2)
                            views.setTextViewText(R.id.widget_later_2, "${DateTimeUtils.formatTime(l2.startEpochMillis)}  $name2")
                        } else {
                            views.setViewVisibility(R.id.widget_later_2, View.GONE)
                        }
                    } else {
                        views.setViewVisibility(R.id.widget_later_label, View.GONE)
                        views.setViewVisibility(R.id.widget_later_1, View.GONE)
                        views.setViewVisibility(R.id.widget_later_2, View.GONE)
                    }

                    val totalCount = upcomingLessons.size
                    views.setTextViewText(
                        R.id.widget_footer,
                        if (totalCount == 1) "1 lesson today" else "$totalCount lessons today"
                    )

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }

        private suspend fun getLessonDisplayName(db: DarsiDatabase, lesson: com.example.data.local.entity.LessonEntity): String {
            return if (lesson.groupId != null) {
                db.groupDao().getGroupByIdDirect(lesson.groupId)?.name ?: "Group"
            } else if (lesson.studentId != null) {
                db.studentDao().getStudentByIdDirect(lesson.studentId)?.name ?: "Student"
            } else "Lesson"
        }
    }
}

object DarsiWidgetHelper {
    fun updateWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, DarsiTodayWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            DarsiTodayWidgetProvider.updateAllWidgets(context, appWidgetManager, appWidgetIds)
        }
    }
}
