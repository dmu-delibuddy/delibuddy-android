package com.example.delibuddy.data.profile

class ProfileNetworkException : IllegalStateException("프로필 서버에 연결하지 못했습니다.")

class ProfileNotFoundException : IllegalStateException("프로필을 찾을 수 없습니다.")

class ProfilePermissionDeniedException : IllegalStateException("프로필에 접근할 권한이 없습니다.")

class ProfileUnknownException : IllegalStateException("프로필 처리 중 오류가 발생했습니다.")
