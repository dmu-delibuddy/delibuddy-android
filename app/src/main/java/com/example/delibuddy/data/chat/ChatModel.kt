package com.example.delibuddy.data.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// 메시지 종류 : 일반 / 시스템 (DB 에는 영어 이름으로 저장)
enum class MessageType { NORMAL, SYSTEM }

// 첨부 이미지 한 개. 파일은 Storage 의 storagePath 에 있음
data class Attachment(
    val storagePath: String = "",
    val contentType: String = "",
    val width: Int? = null,
    val height: Int? = null,
)

/*
    * 채팅 메시지 한 개
    * 위치 : rooms/{roomId}/messages/{messageId}
*/
data class ChatModel(
    @DocumentId val id: String = "",
    val senderId: String? = null,     // 시스템 메시지는 null
    val type: MessageType = MessageType.NORMAL,
    val content: String? = null,      // 이미지만 보낼 때는 null
    val attachments: List<Attachment> = emptyList(),
    // 서버에 저장되는 순간의 시각 (보내는 중엔 null)
    @ServerTimestamp val createdAt: Timestamp? = null,
    val deletedAt: Timestamp? = null, // 삭제된 메시지면 값 있음
)
