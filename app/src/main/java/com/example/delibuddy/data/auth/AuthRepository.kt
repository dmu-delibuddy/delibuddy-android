package com.example.delibuddy.data.auth

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.user.UserModel
import com.example.delibuddy.data.user.UserRepository
import kotlinx.coroutines.tasks.await

class SignUpSessionMissingException : IllegalStateException("로그인된 사용자가 없습니다.")
class EmailNotVerifiedException : IllegalStateException("이메일 인증이 완료되지 않았습니다.")

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    private val profileRepository = ProfileRepository()

    suspend fun isLoggedIn(): Boolean {
        val user = auth.currentUser ?: return false
        user.reload().await()
        if (!user.isEmailVerified) return false

        user.getIdToken(true).await()
        val hasUserDocument = userRepository.exists(user.uid)

        return hasUserDocument && auth.currentUser?.uid == user.uid
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String): FirebaseUser {
        return auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("회원가입 사용자를 찾을 수 없습니다.")
    }

    suspend fun sendVerificationEmail(user: FirebaseUser) {
        user.sendEmailVerification().await()
    }

    suspend fun createUserDocument() {
        val user = requireVerifiedUser()
        userRepository.create(
            user.uid,
            UserModel(univEmail = user.email.orEmpty(), birthDate = null),
        )
        profileRepository.createInitialProfiles(user.uid)
    }

    private suspend fun requireVerifiedUser(): FirebaseUser {
        val user = auth.currentUser ?: throw SignUpSessionMissingException()
        user.reload().await()
        if (!user.isEmailVerified) throw EmailNotVerifiedException()
        user.getIdToken(true).await()
        return user
    }
}
