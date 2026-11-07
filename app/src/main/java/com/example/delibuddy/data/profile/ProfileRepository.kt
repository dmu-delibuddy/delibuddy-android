package com.example.delibuddy.data.profile

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val users = firestore.collection("users")

    private fun profileDocument(userId: String, type: ProfileType) =
        users.document(userId).collection("profiles").document(type.name)

    fun createInitialProfiles(userId: String): Task<Void> {
        val initialProfiles = ProfileType.values().map { type ->
            profileDocument(userId, type) to ProfileModel(
                type = type,
                nickname = RandomNicknameGenerator.generate()
            )
        }

        return firestore.runTransaction<Void> { transaction ->
            val snapshots = initialProfiles.map { (document, _) ->
                transaction.get(document)
            }
            initialProfiles.forEachIndexed { index, (document, profile) ->
                if (!snapshots[index].exists()) {
                    transaction.set(document, profile)
                }
            }
            null
        }
    }

    fun getById(userId: String, type: ProfileType): Task<ProfileModel?> =
        profileDocument(userId, type).get().continueWith { task ->
            task.getResult(Exception::class.java).toObject(ProfileModel::class.java)
        }

    fun update(userId: String, profile: ProfileModel): Task<Void> =
        profileDocument(userId, profile.type).update(
            mapOf(
                "type" to profile.type.name,
                "nickname" to profile.nickname,
                "bio" to profile.bio,
                "imageUrl" to profile.imageUrl
            )
        )

}
