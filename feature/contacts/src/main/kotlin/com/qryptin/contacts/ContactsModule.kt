package com.qryptin.contacts

import android.content.Context
import com.qryptin.auth.AuthModule
import com.qryptin.contacts.repository.ContactsRepository
import com.qryptin.contacts.repository.ContactsRepositoryImpl

object ContactsModule {
    @Volatile private var repository: ContactsRepository? = null

    fun provideContactsRepository(context: Context): ContactsRepository =
        repository ?: synchronized(this) {
            repository ?: ContactsRepositoryImpl(
                context = context,
                sessionRepository = AuthModule.provideSessionRepository(context)
            ).also { repository = it }
        }
}
