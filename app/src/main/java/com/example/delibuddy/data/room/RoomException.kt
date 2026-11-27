package com.example.delibuddy.data.room

class RoomNotLoggedInException : IllegalStateException("로그인이 필요합니다.")

class RoomNotFoundException : IllegalStateException("방을 찾을 수 없습니다.")

class RoomClosedException : IllegalStateException("모집이 마감된 방입니다.")

class RoomFullException : IllegalStateException("정원이 가득 찼습니다.")

class RoomAlreadyJoinedException : IllegalStateException("이미 참여 중인 방입니다.")

class RoomNotHostException : IllegalStateException("방장만 할 수 있습니다.")

class RoomCannotLeaveInProgressException : IllegalStateException("모집 중이거나 완료된 방에서만 나갈 수 있습니다.")

class RoomInvalidStateException : IllegalStateException("지금 상태에서는 할 수 없습니다.")

class RoomAccountRequiredException : IllegalStateException("송금 계좌를 등록해야 참여할 수 있어요. 마이페이지에서 등록해 주세요.")

class RoomNetworkException : IllegalStateException("방 서버에 연결하지 못했습니다.")

class RoomPermissionDeniedException : IllegalStateException("방에 접근하거나 변경할 권한이 없습니다.")

class RoomUnknownException : IllegalStateException("방 처리 중 오류가 발생했습니다.")
