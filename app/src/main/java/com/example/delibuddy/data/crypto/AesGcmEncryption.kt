package com.example.delibuddy.data.crypto

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesGcmEncryption(keyHex: String) {
    companion object {
        private const val ENCRYPTED_PREFIX = "enc:v1:"
        private const val IV_SIZE = 12
        private const val TAG_BITS = 128
    }

    private val secretKey = SecretKeySpec(decodeHex(keyHex), "AES")

    fun encrypt(value: String?): String? {
        if (value == null) return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return ENCRYPTED_PREFIX + encodeHex(cipher.iv + encrypted)
    }

    fun decrypt(value: String?): String? {
        if (value == null || !value.startsWith(ENCRYPTED_PREFIX)) return value
        val payload = decodeHex(value.removePrefix(ENCRYPTED_PREFIX))
        require(payload.size >= IV_SIZE + TAG_BITS / 8) { "Invalid encrypted user data" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(TAG_BITS, payload.copyOfRange(0, IV_SIZE))
        )
        return String(cipher.doFinal(payload.copyOfRange(IV_SIZE, payload.size)), Charsets.UTF_8)
    }

    private fun encodeHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun decodeHex(value: String): ByteArray {
        require(value.length % 2 == 0) { "Invalid encrypted user data" }
        return ByteArray(value.length / 2) { index ->
            value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
