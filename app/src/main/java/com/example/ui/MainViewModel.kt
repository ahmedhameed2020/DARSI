package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DarsiDatabase
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.LessonNoteEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TutorSettingsEntity
import com.example.data.repository.DarsiRepository
import com.example.domain.model.ConflictCheckResult
import com.example.domain.model.GroupDetails
import com.example.domain.model.LessonWithDetails
import com.example.domain.model.PaymentSummary
import com.example.domain.model.StudentWithBalance
import com.example.util.DateTimeUtils
import com.example.widget.DarsiWidgetHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DarsiTab {
    TODAY,
    CALENDAR,
    STUDENTS,
    PAYMENTS,
    MORE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DarsiDatabase.getInstance(application)
    val repository = DarsiRepository(database)

    private val _currentTab = MutableStateFlow(DarsiTab.TODAY)
    val currentTab: StateFlow<DarsiTab> = _currentTab.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId.asStateFlow()

    private val _selectedLessonForDetail = MutableStateFlow<LessonWithDetails?>(null)
    val selectedLessonForDetail: StateFlow<LessonWithDetails?> = _selectedLessonForDetail.asStateFlow()

    private val _isQuickAddOpen = MutableStateFlow(false)
    val isQuickAddOpen: StateFlow<Boolean> = _isQuickAddOpen.asStateFlow()

    private val _quickAddTab = MutableStateFlow(0)
    val quickAddTab: StateFlow<Int> = _quickAddTab.asStateFlow()

    private val _quickAddInitialStudentId = MutableStateFlow<Long?>(null)
    val quickAddInitialStudentId: StateFlow<Long?> = _quickAddInitialStudentId.asStateFlow()

    private val _calendarSelectedDateEpoch = MutableStateFlow(System.currentTimeMillis())
    val calendarSelectedDateEpoch: StateFlow<Long> = _calendarSelectedDateEpoch.asStateFlow()

    val tutorSettings: StateFlow<TutorSettingsEntity?> = repository.tutorSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todaySummary: StateFlow<PaymentSummary> = repository.getTodaySummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentSummary())

    val nextUpcomingLesson: StateFlow<LessonWithDetails?> = repository.getNextUpcomingLesson()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayLessons: StateFlow<List<LessonWithDetails>> = repository.getLessonsForDate(DateTimeUtils.formatTodayDateString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentsWithBalances: StateFlow<List<StudentWithBalance>> = repository.getStudentsWithBalances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGroups: StateFlow<List<GroupEntity>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.getPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Details for selected student
    val currentStudentDetails: StateFlow<StudentWithBalance?> = _selectedStudentId
        .flatMapLatest { id ->
            if (id != null) repository.getStudentDetails(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentStudentLessons: StateFlow<List<LessonWithDetails>> = _selectedStudentId
        .flatMapLatest { id ->
            if (id != null) repository.getLessonsForStudent(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStudentPayments: StateFlow<List<PaymentEntity>> = _selectedStudentId
        .flatMapLatest { id ->
            if (id != null) repository.getPaymentsForStudent(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStudentNotes: StateFlow<List<LessonNoteEntity>> = _selectedStudentId
        .flatMapLatest { id ->
            if (id != null) repository.getNotesForStudent(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Details for selected group
    val currentGroupDetails: StateFlow<GroupDetails?> = _selectedGroupId
        .flatMapLatest { id ->
            if (id != null) repository.getGroupDetails(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Calendar lessons for selected day
    val calendarSelectedLessons: StateFlow<List<LessonWithDetails>> = _calendarSelectedDateEpoch
        .flatMapLatest { epoch ->
            val dateStr = DateTimeUtils.formatDate(epoch)
            repository.getLessonsForDate(dateStr)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: DarsiTab) {
        _currentTab.value = tab
        _selectedStudentId.value = null
        _selectedGroupId.value = null
    }

    fun openStudent(studentId: Long) {
        _selectedStudentId.value = studentId
    }

    fun closeStudent() {
        _selectedStudentId.value = null
    }

    fun openGroup(groupId: Long) {
        _selectedGroupId.value = groupId
    }

    fun closeGroup() {
        _selectedGroupId.value = null
    }

    fun openLessonDetail(lesson: LessonWithDetails) {
        _selectedLessonForDetail.value = lesson
    }

    fun openLessonById(lessonId: Long) {
        viewModelScope.launch {
            val lesson = database.lessonDao().getLessonByIdDirect(lessonId)
            if (lesson != null) {
                val student = lesson.studentId?.let { database.studentDao().getStudentByIdDirect(it) }
                val group = lesson.groupId?.let { database.groupDao().getGroupByIdDirect(it) }
                val members = lesson.groupId?.let { database.groupDao().getGroupMembersDirect(it) } ?: emptyList()
                _selectedLessonForDetail.value = LessonWithDetails(lesson, student, group, members)
            }
        }
    }

    fun closeLessonDetail() {
        _selectedLessonForDetail.value = null
    }

    fun openQuickAdd(initialTab: Int = 0, initialStudentId: Long? = null) {
        _quickAddTab.value = initialTab
        _quickAddInitialStudentId.value = initialStudentId
        _isQuickAddOpen.value = true
    }

    fun closeQuickAdd() {
        _isQuickAddOpen.value = false
        _quickAddInitialStudentId.value = null
    }

    fun setCalendarDate(epoch: Long) {
        _calendarSelectedDateEpoch.value = epoch
    }

    fun updateLessonStatus(lessonId: Long, status: String) {
        viewModelScope.launch {
            repository.updateLessonStatus(lessonId, status)
            refreshWidget()
            val current = _selectedLessonForDetail.value
            if (current?.lesson?.id == lessonId) {
                _selectedLessonForDetail.value = current.copy(
                    lesson = current.lesson.copy(status = status)
                )
            }
        }
    }

    fun updateLessonNotes(lessonId: Long, topic: String, homework: String, privateNote: String) {
        viewModelScope.launch {
            repository.updateLessonNotes(lessonId, topic, homework, privateNote)
            val current = _selectedLessonForDetail.value
            if (current?.lesson?.id == lessonId) {
                _selectedLessonForDetail.value = current.copy(
                    lesson = current.lesson.copy(
                        topicCovered = topic,
                        homework = homework,
                        privateTutorNote = privateNote
                    )
                )
            }
        }
    }

    fun rescheduleLesson(
        lessonId: Long,
        newDateStr: String,
        newHour: Int,
        newMinute: Int,
        durationMinutes: Int,
        applyToOption: Int,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.rescheduleLesson(lessonId, newDateStr, newHour, newMinute, durationMinutes, applyToOption)
            refreshWidget()
            val updated = database.lessonDao().getLessonByIdDirect(lessonId)
            if (updated != null && _selectedLessonForDetail.value?.lesson?.id == lessonId) {
                val student = updated.studentId?.let { database.studentDao().getStudentByIdDirect(it) }
                val group = updated.groupId?.let { database.groupDao().getGroupByIdDirect(it) }
                val members = updated.groupId?.let { database.groupDao().getGroupMembersDirect(it) } ?: emptyList()
                _selectedLessonForDetail.value = LessonWithDetails(updated, student, group, members)
            }
            onComplete?.invoke()
        }
    }

    fun deleteLesson(lessonId: Long, deleteOption: Int = 0) {
        viewModelScope.launch {
            repository.deleteLesson(lessonId, deleteOption)
            closeLessonDetail()
            refreshWidget()
        }
    }

    fun saveStudent(student: StudentEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveStudent(student)
            onComplete?.invoke(id)
        }
    }

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            closeStudent()
            refreshWidget()
        }
    }

    fun saveGroup(group: GroupEntity, memberIds: List<Long>, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val groupId = repository.saveGroup(group)
            memberIds.forEach { sId ->
                repository.addMemberToGroup(groupId, sId)
            }
            onComplete?.invoke(groupId)
        }
    }

    fun addMemberToGroup(groupId: Long, studentId: Long, priceOverride: Double? = null) {
        viewModelScope.launch {
            repository.addMemberToGroup(groupId, studentId, priceOverride)
        }
    }

    fun removeMemberFromGroup(groupId: Long, studentId: Long) {
        viewModelScope.launch {
            repository.removeMemberFromGroup(groupId, studentId)
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
            closeGroup()
            refreshWidget()
        }
    }

    fun recordPayment(payment: PaymentEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.recordPayment(payment)
            onComplete?.invoke()
        }
    }

    fun createPackage(pkg: PackageEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.createPackage(pkg)
            onComplete?.invoke()
        }
    }

    suspend fun checkConflict(
        startEpoch: Long,
        endEpoch: Long,
        excludeLessonId: Long? = null,
        travelTimeMinutes: Int? = null
    ): ConflictCheckResult {
        return repository.checkForConflict(startEpoch, endEpoch, excludeLessonId, travelTimeMinutes)
    }

    fun bookLesson(
        studentId: Long?,
        groupId: Long?,
        dateString: String,
        hour: Int,
        minute: Int,
        durationMinutes: Int,
        price: Double,
        location: String,
        recurrenceOption: String,
        customDays: List<Int> = emptyList(),
        locationTypeOverride: String? = null,
        locationLabelOverride: String? = null,
        areaNameOverride: String? = null,
        addressTextOverride: String? = null,
        mapsLinkOverride: String? = null,
        latitudeOverride: Double? = null,
        longitudeOverride: Double? = null,
        travelTimeMinutesOverride: Int? = null,
        meetingUrlOverride: String? = null,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.bookLesson(
                studentId = studentId,
                groupId = groupId,
                dateString = dateString,
                hour = hour,
                minute = minute,
                durationMinutes = durationMinutes,
                price = price,
                location = location,
                recurrenceOption = recurrenceOption,
                customWeekdays = customDays,
                locationTypeOverride = locationTypeOverride,
                locationLabelOverride = locationLabelOverride,
                areaNameOverride = areaNameOverride,
                addressTextOverride = addressTextOverride,
                mapsLinkOverride = mapsLinkOverride,
                latitudeOverride = latitudeOverride,
                longitudeOverride = longitudeOverride,
                travelTimeMinutesOverride = travelTimeMinutesOverride,
                meetingUrlOverride = meetingUrlOverride
            )
            refreshWidget()
            onComplete?.invoke()
        }
    }

    fun saveLessonNote(
        studentId: Long,
        title: String,
        content: String,
        category: String = "PROGRESS",
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.saveNote(
                LessonNoteEntity(
                    studentId = studentId,
                    title = title,
                    content = content,
                    category = category
                )
            )
            onComplete?.invoke()
        }
    }

    fun deleteLessonNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun saveSettings(settings: TutorSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    fun loadSampleGulfDemoData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.loadSampleGulfDemoData()
            refreshWidget()
            onComplete?.invoke()
        }
    }

    fun clearAllData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAllData()
            refreshWidget()
            onComplete?.invoke()
        }
    }

    private fun refreshWidget() {
        DarsiWidgetHelper.updateWidget(getApplication())
    }
}
