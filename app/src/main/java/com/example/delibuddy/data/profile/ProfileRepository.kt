package com.example.delibuddy.data.profile

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val users = firestore.collection("users")

    private fun getProfileRef(userId: String, type: ProfileType) =
        users.document(userId).collection("profiles").document(type.name)

    suspend fun createInitialProfiles(userId: String): Unit = withErrorHandling {
        ProfileType.values().forEach { type ->
            val document = getProfileRef(userId, type)
            document.set(
                ProfileModel(
                    type = type,
                    nickname = RandomNicknameGenerator.generate()
                )
            ).await()
        }
    }

    suspend fun getProfile(userId: String, type: ProfileType): ProfileModel? = withErrorHandling {
        getProfileRef(userId, type).get().await().toObject(ProfileModel::class.java)
    }

    suspend fun update(userId: String, profile: ProfileModel): Unit = withErrorHandling {
        getProfileRef(userId, profile.type).update(
            mapOf(
                "type" to profile.type.name,
                "nickname" to profile.nickname,
                "bio" to profile.bio,
                "imageUrl" to profile.imageUrl
            )
        ).await()
        Unit
    }

    fun observe(userId: String, type: ProfileType): Flow<ProfileModel?> =
        callbackFlow<ProfileModel?> {
            val registration = getProfileRef(userId, type)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    try {
                        trySend(snapshot?.toObject(ProfileModel::class.java))
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            awaitClose { registration.remove() }
        }.catch { e ->
            withErrorHandling { throw e }
        }

    private suspend fun <T> withErrorHandling(action: suspend () -> T): T {
        return try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw when (e) {
                is FirebaseNetworkException -> ProfileNetworkException()
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE,
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> ProfileNetworkException()
                    FirebaseFirestoreException.Code.NOT_FOUND -> ProfileNotFoundException()
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> ProfilePermissionDeniedException()
                    else -> ProfileUnknownException()
                }
                else -> ProfileUnknownException()
            }
        }
    }
}
