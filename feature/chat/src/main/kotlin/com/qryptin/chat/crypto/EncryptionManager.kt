package com.qryptin.chat.crypto

// ─────────────────────────────────────────────────────────────
//  Encryption-ready placeholders
//
//  No cryptography is implemented here. These interfaces exist so
//  the rest of the chat module (Message model, ChatRepository) is
//  already shaped around an encrypted payload, and a real
//  post-quantum implementation (Kyber key exchange + Dilithium
//  signatures) can be dropped in later without reshaping the
//  message pipeline.
// ─────────────────────────────────────────────────────────────

/**
 * Opaque encrypted envelope that would wrap a [com.qryptin.chat.model.Message]'s
 * plaintext on the wire. [ciphertext] / [signature] are placeholders — currently
 * the plaintext is stored as-is and this payload is unused by RoomChatRepository.
 */
data class SecureMessagePayload(
    val ciphertext     : ByteArray,
    val nonce          : ByteArray,
    val signature      : ByteArray?,
    val senderPublicKey: ByteArray?,
) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

/** A Kyber-style key-encapsulation key pair, scoped to one chat participant. */
data class KeyPair(
    val publicKey  : ByteArray,
    val privateKey : ByteArray,
)

/**
 * Future home for Kyber key exchange. [KeyExchangeRepository] resolves and
 * caches per-chat session keys; no implementation backs this yet.
 */
interface KeyExchangeRepository {
    suspend fun getOrCreateKeyPair(userId: String): KeyPair
    suspend fun negotiateSessionKey(chatId: String, peerPublicKey: ByteArray): ByteArray
}

/**
 * Future home for message-level encrypt/decrypt + Dilithium signing.
 * [NoOpEncryptionManager] is the only implementation today and passes
 * plaintext through untouched, so UI/repository code can already call
 * through this seam.
 */
interface EncryptionManager {
    suspend fun encrypt(plaintext: String, sessionKey: ByteArray): SecureMessagePayload
    suspend fun decrypt(payload: SecureMessagePayload, sessionKey: ByteArray): String
    suspend fun sign(payload: SecureMessagePayload, privateKey: ByteArray): ByteArray
    suspend fun verify(payload: SecureMessagePayload, signature: ByteArray, publicKey: ByteArray): Boolean
}

/** Pass-through implementation used until real Kyber/Dilithium crypto lands. */
class NoOpEncryptionManager : EncryptionManager {
    override suspend fun encrypt(plaintext: String, sessionKey: ByteArray): SecureMessagePayload =
        SecureMessagePayload(
            ciphertext      = plaintext.toByteArray(),
            nonce           = ByteArray(0),
            signature       = null,
            senderPublicKey = null,
        )

    override suspend fun decrypt(payload: SecureMessagePayload, sessionKey: ByteArray): String =
        String(payload.ciphertext)

    override suspend fun sign(payload: SecureMessagePayload, privateKey: ByteArray): ByteArray = ByteArray(0)

    override suspend fun verify(payload: SecureMessagePayload, signature: ByteArray, publicKey: ByteArray): Boolean = true
}
