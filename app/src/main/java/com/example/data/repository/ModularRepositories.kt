package com.example.data.repository

import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.GroupDao
import com.example.data.local.dao.GroupMemberWithStudent
import com.example.data.local.dao.LessonDao
import com.example.data.local.dao.LessonExceptionDao
import com.example.data.local.dao.LessonNoteDao
import com.example.data.local.dao.LessonSeriesDao
import com.example.data.local.dao.PackageDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.PaymentPlanDao
import com.example.data.local.dao.StudentDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.TutorSettingsDao
import com.example.data.local.entity.AttendanceEntity
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
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TutorSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudentsRepository(private val studentDao: StudentDao) {
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val activeStudents: Flow<List<StudentEntity>> = studentDao.getActiveStudents()

    fun getStudentById(id: Long): Flow<StudentEntity?> = studentDao.getStudentById(id)
    suspend fun getStudentByIdDirect(id: Long): StudentEntity? = withContext(Dispatchers.IO) {
        studentDao.getStudentByIdDirect(id)
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
}

class GroupsRepository(private val groupDao: GroupDao) {
    val allGroups: Flow<List<GroupEntity>> = groupDao.getAllGroups()

    fun getGroupById(id: Long): Flow<GroupEntity?> = groupDao.getGroupById(id)
    suspend fun getGroupByIdDirect(id: Long): GroupEntity? = withContext(Dispatchers.IO) {
        groupDao.getGroupByIdDirect(id)
    }

    fun getGroupMembers(groupId: Long): Flow<List<GroupMemberWithStudent>> = groupDao.getGroupMembers(groupId)
    suspend fun getGroupMembersDirect(groupId: Long): List<GroupMemberWithStudent> = withContext(Dispatchers.IO) {
        groupDao.getGroupMembersDirect(groupId)
    }

    suspend fun saveGroup(group: GroupEntity): Long = withContext(Dispatchers.IO) {
        if (group.id == 0L) {
            groupDao.insertGroup(group)
        } else {
            groupDao.updateGroup(group)
            group.id
        }
    }

    suspend fun addMember(groupId: Long, studentId: Long, priceOverride: Double? = null) = withContext(Dispatchers.IO) {
        groupDao.insertMember(GroupMemberEntity(groupId = groupId, studentId = studentId, priceOverride = priceOverride))
    }

    suspend fun removeMember(groupId: Long, studentId: Long) = withContext(Dispatchers.IO) {
        groupDao.removeMember(groupId, studentId)
    }

    suspend fun deleteGroup(id: Long) = withContext(Dispatchers.IO) {
        val g = groupDao.getGroupByIdDirect(id)
        if (g != null) groupDao.deleteGroup(g)
    }
}

class LessonsRepository(private val lessonDao: LessonDao) {
    val allLessons: Flow<List<LessonEntity>> = lessonDao.getAllLessons()

    fun getLessonsForDate(date: String): Flow<List<LessonEntity>> = lessonDao.getLessonsForDate(date)
    fun getLessonsBetweenEpochs(start: Long, end: Long): Flow<List<LessonEntity>> = lessonDao.getLessonsBetweenEpochs(start, end)
    fun getLessonsForStudent(studentId: Long): Flow<List<LessonEntity>> = lessonDao.getLessonsForStudent(studentId)
    fun getLessonsForGroup(groupId: Long): Flow<List<LessonEntity>> = lessonDao.getLessonsForGroup(groupId)
    fun getLessonById(id: Long): Flow<LessonEntity?> = lessonDao.getLessonById(id)
    fun getNextUpcomingLesson(now: Long): Flow<LessonEntity?> = lessonDao.getNextUpcomingLesson(now)

    suspend fun insertLesson(lesson: LessonEntity): Long = withContext(Dispatchers.IO) {
        lessonDao.insertLesson(lesson)
    }

    suspend fun insertLessons(lessons: List<LessonEntity>) = withContext(Dispatchers.IO) {
        lessonDao.insertLessons(lessons)
    }

    suspend fun updateLesson(lesson: LessonEntity) = withContext(Dispatchers.IO) {
        lessonDao.updateLesson(lesson)
    }

    suspend fun updateLessonStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        lessonDao.updateLessonStatus(id, status)
    }

    suspend fun updateLessonNotes(id: Long, topic: String, homework: String, note: String) = withContext(Dispatchers.IO) {
        lessonDao.updateLessonNotes(id, topic, homework, note)
    }

    suspend fun deleteLesson(id: Long) = withContext(Dispatchers.IO) {
        lessonDao.deleteLessonById(id)
    }

    suspend fun deleteFutureLessonsForSeries(seriesId: Long, fromEpoch: Long) = withContext(Dispatchers.IO) {
        lessonDao.deleteFutureLessonsForSeries(seriesId, fromEpoch)
    }

    suspend fun checkForConflict(startEpoch: Long, endEpoch: Long, excludeLessonId: Long? = null): LessonEntity? = withContext(Dispatchers.IO) {
        val existing = lessonDao.getLessonsBetweenEpochsDirect(startEpoch - 86400000L, endEpoch + 86400000L)
        existing.firstOrNull { l ->
            l.id != excludeLessonId &&
            !l.status.startsWith("CANCELLED") &&
            l.startEpochMillis < endEpoch &&
            l.endEpochMillis > startEpoch
        }
    }
}

class LessonSeriesRepository(
    private val lessonSeriesDao: LessonSeriesDao,
    private val lessonExceptionDao: LessonExceptionDao
) {
    val allSeries: Flow<List<LessonSeriesEntity>> = lessonSeriesDao.getAllSeries()

    fun getSeriesForStudent(studentId: Long): Flow<List<LessonSeriesEntity>> = lessonSeriesDao.getSeriesForStudent(studentId)
    fun getSeriesForGroup(groupId: Long): Flow<List<LessonSeriesEntity>> = lessonSeriesDao.getSeriesForGroup(groupId)

    suspend fun createSeries(series: LessonSeriesEntity): Long = withContext(Dispatchers.IO) {
        lessonSeriesDao.insertSeries(series)
    }

    suspend fun updateSeries(series: LessonSeriesEntity) = withContext(Dispatchers.IO) {
        lessonSeriesDao.updateSeries(series)
    }

    suspend fun deleteSeries(id: Long) = withContext(Dispatchers.IO) {
        lessonSeriesDao.deleteSeriesById(id)
    }

    suspend fun recordException(exception: LessonExceptionEntity): Long = withContext(Dispatchers.IO) {
        lessonExceptionDao.insertException(exception)
    }
}

class PaymentsRepository(private val paymentDao: PaymentDao) {
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getPaymentsForStudent(studentId: Long): Flow<List<PaymentEntity>> = paymentDao.getPaymentsForStudent(studentId)
    fun getPaymentsBetween(start: Long, end: Long): Flow<List<PaymentEntity>> = paymentDao.getPaymentsBetween(start, end)
    fun getTotalReceivedBetween(start: Long, end: Long): Flow<Double> = paymentDao.getTotalReceivedBetween(start, end)

    suspend fun insertPayment(payment: PaymentEntity): Long = withContext(Dispatchers.IO) {
        paymentDao.insertPayment(payment)
    }

    suspend fun deletePayment(id: Long): Unit = withContext(Dispatchers.IO) {
        paymentDao.deletePaymentById(id)
    }
}

class PackagesRepository(private val packageDao: PackageDao) {
    val allActivePackages: Flow<List<PackageEntity>> = packageDao.getAllActivePackages()

    fun getPackagesForStudent(studentId: Long): Flow<List<PackageEntity>> = packageDao.getPackagesForStudent(studentId)
    fun getActivePackageForStudent(studentId: Long): Flow<PackageEntity?> = packageDao.getActivePackageForStudent(studentId)

    suspend fun insertPackage(pkg: PackageEntity): Long = withContext(Dispatchers.IO) {
        packageDao.insertPackage(pkg)
    }

    suspend fun updatePackage(pkg: PackageEntity) = withContext(Dispatchers.IO) {
        packageDao.updatePackage(pkg)
    }

    suspend fun decrementLessonOnAttendance(studentId: Long) = withContext(Dispatchers.IO) {
        val pkg = packageDao.getActivePackageForStudentDirect(studentId)
        if (pkg != null && pkg.usedLessons < pkg.totalLessons) {
            val newUsed = pkg.usedLessons + 1
            val newStatus = if (newUsed >= pkg.totalLessons) "COMPLETED" else "ACTIVE"
            packageDao.updatePackage(pkg.copy(usedLessons = newUsed, status = newStatus))
        }
    }
}

class NotesRepository(private val lessonNoteDao: LessonNoteDao) {
    fun getNotesForStudent(studentId: Long): Flow<List<LessonNoteEntity>> = lessonNoteDao.getNotesForStudent(studentId)
    fun getNoteForLesson(lessonId: Long): Flow<LessonNoteEntity?> = lessonNoteDao.getNoteForLesson(lessonId)

    suspend fun addNote(note: LessonNoteEntity): Long = withContext(Dispatchers.IO) {
        lessonNoteDao.insertNote(note)
    }

    suspend fun updateNote(note: LessonNoteEntity) = withContext(Dispatchers.IO) {
        lessonNoteDao.updateNote(note)
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        lessonNoteDao.deleteNoteById(id)
    }
}

class SettingsRepository(private val tutorSettingsDao: TutorSettingsDao) {
    val tutorSettings: Flow<TutorSettingsEntity?> = tutorSettingsDao.getSettings()

    suspend fun saveSettings(settings: TutorSettingsEntity) = withContext(Dispatchers.IO) {
        tutorSettingsDao.saveSettings(settings)
    }
}
