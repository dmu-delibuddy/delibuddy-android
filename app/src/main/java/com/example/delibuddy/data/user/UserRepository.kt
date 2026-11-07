package com.example.delibuddy.data.user

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    suspend fun exists(userId: String): Boolean =
        users.document(userId).get(Source.SERVER).await().exists()

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

    // 생년월일만 수정 (다른 필드는 건드리지 않음)
    fun updateBirthDate(userId: String, birthDate: String?): Task<Void> =
        users.document(userId).update("birthDate", birthDate)

    // 송금 계좌(은행, 계좌번호)만 수정 (다른 필드는 건드리지 않음)
    fun updateAccount(userId: String, bankName: String, account: String): Task<Void> =
        users.document(userId).update(
            mapOf(
                "bankName" to bankName,
                "account" to account,
            )
        )

    fun delete(userId: String): Task<Void> =
        users.document(userId).delete()
}
