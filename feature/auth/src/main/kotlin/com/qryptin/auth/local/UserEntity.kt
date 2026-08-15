package com.qryptin.auth.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────
//  UserEntity
//
//  Single source of truth for a QryptIN user profile, stored
//  locally today (Room) but shaped so a future sync layer can
//  map this 1:1 onto a "users" row in the sovereign backend
//  (PostgreSQL) without changing callers — see UserRepository.
// ─────────────────────────────────────────────────────────────
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["phoneNumber"], unique = true),
        Index(value = ["qryptinId"], unique = true),
    ],
)
data class UserEntity(
    @PrimaryKey
    val userId          : String,

    /** Normalized, lowercase, no-space username. e.g. "debasmita" */
    val qryptinId        : String,

    val fullName         : String,

    /** Full E.164-ish string, e.g. "+9198765XXXXX" — includes dial code. */
    val phoneNumber       : String,

    val email             : String? = null,

    val bio                : String = "",

    /** Local content:// or file:// URI. Null = no photo set (show initials). */
    val profilePhotoUri    : String? = null,

    val createdAt          : Long,
    val updatedAt          : Long,

    /** True once registration (name/bio/qryptinId) has been completed. */
    val isRegistered       : Boolean = false,

    /** True once this user has completed OTP verification at least once. */
    val isVerified          : Boolean = false,
)
