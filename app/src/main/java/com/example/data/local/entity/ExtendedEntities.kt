package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_exceptions")
data class LessonExceptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seriesId: Long,
    val originalEpochMillis: Long,
    val rescheduledStartMillis: Long? = null,
    val rescheduledEndMillis: Long? = null,
    val isCancelled: Boolean = false,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_notes")
data class LessonNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val lessonId: Long? = null,
    val title: String = "",
    val content: String,
    val category: String = "PROGRESS", // PROGRESS, HOMEWORK, OBSERVATION, EXAM
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_plans")
data class PaymentPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val planType: String = "PER_LESSON", // PER_LESSON, MONTHLY_FLAT, PREPAID_PACKAGE, CUSTOM
    val ratePerLesson: Double = 0.0,
    val monthlyFee: Double = 0.0,
    val billingDayOfMonth: Int = 1,
    val notes: String = "",
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#1E40AF",
    val createdAt: Long = System.currentTimeMillis()
)
