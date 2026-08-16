package com.qryptin.contacts.repository

import android.content.Context
import com.qryptin.auth.network.RetrofitClient
import com.qryptin.auth.network.UserApi
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
    private val api: ContactApi = ContactRetrofitClient.contactApi,
    private val userApi: UserApi = RetrofitClient.userApi
) : ContactsRepository {

    private val localRepository = RoomContactsRepository(context)

    override fun observeContacts(): Flow<List<Contact>> = localRepository.observeContacts()

    override suspend fun lookupQryptUser(fullPhoneDigits: String): QryptUser? {
        return try {
            val response = userApi.getUserByPhone(fullPhoneDigits)
            if (response.isSuccessful) {
                val user = response.body() ?: return null
                QryptUser(
                    id = user.id,
                    name = user.fullName,
                    phone = user.phoneNumber,
                    email = "" // Email not in UserResponse
                )
            } else {
                // Fallback to local if backend fails (might be offline or mock testing)
                localRepository.lookupQryptUser(fullPhoneDigits)
            }
        } catch (e: Exception) {
            localRepository.lookupQryptUser(fullPhoneDigits)
        }
    }

    override suspend fun saveExistingQryptContact(
        friendId: String?,
        phone: String,
        originalQryptName: String,
        effectiveName: String,
        email: String,
        saveOption: ContactSaveOption
    ): Boolean {
        val success = localRepository.saveExistingQryptContact(
            friendId, phone, originalQryptName, effectiveName, email, saveOption
        )
        if (success) {
            val userId = sessionRepository.currentUserId() ?: return true
            try {
                // If friendId was found in lookup, use it. Otherwise use phone (backend might handle it)
                val targetId = friendId ?: phone
                api.saveContact(ContactRequest(
                    userId = userId, 
                    friendId = targetId
                ))
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

    override suspend fun checkContactExists(userId: String, friendId: String): Boolean {
        return try {
            val response = api.checkContactExists(userId, friendId)
            response.isSuccessful && response.body()?.exists == true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loadContactsFromServer(userId: String): Boolean {
        return try {
            val response = api.getContacts(userId)
            if (response.isSuccessful) {
                val contacts = response.body() ?: emptyList()
                for (contactResp in contacts) {
                    val friendId = contactResp.friendProfile?.id ?: contactResp.friend_id
                    val friendPhone = contactResp.friendProfile?.phoneNumber ?: contactResp.friend_id
                    val friendName = contactResp.friendProfile?.fullName ?: "Unknown"
                    
                    localRepository.saveExistingQryptContact(
                        friendId = friendId,
                        phone = friendPhone,
                        originalQryptName = friendName,
                        effectiveName = friendName,
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
