package com.qryptin.calls.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calls",
    indices   = [Index("timestamp"), Index("contactId")],
)
data class CallEntity(
    @PrimaryKey
    val callId        : String,
    val contactId     : String,
    val contactName   : String,
    val contactAvatar : String? = null,
    val contactPhone  : String  = "",
    /** Matches CallType enum name: VOICE | VIDEO */
    val callType      : String,
    /** Matches CallDirection enum name: OUTGOING | INCOMING | MISSED */
    val direction     : String,
    val timestamp     : Long,
    val durationSecs  : Int     = 0,
    val isEncrypted   : Boolean = true,
)
