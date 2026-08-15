package com.qryptin.auth.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────
//  SessionEntity
//
//  One row per login session. Only one row should have
//  isActive = true at a time — that is the "currently logged in"
//  session. Kept as full rows (rather than overwritten in place)
//  so session history is preserved, which the future cloud
//  auth service can use for device/session management
//  ("log out of all other devices", audit trail, etc).
// ─────────────────────────────────────────────────────────────
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["userId"],
            childColumns  = ["userId"],
            onDelete      = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("userId"), Index("isActive")],
)
data class SessionEntity(
    @PrimaryKey
    val sessionId    : String,

    val userId        : String,

    val phoneNumber    : String,

    /**
     * Mock local session token today. Future cloud auth service
     * replaces this with a signed JWT / opaque server token —
     * everything reading `token` goes through SessionRepository,
     * so that swap never touches UI or ViewModel code.
     */
    val token           : String,

    val createdAt        : Long,

    val isActive          : Boolean = true,
)
