package com.qryptin.contacts.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContactApi {
    @POST("contacts")
    suspend fun saveContact(@Body request: ContactRequest): Response<ContactResponse>

    @GET("contacts/{userId}")
    suspend fun getContacts(@Path("userId") userId: String): Response<List<ContactResponse>>

    @GET("contacts/exists/{userId}/{friendId}")
    suspend fun checkContactExists(
        @Path("userId") userId: String,
        @Path("friendId") friendId: String
    ): Response<ContactExistsResponse>
}
