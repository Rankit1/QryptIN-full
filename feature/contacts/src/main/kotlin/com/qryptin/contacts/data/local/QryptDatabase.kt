package com.qryptin.contacts.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ─────────────────────────────────────────────────────────────
//  QryptDatabase
//  Single Room database for the contacts feature. Accessed via
//  the thread-safe singleton getInstance() — never construct
//  this directly.
// ─────────────────────────────────────────────────────────────
@Database(
    entities    = [ContactEntity::class],
    version     = 2,
    exportSchema = false,
)
abstract class QryptDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        private const val DATABASE_NAME = "qryptin_contacts.db"

        @Volatile
        private var INSTANCE: QryptDatabase? = null

        fun getInstance(context: Context): QryptDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QryptDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
