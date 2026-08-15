package com.qryptin.auth.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceApi {
    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest): Response<DeviceResponse>

    @GET("devices/{userId}")
    suspend fun getDeviceByUserId(@Path("userId") userId: String): Response<DeviceResponse>
}
