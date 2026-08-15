package com.qryptin.chat.network

import com.google.gson.annotations.SerializedName

data class SendMessageRequest(
    @SerializedName("sender_id")     val senderId: String,
    @SerializedName("receiver_id")   val receiverId: String,
    @SerializedName("message")       val message: String,
    @SerializedName("encrypted_key") val encryptedKey: String? = null,
    @SerializedName("signature")     val signature: String? = null,
    @SerializedName("status")        val status: String = "SENT"
)

data class MessageResponse(
    @SerializedName("id")            val id: Long,
    @SerializedName("sender_id")     val senderId: String,
    @SerializedName("receiver_id")   val receiverId: String,
    @SerializedName("message")       val message: String,
    @SerializedName("encrypted_key") val encryptedKey: String?,
    @SerializedName("signature")     val signature: String?,
    @SerializedName("status")        val status: String, // SENT, DELIVERED, READ
    @SerializedName("timestamp")     val timestamp: Long,
    @SerializedName("created_at")    val createdAt: Long
)

data class InboxItemResponse(
    @SerializedName("user_id")       val userId: String,
    @SerializedName("lastMessage")   val lastMessage: MessageResponse,
    @SerializedName("unreadCount")   val unreadCount: Int
)
