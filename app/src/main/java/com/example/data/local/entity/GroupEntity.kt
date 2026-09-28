package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val subject: String,
    val grade: String,
    val defaultPrice: Double = 0.0,
    val defaultDurationMinutes: Int = 90,
    val notes: String = "",
    val status: String = "ACTIVE",
    val defaultLocationType: String = "TUTOR_LOCATION", // STUDENT_HOME, TUTOR_LOCATION, ONLINE, CUSTOM
    val defaultLocationLabel: String = "Tutor Location",
    val areaName: String = "",
    val addressText: String = "",
    val mapsLink: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val defaultTravelTimeMinutes: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_members",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("groupId"),
        Index("studentId"),
        Index(value = ["groupId", "studentId"], unique = true)
    ]
)
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val studentId: Long,
    val priceOverride: Double? = null // Individual price override per student in group
)
