package com.example.delibuddy.data.chat

// 채팅방에 자동으로 남기는 안내 문구 (DB 에 그대로 저장됨)
object SystemMessage {
    const val ROOM_CREATED = "방이 만들어졌어요"
    const val JOINED = "새 참여자가 들어왔어요"
    const val LEFT = "참여자 한 명이 나갔어요"
    const val CLOSED = "모집이 마감됐어요. 주문을 준비해 주세요"
    const val COMPLETED = "공동 주문이 완료됐어요"
}
