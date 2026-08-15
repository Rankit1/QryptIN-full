package com.qryptin.chat.group.repository

import com.qryptin.chat.group.model.GroupModel
import com.qryptin.chat.group.model.GroupPermissions
import com.qryptin.contacts.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  FakeGroupRepository
//  Singleton, in-memory implementation — no Room wiring needed
//  to see the full flow working. Swap out for a real impl later.
// ─────────────────────────────────────────────────────────────
object FakeGroupRepository : GroupRepository {

    private val _groups = MutableStateFlow<List<GroupModel>>(emptyList())

    override fun observeGroups(): Flow<List<GroupModel>> =
        _groups.asStateFlow().map { it.sortedByDescending { g -> g.createdAt } }

    override suspend fun createGroup(
        name        : String,
        description : String,
        photoUri    : String?,
        members     : List<Contact>,
        permissions : GroupPermissions,
        createdBy   : String,
    ): GroupModel {
        val group = GroupModel(
            groupId          = "grp_${UUID.randomUUID()}",
            groupName        = name.ifBlank { "Group" },
            groupDescription = description,
            groupPhotoUri    = photoUri,
            members          = members,
            createdAt        = System.currentTimeMillis(),
            createdBy        = createdBy,
            permissions      = permissions,
        )
        _groups.value = _groups.value + group
        return group
    }

    override suspend fun getGroup(groupId: String): GroupModel? =
        _groups.value.find { it.groupId == groupId }
}
