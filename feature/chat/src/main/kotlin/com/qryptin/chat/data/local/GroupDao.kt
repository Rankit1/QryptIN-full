package com.qryptin.chat.data.local

import androidx.room.*
import com.qryptin.chat.group.model.GroupEntity
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  GroupDao
// ─────────────────────────────────────────────────────────────
@Dao
interface GroupDao {

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE groupId = :groupId LIMIT 1")
    suspend fun getById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE groupId = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Query("""
        UPDATE groups
        SET lastMessage = :lastMessage
        WHERE groupId = :groupId
    """)
    suspend fun updateLastMessage(groupId: String, lastMessage: String?)
}
