package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LessonNoteEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.domain.model.LessonWithDetails
import com.example.domain.model.StudentWithBalance
import com.example.ui.components.DarsiCard
import com.example.ui.components.DetailRow
import com.example.ui.components.LessonStatusBadge
import com.example.ui.components.LocationEditorDialog
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.StudentLocationCard
import com.example.ui.theme.DarsiAmber
import com.example.ui.theme.DarsiAmberBg
import com.example.ui.theme.DarsiBorder
import com.example.ui.theme.DarsiCoralRed
import com.example.ui.theme.DarsiCoralRedBg
import com.example.ui.theme.DarsiCoralRedDark
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentDetailScreen(
    details: StudentWithBalance,
    lessons: List<LessonWithDetails>,
    payments: List<PaymentEntity>,
    notes: List<LessonNoteEntity>,
    currency: String,
    tutorName: String,
    onBack: () -> Unit,
    onRecordPayment: (Long) -> Unit,
    onBookLesson: (Long) -> Unit,
    onSaveStudent: (StudentEntity) -> Unit,
    onSaveNotes: (Long, String) -> Unit,
    onAddProgressNote: (studentId: Long, title: String, content: String, category: String) -> Unit,
    onDeleteProgressNote: (Long) -> Unit,
    onOpenLesson: (LessonWithDetails) -> Unit,
    onCreatePackage: (PackageEntity) -> Unit,
    onDeleteStudent: (Long) -> Unit,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic = language == "ar"
    val student = details.student

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Overview, 1=Lessons, 2=Payments, 3=Packages & Plans, 4=Notes
    val tabs = if (isArabic) {
        listOf("نظرة عامة", "الحصص (${lessons.size})", "المدفوعات (${payments.size})", "الباقات والخطط", "الملاحظات (${notes.size})")
    } else {
        listOf("Overview", "Lessons (${lessons.size})", "Payments (${payments.size})", "Packages & Plans", "Notes (${notes.size})")
    }

    var privateNotesText by remember(student.privateNotes) { mutableStateOf(student.privateNotes) }
    var notesSavedMessage by remember { mutableStateOf(false) }

    var showEditStudentDialog by remember { mutableStateOf(false) }
    var showLocationEditorDialog by remember { mutableStateOf(false) }
    var showAddPackageDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Lessons filter chip: ALL, SCHEDULED, COMPLETED, CANCELLED
    var lessonFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_detail_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("student_detail_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (isArabic) "رجوع" else "Back",
                    tint = DarsiNavy
                )
            }
            Text(
                text = if (isArabic) "ملف الطالب" else "Student Profile",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DarsiNavy,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showEditStudentDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isArabic) "تعديل الطالب" else "Edit Student",
                    tint = DarsiRoyalBlue
                )
            }
            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = if (isArabic) "حذف الطالب" else "Delete Student",
                    tint = DarsiCoralRed
                )
            }
        }

        // Student Profile Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = student.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        Text(
                            text = "${student.grade} · ${student.subject}",
                            fontSize = 13.sp,
                            color = DarsiNavyMuted
                        )
                    }

                    PaymentStatusBadge(
                        balanceDue = details.balanceDue,
                        currency = currency,
                        packageInfo = details.activePackage?.let {
                            if (isArabic) "${it.totalLessons - it.usedLessons} متبقية" else "${it.totalLessons - it.usedLessons} left"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Action Buttons: WhatsApp Chat, WhatsApp Reminder, Call, + Payment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val msg = WhatsAppHelper.createLessonReminderMessage(
                                studentName = student.name,
                                subject = student.subject,
                                timeString = if (isArabic) "موعدنا القادم" else "our upcoming session",
                                location = if (isArabic) "الموقع المحدد" else "scheduled location",
                                language = language
                            )
                            WhatsAppHelper.openChat(context, student.phone, msg)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarsiSuccessGreenBg,
                            contentColor = DarsiSuccessGreenDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (details.balanceDue > 0) {
                        FilledTonalButton(
                            onClick = {
                                val msg = "Hello ${student.name}, this is $tutorName regarding your private lessons in ${student.subject}. The current outstanding balance is ${CurrencyUtils.format(details.balanceDue, currency)} for ${details.completedLessonsCount} completed lessons. Please let me know when you can arrange payment. Thank you!"
                                WhatsAppHelper.openChat(context, student.phone, msg)
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = DarsiAmberBg,
                                contentColor = DarsiAmber
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Remind Due", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    FilledTonalButton(
                        onClick = { WhatsAppHelper.dialPhone(context, student.phone) },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarsiRoyalBlueSubtle,
                            contentColor = DarsiRoyalBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "اتصال" else "Call", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onRecordPayment(student.id) },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                    ) {
                        Text("+ Pay", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) DarsiRoyalBlue else DarsiNavyMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: OVERVIEW
                    item {
                        if (details.nextLesson != null) {
                            DarsiCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOpenLesson(
                                            LessonWithDetails(
                                                lesson = details.nextLesson,
                                                student = student
                                            )
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = DarsiRoyalBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "NEXT SCHEDULED LESSON",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavySubtle
                                        )
                                        Text(
                                            text = "${DateTimeUtils.formatReadableDate(details.nextLesson.startEpochMillis)} · ${DateTimeUtils.formatTime(details.nextLesson.startEpochMillis)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavy
                                        )
                                        Text(
                                            text = DateTimeUtils.getCountdownString(details.nextLesson.startEpochMillis, details.nextLesson.endEpochMillis),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarsiRoyalBlue
                                        )
                                    }
                                    LessonStatusBadge(status = details.nextLesson.status)
                                }
                            }
                        }
                    }

                    item {
                        // Contact & Academic Info
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "STUDENT & CONTACT INFORMATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = DarsiNavySubtle
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow("Student Phone", student.phone)
                                if (!student.parentPhone.isNullOrEmpty()) {
                                    DetailRow("Parent Phone", student.parentPhone)
                                }
                                if (!student.school.isNullOrEmpty()) {
                                    DetailRow("School", student.school)
                                }
                                DetailRow("Grade", student.grade)
                                DetailRow("Subject", student.subject)
                                DetailRow("Default Duration", "${student.defaultDurationMinutes} min")
                                DetailRow("Default Rate", CurrencyUtils.format(student.defaultPrice, currency))
                                DetailRow("Payment Plan", student.paymentType.replace("_", " "))
                                DetailRow("Status", student.status)
                            }
                        }
                    }

                    item {
                        // Student Location Card
                        StudentLocationCard(
                            student = student,
                            onEditLocation = { showLocationEditorDialog = true }
                        )
                    }

                    item {
                        // Activity & Financial Summary Card
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ACTIVITY & FINANCIAL SUMMARY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = DarsiNavySubtle
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow("Completed Lessons", "${details.completedLessonsCount}")
                                DetailRow("Scheduled Upcoming", "${details.scheduledLessonsCount}")
                                DetailRow("Total Billed", CurrencyUtils.format(details.totalBilled, currency))
                                DetailRow("Total Paid", CurrencyUtils.format(details.totalPaid, currency))
                                DetailRow("Balance Due", CurrencyUtils.format(details.balanceDue, currency))
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: LESSONS HISTORY
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LESSONS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavySubtle
                            )
                            Button(
                                onClick = { onBookLesson(student.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Schedule Lesson", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        // Filters
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL" to "All", "SCHEDULED" to "Scheduled", "COMPLETED" to "Completed", "ABSENT" to "Absent", "CANCELLED" to "Cancelled").forEach { (code, label) ->
                                val isSelected = (lessonFilter == code)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { lessonFilter = code },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    val filteredLessons = lessons.filter { l ->
                        when (lessonFilter) {
                            "SCHEDULED" -> l.lesson.status == "SCHEDULED"
                            "COMPLETED" -> l.lesson.status == "COMPLETED"
                            "ABSENT" -> l.lesson.status == "ABSENT" || l.lesson.status == "NO_SHOW"
                            "CANCELLED" -> l.lesson.status.startsWith("CANCELLED")
                            else -> true
                        }
                    }

                    if (filteredLessons.isEmpty()) {
                        item {
                            DarsiCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No lessons found for this filter.",
                                        fontSize = 13.sp,
                                        color = DarsiNavyMuted
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredLessons) { lwd ->
                            val l = lwd.lesson
                            DarsiCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenLesson(lwd) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${DateTimeUtils.formatReadableDate(l.startEpochMillis)} · ${DateTimeUtils.formatTime(l.startEpochMillis)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavy
                                        )
                                        LessonStatusBadge(status = l.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Duration: ${l.durationMinutes} min · ${CurrencyUtils.format(if (l.price > 0) l.price else student.defaultPrice, currency)} · ${l.location}",
                                        fontSize = 12.sp,
                                        color = DarsiNavyMuted
                                    )
                                    if (l.topicCovered.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Topic: ${l.topicCovered}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarsiNavy
                                        )
                                    }
                                    if (l.homework.isNotEmpty()) {
                                        Text(
                                            text = "Homework: ${l.homework}",
                                            fontSize = 12.sp,
                                            color = DarsiNavyMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: PAYMENTS
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PAYMENT HISTORY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavySubtle
                            )
                            Button(
                                onClick = { onRecordPayment(student.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Payment", fontSize = 12.sp)
                            }
                        }
                    }

                    if (payments.isEmpty()) {
                        item {
                            DarsiCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No payments recorded yet.",
                                        fontSize = 13.sp,
                                        color = DarsiNavyMuted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tap '+ Record Payment' to log cash, bank transfer, or online fees.",
                                        fontSize = 12.sp,
                                        color = DarsiNavySubtle
                                    )
                                }
                            }
                        }
                    } else {
                        items(payments) { p ->
                            DarsiCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = CurrencyUtils.format(p.amount, currency),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiSuccessGreenDark
                                        )
                                        Text(
                                            text = "${DateTimeUtils.formatDate(p.date)} · ${p.paymentMethod.replace("_", " ")} (${p.paymentFor})",
                                            fontSize = 12.sp,
                                            color = DarsiNavyMuted
                                        )
                                        if (p.note.isNotEmpty()) {
                                            Text(
                                                text = p.note,
                                                fontSize = 12.sp,
                                                color = DarsiNavy
                                            )
                                        }
                                    }
                                    Surface(
                                        color = DarsiSuccessGreenBg,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "PAID",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiSuccessGreenDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: PACKAGES & PLANS
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PAYMENT PLAN & PACKAGES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavySubtle
                            )
                            Button(
                                onClick = { showAddPackageDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Package", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        // Current Plan Card
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ACTIVE PLAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavySubtle
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (student.paymentType) {
                                        "PACKAGE" -> "Prepaid Package"
                                        "MONTHLY" -> "Monthly Flat Rate"
                                        else -> "Per Lesson (Pay as you go)"
                                    },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Rate: ${CurrencyUtils.format(student.defaultPrice, currency)} ${if (student.paymentType == "MONTHLY") "/ month" else "/ lesson"}",
                                    fontSize = 13.sp,
                                    color = DarsiNavyMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Payment arrangement:", fontSize = 11.sp, color = DarsiNavySubtle)
                                    OutlinedButton(
                                        onClick = { showEditStudentDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Change Plan", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (details.activePackage != null) {
                        val pkg = details.activePackage
                        val remaining = maxOf(0, pkg.totalLessons - pkg.usedLessons)
                        val progress = if (pkg.totalLessons > 0) pkg.usedLessons.toFloat() / pkg.totalLessons else 0f

                        item {
                            DarsiCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = pkg.packageName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavy
                                        )
                                        Text(
                                            text = CurrencyUtils.format(pkg.price, currency),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiSuccessGreenDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = if (remaining <= 1) DarsiAmber else DarsiRoyalBlue,
                                        trackColor = DarsiRoyalBlueSubtle,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${pkg.usedLessons} of ${pkg.totalLessons} lessons completed",
                                            fontSize = 12.sp,
                                            color = DarsiNavyMuted
                                        )
                                        Text(
                                            text = "$remaining remaining",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (remaining <= 1) DarsiCoralRed else DarsiNavy
                                        )
                                    }
                                    if (remaining <= 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = DarsiAmberBg,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "⚠️ Package almost depleted. Consider renewing before the next lesson.",
                                                fontSize = 11.sp,
                                                color = DarsiAmber,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: NOTES & PROGRESS
                    item {
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PRIVATE TUTOR NOTES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = DarsiNavySubtle
                                    )
                                    if (notesSavedMessage) {
                                        Text(
                                            text = "Saved!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiSuccessGreenDark
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = privateNotesText,
                                    onValueChange = {
                                        privateNotesText = it
                                        notesSavedMessage = false
                                    },
                                    placeholder = { Text("Confidential observations, syllabus progress, parent requests...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    minLines = 3
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        onSaveNotes(student.id, privateNotesText)
                                        notesSavedMessage = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Save Notes", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LEARNING LOG / OBSERVATIONS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavySubtle
                            )
                            Button(
                                onClick = { showAddNoteDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                            ) {
                                Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Entry", fontSize = 12.sp)
                            }
                        }
                    }

                    if (notes.isEmpty()) {
                        item {
                            DarsiCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No progress logs added yet.",
                                        fontSize = 13.sp,
                                        color = DarsiNavyMuted
                                    )
                                }
                            }
                        }
                    } else {
                        items(notes) { note ->
                            DarsiCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = note.title.ifEmpty { "Observation" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavy
                                        )
                                        IconButton(
                                            onClick = { onDeleteProgressNote(note.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Note",
                                                tint = DarsiCoralRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.content,
                                        fontSize = 13.sp,
                                        color = DarsiNavy
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${DateTimeUtils.formatDate(note.createdAt)} · ${note.category}",
                                        fontSize = 11.sp,
                                        color = DarsiNavyMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Edit Student Dialog
    if (showEditStudentDialog) {
        var editName by remember { mutableStateOf(student.name) }
        var editPhone by remember { mutableStateOf(student.phone) }
        var editParentPhone by remember { mutableStateOf(student.parentPhone ?: "") }
        var editSchool by remember { mutableStateOf(student.school ?: "") }
        var editGrade by remember { mutableStateOf(student.grade) }
        var editSubject by remember { mutableStateOf(student.subject) }
        var editPrice by remember { mutableDoubleStateOf(student.defaultPrice) }
        var editDuration by remember { mutableIntStateOf(student.defaultDurationMinutes) }
        var editPaymentType by remember { mutableStateOf(student.paymentType) }
        var editStatus by remember { mutableStateOf(student.status) }

        AlertDialog(
            onDismissRequest = { showEditStudentDialog = false },
            title = { Text("Edit Student Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editParentPhone,
                        onValueChange = { editParentPhone = it },
                        label = { Text("Parent Phone (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGrade,
                        onValueChange = { editGrade = it },
                        label = { Text("Grade") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSchool,
                        onValueChange = { editSchool = it },
                        label = { Text("School (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editPrice.toString(),
                            onValueChange = { editPrice = it.toDoubleOrNull() ?: editPrice },
                            label = { Text("Default Rate ($currency)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editDuration.toString(),
                            onValueChange = { editDuration = it.toIntOrNull() ?: editDuration },
                            label = { Text("Duration (min)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Payment Plan:", fontSize = 12.sp, color = DarsiNavyMuted)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "PER_LESSON" to "Per Lesson",
                            "MONTHLY" to "Monthly",
                            "PACKAGE" to "Package",
                            "CUSTOM" to "Custom"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = (editPaymentType == type),
                                onClick = { editPaymentType = type },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Student Status:", fontSize = 12.sp, color = DarsiNavyMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ACTIVE" to "Active",
                            "PAUSED" to "Paused",
                            "FINISHED" to "Finished"
                        ).forEach { (st, label) ->
                            FilterChip(
                                selected = (editStatus == st),
                                onClick = { editStatus = st },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = student.copy(
                            name = editName,
                            phone = editPhone,
                            parentPhone = editParentPhone.ifBlank { null },
                            school = editSchool.ifBlank { null },
                            grade = editGrade,
                            subject = editSubject,
                            defaultPrice = editPrice,
                            defaultDurationMinutes = editDuration,
                            paymentType = editPaymentType,
                            status = editStatus,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSaveStudent(updated)
                        showEditStudentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Package Dialog
    if (showAddPackageDialog) {
        var pkgName by remember { mutableStateOf("Lesson Package (${student.subject})") }
        var totalLessons by remember { mutableIntStateOf(8) }
        var pkgPrice by remember { mutableDoubleStateOf(student.defaultPrice * 8) }

        AlertDialog(
            onDismissRequest = { showAddPackageDialog = false },
            title = { Text("Add Prepaid Package") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pkgName,
                        onValueChange = { pkgName = it },
                        label = { Text("Package Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Number of Lessons:", fontSize = 12.sp, color = DarsiNavyMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(4, 8, 10, 12).forEach { num ->
                            val isSel = (totalLessons == num)
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    totalLessons = num
                                    pkgPrice = student.defaultPrice * num
                                },
                                label = { Text("$num lessons") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = pkgPrice.toString(),
                        onValueChange = { pkgPrice = it.toDoubleOrNull() ?: pkgPrice },
                        label = { Text("Total Package Price ($currency)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPkg = PackageEntity(
                            studentId = student.id,
                            packageName = pkgName,
                            totalLessons = totalLessons,
                            usedLessons = 0,
                            price = pkgPrice,
                            currency = currency,
                            status = "ACTIVE"
                        )
                        onCreatePackage(newPkg)
                        showAddPackageDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                ) {
                    Text("Create Package")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPackageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Progress Note Dialog
    if (showAddNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }
        var noteCategory by remember { mutableStateOf("PROGRESS") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Learning Log Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title / Focus Area") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Notes / Observations") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PROGRESS" to "Progress", "HOMEWORK" to "Homework", "EXAM" to "Exam").forEach { (code, lbl) ->
                            FilterChip(
                                selected = noteCategory == code,
                                onClick = { noteCategory = code },
                                label = { Text(lbl) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteContent.isNotBlank()) {
                            onAddProgressNote(student.id, noteTitle, noteContent, noteCategory)
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                ) {
                    Text("Add Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Student Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Student?") },
            text = { Text("Are you sure you want to delete ${student.name}? This will remove all their records from your device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudent(student.id)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiCoralRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Student Location Dialog
    if (showLocationEditorDialog) {
        LocationEditorDialog(
            initialLabel = student.locationLabel,
            initialArea = student.areaName,
            initialAddress = student.addressText,
            initialMapsLink = student.mapsLink,
            initialNotes = student.locationNotes,
            initialTravelTime = student.defaultTravelTimeMinutes,
            initialLocationType = student.defaultLessonLocationType,
            initialLat = student.latitude,
            initialLng = student.longitude,
            title = "Edit Location for ${student.name}",
            onDismiss = { showLocationEditorDialog = false },
            onSave = { label, area, address, mapsLink, notes, travelTime, locType, lat, lng ->
                val updated = student.copy(
                    locationLabel = label,
                    areaName = area,
                    addressText = address,
                    mapsLink = mapsLink,
                    locationNotes = notes,
                    defaultTravelTimeMinutes = travelTime,
                    defaultLessonLocationType = locType,
                    latitude = lat,
                    longitude = lng,
                    updatedAt = System.currentTimeMillis()
                )
                onSaveStudent(updated)
            }
        )
    }
}

