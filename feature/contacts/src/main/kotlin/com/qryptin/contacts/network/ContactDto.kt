package com.qryptin.contacts.network

import com.google.gson.annotations.SerializedName

data class ContactRequest(
    @SerializedName("user_id")   val userId: String,
    @SerializedName("friend_id") val friendId: String
)

data class ContactResponse(
    @SerializedName("id")         val id: Long,
    @SerializedName("user_id")    val userId: String,
    @SerializedName("friend_id")  val friend_id: String,
    @SerializedName("created_at") val addedAt: Long,
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
