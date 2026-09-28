package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Timer
import com.example.util.MapsAndLocationHelper
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LessonWithDetails
import com.example.domain.model.PaymentSummary
import com.example.ui.components.DarsiCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DarsiAmber
import com.example.ui.theme.DarsiAmberBg
import com.example.ui.theme.DarsiAmberBorder
import com.example.ui.theme.DarsiAmberDark
import com.example.ui.theme.DarsiBorder
import com.example.ui.theme.DarsiCardWhite
import com.example.ui.theme.DarsiCoralRed
import com.example.ui.theme.DarsiCoralRedDark
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyDark
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueBorder
import com.example.ui.theme.DarsiRoyalBlueDark
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenBorder
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.ui.theme.DarsiSurfaceSecondary
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import com.example.util.WhatsAppHelper

@Composable
fun TodayScreen(
    tutorName: String,
    currency: String,
    nextLesson: LessonWithDetails?,
    todayLessons: List<LessonWithDetails>,
    todaySummary: PaymentSummary,
    travelBufferMinutes: Int = 10,
    onOpenLesson: (LessonWithDetails) -> Unit,
    onBookLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calm Greeting & Date Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateTimeUtils.getGreeting(tutorName),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarsiNavyDark
                    )
                    Text(
                        text = DateTimeUtils.formatReadableDate(System.currentTimeMillis()),
                        fontSize = 13.sp,
                        color = DarsiNavySubtle
                    )
                }
            }
        }

        // NEXT LESSON CARD - Pure White with Subtle Royal Accent
        item {
            if (nextLesson != null) {
                val countdown = DateTimeUtils.getCountdownString(
                    nextLesson.lesson.startEpochMillis,
                    nextLesson.lesson.endEpochMillis
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarsiCardWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarsiRoyalBlueBorder, RoundedCornerShape(20.dp))
                        .testTag("next_lesson_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = DarsiRoyalBlue,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "NEXT LESSON",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Countdown badge (Soft Amber)
                            Surface(
                                color = DarsiAmberBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarsiAmberBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(DarsiAmber)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = countdown,
                                        color = DarsiAmberDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Title & Grade/Subject in Dark Navy
                        Text(
                            text = nextLesson.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavyDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nextLesson.subtitle,
                            fontSize = 13.sp,
                            color = DarsiNavyMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Start & End Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = DarsiRoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${DateTimeUtils.formatTime(nextLesson.lesson.startEpochMillis)} – ${DateTimeUtils.formatTime(nextLesson.lesson.endEpochMillis)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarsiNavyDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${nextLesson.lesson.durationMinutes} min)",
                                fontSize = 12.sp,
                                color = DarsiNavySubtle
                            )
                        }

                        // Location info & Departure recommendation
                        val areaOrLoc = nextLesson.displayAreaOrLocation
                        if (nextLesson.isOnline) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = DarsiRoyalBlueSubtle,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarsiRoyalBlueBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp), tint = DarsiRoyalBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Online Lesson", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarsiRoyalBlue)
                                }
                            }
                        } else if (areaOrLoc.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = DarsiRoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$areaOrLoc · ${nextLesson.lesson.locationLabel.ifBlank { "Student Home" }}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarsiNavy
                                )
                            }
                        }

                        val travelMin = nextLesson.effectiveTravelTimeMinutes
                        if (!nextLesson.isOnline && travelMin != null && travelMin > 0) {
                            val departureText = MapsAndLocationHelper.formatDepartureSuggestion(
                                startEpochMillis = nextLesson.lesson.startEpochMillis,
                                travelTimeMinutes = travelMin,
                                bufferMinutes = travelBufferMinutes
                            )
                            if (departureText != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = DarsiAmberBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarsiAmberBorder)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = DarsiAmberDark)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$departureText (Est. travel $travelMin min)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarsiAmberDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions: Directions / Meeting, WhatsApp, Call, Open Lesson
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!nextLesson.isOnline) {
                                FilledTonalButton(
                                    onClick = {
                                        MapsAndLocationHelper.navigate(
                                            context = context,
                                            latitude = nextLesson.lesson.latitude ?: nextLesson.student?.latitude ?: nextLesson.group?.latitude,
                                            longitude = nextLesson.lesson.longitude ?: nextLesson.student?.longitude ?: nextLesson.group?.longitude,
                                            addressText = nextLesson.lesson.addressText.ifBlank { nextLesson.student?.addressText ?: nextLesson.group?.addressText ?: "" },
                                            areaName = nextLesson.displayAreaOrLocation,
                                            label = nextLesson.title
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("next_lesson_directions_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = DarsiSurfaceSecondary,
                                        contentColor = DarsiRoyalBlue
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = "Directions",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Directions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else if (nextLesson.lesson.meetingUrl.isNotBlank()) {
                                FilledTonalButton(
                                    onClick = {
                                        MapsAndLocationHelper.openMeetingUrl(context, nextLesson.lesson.meetingUrl)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("next_lesson_join_meeting_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = DarsiRoyalBlueSubtle,
                                        contentColor = DarsiRoyalBlue
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = "Meeting",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Join", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            val phone = nextLesson.phoneToContact
                            if (!phone.isNullOrEmpty()) {
                                FilledTonalButton(
                                    onClick = {
                                        val msg = WhatsAppHelper.createLessonReminderMessage(
                                            studentName = nextLesson.student?.name ?: nextLesson.title,
                                            subject = nextLesson.student?.subject ?: "lesson",
                                            timeString = DateTimeUtils.formatTime(nextLesson.lesson.startEpochMillis),
                                            location = nextLesson.displayAreaOrLocation
                                        )
                                        WhatsAppHelper.openChat(context, phone, msg)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("next_lesson_whatsapp_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = DarsiSuccessGreenBg,
                                        contentColor = DarsiSuccessGreenDark
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Chat,
                                        contentDescription = "WhatsApp",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Button(
                                onClick = { onOpenLesson(nextLesson) },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("next_lesson_open_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarsiRoyalBlue
                                )
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Section Title: TODAY'S LESSONS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S LESSONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = DarsiNavySubtle
                )

                Text(
                    text = "${todayLessons.size} scheduled",
                    fontSize = 12.sp,
                    color = DarsiNavySubtle
                )
            }
        }

        // Vertical Timeline or Empty State
        if (todayLessons.isEmpty()) {
            item {
                DarsiCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = DarsiRoyalBlueSubtle,
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = DarsiRoyalBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No lessons today.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavyDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enjoy the free time — or schedule a lesson.",
                            fontSize = 13.sp,
                            color = DarsiNavyMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBookLesson,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                            modifier = Modifier.testTag("book_lesson_empty_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book Lesson", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            items(todayLessons, key = { it.lesson.id }) { item ->
                TodayLessonTimelineItem(
                    lesson = item,
                    onClick = { onOpenLesson(item) }
                )
            }
        }

        // Daily Summary Card (Clean, calm, pure white)
        item {
            DarsiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = DarsiNavySubtle
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Lessons Today
                        Column {
                            Text(
                                text = "Lessons today",
                                fontSize = 11.sp,
                                color = DarsiNavyMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${todaySummary.totalLessonsToday}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavyDark
                            )
                        }

                        // Payments received today (Soft green)
                        Column {
                            Text(
                                text = "Received today",
                                fontSize = 11.sp,
                                color = DarsiNavyMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyUtils.format(todaySummary.paymentsToday, currency),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiSuccessGreenDark
                            )
                        }

                        // Amount currently due (Restrained coral if overdue)
                        Column {
                            Text(
                                text = "Amount due",
                                fontSize = 11.sp,
                                color = DarsiNavyMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyUtils.format(todaySummary.totalDueAllStudents, currency),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todaySummary.totalDueAllStudents > 0) DarsiCoralRedDark else DarsiNavyDark
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar & FAB
        }
    }
}

@Composable
fun TodayLessonTimelineItem(
    lesson: LessonWithDetails,
    onClick: () -> Unit
) {
    DarsiCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_lesson_${lesson.lesson.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = DateTimeUtils.formatTime(lesson.lesson.startEpochMillis),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavyDark
                )
                Text(
                    text = "${lesson.lesson.durationMinutes}m",
                    fontSize = 11.sp,
                    color = DarsiNavySubtle
                )
            }

            // Vertical indicator accent (Soft green for completed, Amber for scheduled, Coral for cancelled)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (lesson.lesson.status) {
                            "COMPLETED" -> DarsiSuccessGreen
                            "CANCELLED_BY_STUDENT", "CANCELLED_BY_TUTOR" -> DarsiCoralRed
                            else -> DarsiAmber
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lesson.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarsiNavyDark
                    )
                    StatusBadge(status = lesson.lesson.status)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = lesson.subtitle,
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )

                val loc = lesson.displayAreaOrLocation
                if (loc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (lesson.isOnline) Icons.Default.Public else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DarsiNavySubtle,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = loc,
                            fontSize = 11.sp,
                            color = DarsiNavyMuted
                        )
                    }
                }

                if (lesson.lesson.topicCovered.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Topic: ${lesson.lesson.topicCovered}",
                        fontSize = 11.sp,
                        color = DarsiRoyalBlueDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
