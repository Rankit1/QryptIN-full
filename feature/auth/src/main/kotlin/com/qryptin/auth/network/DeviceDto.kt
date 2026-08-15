package com.qryptin.auth.network

import com.google.gson.annotations.SerializedName

data class DeviceRegistrationRequest(
    @SerializedName("user_id")        val userId: String,
    @SerializedName("firebase_token") val deviceToken: String,
    @SerializedName("platform")       val platform: String = "ANDROID",
    @SerializedName("device_name")    val model: String
)

data class DeviceResponse(
    @SerializedName("id")             val id: String,
    @SerializedName("user_id")        val userId: String,
    @SerializedName("firebase_token") val firebaseToken: String,
    @SerializedName("device_name")    val deviceName: String,
    @SerializedName("platform")       val platform: String,
    @SerializedName("updated_at")     val updatedAt: Long
)
