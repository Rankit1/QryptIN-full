package com.qryptin.chat.data.repository

import android.content.Context
import com.qryptin.chat.data.local.ChatDatabase
import com.qryptin.chat.data.local.ConversationEntity
import com.qryptin.chat.group.model.GroupEntity
import com.qryptin.chat.group.model.GroupModel
import com.qryptin.chat.group.model.GroupPermissions
import com.qryptin.chat.group.repository.GroupRepository
import com.qryptin.chat.model.ChatType
import com.qryptin.contacts.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  RoomGroupRepository
//  Room-backed implementation of GroupRepository.
//  Groups persist across restarts — no in-memory fakes.
//
//  When a group is created it is also written as a
//  ConversationEntity so that the chat home list reflects it
//  immediately without any additional wiring in the ViewModel.
// ─────────────────────────────────────────────────────────────
class RoomGroupRepository(context: Context) : GroupRepository {

    private val db              = ChatDatabase.getInstance(context)
    private val groupDao        = db.groupDao()
    private val conversationDao = db.conversationDao()

    override fun observeGroups(): Flow<List<GroupModel>> =
        groupDao.observeAll().map { entities -> entities.map { it.toModel() } }

    override suspend fun createGroup(
        name        : String,
        description : String,
        photoUri    : String?,
        members     : List<Contact>,
        permissions : GroupPermissions,
        createdBy   : String,
    ): GroupModel {
        val groupId    = "grp_${UUID.randomUUID()}"
        val memberJson = members.joinToString(",", "[", "]") { "\"${it.id}\"" }
        val now        = System.currentTimeMillis()

        // 1. Persist group metadata
        val entity = GroupEntity(
            groupId                  = groupId,
            groupName                = name.ifBlank { "Group" },
            groupDescription         = description,
            groupPhotoUri            = photoUri,
            memberIdsJson            = memberJson,
            createdAt                = now,
            createdBy                = createdBy,
            permCanSendMessages      = permissions.canSendMessages,
            permCanAddMembers        = permissions.canAddMembers,
            permCanRemoveMembers     = permissions.canRemoveMembers,
            permCanChangeGroupInfo   = permissions.canChangeGroupInfo,
            permCanPinMessages       = permissions.canPinMessages,
            permCanSendMedia         = permissions.canSendMedia,
            permCanApproveNewMembers = permissions.canApproveNewMembers,
            permBlockScreenshot      = permissions.blockScreenshot,
            permMuteNotificationsForAll = permissions.muteNotificationsForAll,
        )
        groupDao.insertGroup(entity)

        // 2. Also create a ConversationEntity so chat home shows the group
        val conversationEntity = ConversationEntity(
            id                 = groupId,                   // same ID links both tables
            type               = ChatType.GROUP.name,
            title              = entity.groupName,
            avatarUrl          = photoUri,
            participantIdsJson = memberJson,
            createdAt          = now,
        )
        conversationDao.insert(conversationEntity)

        return entity.toModel(members)
    }

    override suspend fun getGroup(groupId: String): GroupModel? =
        groupDao.getById(groupId)?.toModel()

    // ── Mapper ────────────────────────────────────────────────

    private fun GroupEntity.toModel(resolvedMembers: List<Contact> = emptyList()) = GroupModel(
        groupId          = groupId,
        groupName        = groupName,
        groupDescription = groupDescription,
        groupPhotoUri    = groupPhotoUri,
        members          = resolvedMembers,
        createdAt        = createdAt,
        createdBy        = createdBy,
        permissions      = GroupPermissions(
            canSendMessages         = permCanSendMessages,
            canAddMembers           = permCanAddMembers,
            canRemoveMembers        = permCanRemoveMembers,
            canChangeGroupInfo      = permCanChangeGroupInfo,
            canPinMessages          = permCanPinMessages,
            canSendMedia            = permCanSendMedia,
            canApproveNewMembers    = permCanApproveNewMembers,
            blockScreenshot        = permBlockScreenshot,
            muteNotificationsForAll= permMuteNotificationsForAll,
        ),
        lastMessage  = lastMessage,
        unreadCount  = unreadCount,
    )
}
