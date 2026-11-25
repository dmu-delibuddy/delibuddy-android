package com.example.delibuddy.data.auth

class SignUpSessionMissingException : IllegalStateException("로그인된 사용자가 없습니다.")

class EmailNotVerifiedException : IllegalStateException("이메일 인증이 완료되지 않았습니다.")
