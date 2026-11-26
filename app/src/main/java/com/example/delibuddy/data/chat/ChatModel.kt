package com.example.delibuddy.data.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

enum class MessageType { NORMAL, SYSTEM }

data class SharedAccount(
    val bankName: String = "",
    val number: String = "",
)
data class ChatModel(
    @DocumentId val id: String = "",
    val senderId: String? = null,
    val type: MessageType = MessageType.NORMAL,
    val content: String? = null,
    val imageId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
    val sharedAccount: SharedAccount? = null,
)
