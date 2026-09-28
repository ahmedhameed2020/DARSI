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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.domain.model.ConflictCheckResult
import com.example.domain.model.LessonWithDetails
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddBottomSheet(
    initialTab: Int,
    students: List<StudentEntity>,
    groups: List<GroupEntity>,
    currency: String,
    initialStudentId: Long? = null,
    onCheckConflict: (suspend (Long, Long, Int?) -> ConflictCheckResult)? = null,
    onDismiss: () -> Unit,
    onBookLesson: (
        studentId: Long?,
        groupId: Long?,
        dateString: String,
        hour: Int,
        minute: Int,
        duration: Int,
        price: Double,
        location: String,
        recurrence: String,
        customDays: List<Int>,
        locationTypeOverride: String?,
        locationLabelOverride: String?,
        areaNameOverride: String?,
        addressTextOverride: String?,
        mapsLinkOverride: String?,
        travelTimeMinutesOverride: Int?,
        meetingUrlOverride: String?
    ) -> Unit,
    onAddStudent: (StudentEntity) -> Unit,
    onAddGroup: (GroupEntity, List<Long>) -> Unit,
    onRecordPayment: (PaymentEntity) -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("Book Lesson", "Add Student", "Add Group", "Record Payment")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
                .testTag("quick_add_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Add",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = DarsiNavyMuted)
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) DarsiRoyalBlue else DarsiNavyMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // 1. BOOK LESSON
                    BookLessonForm(
                        students = students,
                        groups = groups,
                        currency = currency,
                        initialStudentId = initialStudentId,
                        onCheckConflict = onCheckConflict,
                        onSubmit = { sId, gId, dateStr, hr, min, dur, pr, loc, rec, days, locType, locLbl, area, addr, maps, travel, meet ->
                            onBookLesson(sId, gId, dateStr, hr, min, dur, pr, loc, rec, days, locType, locLbl, area, addr, maps, travel, meet)
                            onDismiss()
                        }
                    )
                }
                1 -> {
                    // 2. ADD STUDENT
                    AddStudentForm(
                        currency = currency,
                        onSubmit = { student ->
                            onAddStudent(student)
                            onDismiss()
                        }
                    )
                }
                2 -> {
                    // 3. ADD GROUP
                    AddGroupForm(
                        students = students,
                        currency = currency,
                        onSubmit = { grp, memberIds ->
                            onAddGroup(grp, memberIds)
                            onDismiss()
                        }
                    )
                }
                3 -> {
                    // 4. RECORD PAYMENT
                    RecordPaymentForm(
                        students = students,
                        currency = currency,
                        initialStudentId = initialStudentId,
                        onSubmit = { payment ->
                            onRecordPayment(payment)
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookLessonForm(
    students: List<StudentEntity>,
    groups: List<GroupEntity>,
    currency: String,
    initialStudentId: Long? = null,
    onCheckConflict: (suspend (Long, Long, Int?) -> ConflictCheckResult)? = null,
    onSubmit: (
        studentId: Long?,
        groupId: Long?,
        dateString: String,
        hour: Int,
        minute: Int,
        duration: Int,
        price: Double,
        location: String,
        recurrence: String,
        customDays: List<Int>,
        locationTypeOverride: String?,
        locationLabelOverride: String?,
        areaNameOverride: String?,
        addressTextOverride: String?,
        mapsLinkOverride: String?,
        travelTimeMinutesOverride: Int?,
        meetingUrlOverride: String?
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var conflictResult by remember { mutableStateOf<ConflictCheckResult?>(null) }
    var showConflictDialog by remember { mutableStateOf(false) }

    var isGroupSelected by remember { mutableStateOf(false) }
    var selectedStudentId by remember { mutableStateOf<Long?>(initialStudentId ?: students.firstOrNull()?.id) }
    var selectedGroupId by remember { mutableStateOf<Long?>(groups.firstOrNull()?.id) }
    var lessonDate by remember { mutableStateOf(DateTimeUtils.formatTodayDateString()) }
    var hour by remember { mutableIntStateOf(16) }
    var minute by remember { mutableIntStateOf(30) }
    var duration by remember { mutableIntStateOf(60) }
    var recurrenceOption by remember { mutableStateOf("NEVER") } // NEVER, WEEKLY, BIWEEKLY, CUSTOM
    val customDays = remember { mutableStateListOf<Int>() } // 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat

    var locationType by remember { mutableStateOf("STUDENT_HOME") }
    var locationLabel by remember { mutableStateOf("Student Home") }
    var areaName by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }
    var travelTimeStr by remember { mutableStateOf("") }
    var meetingUrl by remember { mutableStateOf("") }

    // Inherit defaults when student or group changes
    LaunchedEffect(selectedStudentId, isGroupSelected) {
        if (!isGroupSelected) {
            val student = students.firstOrNull { it.id == selectedStudentId }
            if (student != null) {
                locationType = student.defaultLessonLocationType
                locationLabel = student.locationLabel
                areaName = student.areaName
                addressText = student.addressText
                travelTimeStr = student.defaultTravelTimeMinutes?.toString() ?: ""
            }
        }
    }

    LaunchedEffect(selectedGroupId, isGroupSelected) {
        if (isGroupSelected) {
            val group = groups.firstOrNull { it.id == selectedGroupId }
            if (group != null) {
                locationType = group.defaultLocationType
                locationLabel = group.defaultLocationLabel
                areaName = group.areaName
                addressText = group.addressText
                travelTimeStr = group.defaultTravelTimeMinutes?.toString() ?: ""
            }
        }
    }

    fun submitBooking() {
        val sId = if (!isGroupSelected) selectedStudentId else null
        val gId = if (isGroupSelected) selectedGroupId else null
        val effectiveTravel = if (locationType == "ONLINE") null else travelTimeStr.toIntOrNull()
        val locDisplay = if (locationType == "ONLINE") "Online" else areaName.ifBlank { locationLabel }
        onSubmit(
            sId,
            gId,
            lessonDate,
            hour,
            minute,
            duration,
            250.0,
            locDisplay,
            recurrenceOption,
            customDays.toList(),
            locationType,
            locationLabel,
            areaName,
            addressText,
            null,
            effectiveTravel,
            if (locationType == "ONLINE") meetingUrl else null
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Mode toggle: Student vs Group
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { isGroupSelected = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (!isGroupSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                    contentColor = if (!isGroupSelected) Color.White else DarsiNavy
                )
            ) {
                Text("Individual Student", fontSize = 12.sp)
            }
            FilledTonalButton(
                onClick = { isGroupSelected = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isGroupSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                    contentColor = if (isGroupSelected) Color.White else DarsiNavy
                )
            ) {
                Text("Student Group", fontSize = 12.sp)
            }
        }

        // Selection Target
        if (!isGroupSelected) {
            Text("Select Student:", fontSize = 12.sp, color = DarsiNavyMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                students.forEach { s ->
                    val isSel = (selectedStudentId == s.id)
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedStudentId = s.id },
                        label = { Text(s.name, fontSize = 12.sp) }
                    )
                }
            }
        } else {
            Text("Select Group:", fontSize = 12.sp, color = DarsiNavyMuted)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                groups.forEach { g ->
                    val isSel = (selectedGroupId == g.id)
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedGroupId = g.id },
                        label = { Text(g.name, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Date & Time
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = lessonDate,
                onValueChange = { lessonDate = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1.3f)
            )
            OutlinedTextField(
                value = String.format("%02d:%02d", hour, minute),
                onValueChange = {
                    val parts = it.split(":")
                    if (parts.size == 2) {
                        hour = parts[0].toIntOrNull() ?: hour
                        minute = parts[1].toIntOrNull() ?: minute
                    }
                },
                label = { Text("Time (24h)") },
                modifier = Modifier.weight(1f)
            )
        }

        // Duration selector
        Text("Duration:", fontSize = 12.sp, color = DarsiNavyMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(45, 60, 90, 120).forEach { mins ->
                val isSel = (duration == mins)
                FilledTonalButton(
                    onClick = { duration = mins },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSel) DarsiRoyalBlue else DarsiSurfaceVariant,
                        contentColor = if (isSel) Color.White else DarsiNavy
                    )
                ) {
                    Text("${mins}m", fontSize = 12.sp)
                }
            }
        }

        // Location Type Selector
        Text("Lesson Location Type:", fontSize = 12.sp, color = DarsiNavyMuted)
        val locationTypes = listOf(
            "STUDENT_HOME" to "Student Home",
            "TUTOR_LOCATION" to "Tutor Location",
            "ONLINE" to "Online",
            "CUSTOM" to "Custom"
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            locationTypes.forEach { (typeCode, label) ->
                val isSel = (locationType == typeCode)
                FilterChip(
                    selected = isSel,
                    onClick = {
                        locationType = typeCode
                        if (typeCode == "ONLINE") {
                            locationLabel = "Online"
                        } else if (typeCode == "STUDENT_HOME") {
                            locationLabel = "Student Home"
                        } else if (typeCode == "TUTOR_LOCATION") {
                            locationLabel = "Tutor Location"
                        }
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        if (locationType == "ONLINE") {
            OutlinedTextField(
                value = meetingUrl,
                onValueChange = { meetingUrl = it },
                label = { Text("Meeting Link (e.g. Google Meet URL)") },
                placeholder = { Text("https://meet.google.com/...") },
                modifier = Modifier.fillMaxWidth().testTag("lesson_meeting_url_input")
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = areaName,
                    onValueChange = { areaName = it },
                    label = { Text("Area / District") },
                    placeholder = { Text("e.g. Al Waab") },
                    modifier = Modifier.weight(1.2f).testTag("lesson_area_input")
                )
                OutlinedTextField(
                    value = travelTimeStr,
                    onValueChange = { travelTimeStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Travel (min)") },
                    placeholder = { Text("25") },
                    modifier = Modifier.weight(0.8f).testTag("lesson_travel_time_input")
                )
            }

            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Address / Directions (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Recurring Lesson Options
        Text("Recurring Schedule:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarsiNavy)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("NEVER" to "Never", "WEEKLY" to "Every week", "BIWEEKLY" to "Every 2 wks", "CUSTOM" to "Custom").forEach { (code, lbl) ->
                val isSel = (recurrenceOption == code)
                FilterChip(
                    selected = isSel,
                    onClick = { recurrenceOption = code },
                    label = { Text(lbl, fontSize = 11.sp) }
                )
            }
        }

        if (recurrenceOption == "CUSTOM") {
            Text("Select weekdays (e.g. Sunday + Tuesday):", fontSize = 11.sp, color = DarsiNavyMuted)
            val dayNames = listOf("Sun" to 1, "Mon" to 2, "Tue" to 3, "Wed" to 4, "Thu" to 5, "Fri" to 6, "Sat" to 7)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dayNames.forEach { (name, dayNum) ->
                    val isChecked = customDays.contains(dayNum)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            if (isChecked) customDays.remove(dayNum) else customDays.add(dayNum)
                        },
                        label = { Text(name, fontSize = 11.sp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsed = try { sdf.parse(lessonDate) ?: Date() } catch (e: Exception) { Date() }
                val cal = Calendar.getInstance().apply {
                    time = parsed
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = cal.timeInMillis
                val end = start + (duration * 60 * 1000L)
                val effectiveTravel = if (locationType == "ONLINE") null else travelTimeStr.toIntOrNull()

                if (onCheckConflict != null) {
                    coroutineScope.launch {
                        val conflict = onCheckConflict(start, end, effectiveTravel)
                        if (conflict.hasConflict) {
                            conflictResult = conflict
                            showConflictDialog = true
                        } else {
                            submitBooking()
                        }
                    }
                } else {
                    submitBooking()
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_book_lesson_btn")
        ) {
            Text("Confirm Booking", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        if (showConflictDialog && conflictResult != null) {
            val c = conflictResult!!
            val dialogTitle = if (c.isDirectOverlap) "⚠️ Schedule Overlap Detected" else "⚠️ Travel Time Warning"
            AlertDialog(
                onDismissRequest = { showConflictDialog = false },
                title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "${c.warningMessage}\n\nWould you like to go back and adjust the time, or save anyway?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConflictDialog = false
                            submitBooking()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                    ) {
                        Text("Save Anyway")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConflictDialog = false }) {
                        Text("Go Back")
                    }
                }
            )
        }
    }
}

@Composable
private fun AddStudentForm(
    currency: String,
    onSubmit: (StudentEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+974") }
    var parentPhone by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Grade 12") }
    var subject by remember { mutableStateOf("English") }
    var defaultPrice by remember { mutableDoubleStateOf(250.0) }
    var duration by remember { mutableIntStateOf(60) }
    var paymentType by remember { mutableStateOf("PER_LESSON") } // PER_LESSON, MONTHLY, PACKAGE
    var notes by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Student Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_student_name_input")
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone (WhatsApp)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = parentPhone,
                onValueChange = { parentPhone = it },
                label = { Text("Parent Phone (Opt)") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text("Grade") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.weight(1.2f)
            )
        }

        OutlinedTextField(
            value = school,
            onValueChange = { school = it },
            label = { Text("School Name (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = defaultPrice.toString(),
                onValueChange = { defaultPrice = it.toDoubleOrNull() ?: defaultPrice },
                label = { Text("Default Price ($currency)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { duration = it.toIntOrNull() ?: duration },
                label = { Text("Duration (min)") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("Payment Arrangement:", fontSize = 12.sp, color = DarsiNavyMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("PER_LESSON" to "Per Lesson", "MONTHLY" to "Monthly", "PACKAGE" to "Package").forEach { (code, lbl) ->
                FilterChip(
                    selected = (paymentType == code),
                    onClick = { paymentType = code },
                    label = { Text(lbl, fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Private Notes (Optional)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    val s = StudentEntity(
                        name = name.trim(),
                        phone = phone.trim(),
                        parentPhone = parentPhone.trim().ifEmpty { null },
                        school = school.trim().ifEmpty { null },
                        grade = grade.trim(),
                        subject = subject.trim(),
                        defaultPrice = defaultPrice,
                        defaultDurationMinutes = duration,
                        paymentType = paymentType,
                        privateNotes = notes.trim()
                    )
                    onSubmit(s)
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_add_student_btn")
        ) {
            Text("Save Student", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddGroupForm(
    students: List<StudentEntity>,
    currency: String,
    onSubmit: (GroupEntity, List<Long>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("English") }
    var grade by remember { mutableStateOf("Grade 12") }
    var defaultPrice by remember { mutableDoubleStateOf(180.0) }
    var duration by remember { mutableIntStateOf(90) }
    var notes by remember { mutableStateOf("") }
    val selectedMemberIds = remember { mutableStateListOf<Long>() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Group Name (e.g. Grade 12 Group A)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text("Grade") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.weight(1.2f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = defaultPrice.toString(),
                onValueChange = { defaultPrice = it.toDoubleOrNull() ?: defaultPrice },
                label = { Text("Price/Student ($currency)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { duration = it.toIntOrNull() ?: duration },
                label = { Text("Duration (min)") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("Select initial students for group:", fontSize = 12.sp, color = DarsiNavyMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            students.forEach { s ->
                val isChecked = selectedMemberIds.contains(s.id)
                FilterChip(
                    selected = isChecked,
                    onClick = {
                        if (isChecked) selectedMemberIds.remove(s.id) else selectedMemberIds.add(s.id)
                    },
                    label = { Text(s.name, fontSize = 11.sp) }
                )
            }
        }

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    val g = GroupEntity(
                        name = name.trim(),
                        subject = subject.trim(),
                        grade = grade.trim(),
                        defaultPrice = defaultPrice,
                        defaultDurationMinutes = duration,
                        notes = notes.trim()
                    )
                    onSubmit(g, selectedMemberIds.toList())
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Create Group", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordPaymentForm(
    students: List<StudentEntity>,
    currency: String,
    initialStudentId: Long? = null,
    onSubmit: (PaymentEntity) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf<Long?>(initialStudentId ?: students.firstOrNull()?.id) }
    var amount by remember { mutableDoubleStateOf(250.0) }
    var method by remember { mutableStateOf("CASH") } // CASH, BANK_TRANSFER, ONLINE, OTHER
    var purpose by remember { mutableStateOf("LESSON") } // LESSON, PACKAGE, MONTHLY, OTHER
    var note by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Select Student:", fontSize = 12.sp, color = DarsiNavyMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            students.forEach { s ->
                FilterChip(
                    selected = (selectedStudentId == s.id),
                    onClick = { selectedStudentId = s.id },
                    label = { Text(s.name, fontSize = 12.sp) }
                )
            }
        }

        OutlinedTextField(
            value = amount.toString(),
            onValueChange = { amount = it.toDoubleOrNull() ?: amount },
            label = { Text("Payment Amount ($currency)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Payment Method:", fontSize = 12.sp, color = DarsiNavyMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("CASH" to "Cash", "BANK_TRANSFER" to "Bank Transfer", "ONLINE" to "Online", "OTHER" to "Other").forEach { (code, lbl) ->
                FilterChip(
                    selected = (method == code),
                    onClick = { method = code },
                    label = { Text(lbl, fontSize = 11.sp) }
                )
            }
        }

        Text("Payment For:", fontSize = 12.sp, color = DarsiNavyMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("LESSON" to "Lesson", "PACKAGE" to "Package", "MONTHLY" to "Monthly").forEach { (code, lbl) ->
                FilterChip(
                    selected = (purpose == code),
                    onClick = { purpose = code },
                    label = { Text(lbl, fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Payment Note (e.g. CBQ transfer ref #1234)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val sId = selectedStudentId
                if (sId != null && amount > 0) {
                    val p = PaymentEntity(
                        studentId = sId,
                        amount = amount,
                        currency = currency,
                        date = System.currentTimeMillis(),
                        paymentMethod = method,
                        paymentFor = purpose,
                        note = note.trim()
                    )
                    onSubmit(p)
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_record_payment_btn")
        ) {
            Text("Record Payment", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
