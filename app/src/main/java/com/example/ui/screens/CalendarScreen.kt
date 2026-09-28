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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LessonWithDetails
import com.example.ui.components.DarsiCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DarsiBorder
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyLight
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    selectedDateEpoch: Long,
    lessonsForSelectedDate: List<LessonWithDetails>,
    onSelectDate: (Long) -> Unit,
    onOpenLesson: (LessonWithDetails) -> Unit,
    onBookLessonForDate: (String) -> Unit,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    var calendarViewIndex by remember { mutableIntStateOf(1) } // 0=Day, 1=Week (default), 2=Month
    val isArabic = language == "ar"
    val viewModes = if (isArabic) listOf("يوم", "أسبوع", "شهر") else listOf("Day", "Week", "Month")

    val selectedDate = remember(selectedDateEpoch) { Date(selectedDateEpoch) }
    val weekDays = remember(selectedDateEpoch) { DateTimeUtils.getDaysOfWeekForDate(selectedDate) }
    val displayLocale = if (isArabic) Locale("ar") else Locale.US
    val dayNameFormat = remember(language) { SimpleDateFormat("EEE", displayLocale) }
    val dayNumFormat = remember(language) { SimpleDateFormat("d", displayLocale) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // View Mode Selector: Day / Week / Month
        Surface(
            color = DarsiSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                viewModes.forEachIndexed { index, mode ->
                    val isSelected = (calendarViewIndex == index)
                    Surface(
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { calendarViewIndex = index }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = mode,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) DarsiRoyalBlue else DarsiNavyMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DateTimeUtils.formatMonthYear(selectedDateEpoch),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Text(
                    text = DateTimeUtils.formatReadableDate(selectedDateEpoch),
                    fontSize = 12.sp,
                    color = DarsiNavySubtle
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Today button
                FilledTonalButton(
                    onClick = { onSelectDate(System.currentTimeMillis()) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(if (isArabic) "اليوم" else "Today", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            time = selectedDate
                            val delta = if (calendarViewIndex == 0) -1 else if (calendarViewIndex == 1) -7 else -30
                            add(Calendar.DAY_OF_YEAR, delta)
                        }
                        onSelectDate(cal.timeInMillis)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = DarsiNavy
                    )
                }

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            time = selectedDate
                            val delta = if (calendarViewIndex == 0) 1 else if (calendarViewIndex == 1) 7 else 30
                            add(Calendar.DAY_OF_YEAR, delta)
                        }
                        onSelectDate(cal.timeInMillis)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = DarsiNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Week Horizontal Strip (Sun - Sat)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(weekDays) { (date, isToday) ->
                val isSelectedDay = isSameDay(date, selectedDate)
                val dayName = dayNameFormat.format(date)
                val dayNum = dayNumFormat.format(date)

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelectedDay) DarsiRoyalBlue
                            else if (isToday) DarsiRoyalBlueSubtle
                            else Color.Transparent
                        )
                        .clickable { onSelectDate(date.time) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelectedDay) Color.White.copy(alpha = 0.8f) else DarsiNavySubtle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNum,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelectedDay) Color.White else DarsiNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isToday && !isSelectedDay) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(DarsiRoyalBlue)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Schedule list for Selected Day
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (lessonsForSelectedDate.isEmpty()) {
                item {
                    DarsiCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No lessons on this day",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap below to schedule a lesson for this date.",
                                fontSize = 12.sp,
                                color = DarsiNavyMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    val dateStr = DateTimeUtils.formatDate(selectedDateEpoch)
                                    onBookLessonForDate(dateStr)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Schedule Lesson", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                items(lessonsForSelectedDate, key = { it.lesson.id }) { item ->
                    TodayLessonTimelineItem(
                        lesson = item,
                        onClick = { onOpenLesson(item) }
                    )
                }
            }

            // Quick add slot at bottom of calendar
            item {
                OutlinedButton(
                    onClick = {
                        val dateStr = DateTimeUtils.formatDate(selectedDateEpoch)
                        onBookLessonForDate(dateStr)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = DarsiRoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add another lesson on ${dayNumFormat.format(selectedDate)} ${dayNameFormat.format(selectedDate)}",
                        fontSize = 13.sp,
                        color = DarsiRoyalBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

private fun isSameDay(d1: Date, d2: Date): Boolean {
    val c1 = Calendar.getInstance().apply { time = d1 }
    val c2 = Calendar.getInstance().apply { time = d2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}
