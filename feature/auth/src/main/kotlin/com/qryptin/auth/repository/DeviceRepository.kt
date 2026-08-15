package com.qryptin.auth.repository

import com.qryptin.auth.network.DeviceResponse

interface DeviceRepository {
    suspend fun registerDevice(userId: String, deviceToken: String, model: String, osVersion: String): Boolean
    suspend fun getDevice(userId: String): DeviceResponse?
}
