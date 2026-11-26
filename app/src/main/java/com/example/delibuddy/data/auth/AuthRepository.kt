package com.example.delibuddy.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.delibuddy.data.profile.ProfileRepository
import com.example.delibuddy.data.user.UserModel
import com.example.delibuddy.data.user.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    private val profileRepository = ProfileRepository()

    suspend fun isLoggedIn(): Boolean = withErrorHandling {
        val user = auth.currentUser ?: return@withErrorHandling false
        user.reload().await()
        if (!user.isEmailVerified) return@withErrorHandling false

        user.getIdToken(true).await()
        val hasUserDocument = userRepository.exists(user.uid)

        hasUserDocument && auth.currentUser?.uid == user.uid
    }

    suspend fun signIn(email: String, password: String): Unit = withErrorHandling {
        auth.signInWithEmailAndPassword(email, password).await()
        val user = requireVerifiedUser()
        if (!userRepository.exists(user.uid)) throw SignUpIncompleteException()
        if (auth.currentUser?.uid != user.uid) throw SignUpSessionMissingException()
    }

    suspend fun createFirebaseUser(email: String, password: String): FirebaseUser {
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

    private suspend fun <T> withErrorHandling(action: suspend () -> T): T {
        return try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw when (e) {
                is SignUpSessionMissingException,
                is EmailNotVerifiedException,
                is SignUpIncompleteException -> e
                is FirebaseAuthInvalidCredentialsException -> AuthInvalidCredentialsException()
                is FirebaseAuthInvalidUserException -> AuthInvalidUserException()
                is FirebaseNetworkException -> AuthNetworkException()
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> AuthNetworkException()
                    else -> AuthUnknownException()
                }
                else -> AuthUnknownException()
            }
        }
    }
}
