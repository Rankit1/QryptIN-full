package com.qryptin.calls.data.local

import android.content.Context
import androidx.room.*

@Database(entities = [CallEntity::class], version = 1, exportSchema = false)
abstract class CallsDatabase : RoomDatabase() {

    abstract fun callDao(): CallDao

    companion object {
        @Volatile private var INSTANCE: CallsDatabase? = null

        fun getInstance(context: Context): CallsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CallsDatabase::class.java,
                    "qryptin_calls.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
