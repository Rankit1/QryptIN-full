package com.qryptin.calls.data.repository

import android.content.Context
import com.qryptin.calls.data.local.CallEntity
import com.qryptin.calls.data.local.CallsDatabase
import com.qryptin.calls.model.CallDirection
import com.qryptin.calls.model.CallModel
import com.qryptin.calls.model.CallType
import com.qryptin.calls.repository.CallRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class RoomCallRepository(context: Context) : CallRepository {

    private val dao = CallsDatabase.getInstance(context).callDao()

    override fun observeAll(): Flow<List<CallModel>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }.catch { emit(emptyList()) }

    override fun search(query: String): Flow<List<CallModel>> =
        dao.search(query).map { list -> list.map { it.toModel() } }.catch { emit(emptyList()) }

    override suspend fun saveCall(call: CallModel) =
        dao.insert(call.toEntity())

    override suspend fun updateDuration(callId: String, durationSecs: Int) =
        dao.updateDuration(callId, durationSecs)

    override suspend fun delete(callId: String) =
        dao.delete(callId)

    // ── Mappers ───────────────────────────────────────────────

    private fun CallEntity.toModel() = CallModel(
        callId        = callId,
        contactId     = contactId,
        contactName   = contactName,
        contactAvatar = contactAvatar,
        contactPhone  = contactPhone,
        type          = runCatching { CallType.valueOf(callType) }.getOrDefault(CallType.VOICE),
        direction     = runCatching { CallDirection.valueOf(direction) }.getOrDefault(CallDirection.INCOMING),
        timestamp     = timestamp,
        durationSecs  = durationSecs,
        isEncrypted   = isEncrypted,
    )

    private fun CallModel.toEntity() = CallEntity(
        callId        = callId,
        contactId     = contactId,
        contactName   = contactName,
        contactAvatar = contactAvatar,
        contactPhone  = contactPhone,
        callType      = type.name,
        direction     = direction.name,
        timestamp     = timestamp,
        durationSecs  = durationSecs,
        isEncrypted   = isEncrypted,
    )
}
