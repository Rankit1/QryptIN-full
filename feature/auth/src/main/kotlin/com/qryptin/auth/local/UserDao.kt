package com.qryptin.auth.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun findByPhone(phoneNumber: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun findById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun observeById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE qryptinId = :qryptinId LIMIT 1")
    suspend fun findByQryptinId(qryptinId: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE qryptinId = :qryptinId AND userId != :excludingUserId")
    suspend fun countByQryptinIdExcluding(qryptinId: String, excludingUserId: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE qryptinId = :qryptinId")
    suspend fun countByQryptinId(qryptinId: String): Int
}
