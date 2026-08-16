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
    @SerializedName("id")            val id: String?,
    @SerializedName("senderId")      val senderId: String?,
    @SerializedName("sender_id")     val sender_id: String?,
    @SerializedName("receiverId")    val receiverId: String?,
    @SerializedName("receiver_id")   val receiver_id: String?,
    @SerializedName("message")       val message: String?,
    @SerializedName("encryptedKey")  val encryptedKey: String? = null,
    @SerializedName("encrypted_key") val encrypted_key: String? = null,
    @SerializedName("signature")     val signature: String? = null,
    @SerializedName("status")        val status: String? = "SENT",
    @SerializedName("timestamp")     val timestamp: Any? = null,
    @SerializedName("created_at")    val createdAt: Any? = null
) {
    val realSenderId: String get() = senderId ?: sender_id ?: ""
    val realReceiverId: String get() = receiverId ?: receiver_id ?: ""
    val realMessage: String get() = message ?: ""
}

data class InboxItemResponse(
    @SerializedName("user_id")       val userId: String,
    @SerializedName("lastMessage")   val lastMessage: MessageResponse,
    @SerializedName("unreadCount")   val unreadCount: Int
)
