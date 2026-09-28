package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DarsiTab
import com.example.ui.MainViewModel
import com.example.ui.components.DarsiBottomBar
import com.example.ui.components.LessonDetailDialog
import com.example.ui.components.QuickAddBottomSheet
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.GroupDetailScreen
import com.example.ui.screens.MoreSettingsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                DarsiApp(
                    initialLessonId = intent.getLongExtra("OPEN_LESSON_ID", -1L).takeIf { it > 0 }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun DarsiApp(
    initialLessonId: Long? = null,
    viewModel: MainViewModel = viewModel()
) {
    val tutorSettings by viewModel.tutorSettings.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedStudentId by viewModel.selectedStudentId.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val selectedLessonForDetail by viewModel.selectedLessonForDetail.collectAsStateWithLifecycle()
    val isQuickAddOpen by viewModel.isQuickAddOpen.collectAsStateWithLifecycle()
    val quickAddTab by viewModel.quickAddTab.collectAsStateWithLifecycle()
    val quickAddInitialStudentId by viewModel.quickAddInitialStudentId.collectAsStateWithLifecycle()

    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val nextLesson by viewModel.nextUpcomingLesson.collectAsStateWithLifecycle()
    val todayLessons by viewModel.todayLessons.collectAsStateWithLifecycle()
    val studentsWithBalances by viewModel.studentsWithBalances.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val allGroups by viewModel.allGroups.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()

    val currentStudentDetails by viewModel.currentStudentDetails.collectAsStateWithLifecycle()
    val currentStudentLessons by viewModel.currentStudentLessons.collectAsStateWithLifecycle()
    val currentStudentPayments by viewModel.currentStudentPayments.collectAsStateWithLifecycle()
    val currentStudentNotes by viewModel.currentStudentNotes.collectAsStateWithLifecycle()
    val currentGroupDetails by viewModel.currentGroupDetails.collectAsStateWithLifecycle()

    val calendarDateEpoch by viewModel.calendarSelectedDateEpoch.collectAsStateWithLifecycle()
    val calendarLessons by viewModel.calendarSelectedLessons.collectAsStateWithLifecycle()

    // Request notification permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* handled */ }
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Open lesson if launched from widget or notification
    LaunchedEffect(initialLessonId) {
        if (initialLessonId != null) {
            viewModel.openLessonById(initialLessonId)
        }
    }

    val currency = tutorSettings?.defaultCurrency ?: "QAR"
    val tutorName = tutorSettings?.tutorName ?: "Teacher"
    val isOnboardingDone = tutorSettings?.isOnboardingCompleted == true
    val appLanguage = tutorSettings?.appLanguage ?: "en"
    val layoutDirection = if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    if (!isOnboardingDone) {
        OnboardingScreen(
            onComplete = { settings, loadDemo ->
                viewModel.saveSettings(settings)
                if (loadDemo) {
                    viewModel.loadSampleGulfDemoData()
                }
            }
        )
        return
    }

    // Handle Android system back press
    BackHandler(enabled = selectedStudentId != null || selectedGroupId != null) {
        if (selectedStudentId != null) {
            viewModel.closeStudent()
        } else if (selectedGroupId != null) {
            viewModel.closeGroup()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (selectedStudentId == null && selectedGroupId == null) {
                DarsiBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    language = appLanguage
                )
            }
        },
        floatingActionButton = {
            if (selectedStudentId == null && selectedGroupId == null) {
                FloatingActionButton(
                    onClick = { viewModel.openQuickAdd(0) },
                    shape = CircleShape,
                    containerColor = DarsiRoyalBlue,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.testTag("fab_quick_add")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Add",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedStudentId != null && currentStudentDetails != null -> {
                    StudentDetailScreen(
                        details = currentStudentDetails!!,
                        lessons = currentStudentLessons,
                        payments = currentStudentPayments,
                        notes = currentStudentNotes,
                        currency = currency,
                        tutorName = tutorName,
                        onBack = { viewModel.closeStudent() },
                        onRecordPayment = { sId ->
                            viewModel.openQuickAdd(3, sId)
                        },
                        onBookLesson = { sId ->
                            viewModel.openQuickAdd(0, sId)
                        },
                        onSaveStudent = { updatedStudent ->
                            viewModel.saveStudent(updatedStudent)
                        },
                        onSaveNotes = { sId, notes ->
                            val s = currentStudentDetails!!.student.copy(privateNotes = notes)
                            viewModel.saveStudent(s)
                        },
                        onAddProgressNote = { sId, title, content, category ->
                            viewModel.saveLessonNote(sId, title, content, category)
                        },
                        onDeleteProgressNote = { noteId ->
                            viewModel.deleteLessonNote(noteId)
                        },
                        onOpenLesson = { lwd ->
                            viewModel.openLessonDetail(lwd)
                        },
                        onCreatePackage = { pkg ->
                            viewModel.createPackage(pkg)
                        },
                        onDeleteStudent = { sId ->
                            viewModel.deleteStudent(sId)
                        }
                    )
                }

                selectedGroupId != null && currentGroupDetails != null -> {
                    GroupDetailScreen(
                        details = currentGroupDetails!!,
                        allStudents = allStudents,
                        currency = currency,
                        onBack = { viewModel.closeGroup() },
                        onAddMember = { sId, overridePrice ->
                            viewModel.addMemberToGroup(selectedGroupId!!, sId, overridePrice)
                        },
                        onRemoveMember = { sId ->
                            viewModel.removeMemberFromGroup(selectedGroupId!!, sId)
                        },
                        onBookGroupLesson = { gId ->
                            viewModel.openQuickAdd(0)
                        },
                        onDeleteGroup = { gId ->
                            viewModel.deleteGroup(gId)
                        },
                        onUpdateGroup = { g ->
                            viewModel.saveGroup(g, emptyList())
                        }
                    )
                }

                else -> {
                    when (currentTab) {
                        DarsiTab.TODAY -> {
                            TodayScreen(
                                tutorName = tutorName,
                                currency = currency,
                                nextLesson = nextLesson,
                                todayLessons = todayLessons,
                                todaySummary = todaySummary,
                                travelBufferMinutes = tutorSettings?.defaultTravelBufferMinutes ?: 10,
                                language = appLanguage,
                                onOpenLesson = { viewModel.openLessonDetail(it) },
                                onBookLesson = { viewModel.openQuickAdd(0) }
                            )
                        }

                        DarsiTab.CALENDAR -> {
                            CalendarScreen(
                                selectedDateEpoch = calendarDateEpoch,
                                lessonsForSelectedDate = calendarLessons,
                                onSelectDate = { viewModel.setCalendarDate(it) },
                                onOpenLesson = { viewModel.openLessonDetail(it) },
                                onBookLessonForDate = { dateStr ->
                                    viewModel.openQuickAdd(0)
                                },
                                language = appLanguage
                            )
                        }

                        DarsiTab.STUDENTS -> {
                            StudentsScreen(
                                students = studentsWithBalances,
                                groups = allGroups,
                                currency = currency,
                                onOpenStudent = { viewModel.openStudent(it) },
                                onOpenGroup = { viewModel.openGroup(it) },
                                onAddStudent = { viewModel.openQuickAdd(1) },
                                onAddGroup = { viewModel.openQuickAdd(2) },
                                language = appLanguage
                            )
                        }

                        DarsiTab.PAYMENTS -> {
                            PaymentsScreen(
                                students = studentsWithBalances,
                                payments = allPayments,
                                summary = todaySummary,
                                currency = currency,
                                onRecordPayment = { viewModel.openQuickAdd(3) },
                                onOpenStudent = { viewModel.openStudent(it) },
                                language = appLanguage
                            )
                        }

                        DarsiTab.MORE -> {
                            MoreSettingsScreen(
                                settings = tutorSettings,
                                onSaveSettings = { viewModel.saveSettings(it) },
                                onLoadDemoData = { viewModel.loadSampleGulfDemoData() },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }

            // Quick Add Bottom Sheet
            if (isQuickAddOpen) {
                QuickAddBottomSheet(
                    initialTab = quickAddTab,
                    students = allStudents,
                    groups = allGroups,
                    currency = currency,
                    initialStudentId = quickAddInitialStudentId,
                    onCheckConflict = { start, end, travelMin ->
                        viewModel.checkConflict(start, end, travelTimeMinutes = travelMin)
                    },
                    onDismiss = { viewModel.closeQuickAdd() },
                    onBookLesson = { sId, gId, dateStr, hr, min, dur, pr, loc, rec, days, locType, locLbl, area, addr, maps, travel, meet ->
                        viewModel.bookLesson(
                            studentId = sId,
                            groupId = gId,
                            dateString = dateStr,
                            hour = hr,
                            minute = min,
                            durationMinutes = dur,
                            price = pr,
                            location = loc,
                            recurrenceOption = rec,
                            customDays = days,
                            locationTypeOverride = locType,
                            locationLabelOverride = locLbl,
                            areaNameOverride = area,
                            addressTextOverride = addr,
                            mapsLinkOverride = maps,
                            travelTimeMinutesOverride = travel,
                            meetingUrlOverride = meet
                        )
                    },
                    onAddStudent = { s ->
                        viewModel.saveStudent(s)
                    },
                    onAddGroup = { g, memberIds ->
                        viewModel.saveGroup(g, memberIds)
                    },
                    onRecordPayment = { p ->
                        viewModel.recordPayment(p)
                    }
                )
            }

            // Lesson Detail Dialog
            if (selectedLessonForDetail != null) {
                LessonDetailDialog(
                    lessonDetails = selectedLessonForDetail!!,
                    currency = currency,
                    onDismiss = { viewModel.closeLessonDetail() },
                    onUpdateStatus = { lId, status ->
                        viewModel.updateLessonStatus(lId, status)
                    },
                    onUpdateNotes = { lId, topic, hw, note ->
                        viewModel.updateLessonNotes(lId, topic, hw, note)
                    },
                    onRescheduleLesson = { lId, newDate, hr, min, dur, opt ->
                        viewModel.rescheduleLesson(lId, newDate, hr, min, dur, opt)
                    },
                    onDeleteLesson = { lId, deleteOption ->
                        viewModel.deleteLesson(lId, deleteOption)
                    },
                    onOpenPaymentForStudent = { sId ->
                        viewModel.closeLessonDetail()
                        viewModel.openStudent(sId)
                    }
                )
            }
        }
    }
    }
}
