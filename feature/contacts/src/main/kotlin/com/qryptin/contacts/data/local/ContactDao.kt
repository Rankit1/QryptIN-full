package com.qryptin.contacts.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  ContactDao
//  All reads are exposed as Flow so ContactsScreen automatically
//  re-renders whenever a contact is added, edited, or removed —
//  including after the app is killed and relaunched.
// ─────────────────────────────────────────────────────────────
@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY displayName COLLATE NOCASE ASC")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun findByPhoneNumber(phoneNumber: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM contacts WHERE isQryptUser = 1")
    fun observeQryptUserCount(): Flow<Int>
}
