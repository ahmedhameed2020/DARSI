package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.GroupDao
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

@Database(
    entities = [
        StudentEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        LessonSeriesEntity::class,
        LessonEntity::class,
        AttendanceEntity::class,
        PaymentEntity::class,
        PackageEntity::class,
        TutorSettingsEntity::class,
        LessonExceptionEntity::class,
        LessonNoteEntity::class,
        PaymentPlanEntity::class,
        SubjectEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DarsiDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun groupDao(): GroupDao
    abstract fun lessonDao(): LessonDao
    abstract fun lessonSeriesDao(): LessonSeriesDao
    abstract fun paymentDao(): PaymentDao
    abstract fun packageDao(): PackageDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun tutorSettingsDao(): TutorSettingsDao
    abstract fun lessonExceptionDao(): LessonExceptionDao
    abstract fun lessonNoteDao(): LessonNoteDao
    abstract fun paymentPlanDao(): PaymentPlanDao
    abstract fun subjectDao(): SubjectDao

    companion object {
        @Volatile
        private var INSTANCE: DarsiDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS lesson_exceptions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        seriesId INTEGER NOT NULL,
                        originalEpochMillis INTEGER NOT NULL,
                        rescheduledStartMillis INTEGER,
                        rescheduledEndMillis INTEGER,
                        isCancelled INTEGER NOT NULL DEFAULT 0,
                        reason TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS lesson_notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        lessonId INTEGER,
                        title TEXT NOT NULL DEFAULT '',
                        content TEXT NOT NULL,
                        category TEXT NOT NULL DEFAULT 'PROGRESS',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS payment_plans (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        planType TEXT NOT NULL DEFAULT 'PER_LESSON',
                        ratePerLesson REAL NOT NULL DEFAULT 0.0,
                        monthlyFee REAL NOT NULL DEFAULT 0.0,
                        billingDayOfMonth INTEGER NOT NULL DEFAULT 1,
                        notes TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS subjects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        colorHex TEXT NOT NULL DEFAULT '#1E40AF',
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Students location columns
                db.execSQL("ALTER TABLE students ADD COLUMN locationLabel TEXT NOT NULL DEFAULT 'Student Home'")
                db.execSQL("ALTER TABLE students ADD COLUMN areaName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN addressText TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN mapsLink TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE students ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE students ADD COLUMN locationNotes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN defaultLessonLocationType TEXT NOT NULL DEFAULT 'STUDENT_HOME'")
                db.execSQL("ALTER TABLE students ADD COLUMN defaultTravelTimeMinutes INTEGER")

                // Groups location columns
                db.execSQL("ALTER TABLE groups ADD COLUMN defaultLocationType TEXT NOT NULL DEFAULT 'TUTOR_LOCATION'")
                db.execSQL("ALTER TABLE groups ADD COLUMN defaultLocationLabel TEXT NOT NULL DEFAULT 'Tutor Location'")
                db.execSQL("ALTER TABLE groups ADD COLUMN areaName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE groups ADD COLUMN addressText TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE groups ADD COLUMN mapsLink TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE groups ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE groups ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE groups ADD COLUMN defaultTravelTimeMinutes INTEGER")

                // Lessons location snapshot columns
                db.execSQL("ALTER TABLE lessons ADD COLUMN locationType TEXT NOT NULL DEFAULT 'STUDENT_HOME'")
                db.execSQL("ALTER TABLE lessons ADD COLUMN locationLabel TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE lessons ADD COLUMN areaName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE lessons ADD COLUMN addressText TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE lessons ADD COLUMN mapsLink TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE lessons ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE lessons ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE lessons ADD COLUMN travelTimeMinutes INTEGER")
                db.execSQL("ALTER TABLE lessons ADD COLUMN meetingUrl TEXT NOT NULL DEFAULT ''")

                // Tutor Settings travel buffer
                db.execSQL("ALTER TABLE tutor_settings ADD COLUMN defaultTravelBufferMinutes INTEGER NOT NULL DEFAULT 10")
            }
        }

        fun getInstance(context: Context): DarsiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DarsiDatabase::class.java,
                    "darsi_teaching.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
