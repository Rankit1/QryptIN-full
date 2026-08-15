package com.qryptin.auth.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("UPDATE sessions SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAllSessions()

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}
