package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val parentPhone: String? = null,
    val school: String? = null,
    val grade: String,
    val subject: String,
    val defaultPrice: Double = 0.0,
    val defaultDurationMinutes: Int = 60,
    val paymentType: String = "PER_LESSON", // PER_LESSON, MONTHLY, PACKAGE, CUSTOM
    val privateNotes: String = "",
    val status: String = "ACTIVE", // ACTIVE, PAUSED, FINISHED
    val locationLabel: String = "Student Home",
    val areaName: String = "",
    val addressText: String = "",
    val mapsLink: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationNotes: String = "",
    val defaultLessonLocationType: String = "STUDENT_HOME", // STUDENT_HOME, TUTOR_LOCATION, ONLINE, CUSTOM
    val defaultTravelTimeMinutes: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
