package com.example.delibuddy.data.auth

class SignUpSessionMissingException : IllegalStateException("로그인된 사용자가 없습니다.")

class EmailNotVerifiedException : IllegalStateException("이메일 인증이 완료되지 않았습니다.")

class AuthInvalidCredentialsException : IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")

class AuthInvalidUserException : IllegalStateException("계정이 없거나 사용할 수 없는 상태입니다.")

class AuthNetworkException : IllegalStateException("인증 서버에 연결하지 못했습니다.")

class AuthUnknownException : IllegalStateException("인증 처리 중 오류가 발생했습니다.")
