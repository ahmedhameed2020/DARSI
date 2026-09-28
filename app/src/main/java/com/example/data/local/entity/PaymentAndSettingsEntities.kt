package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val amount: Double,
    val currency: String = "QAR",
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "CASH", // CASH, BANK_TRANSFER, ONLINE, OTHER
    val paymentFor: String = "LESSON", // LESSON, PACKAGE, MONTHLY, OTHER
    val lessonId: Long? = null,
    val packageId: Long? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val packageName: String = "Lesson Package",
    val totalLessons: Int = 8,
    val usedLessons: Int = 0,
    val price: Double = 0.0,
    val currency: String = "QAR",
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, EXPIRED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tutor_settings")
data class TutorSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val tutorName: String = "Tutor",
    val subjects: String = "English, Mathematics",
    val defaultDurationMinutes: Int = 60,
    val defaultCurrency: String = "QAR",
    val defaultReminderMinutes: Int = 60,
    val defaultTravelBufferMinutes: Int = 10,
    val googleCalendarEnabled: Boolean = false,
    val googleCalendarName: String = "Darsi",
    val isOnboardingCompleted: Boolean = false,
    val appLanguage: String = "en", // "en" or "ar"
    val updatedAt: Long = System.currentTimeMillis()
)
