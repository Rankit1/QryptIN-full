package com.qryptin.chat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.qryptin.chat.group.model.GroupEntity

// ─────────────────────────────────────────────────────────────
//  ChatDatabase
//  Single Room database for feature:chat.
//  Includes conversations, messages, and groups.
//  Access only via getInstance() — never construct directly.
// ─────────────────────────────────────────────────────────────
@Database(
    entities     = [
        ConversationEntity::class,
        MessageEntity::class,
        GroupEntity::class,
    ],
    version      = 2,    //Bump db version 2
    exportSchema = false,
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun groupDao(): GroupDao

    companion object {
        private const val DATABASE_NAME = "qryptin_chat.db"

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        // Migration object added
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE messages ADD COLUMN receiverId TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context): ChatDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
