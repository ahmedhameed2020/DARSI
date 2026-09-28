package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.LessonSeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY startEpochMillis ASC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE lessonDate = :date ORDER BY startEpochMillis ASC")
    fun getLessonsForDate(date: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE lessonDate = :date ORDER BY startEpochMillis ASC")
    suspend fun getLessonsForDateDirect(date: String): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE startEpochMillis >= :startEpoch AND startEpochMillis <= :endEpoch ORDER BY startEpochMillis ASC")
    fun getLessonsBetweenEpochs(startEpoch: Long, endEpoch: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE startEpochMillis >= :startEpoch AND startEpochMillis <= :endEpoch ORDER BY startEpochMillis ASC")
    suspend fun getLessonsBetweenEpochsDirect(startEpoch: Long, endEpoch: Long): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    fun getLessonById(id: Long): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonByIdDirect(id: Long): LessonEntity?

    @Query("SELECT * FROM lessons WHERE startEpochMillis >= :nowEpoch AND status != 'CANCELLED_BY_STUDENT' AND status != 'CANCELLED_BY_TUTOR' ORDER BY startEpochMillis ASC LIMIT 1")
    fun getNextUpcomingLesson(nowEpoch: Long): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE startEpochMillis >= :nowEpoch AND status != 'CANCELLED_BY_STUDENT' AND status != 'CANCELLED_BY_TUTOR' ORDER BY startEpochMillis ASC LIMIT 1")
    suspend fun getNextUpcomingLessonDirect(nowEpoch: Long): LessonEntity?

    @Query("SELECT * FROM lessons WHERE studentId = :studentId ORDER BY startEpochMillis DESC")
    fun getLessonsForStudent(studentId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE groupId = :groupId ORDER BY startEpochMillis DESC")
    fun getLessonsForGroup(groupId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE studentId = :studentId AND startEpochMillis >= :nowEpoch AND status != 'CANCELLED_BY_STUDENT' AND status != 'CANCELLED_BY_TUTOR' ORDER BY startEpochMillis ASC LIMIT 1")
    fun getNextLessonForStudent(studentId: Long, nowEpoch: Long): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE groupId = :groupId AND startEpochMillis >= :nowEpoch AND status != 'CANCELLED_BY_STUDENT' AND status != 'CANCELLED_BY_TUTOR' ORDER BY startEpochMillis ASC LIMIT 1")
    fun getNextLessonForGroup(groupId: Long, nowEpoch: Long): Flow<LessonEntity?>

    @Query("SELECT COUNT(*) FROM lessons WHERE studentId = :studentId AND status = 'COMPLETED' AND startEpochMillis >= :monthStartEpoch")
    fun getCompletedLessonsCountThisMonth(studentId: Long, monthStartEpoch: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLessonById(id: Long)

    @Query("DELETE FROM lessons WHERE seriesId = :seriesId AND startEpochMillis >= :fromEpoch")
    suspend fun deleteFutureLessonsForSeries(seriesId: Long, fromEpoch: Long)

    @Query("UPDATE lessons SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLessonStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE lessons SET topicCovered = :topic, homework = :homework, privateTutorNote = :note, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLessonNotes(id: Long, topic: String, homework: String, note: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE lessons SET googleEventId = :googleEventId WHERE id = :id")
    suspend fun updateGoogleEventId(id: Long, googleEventId: String?)
}

@Dao
interface LessonSeriesDao {
    @Query("SELECT * FROM lesson_series ORDER BY id DESC")
    fun getAllSeries(): Flow<List<LessonSeriesEntity>>

    @Query("SELECT * FROM lesson_series WHERE id = :id LIMIT 1")
    suspend fun getSeriesById(id: Long): LessonSeriesEntity?

    @Query("SELECT * FROM lesson_series WHERE studentId = :studentId")
    fun getSeriesForStudent(studentId: Long): Flow<List<LessonSeriesEntity>>

    @Query("SELECT * FROM lesson_series WHERE groupId = :groupId")
    fun getSeriesForGroup(groupId: Long): Flow<List<LessonSeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: LessonSeriesEntity): Long

    @Update
    suspend fun updateSeries(series: LessonSeriesEntity)

    @Delete
    suspend fun deleteSeries(series: LessonSeriesEntity)

    @Query("DELETE FROM lesson_series WHERE id = :id")
    suspend fun deleteSeriesById(id: Long)
}
