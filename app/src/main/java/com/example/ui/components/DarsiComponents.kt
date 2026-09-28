package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarsiAmber
import com.example.ui.theme.DarsiAmberBg
import com.example.ui.theme.DarsiAmberBorder
import com.example.ui.theme.DarsiAmberDark
import com.example.ui.theme.DarsiBorder
import com.example.ui.theme.DarsiCardWhite
import com.example.ui.theme.DarsiCoralRed
import com.example.ui.theme.DarsiCoralRedBg
import com.example.ui.theme.DarsiCoralRedBorder
import com.example.ui.theme.DarsiCoralRedDark
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyDark
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenBorder
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.ui.theme.DarsiSurfaceSecondary

@Composable
fun StatusBadge(
    status: String,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val isArabic = language == "ar"
    // Soft green for completed, Amber for upcoming/scheduled, Restrained coral for absent/cancelled
    val (bgColor, borderColor, dotColor, textColor, label) = when (status) {
        "COMPLETED" -> Quintuple(DarsiSuccessGreenBg, DarsiSuccessGreenBorder, DarsiSuccessGreen, DarsiSuccessGreenDark, if (isArabic) "مكتملة" else "Completed")
        "SCHEDULED" -> Quintuple(DarsiAmberBg, DarsiAmberBorder, DarsiAmber, DarsiAmberDark, if (isArabic) "قادمة" else "Upcoming")
        "ABSENT" -> Quintuple(DarsiCoralRedBg, DarsiCoralRedBorder, DarsiCoralRed, DarsiCoralRedDark, if (isArabic) "غائب" else "Absent")
        "CANCELLED_BY_STUDENT" -> Quintuple(DarsiCoralRedBg, DarsiCoralRedBorder, DarsiCoralRed, DarsiCoralRedDark, if (isArabic) "ملغاة (طالب)" else "Cancelled (Student)")
        "CANCELLED_BY_TUTOR" -> Quintuple(DarsiSurfaceSecondary, DarsiBorder, DarsiNavySubtle, DarsiNavyMuted, if (isArabic) "ملغاة (معلم)" else "Cancelled (Tutor)")
        "NO_SHOW" -> Quintuple(DarsiCoralRedBg, DarsiCoralRedBorder, DarsiCoralRed, DarsiCoralRedDark, if (isArabic) "لم يحضر" else "No Show")
        else -> Quintuple(DarsiSurfaceSecondary, DarsiBorder, DarsiNavySubtle, DarsiNavy, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

private data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

@Composable
fun PaymentStatusBadge(
    balanceDue: Double,
    currency: String,
    packageInfo: String? = null,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val isArabic = language == "ar"
    if (packageInfo != null) {
        // Active Package indicator in soft amber
        Surface(
            color = DarsiAmberBg,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarsiAmberBorder),
            modifier = modifier
        ) {
            Text(
                text = packageInfo,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiAmberDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    } else if (balanceDue <= 0.0) {
        // Soft green for paid state
        Surface(
            color = DarsiSuccessGreenBg,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarsiSuccessGreenBorder),
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(DarsiSuccessGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "مسدد" else "Paid",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarsiSuccessGreenDark
                )
            }
        }
    } else {
        // Restrained coral/red only for overdue payments
        Surface(
            color = DarsiCoralRedBg,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarsiCoralRedBorder),
            modifier = modifier
        ) {
            val dueStr = if (balanceDue % 1.0 == 0.0) "${balanceDue.toInt()}" else "%.2f".format(balanceDue)
            Text(
                text = if (isArabic) "مستحق $dueStr $currency" else "$currency $dueStr due",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarsiCoralRedDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
fun DarsiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // Pure white cards with subtle border and soft shadow
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarsiCardWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .border(1.dp, DarsiBorder, RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun LessonStatusBadge(
    status: String,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    StatusBadge(status = status, language = language, modifier = modifier)
}

@Composable
fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = DarsiNavyMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarsiNavyDark)
    }
}
