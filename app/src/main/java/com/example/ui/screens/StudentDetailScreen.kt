package com.example.ui.screens

import java.util.Locale
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
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

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Overview, 1=Lessons, 2=Payments, 3=Notes
    val tabs = if (isArabic) {
        listOf("نظرة عامة", "الحصص (${lessons.size})", "المدفوعات (${payments.size})", "الملاحظات (${notes.size})")
    } else {
        listOf("Overview", "Lessons (${lessons.size})", "Payments (${payments.size})", "Notes (${notes.size})")
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
                        },
                        language = language
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3 Quick Action Buttons: WhatsApp, Call, Book Lesson
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. WhatsApp
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
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("student_action_whatsapp"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarsiSuccessGreenBg,
                            contentColor = DarsiSuccessGreenDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "واتساب" else "WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // 2. Call
                    FilledTonalButton(
                        onClick = { WhatsAppHelper.dialPhone(context, student.phone) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("student_action_call"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarsiSurfaceVariant,
                            contentColor = DarsiNavy
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "اتصال" else "Call", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // 3. Book Lesson
                    Button(
                        onClick = { onBookLesson(student.id) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("student_action_book_lesson"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "حجز حصة" else "Book Lesson", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                            text = if (isArabic) "الحصة القادمة المجدولة" else "NEXT SCHEDULED LESSON",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavySubtle
                                        )
                                        val dateLocale = if (isArabic) Locale.forLanguageTag("ar") else Locale.US
                                        Text(
                                            text = "${DateTimeUtils.formatReadableDate(details.nextLesson.startEpochMillis, dateLocale)} · ${DateTimeUtils.formatTime(details.nextLesson.startEpochMillis, dateLocale)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavy
                                        )
                                        Text(
                                            text = DateTimeUtils.getCountdownString(details.nextLesson.startEpochMillis, details.nextLesson.endEpochMillis, isArabic = isArabic),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarsiRoyalBlue
                                        )
                                    }
                                    LessonStatusBadge(status = details.nextLesson.status, language = language)
                                }
                            }
                        }
                    }

                    item {
                        // Contact & Academic Info
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isArabic) "معلومات الطالب والتواصل" else "STUDENT & CONTACT INFORMATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = DarsiNavySubtle
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow(if (isArabic) "رقم الطالب" else "Student Phone", student.phone)
                                if (!student.parentPhone.isNullOrEmpty()) {
                                    DetailRow(if (isArabic) "رقم ولي الأمر" else "Parent Phone", student.parentPhone)
                                }
                                if (!student.school.isNullOrEmpty()) {
                                    DetailRow(if (isArabic) "المدرسة" else "School", student.school)
                                }
                                DetailRow(if (isArabic) "الصف" else "Grade", student.grade)
                                DetailRow(if (isArabic) "المادة" else "Subject", student.subject)
                                DetailRow(if (isArabic) "المدة الافتراضية" else "Default Duration", if (isArabic) "${student.defaultDurationMinutes} دقيقة" else "${student.defaultDurationMinutes} min")
                                DetailRow(if (isArabic) "السعر الافتراضي" else "Default Rate", CurrencyUtils.format(student.defaultPrice, currency))
                                DetailRow(
                                    if (isArabic) "نظام المحاسبة" else "Payment Plan",
                                    if (isArabic) {
                                        when (student.paymentType) {
                                            "PER_LESSON" -> "لكل حصة"
                                            "MONTHLY" -> "شهري"
                                            "PACKAGE" -> "باقة"
                                            else -> student.paymentType
                                        }
                                    } else {
                                        student.paymentType.replace("_", " ")
                                    }
                                )
                                DetailRow(
                                    if (isArabic) "الحالة" else "Status",
                                    if (isArabic) {
                                        when (student.status) {
                                            "ACTIVE" -> "نشط"
                                            "ARCHIVED" -> "مؤرشف"
                                            "PAUSED" -> "متوقف مؤقتاً"
                                            else -> student.status
                                        }
                                    } else {
                                        student.status
                                    }
                                )
                            }
                        }
                    }

                    item {
                        // Student Location Card
                        StudentLocationCard(
                            student = student,
                            onEditLocation = { showLocationEditorDialog = true },
                            language = language
                        )
                    }

                    item {
                        // Activity & Financial Summary Card
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isArabic) "ملخص النشاط والوضع المالي" else "ACTIVITY & FINANCIAL SUMMARY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = DarsiNavySubtle
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                DetailRow(if (isArabic) "الحصص المكتملة" else "Completed Lessons", "${details.completedLessonsCount}")
                                DetailRow(if (isArabic) "الحصص المجدولة" else "Scheduled Upcoming", "${details.scheduledLessonsCount}")
                                DetailRow(if (isArabic) "إجمالي المستحق" else "Total Billed", CurrencyUtils.format(details.totalBilled, currency))
                                DetailRow(if (isArabic) "إجمالي المدفوع" else "Total Paid", CurrencyUtils.format(details.totalPaid, currency))
                                DetailRow(if (isArabic) "المتبقي / الرصيد" else "Balance Due", CurrencyUtils.format(details.balanceDue, currency))
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
                                text = if (isArabic) "الحصص" else "LESSONS",
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
                                Text(if (isArabic) "إضافة حصة" else "Schedule Lesson", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        // Filters
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to (if (isArabic) "الكل" else "All"),
                                "SCHEDULED" to (if (isArabic) "المجدولة" else "Scheduled"),
                                "COMPLETED" to (if (isArabic) "المكتملة" else "Completed"),
                                "ABSENT" to (if (isArabic) "الغياب" else "Absent"),
                                "CANCELLED" to (if (isArabic) "الملغاة" else "Cancelled")
                            ).forEach { (code, label) ->
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
                                        text = if (isArabic) "لا توجد حصص بهذا التصنيف." else "No lessons found for this filter.",
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
                                        val lessonDateLocale = if (isArabic) Locale.forLanguageTag("ar") else Locale.US
                                        Text(
                                            text = "${DateTimeUtils.formatReadableDate(l.startEpochMillis, lessonDateLocale)} · ${DateTimeUtils.formatTime(l.startEpochMillis, lessonDateLocale)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarsiNavy
                                        )
                                        LessonStatusBadge(status = l.status, language = language)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isArabic) "المدة: ${l.durationMinutes} دقيقة · ${CurrencyUtils.format(if (l.price > 0) l.price else student.defaultPrice, currency)} · ${l.location}" else "Duration: ${l.durationMinutes} min · ${CurrencyUtils.format(if (l.price > 0) l.price else student.defaultPrice, currency)} · ${l.location}",
                                        fontSize = 12.sp,
                                        color = DarsiNavyMuted
                                    )
                                    if (l.topicCovered.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isArabic) "الموضوع: ${l.topicCovered}" else "Topic: ${l.topicCovered}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarsiNavy
                                        )
                                    }
                                    if (l.homework.isNotEmpty()) {
                                        Text(
                                            text = if (isArabic) "الواجب: ${l.homework}" else "Homework: ${l.homework}",
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
                                text = if (isArabic) "سجل المدفوعات" else "PAYMENT HISTORY",
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
                                Text(if (isArabic) "تسجيل دفعة" else "Record Payment", fontSize = 12.sp)
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
                                        text = if (isArabic) "لا توجد مدفوعات مسجلة بعد." else "No payments recorded yet.",
                                        fontSize = 13.sp,
                                        color = DarsiNavyMuted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isArabic) "اضغط على '+ تسجيل دفعة' لتسجيل المبالغ النقدية أو التحويلات." else "Tap '+ Record Payment' to log cash, bank transfer, or online fees.",
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
                                        val methodDisplay = when (p.paymentMethod) {
                                            "CASH" -> if (isArabic) "نقداً" else "Cash"
                                            "BANK_TRANSFER" -> if (isArabic) "تحويل بنكي" else "Bank Transfer"
                                            "ONLINE" -> if (isArabic) "أونلاين" else "Online"
                                            else -> p.paymentMethod.replace("_", " ")
                                        }
                                        val forDisplay = when (p.paymentFor) {
                                            "LESSON" -> if (isArabic) "حصة" else "Lesson"
                                            "PACKAGE" -> if (isArabic) "باقة" else "Package"
                                            "MONTHLY" -> if (isArabic) "شهري" else "Monthly"
                                            else -> p.paymentFor
                                        }
                                        Text(
                                            text = "${DateTimeUtils.formatDate(p.date)} · $methodDisplay ($forDisplay)",
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
                                            text = if (isArabic) "تم الدفع" else "PAID",
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

                    // Packages & Prepaid Plans section inside Payments tab
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "الباقات والاشتراكات" else "PACKAGES & PREPAID PLANS",
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
                                Text(if (isArabic) "باقة جديدة" else "New Package", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        // Current Plan Card
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isArabic) "الخطة الحالية" else "ACTIVE PLAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavySubtle
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (student.paymentType) {
                                        "PACKAGE" -> if (isArabic) "باقة مسبقة الدفع" else "Prepaid Package"
                                        "MONTHLY" -> if (isArabic) "اشتراك شهري ثابت" else "Monthly Flat Rate"
                                        else -> if (isArabic) "لكل حصة (دفع أولاً بأول)" else "Per Lesson (Pay as you go)"
                                    },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isArabic) "السعر: ${CurrencyUtils.format(student.defaultPrice, currency)} ${if (student.paymentType == "MONTHLY") "/ شهر" else "/ حصة"}" else "Rate: ${CurrencyUtils.format(student.defaultPrice, currency)} ${if (student.paymentType == "MONTHLY") "/ month" else "/ lesson"}",
                                    fontSize = 13.sp,
                                    color = DarsiNavyMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (isArabic) "نظام المحاسبة:" else "Payment arrangement:", fontSize = 11.sp, color = DarsiNavySubtle)
                                    OutlinedButton(
                                        onClick = { showEditStudentDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(if (isArabic) "تغيير الخطة" else "Change Plan", fontSize = 11.sp)
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
                                            text = if (isArabic) "${pkg.usedLessons} من ${pkg.totalLessons} حصص مكتملة" else "${pkg.usedLessons} of ${pkg.totalLessons} lessons completed",
                                            fontSize = 12.sp,
                                            color = DarsiNavyMuted
                                        )
                                        Text(
                                            text = if (isArabic) "$remaining متبقية" else "$remaining remaining",
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
                                                text = if (isArabic) "⚠️ أوشكت الباقة على الانتهاء. فكّر في تجديدها قبل الحصة القادمة." else "⚠️ Package almost depleted. Consider renewing before the next lesson.",
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

                3 -> {
                    // TAB 3: NOTES & PROGRESS
                    item {
                        DarsiCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isArabic) "ملاحظات المعلم الخاصة" else "PRIVATE TUTOR NOTES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = DarsiNavySubtle
                                    )
                                    if (notesSavedMessage) {
                                        Text(
                                            text = if (isArabic) "تم الحفظ!" else "Saved!",
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
                                    placeholder = { Text(if (isArabic) "ملاحظات سرية، تقدم المنهج، طلبات ولي الأمر..." else "Confidential observations, syllabus progress, parent requests...") },
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
                                    Text(if (isArabic) "حفظ الملاحظات" else "Save Notes", fontSize = 12.sp)
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
                                text = if (isArabic) "سجل الملاحظات والتقدم" else "LEARNING LOG / OBSERVATIONS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavySubtle
                            )
                            Button(
                                onClick = { showAddNoteDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "إضافة ملاحظة" else "Add Entry", fontSize = 12.sp)
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
                                        text = if (isArabic) "لا توجد ملاحظات مسجلة بعد." else "No progress logs added yet.",
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
            title = { Text(if (isArabic) "تعديل ملف الطالب" else "Edit Student Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(if (isArabic) "الاسم الكامل" else "Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(if (isArabic) "رقم الهاتف" else "Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editParentPhone,
                        onValueChange = { editParentPhone = it },
                        label = { Text(if (isArabic) "هاتف ولي الأمر (اختياري)" else "Parent Phone (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text(if (isArabic) "المادة" else "Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGrade,
                        onValueChange = { editGrade = it },
                        label = { Text(if (isArabic) "الصف" else "Grade") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSchool,
                        onValueChange = { editSchool = it },
                        label = { Text(if (isArabic) "المدرسة (اختياري)" else "School (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editPrice.toString(),
                            onValueChange = { editPrice = it.toDoubleOrNull() ?: editPrice },
                            label = { Text(if (isArabic) "السعر الافتراضي ($currency)" else "Default Rate ($currency)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editDuration.toString(),
                            onValueChange = { editDuration = it.toIntOrNull() ?: editDuration },
                            label = { Text(if (isArabic) "المدة (دقيقة)" else "Duration (min)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(if (isArabic) "خطة الدفع:" else "Payment Plan:", fontSize = 12.sp, color = DarsiNavyMuted)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "PER_LESSON" to (if (isArabic) "بعد كل حصة" else "Per Lesson"),
                            "MONTHLY" to (if (isArabic) "شهري" else "Monthly"),
                            "PACKAGE" to (if (isArabic) "باقة" else "Package"),
                            "CUSTOM" to (if (isArabic) "مخصص" else "Custom")
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = (editPaymentType == type),
                                onClick = { editPaymentType = type },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text(if (isArabic) "حالة الطالب:" else "Student Status:", fontSize = 12.sp, color = DarsiNavyMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ACTIVE" to (if (isArabic) "نشط" else "Active"),
                            "PAUSED" to (if (isArabic) "متوقف مؤقتاً" else "Paused"),
                            "FINISHED" to (if (isArabic) "منتهي" else "Finished")
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
                    Text(if (isArabic) "حفظ التغييرات" else "Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditStudentDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Add Package Dialog
    if (showAddPackageDialog) {
        var pkgName by remember { mutableStateOf(if (isArabic) "باقة حصص (${student.subject})" else "Lesson Package (${student.subject})") }
        var totalLessons by remember { mutableIntStateOf(8) }
        var pkgPrice by remember { mutableDoubleStateOf(student.defaultPrice * 8) }

        AlertDialog(
            onDismissRequest = { showAddPackageDialog = false },
            title = { Text(if (isArabic) "إضافة باقة مسبقة الدفع" else "Add Prepaid Package") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pkgName,
                        onValueChange = { pkgName = it },
                        label = { Text(if (isArabic) "اسم الباقة" else "Package Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(if (isArabic) "عدد الحصص:" else "Number of Lessons:", fontSize = 12.sp, color = DarsiNavyMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(4, 8, 10, 12).forEach { num ->
                            val isSel = (totalLessons == num)
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    totalLessons = num
                                    pkgPrice = student.defaultPrice * num
                                },
                                label = { Text(if (isArabic) "$num حصص" else "$num lessons") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = pkgPrice.toString(),
                        onValueChange = { pkgPrice = it.toDoubleOrNull() ?: pkgPrice },
                        label = { Text(if (isArabic) "إجمالي سعر الباقة ($currency)" else "Total Package Price ($currency)") },
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
                    Text(if (isArabic) "إنشاء الباقة" else "Create Package")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPackageDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
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
            title = { Text(if (isArabic) "إضافة سجل تعليمي" else "Add Learning Log Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text(if (isArabic) "العنوان / محور التركيز" else "Title / Focus Area") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text(if (isArabic) "الملاحظات والمشاهدات" else "Notes / Observations") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "PROGRESS" to (if (isArabic) "تقدم" else "Progress"),
                            "HOMEWORK" to (if (isArabic) "واجب" else "Homework"),
                            "EXAM" to (if (isArabic) "اختبار" else "Exam")
                        ).forEach { (code, lbl) ->
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
                    Text(if (isArabic) "إضافة السجل" else "Add Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Delete Student Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(if (isArabic) "حذف الطالب؟" else "Delete Student?") },
            text = { Text(if (isArabic) "هل أنت متأكد من حذف ${student.name}؟ سيؤدي ذلك إلى إزالة جميع سجلاته من جهازك." else "Are you sure you want to delete ${student.name}? This will remove all their records from your device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudent(student.id)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiCoralRed)
                ) {
                    Text(if (isArabic) "حذف" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
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
            title = if (isArabic) "تعديل موقع ${student.name}" else "Edit Location for ${student.name}",
            language = language,
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

