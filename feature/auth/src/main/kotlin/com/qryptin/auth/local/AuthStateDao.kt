package com.qryptin.auth.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: AuthStateEntity)

    @Query("SELECT * FROM auth_state WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = AuthStateEntity.SINGLETON_ID): AuthStateEntity?

    @Query("SELECT * FROM auth_state WHERE id = :id LIMIT 1")
    fun observe(id: Int = AuthStateEntity.SINGLETON_ID): Flow<AuthStateEntity?>

    @Query("DELETE FROM auth_state")
    suspend fun clear()
}
