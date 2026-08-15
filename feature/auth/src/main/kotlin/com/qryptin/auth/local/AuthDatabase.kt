package com.qryptin.auth.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ─────────────────────────────────────────────────────────────
//  AuthDatabase
//  Single Room database for feature:auth — users, sessions, and
//  the current auth-state snapshot. Access only via getInstance().
// ─────────────────────────────────────────────────────────────
@Database(
    entities     = [
        UserEntity::class,
        SessionEntity::class,
        AuthStateEntity::class,
    ],
    version      = 1,
    exportSchema = false,
)
abstract class AuthDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun authStateDao(): AuthStateDao

    companion object {
        private const val DATABASE_NAME = "qryptin_auth.db"

        @Volatile
        private var INSTANCE: AuthDatabase? = null

        fun getInstance(context: Context): AuthDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuthDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
