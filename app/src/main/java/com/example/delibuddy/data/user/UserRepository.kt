package com.example.delibuddy.data.user

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    fun create(userId: String, user: UserModel): Task<Void> =
        users.document(userId).set(user)

    fun getById(userId: String): Task<UserModel?> =
        users.document(userId).get().continueWith { task ->
            task.result?.toObject(UserModel::class.java)
        }

    fun update(userId: String, user: UserModel): Task<Void> =
        users.document(userId).update(
            mapOf(
                "univEmail" to user.univEmail,
                "birthDate" to user.birthDate,
                "bankName" to user.bankName,
                "account" to user.account
            )
        )

    fun delete(userId: String): Task<Void> =
        users.document(userId).delete()
}
