package com.qryptin.auth.network

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
//  UserDto — matches backend PostgreSQL schema exactly
// ─────────────────────────────────────────────────────────────

data class RegisterRequest(
    @SerializedName("phone")        val phoneNumber: String,  // maps to 'phone' in pgsql
    @SerializedName("username")     val fullName: String,     // maps to 'username' in pgsql
    @SerializedName("id")           val id: String,           // maps to 'id' in pgsql
    @SerializedName("bio")          val bio: String? = null,  // maps to 'bio' in pgsql
    @SerializedName("profile_photo") val profilePhoto: String? = null, // maps to 'profile_photo'
    @SerializedName("public_key")   val publicKey: String? = "stub_key_" + System.currentTimeMillis() // maps to 'public_key'
)

data class UserResponse(
    @SerializedName("id")           val id: String,
    @SerializedName("phone")        val phoneNumber: String,
    @SerializedName("username")     val fullName: String,
    @SerializedName("bio")          val bio: String?,
    @SerializedName("profile_photo") val profilePhoto: String?,
    @SerializedName("public_key")   val publicKey: String?,
    @SerializedName("token")        val token: String?,
)

data class PhoneExistsResponse(
    @SerializedName("exists") val exists: Boolean,
)
