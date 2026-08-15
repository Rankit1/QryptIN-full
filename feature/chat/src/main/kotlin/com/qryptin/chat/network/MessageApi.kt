package com.qryptin.chat.network

import retrofit2.Response
import retrofit2.http.*

interface MessageApi {
    @GET("messages/chat/{senderId}/{receiverId}")
    suspend fun getChatHistory(
        @Path("senderId") senderId: String,
        @Path("receiverId") receiverId: String
    ): Response<List<MessageResponse>>

    @GET("messages/inbox/{receiverId}")
    suspend fun getInbox(@Path("receiverId") receiverId: String): Response<List<InboxItemResponse>>

    @POST("messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageResponse>

    @PUT("messages/status/{id}")
    suspend fun updateMessageStatus(
        @Path("id") messageId: Long,
        @Query("status") status: String
    ): Response<Unit>
}
