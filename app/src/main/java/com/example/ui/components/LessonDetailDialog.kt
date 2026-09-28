package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.outlined.Chat
import com.example.util.MapsAndLocationHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LessonWithDetails
import com.example.ui.theme.DarsiAmber
import com.example.ui.theme.DarsiAmberBg
import com.example.ui.theme.DarsiBorder
import com.example.ui.theme.DarsiCoralRed
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.DateTimeUtils
import com.example.util.GoogleCalendarHelper
import com.example.util.WhatsAppHelper
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonDetailDialog(
    lessonDetails: LessonWithDetails,
    currency: String,
    language: String = "en",
    onDismiss: () -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onUpdateNotes: (Long, String, String, String) -> Unit,
    onRescheduleLesson: (lessonId: Long, newDate: String, hour: Int, minute: Int, duration: Int, applyToOption: Int) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteLesson: (Long, Int) -> Unit, // 0=This only, 1=Future series, 2=All series
    onOpenPaymentForStudent: (Long) -> Unit
) {
    val context = LocalContext.current
    val isArabic = language == "ar"
    val lesson = lessonDetails.lesson

    var selectedStatus by remember(lesson.status) { mutableStateOf(lesson.status) }
    var topicCovered by remember(lesson.topicCovered) { mutableStateOf(lesson.topicCovered) }
    var homework by remember(lesson.homework) { mutableStateOf(lesson.homework) }
    var privateNote by remember(lesson.privateTutorNote) { mutableStateOf(lesson.privateTutorNote) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("lesson_detail_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = lessonDetails.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        if (lesson.seriesId != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = DarsiRoyalBlueSubtle,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = if (isArabic) "متكررة" else "Recurring",
                                        tint = DarsiRoyalBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (isArabic) {
                                            if (lesson.isException) "استثناء" else "متكررة"
                                        } else {
                                            if (lesson.isException) "Exception" else "Recurring"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarsiRoyalBlue
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = lessonDetails.subtitle,
                        fontSize = 12.sp,
                        color = DarsiNavyMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = if (isArabic) "إغلاق" else "Close", tint = DarsiNavyMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Time & Location Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarsiRoyalBlueSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = DarsiRoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${DateTimeUtils.formatReadableDate(lesson.startEpochMillis)} · ${DateTimeUtils.formatTime(lesson.startEpochMillis)} – ${DateTimeUtils.formatTime(lesson.endEpochMillis)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavy
                                )
                            }
                            TextButton(
                                onClick = { showRescheduleDialog = true },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(if (isArabic) "إعادة الجدولة" else "Reschedule", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarsiRoyalBlue)
                            }
                        }
                        val isOnline = lesson.locationType == "ONLINE"
                        val displayLoc = when {
                            isOnline -> if (isArabic) "أونلاين" else "Online"
                            lesson.areaName.isNotBlank() -> lesson.areaName
                            lesson.locationLabel.isNotBlank() -> lesson.locationLabel
                            else -> lesson.location
                        }
                        if (displayLoc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val addrDetail = if (lesson.addressText.isNotBlank() && !isOnline) " · ${lesson.addressText}" else ""
                            Text(
                                text = "📍 $displayLoc$addrDetail",
                                fontSize = 12.sp,
                                color = DarsiNavyMuted
                            )
                        }

                        if (isOnline && lesson.meetingUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = { MapsAndLocationHelper.openMeetingUrl(context, lesson.meetingUrl) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("lesson_detail_join_meeting_btn")
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "دخول الاجتماع" else "Join Online Meeting", fontSize = 12.sp)
                            }
                        } else if (!isOnline && (lesson.areaName.isNotBlank() || lesson.addressText.isNotBlank() || lesson.latitude != null || lesson.mapsLink.isNotBlank())) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        MapsAndLocationHelper.openInMaps(
                                            context = context,
                                            latitude = lesson.latitude,
                                            longitude = lesson.longitude,
                                            addressText = lesson.addressText,
                                            areaName = lesson.areaName,
                                            mapsLink = lesson.mapsLink,
                                            label = lessonDetails.title
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("lesson_detail_open_maps_btn")
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isArabic) "فتح الخريطة" else "Open Maps", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        MapsAndLocationHelper.navigate(
                                            context = context,
                                            latitude = lesson.latitude,
                                            longitude = lesson.longitude,
                                            addressText = lesson.addressText,
                                            areaName = lesson.areaName,
                                            label = lessonDetails.title
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("lesson_detail_navigate_btn")
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Navigate", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Attendance Status Options
                Text(
                    text = "ATTENDANCE / STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = DarsiNavySubtle
                )

                val statuses = listOf(
                    "SCHEDULED" to "Scheduled",
                    "COMPLETED" to "Completed",
                    "ABSENT" to "Absent",
                    "CANCELLED_BY_STUDENT" to "Cancelled (Student)",
                    "CANCELLED_BY_TUTOR" to "Cancelled (Tutor)",
                    "NO_SHOW" to "No Show"
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    statuses.forEach { (code, label) ->
                        val isSelected = (selectedStatus == code)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedStatus = code
                                onUpdateStatus(lesson.id, code)
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Lesson Notes: Topic, Homework, Private Note
                Text(
                    text = "LESSON NOTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = DarsiNavySubtle
                )

                OutlinedTextField(
                    value = topicCovered,
                    onValueChange = {
                        topicCovered = it
                        onUpdateNotes(lesson.id, topicCovered, homework, privateNote)
                    },
                    label = { Text("Topic Covered (e.g. Present Perfect)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = homework,
                    onValueChange = {
                        homework = it
                        onUpdateNotes(lesson.id, topicCovered, homework, privateNote)
                    },
                    label = { Text("Homework Assigned (e.g. Pages 34–35)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = privateNote,
                    onValueChange = {
                        privateNote = it
                        onUpdateNotes(lesson.id, topicCovered, homework, privateNote)
                    },
                    label = { Text("Private Tutor Note (Confidential)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: WhatsApp Reminder & Google Calendar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val phone = lessonDetails.phoneToContact
                    if (!phone.isNullOrEmpty()) {
                        FilledTonalButton(
                            onClick = {
                                val msg = WhatsAppHelper.createLessonReminderMessage(
                                    studentName = lessonDetails.student?.name ?: lessonDetails.title,
                                    subject = lessonDetails.student?.subject ?: "lesson",
                                    timeString = DateTimeUtils.formatTime(lesson.startEpochMillis),
                                    location = lesson.location
                                )
                                WhatsAppHelper.openChat(context, phone, msg)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = DarsiSuccessGreenBg,
                                contentColor = DarsiSuccessGreenDark
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "واتساب" else "WhatsApp", fontSize = 11.sp)
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            GoogleCalendarHelper.exportLessonToCalendar(
                                context = context,
                                title = lessonDetails.title,
                                description = "Topic: $topicCovered\nHomework: $homework",
                                location = lesson.location,
                                startEpochMillis = lesson.startEpochMillis,
                                endEpochMillis = lesson.endEpochMillis
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to G-Cal", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
            ) {
                Text("Done", fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.textButtonColors(contentColor = DarsiCoralRed)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Lesson", fontSize = 12.sp)
            }
        }
    )

    // Reschedule Dialog
    if (showRescheduleDialog) {
        val cal = Calendar.getInstance().apply { timeInMillis = lesson.startEpochMillis }
        var reschedDate by remember { mutableStateOf(lesson.lessonDate) }
        var reschedHour by remember { mutableIntStateOf(cal.get(Calendar.HOUR_OF_DAY)) }
        var reschedMinute by remember { mutableIntStateOf(cal.get(Calendar.MINUTE)) }
        var reschedDuration by remember { mutableIntStateOf(lesson.durationMinutes) }
        var applyOption by remember { mutableIntStateOf(0) } // 0=This only, 1=This and following

        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Reschedule Lesson") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reschedDate,
                        onValueChange = { reschedDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = String.format("%02d:%02d", reschedHour, reschedMinute),
                        onValueChange = {
                            val parts = it.split(":")
                            if (parts.size == 2) {
                                reschedHour = parts[0].toIntOrNull() ?: reschedHour
                                reschedMinute = parts[1].toIntOrNull() ?: reschedMinute
                            }
                        },
                        label = { Text("Start Time (24h)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Duration:", fontSize = 12.sp, color = DarsiNavyMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(45, 60, 90, 120).forEach { mins ->
                            val isSel = (reschedDuration == mins)
                            FilterChip(
                                selected = isSel,
                                onClick = { reschedDuration = mins },
                                label = { Text("${mins}m") }
                            )
                        }
                    }

                    if (lesson.seriesId != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Apply to:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarsiNavy)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = applyOption == 0,
                                onClick = { applyOption = 0 },
                                label = { Text("This occurrence only") }
                            )
                            FilterChip(
                                selected = applyOption == 1,
                                onClick = { applyOption = 1 },
                                label = { Text("This & future lessons") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRescheduleLesson(lesson.id, reschedDate, reschedHour, reschedMinute, reschedDuration, applyOption)
                        showRescheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                ) {
                    Text("Save New Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Lesson?") },
            text = {
                Text(
                    if (lesson.seriesId != null) {
                        "This lesson is part of a recurring schedule. Choose which occurrences to delete:"
                    } else {
                        "Are you sure you want to delete this lesson? This cannot be undone."
                    }
                )
            },
            confirmButton = {
                if (lesson.seriesId != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                onDeleteLesson(lesson.id, 0)
                                showDeleteConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarsiCoralRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete This Occurrence Only")
                        }
                        Button(
                            onClick = {
                                onDeleteLesson(lesson.id, 1)
                                showDeleteConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarsiCoralRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete This & Following Occurrences")
                        }
                        OutlinedButton(
                            onClick = {
                                onDeleteLesson(lesson.id, 2)
                                showDeleteConfirm = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete All in Series", color = DarsiCoralRed)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onDeleteLesson(lesson.id, 0)
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarsiCoralRed)
                    ) {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
