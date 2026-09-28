package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.TutorSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY date DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE date >= :startEpoch AND date <= :endEpoch ORDER BY date DESC")
    fun getPaymentsBetween(startEpoch: Long, endEpoch: Long): Flow<List<PaymentEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE date >= :startEpoch AND date <= :endEpoch")
    fun getTotalReceivedBetween(startEpoch: Long, endEpoch: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE date >= :startEpoch AND date <= :endEpoch")
    suspend fun getTotalReceivedBetweenDirect(startEpoch: Long, endEpoch: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE studentId = :studentId")
    fun getTotalPaidByStudent(studentId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE studentId = :studentId")
    suspend fun getTotalPaidByStudentDirect(studentId: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM packages WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getPackagesForStudent(studentId: Long): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages WHERE studentId = :studentId AND status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    fun getActivePackageForStudent(studentId: Long): Flow<PackageEntity?>

    @Query("SELECT * FROM packages WHERE studentId = :studentId AND status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActivePackageForStudentDirect(studentId: Long): PackageEntity?

    @Query("SELECT * FROM packages WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getAllActivePackages(): Flow<List<PackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PackageEntity): Long

    @Update
    suspend fun updatePackage(pkg: PackageEntity)

    @Delete
    suspend fun deletePackage(pkg: PackageEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE lessonId = :lessonId")
    fun getAttendanceForLesson(lessonId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE lessonId = :lessonId")
    suspend fun getAttendanceForLessonDirect(lessonId: Long): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)
}

@Dao
interface TutorSettingsDao {
    @Query("SELECT * FROM tutor_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<TutorSettingsEntity?>

    @Query("SELECT * FROM tutor_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): TutorSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: TutorSettingsEntity)
}
