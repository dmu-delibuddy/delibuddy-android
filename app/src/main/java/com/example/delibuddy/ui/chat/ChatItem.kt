package com.example.delibuddy.ui.chat

import com.google.firebase.Timestamp

// 전송 상태 : 저장 완료 / 보내는 중 / 실패
enum class SendState { SENT, SENDING, FAILED }

/*
    * 화면에 보여줄 메시지 한 칸
    * 서버 메시지와 아직 저장 안 된 내 메시지를 같은 모양으로 다룸
*/
sealed interface ChatItem {
    val id: String

    // 가운데 안내 문구 (참여, 마감 등)
    data class System(
        override val id: String,
        val content: String,
    ) : ChatItem

    // 말풍선 (나 / 상대)
    data class Bubble(
        override val id: String,
        val isMine: Boolean,
        val isFromHost: Boolean,   // 상대 이름 "방장" 표시용
        val content: String?,
        val createdAt: Timestamp?,
        val sendState: SendState,
        val deleted: Boolean,      // deletedAt 이 있으면 true
    ) : ChatItem
}