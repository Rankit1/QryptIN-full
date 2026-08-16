package com.qryptin.contacts.network

import com.google.gson.annotations.SerializedName

data class ContactRequest(
    val userId: String,
    val friendId: String
)

data class ContactResponse(
    @SerializedName("id")         val id: String, // Changed from Long to String for UUID
    @SerializedName("user_id")    val userId: String,
    @SerializedName("friend_id")  val friend_id: String,
    @SerializedName("created_at") val addedAt: String, // Changed from Long to String for timestamp string
    @SerializedName("friendProfile") val friendProfile: UserProfileResponse? = null
)

data class UserProfileResponse(
    @SerializedName("id")            val id: String,
    @SerializedName("phone")         val phoneNumber: String,
    @SerializedName("username")      val fullName: String,
    @SerializedName("profile_photo") val profilePhotoUri: String?,
    @SerializedName("bio")           val bio: String?
)

data class ContactExistsResponse(
    @SerializedName("exists") val exists: Boolean
)
