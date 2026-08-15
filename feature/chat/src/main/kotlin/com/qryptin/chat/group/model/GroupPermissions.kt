package com.qryptin.chat.group.model

// ─────────────────────────────────────────────────────────────
//  GroupPermissions
//  Per-group member permission flags. Admin always has full
//  access; these flags control what regular members can do.
// ─────────────────────────────────────────────────────────────
data class GroupPermissions(
    val canSendMessages        : Boolean = true,
    val canAddMembers          : Boolean = true,
    val canRemoveMembers       : Boolean = false,
    val canChangeGroupInfo     : Boolean = false,
    val canPinMessages         : Boolean = false,
    val canSendMedia           : Boolean = true,
    val canApproveNewMembers   : Boolean = false,
    val blockScreenshot        : Boolean = false,
    val muteNotificationsForAll: Boolean = false,
)
