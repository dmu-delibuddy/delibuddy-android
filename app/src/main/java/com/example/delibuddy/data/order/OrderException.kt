package com.example.delibuddy.data.order

class InvalidOrderRoomIdException : IllegalArgumentException("방 ID가 필요합니다.")

class InvalidOrderPriceException : IllegalArgumentException("주문 금액은 0 이상이어야 합니다.")

class OrderNotLoggedInException : IllegalStateException("로그인이 필요합니다.")

class InvalidOrderUserIdException : IllegalArgumentException("사용자 ID가 필요합니다.")

class OrderRoomNotFoundException : IllegalStateException("방을 찾을 수 없습니다.")

class OrderNotParticipantException : IllegalStateException("방 참여자만 주문을 변경할 수 있습니다.")

class OrderRoomClosedException : IllegalStateException("모집 마감 후에는 주문을 변경할 수 없습니다.")
