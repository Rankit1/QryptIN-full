package com.qryptin.auth.repository

import com.qryptin.auth.network.DeviceApi
import com.qryptin.auth.network.DeviceRegistrationRequest
import com.qryptin.auth.network.DeviceResponse
import com.qryptin.auth.network.RetrofitClient
import retrofit2.Response

class DeviceRepositoryImpl(
    private val api: DeviceApi = RetrofitClient.deviceApi
) : DeviceRepository {

    override suspend fun registerDevice(userId: String, deviceToken: String, model: String, osVersion: String): Boolean {
        return try {
            val response = api.registerDevice(DeviceRegistrationRequest(userId, deviceToken, "ANDROID", model))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getDevice(userId: String): DeviceResponse? {
        return try {
            val response = api.getDeviceByUserId(userId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}
