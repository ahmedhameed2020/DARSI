package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentByIdDirect(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR grade LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT COUNT(*) FROM students WHERE status = 'ACTIVE'")
    fun getActiveStudentsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getTotalStudentsCount(): Int
}
