package com.example.data.repository

import com.example.data.local.DarsiDatabase
import com.example.data.local.dao.GroupMemberWithStudent
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.GroupMemberEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.LessonExceptionEntity
import com.example.data.local.entity.LessonNoteEntity
import com.example.data.local.entity.LessonSeriesEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PaymentPlanEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TutorSettingsEntity
import com.example.domain.model.ConflictCheckResult
import com.example.domain.model.GroupDetails
import com.example.domain.model.LessonWithDetails
import com.example.domain.model.PaymentSummary
import com.example.domain.model.StudentWithBalance
import com.example.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DarsiRepository(private val database: DarsiDatabase) {
    val studentDao = database.studentDao()
    val groupDao = database.groupDao()
    val lessonDao = database.lessonDao()
    val lessonSeriesDao = database.lessonSeriesDao()
    val paymentDao = database.paymentDao()
    val packageDao = database.packageDao()
    val attendanceDao = database.attendanceDao()
    val tutorSettingsDao = database.tutorSettingsDao()
    val lessonExceptionDao = database.lessonExceptionDao()
    val lessonNoteDao = database.lessonNoteDao()
    val paymentPlanDao = database.paymentPlanDao()
    val subjectDao = database.subjectDao()

    // Modular Repositories abstractions
    val studentsRepository = StudentsRepository(studentDao)
    val groupsRepository = GroupsRepository(groupDao)
    val lessonsRepository = LessonsRepository(lessonDao)
    val lessonSeriesRepository = LessonSeriesRepository(lessonSeriesDao, lessonExceptionDao)
    val paymentsRepository = PaymentsRepository(paymentDao)
    val packagesRepository = PackagesRepository(packageDao)
    val notesRepository = NotesRepository(lessonNoteDao)
    val settingsRepository = SettingsRepository(tutorSettingsDao)

    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val activeStudents: Flow<List<StudentEntity>> = studentDao.getActiveStudents()
    val allGroups: Flow<List<GroupEntity>> = groupDao.getAllGroups()
    val tutorSettings: Flow<TutorSettingsEntity?> = tutorSettingsDao.getSettings()

    fun getStudentById(id: Long): Flow<StudentEntity?> = studentDao.getStudentById(id)

    fun getLessonsForDate(date: String): Flow<List<LessonWithDetails>> {
        return lessonDao.getLessonsForDate(date).map { lessons ->
            lessons.map { lesson ->
                val student = lesson.studentId?.let { studentDao.getStudentByIdDirect(it) }
                val group = lesson.groupId?.let { groupDao.getGroupByIdDirect(it) }
                val members = lesson.groupId?.let { groupDao.getGroupMembersDirect(it) } ?: emptyList()
                LessonWithDetails(lesson, student, group, members)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getLessonsBetweenEpochs(startEpoch: Long, endEpoch: Long): Flow<List<LessonWithDetails>> {
        return lessonDao.getLessonsBetweenEpochs(startEpoch, endEpoch).map { lessons ->
            lessons.map { lesson ->
                val student = lesson.studentId?.let { studentDao.getStudentByIdDirect(it) }
                val group = lesson.groupId?.let { groupDao.getGroupByIdDirect(it) }
                val members = lesson.groupId?.let { groupDao.getGroupMembersDirect(it) } ?: emptyList()
                LessonWithDetails(lesson, student, group, members)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getLessonsForStudent(studentId: Long): Flow<List<LessonWithDetails>> {
        return lessonDao.getLessonsForStudent(studentId).map { lessons ->
            lessons.map { lesson ->
                val student = studentDao.getStudentByIdDirect(studentId)
                LessonWithDetails(lesson, student, null, emptyList())
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPaymentsForStudent(studentId: Long): Flow<List<PaymentEntity>> {
        return paymentDao.getPaymentsForStudent(studentId)
    }

    fun getNotesForStudent(studentId: Long): Flow<List<LessonNoteEntity>> {
        return lessonNoteDao.getNotesForStudent(studentId)
    }

    fun getNextUpcomingLesson(): Flow<LessonWithDetails?> {
        val now = System.currentTimeMillis()
        return lessonDao.getNextUpcomingLesson(now).map { lesson ->
            if (lesson == null) null
            else {
                val student = lesson.studentId?.let { studentDao.getStudentByIdDirect(it) }
                val group = lesson.groupId?.let { groupDao.getGroupByIdDirect(it) }
                val members = lesson.groupId?.let { groupDao.getGroupMembersDirect(it) } ?: emptyList()
                LessonWithDetails(lesson, student, group, members)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getStudentsWithBalances(): Flow<List<StudentWithBalance>> {
        return combine(
            studentDao.getAllStudents(),
            paymentDao.getAllPayments(),
            lessonDao.getAllLessons(),
            packageDao.getAllActivePackages()
        ) { students, payments, lessons, packages ->
            val now = System.currentTimeMillis()
            students.map { student ->
                val studentLessons = lessons.filter { it.studentId == student.id }
                val studentPayments = payments.filter { it.studentId == student.id }
                val totalPaid = studentPayments.sumOf { it.amount }
                
                val completedCount = studentLessons.count { it.status == "COMPLETED" }
                val scheduledCount = studentLessons.count { it.status == "SCHEDULED" }
                
                val totalBilled = when (student.paymentType) {
                    "PACKAGE" -> {
                        packages.filter { it.studentId == student.id }.sumOf { it.price }
                    }
                    "MONTHLY" -> {
                        val monthsActive = maxOf(1, ((now - student.createdAt) / (1000L * 60 * 60 * 24 * 30)).toInt() + 1)
                        student.defaultPrice * monthsActive
                    }
                    else -> {
                        studentLessons.filter { it.status == "COMPLETED" }.sumOf { 
                            if (it.price > 0) it.price else student.defaultPrice 
                        }
                    }
                }

                val balanceDue = maxOf(0.0, totalBilled - totalPaid)
                val activePkg = packages.firstOrNull { it.studentId == student.id && it.status == "ACTIVE" }
                val nextLesson = studentLessons
                    .filter { it.startEpochMillis >= now && !it.status.startsWith("CANCELLED") }
                    .minByOrNull { it.startEpochMillis }

                StudentWithBalance(
                    student = student,
                    completedLessonsCount = completedCount,
                    scheduledLessonsCount = scheduledCount,
                    totalBilled = totalBilled,
                    totalPaid = totalPaid,
                    balanceDue = balanceDue,
                    activePackage = activePkg,
                    nextLesson = nextLesson
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getStudentDetails(studentId: Long): Flow<StudentWithBalance?> {
        return combine(
            studentDao.getStudentById(studentId),
            paymentDao.getPaymentsForStudent(studentId),
            lessonDao.getLessonsForStudent(studentId),
            packageDao.getActivePackageForStudent(studentId)
        ) { student, payments, lessons, activePackage ->
            if (student == null) null
            else {
                val now = System.currentTimeMillis()
                val totalPaid = payments.sumOf { it.amount }
                val completedCount = lessons.count { it.status == "COMPLETED" }
                val scheduledCount = lessons.count { it.status == "SCHEDULED" }

                val totalBilled = when (student.paymentType) {
                    "PACKAGE" -> activePackage?.price ?: 0.0
                    "MONTHLY" -> {
                        val monthsActive = maxOf(1, ((now - student.createdAt) / (1000L * 60 * 60 * 24 * 30)).toInt() + 1)
                        student.defaultPrice * monthsActive
                    }
                    else -> lessons.filter { it.status == "COMPLETED" }.sumOf { 
                        if (it.price > 0) it.price else student.defaultPrice 
                    }
                }

                val balanceDue = maxOf(0.0, totalBilled - totalPaid)
                val nextLesson = lessons
                    .filter { it.startEpochMillis >= now && !it.status.startsWith("CANCELLED") }
                    .minByOrNull { it.startEpochMillis }

                StudentWithBalance(
                    student = student,
                    completedLessonsCount = completedCount,
                    scheduledLessonsCount = scheduledCount,
                    totalBilled = totalBilled,
                    totalPaid = totalPaid,
                    balanceDue = balanceDue,
                    activePackage = activePackage,
                    nextLesson = nextLesson
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getGroupDetails(groupId: Long): Flow<GroupDetails?> {
        return combine(
            groupDao.getGroupById(groupId),
            groupDao.getGroupMembers(groupId),
            lessonDao.getLessonsForGroup(groupId)
        ) { group, members, lessons ->
            if (group == null) null
            else {
                val now = System.currentTimeMillis()
                val next = lessons.filter { it.startEpochMillis >= now && !it.status.startsWith("CANCELLED") }
                    .minByOrNull { it.startEpochMillis }
                val upcomingCount = lessons.count { it.startEpochMillis >= now && !it.status.startsWith("CANCELLED") }
                GroupDetails(group, members, next, upcomingCount)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPayments(): Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getTodaySummary(): Flow<PaymentSummary> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val dayEnd = cal.timeInMillis

        val monthCal = Calendar.getInstance()
        monthCal.set(Calendar.DAY_OF_MONTH, 1)
        monthCal.set(Calendar.HOUR_OF_DAY, 0)
        monthCal.set(Calendar.MINUTE, 0)
        val monthStart = monthCal.timeInMillis

        return combine(
            lessonDao.getLessonsBetweenEpochs(dayStart, dayEnd),
            paymentDao.getPaymentsBetween(dayStart, dayEnd),
            paymentDao.getTotalReceivedBetween(monthStart, dayEnd),
            getStudentsWithBalances()
        ) { todayLessons, todayPayments, monthTotal, studentsWithBalance ->
            val totalDue = studentsWithBalance.sumOf { it.balanceDue }
            val paidToday = todayPayments.sumOf { it.amount }
            PaymentSummary(
                totalReceivedThisMonth = monthTotal,
                totalDueAllStudents = totalDue,
                totalLessonsToday = todayLessons.size,
                paymentsToday = paidToday
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun checkForConflict(
        startEpoch: Long,
        endEpoch: Long,
        excludeLessonId: Long? = null,
        travelTimeMinutes: Int? = null
    ): ConflictCheckResult = withContext(Dispatchers.IO) {
        val existing = lessonDao.getLessonsBetweenEpochsDirect(startEpoch - 86400000L, endEpoch + 86400000L)
            .filter { it.id != excludeLessonId && !it.status.startsWith("CANCELLED") }

        // 1. Direct overlap check
        val directConflict = existing.firstOrNull { l ->
            l.startEpochMillis < endEpoch && l.endEpochMillis > startEpoch
        }
        if (directConflict != null) {
            val student = directConflict.studentId?.let { studentDao.getStudentByIdDirect(it) }
            val group = directConflict.groupId?.let { groupDao.getGroupByIdDirect(it) }
            val members = directConflict.groupId?.let { groupDao.getGroupMembersDirect(it) } ?: emptyList()
            val details = LessonWithDetails(directConflict, student, group, members)
            val startTimeStr = DateTimeUtils.formatTime(directConflict.startEpochMillis)
            val endTimeStr = DateTimeUtils.formatTime(directConflict.endEpochMillis)
            return@withContext ConflictCheckResult(
                hasConflict = true,
                isDirectOverlap = true,
                warningMessage = "You already have ${details.title} scheduled from $startTimeStr to $endTimeStr.",
                conflictingLesson = details
            )
        }

        // 2. Travel time check with preceding lesson
        val prevLesson = existing.filter { it.endEpochMillis <= startEpoch }
            .maxByOrNull { it.endEpochMillis }
        if (prevLesson != null) {
            val availableBeforeMin = (startEpoch - prevLesson.endEpochMillis) / (60 * 1000L)
            val neededTravel = travelTimeMinutes ?: prevLesson.travelTimeMinutes ?: 0
            if (neededTravel > 0 && availableBeforeMin < neededTravel) {
                val student = prevLesson.studentId?.let { studentDao.getStudentByIdDirect(it) }
                val group = prevLesson.groupId?.let { groupDao.getGroupByIdDirect(it) }
                val details = LessonWithDetails(prevLesson, student, group, emptyList())
                return@withContext ConflictCheckResult(
                    hasConflict = true,
                    isTravelTimeWarning = true,
                    warningMessage = "Only $availableBeforeMin minutes available after '${details.title}', but estimated travel time is $neededTravel minutes.",
                    conflictingLesson = details
                )
            }
        }

        // 3. Travel time check with following lesson
        val nextLesson = existing.filter { it.startEpochMillis >= endEpoch }
            .minByOrNull { it.startEpochMillis }
        if (nextLesson != null) {
            val availableAfterMin = (nextLesson.startEpochMillis - endEpoch) / (60 * 1000L)
            val neededTravel = nextLesson.travelTimeMinutes ?: travelTimeMinutes ?: 0
            if (neededTravel > 0 && availableAfterMin < neededTravel) {
                val student = nextLesson.studentId?.let { studentDao.getStudentByIdDirect(it) }
                val group = nextLesson.groupId?.let { groupDao.getGroupByIdDirect(it) }
                val details = LessonWithDetails(nextLesson, student, group, emptyList())
                return@withContext ConflictCheckResult(
                    hasConflict = true,
                    isTravelTimeWarning = true,
                    warningMessage = "Only $availableAfterMin minutes available before '${details.title}', but estimated travel time is $neededTravel minutes.",
                    conflictingLesson = details
                )
            }
        }

        ConflictCheckResult(hasConflict = false)
    }

    suspend fun saveStudent(student: StudentEntity): Long = withContext(Dispatchers.IO) {
        if (student.id == 0L) {
            studentDao.insertStudent(student)
        } else {
            studentDao.updateStudent(student)
            student.id
        }
    }

    suspend fun deleteStudent(id: Long) = withContext(Dispatchers.IO) {
        studentDao.deleteStudentById(id)
    }

    suspend fun saveGroup(group: GroupEntity): Long = withContext(Dispatchers.IO) {
        if (group.id == 0L) {
            groupDao.insertGroup(group)
        } else {
            groupDao.updateGroup(group)
            group.id
        }
    }

    suspend fun addMemberToGroup(groupId: Long, studentId: Long, priceOverride: Double? = null) = withContext(Dispatchers.IO) {
        groupDao.insertMember(GroupMemberEntity(groupId = groupId, studentId = studentId, priceOverride = priceOverride))
    }

    suspend fun removeMemberFromGroup(groupId: Long, studentId: Long) = withContext(Dispatchers.IO) {
        groupDao.removeMember(groupId, studentId)
    }

    suspend fun deleteGroup(id: Long) = withContext(Dispatchers.IO) {
        val group = groupDao.getGroupByIdDirect(id)
        if (group != null) groupDao.deleteGroup(group)
    }

    suspend fun bookLesson(
        studentId: Long?,
        groupId: Long?,
        dateString: String,
        hour: Int,
        minute: Int,
        durationMinutes: Int,
        price: Double,
        location: String,
        recurrenceOption: String,
        customWeekdays: List<Int> = emptyList(),
        weeksAhead: Int = 8,
        locationTypeOverride: String? = null,
        locationLabelOverride: String? = null,
        areaNameOverride: String? = null,
        addressTextOverride: String? = null,
        mapsLinkOverride: String? = null,
        latitudeOverride: Double? = null,
        longitudeOverride: Double? = null,
        travelTimeMinutesOverride: Int? = null,
        meetingUrlOverride: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val student = studentId?.let { studentDao.getStudentByIdDirect(it) }
        val group = groupId?.let { groupDao.getGroupByIdDirect(it) }

        val defaultLocType = when {
            student != null -> student.defaultLessonLocationType
            group != null -> group.defaultLocationType
            else -> "STUDENT_HOME"
        }
        val effLocType = locationTypeOverride ?: defaultLocType

        val effLocLabel = locationLabelOverride?.ifBlank { null } ?: when {
            effLocType == "ONLINE" -> "Online"
            student != null -> student.locationLabel
            group != null -> group.defaultLocationLabel
            else -> location
        }
        val effArea = areaNameOverride?.ifBlank { null } ?: student?.areaName ?: group?.areaName ?: ""
        val effAddress = addressTextOverride?.ifBlank { null } ?: student?.addressText ?: group?.addressText ?: ""
        val effMapsLink = mapsLinkOverride?.ifBlank { null } ?: student?.mapsLink ?: group?.mapsLink ?: ""
        val effLat = latitudeOverride ?: student?.latitude ?: group?.latitude
        val effLng = longitudeOverride ?: student?.longitude ?: group?.longitude
        val effTravel = if (effLocType == "ONLINE") null else (travelTimeMinutesOverride ?: student?.defaultTravelTimeMinutes ?: group?.defaultTravelTimeMinutes)
        val effMeeting = if (effLocType == "ONLINE") (meetingUrlOverride ?: "") else ""

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsedDate = sdf.parse(dateString) ?: Date()
        val cal = Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startEpoch = cal.timeInMillis
        val endEpoch = startEpoch + (durationMinutes * 60 * 1000L)

        if (recurrenceOption == "NEVER") {
            val single = LessonEntity(
                studentId = studentId,
                groupId = groupId,
                lessonDate = dateString,
                startEpochMillis = startEpoch,
                endEpochMillis = endEpoch,
                durationMinutes = durationMinutes,
                price = price,
                location = effLocLabel,
                locationType = effLocType,
                locationLabel = effLocLabel,
                areaName = effArea,
                addressText = effAddress,
                mapsLink = effMapsLink,
                latitude = effLat,
                longitude = effLng,
                travelTimeMinutes = effTravel,
                meetingUrl = effMeeting,
                status = "SCHEDULED"
            )
            lessonDao.insertLesson(single)
        } else {
            val mask = if (recurrenceOption == "CUSTOM") {
                customWeekdays.fold(0) { acc, day -> acc or (1 shl (day - 1)) }
            } else {
                1 shl (cal.get(Calendar.DAY_OF_WEEK) - 1)
            }

            val series = LessonSeriesEntity(
                studentId = studentId,
                groupId = groupId,
                startTimeString = String.format(Locale.US, "%02d:%02d", hour, minute),
                durationMinutes = durationMinutes,
                recurrenceType = recurrenceOption,
                daysOfWeekMask = mask,
                startDate = startEpoch
            )
            val seriesId = lessonSeriesDao.insertSeries(series)

            val lessonsToInsert = mutableListOf<LessonEntity>()
            val loopCal = Calendar.getInstance().apply {
                time = parsedDate
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val stepDays = if (recurrenceOption == "BIWEEKLY") 14 else 7

            if (recurrenceOption == "CUSTOM" && customWeekdays.isNotEmpty()) {
                val startDayCal = Calendar.getInstance().apply { time = parsedDate }
                for (w in 0 until weeksAhead) {
                    for (targetDay in customWeekdays) {
                        val occCal = Calendar.getInstance().apply {
                            time = startDayCal.time
                            add(Calendar.WEEK_OF_YEAR, w)
                            set(Calendar.DAY_OF_WEEK, targetDay)
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (occCal.timeInMillis >= startEpoch) {
                            val occStart = occCal.timeInMillis
                            val occEnd = occStart + (durationMinutes * 60 * 1000L)
                            val occDate = sdf.format(occCal.time)
                            lessonsToInsert.add(
                                LessonEntity(
                                    seriesId = seriesId,
                                    studentId = studentId,
                                    groupId = groupId,
                                    lessonDate = occDate,
                                    startEpochMillis = occStart,
                                    endEpochMillis = occEnd,
                                    durationMinutes = durationMinutes,
                                    price = price,
                                    location = effLocLabel,
                                    locationType = effLocType,
                                    locationLabel = effLocLabel,
                                    areaName = effArea,
                                    addressText = effAddress,
                                    mapsLink = effMapsLink,
                                    latitude = effLat,
                                    longitude = effLng,
                                    travelTimeMinutes = effTravel,
                                    meetingUrl = effMeeting,
                                    status = "SCHEDULED"
                                )
                            )
                        }
                    }
                }
            } else {
                for (i in 0 until (if (recurrenceOption == "BIWEEKLY") weeksAhead / 2 else weeksAhead)) {
                    val occStart = loopCal.timeInMillis
                    val occEnd = occStart + (durationMinutes * 60 * 1000L)
                    val occDate = sdf.format(loopCal.time)
                    lessonsToInsert.add(
                        LessonEntity(
                            seriesId = seriesId,
                            studentId = studentId,
                            groupId = groupId,
                            lessonDate = occDate,
                            startEpochMillis = occStart,
                            endEpochMillis = occEnd,
                            durationMinutes = durationMinutes,
                            price = price,
                            location = effLocLabel,
                            locationType = effLocType,
                            locationLabel = effLocLabel,
                            areaName = effArea,
                            addressText = effAddress,
                            mapsLink = effMapsLink,
                            latitude = effLat,
                            longitude = effLng,
                            travelTimeMinutes = effTravel,
                            meetingUrl = effMeeting,
                            status = "SCHEDULED"
                        )
                    )
                    loopCal.add(Calendar.DAY_OF_YEAR, stepDays)
                }
            }

            lessonDao.insertLessons(lessonsToInsert)
            lessonsToInsert.firstOrNull()?.id ?: seriesId
        }
    }

    suspend fun updateLessonStatus(lessonId: Long, status: String) = withContext(Dispatchers.IO) {
        val lesson = lessonDao.getLessonByIdDirect(lessonId)
        lessonDao.updateLessonStatus(lessonId, status)
        
        if (status == "COMPLETED" && lesson?.studentId != null) {
            packagesRepository.decrementLessonOnAttendance(lesson.studentId)
        }
    }

    suspend fun updateLessonNotes(lessonId: Long, topic: String, homework: String, note: String) = withContext(Dispatchers.IO) {
        lessonDao.updateLessonNotes(lessonId, topic, homework, note)
    }

    suspend fun rescheduleLesson(
        lessonId: Long,
        newDateStr: String,
        newHour: Int,
        newMinute: Int,
        durationMinutes: Int,
        applyToOption: Int // 0=This only, 1=This and following
    ) = withContext(Dispatchers.IO) {
        val lesson = lessonDao.getLessonByIdDirect(lessonId) ?: return@withContext
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsedDate = sdf.parse(newDateStr) ?: Date()
        val cal = Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, newHour)
            set(Calendar.MINUTE, newMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val newStart = cal.timeInMillis
        val newEnd = newStart + (durationMinutes * 60 * 1000L)

        if (applyToOption == 0 || lesson.seriesId == null) {
            // This occurrence only (mark as exception)
            lessonDao.updateLesson(
                lesson.copy(
                    lessonDate = newDateStr,
                    startEpochMillis = newStart,
                    endEpochMillis = newEnd,
                    durationMinutes = durationMinutes,
                    isException = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
            if (lesson.seriesId != null) {
                lessonExceptionDao.insertException(
                    LessonExceptionEntity(
                        seriesId = lesson.seriesId,
                        originalEpochMillis = lesson.startEpochMillis,
                        rescheduledStartMillis = newStart,
                        rescheduledEndMillis = newEnd,
                        reason = "Rescheduled single lesson"
                    )
                )
            }
        } else {
            // This and future lessons in series
            val diff = newStart - lesson.startEpochMillis
            val futureLessons = lessonDao.getLessonsBetweenEpochsDirect(lesson.startEpochMillis, Long.MAX_VALUE)
                .filter { it.seriesId == lesson.seriesId }

            futureLessons.forEach { l ->
                val shiftedStart = l.startEpochMillis + diff
                val shiftedEnd = shiftedStart + (durationMinutes * 60 * 1000L)
                val shiftedDate = sdf.format(Date(shiftedStart))
                lessonDao.updateLesson(
                    l.copy(
                        lessonDate = shiftedDate,
                        startEpochMillis = shiftedStart,
                        endEpochMillis = shiftedEnd,
                        durationMinutes = durationMinutes,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun deleteLesson(lessonId: Long, deleteOption: Int = 0) = withContext(Dispatchers.IO) {
        // deleteOption: 0 = This only, 1 = Future series, 2 = All in series
        val lesson = lessonDao.getLessonByIdDirect(lessonId) ?: return@withContext
        if (lesson.seriesId != null) {
            when (deleteOption) {
                1 -> lessonDao.deleteFutureLessonsForSeries(lesson.seriesId, lesson.startEpochMillis)
                2 -> {
                    lessonDao.deleteFutureLessonsForSeries(lesson.seriesId, 0L)
                    lessonSeriesDao.deleteSeriesById(lesson.seriesId)
                }
                else -> lessonDao.deleteLessonById(lessonId)
            }
        } else {
            lessonDao.deleteLessonById(lessonId)
        }
    }

    suspend fun recordPayment(payment: PaymentEntity): Long = withContext(Dispatchers.IO) {
        paymentDao.insertPayment(payment)
    }

    suspend fun createPackage(pkg: PackageEntity): Long = withContext(Dispatchers.IO) {
        packageDao.insertPackage(pkg)
    }

    suspend fun saveNote(note: LessonNoteEntity): Long = withContext(Dispatchers.IO) {
        if (note.id == 0L) {
            lessonNoteDao.insertNote(note)
        } else {
            lessonNoteDao.updateNote(note)
            note.id
        }
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        lessonNoteDao.deleteNoteById(id)
    }

    suspend fun saveSettings(settings: TutorSettingsEntity) = withContext(Dispatchers.IO) {
        tutorSettingsDao.saveSettings(settings)
    }

    suspend fun loadSampleGulfDemoData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date(now))

        saveSettings(
            TutorSettingsEntity(
                tutorName = "Mr. Tariq",
                subjects = "English, Mathematics",
                defaultDurationMinutes = 60,
                defaultCurrency = "QAR",
                isOnboardingCompleted = true
            )
        )

        // 1. Ahmed Ali (Grade 12, English, 300 QAR/lesson, Package 8 lessons)
        val s1Id = studentDao.insertStudent(
            StudentEntity(
                name = "Ahmed Ali",
                phone = "+97455123456",
                parentPhone = "+97455987654",
                school = "Qatar Science & Tech Academy",
                grade = "Grade 12",
                subject = "English",
                defaultPrice = 300.0,
                defaultDurationMinutes = 60,
                paymentType = "PACKAGE",
                privateNotes = "Working on IELTS prep and advanced essay writing. High aptitude.",
                locationLabel = "Student Home",
                areaName = "Al Waab",
                addressText = "Villa 21, Street 320",
                locationNotes = "Gate entrance is on the side street.",
                defaultLessonLocationType = "STUDENT_HOME",
                defaultTravelTimeMinutes = 25,
                mapsLink = "https://maps.google.com/?q=25.2638,51.4682",
                latitude = 25.2638,
                longitude = 51.4682
            )
        )

        packageDao.insertPackage(
            PackageEntity(
                studentId = s1Id,
                packageName = "IELTS Preparation (8 Lessons)",
                totalLessons = 8,
                usedLessons = 5,
                price = 2400.0,
                currency = "QAR",
                status = "ACTIVE"
            )
        )

        lessonNoteDao.insertNote(
            LessonNoteEntity(
                studentId = s1Id,
                title = "IELTS Task 2 Assessment",
                content = "Scored Band 7.5 in grammar range. Needs more complex transition words in argumentative conclusion.",
                category = "PROGRESS"
            )
        )

        // 2. Mohammed Hassan (Grade 11, English, 250 QAR/lesson, Per Lesson)
        val s2Id = studentDao.insertStudent(
            StudentEntity(
                name = "Mohammed Hassan",
                phone = "+97466234567",
                parentPhone = "+97466876543",
                school = "Al Jazeera Academy",
                grade = "Grade 11",
                subject = "English",
                defaultPrice = 250.0,
                defaultDurationMinutes = 60,
                paymentType = "PER_LESSON",
                privateNotes = "Needs focus on grammar and irregular verb tenses.",
                locationLabel = "Student Home",
                areaName = "West Bay",
                addressText = "Tower 4, Apt 1102, Diplomatic Area",
                locationNotes = "Visitor parking in basement level 1.",
                defaultLessonLocationType = "STUDENT_HOME",
                defaultTravelTimeMinutes = 20,
                mapsLink = "https://maps.google.com/?q=25.3215,51.5290",
                latitude = 25.3215,
                longitude = 51.5290
            )
        )

        lessonNoteDao.insertNote(
            LessonNoteEntity(
                studentId = s2Id,
                title = "Verb Tenses Review",
                content = "Reviewed past perfect continuous. Mohammed made good progress with irregular verbs worksheet.",
                category = "PROGRESS"
            )
        )

        // 3. Khalid Al-Kuwari (Grade 11, English, 250 QAR/lesson, Per Lesson)
        val s3Id = studentDao.insertStudent(
            StudentEntity(
                name = "Khalid Al-Kuwari",
                phone = "+97477345678",
                parentPhone = "+97477987654",
                school = "Doha College",
                grade = "Grade 11",
                subject = "English",
                defaultPrice = 250.0,
                defaultDurationMinutes = 60,
                paymentType = "PER_LESSON",
                privateNotes = "Excellent comprehension. Practicing vocabulary.",
                locationLabel = "Student Home",
                areaName = "Al Waab",
                addressText = "Zone 55, Street 810, Compound 4",
                defaultLessonLocationType = "STUDENT_HOME",
                defaultTravelTimeMinutes = 20
            )
        )

        // 4. Omar Mansoor (Grade 12, English, 1200 QAR/month, Monthly - Online)
        val s4Id = studentDao.insertStudent(
            StudentEntity(
                name = "Omar Mansoor",
                phone = "+97433456789",
                parentPhone = "+97433987654",
                school = "Debakey Health High School",
                grade = "Grade 12",
                subject = "English",
                defaultPrice = 1200.0,
                defaultDurationMinutes = 90,
                paymentType = "MONTHLY",
                privateNotes = "Monthly arrangement (4 lessons/month) conducted online.",
                locationLabel = "Online",
                areaName = "Online",
                defaultLessonLocationType = "ONLINE",
                locationNotes = "Remote lesson via Google Meet"
            )
        )

        // Group: Grade 12 Group A
        val g1Id = groupDao.insertGroup(
            GroupEntity(
                name = "Grade 12 Group A",
                subject = "English Literature",
                grade = "Grade 12",
                defaultPrice = 180.0,
                defaultDurationMinutes = 90,
                notes = "Advanced literature analysis & Shakespeare.",
                defaultLocationType = "TUTOR_LOCATION",
                defaultLocationLabel = "Study Room - The Pearl",
                areaName = "The Pearl",
                addressText = "Porto Arabia, Tower 22, Study Room B",
                defaultTravelTimeMinutes = 30,
                latitude = 25.3713,
                longitude = 51.5478
            )
        )

        groupDao.insertMember(GroupMemberEntity(groupId = g1Id, studentId = s1Id, priceOverride = 150.0))
        groupDao.insertMember(GroupMemberEntity(groupId = g1Id, studentId = s4Id, priceOverride = 180.0))

        // Create Lessons for Today!
        val cal1 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            if (timeInMillis < now) {
                timeInMillis = now + (42 * 60 * 1000L)
            }
        }
        val l1Start = cal1.timeInMillis
        val l1End = l1Start + (60 * 60 * 1000L)
        val l1Date = sdf.format(cal1.time)

        lessonDao.insertLesson(
            LessonEntity(
                studentId = s2Id,
                lessonDate = l1Date,
                startEpochMillis = l1Start,
                endEpochMillis = l1End,
                durationMinutes = 60,
                price = 250.0,
                location = "Student Home",
                locationType = "STUDENT_HOME",
                locationLabel = "Student Home",
                areaName = "West Bay",
                addressText = "Tower 4, Apt 1102, Diplomatic Area",
                travelTimeMinutes = 20,
                mapsLink = "https://maps.google.com/?q=25.3215,51.5290",
                latitude = 25.3215,
                longitude = 51.5290,
                topicCovered = "Present Perfect & Past Continuous",
                homework = "Workbook pages 34–35",
                privateTutorNote = "Review exercise 3 before starting new chapter.",
                status = "SCHEDULED"
            )
        )

        val cal2 = Calendar.getInstance().apply {
            timeInMillis = l1End + (30 * 60 * 1000L)
        }
        lessonDao.insertLesson(
            LessonEntity(
                groupId = g1Id,
                lessonDate = sdf.format(cal2.time),
                startEpochMillis = cal2.timeInMillis,
                endEpochMillis = cal2.timeInMillis + (90 * 60 * 1000L),
                durationMinutes = 90,
                price = 330.0,
                location = "Study Room - The Pearl",
                locationType = "TUTOR_LOCATION",
                locationLabel = "Study Room - The Pearl",
                areaName = "The Pearl",
                addressText = "Porto Arabia, Tower 22, Study Room B",
                travelTimeMinutes = 30,
                latitude = 25.3713,
                longitude = 51.5478,
                topicCovered = "Macbeth Act II Analysis",
                homework = "Essay outline on theme of ambition",
                status = "SCHEDULED"
            )
        )

        val cal3 = Calendar.getInstance().apply {
            timeInMillis = cal2.timeInMillis + (120 * 60 * 1000L)
        }
        lessonDao.insertLesson(
            LessonEntity(
                studentId = s3Id,
                lessonDate = sdf.format(cal3.time),
                startEpochMillis = cal3.timeInMillis,
                endEpochMillis = cal3.timeInMillis + (60 * 60 * 1000L),
                durationMinutes = 60,
                price = 250.0,
                location = "Student Home",
                locationType = "STUDENT_HOME",
                locationLabel = "Student Home",
                areaName = "Al Waab",
                addressText = "Zone 55, Street 810, Compound 4",
                travelTimeMinutes = 20,
                status = "SCHEDULED"
            )
        )

        // Past Completed lesson with payment for Ahmed
        val pastCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -2)
        }
        lessonDao.insertLesson(
            LessonEntity(
                studentId = s1Id,
                lessonDate = sdf.format(pastCal.time),
                startEpochMillis = pastCal.timeInMillis,
                endEpochMillis = pastCal.timeInMillis + (60 * 60 * 1000L),
                durationMinutes = 60,
                price = 300.0,
                status = "COMPLETED",
                topicCovered = "Academic Vocabulary & Writing Task 2",
                homework = "Write 250-word response to sample prompt #4"
            )
        )

        paymentDao.insertPayment(
            PaymentEntity(
                studentId = s1Id,
                amount = 2400.0,
                currency = "QAR",
                date = now - (5 * 24 * 60 * 60 * 1000L),
                paymentMethod = "BANK_TRANSFER",
                paymentFor = "PACKAGE",
                note = "Paid full package fee via CBQ transfer"
            )
        )

        paymentDao.insertPayment(
            PaymentEntity(
                studentId = s3Id,
                amount = 100.0,
                currency = "QAR",
                date = now - (3 * 24 * 60 * 60 * 1000L),
                paymentMethod = "CASH",
                paymentFor = "LESSON",
                note = "Partial cash deposit"
            )
        )
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }
}
