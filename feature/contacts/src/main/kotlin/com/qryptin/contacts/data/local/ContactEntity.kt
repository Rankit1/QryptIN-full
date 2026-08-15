package com.qryptin.contacts.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────
//  ContactEntity
//  Room table backing locally-saved contacts. This is the
//  permanent, on-disk record of every contact the user has
//  saved — it survives app restarts, unlike the previous
//  in-memory MockContacts list.
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "contacts")
data class ContactEntity(

    @PrimaryKey(autoGenerate = true)
    val id               : Long    = 0L,

    /** The name actually shown in the contacts list (nickname if set, else the registered/typed name). */
    val displayName      : String,

    /**
     * The name returned by the QryptIN global registry at the time this
     * contact was saved. Null for non-QryptIN contacts. This value is
     * never modified locally — it represents the immutable registry name.
     */
    val originalQryptName: String?,

    /**
     * A local-only rename chosen by the user (e.g. "Maa", "Office").
     * Never written back to the global registry. Null when the user
     * kept the default / registered name.
     */
    val nickname         : String?,

    /** Full phone number including country code, e.g. "+91 9876543210". */
    val phoneNumber      : String,

    /** Optional email address associated with this contact. */
    val email            : String?,

    /** True when this number was found in the QryptIN global registry. */
    val isQryptUser      : Boolean,

    /** Either "SYNCED_WITH_SIM" or "QRYPTIN_ONLY" — mirrors ContactSaveOption.name. */
    val saveMode         : String,

    /** Either "SMS", "EMAIL", or null when no invite was needed (existing QryptIN user). */
    val inviteMethod     : String?,

    /** Epoch millis when the contact was first saved. */
    val createdAt        : Long,
)
