package com.example.domain.model

import com.example.data.local.dao.GroupMemberWithStudent
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity

data class LessonWithDetails(
    val lesson: LessonEntity,
    val student: StudentEntity? = null,
    val group: GroupEntity? = null,
    val groupMembers: List<GroupMemberWithStudent> = emptyList()
) {
    val title: String
        get() = group?.name ?: student?.name ?: "Lesson"

    val subtitle: String
        get() {
            return if (group != null) {
                "${group.grade} · ${group.subject} (${groupMembers.size} students)"
            } else if (student != null) {
                "${student.grade} · ${student.subject} · Individual"
            } else {
                "Private Lesson"
            }
        }

    val isGroup: Boolean
        get() = group != null

    val phoneToContact: String?
        get() = student?.phone ?: student?.parentPhone

    val displayAreaOrLocation: String
        get() {
            if (lesson.locationType == "ONLINE") return "Online"
            if (lesson.areaName.isNotBlank()) return lesson.areaName
            if (student?.areaName?.isNotBlank() == true) return student.areaName
            if (group?.areaName?.isNotBlank() == true) return group.areaName
            return lesson.locationLabel.ifBlank { lesson.location }
        }

    val isOnline: Boolean
        get() = lesson.locationType == "ONLINE"

    val effectiveTravelTimeMinutes: Int?
        get() {
            if (isOnline) return null
            return lesson.travelTimeMinutes ?: student?.defaultTravelTimeMinutes ?: group?.defaultTravelTimeMinutes
        }
}

data class ConflictCheckResult(
    val hasConflict: Boolean = false,
    val isDirectOverlap: Boolean = false,
    val isTravelTimeWarning: Boolean = false,
    val warningMessage: String = "",
    val conflictingLesson: LessonWithDetails? = null
)

enum class LocationTypeEnum(val code: String, val label: String) {
    STUDENT_HOME("STUDENT_HOME", "Student Home"),
    TUTOR_LOCATION("TUTOR_LOCATION", "Tutor Location"),
    ONLINE("ONLINE", "Online"),
    CUSTOM("CUSTOM", "Custom Location")
}

data class StudentWithBalance(
    val student: StudentEntity,
    val completedLessonsCount: Int = 0,
    val scheduledLessonsCount: Int = 0,
    val totalBilled: Double = 0.0,
    val totalPaid: Double = 0.0,
    val balanceDue: Double = 0.0, // totalBilled - totalPaid
    val activePackage: PackageEntity? = null,
    val nextLesson: LessonEntity? = null,
    val groupNames: List<String> = emptyList()
)

data class GroupDetails(
    val group: GroupEntity,
    val members: List<GroupMemberWithStudent> = emptyList(),
    val nextLesson: LessonEntity? = null,
    val upcomingLessonsCount: Int = 0
)

data class PaymentSummary(
    val totalReceivedThisMonth: Double = 0.0,
    val totalDueAllStudents: Double = 0.0,
    val totalLessonsToday: Int = 0,
    val paymentsToday: Double = 0.0
)

enum class LessonStatusEnum(val label: String) {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    ABSENT("Absent"),
    CANCELLED_BY_STUDENT("Cancelled by Student"),
    CANCELLED_BY_TUTOR("Cancelled by Tutor"),
    NO_SHOW("No Show")
}

enum class PaymentMethodEnum(val label: String) {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    ONLINE("Online"),
    OTHER("Other")
}

enum class RecurrenceOption(val label: String) {
    NEVER("Never"),
    WEEKLY("Every week"),
    BIWEEKLY("Every two weeks"),
    CUSTOM("Custom")
}
