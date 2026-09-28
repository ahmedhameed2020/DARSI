package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_series")
data class LessonSeriesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long? = null,
    val groupId: Long? = null,
    val startTimeString: String, // e.g. "16:30"
    val durationMinutes: Int = 60,
    val recurrenceType: String = "WEEKLY", // NONE, WEEKLY, BIWEEKLY, CUSTOM
    val daysOfWeekMask: Int = 0, // Bitmask: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64
    val startDate: Long,
    val endDate: Long? = null,
    val defaultReminderMinutes: Int = 60,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seriesId: Long? = null,
    val studentId: Long? = null,
    val groupId: Long? = null,
    val lessonDate: String, // YYYY-MM-DD
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val durationMinutes: Int = 60,
    val price: Double = 0.0,
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, ABSENT, CANCELLED_BY_STUDENT, CANCELLED_BY_TUTOR, NO_SHOW
    val location: String = "In-person",
    val locationType: String = "STUDENT_HOME", // STUDENT_HOME, TUTOR_LOCATION, ONLINE, CUSTOM
    val locationLabel: String = "",
    val areaName: String = "",
    val addressText: String = "",
    val mapsLink: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val travelTimeMinutes: Int? = null,
    val meetingUrl: String = "",
    val topicCovered: String = "",
    val homework: String = "",
    val privateTutorNote: String = "",
    val googleEventId: String? = null,
    val isException: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lessonId: Long,
    val studentId: Long,
    val status: String = "ATTENDED", // ATTENDED, ABSENT, EXCUSED, LATE
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
