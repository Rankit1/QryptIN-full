package com.qryptin.calls.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {

    @Query("SELECT * FROM calls ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE contactId = :contactId ORDER BY timestamp DESC")
    fun observeByContact(contactId: String): Flow<List<CallEntity>>

    @Query("""
        SELECT * FROM calls
        WHERE contactName LIKE '%' || :query || '%'
           OR contactPhone LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun search(query: String): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE callId = :callId LIMIT 1")
    suspend fun getById(callId: String): CallEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(call: CallEntity)

    @Query("UPDATE calls SET durationSecs = :secs WHERE callId = :callId")
    suspend fun updateDuration(callId: String, secs: Int)

    @Query("DELETE FROM calls WHERE callId = :callId")
    suspend fun delete(callId: String)

    @Query("DELETE FROM calls")
    suspend fun deleteAll()
}
