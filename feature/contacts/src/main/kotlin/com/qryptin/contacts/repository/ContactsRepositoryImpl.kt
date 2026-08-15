package com.qryptin.contacts.repository

import android.content.Context
import com.qryptin.auth.repository.SessionRepository
import com.qryptin.contacts.data.remote.QryptUser
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.model.InviteMethod
import com.qryptin.contacts.network.ContactApi
import com.qryptin.contacts.network.ContactRequest
import com.qryptin.contacts.network.ContactResponse
import com.qryptin.contacts.network.ContactRetrofitClient
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class ContactsRepositoryImpl(
    context: Context,
    private val sessionRepository: SessionRepository,
    private val api: ContactApi = ContactRetrofitClient.contactApi
) : ContactsRepository {

    private val localRepository = RoomContactsRepository(context)

    override fun observeContacts(): Flow<List<Contact>> = localRepository.observeContacts()

    override suspend fun lookupQryptUser(fullPhoneDigits: String): QryptUser? =
        localRepository.lookupQryptUser(fullPhoneDigits)

    override suspend fun saveExistingQryptContact(
        phone: String,
        originalQryptName: String,
        effectiveName: String,
        email: String,
        saveOption: ContactSaveOption
    ): Boolean {
        val success = localRepository.saveExistingQryptContact(
            phone, originalQryptName, effectiveName, email, saveOption
        )
        if (success) {
            val userId = sessionRepository.currentUserId() ?: "unknown"
            try {
                api.saveContact(ContactRequest(userId = userId, friendId = phone))
            } catch (e: Exception) {
                // Log error
            }
        }
        return success
    }

    override suspend fun saveNonQryptContact(
        phone: String,
        displayName: String,
        email: String,
        saveOption: ContactSaveOption,
        inviteMethod: InviteMethod
    ): Boolean = localRepository.saveNonQryptContact(phone, displayName, email, saveOption, inviteMethod)

    override suspend fun getContactById(id: Long): Contact? = localRepository.getContactById(id)

    override suspend fun updateContact(
        id: Long,
        nickname: String,
        phone: String,
        email: String
    ): Boolean = localRepository.updateContact(id, nickname, phone, email)

    override suspend fun getContactDetail(id: Long): ContactDetail? = localRepository.getContactDetail(id)

    override suspend fun loadContactsFromServer(userId: String): Boolean {
        return try {
            val response = api.getContacts(userId)
            if (response.isSuccessful) {
                val contacts = response.body() ?: emptyList()
                for (contactResp in contacts) {
                    localRepository.saveExistingQryptContact(
                        phone = contactResp.friend_id,
                        originalQryptName = contactResp.friendProfile?.fullName ?: "Unknown",
                        effectiveName = contactResp.friendProfile?.fullName ?: "Unknown",
                        email = "",
                        saveOption = ContactSaveOption.SYNCED_WITH_SIM
                    )
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
