package com.example.delibuddy.data.room

class RoomException(val reason: Reason) : Exception(reason.message) {
    enum class Reason(val message: String) {
        NOT_LOGGED_IN("로그인이 필요합니다."),
        NOT_FOUND("방을 찾을 수 없습니다."),
        CLOSED("모집이 마감된 방입니다."),
        FULL("정원이 가득 찼습니다."),
        ALREADY_JOINED("이미 참여 중인 방입니다."),
        NOT_HOST("방장만 할 수 있습니다."),
        HOST_CANNOT_LEAVE("방장은 참여를 취소할 수 없습니다."),
        INVALID_STATE("지금 상태에서는 할 수 없습니다."),
    }
}