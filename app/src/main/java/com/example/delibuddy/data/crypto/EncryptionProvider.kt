package com.example.delibuddy.data.crypto

object EncryptionProvider {
    private const val ENCRYPTION_KEY = "8f27c1a903de64b5f0c87a1269b3de4051ac76e2d9480fb357a612ce8b904fd3"

    val instance = AesGcmEncryption(ENCRYPTION_KEY)
}
