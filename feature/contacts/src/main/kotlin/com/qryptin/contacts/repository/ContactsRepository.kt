package com.qryptin.contacts.repository

import android.content.Context
import com.qryptin.contacts.data.local.ContactDao
import com.qryptin.contacts.data.local.ContactEntity
import com.qryptin.contacts.data.local.QryptDatabase
import com.qryptin.contacts.data.remote.QryptUser
import com.qryptin.contacts.data.remote.QryptUserDirectory
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.model.InviteMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────
//  ContactsRepository
//
//  Single source of truth for everything contact-related:
//   • Persists saved contacts in Room (ContactDao) — survives
//     app restarts, no more disappearing contacts.
//   • Looks up phone numbers against the simulated QryptIN
//     global registry (QryptUserDirectory → mock_users.json).
//
//  Replaces the old FakeContactsRepository, which held a
//  hardcoded in-memory map of "registered" numbers.
// ─────────────────────────────────────────────────────────────
interface ContactsRepository {
    fun observeContacts(): Flow<List<Contact>>
    suspend fun lookupQryptUser(fullPhoneDigits: String): QryptUser?
    suspend fun saveExistingQryptContact(
        phone: String,
        originalQryptName: String,
        effectiveName: String,
        email: String,
        saveOption: ContactSaveOption,
    ): Boolean

    suspend fun saveNonQryptContact(
        phone: String,
        displayName: String,
        email: String,
        saveOption: ContactSaveOption,
        inviteMethod: InviteMethod,
    ): Boolean

    suspend fun getContactById(id: Long): Contact?
    suspend fun updateContact(
        id: Long,
        nickname: String,
        phone: String,
        email: String,
    ): Boolean

    suspend fun getContactDetail(id: Long): ContactDetail?
    
    // New methods from backend integration
    suspend fun loadContactsFromServer(userId: String): Boolean
}

class RoomContactsRepository(context: Context) : ContactsRepository {

    private val dao: ContactDao = QryptDatabase.getInstance(context).contactDao()
    private val directory       = QryptUserDirectory(context)

    override fun observeContacts(): Flow<List<Contact>> =
        dao.observeContacts().map { entities -> entities.map { it.toContact() } }

    override suspend fun lookupQryptUser(fullPhoneDigits: String): QryptUser? =
        directory.lookupByPhone(fullPhoneDigits)

    override suspend fun saveExistingQryptContact(
        phone             : String,
        originalQryptName : String,
        effectiveName     : String,
        email             : String,
        saveOption        : ContactSaveOption,
    ): Boolean {
        val nickname = effectiveName.takeIf { it.isNotBlank() && it != originalQryptName }
        dao.insert(
            ContactEntity(
                displayName       = effectiveName.ifBlank { originalQryptName },
                originalQryptName = originalQryptName,
                nickname          = nickname,
                phoneNumber       = phone,
                email             = email.ifBlank { null },
                isQryptUser       = true,
                saveMode          = saveOption.name,
                inviteMethod      = null,
                createdAt         = System.currentTimeMillis(),
            )
        )
        return true
    }

    override suspend fun saveNonQryptContact(
        phone        : String,
        displayName  : String,
        email        : String,
        saveOption   : ContactSaveOption,
        inviteMethod : InviteMethod,
    ): Boolean {
        if (displayName.isBlank()) return false
        dao.insert(
            ContactEntity(
                displayName       = displayName.trim(),
                originalQryptName = null,
                nickname          = null,
                phoneNumber       = phone,
                email             = email.ifBlank { null },
                isQryptUser       = false,
                saveMode          = saveOption.name,
                inviteMethod      = inviteMethod.name,
                createdAt         = System.currentTimeMillis(),
            )
        )
        return true
    }

    override suspend fun getContactById(id: Long): Contact? = dao.getById(id)?.toContact()

    override suspend fun updateContact(
        id       : Long,
        nickname : String,
        phone    : String,
        email    : String,
    ): Boolean {
        if (phone.isBlank()) return false
        val existing = dao.getById(id) ?: return false

        val trimmedNickname = nickname.trim().takeIf { it.isNotBlank() }
        val fallbackName = existing.originalQryptName ?: existing.displayName
        val resolvedDisplayName = trimmedNickname ?: fallbackName

        dao.update(
            existing.copy(
                displayName = resolvedDisplayName,
                nickname    = trimmedNickname?.takeIf { it != existing.originalQryptName },
                phoneNumber = phone.trim(),
                email       = email.trim().ifBlank { null },
            )
        )
        return true
    }

    override suspend fun getContactDetail(id: Long): ContactDetail? = dao.getById(id)?.let { entity ->
        ContactDetail(
            id                = entity.id,
            displayName       = entity.displayName,
            originalQryptName = entity.originalQryptName,
            nickname          = entity.nickname,
            phoneNumber       = entity.phoneNumber,
            email             = entity.email,
            isQryptUser       = entity.isQryptUser,
            saveMode          = runCatching { ContactSaveOption.valueOf(entity.saveMode) }
                .getOrDefault(ContactSaveOption.SYNCED_WITH_SIM),
            inviteMethod      = entity.inviteMethod?.let {
                runCatching { InviteMethod.valueOf(it) }.getOrNull()
            },
            createdAt         = entity.createdAt,
        )
    }

    override suspend fun loadContactsFromServer(userId: String): Boolean {
        // Implementation will be in ContactsRepositoryImpl
        return false
    }

    private fun ContactEntity.toContact(): Contact = Contact(
        id          = id.toString(),
        displayName = displayName,
        phone       = phoneNumber,
        isOnQryptIN = isQryptUser,
        createdAt   = createdAt,
    )
}

/** Rich detail projection used by ContactDetailScreen — not needed for the list view. */
data class ContactDetail(
    val id                : Long,
    val displayName       : String,
    val originalQryptName : String?,
    val nickname          : String?,
    val phoneNumber       : String,
    val email             : String?,
    val isQryptUser       : Boolean,
    val saveMode          : ContactSaveOption,
    val inviteMethod      : InviteMethod?,
    val createdAt         : Long,
)
