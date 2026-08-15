package com.qryptin.chat

import android.content.Context
import com.qryptin.auth.AuthModule
import com.qryptin.chat.repository.ChatRepository
import com.qryptin.chat.repository.ChatRepositoryImpl

object ChatModule {
    @Volatile private var repository: ChatRepository? = null

    fun provideChatRepository(context: Context): ChatRepository =
        repository ?: synchronized(this) {
            repository ?: ChatRepositoryImpl(
                context = context,
                sessionRepository = AuthModule.provideSessionRepository(context)
            ).also { repository = it }
        }
}
