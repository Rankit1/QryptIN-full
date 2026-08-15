package com.qryptin.chat.group.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qryptin.contacts.model.Contact

// ─────────────────────────────────────────────────────────────
//  GroupModel  — pure domain object used inside the UI layer
// ─────────────────────────────────────────────────────────────
data class GroupModel(
    val groupId         : String,
    val groupName       : String,
    val groupDescription: String       = "",
    val groupPhotoUri   : String?      = null,
    val members         : List<Contact> = emptyList(),
    val createdAt       : Long,
    val createdBy       : String,          // userId of creator
    val permissions     : GroupPermissions = GroupPermissions(),
    val lastMessage     : String?      = null,
    val unreadCount     : Int          = 0,
)

// ─────────────────────────────────────────────────────────────
//  GroupEntity  — Room table row
//  Members and permissions are JSON-serialised strings; a real
//  app would use TypeConverters — kept simple here so the module
//  compiles without a third-party serialisation dependency.
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val groupId          : String,
    val groupName        : String,
    val groupDescription : String  = "",
    val groupPhotoUri    : String? = null,
    val memberIdsJson    : String  = "[]",   // serialised List<String>
    val createdAt        : Long,
    val createdBy        : String,
    val lastMessage      : String? = null,
    val unreadCount      : Int     = 0,
    // permissions stored as individual columns for easy queries
    val permCanSendMessages        : Boolean = true,
    val permCanAddMembers          : Boolean = true,
    val permCanRemoveMembers       : Boolean = false,
    val permCanChangeGroupInfo     : Boolean = false,
    val permCanPinMessages         : Boolean = false,
    val permCanSendMedia           : Boolean = true,
    val permCanApproveNewMembers   : Boolean = false,
    val permBlockScreenshot        : Boolean = false,
    val permMuteNotificationsForAll: Boolean = false,
)
