package com.example.delibuddy.data.chat

import com.example.delibuddy.data.room.RoomStatus

// 채팅방에 자동으로 남기는 안내 문구 (DB 에 그대로 저장됨)
object SystemMessage {
    const val ROOM_CREATED = "방이 만들어졌어요"
    const val JOINED = "새 참여자가 들어왔어요"
    const val LEFT = "참여자 한 명이 나갔어요"
    const val ORDER_CONFIRMED = "주문이 확정됐어요. 이제 메뉴를 바꿀 수 없어요."
    const val ORDERED = "방장이 배달 앱에서 주문했어요"
    const val RECEIVED = "음식이 도착했어요. 나눠 가져가요"
    const val COMPLETED = "공동 주문이 완료됐어요"

    fun forStatus(status: RoomStatus) : String? =
        when (status) {
            RoomStatus.ORDER_CONFIRMED -> ORDER_CONFIRMED
            RoomStatus.ORDERED -> ORDERED
            RoomStatus.RECEIVED -> RECEIVED
            RoomStatus.COMPLETED -> COMPLETED
            RoomStatus.RECRUITING -> null
        }
}
