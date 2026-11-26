package com.example.delibuddy.data.profile

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val users = firestore.collection("users")

    private fun getProfileRef(userId: String, type: ProfileType) =
        users.document(userId).collection("profiles").document(type.name)

    suspend fun createInitialProfiles(userId: String) {
        val initialProfiles = ProfileType.values().map { type ->
            getProfileRef(userId, type) to ProfileModel(
                type = type,
                nickname = RandomNicknameGenerator.generate()
            )
        }

        firestore.runTransaction<Void> { transaction ->
            val snapshots = initialProfiles.map { (document, _) ->
                transaction.get(document)
            }
            initialProfiles.forEachIndexed { index, (document, profile) ->
                if (!snapshots[index].exists()) {
                    transaction.set(document, profile)
                }
            }
            null
        }.await()
    }

    suspend fun getProfile(userId: String, type: ProfileType): ProfileModel? =
        getProfileRef(userId, type).get().await().toObject(ProfileModel::class.java)

    suspend fun update(userId: String, profile: ProfileModel) {
        getProfileRef(userId, profile.type).update(
            mapOf(
                "type" to profile.type.name,
                "nickname" to profile.nickname,
                "bio" to profile.bio,
                "imageUrl" to profile.imageUrl
            )
        ).await()
    }

    fun observe(userId: String, type: ProfileType): Flow<ProfileModel?> = callbackFlow {
        val registration = getProfileRef(userId, type)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(ProfileModel::class.java))
            }
        awaitClose { registration.remove() }
    }
}
