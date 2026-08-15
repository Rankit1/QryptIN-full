package com.qryptin.chat.group.repository

import com.qryptin.chat.group.model.GroupModel
import com.qryptin.chat.group.model.GroupPermissions
import com.qryptin.contacts.model.Contact
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────
//  GroupRepository
//  Interface-only contract. Today: FakeGroupRepository (in-
//  memory). Future: Room + remote backend — no ViewModel changes.
// ─────────────────────────────────────────────────────────────
interface GroupRepository {

    /** All created groups, newest first. */
    fun observeGroups(): Flow<List<GroupModel>>

    /** Create a group, persist it locally, return the created model. */
    suspend fun createGroup(
        name        : String,
        description : String,
        photoUri    : String?,
        members     : List<Contact>,
        permissions : GroupPermissions,
        createdBy   : String,
    ): GroupModel

    /** Fetch a single group by id. */
    suspend fun getGroup(groupId: String): GroupModel?
}
