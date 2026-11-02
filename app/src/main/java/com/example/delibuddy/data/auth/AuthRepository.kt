package com.example.delibuddy.data.auth

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.example.delibuddy.data.user.UserModel
import com.example.delibuddy.data.user.UserRepository

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun signIn(email: String, password: String): Task<AuthResult> =
        auth.signInWithEmailAndPassword(email, password)

    fun signUp(email: String, password: String): Task<AuthResult> =
        auth.createUserWithEmailAndPassword(email, password)
            .continueWithTask { createTask ->
                val result = createTask.getResult(Exception::class.java)
                val user = result.user
                    ?: throw IllegalStateException("회원가입 사용자를 찾을 수 없습니다.")

                user.sendEmailVerification().continueWith { verificationTask ->
                    verificationTask.getResult(Exception::class.java)
                    result
                }
            }

    fun completeSignUp(userData: UserModel): Task<Void> {
        val user = auth.currentUser
            ?: return Tasks.forException(IllegalStateException("로그인된 사용자가 없습니다."))

        return user.reload().continueWithTask { reloadTask ->
            reloadTask.getResult(Exception::class.java)
            if (!user.isEmailVerified) {
                return@continueWithTask Tasks.forException(
                    IllegalStateException("학교 이메일 인증을 먼저 완료해주세요.")
                )
            }

            userRepository.create(
                user.uid,
                userData.copy(univEmail = user.email ?: userData.univEmail)
            )
        }
    }
}
