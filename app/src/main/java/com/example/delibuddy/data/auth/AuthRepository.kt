package com.example.delibuddy.data.auth

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.example.delibuddy.data.user.UserModel
import com.example.delibuddy.data.user.UserRepository
import kotlinx.coroutines.tasks.await

class SignUpSessionMissingException : IllegalStateException()
class EmailNotVerifiedException : IllegalStateException()

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    suspend fun isLoggedIn(): Boolean {
        val user = auth.currentUser ?: return false
        user.reload().await()
        if (!user.isEmailVerified) return false

        // 인증 직후에도 Firestore가 최신 이메일 인증 상태를 확인하도록 토큰 갱신
        user.getIdToken(true).await()
        val hasProfile = userRepository.exists(user.uid)

        return hasProfile && auth.currentUser?.uid == user.uid
    }

    suspend fun sendSignUpVerification(email: String, password: String) {
        val currentUser = auth.currentUser
        if (currentUser?.email.equals(email, ignoreCase = true)) {
            // 계정 생성 후 발송에 실패했거나 재발송하는 경우 비밀번호를 확인하고 재시도
            val user = signIn(email, password).await().user
                ?: error("사용자를 찾을 수 없습니다.")
            user.sendEmailVerification().await()
        } else {
            signUp(email, password).await()
        }
    }

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
            ?: return Tasks.forException(SignUpSessionMissingException())

        return user.reload().continueWithTask { reloadTask ->
            reloadTask.getResult(Exception::class.java)
            if (!user.isEmailVerified) {
                return@continueWithTask Tasks.forException(
                    EmailNotVerifiedException()
                )
            }

            userRepository.create(
                user.uid,
                userData.copy(univEmail = user.email ?: userData.univEmail)
            )
        }
    }
}
