package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.DarsiDatabase
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.GroupMemberEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.TutorSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {
    suspend fun createBackupJson(database: DarsiDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Darsi")
        root.put("exportTimestamp", System.currentTimeMillis())

        // Students
        val students = database.studentDao().getAllStudents().firstOrNull() ?: emptyList()
        val studentsArr = JSONArray()
        students.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("phone", s.phone)
                put("parentPhone", s.parentPhone ?: "")
                put("school", s.school ?: "")
                put("grade", s.grade)
                put("subject", s.subject)
                put("defaultPrice", s.defaultPrice)
                put("defaultDurationMinutes", s.defaultDurationMinutes)
                put("paymentType", s.paymentType)
                put("privateNotes", s.privateNotes)
                put("status", s.status)
                put("createdAt", s.createdAt)
            }
            studentsArr.put(obj)
        }
        root.put("students", studentsArr)

        // Groups
        val groups = database.groupDao().getAllGroups().firstOrNull() ?: emptyList()
        val groupsArr = JSONArray()
        groups.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("subject", g.subject)
                put("grade", g.grade)
                put("defaultPrice", g.defaultPrice)
                put("defaultDurationMinutes", g.defaultDurationMinutes)
                put("notes", g.notes)
                put("status", g.status)
            }
            groupsArr.put(obj)
        }
        root.put("groups", groupsArr)

        // Lessons
        val lessons = database.lessonDao().getAllLessons().firstOrNull() ?: emptyList()
        val lessonsArr = JSONArray()
        lessons.forEach { l ->
            val obj = JSONObject().apply {
                put("id", l.id)
                put("studentId", l.studentId ?: -1L)
                put("groupId", l.groupId ?: -1L)
                put("lessonDate", l.lessonDate)
                put("startEpochMillis", l.startEpochMillis)
                put("endEpochMillis", l.endEpochMillis)
                put("durationMinutes", l.durationMinutes)
                put("price", l.price)
                put("status", l.status)
                put("location", l.location)
                put("topicCovered", l.topicCovered)
                put("homework", l.homework)
                put("privateTutorNote", l.privateTutorNote)
            }
            lessonsArr.put(obj)
        }
        root.put("lessons", lessonsArr)

        // Payments
        val payments = database.paymentDao().getAllPayments().firstOrNull() ?: emptyList()
        val paymentsArr = JSONArray()
        payments.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("studentId", p.studentId)
                put("amount", p.amount)
                put("currency", p.currency)
                put("date", p.date)
                put("paymentMethod", p.paymentMethod)
                put("paymentFor", p.paymentFor)
                put("note", p.note)
            }
            paymentsArr.put(obj)
        }
        root.put("payments", paymentsArr)

        // Packages
        val packages = database.packageDao().getAllActivePackages().firstOrNull() ?: emptyList()
        val packagesArr = JSONArray()
        packages.forEach { pkg ->
            val obj = JSONObject().apply {
                put("id", pkg.id)
                put("studentId", pkg.studentId)
                put("packageName", pkg.packageName)
                put("totalLessons", pkg.totalLessons)
                put("usedLessons", pkg.usedLessons)
                put("price", pkg.price)
                put("currency", pkg.currency)
                put("status", pkg.status)
            }
            packagesArr.put(obj)
        }
        root.put("packages", packagesArr)

        // Settings
        val settings = database.tutorSettingsDao().getSettingsDirect()
        if (settings != null) {
            val sObj = JSONObject().apply {
                put("tutorName", settings.tutorName)
                put("subjects", settings.subjects)
                put("defaultDurationMinutes", settings.defaultDurationMinutes)
                put("defaultCurrency", settings.defaultCurrency)
            }
            root.put("settings", sObj)
        }

        root.toString(2)
    }

    fun shareBackup(context: Context, backupJson: String) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "Darsi Teaching Backup ($timeStamp)")
            putExtra(Intent.EXTRA_TEXT, backupJson)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Darsi Backup JSON"))
    }

    suspend fun restoreBackupJson(database: DarsiDatabase, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("students")) return@withContext false

            // Restore students
            val studentsArr = root.getJSONArray("students")
            for (i in 0 until studentsArr.length()) {
                val obj = studentsArr.getJSONObject(i)
                val s = StudentEntity(
                    name = obj.getString("name"),
                    phone = obj.getString("phone"),
                    parentPhone = if (obj.has("parentPhone")) obj.optString("parentPhone") else null,
                    school = if (obj.has("school")) obj.optString("school") else null,
                    grade = obj.optString("grade", "Grade 12"),
                    subject = obj.optString("subject", "English"),
                    defaultPrice = obj.optDouble("defaultPrice", 250.0),
                    defaultDurationMinutes = obj.optInt("defaultDurationMinutes", 60),
                    paymentType = obj.optString("paymentType", "PER_LESSON"),
                    privateNotes = obj.optString("privateNotes", ""),
                    status = obj.optString("status", "ACTIVE")
                )
                database.studentDao().insertStudent(s)
            }

            // Restore groups
            if (root.has("groups")) {
                val groupsArr = root.getJSONArray("groups")
                for (i in 0 until groupsArr.length()) {
                    val obj = groupsArr.getJSONObject(i)
                    val g = GroupEntity(
                        name = obj.getString("name"),
                        subject = obj.optString("subject", "General"),
                        grade = obj.optString("grade", "Grade 12"),
                        defaultPrice = obj.optDouble("defaultPrice", 200.0),
                        defaultDurationMinutes = obj.optInt("defaultDurationMinutes", 90),
                        notes = obj.optString("notes", ""),
                        status = obj.optString("status", "ACTIVE")
                    )
                    database.groupDao().insertGroup(g)
                }
            }

            // Restore lessons
            if (root.has("lessons")) {
                val lessonsArr = root.getJSONArray("lessons")
                for (i in 0 until lessonsArr.length()) {
                    val obj = lessonsArr.getJSONObject(i)
                    val studentId = if (obj.getLong("studentId") > 0) obj.getLong("studentId") else null
                    val groupId = if (obj.getLong("groupId") > 0) obj.getLong("groupId") else null
                    val l = LessonEntity(
                        studentId = studentId,
                        groupId = groupId,
                        lessonDate = obj.getString("lessonDate"),
                        startEpochMillis = obj.getLong("startEpochMillis"),
                        endEpochMillis = obj.getLong("endEpochMillis"),
                        durationMinutes = obj.optInt("durationMinutes", 60),
                        price = obj.optDouble("price", 0.0),
                        status = obj.optString("status", "SCHEDULED"),
                        location = obj.optString("location", "In-person"),
                        topicCovered = obj.optString("topicCovered", ""),
                        homework = obj.optString("homework", ""),
                        privateTutorNote = obj.optString("privateTutorNote", "")
                    )
                    database.lessonDao().insertLesson(l)
                }
            }

            // Restore payments
            if (root.has("payments")) {
                val paymentsArr = root.getJSONArray("payments")
                for (i in 0 until paymentsArr.length()) {
                    val obj = paymentsArr.getJSONObject(i)
                    val p = PaymentEntity(
                        studentId = obj.getLong("studentId"),
                        amount = obj.getDouble("amount"),
                        currency = obj.optString("currency", "QAR"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        paymentMethod = obj.optString("paymentMethod", "CASH"),
                        paymentFor = obj.optString("paymentFor", "LESSON"),
                        note = obj.optString("note", "")
                    )
                    database.paymentDao().insertPayment(p)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
