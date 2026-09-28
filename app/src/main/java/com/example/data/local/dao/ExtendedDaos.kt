package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LessonExceptionEntity
import com.example.data.local.entity.LessonNoteEntity
import com.example.data.local.entity.PaymentPlanEntity
import com.example.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonExceptionDao {
    @Query("SELECT * FROM lesson_exceptions WHERE seriesId = :seriesId")
    fun getExceptionsForSeries(seriesId: Long): Flow<List<LessonExceptionEntity>>

    @Query("SELECT * FROM lesson_exceptions WHERE seriesId = :seriesId")
    suspend fun getExceptionsForSeriesDirect(seriesId: Long): List<LessonExceptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertException(exception: LessonExceptionEntity): Long

    @Delete
    suspend fun deleteException(exception: LessonExceptionEntity)
}

@Dao
interface LessonNoteDao {
    @Query("SELECT * FROM lesson_notes WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getNotesForStudent(studentId: Long): Flow<List<LessonNoteEntity>>

    @Query("SELECT * FROM lesson_notes WHERE studentId = :studentId ORDER BY createdAt DESC")
    suspend fun getNotesForStudentDirect(studentId: Long): List<LessonNoteEntity>

    @Query("SELECT * FROM lesson_notes WHERE lessonId = :lessonId LIMIT 1")
    fun getNoteForLesson(lessonId: Long): Flow<LessonNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: LessonNoteEntity): Long

    @Update
    suspend fun updateNote(note: LessonNoteEntity)

    @Query("DELETE FROM lesson_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface PaymentPlanDao {
    @Query("SELECT * FROM payment_plans WHERE studentId = :studentId AND status = 'ACTIVE' LIMIT 1")
    fun getActivePlanForStudent(studentId: Long): Flow<PaymentPlanEntity?>

    @Query("SELECT * FROM payment_plans WHERE studentId = :studentId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActivePlanForStudentDirect(studentId: Long): PaymentPlanEntity?

    @Query("SELECT * FROM payment_plans WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getAllPlansForStudent(studentId: Long): Flow<List<PaymentPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PaymentPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: PaymentPlanEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}
