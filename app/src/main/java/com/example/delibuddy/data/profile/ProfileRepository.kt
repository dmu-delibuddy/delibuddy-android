package com.example.delibuddy.data.profile

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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

    // 모드별 프로필 실시간 구독 (마이페이지). 문서가 없으면 null
    fun observe(userId: String, type: ProfileType): Flow<ProfileModel?> = callbackFlow {
        val registration = profileDocument(userId, type)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(ProfileModel::class.java))
            }
        awaitClose { registration.remove() }
    }

    // 모드별 프로필 저장. 문서가 없으면 새로 만듦
    // (#28 이전에 가입해 프로필 문서가 없는 계정도 저장되도록 update 대신 set)
    suspend fun save(userId: String, profile: ProfileModel) {
        profileDocument(userId, profile.type).set(profile).await()
    }

}
