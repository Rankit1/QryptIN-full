package com.qryptin.auth.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path

// ─────────────────────────────────────────────────────────────
//  UserApi — Retrofit interface for backend team's endpoints
//  Base URL configured in NetworkConfig
// ─────────────────────────────────────────────────────────────
interface UserApi {

    // POST /users/register — called after Firebase OTP verified
    @retrofit2.http.POST("users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<UserResponse>

    // GET /users/{id} — load current user profile
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<UserResponse>

    // GET /users/exists/{phone} — check if phone is registered
    @GET("users/exists/{phone}")
    suspend fun checkPhoneExists(@Path("phone") phone: String): Response<PhoneExistsResponse>

    // GET /users/phone/{phone} — lookup user by phone
    @GET("users/phone/{phone}")
    suspend fun getUserByPhone(@Path("phone") phone: String): Response<UserResponse>
}