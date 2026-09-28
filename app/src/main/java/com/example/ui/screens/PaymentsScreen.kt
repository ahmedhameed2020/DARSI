package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentEntity
import com.example.domain.model.PaymentSummary
import com.example.domain.model.StudentWithBalance
import com.example.ui.components.DarsiCard
import com.example.ui.components.PaymentStatusBadge
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
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import com.example.util.WhatsAppHelper

@Composable
fun PaymentsScreen(
    students: List<StudentWithBalance>,
    payments: List<PaymentEntity>,
    summary: PaymentSummary,
    currency: String,
    onRecordPayment: () -> Unit,
    onOpenStudent: (Long) -> Unit,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic = language == "ar"
    var showOnlyBalancesDue by remember { mutableStateOf(false) }

    val displayedStudents = remember(students, showOnlyBalancesDue) {
        if (showOnlyBalancesDue) {
            students.filter { it.balanceDue > 0 }
        } else {
            students
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("payments_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Month Indicator Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "المدفوعات" else "Payments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Text(
                    text = DateTimeUtils.formatMonthYear(System.currentTimeMillis(), if (isArabic) java.util.Locale.forLanguageTag("ar") else java.util.Locale.US),
                    fontSize = 13.sp,
                    color = DarsiNavySubtle
                )
            }

            Button(
                onClick = onRecordPayment,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                modifier = Modifier.testTag("record_payment_top_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isArabic) "تسجيل دفعة" else "Record Payment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simple Calm Received vs Due Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Received
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarsiSuccessGreenBg),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "تم التحصيل هذا الشهر" else "Received this month",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarsiSuccessGreenDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.format(summary.totalReceivedThisMonth, currency),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarsiSuccessGreenDark
                    )
                }
            }

            // Due
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarsiCoralRedBg),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isArabic) "إجمالي المستحق" else "Total amount due",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarsiCoralRedDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.format(summary.totalDueAllStudents, currency),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarsiCoralRedDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Student Payment Rows Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "حالة مدفوعات الطلاب" else "STUDENT PAYMENT STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = DarsiNavySubtle
            )

            FilledTonalButton(
                onClick = { showOnlyBalancesDue = !showOnlyBalancesDue },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = if (showOnlyBalancesDue) {
                        if (isArabic) "عرض الكل" else "Show All"
                    } else {
                        if (isArabic) "المستحق فقط" else "Show Overdue Only"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Student Payment Rows List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (displayedStudents.isEmpty()) {
                item {
                    DarsiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (showOnlyBalancesDue) {
                                    if (isArabic) "جميع الطلاب سددوا مستحقاتهم!" else "All students are settled up!"
                                } else {
                                    if (isArabic) "لم يتم تسجيل أي طالب حتى الآن." else "No students registered yet."
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavy
                            )
                        }
                    }
                }
            } else {
                items(displayedStudents, key = { it.student.id }) { item ->
                    StudentPaymentRow(
                        item = item,
                        currency = currency,
                        language = language,
                        onOpenStudent = { onOpenStudent(item.student.id) },
                        onSendReminder = {
                            val msg = WhatsAppHelper.createPaymentReminderMessage(
                                studentName = item.student.name,
                                amountDue = item.balanceDue,
                                currency = currency,
                                language = language
                            )
                            WhatsAppHelper.openChat(context, item.student.phone, msg)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StudentPaymentRow(
    item: StudentWithBalance,
    currency: String,
    language: String = "en",
    onOpenStudent: () -> Unit,
    onSendReminder: () -> Unit
) {
    val isArabic = language == "ar"
    val student = item.student
    val packageInfo = item.activePackage?.let {
        val remaining = it.totalLessons - it.usedLessons
        if (isArabic) "$remaining حصص متبقية" else "$remaining lessons remaining"
    }

    val paymentTypeDisplay = when (student.paymentType) {
        "PER_LESSON", "POST_LESSON" -> if (isArabic) "بعد كل حصة" else "Per Lesson"
        "MONTHLY" -> if (isArabic) "شهري" else "Monthly"
        "PACKAGE" -> if (isArabic) "باقة" else "Package"
        "PREPAID" -> if (isArabic) "مسبق الدفع" else "Prepaid"
        else -> student.paymentType.replace("_", " ")
    }

    DarsiCard(
        onClick = onOpenStudent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Text(
                    text = "${student.grade} · $paymentTypeDisplay",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PaymentStatusBadge(
                    balanceDue = item.balanceDue,
                    currency = currency,
                    packageInfo = packageInfo,
                    language = language
                )

                if (item.balanceDue > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = onSendReminder,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarsiSuccessGreenBg,
                            contentColor = DarsiSuccessGreenDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = if (isArabic) "تذكير بالدفع عبر واتساب" else "WhatsApp Payment Reminder",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
