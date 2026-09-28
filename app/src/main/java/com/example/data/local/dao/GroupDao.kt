package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.GroupMemberEntity
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

data class GroupMemberWithStudent(
    val memberId: Long,
    val groupId: Long,
    val studentId: Long,
    val priceOverride: Double?,
    val studentName: String,
    val studentPhone: String,
    val studentGrade: String,
    val defaultStudentPrice: Double
)

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    fun getGroupById(id: Long): Flow<GroupEntity?>

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    suspend fun getGroupByIdDirect(id: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity): Long

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND studentId = :studentId")
    suspend fun removeMember(groupId: Long, studentId: Long)

    @Query("""
        SELECT 
            gm.id AS memberId,
            gm.groupId AS groupId,
            gm.studentId AS studentId,
            gm.priceOverride AS priceOverride,
            s.name AS studentName,
            s.phone AS studentPhone,
            s.grade AS studentGrade,
            s.defaultPrice AS defaultStudentPrice
        FROM group_members gm
        INNER JOIN students s ON gm.studentId = s.id
        WHERE gm.groupId = :groupId
        ORDER BY s.name ASC
    """)
    fun getGroupMembers(groupId: Long): Flow<List<GroupMemberWithStudent>>

    @Query("""
        SELECT 
            gm.id AS memberId,
            gm.groupId AS groupId,
            gm.studentId AS studentId,
            gm.priceOverride AS priceOverride,
            s.name AS studentName,
            s.phone AS studentPhone,
            s.grade AS studentGrade,
            s.defaultPrice AS defaultStudentPrice
        FROM group_members gm
        INNER JOIN students s ON gm.studentId = s.id
        WHERE gm.groupId = :groupId
    """)
    suspend fun getGroupMembersDirect(groupId: Long): List<GroupMemberWithStudent>

    @Query("SELECT * FROM groups WHERE id IN (SELECT groupId FROM group_members WHERE studentId = :studentId)")
    fun getGroupsForStudent(studentId: Long): Flow<List<GroupEntity>>

    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId")
    fun getGroupMemberCount(groupId: Long): Flow<Int>
}
