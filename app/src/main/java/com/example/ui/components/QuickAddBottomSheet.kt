package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.domain.model.ConflictCheckResult
import com.example.ui.theme.DarsiAmber
import com.example.ui.theme.DarsiAmberBg
import com.example.ui.theme.DarsiAmberDark
import com.example.ui.theme.DarsiBorder
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
    language: String = "en",
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
    val isArabic = language == "ar"
    // selectedTab: -1 = Action Sheet (3 choices), 0 = Book Lesson, 1 = Add Student, 2 = Add Group, 3 = Record Payment
    var selectedTab by remember { mutableIntStateOf(initialTab) }

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
            if (selectedTab == -1) {
                // ACTION SHEET: 3 CHOICES ONLY
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "إضافة سريعة" else "Quick Add",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarsiNavy
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = DarsiNavyMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Choice 1: Book Lesson
                QuickActionRow(
                    title = if (isArabic) "إضافة حصة" else "Book Lesson",
                    subtitle = if (isArabic) "جدولة حصة جديدة لطالب أو مجموعة" else "Schedule a lesson with student or group",
                    icon = Icons.Default.CalendarMonth,
                    iconBgColor = DarsiRoyalBlueSubtle,
                    iconTintColor = DarsiRoyalBlue,
                    testTag = "quick_add_choice_lesson",
                    onClick = { selectedTab = 0 }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Choice 2: Add Student
                QuickActionRow(
                    title = if (isArabic) "إضافة طالب" else "Add Student",
                    subtitle = if (isArabic) "تسجيل ملف طالب جديد" else "Register a new student profile",
                    icon = Icons.Default.Person,
                    iconBgColor = DarsiSuccessGreenBg,
                    iconTintColor = DarsiSuccessGreenDark,
                    testTag = "quick_add_choice_student",
                    onClick = { selectedTab = 1 }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Choice 3: Record Payment
                QuickActionRow(
                    title = if (isArabic) "تسجيل دفعة" else "Record Payment",
                    subtitle = if (isArabic) "تسجيل مبلغ مدفوع لحصة أو باقة" else "Log incoming lesson fee payment",
                    icon = Icons.Default.Payments,
                    iconBgColor = DarsiAmberBg,
                    iconTintColor = DarsiAmberDark,
                    testTag = "quick_add_choice_payment",
                    onClick = { selectedTab = 3 }
                )

                Spacer(modifier = Modifier.height(28.dp))
            } else {
                // FORM HEADER (With Back arrow if launched from Action Sheet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (initialTab == -1) {
                            IconButton(onClick = { selectedTab = -1 }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isArabic) "رجوع" else "Back",
                                    tint = DarsiNavy
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = when (selectedTab) {
                                0 -> if (isArabic) "إضافة حصة" else "Book Lesson"
                                1 -> if (isArabic) "إضافة طالب" else "Add Student"
                                2 -> if (isArabic) "إضافة مجموعة" else "Add Group"
                                3 -> if (isArabic) "تسجيل دفعة" else "Record Payment"
                                else -> ""
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isArabic) "إغلاق" else "Close",
                            tint = DarsiNavyMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        BookLessonForm(
                            students = students,
                            groups = groups,
                            currency = currency,
                            initialStudentId = initialStudentId,
                            language = language,
                            onCheckConflict = onCheckConflict,
                            onSubmit = { sId, gId, dateStr, hr, min, dur, pr, loc, rec, days, locType, locLbl, area, addr, maps, travel, meet ->
                                onBookLesson(sId, gId, dateStr, hr, min, dur, pr, loc, rec, days, locType, locLbl, area, addr, maps, travel, meet)
                                onDismiss()
                            }
                        )
                    }
                    1 -> {
                        AddStudentForm(
                            currency = currency,
                            language = language,
                            onSubmit = { student ->
                                onAddStudent(student)
                                onDismiss()
                            }
                        )
                    }
                    2 -> {
                        AddGroupForm(
                            students = students,
                            currency = currency,
                            language = language,
                            onSubmit = { grp, memberIds ->
                                onAddGroup(grp, memberIds)
                                onDismiss()
                            }
                        )
                    }
                    3 -> {
                        RecordPaymentForm(
                            students = students,
                            currency = currency,
                            initialStudentId = initialStudentId,
                            language = language,
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
}

@Composable
private fun QuickActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarsiBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconBgColor,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
            }
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
    language: String = "en",
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

    val isArabic = language == "ar"
    var isGroupSelected by remember { mutableStateOf(false) }
    var selectedStudentId by remember { mutableStateOf<Long?>(initialStudentId ?: students.firstOrNull()?.id) }
    var selectedGroupId by remember { mutableStateOf<Long?>(groups.firstOrNull()?.id) }

    val now = remember { Calendar.getInstance() }
    val defaultHour = if (now.get(Calendar.MINUTE) > 30) (now.get(Calendar.HOUR_OF_DAY) + 1) % 24 else now.get(Calendar.HOUR_OF_DAY)
    var lessonDate by remember { mutableStateOf(DateTimeUtils.formatTodayDateString()) }
    var hour by remember { mutableIntStateOf(defaultHour) }
    var minute by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(60) }
    var recurrenceOption by remember { mutableStateOf("NEVER") } // NEVER, WEEKLY
    val customDays = remember { mutableStateListOf<Int>() }

    var locationType by remember { mutableStateOf("STUDENT_HOME") } // STUDENT_HOME, ONLINE, TUTOR_LOCATION, OTHER
    var locationLabel by remember { mutableStateOf(if (language == "ar") "منزل الطالب" else "Student Home") }
    var areaName by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }
    var travelTimeStr by remember { mutableStateOf("") }
    var meetingUrl by remember { mutableStateOf("") }
    var priceOverride by remember { mutableStateOf("") }
    var showMoreOptions by remember { mutableStateOf(false) }

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
                priceOverride = if (student.defaultPrice > 0) student.defaultPrice.toInt().toString() else ""
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
                priceOverride = if (group.defaultPrice > 0) group.defaultPrice.toInt().toString() else ""
            }
        }
    }

    fun submitBooking() {
        val sId = if (!isGroupSelected) selectedStudentId else null
        val gId = if (isGroupSelected) selectedGroupId else null
        val effectiveTravel = if (locationType == "ONLINE") null else travelTimeStr.toIntOrNull()
        val locDisplay = if (locationType == "ONLINE") "Online" else areaName.ifBlank { locationLabel }
        val price = priceOverride.toDoubleOrNull() ?: 200.0
        onSubmit(
            sId,
            gId,
            lessonDate,
            hour,
            minute,
            duration,
            price,
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

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Student / Group Selector
        if (groups.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { isGroupSelected = false },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (!isGroupSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                        contentColor = if (!isGroupSelected) Color.White else DarsiNavy
                    )
                ) {
                    Text(if (isArabic) "طالب" else "Student", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                FilledTonalButton(
                    onClick = { isGroupSelected = true },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isGroupSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                        contentColor = if (isGroupSelected) Color.White else DarsiNavy
                    )
                ) {
                    Text(if (isArabic) "مجموعة" else "Group", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (!isGroupSelected) {
            Column {
                Text(
                    text = if (isArabic) "الطالب:" else "Student:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (students.isEmpty()) {
                    Text(
                        text = if (isArabic) "لا يوجد طلاب مضافون بعد." else "No students added yet.",
                        fontSize = 13.sp,
                        color = DarsiNavySubtle
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        students.forEach { s ->
                            val isSel = (selectedStudentId == s.id)
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedStudentId = s.id },
                                label = { Text(s.name, fontSize = 13.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }
            }
        } else {
            Column {
                Text(
                    text = if (isArabic) "المجموعة:" else "Group:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groups.forEach { g ->
                        val isSel = (selectedGroupId == g.id)
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedGroupId = g.id },
                            label = { Text(g.name, fontSize = 13.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        // 2. Date & Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = lessonDate,
                onValueChange = { lessonDate = it },
                label = { Text(if (isArabic) "التاريخ (YYYY-MM-DD)" else "Date") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.3f)
            )
            OutlinedTextField(
                value = String.format(Locale.US, "%02d:%02d", hour, minute),
                onValueChange = {
                    val parts = it.split(":")
                    if (parts.size == 2) {
                        hour = parts[0].toIntOrNull() ?: hour
                        minute = parts[1].toIntOrNull() ?: minute
                    }
                },
                label = { Text(if (isArabic) "الوقت (24h)" else "Time") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Duration: 60 / 90 / 120 min chips
        Column {
            Text(
                text = if (isArabic) "المدة:" else "Duration:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiNavyMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(60, 90, 120).forEach { mins ->
                    val isSel = (duration == mins)
                    FilledTonalButton(
                        onClick = { duration = mins },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSel) DarsiRoyalBlue else DarsiSurfaceVariant,
                            contentColor = if (isSel) Color.White else DarsiNavy
                        )
                    ) {
                        Text(
                            if (isArabic) "$mins د" else "${mins}m",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Location: Student Home / Online / My Place / Other
        Column {
            Text(
                text = if (isArabic) "المكان:" else "Location:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiNavyMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            val locationOptions = listOf(
                "STUDENT_HOME" to (if (isArabic) "منزل الطالب" else "Student Home"),
                "ONLINE" to (if (isArabic) "أونلاين" else "Online"),
                "TUTOR_LOCATION" to (if (isArabic) "مقر المعلم" else "My Place"),
                "OTHER" to (if (isArabic) "آخر" else "Other")
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                locationOptions.forEach { (typeCode, label) ->
                    val isSel = (locationType == typeCode)
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            locationType = typeCode
                            locationLabel = label
                        },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Auto-shown meeting link if "Online" is chosen
            if (locationType == "ONLINE") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = meetingUrl,
                    onValueChange = { meetingUrl = it },
                    label = { Text(if (isArabic) "رابط اللقاء (Google Meet / Zoom)" else "Meeting Link (Google Meet / Zoom)") },
                    placeholder = { Text("https://meet.google.com/...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("lesson_meeting_url_input")
                )
            }
        }

        // 5. Repeat: None / Weekly
        Column {
            Text(
                text = if (isArabic) "التكرار:" else "Repeat:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiNavyMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "NEVER" to (if (isArabic) "بدون تكرار" else "None"),
                    "WEEKLY" to (if (isArabic) "أسبوعياً" else "Weekly")
                ).forEach { (code, lbl) ->
                    val isSel = (recurrenceOption == code)
                    FilterChip(
                        selected = isSel,
                        onClick = { recurrenceOption = code },
                        label = { Text(lbl, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // Collapsible: More options / خيارات إضافية
        TextButton(
            onClick = { showMoreOptions = !showMoreOptions },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (showMoreOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = DarsiNavyMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "خيارات إضافية" else "More options",
                    fontSize = 13.sp,
                    color = DarsiNavyMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showMoreOptions) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (locationType != "ONLINE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = areaName,
                            onValueChange = { areaName = it },
                            label = { Text(if (isArabic) "المنطقة / الحي" else "Area / District") },
                            placeholder = { Text(if (isArabic) "مثال: الوعب" else "e.g. Al Waab") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f).testTag("lesson_area_input")
                        )
                        OutlinedTextField(
                            value = travelTimeStr,
                            onValueChange = { travelTimeStr = it.filter { ch -> ch.isDigit() } },
                            label = { Text(if (isArabic) "التنقل (د)" else "Travel (min)") },
                            placeholder = { Text("20") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f).testTag("lesson_travel_time_input")
                        )
                    }

                    OutlinedTextField(
                        value = addressText,
                        onValueChange = { addressText = it },
                        label = { Text(if (isArabic) "تفاصيل العنوان" else "Address details") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = priceOverride,
                    onValueChange = { priceOverride = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(if (isArabic) "السعر ($currency)" else "Price ($currency)") },
                    placeholder = { Text("200") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Primary Button: "Save lesson" / "حفظ الحصة"
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
            shape = RoundedCornerShape(12.dp),
            enabled = (selectedStudentId != null || selectedGroupId != null),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_book_lesson_btn")
        ) {
            Text(
                if (isArabic) "حفظ الحصة" else "Save lesson",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showConflictDialog && conflictResult != null) {
            val c = conflictResult!!
            val dialogTitle = if (isArabic) {
                if (c.isDirectOverlap) "⚠️ تعارض في المواعيد" else "⚠️ تنبيه وقت التنقل"
            } else {
                if (c.isDirectOverlap) "⚠️ Schedule Overlap Detected" else "⚠️ Travel Time Warning"
            }
            AlertDialog(
                onDismissRequest = { showConflictDialog = false },
                title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        if (isArabic) {
                            "${c.warningMessage}\n\nهل ترغب في تعديل الوقت أم الحفظ على أي حال؟"
                        } else {
                            "${c.warningMessage}\n\nWould you like to adjust the time, or save anyway?"
                        }
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
                        Text(if (isArabic) "حفظ على أي حال" else "Save Anyway")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConflictDialog = false }) {
                        Text(if (isArabic) "تعديل" else "Adjust")
                    }
                }
            )
        }
    }
}

@Composable
private fun AddStudentForm(
    currency: String,
    language: String = "en",
    onSubmit: (StudentEntity) -> Unit
) {
    val isArabic = language == "ar"
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Grade 12") }
    var subject by remember { mutableStateOf("English") }
    var defaultPrice by remember { mutableDoubleStateOf(200.0) }
    var duration by remember { mutableIntStateOf(60) }
    var notes by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (isArabic) "اسم الطالب" else "Student Name") },
            placeholder = { Text(if (isArabic) "مثال: خالد محمد" else "e.g. Khalid") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_student_name_input")
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(if (isArabic) "رقم الطالب (واتساب)" else "Student Phone") },
                placeholder = { Text("+974...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = parentPhone,
                onValueChange = { parentPhone = it },
                label = { Text(if (isArabic) "رقم ولي الأمر" else "Parent Phone") },
                placeholder = { Text("+974...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text(if (isArabic) "الصف" else "Grade") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text(if (isArabic) "المادة" else "Subject") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.2f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = defaultPrice.toInt().toString(),
                onValueChange = { defaultPrice = it.toDoubleOrNull() ?: defaultPrice },
                label = { Text(if (isArabic) "السعر ($currency)" else "Price ($currency)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { duration = it.toIntOrNull() ?: duration },
                label = { Text(if (isArabic) "المدة (د)" else "Duration (min)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(if (isArabic) "ملاحظات (اختياري)" else "Notes (optional)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    val s = StudentEntity(
                        name = name.trim(),
                        phone = phone.trim(),
                        parentPhone = parentPhone.trim().ifEmpty { null },
                        school = null,
                        grade = grade.trim(),
                        subject = subject.trim(),
                        defaultPrice = defaultPrice,
                        defaultDurationMinutes = duration,
                        paymentType = "PER_LESSON",
                        privateNotes = notes.trim()
                    )
                    onSubmit(s)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_add_student_btn")
        ) {
            Text(if (isArabic) "حفظ الطالب" else "Save Student", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddGroupForm(
    students: List<StudentEntity>,
    currency: String,
    language: String = "en",
    onSubmit: (GroupEntity, List<Long>) -> Unit
) {
    val isArabic = language == "ar"
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("English") }
    var grade by remember { mutableStateOf("Grade 12") }
    var defaultPrice by remember { mutableDoubleStateOf(150.0) }
    var duration by remember { mutableIntStateOf(90) }
    val selectedMemberIds = remember { mutableStateListOf<Long>() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (isArabic) "اسم المجموعة" else "Group Name") },
            placeholder = { Text(if (isArabic) "مثال: الصف 12 - مجموعة أ" else "e.g. Grade 12 - Group A") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text(if (isArabic) "الصف" else "Grade") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text(if (isArabic) "المادة" else "Subject") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.2f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = defaultPrice.toInt().toString(),
                onValueChange = { defaultPrice = it.toDoubleOrNull() ?: defaultPrice },
                label = { Text(if (isArabic) "السعر لكل طالب ($currency)" else "Price/Student ($currency)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { duration = it.toIntOrNull() ?: duration },
                label = { Text(if (isArabic) "المدة (د)" else "Duration (min)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        if (students.isNotEmpty()) {
            Text(
                text = if (isArabic) "طلاب المجموعة:" else "Group Students:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiNavyMuted
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                students.forEach { s ->
                    val isChecked = selectedMemberIds.contains(s.id)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            if (isChecked) selectedMemberIds.remove(s.id) else selectedMemberIds.add(s.id)
                        },
                        label = { Text(s.name, fontSize = 12.sp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    val g = GroupEntity(
                        name = name.trim(),
                        subject = subject.trim(),
                        grade = grade.trim(),
                        defaultPrice = defaultPrice,
                        defaultDurationMinutes = duration
                    )
                    onSubmit(g, selectedMemberIds.toList())
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(if (isArabic) "حفظ المجموعة" else "Save Group", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordPaymentForm(
    students: List<StudentEntity>,
    currency: String,
    initialStudentId: Long? = null,
    language: String = "en",
    onSubmit: (PaymentEntity) -> Unit
) {
    val isArabic = language == "ar"
    var selectedStudentId by remember { mutableStateOf<Long?>(initialStudentId ?: students.firstOrNull()?.id) }
    var amount by remember { mutableDoubleStateOf(200.0) }
    var method by remember { mutableStateOf("CASH") } // CASH, BANK_TRANSFER
    var note by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = if (isArabic) "اختر الطالب:" else "Select Student:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiNavyMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                students.forEach { s ->
                    val isSel = (selectedStudentId == s.id)
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedStudentId = s.id },
                        label = { Text(s.name, fontSize = 13.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString(),
            onValueChange = { amount = it.toDoubleOrNull() ?: amount },
            label = { Text(if (isArabic) "المبلغ ($currency)" else "Amount ($currency)") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Text(
                text = if (isArabic) "طريقة الدفع:" else "Payment Method:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiNavyMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "CASH" to (if (isArabic) "نقداً" else "Cash"),
                    "BANK_TRANSFER" to (if (isArabic) "تحويل بنكي" else "Bank Transfer")
                ).forEach { (code, lbl) ->
                    val isSel = (method == code)
                    FilterChip(
                        selected = isSel,
                        onClick = { method = code },
                        label = { Text(lbl, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(if (isArabic) "ملاحظة (اختياري)" else "Note (optional)") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (students.isEmpty()) {
            Text(
                text = if (isArabic) "يرجى إضافة طالب أولاً لتسجيل دفعة." else "Please add a student first to record a payment.",
                fontSize = 12.sp,
                color = DarsiNavyMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                        paymentFor = "LESSON",
                        note = note.trim()
                    )
                    onSubmit(p)
                }
            },
            enabled = (selectedStudentId != null && amount > 0),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_record_payment_btn")
        ) {
            Text(if (isArabic) "تسجيل الدفعة" else "Record Payment", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
